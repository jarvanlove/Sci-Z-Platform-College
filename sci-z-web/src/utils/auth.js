// 认证相关工具函数

// ========================================
// Token 管理（支持"记住我"功能）
// ========================================

/**
 * 获取 token
 * 优先从 localStorage 获取（持久化），再从 sessionStorage 获取（会话）
 */
export const getToken = () => {
  return localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token')
}

/**
 * 设置 token
 * @param {string} token - Token 值
 * @param {boolean} rememberMe - 是否记住我（true: localStorage持久化, false: sessionStorage会话）
 */
export const setToken = (token, rememberMe = false) => {
  if (rememberMe) {
    // 记住我：存储到 localStorage（持久化，浏览器关闭后依然保留）
    localStorage.setItem('auth_token', token)
    // 清除 sessionStorage 中可能存在的旧 token
    sessionStorage.removeItem('auth_token')
  } else {
    // 不记住：存储到 sessionStorage（会话，浏览器关闭后清除）
    sessionStorage.setItem('auth_token', token)
    // 清除 localStorage 中可能存在的旧 token
    localStorage.removeItem('auth_token')
  }
}

/**
 * 移除 token
 * 同时清除 localStorage 和 sessionStorage
 */
export const removeToken = () => {
  localStorage.removeItem('auth_token')
  sessionStorage.removeItem('auth_token')
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

// 获取权限列表
export const getPermissions = () => {
  const permissions = localStorage.getItem('user_permissions')
  return permissions ? JSON.parse(permissions) : []
}

// 设置权限列表
export const setPermissions = (permissions) => {
  localStorage.setItem('user_permissions', JSON.stringify(permissions))
}

// 移除权限列表
export const removePermissions = () => {
  localStorage.removeItem('user_permissions')
}

// 获取角色列表
export const getRoles = () => {
  const roles = localStorage.getItem('user_roles')
  return roles ? JSON.parse(roles) : []
}

// 设置角色列表
export const setRoles = (roles) => {
  localStorage.setItem('user_roles', JSON.stringify(roles))
}

// 移除角色列表
export const removeRoles = () => {
  localStorage.removeItem('user_roles')
}

// 获取菜单列表
export const getMenus = () => {
  const menus = localStorage.getItem('user_menus')
  return menus ? JSON.parse(menus) : []
}

// 设置菜单列表
export const setMenus = (menus) => {
  localStorage.setItem('user_menus', JSON.stringify(menus))
}

// 移除菜单列表
export const removeMenus = () => {
  localStorage.removeItem('user_menus')
}

// 清除所有认证信息
export const clearAuth = () => {
  removeToken()
  removeUserInfo()
  removePermissions()
  removeRoles()
  removeMenus()
}

// ========================================
// 上次登录用户名管理（提升用户体验）
// ========================================

/**
 * 保存上次登录的用户名
 * @param {string} username - 用户名
 */
export const saveLastUsername = (username) => {
  if (username) {
    localStorage.setItem('last_login_username', username)
  }
}

/**
 * 获取上次登录的用户名
 * @returns {string} 上次登录的用户名，如果没有则返回空字符串
 */
export const getLastUsername = () => {
  return localStorage.getItem('last_login_username') || ''
}

/**
 * 清除上次登录的用户名
 */
export const removeLastUsername = () => {
  localStorage.removeItem('last_login_username')
}
