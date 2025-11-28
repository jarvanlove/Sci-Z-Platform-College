<!--
/**
 * @description 申报详情页面组件
 * 展示申报项目的详细信息，包括基本信息、研究信息、工作流状态等
 */
-->
<template>
  <div class="declaration-detail-container">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <BackButton @click="handleBack" />
      <h1 class="page-title">{{ $t('declaration.detailTitle') }}</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>{{ $t('common.loading') }}</span>
    </div>

    <!-- 详情内容 -->
    <div v-else-if="declaration.id" class="detail-content">
      <!-- 基本信息卡片 -->
      <InfoCard
        :title="$t('declaration.basicInfo')"
        :info-items="basicInfoItems"
        layout="grid"
        :grid-columns="3"
        custom-class="detail-card basic-info-card"
      />

      <!-- 研究信息卡片 -->
      <BaseCard class="detail-card">
        <template #header>
          <div class="card-title">{{ $t('declaration.researchInfo') }}</div>
        </template>
        <div class="research-info-content">
          <div class="info-item">
            <div class="info-label">{{ $t('declaration.direction') }}</div>
            <div class="research-direction">{{ declaration.researchDirection || '-' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">{{ $t('declaration.topic') }}</div>
            <div class="research-direction">{{ declaration.researchTopic || '-' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">{{ $t('declaration.fields') }}</div>
            <div class="field-tags">
              <el-tag
                v-for="field in researchFields"
                :key="field"
                class="field-tag"
                size="large"
              >
                {{ field }}
              </el-tag>
              <span v-if="researchFields.length === 0" class="no-field">{{ $t('declaration.noFields') }}</span>
            </div>
          </div>
        </div>
      </BaseCard>

      <!-- 工作流状态卡片 -->
      <BaseCard class="detail-card">
        <template #header>
          <div class="card-title">{{ $t('declaration.workflowStatus') }}</div>
        </template>
        <div class="workflow-container">
          <!-- 工作流状态 -->
          <div class="workflow-status">
            <div class="workflow-icon" :class="workflowStatus.type">
              {{ workflowStatus.icon }}
            </div>
            <div class="workflow-info">
              <div class="workflow-title">{{ workflowStatus.title }}</div>
              <div class="workflow-description">
                {{ workflowStatus.description }}
              </div>
              <!-- 进度条 - 始终显示，根据步骤计算进度（包括失败状态） -->
              <!-- 只要有工作流步骤数据就显示进度条，无论成功还是失败 -->
              <ProgressBar
                v-if="workflowTimeline && workflowTimeline.length > 0"
                :percentage="Number(calculateProgress)"
                :status="progressStatus"
                :status-map="{
                  success: { text: '已完成', type: 'success' },
                  warning: { text: '处理中', type: 'warning' },
                  danger: { text: '处理失败', type: 'danger' },
                  info: { text: '处理中', type: 'info' }
                }"
                size="default"
                :animated="workflowStatus.type === 'processing' || workflowStatus.type === 'running'"
                class="workflow-progress"
              />
            </div>
          </div>

          <!-- 时间线 - 始终显示，即使为空也显示空状态 -->
          <Timeline
            :items="workflowTimeline"
            direction="vertical"
            class="workflow-timeline"
          />

        </div>
      </BaseCard>

    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty :description="$t('declaration.detailNotFound')" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElIcon } from 'element-plus'
import {
  Loading,
  Check,
  Close
} from '@element-plus/icons-vue'
import { BaseCard, BackButton } from '@/components/Common'
import { InfoCard, Timeline, ProgressBar } from '@/components/Business/Detail'
import { getDeclarationDetail, getDeclarationWorkflowStatus } from '@/api/Declaration/declaration'
import { createLogger } from '@/utils/simpleLogger'
import { DECLARATION_STATUS_CONFIG } from '@/utils/constants'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const logger = createLogger('DeclarationDetail')

// 状态数字到字符串的映射（与列表页保持一致）
const STATUS_MAP = {
  1: { type: 'submitting', label: '申报中' },
  2: { type: 'success', label: '申报成功' },
  3: { type: 'failed', label: '申报失败' }
}

// 申报数据
const declaration = ref({})
const loading = ref(false)

// 工作流时间线
const workflowTimeline = ref([])

// 工作流结果
const workflowResult = ref(null)

// 工作流状态轮询定时器
let workflowPollingTimer = null
// 轮询间隔（4秒，在3-5秒之间）
const POLLING_INTERVAL = 4000

// 计算研究领域列表
const researchFields = computed(() => {
  const fields = declaration.value.researchFields
  if (!fields) return []
  if (typeof fields === 'string') {
    return fields.split(/[、，,]/).map(f => f.trim()).filter(f => f)
  }
  if (Array.isArray(fields)) {
    return fields.filter(f => f)
  }
  return []
})

// 基本信息项
const basicInfoItems = computed(() => {
  return [
    {
      key: 'number',
      label: t('declaration.number'),
      value: declaration.value.declarationNumber || '-',
      type: 'text'
    },
    {
      key: 'applicant',
      label: t('declaration.applicant'),
      value: declaration.value.applicantName || '-',
      type: 'text'
    },
    {
      key: 'department',
      label: t('declaration.department'),
      value: declaration.value.departmentName || '-',
      type: 'text'
    },
    {
      key: 'projectLeader',
      label: t('declaration.projectLeader'),
      value: declaration.value.projectLeader || '-',
      type: 'text'
    },
    {
      key: 'documentPublishTime',
      label: t('declaration.documentPublishTime'),
      value: declaration.value.documentPublishTime || '-',
      type: 'date',
      dateFormat: 'YYYY-MM-DD'
    },
    {
      key: 'projectStartTime',
      label: t('declaration.projectStartTime'),
      value: declaration.value.projectStartTime || '-',
      type: 'date',
      dateFormat: 'YYYY-MM-DD'
    },
    {
      key: 'projectEndTime',
      label: t('declaration.projectEndTime'),
      value: declaration.value.projectEndTime || '-',
      type: 'date',
      dateFormat: 'YYYY-MM-DD'
    },
    {
      key: 'submitTime',
      label: t('declaration.submitTime'),
      value: declaration.value.submitTime || '-',
      type: 'date',
      dateFormat: 'YYYY-MM-DD HH:mm'
    },
    {
      key: 'status',
      label: t('declaration.currentStatus'),
      value: declaration.value.statusType || 'submitting',
      type: 'status',
      statusMap: {
        submitting: { text: t('declaration.statusSubmitting'), type: 'warning' },
        success: { text: t('declaration.statusSuccess'), type: 'success' },
        failed: { text: t('declaration.statusFailed'), type: 'danger' }
      },
      tagSize: 'large'
    }
  ]
})

// 工作流状态 - 使用 workflowStatus 字段（pending/running/completed/failed）
const workflowStatus = computed(() => {
  const status = declaration.value.workflowStatus
  const progress = calculateProgress.value

  switch (status) {
    case 'pending':
      return {
        type: 'pending',
        icon: '⏳',
        title: t('declaration.workflowStatusPending'),
        description: t('declaration.workflowStatusPendingDesc'),
        showProgress: false,
        progress: 0
      }
    case 'running':
      return {
        type: 'processing',
        icon: '⏳',
        title: t('declaration.processing'),
        description: t('declaration.processingDesc'),
        showProgress: true,
        progress: progress
      }
    case 'completed':
      return {
        type: 'completed',
        icon: '✓',
        title: t('declaration.completed'),
        description: t('declaration.completedDesc'),
        showProgress: false,
        progress: 100
      }
    case 'failed':
      return {
        type: 'failed',
        icon: '✗',
        title: t('declaration.failed'),
        description: t('declaration.failedDesc'),
        showProgress: true, 
        progress: progress
      }
    default:
      return {
        type: 'pending',
        icon: '?',
        title: t('declaration.unknown'),
        description: t('declaration.unknownDesc'),
        showProgress: false,
        progress: 0
      }
  }
})

// 计算进度 - 根据 workflowResult.steps 计算
// 固定总步数为 6 步，只计算成功的步骤（type === 'success' 或 status === 'completed'）
const TOTAL_STEPS = 6
const calculateProgress = computed(() => {
  // 如果没有时间线数据，返回 0
  if (!workflowTimeline.value || workflowTimeline.value.length === 0) {
    logger.warn('Workflow timeline is empty, progress is 0', { 
      workflowTimeline: workflowTimeline.value 
    })
    return 0
  }
  
  // 只计算成功的步骤
  // 检查条件：type === 'success' 或 status === 'completed' 或 completed === true
  const successSteps = workflowTimeline.value.filter(item => {
    // 检查 type 字段：只有 'success' 才算成功
    if (item.type === 'success') {
      logger.debug('Step is success by type', { title: item.title, type: item.type })
      return true
    }
    // 检查 status 字段：只有 'completed' 才算成功
    if (item.status === 'completed') {
      logger.debug('Step is success by status', { title: item.title, status: item.status })
      return true
    }
    // 检查 completed 字段
    if (item.completed === true) {
      logger.debug('Step is success by completed', { title: item.title, completed: item.completed })
      return true
    }
    logger.debug('Step is not success', { 
      title: item.title, 
      type: item.type, 
      status: item.status, 
      completed: item.completed 
    })
    return false
  }).length
  
  // 使用固定的 6 步作为总数
  const progress = Math.round((successSteps / TOTAL_STEPS) * 100)
  
  // 详细日志，帮助调试
  logger.info('Progress calculated', {
    totalSteps: TOTAL_STEPS,
    successSteps,
    progress,
    timelineLength: workflowTimeline.value.length,
    timeline: workflowTimeline.value.map(item => ({ 
      title: item.title, 
      type: item.type,
      status: item.status,
      completed: item.completed
    }))
  })
  
  return progress
})

// 进度条状态 - 根据工作流状态和进度计算
const progressStatus = computed(() => {
  const status = declaration.value?.workflowStatus
  const progress = calculateProgress.value
  
  // 如果工作流状态是 failed，即使有进度也显示为 danger
  if (status === 'failed') {
    return 'danger'
  }
  
  // 如果工作流状态是 completed，显示为 success
  if (status === 'completed') {
    return 'success'
  }
  
  // 如果工作流状态是 running，显示为 warning
  if (status === 'running') {
    return 'warning'
  }
  
  // 根据进度计算状态
  if (progress === 100) return 'success'
  if (progress >= 50) return 'warning'
  if (progress > 0) return 'info'
  return 'info' // 默认返回 info，避免显示"未知状态"
})


// 加载工作流状态（轮询接口）
// 轮询启动后，使用此接口的数据更新状态和进度
const loadWorkflowStatus = async (declarationId) => {
  try {
    logger.info('Loading workflow status from polling API', { id: declarationId })
    
    const response = await getDeclarationWorkflowStatus(declarationId)
    const responseData = response?.data || response
    const workflowData = responseData?.data || {}

    // 优先使用轮询接口返回的 workflowStatus（如果存在）
    if (workflowData.workflowStatus) {
      declaration.value.workflowStatus = workflowData.workflowStatus
      logger.info('Workflow status updated from polling API', { 
        workflowStatus: workflowData.workflowStatus 
      })
    }

    // 处理工作流步骤（时间线）
    // 注意：工作流未开始时，steps 可能为空数组或 null
    const steps = workflowData.steps || []
    
    if (Array.isArray(steps) && steps.length > 0) {
      // 转换步骤数据为时间线格式（与明细接口格式一致）
      // 使用响应式更新，确保页面实时刷新
      workflowTimeline.value = steps.map((step, index) => {
        const stepStatus = step.status || 'pending'
        const isCompleted = stepStatus === 'success'
        const isProcessing = stepStatus === 'running'
        const isFailed = stepStatus === 'failed'
        
        return {
          key: `timeline-${index}`,
          title: step.name || '',
          time: step.timestamp || '',
          status: isCompleted ? 'completed' : isFailed ? 'failed' : isProcessing ? 'processing' : 'pending',
          completed: isCompleted,
          // 注意：type 用于进度计算，只有 success 才算成功
          type: isCompleted ? 'success' : isFailed ? 'danger' : isProcessing ? 'warning' : 'info',
          // 添加图标：success 显示绿色勾，failed 显示红色叉
          icon: isCompleted ? Check : isFailed ? Close : undefined,
          // 添加状态映射，用于StatusTag显示
          statusMap: {
            completed: { text: '已完成', type: 'success' },
            failed: { text: '失败', type: 'danger' },
            processing: { text: '处理中', type: 'warning' },
            pending: { text: '待处理', type: 'info' }
          }
        }
      })
      
      logger.info('Workflow timeline updated from polling API', {
        timelineCount: workflowTimeline.value.length,
        progress: calculateProgress.value
      })
      
      // 处理工作流结果（成功或失败的额外信息）
      if (declaration.value.workflowStatus === 'completed') {
        workflowResult.value = {
          projectId: workflowData.projectId,
          projectName: workflowData.projectName,
          projectDescription: workflowData.projectDescription
        }
      } else if (declaration.value.workflowStatus === 'failed') {
        workflowResult.value = {
          errorMessage: workflowData.errorMessage || workflowData.error,
          suggestion: workflowData.suggestion,
          errorDetails: workflowData.errorDetails
        }
      }
      
      // 检查工作流状态，决定是否停止轮询
      // 1. 如果 workflowStatus === 'completed'，停止轮询
      if (declaration.value.workflowStatus === 'completed') {
        logger.info('Workflow completed, stopping polling')
        stopWorkflowPolling()
        return
      }
      
      // 2. 如果 workflowStatus === 'failed'，停止轮询
      if (declaration.value.workflowStatus === 'failed') {
        logger.info('Workflow failed, stopping polling')
        stopWorkflowPolling()
        return
      }
      
      // 3. 如果没有 workflowStatus，根据 steps 判断
      // 检查是否有失败的步骤
      const hasFailedStep = steps.some(step => step.status === 'failed')
      if (hasFailedStep) {
        declaration.value.workflowStatus = 'failed'
        stopWorkflowPolling()
        return
      }
      
      // 检查是否所有步骤都完成
      const allCompleted = steps.length > 0 && steps.every(step => step.status === 'success')
      if (allCompleted) {
        declaration.value.workflowStatus = 'completed'
        stopWorkflowPolling()
        return
      }
    } else {
      // 工作流未开始时，steps 为空数组或 null
      workflowTimeline.value = []
    }

    logger.info('Workflow status loaded successfully from polling API', { 
      id: declarationId, 
      stepsCount: steps.length,
      workflowStatus: declaration.value.workflowStatus,
      progress: calculateProgress.value
    })
  } catch (error) {
    logger.error('Failed to load workflow status from polling API', error)
    // 轮询失败不提示用户，避免干扰
  }
}

// 启动工作流状态轮询
// 轮询启动后，使用轮询接口的数据更新状态和进度
const startWorkflowPolling = (declarationId) => {
  // 清除之前的定时器
  stopWorkflowPolling()
  
  logger.info('Starting workflow polling', { id: declarationId, interval: POLLING_INTERVAL })
  
  // 立即加载一次
  loadWorkflowStatus(declarationId)
  
  // 设置轮询定时器（每4秒轮询一次，在3-5秒之间）
  workflowPollingTimer = setInterval(() => {
    // 检查当前工作流状态
    const workflowStatus = declaration.value.workflowStatus
    
    // 只在工作流运行中或待处理时继续轮询
    if (workflowStatus === 'running' || workflowStatus === 'pending') {
      loadWorkflowStatus(declarationId)
    } else {
      // 已完成或失败，停止轮询
      logger.info('Workflow status is completed or failed, stopping polling', { workflowStatus })
      stopWorkflowPolling()
    }
  }, POLLING_INTERVAL)
}

// 停止工作流状态轮询
const stopWorkflowPolling = () => {
  if (workflowPollingTimer) {
    clearInterval(workflowPollingTimer)
    workflowPollingTimer = null
    logger.info('Workflow polling stopped')
  }
}

// 加载申报详情
const loadDeclarationDetail = async () => {
  const declarationId = route.params.id
  if (!declarationId) {
    ElMessage.error(t('declaration.invalidId'))
    router.push('/declaration/list')
    return
  }

  try {
    loading.value = true
    logger.info('Starting to load declaration detail', { id: declarationId })
    
    const response = await getDeclarationDetail(declarationId)
    
    // 处理响应数据
    const detailData = response?.data || {}
    
    // 调试日志：输出原始响应数据结构
    logger.info('Response data structure', { 
      responseKeys: Object.keys(response || {}),
      hasData: !!response?.data,
      detailDataKeys: Object.keys(detailData),
      detailDataId: detailData.id
    })

    // 数据映射：后端字段 -> 前端字段
    declaration.value = {
      id: detailData.id,
      declarationNumber: detailData.number || detailData.declarationNumber,
      applicantName: detailData.applicantName,
      departmentName: detailData.department || detailData.departmentName,
      projectLeader: detailData.projectLeader,
      documentPublishTime: detailData.documentPublishTime,
      projectStartTime: detailData.projectStartTime,
      projectEndTime: detailData.projectEndTime,
      submitTime: detailData.submitTime || detailData.createdTime || detailData.createdAt,
      createdTime: detailData.createdTime,
      updatedTime: detailData.updatedTime,
      researchDirection: detailData.researchDirection,
      researchTopic: detailData.researchTopic,
      contentSummary: detailData.contentSummary || '',
      researchFields: detailData.researchFields || [],
      status: detailData.statusDescription || STATUS_MAP[detailData.status]?.label || '申报中',
      statusType: STATUS_MAP[detailData.status]?.type || 'submitting',
      workflowStatus: detailData.workflowStatus || 'pending',
      workflowStatusDescription: detailData.workflowStatusDescription || '',
      workflowId: detailData.workflowId || null,
      hasAttachment: detailData.hasAttachment || false,
      attachmentId: detailData.attachmentId || null,
      attachmentUrl: detailData.attachmentUrl || null
      // 注意：fileUrl 和 fileFormat 字段不需要在这里保存，只在工作流轮询时临时使用
    }

    // 处理工作流结果（workflowResult 包含 steps 数组，用于展示工作流执行的明细）
    // 无论成功失败都需要将每一步展示出来
    if (detailData.workflowResult && detailData.workflowResult.steps) {
      const steps = detailData.workflowResult.steps
      
      logger.info('Processing workflow steps', { 
        stepsCount: steps.length,
        steps: steps 
      })
      
      if (Array.isArray(steps) && steps.length > 0) {
        // 处理工作流步骤（时间线）- 始终显示所有步骤
        workflowTimeline.value = steps.map((step, index) => {
          const stepStatus = step.status || 'pending'
          const isCompleted = stepStatus === 'success'
          const isProcessing = stepStatus === 'running'
          const isFailed = stepStatus === 'failed'
          
          return {
            key: `timeline-${index}`,
            title: step.name || '',
            time: step.timestamp || '',
            status: isCompleted ? 'completed' : isFailed ? 'failed' : isProcessing ? 'processing' : 'pending',
            completed: isCompleted,
            // 注意：type 用于进度计算，success 和 danger 都算已完成
            type: isCompleted ? 'success' : isFailed ? 'danger' : isProcessing ? 'warning' : 'info',
            // 添加图标：success 显示绿色勾，failed 显示红色叉
            icon: isCompleted ? Check : isFailed ? Close : undefined,
            // 添加状态映射，用于StatusTag显示
            statusMap: {
              completed: { text: '已完成', type: 'success' },
              failed: { text: '失败', type: 'danger' },
              processing: { text: '处理中', type: 'warning' },
              pending: { text: '待处理', type: 'info' }
            }
          }
        })
        
        logger.info('Workflow timeline created', { 
          timelineCount: workflowTimeline.value.length 
        })
      } else {
        workflowTimeline.value = []
      }
      
      // 处理工作流结果（成功或失败的额外信息）
      // 注意：workflowResult 主要用于展示步骤明细，成功/失败信息是额外的
      if (declaration.value.workflowStatus === 'completed') {
        workflowResult.value = {
          projectId: detailData.workflowResult.projectId,
          projectName: detailData.workflowResult.projectName,
          projectDescription: detailData.workflowResult.projectDescription
        }
      } else if (declaration.value.workflowStatus === 'failed') {
        workflowResult.value = {
          errorMessage: detailData.workflowResult.errorMessage || detailData.workflowResult.error,
          suggestion: detailData.workflowResult.suggestion,
          errorDetails: detailData.workflowResult.errorDetails
        }
      }
    }

    // 如果详情接口没有返回工作流步骤信息，尝试从工作流状态接口获取
    if (!workflowTimeline.value || workflowTimeline.value.length === 0) {
      logger.info('No workflow steps in detail response, loading from workflow status API')
      await loadWorkflowStatus(declarationId)
    }
    
    // 如果工作流状态是进行中（running）或待处理（pending），启动轮询
    // 轮询启动后，使用轮询接口的数据更新状态和进度
    if (declaration.value.workflowStatus === 'running' || declaration.value.workflowStatus === 'pending') {
      logger.info('Workflow is running or pending, starting polling', { 
        workflowStatus: declaration.value.workflowStatus 
      })
      // 工作流运行中，启动轮询监控工作流进度
      // 轮询启动后，loadWorkflowStatus 会使用轮询接口的数据更新状态和进度
      startWorkflowPolling(declarationId)
    } else {
      logger.info('Workflow is completed or failed, no polling needed', { 
        workflowStatus: declaration.value.workflowStatus 
      })
    }

    logger.info('Declaration detail loaded successfully', { id: declaration.value.id })
  } catch (error) {
    logger.error('Failed to load declaration detail', error)
    ElMessage.error(t('declaration.loadError'))
  } finally {
    loading.value = false
  }
}

// 事件处理
const handleBack = () => {
  router.push('/declaration/list')
}

onMounted(() => {
  loadDeclarationDetail()
})

// 组件卸载时停止轮询
onBeforeUnmount(() => {
  stopWorkflowPolling()
})
</script>

<style lang="scss" scoped>
.declaration-detail-container {
  padding: var(--gap-lg);
  background: var(--bg);
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
}

.back-button {
  flex-shrink: 0;
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 20px;
  font-size: 16px;
  color: var(--text-2, #6b7280);
}

.empty-container {
  padding: 60px 20px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  padding: var(--gap-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
}

.card-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
  padding-bottom: var(--gap-sm);
  border-bottom: 1px solid var(--border);
}

// 研究信息样式
.research-info-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.research-info-content .info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  color: var(--text-2, #6b7280);
  font-weight: 500;
}

.research-direction {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: var(--gap-md);
  font-size: var(--font-size-base);
  line-height: var(--line-height-normal);
  color: var(--text);
  white-space: nowrap; // 确保在一行显示
  overflow: hidden;
  text-overflow: ellipsis;
}

.field-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.field-tag {
  background: var(--color-primary-light, #e0e7ff);
  color: var(--color-primary, #1e3a8a);
  border: none;
}

.no-field {
  color: var(--text-3, #9ca3af);
  font-size: 14px;
}

// 工作流状态样式
.workflow-container {
  margin-top: 16px;
}

.workflow-status {
  display: flex;
  align-items: flex-start;
  gap: var(--gap-md);
  margin-bottom: var(--gap-lg);
  padding: var(--gap-md);
  background: var(--bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
}

.workflow-icon {
  font-size: 20px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;

  &.processing {
    background: var(--color-warning, #f59e0b);
    color: white;
    animation: pulse 2s infinite;
  }

  &.completed {
    background: var(--color-success, #16a34a);
    color: white;
  }

  &.failed {
    background: var(--color-error, #dc2626);
    color: white;
  }

  &.pending {
    background: var(--border, #e5e7eb);
    color: var(--text-2, #6b7280);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.workflow-info {
  flex: 1;
  min-width: 0;
}

.workflow-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text, #374151);
  margin-bottom: 4px;
}

.workflow-description {
  font-size: 14px;
  color: var(--text-2, #6b7280);
  margin-bottom: 8px;
}

.workflow-progress {
  margin-top: 8px;
}

.workflow-timeline {
  margin-top: var(--gap-lg);
  
  // 时间线样式优化 - 时间对齐、状态标签对齐、圆角
  :deep(.timeline-item) {
    .timeline-content {
      .content-header {
        align-items: center; // 确保标题和时间对齐
        
        .content-title {
          .item-title {
            margin: 0; // 移除默认margin，确保对齐
            line-height: 1.5;
          }
        }
        
        .content-meta {
          display: flex;
          align-items: center;
          gap: var(--gap-md);
          flex-shrink: 0; // 防止压缩
          min-width: 240px; // 固定总宽度，确保对齐
          
          .item-time {
            width: 160px; // 固定时间宽度，确保对齐
            text-align: right; // 右对齐
            font-size: var(--font-size-sm);
            color: var(--text-3);
            white-space: nowrap;
            flex-shrink: 0;
          }
          
          // 状态标签样式 - 圆角、对齐，确保失败和完成按钮一样大
          // 使用更具体的选择器确保样式生效，强制固定宽度覆盖自适应行为
          :deep(.status-tag),
          :deep(.el-tag),
          :deep(.timeline-item .content-meta .status-tag),
          :deep(.timeline-item .content-meta .el-tag),
          :deep(.workflow-timeline .status-tag),
          :deep(.workflow-timeline .el-tag) {
            border-radius: var(--radius-md) !important; // 圆角，使用!important确保生效
            padding: 4px 8px !important; // 减小padding，确保文字能完整显示
            font-size: var(--font-size-sm) !important;
            white-space: nowrap !important;
            // 强制固定宽度和高度，覆盖 Element Plus 的自适应行为
            min-width: 60px !important;
            max-width: 60px !important;
            width: 60px !important;
            height: 24px !important;
            text-align: center !important;
            display: inline-flex !important;
            align-items: center !important;
            justify-content: center !important;
            flex-shrink: 0 !important;
            box-sizing: border-box !important; // 确保padding不影响宽度
            line-height: 1 !important; // 确保文字垂直居中
            overflow: hidden !important; // 防止内容溢出
            
            // 确保所有状态标签（成功、失败、处理中等）都有相同的尺寸
            .status-text {
              display: inline-block;
              width: 100%;
              text-align: center;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            // 覆盖 Element Plus 的默认样式和所有变体
            &.el-tag--success,
            &.el-tag--danger,
            &.el-tag--warning,
            &.el-tag--info,
            &.el-tag--primary {
              min-width: 60px !important;
              max-width: 60px !important;
              width: 60px !important;
              height: 24px !important;
              padding: 4px 8px !important;
            }
            
            // 覆盖不同尺寸的默认样式
            &.el-tag--small,
            &.el-tag--default,
            &.el-tag--large {
              min-width: 60px !important;
              max-width: 60px !important;
              width: 60px !important;
              height: 24px !important;
            }
          }
        }
      }
    }
  }
}



.basic-info-card {
  // 基本信息卡片样式 - 横向布局
  :deep(.info-list.layout-grid) {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--gap-lg);
    
    .info-item {
      flex-direction: column;
      align-items: stretch;
      padding: var(--gap-md);
      background: var(--bg);
      border-radius: var(--radius-md);
      border: 1px solid var(--border);
      
      .info-label {
        min-width: auto;
        margin-bottom: var(--gap-xs);
        font-size: var(--font-size-sm);
        color: var(--text-2);
        font-weight: 500;
        white-space: nowrap;
      }
      
      .info-value {
        font-size: var(--font-size-base);
        color: var(--text);
        word-break: break-word;
      }
    }
  }
  
  // InfoCard 标题样式
  :deep(.card-title) {
    color: var(--color-primary) !important;
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .basic-info-card {
    :deep(.info-list.layout-grid) {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}

@media (max-width: 768px) {
  .declaration-detail-container {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .detail-card {
    padding: 16px;
  }

  .workflow-status {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .research-info-content {
    gap: 12px;
  }

  .basic-info-card {
    :deep(.info-list.layout-grid) {
      grid-template-columns: 1fr;
    }
  }
}
</style>

