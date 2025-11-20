<!--
/**
 * @description 系统管理 - 角色权限业务组件
 * 负责角色列表展示、新增/编辑角色、配置权限等功能
 */
-->
<template>
  <div class="role-management-container">
    <div class="page-header">
      <h1 class="page-title">{{ t('system.role.title') }}</h1>
    </div>

    <BaseCard class="content-card">
      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="t('system.role.searchPlaceholder')"
          clearable
          style="width: 250px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
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
          {{ t('system.role.addRole') }}
        </el-button>
      </div>

      <!-- 角色列表表格 -->
      <BaseTable
        :data="roleList"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        :action-width="360"
        action-fixed="right"
        :empty-text="t('common.noData')"
        stripe
        class="role-table"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      >
        <!-- 用户数量列自定义 -->
        <template #userCount="{ row }">
          <el-tag 
            type="info" 
            class="user-count-tag"
            :class="{ 'clickable': row.userCount > 0 }"
            @click="row.userCount > 0 && handleViewRoleUsers(row)"
          >
            {{ row.userCount || 0 }}
          </el-tag>
        </template>

        <!-- 创建时间列自定义 -->
        <template #createdTime="{ row }">
          {{ formatDate(row.createdTime, 'YYYY-MM-DD HH:mm:ss') }}
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
                class="action-btn btn-info"
                @click="handleConfigPermission(row)"
              >
                {{ t('system.role.configPermission') }}
              </button>
              <button
                class="action-btn btn-danger"
                :disabled="row.userCount > 0 || isSystemRole(row)"
                :title="getDeleteButtonTitle(row)"
                @click="handleDelete(row)"
              >
                {{ t('common.delete') }}
              </button>
            </div>
          </div>
        </template>
      </BaseTable>
    </BaseCard>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="showRoleDialog"
      :title="roleDialogTitle"
      width="500px"
      @close="handleRoleDialogClose"
    >
      <el-form
        ref="roleFormRef"
        :model="roleFormData"
        :rules="roleFormRules"
        label-width="100px"
      >
        <el-form-item :label="t('system.role.roleName')" prop="name">
          <el-input v-model="roleFormData.name" />
        </el-form-item>
        <el-form-item :label="t('system.roleCode')" prop="code">
          <el-input v-model="roleFormData.code" :disabled="isEdit" :placeholder="t('system.role.codePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('system.role.roleDescription')" prop="description">
          <el-input
            v-model="roleFormData.description"
            type="textarea"
            :rows="3"
            :placeholder="t('system.role.descriptionPlaceholder')"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <BaseButton @click="showRoleDialog = false">{{ t('common.cancel') }}</BaseButton>
        <BaseButton type="primary" @click="handleRoleSubmit" :loading="submitting">
          {{ t('common.confirm') }}
        </BaseButton>
      </template>
    </el-dialog>

    <!-- 权限配置对话框 -->
    <el-dialog
      v-model="showPermissionDialog"
      :title="t('system.role.configPermission')"
      width="800px"
      @close="handlePermissionDialogClose"
    >
      <div class="permission-config">
        <!-- 当前角色信息 -->
        <div class="permission-role-info">
          <strong>{{ t('system.role.currentRole') }}：</strong>
          <span class="role-name">{{ currentRole?.roleName || currentRole?.name }}</span>
        </div>

        <!-- 权限列表容器 -->
        <div class="permission-tree-container">
          <!-- 权限列表标题和操作按钮 -->
          <div class="permission-tree-header">
            <span class="permission-list-title">{{ t('system.role.permissionList') }}</span>
            <div class="permission-actions">
              <el-button size="small" @click="handleExpandAll">{{ t('system.role.expandAll') }}</el-button>
              <el-button size="small" @click="handleCollapseAll">{{ t('system.role.collapseAll') }}</el-button>
              <el-button size="small" @click="handleSelectAll">{{ t('system.role.selectAll') }}</el-button>
              <el-button size="small" @click="handleClearAll">{{ t('system.role.clearAll') }}</el-button>
            </div>
          </div>

          <!-- 搜索框 -->
          <div class="permission-tree-search">
            <el-input
              v-model="permissionSearchText"
              :placeholder="t('system.role.searchPermissionName')"
              clearable
              @input="handlePermissionSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <!-- 权限树 -->
          <el-tree
            ref="permissionTreeRef"
            :data="filteredPermissionTree"
            :props="treeProps"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedPermissions"
            :filter-node-method="filterNode"
            class="permission-tree"
          >
            <template #default="{ node, data }">
              <div class="permission-node">
                <el-icon class="permission-icon">
                  <component :is="getPermissionIcon(data)" />
                </el-icon>
                <span class="permission-label">{{ node.label }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </div>

      <template #footer>
        <BaseButton @click="showPermissionDialog = false">{{ t('common.cancel') }}</BaseButton>
        <BaseButton type="primary" @click="handlePermissionSubmit" :loading="submitting">
          {{ t('system.role.savePermission') }}
        </BaseButton>
      </template>
    </el-dialog>

    <!-- 查看角色用户对话框 -->
    <el-dialog
      v-model="showViewUsersDialog"
      :title="t('system.role.viewRoleUsers')"
      width="1000px"
      @close="handleViewUsersDialogClose"
    >
      <div class="view-users-container">
        <!-- 当前角色信息 -->
        <div class="view-users-role-info">
          <strong>{{ t('system.role.role') }}：</strong>
          <span class="role-name">{{ currentRole?.roleName || currentRole?.name }}</span>
        </div>

        <!-- 搜索区域 -->
        <div class="view-users-search">
          <el-input
            v-model="roleUserSearchKeyword"
            :placeholder="t('system.role.searchUserPlaceholder')"
            clearable
            style="width: 100%"
            @keyup.enter="handleSearchRoleUsers"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearchRoleUsers">
            <el-icon><Search /></el-icon>
            {{ t('common.search') }}
          </el-button>
        </div>

        <!-- 用户列表表格 -->
        <BaseTable
          :data="roleUserList"
          :columns="roleUserTableColumns"
          :loading="roleUserLoading"
          :pagination="roleUserPagination"
          :empty-text="t('common.noData')"
          stripe
          class="view-users-table"
          @current-change="handleRoleUserPageChange"
          @size-change="handleRoleUserPageSizeChange"
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
        <BaseButton @click="showViewUsersDialog = false">{{ t('common.close') }}</BaseButton>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Folder, Document, Menu, Setting } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { formatDate } from '@/utils/date'
