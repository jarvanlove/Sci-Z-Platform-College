import request from '@/utils/request'
import { KNOWLEDGE_API, HTTP_METHODS } from '../Common/constants'

/**
 * 知识库模块 API 接口
 *
 */

/**
 * 获取知识库列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页数量，默认10
 * @returns {Promise} 知识库列表响应
 */
export const getKnowledgeList = (params = {}) => {
  return request({
    url: KNOWLEDGE_API.LIST,
    method: HTTP_METHODS.GET,
    params: {
      page: params.page || 1,
      size: params.size || 10
    }
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
 * 上传文件（旧接口，保留兼容）
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
 * 上传文件到知识库（新接口，使用 difyKbId）
 * @param {string|number} difyKbId - 知识库的 Dify KB ID
 * @param {File} file - 要上传的文件
 * @param {number} [folderId=0] - 文件夹ID，默认为0（根目录）
 * @returns {Promise} 上传文件响应
 */
export const uploadFileToKnowledge = (difyKbId, file, folderId = 0) => {
  const formData = new FormData()
  formData.append('file', file)
  if (folderId && folderId !== 0) {
    formData.append('folderId', folderId)
  }
  
  return request({
    url: KNOWLEDGE_API.UPLOAD(difyKbId),
    method: HTTP_METHODS.POST,
    data: formData,
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
 * 搜索知识库
 * @param {number} knowledgeId - 知识库ID
 * @param {string} query - 搜索关键词
 * @returns {Promise} 搜索结果响应
 */
export const searchKnowledge = (knowledgeId, query) => {
  return request({
    url: KNOWLEDGE_API.SEARCH(knowledgeId),
    method: HTTP_METHODS.GET,
    params: { query }
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

/**
 * 基于知识库的Chatbot流式对话
 * @param {Object} params - 对话参数
 * @param {string} params.knowledgeId - 知识库ID（Dify知识库ID，String类型）
 * @param {string} params.query - 用户问题
 * @param {string} [params.conversationId] - 会话ID，用于保持对话上下文
 * @param {string} [params.user] - 用户标识，如果不提供则使用当前登录用户ID
 * @param {Function} params.onMessage - 消息片段回调函数 (answer: string) => void
 * @param {Function} params.onEnd - 消息结束回调函数 (data: {conversationId, messageId, documents}) => void
 * @param {Function} params.onError - 错误回调函数 (error: Error) => void
 * @returns {Promise} 返回AbortController用于取消请求
 */
export const streamKnowledgeChatbot = async (params) => {
  const { knowledgeId, query, conversationId, user, onMessage, onEnd, onError } = params
  
  // 获取token
  const authStore = await import('@/store/modules/auth').then(m => m.useAuthStore())
  const token = authStore.token
  
  if (!token) {
    const error = new Error('未登录')
    onError?.(error)
    throw error
  }

  // 直接使用固定的API路径
  const url = '/api/api/knowledge/chatbot/stream'
  
  const logger = (await import('@/utils/simpleLogger')).createLogger('KnowledgeChatbot')
  logger.info('流式对话请求URL', { url, knowledgeId, query })
  
  const abortController = new AbortController()
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        knowledgeId,
        query,
        conversationId,
        user
      }),
      signal: abortController.signal
    })

    // 检查是否是错误响应（非流式）
    if (!response.ok) {
      const contentType = response.headers.get('content-type')
      if (contentType && contentType.includes('application/json')) {
        // 处理JSON错误响应（如CHATBOT_NOT_CREATED）
        const errorData = await response.json()
        const error = new Error(errorData.message || '请求失败')
        error.code = errorData.code
        error.hint = errorData.hint
        onError?.(error)
        throw error
      } else {
        const error = new Error(`请求失败: ${response.status}`)
        onError?.(error)
        throw error
      }
    }

    // 处理流式响应
    if (!response.body) {
      const error = new Error('响应体为空')
      onError?.(error)
      throw error
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 保留最后不完整的行

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine || !trimmedLine.startsWith('data:')) continue

        const dataStr = trimmedLine.replace(/^data:\s*/, '')
        if (!dataStr || dataStr === '[DONE]') continue

        try {
          const data = JSON.parse(dataStr)
          
          if (data.event === 'message') {
            // 消息片段
            onMessage?.(data.answer || '')
          } else if (data.event === 'message_end') {
            // 消息结束，提取文档片段数据
            // 文档片段数据在 metadata.retriever_resources 中
            const retrieverResources = data.metadata?.retriever_resources || []
            const documents = retrieverResources.map((resource, index) => ({
              id: resource.segment_id || `doc-${index}`,
              name: resource.document_name || resource.dataset_name || `文档 ${index + 1}`,
              datasetName: resource.dataset_name,
              content: resource.content || '',
              score: resource.score || 0,
              position: resource.position || index + 1,
              documentId: resource.document_id,
              segmentId: resource.segment_id
            }))
            
            onEnd?.({
              conversationId: data.conversation_id,
              messageId: data.message_id,
              documents: documents, // 文档片段数据（从 retriever_resources 提取）
              metadata: data.metadata // 保留完整的元数据
            })
          }
        } catch (parseError) {
          // 忽略解析错误，继续处理下一行
          console.warn('解析SSE数据失败:', parseError, dataStr)
        }
      }
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      // 请求被取消，不处理
      return abortController
    }
    onError?.(error)
    throw error
  }

  return abortController
}

// ================================
// 知识库文件关联接口
// ================================

/**
 * 创建知识库文件关联
 * @param {Object} data - 文件关联数据
 * @param {number} data.knowledgeId - 知识库ID（必填）
 * @param {number} [data.folderId=0] - 文件夹ID，默认为0（根目录）
 * @param {number} data.attachmentId - 附件ID（必填）
 * @param {string} [data.fileName] - 文件显示名称，最大255个字符
 * @param {number} [data.sortOrder=0] - 排序号，默认为0
 * @param {string} [data.callback] - 回调数据（Dify API返回的完整JSON数据）
 * @returns {Promise} 创建文件关联响应
 */
export const createKnowledgeFileRelation = (data) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_CREATE,
    method: HTTP_METHODS.POST,
    data: {
      knowledgeId: Number(data.knowledgeId),
      folderId: data.folderId ? Number(data.folderId) : 0,
      attachmentId: Number(data.attachmentId),
      fileName: data.fileName,
      sortOrder: data.sortOrder || 0,
      callback: data.callback
    }
  })
}

