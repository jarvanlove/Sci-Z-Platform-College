<!--
/**
 * @description 仪表板业务组件
 * 展示统计数据、最近申报、项目进度、快捷操作等
 */
-->
<template>
  <BaseScrollbar 
    class="dashboard-container"
    :custom-style="{ height: '100%', overflow: 'auto' }"
  >
    <!-- 页面头部 -->
    <div class="page-header">
      <BackButton :tooltip="$t('practice.backToPractice')" @click="handleBack" />
      <h1 class="page-title">{{ $t('menu.dashboard') }}</h1>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-cards">
      <div
        v-for="stat in stats"
        :key="stat.key"
        class="stat-card"
        :class="`stat-${stat.key}`"
      >
        <div class="stat-icon">{{ stat.icon }}</div>
        <div class="stat-value" :class="stat.valueClass">{{ formatNumber(stat.value) }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 顶部区域 -->
      <div class="top-section">
        <!-- 最近申报 -->
        <BaseCard class="declaration-card content-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">{{ $t('dashboard.recentDeclarations') }}</h3>
              <a href="#" class="card-action" @click.prevent="handleViewAllDeclarations">
                {{ $t('dashboard.viewAll') }}
              </a>
            </div>
          </template>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-skeleton">
            <el-skeleton :rows="5" animated />
          </div>

          <!-- 申报数据表格 - 使用 Element Plus el-table -->
          <el-table
            v-else
            :data="recentDeclarations"
            :empty-text="$t('common.noData')"
            :show-header="true"
            stripe
            class="declaration-table"
          >
            <el-table-column
              prop="number"
              :label="$t('dashboard.declarationNumber')"
              min-width="120"
              align="center"
            >
              <template #default="{ row }">
                <span class="number-cell" style="white-space: nowrap;">{{ row.number }}</span>
              </template>
            </el-table-column>

            <el-table-column
              prop="topic"
              :label="$t('declaration.topic')"
              min-width="200"
              show-overflow-tooltip
            >
              <template #default="{ row }">
                <div class="topic-cell">{{ row.topic }}</div>
              </template>
            </el-table-column>

            <el-table-column
              prop="submitTime"
              :label="$t('dashboard.submitTime')"
              min-width="120"
              align="center"
            >
              <template #default="{ row }">
                <span class="time-cell">{{ row.submitTime }}</span>
              </template>
            </el-table-column>

            <el-table-column
              prop="status"
              :label="$t('common.status')"
              min-width="100"
              align="center"
            >
              <template #default="{ row }">
                <span
                  class="status-tag"
                  :class="`status-${row.statusType}`"
                >
                  {{ row.status }}
                </span>
              </template>
            </el-table-column>

            <el-table-column
              :label="$t('common.actions')"
              min-width="80"
              align="center"
              fixed="right"
            >
              <template #default="{ row }">
                <BaseTooltip :content="$t('common.view')" placement="top">
                  <button
                    class="action-btn btn-primary"
                    @click.stop="handleDeclarationClick(row)"
                  >
                    <el-icon><TopRight /></el-icon>
                  </button>
                </BaseTooltip>
              </template>
            </el-table-column>
          </el-table>
        </BaseCard>

        <!-- 快捷操作 -->
        <BaseCard class="quick-actions-card content-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">{{ $t('dashboard.quickActions') }}</h3>
            </div>
          </template>
          <div class="quick-actions">
            <button
              v-for="action in quickActions"
              :key="action.key"
              class="action-button"
              :class="action.buttonClass"
              @click="handleQuickAction(action.key)"
            >
              <el-icon class="action-icon"><component :is="action.icon" /></el-icon>
              {{ action.label }}
            </button>
          </div>
        </BaseCard>
      </div>

      <!-- 底部区域 -->
      <div class="bottom-section">
        <!-- 项目进度概览 -->
        <BaseCard class="progress-card content-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">{{ $t('dashboard.projectProgress') }}</h3>
              <a href="#" class="card-action" @click.prevent="handleViewAllProjects">
                {{ $t('dashboard.viewAll') }}
              </a>
            </div>
          </template>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-skeleton">
            <el-skeleton :rows="3" animated />
          </div>

          <!-- 项目进度列表 -->
          <div v-else class="progress-grid">
            <div
              v-for="project in projectProgress"
              :key="project.id"
              class="progress-item"
              @click="handleProjectClick(project.id)"
            >
              <div class="progress-info">
                <div class="progress-name">{{ project.name }}</div>
                <div class="progress-time">
                  {{ $t('dashboard.expectedCompletion') }}: {{ project.expectedDate }}
                </div>
              </div>
              <div class="progress-bar-container">
                <ProjectProgressBar
                  :progress="project.progress"
                  :height="8"
                  :show-text="false"
                />
              </div>
              <div class="progress-percentage">{{ project.progress }}%</div>
            </div>
          </div>
        </BaseCard>
      </div>
    </div>
  </BaseScrollbar>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Document, Folder, Check, Search, TopRight, Reading } from '@element-plus/icons-vue'
