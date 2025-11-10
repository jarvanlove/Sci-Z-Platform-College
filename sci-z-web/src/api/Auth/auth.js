import request from '@/utils/request'
import { AUTH_API, HTTP_METHODS } from '../Common/constants'
import { LoginRequest, LoginResponse, RegisterRequest, ResetPasswordRequest, CaptchaResponse } from '../Common/types'

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
  /**
   * 登录接口
   * 后端接口：POST /auth/login
   * 入参：{ username, password, rememberMe, captcha }
   * 出参：{ token, tokenType, expiresIn, userInfo, roles, permissions, menus }
   * 说明：采用方案一，登录接口一次性返回所有初始化数据（包括菜单）
   */
  return request({
    url: AUTH_API.LOGIN,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 用户注册
 * @param {RegisterRequest} data - 注册请求参数
 * 结构说明：
 * - username: 用户名
 * - realName: 真实姓名
 * - email: 邮箱
 * - phone: 手机号
 * - department: 所属院系
 * - password: 登录密码
 * - captcha: 图形验证码
 * - captchaKey: 图形验证码唯一标识
 * - verificationCode: 手机或邮箱验证码
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
    data,
    skipAuth: true
  })
}

/**
 * 获取验证码
 * @returns {Promise<ApiResponse<CaptchaResponse>>}
 * 
 * 后端接口：GET /auth/captcha
 * 
 * 返回参数（根据后端 CaptchaResp 定义）：
 * - captchaKey: String - 验证码唯一标识(UUID)，前端在登录时需要携带此 key
 * - captchaImage: String - 验证码图片(Base64 编码)，格式: data:image/png;base64,iVBORw0KGgo...
 * - expiresIn: Number - 验证码过期时间(秒)
 * 
 * 使用示例：
 * const response = await getCaptcha()
 * const { captchaKey, captchaImage, expiresIn } = response.data
 * // captchaImage 可以直接作为 img 标签的 src 使用
 * // 登录时需要将 captchaKey 作为请求参数传递
 */
export const getCaptcha = () => {
  return request({
    url: AUTH_API.CAPTCHA,
    method: HTTP_METHODS.GET,
    skipAuth: true
  })
}

/**
 * 获取行业部门标签列表
 * @param {Object} params - 查询参数
 * @param {string} params.industry - 行业编码
 * @returns {Promise<ApiResponse<Array>>}
 */
export const getDepartmentLabels = (params = {}) => {
  return request({
    url: AUTH_API.DEPARTMENT_LABEL,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 发送邮箱验证码
 * @param {Object} data - 发送验证码请求参数
 * @param {string} data.email - 邮箱地址
 * @param {string} data.captcha - 图形验证码
 * @returns {Promise<ApiResponse>}
 */
export const sendEmailCode = (data) => {
  return request({
    url: AUTH_API.SEND_EMAIL_CODE,
    method: HTTP_METHODS.POST,
    data,
    skipAuth: true
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
  /**
   * 获取用户档案信息
   * 后端接口：POST /auth/profile
   * 出参：与登录接口相同的结构（userInfo, roles, permissions, menus、token 信息）
   * 说明：用于会话恢复时重新获取用户初始化数据
   */
  return request({
    url: AUTH_API.PROFILE,
    method: HTTP_METHODS.POST
  })
}

/**
 * 获取用户权限列表
 * @returns {Promise<ApiResponse>}
 */
export const getPermissions = () => {
  return request({
    url: AUTH_API.PERMISSIONS,
    method: HTTP_METHODS.GET
  })
}

/**
 * 获取用户菜单列表
 * @returns {Promise<ApiResponse>}
 */
export const getMenus = () => {
  return request({
    url: AUTH_API.MENUS,
    method: HTTP_METHODS.GET
  })
}

/**
 * 校验当前登录状态
 * @returns {Promise<ApiResponse>}
 */
export const checkLoginStatus = () => {
  return request({
    url: AUTH_API.CHECK_LOGIN,
    method: HTTP_METHODS.GET
  })
}

/**
 * 校验指定角色是否存在
 * @param {Object} params
 * @param {string} params.roleCode - 角色编码
 * @param {string} [params.industryType] - 行业类型
 * @returns {Promise<ApiResponse>}
 */
export const checkRole = (params = {}) => {
  return request({
    url: AUTH_API.CHECK_ROLE,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 校验指定权限是否存在
 * @param {Object} params
 * @param {string} params.permissionCode - 权限编码
 * @param {string} [params.industryType] - 行业类型
 * @returns {Promise<ApiResponse>}
 */
export const checkPermission = (params = {}) => {
  return request({
    url: AUTH_API.CHECK_PERMISSION,
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 用户退出登录
 * @returns {Promise<ApiResponse>}
 */
export const logout = () => {
  /**
   * 退出登录接口
   * 后端接口：POST /auth/logout
   * 说明：清除服务端 Sa-Token 会话
   */
  return request({
    url: AUTH_API.LOGOUT,
    method: HTTP_METHODS.POST
  })
}