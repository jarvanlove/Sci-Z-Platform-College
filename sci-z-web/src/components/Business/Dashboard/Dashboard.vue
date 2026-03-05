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

    <!-- 顶部统计卡片区域：改用 DashboardStatCard，更靠近 dashboard 视觉 -->
    <div class="stats-cards">
      <DashboardStatCard
        v-for="stat in stats"
        :key="stat.key"
        :type="stat.type"
        :title="stat.label"
        :value="stat.value"
        :unit="stat.unit"
        :icon="stat.icon"
        :show-trend="!!stat.trend"
        :trend="stat.trend"
        :custom-class="`stat-${stat.key}`"
      />
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 第一行：趋势 + 状态分布 -->
      <div class="top-section">
        <!-- 申报 & 项目趋势 -->
        <BaseCard class="trend-card content-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">{{ $t('dashboard.trendTitle') }}</h3>
            </div>
          </template>
          <div class="trend-chart-wrap">
            <EfficiencyChart
              :data="trendChartData"
              :chart-type="'line'"
              :loading="loading"
              :empty-text="$t('dashboard.loadError')"
              :x-axis-name="$t('common.month')"
              :y-axis-name="$t('common.quantity')"
            />
          </div>
        </BaseCard>

        <!-- 状态分布（申报/项目切换） -->
        <BaseCard class="status-card content-card">
          <template #header>
            <div class="card-header status-header">
              <h3 class="card-title">
                {{
                  activeStatusTab === 'declaration'
                    ? $t('dashboard.declarationStatusDistribution')
                    : $t('dashboard.projectStatusDistribution')
                }}
              </h3>
              <div class="status-tabs">
                <button
                  class="status-tab"
                  :class="{ active: activeStatusTab === 'declaration' }"
                  @click="activeStatusTab = 'declaration'"
                >
                  {{ $t('menu.declaration') }}
                </button>
                <button
                  class="status-tab"
                  :class="{ active: activeStatusTab === 'project' }"
                  @click="activeStatusTab = 'project'"
                >
                  {{ $t('menu.project') }}
                </button>
              </div>
            </div>
          </template>
          <DashboardPieChart
            :data="activeStatusTab === 'declaration' ? declarationStatusSeries : projectStatusSeries"
            :loading="loading"
            :empty-text="$t('dashboard.loadError')"
          />
        </BaseCard>
      </div>

      <!-- 第二行：按学院 / 类型分布 -->
      <div class="middle-section">
        <BaseCard class="content-card dept-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">{{ $t('dashboard.byDepartmentTitle') }}</h3>
            </div>
          </template>
          <EfficiencyChart
            :data="departmentChartData"
            :chart-type="'bar'"
            :loading="loading"
            :empty-text="$t('dashboard.loadError')"
            :x-axis-name="$t('dashboard.topicDepartment')"
            :y-axis-name="$t('common.quantity')"
            :tooltip-quantity-label="$t('common.quantity')"
          />
        </BaseCard>

        <!-- 按项目类型分布 - 暂时隐藏
        <BaseCard class="content-card type-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">{{ $t('dashboard.byTypeTitle') }}</h3>
            </div>
          </template>
          <DashboardPieChart
            :data="projectTypeSeries"
            :loading="loading"
            :empty-text="$t('dashboard.loadError')"
          />
        </BaseCard>
        -->
      </div>

      <!-- 第三行：项目延期预警 -->
      <div class="bottom-section">
        <BaseCard class="content-card delay-warning-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">{{ $t('dashboard.delayWarningTitle') }}</h3>
            </div>
          </template>
          <DelayWarningChart
            :data="delayWarningData"
            :loading="loading"
            :empty-text="$t('dashboard.loadError')"
            :project-count-label="$t('dashboard.projectCountLabel')"
            :risk-level-labels="riskLevelLabels"
          />
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
import { Document, Folder, Check, Search, TopRight, Reading, PieChart, TrendCharts } from '@element-plus/icons-vue'
import { BaseCard, ProjectProgressBar, BaseScrollbar, BaseTooltip, BackButton } from '@/components/Common'
import { createLogger } from '@/utils/simpleLogger'
import { getProjectStatistics } from '@/api/Project/project'
import { getDashboardTrend, getDeclarationStatusStats, getProjectStatusStats, getByDepartmentStats, getByTypeStats, getDelayWarningStats } from '@/api/Dashboard/dashboard'
import EfficiencyChart from '@/components/Practice/EfficiencyChart.vue'
import DashboardPieChart from './DashboardPieChart.vue'
import DashboardStatCard from './DashboardStatCard.vue'
import DelayWarningChart from './DelayWarningChart.vue'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('Dashboard')

