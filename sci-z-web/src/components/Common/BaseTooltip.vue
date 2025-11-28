<!--
/**
 * @description 通用 Tooltip 组件
 * 统一封装 Element Plus 的 el-tooltip，提供一致的样式和交互体验
 * 可在所有页面中复用，确保提示风格统一
 */
-->
<template>
  <el-tooltip
    :content="content"
    :placement="placement"
    :effect="effect"
    :disabled="disabled"
    :offset="offset"
    :show-after="showAfter"
    :hide-after="hideAfter"
    :popper-class="popperClass"
  >
    <slot />
  </el-tooltip>
</template>

<script setup>
/**
 * Props 定义
 */
const props = defineProps({
  /**
   * 提示内容
   */
  content: {
    type: String,
    required: true
  },
  /**
   * 提示位置
   * @values top/top-start/top-end/bottom/bottom-start/bottom-end/left/left-start/left-end/right/right-start/right-end
   */
  placement: {
    type: String,
    default: 'top',
    validator: (value) => [
      'top', 'top-start', 'top-end',
      'bottom', 'bottom-start', 'bottom-end',
      'left', 'left-start', 'left-end',
      'right', 'right-start', 'right-end'
    ].includes(value)
  },
  /**
   * 主题
   * @values dark/light
   */
  effect: {
    type: String,
    default: 'dark',
    validator: (value) => ['dark', 'light'].includes(value)
  },
  /**
   * 是否禁用
   */
  disabled: {
    type: Boolean,
    default: false
  },
  /**
   * 出现位置的偏移量
   */
  offset: {
    type: Number,
    default: 0
  },
  /**
   * 延迟显示，单位毫秒
   */
  showAfter: {
    type: Number,
    default: 0
  },
  /**
   * 延迟隐藏，单位毫秒
   */
  hideAfter: {
    type: Number,
    default: 0
  },
  /**
   * 为 Tooltip 的 popper 添加类名
   */
  popperClass: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss">
// 全局 Tooltip 样式（与 Element Plus 默认样式保持一致）
// 注意：这里不使用 scoped，因为 tooltip 是挂载到 body 的
// 只做最小化的样式调整，保持与 Element Plus 默认样式一致
.el-tooltip__popper {
  max-width: 300px;
  word-wrap: break-word;
  
  // 确保 dark 主题样式与 Element Plus 默认一致
  &.is-dark {
    background-color: rgba(0, 0, 0, 0.75) !important;
    color: #fff !important;
  }
  
  // light 主题样式
  &.is-light {
    background-color: #fff !important;
    color: #606266 !important;
    border: 1px solid var(--border) !important;
  }
}
</style>