/**
 * 更新知识库文件关联
 * @param {number} id - 关联ID
 * @param {Object} data - 更新数据
 * @param {number} [data.folderId] - 文件夹ID
 * @param {string} [data.fileName] - 文件显示名称，最大255个字符
 * @param {number} [data.sortOrder] - 排序号
 * @returns {Promise} 更新文件关联响应
 */
export const updateKnowledgeFileRelation = (id, data) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_UPDATE(Number(id)),
    method: HTTP_METHODS.PUT,
    data: {
      folderId: data.folderId !== undefined ? Number(data.folderId) : undefined,
      fileName: data.fileName,
      sortOrder: data.sortOrder !== undefined ? Number(data.sortOrder) : undefined
    }
  })
}

/**
 * 删除知识库文件关联（软删除）
 * @param {number} id - 关联ID
 * @returns {Promise} 删除文件关联响应
 */
export const deleteKnowledgeFileRelation = (id) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_DELETE(Number(id)),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 查询知识库文件关联详情
 * @param {number} id - 关联ID
 * @returns {Promise} 文件关联详情响应
 */
export const getKnowledgeFileRelationDetail = (id) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_DETAIL(Number(id)),
    method: HTTP_METHODS.GET
  })
}

/**
 * 分页查询知识库文件关联列表
 * @param {Object} params - 查询参数
 * @param {number} params.knowledgeId - 知识库ID（必填）
 * @param {number} [params.folderId] - 文件夹ID，如果指定则只查询该文件夹下的文件
 * @param {number} [params.page=1] - 页码，默认为1
 * @param {number} [params.size=10] - 每页大小，默认为10
 * @returns {Promise} 文件关联列表响应（分页）
 */
export const getKnowledgeFileRelationList = (params) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_LIST,
    method: HTTP_METHODS.GET,
    params: {
      knowledgeId: Number(params.knowledgeId),
      folderId: params.folderId !== null && params.folderId !== undefined ? Number(params.folderId) : undefined,
      page: params.page || 1,
      size: params.size || 10
    }
  })
}