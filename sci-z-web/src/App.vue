<template>
  <div id="app">
    <router-view v-slot="{ Component, route }">
      <template v-if="Component">
        <MainLayout v-if="shouldUseMainLayout(route)">
          <component :is="Component" />
        </MainLayout>
        <component v-else :is="Component" />
      </template>
    </router-view>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/store/modules/auth'
import MainLayout from '@/components/Layout/MainLayout.vue'

const authStore = useAuthStore()

// 判断是否应该使用MainLayout
const shouldUseMainLayout = (route) => {
  // 如果路由明确指定了layout，使用路由的配置
  if (route.meta?.layout === 'main') {
    return true
  }
  
  // 如果是404页面且用户已登录，也应该使用MainLayout
  if (route.name === 'NotFound' && authStore.isLoggedIn) {
    return true
  }
  
  return false
}
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100vh;
  font-family: Inter, system-ui, -apple-system, Segoe UI, Roboto, PingFang SC, Microsoft YaHei, sans-serif;
}
</style>
