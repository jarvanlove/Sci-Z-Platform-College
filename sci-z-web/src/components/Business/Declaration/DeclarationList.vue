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
          :placeholder="$t('declaration.declarationStatusPlaceholder') || '请选择申报状态'"
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
        @row-click="handleRowClick"
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
            <el-dropdown
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
          </div>
        </template>

        <!-- 工作流状态列自定义 -->
        <template #workflowStatus="{ row }">
          <span
            class="workflow-status-tag"
            :class="`workflow-status-${row.workflowStatus}`"
          >
            {{ getWorkflowStatusLabel(row.workflowStatus) }}
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
            <div v-if="row.statusType === 'success'" class="action-row" @click.stop.prevent>
              <el-dropdown
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
                      :command="{id: row.id, format: 'word'}"
                    >
                      <el-icon><Edit /></el-icon>
                      Word格式
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{id: row.id, format: 'pdf'}"
                    >
                      <el-icon><Document /></el-icon>
                      PDF格式
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <button
                class="action-btn btn-info"
                @click.stop="handlePreview(row.id)"
              >
                {{ $t('declaration.preview') }}
              </button>
            </div>
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
import { Plus, Search, Refresh, Document, Edit } from '@element-plus/icons-vue'
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

// 工作流状态配置
const WORKFLOW_STATUS_CONFIG = {
  pending: { label: '待处理', color: '#f59e0b', bgColor: '#fef3c7' },
  running: { label: '处理中', color: '#3b82f6', bgColor: '#dbeafe' },
  completed: { label: '已完成', color: '#16a34a', bgColor: '#dcfce7' },
  failed: { label: '失败', color: '#dc2626', bgColor: '#fee2e2' }
}

// 获取工作流状态标签
const getWorkflowStatusLabel = (status) => {
  return WORKFLOW_STATUS_CONFIG[status]?.label || status || '-'
}

// 表格列配置 - 使用自适应策略
const tableColumns = computed(() => [
  {
    prop: 'number',
    label: t('declaration.number'),
    minWidth: 140, // 使用 minWidth 而非固定 width，允许自适应
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
        statusType: 'success',
        workflowStatus: 'completed'
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
        statusType: 'success',
        workflowStatus: 'running'
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
        statusType: 'failed',
        workflowStatus: 'failed'
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
        statusType: 'success',
        workflowStatus: 'pending'
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
        statusType: 'failed',
        workflowStatus: 'failed'
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
        statusType: 'submitting',
        workflowStatus: 'running'
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
    word: 'Word'
  }
  
  try {
    logger.info('User started download', { id, format })
    ElMessage.info(t('declaration.downloading', { format: formatNames[format] }) || `正在下载${formatNames[format]}格式文档...`)
    
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
    
    ElMessage.success(t('declaration.downloadComplete', { format: formatNames[format] }) || `${formatNames[format]}格式文档下载完成`)
    logger.info('Download completed successfully', { id, format })
  } catch (error) {
    logger.error('Download failed', error)
    ElMessage.error(t('declaration.downloadFailed') || '下载失败')
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
    // 🔥 参考原型图：选择非当前状态后，弹出确认对话框
    await ElMessageBox.confirm(
      t('declaration.confirmStatusChange', { status: statusLabels[newStatus] }) || 
      `确定要将申报状态修改为"${statusLabels[newStatus]}"吗？`,
      t('declaration.confirmTitle') || '确认修改',
      {
        confirmButtonText: t('common.confirm') || '确定',
        cancelButtonText: t('common.cancel') || '取消',
        type: 'warning',
        customClass: 'modern-confirm-dialog',
        center: false,
        showClose: true,
        closeOnClickModal: false,
        closeOnPressEscape: true,
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true
            instance.confirmButtonText = t('declaration.statusChanging') || '修改中...'
            // 🔥 真实场景时需要调用后端接口
            // TODO: 后端接口开发完成后替换为实际接口调用
            // updateDeclarationStatus({ id, status: newStatus })
            //   .then(() => {
            //     done()
            //     setTimeout(() => {
            //       instance.confirmButtonLoading = false
            //     }, 300)
            //   })
            //   .catch((error) => {
            //     instance.confirmButtonLoading = false
            //     ElMessage.error('状态更新失败')
            //     throw error
            //   })
            
            // 临时模拟接口调用（后端开发完成后删除）
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
    
    // 🔥 真实场景时需要调用后端接口
    // TODO: 后端接口开发完成后替换为实际接口调用
    // await updateDeclarationStatus({ id, status: newStatus })
    
    // 临时模拟状态更新（后端开发完成后删除）
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    
    // 更新申报状态
    declaration.statusType = newStatus
    declaration.status = statusLabels[newStatus]
    
    ElMessage.success(t('declaration.statusChanged', { status: statusLabels[newStatus] }) || 
      `申报状态已修改为"${statusLabels[newStatus]}"`)
    logger.info('Status updated successfully', { id, newStatus })
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消
      ElMessage.info(t('common.cancelled') || '已取消修改')
      logger.info('User cancelled status change', { id })
    } else {
      // 接口错误
      logger.error('Status update failed', error)
      ElMessage.error(t('declaration.statusUpdateFailed') || '状态更新失败')
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

