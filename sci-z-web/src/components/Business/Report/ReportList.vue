<!--
/**
 * @description 报告列表业务组件
 * 展示所有报告，支持搜索、筛选、分页等功能
 * 使用卡片视图展示报告列表
 */
-->
<template>
  <div class="report-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">{{ $t('report.listPage.title') }}</h1>
      <BaseButton type="primary" @click="handleGenerateReport">
        <el-icon><Plus /></el-icon>
        {{ $t('report.listPage.generateNewReport') }}
      </BaseButton>
    </div>

    <!-- 搜索筛选区域 -->
    <BaseCard class="search-card">
      <el-form :model="searchForm" class="search-form" label-width="100px">
        <el-form-item :label="$t('report.listPage.keywordSearch')">
          <el-input
            v-model="searchForm.keyword"
            :placeholder="$t('report.listPage.keywordPlaceholder')"
            clearable
            style="width: 300px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item :label="$t('report.type')">
          <el-select
            v-model="searchForm.reportType"
            :placeholder="$t('common.all')"
            clearable
            style="width: 150px"
            @change="handleFilterChange"
          >
            <el-option
              v-for="option in reportTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="$t('report.listPage.status')">
          <el-select
            v-model="searchForm.status"
            :placeholder="$t('common.all')"
            clearable
            style="width: 150px"
            @change="handleFilterChange"
          >
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="$t('report.listPage.dateRange')">
          <BaseDatePicker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            :start-placeholder="$t('common.startDate')"
            :end-placeholder="$t('common.endDate')"
            style="width: 240px"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
      </el-form>

      <!-- 按钮行 -->
      <div class="search-actions">
        <BaseButton type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ $t('common.search') }}
        </BaseButton>
        <BaseButton @click="handleReset">
          <el-icon><Refresh /></el-icon>
          {{ $t('common.reset') }}
        </BaseButton>
      </div>
    </BaseCard>

    <!-- 报告列表卡片 -->
    <BaseCard class="content-card">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ $t('common.loading') }}</span>
      </div>

      <!-- 报告统计栏 -->
      <div v-if="!loading && reportList.length > 0" class="result-count-bar">
        {{ $t('report.listPage.totalReports', { total: pagination.total }) }}
      </div>

      <!-- 卡片视图 -->
      <div v-if="!loading" class="card-grid">
        <div
          v-for="report in reportList"
          :key="report.id"
          class="report-card"
        >
          <div class="card-header">
            <div class="report-type-tag" :class="report.reportType">
              <span>{{ report.reportType === 'tech' ? '📄' : '📊' }}</span>
              {{ getReportTypeText(report.reportType) }}
            </div>
          </div>

          <div class="card-content">
            <h3 class="project-name">{{ report.projectName }}</h3>
            <p class="report-summary">{{ report.summary || $t('report.listPage.noSummary') }}</p>
          </div>

          <div class="card-meta">
            <div class="meta-item">
              <p class="meta-value">{{ formatDate(report.generateTime || report.createdTime) }}</p>
              <p class="meta-label">{{ $t('report.listPage.generateTime') }}</p>
            </div>
            <div class="meta-item">
              <p class="meta-value">{{ report.creatorName || '-' }}</p>
              <p class="meta-label">{{ $t('report.listPage.creator') }}</p>
            </div>
            <div class="meta-item">
              <p class="meta-value">
                <el-tag :type="getStatusTagType(report.status)" size="small">
                  {{ getStatusText(report.status) }}
                </el-tag>
              </p>
              <p class="meta-label">{{ $t('report.status') }}</p>
            </div>
          </div>

          <div class="card-actions" @click.stop>
            <div class="action-group-primary">
              <BaseButton class="action-btn primary" @click="handlePreview(report)">
                {{ $t('report.listPage.preview') }}
              </BaseButton>
              <el-dropdown
                @command="(format) => handleDownload(report, format)"
                trigger="click"
              >
                <BaseButton class="action-btn primary">
                  {{ $t('report.listPage.download') }}
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </BaseButton>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="pdf">PDF</el-dropdown-item>
                    <el-dropdown-item command="word">Word</el-dropdown-item>
                    <el-dropdown-item command="markdown">Markdown</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <BaseButton class="action-btn primary" @click="handleRegenerate(report)">
                {{ $t('report.listPage.regenerate') }}
              </BaseButton>
            </div>
            <div class="action-group-danger">
              <BaseButton class="action-btn danger" @click="handleDelete(report)">
                {{ $t('common.delete') }}
              </BaseButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && reportList.length === 0" class="empty-state">
        <div class="empty-icon">📄</div>
        <div class="empty-title">{{ $t('report.listPage.noReports') }}</div>
        <div class="empty-description">{{ $t('report.listPage.noReportsHint') }}</div>
        <BaseButton type="primary" @click="handleGenerateReport">
          {{ $t('report.listPage.generateNewReport') }}
        </BaseButton>
      </div>

      <!-- 分页 -->
      <div v-if="!loading && reportList.length > 0" class="pagination-section">
        <BasePagination
          :current="pagination.current"
          :size="pagination.size"
          :total="pagination.total"
          @change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </BaseCard>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, ArrowDown, Loading } from '@element-plus/icons-vue'
