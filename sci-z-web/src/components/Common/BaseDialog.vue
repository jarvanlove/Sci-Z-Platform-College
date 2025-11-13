<!--
/**
 * @description 基础弹窗组件
 * 统一的弹窗样式和交互，支持多种类型和自定义内容
 * 可在所有页面中复用，确保弹窗风格统一
 */
-->
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    :class="dialogClass"
    @open="handleOpen"
    @close="handleClose"
  >
    <!-- 弹窗内容 -->
    <BaseScrollbar 
      class="base-dialog__content"
      size="small"
      custom-class="dialog-scrollbar"
    >
      <slot />
    </BaseScrollbar>

    <!-- 弹窗底部操作区 -->
    <template v-if="$slots.footer" #footer>
      <div class="base-dialog__footer">
        <slot name="footer" />
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, watch } from 'vue'
import BaseScrollbar from './BaseScrollbar.vue'

// Props 定义
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  width: {
    type: [String, Number],
    default: '600px'
  },
  closeOnClickModal: {
    type: Boolean,
    default: false
  },
  closeOnPressEscape: {
    type: Boolean,
    default: true
  },
  showClose: {
    type: Boolean,
    default: true
  },
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'form', 'confirm', 'info'].includes(value)
  }
})

// Emits 定义
const emit = defineEmits(['update:modelValue', 'open', 'close', 'confirm', 'cancel'])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogClass = computed(() => ({
  'base-dialog': true,
  [`base-dialog--${props.type}`]: true
}))

// 事件处理
const handleOpen = () => {
  emit('open')
}

const handleClose = () => {
  emit('close')
}

const handleBeforeClose = (done) => {
  if (props.type === 'confirm') {
    // 确认类型弹窗需要特殊处理
    emit('cancel')
  }
  done()
}

// 监听弹窗状态
watch(visible, (newVal) => {
  if (newVal) {
    // 弹窗打开时的处理
    document.body.style.overflow = 'hidden'
  } else {
    // 弹窗关闭时的处理
    document.body.style.overflow = ''
  }
})
</script>

<style lang="scss" scoped>
.base-dialog {
  &__content {
    color: var(--text);
    font-size: var(--font-size-base);
    line-height: var(--line-height-normal);
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: var(--gap-md);
  }

  &--form {
    .base-dialog__content {
      padding: 0;
    }
  }

  &--confirm {
    .base-dialog__content {
      text-align: center;
      padding: var(--gap-lg) 0;
    }
  }

  &--info {
    .base-dialog__content {
      padding: var(--gap-md) 0;
    }
  }
}

// 覆盖 Element Plus Dialog 遮罩层样式 - 暗色主题适配
:deep(.el-overlay) {
  background-color: rgba(0, 0, 0, 0.5) !important;
  
  &[data-theme='dark'],
  &.dark {
    background-color: rgba(0, 0, 0, 0.7) !important;
  }
}

