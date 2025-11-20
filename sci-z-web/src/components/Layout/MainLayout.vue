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
  // 🔥 响应式内边距：大屏幕上减少内边距，充分利用空间
  padding: 16px;
  
  @media (min-width: 1920px) {
    padding: 16px 24px; // 大屏幕：左右内边距稍大，上下保持
  }
  
  @media (min-width: 2560px) {
    padding: 16px 32px; // 超大屏幕：进一步优化内边距
  }
  
  .content-wrapper {
    // 🔥 响应式最大宽度：在不同屏幕尺寸下自适应
    // 移除固定的 max-width: 1400px，让内容充分利用可用空间
    // 小屏幕（< 1200px）：100% 宽度
    // 中等屏幕（1200px - 1920px）：100% 宽度，充分利用
    // 大屏幕（> 1920px）：100% 宽度，适合大屏投影
    max-width: 100%;
    margin: 0;
    min-height: calc(100vh - 108px); // 减去 header 和 padding
    width: 100%;
    box-sizing: border-box;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .main-content {
    padding: 12px;
    
    .content-wrapper {
      min-height: calc(100vh - 88px);
      padding: 0;
    }
  }
}
</style>
