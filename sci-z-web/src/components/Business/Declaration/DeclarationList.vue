<!--
/**
 * @description 申报列表业务组件
 * 展示所有申报项目，支持搜索、筛选、分页、状态管理等功能
 */
-->
<template>
  <div class="declaration-list-container">
    <div class="page-header">
      <h1 class="page-title">{{ $t('declaration.title') }}</h1>
    </div>

    <BaseCard class="content-card">
      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="$t('declaration.keywordPlaceholder')"
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
          :placeholder="$t('declaration.declarationStatusPlaceholder')"
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
        
        <el-button type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ $t('common.search') }}
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          {{ $t('common.reset') }}
        </el-button>
        <el-button type="primary" @click="handleNewDeclaration">
          <el-icon><Plus /></el-icon>
          {{ $t('declaration.newDeclaration') }}
        </el-button>
      </div>

      <!-- 申报列表表格 -->
      <BaseTable
        :data="declarations"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        :action-width="200"
        action-fixed="right"
        :empty-text="$t('declaration.noData')"
        stripe
        class="declaration-table"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      >
        <!-- 研究方向列自定义 -->
        <template #direction="{ row }">
          <div class="direction-cell base-table__cell-wrap">{{ row.direction }}</div>
        </template>

        <!-- 研究领域列自定义 -->
        <template #fields="{ row }">
          <div class="fields-cell base-table__cell-wrap">
            <span
              v-for="field in row.fields"
              :key="field"
              class="field-tag"
            >
              {{ field }}
            </span>
          </div>
        </template>

        <!-- 研究主题列自定义 -->
        <template #topic="{ row }">
          <div class="topic-cell base-table__cell-wrap">{{ row.topic || $t('declaration.noTopic') }}</div>
        </template>

        <!-- 申报状态列自定义 -->
        <template #declarationStatus="{ row }">
          <div @click.stop.prevent>
            <!-- 只有处理状态为已完成时才能操作申报状态 -->
            <el-dropdown
              v-if="row.workflowStatus === 'completed'"
              @command="(command) => handleStatusEdit(row.id, command)"
              trigger="click"
              class="status-dropdown"
            >
              <span
                class="status-tag status-clickable"
                :class="`status-${row.statusType}`"
                @click.stop.prevent
              >
                {{ row.status }}
              </span>
              <template #dropdown>
                <el-dropdown-menu class="status-dropdown-menu">
                  <el-dropdown-item
                    v-for="option in editableStatusOptions"
                    :key="option.value"
                    :command="option.value"
                    :disabled="option.value === row.statusType"
                  >
                    <span
                      class="status-tag"
                      :class="`status-${option.value}`"
                    >
                      {{ option.label }}
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <!-- 处理状态未完成时，显示不可点击的状态标签 -->
            <BaseTooltip
              v-else
              :content="$t('declaration.workflowNotCompletedHint')"
              placement="top"
            >
              <span
                class="status-tag status-disabled"
                :class="`status-${row.statusType}`"
              >
                {{ row.status }}
              </span>
            </BaseTooltip>
          </div>
        </template>

        <!-- 工作流状态列自定义 -->
        <template #workflowStatus="{ row }">
          <span
            class="workflow-status-tag"
            :class="`workflow-status-${row.workflowStatus}`"
          >
            {{ getWorkflowStatusLabel(row.workflowStatus, row.workflowStatusDescription) }}
          </span>
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <div class="action-buttons">
            <button
              class="action-btn btn-primary"
              @click.stop="handleView(row.id)"
            >
              {{ $t('common.view') }}
            </button>
            <!-- 根据详细判断规则显示下载和预览按钮 -->
            <div v-if="canDownloadPreview(row)" class="action-row" @click.stop.prevent>
              <el-dropdown
                v-if="canDownload(row)"
                @command="handleDownload"
                trigger="click"
              >
                <span 
                  class="action-btn btn-success" 
                  style="cursor: pointer;"
                  @click.stop.prevent
                >
                  {{ $t('declaration.download') }}
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      :command="{id: row.id, attachmentId: row.attachmentId, format: 'word'}"
                    >
                      <el-icon><Edit /></el-icon>
                      Word格式
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{id: row.id, attachmentId: row.attachmentId, format: 'pdf'}"
                    >
                      <el-icon><Document /></el-icon>
                      PDF格式
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <button
                v-if="canPreview(row)"
                class="action-btn btn-info"
                @click.stop="handlePreview(row)"
              >
                {{ $t('declaration.preview') }}
              </button>
            </div>
          </div>
        </template>
      </BaseTable>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Document, Edit } from '@element-plus/icons-vue'
