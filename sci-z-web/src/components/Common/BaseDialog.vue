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
</style>
