<!--
/**
 * @description 系统管理 - 日志管理业务组件
 * 依据原型图实现筛选、列表、分页、详情等功能
 */
-->
<template>
  <div class="log-management-container">
    <div class="page-header">
      <h1 class="page-title">{{ t('system.logs.title') }}</h1>
    </div>

    <BaseCard class="content-card">
      <div class="filter-section">
        <!-- 🔥 用户名输入框移到最左边 -->
        <el-input
          v-model="searchForm.user"
          :placeholder="$t('system.logs.userPlaceholder')"
          clearable
          style="width: 200px"
          @blur="handleUserInputBlur"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

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
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <BaseDatePicker
          v-model="searchForm.dateRange"
          type="daterange"
          :unlink-panels="true"
          :start-placeholder="t('system.logs.startDate')"
          :end-placeholder="t('system.logs.endDate')"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 300px"
          @change="handleSearch"
        />

        <el-button type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ t('common.search') }}
        </el-button>
        <el-button type="primary" @click="handleReset">
          <el-icon><Refresh /></el-icon>
          {{ t('common.reset') }}
        </el-button>
      </div>

      <BaseTable
        :data="logs"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        :action-width="80"
        action-fixed="right"
        :empty-text="t('common.noData')"
        stripe
        class="log-table"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      >
        <!-- 日志级别列自定义 - 圆角处理 -->
        <template #level="{ row }">
          <el-tag :type="getLevelTagType(row.level)" class="level-tag">
            {{ row.level || '-' }}
          </el-tag>
        </template>

        <!-- 操作列 - 使用图标按钮 -->
        <template #actions="{ row }">
          <BaseTooltip :content="t('system.logs.viewDetail')" placement="top">
            <button
              class="action-btn btn-primary"
              @click.stop="handleViewDetail(row)"
            >
              <el-icon><TopRight /></el-icon>
            </button>
          </BaseTooltip>
        </template>
      </BaseTable>
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
            <el-tag :type="getLevelTagType(currentLog.level)" class="level-tag">
              {{ currentLog.level || '-' }}
            </el-tag>
          </div>
        </div>
        <div class="detail-row" v-if="currentLog.userId">
          <div class="detail-label">{{ t('system.logs.userId') }}</div>
          <div class="detail-value">{{ currentLog.userId }}</div>
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
          <div class="detail-label">{{ t('system.logs.operation') }}</div>
          <div class="detail-value">{{ currentLog.operation || '-' }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.method">
          <div class="detail-label">{{ t('system.logs.method') }}</div>
          <div class="detail-value">{{ currentLog.method }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.requestUrl">
          <div class="detail-label">{{ t('system.logs.requestUrl') }}</div>
          <div class="detail-value code-block">{{ currentLog.requestUrl }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.ipAddress') }}</div>
          <div class="detail-value">{{ currentLog.ip || '-' }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.location">
          <div class="detail-label">{{ t('system.logs.location') }}</div>
          <div class="detail-value">{{ currentLog.location }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.browser">
          <div class="detail-label">{{ t('system.logs.browser') }}</div>
          <div class="detail-value">{{ currentLog.browser }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.os">
          <div class="detail-label">{{ t('system.logs.os') }}</div>
          <div class="detail-value">{{ currentLog.os }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.userAgent">
          <div class="detail-label">{{ t('system.logs.userAgent') }}</div>
          <div class="detail-value code-block">{{ currentLog.userAgent }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.status !== '' && currentLog.status !== null && currentLog.status !== undefined">
          <div class="detail-label">{{ t('system.logs.status') }}</div>
          <div class="detail-value">{{ currentLog.status }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.executionTime !== '' && currentLog.executionTime !== null && currentLog.executionTime !== undefined">
          <div class="detail-label">{{ t('system.logs.executionTime') }}</div>
          <div class="detail-value">{{ currentLog.executionTime }}ms</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">{{ t('system.logs.detail') }}</div>
          <div class="detail-value">{{ currentLog.detail || '-' }}</div>
        </div>
        <div class="detail-row" v-if="currentLog.errorMessage">
          <div class="detail-label">{{ t('system.logs.errorMessage') }}</div>
          <div class="detail-value code-block error-message">{{ currentLog.errorMessage }}</div>
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
        <el-button @click="detailVisible = false">
          {{ t('common.close') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Search, Refresh, TopRight } from '@element-plus/icons-vue'
import { BaseCard, BaseTable, BaseDatePicker, BaseTooltip } from '@/components/Common'
import { getOperationLogs } from '@/api/System/system'
import { LOG_LEVEL_OPTIONS, LOG_LEVEL_TAG_CONFIG, PAGINATION } from '@/utils/constants'
import { formatDate } from '@/utils/date'
import { createLogger } from '@/utils/simpleLogger'

const { t } = useI18n()
const logger = createLogger('LogManagement')

const loading = ref(false)
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

// 表格列配置 - 所有列居中，优化列宽确保6个表头都能显示
const tableColumns = computed(() => [
  {
    prop: 'timestamp',
    label: t('system.logs.time'),
    minWidth: 160,
    align: 'center'
  },
  {
    prop: 'level',
    label: t('system.logs.level'),
    minWidth: 100,
    align: 'center'
  },
  {
    prop: 'user',
    label: t('system.logs.user'),
    minWidth: 100,
    align: 'center'
  },
  {
    prop: 'operation',
    label: t('system.logs.operation'),
    minWidth: 150,
    wrap: true, // 🔥 允许换行，充分利用空间
    align: 'center'
  },
  {
    prop: 'ip',
    label: t('system.logs.ipAddress'),
    minWidth: 140,
    align: 'center'
  }
])

const buildQueryParams = () => {
  const payload = {
    pageNo: pagination.current,
    pageSize: pagination.size
  }
  if (searchForm.level) {
    payload.level = searchForm.level
  }
  if (searchForm.user) {
    // 🔥 后端参数名是 username，不是 user
    payload.username = searchForm.user.trim()
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    // 🔥 后端需要 LocalDateTime 格式，Spring Boot 默认接受 yyyy-MM-dd HH:mm:ss 格式（带空格）
    // 开始时间为当天的 00:00:00，结束时间为当天的 23:59:59
    payload.startTime = `${searchForm.dateRange[0]} 00:00:00`
    payload.endTime = `${searchForm.dateRange[1]} 23:59:59`
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

  // 处理 logLevel 对象（可能包含 code 和 description）
  const logLevelObj = item.logLevel || {}
  const level = item.level || logLevelObj.code || 'INFO'

  return {
    id: item.id || item.logId || index,
    userId: item.userId || '',
    level: level,
    user: item.user || item.username || item.operator || '',
    department: item.department || item.departmentName || '',
    operation: item.operation || item.action || item.event || '',
    method: item.method || '',
    requestUrl: item.requestUrl || '',
    ip: item.ip || item.ipAddress || '',
    location: item.location || '',
    browser: item.browser || '',
    os: item.os || '',
    status: item.status !== undefined && item.status !== null ? item.status : '',
    executionTime: item.executionTime !== undefined && item.executionTime !== null ? item.executionTime : '',
    detail: item.detail || item.detailInfo || item.description || '',
    errorMessage: item.errorMessage || '',
    timestamp: formatDate(item.timestamp || item.operationTime || item.operateTime || item.createdTime || item.createTime, 'YYYY-MM-DD HH:mm:ss'),
    userAgent: item.userAgent || '',
    requestParams: stringify(item.requestParams || item.requestBody),
    response: stringify(item.response || item.responseResult || item.responseBody),
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

// 🔥 用户名输入框失去焦点时，如果有值则自动查询
const handleUserInputBlur = () => {
  if (searchForm.user && searchForm.user.trim()) {
    logger.info('User input blur with value, auto search', { user: searchForm.user })
    pagination.current = 1
    loadLogs()
  }
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



onMounted(() => {
  loadLogs()
})
</script>

<style lang="scss" scoped>
.log-management-container {
  padding: var(--gap-lg);
  background: var(--bg-secondary);
  min-height: calc(100vh - 56px);
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden; // 🔥 适配 TOC 布局：避免横向溢出
  display: flex;
  flex-direction: column;
  gap: 20px;

  .page-header {
    margin-bottom: var(--gap-lg);

    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: var(--color-primary); // 🔥 主题颜色
    }
  }

  .content-card {
    background: var(--surface);
    border-radius: 12px;
    padding: 24px;
    width: 100%;
    box-sizing: border-box;
    margin: 0;

    :deep(.base-card__content) {
      padding: 0;
      width: 100%;
    }
  }

  .filter-section {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 16px;
    align-items: center;
  }

  .log-table {
    margin-top: 12px;
    width: 100%;
    border-radius: 8px;
    overflow: visible;
    border: 1px solid var(--border);
    
    // 🔥 确保表格内容居中显示
    :deep(.el-table__cell) {
      text-align: center !important;
    }
    
    :deep(.el-table__header .el-table__cell) {
      text-align: center !important;
    }
    
    // 🔥 确保单元格内容居中
    :deep(.el-table__body-wrapper .el-table__body .el-table__row .el-table__cell .cell) {
      text-align: center !important;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    
    // 🔥 确保表头内容居中
    :deep(.el-table__header-wrapper .el-table__header .el-table__row .el-table__cell .cell) {
      text-align: center !important;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
  
  // 🔥 级别标签圆角处理
  .level-tag {
    border-radius: 12px !important;
    padding: 4px 8px !important;
    font-size: 12px !important;
    font-weight: 500 !important;
  }
  
  // 🔥 操作按钮样式 - 图标按钮
  .action-buttons {
    display: flex;
    flex-direction: row;
    gap: 8px;
    justify-content: center;
    align-items: center;
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
    
    .el-icon {
      font-size: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    &.btn-primary {
      color: var(--color-primary);
      border-color: var(--color-primary);
      
      &:hover:not(:disabled) {
        background: var(--color-primary);
        color: var(--surface);
      }
    }
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

  // 🔥 按照 @页面修改.md 修改表单标签样式
  .detail-label {
    width: 120px;
    font-size: 14px !important; // 🔥 表单标签字体大小
    color: var(--text-2) !important; // 🔥 表单标签字体颜色
    font-weight: 600 !important; // 🔥 表单标签字体粗细
  }

  // 🔥 按照 @页面修改.md 修改表单内容样式
  .detail-value {
    flex: 1;
    font-size: 14px !important; // 🔥 表单内容字体大小
    color: var(--text-3) !important; // 🔥 表单内容字体颜色
    font-weight: 400 !important; // 🔥 表单内容字体粗细（normal）
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
    word-break: break-all;
  }

  .error-message {
    color: var(--color-error);
    background: rgba(220, 38, 38, 0.1);
    border-color: var(--color-error);
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

