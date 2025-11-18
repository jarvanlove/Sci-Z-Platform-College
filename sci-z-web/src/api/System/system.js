import request from '@/utils/request'
import { SYSTEM_API, HTTP_METHODS } from '../Common/constants'

/**
 * 系统管理模块 API 接口
 *
 */

/**
 * 获取行业配置
 * @returns {Promise} 行业配置响应
 */
export const getIndustryConfig = () => {
  return request({
    url: SYSTEM_API.INDUSTRY_CONFIG,
    method: HTTP_METHODS.GET
  })
}

/**
 * 获取Dify配置
 * @returns {Promise} Dify配置响应
 */
export const getDifyConfig = () => {
  return request({
    url: SYSTEM_API.DIFY_CONFIG,
    method: HTTP_METHODS.GET
  })
}

/**
 * 保存行业配置
 * @param {Object} data - 行业配置数据
 * @returns {Promise} 保存响应
 */
export const saveIndustryConfig = (data) => {
  return request({
    url: SYSTEM_API.SAVE_INDUSTRY_CONFIG,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 保存Dify配置
 * @param {Object} data - Dify配置数据
 * @returns {Promise} 保存响应
 */
export const saveDifyConfig = (data) => {
  return request({
    url: SYSTEM_API.SAVE_DIFY_CONFIG,
    method: HTTP_METHODS.PUT,
    data
  })
}



/**
 * 分页查询角色列表
 * @param {Object} data - 查询参数
 * @param {number} data.pageNo - 页码
 * @param {number} data.pageSize - 每页数量
 * @param {string} data.sortBy - 排序字段
 * @param {string} data.sortOrder - 排序方向 (ASC/DESC)
 * @param {string} [data.keyword] - 关键词搜索（可选）
 * @param {number} [data.status] - 状态（可选，1=正常，0=禁用）
 * @returns {Promise} 角色列表响应
 */
export const getRoles = (data) => {
  return request({
    url: SYSTEM_API.ROLES_LIST,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 创建角色
 * @param {Object} data - 角色数据
 * @param {string} data.roleName - 角色名称（必填，最大长度50个字符）
 * @param {string} data.roleCode - 角色编码（必填，最大长度50个字符，唯一）
 * @param {string} [data.description] - 角色描述（可选，最大长度200个字符）
 * @param {number} [data.sortOrder] - 排序值（可选，默认0）
 * @returns {Promise} 创建角色响应
 */
export const createRole = (data) => {
  return request({
    url: SYSTEM_API.ROLES,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新角色
 * @param {number} id - 角色ID（路径参数）
 * @param {Object} data - 角色数据
 * @param {number} data.id - 角色ID（必填，必须与路径参数一致）
 * @param {string} data.roleName - 角色名称（必填，最大长度50个字符）
 * @param {string} [data.description] - 角色描述（可选，最大长度200个字符）
 * @param {number} [data.sortOrder] - 排序值（可选，默认0）
 * @param {number} [data.status] - 角色状态（可选，1=启用，0=禁用）
 * @returns {Promise} 更新角色响应
 */
export const updateRole = (id, data) => {
  return request({
    url: SYSTEM_API.ROLE_DETAIL(id),
    method: HTTP_METHODS.PUT,
    data: {
      ...data,
      id // 确保id字段与路径参数一致
    }
  })
}

/**
 * 删除角色
 * @param {number} id - 角色ID
 * @returns {Promise} 删除角色响应
 */
export const deleteRole = (id) => {
  return request({
    url: SYSTEM_API.ROLE_DETAIL(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 获取权限树
 * @returns {Promise} 权限树响应
 */
export const getPermissionsTree = () => {
  return request({
    url: SYSTEM_API.PERMISSIONS_TREE,
    method: HTTP_METHODS.GET
  })
}

/**
 * 更新角色权限
 * @param {number} roleId - 角色ID
 * @param {Array<number>} permissionIdList - 权限ID列表
 * @returns {Promise} 更新权限响应
 */
export const updateRolePermissions = (roleId, permissionIdList) => {
  return request({
    url: SYSTEM_API.ROLE_PERMISSIONS_UPDATE,
    method: HTTP_METHODS.POST,
    data: {
      roleId,
      permissionIdList
    }
  })
}

/**
 * 获取角色权限ID集合（用于回显）
 * @param {number} roleId - 角色ID（路径参数）
 * @returns {Promise} 权限ID集合响应 { permissionIdList: number[] }
 */
export const getRolePermissions = (roleId) => {
  return request({
    url: SYSTEM_API.ROLE_PERMISSIONS(roleId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 分页查询指定角色下的所有用户（用于点击用户数量时显示）
 * @param {number} roleId - 角色ID（路径参数）
 * @param {Object} data - 查询参数（请求体）
 * @param {number} [data.pageNo] - 页码，从1开始，默认1，最小值为1
 * @param {number} [data.pageSize] - 每页数量，默认10，最小值为1
 * @param {string} [data.sortBy] - 排序字段（可选，如"createTime"）
 * @param {string} [data.sortOrder] - 排序方式，支持"ASC"或"DESC"，默认"DESC"（可选）
 * @param {string} [data.keyword] - 搜索关键字（可选），支持用户名/真实姓名/邮箱/手机号
 * @param {number} [data.roleId] - 角色ID（可选，此接口中会被路径参数覆盖，可传null）
 * @param {number} [data.status] - 用户状态（可选），1=正常，0=禁用，null=全部
 * @returns {Promise} 用户列表响应
 */
export const getRoleUsers = (roleId, data = {}) => {
  return request({
    url: SYSTEM_API.ROLE_USERS(roleId),
    method: HTTP_METHODS.POST,
    data: {
      pageNo: 1,
      pageSize: 10,
      sortBy: 'createTime',
      sortOrder: 'DESC',
      ...data,
      roleId // 自动设置roleId，即使传入也会被覆盖
    }
  })
}

/**
 * 获取角色绑定的用户ID集合（用于回显）
 * @param {number} roleId - 角色ID（路径参数）
 * @returns {Promise} 用户ID集合响应 { userIdList: number[] }
 */
export const getRoleUserIds = (roleId) => {
  return request({
    url: SYSTEM_API.ROLE_USER_IDS(roleId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 更新用户角色（全量替换用户在当前行业下的角色集合）
 * @param {Object} data - 更新数据
 * @param {number} data.userId - 用户ID（必填，不能为空）
 * @param {Array<number>} data.roleIdList - 角色ID集合（必填，不能为空）
 * @returns {Promise} 更新响应（data为null）
 */
export const updateUserRoles = (data) => {
  return request({
    url: SYSTEM_API.USER_ROLES_UPDATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取用户已绑定的角色ID集合（用于回显）
 * @param {number} userId - 用户ID
 * @returns {Promise} 用户角色ID集合响应
 */
export const getUserRoles = (userId) => {
  return request({
    url: SYSTEM_API.USER_ROLES(userId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 从角色解绑用户
 * @param {number} roleId - 角色ID
 * @param {number} userId - 用户ID
 * @returns {Promise} 解绑响应
 */
export const unbindUserFromRole = (roleId, userId) => {
  return request({
    url: `${SYSTEM_API.ROLE_BIND_USERS(roleId)}/${userId}`,
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 创建权限
 * @param {Object} data - 权限数据
 * @param {string} data.name - 权限名称
 * @param {string} data.code - 权限编码
 * @param {string} data.type - 权限类型
 * @param {number} data.parentId - 父权限ID
 * @param {string} data.path - 权限路径
 * @param {string} data.icon - 图标
 * @param {number} data.sort - 排序
 * @returns {Promise} 创建权限响应
 */
export const createPermission = (data) => {
  return request({
    url: SYSTEM_API.PERMISSIONS,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取用户管理列表
 * @param {Object} data - 查询参数
 * @param {number} data.pageNo - 页码
 * @param {number} data.pageSize - 每页数量
 * @param {string} data.sortBy - 排序字段
 * @param {string} data.sortOrder - 排序方向 (ASC/DESC)
 * @param {string} data.keyword - 关键词搜索（可选）
 * @param {number} data.roleId - 角色ID（可选）
 * @param {number} data.status - 状态（可选，1=正常，0=禁用）
 * @returns {Promise} 用户列表响应
 */
export const getUsers = (data) => {
  return request({
    url: SYSTEM_API.USER_LIST,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @param {string} data.username - 用户名
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @param {string} data.realName - 真实姓名
 * @param {string} data.password - 密码
 * @param {number} data.departmentId - 部门ID
 * @returns {Promise} 创建用户响应
 */
export const createUser = (data) => {
  return request({
    url: SYSTEM_API.USER_CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新用户
 * @param {number} id - 用户ID（路径参数）
 * @param {Object} data - 用户数据
 * @param {number} data.id - 用户ID（必须与路径参数中的id一致）
 * @param {string} data.realName - 真实姓名（必填，最大长度50字符）
 * @param {string} data.email - 邮箱（必填，必须是有效邮箱格式，最大长度100字符）
 * @param {string} data.phone - 手机号（必填，必须是大陆11位手机号，1开头）
 * @param {number} data.departmentId - 部门ID（必填）
 * @returns {Promise} 更新用户响应
 */
export const updateUser = (id, data) => {
  return request({
    url: SYSTEM_API.USER_DETAIL(id),
    method: HTTP_METHODS.PUT,
    data: {
      ...data,
      id // 确保请求体中包含id，且与路径参数一致
    }
  })
}

/**
 * 删除用户（软删除）
 * @param {number} id - 用户ID（路径参数）
 * @returns {Promise} 删除用户响应
 */
export const deleteUser = (id) => {
  return request({
    url: SYSTEM_API.USER_DETAIL(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 禁用/启用用户
 * @param {number} id - 用户ID（路径参数）
 * @param {boolean} disabled - true=禁用, false=启用
 * @returns {Promise} 操作响应
 */
export const updateUserStatus = (id, disabled) => {
  return request({
    url: SYSTEM_API.USER_STATUS(id),
    method: HTTP_METHODS.PUT,
    params: {
      disabled
    }
  })
}

/**
 * 管理员重置用户密码
 * @param {Object} data - 重置密码数据
 * @param {number} data.userId - 用户ID
 * @param {string} data.newPassword - 新密码（必填，长度6-32个字符）
 * @returns {Promise} 重置密码响应
 */
export const resetUserPassword = (data) => {
  return request({
    url: SYSTEM_API.RESET_PASSWORD,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 获取操作日志列表
 * @param {Object} data - 查询参数
 * @param {number} data.pageNo - 页码
 * @param {number} data.pageSize - 每页数量
 * @param {string} [data.level] - 日志级别
 * @param {string} [data.user] - 操作用户
 * @param {string} [data.startTime] - 开始时间
 * @param {string} [data.endTime] - 结束时间
 * @returns {Promise} 日志列表响应
 */
export const getOperationLogs = (data) => {
  return request({
    url: SYSTEM_API.OPERATION_LOGS,
    method: HTTP_METHODS.POST,
    data
  })
}