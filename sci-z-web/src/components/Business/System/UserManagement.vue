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
        
        <el-button type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ t('common.search') }}
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          {{ t('common.reset') }}
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          {{ t('system.user.addUser') }}
        </el-button>
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
          <div v-if="getRoleNames(row).length > 0" class="role-tags-container">
            <span
              v-for="(roleName, index) in getRoleNames(row)"
              :key="index"
              class="role-tag"
              :class="getRoleClass(roleName)"
            >
              {{ getRoleText(roleName) }}
            </span>
          </div>
          <span v-else class="role-empty">—</span>
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
            <div class="action-row">
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
            </div>
            <div class="action-row">
              <button
                class="action-btn btn-warning"
                @click="handleBindRoles(row)"
              >
                {{ t('system.user.bindRoles') }}
              </button>
              <button
                class="action-btn"
                :class="(row.status === 1 || row.status === 'active') ? 'btn-info' : 'btn-success'"
                @click="handleToggleStatus(row)"
              >
                {{ (row.status === 1 || row.status === 'active') ? t('common.disable') : t('common.enable') }}
              </button>
            </div>
            <div class="action-row">
              <button
                class="action-btn btn-danger"
                :disabled="isAdminUser(row)"
                :title="isAdminUser(row) ? t('system.user.cannotDeleteAdmin') : ''"
                @click="handleDelete(row)"
              >
                {{ t('common.delete') }}
              </button>
            </div>
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
        <el-form-item :label="departmentFieldLabel" prop="departmentCode">
          <el-select
            v-model="formData.departmentCode"
            :placeholder="departmentFieldPlaceholder"
            style="width: 100%"
            filterable
            @change="() => formRef?.clearValidate('departmentCode')"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.code"
              :label="dept.name"
              :value="dept.code"
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

    <!-- 绑定角色对话框 -->
    <el-dialog
      v-model="showBindRolesDialog"
      :title="t('system.user.bindRoles')"
      width="1000px"
      @close="handleBindRolesDialogClose"
    >
      <div class="bind-roles-container">
        <!-- 当前用户信息 -->
        <div class="bind-roles-user-info">
          <strong>{{ t('system.user.user') }}：</strong>
          <span class="user-name">{{ currentUser?.realName || currentUser?.username }}</span>
        </div>

        <!-- 搜索区域 -->
        <div class="bind-roles-search">
          <el-input
            v-model="roleSearchKeyword"
            :placeholder="t('system.user.searchRolePlaceholder')"
            clearable
            style="width: 100%"
            @keyup.enter="handleSearchRoles"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearchRoles">
            <el-icon><Search /></el-icon>
            {{ t('common.search') }}
          </el-button>
        </div>

        <!-- 角色列表表格 -->
        <BaseTable
          ref="bindRolesTableRef"
          :data="availableRoleList"
          :columns="availableRoleTableColumns"
          :loading="availableRoleLoading"
          :pagination="availableRolePagination"
          :selectable="true"
          row-key="id"
          :empty-text="t('common.noData')"
          stripe
          class="bind-roles-table"
          @selection-change="handleAvailableRoleSelectionChange"
          @current-change="handleAvailableRolePageChange"
          @size-change="handleAvailableRolePageSizeChange"
        >
          <!-- 状态列自定义 -->
          <template #status="{ row }">
            <span class="status-tag" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </BaseTable>
      </div>

      <template #footer>
        <div class="bind-roles-footer">
          <span class="selected-count">
            {{ t('system.user.selectedRoles', { count: selectedRoleIds.length }) }}
          </span>
          <div>
            <BaseButton @click="showBindRolesDialog = false">{{ t('common.cancel') }}</BaseButton>
            <BaseButton type="primary" @click="handleConfirmBindRoles" :loading="submitting">
              {{ t('common.confirm') }}
            </BaseButton>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { BaseButton, BaseCard, BaseTable, BaseDialog, BaseSwitch } from '@/components/Common'
import { getUsers, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword, getRoles, getUserRoles, updateUserRoles } from '@/api/System/system'
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

