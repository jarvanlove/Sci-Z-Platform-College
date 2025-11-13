import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页'
    }
  },
  {
    path: '/demo',
    name: 'Demo',
    component: () => import('../components/DifyDemo.vue'),
    meta: {
      title: 'Dify API 演示'
    }
  },
  {
    path: '/datasets',
    name: 'Datasets',
    component: () => import('../views/Datasets.vue'),
    meta: {
      title: '数据集管理'
    }
  },
  {
    path: '/query',
    name: 'Query',
    component: () => import('../views/Query.vue'),
    meta: {
      title: '知识库查询'
    }
  },
  {
    path: '/upload',
    name: 'DocumentUpload',
    component: () => import('../views/DocumentUpload.vue'),
    meta: {
      title: '文档上传'
    }
  },
  {
    path: '/create-dataset',
    name: 'CreateDataset',
    component: () => import('../views/CreateDataset.vue'),
    meta: {
      title: '创建知识库'
    }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue'),
    meta: {
      title: '关于'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      title: '页面未找到'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Dify API 演示平台`
  }
  next()
})

export default router
