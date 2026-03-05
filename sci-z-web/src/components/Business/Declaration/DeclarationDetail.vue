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
          <div class="card-header-with-loading">
            <span class="card-title">{{ $t('declaration.workflowStatus') }}</span>
            <el-icon v-if="isWorkflowRunning" class="workflow-header-loading is-loading"><Loading /></el-icon>
          </div>
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
                <template v-if="workflowStatus.type === 'completed'">
                  {{ t('declaration.completedDescPrefix') }}
                  <a href="javascript:void(0)" class="workflow-detail-link" @click="goToDeclarationList">{{ t('declaration.completedDescLink') }}</a>
                </template>
                <template v-else>
                  {{ workflowStatus.description }}
                </template>
              </div>
              <!-- 进度条 - 始终显示，根据步骤计算进度（包括失败状态） -->
              <!-- 只要有工作流步骤数据就显示进度条，无论成功还是失败 -->
              <ProgressBar
                v-if="workflowTimeline && workflowTimeline.length > 0"
                :percentage="declaration.workflowStatus === 'completed' ? 100 : Number(calculateProgress)"
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

          <!-- 时间线 - 始终显示，失败步骤可点开展示错误信息 -->
          <Timeline
            :items="workflowTimeline"
            direction="vertical"
            class="workflow-timeline"
          >
            <template v-for="(item, index) in workflowTimeline" #[`content-${index}`]="{ item: slotItem }">
              <div :key="'err-'+index" class="step-error-wrapper">
                <div v-if="slotItem.errorMessage" class="step-error-block">
                  <a
                    class="step-error-toggle"
                    href="javascript:void(0)"
                    @click.prevent="expandedError = expandedError === index ? null : index"
                  >
                    {{ expandedError === index ? t('declaration.collapseError') : t('declaration.viewErrorReason') }}
                  </a>
                  <div v-if="expandedError === index" class="step-error-message">{{ slotItem.errorMessage }}</div>
                </div>
              </div>
            </template>
          </Timeline>

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
  1: { type: 'submitting', label: '申报已提交' },
  2: { type: 'success', label: '申报成功' },
  3: { type: 'failed', label: '申报未通过' }
}

// 申报数据
const declaration = ref({})
const loading = ref(false)

// 工作流时间线
const workflowTimeline = ref([])

// 工作流结果（预留，当前错误信息仅使用各步骤 steps[].errorMessage，不依赖整体 errorMessage）
const workflowResult = ref(null)

// 当前展开显示失败原因的步骤索引（用于时间线失败节点点开展示）
const expandedError = ref(null)

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

