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
  // TODO: 后端开发完成后，移除模拟登录逻辑，直接调用真实接口
  // 当前使用模拟数据，支持 admin/admin 默认登录
  
  // 模拟登录逻辑
  if (data.username === 'admin' && (data.password === 'admin' || data.password === 'admin123')) {
    return Promise.resolve({
      data: {
        token: 'mock-admin-token-' + Date.now(),
        userInfo: {
          id: 1,
          username: 'admin',
          nickname: '系统管理员',
          email: 'admin@example.com',
          avatar: '',
          role: 'admin',
          status: 'active',
          createTime: new Date().toISOString()
        },
        permissions: ['*'], // 管理员拥有所有权限
        roles: ['admin']
      },
      code: 200,
      message: '登录成功'
    })
  }
  
  // 普通用户登录
  if (data.username === 'user' && data.password === 'user') {
    return Promise.resolve({
      data: {
        token: 'mock-user-token-' + Date.now(),
        userInfo: {
          id: 2,
          username: 'user',
          nickname: '普通用户',
          email: 'user@example.com',
          avatar: '',
          role: 'user',
          status: 'active',
          createTime: new Date().toISOString()
        },
        permissions: [
          'menu:dashboard:view',
          'menu:declaration:view',
          'menu:declaration:create',
          'menu:project:view',
          'menu:project:create',
          'menu:report:view',
          'menu:report:generate',
          'menu:knowledge:view',
          'menu:knowledge:create',
          'menu:ai:view',
          'menu:user:profile',
          'menu:user:security',
          'button:declaration:create',
          'button:project:create',
          'button:report:generate',
          'button:knowledge:create',
          'data:own'
        ],
        roles: ['user']
      },
      code: 200,
      message: '登录成功'
    })
  }
  
  // 其他情况返回错误
  return Promise.reject({
    response: {
      data: {
        code: 401,
        message: '用户名或密码错误'
      }
    }
  })
  
  // 后端开发完成后的真实接口调用
  // return request({
  //   url: AUTH_API.LOGIN,
  //   method: HTTP_METHODS.POST,
  //   data
  // })
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
    data
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
  // TODO: 后端开发完成后，取消注释以下代码，移除模拟逻辑
  // return request({
  //   url: AUTH_API.USER_INFO,
  //   method: HTTP_METHODS.GET
  // })
  
  // 当前使用模拟数据，从localStorage获取用户信息
  const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
  const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
  const roles = JSON.parse(localStorage.getItem('roles') || '[]')
  
  return Promise.resolve({
    data: {
      userInfo,
      permissions,
      roles
    },
    code: 200,
    message: '获取用户信息成功'
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
 * 用户退出登录
 * @returns {Promise<ApiResponse>}
 */
export const logout = () => {
  // TODO: 后端开发完成后，取消注释以下代码，移除模拟逻辑
  // return request({
  //   url: AUTH_API.LOGOUT,
  //   method: HTTP_METHODS.POST
  // })
  
  // 当前使用模拟数据，模拟退出登录成功
  return Promise.resolve({
    data: {
      message: '退出登录成功'
    },
    code: 200,
    message: '退出登录成功'
  })
}