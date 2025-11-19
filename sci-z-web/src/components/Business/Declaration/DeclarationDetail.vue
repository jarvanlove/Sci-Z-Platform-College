<template>
  <div class="declaration-detail-container">
    <div class="page-header">
      <div class="brand">
        <img :src="brandLogo" :alt="$t('app.title')" class="brand-logo" />
      </div>
      <h1 class="page-title">{{ $t('declaration.detailTitle') }}</h1>
    </div>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>{{ $t('common.loading') }}</span>
    </div>

    <template v-else>
      <div class="detail-card">
        <div class="card-title">{{ $t('declaration.basicInfo') }}</div>
        <div class="info-grid">
          <div class="info-item" v-for="item in basicInfoItems" :key="item.label">
            <div class="info-label">{{ item.label }}</div>
            <div class="info-value">{{ item.value }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">{{ $t('declaration.currentStatus') }}</div>
            <div class="info-value">
              <el-tag
                :type="getStatusTagType(declaration.statusType)"
                :class="`status-${declaration.statusType}`"
                size="large"
              >
                {{ getStatusLabel(declaration.statusType) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-card">
        <div class="card-title">{{ $t('declaration.researchInfo') }}</div>
        <div class="info-item">
          <div class="info-label">{{ $t('declaration.direction') }}</div>
          <div class="research-direction">{{ declaration.direction }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('declaration.topic') }}</div>
          <div class="research-direction">{{ declaration.topic }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('declaration.fields') }}</div>
          <div class="field-tags">
            <el-tag
              v-for="field in declaration.fields"
              :key="field"
              class="field-tag"
              size="large"
            >
              {{ field }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="detail-card">
        <div class="card-title">{{ $t('declaration.workflowStatus') }}</div>
        <div class="workflow-container">
          <div class="workflow-status">
            <div class="workflow-icon" :class="workflowStatus.type">
              {{ workflowStatus.icon }}
            </div>
            <div class="workflow-info">
              <div class="workflow-title">{{ workflowStatus.title }}</div>
              <div class="workflow-description">{{ workflowStatus.description }}</div>
              <div v-if="workflowStatus.showProgress" class="progress-bar">
                <el-progress
                  :percentage="workflowStatus.progress"
                  :color="getProgressColor(workflowStatus.progress)"
                  :stroke-width="6"
                  :show-text="false"
                />
              </div>
            </div>
          </div>

          <div v-if="workflowTimeline.length" class="workflow-timeline">
            <div
              v-for="item in workflowTimeline"
              :key="item.id"
              class="timeline-item"
            >
              <div class="timeline-icon" :class="item.status">
                {{ item.icon }}
              </div>
              <div class="timeline-content">
                <div class="timeline-title">{{ item.title }}</div>
                <div class="timeline-time">{{ item.time }}</div>
              </div>
            </div>
          </div>

          <div v-if="workflowResult" class="result-container">
            <div v-if="declaration.statusType === 'success'" class="result-success">
              <div class="result-title">
                <el-icon><Check /></el-icon>
                {{ $t('declaration.processSuccess') }}
              </div>
              <div class="result-content">
                <p>
                  <strong>{{ $t('declaration.projectId') }}:</strong>
                  {{ workflowResult.projectId }}
                </p>
                <p>
                  <strong>{{ $t('declaration.projectName') }}:</strong>
                  {{ workflowResult.projectName }}
                </p>
                <p>
                  <strong>{{ $t('declaration.projectDescription') }}:</strong>
                  {{ workflowResult.projectDescription }}
                </p>
                <p>
                  <a class="project-link" href="javascript:void(0)" @click.prevent="handleViewProject">
                    <el-icon><View /></el-icon>
                    {{ $t('declaration.viewProject') }}
                  </a>
                </p>
              </div>
            </div>
            <div v-else-if="declaration.statusType === 'failed'" class="result-failed">
              <div class="result-title">
                <el-icon><Close /></el-icon>
                {{ $t('declaration.processFailed') }}
              </div>
              <div class="result-content">
                <p>
                  <strong>{{ $t('declaration.failureReason') }}:</strong>
                  {{ workflowResult.errorMessage }}
                </p>
                <p>
                  <strong>{{ $t('declaration.suggestion') }}:</strong>
                  {{ workflowResult.suggestion }}
                </p>
                <div class="error-details" v-if="workflowResult.errorDetails">
                  {{ workflowResult.errorDetails }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-card" v-if="showActionButtons">
        <div class="action-buttons">
          <el-button
            v-if="declaration.statusType === 'failed'"
            type="info"
            @click="handleRedeclare"
          >
            <el-icon><Refresh /></el-icon>
            {{ $t('declaration.redeclare') }}
          </el-button>
          <el-button
            v-if="declaration.statusType === 'success'"
            type="primary"
            @click="handleViewProject"
          >
            <el-icon><View /></el-icon>
            {{ $t('declaration.viewProject') }}
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Loading, Check, Close, Refresh, View } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/date'
import { DECLARATION_STATUS_CONFIG } from '@/utils/constants'
import { createLogger } from '@/utils/simpleLogger'
import brandLogo from '@/assets/images/logo.svg'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const logger = createLogger('DeclarationDetail')

const declaration = ref({
  id: '',
  number: '',
  applicant: '',
  department: '',
  projectLeader: '',
  documentPublishTime: '',
  projectStartTime: '',
  projectEndTime: '',
  direction: '',
  topic: '',
  fields: [],
  submitTime: '',
  statusType: 'submitting',
  progress: 0
})

const loading = ref(false)
const workflowTimeline = ref([])
const workflowResult = ref(null)

const statusLabelMap = computed(() => ({
  submitting: t('declaration.statusSubmitting'),
  success: t('declaration.statusSuccess'),
  failed: t('declaration.statusFailed')
}))

const getStatusLabel = (type) => statusLabelMap.value[type] || type

const basicInfoItems = computed(() => [
  { label: t('declaration.number'), value: declaration.value.number },
  { label: t('declaration.applicant'), value: declaration.value.applicant },
  { label: t('declaration.department'), value: declaration.value.department },
  { label: t('declaration.projectLeader'), value: declaration.value.projectLeader },
  { label: t('declaration.documentPublishTime'), value: formatDate(declaration.value.documentPublishTime) },
  { label: t('declaration.projectStartTime'), value: formatDate(declaration.value.projectStartTime) },
  { label: t('declaration.projectEndTime'), value: formatDate(declaration.value.projectEndTime) },
  { label: t('declaration.submitTime'), value: declaration.value.submitTime }
])

const workflowStatus = computed(() => {
  const status = declaration.value.statusType
  if (status === 'submitting') {
    return {
      type: 'processing',
      icon: '⏳',
      title: t('declaration.processing'),
      description: t('declaration.processingDesc'),
      showProgress: true,
      progress: declaration.value.progress || 65
    }
  }
  if (status === 'success') {
    return {
      type: 'completed',
      icon: '✓',
      title: t('declaration.completed'),
      description: t('declaration.completedDesc'),
      showProgress: false,
      progress: 100
    }
  }
  if (status === 'failed') {
    return {
      type: 'failed',
      icon: '✗',
      title: t('declaration.failed'),
      description: t('declaration.failedDesc'),
      showProgress: false,
      progress: 0
    }
  }
  return {
    type: 'pending',
    icon: '?',
    title: t('declaration.unknown'),
    description: t('declaration.unknownDesc'),
    showProgress: false,
    progress: 0
  }
})

const showActionButtons = computed(() => ['success', 'failed'].includes(declaration.value.statusType))

const getStatusTagType = (statusType) => DECLARATION_STATUS_CONFIG[statusType]?.type || 'info'

const getProgressColor = (progress) => {
  if (progress <= 30) return '#dc2626'
  if (progress <= 70) return '#f59e0b'
  return '#16a34a'
}

const loadDeclarationDetail = async () => {
  try {
    loading.value = true
    logger.info('Loading declaration detail', { id: route.params.id })

    await new Promise(resolve => setTimeout(resolve, 700))

    declaration.value = {
      id: route.params.id || 1,
      number: 'SB2024001',
      applicant: '张教授',
      department: '科研管理部',
      projectLeader: '张教授',
      documentPublishTime: '2024-01-10',
      projectStartTime: '2024-02-01',
      projectEndTime: '2024-12-31',
      direction: '基于深度学习的智能制造关键技术研究，聚焦工业互联网、智能传感器、机器视觉等核心方向，构建可落地的智能制造平台。',
      topic: '面向智能制造的多模态数据融合与决策优化技术研究',
      fields: ['人工智能', '智能制造', '机器学习', '工业互联网', '数据融合'],
      submitTime: '2024-01-15 14:30',
      statusType: 'submitting',
      progress: 65
    }

    workflowTimeline.value = [
      { id: 1, title: t('declaration.timelineSubmit'), time: '2024-01-15 14:30:25', status: 'completed', icon: '✓' },
      { id: 2, title: t('declaration.timelineStart'), time: '2024-01-15 14:30:28', status: 'completed', icon: '✓' },
      { id: 3, title: t('declaration.timelineAnalysis'), time: '2024-01-15 14:31:15', status: 'completed', icon: '✓' },
      { id: 4, title: t('declaration.timelineGenerate'), time: '2024-01-15 14:32:45', status: 'completed', icon: '✓' },
      { id: 5, title: t('declaration.timelineStore'), time: '2024-01-15 14:33:00', status: 'completed', icon: '✓' },
      { id: 6, title: t('declaration.timelineDocument'), time: '2024-01-15 14:33:30', status: 'completed', icon: '✓' }
    ]

    if (declaration.value.statusType === 'success') {
      workflowResult.value = {
        projectId: 'PRJ_20240115_001',
        projectName: t('declaration.sampleProjectName'),
        projectDescription: t('declaration.sampleProjectDesc')
      }
    } else if (declaration.value.statusType === 'failed') {
      workflowResult.value = {
        errorMessage: t('declaration.sampleErrorMessage'),
        suggestion: t('declaration.sampleSuggestion'),
        errorDetails: t('declaration.sampleErrorDetails')
      }
    } else {
      workflowResult.value = null
    }

    logger.info('Declaration detail loaded', { id: declaration.value.id })
  } catch (error) {
    logger.error('Failed to load declaration detail', error)
    ElMessage.error(t('declaration.loadError'))
  } finally {
    loading.value = false
  }
}

const handleRedeclare = () => {
  ElMessage.info(t('declaration.redeclareInfo'))
  router.push('/declaration/create')
}

const handleViewProject = () => {
  ElMessage.info(t('declaration.viewProjectInfo'))
  router.push('/project/detail/preview')
}

onMounted(() => {
  loadDeclarationDetail()
})
</script>

<style scoped lang="scss">
.declaration-detail-container {
  padding: 20px;
  background: #f7f9fc;
  min-height: calc(100vh - 56px);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.brand {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.brand-logo {
  width: 46px;
  height: 46px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e3a8a;
  margin: 0;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 0;
  color: #6b7280;
}

.detail-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e3a8a;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e5e7eb;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}

.info-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.info-value {
  font-size: 16px;
  color: #1f2937;
  font-weight: 500;
}

:deep(.el-tag.status-submitting) {
  background-color: #fef3c7 !important;
  color: #f59e0b !important;
  border-color: #fef3c7 !important;
}

:deep(.el-tag.status-success) {
  background-color: #dcfce7 !important;
  color: #16a34a !important;
  border-color: #dcfce7 !important;
}

:deep(.el-tag.status-failed) {
  background-color: #fee2e2 !important;
  color: #dc2626 !important;
  border-color: #fee2e2 !important;
}

.field-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.field-tag {
  background: #e0e7ff;
  color: #1e3a8a;
  border: none;
}

.research-direction {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #1f2937;
}

.workflow-container {
  margin-top: 8px;
}

.workflow-status {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

.workflow-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #ffffff;

  &.processing {
    background: #f59e0b;
    animation: pulse 2s infinite;
  }

  &.completed {
    background: #16a34a;
  }

  &.failed {
    background: #dc2626;
  }

  &.pending {
    background: #9ca3af;
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }
}

.workflow-info {
  flex: 1;
}

.workflow-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.workflow-description {
  font-size: 14px;
  color: #6b7280;
}

.workflow-timeline {
  margin-top: 16px;
}

.timeline-item {
  display: flex;
  gap: 12px;
  position: relative;
  padding: 12px 0;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    left: 12px;
    top: 32px;
    bottom: -12px;
    width: 2px;
    background: #e5e7eb;
  }
}

.timeline-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 12px;
  z-index: 1;

  &.completed {
    background: #16a34a;
  }

  &.processing {
    background: #f59e0b;
  }

  &.failed {
    background: #dc2626;
  }
}

.timeline-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.timeline-time {
  font-size: 12px;
  color: #6b7280;
}

.result-container {
  margin-top: 16px;
}

.result-success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 16px;
}

.result-failed {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 16px;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.result-success .result-title {
  color: #16a34a;
}

.result-failed .result-title {
  color: #dc2626;
}

.result-content {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.7;
}

.project-link {
  color: #1e3a8a;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &:hover {
    text-decoration: underline;
  }
}

.error-details {
  margin-top: 10px;
  background: #fff;
  padding: 12px;
  border-radius: 6px;
  border: 1px dashed #fecaca;
  font-family: monospace;
  font-size: 12px;
  color: #b91c1c;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