import { validateRoleName } from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'
import { BaseCard, BaseButton, BaseTable } from '@/components/Common/index.js'
import {
  getRoles,
  createRole,
  updateRole,
  deleteRole,
  getPermissionsTree,
  getRolePermissions,
  updateRolePermissions,
  getRoleUsers,
  getRoleUserIds,
  updateUserRoles,
  getUsers
} from '@/api/System/system'

const { t } = useI18n()
const logger = createLogger('RoleManagement')

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showRoleDialog = ref(false)
const showPermissionDialog = ref(false)
const showBindUsersDialog = ref(false)
const showViewUsersDialog = ref(false)
const isEdit = ref(false)
const roleFormRef = ref()
const permissionTreeRef = ref()
const permissionSearchText = ref('')
const expandedKeys = ref([])

// 绑定用户相关
const availableUserLoading = ref(false)
const userSearchKeyword = ref('')
const availableUserList = ref([])
const selectedUserIds = ref([])
const bindUsersTableRef = ref(null)

// 查看角色用户相关
const roleUserLoading = ref(false)
const roleUserSearchKeyword = ref('')
const roleUserList = ref([])

// 用户分页
const availableUserPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 角色用户分页
const roleUserPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 角色表单数据
const roleFormData = reactive({
  id: null,
  name: '', // 用于表单显示，提交时转换为roleName
  code: '', // 用于表单显示，提交时转换为roleCode
  description: '',
  sortOrder: 0 // 排序值，默认0
})

