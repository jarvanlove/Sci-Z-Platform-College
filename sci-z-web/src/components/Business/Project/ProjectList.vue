<!--
/**
 * @description 项目列表业务组件
 * 展示所有科研项目，支持搜索、筛选、分页等功能
 */
-->
<template>
  <div class="project-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <BackButton :tooltip="$t('practice.backToPractice')" @click="handleBack" />
      <h1 class="page-title">{{ $t('project.list.title') }}</h1>
      <!-- 注意：原型图中新建项目按钮被注释了，因为项目由申报成功后后台直接生成 -->
    </div>

    <!-- 搜索筛选和表格区域 -->
    <BaseCard class="content-card">
      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="$t('project.list.keywordPlaceholder')"
          clearable
          style="width: 250px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="searchForm.status"
          :placeholder="$t('project.list.statusPlaceholder')"
          clearable
          style="width: 150px"
        >
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        
        <BaseDatePicker
          v-model="searchForm.dateRange"
          type="daterange"
          :unlink-panels="true"
          :start-placeholder="$t('project.list.startDate')"
          :end-placeholder="$t('project.list.endDate')"
          style="width: 200px"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
        
        <el-button type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ $t('common.search') }}
        </el-button>
        <el-button type="primary" @click="handleReset">
          <el-icon><Refresh /></el-icon>
          {{ $t('common.reset') }}
        </el-button>
      </div>

      <!-- 项目列表表格 -->
      <BaseTable
        :data="projects"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        :action-width="280"
        action-fixed="right"
        :empty-text="$t('project.list.noData')"
        stripe
        class="project-table"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      >
        <!-- 项目名称列自定义 - 支持换行 -->
        <template #name="{ row }">
          <div class="name-cell base-table__cell-wrap">{{ row.name || '-' }}</div>
        </template>

        <!-- 状态列自定义 -->
        <template #status="{ row }">
          <el-tag
            :type="getStatusTagType(row.status)"
            size="small"
            round
          >
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>

        <!-- 进度列自定义 -->
        <template #progress="{ row }">
          <div class="progress-cell-wrapper">
            <ProjectProgressBar
              :progress="row.progress || 0"
              :height="6"
              :show-text="true"
            />
          </div>
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <div class="action-buttons">
            <!-- 进度按钮 -->
            <BaseTooltip :content="$t('project.list.progress')" placement="top">
              <button
                class="action-btn btn-success"
                @click.stop="handleProgress(row)"
              >
                <el-icon><DataLine /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 查看按钮 -->
            <BaseTooltip :content="$t('common.view')" placement="top">
              <button
                class="action-btn btn-primary"
                @click.stop="handleView(row)"
              >
                <el-icon><TopRight /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 编辑按钮 -->
            <BaseTooltip
              v-if="row.status !== PROJECT_STATUS.CANCELLED"
              :content="$t('common.edit')"
              placement="top"
            >
              <button
                class="action-btn btn-primary"
                @click.stop="handleEdit(row)"
              >
                <el-icon><Edit /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 取消按钮 -->
            <BaseTooltip
              v-if="row.canDelete && row.status !== PROJECT_STATUS.CANCELLED"
              :content="$t('project.list.cancel')"
              placement="top"
            >
              <button
                class="action-btn btn-danger"
                @click.stop="handleCancel(row)"
              >
                <el-icon><Close /></el-icon>
              </button>
            </BaseTooltip>
          </div>
        </template>
      </BaseTable>
    </BaseCard>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DataLine, TopRight, Edit, Close } from '@element-plus/icons-vue'
import { BaseCard, BaseTable, BaseDatePicker, ProjectProgressBar, BaseTooltip, BackButton } from '@/components/Common'
import { PROJECT_STATUS, PROJECT_STATUS_CONFIG } from '@/utils/constants'
import { getProjectList, cancelProject } from '@/api/Project'
import { createLogger } from '@/utils/simpleLogger'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('ProjectList')

