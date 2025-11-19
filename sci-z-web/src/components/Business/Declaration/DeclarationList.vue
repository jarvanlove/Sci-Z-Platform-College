<template>
  <div class="declaration-list-container">
    <div class="page-header">
      <div class="brand">
        <img :src="brandLogo" :alt="$t('app.title')" class="brand-logo" />
      </div>
      <div class="page-header-content">
        <h1 class="page-title">{{ $t('declaration.listTitle') }}</h1>
        <el-button type="primary" size="large" @click="handleNewDeclaration">
          <el-icon><Plus /></el-icon>
          {{ $t('declaration.newDeclaration') }}
        </el-button>
      </div>
    </div>

    <div class="search-card">
      <el-form :model="searchForm" class="search-form" label-width="120px">
        <el-form-item :label="$t('declaration.keywordSearch')">
          <el-input
            v-model="searchForm.keyword"
            :placeholder="$t('declaration.keywordPlaceholder')"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('declaration.statusFilter')">
          <el-select
            v-model="searchForm.status"
            :placeholder="$t('declaration.statusPlaceholder')"
            clearable
            class="status-select"
            @change="handleStatusChange"
          >
            <el-option
              v-for="option in statusFilterOptions"
              :key="option.value || 'all'"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            {{ $t('common.search') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            {{ $t('common.reset') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ $t('common.loading') }}</span>
      </div>

      <template v-else>
        <el-table
          :data="declarations"
          class="declaration-table"
          :header-cell-style="tableHeaderStyle"
          :row-style="{ cursor: 'pointer' }"
          @row-click="handleRowClick"
        >
          <el-table-column prop="number" :label="$t('declaration.number')" width="160" align="center" />
          <el-table-column prop="applicant" :label="$t('declaration.applicant')" width="120" align="center" />
          <el-table-column prop="submitTime" :label="$t('declaration.submitTime')" width="160" align="center" />
          <el-table-column prop="direction" :label="$t('declaration.direction')" min-width="200" show-overflow-tooltip />
          <el-table-column prop="fields" :label="$t('declaration.fields')" min-width="200">
            <template #default="{ row }">
              <div class="fields-cell">
                <el-tag
                  v-for="field in row.fields"
                  :key="field"
                  size="small"
                >
                  {{ field }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="topic" :label="$t('declaration.topic')" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.topic || $t('declaration.noTopic') }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('common.status')" width="160" align="center">
            <template #default="{ row }">
              <el-dropdown
                trigger="click"
                @command="(command) => handleStatusEdit(row, command)"
                class="status-dropdown"
              >
                <span class="status-tag status-clickable" :class="`status-${row.statusType}`">
                  {{ getStatusLabel(row.statusType) }}
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
                      <span class="status-tag" :class="`status-${option.value}`">
                        {{ option.label }}
                      </span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
          <el-table-column :label="$t('common.actions')" width="220" align="center" fixed="right">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-button type="primary" size="small" link @click.stop="handleView(row.id)">
                  {{ $t('common.view') }}
                </el-button>
                <el-dropdown
                  v-if="row.statusType === 'success'"
                  trigger="click"
                  @command="handleDownload"
                >
                  <el-button type="success" size="small" link>
                    {{ $t('declaration.download') }}
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ id: row.id, format: 'pdf' }">
                        <el-icon><Document /></el-icon>
                        PDF
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ id: row.id, format: 'word' }">
                        <el-icon><Document /></el-icon>
                        Word
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ id: row.id, format: 'markdown' }">
                        <el-icon><Document /></el-icon>
                        Markdown
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button
                  v-if="row.statusType === 'success'"
                  type="info"
                  size="small"
                  link
                  @click.stop="handlePreview(row.id)"
                >
                  {{ $t('declaration.preview') }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!declarations.length" class="empty-state">
          <div class="empty-state-icon">📝</div>
          <div class="empty-state-text">{{ $t('declaration.noData') }}</div>
          <div class="empty-state-hint">{{ $t('declaration.noDataHint') }}</div>
        </div>

        <div v-if="declarations.length" class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Loading, ArrowDown, Document } from '@element-plus/icons-vue'
import { createLogger } from '@/utils/simpleLogger'
import brandLogo from '@/assets/images/logo.svg'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('DeclarationList')

const loading = ref(false)
const declarations = ref([])

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const statusLabelMap = computed(() => ({
  submitting: t('declaration.statusSubmitting'),
  success: t('declaration.statusSuccess'),
  failed: t('declaration.statusFailed')
}))

const editableStatusOptions = computed(() =>
  ['submitting', 'success', 'failed'].map(value => ({
    value,
    label: statusLabelMap.value[value]
  }))
)

const statusFilterOptions = computed(() => [
  { label: t('common.all'), value: '' },
  ...editableStatusOptions.value
])

const tableHeaderStyle = {
  backgroundColor: '#f5f5f5',
  color: '#374151',
  fontWeight: 600
}

const mockDeclarations = [
  {
    id: 1,
    number: 'SB2024001',
    applicant: '张教授',
    direction: '人工智能在教育领域的应用研究',
    topic: '基于深度学习的个性化学习系统研究',
    fields: ['人工智能', '教育技术', '机器学习'],
    submitTime: '2024-01-15 13:25',
    statusType: 'submitting'
  },
  {
    id: 2,
    number: 'SB2024002',
    applicant: '李博士',
    direction: '量子计算算法研究',
    topic: '量子机器学习算法优化研究',
    fields: ['量子计算', '机器学习', '算法优化'],
    submitTime: '2024-01-16 09:12',
    statusType: 'success'
  },
  {
    id: 3,
    number: 'SB2024003',
    applicant: '王教授',
    direction: '生物信息学数据分析',
    topic: '基因组数据分析方法研究',
    fields: ['生物信息学', '数据分析', '基因组学'],
    submitTime: '2024-01-17 10:45',
    statusType: 'failed'
  }
]

