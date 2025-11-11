import { defineStore } from 'pinia'
import { login, logout as logoutApi, getUserInfo as fetchUserProfile, refreshToken as refreshTokenApi, checkLoginStatus, checkRole as checkRoleApi, checkPermission as checkPermissionApi } from '@/api/Auth'
import { getToken, setToken, removeToken, getUserInfo as getLocalUserInfo, setUserInfo, removeUserInfo, getPermissions, setPermissions, removePermissions, getRoles, setRoles, removeRoles, getMenus, setMenus, removeMenus, saveLastUsername } from '@/utils/auth'
import { createLogger } from '@/utils/simpleLogger'

// 创建认证模块日志器
const authLogger = createLogger('Auth')

const SESSION_CHECK_INTERVAL = 60 * 1000 // 1 分钟内不重复调用校验接口
const TOKEN_REFRESH_THRESHOLD = 5 * 60 // 剩余 5 分钟触发刷新
const REMEMBER_ME_KEY = 'auth_remember_me'

const toNumber = (value) => {
  if (value === null || value === undefined) return null
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

const toTimestamp = (value) => {
  if (value === null || value === undefined) return null
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value > 0 && value < 1e12 ? value * 1000 : value
  }
  const parsed = Date.parse(value)
  return Number.isFinite(parsed) ? parsed : null
}

const unwrapResponse = (response) => {
  if (response && Object.prototype.hasOwnProperty.call(response, 'data')) {
    return response.data
  }
  return response
}