// 覆盖 Element Plus Dialog 默认样式 - 暗色主题适配
:deep(.el-dialog) {
  // 弹窗圆角
  border-radius: 10px !important;
  overflow: hidden;
  background-color: var(--surface) !important;
  border: 1px solid var(--border) !important;
  
  // 标题样式
  .el-dialog__header {
    padding: 20px 20px 10px;
    background-color: var(--surface) !important;
    border-bottom: 1px solid var(--border) !important;
    
    .el-dialog__title {
      color: var(--color-primary) !important;
      font-size: 16px;
      font-weight: 600;
    }
    
    // 关闭按钮
    .el-dialog__headerbtn {
      .el-dialog__close {
        color: var(--text-3) !important;
        font-size: 18px;
        
        &:hover {
          color: var(--color-primary) !important;
        }
      }
    }
  }
  
  // 内容区域
  .el-dialog__body {
    padding: 20px;
    background-color: var(--surface) !important;
    color: var(--text) !important;
  }
  
  // 底部按钮区域
  .el-dialog__footer {
    padding: 10px 20px 20px;
    background-color: var(--surface) !important;
    border-top: 1px solid var(--border) !important;
    
    // 按钮样式
    .el-button {
      border-radius: 6px !important;
      padding: 8px 20px;
      font-size: 14px;
      
      // 取消按钮
      &--default {
        color: var(--text-2) !important;
        border-color: var(--border) !important;
        background-color: var(--surface) !important;
        
        &:hover {
          color: var(--text-2) !important;
          border-color: var(--border-hover) !important;
          background-color: var(--hover) !important;
        }
        
        &:active {
          color: var(--text-2) !important;
          border-color: var(--border) !important;
          background-color: var(--surface) !important;
        }
      }
      
      // 确认按钮
      &--primary {
        background-color: var(--color-primary) !important;
        border-color: var(--color-primary) !important;
        color: var(--surface) !important;
        
        &:hover {
          background-color: var(--color-primary-dark) !important;
          border-color: var(--color-primary-dark) !important;
        }
        
        /* 暗色主题下确认按钮悬浮样式 */
        [data-theme='dark'] &,
        .dark & {
          &:hover {
            background-color: var(--color-primary-light) !important;
            border-color: var(--color-primary-light) !important;
          }
        }
        
        &:active {
          background-color: var(--color-primary-dark) !important;
          border-color: var(--color-primary-dark) !important;
        }
      }
    }
  }
}

// MessageBox 样式覆盖（用于 ElMessageBox.confirm 等）- 暗色主题适配
:deep(.el-message-box) {
  // 弹窗圆角
  border-radius: 10px !important;
  overflow: hidden;
  background-color: var(--surface) !important;
  border: 1px solid var(--border) !important;
  
  // 标题样式
  .el-message-box__header {
    padding-top: 20px;
    background-color: var(--surface) !important;
    border-bottom: 1px solid var(--border) !important;
    
    .el-message-box__title {
      color: var(--color-primary) !important;
      font-size: 16px;
      font-weight: 600;
    }
    
    // 关闭按钮
    .el-message-box__headerbtn {
      .el-message-box__close {
        color: var(--text-3) !important;
        
        &:hover {
          color: var(--color-primary) !important;
        }
      }
    }
  }
  
  // 内容区域
  .el-message-box__content {
    padding: 20px;
    background-color: var(--surface) !important;
    
    .el-message-box__message {
      color: var(--text) !important;
    }
    
    .el-message-box__icon {
      color: var(--color-warning) !important;
    }
  }
  
  // 底部按钮
  .el-message-box__btns {
    padding: 10px 20px 20px;
    background-color: var(--surface) !important;
    border-top: 1px solid var(--border) !important;
    
    .el-button {
      border-radius: 6px !important;
      padding: 8px 20px;
      font-size: 14px;
      
      // 取消按钮
      &--default {
        color: var(--text-2) !important;
        border-color: var(--border) !important;
        background-color: var(--surface) !important;
        
        &:hover {
          color: var(--text-2) !important;
          border-color: var(--border-hover) !important;
          background-color: var(--hover) !important;
        }
        
        &:active {
          color: var(--text-2) !important;
          border-color: var(--border) !important;
          background-color: var(--surface) !important;
        }
      }
      
      // 确认按钮
      &--primary {
        background-color: var(--color-primary) !important;
        border-color: var(--color-primary) !important;
        color: var(--surface) !important;
        
        &:hover {
          background-color: var(--color-primary-dark) !important;
          border-color: var(--color-primary-dark) !important;
        }
        
        /* 暗色主题下确认按钮悬浮样式 */
        [data-theme='dark'] &,
        .dark & {
          &:hover {
            background-color: var(--color-primary-light) !important;
            border-color: var(--color-primary-light) !important;
          }
        }
        
        &:active {
          background-color: var(--color-primary-dark) !important;
          border-color: var(--color-primary-dark) !important;
        }
      }
    }
  }
}
</style>
