<!--
/**
 * @description 申报列表业务组件
 * 展示所有申报项目，支持搜索、筛选、分页、状态管理等功能
 */
-->
<template>
  <div class="declaration-list-container">
    <div class="page-header">
      <BackButton :tooltip="$t('practice.backToPractice')" @click="handleBack" />
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
          <span class="button-text">{{ $t('common.search') }}</span>
        </el-button>
        <el-button type="primary" @click="handleReset">
          <el-icon><Refresh /></el-icon>
          <span class="button-text">{{ $t('common.reset') }}</span>
        </el-button>
        <el-button type="primary" @click="handleNewDeclaration">
          <el-icon><Plus /></el-icon>
          <span class="button-text">{{ $t('declaration.newDeclaration') }}</span>
        </el-button>
      </div>

      <!-- 申报列表表格 -->
      <BaseTable
        :data="declarations"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        :action-width="240"
        action-fixed="right"
        :empty-text="$t('declaration.noData')"
        stripe
        class="declaration-table"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      >
        <!-- 申报编号列自定义 - 显示前4位后4位，中间省略 -->
        <template #number="{ row }">
          <BaseTooltip :content="row.number || '-'" placement="top">
            <div class="number-cell base-table__cell-wrap">
              {{ formatNumber(row.number) }}
            </div>
          </BaseTooltip>
        </template>

        <!-- 研究方向列自定义 -->
        <template #direction="{ row }">
          <div class="direction-cell base-table__cell-wrap">{{ row.direction }}</div>
        </template>

        <!-- 研究领域列自定义 - 只显示前6个字符，鼠标悬浮显示完整内容 -->
        <template #fields="{ row }">
          <BaseTooltip 
            :content="formatFields(row.fields)" 
            placement="top"
          >
            <div class="fields-cell base-table__cell-wrap">
              {{ truncateFields(row.fields, 6) }}
            </div>
          </BaseTooltip>
        </template>

        <!-- 研究主题列自定义 -->
        <template #topic="{ row }">
          <div class="topic-cell base-table__cell-wrap">{{ row.topic || $t('declaration.noTopic') }}</div>
        </template>

        <!-- 申报状态列自定义 - 只显示状态标签，不提供点击交互 -->
        <template #declarationStatus="{ row }">
          <div class="status-cell-wrapper">
            <!-- 只显示状态标签，不提供点击交互 -->
            <span
              class="status-tag"
              :class="`status-${row.statusType}`"
            >
              {{ row.status }}
            </span>
          </div>
        </template>

        <!-- ========== 旧的申报状态列交互代码（已注释，保留备用） ========== -->
        <!--
        <template #declarationStatus="{ row }">
          <div class="status-cell-wrapper" @click.stop.prevent>
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
        -->

        <!-- 工作流状态列自定义 -->
        <template #workflowStatus="{ row }">
          <div class="status-cell-wrapper">
            <span
              class="workflow-status-tag"
              :class="`workflow-status-${row.workflowStatus}`"
            >
              {{ getWorkflowStatusLabel(row.workflowStatus, row.workflowStatusDescription) }}
            </span>
          </div>
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <div class="action-buttons">
            <!-- 更新申报状态按钮：仅当申报书生成状态为「已完成」时显示 -->
            <BaseTooltip
              v-if="row.workflowStatus === 'completed'"
              :content="$t('declaration.updateStatus') || '更新申报状态'"
              placement="top"
            >
              <el-dropdown
                @command="(command) => handleStatusEdit(row.id, command)"
                trigger="click"
                @click.stop.prevent
              >
                <span 
                  class="action-btn btn-warning" 
                  style="cursor: pointer;"
                  @click.stop.prevent
                >
                  <el-icon><Edit /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu class="status-dropdown-menu">
                    <el-dropdown-item
                      v-for="option in statusUpdateOptions"
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
            </BaseTooltip>
            <!-- 查看详情按钮 -->
            <BaseTooltip :content="$t('common.view')" placement="top">
              <button
                class="action-btn btn-primary"
                @click.stop="handleView(row.id)"
              >
                <el-icon><TopRight /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 下载按钮 -->
            <BaseTooltip
              v-if="canDownload(row)"
              :content="$t('declaration.download')"
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
            </BaseTooltip>
            <!-- 申报书编辑上传按钮：有申报书生成状态时显示，上传后覆盖当前申报书 -->
            <BaseTooltip
              v-if="canUploadDocument(row)"
              :content="$t('declaration.uploadDocumentEdit') || '申报书编辑'"
              placement="top"
            >
              <button
                class="action-btn btn-upload"
                @click.stop="handleUploadDocument(row)"
              >
                <el-icon><Upload /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 预览按钮 -->
            <BaseTooltip
              v-if="canPreview(row)"
              :content="$t('declaration.preview')"
              placement="top"
            >
              <button
                class="action-btn btn-info"
                @click.stop="handlePreview(row)"
              >
                <el-icon><View /></el-icon>
              </button>
            </BaseTooltip>
          </div>
        </template>
      </BaseTable>
    </BaseCard>

    <!-- 申报书编辑：上传本地文件弹窗（与项目里程碑一致：先选文件展示，点击开始上传后调接口并显示进度） -->
    <el-dialog
      v-model="showDocumentUploadDialog"
      :title="$t('knowledge.uploadLocalFile')"
      width="800px"
      :close-on-click-modal="false"
      @close="onDocumentUploadDialogClose"
    >
      <!-- 上传进度列表（点击开始上传后显示） -->
      <FileUploadProgressList
        v-if="showDocumentUploadProgress && uploadDocumentResults.length > 0"
        :upload-results="uploadDocumentResults"
        @close="handleDocumentUploadProgressClose"
      />
      <!-- 文件选择区域（未开始上传时显示：拖拽区 + 待上传列表 + 开始上传） -->
      <template v-else>
        <FileUpload
          v-model="declarationPendingFiles"
          mode="batch"
          :limit="1"
          :max-batch-count="1"
          :multiple="false"
          accept=".doc,.docx,.pdf"
          :drag="true"
          :show-file-list="true"
          :show-tips="false"
          :allowed-types="declarationDocumentAllowedTypes"
          :max-size="200"
          :upload-tip="$t('project.detail.uploadHint') || '支持拖拽上传，可批量上传多个文件'"
          @batch-upload="onDeclarationBatchUpload"
        />
      </template>
      <template #footer>
        <el-button v-if="!showDocumentUploadProgress" @click="closeDocumentUploadDialog">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览组件 - 已改为新窗口打开，保留组件以防需要 -->
    <!-- <FilePreview
      v-model="showPreviewDialog"
      :file-info="previewFileInfo"
      @close="closePreview"
    /> -->
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Document, Edit, View, Download, TopRight, Upload } from '@element-plus/icons-vue'
import { BaseCard, BaseTable, BaseTooltip, BackButton } from '@/components/Common'
import { FileUpload } from '@/components/Business/Form'
import FileUploadProgressList from '@/components/Business/Knowledge/FileUploadProgressList.vue'
import { validateFileSize, validateFileType } from '@/constants/attachment'
import { DECLARATION_STATUS_CONFIG } from '@/utils/constants'
import { 
  getDeclarationList, 
  updateDeclarationStatus,
  uploadDeclarationDocument
} from '@/api/Declaration'
import { downloadFile } from '@/api/File'
import { createLogger } from '@/utils/simpleLogger'
import { openFilePreviewInNewWindow } from '@/utils/file'

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

