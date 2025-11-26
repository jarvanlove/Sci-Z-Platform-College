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
              <!-- 进度条 -->
              <ProgressBar
                v-if="workflowStatus.showProgress"
                :percentage="workflowStatus.progress"
                :status="progressStatus"
                size="default"
                :animated="workflowStatus.type === 'processing'"
                class="workflow-progress"
              />
            </div>
          </div>

          <!-- 时间线 -->
          <Timeline
            v-if="workflowTimeline.length > 0"
            :items="workflowTimeline"
            direction="vertical"
            class="workflow-timeline"
          />

          <!-- 结果展示 -->
          <div v-if="workflowResult" class="result-container">
            <!-- 成功结果 -->
            <div
              v-if="declaration.statusType === 'success'"
              class="result-success"
            >
              <div class="result-title">
                <el-icon><Check /></el-icon>
                {{ $t('declaration.processSuccess') }}
              </div>
              <div class="result-content">
                <p>
                  <strong>{{ $t('declaration.projectId') }}:</strong> {{ workflowResult.projectId || '-' }}
                </p>
                <p>
                  <strong>{{ $t('declaration.projectName') }}:</strong> {{ workflowResult.projectName || '-' }}
                </p>
                <p v-if="workflowResult.projectDescription">
                  <strong>{{ $t('declaration.projectDescription') }}:</strong> {{ workflowResult.projectDescription }}
                </p>
              </div>
            </div>

            <!-- 失败结果 -->
            <div
              v-else-if="declaration.statusType === 'failed'"
              class="result-failed"
            >
              <div class="result-title">
                <el-icon><Close /></el-icon>
                {{ $t('declaration.processFailed') }}
              </div>
              <div class="result-content">
                <p v-if="workflowResult.errorMessage">
                  <strong>{{ $t('declaration.failureReason') }}:</strong> {{ workflowResult.errorMessage }}
                </p>
                <p v-if="workflowResult.suggestion">
                  <strong>{{ $t('declaration.suggestion') }}:</strong> {{ workflowResult.suggestion }}
                </p>
                <div v-if="workflowResult.errorDetails" class="error-details">
                  {{ workflowResult.errorDetails }}
                </div>
              </div>
            </div>
          </div>
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

// 工作流状态
const workflowStatus = computed(() => {
  const status = declaration.value.statusType || 'submitting'
  const progress = calculateProgress.value

  switch (status) {
    case 'submitting':
      return {
        type: 'processing',
        icon: '⏳',
        title: t('declaration.processing'),
        description: t('declaration.processingDesc'),
        showProgress: true,
        progress: progress
      }
    case 'success':
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
        showProgress: false,
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

// 计算进度
const calculateProgress = computed(() => {
  if (!workflowTimeline.value || workflowTimeline.value.length === 0) {
    return 0
  }
  const totalSteps = workflowTimeline.value.length
  const completedSteps = workflowTimeline.value.filter(
    item => item.completed || item.status === 'completed'
  ).length
  return Math.round((completedSteps / totalSteps) * 100)
})

// 进度条状态
const progressStatus = computed(() => {
  const progress = calculateProgress.value
  if (progress === 100) return 'success'
  if (progress >= 50) return 'warning'
  return 'info'
})


// 加载工作流状态
const loadWorkflowStatus = async (declarationId) => {
  try {
    logger.info('Loading workflow status', { id: declarationId })
    
    const response = await getDeclarationWorkflowStatus(declarationId)
    const responseData = response?.data || response
    const workflowData = responseData?.data || {}

    // 处理工作流步骤（时间线）
    // 注意：工作流未开始时，steps 可能为空数组或 null
    const steps = workflowData.steps || []
    
    if (Array.isArray(steps) && steps.length > 0) {
      // 转换步骤数据为时间线格式
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
          type: isCompleted ? 'success' : isFailed ? 'danger' : isProcessing ? 'warning' : 'info'
        }
      })
      
      // 检查是否有失败的步骤
      const hasFailedStep = steps.some(step => step.status === 'failed')
      if (hasFailedStep) {
        // 步骤状态为 failed 时，工作流执行失败
        declaration.value.statusType = 'failed'
        declaration.value.status = STATUS_MAP[3]?.label || '申报失败'
        declaration.value.workflowStatus = 'failed'
        // 停止轮询
        stopWorkflowPolling()
      } else {
        // 检查是否所有步骤都完成
        const allCompleted = steps.length > 0 && steps.every(step => step.status === 'success')
        if (allCompleted) {
          declaration.value.workflowStatus = 'completed'
          // 如果状态还是申报中，更新为成功
          if (declaration.value.statusType === 'submitting') {
            declaration.value.statusType = 'success'
            declaration.value.status = STATUS_MAP[2]?.label || '申报成功'
          }
        }
      }
    } else {
      // 工作流未开始时，steps 为空数组或 null
      workflowTimeline.value = []
    }

    // 注意：工作流状态接口返回的 fileUrl 和 fileFormat 在详情页不需要展示
    // 只需要根据工作流完成状态停止轮询
    const fileUrl = workflowData.fileUrl
    const fileFormat = workflowData.fileFormat
    
    if (fileUrl && fileFormat) {
      // 工作流完成且有文件，停止轮询
      const allCompleted = steps.every(step => step.status === 'success')
      if (allCompleted && steps.length > 0) {
        stopWorkflowPolling()
      }
    }

    logger.info('Workflow status loaded successfully', { 
      id: declarationId, 
      stepsCount: steps.length
    })
  } catch (error) {
    logger.error('Failed to load workflow status', error)
    // 轮询失败不提示用户，避免干扰
  }
}

