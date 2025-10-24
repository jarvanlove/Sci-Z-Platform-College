import request from '@/utils/request'
import { KNOWLEDGE_API, HTTP_METHODS } from '../Common/constants'

/**
 * 知识库模块 API 接口
 *
 */

/**
 * 获取知识库列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @param {string} params.type - 知识库类型
 * @param {number} params.ownerId - 所有者ID
 * @param {number} params.departmentId - 部门ID
 * @param {string} params.status - 状态筛选
 * @returns {Promise} 知识库列表响应
 */
export const getKnowledgeList = (params) => {
  return request({
    url: KNOWLEDGE_API.LIST,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 创建知识库
 * @param {Object} data - 知识库数据
 * @param {string} data.name - 知识库名称
 * @param {string} data.description - 知识库描述
 * @param {string} data.type - 知识库类型
 * @param {number} data.departmentId - 部门ID
 * @returns {Promise} 创建知识库响应
 */
export const createKnowledge = (data) => {
  return request({
    url: KNOWLEDGE_API.CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取知识库详情
 * @param {number} id - 知识库ID
 * @returns {Promise} 知识库详情响应
 */
export const getKnowledgeDetail = (id) => {
  return request({
    url: KNOWLEDGE_API.DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 更新知识库
 * @param {number} id - 知识库ID
 * @param {Object} data - 知识库数据
 * @returns {Promise} 更新知识库响应
 */
export const updateKnowledge = (id, data) => {
  return request({
    url: KNOWLEDGE_API.UPDATE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除知识库
 * @param {number} id - 知识库ID
 * @returns {Promise} 删除知识库响应
 */
export const deleteKnowledge = (id) => {
  return request({
    url: KNOWLEDGE_API.DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 获取文件夹树
 * @param {number} knowledgeId - 知识库ID
 * @returns {Promise} 文件夹树响应
 */
export const getKnowledgeFolders = (knowledgeId) => {
  return request({
    url: KNOWLEDGE_API.FOLDERS(knowledgeId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 创建文件夹
 * @param {Object} data - 文件夹数据
 * @param {string} data.name - 文件夹名称
 * @param {number} data.knowledgeId - 知识库ID
 * @param {number} data.parentId - 父文件夹ID
 * @returns {Promise} 创建文件夹响应
 */
export const createKnowledgeFolder = (data) => {
  return request({
    url: KNOWLEDGE_API.CREATE_FOLDER,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新文件夹
 * @param {number} id - 文件夹ID
 * @param {Object} data - 文件夹数据
 * @returns {Promise} 更新文件夹响应
 */
export const updateKnowledgeFolder = (id, data) => {
  return request({
    url: KNOWLEDGE_API.UPDATE_FOLDER(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除文件夹
 * @param {number} id - 文件夹ID
 * @returns {Promise} 删除文件夹响应
 */
export const deleteKnowledgeFolder = (id) => {
  return request({
    url: KNOWLEDGE_API.DELETE_FOLDER(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 上传文件
 * @param {FormData} data - 文件数据
 * @param {File} data.file - 文件对象
 * @param {number} data.knowledgeId - 知识库ID
 * @param {number} data.folderId - 文件夹ID
 * @param {string} data.description - 文件描述
 * @returns {Promise} 上传文件响应
 */
export const uploadKnowledgeFile = (data) => {
  return request({
    url: KNOWLEDGE_API.UPLOAD_FILE,
    method: HTTP_METHODS.POST,
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取文件列表
 * @param {number} knowledgeId - 知识库ID
 * @param {Object} params - 查询参数
 * @param {number} params.folderId - 文件夹ID
 * @param {string} params.keyword - 关键词搜索
 * @param {string} params.type - 文件类型
 * @returns {Promise} 文件列表响应
 */
export const getKnowledgeFiles = (knowledgeId, params) => {
  return request({
    url: KNOWLEDGE_API.FILES(knowledgeId),
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 删除文件
 * @param {number} id - 文件ID
 * @returns {Promise} 删除文件响应
 */
export const deleteKnowledgeFile = (id) => {
  return request({
    url: KNOWLEDGE_API.DELETE_FILE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 重命名文件
 * @param {number} id - 文件ID
 * @param {Object} data - 重命名数据
 * @param {string} data.name - 新文件名
 * @returns {Promise} 重命名文件响应
 */
export const renameKnowledgeFile = (id, data) => {
  return request({
    url: KNOWLEDGE_API.RENAME_FILE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 移动文件
 * @param {number} id - 文件ID
 * @param {Object} data - 移动数据
 * @param {number} data.folderId - 目标文件夹ID
 * @returns {Promise} 移动文件响应
 */
export const moveKnowledgeFile = (id, data) => {
  return request({
    url: KNOWLEDGE_API.MOVE_FILE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 知识库问答
 * @param {Object} data - 问答数据
 * @param {number} data.knowledgeId - 知识库ID
 * @param {string} data.question - 问题
 * @param {Object} data.options - 问答选项
 * @returns {Promise} 问答响应
 */
export const askKnowledgeQuestion = (data) => {
  return request({
    url: KNOWLEDGE_API.QA_ASK,
    method: HTTP_METHODS.POST,
    data
  })
}