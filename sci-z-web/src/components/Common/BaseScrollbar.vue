<!--
/**
 * @description 滚动条通用组件
 * 提供统一的滚动条样式，支持不同尺寸和自定义样式
 * 包含滚动条轨道、滑块、角落等完整样式定义
 */
-->
<template>
  <div 
    ref="scrollbarRef"
    class="base-scrollbar" 
    :class="[customClass, scrollbarSize, { 'scrollbar-visible': isScrollbarVisible }]"
    :style="customStyle"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @scroll="handleScroll"
    @mousemove="handleMouseMove"
  >
    <slot />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'

// Props
const props = defineProps({
  // 滚动条大小
  size: {
    type: String,
    default: 'medium', // small, medium, large
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },
  // 自定义样式
  customStyle: {
    type: Object,
    default: () => ({})
  },
  // 是否启用自动隐藏（默认启用）
  autoHide: {
    type: Boolean,
    default: true
  }
})

// 滚动条大小类名
const scrollbarSize = computed(() => `scrollbar--${props.size}`)

// 滚动条可见性控制
const scrollbarRef = ref(null)
const isScrollbarVisible = ref(!props.autoHide) // 如果启用自动隐藏，初始状态为隐藏
let hideTimer = null

// 显示滚动条
const showScrollbar = () => {
  if (!props.autoHide) {
    isScrollbarVisible.value = true
    return
  }
  
  isScrollbarVisible.value = true
  
  // 清除之前的隐藏定时器
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
  
  // 2秒后隐藏
  hideTimer = setTimeout(() => {
    isScrollbarVisible.value = false
    hideTimer = null
  }, 2000)
}

// 鼠标进入
const handleMouseEnter = () => {
  if (props.autoHide) {
    showScrollbar()
  }
}

// 鼠标离开
const handleMouseLeave = () => {
  if (props.autoHide && hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = setTimeout(() => {
      isScrollbarVisible.value = false
      hideTimer = null
    }, 2000)
  }
}

// 鼠标移动
const handleMouseMove = () => {
  if (props.autoHide) {
    showScrollbar()
  }
}

// 滚动事件
const handleScroll = () => {
  if (props.autoHide) {
    showScrollbar()
  }
}

// 组件挂载后，如果禁用自动隐藏，确保滚动条可见
onMounted(() => {
  if (!props.autoHide) {
    isScrollbarVisible.value = true
  }
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
})
</script>

<style lang="scss">
/* 🔥 使用非 scoped 样式，确保伪元素选择器生效 */
/* 使用高优先级选择器，确保样式不被覆盖 */
.base-scrollbar.base-scrollbar {
  overflow: auto !important;
  
  // 滚动条轨道
  &::-webkit-scrollbar {
    width: 8px !important;
    height: 8px !important;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent !important;
    border-radius: 4px !important;
  }
  
  // 滚动条滑块
  &::-webkit-scrollbar-thumb {
    background: var(--border) !important;
    border-radius: 4px !important;
    transition: opacity 0.3s ease, background 0.3s ease !important;
    
    &:hover {
      background: var(--border-hover) !important;
    }
  }
  
  // 🔥 自动隐藏功能：默认隐藏滚动条
  &:not(.scrollbar-visible) {
    &::-webkit-scrollbar-thumb {
      opacity: 0 !important;
    }
    
    &::-webkit-scrollbar-track {
      opacity: 0 !important;
    }
  }
  
  // 显示滚动条
  &.scrollbar-visible {
    &::-webkit-scrollbar-thumb {
      opacity: 1 !important;
    }
    
    &::-webkit-scrollbar-track {
      opacity: 1 !important;
    }
  }
  
  // 滚动条角落
  &::-webkit-scrollbar-corner {
    background: transparent !important;
  }
  
  // 不同尺寸
  &.scrollbar--small {
    &::-webkit-scrollbar {
      width: 6px !important;
      height: 6px !important;
    }
  }
  
  &.scrollbar--large {
    &::-webkit-scrollbar {
      width: 12px !important;
      height: 12px !important;
    }
  }
  
  // Firefox 滚动条样式
  scrollbar-width: thin !important;
  scrollbar-color: var(--border) transparent !important;
  
  // 自动隐藏时 Firefox 滚动条也隐藏
  &:not(.scrollbar-visible) {
    scrollbar-width: none !important;
  }
  
  &.scrollbar-visible {
    scrollbar-width: thin !important;
  }
}

</style>