// 启动工作流状态轮询
const startWorkflowPolling = (declarationId) => {
  // 清除之前的定时器
  stopWorkflowPolling()
  
  // 立即加载一次
  loadWorkflowStatus(declarationId)
  
  // 设置轮询定时器（每4秒轮询一次，在3-5秒之间）
  workflowPollingTimer = setInterval(() => {
    // 只在工作流未完成时继续轮询
    const statusType = declaration.value.statusType
    if (statusType === 'submitting' || statusType === undefined) {
      loadWorkflowStatus(declarationId)
    } else {
      // 已完成或失败，停止轮询
      stopWorkflowPolling()
    }
  }, POLLING_INTERVAL)
  
  logger.info('Workflow polling started', { id: declarationId, interval: POLLING_INTERVAL })
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
    const responseData = response?.data || response
    const detailData = responseData?.data || {}

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

    // 处理工作流结果（根据图1的接口返回结构，workflowResult 包含 steps 数组）
    if (detailData.workflowResult) {
      const result = detailData.workflowResult
      
      // 处理工作流步骤（时间线）- 直接从详情接口获取
      const steps = result.steps || []
      if (Array.isArray(steps) && steps.length > 0) {
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
            type: isCompleted ? 'success' : isFailed ? 'danger' : isProcessing ? 'warning' : 'info'
          }
        })
      } else {
        workflowTimeline.value = []
      }
      
      // 处理成功结果
      if (declaration.value.statusType === 'success') {
        workflowResult.value = {
          projectId: result.projectId,
          projectName: result.projectName,
          projectDescription: result.projectDescription
        }
      } 
      // 处理失败结果
      else if (declaration.value.statusType === 'failed') {
        workflowResult.value = {
          errorMessage: result.errorMessage || result.error,
          suggestion: result.suggestion,
          errorDetails: result.errorDetails
        }
      }
    }

    // 如果工作流状态是进行中，启动轮询
    // 注意：只有当状态为申报中且工作流状态为 running 或 pending 时才轮询
    // 如果详情接口已经返回了 workflowResult.steps，则不需要再调用工作流状态接口
    if (declaration.value.statusType === 'submitting' && !detailData.workflowResult?.steps) {
      // 申报中状态且详情接口没有返回步骤信息，启动轮询监控工作流进度
      startWorkflowPolling(declarationId)
    } else if (declaration.value.statusType === 'submitting') {
      // 详情接口已返回步骤信息，但可能还在进行中，启动轮询继续更新
      startWorkflowPolling(declarationId)
    } else {
      // 已完成或失败，如果详情接口没有返回工作流信息，再调用一次工作流状态接口
      if (!detailData.workflowResult?.steps) {
        await loadWorkflowStatus(declarationId)
      }
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
  padding: 20px;
  background: var(--bg-2, #f7f9fc);
  min-height: calc(100vh - 56px);
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
  font-size: 24px;
  font-weight: 600;
  color: var(--text, #1e3a8a);
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
  background: var(--surface, #ffffff);
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--border, #e5e7eb);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text, #1e3a8a);
  margin: 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border, #e5e7eb);
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
  background: var(--bg-1, #f8fafc);
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 6px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text, #374151);
  white-space: pre-wrap;
  word-break: break-word;
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
  gap: 12px;
  margin-bottom: 24px;
  padding: 12px;
  background: var(--bg-1, #f8fafc);
  border-radius: 6px;
  border: 1px solid var(--border, #e5e7eb);
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
  margin-top: 16px;
}

// 结果展示样式
.result-container {
  margin-top: 24px;
}

.result-success {
  background: var(--color-success-light, #f0fdf4);
  border: 1px solid var(--color-success-border, #bbf7d0);
  border-radius: 6px;
  padding: 16px;
}

.result-failed {
  background: var(--color-error-light, #fef2f2);
  border: 1px solid var(--color-error-border, #fecaca);
  border-radius: 6px;
  padding: 16px;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-success .result-title {
  color: var(--color-success, #16a34a);
}

.result-failed .result-title {
  color: var(--color-error, #dc2626);
}

.result-content {
  font-size: 14px;
  color: var(--text, #374151);
  line-height: 1.6;

  p {
    margin: 8px 0;

    &:first-child {
      margin-top: 0;
    }

    &:last-child {
      margin-bottom: 0;
    }

    strong {
      font-weight: 600;
      color: var(--text, #374151);
    }
  }
}

.error-details {
  background: var(--color-error-light, #fef2f2);
  border: 1px solid var(--color-error-border, #fecaca);
  border-radius: 6px;
  padding: 12px;
  margin-top: 12px;
  font-family: monospace;
  font-size: 12px;
  color: var(--color-error, #dc2626);
  white-space: pre-wrap;
  word-break: break-word;
}


.basic-info-card {
  // 基本信息卡片样式
  :deep(.info-list.layout-grid) {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    
    .info-item {
      flex-direction: column;
      align-items: stretch;
      padding: 12px;
      
      .info-label {
        min-width: auto;
        margin-bottom: 4px;
      }
    }
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