// 响应式数据
const loading = ref(false)

// 统计数据 - 使用 computed 使标签响应语言切换
const stats = computed(() => [
  {
    key: 'total',
    type: 'primary',
    icon: 'Folder',
    value: statsValues.value.total,
    unit: '',
    label: t('dashboard.totalProjects'),
    trend: null
  },
  {
    key: 'progress',
    type: 'warning',
    icon: 'Document',
    value: statsValues.value.progress,
    unit: '',
    label: t('dashboard.inProgress'),
    trend: null
  },
  {
    key: 'pending',
    type: 'danger',
    icon: 'PieChart',
    value: statsValues.value.pending,
    unit: '',
    label: t('dashboard.delayedProjects'),
    trend: null
  },
  {
    key: 'completed',
    type: 'success',
    icon: 'Check',
    value: statsValues.value.completed,
    unit: '',
    label: t('dashboard.completed'),
    trend: null
  }
])

// 统计数据值
const statsValues = ref({
  total: 0,
  progress: 0,
  pending: 0,
  completed: 0
})

// 新仪表板不再展示申报列表、项目进度等旧模块，相关数据与接口调用已移除

// 图表相关数据（由后端聚合接口提供）
// 趋势图数据结构：{ x: string[], series: [{ name: string, data: number[] }, ...] }
const trendChartData = ref({
  x: [],
  series: []
})

const declarationStatusSeries = ref([])

const projectStatusSeries = ref([])

const departmentChartData = ref({
  x: [],
  y: []
})

const projectTypeSeries = ref([])

// 延期预警数据
const delayWarningData = ref({
  riskLevels: [],
  upcomingDeadlines: []
})

// 风险等级 i18n 标签
const riskLevelLabels = computed(() => ({
  1: t('dashboard.riskLevel.delayed'),
  2: t('dashboard.riskLevel.sevenDays'),
  3: t('dashboard.riskLevel.thirtyDays'),
  4: t('dashboard.riskLevel.normal')
}))

const activeStatusTab = ref('declaration')

// 格式化数字
const formatNumber = (num) => {
  return num.toLocaleString()
}

