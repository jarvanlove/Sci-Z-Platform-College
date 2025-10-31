import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/Auth'
import { getToken, setToken, removeToken, getUserInfo as getLocalUserInfo, setUserInfo, removeUserInfo, getPermissions, setPermissions, removePermissions, getRoles, setRoles, removeRoles, getMenus, setMenus, removeMenus } from '@/utils/auth'
import { createLogger } from '@/utils/simpleLogger'

// 创建认证模块日志器
const authLogger = createLogger('Auth')

// TODO: 后端开发完成后，取消注释以下导入
// import { getPermissions, getMenus } from '@/api/Auth'

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = getToken()
    const userInfo = getLocalUserInfo()
    let permissions = getPermissions()
    let roles = getRoles()
    let menus = getMenus()
    
    // 如果用户已登录但没有权限数据，根据用户角色初始化权限
    if (token && userInfo && (!permissions || permissions.length === 0)) {
      authLogger.info('检测到已登录用户但权限数据为空，根据角色初始化权限')
      const userRole = userInfo.role || 'user'
      
      if (userRole === 'admin') {
        permissions = ['*'] // 管理员拥有所有权限
        roles = ['admin']
        // 保存到localStorage
        setPermissions(permissions)
        setRoles(roles)
        authLogger.debug('已为admin用户初始化权限', { permissions })
      } else {
        // 为普通用户初始化基础权限
        permissions = [
          'menu:dashboard:view',
          'menu:declaration:list',
          'menu:declaration:create',
          'menu:declaration:detail',
          'menu:project:list',
          'menu:project:detail',
          'menu:project:progress',
          'menu:report:list',
          'menu:report:generate',
          'menu:knowledge:list',
          'menu:knowledge:detail',
          'menu:ai:chat',
          'menu:user:profile',
          'menu:user:security',
          'button:declaration:create',
          'button:project:create',
          'button:report:generate',
          'button:knowledge:create',
          'data:own'
        ]
        roles = ['user']
        setPermissions(permissions)
        setRoles(roles)
        authLogger.debug('已为普通用户初始化权限', { permissions })
      }
    }
    
    // 如果用户已登录但没有菜单数据，初始化菜单
    if (token && userInfo && (!menus || menus.length === 0)) {
      authLogger.info('检测到已登录用户但菜单数据为空，初始化菜单')
      // 使用临时的菜单数据，稍后在fetchMenus中会重新设置
      menus = []
      authLogger.debug('菜单数据将在fetchMenus中初始化')
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
        const { token, userInfo, permissions, roles, menus } = response.data
        
        authLogger.debug('解构后的数据', { 
          hasToken: !!token, 
          hasUserInfo: !!userInfo, 
          permissionsCount: permissions?.length || 0, 
          rolesCount: roles?.length || 0,
          menusCount: menus?.length || 0 
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
        
        setToken(token)
        setUserInfo(userInfo)
        setPermissions(this.permissions)
        setRoles(this.roles)
        setMenus(this.menus) // 保存菜单数据到localStorage
        
        authLogger.info('🎉 登录成功', { 
          permissions: this.permissions,
          roles: this.roles,
          menusCount: this.menus?.length || 0,
          username: this.userInfo?.username
        })
        
        // 在开发环境中显示登录成功信息
        if (import.meta.env.DEV) {
          console.log('🎉 用户登录成功!')
          console.log('👤 用户信息:', this.userInfo)
          console.log('🔑 权限列表:', this.permissions)
          console.log('👥 角色列表:', this.roles)
          console.log('📋 菜单列表:', this.menus)
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

    // 获取权限列表
    async fetchPermissions() {
      try {
        // TODO: 后端开发完成后，取消注释以下代码
        // const response = await getPermissions()
        // this.permissions = response.data || []
        // console.log('权限数据获取成功:', this.permissions)
        
        // 当前使用模拟数据
        this.permissions = this.getMockPermissions()
        authLogger.debug('使用模拟权限数据', { permissions: this.permissions })
      } catch (error) {
        authLogger.error('获取权限失败', { error: error.message })
        // 如果接口失败，使用模拟数据
        this.permissions = this.getMockPermissions()
      }
    },

    // 获取菜单列表
    async fetchMenus() {
      try {
        // TODO: 后端开发完成后，取消注释以下代码
        // const response = await getMenus()
        // this.menus = response.data || []
        // console.log('菜单数据获取成功:', this.menus)
        
        // 当前使用模拟数据
        this.menus = this.getMockMenus()
        authLogger.debug('使用模拟菜单数据', { menusCount: this.menus?.length || 0 })
      } catch (error) {
        authLogger.error('获取菜单失败', { error: error.message })
        // 如果接口失败，使用模拟数据
        this.menus = this.getMockMenus()
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

    // ================================
    // 模拟数据方法（后端开发完成后删除）
    // ================================
    
    // 获取模拟权限数据
    getMockPermissions() {
      const userRole = this.userInfo?.role || 'user'
      
      const mockPermissions = {
        admin: [
          '*', // 超级管理员拥有所有权限
        ],
        user: [
          'menu:dashboard:view',
          'menu:declaration:list',
          'menu:declaration:create',
          'menu:declaration:detail',
          'menu:project:list',
          'menu:project:detail',
          'menu:project:progress',
          'menu:report:list',
          'menu:report:generate',
          'menu:knowledge:list',
          'menu:knowledge:detail',
          'menu:ai:chat',
          'menu:user:profile',
          'menu:user:security',
          'menu:system:user',
          'menu:system:role',
          'menu:system:config',
          'menu:system:logs',
          'button:declaration:create',
          'button:project:create',
          'button:report:generate',
          'button:knowledge:create',
          'data:own'
        ],
        guest: [
          'menu:dashboard:view',
          'data:public'
        ]
      }
      
      return mockPermissions[userRole] || mockPermissions.user
    },

    // 获取模拟菜单数据
    getMockMenus() {
      return [
        {
          path: '/dashboard',
          title: '仪表板',
          icon: 'Odometer',
          permission: 'menu:dashboard:view'
        },
        {
          path: 'declaration',
          title: '申报管理',
          icon: 'Document',
          permission: 'menu:declaration:list',
          children: [
            {
              path: '/declaration/list',
              title: '申报列表',
              icon: 'List',
              permission: 'menu:declaration:list'
            }
          ]
        },
        {
          path: 'project',
          title: '项目管理',
          icon: 'Folder',
          permission: 'menu:project:list',
          children: [
            {
              path: '/project/list',
              title: '项目列表',
              icon: 'List',
              permission: 'menu:project:list'
            }
          ]
        },
        {
          path: 'acceptance',
          title: '验收管理',
          icon: 'Check',
          permission: 'menu:report:list',
          children: [
            {
              path: '/report/list',
              title: '报告管理',
              icon: 'List',
              permission: 'menu:report:list'
            }
          ]
        },
        {
          path: 'ai',
          title: 'AI助手',
          icon: 'Monitor',
          permission: 'menu:ai:chat',
          children: [
            {
              path: '/knowledge/list',
              title: '知识库',
              icon: 'Reading',
              permission: 'menu:knowledge:list'
            },
            {
              path: '/ai/chat',
              title: 'AI对话',
              icon: 'Avatar',
              permission: 'menu:ai:chat'
            }
          ]
        },
        {
          path: 'user',
          title: '用户中心',
          icon: 'User',
          permission: 'menu:user:profile',
          children: [
            {
              path: '/user/profile',
              title: '个人信息',
              icon: 'House',
              permission: 'menu:user:profile'
            },
            {
              path: '/user/security',
              title: '安全设置',
              icon: 'Lock',
              permission: 'menu:user:security'
            }
          ]
        },
        {
          path: 'system',
          title: '系统管理',
          icon: 'Setting',
          permission: 'menu:system:user',
          children: [
            {
              path: '/system/user',
              title: '用户管理',
              icon: 'User',
              permission: 'menu:system:user'
            },
            {
              path: '/system/role',
              title: '角色权限',
              icon: 'Key',
              permission: 'menu:system:role'
            },
            {
              path: '/system/config',
              title: '系统配置',
              icon: 'Setting',
              permission: 'menu:system:config'
            },
            {
              path: '/system/logs',
              title: '日志管理',
              icon: 'Document',
              permission: 'menu:system:logs'
            }
          ]
        }
      ]
    }
  }
})