// 角色表单验证规则
const roleFormRules = computed(() => ({
  name: [
    { required: true, message: t('system.role.nameRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validateRoleName(value)) {
          callback(new Error(t('system.role.nameFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: t('system.role.codeRequired'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: t('system.role.codeFormat'),
      trigger: 'blur'
    }
  ],
  description: [
    { required: true, message: t('system.role.descriptionRequired'), trigger: 'blur' }
  ]
}))

// 当前操作的角色
const currentRole = ref(null)
const checkedPermissions = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 角色列表数据
const roleList = ref([])

// 表格列配置
const tableColumns = computed(() => [
  { prop: 'roleName', label: t('system.role.roleName'), minWidth: 150 },
  { 
    prop: 'description', 
    label: t('system.role.roleDescription'), 
    minWidth: 200, 
    wrap: true // 🔥 允许换行，充分利用空间
  },
  { prop: 'userCount', label: t('system.role.userCount'), minWidth: 120, align: 'center' },
  { prop: 'createdTime', label: t('common.createTime'), minWidth: 180 }
])

// 用户表格列配置
const userTableColumns = computed(() => [
  { prop: 'username', label: t('system.user.username'), minWidth: 150 },
  { prop: 'realName', label: t('system.user.realName'), minWidth: 120 },
  { prop: 'email', label: t('system.user.email'), minWidth: 200, wrap: true },
  { prop: 'phone', label: t('system.user.phone'), minWidth: 150 },
  { prop: 'status', label: t('common.status'), minWidth: 100, align: 'center' }
])

// 可选用户表格列配置 - 所有列居中对齐
const availableUserTableColumns = computed(() => [
  { prop: 'username', label: t('system.user.username'), minWidth: 150, align: 'center' },
  { prop: 'realName', label: t('system.user.realName'), minWidth: 120, align: 'center' },
  { prop: 'email', label: t('system.user.email'), minWidth: 200, align: 'center', wrap: true },
  { prop: 'phone', label: t('system.user.phone'), minWidth: 150, align: 'center' },
  { prop: 'status', label: t('common.status'), minWidth: 100, align: 'center' }
])

// 角色用户表格列配置
const roleUserTableColumns = computed(() => [
  { prop: 'username', label: t('system.user.username'), minWidth: 150 },
  { prop: 'realName', label: t('system.user.realName'), minWidth: 120 },
  { prop: 'email', label: t('system.user.email'), minWidth: 200, wrap: true },
  { prop: 'phone', label: t('system.user.phone'), minWidth: 150 },
  { prop: 'departmentName', label: t('system.user.department'), minWidth: 150, wrap: true },
  { prop: 'status', label: t('common.status'), minWidth: 100, align: 'center' }
])

// 状态相关方法
const getStatusClass = (status) => {
  const isActive = status === 1 || status === 'active'
  return isActive ? 'status-normal' : 'status-disabled'
}

const getStatusText = (status) => {
  const isActive = status === 1 || status === 'active'
  return isActive ? t('common.active') : t('common.inactive')
}

// 判断是否为系统管理员角色（不允许删除）
const isSystemRole = (role) => {
  return role.roleCode === 'admin'
}

// 获取删除按钮的提示信息
const getDeleteButtonTitle = (role) => {
  if (isSystemRole(role)) {
    return t('system.role.cannotDeleteSystemRole')
  }
  if (role.userCount > 0) {
    return t('system.role.cannotDeleteWithUsers')
  }
  return ''
}

// 权限树数据
const permissionTree = ref([])

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'label'
}

// 计算属性
const roleDialogTitle = computed(() =>
  isEdit.value ? t('system.role.editRole') : t('system.role.addRole')
)

const filteredPermissionTree = computed(() => {
  if (!permissionSearchText.value) {
    return permissionTree.value
  }

  const filterTree = (nodes) => {
    return nodes
      .filter((node) => {
        const matchesSearch = node.label
          .toLowerCase()
          .includes(permissionSearchText.value.toLowerCase())
        const hasMatchingChildren = node.children
          ? filterTree(node.children).length > 0
          : false

        return matchesSearch || hasMatchingChildren
      })
      .map((node) => ({
        ...node,
        children: node.children ? filterTree(node.children) : undefined
      }))
  }

  return filterTree(permissionTree.value)
})

// 加载角色列表
const loadRoles = async () => {
  try {
    loading.value = true
    logger.info('Loading roles list', {
      pageNo: pagination.current,
      pageSize: pagination.size,
      filters: searchForm
    })

    const requestData = {
      pageNo: pagination.current,
      pageSize: pagination.size,
      sortBy: 'sortOrder',
      sortOrder: 'ASC'
    }

    // 只有用户输入时才传这些参数
    if (searchForm.keyword) {
      requestData.keyword = searchForm.keyword
    }

    const response = await getRoles(requestData)
    const data = response?.data?.data || response?.data || {}

    // 映射后端返回的数据结构
    roleList.value = (data.records || []).map((role) => ({
      id: role.id,
      name: role.roleName, // 映射roleName为name，用于编辑表单
      roleName: role.roleName,
      roleCode: role.roleCode,
      roleType: role.roleType,
      description: role.description,
      status: role.status,
      sortOrder: role.sortOrder,
      createdTime: role.createdTime,
      updatedTime: role.updatedTime,
      userCount: role.userCount || 0 // 如果后端没有返回，默认为0
    }))

    pagination.total = data.total || 0
    pagination.current = data.current || data.pageNo || pagination.current
    pagination.size = data.size || data.pageSize || pagination.size

    logger.info('Roles list loaded successfully', { count: roleList.value.length })
  } catch (error) {
    logger.error('Failed to load roles list', error)
    ElMessage.error(error.response?.data?.message || t('system.role.loadError'))
    roleList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadRoles()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  pagination.current = 1
  loadRoles()
  ElMessage.success(t('common.resetSuccess'))
}

// 加载权限树数据
const loadPermissionTree = async () => {
  try {
    logger.info('Loading permission tree')

    const response = await getPermissionsTree()
    
    // 处理不同的响应格式
    let treeData = []
    if (response && response.data) {
      // 可能的数据格式：
      // 1. { code: 200, data: { tree: [...] } }
      // 2. { code: 200, data: [...] }
      // 3. { data: { tree: [...] } }
      // 4. { data: [...] }
      const data = response.data
      if (data.tree && Array.isArray(data.tree)) {
        treeData = data.tree
      } else if (Array.isArray(data)) {
        treeData = data
      } else if (data.data && Array.isArray(data.data)) {
        treeData = data.data
      }
      
      // 确保数据格式正确：每个节点需要有 id 和 label
      treeData = normalizeTreeData(treeData)
      
      permissionTree.value = treeData
      logger.info('Permission tree loaded successfully', {
        count: permissionTree.value.length,
        sample: treeData.length > 0 ? treeData[0] : null
      })
    } else {
      logger.warn('Invalid permission tree response format', response)
      permissionTree.value = []
    }
  } catch (error) {
    logger.error('Failed to load permission tree', error)
    ElMessage.error(error.response?.data?.message || t('system.role.permissionTreeLoadError'))
    permissionTree.value = []
  }
}

// 标准化树形数据格式
const normalizeTreeData = (nodes) => {
  if (!Array.isArray(nodes)) return []
  
  return nodes.map((node) => {
    // 确保每个节点都有必需的字段
    const normalized = {
      id: node.id || node.permissionId || node.key,
      label: node.label || node.name || node.title || node.permissionName || '',
      code: node.code || node.permissionCode || '',
      type: node.type || node.permissionType || 'menu',
      children: node.children ? normalizeTreeData(node.children) : undefined
    }
    
    // 如果没有 children 或 children 为空数组，则删除 children 属性
    if (!normalized.children || normalized.children.length === 0) {
      delete normalized.children
    }
    
    return normalized
  })
}

// 加载角色权限
const loadRolePermissions = async (roleId) => {
  try {
    logger.info('Loading role permissions', { roleId })

    const response = await getRolePermissions(roleId)

    if (response && response.data) {
      // 新接口返回格式：{ permissionIdList: number[] }
      const permissionIdList = response.data.permissionIdList || response.data.permissions || []
      checkedPermissions.value = Array.isArray(permissionIdList) 
        ? permissionIdList 
        : permissionIdList.map((p) => p.id || p)
      logger.info('Role permissions loaded successfully', {
        roleId,
        count: checkedPermissions.value.length
      })
    } else {
      logger.warn('Invalid role permissions response format', response)
      checkedPermissions.value = []
    }
  } catch (error) {
    logger.error('Failed to load role permissions', error)
    checkedPermissions.value = []
  }
}

// 方法
const handleAdd = () => {
  isEdit.value = false
  resetRoleForm()
  showRoleDialog.value = true
}

const handleEdit = (role) => {
  isEdit.value = true
  Object.assign(roleFormData, {
    id: role.id,
    name: role.roleName || role.name, // 兼容roleName和name字段
    code: role.roleCode || role.code, // 兼容roleCode和code字段
    description: role.description || '',
    sortOrder: role.sortOrder || 0 // 排序值
  })
  showRoleDialog.value = true
}

const handleConfigPermission = async (role) => {
  currentRole.value = role
  showPermissionDialog.value = true
  expandedKeys.value = [] // 重置展开状态，避免刷新后还保持展开

  try {
    // 如果权限树还未加载，先加载权限树
    if (permissionTree.value.length === 0) {
      await loadPermissionTree()
    }

    // 加载角色权限
    await loadRolePermissions(role.id)

    // 等待 DOM 更新后再设置选中状态和重置展开状态
    await nextTick()
    if (permissionTreeRef.value && permissionTree.value.length > 0) {
      permissionTreeRef.value.setCheckedKeys(checkedPermissions.value)
      
      // 确保所有节点都是收起状态（通过 expandedKeys 控制）
      expandedKeys.value = []
      
      logger.info('Permission tree initialized', {
        roleId: role.id,
        checkedCount: checkedPermissions.value.length,
        treeNodeCount: permissionTree.value.length
      })
    }
  } catch (error) {
    logger.error('Failed to initialize permission dialog', error)
    ElMessage.error(t('system.role.permissionDialogInitError'))
  }
}

const handleDelete = async (role) => {
  if (isSystemRole(role)) {
    ElMessage.warning(t('system.role.cannotDeleteSystemRole'))
    return
  }

  if (role.userCount > 0) {
    ElMessage.warning(t('system.role.cannotDeleteWithUsers'))
    return
  }

  try {
    await ElMessageBox.confirm(
      t('system.role.deleteConfirm', { name: role.roleName || role.name }),
      t('system.role.deleteRole'),
      { type: 'warning' }
    )

    logger.info('Deleting role', { roleId: role.id, roleName: role.roleName || role.name })

    await deleteRole(role.id)

    ElMessage.success(t('system.role.deleteSuccess'))
    logger.info('Role deleted successfully', { roleId: role.id })

    // 重新加载列表
    await loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Failed to delete role', error)
      ElMessage.error(t('system.role.deleteFailed'))
    }
  }
}

const handleRoleSubmit = async () => {
  try {
    await roleFormRef.value.validate()

    submitting.value = true
    logger.info('Submitting role form', { isEdit: isEdit.value, roleData: roleFormData })

    if (isEdit.value) {
      // 更新角色：根据API规范，需要传递 id, roleName, description, sortOrder, status
      await updateRole(roleFormData.id, {
        id: roleFormData.id, // 必须与路径参数一致
        roleName: roleFormData.name, // 转换为roleName
        description: roleFormData.description || '', // 可选字段
        sortOrder: roleFormData.sortOrder || 0 // 可选字段，默认0
        // status 字段如果需要更新，可以从当前角色数据中获取
      })
      ElMessage.success(t('system.role.updateSuccess'))
      logger.info('Role updated successfully', { roleId: roleFormData.id })
    } else {
      // 创建角色：根据API规范，需要传递 roleName, roleCode, description, sortOrder
      await createRole({
        roleName: roleFormData.name, // 转换为roleName
        roleCode: roleFormData.code, // 转换为roleCode
        description: roleFormData.description || '', // 可选字段
        sortOrder: roleFormData.sortOrder || 0 // 可选字段，默认0
      })
      ElMessage.success(t('system.role.createSuccess'))
      logger.info('Role created successfully')
    }

    showRoleDialog.value = false
    submitting.value = false

    // 重新加载列表
    await loadRoles()
  } catch (error) {
    submitting.value = false
    if (error !== false) {
      logger.error('Role submit failed', error)
      const errorMessage = error.response?.data?.message || t('common.operationFailed')
      ElMessage.error(errorMessage)
    }
  }
}

const handlePermissionSubmit = async () => {
  try {
    submitting.value = true

    // 获取选中的权限
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]

    logger.info('Submitting role permissions', {
      roleId: currentRole.value.id,
      permissions: allCheckedKeys
    })

    await updateRolePermissions(currentRole.value.id, allCheckedKeys)

    ElMessage.success(t('system.role.permissionConfigSuccess'))
    showPermissionDialog.value = false
    submitting.value = false
    logger.info('Role permissions updated successfully', { roleId: currentRole.value.id })

    // 重新加载列表
    await loadRoles()
  } catch (error) {
    submitting.value = false
    logger.error('Failed to update role permissions', error)
    ElMessage.error(t('system.role.permissionConfigFailed'))
  }
}

const handleRoleDialogClose = () => {
  resetRoleForm()
  roleFormRef.value?.resetFields()
}

const handlePermissionDialogClose = () => {
  currentRole.value = null
  checkedPermissions.value = []
  permissionSearchText.value = ''
  expandedKeys.value = [] // 重置展开状态
}

// 绑定用户相关方法
const handleBindUsers = async (role) => {
  currentRole.value = role
  showBindUsersDialog.value = true
  availableUserPagination.current = 1
  userSearchKeyword.value = ''
  selectedUserIds.value = []
  
  // 先加载用户列表
  await loadAvailableUsers()
  
  // 然后加载已绑定的用户ID，用于回显
  await loadRoleUserIds(role.id)
}

const loadAvailableUsers = async () => {
  try {
    availableUserLoading.value = true
    logger.info('Loading available users', { roleId: currentRole.value?.id })

    const requestData = {
      pageNo: availableUserPagination.current,
      pageSize: availableUserPagination.size,
      sortBy: 'createdTime',
      sortOrder: 'DESC',
      status: 1 // 只加载启用状态的用户（1=正常，0=禁用）
    }

    // 如果有搜索关键词，添加到请求参数
    if (userSearchKeyword.value) {
      requestData.keyword = userSearchKeyword.value
    }

    const response = await getUsers(requestData)
    const data = response?.data?.data || response?.data || {}

    // 过滤掉禁用用户（status === 0），确保列表中只显示启用用户
    availableUserList.value = (data.records || [])
      .filter(user => {
        const status = user.status
        // 只保留启用状态的用户（status === 1 或 status === 'active'）
        return status === 1 || status === 'active' || status === 'enabled' || status === true
      })
      .map((user) => ({
        id: user.id,
        username: user.username,
        realName: user.realName || user.name,
        email: user.email,
        phone: user.phone,
        status: user.status
      }))

    availableUserPagination.total = data.total || 0
    availableUserPagination.current = data.current || data.pageNo || availableUserPagination.current
    availableUserPagination.size = data.size || data.pageSize || availableUserPagination.size

    logger.info('Available users loaded successfully', { count: availableUserList.value.length })
  } catch (error) {
    logger.error('Failed to load available users', error)
    ElMessage.error(error.response?.data?.message || t('system.role.loadAvailableUsersError'))
    availableUserList.value = []
    availableUserPagination.total = 0
  } finally {
    availableUserLoading.value = false
  }
}

const handleSearchUsers = () => {
  availableUserPagination.current = 1
  loadAvailableUsers()
}

const handleAvailableUserSelectionChange = (selection) => {
  // 由于列表已经过滤掉禁用用户，这里直接处理选择
  selectedUserIds.value = selection.map((user) => user.id)
  logger.info('User selection changed', { 
    count: selectedUserIds.value.length, 
    selectedIds: selectedUserIds.value 
  })
}

/**
 * 加载角色绑定的用户ID集合（用于回显）
 */
const loadRoleUserIds = async (roleId) => {
  try {
    logger.info('Loading role user IDs for echo', { roleId })
    
    const response = await getRoleUserIds(roleId)
    const userIdList = response?.data?.data?.userIdList || response?.data?.userIdList || []
    
    // 设置已选中的用户ID
    selectedUserIds.value = userIdList
    
    // 等待表格渲染完成后，设置选中状态
    await nextTick()
    if (bindUsersTableRef.value && bindUsersTableRef.value.tableRef) {
      const elTable = bindUsersTableRef.value.tableRef
      // 遍历用户列表，选中已绑定的用户
      availableUserList.value.forEach(user => {
        if (userIdList.includes(user.id)) {
          elTable.toggleRowSelection(user, true)
        }
      })
    }
    
    logger.info('Role user IDs loaded for echo', { 
      roleId, 
      count: userIdList.length,
      userIds: userIdList 
    })
  } catch (error) {
    logger.error('Failed to load role user IDs', error)
    // 回显失败不影响功能，只记录日志
  }
}

/**
 * 确认绑定用户到角色
 * 调用接口：POST /api/user/users/roles
 * 请求体：{ userId: number, roleIdList: number[] }
 * 注意：需要为每个选中的用户调用此接口，将该角色添加到用户的角色列表中
 */
const handleConfirmBindUsers = async () => {
  if (selectedUserIds.value.length === 0) {
    ElMessage.warning(t('system.role.noUsersSelected'))
    return
  }

  try {
    submitting.value = true
    const roleId = currentRole.value.id
    
    logger.info('Binding users to role', {
      roleId,
      userIds: selectedUserIds.value,
      api: 'POST /api/user/users/roles'
    })

    // 为每个选中的用户更新角色列表（将该角色添加到用户的角色列表中）
    // 接口是全量替换，所以需要为每个用户单独调用
    const updatePromises = selectedUserIds.value.map(userId => {
      return updateUserRoles({
        userId,
        roleIdList: [roleId] // 全量替换为当前角色
      })
    })
    
    await Promise.all(updatePromises)

    ElMessage.success(t('system.role.bindUsersSuccess'))
    showBindUsersDialog.value = false
    submitting.value = false
    selectedUserIds.value = []

    // 重新加载角色列表（更新用户数量）
    await loadRoles()
  } catch (error) {
    submitting.value = false
    logger.error('Failed to bind users to role', error)
    ElMessage.error(error.response?.data?.message || t('system.role.bindUsersFailed'))
  }
}

const handleBindUsersDialogClose = () => {
  currentRole.value = null
  availableUserList.value = []
  userSearchKeyword.value = ''
  selectedUserIds.value = []
  availableUserPagination.current = 1
  availableUserPagination.total = 0
}

// 查看角色用户相关方法
const handleViewRoleUsers = async (role) => {
  currentRole.value = role
  showViewUsersDialog.value = true
  roleUserPagination.current = 1
  roleUserSearchKeyword.value = ''
  await loadRoleUsers()
}

const loadRoleUsers = async () => {
  try {
    roleUserLoading.value = true
    logger.info('Loading role users', { roleId: currentRole.value?.id })

    const requestData = {
      pageNo: roleUserPagination.current,
      pageSize: roleUserPagination.size,
      sortBy: 'createTime',
      sortOrder: 'DESC'
    }

    // 如果有搜索关键词，添加到请求参数
    if (roleUserSearchKeyword.value) {
      requestData.keyword = roleUserSearchKeyword.value
    }

    const response = await getRoleUsers(currentRole.value.id, requestData)
    const data = response?.data?.data || response?.data || {}

    roleUserList.value = (data.records || []).map((user) => ({
      id: user.id,
      username: user.username,
      realName: user.realName,
      email: user.email,
      phone: user.phone,
      departmentId: user.departmentId,
      departmentName: user.departmentName,
      departmentLabel: user.departmentLabel,
      roleNames: user.roleNames || [],
      status: user.status,
      createTime: user.createTime
    }))

    roleUserPagination.total = data.total || 0
    roleUserPagination.current = data.current || data.pageNo || roleUserPagination.current
    roleUserPagination.size = data.size || data.pageSize || roleUserPagination.size

    logger.info('Role users loaded successfully', { count: roleUserList.value.length })
  } catch (error) {
    logger.error('Failed to load role users', error)
    ElMessage.error(error.response?.data?.message || t('system.role.loadRoleUsersError'))
    roleUserList.value = []
    roleUserPagination.total = 0
  } finally {
    roleUserLoading.value = false
  }
}

const handleSearchRoleUsers = () => {
  roleUserPagination.current = 1
  loadRoleUsers()
}

const handleRoleUserPageChange = (page) => {
  roleUserPagination.current = page
  loadRoleUsers()
}

const handleRoleUserPageSizeChange = (size) => {
  roleUserPagination.size = size
  roleUserPagination.current = 1
  loadRoleUsers()
}

const handleViewUsersDialogClose = () => {
  currentRole.value = null
  roleUserList.value = []
  roleUserSearchKeyword.value = ''
  roleUserPagination.current = 1
  roleUserPagination.total = 0
}

const handleAvailableUserPageChange = (page) => {
  availableUserPagination.current = page
  loadAvailableUsers()
}

const handleAvailableUserPageSizeChange = (size) => {
  availableUserPagination.size = size
  availableUserPagination.current = 1
  loadAvailableUsers()
}

const resetRoleForm = () => {
  Object.assign(roleFormData, {
    id: null,
    name: '',
    code: '',
    description: '',
    sortOrder: 0
  })
}

// 展开所有节点 - 优化性能：分批处理避免卡顿
const handleExpandAll = () => {
  if (!permissionTreeRef.value || !permissionTreeRef.value.store) {
    logger.warn('Permission tree ref or store is not available')
    return
  }
  
  try {
    const store = permissionTreeRef.value.store
    const nodesMap = store.nodesMap
    
    // 先收集所有需要展开的节点ID（扁平化收集，避免递归时的性能问题）
    const nodeIdsToExpand = []
    const collectExpandableNodes = (nodes) => {
      if (!nodes || !Array.isArray(nodes)) return
      nodes.forEach((node) => {
        if (node.children && node.children.length > 0) {
          nodeIdsToExpand.push(node.id)
          collectExpandableNodes(node.children)
        }
      })
    }
    
    collectExpandableNodes(filteredPermissionTree.value)
    
    if (nodeIdsToExpand.length === 0) {
      logger.warn('No expandable nodes found')
      return
    }
    
    // 分批处理节点，每批处理50个，避免一次性更新太多导致卡顿
    const BATCH_SIZE = 50
    let currentIndex = 0
    
    const processBatch = () => {
      const endIndex = Math.min(currentIndex + BATCH_SIZE, nodeIdsToExpand.length)
      
      // 批量更新当前批次的节点
      for (let i = currentIndex; i < endIndex; i++) {
        const nodeId = nodeIdsToExpand[i]
        if (nodesMap[nodeId]) {
          nodesMap[nodeId].expanded = true
        }
      }
      
      currentIndex = endIndex
      
      // 如果还有节点需要处理，使用 requestAnimationFrame 继续处理下一批
      if (currentIndex < nodeIdsToExpand.length) {
        requestAnimationFrame(processBatch)
      } else {
        logger.info('Expanded all permission nodes', { 
          totalNodes: nodeIdsToExpand.length
        })
      }
    }
    
    // 开始处理第一批
    requestAnimationFrame(processBatch)
  } catch (error) {
    logger.error('Failed to expand all nodes', error)
    ElMessage.error('展开全部失败，请稍后重试')
  }
}

// 收起所有节点 - 优化性能：分批处理避免卡顿
const handleCollapseAll = () => {
  if (!permissionTreeRef.value || !permissionTreeRef.value.store) {
    logger.warn('Permission tree ref or store is not available')
    return
  }
  
  try {
    const store = permissionTreeRef.value.store
    const nodesMap = store.nodesMap
    
    // 先收集所有需要收起的节点ID（扁平化收集）
    const nodeIdsToCollapse = []
    const collectCollapsibleNodes = (nodes) => {
      if (!nodes || !Array.isArray(nodes)) return
      nodes.forEach((node) => {
        if (node.children && node.children.length > 0) {
          nodeIdsToCollapse.push(node.id)
          collectCollapsibleNodes(node.children)
        }
      })
    }
    
    collectCollapsibleNodes(filteredPermissionTree.value)
    
    if (nodeIdsToCollapse.length === 0) {
      logger.info('No collapsible nodes found')
      return
    }
    
    // 分批处理节点，每批处理50个，避免一次性更新太多导致卡顿
    const BATCH_SIZE = 50
    let currentIndex = 0
    
    const processBatch = () => {
      const endIndex = Math.min(currentIndex + BATCH_SIZE, nodeIdsToCollapse.length)
      
      // 批量更新当前批次的节点
      for (let i = currentIndex; i < endIndex; i++) {
        const nodeId = nodeIdsToCollapse[i]
        if (nodesMap[nodeId]) {
          nodesMap[nodeId].expanded = false
        }
      }
      
      currentIndex = endIndex
      
      // 如果还有节点需要处理，使用 requestAnimationFrame 继续处理下一批
      if (currentIndex < nodeIdsToCollapse.length) {
        requestAnimationFrame(processBatch)
      } else {
        logger.info('Collapsed all permission nodes', {
          totalNodes: nodeIdsToCollapse.length
        })
      }
    }
    
    // 开始处理第一批
    requestAnimationFrame(processBatch)
  } catch (error) {
    logger.error('Failed to collapse all nodes', error)
    ElMessage.error('收起全部失败，请稍后重试')
  }
}


const handleSelectAll = () => {
  const allKeys = []
  const getAllKeys = (nodes) => {
    nodes.forEach((node) => {
      if (!node.children) {
        allKeys.push(node.id)
      } else {
        getAllKeys(node.children)
      }
    })
  }
  getAllKeys(permissionTree.value)
  permissionTreeRef.value?.setCheckedKeys(allKeys)
}

const handleClearAll = () => {
  permissionTreeRef.value?.setCheckedKeys([])
}

const filterNode = (value, data) => {
  if (!value) return true
  const searchValue = value.toLowerCase()
  return (
    data.label?.toLowerCase().includes(searchValue) ||
    data.code?.toLowerCase().includes(searchValue)
  )
}

// 获取权限图标
const getPermissionIcon = (data) => {
  // 根据权限类型返回现代化的图标
  if (data.children && data.children.length > 0) {
    return Folder // 有子节点的使用文件夹图标
  }
  // 根据权限代码判断图标类型
  if (data.code?.startsWith('menu:')) {
    return Menu // 菜单项使用菜单图标
  } else if (data.code?.startsWith('button:') || data.code?.startsWith('btn:')) {
    return Setting // 按钮权限使用设置图标
  }
  return Document // 默认使用文档图标
}

const handlePermissionSearch = () => {
  permissionTreeRef.value?.filter(permissionSearchText.value)
}

const handlePageChange = (page) => {
  pagination.current = page
  loadRoles()
}

const handlePageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadRoles()
}

// 监听权限搜索文本变化
watch(permissionSearchText, (val) => {
  permissionTreeRef.value?.filter(val)
})

// 生命周期
onMounted(() => {
  loadRoles()
  // 权限树改为按需加载，在打开权限配置对话框时再加载
  // loadPermissionTree()
})
</script>

<style lang="scss" scoped>
.role-management-container {
  padding: var(--gap-lg);
  background: var(--bg);
  min-height: calc(100vh - 56px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gap-lg);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: var(--gap-md);
}

.content-card {
  margin-top: var(--gap-lg);
}

.filter-section {
  display: flex;
  gap: var(--gap-md);
  margin-bottom: var(--gap-lg);
  flex-wrap: wrap;
  align-items: center;
}

.permission-config {
  // 当前角色信息
  .permission-role-info {
    margin-bottom: var(--gap-lg);
    font-size: var(--font-size-base);
    color: var(--text-1);

    strong {
      color: var(--color-primary);
      font-weight: 600;
    }

    .role-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }

  // 权限列表标题
  .permission-list-title {
    color: var(--color-primary) !important;
  }

  // 权限树容器
  .permission-tree-container {
    border: 1px solid var(--border-color);
    border-radius: var(--border-radius);
    padding: var(--gap-md);
    background: var(--bg-secondary);
    max-height: 500px;
    display: flex;
    flex-direction: column;
  }

  // 权限列表标题和操作按钮
  .permission-tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--gap-md);
    padding-bottom: var(--gap-md);
    border-bottom: 1px solid var(--border-color);

    .permission-list-title {
      font-size: var(--font-size-base);
      font-weight: 600;
      color: var(--text-1);
    }

    .permission-actions {
      display: flex;
      gap: var(--gap-sm);
      align-items: center;
      
      // 按钮样式 - 使用系统主题色
      :deep(.el-button) {
        color: var(--text-2);
        border-color: var(--border-color);
        background: var(--bg-secondary);
        
        &:hover {
          color: var(--color-primary);
          border-color: var(--color-primary);
          background: var(--bg);
        }
        
        &:active {
          color: var(--color-primary);
          border-color: var(--color-primary);
          background: var(--bg-secondary);
        }
      }
    }
  }

  // 搜索框
  .permission-tree-search {
    margin-bottom: var(--gap-md);
  }

  // 权限树
  .permission-tree {
    flex: 1;
    overflow-y: auto;
    background: transparent;
  }

  // 权限节点
  .permission-node {
    display: flex;
    align-items: center;
    gap: var(--gap-sm);
    flex: 1;
    width: 100%;

    .permission-icon {
      font-size: 18px;
      color: var(--color-primary);
      flex-shrink: 0;
      margin-right: 8px;
      display: flex;
      align-items: center;
    }

    .permission-label {
      font-size: var(--font-size-base);
      color: var(--text-1);
      flex: 1;
    }
  }
}

