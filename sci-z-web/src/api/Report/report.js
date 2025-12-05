import request from '@/utils/request'
import { REPORT_API, HTTP_METHODS } from '../Common/constants'

/**
 * 报告模块 API 接口
 * 
 * 注意：报告管理接口使用新的路径 /api/report-management
 * 原有的报告接口路径为 /api/report（保留兼容）
 */

// ==================== 报告管理接口（新接口） ====================

/**
 * 分页查询报告管理列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码，从 1 开始，默认 1
 * @param {number} params.pageSize - 每页数量，默认 10
 * @param {string} params.sortBy - 排序字段
 * @param {string} params.sortOrder - 排序方式：ASC 或 DESC，默认 DESC
 * @param {string} params.keyword - 搜索关键字（报告编号/项目名称/创建人）
 * @param {string} params.status - 报告状态（null表示全部）
 * @param {string} params.reportType - 报告类型：tech（技术报告）、self（自评报告），null表示全部
 * @returns {Promise} 报告列表响应
 */
export const getReportManagementList = (params) => {
  return request({
    url: '/report-management',
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 获取报告管理详情
 * @param {number} id - 报告ID
 * @returns {Promise} 报告详情响应
 */
export const getReportManagementDetail = (id) => {
  return request({
    url: `/report-management/${id}`,
    method: HTTP_METHODS.GET
  })
}

/**
 * 创建报告管理
 * @param {Object} data - 报告数据
 * @param {number} data.projectId - 项目ID
 * @param {string} data.projectName - 项目名称
 * @param {string} data.projectCode - 项目编号
 * @param {string} data.projectKnowledgeId - 项目知识库ID
 * @param {string} data.reportType - 报告类型：tech（技术报告）、self（自评报告）
 * @param {string} data.summary - 报告摘要
 * @returns {Promise} 创建报告响应（返回报告ID）
 */
export const createReportManagement = (data) => {
  return request({
    url: '/report-management',
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新报告管理
 * @param {Object} data - 报告数据
 * @param {number} data.id - 报告ID（必填）
 * @param {string} data.projectName - 项目名称
 * @param {string} data.projectCode - 项目编号
 * @param {string} data.projectKnowledgeId - 项目知识库ID
 * @param {string} data.reportType - 报告类型
 * @param {string} data.summary - 报告摘要
 * @param {string} data.status - 状态
 * @returns {Promise} 更新报告响应
 */
export const updateReportManagement = (data) => {
  return request({
    url: '/report-management',
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除报告管理
 * @param {number} id - 报告ID
 * @returns {Promise} 删除报告响应
 */
export const deleteReportManagement = (id) => {
  return request({
    url: `/report-management/${id}`,
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 获取报告类型列表
 * 查询 key_type = 'workflow' 且 key_name 包含"报告"的密钥列表
 * @returns {Promise} 报告类型列表响应
 */
export const getReportTypes = () => {
  return request({
    url: '/report-management/types',
    method: HTTP_METHODS.GET
  })
}

/**
 * 获取报告工作流列表
 * 获取可用于报告生成的工作流列表
 * @returns {Promise} 工作流列表响应
 */
export const getReportWorkflows = () => {
  return request({
    url: '/report-management/workflow',
    method: HTTP_METHODS.GET
  })
}

// ==================== 原有报告接口（保留兼容） ====================

/**
 * 获取报告列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @param {string} params.type - 报告类型
 * @param {string} params.status - 状态筛选
 * @param {number} params.projectId - 项目ID
 * @param {number} params.authorId - 作者ID
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @returns {Promise} 报告列表响应
 */
export const getReportList = (params) => {
  return request({
    url: REPORT_API.LIST,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 创建报告
 * @param {Object} data - 报告数据
 * @param {string} data.title - 报告标题
 * @param {string} data.type - 报告类型
 * @param {number} data.projectId - 项目ID
 * @param {string} data.content - 报告内容
 * @param {Array} data.attachments - 附件列表
 * @returns {Promise} 创建报告响应
 */
export const createReport = (data) => {
  return request({
    url: REPORT_API.CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取报告详情
 * @param {number} id - 报告ID
 * @returns {Promise} 报告详情响应
 */
export const getReportDetail = (id) => {
  return request({
    url: REPORT_API.DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 开始生成报告
 * @param {Object} data - 生成参数
 * @param {number} data.projectId - 项目ID
 * @param {string} data.type - 报告类型
 * @param {number} data.templateId - 模板ID
 * @param {Object} data.parameters - 生成参数
 * @returns {Promise} 生成报告响应
 */
export const generateReport = (data) => {
  return request({
    url: REPORT_API.GENERATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 查询报告生成状态
 * @param {number} reportId - 报告ID
 * @returns {Promise} 生成状态响应
 */
export const getReportStatus = (reportId) => {
  return request({
    url: REPORT_API.STATUS(reportId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 导出报告
 * @param {Object} data - 导出参数
 * @param {number} data.reportId - 报告ID
 * @param {string} data.format - 导出格式
 * @returns {Promise} 导出报告响应
 */
export const exportReport = (data) => {
  return request({
    url: REPORT_API.EXPORT,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取报告导出文件列表
 * @param {number} reportId - 报告ID
 * @returns {Promise} 导出文件列表响应
 */
export const getReportExports = (reportId) => {
  return request({
    url: REPORT_API.EXPORTS(reportId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 重新生成报告
 * @param {Object} data - 重新生成参数
 * @param {number} data.reportId - 报告ID
 * @param {Object} data.parameters - 生成参数
 * @returns {Promise} 重新生成报告响应
 */
export const regenerateReport = (data) => {
  return request({
    url: REPORT_API.REGENERATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 删除报告
 * @param {number} id - 报告ID
 * @returns {Promise} 删除报告响应
 */
export const deleteReport = (id) => {
  return request({
    url: REPORT_API.DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}