// 响应式数据
const loading = ref(false)
const projects = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 状态选项（从constants.js导入的项目状态常量生成）
const statusOptions = computed(() => [
  { label: t('common.all'), value: '' },
  { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.PROGRESS].text, value: PROJECT_STATUS.PROGRESS },
  { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.COMPLETED].text, value: PROJECT_STATUS.COMPLETED },
  { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.DELAYED].text, value: PROJECT_STATUS.DELAYED },
  { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.CANCELLED].text, value: PROJECT_STATUS.CANCELLED }
])

// 表格列配置 - 使用自适应策略
const tableColumns = computed(() => [
  {
    prop: 'number',
    label: t('project.list.columns.number'),
    minWidth: 160,
    align: 'center'
  },
  {
    prop: 'name',
    label: t('project.list.columns.name'),
    minWidth: 150,
    showOverflowTooltip: false, // 允许换行，不需要 tooltip
    wrap: true // 明确指定允许换行
  },
  {
    prop: 'manager',
    label: t('project.list.columns.manager'),
    minWidth: 120,
    align: 'center' 
  },
  {
    prop: 'status',
    label: t('project.list.columns.status'),
    minWidth: 90,
    align: 'left' // 🔥 左对齐，值向左移动
  },
  {
    prop: 'progress',
    label: t('project.list.columns.progress'),
    minWidth: 120,
    align: 'center'
  },
  {
    prop: 'startTime',
    label: t('project.list.columns.startTime'),
    minWidth: 130,
    align: 'center'
  },
  {
    prop: 'endTime',
    label: t('project.list.columns.endTime'),
    minWidth: 130,
    align: 'center'
  }
])

// 获取状态文本（使用constants.js的配置）
const getStatusText = (status) => {
  return PROJECT_STATUS_CONFIG[status]?.text || status
}

// 获取状态标签类型（使用constants.js的配置）
const getStatusTagType = (status) => {
  return PROJECT_STATUS_CONFIG[status]?.type || 'info'
}

// 注意：进度条颜色逻辑已移至 ProjectProgressBar 组件中

// 状态映射：将后端状态值映射到前端状态值（与后端枚举保持一致）
// 后端枚举：1-进行中, 2-已完成, 3-已延期, 4-已取消
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

// 状态反向映射：将前端状态值转换为后端 API 期望的值
// 后端枚举：1-进行中, 2-已完成, 3-已延期, 4-已取消
const mapStatusToBackend = (status) => {
  const frontendToBackendMap = {
    [PROJECT_STATUS.PROGRESS]: '1',  // 进行中
    [PROJECT_STATUS.COMPLETED]: '2', // 已完成
    [PROJECT_STATUS.DELAYED]: '3',   // 已延期
    [PROJECT_STATUS.CANCELLED]: '4'  // 已取消
  }
  return frontendToBackendMap[status] || status
}

// 加载项目列表（通过接口获取）
const loadProjects = async () => {
  try {
    loading.value = true
    logger.info('Loading project list', {
      page: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status,
      dateRange: searchForm.dateRange
    })

    // 构建请求参数（根据 API 文档）
    const params = {
      pageNo: pagination.current,
      pageSize: pagination.size
    }
    
    // 可选参数
    if (searchForm.keyword && searchForm.keyword.trim()) {
      params.keyword = searchForm.keyword.trim()
    }
    
    // 状态筛选：将前端状态值转换为后端期望的值
    if (searchForm.status) {
      params.status = mapStatusToBackend(searchForm.status)
    }
    
    // 🔥 时间范围筛选：将 dateRange 转换为 startTime 和 endTime（LocalDate 格式：YYYY-MM-DD）
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0] // 开始时间：YYYY-MM-DD
      params.endTime = searchForm.dateRange[1]   // 结束时间：YYYY-MM-DD
    }
    
    // 排序参数：默认按更新时间倒序，便于更新后列表顺序变化
    params.sortBy = 'updatedTime'
    params.sortOrder = 'DESC'

    const response = await getProjectList(params)
    
    if (response.code === 200 && response.data) {
      // 数据映射：将后端字段映射到前端字段
      // API 返回的列表字段是 records，不是 list
      projects.value = (response.data.records || []).map(item => ({
        ...item,
        // 映射字段名
        manager: item.projectLeader || '-', // 项目负责人（优先使用 projectLeader）
        endTime: item.estimatedCompletionTime || item.endTime || '-', // 预计完成时间
        startTime: item.startTime || '-', // 开始时间
        // 映射状态值（后端返回的 status 可能是字符串数字如 "1"）
        status: mapStatus(item.status),
        // 其他字段保持原样
        canDelete: item.canDelete !== false // 默认可以删除，除非明确设置为 false
      }))
      pagination.total = response.data.total || 0
      
      logger.info('Project list loaded', { count: projects.value.length })
    } else {
      throw new Error(response.message || '获取项目列表失败')
    }
  } catch (error) {
    logger.error('Failed to load project list', error)
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(error.message || t('project.list.loadError'))
    }
    projects.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  loadProjects()
}

