<!--
/**
 * @description 系统管理 - 用户管理业务组件
 * 负责用户列表展示、搜索筛选、新增/编辑/删除用户等功能
 */
-->
<template>
  <div class="user-management-container">
    <div class="page-header">
      <h1 class="page-title">{{ t('system.user.title') }}</h1>
    </div>

    <BaseCard class="content-card">
      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="t('system.user.searchPlaceholder')"
          clearable
          style="width: 250px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 角色筛选暂时下线，统一到角色管理绑定，保留结构以便后续恢复 -->
        <!--
        <el-select
          v-model="searchForm.role"
          :placeholder="t('system.user.selectRole')"
          clearable
          style="width: 150px"
          @visible-change="handleRoleFilterVisible"
        >
          <el-option :label="t('common.all')" value="" />
          <el-option
            v-for="role in roleOptions"
            :key="role.id"
            :label="role.roleName || role.name"
            :value="role.id"
          />
        </el-select>
        -->
        
        <el-select
          v-model="searchForm.status"
          :placeholder="t('system.user.selectStatus')"
          clearable
          style="width: 120px"
        >
          <el-option :label="t('common.all')" value="" />
          <el-option :label="t('common.active')" value="active" />
          <el-option :label="t('common.inactive')" value="inactive" />
        </el-select>
        
        <BaseButton type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ t('common.search') }}
        </BaseButton>
        <BaseButton @click="handleReset">
          <el-icon><Refresh /></el-icon>
          {{ t('common.reset') }}
        </BaseButton>
        <BaseButton type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          {{ t('system.user.addUser') }}
        </BaseButton>
      </div>

      <!-- 用户列表表格 -->
      <BaseTable
        :data="userList"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        :action-width="280"
        action-fixed="right"
        :empty-text="t('common.noData')"
        stripe
        class="user-table"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      >
        <!-- 角色列自定义 -->
        <template #role="{ row }">
          <span class="role-tag" :class="getRoleClass(row.role || row.roleName)">
            {{ getRoleText(row.role || row.roleName) }}
          </span>
        </template>

        <!-- 状态列自定义 -->
        <template #status="{ row }">
          <span class="status-tag" :class="getStatusClass(row.status)">
            {{ getStatusText(row.status) }}
          </span>
        </template>

        <!-- 创建时间列自定义 -->
        <template #createTime="{ row }">
          {{ formatDisplayTime(row.createTime) }}
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <div class="action-buttons">
            <button
              class="action-btn btn-primary"
              @click="handleEdit(row)"
            >
              {{ t('common.edit') }}
            </button>
            <button
              class="action-btn btn-warning"
              @click="handleResetPassword(row)"
            >
              {{ t('system.user.resetPassword') }}
            </button>
            <button
              class="action-btn"
              :class="(row.status === 1 || row.status === 'active') ? 'btn-info' : 'btn-success'"
              @click="handleToggleStatus(row)"
            >
              {{ (row.status === 1 || row.status === 'active') ? t('common.disable') : t('common.enable') }}
            </button>
            <button
              class="action-btn btn-danger"
              @click="handleDelete(row)"
            >
              {{ t('common.delete') }}
            </button>
          </div>
        </template>
      </BaseTable>
    </BaseCard>

    <!-- 新增/编辑用户对话框 -->
    <BaseDialog
      v-model="showDialog"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item :label="t('system.user.username')" prop="username">
          <el-input
            v-model="formData.username"
            :disabled="isEdit"
            :placeholder="t('system.user.usernamePlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('system.user.realName')" prop="realName">
          <el-input
            v-model="formData.realName"
            :placeholder="t('system.user.realNamePlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('system.user.email')" prop="email">
          <el-input
            v-model="formData.email"
            :placeholder="t('system.user.emailPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('system.user.phone')" prop="phone">
          <el-input
            v-model="formData.phone"
            :placeholder="t('system.user.phonePlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="departmentFieldLabel" prop="departmentId">
          <el-select
            v-model="formData.departmentId"
            :placeholder="departmentFieldPlaceholder"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <!-- 角色暂不在此维护，后续由角色管理绑定 -->
        <!--
        <el-form-item :label="t('system.user.role')" prop="roleId">
          <el-select
            v-model="formData.roleId"
            :placeholder="t('system.user.selectRole')"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName || role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        -->
        <el-form-item :label="t('common.status')" prop="status">
          <BaseSwitch
            v-model="formData.status"
            active-value="active"
            inactive-value="inactive"
          />
        </el-form-item>
        <el-form-item
          v-if="!isEdit"
          :label="t('system.user.password')"
          prop="password"
        >
          <el-input
            v-model="formData.password"
            type="password"
            show-password
            :placeholder="t('system.user.passwordPlaceholder')"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <BaseButton @click="showDialog = false">
          {{ t('common.cancel') }}
        </BaseButton>
        <BaseButton type="primary" @click="handleSubmit" :loading="submitting">
          {{ t('common.confirm') }}
        </BaseButton>
      </template>
    </BaseDialog>

    <!-- 重置密码对话框 -->
    <BaseDialog
      v-model="showResetPasswordDialog"
      :title="t('system.user.resetPassword')"
      width="500px"
      :show-close="false"
      @close="handleResetPasswordDialogClose"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
      >
        <el-form-item prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            show-password
            :placeholder="t('system.user.newPasswordPlaceholder')"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <BaseButton @click="showResetPasswordDialog = false">
          {{ t('common.cancel') }}
        </BaseButton>
        <BaseButton type="primary" @click="handleConfirmResetPassword" :loading="resettingPassword">
          {{ t('common.confirm') }}
        </BaseButton>
      </template>
    </BaseDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { BaseButton, BaseCard, BaseTable, BaseDialog, BaseSwitch } from '@/components/Common'
