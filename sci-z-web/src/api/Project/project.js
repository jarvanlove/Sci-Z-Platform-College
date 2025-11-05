import request from '@/utils/request'
import { PROJECT_API, HTTP_METHODS } from '../Common/constants'

/**
 * 项目模块 API 接口
 *
 */

/**
 * 获取项目列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @param {string} params.status - 状态筛选
 * @param {number} params.leaderId - 负责人ID
 * @param {number} params.departmentId - 部门ID
 * @param {string} params.type - 项目类型
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @returns {Promise} 项目列表响应
 */
export const getProjectList = (params) => {
  return request({
    url: PROJECT_API.LIST,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 获取项目详情
 * @param {number} id - 项目ID
 * @returns {Promise} 项目详情响应
 */
export const getProjectDetail = (id) => {
  return request({
    url: PROJECT_API.DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 创建项目
 * @param {Object} data - 项目数据
 * @param {string} data.name - 项目名称
 * @param {string} data.description - 项目描述
 * @param {string} data.type - 项目类型
 * @param {number} data.budget - 预算金额
 * @param {string} data.startDate - 开始日期
 * @param {string} data.endDate - 结束日期
 * @param {Array} data.members - 项目成员
 * @param {Array} data.attachments - 附件列表
 * @returns {Promise} 创建项目响应
 */
export const createProject = (data) => {
  return request({
    url: PROJECT_API.CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新项目信息
 * @param {number} id - 项目ID
 * @param {Object} data - 项目数据
 * @returns {Promise} 更新项目响应
 */
export const updateProject = (id, data) => {
  return request({
    url: PROJECT_API.UPDATE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 获取项目成员
 * @param {number} id - 项目ID
 * @returns {Promise} 项目成员响应
 */
export const getProjectMembers = (id) => {
  return request({
    url: PROJECT_API.MEMBERS(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 添加项目成员
 * @param {Object} data - 成员数据
 * @param {number} data.projectId - 项目ID
 * @param {number} data.userId - 用户ID
 * @param {string} data.role - 角色
 * @returns {Promise} 添加成员响应
 */
export const addProjectMember = (data) => {
  return request({
    url: PROJECT_API.ADD_MEMBER,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 移除项目成员
 * @param {Object} data - 移除数据
 * @param {number} data.projectId - 项目ID
 * @param {number} data.userId - 用户ID
 * @returns {Promise} 移除成员响应
 */
export const removeProjectMember = (data) => {
  return request({
    url: PROJECT_API.REMOVE_MEMBER,
    method: HTTP_METHODS.DELETE,
    data
  })
}

/**
 * 获取项目进度
 * @param {number} id - 项目ID
 * @returns {Promise} 项目进度响应
 */
export const getProjectProgress = (id) => {
  return request({
    url: PROJECT_API.PROGRESS(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 添加进度记录
 * @param {Object} data - 进度数据
 * @param {number} data.projectId - 项目ID
 * @param {string} data.title - 进度标题
 * @param {string} data.description - 进度描述
 * @param {number} data.progress - 进度百分比
 * @param {Array} data.attachments - 附件列表
 * @returns {Promise} 添加进度响应
 */
export const addProjectProgress = (data) => {
  return request({
    url: PROJECT_API.ADD_PROGRESS,
    method: HTTP_METHODS.POST,
    data
  })
}