import { BaseCard, ProjectProgressBar, BaseScrollbar, BaseTooltip, BackButton } from '@/components/Common'
import { formatDate } from '@/utils/date'
import { DECLARATION_STATUS_CONFIG } from '@/utils/constants'
import { createLogger } from '@/utils/simpleLogger'
import { getDeclarationList } from '@/api/Declaration'
import { getProjectList, getProjectStatistics } from '@/api/Project'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('Dashboard')

// 响应式数据
const loading = ref(false)

// 统计数据 - 使用 computed 使标签响应语言切换
const stats = computed(() => [
  { key: 'total', icon: '📁', value: statsValues.value.total, label: t('dashboard.totalProjects'), valueClass: 'stat-total' },
  { key: 'progress', icon: '⏰', value: statsValues.value.progress, label: t('dashboard.inProgress'), valueClass: 'stat-progress' },
  { key: 'pending', icon: '⏳', value: statsValues.value.pending, label: t('dashboard.delayedProjects'), valueClass: 'stat-pending' },
  { key: 'completed', icon: '✅', value: statsValues.value.completed, label: t('dashboard.completed'), valueClass: 'stat-completed' }
])

// 统计数据值
const statsValues = ref({
  total: 0,
  progress: 0,
  pending: 0,
  completed: 0
})

const recentDeclarations = ref([])
const projectProgress = ref([])

// 快捷操作 - 使用 computed 使标签响应语言切换
const quickActions = computed(() => [
  { key: 'newDeclaration', icon: Document, buttonClass: 'primary', label: t('dashboard.newDeclaration') },
  { key: 'projectList', icon: Folder, buttonClass: 'secondary', label: t('dashboard.projectList') },
  { key: 'applyAcceptance', icon: Check, buttonClass: 'secondary', label: t('dashboard.applyAcceptance') },
  { key: 'academicSearch', icon: Reading, buttonClass: 'secondary', label: t('dashboard.academicSearch') },
  { key: 'knowledgeSearch', icon: Search, buttonClass: 'text', label: t('dashboard.knowledgeSearch') }
])


// 格式化数字
const formatNumber = (num) => {
  return num.toLocaleString()
}

// 获取状态标签类型
const getStatusTagType = (statusType) => {
  const typeMap = {
    submitting: 'warning',
    success: 'success',
    failed: 'danger',
    withdrawn: 'info'
  }
  return typeMap[statusType] || 'info'
}

// 注意：进度条颜色逻辑已移至 ProjectProgressBar 组件中

// 状态数字到字符串的映射
const STATUS_MAP = {
  1: { type: 'submitting', label: '申报已提交' },
  2: { type: 'success', label: '申报成功' },
  3: { type: 'failed', label: '申报未通过' }
}

// 将后端状态数字转换为前端状态类型
const mapStatusType = (status) => {
  return STATUS_MAP[status]?.type || 'submitting'
}

// 格式化提交时间 - 格式：yyyy-mm-dd
const formatSubmitTime = (timeStr) => {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (e) {
    return timeStr
  }
}