// 绑定角色相关
const showBindRolesDialog = ref(false)
const availableRoleLoading = ref(false)
const roleSearchKeyword = ref('')
const availableRoleList = ref([])
const selectedRoleIds = ref([])
const bindRolesTableRef = ref(null)
const currentUser = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 角色分页
const availableRolePagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  departmentCode: '',
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
  const target = departmentOptions.value.find((dept) => dept.code === formData.departmentCode)
  return target?.name || ''
}

// 可选角色表格列配置
const availableRoleTableColumns = computed(() => [
  { prop: 'roleName', label: t('system.role.roleName'), width: 150, align: 'center' },
  { prop: 'description', label: t('system.role.roleDescription'), minWidth: 200, align: 'center', showOverflowTooltip: true },
  { prop: 'status', label: t('common.status'), width: 100, align: 'center' }
])

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
    width: 240,
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
    width: 140,
    showOverflowTooltip: true
  },
  {
    prop: 'role',
    label: industryStore.roleLabel || t('system.user.role'),
    width: 200,
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
  departmentCode: [
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
// 获取角色名称数组（支持 roleNames 数组和旧的 roleName 字符串）
const getRoleNames = (row) => {
  // 优先使用 roleNames 数组
  if (Array.isArray(row.roleNames) && row.roleNames.length > 0) {
    return row.roleNames.filter(name => name && name.trim())
  }
  // 兼容旧的 roleName 字符串
  if (row.roleName && typeof row.roleName === 'string') {
    return [row.roleName]
  }
  // 兼容旧的 role 字段
  if (row.role && typeof row.role === 'string') {
    return [row.role]
  }
  return []
}

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

// 判断是否为管理员用户（不允许删除）
const isAdminUser = (user) => {
  return user.username === 'admin' || user.realName === '系统管理员'
}

const findDepartmentOption = (value) => {
  if (value === undefined || value === null || value === '') return null
  const valueStr = String(value)
  return departmentOptions.value.find((option) => {
    if (!option) return false
    // 优先匹配 code，因为现在使用 departmentCode
    const candidates = [
      option.code,
      option.name,
      option.id,
      option.numericId,
      option.code ? String(option.code) : undefined,
      option.name ? String(option.name) : undefined,
      typeof option.id === 'number' ? String(option.id) : undefined,
      typeof option.numericId === 'number' ? String(option.numericId) : undefined
    ].filter((item) => item !== undefined && item !== null)
    return candidates.some((candidate) => String(candidate) === valueStr)
  })
}

const syncDepartmentValue = (value = formData.departmentCode) => {
  if (value === undefined || value === null || value === '') return
  const match = findDepartmentOption(value)
  if (match && match.code) {
    formData.departmentCode = match.code
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
  // 现在直接使用 departmentCode（string类型）
  const selected = findDepartmentOption(formData.departmentCode)
  const departmentCode = selected?.code || formData.departmentCode || ''
  return {
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
  
  // 先设置基本信息
  Object.assign(formData, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    email: user.email,
    phone: user.phone,
    industryType: user.industryType || industryStore.industryCode || '',
    status: user.status === 1 || user.status === 'active' ? 'active' : 'inactive',
    password: ''
  })
  
  // 处理部门回显：优先使用 departmentCode（string类型）
  const departmentChain = [
    user.departmentCode,
    user.department?.code,
    user.departmentId, // 兼容旧数据
    user.department?.id, // 兼容旧数据
    user.departmentName,
    user.department?.name
  ].filter((item) => item !== undefined && item !== null && item !== '')
  
  // 尝试在选项中找到匹配项
  let matched = false
  for (const candidate of departmentChain) {
    const match = findDepartmentOption(candidate)
    if (match && match.code) {
      formData.departmentCode = match.code
      matched = true
      logger.debug('Department matched', {
        candidate,
        matchedCode: match.code,
        matchedName: match.name
      })
      break
    }
  }
  
  // 如果找不到匹配项，但有部门名称，创建一个临时选项
  if (!matched && (user.departmentName || user.department?.name)) {
    const fallbackName = user.departmentName || user.department?.name
    const fallbackCode = user.departmentCode || user.department?.code || `TEMP_${Date.now()}`
    const fallbackOption = {
      id: fallbackCode,
      numericId: null,
      code: fallbackCode,
      name: fallbackName
    }
    departmentOptions.value = [fallbackOption, ...departmentOptions.value]
    formData.departmentCode = fallbackOption.code
    logger.debug('Created fallback department option', {
      fallbackOption,
      formDataDepartmentCode: formData.departmentCode
    })
  } else if (!matched) {
    // 如果没有任何部门信息，设置为空字符串，让用户选择
    formData.departmentCode = ''
    logger.warn('No department found, setting to empty', {
      user,
      availableOptions: departmentOptions.value.map(d => ({ code: d.code, name: d.name }))
    })
  }
  
  showDialog.value = true
  // 等待 DOM 更新后清除验证状态，确保部门选择正确回显
  await nextTick()
  // 再次等待，确保 el-select 已经渲染完成
  setTimeout(() => {
    if (formRef.value) {
      formRef.value.clearValidate('departmentCode')
      formRef.value.clearValidate()
    }
  }, 100)
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
    // 确保 userId 是数字类型，避免后端 JSON 解析错误
    const userId = typeof currentResetUser.value.id === 'number' 
      ? currentResetUser.value.id 
      : Number(currentResetUser.value.id)
    if (isNaN(userId) || !isFinite(userId)) {
      throw new Error('用户ID无效')
    }
    
    await resetUserPassword({
      userId: userId,
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
      {
        type: 'warning',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
      }
    )

    logger.info('Toggle user status', {
      userId: user.id,
      username: user.username,
      action
    })

    // 使用新的API：PUT /api/user/users/{id}/status?disabled=true/false
    // 确保 user.id 是数字类型，避免后端解析错误
    const userId = typeof user.id === 'number' ? user.id : Number(user.id)
    if (isNaN(userId) || !isFinite(userId)) {
      throw new Error('用户ID无效')
    }
    const disabled = isActive // status=1 -> disabled=true, status=0 -> disabled=false
    await updateUserStatus(userId, disabled)

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
  if (isAdminUser(user)) {
    ElMessage.warning(t('system.user.cannotDeleteAdmin'))
    return
  }

  try {
    await ElMessageBox.confirm(
      t('system.user.deleteConfirm', { name: user.realName || user.username }),
      t('system.user.deleteUser'),
      {
        type: 'warning',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
      }
    )

    logger.info('Delete user', { userId: user.id, username: user.username })

    // 确保 user.id 是数字类型，避免后端解析错误
    const userId = typeof user.id === 'number' ? user.id : Number(user.id)
    if (isNaN(userId) || !isFinite(userId)) {
      throw new Error('用户ID无效')
    }
    await deleteUser(userId)

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
      // 更新用户：根据API文档，需要 id, realName, email, phone, departmentCode（string类型）
      // 确保所有 ID 都是数字类型，避免后端 JSON 解析错误
      const userId = typeof formData.id === 'number' ? formData.id : Number(formData.id)
      if (isNaN(userId) || !isFinite(userId)) {
        throw new Error('用户ID无效')
      }
      
      const departmentPayload = resolveDepartmentPayload()
      const submitData = {
        id: userId, // 必须与路径参数一致，且必须是数字
        realName: formData.realName,
        email: formData.email,
        phone: formData.phone,
        departmentCode: departmentPayload.departmentCode // 必须传递 departmentCode（string类型）
      }
      // departmentCode 是必填字段，如果为空则报错
      if (!submitData.departmentCode || submitData.departmentCode.trim() === '') {
        ElMessage.error(t('system.user.departmentRequired'))
        throw new Error('部门代码不能为空')
      }
      await updateUser(userId, submitData)
      ElMessage.success(t('system.user.updateSuccess'))
      logger.info('User updated successfully', { userId: formData.id })
    } else {
      // 新增用户：使用 departmentCode（string类型）
      const departmentPayload = resolveDepartmentPayload()
      const submitData = {
        username: formData.username,
        realName: formData.realName,
        email: formData.email,
        phone: formData.phone,
        departmentCode: departmentPayload.departmentCode, // 使用 departmentCode（string类型）
        industryType: formData.industryType || industryStore.industryCode || 'default',
        password: formData.password
      }
      // departmentCode 是必填字段，如果为空则报错
      if (!submitData.departmentCode || submitData.departmentCode.trim() === '') {
        ElMessage.error(t('system.user.departmentRequired'))
        throw new Error('部门代码不能为空')
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
    departmentCode: '',
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

// 绑定角色相关方法
const handleBindRoles = async (user) => {
  currentUser.value = user
  showBindRolesDialog.value = true
  availableRolePagination.current = 1
  roleSearchKeyword.value = ''
  selectedRoleIds.value = []
  
  // 先加载角色列表
  await loadAvailableRoles()
  
  // 然后加载已绑定的角色ID，用于回显
  await loadUserRoleIds(user.id)
}

const loadAvailableRoles = async () => {
  try {
    availableRoleLoading.value = true
    logger.info('Loading available roles', { userId: currentUser.value?.id })

    const requestData = {
      pageNo: availableRolePagination.current,
      pageSize: availableRolePagination.size,
      sortBy: 'sortOrder',
      sortOrder: 'ASC'
    }

    // 如果有搜索关键词，添加到请求参数
    if (roleSearchKeyword.value) {
      requestData.keyword = roleSearchKeyword.value
    }

    const response = await getRoles(requestData)
    const data = response?.data?.data || response?.data || {}

    availableRoleList.value = (data.records || []).map((role) => ({
      id: role.id,
      roleName: role.roleName,
      roleCode: role.roleCode,
      description: role.description,
      status: role.status !== undefined ? role.status : 1 // 默认启用状态（1=启用，0=禁用）
    }))

    availableRolePagination.total = data.total || 0
    availableRolePagination.current = data.current || data.pageNo || availableRolePagination.current
    availableRolePagination.size = data.size || data.pageSize || availableRolePagination.size

    logger.info('Available roles loaded successfully', { count: availableRoleList.value.length })
  } catch (error) {
    logger.error('Failed to load available roles', error)
    ElMessage.error(error.response?.data?.message || t('system.user.loadAvailableRolesError'))
    availableRoleList.value = []
    availableRolePagination.total = 0
  } finally {
    availableRoleLoading.value = false
  }
}

const handleSearchRoles = () => {
  availableRolePagination.current = 1
  loadAvailableRoles()
}

const handleAvailableRoleSelectionChange = (selection) => {
  selectedRoleIds.value = selection.map((role) => role.id)
  logger.info('Role selection changed', { 
    count: selectedRoleIds.value.length, 
    selectedIds: selectedRoleIds.value 
  })
}

/**
 * 加载用户绑定的角色ID集合（用于回显）
 */
const loadUserRoleIds = async (userId) => {
  try {
    logger.info('Loading user role IDs for echo', { userId })
    
    const response = await getUserRoles(userId)
    const roleIdList = response?.data?.data?.roleIdList || response?.data?.roleIdList || []
    
    // 设置已选中的角色ID
    selectedRoleIds.value = roleIdList
    
    // 等待表格渲染完成后，设置选中状态
    await nextTick()
    if (bindRolesTableRef.value && bindRolesTableRef.value.tableRef) {
      const elTable = bindRolesTableRef.value.tableRef
      // 遍历角色列表，选中已绑定的角色
      availableRoleList.value.forEach(role => {
        if (roleIdList.includes(role.id)) {
          elTable.toggleRowSelection(role, true)
        }
      })
    }
    
    logger.info('User role IDs loaded for echo', { 
      userId, 
      count: roleIdList.length,
      roleIds: roleIdList 
    })
  } catch (error) {
    logger.error('Failed to load user role IDs', error)
    // 回显失败不影响功能，只记录日志
  }
}

/**
 * 确认绑定角色到用户
 * 调用接口：POST /api/user/users/roles
 * 请求体：{ userId: number, roleIdList: number[] }
 */
const handleConfirmBindRoles = async () => {
  if (selectedRoleIds.value.length === 0) {
    ElMessage.warning(t('system.user.noRolesSelected'))
    return
  }

  try {
    submitting.value = true
    const userId = currentUser.value.id
    
    logger.info('Binding roles to user', {
      userId,
      roleIds: selectedRoleIds.value,
      api: 'POST /api/user/users/roles'
    })

    await updateUserRoles({
      userId,
      roleIdList: selectedRoleIds.value
    })

    ElMessage.success(t('system.user.bindRolesSuccess'))
    showBindRolesDialog.value = false
    submitting.value = false
    selectedRoleIds.value = []

    // 重新加载用户列表（更新角色信息）
    await loadUsers()
  } catch (error) {
    submitting.value = false
    logger.error('Failed to bind roles to user', error)
    ElMessage.error(error.response?.data?.message || t('system.user.bindRolesFailed'))
  }
}

const handleBindRolesDialogClose = () => {
  currentUser.value = null
  availableRoleList.value = []
  roleSearchKeyword.value = ''
  selectedRoleIds.value = []
  availableRolePagination.current = 1
  availableRolePagination.total = 0
}

const handleAvailableRolePageChange = (page) => {
  availableRolePagination.current = page
  loadAvailableRoles()
}

const handleAvailableRolePageSizeChange = (size) => {
  availableRolePagination.size = size
  availableRolePagination.current = 1
  loadAvailableRoles()
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
    display: flex;
    flex-direction: column;
  }
  
  :deep(.base-table__table) {
    width: 100% !important;
    min-width: 100%;
    flex: 1;
    overflow: auto;
  }
  
  // Element Plus 表格会自动处理表头和表体的对齐，不需要额外样式
  
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
  
  &.btn-success {
    color: #16a34a;
    border-color: #16a34a;
    
    &:hover:not(:disabled) {
      background: #16a34a;
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

// 角色和状态标签样式 - 参考原型图
.role-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-width: 150px;
  max-width: 100%;
}

.role-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  width: auto;
  min-width: 70px;
  max-width: 120px;
  flex-shrink: 0;
  text-align: center;
  
  &.role-admin {
    background: #fecaca;
    color: #b91c1c;
  }
  
  &.role-teacher {
    background: #bfdbfe;
    color: #1e40af;
  }
  
  &.role-student {
    background: #bfdbfe;
    color: #1d4ed8;
  }
  
  &.role-default {
    background: #e5e7eb;
    color: #374151;
  }
}

.role-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  font-size: 20px;
  font-weight: 300;
  width: 100%;
  min-height: 40px;
  letter-spacing: 1px;
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

// 绑定角色对话框样式
.bind-roles-container {
  // 当前用户信息
  .bind-roles-user-info {
    margin-bottom: var(--gap-lg);
    font-size: var(--font-size-base);
    color: var(--text-1);

    strong {
      color: var(--color-primary);
      font-weight: 600;
    }

    .user-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }

  // 搜索区域
  .bind-roles-search {
    display: flex;
    gap: var(--gap-md);
    margin-bottom: var(--gap-lg);
    align-items: center;
  }

  // 角色表格
  .bind-roles-table {
    width: 100%;
    
    :deep(.base-table) {
      width: 100%;
    }
    
    :deep(.el-table) {
      width: 100%;
    }
    
    // 确保表格内容居中
    :deep(.el-table__cell) {
      text-align: center;
    }
    
    // 表头也居中
    :deep(.el-table__header-wrapper .el-table__cell) {
      text-align: center;
    }
  }
}

// 绑定角色对话框底部
.bind-roles-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .selected-count {
    color: var(--text-2);
    font-size: var(--font-size-sm);
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

