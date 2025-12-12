import request from '@/utils/request'
import { PROJECT_API, HTTP_METHODS } from '../Common/constants'

/**
 * 项目模块 API 接口
 *
 */

/**
 * 获取项目列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码（从1开始）
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.keyword] - 关键词搜索（可选）
 * @param {string} [params.status] - 状态筛选（可选，如 "1"）
 * @param {string} [params.startTime] - 项目开始时间（可选，LocalDate 格式：YYYY-MM-DD，查询开始时间 >= startTime 的项目）
 * @param {string} [params.endTime] - 项目结束时间（可选，LocalDate 格式：YYYY-MM-DD，查询结束时间 <= endTime 的项目）
 * @param {string} [params.sortBy] - 排序字段（可选，如 "createdTime"）
 * @param {string} [params.sortOrder] - 排序方向（可选，如 "DESC"）
 * @returns {Promise} 项目列表响应
 */
export const getProjectList = (params) => {
  return request({
    url: PROJECT_API.LIST,
    method: HTTP_METHODS.POST,
    data: params
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

/**
 * 获取项目统计信息
 * @returns {Promise} 项目统计信息响应
 * 响应格式：{ code: 200, data: { totalProjects: 10, inProgressCount: 5, delayedCount: 2, completedCount: 3 } }
 */
export const getProjectStatistics = () => {
  return request({
    url: PROJECT_API.STATISTICS,
    method: HTTP_METHODS.GET
  })
}

/**
 * 批量上传项目里程碑文档
 * @param {FormData} formData - 文件表单数据
 * @param {File[]} formData.files - 文件对象数组（批量上传）
 * @param {string} formData.relationType - 关联类型（固定值：'project'）
 * @param {string} formData.attachmentType - 附件类型（固定值：'document'）
 * @param {number} formData.relationId - 关联ID（传项目ID，后端会处理）
 * @param {string} formData.relationName - 关联名称（项目编号/里程碑名称）
 * @param {number} formData.isPublic - 是否公开（0:私有, 1:公开，默认0）
 * @returns {Promise} 文件上传响应，data 为文件信息数组
 * 响应格式：{ code: 200, data: [{ id, fileName, originalName, fileType, fileSize, uploaderName, uploadTime, fileUrl, previewUrl }, ...] }
 */
export const uploadMilestoneDocument = (formData) => {
  return request({
    url: PROJECT_API.MILESTONE_DOCUMENT,
    method: HTTP_METHODS.POST,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 完成里程碑
 * @param {number} milestoneId - 里程碑ID
 * @returns {Promise} 完成里程碑响应
 * 响应格式：{ code: 200, message: "操作成功", data: null }
 */
export const completeMilestone = (milestoneId) => {
  return request({
    url: PROJECT_API.MILESTONE_COMPLETE(milestoneId),
    method: HTTP_METHODS.PUT
  })
}

/**
 * 取消完成里程碑
 * @param {number} milestoneId - 里程碑ID
 * @returns {Promise} 取消完成里程碑响应
 * 响应格式：{ code: 200, message: "操作成功", data: null }
 */
export const cancelCompleteMilestone = (milestoneId) => {
  return request({
    url: PROJECT_API.MILESTONE_CANCEL_COMPLETE(milestoneId),
    method: HTTP_METHODS.PUT
  })
}

/**
 * 删除里程碑文档
 * @param {Object} data - 删除数据
 * @param {number} data.attachmentId - 附件ID（必填）
 * @param {number} data.projectId - 项目ID（必填）
 * @param {string} [data.difyDocId] - Dify文档ID（可选，如果为空则跳过Dify删除）
 * @returns {Promise} 删除文档响应
 * 响应格式：{ code: 200, message: "删除成功", data: null }
 */
export const deleteMilestoneDocument = (data) => {
  return request({
    url: PROJECT_API.MILESTONE_DOCUMENT_DELETE,
    method: HTTP_METHODS.DELETE,
    data
  })
}

/**
 * 取消项目
 * @param {number} id - 项目ID
 * @returns {Promise} 取消项目响应
 * 响应格式：{ code: 200, message: "操作成功", data: null }
 */
export const cancelProject = (id) => {
  return request({
    url: PROJECT_API.CANCEL(id),
    method: HTTP_METHODS.PUT
  })
}

/**
 * 获取报告生成可选项目列表
 * @returns {Promise} 项目列表响应
 * 响应格式：{ code: 200, message: "操作成功", data: [{ id, number, name, statusDescription, documentCount, totalWords, progress, totalDownloadCount }] }
 * @property {number} id - 项目ID
 * @property {string} number - 项目编号
 * @property {string} name - 项目名称
 * @property {string} statusDescription - 状态描述（如：进行中、已完成）
 * @property {number} documentCount - 文档数量
 * @property {number} totalWords - 总字数
 * @property {number} progress - 项目进度（0-100）
 * @property {number} totalDownloadCount - 下载总次数
 */
export const getProjectReportSelect = () => {
  return request({
    url: PROJECT_API.REPORT_SELECT,
    method: HTTP_METHODS.GET
  })
}