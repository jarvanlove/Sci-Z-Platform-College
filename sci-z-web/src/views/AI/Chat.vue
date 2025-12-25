<!--
/**
 * @description AI对话页面
 * 极薄包装层，仅引入业务组件
 */
-->
<template>
  <div class="ai-chat-view-wrapper" ref="viewWrapperRef">
    <!-- 🔥 调试：确保视图层已渲染 -->
    <div v-if="showDebug" style="position: fixed; top: 60px; left: 10px; z-index: 99999; background: red; color: white; padding: 4px; font-size: 12px;">
      Chat.vue 视图层已加载
    </div>
    <!-- 🔥 错误提示 -->
    <div v-if="componentError" style="padding: 20px; text-align: center; color: red;">
      <h3>组件加载失败</h3>
      <p>{{ componentError }}</p>
      <button @click="componentError = null; aiChatReady = true">重试</button>
    </div>
    <!-- 🔥 组件渲染 -->
    <AIChat v-else-if="aiChatReady" ref="aiChatRef" />
    <div v-else style="padding: 20px; text-align: center;">
      正在加载 AI 对话组件...
    </div>
  </div>
</template>

<script setup>
import { onMounted, nextTick, ref, onErrorCaptured } from 'vue'

// 🔥 修复：直接导入组件，避免可能的导出问题
import AIChat from '@/components/Business/AI/AIChat.vue'

const viewWrapperRef = ref(null)
const aiChatRef = ref(null)
const aiChatReady = ref(true) // 组件是否准备好
const showDebug = ref(false) // 生产环境设为 false
const componentError = ref(null) // 组件错误

// 🔥 捕获组件渲染错误
onErrorCaptured((err, instance, info) => {
  console.error('[Chat View] 组件渲染错误', { err, instance, info })
  componentError.value = err.message || String(err)
  return false // 阻止错误继续传播
})

// 🔥 修复：确保组件挂载后强制应用样式
onMounted(async () => {
  console.log('[Chat View] onMounted 开始')
  
  // 等待多个 nextTick，确保所有组件都已渲染
  await nextTick()
  await nextTick()
  await nextTick()
  
  console.log('[Chat View] 检查组件状态', {
    viewWrapper: !!viewWrapperRef.value,
    aiChatRef: !!aiChatRef.value,
    AIChatComponent: typeof AIChat,
    containerInDOM: !!document.querySelector('.ai-chat-container'),
    wrapperInDOM: !!document.querySelector('.ai-chat-view-wrapper')
  })
  
  // 给 body 或根元素添加类名，用于样式选择器
  document.body.classList.add('ai-chat-page-active')
  
  // 强制移除 MainLayout 的 padding
  const mainContent = document.querySelector('.main-content')
  if (mainContent) {
    mainContent.style.padding = '0'
    mainContent.style.height = '100%'
    mainContent.style.overflow = 'hidden'
    mainContent.classList.add('ai-chat-page-active')
  }
  
  const contentWrapper = document.querySelector('.content-wrapper')
  if (contentWrapper) {
    contentWrapper.style.padding = '0'
    contentWrapper.style.margin = '0'
    contentWrapper.style.height = '100%'
    contentWrapper.style.overflow = 'hidden'
    contentWrapper.classList.add('ai-chat-page-active')
  }
  
  const layoutContent = document.querySelector('.layout-content')
  if (layoutContent) {
    layoutContent.style.height = '100%'
    layoutContent.style.overflow = 'hidden'
    layoutContent.classList.add('ai-chat-page-active')
  }
  
  // 🔥 再次检查容器（延迟检查，确保组件已渲染）
  setTimeout(() => {
    const container = document.querySelector('.ai-chat-container')
    console.log('[AIChat View] 延迟检查容器', {
      mainContent: !!mainContent,
      contentWrapper: !!contentWrapper,
      layoutContent: !!layoutContent,
      container: !!container,
      containerHeight: container?.offsetHeight || 0,
      containerWidth: container?.offsetWidth || 0,
      containerDisplay: container ? window.getComputedStyle(container).display : 'null',
      containerVisibility: container ? window.getComputedStyle(container).visibility : 'null',
      // 🔥 检查 wrapper 的子元素
      wrapperChildren: viewWrapperRef.value ? viewWrapperRef.value.children.length : 0,
      wrapperHTML: viewWrapperRef.value ? viewWrapperRef.value.innerHTML.substring(0, 500) : 'null'
    })
    
    // 🔥 如果容器仍然不存在，尝试手动创建或提示
    if (!container) {
      console.error('[AIChat View] ❌ 容器元素仍然不存在！')
      console.error('[AIChat View] 检查 wrapper 内容:', viewWrapperRef.value?.innerHTML)
      console.error('[AIChat View] 检查 AIChat 组件:', aiChatRef.value)
    }
  }, 500)
  
  console.log('[AIChat View] 样式已强制应用', {
    mainContent: !!mainContent,
    contentWrapper: !!contentWrapper,
    layoutContent: !!layoutContent,
    container: !!document.querySelector('.ai-chat-container')
  })
})
</script>

<style lang="scss" scoped>
// 🔥 修复：在视图层也添加样式覆盖，确保优先级
.ai-chat-view-wrapper {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>

<style lang="scss">
// 🔥 修复：使用全局样式，确保覆盖 MainLayout 的样式（不使用 scoped）
// 这样在生产环境构建后也能生效
.main-content:has(.ai-chat-container),
.main-content .content-wrapper:has(.ai-chat-container) {
  padding: 0 !important;
  margin: 0 !important;
  height: 100% !important;
  max-width: 100% !important;
  overflow: hidden !important;
}

.layout-content:has(.ai-chat-container) {
  height: 100% !important;
  overflow: hidden !important;
}

// 如果浏览器不支持 :has()，使用类名选择器作为降级方案
.ai-chat-page-active .main-content,
.ai-chat-page-active .main-content .content-wrapper {
  padding: 0 !important;
  margin: 0 !important;
  height: 100% !important;
  max-width: 100% !important;
  overflow: hidden !important;
}

.ai-chat-page-active .layout-content {
  height: 100% !important;
  overflow: hidden !important;
}
</style>