// 🔥 使用 watch 监听筛选条件变化，自动触发查询（带防抖）
let searchTimer = null
watch(
  () => [searchForm.keyword, searchForm.status, searchForm.dateRange],
  () => {
    // 清除之前的定时器
    if (searchTimer) {
      clearTimeout(searchTimer)
    }
    // 设置新的定时器，300ms 后执行查询（输入框防抖）
    searchTimer = setTimeout(() => {
      logger.info('Filter conditions changed, auto search', { 
        keyword: searchForm.keyword, 
        status: searchForm.status,
        dateRange: searchForm.dateRange
      })
      pagination.current = 1
      loadProjects()
    }, 300)
  },
  { deep: true }
)

// 重置搜索
const handleReset = () => {
  // 🔥 清除防抖定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    dateRange: []
  })
  pagination.current = 1
  loadProjects()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadProjects()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  loadProjects()
}

// 操作处理
const handleBack = () => {
  router.push('/practice')
}

const handleView = (project) => {
  router.push(`/project/detail/${project.id}`)
}

const handleEdit = (project) => {
  router.push(`/project/detail/${project.id}?mode=edit`)
}

const handleProgress = (project) => {
  router.push(`/project/progress/${project.id}`)
}

const handleCancel = async (project) => {
  try {
    await ElMessageBox.confirm(
      t('project.list.cancelConfirm', { name: project.name }),
      t('project.list.cancelTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    logger.info('Canceling project', { projectId: project.id, projectName: project.name })
    const response = await cancelProject(project.id)
    
    if (response.code === 200) {
      ElMessage.success(t('project.list.cancelSuccess', { name: project.name }))
      logger.info('Project canceled successfully', { projectId: project.id })
      loadProjects()
    } else {
      throw new Error(response.message || '取消项目失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Failed to cancel project', error)
      // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
      if (!error._messageShown) {
        ElMessage.error(error.message || t('project.list.cancelError'))
      }
    }
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style lang="scss" scoped>
.project-list-container {
  padding: var(--gap-lg);
  background: var(--bg-secondary);
  min-height: calc(100vh - 56px);
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden; // 🔥 不允许表格内容超出容器
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

.content-card {
  background: var(--surface);
  border-radius: 12px;
  padding: 24px;
  width: 100%;
  box-sizing: border-box;
  margin-bottom: var(--gap-lg);
  
  :deep(.base-card__content) {
    padding: 0;
    width: 100%;
  }
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
  
  // 暗色主题下重置按钮样式优化
  :deep(.el-button:not(.el-button--primary)) {
    background-color: var(--surface) !important;
    border-color: var(--border) !important;
    color: var(--text-2) !important;
    
    &:hover {
      background-color: var(--hover) !important;
      border-color: var(--border-hover) !important;
      color: var(--text-1) !important;
    }
    
    &:active {
      background-color: var(--hover-light) !important;
      border-color: var(--border) !important;
    }
  }
}

// 进度列样式 - 限制进度条宽度，避免百分比文本被遮挡
.progress-cell-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 100%;
  
  :deep(.project-progress-bar) {
    width: 100%;
    max-width: 100%;
    
    .progress-bar-wrapper {
      flex: 1;
      min-width: 0;
      max-width: calc(100% - 50px); // 为百分比文本预留空间（32px文本 + 10px间距）
    }
    
    .progress-text {
      flex-shrink: 0;
      min-width: 32px;
    }
  }
}


.project-table {
  width: 100%;
  border-radius: 8px;
  overflow: visible; // 🔥 允许表格内容超出容器，触发横向滚动
  border: 1px solid var(--border);
  
  :deep(.base-table) {
    width: 100%;
    display: flex;
    flex-direction: column;
    // 🔥 确保表格容器可以横向滚动
    overflow: visible;
  }
  
  :deep(.base-table__table) {
    width: 100% !important;
    // 🔥 允许表格内容超出容器宽度，触发横向滚动
    min-width: fit-content;
    flex: 1;
    overflow: visible;
  }
  
  // 🔥 确保表格内部可以正确显示滚动条
  :deep(.el-table) {
    width: 100%;
    // 🔥 表格内容可以超出，但滚动条在表体底部显示
    min-width: fit-content;
  }
  
  :deep(.el-table) {
    
    // 表头样式
    .el-table__header {
      th {
        padding: 14px 16px !important;
        font-size: 14px;
        font-weight: 600 !important;
        color: var(--text-2);
        background-color: var(--surface) !important;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        // 🔥 确保表头列宽固定，作为所有行的基准
        box-sizing: border-box;
      }
      
      // 🔥 项目名称表头左对齐
      th[data-column-key="name"] {
        text-align: left !important;
      }
      
      // 🔥 项目负责人和项目状态表头左对齐
      th[data-column-key="manager"],
      th[data-column-key="status"] {
        text-align: left !important;
      }
    }
    
    // 表体样式
    .el-table__body {
      // 🔥 Element Plus 标准做法：表体的列宽由表头决定，所有行必须使用相同的列宽
      // 不要设置 width、min-width、max-width，让 Element Plus 根据表头自动同步
      td {
        padding: 12px 16px !important;
        font-size: 14px;
        color: var(--text);
        // 🔥 确保列宽与表头同步，防止不同行的列宽不一致
        box-sizing: border-box;
        // 🔥 移除所有可能干扰列宽同步的样式，让 Element Plus 自动处理
      }
      
      // 🔥 项目名称列支持换行
      td[data-column-key="name"] {
        text-align: left !important;
        
        .cell {
          white-space: normal !important;
          word-break: break-word !important;
          line-height: 1.6 !important;
        }
        
        .name-cell {
          white-space: normal !important;
          word-break: break-word !important;
          line-height: 1.6 !important;
          max-width: 100%;
          text-align: left;
        }
      }
      
      // 🔥 项目负责人和项目状态列左对齐，值向左移动
      td[data-column-key="manager"],
      td[data-column-key="status"] {
        text-align: left !important;
        padding-left: 16px !important; // 保持左边距
        
        .cell {
          text-align: left !important;
          display: flex;
          justify-content: flex-start;
          align-items: center;
        }
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
}

.action-buttons {
  display: flex;
  flex-direction: row;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap;
}

.action-btn {
  padding: 4px;
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
  gap: 0;
  user-select: none;
  
  .el-icon {
    font-size: 16px;
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
    
    &:hover:not(:disabled) {
      background: #16a34a;
      color: var(--surface);
    }
  }
  
  &.btn-danger {
    color: #dc2626;
    border-color: #dc2626;
    
    &:hover:not(:disabled) {
      background: #dc2626;
      color: var(--surface);
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .project-list-container {
    padding: 24px 32px;
  }
}

@media (max-width: 768px) {
  .project-list-container {
    padding: 16px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: var(--gap-sm);
  }
  
  .content-card {
    padding: 16px;
  }
  
  .action-buttons {
    flex-direction: row;
    gap: 4px;
    flex-wrap: nowrap;
  }
}
</style>

