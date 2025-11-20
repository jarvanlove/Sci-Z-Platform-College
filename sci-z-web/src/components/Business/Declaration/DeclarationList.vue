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
          :placeholder="$t('declaration.statusPlaceholder')"
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
        @row-click="handleRowClick"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      >
        <!-- 研究方向列自定义 -->
        <template #direction="{ row }">
          <span class="direction-cell">{{ row.direction }}</span>
        </template>

        <!-- 研究领域列自定义 -->
        <template #fields="{ row }">
          <div class="fields-cell">
            <el-tag
              v-for="field in row.fields"
              :key="field"
              size="small"
              class="field-tag"
            >
              {{ field }}
            </el-tag>
          </div>
        </template>

        <!-- 研究主题列自定义 -->
        <template #topic="{ row }">
          <span class="topic-cell">{{ row.topic || $t('declaration.noTopic') }}</span>
        </template>

        <!-- 状态列自定义 -->
        <template #status="{ row }">
          <el-dropdown
            @command="(command) => handleStatusEdit(row.id, command)"
            trigger="click"
            class="status-dropdown"
          >
            <span
              class="status-tag status-clickable"
              :class="`status-${row.statusType}`"
            >
              {{ row.status }}
              <el-icon class="status-arrow"><ArrowDown /></el-icon>
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
            <el-dropdown
              v-if="row.statusType === 'success'"
              @command="handleDownload"
              trigger="click"
            >
              <span class="action-btn btn-success" style="cursor: pointer;">
                {{ $t('declaration.download') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    :command="{id: row.id, format: 'pdf'}"
                  >
                    <el-icon><Document /></el-icon>
                    PDF格式
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="{id: row.id, format: 'word'}"
                  >
                    <el-icon><Document /></el-icon>
                    Word格式
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="{id: row.id, format: 'markdown'}"
                  >
                    <el-icon><Document /></el-icon>
                    Markdown格式
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <button
              v-if="row.statusType === 'success'"
              class="action-btn btn-info"
              @click.stop="handlePreview(row.id)"
            >
              {{ $t('declaration.preview') }}
            </button>
          </div>
        </template>
      </BaseTable>
    </BaseCard>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, ArrowDown, Document } from '@element-plus/icons-vue'
import { BaseCard, BaseTable } from '@/components/Common'
import { DECLARATION_STATUS_CONFIG } from '@/utils/constants'
import { getDeclarationList, updateDeclarationStatus, downloadDeclaration, getDeclarationPreview } from '@/api/Declaration'
import { createLogger } from '@/utils/simpleLogger'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('DeclarationList')

// 响应式数据
const loading = ref(false)
const declarations = ref([])

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

// 状态选项
const statusOptions = computed(() => [
  { label: t('common.all'), value: '' },
  { label: t('declaration.statusSubmitting'), value: 'submitting' },
  { label: t('declaration.statusSuccess'), value: 'success' },
  { label: t('declaration.statusFailed'), value: 'failed' }
])

// 可编辑状态选项
const editableStatusOptions = computed(() => [
  { label: t('declaration.statusSubmitting'), value: 'submitting' },
  { label: t('declaration.statusSuccess'), value: 'success' },
  { label: t('declaration.statusFailed'), value: 'failed' }
])

// 表格列配置
const tableColumns = computed(() => [
  {
    prop: 'number',
    label: t('declaration.number'),
    width: 120,
    align: 'center'
  },
  {
    prop: 'applicant',
    label: t('declaration.applicant'),
    width: 100,
    align: 'center'
  },
  {
    prop: 'submitTime',
    label: t('declaration.submitTime'),
    width: 120,
    align: 'center'
  },
  {
    prop: 'direction',
    label: t('declaration.direction'),
    minWidth: 200,
    showOverflowTooltip: true
  },
  {
    prop: 'fields',
    label: t('declaration.fields'),
    width: 150
  },
  {
    prop: 'topic',
    label: t('declaration.topic'),
    minWidth: 180,
    showOverflowTooltip: true
  },
  {
    prop: 'status',
    label: t('common.status'),
    width: 120,
    align: 'center'
  }
])

// 加载申报列表
const loadDeclarations = async () => {
  try {
    loading.value = true
    logger.info('Starting to load declaration list data')
    
    // TODO: 后端接口开发完成后替换为实际接口调用
    // const response = await getDeclarationList({
    //   page: pagination.current,
    //   size: pagination.size,
    //   keyword: searchForm.keyword,
    //   status: searchForm.status
    // })
    // declarations.value = response.data?.list || []
    // pagination.total = response.data?.total || 0
    
    // 临时模拟数据（后端开发完成后删除）
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    
    const mockData = [
      {
        id: 1,
        number: 'SK20251032101',
        applicant: '张教授',
        direction: '科学家智能体前沿技术研究',
        topic: '具身智能体操作系统与工具链技术',
        fields: ['人工智能', '前沿技术研究与应用'],
        submitTime: '2025-11-03',
        status: '申报成功',
        statusType: 'success'
      },
      {
        id: 2,
        number: 'SK20251032102',
        applicant: '李博士',
        direction: '区块链技术在供应链管理中的应用',
        topic: '面向供应链透明度的区块链溯源技术研究',
        fields: ['区块链', '供应链', '分布式系统'],
        submitTime: '2025-01-14',
        status: '申报成功',
        statusType: 'success'
      },
      {
        id: 3,
        number: 'SK20251032103',
        applicant: '王研究员',
        direction: '量子计算算法优化研究',
        topic: '量子近似优化算法的改进与应用',
        fields: ['量子计算', '算法优化', '物理'],
        submitTime: '2025-01-13',
        status: '申报失败',
        statusType: 'failed'
      },
      {
        id: 4,
        number: 'SK20251032104',
        applicant: '刘教授',
        direction: '生物信息学数据分析方法研究',
        topic: '多组学数据整合分析新方法研究',
        fields: ['生物信息学', '数据分析', '统计学'],
        submitTime: '2025-01-12',
        status: '申报成功',
        statusType: 'success'
      },
      {
        id: 5,
        number: 'SK20251032105',
        applicant: '陈博士',
        direction: '物联网安全防护技术研究',
        topic: '轻量级物联网设备安全认证机制研究',
        fields: ['物联网', '网络安全', '加密技术'],
        submitTime: '2025-01-11',
        status: '申报失败',
        statusType: 'failed'
      },
      {
        id: 6,
        number: 'SK20251032106',
        applicant: '赵教授',
        direction: '机器学习在医疗诊断中的应用',
        topic: '基于联邦学习的医疗影像诊断系统',
        fields: ['机器学习', '医疗诊断', '深度学习'],
        submitTime: '2025-01-10',
        status: '申报中',
        statusType: 'submitting'
      }
    ]
    
    // 应用搜索和分页
    let filteredData = mockData
    if (searchForm.keyword) {
      const keyword = searchForm.keyword.toLowerCase()
      filteredData = filteredData.filter(item => 
        item.number?.toLowerCase().includes(keyword) ||
        item.applicant?.toLowerCase().includes(keyword) ||
        item.direction?.toLowerCase().includes(keyword) ||
        item.topic?.toLowerCase().includes(keyword) ||
        item.fields?.some(field => field.toLowerCase().includes(keyword))
      )
    }
    if (searchForm.status) {
      filteredData = filteredData.filter(item => item.statusType === searchForm.status)
    }
    
    // 分页处理
    const start = (pagination.current - 1) * pagination.size
    const end = start + pagination.size
    declarations.value = filteredData.slice(start, end)
    pagination.total = filteredData.length
    
    logger.info('Declaration list data loaded successfully', { count: declarations.value.length })
  } catch (error) {
    logger.error('Declaration list data loading failed', error)
    ElMessage.error(t('declaration.loadError'))
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  logger.info('User performed search', { status: searchForm.status })
  pagination.current = 1
  loadDeclarations()
  ElMessage.info(t('declaration.searchComplete'))
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
  ElMessage.info(t('declaration.resetComplete'))
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

// 行点击
const handleRowClick = (row) => {
  logger.info('User clicked declaration row', { id: row.id, number: row.number })
  handleView(row.id)
}

// 下载处理
const handleDownload = async (command) => {
  const { id, format } = command
  const formatNames = {
    pdf: 'PDF',
    word: 'Word',
    markdown: 'Markdown'
  }
  
  try {
    logger.info('User started download', { id, format })
    ElMessage.info(t('declaration.downloading', { format: formatNames[format] }))
    
    // TODO: 后端接口开发完成后替换为实际接口调用
    // const response = await downloadDeclaration({ id, format })
    // const blob = new Blob([response.data], { type: response.headers['content-type'] })
    // const url = URL.createObjectURL(blob)
    // const link = document.createElement('a')
    // link.href = url
    // link.download = response.headers['content-disposition']?.split('filename=')[1] || `申报文档_${id}.${format === 'word' ? 'docx' : format}`
    // document.body.appendChild(link)
    // link.click()
    // document.body.removeChild(link)
    // URL.revokeObjectURL(url)
    
    // 临时模拟下载过程（后端开发完成后删除）
    await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟网络延迟
    
    ElMessage.success(t('declaration.downloadComplete', { format: formatNames[format] }))
    logger.info('Download completed successfully', { id, format })
  } catch (error) {
    logger.error('Download failed', error)
    ElMessage.error(t('declaration.downloadFailed'))
  }
}

// 预览处理
const handlePreview = async (id) => {
  try {
    logger.info('User started preview', { id })
    ElMessage.info(t('declaration.previewing'))
    
    // TODO: 后端接口开发完成后替换为实际接口调用
    // const response = await getDeclarationPreview({ id })
    // const previewContent = response.data.content
    
    // 临时模拟预览内容（后端开发完成后删除）
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    
    const previewContent = t('declaration.previewContent', { 
      id: declarations.value.find(d => d.id === id)?.number || `SB2024${String(id).padStart(3, '0')}`
    })
    
    ElMessageBox.alert(
      previewContent,
      t('declaration.previewTitle'),
      {
        confirmButtonText: t('common.close'),
        type: 'info',
        customClass: 'preview-dialog'
      }
    )
    
    logger.info('Preview displayed successfully', { id })
  } catch (error) {
    logger.error('Preview failed', error)
    ElMessage.error(t('declaration.previewFailed'))
  }
}

// 状态编辑处理
const handleStatusEdit = async (id, newStatus) => {
  const statusLabels = {
    submitting: t('declaration.statusSubmitting'),
    success: t('declaration.statusSuccess'),
    failed: t('declaration.statusFailed')
  }
  
  const declaration = declarations.value.find(d => d.id === id)
  if (!declaration) return
  
  const oldStatus = declaration.statusType
  const oldStatusLabel = declaration.status
  
  if (newStatus === oldStatus) return
  
  try {
    await ElMessageBox.confirm(
      t('declaration.confirmStatusChange', { status: statusLabels[newStatus] }),
      t('declaration.confirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
        customClass: 'modern-confirm-dialog',
        center: false,
        showClose: true,
        closeOnClickModal: false,
        closeOnPressEscape: true,
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true
            instance.confirmButtonText = t('declaration.statusChanging')
            // 模拟接口调用
            setTimeout(() => {
              done()
              setTimeout(() => {
                instance.confirmButtonLoading = false
              }, 300)
            }, 800)
          } else {
            done()
          }
        }
      }
    )
    
    logger.info('User confirmed status change', { id, oldStatus, newStatus })
    
    // TODO: 后端接口开发完成后替换为实际接口调用
    // await updateDeclarationStatus({ id, status: newStatus })
    
    // 临时模拟状态更新（后端开发完成后删除）
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    
    // 更新申报状态
    declaration.statusType = newStatus
    declaration.status = statusLabels[newStatus]
    
    ElMessage.success(t('declaration.statusChanged', { status: statusLabels[newStatus] }))
    logger.info('Status updated successfully', { id, newStatus })
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消
      ElMessage.info(t('common.cancelled'))
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
  box-sizing: border-box;
  
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
}

.topic-cell { 
  color: var(--text-1);
}

.fields-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  line-height: 1.4;
  
  .field-tag {
    margin-right: 4px !important;
    margin-bottom: 4px !important;
    display: inline-block;
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

.status-clickable {
  cursor: pointer;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.status-arrow {
  font-size: 12px;
  opacity: 0.6;
  transition: transform 0.2s ease;
}

.status-clickable:hover .status-arrow {
  opacity: 1;
  transform: translateY(1px);
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
:deep(.base-table) {
  width: 100%;
  display: flex;
  flex-direction: column;
  
  .base-table__table {
    width: 100% !important;
    min-width: 100%;
    flex: 1;
    overflow: auto;
  }
  
  .el-table {
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
    
    // 表体样式
    .el-table__body {
      td {
        padding: 12px 16px !important;
        font-size: 14px;
        color: var(--text);
        white-space: nowrap;
        overflow: visible !important;
        position: relative !important;
        
        .cell {
          padding: 0 !important;
          line-height: 1.6;
          white-space: nowrap;
          overflow: visible !important;
          position: relative !important;
        }
      }
      
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