import { getUsers, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/System/system'
import { validateEmail, validatePhone, validateUsername, validateChineseName } from '@/utils/validate'
import { formatDate } from '@/utils/date'
import { createLogger } from '@/utils/simpleLogger'
import { useIndustryStore } from '@/store/modules/industry'

const { t } = useI18n()
const logger = createLogger('UserManagement')
const industryStore = useIndustryStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const formRef = ref()
const showResetPasswordDialog = ref(false)
const resetPasswordFormRef = ref()
const resettingPassword = ref(false)
const currentResetUser = ref(null)
const resetPasswordForm = reactive({
  newPassword: ''
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  departmentId: null,
  industryType: '',
  status: 'active',
  password: ''
})

// 用户列表
const userList = ref([])

const departmentOptions = ref([])
const departmentsLoaded = ref(false)

const departmentFieldLabel = computed(() => {
  const key = industryStore.departmentLabelKey || 'system.user.department'
  return t(key)
})

const departmentFieldPlaceholder = computed(() => {
  const key = industryStore.departmentPlaceholderKey || 'system.user.departmentPlaceholder'
  return t(key)
})

const getSelectedDepartmentName = () => {
  const target = departmentOptions.value.find((dept) => dept.id === formData.departmentId)
  return target?.name || ''
}

// 表格列配置
const tableColumns = computed(() => [
  {
    prop: 'username',
    label: t('system.user.username'),
    width: 120,
    showOverflowTooltip: true
  },
  {
    prop: 'realName',
    label: t('system.user.realName'),
    width: 150,
    showOverflowTooltip: true
  },
  {
    prop: 'employeeId',
    label: industryStore.employeeIdLabel || t('system.user.employeeId'),
    width: 250,
    showOverflowTooltip: true
  },
  {
    prop: 'email',
    label: t('system.user.email'),
    width: 200,
    showOverflowTooltip: true
  },
  {
    prop: 'departmentName',
    label: t(industryStore.departmentLabelKey || 'system.user.department'),
    width: 160,
    showOverflowTooltip: true
  },
  {
    prop: 'role',
    label: industryStore.roleLabel || t('system.user.role'),
    width: 120,
    align: 'center'
  },
  {
    prop: 'status',
    label: t('common.status'),
    width: 100,
    align: 'center'
  },
  {
    prop: 'createTime',
    label: t('common.createTime'),
    width: 200,
    showOverflowTooltip: true
  }
])

const ensureDepartmentOptions = async (force = false) => {
  if (!departmentsLoaded.value || force) {
    await loadDepartments(force)
  }
}

const ensureFormOptions = async (force = false) => {
  await ensureDepartmentOptions(force)
}

// 重置密码表单验证规则
const resetPasswordRules = computed(() => ({
  newPassword: [
    { required: true, message: t('system.user.newPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('system.user.newPasswordLength'), trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/, 
      message: '密码必须包含大小写字母和数字', 
      trigger: 'blur' 
    }
  ]
}))

// 表单验证规则
const formRules = computed(() => ({
  username: [
    { required: true, message: t('system.user.usernameRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validateUsername(value)) {
          callback(new Error(t('system.user.usernameFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: t('system.user.realNameRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && !validateChineseName(value)) {
          callback(new Error(t('system.user.realNameFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: t('system.user.emailRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validateEmail(value)) {
          callback(new Error(t('system.user.emailFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: t('system.user.phoneRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && !validatePhone(value)) {
          callback(new Error(t('system.user.phoneFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  departmentId: [
    { required: true, message: t('system.user.departmentRequired'), trigger: 'change' }
  ],
  password: [
    {
      required: !isEdit.value,
      message: t('system.user.passwordRequired'),
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        if (!isEdit.value && value) {
          if (value.length < 6 || value.length > 20) {
            callback(new Error(t('system.user.passwordLength')))
          } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/.test(value)) {
            callback(new Error(t('system.user.passwordFormat')))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}))

// 计算属性
const dialogTitle = computed(() =>
  isEdit.value ? t('system.user.editUser') : t('system.user.addUser')
)

// 方法
const getRoleClass = (role) => {
  // 支持后端返回的roleName字段（字符串）
  if (typeof role === 'string') {
    if (role === 'admin' || role === '管理员' || role?.includes('管理员')) {
      return 'role-admin'
    }
    if (role === 'teacher' || role === '教师' || role?.includes('教师')) {
      return 'role-teacher'
    }
    if (role === 'student' || role === '学生' || role?.includes('学生')) {
      return 'role-student'
    }
  }
  return 'role-default'
}

const getRoleText = (role) => {
  // 支持后端返回的roleName字段（字符串）
  if (typeof role === 'string' && (role === 'admin' || role === 'teacher' || role === 'student')) {
    const textMap = {
      admin: t('system.user.roleAdmin'),
      teacher: t('system.user.roleTeacher'),
      student: t('system.user.roleStudent')
    }
    return textMap[role] || role
  }
  // 如果role是roleName（如"管理员"、"教师"、"学生"），直接返回
  return role || ''
}

const getStatusClass = (status) => {
  const isActive = status === 1 || status === 'active'
  return isActive ? 'status-normal' : 'status-disabled'
}

const getStatusText = (status) => {
  const isActive = status === 1 || status === 'active'
  return isActive ? t('common.active') : t('common.inactive')
}

const formatDisplayTime = (time) => {
  return formatDate(time, 'YYYY-MM-DD HH:mm:ss')
}

const findDepartmentOption = (value) => {
  if (value === undefined || value === null || value === '') return null
  const valueStr = String(value)
  return departmentOptions.value.find((option) => {
    if (!option) return false
    const candidates = [
      option.id,
      option.numericId,
      option.code,
      option.name,
      typeof option.id === 'number' ? String(option.id) : undefined,
      typeof option.numericId === 'number' ? String(option.numericId) : undefined,
      option.code ? String(option.code) : undefined,
      option.name ? String(option.name) : undefined
    ].filter((item) => item !== undefined && item !== null)
    return candidates.some((candidate) => String(candidate) === valueStr)
  })
}

const syncDepartmentValue = (value = formData.departmentId) => {
  if (value === undefined || value === null || value === '') return
  const match = findDepartmentOption(value)
  if (match) {
    formData.departmentId = match.id
  }
}

const loadDepartments = async (force = false) => {
  if (departmentsLoaded.value && !force) return
  try {
    const list = await industryStore.fetchDepartmentLabels()
    if (Array.isArray(list)) {
      departmentOptions.value = list.map((item) => {
        const numericId = item.id ?? item.departmentId ?? null
        const code =
          item.code ||
          item.value ||
          (typeof numericId === 'number' ? String(numericId) : '') ||
          item.label ||
          ''
        return {
          id: numericId ?? code,
          numericId,
          code,
          name: item.label || item.name || item.title || item.value
        }
      })
    } else {
      departmentOptions.value = []
    }
    departmentsLoaded.value = true
    logger.info('Department labels loaded', { count: departmentOptions.value.length })
    syncDepartmentValue()
  } catch (error) {
    departmentOptions.value = []
    logger.error('Failed to load department labels', error)
    throw error
  }
}

const resolveDepartmentPayload = () => {
  const selected = findDepartmentOption(formData.departmentId)
  const departmentId =
    typeof selected?.numericId === 'number'
      ? selected.numericId
      : typeof formData.departmentId === 'number'
        ? formData.departmentId
        : undefined
  const departmentCode =
    selected?.code ||
    (typeof formData.departmentId === 'string' ? formData.departmentId : undefined)
  return {
    departmentId,
    departmentCode
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    loading.value = true
    logger.info('Loading users list', {
      page: pagination.current,
      size: pagination.size,
      filters: searchForm
    })

    const requestData = {
      pageNo: pagination.current,
      pageSize: pagination.size,
      sortBy: 'createdTime',
      sortOrder: 'DESC'
    }

    // 只有用户选择时才传这些参数
    if (searchForm.keyword) {
      requestData.keyword = searchForm.keyword
    }
    if (searchForm.status) {
      // 将 active/inactive 转换为 1/0
      // 后端返回：1=正常，0=禁用
      requestData.status = searchForm.status === 'active' ? 1 : 0
    }

    const response = await getUsers(requestData)
    const data = response?.data?.data || response?.data || {}
    userList.value = data.records || data.list || []
    pagination.total = data.total || 0
    pagination.current = data.current || data.pageNo || pagination.current
    pagination.size = data.size || data.pageSize || pagination.size

    logger.info('Users list loaded successfully', { count: userList.value.length })
  } catch (error) {
    logger.error('Failed to load users list', error)
    ElMessage.error(error.response?.data?.message || t('system.user.loadError'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadUsers()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.current = 1
  loadUsers()
  ElMessage.success(t('common.resetSuccess'))
}

const handleAdd = async () => {
  try {
    await ensureFormOptions(!departmentsLoaded.value)
  } catch (error) {
    logger.error('Failed to prepare form options', error)
    ElMessage.error(t('common.operationFailed'))
    return
  }
  isEdit.value = false
  resetForm()
  showDialog.value = true
}

const handleEdit = async (user) => {
  try {
    await ensureFormOptions(!departmentsLoaded.value)
  } catch (error) {
    logger.error('Failed to prepare form options', error)
    ElMessage.error(t('common.operationFailed'))
    return
  }
  isEdit.value = true
  Object.assign(formData, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    email: user.email,
    phone: user.phone,
    departmentId: user.departmentId || user.department?.id || null,
    industryType: user.industryType || industryStore.industryCode || '',
    status: user.status === 1 || user.status === 'active' ? 'active' : 'inactive',
    password: ''
  })
  const departmentChain = [
    user.departmentCode,
    user.department?.code,
    user.departmentId,
    user.department?.id,
    user.departmentName,
    user.department?.name
  ].filter((item) => item !== undefined && item !== null && item !== '')

  for (const candidate of departmentChain) {
    syncDepartmentValue(candidate)
    if (findDepartmentOption(candidate)) break
  }

  if (!findDepartmentOption(formData.departmentId)) {
    const fallbackName =
      user.departmentName || user.department?.name || user.departmentCode || ''
    if (fallbackName) {
      const fallbackOption = {
        id: fallbackName,
        numericId: typeof user.departmentId === 'number' ? user.departmentId : undefined,
        code: user.departmentCode || fallbackName,
        name: fallbackName
      }
      departmentOptions.value = [fallbackOption, ...departmentOptions.value]
      formData.departmentId = fallbackOption.id
    }
  }
  showDialog.value = true
}

const handleResetPassword = (user) => {
  currentResetUser.value = user
  resetPasswordForm.newPassword = ''
  showResetPasswordDialog.value = true
  nextTick(() => {
    resetPasswordFormRef.value?.clearValidate()
  })
}

const handleConfirmResetPassword = async () => {
  if (!resetPasswordFormRef.value) return
  
  try {
    await resetPasswordFormRef.value.validate()
    
    if (!currentResetUser.value) return
    
    resettingPassword.value = true
    logger.info('Reset user password', { 
      userId: currentResetUser.value.id, 
      username: currentResetUser.value.username 
    })

    // 调用新的API：PUT /api/user/users/password/reset
    await resetUserPassword({
      userId: currentResetUser.value.id,
      newPassword: resetPasswordForm.newPassword.trim()
    })

    ElMessage.success(t('system.user.resetPasswordSuccess'))
    logger.info('Password reset successful', { userId: currentResetUser.value.id })
    
    showResetPasswordDialog.value = false
    resetPasswordForm.newPassword = ''
    currentResetUser.value = null
  } catch (error) {
    if (error !== false) { // 表单验证失败会返回false
      logger.error('Failed to reset password', error)
      ElMessage.error(error.response?.data?.message || t('system.user.resetPasswordFailed'))
    }
  } finally {
    resettingPassword.value = false
  }
}

const handleResetPasswordDialogClose = () => {
  resetPasswordForm.newPassword = ''
  currentResetUser.value = null
  resetPasswordFormRef.value?.clearValidate()
}

const handleToggleStatus = async (user) => {
  const isActive = user.status === 1 || user.status === 'active'
  const action = isActive ? t('common.disable') : t('common.enable')
  try {
    await ElMessageBox.confirm(
      t('system.user.toggleStatusConfirm', { action, name: user.realName || user.username }),
      t('system.user.toggleStatus'),
      { type: 'warning' }
    )

    logger.info('Toggle user status', {
      userId: user.id,
      username: user.username,
      action
    })

    // 使用新的API：PUT /api/user/users/{id}/status?disabled=true/false
    const disabled = isActive // status=1 -> disabled=true, status=0 -> disabled=false
    await updateUserStatus(user.id, disabled)

    ElMessage.success(t('system.user.toggleStatusSuccess', { action }))
    logger.info('User status toggled successfully', {
      userId: user.id,
      disabled
    })

    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Failed to toggle user status', error)
      ElMessage.error(error.response?.data?.message || t('system.user.toggleStatusFailed'))
    }
  }
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      t('system.user.deleteConfirm', { name: user.realName || user.username }),
      t('system.user.deleteUser'),
      { type: 'warning' }
    )

    logger.info('Delete user', { userId: user.id, username: user.username })

    await deleteUser(user.id)

    ElMessage.success(t('system.user.deleteSuccess'))
    logger.info('User deleted successfully', { userId: user.id })

    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Failed to delete user', error)
      ElMessage.error(error.response?.data?.message || t('system.user.deleteFailed'))
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true
    logger.info('Submit user form', { isEdit: isEdit.value, userId: formData.id })

    if (isEdit.value) {
      // 更新用户：根据API文档，需要 id, realName, email, phone, departmentId
      const departmentPayload = resolveDepartmentPayload()
      const submitData = {
        id: formData.id, // 必须与路径参数一致
        realName: formData.realName,
        email: formData.email,
        phone: formData.phone,
        departmentId: departmentPayload.departmentId ?? formData.departmentId,
        departmentCode: departmentPayload.departmentCode
      }
      await updateUser(formData.id, submitData)
      ElMessage.success(t('system.user.updateSuccess'))
      logger.info('User updated successfully', { userId: formData.id })
    } else {
      // 新增用户：保持原有数据结构
      const departmentPayload = resolveDepartmentPayload()
      const submitData = {
        username: formData.username,
        realName: formData.realName,
        email: formData.email,
        phone: formData.phone,
        departmentId: departmentPayload.departmentId ?? formData.departmentId,
        departmentCode: departmentPayload.departmentCode,
        industryType: formData.industryType || industryStore.industryCode || 'default',
        password: formData.password
      }
      await createUser(submitData)
      ElMessage.success(t('system.user.createSuccess'))
      logger.info('User created successfully', { username: submitData.username })
    }

    showDialog.value = false
    submitting.value = false
    loadUsers()
  } catch (error) {
    submitting.value = false
    logger.error('User form submission failed', error)
    if (error !== false) {
      ElMessage.error(error.response?.data?.message || t('common.operationFailed'))
    }
  }
}

const handleDialogClose = () => {
  resetForm()
  formRef.value?.resetFields()
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    departmentId: null,
    industryType: industryStore.industryCode || 'default',
    status: 'active',
    password: ''
  })
}

const handlePageChange = (page) => {
  pagination.current = page
  loadUsers()
}

const handlePageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadUsers()
}

watch(
  () => departmentOptions.value,
  () => {
    syncDepartmentValue()
  },
  { deep: true }
)

// 生命周期
onMounted(async () => {
  await industryStore.fetchIndustryConfig()
  loadUsers()
})
</script>

<style lang="scss" scoped>
.user-management-container {
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

  .header-actions {
    display: flex;
    gap: 12px;
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

.user-table {
  width: 100%;
  border-radius: 8px;
  overflow: visible;
  border: 1px solid var(--border);
  
  // 确保表格充分利用宽度
  :deep(.base-table) {
    width: 100%;
  }
  
  :deep(.base-table__table) {
    width: 100% !important;
    min-width: 100%;
  }
  
  // 业务特定样式：用户管理表格的特定样式
  // 注意：通用表格样式已在 BaseTable.vue 中定义
  :deep(.el-table) {
    // 表头样式（业务特定）
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
    
    // 表体样式（业务特定）
    .el-table__body {
      td {
        padding: 12px 16px !important;
        font-size: 14px;
        color: var(--text);
        white-space: nowrap;
        // 确保所有行的内容都可以被滚动隐藏
        overflow: visible !important;
        position: relative !important;
        
        .cell {
          padding: 0 !important;
          line-height: 1.6;
          white-space: nowrap;
          // 确保 cell 内容可以被滚动隐藏
          overflow: visible !important;
          position: relative !important;
        }
      }
      
      // 确保 stripe 行也能正确滚动隐藏 - 强制设置
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
    
    // 操作列固定右侧（业务特定）
    .el-table__fixed-right {
      right: 0 !important;
      box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
      
      // 确保固定列中的 stripe 行也能正确滚动隐藏 - 强制设置
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

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
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
  
  &.btn-primary {
    color: var(--color-primary);
    border-color: var(--color-primary);
    
    &:hover {
      background: var(--color-primary);
      color: var(--surface);
    }
  }
  
  &.btn-warning {
    color: #f59e0b;
    border-color: #f59e0b;
    
    &:hover {
      background: #f59e0b;
      color: var(--surface);
    }
  }
  
  &.btn-info {
    color: var(--text-3);
    border-color: var(--text-3);
    
    &:hover {
      background: var(--text-3);
      color: var(--surface);
    }
  }
  
  &.btn-success {
    color: #16a34a;
    border-color: #16a34a;
    
    &:hover {
      background: #16a34a;
      color: var(--surface);
    }
  }
  
  &.btn-danger {
    color: #dc2626;
    border-color: #dc2626;
    
    &:hover {
      background: #dc2626;
      color: var(--surface);
    }
  }
}

// 角色和状态标签样式 - 参考原型图
.role-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  
  &.role-admin {
    background: #fee2e2;
    color: #dc2626;
  }
  
  &.role-teacher {
    background: #dbeafe;
    color: #1e3a8a;
  }
  
  &.role-student {
    background: #dbeafe;
    color: #2563eb;
  }
  
  &.role-default {
    background: var(--hover);
    color: var(--text-2);
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  
  &.status-normal {
    background: #dcfce7;
    color: #16a34a;
  }
  
  &.status-disabled {
    background: #fee2e2;
    color: #dc2626;
  }
}

@media (max-width: 1200px) {
  .user-management-container {
    padding: 24px 32px;
  }
}

@media (max-width: 768px) {
  .user-management-container {
    padding: 16px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .content-card {
    padding: 16px;
  }
}
</style>

