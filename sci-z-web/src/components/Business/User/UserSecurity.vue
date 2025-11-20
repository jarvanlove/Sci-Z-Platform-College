<!--
/**
 * @description 用户中心 - 安全设置业务组件
 * 负责密码修改与登录日志展示
 */
-->
<template>
  <div class="security-container">
    <div class="page-header">
      <h1 class="page-title">{{ t('user.security.title') }}</h1>
    </div>

    <div class="security-grid">
      <!-- 修改密码 -->
      <BaseCard class="security-card">
        <template #header>
          <div class="card-title">{{ t('user.security.changePassword') }}</div>
        </template>

        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="120px"
          class="password-form"
          :disabled="changing"
        >
          <el-form-item :label="t('user.security.oldPassword')" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              :placeholder="t('user.security.oldPasswordPlaceholder')"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item :label="t('user.security.newPassword')" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              :placeholder="t('user.security.newPasswordPlaceholder')"
              show-password
              clearable
              @input="checkPasswordStrength"
            />
            <div v-if="passwordForm.newPassword" class="strength-container">
              <div class="strength-header">
                <span>{{ t('user.security.passwordStrengthLabel') }}</span>
                <span :class="passwordStrengthLevel">
                  {{ passwordStrengthText }}
                </span>
              </div>
              <div class="strength-bar">
                <div class="strength-fill" :class="passwordStrengthLevel" />
              </div>
              <div class="strength-tips">
                {{ passwordStrengthTips }}
              </div>
            </div>
          </el-form-item>

          <el-form-item :label="t('user.security.confirmPassword')" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              :placeholder="t('user.security.confirmPasswordPlaceholder')"
              show-password
              clearable
            />
          </el-form-item>
        </el-form>

        <div class="form-actions">
          <BaseButton
            type="primary"
            size="large"
            :loading="changing"
            @click="handleChangePassword"
          >
            {{ t('user.security.submit') }}
          </BaseButton>
        </div>
      </BaseCard>

      <!-- 登录日志 -->
      <BaseCard class="security-card">
        <template #header>
          <div class="card-title">{{ t('user.security.loginLogsTitle') }}</div>
        </template>

        <div class="log-filters">
          <el-select
            v-model="logFilters.status"
            :placeholder="t('user.security.selectStatus')"
            style="width: 160px"
            @change="handleFilterChange"
          >
            <el-option :label="t('common.all')" value="all" />
            <el-option :label="t('user.security.loginSuccess')" value="success" />
            <el-option :label="t('user.security.loginFailed')" value="failed" />
            <el-option :label="t('user.security.abnormalLogin')" value="warning" />
          </el-select>

          <BaseDatePicker
            v-model="logFilters.dateRange"
            type="daterange"
            unlink-panels
            :range-separator="t('common.to')"
            :start-placeholder="t('common.startDate')"
            :end-placeholder="t('common.endDate')"
            style="width: 320px"
            @change="handleFilterChange"
          />
        </div>

        <BaseTable
          :data="loginLogs"
          :columns="tableColumns"
          :loading="logsLoading"
          :pagination="pagination"
          :empty-text="t('common.noData')"
          stripe
          class="login-logs-table"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        >
          <!-- 登录时间列自定义 -->
          <template #loginTime="{ row }">
            {{ formatDisplayTime(row.loginTime) }}
          </template>

          <!-- 登录IP列自定义 -->
          <template #loginIp="{ row }">
            {{ row.loginIp || row.ip }}
          </template>

          <!-- 登录地点列自定义 -->
          <template #loginLocation="{ row }">
            {{ row.loginLocation || row.location }}
          </template>

          <!-- 登录状态列自定义 -->
          <template #status="{ row }">
            <el-tag
              :type="statusTagType(row.status === 1 ? 'success' : row.status === 0 ? 'failed' : 'warning')"
              effect="light"
              size="small"
            >
              {{ statusText(row.status) || row.message }}
            </el-tag>
          </template>
        </BaseTable>
      </BaseCard>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BaseButton, BaseCard, BaseTable, BaseDatePicker } from '@/components/Common'