// 可编辑状态选项（用于申报状态列的下拉菜单）
const editableStatusOptions = computed(() => [
  { label: t('declaration.statusSubmitting'), value: 'submitting' },
  { label: t('declaration.statusSuccess'), value: 'success' },
  { label: t('declaration.statusFailed'), value: 'failed' }
])

// 状态更新选项（用于操作列按钮的下拉菜单）：申报成功、申报未通过
const statusUpdateOptions = computed(() => [
  { label: t('declaration.statusSuccess'), value: 'success' },
  { label: t('declaration.statusRejected') || '申报未通过', value: 'failed' }
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

// 表格列配置 - 优化自适应策略，参考 Element Plus 官方最佳实践
const tableColumns = computed(() => [
  {
    prop: 'number',
    label: t('declaration.number'),
    minWidth: 120, // 🔥 减小最小宽度，将间距给到研究方向
    align: 'center'
  },
  {
    prop: 'applicant',
    label: t('declaration.applicant'),
    minWidth: 100, // 优化最小宽度
    align: 'center'
  },
  {
    prop: 'submitTime',
    label: t('declaration.submitTime'),
    minWidth: 110, // 优化最小宽度
    align: 'center'
  },
  {
    prop: 'direction',
    label: t('declaration.direction'),
    minWidth: 220, 
    showOverflowTooltip: false, // 允许换行，不需要 tooltip
    wrap: true, // 🔥 明确指定允许换行
    align: 'center' // 🔥 表头和内容都居中对齐，位于列值的正中间
  },
  {
    prop: 'fields',
    label: t('declaration.fields'),
    minWidth: 100, // 🔥 优化最小宽度，允许根据内容自适应
    wrap: true, // 🔥 明确指定允许换行
    align: 'left', // 🔥 内容左对齐
    headerAlign: 'left' // 🔥 表头左对齐
  },
  {
    prop: 'topic',
    label: t('declaration.topic'),
    minWidth: 155, // 🔥 优化最小宽度，允许自适应扩展
    showOverflowTooltip: false, // 允许换行，不需要 tooltip
    wrap: true, // 🔥 明确指定允许换行
    headerAlign: 'left' // 🔥 表头左对齐
  },
  {
    prop: 'declarationStatus',
    label: t('declaration.declarationStatus'),
    minWidth: 100, // 🔥 优化最小宽度，确保状态文字完整显示
    align: 'center',
    headerAlign: 'center' // 🔥 表头居中对齐
  },
  {
    prop: 'workflowStatus',
    label: t('declaration.workflowStatusColumn'),
    minWidth: 120,
    align: 'center'
  }
])

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

// 申报书编辑上传：展示时机与下载按钮一致（有申报书时可替换）
const canUploadDocument = (record) => {
  return canDownload(record)
}

// 申报书编辑上传：仅允许 doc / docx / pdf，最大 200MB（调用申报书覆盖接口，保证列表下载/预览可用）
const declarationDocumentAllowedTypes = ['doc', 'docx', 'pdf']

const showDocumentUploadDialog = ref(false)
const declarationIdForUpload = ref(null)
const declarationPendingFiles = ref([])
const showDocumentUploadProgress = ref(false)
const uploadDocumentResults = ref([])

const handleUploadDocument = (row) => {
  declarationIdForUpload.value = row.id
  declarationPendingFiles.value = []
  showDocumentUploadProgress.value = false
  uploadDocumentResults.value = []
  showDocumentUploadDialog.value = true
}

// 点击「开始上传」后调用：校验 → 调后端 → 展示进度
const onDeclarationBatchUpload = async (files) => {
  const declarationId = declarationIdForUpload.value
  if (!declarationId || !files || files.length === 0) {
    ElMessage.warning(t('declaration.uploadDocumentNoFile') || '请先选择文件')
    return
  }
  const file = files[0]

  const maxSizeMB = 200
  const sizeValidation = validateFileSize(file, maxSizeMB)
  if (!sizeValidation.passed) {
    ElMessage.error(`${file.name}: ${sizeValidation.reason}`)
    return
  }
  const typeValidation = validateFileType(file, declarationDocumentAllowedTypes)
  if (!typeValidation.passed) {
    ElMessage.error(`${file.name}: ${typeValidation.reason}`)
    return
  }

  showDocumentUploadProgress.value = true
  uploadDocumentResults.value = [{
    fileName: file.name,
    success: false,
    errorMessage: null,
    attachmentId: null,
    fileSize: file.size || 0,
    stage: 1,
    stageDescription: t('knowledge.stageMinIO') || '正在上传到存储...'
  }]

  try {
    const res = await uploadDeclarationDocument(declarationId, file)
    const ok = res?.code === 200 && res?.data
    uploadDocumentResults.value = [{
      fileName: file.name,
      success: !!ok,
      errorMessage: ok ? null : (res?.message || t('declaration.uploadDocumentFailed')),
      attachmentId: res?.data?.id ?? null,
      fileSize: file.size || 0,
      stage: ok ? 5 : 0,
      stageDescription: ok ? (t('knowledge.uploadComplete') || '上传完成') : (res?.message || t('declaration.uploadDocumentFailed'))
    }]
    if (ok) {
      ElMessage.success(t('declaration.uploadDocumentSuccess') || '申报书已更新')
    }
  } catch (err) {
    uploadDocumentResults.value = [{
      fileName: file.name,
      success: false,
      errorMessage: err.response?.data?.message || err.message || (t('declaration.uploadDocumentFailed') || '申报书上传失败'),
      attachmentId: null,
      fileSize: file.size || 0,
      stage: 0,
      stageDescription: t('knowledge.uploadFailed') || '上传失败'
    }]
    if (!err._messageShown) {
      ElMessage.error(err.response?.data?.message || err.message || t('declaration.uploadDocumentFailed'))
    }
  }
}

const onDocumentUploadDialogClose = () => {
  declarationIdForUpload.value = null
  declarationPendingFiles.value = []
  showDocumentUploadProgress.value = false
  uploadDocumentResults.value = []
}

const closeDocumentUploadDialog = () => {
  showDocumentUploadDialog.value = false
  onDocumentUploadDialogClose()
}

const handleDocumentUploadProgressClose = () => {
  showDocumentUploadDialog.value = false
  onDocumentUploadDialogClose()
  loadDeclarations()
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

// 格式化申报编号 - 显示前4位后4位，中间省略
const formatNumber = (number) => {
  if (!number || number === '-') return '-'
  if (number.length <= 8) return number // 如果长度小于等于8，直接显示
  return `${number.substring(0, 4)}...${number.substring(number.length - 4)}`
}

// 🔥 格式化研究领域：将所有字段合并成一个字符串
const formatFields = (fields) => {
  if (!fields || !Array.isArray(fields) || fields.length === 0) {
    return '-'
  }
  return fields.join('、') // 使用中文顿号分隔
}

// 🔥 截取研究领域：只显示前6个字符
const truncateFields = (fields, maxLength = 6) => {
  if (!fields || !Array.isArray(fields) || fields.length === 0) {
    return '-'
  }
  const fullText = fields.join('、') // 使用中文顿号分隔
  if (fullText.length <= maxLength) {
    return fullText
  }
  return fullText.substring(0, maxLength) + '...'
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
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(t('declaration.loadError'))
    }
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

// 🔥 使用 watch 监听筛选条件变化，自动触发查询（带防抖）
let searchTimer = null
watch(
  () => [searchForm.keyword, searchForm.status],
  () => {
    // 清除之前的定时器
    if (searchTimer) {
      clearTimeout(searchTimer)
    }
    // 设置新的定时器，300ms 后执行查询（输入框防抖）
    searchTimer = setTimeout(() => {
      logger.info('Filter conditions changed, auto search', { 
        keyword: searchForm.keyword, 
        status: searchForm.status 
      })
      pagination.current = 1
      loadDeclarations()
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

const handleBack = () => {
  router.push('/practice')
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
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(t('declaration.downloadFailed'))
    }
  }
}

// 预览处理 - 在新窗口打开
const handlePreview = async (row) => {
  const { id, attachmentId, direction } = row
  
  logger.info('User started preview', { id, attachmentId })
  
  // 必须有 attachmentId 才能预览
  if (!attachmentId) {
    ElMessage.error(t('declaration.attachmentIdNotFound'))
    logger.warn('Preview failed: attachmentId is missing', { id })
    return
  }
  
  try {
    const fileName = `${direction || '申报文件'}.docx`
    await openFilePreviewInNewWindow(attachmentId, fileName)
    logger.info('Preview opened in new window', { attachmentId, fileName })
  } catch (error) {
    logger.error('Failed to open preview in new window', error)
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(error.message || t('declaration.previewFailed') || '预览失败，请稍后重试')
    }
  }
}

// 状态字符串到数字的映射（用于更新状态接口）
const STATUS_TYPE_TO_NUMBER = {
  submitting: 1,
  success: 2,
  failed: 3
}

// 状态编辑处理（用于操作列按钮的下拉菜单，逻辑与之前申报状态列完全一致）
const handleStatusEdit = async (id, newStatusType) => {
  // 状态标签映射（用于显示和确认对话框）
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
    // 确认对话框使用原始状态标签
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
    
    // 更新本地状态（使用原始状态标签，因为表格中显示的是原始标签）
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
      // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
      if (!error._messageShown) {
        ElMessage.error(t('declaration.statusUpdateFailed'))
      }
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
  padding: var(--gap-lg);
  background: var(--bg-secondary);
  min-height: calc(100vh - 56px);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow-x: hidden; // 🔥 不允许内容超出容器，横向滚动交给表格内部处理
}

.page-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
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
  // 🔥 允许内容溢出，让表格可以显示自己的滚动条
  overflow: visible;
  
  :deep(.base-card__content) {
    padding: 0;
    width: 100%;
    max-width: 100%;
    // 🔥 确保内容区域不限制表格宽度
    overflow: visible;
  }
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
  
  // 🔥 按钮图标和文字间距优化
  :deep(.el-button) {
    display: inline-flex !important;
    align-items: center !important;
    gap: 8px !important; // 🔥 图标和文字之间的间距
    
    // 确保图标和文字之间有间距
    .el-icon {
      margin-right: 0 !important;
      margin-left: 0 !important;
    }
    
    .button-text,
    span:not(.el-icon) {
      margin-left: 0 !important;
      margin-right: 0 !important;
    }
    
    // 针对 Element Plus 按钮内部结构
    > * {
      margin-left: 0 !important;
      margin-right: 0 !important;
    }
  }
  
  :deep(.el-select) {
    .el-select__placeholder {
      color: var(--disabled) !important; // 字体颜色与输入框placeholder一致
      font-size: var(--font-size-base) !important;
      font-weight: 400 !important;
    }
    
    .el-select__selected-item {
      color: var(--text) !important; 
      font-size: var(--font-size-base) !important;
      font-weight: 500 !important;
    }
    
    .el-select__wrapper {
      color: var(--text) !important; 
      
      .el-select__selected-item {
        color: var(--text) !important; 
        font-size: var(--font-size-base) !important;
        font-weight: 500 !important;
      }
      
      .el-select__placeholder {
        color: var(--disabled) !important; 
        font-size: var(--font-size-base) !important;
        font-weight: 400 !important;
      }
      
      .el-select__input {
        color: var(--text) !important;
        font-size: var(--font-size-base) !important;
        font-weight: 500 !important;
      }
    }
    
    // 确保下拉框内部输入框样式也一致
    .el-select__input {
      color: var(--text) !important;
      font-size: var(--font-size-base) !important;
      font-weight: 500 !important;
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
  text-align: left; // 🔥 内容左对齐
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
  align-items: center;
  line-height: 1.4;
  font-size: 14px;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  cursor: pointer; // 🔥 鼠标悬浮时显示为手型，提示可以查看完整内容
}

// 状态标签样式
.status-tag {
  padding: 4px 8px !important; // 🔥 确保内边距一致
  border-radius: 12px !important; // 🔥 确保圆角一致，与处理状态保持一致
  font-size: 12px !important; // 🔥 确保字体大小一致
  font-weight: 500 !important; // 🔥 确保字体粗细一致
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
  position: relative;
  
  // 🔥 确保所有状态标签的圆角都一致
  &.status-submitting,
  &.status-success,
  &.status-failed {
    border-radius: 12px !important;
    padding: 4px 8px !important;
    font-size: 12px !important;
    font-weight: 500 !important;
  }
}

.status-submitting {
  background-color: #fef3c7;
  color: #f59e0b;
}

.status-success {
  background-color: #d1fae5; // 🔥 使用更协调的绿色背景（emerald-100）
  color: #059669; // 🔥 使用更标准的成功绿色（emerald-600），更协调美观
}

.status-failed {
  background-color: #fee2e2;
  color: #dc2626;
}

// 工作流状态标签样式
.workflow-status-tag {
  padding: 4px 8px !important; // 🔥 确保内边距一致
  border-radius: 12px !important; // 🔥 确保圆角一致，与申报状态保持一致
  font-size: 12px !important; // 🔥 确保字体大小一致
  font-weight: 500 !important; // 🔥 确保字体粗细一致
  display: inline-flex;
  align-items: center;
  transition: all 0.2s ease;
  
  // 🔥 确保所有工作流状态标签的圆角都一致
  &.workflow-status-pending,
  &.workflow-status-running,
  &.workflow-status-completed,
  &.workflow-status-failed {
    border-radius: 12px !important;
    padding: 4px 8px !important;
    font-size: 12px !important;
    font-weight: 500 !important;
  }
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
  background-color: #d1fae5; // 🔥 使用更协调的绿色背景（emerald-100）
  color: #059669; // 🔥 使用更标准的成功绿色（emerald-600），更协调美观
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
    background: rgba(5, 150, 105, 0.2); // 🔥 使用更协调的绿色（emerald-600透明度）
    color: #10b981; // 🔥 使用更标准的成功绿色（emerald-500），更协调美观
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

// 操作按钮样式 - 4个图标放在一行（更新状态、查看详情、下载、预览）
.action-buttons {
  display: flex;
  flex-direction: row; // 🔥 改为横向排列，图标放在一行
  gap: 6px; // 🔥 减小间距，确保4个按钮能在一行显示
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap; // 🔥 确保不换行
}

.action-btn {
  padding: 4px; // 🔥 减小内边距，确保按钮更紧凑
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: none;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  justify-content: center; // 🔥 图标居中显示
  gap: 0; // 🔥 只有图标，不需要 gap
  user-select: none;
  min-width: 28px; // 🔥 减小最小宽度，确保4个按钮能在一行显示
  width: 28px; // 🔥 固定宽度，确保按钮大小一致
  height: 28px; // 🔥 统一高度
  flex-shrink: 0; // 🔥 防止按钮被压缩
  
  // 🔥 图标样式
  .el-icon {
    font-size: 16px; // 🔥 图标大小
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
    display: inline-flex;
    align-items: center;
    
    &:hover:not(:disabled) {
      background: #16a34a;
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
  
  &.btn-info {
    color: var(--text-3);
    border-color: var(--text-3);
    
    &:hover:not(:disabled) {
      background: var(--text-3);
      color: var(--surface);
    }
  }

  &.btn-upload {
    color: #0d9488;
    border-color: #0d9488;

    &:hover:not(:disabled) {
      background: #0d9488;
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

// 表格样式 - 优化自适应显示
.declaration-table {
  width: 100%;
  border-radius: 8px;
  overflow: visible;
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
      }
      
      // 🔥 研究方向表头居中对齐（通过列的 align 属性，BaseTable 会自动添加 is-center 类）
      // 使用稳定的 data-column-key，避免 :contains() 这类无效选择器造成兼容问题
      th[data-column-key="direction"],
      th.is-center {
        text-align: center !important;
        
        .cell {
          text-align: center !important;
          display: flex !important;
          justify-content: center !important;
          align-items: center !important;
        }
      }
      
      // 🔥 研究领域表头左对齐
      th[data-column-key="fields"] {
        text-align: left !important;
        
        .cell {
          text-align: left !important;
        }
      }
      
      // 🔥 研究课题表头左对齐
      th[data-column-key="topic"] {
        text-align: left !important;
        
        .cell {
          text-align: left !important;
        }
      }
    }
    
    // 表体样式 - BaseTable 已提供通用换行支持，这里只做微调
    .el-table__body {
      td {
        padding: 12px 16px !important;
        font-size: 14px;
        color: var(--text);
      }
      
      // 🔥 研究方向列内容居中对齐（与表头保持一致）
      td.is-center {
        text-align: center !important;
        
        .cell {
          text-align: center !important;
          display: flex !important;
          justify-content: center !important;
          align-items: center !important;
        }
        
        // 研究方向列的内容（direction-cell）也需要居中对齐
        .direction-cell {
          text-align: center !important;
          margin: 0 auto; // 🔥 确保内容居中
        }
      }
      
      // 🔥 申报状态和处理状态列的值居中显示
      .status-cell-wrapper {
        display: flex;
        justify-content: center; // 🔥 内容居中对齐
        align-items: center;
        padding-left: 0;
        margin-left: 0; // 🔥 不需要偏移
      }
      
      // 🔥 研究领域列内容左对齐
      td[data-column-key="fields"] {
        text-align: left !important;
      }
      
      // 🔥 研究课题列内容左对齐
      td[data-column-key="topic"] {
        text-align: left !important;
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

