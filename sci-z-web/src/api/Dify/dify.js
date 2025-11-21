import request from '@/utils/request'
import { HTTP_METHODS } from '../Common/constants'

/**
 * Dify API 密钥管理接口
 */

const DIFY_API_BASE = '/dify/keys'

/**
 * 获取 API Key 列表
 * @param {Object} params - 查询参数
 * @returns {Promise} API Key 列表响应
 */
export const getApiKeyList = (params) => {
  return request({
    url: `${DIFY_API_BASE}/list`,
    method: HTTP_METHODS.POST,
    data: params || {}
  })
}

/**
 * 获取 API Key 详情
 * @param {Number} id - API Key ID
 * @returns {Promise} API Key 详情响应
 */
export const getApiKeyDetail = (id) => {
  return request({
    url: `${DIFY_API_BASE}/get/${id}`,
    method: HTTP_METHODS.GET
  })
}

/**
 * 创建 API Key
 * @param {Object} data - API Key 数据
 * @returns {Promise} 创建响应
 */
export const createApiKey = (data) => {
  return request({
    url: `${DIFY_API_BASE}/save`,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新 API Key
 * @param {Object} data - API Key 数据
 * @returns {Promise} 更新响应
 */
export const updateApiKey = (data) => {
  return request({
    url: `${DIFY_API_BASE}/update`,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除 API Key
 * @param {Number} id - API Key ID
 * @returns {Promise} 删除响应
 */
export const deleteApiKey = (id) => {
  return request({
    url: `${DIFY_API_BASE}/delete/${id}`,
    method: HTTP_METHODS.DELETE
  })
}

