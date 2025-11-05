<!--
/**
 * @description 表单操作按钮组组件
 * 用于表单底部的操作按钮，支持提交、重置、取消等操作
 * 包含加载状态、权限控制、确认对话框等功能
 */
-->
<template>
  <div class="form-actions" :class="[`form-actions--${align}`, customClass]">
    <div class="actions-content">
      <!-- 左侧操作 -->
      <div class="actions-left">
        <slot name="left">
          <BaseButton
            v-if="showReset"
            :disabled="disabled || loading"
            @click="handleReset"
          >
            <el-icon><Refresh /></el-icon>
            {{ resetText }}
          </BaseButton>
        </slot>
      </div>
      
      <!-- 右侧操作 -->
      <div class="actions-right">
        <slot name="right">
          <!-- 取消按钮 -->
          <BaseButton
            v-if="showCancel"
            :disabled="disabled || loading"
            @click="handleCancel"
          >
            {{ cancelText }}
          </BaseButton>
          
          <!-- 保存草稿按钮 -->
          <BaseButton
            v-if="showDraft"
            :disabled="disabled || loading"
            @click="handleDraft"
          >
            <el-icon><Document /></el-icon>
            {{ draftText }}
          </BaseButton>
          
          <!-- 提交按钮 -->
          <BaseButton
            v-if="showSubmit"
            type="primary"
            :loading="loading"
            :disabled="disabled"
            @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            {{ submitText }}
          </BaseButton>
        </slot>
      </div>
    </div>
    
    <!-- 操作提示 -->
    <div v-if="showTips" class="action-tips">
      <el-alert
        :title="tipsTitle"
        :description="tipsDescription"
        :type="tipsType"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BaseButton } from '@/components/Common'
import { Refresh, Document, Check } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 按钮对齐方式
  align: {
    type: String,
    default: 'right',
    validator: (value) => ['left', 'center', 'right', 'space-between'].includes(value)
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否显示重置按钮
  showReset: {
    type: Boolean,
    default: true
  },
  // 是否显示取消按钮
  showCancel: {
    type: Boolean,
    default: true
  },
  // 是否显示保存草稿按钮
  showDraft: {
    type: Boolean,
    default: false
  },
  // 是否显示提交按钮
  showSubmit: {
    type: Boolean,
    default: true
  },
  // 重置按钮文本
  resetText: {
    type: String,
    default: '重置'
  },
  // 取消按钮文本
  cancelText: {
    type: String,
    default: '取消'
  },
  // 保存草稿按钮文本
  draftText: {
    type: String,
    default: '保存草稿'
  },
  // 提交按钮文本
  submitText: {
    type: String,
    default: '提交'
  },
  // 是否显示操作提示
  showTips: {
    type: Boolean,
    default: false
  },
  // 提示标题
  tipsTitle: {
    type: String,
    default: '操作提示'
  },
  // 提示描述
  tipsDescription: {
    type: String,
    default: ''
  },
  // 提示类型
  tipsType: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'warning', 'info', 'error'].includes(value)
  },
  // 提交确认配置
  submitConfirm: {
    type: Object,
    default: () => null
  },
  // 重置确认配置
  resetConfirm: {
    type: Object,
    default: () => null
  },
  // 取消确认配置
  cancelConfirm: {
    type: Object,
    default: () => null
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['submit', 'reset', 'cancel', 'draft'])

// 处理提交
const handleSubmit = async () => {
  try {
    // 提交确认
    if (props.submitConfirm) {
      const confirmResult = await ElMessageBox.confirm(
        props.submitConfirm.message || '确认提交表单吗？',
        props.submitConfirm.title || '确认提交',
        {
          confirmButtonText: props.submitConfirm.confirmText || '确定',
          cancelButtonText: props.submitConfirm.cancelText || '取消',
          type: props.submitConfirm.type || 'warning'
        }
      )
      
      if (!confirmResult) return
    }
    
    emit('submit')
  } catch (error) {
    // 用户取消操作
  }
}

// 处理重置
const handleReset = async () => {
  try {
    // 重置确认
    if (props.resetConfirm) {
      const confirmResult = await ElMessageBox.confirm(
        props.resetConfirm.message || '确认重置表单吗？',
        props.resetConfirm.title || '确认重置',
        {
          confirmButtonText: props.resetConfirm.confirmText || '确定',
          cancelButtonText: props.resetConfirm.cancelText || '取消',
          type: props.resetConfirm.type || 'warning'
        }
      )
      
      if (!confirmResult) return
    }
    
    emit('reset')
  } catch (error) {
    // 用户取消操作
  }
}

// 处理取消
const handleCancel = async () => {
  try {
    // 取消确认
    if (props.cancelConfirm) {
      const confirmResult = await ElMessageBox.confirm(
        props.cancelConfirm.message || '确认取消操作吗？',
        props.cancelConfirm.title || '确认取消',
        {
          confirmButtonText: props.cancelConfirm.confirmText || '确定',
          cancelButtonText: props.cancelConfirm.cancelText || '取消',
          type: props.cancelConfirm.type || 'warning'
        }
      )
      
      if (!confirmResult) return
    }
    
    emit('cancel')
  } catch (error) {
    // 用户取消操作
  }
}

// 处理保存草稿
const handleDraft = () => {
  emit('draft')
}
</script>

<style lang="scss" scoped>
.form-actions {
  margin-top: var(--gap-2xl);
  padding-top: var(--gap-lg);
  border-top: 1px solid var(--border);
  
  .actions-content {
    display: flex;
    align-items: center;
    gap: var(--gap-md);
    
    .actions-left,
    .actions-right {
      display: flex;
      align-items: center;
      gap: var(--gap-sm);
    }
  }
  
  // 对齐方式
  &--left {
    .actions-content {
      justify-content: flex-start;
    }
  }
  
  &--center {
    .actions-content {
      justify-content: center;
    }
  }
  
  &--right {
    .actions-content {
      justify-content: flex-end;
    }
  }
  
  &--space-between {
    .actions-content {
      justify-content: space-between;
    }
  }
  
  .action-tips {
    margin-top: var(--gap-lg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .form-actions {
    .actions-content {
      flex-direction: column;
      align-items: stretch;
      gap: var(--gap-md);
      
      .actions-left,
      .actions-right {
        justify-content: center;
      }
    }
  }
}
</style>