// 工作流是否进行中：与轮询逻辑一致——pending/running 时显示 loading，completed/failed 时消失（流程全部完成即停止轮询）
const isWorkflowRunning = computed(() => {
  const status = declaration.value?.workflowStatus
  return status === 'pending' || status === 'running'
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

// 计算进度 - 根据工作流步骤（workflowTimeline 来自 steps）计算
// 总步数与后端一致：申报提交 + 工作流启动 + AI内容分析 + 申报信息生成 + 数据库存储 + 申报书生成 + 项目创建 = 7 步
const TOTAL_STEPS = 7
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
    
    // 🔥 修复数据解析逻辑：接口返回格式为 { code: 200, data: { workflowStatus, steps, ... } }
    // 如果 response.data 存在且是对象，直接使用；否则尝试 response.data.data
    let workflowData = {}
    if (response?.data) {
      // 检查 response.data 是否包含 workflowStatus 或 steps（说明它就是工作流数据）
      if (response.data.workflowStatus !== undefined || response.data.steps !== undefined) {
        workflowData = response.data
      } else if (response.data.data) {
        // 如果 response.data.data 存在，使用它（嵌套结构）
        workflowData = response.data.data
      } else {
        workflowData = response.data
      }
    } else {
      workflowData = response || {}
    }
    
    logger.info('Parsed workflow data from API', { 
      workflowData,
      hasWorkflowStatus: 'workflowStatus' in workflowData,
      workflowStatus: workflowData.workflowStatus,
      stepsCount: Array.isArray(workflowData.steps) ? workflowData.steps.length : 0
    })

    // 🔥 优先使用轮询接口返回的 workflowStatus（如果存在），立即更新状态
    if (workflowData.workflowStatus !== undefined) {
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
          },
          errorMessage: step.errorMessage || ''
        }
      })
      
      logger.info('Workflow timeline updated from polling API', {
        timelineCount: workflowTimeline.value.length,
        progress: calculateProgress.value
      })
      
      // 失败/成功信息仅来自各步骤（steps[].errorMessage），不再使用整体 workflowResult 的 errorMessage
    } else {
      // 工作流未开始时，steps 为空数组或 null
      workflowTimeline.value = []
    }
    
    // 🔥 检查工作流状态，决定是否停止轮询（必须在更新状态后立即检查）
    const currentStatus = declaration.value.workflowStatus
    
    // 1. 如果 workflowStatus === 'completed'，立即停止轮询
    if (currentStatus === 'completed') {
      logger.info('Workflow completed, stopping polling immediately', { 
        workflowStatus: currentStatus,
        stepsCount: steps.length 
      })
      stopWorkflowPolling()
      return
    }
    
    // 2. 如果 workflowStatus === 'failed'，立即停止轮询
    if (currentStatus === 'failed') {
      logger.info('Workflow failed, stopping polling immediately', { 
        workflowStatus: currentStatus 
      })
      stopWorkflowPolling()
      return
    }
    
    // 3. 仅当接口未返回 workflowStatus 时，才根据 steps 推断失败；是否完成必须以接口返回的 workflowStatus === 'completed' 为准
    // 不能因「当前 steps 全部 success」就设为 completed 并停止轮询，否则只有 2 步时会把 running 误判为完成
    if (Array.isArray(steps) && steps.length > 0) {
      const hasFailedStep = steps.some(step => step.status === 'failed')
      if (hasFailedStep) {
        declaration.value.workflowStatus = 'failed'
        logger.info('Workflow has failed step, stopping polling', { steps })
        stopWorkflowPolling()
        return
      }
      // 不再根据 allCompleted 覆盖为 completed：轮询仅在接口返回 workflowStatus === 'completed' 时停止
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
    // 🔥 检查当前工作流状态（使用最新的响应式值）
    const workflowStatus = declaration.value.workflowStatus
    
    logger.debug('Polling timer triggered', { workflowStatus, declarationId })
    
    // 🔥 只在工作流运行中或待处理时继续轮询
    // 如果状态是 completed 或 failed，loadWorkflowStatus 中已经会停止轮询
    // 但这里也做一次检查，确保不会继续轮询
    if (workflowStatus === 'running' || workflowStatus === 'pending' || !workflowStatus) {
      loadWorkflowStatus(declarationId)
    } else {
      // 已完成或失败，停止轮询
      logger.info('Workflow status is completed or failed, stopping polling in timer', { workflowStatus })
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
    // 防止详情接口误返 completed：若为 completed 但步骤数不足 7，视为 running，由轮询接口为准
    let initialWorkflowStatus = detailData.workflowStatus || 'pending'
    const detailSteps = detailData.workflowResult?.steps || []
    const successStepCount = detailSteps.filter((s) => s.status === 'success').length
    if (initialWorkflowStatus === 'completed' && successStepCount < 7) {
      initialWorkflowStatus = 'running'
      logger.info('Detail returned completed but steps < 7, treat as running and will poll', { successStepCount })
    }

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
      workflowStatus: initialWorkflowStatus,
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
            },
            errorMessage: step.errorMessage || ''
          }
        })
        
        logger.info('Workflow timeline created', { 
          timelineCount: workflowTimeline.value.length 
        })
      } else {
        workflowTimeline.value = []
      }
      
      // 失败/成功信息仅来自各步骤（steps[].errorMessage），不再使用整体 workflowResult 的 errorMessage
    }

    // 若需要轮询，先拉一次工作流状态接口，保证首屏状态、进度条、loading 与后端一致
    const currentWorkflowStatus = declaration.value.workflowStatus
    const hasWorkflowId = declaration.value.workflowId
    const shouldStartPolling =
      currentWorkflowStatus === 'running' ||
      currentWorkflowStatus === 'pending' ||
      !currentWorkflowStatus ||
      (hasWorkflowId && currentWorkflowStatus !== 'completed' && currentWorkflowStatus !== 'failed')

    if (shouldStartPolling) {
      logger.info('Fetching workflow status once then starting polling', { workflowStatus: currentWorkflowStatus, hasWorkflowId })
      await loadWorkflowStatus(declarationId)
      // 仅当轮询未在 loadWorkflowStatus 内被停止（即接口仍未返回 completed/failed）时再启动定时轮询
      const stillRunning = declaration.value.workflowStatus === 'running' || declaration.value.workflowStatus === 'pending' || !declaration.value.workflowStatus
      if (stillRunning) {
        startWorkflowPolling(declarationId)
      }
    } else {
      if (!workflowTimeline.value || workflowTimeline.value.length === 0) {
        await loadWorkflowStatus(declarationId)
      }
      logger.info('Workflow is completed or failed, no polling', { workflowStatus: currentWorkflowStatus })
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

const goToDeclarationList = () => {
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

.card-header-with-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: var(--gap-sm);
  border-bottom: 1px solid var(--border);
  min-height: 28px;

  .card-title {
    font-size: var(--font-size-lg);
    font-weight: 600;
    color: var(--color-primary);
    margin: 0;
    line-height: 1.4;
    padding-bottom: 0;
    border-bottom: none; 
  }

  .workflow-header-loading {
    font-size: 18px;
    color: var(--color-primary);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    vertical-align: middle;
  }
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

// 🔥 研究信息模块标签样式：按照页面修改.md规范
.research-info-content .info-label {
  font-size: 14px !important;
  color: var(--text-2) !important; // #4b5563
  font-weight: 600 !important; // 加粗
}

// 🔥 研究方向和研究课题内容样式：按照页面修改.md规范
.research-direction {
  // 移除背景、边框、padding等装饰样式，只保留文本样式
  background: none !important;
  border: none !important;
  padding: 0 !important;
  font-size: 14px !important; // 与 el-input placeholder 一致
  line-height: var(--line-height-normal);
  color: var(--text-3) !important; // #6b7280（比 placeholder 稍深，与表格内容颜色一致）
  font-weight: 400 !important; // normal
  white-space: normal; // 允许换行
  word-break: break-word;
  overflow: visible;
  text-overflow: unset;
}

.field-tags {
  display: flex;
  flex-direction: row; // 🔥 横向排列，一行显示
  flex-wrap: wrap; // 允许换行
  gap: 6px;
  align-items: center;
  line-height: 1.4;
}

// 🔥 研究领域标签样式：与列表页保持一致
.field-tag {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 4px 12px !important;
  border-radius: 12px !important;
  font-size: 12px !important;
  font-weight: 500 !important;
  white-space: nowrap !important;
  background: var(--hover-light) !important; // 🔥 使用浅灰色背景，更协调
  color: var(--text-3) !important; // 🔥 与表格字体颜色保持一致
  border: 1px solid var(--border) !important; // 🔥 添加边框，更精致
  transition: all 0.2s ease !important;
  
  &:hover {
    background: var(--hover) !important;
    border-color: var(--border-hover) !important;
    color: var(--text-2) !important; // 🔥 hover时稍微加深
  }
}

// 🔥 暗色主题下的研究领域标签样式
[data-theme='dark'] .field-tag,
.dark .field-tag {
  background: rgba(148, 163, 184, 0.1) !important; // 🔥 暗色主题下使用半透明背景
  color: var(--text-3) !important; // 🔥 与表格字体颜色保持一致
  border-color: var(--border) !important;
  
  &:hover {
    background: rgba(148, 163, 184, 0.2) !important;
    border-color: var(--border-hover) !important;
    color: var(--text-2) !important;
  }
}

.no-field {
  color: var(--text-3, #9ca3af);
  font-size: 14px;
}

// 工作流状态样式 - 成功态使用墨绿色（仅本区域）
.workflow-container {
  margin-top: 16px;
  --color-success: #1b5e20; /* 墨绿色 */
  --color-success-bg: #b8d4b8; /* 墨绿浅底（状态标签用） */
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
    background: var(--color-success);
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

// 🔥 工作流描述：与表单内容样式保持一致（图1红色圈1）
.workflow-description {
  font-size: 14px !important; // 与表单内容一致
  color: var(--text-3) !important; // #6b7280，与表单内容一致
  font-weight: 400 !important; // normal，与表单内容一致
  margin-bottom: 8px;

  .workflow-detail-link {
    color: var(--color-primary);
    text-decoration: none;
    cursor: pointer;
    font-weight: 500;
    &:hover {
      text-decoration: underline;
    }
  }
}

.workflow-progress {
  margin-top: 8px;
  
  // 🔥 进度条百分比：与表单内容样式保持一致（图1红色圈2）
  :deep(.progress-percentage) {
    font-size: 14px !important; // 与表单内容一致
    color: var(--text-3) !important; // #6b7280，与表单内容一致
    font-weight: 400 !important; // normal，与表单内容一致
  }
  
  // 🔥 进度条中的状态标签：需要圆角处理（图1红色圈1）
  :deep(.status-tag),
  :deep(.el-tag),
  :deep(.progress-header .status-tag),
  :deep(.progress-header .el-tag) {
    padding: 4px 8px !important; // 🔥 与列表页一致
    border-radius: 12px !important; // 🔥 圆角处理，与列表页一致
    font-size: 12px !important; // 🔥 与列表页一致
    font-weight: 500 !important; // 🔥 与列表页一致
    display: inline-flex !important;
    align-items: center !important;
    gap: 4px !important;
    transition: all 0.2s ease !important;
    
    // 🔥 成功状态样式：墨绿色（仅处理状态区域）
    &.el-tag--success {
      background-color: var(--color-success-bg) !important;
      color: var(--color-success) !important;
      border-color: var(--color-success-bg) !important;
    }
    
    // 🔥 警告状态样式：与列表页保持一致
    &.el-tag--warning {
      background-color: #fef3c7 !important;
      color: #f59e0b !important;
      border-color: #fef3c7 !important;
    }
    
    // 🔥 危险状态样式：与列表页保持一致
    &.el-tag--danger {
      background-color: #fee2e2 !important;
      color: #dc2626 !important;
      border-color: #fee2e2 !important;
    }
    
    // 🔥 信息状态样式：与列表页保持一致
    &.el-tag--info {
      background-color: #e0e7ff !important;
      color: #2563eb !important;
      border-color: #e0e7ff !important;
    }
  }
}

.workflow-timeline {
  margin-top: var(--gap-lg);
  
  // 失败步骤：可点开展示失败原因
  :deep(.step-error-block) {
    margin-top: var(--gap-sm);
    .step-error-toggle {
      font-size: var(--font-size-sm);
      color: var(--color-primary);
      text-decoration: none;
      cursor: pointer;
      &:hover {
        text-decoration: underline;
      }
    }
    .step-error-message {
      margin-top: var(--gap-xs);
      padding: var(--gap-sm);
      background: #fee2e2;
      color: #b91c1c;
      font-size: var(--font-size-sm);
      border-radius: var(--radius-md);
      border: 1px solid #fecaca;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
  
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
          
          // 🔥 时间戳：与表单内容样式保持一致（图1红色圈3）
          .item-time {
            width: 160px; // 固定时间宽度，确保对齐
            text-align: right; // 右对齐
            font-size: 14px !important; // 与表单内容一致
            color: var(--text-3) !important; // #6b7280，与表单内容一致
            font-weight: 400 !important; // normal，与表单内容一致
            white-space: nowrap;
            flex-shrink: 0;
          }
          
          // 🔥 状态标签样式 - 圆角、对齐，与列表页保持一致（图2红色圈）
          // 使用更具体的选择器确保覆盖 StatusTag 组件内部样式
          :deep(.status-tag),
          :deep(.el-tag),
          :deep(.timeline-item .content-meta .status-tag),
          :deep(.timeline-item .content-meta .el-tag),
          :deep(.workflow-timeline .status-tag),
          :deep(.workflow-timeline .el-tag),
          :deep(.content-meta .status-tag),
          :deep(.content-meta .el-tag),
          :deep(.content-meta .status-tag.el-tag),
          :deep(.content-meta .el-tag.status-tag) {
            padding: 4px 8px !important; // 🔥 与列表页一致
            border-radius: 12px !important; // 🔥 圆角处理，与列表页一致（必须生效）
            font-size: 12px !important; // 🔥 与列表页一致
            font-weight: 500 !important; // 🔥 与列表页一致
            display: inline-flex !important;
            align-items: center !important;
            gap: 4px !important;
            transition: all 0.2s ease !important;
            position: relative !important;
            
            // 🔥 成功状态样式：墨绿色（仅处理状态区域）
            &.el-tag--success {
              background-color: var(--color-success-bg) !important;
              color: var(--color-success) !important;
              border-color: var(--color-success-bg) !important;
              border-radius: 12px !important;
            }
            
            // 🔥 警告状态样式：与列表页保持一致
            &.el-tag--warning {
              background-color: #fef3c7 !important;
              color: #f59e0b !important;
              border-color: #fef3c7 !important;
              border-radius: 12px !important; // 🔥 再次强调圆角
            }
            
            // 🔥 危险状态样式：与列表页保持一致
            &.el-tag--danger {
              background-color: #fee2e2 !important;
              color: #dc2626 !important;
              border-color: #fee2e2 !important;
              border-radius: 12px !important; // 🔥 再次强调圆角
            }
            
            // 🔥 信息状态样式：与列表页保持一致
            &.el-tag--info {
              background-color: #e0e7ff !important;
              color: #2563eb !important;
              border-color: #e0e7ff !important;
              border-radius: 12px !important; // 🔥 再次强调圆角
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
        // 🔥 表单标签字体：比内容大，使用表头颜色
        font-size: 14px; // 标签字体大小（比内容大）
        color: var(--text-2) !important; // 表头颜色
        font-weight: 600; // 表头粗细
        white-space: nowrap;
      }
      
      .info-value {
        // 🔥 表单内容字体：比 placeholder 稍深一点
        font-size: 14px; // 与 placeholder 一致（var(--font-size-base)）
        color: var(--text-3) !important; // 比 placeholder 稍深（#6b7280，比 #9ca3af 深）
        word-break: break-word;
        font-weight: 400; // 正常粗细
      }
    }
  }
  
  // InfoCard 标题样式
  :deep(.card-title) {
    color: var(--color-primary) !important;
  }
  
  // 🔥 状态标签样式：与列表页保持一致
  :deep(.info-value) {
    .status-tag,
    .el-tag {
      padding: 4px 8px !important;
      border-radius: 12px !important; // 🔥 圆角与列表页一致
      font-size: 12px !important;
      font-weight: 500 !important;
      display: inline-flex !important;
      align-items: center !important;
      gap: 4px !important;
      transition: all 0.2s ease !important;
      
      // 成功状态样式
      &.el-tag--success {
        background-color: #d1fae5 !important; // 🔥 与列表页一致
        color: #059669 !important; // 🔥 与列表页一致
        border-color: #d1fae5 !important;
      }
      
      // 警告状态样式
      &.el-tag--warning {
        background-color: #fef3c7 !important;
        color: #f59e0b !important;
        border-color: #fef3c7 !important;
      }
      
      // 危险状态样式
      &.el-tag--danger {
        background-color: #fee2e2 !important;
        color: #dc2626 !important;
        border-color: #fee2e2 !important;
      }
      
      // 信息状态样式
      &.el-tag--info {
        background-color: #e0e7ff !important;
        color: #2563eb !important;
        border-color: #e0e7ff !important;
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