// 加载数据
const loadDashboardData = async () => {
  try {
    loading.value = true
    logger.info('Starting to load dashboard data')

    // 1）获取项目统计信息
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

    // 2）加载趋势图数据
    try {
      const trendResp = await getDashboardTrend()
      logger.info('Dashboard trend API response:', trendResp)
      if (trendResp.code === 200 && trendResp.data) {
        const data = trendResp.data
        // 后端返回格式: { x: [], declarationSeries: [], projectSeries: [] }
        // 注意：后端返回的是 Long 类型，前端需要转换为 Number
        const declarationData = (data.declarationSeries || []).map(v => Number(v))
        const projectData = (data.projectSeries || []).map(v => Number(v))

        logger.info('Trend data processed:', { x: data.x, declarationData, projectData })

        trendChartData.value = {
          x: data.x || [],
          series: [
            {
              name: t('dashboard.declarationCount'),
              data: declarationData,
              color: '#1e3a8a' // 深蓝色 - 申报
            },
            {
              name: t('dashboard.projectCount'),
              data: projectData,
              color: '#f59e0b' // 琥珀色/黄色 - 项目（与饼图已延期项目颜色一致）
            }
          ]
        }
        logger.info('Dashboard trend data loaded successfully', trendChartData.value)
      }
    } catch (error) {
      logger.error('Failed to load dashboard trend data', error)
    }

    // 3）加载状态分布数据
    try {
      const declarationStatusResp = await getDeclarationStatusStats()
      if (declarationStatusResp.code === 200 && Array.isArray(declarationStatusResp.data)) {
        // 申报状态映射：按用户要求显示为 申报进行中、申报成功、申报未通过
        // 支持字符串状态和数字状态（1=进行中，2=成功，3=未通过）
        const declarationStatusMap = {
          '1': { name: t('declaration.statusInProgress'), color: '#3b82f6' },
          '2': { name: t('declaration.statusSuccess'), color: '#22c55e' },
          '3': { name: t('declaration.statusFailed'), color: '#ef4444' }
        }

        // 构建所有3个状态的完整数据（缺少数值为0）
        const declarationCounts = {}
        declarationStatusResp.data.forEach((item) => {
          const rawStatus = String(item.status || '')
          declarationCounts[rawStatus] = item.count
        })

        declarationStatusSeries.value = [
          { name: declarationStatusMap['1'].name, value: declarationCounts['1'] || 0, itemStyle: { color: declarationStatusMap['1'].color } },
          { name: declarationStatusMap['2'].name, value: declarationCounts['2'] || 0, itemStyle: { color: declarationStatusMap['2'].color } },
          { name: declarationStatusMap['3'].name, value: declarationCounts['3'] || 0, itemStyle: { color: declarationStatusMap['3'].color } }
        ]
      }

      const projectStatusResp = await getProjectStatusStats()
      if (projectStatusResp.code === 200 && Array.isArray(projectStatusResp.data)) {
        // 项目状态映射：按用户要求显示为 进行中、已完成、已延期、已取消
        // 支持字符串状态和数字状态（1=进行中，2=已完成，3=已延期，4=已取消）
        const projectStatusMap = {
          '1': { name: t('project.statusInProgress'), color: '#3b82f6' },
          '2': { name: t('project.statusCompleted'), color: '#22c55e' },
          '3': { name: t('dashboard.delayedProjects'), color: '#f59e0b' },
          '4': { name: t('project.statusCancelled'), color: '#6b7280' }
        }

        // 构建所有4个状态的完整数据（缺少数值为0）
        const projectCounts = {}
        projectStatusResp.data.forEach((item) => {
          const rawStatus = String(item.status || '')
          projectCounts[rawStatus] = item.count
        })

        projectStatusSeries.value = [
          { name: projectStatusMap['1'].name, value: projectCounts['1'] || 0, itemStyle: { color: projectStatusMap['1'].color } },
          { name: projectStatusMap['2'].name, value: projectCounts['2'] || 0, itemStyle: { color: projectStatusMap['2'].color } },
          { name: projectStatusMap['3'].name, value: projectCounts['3'] || 0, itemStyle: { color: projectStatusMap['3'].color } },
          { name: projectStatusMap['4'].name, value: projectCounts['4'] || 0, itemStyle: { color: projectStatusMap['4'].color } }
        ]
      }
    } catch (error) {
      logger.error('Failed to load status distribution data', error)
    }

    // 4）加载按课题发布部门统计
    try {
      const deptResp = await getByDepartmentStats()
      logger.info('Department API raw response:', deptResp)
      if (deptResp.code === 200 && Array.isArray(deptResp.data)) {
        // 定义部门映射（后端返回的中文 -> i18n键）
        const departmentMap = {
          '国自然-青年基金': t('dashboard.departments.youthFund'),
          '国自然-面上基金': t('dashboard.departments.generalFund'),
          '国自然-地区项目': t('dashboard.departments.regionalProject'),
          '省市级项目': t('dashboard.departments.provincialProject'),
          '其他': t('dashboard.departments.other')
        }

        // 定义部门排序顺序
        const departmentOrder = [
          '国自然-青年基金',
          '国自然-面上基金',
          '国自然-地区项目',
          '省市级项目'
        ]

        // 处理返回数据：映射i18n、排序、其他项放最后
        const processedData = []
        let otherCount = 0

        deptResp.data.forEach((item) => {
          const rawName = item.departmentName
          const count = item.count

          if (departmentOrder.includes(rawName)) {
            // 已知部门，按顺序放入
            processedData.push({
              name: departmentMap[rawName] || rawName,
              count: count,
              order: departmentOrder.indexOf(rawName)
            })
          } else {
            // 未知部门，归入"其他"
            otherCount += count
          }
        })

        // 按固定顺序排序
        processedData.sort((a, b) => a.order - b.order)

        // 如果有"其他"数据或未分类的数据，添加到最后
        const existingOther = deptResp.data.find(item => item.departmentName === '其他')
        if (existingOther) {
          otherCount += existingOther.count
        }

        if (otherCount > 0) {
          processedData.push({
            name: t('dashboard.departments.other'),
            count: otherCount,
            order: 999
          })
        }

        // 提取x轴和y轴数据
        const x = processedData.map(item => item.name)
        const y = processedData.map(item => item.count)

        departmentChartData.value = { x, y }
        logger.info('Dashboard department stats loaded:', { rawData: deptResp.data, processed: departmentChartData.value })
      }
    } catch (error) {
      logger.error('Failed to load department stats', error)
    }

    // 5）加载按项目类型分布 - 暂时注释，后续按需启用
    // try {
    //   const typeResp = await getByTypeStats()
    //   logger.info('Type API raw response:', typeResp)
    //   if (typeResp.code === 200 && Array.isArray(typeResp.data)) {
    //     projectTypeSeries.value = typeResp.data.map((item) => ({
    //       name: item.type,
    //       value: item.count
    //     }))
    //     logger.info('Dashboard type stats loaded:', { rawData: typeResp.data, processed: projectTypeSeries.value })
    //   }
    // } catch (error) {
    //   logger.error('Failed to load project type stats', error)
    // }

    // 6）加载项目延期预警数据
    try {
      const delayWarningResp = await getDelayWarningStats()
      logger.info('Delay warning API response:', delayWarningResp)
      if (delayWarningResp.code === 200 && delayWarningResp.data) {
        delayWarningData.value = {
          riskLevels: delayWarningResp.data.riskLevels || [],
          upcomingDeadlines: delayWarningResp.data.upcomingDeadlines || []
        }
        logger.info('Dashboard delay warning loaded:', delayWarningData.value)
      }
    } catch (error) {
      logger.error('Failed to load delay warning stats', error)
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

.status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.status-tabs {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.04);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.25);
}

.status-tab {
  min-width: 90px;
  padding: 4px 12px;
  border-radius: 999px;
  border: none;
  background: transparent;
  color: var(--text-2);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.status-tab:hover {
  color: var(--color-primary);
}

.status-tab.active {
  background: var(--color-primary);
  color: #ffffff;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.35);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  // 统计卡片左侧彩色条纹 - 使用 :deep() 穿透到子组件
  :deep(.dashboard-stat-card) {
    border-left: 4px solid transparent;
  }

  :deep(.stat-total),
  :deep(.stat--primary) {
    border-left-color: var(--color-primary) !important;
  }

  :deep(.stat-progress),
  :deep(.stat--warning) {
    border-left-color: var(--color-warning) !important;
  }

  :deep(.stat-pending),
  :deep(.stat--danger) {
    border-left-color: var(--color-danger) !important;
  }

  :deep(.stat-completed),
  :deep(.stat--success) {
    border-left-color: var(--color-success) !important;
  }
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
.stat-pending { color: #dc2626; }
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

// 延期预警区域
.bottom-section {
  width: 100%;
}

.delay-warning-card {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;

  :deep(.base-card__content) {
    padding: var(--gap-lg);
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
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

