<!--
/**
 * @description 基础卡片组件
 * 统一的卡片样式和布局，支持标题、内容、操作区域
 * 可在所有页面中复用，确保卡片风格统一
 */
-->
<template>
  <div class="base-card" :class="cardClass">
    <!-- 卡片头部 -->
    <div v-if="title || $slots.header" class="base-card__header">
      <slot name="header">
        <h3 v-if="title" class="base-card__title">{{ title }}</h3>
      </slot>
      <div v-if="$slots.actions" class="base-card__actions">
        <slot name="actions" />
      </div>
    </div>

    <!-- 卡片内容 -->
    <div class="base-card__content">
      <slot />
    </div>

    <!-- 卡片底部 -->
    <div v-if="$slots.footer" class="base-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props 定义
const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  hoverable: {
    type: Boolean,
    default: true
  },
  bordered: {
    type: Boolean,
    default: true
  },
  shadow: {
    type: String,
    default: 'sm',
    validator: (value) => ['sm', 'md', 'lg', 'none'].includes(value)
  }
})

// 计算属性
const cardClass = computed(() => ({
  'base-card--hoverable': props.hoverable,
  'base-card--bordered': props.bordered,
  [`base-card--shadow-${props.shadow}`]: props.shadow !== 'none'
}))
</script>

<style lang="scss" scoped>
.base-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.3s ease;

  &--bordered {
    border: 1px solid var(--border);
  }

  &--shadow-sm {
    box-shadow: var(--shadow-sm);
  }

  &--shadow-md {
    box-shadow: var(--shadow-md);
  }

  &--shadow-lg {
    box-shadow: var(--shadow-lg);
  }

  &--hoverable:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--gap-lg);
    border-bottom: 1px solid var(--border);
  }

  &__title {
    font-size: var(--font-size-xl);
    font-weight: 600;
    color: var(--color-primary);
    margin: 0;
  }

  &__actions {
    display: flex;
    gap: var(--gap-sm);
  }

  &__content {
    padding: var(--gap-lg);
  }

  &__footer {
    padding: var(--gap-lg);
    border-top: 1px solid var(--border);
    background: #fafafa;
  }
}
</style>