const extractSessionInfo = (payload = {}) => {
  const tokenInfoSource = payload.tokenInfo || payload.token || {}
  const tokenInfo = typeof tokenInfoSource === 'object' ? tokenInfoSource : {}
  const loginId = payload.loginId ?? tokenInfo.loginId ?? null
  const expiresIn = toNumber(
    payload.expiresIn ??
    payload.tokenRemainingSeconds ??
    payload.tokenRemaining ??
    tokenInfo.expiresIn ??
    tokenInfo.remainingSeconds ??
    tokenInfo.timeout ??
    tokenInfo.tokenTimeout ??
    null
  )
  const expiresAt = toTimestamp(
    payload.expiresAt ??
    tokenInfo.expiresAt ??
    tokenInfo.expireAt ??
    null
  )
  const tokenValue = payload.tokenValue ?? tokenInfo.tokenValue ?? null
  const tokenName = payload.tokenName ?? tokenInfo.tokenName ?? null

  return {
    loginId,
    expiresIn,
    expiresAt,
    tokenValue,
    tokenName
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = getToken()
    const userInfo = getLocalUserInfo()
    let permissions = getPermissions()
    let roles = getRoles()
    let menus = getMenus()
    const rememberedFlag = localStorage.getItem(REMEMBER_ME_KEY)
    const hasLocalToken = !!localStorage.getItem('auth_token')
    const hasSessionToken = !!sessionStorage.getItem('auth_token')
    const rememberMe = rememberedFlag === null
      ? true
      : rememberedFlag === '1'
    
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
      roles,       // 从localStorage恢复角色列表
      rememberMe,
      sessionInfo: null,
      lastSessionCheck: 0,
      refreshPromise: null
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
    applySessionInfo(payload = {}) {
      const info = extractSessionInfo(payload)
      const previous = this.sessionInfo || {}
      const merged = {
        loginId: info.loginId ?? previous.loginId ?? null,
        expiresIn: info.expiresIn ?? previous.expiresIn ?? null,
        expiresAt: info.expiresAt ?? previous.expiresAt ?? null,
        tokenName: info.tokenName ?? previous.tokenName ?? null
      }
      
      if (info.tokenValue && info.tokenValue !== this.token) {
        authLogger.debug('检测到新的 tokenValue，执行本地更新', { tokenName: merged.tokenName })
        setToken(info.tokenValue, this.rememberMe)
        this.token = info.tokenValue
      }
      
      this.sessionInfo = merged
      this.lastSessionCheck = Date.now()
      authLogger.debug('会话信息已更新', merged)
      return merged
    },
    
    // 登录
    async login(loginForm) {
      try {
        const response = await login(loginForm)
        const payload = unwrapResponse(response) || {}
        authLogger.debug('API返回的完整数据', payload)
        
        // 方案一：从登录接口直接获取所有数据，包括 menus
        const { token, userInfo, permissions, roles, menus, rememberMe: rememberFromApi } = payload
        
        authLogger.debug('解构后的数据', { 
          hasToken: !!token, 
          hasUserInfo: !!userInfo, 
          permissionsCount: permissions?.length || 0, 
          rolesCount: roles?.length || 0,
          menusCount: menus?.length || 0,
          rememberMe: rememberFromApi
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
        const shouldRemember = typeof rememberFromApi === 'boolean'
          ? rememberFromApi
          : !!loginForm.rememberMe
        this.rememberMe = shouldRemember
        localStorage.setItem(REMEMBER_ME_KEY, shouldRemember ? '1' : '0')
        setToken(token, shouldRemember)
        
        // 保存用户信息和权限数据（始终使用 localStorage）
        setUserInfo(userInfo)
        setPermissions(this.permissions)
        setRoles(this.roles)
        setMenus(this.menus)
        
        // 会话信息与过期时间同步
        this.applySessionInfo({ ...payload, tokenValue: payload?.tokenValue ?? token })
        
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
    async getUserInfo(force = false) {
      if (!this.token) {
        authLogger.warn('尝试在未登录状态下获取用户信息')
        return null
      }
      
      if (!force && this.userInfo && this.permissions?.length && this.roles?.length) {
        authLogger.debug('用户信息已存在，跳过重复请求')
        return {
          data: {
            userInfo: this.userInfo,
            permissions: this.permissions,
            roles: this.roles,
            menus: this.menus
          }
        }
      }
      
      try {
        const response = await fetchUserProfile()
        const payload = unwrapResponse(response) || {}
        const { userInfo, permissions, roles, menus } = payload
        
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
        
        // 同步会话信息
        this.applySessionInfo(payload)
        
        return response
      } catch (error) {
        authLogger.error('获取用户信息失败', { error: error.message })
        throw error
      }
    },

    async verifyLoginStatus(force = false) {
      if (!this.token) {
        authLogger.debug('本地无 token，跳过服务端校验')
        return false
      }
      
      const now = Date.now()
      if (!force && this.lastSessionCheck && now - this.lastSessionCheck < SESSION_CHECK_INTERVAL) {
        authLogger.debug('使用缓存的登录状态校验结果', {
          lastSessionCheck: this.lastSessionCheck,
          sessionInfo: this.sessionInfo
        })
        return true
      }
      
      try {
        const response = await checkLoginStatus()
        const payload = unwrapResponse(response) || {}
        const isLoggedIn = payload?.login ?? payload?.isLogin ?? payload?.isLoggedIn ?? false
        
        if (!isLoggedIn) {
          authLogger.warn('服务端会话已失效，执行本地清理')
          this.resetState()
          return false
        }
        
        const session = this.applySessionInfo(payload)
        const expiresIn = session?.expiresIn
        if (expiresIn !== null && expiresIn <= TOKEN_REFRESH_THRESHOLD) {
          authLogger.info('Token 即将过期，尝试刷新', { expiresIn })
          try {
            await this.refreshSessionToken()
          } catch (refreshError) {
            authLogger.error('Token 刷新失败', { error: refreshError.message })
          }
        }
        
        return true
      } catch (error) {
        authLogger.error('登录状态校验失败', { error: error.message })
        throw error
      }
    },

    async refreshSessionToken(force = false) {
      if (!this.token) {
        authLogger.debug('未登录状态无需刷新 token')
        return null
      }
      
      if (this.refreshPromise && !force) {
        authLogger.debug('检测到正在进行的刷新任务，直接复用 Promise')
        return this.refreshPromise
      }
      
      this.refreshPromise = (async () => {
        try {
          const response = await refreshTokenApi()
          const payload = unwrapResponse(response) || {}
          const session = this.applySessionInfo(payload)
          authLogger.info('Token 刷新成功', {
            expiresIn: session?.expiresIn,
            expiresAt: session?.expiresAt
          })
          return payload
        } catch (error) {
          authLogger.error('Token 刷新失败', { error: error.message })
          throw error
        } finally {
          this.refreshPromise = null
        }
      })()
      
      return this.refreshPromise
    },

    async validateRole(roleCode, industryType) {
      if (!roleCode) {
        authLogger.warn('未提供角色编码，跳过校验')
        return false
      }
      
      if (this.roles.includes('admin') || this.roles.includes(roleCode)) {
        return true
      }
      
      try {
        const response = await checkRoleApi({
          roleCode,
          ...(industryType ? { industryType } : {})
        })
        const payload = unwrapResponse(response) || {}
        const result = payload?.hasRole ?? payload?.isGranted ?? payload?.data?.hasRole ?? false
        authLogger.debug('服务端角色校验结果', {
          roleCode,
          industryType,
          result
        })
        return !!result
      } catch (error) {
        authLogger.error('角色校验接口调用失败', {
          error: error.message,
          roleCode,
          industryType
        })
        throw error
      }
    },

    async validatePermission(permissionCode, industryType) {
      if (!permissionCode) {
        authLogger.warn('未提供权限编码，跳过校验')
        return false
      }
      
      if (this.permissions.includes('*') || this.permissions.includes(permissionCode)) {
        return true
      }
      
      try {
        const response = await checkPermissionApi({
          permissionCode,
          ...(industryType ? { industryType } : {})
        })
        const payload = unwrapResponse(response) || {}
        const result = payload?.hasPermission ?? payload?.isGranted ?? payload?.data?.hasPermission ?? false
        authLogger.debug('服务端权限校验结果', {
          permissionCode,
          industryType,
          result
        })
        return !!result
      } catch (error) {
        authLogger.error('权限校验接口调用失败', {
          error: error.message,
          permissionCode,
          industryType
        })
        throw error
      }
    },


    // 退出登录
    async logout(options = {}) {
      const {
        redirect = true,
        redirectPath = '/login',
        useReplace = false,
        clearRemember = false
      } = options

      try {
        if (this.token) {
          await logoutApi()
        }
      } catch (error) {
        authLogger.error('退出登录失败', { error: error.message })
      } finally {
        this.resetState({ clearRemember })

        if (redirect && typeof window !== 'undefined') {
          try {
            const routerModule = await import('@/router')
            const routerInstance = routerModule?.default || routerModule
            if (routerInstance) {
              const currentRoute = routerInstance.currentRoute?.value
              const target =
                typeof redirectPath === 'string'
                  ? { path: redirectPath }
                  : redirectPath || { path: '/login' }
              const currentFullPath = currentRoute?.fullPath || currentRoute?.path
              const targetPath =
                typeof redirectPath === 'string'
                  ? redirectPath
                  : target.path || '/login'

              if (currentFullPath !== targetPath) {
                const navigation = useReplace
                  ? routerInstance.replace(target)
                  : routerInstance.push(target)
                navigation.catch(() => {})
              }
            } else {
              window.location.href =
                typeof redirectPath === 'string' ? redirectPath : '/login'
            }
          } catch (navigationError) {
            authLogger.error('退出后跳转登录页失败，使用浏览器跳转', {
              error: navigationError.message
            })
            window.location.href =
              typeof redirectPath === 'string' ? redirectPath : '/login'
          }
        }
      }
    },

    // 重置状态
    resetState(options = {}) {
      const { clearRemember = false } = options
      this.token = null
      this.userInfo = null
      this.permissions = []
      this.menus = []
      this.roles = []
      this.sessionInfo = null
      this.lastSessionCheck = 0
      this.refreshPromise = null
      
      if (clearRemember) {
        this.rememberMe = true
        localStorage.removeItem(REMEMBER_ME_KEY)
      }
      
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
        
        const sessionValid = await this.verifyLoginStatus()
        if (!sessionValid) {
          throw new Error('登录状态已失效，请重新登录')
        }
        
        // 权限数据已在登录时获取，这里只做验证
        if (!this.permissions || this.permissions.length === 0) {
          authLogger.warn('权限数据为空，尝试重新获取用户信息')
          await this.getUserInfo(true)
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