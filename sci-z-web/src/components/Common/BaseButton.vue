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
</style>