const getStatusLabel = (statusType) => statusLabelMap.value[statusType] || statusType

const loadDeclarations = async () => {
  try {
    loading.value = true
    logger.info('Loading declaration list')

    await new Promise(resolve => setTimeout(resolve, 600))

    let filtered = [...mockDeclarations]

    if (searchForm.keyword) {
      const keyword = searchForm.keyword.toLowerCase()
      filtered = filtered.filter(item =>
        item.direction.toLowerCase().includes(keyword) ||
        item.topic.toLowerCase().includes(keyword) ||
        item.number.toLowerCase().includes(keyword)
      )
    }

    if (searchForm.status) {
      filtered = filtered.filter(item => item.statusType === searchForm.status)
    }

    pagination.total = filtered.length
    const start = (pagination.current - 1) * pagination.size
    const end = start + pagination.size
    declarations.value = filtered.slice(start, end)

    logger.info('Declaration list loaded', { count: declarations.value.length })
  } catch (error) {
    logger.error('Declaration list loading failed', error)
    ElMessage.error(t('declaration.loadError'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadDeclarations()
  ElMessage.success(t('declaration.searchComplete'))
}

const handleStatusChange = () => {
  pagination.current = 1
  loadDeclarations()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.current = 1
  loadDeclarations()
  ElMessage.info(t('declaration.resetComplete'))
}

const handleNewDeclaration = () => {
  router.push('/declaration/create')
}

const handleView = (id) => {
  router.push(`/declaration/detail/${id}`)
}

const handleRowClick = (row) => {
  handleView(row.id)
}

const handleDownload = async (command) => {
  const { id, format } = command
  const formatNames = {
    pdf: 'PDF',
    word: 'Word',
    markdown: 'Markdown'
  }

  try {
    ElMessage.info(t('declaration.downloading', { id, format: formatNames[format] }))
    await new Promise(resolve => setTimeout(resolve, 800))

    const blob = new Blob(
      [`Declaration Content - ID: ${id}, Format: ${formatNames[format]}`],
      { type: 'text/plain' }
    )
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `declaration_${id}.${format === 'word' ? 'docx' : format}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    ElMessage.success(t('declaration.downloadComplete', { format: formatNames[format] }))
  } catch (error) {
    logger.error('Download failed', error)
    ElMessage.error(t('declaration.downloadFailed'))
  }
}

const handlePreview = async (id) => {
  try {
    ElMessage.info(t('declaration.previewing', { id }))
    await new Promise(resolve => setTimeout(resolve, 400))
    await ElMessageBox.alert(
      t('declaration.previewContent', { id: `SB${String(id).padStart(6, '0')}` }),
      t('declaration.previewTitle'),
      {
        confirmButtonText: t('common.close'),
        customClass: 'preview-dialog'
      }
    )
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Preview failed', error)
      ElMessage.error(t('declaration.previewFailed'))
    }
  }
}

const handleStatusEdit = async (row, newStatus) => {
  if (row.statusType === newStatus) return

  try {
    await ElMessageBox.confirm(
      t('declaration.confirmStatusChange', { status: getStatusLabel(newStatus) }),
      t('declaration.confirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
        customClass: 'modern-confirm-dialog'
      }
    )

    await new Promise(resolve => setTimeout(resolve, 500))
    row.statusType = newStatus

    ElMessage.success(t('declaration.statusChanged', { status: getStatusLabel(newStatus) }))
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info(t('common.cancelled'))
    } else {
      logger.error('Status update failed', error)
      ElMessage.error(t('declaration.statusUpdateFailed'))
    }
  }
}

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

<style scoped lang="scss">
.declaration-list-container {
  padding: 20px;
  background: #f7f9fc;
  min-height: calc(100vh - 56px);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.brand {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.brand-logo {
  width: 46px;
  height: 46px;
}

.page-header-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e3a8a;
  margin: 0;
}

.search-card,
.table-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;

  .search-input {
    width: 320px;
  }

  .status-select {
    width: 200px;
  }
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #6b7280;
}

:deep(.declaration-table) {
  .el-table__header th {
    background-color: #f5f5f5 !important;
    color: #374151 !important;
    font-weight: 600 !important;
  }

  .el-table__body tr {
    transition: background 0.2s ease;

    &:hover {
      background: #f3f4f6 !important;
    }
  }

  .fields-cell {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.status-clickable {
  cursor: pointer;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.12);
  }
}

.status-submitting {
  background: #fef3c7;
  color: #f59e0b;
}

.status-success {
  background: #dcfce7;
  color: #16a34a;
}

.status-failed {
  background: #fee2e2;
  color: #dc2626;
}

.status-arrow {
  font-size: 12px;
}

:deep(.status-dropdown-menu) {
  min-width: 140px;
  padding: 8px 0;

  .el-dropdown-menu__item {
    padding: 6px 16px;
  }
}

.operation-buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
}

.empty-state {
  text-align: center;
  padding: 48px 16px;
  color: #6b7280;
}

.empty-state-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-state-text {
  font-size: 16px;
  font-weight: 600;
}

.empty-state-hint {
  font-size: 14px;
  color: #9ca3af;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.preview-dialog {
  width: 600px;
}

@media (max-width: 768px) {
  .page-header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-form {
    flex-direction: column;
    align-items: stretch;

    .search-input,
    .status-select {
      width: 100%;
    }
  }
}
</style>
