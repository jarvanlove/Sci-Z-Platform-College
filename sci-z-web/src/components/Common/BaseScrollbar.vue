<!--
/**
 * @description 滚动条通用组件
 * 提供统一的滚动条样式，支持不同尺寸和自定义样式
 * 包含滚动条轨道、滑块、角落等完整样式定义
 */
-->
<template>
  <div 
    class="base-scrollbar" 
    :class="[customClass, scrollbarSize]"
    :style="customStyle"
  >
    <div 
      class="scrollbar-content"
      :style="contentStyle"
    >
      <slot />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

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
  // 内容区域样式
  contentStyle: {
    type: Object,
    default: () => ({})
  }
})

// 滚动条大小类名
const scrollbarSize = computed(() => `scrollbar--${props.size}`)
</script>

<style lang="scss" scoped>
.base-scrollbar {
  overflow: auto;
  
  // 滚动条轨道
  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
    border-radius: 4px;
  }
  
  // 滚动条滑块
  &::-webkit-scrollbar-thumb {
    background: var(--color-border);
    border-radius: 4px;
    transition: all 0.3s ease;
    
    &:hover {
      background: var(--color-border-hover);
    }
  }
  
  // 滚动条角落
  &::-webkit-scrollbar-corner {
    background: transparent;
  }
  
  // 不同尺寸
  &.scrollbar--small {
    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }
  }
  
  &.scrollbar--large {
    &::-webkit-scrollbar {
      width: 12px;
      height: 12px;
    }
  }
}

// 全局滚动条样式（用于页面级别）
:global(.global-scrollbar) {
  overflow: auto;
  
  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: var(--color-border);
    border-radius: 4px;
    transition: all 0.3s ease;
    
    &:hover {
      background: var(--color-border-hover);
    }
  }
  
  &::-webkit-scrollbar-corner {
    background: transparent;
  }
}
</style>
