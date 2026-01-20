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
    
    <!-- 全局认证弹窗 -->
    <LoginModal v-model="showLoginModal" />
    <RegisterModal v-model="showRegisterModal" />
    <ResetPasswordModal v-model="showResetPasswordModal" />
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import MainLayout from '@/components/Layout/MainLayout.vue'
import ToCLayout from '@/components/Layout/ToCLayout.vue'
import { LoginModal, RegisterModal, ResetPasswordModal } from '@/components/Business/Auth'
import { useAuthModal } from '@/composables/useAuthModal'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { showLoginModal, showRegisterModal, showResetPasswordModal, closeAllModals, openLoginModal, openRegisterModal, openResetPasswordModal } = useAuthModal()

// 监听登录状态，如果已登录则关闭所有弹窗
watch(() => authStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    closeAllModals()
  }
})

// 监听路由变化，当路由变化到 /login、/register 或 /reset-password 时，打开对应的弹窗
watch(() => route.path, (newPath) => {
  if (newPath === '/login') {
    // 如果路由是 /login，打开登录弹窗
    openLoginModal()
  } else if (newPath === '/register') {
    // 如果路由是 /register，打开注册弹窗
    openRegisterModal()
  } else if (newPath === '/reset-password') {
    // 如果路由是 /reset-password，打开重置密码弹窗
    openResetPasswordModal()
  }
}, { immediate: true })

// 获取路由应该使用的布局
const getLayout = (route) => {
  // 如果路由明确指定了 layout，使用路由的配置
  if (route.meta?.layout) {
    return route.meta.layout
  }
  
  // 注册、重置密码页面不使用布局（登录页面使用 TOC 布局显示弹窗）
  const noLayoutPages = ['/register', '/reset-password']
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
