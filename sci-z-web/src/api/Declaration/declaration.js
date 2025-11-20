import request from '@/utils/request'
import { DECLARATION_API, HTTP_METHODS } from '../Common/constants'

/**
 * 申报模块 API 接口
 *
 */

/**
 * 获取申报列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @param {string} params.status - 状态筛选
 * @param {number} params.applicantId - 申报人ID
 * @param {number} params.departmentId - 部门ID
 * @param {string} params.type - 申报类型
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @returns {Promise} 申报列表响应
 */
export const getDeclarationList = (params) => {
  return request({
    url: DECLARATION_API.LIST,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 获取申报详情
 * @param {number} id - 申报ID
 * @returns {Promise} 申报详情响应
 */
export const getDeclarationDetail = (id) => {
  return request({
    url: DECLARATION_API.DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 创建申报
 * @param {Object} data - 申报数据
 * @param {string} data.title - 申报标题
 * @param {string} data.description - 申报描述
 * @param {string} data.type - 申报类型
 * @param {number} data.budget - 预算金额
 * @param {string} data.startDate - 开始日期
 * @param {string} data.endDate - 结束日期
 * @param {Array} data.attachments - 附件列表
 * @returns {Promise} 创建申报响应
 */
export const createDeclaration = (data) => {
  return request({
    url: DECLARATION_API.CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新申报
 * @param {number} id - 申报ID
 * @param {Object} data - 申报数据
 * @returns {Promise} 更新申报响应
 */
export const updateDeclaration = (id, data) => {
  return request({
    url: DECLARATION_API.UPDATE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除申报
 * @param {number} id - 申报ID
 * @returns {Promise} 删除申报响应
 */
export const deleteDeclaration = (id) => {
  return request({
    url: DECLARATION_API.DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 提交申报
 * @param {number} id - 申报ID
 * @returns {Promise} 提交申报响应
 */
export const submitDeclaration = (id) => {
  return request({
    url: DECLARATION_API.SUBMIT(id),
    method: HTTP_METHODS.POST
  })
}

/**
 * 更新申报状态
 * @param {Object} params - 更新参数
 * @param {number} params.id - 申报ID
 * @param {string} params.status - 新状态 (submitting/success/failed)
 * @returns {Promise} 更新状态响应
 */
export const updateDeclarationStatus = (params) => {
  return request({
    url: DECLARATION_API.UPDATE(params.id),
    method: HTTP_METHODS.PUT,
    data: { status: params.status }
  })
}

/**
 * 下载申报文档
 * @param {Object} params - 下载参数
 * @param {number} params.id - 申报ID
 * @param {string} params.format - 下载格式 (pdf/word/markdown)
 * @returns {Promise} 下载响应（Blob）
 */
export const downloadDeclaration = (params) => {
  return request({
    url: `${DECLARATION_API.DETAIL(params.id)}/download`,
    method: HTTP_METHODS.GET,
    params: { format: params.format },
    responseType: 'blob'
  })
}

/**
 * 获取申报预览内容
 * @param {Object} params - 预览参数
 * @param {number} params.id - 申报ID
 * @returns {Promise} 预览内容响应
 */
export const getDeclarationPreview = (params) => {
  return request({
    url: `${DECLARATION_API.DETAIL(params.id)}/preview`,
    method: HTTP_METHODS.GET
  })
}