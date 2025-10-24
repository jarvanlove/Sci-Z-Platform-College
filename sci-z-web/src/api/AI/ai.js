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