// 加载数据
const loadDashboardData = async () => {
  try {
    loading.value = true
    logger.info('Starting to load dashboard data')

    // 获取项目统计信息
    try {
      const statsResponse = await getProjectStatistics()
      if (statsResponse.code === 200 && statsResponse.data) {
        statsValues.value.total = statsResponse.data.totalProjects || 0
        statsValues.value.progress = statsResponse.data.inProgressCount || 0
        statsValues.value.pending = statsResponse.data.delayedCount || 0
        statsValues.value.completed = statsResponse.data.completedCount || 0
        logger.info('Project statistics loaded successfully', statsResponse.data)
      }
    } catch (error) {
      logger.error('Failed to load project statistics', error)
      // 统计信息加载失败不影响其他数据展示
    }

    // 使用申报分页查询接口，获取最近5条
    const declarationsResponse = await getDeclarationList({
      pageNo: 1,
      pageSize: 5,
      sortBy: 'submitTime',
      sortOrder: 'DESC'
    })

    // 更新最近申报数据（限制5条）
    if (declarationsResponse.code === 200 && declarationsResponse.data) {
      const records = declarationsResponse.data.records || []
      recentDeclarations.value = records.slice(0, 5).map(item => ({
        id: item.id,
        number: item.number || '-',
        topic: item.researchTopic || '-',
        submitTime: formatSubmitTime(item.submitTime),
        status: item.statusDescription || STATUS_MAP[item.status]?.label || '-',
        statusType: mapStatusType(item.status)
      }))
      logger.info('Recent declarations loaded successfully', { count: recentDeclarations.value.length })
    }

    // 使用项目分页查询接口，获取最近6条
    const progressResponse = await getProjectList({
      pageNo: 1,
      pageSize: 6,
      sortBy: 'createdTime',
      sortOrder: 'DESC'
    })

    // 更新项目进度数据（限制6条）
    if (progressResponse.code === 200 && progressResponse.data) {
      const records = progressResponse.data.records || []
      projectProgress.value = records.slice(0, 6).map(item => ({
        id: item.id,
        name: item.name || '-',
        progress: item.progress || 0,
        expectedDate: formatDate(item.estimatedCompletionTime || item.endTime, 'YYYY-MM-DD')
      }))
      logger.info('Project progress loaded successfully', { count: projectProgress.value.length })
    }
  } catch (error) {
    logger.error('Dashboard data loading failed', error)
    ElMessage.error(t('dashboard.loadError'))
  } finally {
    loading.value = false
    logger.info('Dashboard data loading completed')
  }
}

// 事件处理
const handleBack = () => {
  router.push('/practice')
}

const handleDeclarationClick = (item) => {
  logger.info('User clicked declaration item', { id: item.id, number: item.number })
  router.push(`/declaration/detail/${item.id}`)
}

const handleViewAllDeclarations = () => {
  logger.info('User clicked view all declarations')
  router.push('/declaration/list')
}

const handleProjectClick = (projectId) => {
  logger.info('User clicked project', { projectId })
  router.push(`/project/detail/${projectId}`)
}

const handleViewAllProjects = () => {
  logger.info('User clicked view all projects')
  router.push('/project/list')
}

