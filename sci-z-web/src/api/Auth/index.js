/**
 * 认证模块 API 接口
 * 包含登录、注册、重置密码等认证相关接口
 */

export * from './auth'

// 注意：getPermissions 和 getMenus 已实现，但当前通过登录接口和 getUserInfo 接口
// 一次性返回权限和菜单数据，这两个独立接口保留用于特殊场景（如单独刷新权限/菜单）
export { getPermissions, getMenus } from './auth'