import request from '@/utils/request'
import { LITERATURE_API, HTTP_METHODS } from '../Common/constants'

/**
 * 文献搜索模块 API 接口
 */

/**
 * 搜索文献
 * @param {Object} params - 查询参数
 * @param {string} params.search - 搜索关键词
 * @param {string} [params.publicationYearFilter] - 发表年份过滤（如：2023-2025）
 * @param {string} [params.dataSource] - 数据源（如：openalex）
 * @param {number} [params.page=1] - 页码，默认1
 * @param {number} [params.perPage=10] - 每页数量，默认10
 * @returns {Promise} 文献搜索结果响应
 */
export const searchLiterature = (params = {}) => {
  return request({
    url: LITERATURE_API.SEARCH,
    method: HTTP_METHODS.GET,
    params: {
      search: params.search || '',
      publicationYearFilter: params.publicationYearFilter || '',
      dataSource: params.dataSource || 'openalex',
      page: params.page || 1,
      perPage: params.perPage || 10
    }
  })
}

/**
 * 获取文献详情
 * @param {string|number} id - 文献ID
 * @param {string} [dataSource='openalex'] - 数据源
 * @returns {Promise} 文献详情响应
 */
export const getLiteratureDetail = (id, dataSource = 'openalex') => {
  return request({
    url: LITERATURE_API.DETAIL,
    method: HTTP_METHODS.GET,
    params: {
      id: id,
      dataSource: dataSource
    }
  })
}

/**
 * 下载文献
 * @param {string|number} id - 文献ID
 * @returns {Promise} 文献下载响应
 */
export const downloadLiterature = (id) => {
  return request({
    url: LITERATURE_API.DOWNLOAD(id),
    method: HTTP_METHODS.GET,
    responseType: 'blob'
  })
}

