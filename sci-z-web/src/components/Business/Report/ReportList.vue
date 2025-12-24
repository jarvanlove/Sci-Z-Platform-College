<!--
/**
 * @description 报告列表业务组件
 * 展示所有报告，支持搜索、筛选、分页等功能
 * 使用卡片视图展示报告列表
 */
-->
<template>
  <div class="report-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ $t('report.listPage.title') }}</h1>
    </div>

    <!-- 搜索筛选和列表区域 -->
    <BaseCard class="content-card">
      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="$t('report.listPage.keywordPlaceholder')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
          @blur="handleKeywordBlur"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="searchForm.reportType"
          :placeholder="$t('report.listPage.reportTypePlaceholder')"
          clearable
          style="width: 160px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="option in reportTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        
        <el-select
          v-model="searchForm.status"
          :placeholder="$t('report.listPage.statusPlaceholder')"
          clearable
          style="width: 160px"
          @change="handleFilterChange"
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
          :start-placeholder="$t('common.startDate')"
          :end-placeholder="$t('common.endDate')"
          style="width: 240px"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        
        <el-button type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ $t('common.search') }}
        </el-button>
        <el-button type="primary" @click="handleReset">
          <el-icon><Refresh /></el-icon>
          {{ $t('common.reset') }}
        </el-button>
        <el-button type="primary" @click="handleGenerateReport" style="white-space: nowrap;">
          <el-icon><Plus /></el-icon>
          {{ $t('report.listPage.generateNewReport') }}
        </el-button>
      </div>
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ $t('common.loading') }}</span>
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
            <BaseTooltip 
              v-if="report.summary && report.summary.length > 50"
              :content="report.summary" 
              placement="top"
            >
              <p class="report-summary">{{ report.summary }}</p>
            </BaseTooltip>
            <p v-else class="report-summary">{{ report.summary || $t('report.listPage.noSummary') }}</p>
          </div>

          <div class="card-meta">
            <div class="meta-item">
              <p class="meta-label">{{ $t('report.listPage.generateTime') }}</p>
              <p class="meta-value">{{ formatDate(report.generateTime || report.createdTime) }}</p>
            </div>
            <div class="meta-item">
              <p class="meta-label">{{ $t('report.listPage.creator') }}</p>
              <p class="meta-value">{{ report.creatorName || '-' }}</p>
            </div>
            <div class="meta-item">
              <p class="meta-label">{{ $t('report.status') }}</p>
              <p class="meta-value">
                <el-tag :type="getStatusTagType(report.status)" size="small" class="status-tag">
                  {{ getStatusText(report.status) }}
                </el-tag>
              </p>
            </div>
          </div>

          <div class="card-actions" @click.stop>
            <!-- 预览按钮 -->
            <BaseTooltip
              v-if="canPreview(report)"
              :content="$t('report.listPage.preview')"
              placement="top"
            >
              <button
                class="action-btn btn-info"
                @click.stop="handlePreview(report)"
              >
                <el-icon><View /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 下载按钮 -->
            <BaseTooltip
              v-if="canDownload(report)"
              :content="$t('report.listPage.download')"
              placement="top"
            >
              <el-dropdown
                @command="handleDownload"
                trigger="click"
                @click.stop.prevent
              >
                <span 
                  class="action-btn btn-success" 
                  style="cursor: pointer;"
                  @click.stop.prevent
                >
                  <el-icon><Download /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      :command="{id: report.id, attachmentId: report.attachmentId, format: 'word'}"
                    >
                      <el-icon><Edit /></el-icon>
                      Word格式
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{id: report.id, attachmentId: report.attachmentId, format: 'pdf'}"
                    >
                      <el-icon><Document /></el-icon>
                      PDF格式
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </BaseTooltip>
            <!-- 重新生成按钮 -->
            <BaseTooltip :content="$t('report.listPage.regenerate')" placement="top">
              <button
                class="action-btn btn-warning"
                @click.stop="handleRegenerate(report)"
              >
                <el-icon><Refresh /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 删除按钮 -->
            <BaseTooltip :content="$t('common.delete')" placement="top">
              <button
                class="action-btn btn-danger"
                @click.stop="handleDelete(report)"
              >
                <el-icon><Delete /></el-icon>
              </button>
            </BaseTooltip>
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

    <!-- 文件预览组件 -->
    <FilePreview
      v-model="showPreviewDialog"
      :file-info="previewFileInfo"
      @close="closePreview"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Loading, View, Download, Delete, Document, Edit } from '@element-plus/icons-vue'
import { BaseCard, BaseButton, BaseDatePicker, BasePagination, BaseTooltip, FilePreview } from '@/components/Common'
import { getReportManagementList, deleteReportManagement } from '@/api/Report'
import { downloadFile } from '@/api/File/file'
import { createLogger } from '@/utils/simpleLogger'
import { formatDate } from '@/utils/date'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('ReportList')

// 响应式数据
const loading = ref(false)
const reportList = ref([])

// 文件预览相关
const showPreviewDialog = ref(false)
const previewFileInfo = ref(null)

// 定时刷新相关
const refreshTimer = ref(null)
const REFRESH_INTERVAL = 5000 // 5秒刷新一次

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
  logger.info('User changed filter')
  pagination.current = 1
  loadReportList()
}

