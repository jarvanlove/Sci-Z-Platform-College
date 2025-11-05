<!--
/**
 * @description 表单区块组件
 * 用于将表单内容分组显示，提供标题、描述、折叠等功能
 * 支持嵌套、验证状态显示、操作按钮等
 */
-->
<template>
  <BaseCard class="form-section" :class="[`form-section--${variant}`, customClass]">
    <!-- 区块头部 -->
    <div v-if="showHeader" class="section-header">
      <div class="header-left">
        <h3 v-if="title" class="section-title">
          <el-icon v-if="icon" class="title-icon">
            <component :is="icon" />
          </el-icon>
          {{ title }}
        </h3>
        <p v-if="description" class="section-description">{{ description }}</p>
      </div>
      <div class="header-right">
        <slot name="header-actions">
          <el-button
            v-if="collapsible"
            type="text"
            @click="toggleCollapse"
          >
            <el-icon>
              <ArrowDown v-if="!collapsed" />
              <ArrowRight v-else />
            </el-icon>
            {{ collapsed ? '展开' : '收起' }}
          </el-button>
        </slot>
      </div>
    </div>
    
    <!-- 区块内容 -->
    <el-collapse-transition>
      <div v-show="!collapsed" class="section-content">
        <!-- 验证状态提示 -->
        <el-alert
          v-if="showValidationStatus && validationStatus"
          :type="validationStatus.type"
          :title="validationStatus.title"
          :description="validationStatus.description"
          :closable="false"
          class="validation-alert"
        />
        
        <!-- 表单内容 -->
        <div class="form-content">
          <slot></slot>
        </div>
        
        <!-- 区块底部操作 -->
        <div v-if="$slots.footer" class="section-footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </el-collapse-transition>
  </BaseCard>
</template>

<script setup>
import { ref, computed } from 'vue'
import { BaseCard } from '@/components/Common'
import { ArrowDown, ArrowRight } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 区块标题
  title: {
    type: String,
    default: ''
  },
  // 区块描述
  description: {
    type: String,
    default: ''
  },
  // 区块图标
  icon: {
    type: String,
    default: ''
  },
  // 区块变体
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'danger'].includes(value)
  },
  // 是否可折叠
  collapsible: {
    type: Boolean,
    default: false
  },
  // 是否默认折叠
  defaultCollapsed: {
    type: Boolean,
    default: false
  },
  // 是否显示头部
  showHeader: {
    type: Boolean,
    default: true
  },
  // 是否显示验证状态
  showValidationStatus: {
    type: Boolean,
    default: false
  },
  // 验证状态
  validationStatus: {
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
const emit = defineEmits(['toggle-collapse'])

// 响应式数据
const collapsed = ref(props.defaultCollapsed)

// 切换折叠状态
const toggleCollapse = () => {
  collapsed.value = !collapsed.value
  emit('toggle-collapse', collapsed.value)
}
</script>

<style lang="scss" scoped>
.form-section {
  margin-bottom: var(--gap-lg);
  
  &--primary {
    border-left: 4px solid var(--color-primary);
  }
  
  &--success {
    border-left: 4px solid var(--color-success);
  }
  
  &--warning {
    border-left: 4px solid var(--color-warning);
  }
  
  &--danger {
    border-left: 4px solid var(--color-error);
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--gap-lg);
    
    .header-left {
      flex: 1;
      
      .section-title {
        display: flex;
        align-items: center;
        gap: var(--gap-sm);
        margin: 0 0 var(--gap-xs) 0;
        font-size: var(--font-size-lg);
        font-weight: 600;
        color: var(--text);
        
        .title-icon {
          color: var(--color-primary);
        }
      }
      
      .section-description {
        margin: 0;
        color: var(--text-2);
        font-size: var(--font-size-sm);
        line-height: var(--line-height-normal);
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: var(--gap-sm);
    }
  }
  
  .section-content {
    .validation-alert {
      margin-bottom: var(--gap-lg);
    }
    
    .form-content {
      // 表单内容样式可以在这里添加
    }
    
    .section-footer {
      margin-top: var(--gap-lg);
      padding-top: var(--gap-lg);
      border-top: 1px solid var(--border);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .form-section {
    .section-header {
      flex-direction: column;
      gap: var(--gap-sm);
      align-items: stretch;
      
      .header-right {
        justify-content: flex-end;
      }
    }
  }
}
</style>
