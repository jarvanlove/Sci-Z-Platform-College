import request from '@/utils/request'
import { FILE_API, HTTP_METHODS } from '../Common/constants'

/**
 * 文件管理模块 API 接口
 *
 */

/**
 * 文件上传
 * @param {FormData} data - 文件数据
 * @param {File} data.file - 文件对象
 * @param {string} data.description - 文件描述
 * @returns {Promise} 文件上传响应
 */
export const uploadFile = (data) => {
  return request({
    url: FILE_API.UPLOAD,
    method: HTTP_METHODS.POST,
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量文件上传
 * @param {FormData} data - 文件数据
 * @param {File[]} data.files - 文件数组
 * @param {string} data.description - 文件描述
 * @returns {Promise} 批量上传响应
 */
export const batchUploadFiles = (data) => {
  return request({
    url: FILE_API.BATCH_UPLOAD,
    method: HTTP_METHODS.POST,
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取文件列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @param {string} params.type - 文件类型
 * @param {number} params.uploaderId - 上传者ID
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @returns {Promise} 文件列表响应
 */
export const getFileList = (params) => {
  return request({
    url: FILE_API.LIST,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 文件下载
 * @param {number} id - 文件ID
 * @returns {Promise} 文件下载响应
 */
export const downloadFile = (id) => {
  return request({
    url: FILE_API.DOWNLOAD(id),
    method: HTTP_METHODS.GET,
    responseType: 'blob'
  })
}

/**
 * 文件预览
 * @param {number} id - 文件ID
 * @returns {Promise} 文件预览响应
 */
export const previewFile = (id) => {
  return request({
    url: FILE_API.PREVIEW(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 删除文件
 * @param {number} id - 文件ID
 * @returns {Promise} 删除文件响应
 */
export const deleteFile = (id) => {
  return request({
    url: FILE_API.DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 文件去重检查
 * @param {Object} data - 检查数据
 * @param {string} data.fileHash - 文件哈希值
 * @param {string} data.fileName - 文件名
 * @param {number} data.fileSize - 文件大小
 * @returns {Promise} 去重检查响应
 */
export const checkFileDuplicate = (data) => {
  return request({
    url: FILE_API.CHECK_DUPLICATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 同步到 Dify 知识库
 * @param {Object} data - 同步数据
 * @param {number} data.fileId - 文件ID
 * @param {number} data.knowledgeId - 知识库ID
 * @param {string} data.action - 同步动作
 * @returns {Promise} 同步响应
 */
export const syncFileToDify = (data) => {
  return request({
    url: FILE_API.SYNC_DIFY,
    method: HTTP_METHODS.POST,
    data
  })
}