import { BaseCard, BaseTable, FilePreview, BaseTooltip } from '@/components/Common'
import { DECLARATION_STATUS_CONFIG } from '@/utils/constants'
import { 
  getDeclarationList, 
  updateDeclarationStatus
} from '@/api/Declaration'
import { downloadFile } from '@/api/File'
import { createLogger } from '@/utils/simpleLogger'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('DeclarationList')

// 响应式数据
const loading = ref(false)
const declarations = ref([])

// 文件预览相关
const showPreviewDialog = ref(false)
const previewFileInfo = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 排序信息
const sortInfo = reactive({
  sortBy: 'submitTime',
  sortOrder: 'DESC'
})

// 状态选项（后端使用数字状态：1-申报中，2-申报成功，3-申报失败）
const statusOptions = computed(() => [
  { label: t('common.all'), value: '' },
  { label: t('declaration.statusSubmitting'), value: '1' },
  { label: t('declaration.statusSuccess'), value: '2' },
  { label: t('declaration.statusFailed'), value: '3' }
])

// 可编辑状态选项
const editableStatusOptions = computed(() => [
  { label: t('declaration.statusSubmitting'), value: 'submitting' },
  { label: t('declaration.statusSuccess'), value: 'success' },
  { label: t('declaration.statusFailed'), value: 'failed' }
])

// 获取工作流状态标签（使用后端返回的 workflowStatusDescription，如果没有则使用默认标签）
const getWorkflowStatusLabel = (status, description) => {
  if (description) return description
  
  // 如果后端没有返回描述，使用默认映射
  const defaultLabels = {
    pending: t('declaration.workflowStatusPending') || '待处理',
    running: t('declaration.workflowStatusRunning') || '处理中',
    completed: t('declaration.workflowStatusCompleted') || '已完成',
    failed: t('declaration.workflowStatusFailed') || '失败'
  }
  return defaultLabels[status] || status || '-'
}

// 表格列配置 - 使用自适应策略
const tableColumns = computed(() => [
  {
    prop: 'number',
    label: t('declaration.number'),
    minWidth: 160, // 使用 minWidth 而非固定 width，允许自适应
    align: 'center'
  },
  {
    prop: 'applicant',
    label: t('declaration.applicant'),
    minWidth: 100,
    align: 'center'
  },
  {
    prop: 'submitTime',
    label: t('declaration.submitTime'),
    minWidth: 120,
    align: 'center'
  },
  {
    prop: 'direction',
    label: t('declaration.direction'),
    minWidth: 200, // 降低最小宽度，允许自适应扩展
    showOverflowTooltip: false, // 允许换行，不需要 tooltip
    wrap: true // 🔥 明确指定允许换行
  },
  {
    prop: 'fields',
    label: t('declaration.fields'),
    minWidth: 150, // 使用 minWidth，允许根据内容自适应
    wrap: true // 🔥 明确指定允许换行
  },
  {
    prop: 'topic',
    label: t('declaration.topic'),
    minWidth: 200, // 降低最小宽度，允许自适应扩展
    showOverflowTooltip: false, // 允许换行，不需要 tooltip
    wrap: true // 🔥 明确指定允许换行
  },
  {
    prop: 'declarationStatus',
    label: t('declaration.declarationStatus'),
    minWidth: 120,
    align: 'center'
  },
  {
    prop: 'workflowStatus',
    label: t('declaration.workflowStatus'),
    minWidth: 120,
    align: 'center'
  }
])

// 状态数字到字符串的映射
const STATUS_MAP = {
  1: { type: 'submitting', label: '申报中' },
  2: { type: 'success', label: '申报成功' },
  3: { type: 'failed', label: '申报失败' }
}

// 将后端状态数字转换为前端状态类型
const mapStatusType = (status) => {
  return STATUS_MAP[status]?.type || 'submitting'
}

// 判断是否可以下载/预览（详细判断规则，不简化）
// 规则：工作流完成后 → hasAttachment = true → attachmentId 有值 → 可以下载/预览
const canDownloadPreview = (record) => {
  // 完整判断逻辑：三个条件必须同时满足
  return (
    record.workflowStatus === 'completed' &&
    record.hasAttachment === true &&
    record.attachmentId != null
  )
}

// 判断是否可以下载
const canDownload = (record) => {
  return canDownloadPreview(record)
}

