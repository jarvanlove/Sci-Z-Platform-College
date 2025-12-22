<!--
/**
 * @description 知识库列表页面
 * 极薄包装层，仅引入业务组件
 */
-->
<template>
  <div class="knowledge-list-view-wrapper">
    <KnowledgeList />
  </div>
</template>

<script setup>
import { KnowledgeList } from '@/components/Business/Knowledge'
// import { onMounted, onUnmounted } from 'vue'

// ========== 以下代码为布局调整代码，已注释以符合架构设计 ==========
// 架构设计：views 层应为极薄包装层，不应包含复杂的样式覆盖和 DOM 操作
// 如需处理滚动条问题，应在业务组件内部处理

// // 页面挂载时添加 body 和 html 的 class，确保不产生全局滚动条
// onMounted(() => {
//   document.body.classList.add('knowledge-page-active')
//   document.documentElement.classList.add('knowledge-page-active')
// })

// // 页面卸载时移除 class
// onUnmounted(() => {
//   document.body.classList.remove('knowledge-page-active')
//   document.documentElement.classList.remove('knowledge-page-active')
// })
</script>

<style lang="scss" scoped>
// ========== 以下样式为布局调整代码，已注释以符合架构设计 ==========
// 架构设计：views 层应为极薄包装层，不应包含复杂的样式覆盖
// 如需处理滚动条问题，应在业务组件内部处理

// // 知识库页面 - 只覆盖 MainLayout 的 overflow，去掉右侧滚动条
// // 最小化修改，不影响其他布局
// :deep(.main-content) {
//   overflow: hidden !important; // 🔥 只覆盖这个属性，去掉右侧滚动条
// }
</style>

<!-- ========== 以下全局样式为布局调整代码，已注释以符合架构设计 ========== -->
<!-- 架构设计：views 层应为极薄包装层，不应包含全局样式覆盖 -->
<!-- 如需处理滚动条问题，应在业务组件内部处理 -->
<!--
<style lang="scss">
// 知识库页面全局样式 - 确保 body 和 html 不产生滚动条
// 只针对知识库路由页面
body.knowledge-page-active {
  overflow: hidden !important;
  height: 100vh !important;
  max-height: 100vh !important;
}

html.knowledge-page-active {
  overflow: hidden !important;
  height: 100% !important;
}
</style>
-->
