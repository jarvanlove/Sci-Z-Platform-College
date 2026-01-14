<template>
  <div id="app">
    <router-view v-slot="{ Component, route }">
      <template v-if="Component">
        <!-- ToC 布局（默认） -->
        <ToCLayout v-if="getLayout(route) === 'toc'">
          <component :is="Component" />
        </ToCLayout>
        <!-- Main 布局（管理型，用于系统管理等功能） -->
        <MainLayout v-else-if="getLayout(route) === 'main'">
          <component :is="Component" />
        </MainLayout>
        <!-- 无布局（登录页等） -->
        <component v-else :is="Component" />
      </template>
    </router-view>
    
    <!-- 全局登录弹窗 -->
    <LoginModal v-model="showLoginModal" />
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useAuthStore } from '@/store/modules/auth'
import MainLayout from '@/components/Layout/MainLayout.vue'
import ToCLayout from '@/components/Layout/ToCLayout.vue'
import { LoginModal } from '@/components/Business/Auth'
import { useLoginModal } from '@/composables/useLoginModal'

const authStore = useAuthStore()
const { showLoginModal } = useLoginModal()

// 监听登录状态，如果已登录则关闭弹窗
watch(() => authStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    showLoginModal.value = false
  }
})

// 获取路由应该使用的布局
const getLayout = (route) => {
  // 如果路由明确指定了 layout，使用路由的配置
  if (route.meta?.layout) {
    return route.meta.layout
  }
  
  // 登录、注册、重置密码页面不使用布局
  const noLayoutPages = ['/login', '/register', '/reset-password']
  if (noLayoutPages.includes(route.path)) {
    return null
  }
  
  // 默认使用 ToC 布局
  return 'toc'
}
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100vh;
  font-family: Inter, system-ui, -apple-system, Segoe UI, Roboto, PingFang SC, Microsoft YaHei, sans-serif;
}
</style>