// 日期变化处理
const handleDateChange = () => {
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

// 判断是否可以下载/预览
// 规则：attachmentId 有值 → 可以下载/预览
const canDownloadPreview = (record) => {
  return record.attachmentId != null
}

// 判断是否可以下载
const canDownload = (record) => {
  return canDownloadPreview(record)
}

// 判断是否可以预览
const canPreview = (record) => {
  return canDownloadPreview(record)
}

// 预览处理 - 完全按照申报列表的实现
const handlePreview = (report) => {
  const { id, attachmentId, projectName } = report
  
  logger.info('User started preview', { id, attachmentId })
  
  // 必须有 attachmentId 才能预览
  if (!attachmentId) {
    ElMessage.error(t('report.listPage.noAttachment') || '报告文件尚未生成，无法预览')
    logger.warn('Preview failed: attachmentId is missing', { id })
    return
  }
  
  // 使用通用预览组件
  // 注意：由于列表接口可能没有返回文件名，使用默认的 .docx 扩展名
  // FilePreview 组件会从预览 URL 中自动识别文件类型
  previewFileInfo.value = {
    name: `${projectName || '报告文件'}.docx`, // 添加默认扩展名，帮助识别文件类型
    attachmentId
  }
  showPreviewDialog.value = true
}

/**
 * 关闭预览
 */
const closePreview = () => {
  showPreviewDialog.value = false
  previewFileInfo.value = null
}

// 下载报告 - 参考申报列表的实现
const handleDownload = async (command) => {
  const { id, attachmentId, format } = command
  const formatNames = {
    pdf: 'PDF',
    docx: 'Word',
    word: 'Word'
  }
  
  try {
    logger.info('User started download', { id, attachmentId, format })
    
    // 必须有 attachmentId 才能下载
    if (!attachmentId) {
      ElMessage.error(t('report.listPage.noAttachment') || '报告文件尚未生成，无法下载')
      logger.warn('Download failed: attachmentId is missing', { id })
      return
    }
  
    const fileFormat = format === 'word' ? 'docx' : format // 将 word 转换为 docx
    
    // 调用下载接口
    const response = await downloadFile(attachmentId, fileFormat)
    
    // 创建 blob URL 并触发下载
    // 后端应该正确设置 Content-Disposition 头，浏览器会自动处理文件名
    const blob = new Blob([response.data], { 
      type: response.headers['content-type'] || 'application/octet-stream' 
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    // 使用简单的默认文件名，如果后端正确设置了 Content-Disposition 头，浏览器会使用后端提供的文件名
    link.download = `报告_${id}.${fileFormat || 'pdf'}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success(t('report.listPage.downloadComplete', { format: formatNames[format] || '文件' }) || '文档下载完成')
    logger.info('Download completed', { id, attachmentId, format: fileFormat })
  } catch (error) {
    logger.error('Download failed', error)
    ElMessage.error(t('report.listPage.downloadError') || '下载失败')
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
  logger.info('User changed page', { page })
  pagination.current = page
  loadReportList()
}

// 每页数量变化处理
const handlePageSizeChange = (size) => {
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
    // 只有在没有正在加载时才刷新
    if (!loading.value) {
      logger.debug('Auto refreshing report list')
      loadReportList()
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

// 检查并启动/停止定时刷新
const checkAndStartAutoRefresh = () => {
  if (hasPendingOrGeneratingReports()) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
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
  loadReportList()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  logger.info('Report list page unmounted, cleaning up timer')
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.report-list-container {
  padding: var(--gap-lg);
  background: var(--bg-secondary);
  min-height: calc(100vh - 56px);
  width: 100%;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: nowrap;
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

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--gap-lg);
  margin-bottom: var(--gap-lg);
  
  @media (max-width: 1400px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.report-card {
    background: var(--surface);
    border-radius: 12px;
    padding: var(--gap-lg);
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border);
    transition: all 0.3s ease;
    cursor: pointer;
    position: relative;
    overflow: hidden;

    &:hover {
      box-shadow: var(--shadow-md);
      transform: translateY(-2px);
      border-color: var(--border-hover);
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
          color: var(--color-primary) !important;
        }

        &.self {
          background: #e0f2fe;
          color: var(--color-primary) !important;
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
        color: var(--text-3, #6b7280);
        font-weight: 400;
        line-height: 1.6;
        margin: 0;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
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
          font-weight: 400;
          color: var(--text-3, #6b7280);
          margin: 0;
          
          .status-tag {
            border-radius: 12px !important;
            padding: 4px 8px !important;
            font-size: 12px !important;
            font-weight: 500 !important;
          }
        }

        .meta-label {
          font-size: 14px;
          font-weight: 600;
          color: var(--color-primary);
          margin: 0 0 4px 0;
        }
      }
    }

    .card-actions {
      display: flex;
      gap: 12px;
      padding-top: var(--gap-md);
      border-top: 1px solid var(--border-color, #f3f4f6);
      box-sizing: border-box;
      width: 100%;
      justify-content: space-between;
      align-items: center;
      
      > * {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      .action-btn {
        min-width: 32px;
        width: 100%;
        height: 28px;
        padding: 4px;
      }
      
      .el-dropdown {
        flex: 1;
        display: flex;
        
        .action-btn {
          width: 100%;
          min-width: 32px;
          height: 28px;
          padding: 4px;
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
    display: flex;
    align-items: center;
    justify-content: center;
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
  
  &.btn-info {
    color: var(--text-3);
    border-color: var(--text-3);
    
    &:hover:not(:disabled) {
      background: var(--text-3);
      color: var(--surface);
    }
  }
  
  &.btn-warning {
    color: #f59e0b;
    border-color: #f59e0b;
    
    &:hover:not(:disabled) {
      background: #f59e0b;
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

// 下载下拉菜单样式
:deep(.el-dropdown-menu) {
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      font-size: 16px;
    }
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

  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: var(--gap-sm);
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

