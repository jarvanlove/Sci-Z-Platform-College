import request from '@/utils/request'
import { AI_API, HTTP_METHODS, API_BASE_URL } from '../Common/constants'

/**
 * AI助手模块 API 接口
 *
 */

/**
 * 获取对话列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键词搜索
 * @returns {Promise} 对话列表响应
 */
export const getConversations = (params) => {
  return request({
    url: AI_API.CONVERSATIONS,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 创建新对话
 * @param {Object} data - 对话数据
 * @param {string} data.title - 对话标题
 * @returns {Promise} 创建对话响应
 */
export const createConversation = (data) => {
  return request({
    url: AI_API.CONVERSATIONS,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取对话详情
 * @param {number} id - 对话ID
 * @returns {Promise} 对话详情响应
 */
export const getConversationDetail = (id) => {
  return request({
    url: AI_API.CONVERSATION_DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 更新对话标题
 * @param {number} id - 对话ID
 * @param {Object} data - 更新数据
 * @param {string} data.title - 新标题
 * @returns {Promise} 更新对话响应
 */
export const updateConversation = (id, data) => {
  return request({
    url: AI_API.CONVERSATION_DETAIL(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除对话
 * @param {number} id - 对话ID
 * @returns {Promise} 删除对话响应
 */
export const deleteConversation = (id) => {
  return request({
    url: AI_API.CONVERSATION_DETAIL(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {number} data.conversationId - 对话ID
 * @param {string} data.content - 消息内容
 * @param {string} data.type - 消息类型
 * @returns {Promise} 发送消息响应
 */
export const sendMessage = (data) => {
  return request({
    url: AI_API.MESSAGES,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取对话消息历史
 * @param {number} conversationId - 对话ID
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @returns {Promise} 消息历史响应
 */
export const getConversationMessages = (conversationId, params) => {
  return request({
    url: AI_API.MESSAGES_BY_CONVERSATION(conversationId),
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 删除消息
 * @param {number} id - 消息ID
 * @returns {Promise} 删除消息响应
 */
export const deleteMessage = (id) => {
  return request({
    url: AI_API.DELETE_MESSAGE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 执行 Dify 工作流
 * @param {Object} data - 工作流数据
 * @param {string} data.workflowId - 工作流ID
 * @param {Object} data.inputs - 输入参数
 * @returns {Promise} 工作流执行响应
 */
export const executeWorkflow = (data) => {
  return request({
    url: AI_API.WORKFLOW_EXECUTE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 同步知识库
 * @param {Object} data - 同步数据
 * @param {number} data.knowledgeId - 知识库ID
 * @param {string} data.action - 同步动作
 * @returns {Promise} 同步知识库响应
 */
export const syncKnowledge = (data) => {
  return request({
    url: AI_API.KNOWLEDGE_SYNC,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 查询工作流状态
 * @param {string} taskId - 任务ID
 * @returns {Promise} 工作流状态响应
 */
export const getWorkflowStatus = (taskId) => {
  return request({
    url: AI_API.WORKFLOW_STATUS(taskId),
    method: HTTP_METHODS.GET
  })
}

// ================================
// AI会话管理接口（新接口）
// ================================

/**
 * 创建AI会话
 * @param {Object} data - 会话数据
 * @param {string} data.title - 会话标题，最大255个字符（必填）
 * @param {string} [data.difyConversationId] - Dify会话ID，最大100个字符
 * @param {number} [data.isPinned=0] - 是否置顶(0:否,1:是)，默认为0
 * @returns {Promise} 创建会话响应
 */
export const createAiConversation = (data) => {
  return request({
    url: AI_API.CONVERSATION_CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新AI会话
 * @param {Object} data - 更新数据
 * @param {string} data.id - 会话ID（必填）
 * @param {string} [data.title] - 会话标题，最大255个字符
 * @param {string} [data.difyConversationId] - Dify会话ID，最大100个字符
 * @param {number} [data.isPinned] - 是否置顶(0:否,1:是)
 * @returns {Promise} 更新会话响应
 */
export const updateAiConversation = (data) => {
  return request({
    url: AI_API.CONVERSATION_UPDATE,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 查询AI会话详情
 * @param {string} id - 会话ID
 * @returns {Promise} 会话详情响应
 */
export const getAiConversationDetail = (id) => {
  return request({
    url: AI_API.CONVERSATION_DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 分页查询AI会话列表
 * @param {Object} params - 查询参数
 * @param {number} [params.pageNo=1] - 页码，默认为1
 * @param {number} [params.pageSize=10] - 每页数量，默认为10
 * @param {string} [params.sortBy] - 排序字段
 * @param {string} [params.sortOrder=DESC] - 排序方式（ASC/DESC），默认为DESC
 * @param {string} [params.keyword] - 关键字搜索（标题）
 * @returns {Promise} 会话列表响应（分页）
 */
export const pageAiConversations = (params = {}) => {
  return request({
    url: AI_API.CONVERSATION_PAGE,
    method: HTTP_METHODS.GET,
    params: {
      pageNo: params.pageNo || 1,
      pageSize: params.pageSize || 10,
      sortBy: params.sortBy,
      sortOrder: params.sortOrder || 'DESC',
      keyword: params.keyword
    }
  })
}

/**
 * 查询AI会话列表（不分页）
 * @returns {Promise} 会话列表响应
 */
export const listAiConversations = () => {
  return request({
    url: AI_API.CONVERSATION_LIST,
    method: HTTP_METHODS.GET
  })
}

/**
 * 删除AI会话
 * @param {string} id - 会话ID
 * @returns {Promise} 删除会话响应
 */
export const deleteAiConversation = (id) => {
  return request({
    url: AI_API.CONVERSATION_DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 批量删除AI会话
 * @param {Array<string>} ids - 会话ID列表
 * @returns {Promise} 批量删除响应
 */
export const deleteBatchAiConversations = (ids) => {
  return request({
    url: AI_API.CONVERSATION_BATCH_DELETE,
    method: HTTP_METHODS.DELETE,
    data: ids
  })
}

/**
 * 更新AI会话置顶状态
 * @param {string} id - 会话ID
 * @param {number} isPinned - 是否置顶(0:否,1:是)
 * @returns {Promise} 更新置顶状态响应
 */
export const updateAiConversationPinnedStatus = (id, isPinned) => {
  return request({
    url: AI_API.CONVERSATION_PINNED(id),
    method: HTTP_METHODS.PUT,
    params: { isPinned }
  })
}

// ================================
// AI消息管理接口（新接口）
// ================================

/**
 * 创建AI消息
 * @param {Object} data - 消息数据
 * @param {string} data.conversationId - 会话ID（必填）
 * @param {string} data.role - 角色(user/assistant)，最大10个字符（必填）
 * @param {string} data.content - 消息内容（必填）
 * @param {string} [data.difyMessageId] - Dify消息ID，最大100个字符
 * @param {string} [data.sources] - 知识来源(JSON格式)
 * @param {number} [data.confidence] - 置信度
 * @param {string} [data.sendTime] - 发送时间，默认为当前时间
 * @returns {Promise} 创建消息响应
 */
export const createAiMessage = (data) => {
  return request({
    url: AI_API.MESSAGE_CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新AI消息
 * @param {Object} data - 更新数据
 * @param {string} data.id - 消息ID（必填）
 * @param {string} [data.conversationId] - 会话ID
 * @param {string} [data.role] - 角色(user/assistant)，最大10个字符
 * @param {string} [data.content] - 消息内容
 * @param {string} [data.difyMessageId] - Dify消息ID，最大100个字符
 * @param {string} [data.sources] - 知识来源(JSON格式)
 * @param {number} [data.confidence] - 置信度
 * @param {string} [data.sendTime] - 发送时间
 * @returns {Promise} 更新消息响应
 */
export const updateAiMessage = (data) => {
  return request({
    url: AI_API.MESSAGE_UPDATE,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 查询AI消息详情
 * @param {string} id - 消息ID
 * @returns {Promise} 消息详情响应
 */
export const getAiMessageDetail = (id) => {
  return request({
    url: AI_API.MESSAGE_DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 分页查询AI消息列表
 * @param {Object} params - 查询参数
 * @param {string} params.conversationId - 会话ID（必填）
 * @param {number} [params.pageNo=1] - 页码，默认为1
 * @param {number} [params.pageSize=10] - 每页数量，默认为10
 * @param {string} [params.sortBy] - 排序字段
 * @param {string} [params.sortOrder=ASC] - 排序方式（ASC/DESC），默认为ASC
 * @returns {Promise} 消息列表响应（分页）
 */
export const pageAiMessages = (params) => {
  return request({
    url: AI_API.MESSAGE_PAGE,
    method: HTTP_METHODS.GET,
    params: {
      conversationId: params.conversationId,
      pageNo: params.pageNo || 1,
      pageSize: params.pageSize || 10,
      sortBy: params.sortBy,
      sortOrder: params.sortOrder || 'ASC'
    }
  })
}

/**
 * 查询AI消息列表（不分页）
 * @param {string} conversationId - 会话ID
 * @returns {Promise} 消息列表响应
 */
export const listAiMessages = (conversationId) => {
  return request({
    url: AI_API.MESSAGE_LIST(conversationId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 删除AI消息
 * @param {string} id - 消息ID
 * @returns {Promise} 删除消息响应
 */
export const deleteAiMessage = (id) => {
  return request({
    url: AI_API.MESSAGE_DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 批量删除AI消息
 * @param {Array<string>} ids - 消息ID列表
 * @returns {Promise} 批量删除响应
 */
export const deleteBatchAiMessages = (ids) => {
  return request({
    url: AI_API.MESSAGE_BATCH_DELETE,
    method: HTTP_METHODS.DELETE,
    data: ids
  })
}

/**
 * 删除会话所有消息
 * @param {string} conversationId - 会话ID
 * @returns {Promise} 删除响应
 */
export const deleteAiMessagesByConversationId = (conversationId) => {
  return request({
    url: AI_API.MESSAGE_DELETE_BY_CONVERSATION(conversationId),
    method: HTTP_METHODS.DELETE
  })
}

// ================================
// Chat 工作流接口
// ================================

/**
 * 执行 Dify 工作流或直接调用 Chatbot 流式对话
 * 支持四种提问模式：
 * 1. 单独提问：不传文件和知识库，直接提问（不使用知识库）
 * 2. 根据知识库提问：只传知识库ID，基于知识库提问
 * 3. 根据文件提问：只传文件，执行工作流后再提问（使用 file 类型的 API key）
 * 4. 根据文件和知识库提问：同时传文件和知识库ID，执行工作流后再提问（文件上传到知识库）
 * 
 * @param {Object} params - 对话参数
 * @param {string} params.query - 用户问题（必填）
 * @param {string|string[]} [params.knowledgeId] - 知识库ID（可选，支持单个ID或数组，不传则不使用知识库）
 * @param {string} [params.workflowId] - 工作流ID（可选，仅在传文件时使用，不传则由后端决定）
 * @param {File[]} [params.files] - 上传的文件列表（可选，不传则不执行工作流）
 * @param {string} [params.conversationId] - 会话ID（可选，用于 chatbot 流式对话）
 * @param {string} [params.user] - 用户标识（可选，默认使用当前登录用户ID）
 * @param {Function} params.onMessage - 消息片段回调函数 (answer: string) => void
 * @param {Function} params.onEnd - 消息结束回调函数 (data: {conversationId, messageId, documents}) => void
 * @param {Function} params.onError - 错误回调函数 (error: Error) => void
 * @returns {Promise} 返回AbortController用于取消请求
 */
export const runWorkflowStream = async (params) => {
  const { query, knowledgeId, workflowId, files, conversationId, user, onMessage, onEnd, onError } = params
  
  // 获取token
  const authStore = await import('@/store/modules/auth').then(m => m.useAuthStore())
  const token = authStore.token
  
  if (!token) {
    const error = new Error('未登录')
    onError?.(error)
    throw error
  }

  // 使用新的API路径（fetch 需要完整路径，需要手动添加 /api 前缀）
  const url = `${API_BASE_URL}${AI_API.CHAT_WORKFLOW_RUN}`
  
  const logger = (await import('@/utils/simpleLogger')).createLogger('ChatWorkflow')
  logger.info('工作流流式对话请求', { url, query, knowledgeId, workflowId, filesCount: files?.length || 0 })
  
  const abortController = new AbortController()
  
  try {
    // 构建 FormData（即使没有文件也使用 FormData，以支持文件上传场景）
    // 如果没有文件，FormData 只包含文本参数，后端也能正常处理
    const formData = new FormData()
    formData.append('query', query) // 必填参数
    if (knowledgeId) {
      // 支持多个知识库ID（数组）或单个ID（字符串）
      if (Array.isArray(knowledgeId)) {
        // 如果是数组，添加多个 knowledgeId 参数
        knowledgeId.forEach(id => {
          formData.append('knowledgeId', id)
        })
      } else {
        // 如果是字符串，直接添加
        formData.append('knowledgeId', knowledgeId)
      }
    }
    if (workflowId) {
      formData.append('workflowId', workflowId)
    }
    // 只有当有文件时才添加文件参数
    if (files && files.length > 0) {
      files.forEach(file => {
        formData.append('files', file)
      })
    }
    if (conversationId) {
      formData.append('conversationId', conversationId)
    }
    if (user) {
      formData.append('user', user)
    }

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData,
      signal: abortController.signal
    })

    // 检查是否是错误响应（非流式）
    if (!response.ok) {
      const contentType = response.headers.get('content-type')
      if (contentType && contentType.includes('application/json')) {
        // 处理JSON错误响应
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
    let conversationIdFromResponse = null
    let messageIdFromResponse = null
    let documentsFromResponse = []

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 保留最后不完整的行

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine) continue

        // 处理 SSE 格式
        if (trimmedLine.startsWith('event:')) {
          // 事件类型
          const eventType = trimmedLine.substring(6).trim()
          logger.debug('收到SSE事件', eventType)
        } else if (trimmedLine.startsWith('data:')) {
          const dataStr = trimmedLine.substring(5).trim()
          if (!dataStr || dataStr === '[DONE]') continue

          try {
            const data = JSON.parse(dataStr)
            
            if (data.event === 'message' || data.answer) {
              // 消息片段
              onMessage?.(data.answer || '')
            } else if (data.event === 'message_end') {
              // 消息结束
              onEnd?.({
                conversationId: conversationIdFromResponse || conversationId,
                messageId: messageIdFromResponse,
                documents: documentsFromResponse,
                metadata: data.metadata
              })
            } else if (data.event === 'error') {
              // 错误事件
              const error = new Error(data.message || '请求失败')
              error.code = data.code
              onError?.(error)
              throw error
            } else {
              // 其他数据，尝试提取信息
              if (data.conversation_id) {
                conversationIdFromResponse = data.conversation_id
              }
              if (data.message_id) {
                messageIdFromResponse = data.message_id
              }
              // 提取文档片段数据
              if (data.metadata?.retriever_resources) {
                const retrieverResources = data.metadata.retriever_resources
                documentsFromResponse = retrieverResources.map((resource, index) => ({
                  id: resource.segment_id || `doc-${index}`,
                  name: resource.document_name || resource.dataset_name || `文档 ${index + 1}`,
                  datasetName: resource.dataset_name,
                  content: resource.content || '',
                  score: resource.score || 0,
                  position: resource.position || index + 1,
                  documentId: resource.document_id,
                  segmentId: resource.segment_id,
                  document_name: resource.document_name,
                  dataset_name: resource.dataset_name,
                  segment_id: resource.segment_id
                }))
              }
              // 如果有 answer 字段，也触发 onMessage
              if (data.answer) {
                onMessage?.(data.answer)
              }
            }
          } catch (parseError) {
            // 忽略解析错误，继续处理下一行
            logger.warn('解析SSE数据失败', parseError, dataStr)
          }
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