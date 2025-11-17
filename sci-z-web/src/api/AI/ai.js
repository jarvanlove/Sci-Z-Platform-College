import request from '@/utils/request'
import { AI_API, HTTP_METHODS } from '../Common/constants'

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