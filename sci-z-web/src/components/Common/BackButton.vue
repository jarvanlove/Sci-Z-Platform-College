<!--
/**
 * @description 通用返回按钮组件
 * 使用 logo 图标，支持主题和悬浮交互
 */
-->
<template>
  <el-tooltip :content="tooltip || $t('common.backToList')" placement="bottom">
    <div 
      class="back-button" 
      @click="handleClick"
      :class="{ 'back-button--disabled': disabled }"
    >
      <el-icon><ArrowLeft /></el-icon>
    </div>
  </el-tooltip>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElTooltip } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  /**
   * 工具提示文本
   */
  tooltip: {
    type: String,
    default: ''
  },
  /**
   * 是否禁用
   */
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  if (!props.disabled) {
    emit('click')
  }
}
</script>

<style lang="scss" scoped>
.back-button {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-2);
  background: var(--surface);
  border: 1px solid var(--border);
  
  &:hover:not(.back-button--disabled) {
    color: var(--color-primary);
    background: var(--hover);
    border-color: var(--color-primary);
    transform: translateX(-2px);
  }
  
  &:active:not(.back-button--disabled) {
    transform: translateX(-1px);
  }
  
  .el-icon {
    font-size: 18px;
  }
  
  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 暗色主题适配
[data-theme='dark'] .back-button,
.dark .back-button {
  background: var(--surface);
  border-color: var(--border);
  color: var(--text-2);
  
  &:hover:not(.back-button--disabled) {
    background: var(--hover);
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
}
</style>
