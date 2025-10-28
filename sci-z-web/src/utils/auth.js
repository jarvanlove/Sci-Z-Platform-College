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
