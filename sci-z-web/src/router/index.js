import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

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
    redirect: '/dashboard'
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
    path: '/component-test',
    name: 'ComponentTest',
    component: () => import('@/views/ComponentTest.vue'),
    meta: { title: '组件测试', layout: 'main' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register', '/reset-password']
  const authRequired = !publicPages.includes(to.path)

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 高校科研项目管理平台`
  }

  // 检查是否需要认证
  if (authRequired) {
    const token = localStorage.getItem('auth_token')
    if (!token) {
      // 暂时注释掉错误提示，避免在开发阶段干扰
      // ElMessage.error('请先登录')
      return next('/login')
    }
  }

  next()
})

export default router