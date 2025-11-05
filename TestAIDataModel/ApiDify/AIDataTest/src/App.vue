<script setup lang="ts">
import Navigation from './components/Navigation.vue'
</script>

<template>
  <div id="app">
    <Navigation />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 0;
}

/* 路由过渡动画 */
.router-enter-active,
.router-leave-active {
  transition: opacity 0.3s ease;
}

.router-enter-from,
.router-leave-to {
  opacity: 0;
}
</style>
