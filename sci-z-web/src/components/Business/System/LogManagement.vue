<!--
/**
 * @description 系统管理 - 日志管理业务组件
 * 依据原型图实现筛选、列表、分页、详情等功能
 */
-->
<template>
  <div class="log-management-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ t('system.logs.title') }}</h1>
        <p class="page-subtitle">{{ t('system.logs.subtitle') }}</p>
      </div>
      <div class="header-actions">
        <BaseButton type="primary" :loading="exporting" @click="handleExport">
          {{ t('system.logs.exportLogs') }}
        </BaseButton>
        <BaseButton type="danger" :loading="clearing" @click="handleClear">
          {{ t('system.logs.clearLogs') }}
        </BaseButton>
      </div>
    </div>

    <BaseCard>
      <div class="filter-section">
        <div class="filter-item">
          <span class="filter-label">{{ t('system.logs.logLevel') }}</span>
          <el-select
            v-model="searchForm.level"
            :placeholder="t('common.all')"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option :label="t('common.all')" value="" />
            <el-option
              v-for="option in levelOptions"
              :key="option.value"
              :label="t(`system.logs.levelLabel.${option.value.toLowerCase()}`)"
              :value="option.value"
            />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="filter-label">{{ t('system.logs.dateRange') }}</span>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="~"
            :start-placeholder="t('system.logs.startDate')"
            :end-placeholder="t('system.logs.endDate')"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 320px"
          />
        </div>

        <div class="filter-item">
          <span class="filter-label">{{ t('system.logs.user') }}</span>
          <el-input
            v-model="searchForm.user"
            :placeholder="t('system.logs.userPlaceholder')"
            clearable
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <BaseButton type="primary" :loading="loading" @click="handleSearch">
          {{ t('common.search') }}
        </BaseButton>
        <BaseButton @click="handleReset">
          {{ t('common.reset') }}
        </BaseButton>
      </div>

      <el-table
        :data="logs"
        v-loading="loading"
        stripe
        class="log-table"
        :empty-text="t('common.noData')"
      >
        <el-table-column
          prop="timestamp"
          :label="t('system.logs.time')"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="level"
          :label="t('system.logs.level')"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)">
              {{ row.level || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="user"
          :label="t('system.logs.user')"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="operation"
          :label="t('system.logs.operation')"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column
          prop="ip"
          :label="t('system.logs.ipAddress')"
          width="140"
          show-overflow-tooltip
        />
        <el-table-column
          prop="detail"
          :label="t('system.logs.detail')"
          min-width="260"
          show-overflow-tooltip
        />
        <el-table-column
          :label="t('common.actions')"
          width="140"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <BaseButton size="small" @click="handleViewDetail(row)">
              {{ t('system.logs.viewDetail') }}
            </BaseButton>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination
        :current="pagination.current"
        :size="pagination.size"
        :total="pagination.total"
        :disabled="loading"
        @change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </BaseCard>

    <el-dialog
      v-model="detailVisible"
      width="720px"
      :title="t('system.logs.logDetail')"
    >
      <div v-if="currentLog" class="log-detail">
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.time') }}</div>
          <div class="detail-value">{{ currentLog.timestamp || '-' }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.level') }}</div>
          <div class="detail-value">
            <el-tag :type="getLevelTagType(currentLog.level)">
              {{ currentLog.level || '-' }}
            </el-tag>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.user') }}</div>
          <div class="detail-value">{{ currentLog.user || '-' }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.department">
          <div class="detail-label">{{ t('system.logs.department') }}</div>
          <div class="detail-value">{{ currentLog.department }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.ipAddress') }}</div>
          <div class="detail-value">{{ currentLog.ip || '-' }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.userAgent">
          <div class="detail-label">{{ t('system.logs.userAgent') }}</div>
          <div class="detail-value">{{ currentLog.userAgent }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.operation') }}</div>
          <div class="detail-value">{{ currentLog.operation || '-' }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.detail') }}</div>
          <div class="detail-value">{{ currentLog.detail || '-' }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.requestParams">
          <div class="detail-label">{{ t('system.logs.requestParams') }}</div>
          <div class="detail-value code-block">{{ currentLog.requestParams }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.response">
          <div class="detail-label">{{ t('system.logs.response') }}</div>
          <div class="detail-value code-block">{{ currentLog.response }}</div>
        </div>
      </div>
      <template #footer>
        <BaseButton @click="detailVisible = false">
          {{ t('common.close') }}
        </BaseButton>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { BaseCard, BaseButton, BasePagination } from '@/components/Common'
import { getOperationLogs } from '@/api/System/system'
import { LOG_LEVEL_OPTIONS, LOG_LEVEL_TAG_CONFIG, PAGINATION } from '@/utils/constants'
import { formatDate } from '@/utils/date'
import { createLogger } from '@/utils/simpleLogger'

const { t } = useI18n()
const logger = createLogger('LogManagement')

const loading = ref(false)
const exporting = ref(false)
const clearing = ref(false)
const logs = ref([])
const detailVisible = ref(false)
const currentLog = ref(null)

const pagination = reactive({
  current: 1,
  size: PAGINATION.DEFAULT_PAGE_SIZE,
  total: 0
})

const searchForm = reactive({
  level: '',
  user: '',
  dateRange: []
})

const levelOptions = LOG_LEVEL_OPTIONS

const normalizedLogs = computed(() => logs.value || [])

const getLevelTagType = (level) => LOG_LEVEL_TAG_CONFIG[level]?.type || 'info'

const buildQueryParams = () => {
  const payload = {
    pageNo: pagination.current,
    pageSize: pagination.size
  }
  if (searchForm.level) {
    payload.level = searchForm.level
  }
  if (searchForm.user) {
    payload.user = searchForm.user.trim()
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    payload.startTime = searchForm.dateRange[0]
    payload.endTime = searchForm.dateRange[1]
  }
  return payload
}

const normalizeLogItem = (item, index) => {
  const stringify = (value) => {
    if (value === undefined || value === null) return ''
    if (typeof value === 'string') return value
    try {
      return JSON.stringify(value, null, 2)
    } catch (error) {
      return String(value)
    }
  }

  return {
    id: item.id || item.logId || index,
    level: item.level || item.logLevel || 'INFO',
    user: item.user || item.username || item.operator || '',
    department: item.department || item.departmentName || '',
    operation: item.operation || item.action || item.event || '',
    ip: item.ip || item.ipAddress || '',
    detail: item.detail || item.description || '',
    timestamp: formatDate(item.timestamp || item.operateTime || item.createdTime || item.createTime, 'YYYY-MM-DD HH:mm:ss'),
    userAgent: item.userAgent || '',
    requestParams: stringify(item.requestParams || item.requestBody),
    response: stringify(item.response || item.responseBody),
    raw: item
  }
}

const loadLogs = async () => {
  try {
    loading.value = true
    logger.info('正在加载操作日志', {
      page: pagination.current,
      size: pagination.size,
      search: searchForm
    })
    const response = await getOperationLogs(buildQueryParams())
    const payload = response?.data?.data || response?.data || {}
    const list = Array.isArray(payload.records) ? payload.records : payload.list || []
    logs.value = list.map(normalizeLogItem)
    pagination.total = payload.total || 0
    pagination.current = payload.current || payload.pageNo || pagination.current
    pagination.size = payload.size || payload.pageSize || pagination.size
    logger.info('日志加载完成', { count: logs.value.length })
  } catch (error) {
    logger.error('操作日志加载失败', { error: error.message })
    ElMessage.error(t('system.logs.loadError'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadLogs()
}

const handleReset = () => {
  searchForm.level = ''
  searchForm.user = ''
  searchForm.dateRange = []
  pagination.current = 1
  loadLogs()
  ElMessage.success(t('common.resetSuccess'))
}

const handlePageChange = (page) => {
  pagination.current = page
  loadLogs()
}

const handlePageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadLogs()
}

const handleViewDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleExport = async () => {
  try {
    exporting.value = true
    const response = await getOperationLogs({
      ...buildQueryParams(),
      pageNo: 1,
      pageSize: Math.max(pagination.total, pagination.size)
    })
    const payload = response?.data?.data || response?.data || {}
    const list = Array.isArray(payload.records) ? payload.records : payload.list || []
    const exportData = list.map(normalizeLogItem)
    if (!exportData.length) {
      ElMessage.info(t('system.logs.exportEmpty'))
      return
    }
    const headers = [
      t('system.logs.time'),
      t('system.logs.level'),
      t('system.logs.user'),
      t('system.logs.operation'),
      t('system.logs.ipAddress'),
      t('system.logs.detail')
    ]
    const csvRows = exportData.map((item) =>
      [item.timestamp, item.level, item.user, item.operation, item.ip, item.detail]
        .map((value) => `"${(value || '').replace(/"/g, '""')}"`)
        .join(',')
    )
    const csvContent = [headers.join(','), ...csvRows].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `operation-logs-${formatDate(new Date(), 'YYYYMMDDHHmmss')}.csv`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success(t('system.logs.exportSuccess'))
  } catch (error) {
    logger.error('导出日志失败', { error: error.message })
    ElMessage.error(t('system.logs.exportFailed'))
  } finally {
    exporting.value = false
  }
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm(
      t('system.logs.clearConfirm'),
      t('system.logs.clearLogs'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    clearing.value = true
    // TODO: 接入后端清空日志接口
    await new Promise((resolve) => setTimeout(resolve, 600))
    ElMessage.info(t('system.logs.clearPending'))
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('清空日志操作失败', { error: error.message })
      ElMessage.error(t('system.logs.clearFailed'))
    }
  } finally {
    clearing.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style lang="scss" scoped>
.log-management-container {
  display: flex;
  flex-direction: column;
  gap: 20px;

  .page-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;

    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: var(--text-1);
    }

    .page-subtitle {
      margin: 4px 0 0;
      font-size: 14px;
      color: var(--text-3);
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .filter-section {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 16px;
    align-items: center;

    .filter-item {
      display: flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;

      .filter-label {
        font-size: 14px;
        color: var(--text-2);
      }
    }
  }

  .log-table {
    margin-top: 12px;
  }
}

.log-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .detail-row {
    display: flex;
    gap: 12px;
    align-items: flex-start;
    border-bottom: 1px solid var(--border);
    padding-bottom: 10px;
    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }
  }

  .detail-label {
    width: 120px;
    font-weight: 600;
    color: var(--text-2);
  }

  .detail-value {
    flex: 1;
    color: var(--text-1);
    word-break: break-all;
  }

  .code-block {
    background: var(--hover);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 10px;
    font-family: 'JetBrains Mono', Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
  }
}

@media (max-width: 768px) {
  .log-management-container {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
    }

    .filter-section {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>

