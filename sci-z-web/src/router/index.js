import { createRouter, createWebHistory, RouterView } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { createLogger } from '@/utils/simpleLogger'

// 创建路由模块日志器
const routerLogger = createLogger('Router')

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register/index.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/ResetPassword/index.vue'),
    meta: { title: '重置密码', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard/index.vue'),
    meta: { 
      title: '仪表板', 
      requiresAuth: true,
      permission: 'menu:dashboard:view',
      layout: 'main'
    }
  },
  {
    path: '/declaration',
    component: RouterView,
    children: [
      {
        path: 'list',
        name: 'DeclarationList',
        component: () => import('@/views/Declaration/List.vue'),
        meta: { 
          title: '申报列表', 
          requiresAuth: true,
          permission: 'menu:declaration:list',
          layout: 'main'
        }
      },
      {
        path: 'create',
        name: 'DeclarationCreate',
        component: () => import('@/views/Declaration/Create.vue'),
        meta: { 
          title: '新建申报', 
          requiresAuth: true,
          // 🔥 详情页和创建页不是独立菜单，继承列表页权限
          permission: 'menu:declaration:list',
          layout: 'main'
        }
      },
      {
        path: 'detail/:id',
        name: 'DeclarationDetail',
        component: () => import('@/views/Declaration/Detail.vue'),
        meta: { 
          title: '申报详情', 
          requiresAuth: true,
          // 🔥 详情页和创建页不是独立菜单，继承列表页权限
          permission: 'menu:declaration:list',
          layout: 'main'
        }
      }
    ]
  },
  {
    path: '/project',
    component: RouterView,
    children: [
      {
        path: 'list',
        name: 'ProjectList',
        component: () => import('@/views/Project/List.vue'),
        meta: { 
          title: '项目列表', 
          requiresAuth: true,
          permission: 'menu:project:list',
          layout: 'main'
        }
      },
      {
        path: 'detail/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/Project/Detail.vue'),
        meta: { 
          title: '项目详情', 
          requiresAuth: true,
          permission: 'menu:project:detail',
          layout: 'main'
        }
      },
      {
        path: 'progress/:id',
        name: 'ProjectProgress',
        component: () => import('@/views/Project/Progress.vue'),
        meta: { 
          title: '项目进度', 
          requiresAuth: true,
          permission: 'menu:project:progress',
          layout: 'main'
        }
      }
    ]
  },
  {
    path: '/report',
    component: RouterView,
    children: [
      {
        path: 'list',
        name: 'ReportList',
        component: () => import('@/views/Report/List.vue'),
        meta: { 
          title: '报告列表', 
          requiresAuth: true,
          permission: 'menu:report:list',
          layout: 'main'
        }
      },
      {
        path: 'generate',
        name: 'ReportGenerate',
        component: () => import('@/views/Report/Generate.vue'),
        meta: { 
          title: '报告生成', 
          requiresAuth: true,
          permission: 'menu:report:generate',
          layout: 'main'
        }
      }
    ]
  },
  {
    path: '/knowledge',
    component: RouterView,
    children: [
      {
        path: 'list',
        name: 'KnowledgeList',
        component: () => import('@/views/Knowledge/List.vue'),
        meta: { 
          title: '知识库列表', 
          requiresAuth: true,
          permission: 'menu:knowledge:list',
          layout: 'main'
        }
      },
      {
        path: 'detail/:id',
        name: 'KnowledgeDetail',
        component: () => import('@/views/Knowledge/Detail.vue'),
        meta: { 
          title: '知识库详情', 
          requiresAuth: true,
          permission: 'menu:knowledge:detail',
          layout: 'main'
        }
      }
    ]
  },
  {
    path: '/ai/chat',
    name: 'AIChat',
    component: () => import('@/views/AI/Chat.vue'),
    meta: { 
      title: 'AI助手', 
      requiresAuth: true,
      permission: 'menu:ai:chat',
      layout: 'main'
    }
  },
  {
    path: '/user',
    component: RouterView,
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/User/Profile.vue'),
        meta: { 
          title: '个人信息', 
          requiresAuth: true,
          permission: 'menu:user:profile',
          layout: 'main'
        }
      },
      {
        path: 'security',
        name: 'UserSecurity',
        component: () => import('@/views/User/Security.vue'),
        meta: { 
          title: '安全设置', 
          requiresAuth: true,
          permission: 'menu:user:security',
          layout: 'main'
        }
      }
    ]
  },
  {
    path: '/system',
    component: RouterView,
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/System/User.vue'),
        meta: { 
          title: '用户管理', 
          requiresAuth: true,
          permission: 'menu:system:user',
          layout: 'main'
        }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/System/Role.vue'),
        meta: { 
          title: '角色权限', 
          requiresAuth: true,
          permission: 'menu:system:role',
          layout: 'main'
        }
      },
      {
        path: 'config',
        name: 'SystemConfig',
        component: () => import('@/views/System/Config.vue'),
        meta: { 
          title: '系统配置', 
          requiresAuth: true,
          permission: 'menu:system:config',
          layout: 'main'
        }
      },
      {
        path: 'logs',
        name: 'SystemLogs',
        component: () => import('@/views/System/Logs.vue'),
        meta: { 
          title: '日志管理', 
          requiresAuth: true,
          permission: 'menu:system:logs',
          layout: 'main'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory('/'),
  routes
})