// 判断是否可以预览
const canPreview = (record) => {
  return canDownloadPreview(record)
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

// 加载申报列表
const loadDeclarations = async () => {
  try {
    loading.value = true
    logger.info('Starting to load declaration list data', { 
      pageNo: pagination.current, 
      pageSize: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    
    // 构建请求参数
    const requestParams = {
      pageNo: pagination.current,
      pageSize: pagination.size,
      sortBy: sortInfo.sortBy,
      sortOrder: sortInfo.sortOrder,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status ? parseInt(searchForm.status) : undefined
    }
    
    // 移除空值
    Object.keys(requestParams).forEach(key => {
      if (requestParams[key] === undefined || requestParams[key] === '') {
        delete requestParams[key]
      }
    })
    
    // 调用接口
    const response = await getDeclarationList(requestParams)
    
    // 处理响应数据
    const listData = response?.data || {}
    const records = listData?.records || []
    
    // 调试日志：输出原始响应数据结构
    logger.info('Response data structure', { 
      responseKeys: Object.keys(response || {}),
      hasData: !!response?.data,
      listDataKeys: Object.keys(listData),
      recordsCount: records.length,
      firstRecord: records[0] || null
    })
    
    // 数据映射：后端字段 -> 前端字段
    declarations.value = records.map(record => ({
      id: record.id,
      number: record.number || '-',
      applicant: record.applicantName || '-',
      direction: record.researchDirection || '-',
      topic: record.researchTopic || '-',
      fields: Array.isArray(record.researchFields) ? record.researchFields : [],
      submitTime: formatSubmitTime(record.submitTime),
      status: record.statusDescription || STATUS_MAP[record.status]?.label || '-',
      statusType: mapStatusType(record.status),
      workflowStatus: record.workflowStatus || 'pending',
      workflowStatusDescription: record.workflowStatusDescription || '',
      hasAttachment: record.hasAttachment || false,
      attachmentId: record.attachmentId || null // 新增：附件ID，有附件时返回
    }))
    
    // 更新分页信息
    pagination.total = listData?.total || 0
    pagination.current = listData?.current || pagination.current
    pagination.size = listData?.size || pagination.size
    
    logger.info('Declaration list data loaded successfully', { 
      count: declarations.value.length, 
      total: pagination.total 
    })
  } catch (error) {
    logger.error('Declaration list data loading failed', error)
    ElMessage.error(t('declaration.loadError'))
    declarations.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  logger.info('User performed search', { keyword: searchForm.keyword, status: searchForm.status })
  pagination.current = 1
  loadDeclarations()
}

// 状态筛选处理
const handleStatusChange = () => {
  logger.info('User switched status filter', { status: searchForm.status })
  pagination.current = 1
  loadDeclarations()
}

// 重置搜索
const handleReset = () => {
  logger.info('User reset search conditions')
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.current = 1
  loadDeclarations()
}


// 新建申报
const handleNewDeclaration = () => {
  logger.info('User clicked new declaration')
  router.push('/declaration/create')
}

// 查看详情
const handleView = (id) => {
  logger.info('User viewed declaration details', { id })
  router.push(`/declaration/detail/${id}`)
}


// 下载处理 - 直接调用下载接口，让浏览器自动处理
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
      ElMessage.error(t('declaration.attachmentIdNotFound'))
      logger.warn('Download failed: attachmentId is missing', { id })
      return
    }
  
    const fileFormat = format === 'word' ? 'docx' : format // 将 word 转换为 docx
    
    // 直接调用下载接口
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
    link.download = `申报文档_${id}.${fileFormat || 'pdf'}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success(t('declaration.downloadComplete', { format: formatNames[format] || '文件' }) || '文档下载完成')
    logger.info('Download completed', { id, attachmentId, format: fileFormat })
  } catch (error) {
    logger.error('Download failed', error)
    ElMessage.error(t('declaration.downloadFailed'))
  }
}

// 预览处理
const handlePreview = (row) => {
  const { id, attachmentId, direction } = row
  
  logger.info('User started preview', { id, attachmentId })
  
  // 必须有 attachmentId 才能预览
  if (!attachmentId) {
    ElMessage.error(t('declaration.attachmentIdNotFound'))
    logger.warn('Preview failed: attachmentId is missing', { id })
    return
  }
  
  // 使用通用预览组件
  // 注意：由于列表接口可能没有返回文件名，使用默认的 .docx 扩展名
  // FilePreview 组件会从预览 URL 中自动识别文件类型
  previewFileInfo.value = {
    name: `${direction || '申报文件'}.docx`, // 添加默认扩展名，帮助识别文件类型
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

// 状态字符串到数字的映射（用于更新状态接口）
const STATUS_TYPE_TO_NUMBER = {
  submitting: 1,
  success: 2,
  failed: 3
}

// 状态编辑处理
const handleStatusEdit = async (id, newStatusType) => {
  const statusLabels = {
    submitting: t('declaration.statusSubmitting'),
    success: t('declaration.statusSuccess'),
    failed: t('declaration.statusFailed')
  }
  
  const declaration = declarations.value.find(d => d.id === id)
  if (!declaration) return
  
  const oldStatusType = declaration.statusType
  
  if (newStatusType === oldStatusType) return
  
  try {
    await ElMessageBox.confirm(
      t('declaration.confirmStatusChange', { status: statusLabels[newStatusType] }) || 
      `确定要将申报状态修改为"${statusLabels[newStatusType]}"吗？`,
      t('declaration.confirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
        customClass: 'modern-confirm-dialog',
        center: false,
        showClose: true,
        closeOnClickModal: false,
        closeOnPressEscape: true
      }
    )
    
    logger.info('User confirmed status change', { id, oldStatusType, newStatusType })
    
    // 调用后端接口更新状态（后端需要数字状态）
    const statusNumber = STATUS_TYPE_TO_NUMBER[newStatusType]
    await updateDeclarationStatus({ id, status: statusNumber })
    
    // 更新本地状态
    declaration.statusType = newStatusType
    declaration.status = statusLabels[newStatusType]
    
    ElMessage.success(t('declaration.statusChanged', { status: statusLabels[newStatusType] }) || 
      `申报状态已修改为"${statusLabels[newStatusType]}"`)
    logger.info('Status updated successfully', { id, newStatusType })
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消
      logger.info('User cancelled status change', { id })
    } else {
      // 接口错误
      logger.error('Status update failed', error)
      ElMessage.error(t('declaration.statusUpdateFailed'))
    }
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadDeclarations()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadDeclarations()
}


onMounted(() => {
  loadDeclarations()
})
</script>

<style lang="scss" scoped>
.declaration-list-container {
  padding: 0;
  background: var(--bg);
  min-height: calc(100vh - 60px);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  // 🔥 确保容器充分利用可用宽度，不受父容器限制
  margin: 0;
  overflow-x: visible;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--color-primary);
    margin: 0;
  }
}

.content-card {
  background: var(--surface);
  border-radius: 12px;
  padding: 24px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  // 🔥 确保卡片充分利用宽度
  margin: 0;
  
  :deep(.base-card__content) {
    padding: 0;
    width: 100%;
    max-width: 100%;
  }
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
  
  // 🔥 暗色主题下重置按钮样式优化
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

.number-cell { 
  color: var(--text-3); 
  font-weight: 500; 
}

.applicant-cell { 
  color: var(--text-1);
}

.time-cell { 
  color: var(--text-3); 
}

.direction-cell { 
  color: var(--text-1);
  // 🔥 允许换行，充分利用空间
  white-space: normal;
  word-break: break-word;
  line-height: 1.6;
  max-width: 100%;
}

.topic-cell { 
  color: var(--text-1);
  // 🔥 允许换行，充分利用空间
  white-space: normal;
  word-break: break-word;
  line-height: 1.6;
  max-width: 100%;
}

.fields-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  line-height: 1.4;
  justify-content: center;
  align-items: center;
  
  .field-tag {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    white-space: nowrap;
    background: #e0e7ff;
    color: #6366f1;
    border: none;
    transition: all 0.2s ease;
    
    &:hover {
      background: #c7d2fe;
      color: #4f46e5;
    }
  }
}

// 暗色主题下的标签样式
[data-theme='dark'] .fields-cell .field-tag,
.dark .fields-cell .field-tag {
  background: rgba(99, 102, 241, 0.2);
  color: #818cf8; // 🔥 暗色主题下使用稍亮的蓝色，保持可读性
  
  &:hover {
    background: rgba(99, 102, 241, 0.3);
    color: #a5b4fc;
  }
}

// 状态标签样式
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

// 工作流状态标签样式
.workflow-status-tag {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  transition: all 0.2s ease;
}

.workflow-status-pending {
  background-color: #fef3c7;
  color: #f59e0b;
}

.workflow-status-running {
  background-color: #dbeafe;
  color: #3b82f6;
}

.workflow-status-completed {
  background-color: #dcfce7;
  color: #16a34a;
}

.workflow-status-failed {
  background-color: #fee2e2;
  color: #dc2626;
}

// 暗色主题下的工作流状态样式
[data-theme='dark'] .workflow-status-tag,
.dark .workflow-status-tag {
  &.workflow-status-pending {
    background: rgba(245, 158, 11, 0.2);
    color: #fbbf24;
  }
  
  &.workflow-status-running {
    background: rgba(59, 130, 246, 0.2);
    color: #60a5fa;
  }
  
  &.workflow-status-completed {
    background: rgba(22, 163, 74, 0.2);
    color: #4ade80;
  }
  
  &.workflow-status-failed {
    background: rgba(220, 38, 38, 0.2);
    color: #f87171;
  }
}

.status-clickable {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  transition: all 0.2s ease;
  position: relative;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.status-disabled {
  cursor: not-allowed;
  opacity: 0.6;
  display: inline-flex;
  align-items: center;
}

// 下拉菜单样式
:deep(.status-dropdown-menu) {
  min-width: 120px;
  padding: 8px 0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-color);

  .el-dropdown-menu__item {
    padding: 8px 16px;
    line-height: 1.4;
    transition: background-color 0.2s ease;

    &:hover {
      background-color: var(--bg-hover);
    }

    &.is-disabled {
      opacity: 0.5;
      cursor: not-allowed;

      &:hover {
        background-color: transparent;
      }
    }

    .status-tag {
      width: 100%;
      text-align: center;
      display: block;
      margin: 0;
    }
  }
}

// 操作按钮样式 - 参考 UserManagement.vue
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
  align-items: center;
  
  .action-row {
    display: flex;
    gap: 8px;
    justify-content: center;
  }
}

.action-btn {
  padding: 5px 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: none;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  user-select: none;
  
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

// 确认对话框样式
:deep(.modern-confirm-dialog) {
  border-radius: 8px !important;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  border: none !important;
  width: 400px !important;
}

:deep(.modern-confirm-dialog .el-message-box__header) {
  background: white !important;
  color: #333 !important;
  padding: 20px 24px 16px 24px !important;
  border-bottom: 1px solid #f0f0f0 !important;
}

:deep(.modern-confirm-dialog .el-message-box__title) {
  color: var(--color-primary) !important;
  font-size: 16px !important;
  font-weight: 600 !important;
}

:deep(.modern-confirm-dialog .el-message-box__content) {
  padding: 20px 24px !important;
  background: white !important;
}

:deep(.modern-confirm-dialog .el-message-box__message) {
  font-size: 14px !important;
  line-height: 1.5 !important;
  color: #666 !important;
  margin: 0 !important;
}

:deep(.modern-confirm-dialog .el-message-box__btns) {
  padding: 16px 24px 20px 24px !important;
  background: white !important;
  display: flex !important;
  justify-content: flex-end !important;
  gap: 12px !important;
}

:deep(.modern-confirm-dialog .el-message-box__btns .el-button) {
  padding: 8px 20px !important;
  border-radius: 4px !important;
  font-size: 14px !important;
  font-weight: 400 !important;
  border: 1px solid !important;
  transition: all 0.2s ease !important;
  min-width: 80px !important;
}

:deep(.modern-confirm-dialog .el-message-box__btns .el-button--default) {
  background: white !important;
  border-color: #d9d9d9 !important;
  color: #333 !important;
}

:deep(.modern-confirm-dialog .el-message-box__btns .el-button--default:hover) {
  background: #f5f5f5 !important;
  border-color: #b3b3b3 !important;
  color: #333 !important;
}

:deep(.modern-confirm-dialog .el-message-box__btns .el-button--primary) {
  background: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
  color: white !important;
}

:deep(.modern-confirm-dialog .el-message-box__btns .el-button--primary:hover) {
  background: #1e40af !important;
  border-color: #1e40af !important;
}

// 预览对话框样式
:deep(.preview-dialog) {
  width: 600px;
}

:deep(.preview-dialog .el-message-box__message) {
  white-space: pre-line;
  line-height: 1.6;
}

// 表格样式 - 参考 UserManagement.vue
.declaration-table {
  width: 100%;
  border-radius: 8px;
  overflow: visible;
  border: 1px solid var(--border);
  
  :deep(.base-table) {
    width: 100%;
    display: flex;
    flex-direction: column;
  }
  
  :deep(.base-table__table) {
    width: 100% !important;
    min-width: 100%;
    flex: 1;
    overflow: auto;
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
      }
    }
    
    // 表体样式 - BaseTable 已提供通用换行支持，这里只做微调
    .el-table__body {
      td {
        padding: 12px 16px !important;
        font-size: 14px;
        color: var(--text);
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

// 响应式设计
@media (max-width: 1200px) {
  .declaration-list-container {
    padding: 24px 32px;
  }
}

@media (max-width: 768px) {
  .declaration-list-container {
    padding: 16px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .content-card {
    padding: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>

