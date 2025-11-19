<template>
  <div class="main-layout">
    <Header />
    <div class="layout-content">
      <Sidebar />
      <main class="main-content">
        <div class="content-wrapper">
          <slot>
            <router-view />
          </slot>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import Header from './Header.vue'
import Sidebar from './Sidebar.vue'
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.layout-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.main-content {
  flex: 1;
  overflow: auto;
  background: var(--bg);
  padding: 16px;
  
  .content-wrapper {
    max-width: 1400px;
    margin: 0 auto;
    min-height: calc(100vh - 108px); // 减去 header 和 padding
  }
}

// 响应式设计
@media (max-width: 768px) {
  .main-content {
    padding: 12px;
    
    .content-wrapper {
      min-height: calc(100vh - 88px);
    }
  }
}
</style>
