import request from '@/utils/request'
import { USER_API, AUTH_API, HTTP_METHODS } from '../Common/constants'

/**
 * 用户模块 API 接口
 *
 */

/**
 * 获取用户信息
 * @returns {Promise} 用户信息响应
 */
export const getUserInfo = () => {
  return request({
    url: AUTH_API.PROFILE,
    method: HTTP_METHODS.POST
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息数据
 * @param {string} data.realName - 真实姓名
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @param {string} data.department - 所属院系
 * @param {string} data.title - 职称/职务
 * @param {string} data.industryCode - 行业编码
 * @returns {Promise} 更新用户信息响应
 */
export const updateUserInfo = (data) => {
  return request({
    url: USER_API.INFO,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 获取个人信息可配置字段（含下拉选项）
 * @param {Object} params - 查询参数
 * @param {string} [params.industry] - 行业编码
 * @returns {Promise} 字段定义响应
 */
export const getProfileFields = (params = {}) => {
  return request({
    url: USER_API.PROFILE_FIELDS,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码数据
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @param {string} data.confirmPassword - 确认新密码
 * @returns {Promise} 修改密码响应
 */
export const changePassword = (data) => {
  return request({
    url: AUTH_API.CHANGE_PASSWORD,
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 上传头像
 * @param {FormData} data - 头像数据
 * @param {File} data.avatar - 头像文件
 * @returns {Promise} 上传头像响应
 */
export const uploadAvatar = (data) => {
  return request({
    url: USER_API.UPLOAD_AVATAR,
    method: HTTP_METHODS.POST,
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取登录日志
 * @param {Object} data - 查询参数（POST 请求体）
 * @param {number} data.pageNo - 页码（从1开始）
 * @param {number} data.pageSize - 每页数量
 * @param {string} [data.sortBy] - 排序字段（loginTime/status），默认 loginTime
 * @param {string} [data.sortOrder] - 排序方式（ASC/DESC），默认 DESC
 * @param {number} [data.status] - 登录状态（0=失败，1=成功，null=全部）
 * @param {string} [data.startDate] - 开始日期（格式：yyyy-MM-dd）
 * @param {string} [data.endDate] - 结束日期（格式：yyyy-MM-dd）
 * @returns {Promise} 登录日志响应
 */
export const getLoginLogs = (data) => {
  return request({
    url: USER_API.LOGIN_LOGS,
    method: HTTP_METHODS.POST,
    data
  })
}