// 用户数量标签样式
.user-count-tag {
  cursor: default;
  transition: all 0.2s;

  &.clickable {
    cursor: pointer;

    &:hover {
      opacity: 0.8;
      transform: scale(1.05);
    }
  }
}

// 查看用户对话框样式
.view-users-container {
  // 当前角色信息
  .view-users-role-info {
    margin-bottom: var(--gap-lg);
    font-size: var(--font-size-base);
    color: var(--text-1);

    strong {
      color: var(--color-primary);
      font-weight: 600;
    }

    .role-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }

  // 搜索区域
  .view-users-search {
    display: flex;
    gap: var(--gap-md);
    margin-bottom: var(--gap-lg);
    align-items: center;
  }

  // 用户表格
  .view-users-table {
    width: 100%;
  }
}

// 角色表格样式 - 减小列间距
.role-table {
  :deep(.el-table__cell) {
    padding: 12px 8px !important; // 减小列间距
  }
  
  :deep(.el-table__header .el-table__cell) {
    padding: 12px 8px !important;
  }
}

:deep(.el-table) {
  border-radius: var(--border-radius);
  overflow: hidden;
}

:deep(.el-tag) {
  border-radius: var(--border-radius-sm);
  font-weight: 500;
}

:deep(.el-dialog) {
  border-radius: var(--border-radius-lg);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid var(--border-color);
  padding: var(--gap-lg) var(--gap-xl) var(--gap-md);
}

