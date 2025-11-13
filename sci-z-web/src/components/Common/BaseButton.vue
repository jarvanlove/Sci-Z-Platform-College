<!--
/**
 * @description 基础按钮组件
 * 统一的按钮样式和交互效果，支持多种类型和尺寸
 * 可在所有页面中复用，确保按钮风格统一
 */
-->
<template>
  <el-button
    :type="type"
    :size="size"
    :loading="loading"
    :disabled="disabled"
    :icon="icon"
    :class="buttonClass"
    @click="handleClick"
  >
    <slot />
  </el-button>
</template>

<script setup>
import { computed } from 'vue'

// Props 定义
const props = defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'danger', 'info', 'text'].includes(value)
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  icon: {
    type: [String, Object],
    default: null
  },
  block: {
    type: Boolean,
    default: false
  }
})

// Emits 定义
const emit = defineEmits(['click'])

// 计算属性
const buttonClass = computed(() => ({
  'base-button': true,
  'base-button--block': props.block
}))

// 事件处理
const handleClick = (event) => {
  if (!props.loading && !props.disabled) {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
.base-button {
  font-weight: 500;
  transition: all 0.2s ease;
  
  &--block {
    width: 100%;
  }
}

// 确保 BaseButton 的样式优先级足够高
:deep(.el-button) {
  &.base-button {
    // 主要按钮样式已在全局 design-system.scss 中定义
    // 这里确保样式正确应用
  }
}
</style>
