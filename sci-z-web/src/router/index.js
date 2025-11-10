import { createRouter, createWebHistory } from 'vue-router'
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
          permission: 'menu:declaration:create',
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
          permission: 'menu:declaration:detail',
          layout: 'main'
        }
      }
    ]
  },
  {
    path: '/project',
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

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const publicPages = ['/login', '/register', '/reset-password']
  const authRequired = !publicPages.includes(to.path)
  const authStore = useAuthStore()

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
          routerLogger.warn('服务端会话无效，重定向到登录页面', { targetPath: to.path })
          ElMessage.error('登录状态已过期，请重新登录')
          return next('/login')
        }
      } catch (error) {
        routerLogger.error('登录状态校验异常', { error: error.message })
        ElMessage.error('校验登录状态失败，请重新登录')
        return next('/login')
      }
    
    // 检查页面权限
    if (to.meta.permission) {
      let hasPermission = authStore.hasPermission(to.meta.permission)
      
      if (!hasPermission) {
        routerLogger.info('本地权限校验失败，尝试调用服务端校验接口', {
          requiredPermission: to.meta.permission
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
      failure: failure.message 
    })
  } else {
    routerLogger.info('路由解析成功', { 
      to: to.path, 
      from: from.path,
      component: to.matched[0]?.components?.default?.name || 'Unknown'
    })
  }
})

export default router