:deep(.el-dialog__title) {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-primary);
}

:deep(.el-dialog__body) {
  padding: var(--gap-lg) var(--gap-xl);
}

:deep(.el-dialog__footer) {
  border-top: 1px solid var(--border-color);
  padding: var(--gap-md) var(--gap-xl) var(--gap-lg);
}

:deep(.el-tree) {
  background: transparent !important;
}

:deep(.el-tree-node__content) {
  height: 32px;
  line-height: 32px;
  padding: 0 8px !important;
  border-radius: 4px;
  transition: background-color 0.2s;
}

:deep(.el-tree-node__content:hover) {
  background: var(--hover) !important;
}

:deep(.el-tree-node__label) {
  font-size: var(--font-size-base);
  color: var(--text-1);
  width: 100%;
}

// 树形选择框选中的对勾颜色
:deep(.el-tree .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

:deep(.el-tree .el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

:deep(.el-checkbox) {
  margin-right: var(--gap-sm);
}

.bind-users-container {
  // 当前角色信息
  .bind-users-role-info {
    margin-bottom: var(--gap-lg);
    font-size: var(--font-size-base);
    color: var(--text-1);

    strong {
      color: var(--color-primary);
      font-weight: 600;
    }

    .role-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }

  // 搜索区域
  .bind-users-search {
    display: flex;
    gap: var(--gap-md);
    margin-bottom: var(--gap-lg);
    align-items: center;
  }

  // 用户表格 - 居中显示
  .bind-users-table {
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

// 绑定用户对话框底部
.bind-users-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .selected-count {
    color: var(--text-2);
    font-size: var(--font-size-sm);
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

// 操作按钮样式 - 与 UserManagement.vue 保持一致
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
  
  &.btn-info {
    color: var(--text-3);
    border-color: var(--text-3);
    
    &:hover:not(:disabled) {
      background: var(--text-3);
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
</style>

