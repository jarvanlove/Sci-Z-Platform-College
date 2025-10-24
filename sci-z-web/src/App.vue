<template>
  <div id="app">
    <router-view v-slot="{ Component, route }">
      <template v-if="Component">
        <MainLayout v-if="route.meta?.layout === 'main'">
          <component :is="Component" />
        </MainLayout>
        <component v-else :is="Component" />
      </template>
    </router-view>
  </div>
</template>

<script setup>
import MainLayout from '@/components/Layout/MainLayout.vue'
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100vh;
  font-family: Inter, system-ui, -apple-system, Segoe UI, Roboto, PingFang SC, Microsoft YaHei, sans-serif;
}
</style>