import { changePassword, getLoginLogs } from '@/api/User'
import { useAuthStore } from '@/store/modules/auth'
import { LOGIN_STATUS_TAG_CONFIG } from '@/utils/constants'
import { validatePasswordLength } from '@/utils/validate'
import { formatDate } from '@/utils/date'
import { createLogger } from '@/utils/simpleLogger'

const { t } = useI18n()
const authStore = useAuthStore()
const logger = createLogger('UserSecurityComponent')

logger.info('UserSecurity component setup initialized')

const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const changing = ref(false)

const passwordStrength = ref(0)

const passwordRules = {
  oldPassword: [
    { required: true, message: t('user.security.oldPasswordRequired'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('user.security.newPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('user.security.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(t('user.security.passwordMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loginLogs = ref([])
const logsLoading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const logFilters = reactive({
  status: 'all',
  dateRange: []
})

// 表格列配置
const tableColumns = computed(() => [
  {
    prop: 'loginTime',
    label: t('user.security.loginTime'),
    minWidth: 180
  },
  {
    prop: 'loginIp',
    label: t('user.security.loginIP'),
    minWidth: 140
  },
  {
    prop: 'loginLocation',
    label: t('user.security.loginLocation'),
    minWidth: 160,
    wrap: true // 🔥 允许换行，充分利用空间
  },
  {
    prop: 'browser',
    label: t('user.security.browser'),
    minWidth: 150,
    wrap: true // 🔥 允许换行，充分利用空间
  },
  {
    prop: 'os',
    label: t('user.security.operatingSystem'),
    minWidth: 140,
    wrap: true // 🔥 允许换行，充分利用空间
  },
  {
    prop: 'status',
    label: t('user.security.loginStatus'),
    minWidth: 120,
    align: 'center'
  }
])

const checkPasswordStrength = () => {
  const pwd = passwordForm.newPassword || ''
  logger.debug?.('计算密码强度', { length: pwd.length })
  if (!pwd) {
    passwordStrength.value = 0
    return
  }

  let score = 0
  if (pwd.length >= 8) score++
  if (pwd.length >= 12) score++
  if (/\d/.test(pwd)) score++
  if (/[a-z]/.test(pwd)) score++
  if (/[A-Z]/.test(pwd)) score++
  if (/[^a-zA-Z0-9]/.test(pwd)) score++

  if (score <= 2) {
    passwordStrength.value = 1
  } else if (score <= 4) {
    passwordStrength.value = 2
  } else {
    passwordStrength.value = 3
  }
}

const passwordStrengthLevel = computed(() => {
  const levels = ['', 'weak', 'medium', 'strong']
  return levels[passwordStrength.value]
})

const passwordStrengthText = computed(() => {
  const texts = ['', t('user.security.weak'), t('user.security.medium'), t('user.security.strong')]
  return texts[passwordStrength.value]
})

const passwordStrengthTips = computed(() => {
  switch (passwordStrength.value) {
    case 1:
      return t('user.security.weakTips')
    case 2:
      return t('user.security.mediumTips')
    case 3:
      return t('user.security.strongTips')
    default:
      return ''
  }
})

const handleChangePassword = async () => {
  logger.info('准备提交修改密码')
  try {
    await passwordFormRef.value.validate()
    await ElMessageBox.confirm(
      t('user.security.changePasswordConfirmMessage'),
      t('user.security.changePasswordConfirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    changing.value = true
    const response = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    // 后端响应格式：{ code: 200, message: "密码修改成功", data: null }
    const successMessage = response?.data?.message || t('user.security.changePasswordSuccess')
    ElMessage.success(successMessage)
    ElMessage.info(t('user.security.redirectToLogin'))
    await authStore.logout({ redirectPath: '/login', useReplace: true })
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('change password failed', { error: error.message })
      // 后端错误响应格式：{ code: 2003, message: "原密码错误", data: null }
      const errorMessage = error.response?.data?.message || t('user.security.changePasswordError')
      ElMessage.error(errorMessage)
    }
  } finally {
    changing.value = false
  }
}


const formatDisplayTime = (time) => {
  return formatDate(time, 'YYYY-MM-DD HH:mm:ss')
}

const statusText = (status) => {
  // 后端返回的是数字：1=成功，0=失败
  const statusStr = typeof status === 'number' 
    ? (status === 1 ? 'success' : status === 0 ? 'failed' : 'warning')
    : status
  switch (statusStr) {
    case 'success':
      return t('user.security.loginSuccess')
    case 'failed':
      return t('user.security.loginFailed')
    case 'warning':
      return t('user.security.abnormalLogin')
    default:
      return statusStr || ''
  }
}

const statusTagType = (status) => {
  return LOGIN_STATUS_TAG_CONFIG[status]?.type || 'info'
}

const buildLogParams = () => {
  const params = {
    pageNo: pagination.current,
    pageSize: pagination.size,
    sortBy: 'loginTime',
    sortOrder: 'DESC'
  }
  // 状态筛选：all -> null, success -> 1, failed -> 0, warning -> 0
  if (logFilters.status && logFilters.status !== 'all') {
    params.status = logFilters.status === 'success' ? 1 : 0
  }
  // 日期范围筛选
  if (Array.isArray(logFilters.dateRange) && logFilters.dateRange.length === 2) {
    params.startDate = formatDate(logFilters.dateRange[0], 'YYYY-MM-DD')
    params.endDate = formatDate(logFilters.dateRange[1], 'YYYY-MM-DD')
  }
  return params
}

const loadLoginLogs = async () => {
  logger.info('加载登录日志', buildLogParams())
  try {
    logsLoading.value = true
    const response = await getLoginLogs(buildLogParams())
    // 后端响应格式：{ code, message, data: { records, total, current, size, pages } }
    const payload = response?.data?.data || response?.data || {}
    const list = payload.records || []
    loginLogs.value = Array.isArray(list) ? list : []
    // 更新分页信息
    pagination.total = payload.total || 0
    pagination.current = payload.current || pagination.current
    pagination.size = payload.size || pagination.size
  } catch (error) {
    logger.error('load login logs failed', { error: error.message })
    ElMessage.error(error.response?.data?.message || t('user.security.loadLogsError'))
  } finally {
    logsLoading.value = false
  }
}

const handleFilterChange = () => {
  pagination.current = 1
  logger.info('筛选条件变化，重新加载日志', logFilters)
  loadLoginLogs()
}

const handlePageChange = (page) => {
  pagination.current = page
  logger.info('分页变化', { page })
  loadLoginLogs()
}

const handlePageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  logger.info('分页大小变化', { size })
  loadLoginLogs()
}

watch(
  () => authStore.token,
  (token) => {
    if (token) {
      logger.info('检测到 token，加载登录日志')
      loadLoginLogs()
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.security-container {
  padding: 24px;
  background: var(--bg);
  min-height: calc(100vh - 56px);
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--color-primary);
    margin: 0;
  }

  .page-subtitle {
    margin-top: 4px;
    color: var(--text-3);
    font-size: 14px;
  }
}

.security-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.security-card {
  background: var(--surface);
  border-radius: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-1);
}

.password-form {
  max-width: 540px;
}

.strength-container {
  margin-top: 12px;
}

.strength-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--text-3);

  .weak {
    color: #dc2626;
  }

  .medium {
    color: #f59e0b;
  }

  .strong {
    color: #16a34a;
  }
}

.strength-bar {
  position: relative;
  width: 100%;
  height: 6px;
  background: var(--border);
  border-radius: 4px;
  overflow: hidden;
  margin-top: 6px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 4px;

  &.weak {
    width: 33%;
    background: #dc2626;
  }

  &.medium {
    width: 66%;
    background: #f59e0b;
  }

  &.strong {
    width: 100%;
    background: #16a34a;
  }
}

.strength-tips {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-3);
}

.form-actions {
  margin-top: 32px;
  display: flex;
  justify-content: flex-start;
  gap: 12px;
  padding-left: 120px; /* 与表单 label-width 对齐 */
}

.log-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: var(--hover);
  border-radius: 8px;
}

.login-logs-table {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border);
}

@media (max-width: 768px) {
  .security-container {
    padding: 16px;
  }

  .log-filters {
    flex-direction: column;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

