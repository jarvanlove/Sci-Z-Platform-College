<!--
/**
 * @description 项目进度业务组件
 * 展示项目进度概览、里程碑时间轴、进度统计等信息
 */
-->
<template>
  <div class="project-progress-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <BackButton @click="handleBack" />
        <h1 class="page-title">{{ $t('project.progress.title') }}</h1>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>{{ $t('common.loading') }}</span>
    </div>

    <!-- 主要内容 -->
    <div v-else>
      <!-- 项目概览 -->
      <BaseCard class="overview-card">
        <template #header>
          <h2 class="card-title">{{ $t('project.progress.overview') }}</h2>
        </template>

        <div class="overview-grid">
          <!-- 项目基本信息 -->
          <div class="info-section">
            <h3 class="section-title">{{ $t('project.progress.projectInfo') }}</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">{{ $t('project.progress.name') }}：</span>
                <span class="info-value">{{ projectInfo.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ $t('project.progress.number') }}：</span>
                <span class="info-value">{{ projectInfo.number }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ $t('project.progress.manager') }}：</span>
                <span class="info-value">{{ projectInfo.manager }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ $t('project.progress.status') }}：</span>
                <el-tag :type="getStatusTagType(projectInfo.status)" size="small" round>
                  {{ getStatusText(projectInfo.status) }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 整体进度 -->
          <div class="info-section">
            <h3 class="section-title">{{ $t('project.progress.overallProgress') }}</h3>
            <div class="progress-display">
              <div class="progress-percentage">{{ overallProgress }}%</div>
              <div class="progress-bar-large">
                <div
                  class="progress-fill-large"
                  :style="{ width: overallProgress + '%' }"
                ></div>
              </div>
              <div class="progress-time">
                <div>{{ $t('project.progress.startTime') }}：{{ projectStartTime }}</div>
                <div>{{ $t('project.progress.endTime') }}：{{ projectEndTime }}</div>
              </div>
            </div>
          </div>
        </div>
      </BaseCard>

      <!-- 时间轴视图 -->
      <BaseCard class="timeline-card">
        <template #header>
          <h2 class="card-title">{{ $t('project.progress.timeline') }}</h2>
        </template>

        <div v-if="milestones.length > 0" class="timeline-container">
          <div class="timeline-line"></div>
          <div v-for="milestone in milestones" :key="milestone.id" class="timeline-item">
            <div :class="`timeline-dot ${milestone.status}`"></div>
            <div class="timeline-content">
              <div class="timeline-title">{{ milestone.name }}</div>
              <div class="timeline-time">
                {{ milestone.startTime }} - {{ milestone.endTime }}
              </div>
              <div class="timeline-description">
                {{ milestone.description }}
              </div>
              <div class="timeline-progress">
                <ProjectProgressBar
                  :progress="milestone.progress"
                  :height="6"
                  :show-text="true"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-state-icon">📅</div>
          <div class="empty-state-text">{{ $t('project.progress.noTimeline') }}</div>
          <div class="empty-state-hint">{{ $t('project.progress.timelineHint') }}</div>
        </div>
      </BaseCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { BaseCard, BackButton, ProjectProgressBar } from '@/components/Common'
import { PROJECT_STATUS, PROJECT_STATUS_CONFIG } from '@/utils/constants'
import { getProjectProgress } from '@/api/Project/project'
import { createLogger } from '@/utils/simpleLogger'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const logger = createLogger('ProjectProgress')

// 响应式数据
const loading = ref(false)
const milestones = ref([])
const overallProgress = ref(0)
const projectStartTime = ref('')
const projectEndTime = ref('')

// 项目信息（通过接口获取）
const projectInfo = ref({
  name: '',
  number: '',
  manager: '',
  status: 'progress'
})

// 状态映射：将后端状态值映射到前端状态值
const mapStatus = (status) => {
  // 如果状态是字符串数字，映射到前端状态值
  const statusNumberMap = {
    '1': PROJECT_STATUS.PROGRESS,  // 进行中
    '2': PROJECT_STATUS.COMPLETED, // 已完成
    '3': PROJECT_STATUS.DELAYED,   // 已延期
    '4': PROJECT_STATUS.CANCELLED  // 已取消
  }
  
  // 如果状态是字符串描述，映射到前端状态值
  const statusTextMap = {
    'in_progress': PROJECT_STATUS.PROGRESS,
    'progress': PROJECT_STATUS.PROGRESS,
    '进行中': PROJECT_STATUS.PROGRESS,
    'completed': PROJECT_STATUS.COMPLETED,
    '已完成': PROJECT_STATUS.COMPLETED,
    'delayed': PROJECT_STATUS.DELAYED,
    '已延期': PROJECT_STATUS.DELAYED,
    'cancelled': PROJECT_STATUS.CANCELLED,
    '已取消': PROJECT_STATUS.CANCELLED
  }
  
  // 优先匹配数字状态，再匹配文本状态
  return statusNumberMap[String(status)] || statusTextMap[status] || status
}

// 获取状态文本
const getStatusText = (status) => {
  const mappedStatus = mapStatus(status)
  return PROJECT_STATUS_CONFIG[mappedStatus]?.text || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const mappedStatus = mapStatus(status)
  return PROJECT_STATUS_CONFIG[mappedStatus]?.type || 'info'
}

// 加载项目进度数据
const loadProjectProgress = async (projectId) => {
  try {
    loading.value = true
    logger.info('Loading project progress', { projectId })
    
    const response = await getProjectProgress(projectId)
    const data = response.data
    
    // 映射后端数据到前端格式（根据接口返回结构）
    // projectInfo 对象
    if (data.projectInfo) {
      projectInfo.value = {
        name: data.projectInfo.projectName || data.projectInfo.name || '',
        number: data.projectInfo.projectNumber || data.projectInfo.number || '',
        manager: data.projectInfo.projectManager || data.projectInfo.manager || '',
        status: mapStatus(data.projectInfo.projectStatus || data.projectInfo.status || 'progress')
      }
    } else {
      // 兼容旧格式
      projectInfo.value = {
        name: data.projectName || data.name || '',
        number: data.projectNumber || data.number || '',
        manager: data.manager || '',
        status: mapStatus(data.status || 'progress')
      }
    }
    
    // overallProgress 对象
    if (data.overallProgress && typeof data.overallProgress === 'object') {
      overallProgress.value = data.overallProgress.percentage || data.overallProgress.progress || 0
      projectStartTime.value = data.overallProgress.startDate || data.overallProgress.startTime || ''
      projectEndTime.value = data.overallProgress.estimatedCompletion || data.overallProgress.endTime || ''
    } else {
      // 兼容旧格式
      overallProgress.value = data.overallProgress || data.progress || 0
      projectStartTime.value = data.startTime || data.startDate || ''
      projectEndTime.value = data.endTime || data.endDate || ''
    }
    
    // 里程碑数据映射
    milestones.value = (data.milestones || []).map(milestone => ({
      id: milestone.id,
      name: milestone.title || milestone.name || '',
      description: milestone.description || '',
      startTime: milestone.startTime || '',
      endTime: milestone.endTime || '',
      progress: milestone.progress || 0,
      status: milestone.status || 'planned'
    }))
    
    logger.info('Project progress loaded successfully', { 
      projectId, 
      progress: overallProgress.value,
      milestonesCount: milestones.value.length 
    })
  } catch (error) {
    logger.error('Failed to load project progress', error)
    ElMessage.error(t('project.progress.loadError'))
  } finally {
    loading.value = false
  }
}

// 返回列表页
const handleBack = () => {
  router.push('/project/list')
}

// 组件挂载时加载数据
onMounted(() => {
  // 从路由参数获取项目ID
  const projectId = route.params.id
  
  if (projectId) {
    loadProjectProgress(projectId)
  } else {
    logger.warn('No project ID found in route parameters')
    ElMessage.warning(t('project.progress.noProjectId'))
  }
})
</script>

<style lang="scss" scoped>
.project-progress-container {
  padding: var(--gap-lg);
  background: var(--bg-secondary, #f7f9fc);
  min-height: calc(100vh - 56px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gap-lg);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--gap-md);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary, #1e3a8a);
  margin: 0;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: var(--gap-sm);
  color: var(--text-3, #6b7280);
}

.overview-card,
.timeline-card {
  margin-bottom: var(--gap-lg);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary, #1e3a8a);
  margin: 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--gap-lg);
}

.info-section {
  padding: var(--gap-md);
  background: var(--bg-tertiary, #f8fafc);
  border-radius: var(--radius-md, 8px);
  border: 1px solid var(--border, #e5e7eb);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-2, #374151);
  margin-bottom: var(--gap-md);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--gap-sm);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  color: var(--text-3, #6b7280);
  font-size: 14px;
}

.info-value {
  color: var(--text-1, #111827);
  font-weight: 500;
  font-size: 14px;
}


// 整体进度样式
.progress-display {
  text-align: center;
}

.progress-percentage {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-primary, #1e3a8a);
  margin-bottom: var(--gap-sm);
}

.progress-bar-large {
  width: 100%;
  height: 12px;
  background: var(--border, #e5e7eb);
  border-radius: var(--radius-sm, 6px);
  overflow: hidden;
  margin-bottom: var(--gap-md);
}

.progress-fill-large {
  height: 100%;
  background: linear-gradient(90deg, #1e3a8a 0%, #0ea5e9 100%);
  border-radius: var(--radius-sm, 6px);
  transition: width 0.5s ease;
}

.progress-time {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-3, #6b7280);
}

// 时间轴样式
.timeline-container {
  position: relative;
  padding-left: 24px;
}

.timeline-line {
  position: absolute;
  left: 12px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--border, #e5e7eb);
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
  padding-left: 24px;
}

.timeline-dot {
  position: absolute;
  left: -6px;
  top: 8px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 3px solid var(--surface, #ffffff);
  box-shadow: 0 0 0 2px var(--border, #e5e7eb);
}

.timeline-dot.completed {
  background: var(--milestone-completed-color, #16a34a);
  box-shadow: 0 0 0 2px var(--milestone-completed-color, #16a34a);
}

.timeline-dot.progress {
  background: var(--milestone-progress-color, #f59e0b);
  box-shadow: 0 0 0 2px var(--milestone-progress-color, #f59e0b);
}

.timeline-dot.planned {
  background: var(--milestone-planned-color, #2563eb);
  box-shadow: 0 0 0 2px var(--milestone-planned-color, #2563eb);
}

.timeline-dot.delayed {
  background: var(--milestone-delayed-color, #dc2626);
  box-shadow: 0 0 0 2px var(--milestone-delayed-color, #dc2626);
}

.timeline-content {
  background: var(--surface, #ffffff);
  border-radius: var(--radius-md, 8px);
  padding: var(--gap-md);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-1, #111827);
  margin-bottom: 4px;
}

.timeline-time {
  font-size: 12px;
  color: var(--text-3, #6b7280);
  margin-bottom: var(--gap-sm);
}

.timeline-description {
  font-size: 14px;
  color: var(--text-2, #374151);
  line-height: 1.5;
  margin-bottom: var(--gap-sm);
}

.timeline-progress {
  margin-top: var(--gap-sm);
}

// 空状态样式
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-3, #6b7280);
}

.empty-state-icon {
  font-size: 48px;
  margin-bottom: var(--gap-md);
  opacity: 0.5;
}

.empty-state-text {
  font-size: 16px;
  margin-bottom: var(--gap-sm);
}

.empty-state-hint {
  font-size: 14px;
  color: var(--text-4, #9ca3af);
}

// 响应式设计
@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .progress-time {
    flex-direction: column;
    gap: 4px;
  }
}
</style>