// 登录/注册/忘记密码页面路径（不参与主题切换）
const AUTH_PAGES = ['/login', '/register', '/reset-password']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const publicPages = ['/login', '/register', '/reset-password']
  const authRequired = !publicPages.includes(to.path)
  const authStore = useAuthStore()

  // 🔥 关键修复：登录/注册/忘记密码页面强制使用明亮主题
  const isAuthPage = AUTH_PAGES.includes(to.path)
  if (isAuthPage) {
    const html = document.documentElement
    html.classList.remove('dark')
    html.setAttribute('data-theme', 'light')
  }

  // 路由守卫日志
  routerLogger.info('路由守卫触发', {
    to: to.path,
    from: from.path,
    isLoggedIn: authStore.isLoggedIn,
    permissionsCount: authStore.permissions?.length || 0,
    authRequired,
    isPublicPage: publicPages.includes(to.path)
  })

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 高校科研管理平台`
  }

  // 检查是否需要认证
  if (authRequired) {
      if (!authStore.isLoggedIn) {
        routerLogger.info('用户未登录，重定向到登录页面', { targetPath: to.path })
        ElMessage.error('请先登录')
        return next('/login')
      }
      
      try {
        const sessionValid = await authStore.verifyLoginStatus()
        if (!sessionValid) {
          // 检查最近 5 分钟内是否有网络错误（后端服务未启动）
          const now = Date.now()
          const hasRecentNetworkError = authStore.lastNetworkError && 
                                       (now - authStore.lastNetworkError) < 5 * 60 * 1000
          if (hasRecentNetworkError) {
            // 网络错误时，允许继续访问，避免阻塞用户操作
            routerLogger.warn('网络错误，跳过登录状态校验，允许继续访问', { 
              targetPath: to.path,
              lastNetworkError: new Date(authStore.lastNetworkError).toLocaleTimeString()
            })
            // 不阻止路由跳转，允许用户继续使用（前端可以独立开发调试）
          } else {
            // 非网络错误的情况，说明服务端会话已失效
            routerLogger.warn('服务端会话无效，重定向到登录页面', { targetPath: to.path })
            ElMessage.error('登录状态已过期，请重新登录')
            return next('/login')
          }
        }
      } catch (error) {
        // verifyLoginStatus 在网络错误时不会抛出异常，这里主要处理其他类型的错误
        const isNetworkError = error.code === 'ECONNREFUSED' || 
                               error.code === 'ECONNABORTED' || 
                               error.message?.includes('timeout') ||
                               !error.response
        if (isNetworkError) {
          routerLogger.warn('网络错误，跳过登录状态校验，允许继续访问', { 
            error: error.message,
            targetPath: to.path 
          })
          // 不阻止路由跳转
        } else {
          routerLogger.error('登录状态校验异常', { error: error.message })
          ElMessage.error('校验登录状态失败，请重新登录')
          return next('/login')
        }
      }
    
    // 检查页面权限 - 完全依赖后端返回的权限列表，不进行任何硬编码
    if (to.meta.permission) {
      // 🔥 关键修复：权限检查完全依赖后端返回的权限列表，不进行任何硬编码
      let hasPermission = authStore.hasPermission(to.meta.permission)
      
      // 如果本地权限检查失败，尝试调用服务端校验接口
      if (!hasPermission) {
        routerLogger.info('本地权限校验失败，尝试调用服务端校验接口', {
          requiredPermission: to.meta.permission,
          userPermissions: authStore.permissions
        })
        try {
          hasPermission = await authStore.validatePermission(to.meta.permission)
          if (hasPermission) {
            routerLogger.info('服务端确认具有权限，准备同步用户信息')
            await authStore.getUserInfo(true)
          }
        } catch (error) {
          routerLogger.error('服务端权限校验失败', {
            error: error.message,
            requiredPermission: to.meta.permission
          })
        }
      }
      
      if (!hasPermission) {
        routerLogger.warn('权限检查失败', { 
          requiredPermission: to.meta.permission, 
          userPermissions: authStore.permissions,
          userRoles: authStore.roles,
          targetPath: to.path
        })
        ElMessage.error('没有权限访问该页面')
        
        // 如果当前要访问的就是dashboard，则重定向到登录页面避免无限循环
        if (to.path === '/dashboard') {
          routerLogger.warn('无权限访问dashboard，重定向到登录页面')
          return next('/login')
        }
        
        // 重定向到有权限的页面
        return next('/dashboard')
      }
    }
  }

  // 如果已登录用户访问登录页面，重定向到仪表板
  if (to.path === '/login' && authStore.isLoggedIn) {
    routerLogger.info('已登录用户访问登录页面，重定向到仪表板')
    return next('/dashboard')
  }

  next()
})

// 路由解析完成后的日志
router.afterEach((to, from, failure) => {
  if (failure) {
    routerLogger.error('路由解析失败', { 
      to: to.path, 
      from: from.path, 
      failure: failure.message,
      error: failure
    })
  } else {
    const matchedRoute = to.matched[to.matched.length - 1]
    const componentName = matchedRoute?.components?.default?.name || 
                          matchedRoute?.components?.default?.__name ||
                          matchedRoute?.name ||
                          'Unknown'
    
    routerLogger.info('路由解析成功', { 
      to: to.path, 
      from: from.path,
      routeName: to.name,
      component: componentName,
      matchedRoutes: to.matched.map(r => r.name || r.path)
    })
  }
})

export default router