const handleQuickAction = (action) => {
  logger.info('User clicked quick action', { action })
  
  const pathMap = {
    newDeclaration: '/declaration/create',
    projectList: '/project/list',
    applyAcceptance: '/report/list',
    academicSearch: '/literature/search',
    knowledgeSearch: '/knowledge/list'
  }
  
  if (pathMap[action]) {
    router.push(pathMap[action])
  } else {
    logger.warn('Unknown quick action', { action })
    ElMessage.warning(t('dashboard.actionNotImplemented') || '该功能暂未实现')
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  background: var(--bg);
  min-height: calc(100vh - 56px);
  overflow-x: hidden;
  max-width: 100%;
  // 🔥 滚动条样式由 BaseScrollbar 组件提供
}

.page-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
  margin-bottom: var(--gap-lg);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: var(--radius-lg);
    opacity: 0;
    transition: opacity 0.3s ease;
    z-index: -1;
  }

  // 根据不同类型设置不同的渐变色
  &.stat-total::before {
    background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  }

  &.stat-progress::before {
    background: linear-gradient(135deg, #d97706 0%, #f59e0b 50%, #fbbf24 100%);
  }

  &.stat-pending::before {
    background: linear-gradient(135deg, #991b1b 0%, #dc2626 50%, #ef4444 100%);
  }

  &.stat-completed::before {
    background: linear-gradient(135deg, #15803d 0%, #16a34a 50%, #22c55e 100%);
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(30, 58, 138, 0.15);
    border: 1px solid transparent;

    &::before {
      opacity: 1;
    }

    .stat-icon {
      transform: scale(1.1);
    }

    .stat-value {
      color: #ffffff;
    }

    .stat-label {
      color: #e0e7ff;
    }
  }
}

.stat-icon {
  font-size: 24px;
  margin-bottom: 8px;
  transition: transform 0.3s ease;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  transition: color 0.3s ease;
}

.stat-total { color: #1e3a8a; }
.stat-progress { color: #f59e0b; }
.stat-pending { color: #2563eb; }
.stat-completed { color: #16a34a; }

.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
}

.top-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  align-items: start;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  
  // 🔥 确保两个卡片都不会溢出
  > * {
    min-width: 0; // 🔥 关键：允许 grid 项目收缩
    max-width: 100%;
    box-sizing: border-box;
  }
}

.content-card {
  // BaseCard 组件已经提供了基础样式，这里只需要覆盖特定样式
  // 注意：BaseCard 的 __content 已经有 padding，这里不需要再添加 padding
  // 如果需要自定义 padding，应该通过 :deep() 覆盖 BaseCard 的内部样式
  
  // 🔥 修复：移除外层 padding，避免与 BaseCard 的 __content padding 冲突
  // BaseCard 组件本身已经有 background、border-radius、border、box-shadow
  // 这里只需要确保样式一致性
  width: 100%;
  max-width: 100%;
  min-width: 0; // 🔥 关键：允许在 grid 中收缩
  box-sizing: border-box;
  
  // 🔥 确保 header 样式正确
  :deep(.base-card__header) {
    padding: 0;
    border-bottom: none;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--gap-lg);
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
  width: 100%;
  box-sizing: border-box;
  margin: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
  flex-shrink: 0;
}

.card-action {
  color: var(--color-primary);
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  white-space: nowrap;
  margin-left: auto;
  flex-shrink: 0;

  &:hover {
    text-decoration: underline;
  }
}

.declaration-card {
  min-height: auto;
  height: fit-content;
  width: 100%;
  max-width: 100%;
  min-width: 0; // 🔥 关键：允许在 grid 中收缩
  box-sizing: border-box;
  
  // 🔥 表格容器需要限制宽度，防止挤压右侧
  :deep(.base-card__content) {
    padding: 0;
    width: 100%;
    max-width: 100%;
    min-width: 0;
    overflow-x: auto; // 🔥 允许表格横向滚动
    overflow-y: visible;
    box-sizing: border-box;
  }
}

.quick-actions-card {
  min-height: auto;
  height: fit-content;
  width: 100%;
  max-width: 100%;
  min-width: 0; // 🔥 关键：允许在 grid 中收缩
  box-sizing: border-box;
  overflow: hidden; // 🔥 防止内容溢出
  
  // 🔥 快捷操作区域需要 padding
  :deep(.base-card__content) {
    padding: var(--gap-lg);
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
}

.progress-card {
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  box-sizing: border-box;
  
  // 🔥 项目进度区域需要 padding
  :deep(.base-card__content) {
    padding: var(--gap-lg);
  }
}


// Element Plus 表格样式 - 与 DeclarationList.vue 保持一致
.declaration-table {
  width: 100%;
  max-width: 100%; // 🔥 限制最大宽度，防止溢出
  border-radius: 8px;
  overflow: hidden; // 🔥 改为 hidden，防止内容溢出
  border: 1px solid var(--border);
  box-sizing: border-box; // 🔥 确保盒模型正确

  // 🔥 使用最强的选择器优先级，确保覆盖所有全局样式
  // 关键：使用 .declaration-table 类名 + 完整的选择器路径 + 多重选择器
  :deep(.el-table) {
    width: 100%;
    max-width: 100%;
    
    // 表头样式 - 使用最高优先级覆盖全局样式
    .el-table__header-wrapper {
      width: 100%;
      
      .el-table__header {
        width: 100% !important;
        
        th {
          padding: 14px 16px !important;
          font-size: 14px !important;
          font-weight: 600 !important; // 🔥 加粗
          color: #000000 !important; // 🔥 变黑 - 覆盖所有全局样式
          background-color: var(--surface) !important;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          border-bottom: 1px solid var(--border) !important;
        }
        
        // 🔥 确保表头单元格内的文字也应用黑色和加粗（使用独立选择器提高优先级）
        th .cell {
          font-weight: 600 !important; // 🔥 加粗
          color: #000000 !important; // 🔥 变黑 - 覆盖所有全局样式
          font-size: 14px !important;
          line-height: 1.5 !important;
        }
      }
    }
    
    // 🔥 确保表体容器宽度正确
    .el-table__body-wrapper {
      width: 100%;
      max-width: 100%;
    }
  }
  
  // 🔥 额外的样式规则，使用更直接的选择器确保覆盖
  :deep(.el-table__header th),
  :deep(.el-table__header th.cell),
  :deep(.el-table__header th .cell) {
    font-weight: 600 !important; // 🔥 加粗
    color: #000000 !important; // 🔥 变黑
    font-size: 14px !important;
  }
  
  // 表体样式
  :deep(.el-table) {
    .el-table__body {
      width: 100%;
      
      tr {
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background-color: var(--hover) !important;
        }
      }

      td {
        padding: 12px 16px !important;
        font-size: 14px;
        color: var(--text);
        border-bottom: 1px solid var(--border) !important;
      }
    }
    
    // 操作列固定右侧
    .el-table__fixed-right {
      right: 0 !important;
      box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
      
      .el-table__fixed-body-wrapper {
        tr.el-table__row--striped {
          td {
            overflow: visible !important;
            position: relative !important;
            
            .cell {
              overflow: visible !important;
              position: relative !important;
            }
          }
        }
      }
    }
  }

  // 研究课题列允许换行
  :deep(.el-table__body-wrapper) {
    .topic-cell {
      word-break: break-word;
      white-space: normal !important; // 🔥 研究课题允许换行
      line-height: 1.5;
    }
    
    .number-cell {
      white-space: nowrap !important; // 🔥 申报编号不换行
    }
  }
}

.number-cell { 
  color: var(--text-3); 
  font-weight: 500; 
}

.topic-cell { 
  color: var(--text-1);
  word-break: break-word;
  white-space: normal;
}

.time-cell { 
  color: var(--text-3); 
}

// 状态标签样式 - 与 DeclarationList.vue 保持一致
.status-tag {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
  position: relative;
}

.status-submitting {
  background-color: #fef3c7;
  color: #f59e0b;
}

.status-success {
  background-color: #dcfce7;
  color: #16a34a;
}

.status-failed {
  background-color: #fee2e2;
  color: #dc2626;
}

.status-withdrawn {
  background-color: #e0e7ff;
  color: #2563eb;
}

// 操作按钮样式 - 与 DeclarationList.vue 保持一致
.action-btn {
  padding: 4px; // 🔥 图标按钮紧凑样式
  min-width: 32px;
  height: 28px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: none;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0; // 🔥 图标按钮不需要间距
  user-select: none;
  
  .el-icon {
    font-size: 16px; // 🔥 图标大小
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    
    &:hover {
      background: none;
      color: inherit;
    }
  }
  
  &.btn-primary {
    color: var(--color-primary);
    border-color: var(--color-primary);
    
    &:hover:not(:disabled) {
      background: var(--color-primary);
      color: var(--surface);
    }
  }
  
  &.btn-success {
    color: #16a34a;
    border-color: #16a34a;
    display: inline-flex;
    align-items: center;
    
    &:hover:not(:disabled) {
      background: #16a34a;
      color: var(--surface);
    }
  }
  
  &.btn-info {
    color: var(--text-3);
    border-color: var(--text-3);
    
    &:hover:not(:disabled) {
      background: var(--text-3);
      color: var(--surface);
    }
  }
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-button {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  border-radius: 6px;
  text-decoration: none;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  box-sizing: border-box;

  .action-icon {
    margin-right: 8px;
    font-size: 16px;
  }

  &.primary {
    background-color: var(--surface);
    border: 1px solid var(--color-primary);
    color: var(--color-primary);
  }

  &.primary:hover {
    background-color: #e0f2fe;
    border-color: #1e40af;
    color: #1e40af;
  }

  &.secondary {
    background-color: var(--surface);
    border: 1px solid var(--color-primary);
    color: var(--color-primary);
  }

  &.secondary:hover {
    background-color: #e0f2fe;
    border-color: #1e40af;
    color: #1e40af;
  }

  &.text {
    background-color: var(--surface);
    color: var(--color-primary);
    border: 1px solid var(--color-primary);
  }

  &.text:hover {
    background-color: #e0f2fe;
    border-color: #1e40af;
    color: #1e40af;
  }
}

.progress-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%;
  box-sizing: border-box;
}

.progress-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 12px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s;
  gap: 8px;
  background: var(--hover);
  border: 1px solid var(--border);
  width: 100%;
  box-sizing: border-box;

  &:hover {
    background-color: var(--hover);
    border-color: var(--color-primary);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(30, 58, 138, 0.1);
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.progress-info {
  flex: 1;
  min-width: 0;
  max-width: 40%;
}

.progress-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-1);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-time {
  font-size: 12px;
  color: var(--text-2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-bar-container {
  width: 100px;
  flex-shrink: 0;
  margin: 0 8px;
}

.progress-percentage {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  min-width: 30px;
  text-align: right;
  flex-shrink: 0;
}

.loading-skeleton {
  padding: 16px;
}

// 响应式设计
@media (max-width: 1200px) {
  .top-section {
    grid-template-columns: 1fr;
  }

  .progress-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .progress-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .dashboard-container {
    padding: 16px;
  }

  .progress-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .dashboard-container {
    padding: 12px;
  }

  .progress-grid {
    grid-template-columns: 1fr;
  }
}
</style>

