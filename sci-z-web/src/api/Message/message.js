/**
 * 站内消息 API（系统消息列表、详情、已读、未读数）
 */
import request from '@/utils/request'
import { MESSAGE_API, HTTP_METHODS } from '../Common/constants'

/**
 * 消息列表（分页）
 * @param {Object} params
 * @param {number} [params.page] - 页码，从 1 开始
 * @param {number} [params.size] - 每页条数
 * @param {boolean} [params.unreadOnly] - 是否仅未读
 * @returns {Promise<{ records: Array, total: number, current: number, size: number }>}
 */
export const getMessageList = (params = {}) => {
  return request({
    url: MESSAGE_API.LIST,
    method: HTTP_METHODS.GET,
    params: {
      page: params.page ?? 1,
      size: params.size ?? 10,
      unreadOnly: params.unreadOnly
    }
  })
}

/**
 * 消息详情（仅接收人可查看）
 * @param {number} id - 消息ID
 * @returns {Promise<Object>}
 */
export const getMessageDetail = (id) => {
  return request({
    url: MESSAGE_API.DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 未读消息数
 * @returns {Promise<number>}
 */
export const getUnreadCount = () => {
  return request({
    url: MESSAGE_API.UNREAD_COUNT,
    method: HTTP_METHODS.GET
  })
}

/**
 * 标为已读
 * @param {number} id - 消息ID
 */
export const markMessageRead = (id) => {
  return request({
    url: MESSAGE_API.MARK_READ(id),
    method: HTTP_METHODS.PUT
  })
}

/**
 * 全部标为已读
 */
export const markAllMessagesRead = () => {
  return request({
    url: MESSAGE_API.MARK_READ_ALL,
    method: HTTP_METHODS.PUT
  })
}