import { BaseCard, BaseButton, BaseDatePicker, BasePagination } from '@/components/Common'
import { getReportManagementList, deleteReportManagement } from '@/api/Report'
import { previewFile, downloadFile } from '@/api/File/file'
import { createLogger } from '@/utils/simpleLogger'
import { formatDate } from '@/utils/date'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('ReportList')

// 响应式数据
const loading = ref(false)
const reportList = ref([])

// 定时刷新相关
const refreshTimer = ref(null)
const REFRESH_INTERVAL = 10000 // 10秒刷新一次（优化：减少刷新频率）
const userInteracting = ref(false) // 用户是否正在交互
const userInteractionTimer = ref(null) // 用户交互计时器
const USER_INTERACTION_DELAY = 3000 // 用户停止操作后3秒再恢复刷新
const isPageVisible = ref(true) // 页面是否可见

// 搜索表单
const searchForm = reactive({
  keyword: '',
  reportType: '',
  status: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 报告类型选项
const reportTypeOptions = computed(() => [
  { label: t('common.all'), value: '' },
  { label: t('report.typeTech'), value: 'tech' },
  { label: t('report.typeSelf'), value: 'self' }
])

// 状态选项
const statusOptions = computed(() => [
  { label: t('common.all'), value: '' },
  { label: t('report.listPage.statusPending'), value: 'pending' },
  { label: t('report.listPage.statusGenerating'), value: 'generating' },
  { label: t('report.listPage.statusGenerated'), value: 'generated' },
  { label: t('report.listPage.statusFailed'), value: 'failed' }
])

// 获取报告类型文本
const getReportTypeText = (type) => {
  return type === 'tech' ? t('report.typeTech') : t('report.typeSelf')
}

// 获取状态文本（确保所有状态都显示为中文）
const getStatusText = (status) => {
  if (!status) {
    return '-'
  }
  // 统一转换为小写，确保匹配
  const statusLower = String(status).toLowerCase()
  const statusMap = {
    pending: t('report.listPage.statusPending'),
    generating: t('report.listPage.statusGenerating'),
    generated: t('report.listPage.statusGenerated'),
    completed: t('report.listPage.statusCompleted'), // 兼容旧数据
    failed: t('report.listPage.statusFailed')
  }
  // 如果状态在映射中，返回中文；否则返回默认值
  return statusMap[statusLower] || t('report.listPage.statusUnknown') || '未知状态'
}

// 获取状态标签类型（确保所有状态都有对应的标签类型）
const getStatusTagType = (status) => {
  if (!status) {
    return 'info'
  }
  // 统一转换为小写，确保匹配
  const statusLower = String(status).toLowerCase()
  const typeMap = {
    pending: 'info',
    generating: 'warning',
    generated: 'success',
    completed: 'success', // 兼容旧数据
    failed: 'danger'
  }
  return typeMap[statusLower] || 'info'
}

// 加载报告列表
const loadReportList = async () => {
  try {
    loading.value = true
    logger.info('Starting to load report list', {
      pageNo: pagination.current,
      pageSize: pagination.size,
      keyword: searchForm.keyword,
      reportType: searchForm.reportType,
      status: searchForm.status
    })

    const params = {
      pageNo: pagination.current,
      pageSize: pagination.size,
      sortBy: 'generateTime',
      sortOrder: 'DESC'
    }

    // 添加筛选条件
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.reportType) {
      params.reportType = searchForm.reportType
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }

    // 日期范围筛选（如果有）
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      // 注意：后端接口可能不支持日期范围筛选，这里先不传
      // 如果后端支持，可以添加 startDate 和 endDate 参数
    }

    const response = await getReportManagementList(params)
    const result = response || {}

    if (result.code !== 200) {
      throw new Error(result.message || t('report.listPage.loadError'))
    }

    const data = result.data || {}
    reportList.value = data.records || data.list || []
    pagination.total = data.total || 0
    pagination.current = data.current || data.pageNo || pagination.current
    pagination.size = data.size || data.pageSize || pagination.size

    logger.info('Report list loaded successfully', {
      count: reportList.value.length,
      total: pagination.total
    })

    // 检查是否需要启动定时刷新
    checkAndStartAutoRefresh()
  } catch (error) {
    logger.error('Failed to load report list', error)
    const errorMessage = error.response?.data?.message || error.message || t('report.listPage.loadError')
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  markUserInteracting() // 标记用户正在交互
  logger.info('User performed search', {
    keyword: searchForm.keyword,
    reportType: searchForm.reportType,
    status: searchForm.status
  })
  pagination.current = 1
  loadReportList()
}

// 筛选变化处理
const handleFilterChange = () => {
  markUserInteracting() // 标记用户正在交互
  logger.info('User changed filter')
  pagination.current = 1
  loadReportList()
}

// 日期变化处理
const handleDateChange = () => {
  markUserInteracting() // 标记用户正在交互
  logger.info('User changed date range')
  pagination.current = 1
  loadReportList()
}

// 重置搜索
const handleReset = () => {
  logger.info('User reset search conditions')
  searchForm.keyword = ''
  searchForm.reportType = ''
  searchForm.status = ''
  searchForm.dateRange = []
  pagination.current = 1
  loadReportList()
  ElMessage.info(t('common.resetSuccess'))
}

// 生成新报告（跳转到生成页面）
const handleGenerateReport = () => {
  logger.info('User clicked generate new report')
  router.push('/report/generate')
}

// 预览报告
const handlePreview = async (report) => {
  try {
    logger.info('User previewed report', { id: report.id, attachmentId: report.attachmentId })
    
    // 检查是否有附件 ID
    if (!report.attachmentId) {
      ElMessage.warning(t('report.listPage.noAttachment') || '报告文件尚未生成，无法预览')
      return
    }
    
    // 调用预览接口获取预览 URL
    const response = await previewFile(report.attachmentId)
    const responseData = response?.data || response
    const previewUrl = responseData?.data || responseData
    
    if (previewUrl) {
      // 在新窗口打开预览
      window.open(previewUrl, '_blank')
      ElMessage.success(t('report.listPage.previewSuccess') || '预览已打开')
      logger.info('Preview opened successfully', { id: report.id, previewUrl })
    } else {
      throw new Error('预览 URL 为空')
    }
  } catch (error) {
    logger.error('Failed to preview report', error)
    const errorMessage = error.response?.data?.message || error.message || t('report.listPage.previewError') || '预览失败'
    ElMessage.error(errorMessage)
  }
}

// 下载报告
const handleDownload = async (report, format) => {
  try {
    logger.info('User downloaded report', { id: report.id, attachmentId: report.attachmentId, format })
    
    // 检查是否有附件 ID
    if (!report.attachmentId) {
      ElMessage.warning(t('report.listPage.noAttachment') || '报告文件尚未生成，无法下载')
      return
    }
    
    // 根据格式参数决定下载格式
    let downloadFormat = null
    if (format === 'pdf') {
      downloadFormat = 'pdf'
    } else if (format === 'word') {
      downloadFormat = 'docx'
    }
    // markdown 格式不转换，直接下载原文件
    
    // 调用下载接口
    const response = await downloadFile(report.attachmentId, downloadFormat)
    
    // 创建 Blob 对象
    const blob = new Blob([response.data], { 
      type: response.headers['content-type'] || 'application/octet-stream' 
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 从响应头获取文件名，或使用默认文件名
    const contentDisposition = response.headers['content-disposition']
    let fileName = `报告_${report.number || report.id}`
    
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1].replace(/['"]/g, ''))
      }
    } else {
      // 根据格式添加扩展名
      const ext = format === 'pdf' ? '.pdf' : format === 'word' ? '.docx' : format === 'markdown' ? '.md' : ''
      fileName += ext
    }
    
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(t('report.listPage.downloadSuccess') || '下载成功')
    logger.info('Download completed successfully', { id: report.id, fileName, format })
  } catch (error) {
    logger.error('Failed to download report', error)
    const errorMessage = error.response?.data?.message || error.message || t('report.listPage.downloadError') || '下载失败'
    ElMessage.error(errorMessage)
  }
}

// 重新生成报告
const handleRegenerate = async (report) => {
  try {
    await ElMessageBox.confirm(
      t('report.listPage.regenerateConfirm', { name: report.projectName }),
      t('report.listPage.regenerate'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    logger.info('User regenerated report', { id: report.id })
    // TODO: 实现重新生成功能
    ElMessage.success(t('report.listPage.regenerateSuccess'))
  } catch (error) {
    // 用户取消
  }
}

// 删除报告
const handleDelete = async (report) => {
  try {
    await ElMessageBox.confirm(
      t('report.listPage.deleteConfirm', { name: report.projectName }),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    logger.info('User deleted report', { id: report.id })
    
    await deleteReportManagement(report.id)
    ElMessage.success(t('report.listPage.deleteSuccess'))
    loadReportList()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Failed to delete report', error)
      const errorMessage = error.response?.data?.message || error.message || t('report.listPage.deleteError')
      ElMessage.error(errorMessage)
    }
  }
}

// 分页变化处理
const handlePageChange = (page) => {
  markUserInteracting() // 标记用户正在交互
  logger.info('User changed page', { page })
  pagination.current = page
  loadReportList()
}

// 每页数量变化处理
const handlePageSizeChange = (size) => {
  markUserInteracting() // 标记用户正在交互
  logger.info('User changed page size', { size })
  pagination.size = size
  pagination.current = 1
  loadReportList()
}

// 检查是否有待刷新状态的报告（pending 或 generating）
const hasPendingOrGeneratingReports = () => {
  if (!reportList.value || reportList.value.length === 0) {
    return false
  }
  return reportList.value.some(report => {
    const status = String(report.status || '').toLowerCase()
    return status === 'pending' || status === 'generating'
  })
}

// 启动定时刷新
const startAutoRefresh = () => {
  // 如果已经有定时器在运行，先清除
  stopAutoRefresh()

  logger.info('Starting auto refresh timer', { interval: REFRESH_INTERVAL })
  refreshTimer.value = setInterval(() => {
    // 优化：只在以下条件都满足时才刷新
    // 1. 没有正在加载
    // 2. 用户没有正在交互
    // 3. 页面可见
    if (!loading.value && !userInteracting.value && isPageVisible.value) {
      logger.debug('Auto refreshing report list (silent mode)')
      // 静默刷新：不显示loading状态，避免打断用户
      loadReportListSilently()
    } else {
      logger.debug('Skipping auto refresh', {
        loading: loading.value,
        userInteracting: userInteracting.value,
        isPageVisible: isPageVisible.value
      })
    }
  }, REFRESH_INTERVAL)
}

// 停止定时刷新
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    logger.info('Stopping auto refresh timer')
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

// 静默刷新报告列表（不显示loading状态）
const loadReportListSilently = async () => {
  try {
    // 不设置 loading.value = true，避免显示加载状态
    logger.debug('Silent loading report list')

    const params = {
      pageNo: pagination.current,
      pageSize: pagination.size,
      sortBy: 'generateTime',
      sortOrder: 'DESC'
    }

    // 添加筛选条件
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.reportType) {
      params.reportType = searchForm.reportType
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }

    const response = await getReportManagementList(params)
    const result = response || {}

    if (result.code !== 200) {
      logger.warn('Silent refresh failed', { message: result.message })
      return
    }

    const data = result.data || {}
    reportList.value = data.records || data.list || []
    pagination.total = data.total || 0
    pagination.current = data.current || data.pageNo || pagination.current
    pagination.size = data.size || data.pageSize || pagination.size

    logger.debug('Silent refresh completed', {
      count: reportList.value.length,
      total: pagination.total
    })

    // 检查是否需要继续定时刷新
    checkAndStartAutoRefresh()
  } catch (error) {
    logger.warn('Silent refresh error', error)
    // 静默刷新失败时不显示错误提示，避免打扰用户
  }
}

// 标记用户正在交互
const markUserInteracting = () => {
  userInteracting.value = true
  
  // 清除之前的计时器
  if (userInteractionTimer.value) {
    clearTimeout(userInteractionTimer.value)
  }
  
  // 用户停止操作后，延迟恢复刷新
  userInteractionTimer.value = setTimeout(() => {
    userInteracting.value = false
    logger.debug('User interaction ended, resuming auto refresh')
    // 如果还有待刷新的报告，确保定时器在运行
    if (hasPendingOrGeneratingReports()) {
      checkAndStartAutoRefresh()
    }
  }, USER_INTERACTION_DELAY)
}

// 检查并启动/停止定时刷新
const checkAndStartAutoRefresh = () => {
  if (hasPendingOrGeneratingReports()) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

// 页面可见性变化处理
const handleVisibilityChange = () => {
  isPageVisible.value = !document.hidden
  logger.debug('Page visibility changed', { isVisible: isPageVisible.value })
  
  // 页面可见时，如果有待刷新的报告，确保定时器在运行
  if (isPageVisible.value && hasPendingOrGeneratingReports()) {
    checkAndStartAutoRefresh()
  }
}

// 监听报告列表变化，自动调整定时器
watch(
  () => reportList.value,
  () => {
    checkAndStartAutoRefresh()
  },
  { deep: true }
)

// 组件挂载
onMounted(() => {
  logger.info('Report list page mounted')
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 初始化页面可见性状态
  isPageVisible.value = !document.hidden
  
  loadReportList()
})

// 组件卸载时清理定时器和事件监听
onBeforeUnmount(() => {
  logger.info('Report list page unmounting, cleaning up')
  
  // 清理定时器
  stopAutoRefresh()
  
  // 清理用户交互计时器
  if (userInteractionTimer.value) {
    clearTimeout(userInteractionTimer.value)
    userInteractionTimer.value = null
  }
  
  // 移除页面可见性监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style lang="scss" scoped>
.report-list-container {
  padding: var(--gap-lg);
  background: var(--background, #f7f9fc);
  min-height: calc(100vh - 56px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gap-lg);

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--color-primary, #1e3a8a);
    margin: 0;
  }
}

.search-card {
  margin-bottom: var(--gap-lg);

  .search-form {
    display: flex;
    gap: var(--gap-md);
    align-items: end;
    flex-wrap: wrap;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }

  .search-actions {
    display: flex;
    justify-content: flex-end;
    gap: var(--gap-sm);
    margin-top: var(--gap-md);
  }
}

.content-card {
  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: var(--text-secondary, #6b7280);
    font-size: 14px;
    gap: var(--gap-sm);

    .el-icon {
      font-size: 20px;
    }
  }

  .result-count-bar {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    margin-bottom: var(--gap-lg);
    padding: var(--gap-sm) 0;
  }

  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: var(--gap-lg);
    margin-bottom: var(--gap-lg);
  }

  .report-card {
    background: var(--surface, #ffffff);
    border-radius: 8px;
    padding: var(--gap-lg);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--border-color, #e5e7eb);
    transition: all 0.2s;
    cursor: pointer;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }

    .card-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: var(--gap-md);

      .report-type-tag {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;

        &.tech {
          background: #dbeafe;
          color: #1e40af;
        }

        &.self {
          background: #e0f2fe;
          color: #0369a1;
        }
      }
    }

    .card-content {
      margin-bottom: var(--gap-md);

      .project-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary, #111827);
        margin: 0 0 var(--gap-sm) 0;
        line-height: 1.4;
      }

      .report-summary {
        font-size: 14px;
        color: var(--text-secondary, #6b7280);
        line-height: 1.6;
        margin: 0;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .card-meta {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: var(--gap-md);
      margin-bottom: var(--gap-md);
      padding: var(--gap-md) 0;
      border-top: 1px solid var(--border-color, #f3f4f6);
      border-bottom: 1px solid var(--border-color, #f3f4f6);

      .meta-item {
        text-align: center;

        .meta-value {
          font-size: 14px;
          font-weight: 600;
          color: var(--color-primary, #1e3a8a);
          margin: 0 0 2px 0;
        }

        .meta-label {
          font-size: 12px;
          color: var(--text-secondary, #9ca3af);
          margin: 0;
        }
      }
    }

    .card-actions {
      display: flex;
      gap: var(--gap-sm);
      padding-top: var(--gap-md);
      border-top: 1px solid var(--border-color, #f3f4f6);
      box-sizing: border-box;

      .action-group-primary {
        display: flex;
        gap: var(--gap-sm);
        flex: 1;
        min-width: 0;
      }

      .action-group-danger {
        display: flex;
        gap: var(--gap-sm);
        padding-left: var(--gap-sm);
        border-left: 1px solid var(--border-color, #e5e7eb);
        flex-shrink: 0;
      }

      .action-btn {
        flex: 1;
        padding: 8px 12px;
        border: 1px solid var(--border-color, #e5e7eb);
        border-radius: 6px;
        background: var(--surface, #ffffff);
        color: var(--text-secondary, #6b7280);
        cursor: pointer;
        font-size: 12px;
        transition: all 0.2s;
        text-align: center;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        min-width: 0;
        box-sizing: border-box;

        &:hover {
          background: var(--background-hover, #f9fafb);
          color: var(--text-primary, #374151);
          border-color: var(--border-color-hover, #d1d5db);
        }

        &.primary {
          background: var(--color-primary, #1e3a8a);
          color: var(--surface, #ffffff);
          border-color: var(--color-primary, #1e3a8a);

          &:hover {
            background: var(--color-primary-hover, #1e40af);
          }
        }

        &.danger {
          background: #dc2626;
          color: var(--surface, #ffffff);
          border-color: #dc2626;
          font-weight: 600;
          flex: 0 0 auto;
          min-width: 70px;
          max-width: 80px;

          &:hover {
            background: #b91c1c;
            border-color: #b91c1c;
          }
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 80px 20px;
    background: var(--surface, #ffffff);
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    .empty-icon {
      font-size: 64px;
      color: var(--text-secondary, #d1d5db);
      margin-bottom: var(--gap-lg);
    }

    .empty-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary, #374151);
      margin: 0 0 var(--gap-sm) 0;
    }

    .empty-description {
      font-size: 14px;
      color: var(--text-secondary, #9ca3af);
      margin: 0 0 var(--gap-lg) 0;
    }
  }

  .pagination-section {
    margin-top: var(--gap-lg);
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)) !important;
  }
}

@media (max-width: 768px) {
  .report-list-container {
    padding: var(--gap-md);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--gap-md);
  }

  .search-form {
    flex-direction: column;
    align-items: stretch !important;

    :deep(.el-form-item) {
      width: 100%;

      .el-input,
      .el-select {
        width: 100% !important;
      }
    }
  }

  .card-grid {
    grid-template-columns: 1fr !important;
  }

  .card-meta {
    grid-template-columns: 1fr !important;
    text-align: left !important;
  }

  .card-actions {
    flex-direction: column;
  }
}
</style>

