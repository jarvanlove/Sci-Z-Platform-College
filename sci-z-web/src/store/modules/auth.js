import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/Auth'
import { getToken, setToken, removeToken, getUserInfo as getLocalUserInfo, setUserInfo, removeUserInfo, getPermissions, setPermissions, removePermissions, getRoles, setRoles, removeRoles, getMenus, setMenus, removeMenus, saveLastUsername } from '@/utils/auth'
import { createLogger } from '@/utils/simpleLogger'

// 创建认证模块日志器
const authLogger = createLogger('Auth')

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = getToken()
    const userInfo = getLocalUserInfo()
    let permissions = getPermissions()
    let roles = getRoles()
    let menus = getMenus()
    
    // 如果用户已登录但权限数据为空，说明 localStorage 数据不完整，需要重新登录
    if (token && userInfo && (!permissions || permissions.length === 0 || !menus || menus.length === 0)) {
      authLogger.warn('检测到已登录但权限或菜单数据为空，数据可能已损坏，建议重新登录')
      // 不使用模拟数据，保持空数组，让用户重新登录
      permissions = permissions || []
      roles = roles || []
      menus = menus || []
    }
    
    authLogger.debug('Store初始化完成', {
      token: !!token,
      userInfo: !!userInfo,
      permissionsCount: permissions?.length || 0,
      rolesCount: roles?.length || 0,
      menusCount: menus?.length || 0
    })
    
    return {
      token,
      userInfo,
      permissions, // 从localStorage恢复权限列表
      menus,       // 从localStorage恢复菜单列表
      roles // 从localStorage恢复角色列表
    }
  },

  getters: {
    isLoggedIn: (state) => !!state.token,
    
    // 权限检查
    hasPermission: (state) => (permission) => {
      const hasPermission = state.permissions.includes(permission) || state.permissions.includes('*')
          // 权限检查日志
          authLogger.debug('权限检查', { 
            permission, 
            hasPermission, 
            userPermissions: state.permissions 
          })
      return hasPermission
    },
    
    // 角色检查
    hasRole: (state) => (role) => {
      return state.roles.includes(role) || state.roles.includes('admin')
    },
    
    // 菜单权限检查
    hasMenuPermission: (state) => (menuPath) => {
      return state.menus.some(menu => menu.path === menuPath)
    },
    
    // 获取用户角色
    userRole: (state) => {
      return state.userInfo?.role || 'user'
    }
  },

  actions: {
    // 登录
    async login(loginForm) {
      try {
        const response = await login(loginForm)
        authLogger.debug('API返回的完整数据', response.data)
        
        // 方案一：从登录接口直接获取所有数据，包括 menus
        const { token, userInfo, permissions, roles, menus, rememberMe } = response.data
        
        authLogger.debug('解构后的数据', { 
          hasToken: !!token, 
          hasUserInfo: !!userInfo, 
          permissionsCount: permissions?.length || 0, 
          rolesCount: roles?.length || 0,
          menusCount: menus?.length || 0,
          rememberMe: rememberMe
        })
        
        this.token = token
        this.userInfo = userInfo
        this.permissions = permissions || []
        this.roles = roles || []
        this.menus = menus || [] // 方案一：直接从登录接口获取菜单数据
        
        authLogger.debug('设置后的store状态', {
          hasToken: !!this.token,
          hasUserInfo: !!this.userInfo,
          permissionsCount: this.permissions?.length || 0,
          rolesCount: this.roles?.length || 0,
          menusCount: this.menus?.length || 0
        })
        
        // 🔑 核心：根据"记住我"选项存储 token
        // rememberMe = true: 存储到 localStorage（持久化，浏览器关闭后依然保留）
        // rememberMe = false: 存储到 sessionStorage（会话，浏览器关闭后清除）
        const shouldRemember = rememberMe || loginForm.rememberMe || false
        setToken(token, shouldRemember)
        
        // 保存用户信息和权限数据（始终使用 localStorage）
        setUserInfo(userInfo)
        setPermissions(this.permissions)
        setRoles(this.roles)
        setMenus(this.menus)
        
        // 💾 保存上次登录的用户名（用于退出登录后自动填充）
        if (userInfo?.username) {
          saveLastUsername(userInfo.username)
        }
        
        authLogger.info('🎉 登录成功', { 
          permissions: this.permissions,
          roles: this.roles,
          menusCount: this.menus?.length || 0,
          username: this.userInfo?.username,
          rememberMe: shouldRemember
        })
        
        // 在开发环境中显示登录成功信息
        if (import.meta.env.DEV) {
          console.log('🎉 用户登录成功!')
          console.log('👤 用户信息:', this.userInfo)
          console.log('🔑 权限列表:', this.permissions)
          console.log('👥 角色列表:', this.roles)
          console.log('📋 菜单列表:', this.menus)
          console.log('💾 记住我:', shouldRemember ? 'localStorage (持久化)' : 'sessionStorage (会话)')
        }
        
        return response
      } catch (error) {
        authLogger.error('❌ 登录失败', { error: error.message, stack: error.stack })
        
        // 在开发环境中显示登录失败信息
        if (import.meta.env.DEV) {
          console.log('❌ 登录失败!')
          console.log('🚨 错误信息:', error.message)
        }
        throw error
      }
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await getUserInfo()
        // 方案一：如果 getUserInfo 接口也返回 menus，则直接获取
        const { userInfo, permissions, roles, menus } = response.data
        
        this.userInfo = userInfo
        this.permissions = permissions || []
        this.roles = roles || []
        
        // 如果接口返回了 menus，则使用接口返回的菜单；否则保持原有菜单不变
        if (menus && menus.length > 0) {
          this.menus = menus
          setMenus(this.menus)
        }
        
        setUserInfo(userInfo)
        setPermissions(this.permissions)
        setRoles(this.roles)
        
        return response
      } catch (error) {
        throw error
      }
    },


    // 退出登录
    async logout() {
      try {
        if (this.token) {
          await logout()
        }
      } catch (error) {
        authLogger.error('退出登录失败', { error: error.message })
      } finally {
        this.token = null
        this.userInfo = null
        this.permissions = []
        this.menus = []
        this.roles = []
        
        removeToken()
        removeUserInfo()
        removePermissions()
        removeRoles()
        removeMenus()
      }
    },

    // 重置状态
    resetState() {
      this.token = null
      this.userInfo = null
      this.permissions = []
      this.menus = []
      this.roles = []
      
      removeToken()
      removeUserInfo()
      removePermissions()
      removeRoles()
      removeMenus()
    },

    // 初始化权限（登录成功后调用）
    async initPermissions() {
      try {
        authLogger.info('开始初始化权限', {
          permissionsCount: this.permissions?.length || 0,
          menusCount: this.menus?.length || 0
        })
        
        // 权限数据已在登录时获取，这里只做验证
        if (!this.permissions || this.permissions.length === 0) {
          authLogger.warn('权限数据为空，尝试重新获取用户信息')
          await this.getUserInfo()
        }
        
        authLogger.info('权限初始化完成', {
          permissionsCount: this.permissions?.length || 0,
          menusCount: this.menus?.length || 0
        })
        
        return true
      } catch (error) {
        authLogger.error('权限初始化失败', { error: error.message })
        throw error
      }
    }
  }
})