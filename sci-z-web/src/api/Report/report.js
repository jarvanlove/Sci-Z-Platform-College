import request from '@/utils/request'
import { REPORT_API, HTTP_METHODS } from '../Common/constants'

/**
 * 报告模块 API 接口
 *
 */

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