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
    meta: { title: '登录', requiresAuth: false, layout: 'toc' } // 🔥 修复：使用 TOC 布局，显示弹窗而不是旧页面
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
    redirect: '/ai/chat' // 默认跳转到AI对话（公开页面），路由守卫会根据登录状态进一步处理
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard/index.vue'),
    meta: { 
      title: '仪表板', 
      requiresAuth: true,
      permission: 'menu:dashboard:view',
      layout: 'toc' // 🔥 修复：使用 ToC 布局
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
          layout: 'toc' // 使用 ToC 布局
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
          layout: 'toc' // 使用 ToC 布局
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
          layout: 'toc' // 使用 ToC 布局
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
          layout: 'toc' // 使用 ToC 布局
        }
      },
      {
        path: 'detail/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/Project/Detail.vue'),
        meta: { 
          title: '项目详情', 
          requiresAuth: true,
          // 🔥 详情页和进度页不是独立菜单，继承列表页权限
          permission: 'menu:project:list',
          layout: 'toc' // 使用 ToC 布局
        }
      },
      {
        path: 'progress/:id',
        name: 'ProjectProgress',
        component: () => import('@/views/Project/Progress.vue'),
        meta: { 
          title: '项目进度', 
          requiresAuth: true,
          // 🔥 详情页和进度页不是独立菜单，继承列表页权限
          permission: 'menu:project:list',
          layout: 'toc' // 使用 ToC 布局
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
          layout: 'toc' // 使用 ToC 布局
        }
      },
      {
        path: 'generate',
        name: 'ReportGenerate',
        component: () => import('@/views/Report/Generate.vue'),
        meta: { 
          title: '报告生成', 
          requiresAuth: true,
          // 🔥 报告生成页不是独立菜单，继承列表页权限
          permission: 'menu:report:list',
          layout: 'toc' // 使用 ToC 布局
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
          title: '知识库', 
          requiresAuth: true, // 需要登录
          permission: 'menu:knowledge:list',
          layout: 'toc' // 使用 ToC 布局
        }
      }
    ]
  },
  {
    path: '/practice',
    name: 'Practice',
    component: () => import('@/views/Practice/index.vue'),
    meta: { 
      title: '实践', 
      requiresAuth: true, // 需要登录
      layout: 'toc' // 使用 ToC 布局
    }
  },
  {
    path: '/literature',
    component: RouterView,
    children: [
      {
        path: 'search',
        name: 'LiteratureSearch',
        component: () => import('@/views/Literature/Search.vue'),
        meta: { 
          title: '文献搜索', 
          requiresAuth: false, // 不需要登录即可访问，但功能受限
          layout: 'toc' // 使用 ToC 布局
        }
      },
      {
        path: 'detail/:id',
        name: 'LiteratureDetail',
        component: () => import('@/views/Literature/Detail.vue'),
        meta: { 
          title: '文献详情', 
          requiresAuth: true,
          // 详情页继承搜索页权限
          permission: 'menu:literature:search',
          layout: 'toc' // 使用 ToC 布局
        }
      }
    ]
  },
  {
    path: '/ai/chat',
    name: 'AIChat',
    component: () => import('@/views/AI/Chat.vue').catch(err => {
      console.error('[Router] AI Chat 组件加载失败', err)
      // 返回一个错误组件
      return {
        template: '<div style="padding: 20px; text-align: center;"><h3>组件加载失败</h3><p>请刷新页面重试</p></div>'
      }
    }),
    meta: { 
      title: 'AI助手', 
      requiresAuth: false, // 不需要登录即可访问，但功能受限
      layout: 'toc' // 使用 ToC 布局
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
          layout: 'toc' // 🔥 修复：使用 ToC 布局，在侧边栏右侧显示
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
          layout: 'toc' // 🔥 修复：使用 ToC 布局，在侧边栏右侧显示
        }
      }
    ]
  },
  {
    path: '/manual',
    name: 'Manual',
    component: () => import('@/views/Manual/index.vue'),
    meta: { 
      title: '操作手册', 
      requiresAuth: true,
      layout: 'toc' // 🔥 修复：使用 ToC 布局
    }
  },
  {
    path: '/system',
    component: RouterView,
    children: [
      {
        path: '',
        name: 'SystemIndex',
        component: () => import('@/views/System/index.vue'),
        meta: { 
          title: '系统设置', 
          requiresAuth: true,
          layout: 'toc'
        }
      },
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/System/User.vue'),
        meta: { 
          title: '用户管理', 
          requiresAuth: true,
          permission: 'menu:system:user',
          layout: 'toc' // 🔥 修复：使用 ToC 布局
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
          layout: 'toc' // 🔥 修复：使用 ToC 布局
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
          layout: 'toc' // 🔥 修复：使用 ToC 布局
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
          layout: 'toc' // 🔥 修复：使用 ToC 布局
        }
      },
      {
        path: 'apikey',
        name: 'SystemApiKey',
        component: () => import('@/views/System/ApiKey.vue'),
        meta: { 
          title: 'API Key 配置', 
          requiresAuth: true,
          permission: 'menu:system:apikey',
          layout: 'toc' // 🔥 修复：使用 ToC 布局
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
  const authStore = useAuthStore()
  
  // 🔥 处理根路径重定向：始终跳转到AI对话页面
  // 当访问根路径时，Vue Router 会先执行 redirect 到 /ai/chat，然后进入路由守卫
  // 这里不再重定向到知识库，保持默认在AI对话页面
  if (to.path === '/') {
    routerLogger.info('访问根路径，重定向到AI对话')
    return next('/ai/chat')
  }
  
  // 公开页面：登录、注册、重置密码、AI对话、文献搜索
  const publicPages = ['/login', '/register', '/reset-password', '/ai/chat', '/literature/search']
  // 如果路由明确标记为不需要认证，也视为公开页面
  const isPublicPage = publicPages.includes(to.path) || to.meta.requiresAuth === false
  const authRequired = !isPublicPage

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
    document.title = `${to.meta.title} - AI.科研管理平台`
  }

  // 检查是否需要认证
  if (authRequired) {
      if (!authStore.isLoggedIn) {
        routerLogger.info('用户未登录，重定向到AI对话页面', { targetPath: to.path })
        // 🔥 修改：未登录用户访问任何需要认证的页面，都重定向到 /ai/chat
        return next('/ai/chat')
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
            routerLogger.warn('服务端会话无效，重定向到AI对话页面', { targetPath: to.path })
            ElMessage.error('登录状态已过期，请重新登录')
            // 🔥 修改：会话失效时重定向到 /ai/chat
            return next('/ai/chat')
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
          // 🔥 修改：校验失败时重定向到 /ai/chat
          return next('/ai/chat')
        }
      }
    
    // 检查页面权限 - 完全依赖后端返回的权限列表，不进行任何硬编码
    // 只有需要登录的页面才检查权限
    if (to.meta.permission && authRequired) {
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
        
        // 如果当前要访问的就是dashboard，则重定向到AI对话页面
        if (to.path === '/dashboard') {
          routerLogger.warn('无权限访问dashboard，重定向到AI对话页面')
          return next('/ai/chat')
        }
        
        // 重定向到AI对话页面
        return next('/ai/chat')
      }
    }
  }

  // 🔥 修复：允许 /login 路由存在，通过 App.vue 的监听器打开弹窗，但不立即重定向（保持 /login 路由）
  // 用户希望看到 /login 路由，所以不重定向
  if (to.path === '/login') {
    if (authStore.isLoggedIn) {
      routerLogger.info('已登录用户访问登录页面，重定向到AI对话页面')
      return next('/ai/chat')
    } else {
      // 🔥 修复：允许路由保持在 /login，不重定向
      routerLogger.info('未登录用户访问登录页面，打开弹窗，保持 /login 路由', { path: to.path })
      return next()
    }
  }
  
  // 🔥 修改：未登录用户访问注册/重置密码页面，允许访问（会以弹窗形式显示）
  // 如果需要弹窗形式，可以在对应的页面组件中处理
  // 暂时允许直接访问这些页面
  // if ((to.path === '/register' || to.path === '/reset-password') && !authStore.isLoggedIn) {
  //   routerLogger.info('未登录用户访问认证页面，重定向到AI对话页面', { targetPath: to.path })
  //   return next('/ai/chat')
  // }

  next()
})

// 路由解析完成后的日志
router.afterEach((to, from, failure) => {
  // 🔥 修复：不再在 afterEach 中重定向 /login，保持路由为 /login
  
  if (failure) {
    // 🔥 修复：过滤冗余导航错误，这是 Vue Router 的正常优化行为，不应该记录为错误
    const isRedundantNavigation = failure.message?.includes('Avoided redundant navigation') ||
                                  failure.message?.includes('redundant navigation')
    
    if (isRedundantNavigation) {
      // 冗余导航是正常的优化行为，只记录为调试信息，不记录为错误
      routerLogger.debug('路由冗余导航已阻止（正常优化）', { 
        to: to.path, 
        from: from.path
      })
    } else {
      // 其他类型的路由失败才记录为错误
      routerLogger.error('路由解析失败', { 
        to: to.path, 
        from: from.path, 
        failure: failure.message,
        error: failure
      })
    }
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