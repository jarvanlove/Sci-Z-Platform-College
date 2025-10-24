import request from '@/utils/request'
import { AUTH_API, HTTP_METHODS } from '../Common/constants'
import { LoginRequest, LoginResponse, RegisterRequest, ResetPasswordRequest } from '../Common/types'

/**
 * 认证模块 API 接口
 *
 */

/**
 * 用户登录
 * @param {LoginRequest} data - 登录请求参数
 * @returns {Promise<LoginResponse>}
 */
export const login = (data) => {
  return request({
    url: AUTH_API.LOGIN,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 用户注册
 * @param {RegisterRequest} data - 注册请求参数
 * @returns {Promise<ApiResponse>}
 */
export const register = (data) => {
  return request({
    url: AUTH_API.REGISTER,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 重置密码
 * @param {ResetPasswordRequest} data - 重置密码请求参数
 * @returns {Promise<ApiResponse>}
 */
export const resetPassword = (data) => {
  return request({
    url: AUTH_API.RESET_PASSWORD,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取验证码
 * @returns {Promise<ApiResponse>}
 */
export const getCaptcha = () => {
  return request({
    url: AUTH_API.CAPTCHA,
    method: HTTP_METHODS.GET
  })
}

/**
 * 刷新Token
 * @returns {Promise<ApiResponse>}
 */
export const refreshToken = () => {
  return request({
    url: AUTH_API.REFRESH_TOKEN,
    method: HTTP_METHODS.POST
  })
}

/**
 * 获取用户信息
 * @returns {Promise<ApiResponse>}
 */
export const getUserInfo = () => {
  return request({
    url: AUTH_API.USER_INFO,
    method: HTTP_METHODS.GET
  })
}

/**
 * 用户退出登录
 * @returns {Promise<ApiResponse>}
 */
export const logout = () => {
  return request({
    url: AUTH_API.LOGOUT,
    method: HTTP_METHODS.POST
  })
}