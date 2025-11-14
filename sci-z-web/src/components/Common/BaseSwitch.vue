<!--
/**
 * @description 基础开关组件
 * 统一的开关样式，背景颜色与确认按钮一致
 * 可在所有页面中复用，确保开关风格统一
 */
-->
<template>
  <el-switch
    v-model="switchValue"
    :active-value="activeValue"
    :inactive-value="inactiveValue"
    :disabled="disabled"
    :size="size"
    @change="handleChange"
  />
</template>

<script setup>
import { computed } from 'vue'

// Props 定义
const props = defineProps({
  modelValue: {
    type: [Boolean, String, Number],
    default: false
  },
  activeValue: {
    type: [Boolean, String, Number],
    default: true
  },
  inactiveValue: {
    type: [Boolean, String, Number],
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  }
})

// Emits 定义
const emit = defineEmits(['update:modelValue', 'change'])

// 计算属性
const switchValue = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
    emit('change', value)
  }
})

// 事件处理
const handleChange = (value) => {
  emit('change', value)
}
</script>

<style lang="scss" scoped>
// 覆盖 Element Plus Switch 样式，使其背景颜色与确认按钮一致
// 使用更高优先级的选择器确保样式生效
:deep(.el-switch) {
  .el-switch__core {
    background-color: var(--border) !important;
    border-color: var(--border) !important;
    width: 40px !important;
    height: 20px !important;
    transition: all 0.3s ease !important;
    
    &::after {
      background-color: var(--surface) !important;
      width: 16px !important;
      height: 16px !important;
      transition: all 0.3s ease !important;
    }
  }
  
  &.is-checked {
    .el-switch__core {
      background-color: var(--color-primary) !important;
      border-color: var(--color-primary) !important;
      
      &::after {
        left: calc(100% - 18px) !important;
      }
    }
  }
  
  &:hover:not(.is-disabled) {
    .el-switch__core {
      border-color: var(--color-primary) !important;
      
      &:not(.is-checked) {
        background-color: var(--border-hover) !important;
      }
      
      &.is-checked {
        background-color: var(--color-primary-dark) !important;
        border-color: var(--color-primary-dark) !important;
      }
    }
  }
  
  /* 暗色主题下开关悬浮样式 */
  [data-theme='dark'] &:hover:not(.is-disabled) .el-switch__core.is-checked,
  .dark &:hover:not(.is-disabled) .el-switch__core.is-checked {
    background-color: var(--color-primary-light) !important;
    border-color: var(--color-primary-light) !important;
  }
  
  &.is-disabled {
    opacity: 0.5;
  }
}

</style>

