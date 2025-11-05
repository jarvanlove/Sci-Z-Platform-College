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
 * 更新行业配置
 * @param {Object} data - 配置数据
 * @returns {Promise} 更新配置响应
 */
export const updateIndustryConfig = (data) => {
  return request({
    url: SYSTEM_API.INDUSTRY_CONFIG,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 获取部门列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @param {number} params.parentId - 父部门ID
 * @returns {Promise} 部门列表响应
 */
export const getDepartments = (params) => {
  return request({
    url: SYSTEM_API.DEPARTMENTS,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 创建部门
 * @param {Object} data - 部门数据
 * @param {string} data.name - 部门名称
 * @param {string} data.code - 部门编码
 * @param {number} data.parentId - 父部门ID
 * @param {number} data.leaderId - 负责人ID
 * @param {string} data.description - 部门描述
 * @returns {Promise} 创建部门响应
 */
export const createDepartment = (data) => {
  return request({
    url: SYSTEM_API.DEPARTMENTS,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新部门
 * @param {number} id - 部门ID
 * @param {Object} data - 部门数据
 * @returns {Promise} 更新部门响应
 */
export const updateDepartment = (id, data) => {
  return request({
    url: SYSTEM_API.DEPARTMENT_DETAIL(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除部门
 * @param {number} id - 部门ID
 * @returns {Promise} 删除部门响应
 */
export const deleteDepartment = (id) => {
  return request({
    url: SYSTEM_API.DEPARTMENT_DETAIL(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 获取角色列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @returns {Promise} 角色列表响应
 */
export const getRoles = (params) => {
  return request({
    url: SYSTEM_API.ROLES,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 创建角色
 * @param {Object} data - 角色数据
 * @param {string} data.name - 角色名称
 * @param {string} data.code - 角色编码
 * @param {string} data.description - 角色描述
 * @param {Array} data.permissions - 权限列表
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
 * @param {number} id - 角色ID
 * @param {Object} data - 角色数据
 * @returns {Promise} 更新角色响应
 */
export const updateRole = (id, data) => {
  return request({
    url: SYSTEM_API.ROLE_DETAIL(id),
    method: HTTP_METHODS.PUT,
    data
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
 * 更新权限
 * @param {number} id - 权限ID
 * @param {Object} data - 权限数据
 * @returns {Promise} 更新权限响应
 */
export const updatePermission = (id, data) => {
  return request({
    url: SYSTEM_API.PERMISSION_DETAIL(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除权限
 * @param {number} id - 权限ID
 * @returns {Promise} 删除权限响应
 */
export const deletePermission = (id) => {
  return request({
    url: SYSTEM_API.PERMISSION_DETAIL(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 获取用户管理列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @param {number} params.departmentId - 部门ID
 * @param {number} params.roleId - 角色ID
 * @param {string} params.status - 状态筛选
 * @returns {Promise} 用户列表响应
 */
export const getUsers = (params) => {
  return request({
    url: SYSTEM_API.USERS,
    method: HTTP_METHODS.GET,
    params
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
 * @param {number} data.roleId - 角色ID
 * @returns {Promise} 创建用户响应
 */
export const createUser = (data) => {
  return request({
    url: SYSTEM_API.USERS,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新用户
 * @param {number} id - 用户ID
 * @param {Object} data - 用户数据
 * @returns {Promise} 更新用户响应
 */
export const updateUser = (id, data) => {
  return request({
    url: SYSTEM_API.USER_DETAIL(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise} 删除用户响应
 */
export const deleteUser = (id) => {
  return request({
    url: SYSTEM_API.USER_DETAIL(id),
    method: HTTP_METHODS.DELETE
  })
}