// 认证相关工具函数

// 获取token
export const getToken = () => {
  return localStorage.getItem('auth_token')
}

// 设置token
export const setToken = (token) => {
  localStorage.setItem('auth_token', token)
}

// 移除token
export const removeToken = () => {
  localStorage.removeItem('auth_token')
}

// 检查是否已登录
export const isLoggedIn = () => {
  return !!getToken()
}

// 获取用户信息
export const getUserInfo = () => {
  const userInfo = localStorage.getItem('user_info')
  return userInfo ? JSON.parse(userInfo) : null
}

// 设置用户信息
export const setUserInfo = (userInfo) => {
  localStorage.setItem('user_info', JSON.stringify(userInfo))
}

// 移除用户信息
export const removeUserInfo = () => {
  localStorage.removeItem('user_info')
}

// 清除所有认证信息
export const clearAuth = () => {
  removeToken()
  removeUserInfo()
}
