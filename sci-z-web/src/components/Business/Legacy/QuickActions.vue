<!--
/**
 * @description 快捷操作组件
 * 用于展示常用操作按钮，支持多种布局和样式
 * 包含图标、文本、徽章、权限控制等功能
 */
-->
<template>
  <div class="quick-actions-container" :class="[`actions--${layout}`, customClass]">
    <!-- 组件标题 -->
    <div v-if="showTitle" class="actions-header">
      <h3 class="actions-title">
        <el-icon v-if="icon" class="title-icon">
          <component :is="icon" />
        </el-icon>
        {{ title }}
      </h3>
      <p v-if="description" class="actions-description">{{ description }}</p>
    </div>
    
    <!-- 操作按钮列表 -->
    <div class="actions-list" :class="[`list--${layout}`]">
      <div
        v-for="(action, index) in visibleActions"
        :key="action.key || index"
        class="action-item"
        :class="[
          `item--${size}`,
          { 'is-disabled': action.disabled },
          { 'is-loading': action.loading }
        ]"
        @click="handleActionClick(action, index)"
      >
        <!-- 操作图标 -->
        <div class="action-icon" :class="`icon--${action.variant || 'default'}`">
          <el-icon v-if="action.icon" class="icon">
            <component :is="action.icon" />
          </el-icon>
          <img v-else-if="action.image" :src="action.image" :alt="action.label" class="icon-image" />
          <span v-else class="icon-text">{{ action.label.charAt(0) }}</span>
          
          <!-- 徽章 -->
          <div v-if="action.badge" class="action-badge" :class="`badge--${action.badge.type || 'primary'}`">
            {{ action.badge.text }}
          </div>
        </div>
        
        <!-- 操作文本 -->
        <div class="action-content">
          <div class="action-label">{{ action.label }}</div>
          <div v-if="action.description" class="action-description">{{ action.description }}</div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="action.loading" class="action-loading">
          <el-icon class="loading-icon">
            <Loading />
          </el-icon>
        </div>
        
        <!-- 右侧箭头 -->
        <div v-if="showArrow" class="action-arrow">
          <el-icon class="arrow-icon">
            <ArrowRight />
          </el-icon>
        </div>
      </div>
    </div>
    
    <!-- 查看更多 -->
    <div v-if="showMore && hasMoreActions" class="actions-more">
      <BaseButton type="text" @click="toggleMore">
        {{ showAllActions ? '收起' : `查看更多 (${moreActionsCount})` }}
        <el-icon class="el-icon--right">
          <component :is="showAllActions ? 'ArrowUp' : 'ArrowDown'" />
        </el-icon>
      </BaseButton>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { BaseButton } from '@/components/Common'
import { Loading, ArrowRight, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 标题
  title: {
    type: String,
    default: '快捷操作'
  },
  // 描述
  description: {
    type: String,
    default: ''
  },
  // 图标
  icon: {
    type: String,
    default: ''
  },
  // 操作列表
  actions: {
    type: Array,
    default: () => []
  },
  // 布局方式
  layout: {
    type: String,
    default: 'grid',
    validator: (value) => ['grid', 'list', 'horizontal'].includes(value)
  },
  // 按钮尺寸
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  // 是否显示标题
  showTitle: {
    type: Boolean,
    default: true
  },
  // 是否显示箭头
  showArrow: {
    type: Boolean,
    default: false
  },
  // 是否显示更多
  showMore: {
    type: Boolean,
    default: true
  },
  // 默认显示数量
  defaultShowCount: {
    type: Number,
    default: 6
  },
  // 权限检查函数
  permissionCheck: {
    type: Function,
    default: () => true
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['action-click', 'more-toggle'])

// 响应式数据
const showAllActions = ref(false)

// 计算属性
const visibleActions = computed(() => {
  const actions = props.actions.filter(action => {
    // 权限检查
    if (action.permission && !props.permissionCheck(action.permission)) {
      return false
    }
    
    // 条件显示检查
    if (action.show && typeof action.show === 'function') {
      return action.show()
    }
    
    return true
  })
  
  if (showAllActions.value) {
    return actions
  }
  
  return actions.slice(0, props.defaultShowCount)
})

const hasMoreActions = computed(() => {
  const totalActions = props.actions.filter(action => {
    if (action.permission && !props.permissionCheck(action.permission)) {
      return false
    }
    if (action.show && typeof action.show === 'function') {
      return action.show()
    }
    return true
  }).length
  
  return totalActions > props.defaultShowCount
})

const moreActionsCount = computed(() => {
  const totalActions = props.actions.filter(action => {
    if (action.permission && !props.permissionCheck(action.permission)) {
      return false
    }
    if (action.show && typeof action.show === 'function') {
      return action.show()
    }
    return true
  }).length
  
  return totalActions - props.defaultShowCount
})

// 处理操作点击
const handleActionClick = (action, index) => {
  if (action.disabled || action.loading) return
  
  emit('action-click', { action, index })
  
  // 执行操作处理函数
  if (action.handler && typeof action.handler === 'function') {
    action.handler(action, index)
  }
}

// 切换更多显示
const toggleMore = () => {
  showAllActions.value = !showAllActions.value
  emit('more-toggle', showAllActions.value)
}
</script>

<style lang="scss" scoped>
.quick-actions-container {
  .actions-header {
    margin-bottom: var(--gap-lg);
    
    .actions-title {
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
    
    .actions-description {
      margin: 0;
      font-size: var(--font-size-sm);
      color: var(--text-2);
    }
  }
  
  .actions-list {
    &.list--grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      gap: var(--gap-lg);
      
      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        padding: var(--gap-lg);
        border: 1px solid var(--border);
        border-radius: var(--radius-lg);
        background: var(--surface);
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: var(--color-primary);
          box-shadow: var(--shadow-md);
          transform: translateY(-2px);
        }
        
        &.is-disabled {
          opacity: 0.5;
          cursor: not-allowed;
          
          &:hover {
            transform: none;
            box-shadow: none;
          }
        }
        
        .action-icon {
          margin-bottom: var(--gap-md);
        }
        
        .action-content {
          .action-label {
            font-size: var(--font-size-sm);
            font-weight: 500;
            color: var(--text);
            margin-bottom: var(--gap-xs);
          }
          
          .action-description {
            font-size: var(--font-size-xs);
            color: var(--text-2);
            line-height: var(--line-height-tight);
          }
        }
      }
    }
    
    &.list--list {
      display: flex;
      flex-direction: column;
      gap: var(--gap-sm);
      
      .action-item {
        display: flex;
        align-items: center;
        gap: var(--gap-md);
        padding: var(--gap-md);
        border: 1px solid var(--border);
        border-radius: var(--radius-md);
        background: var(--surface);
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: var(--color-primary);
          background: var(--hover);
        }
        
        &.is-disabled {
          opacity: 0.5;
          cursor: not-allowed;
          
          &:hover {
            background: var(--surface);
          }
        }
        
        .action-content {
          flex: 1;
          
          .action-label {
            font-size: var(--font-size-base);
            font-weight: 500;
            color: var(--text);
            margin-bottom: var(--gap-xs);
          }
          
          .action-description {
            font-size: var(--font-size-sm);
            color: var(--text-2);
          }
        }
        
        .action-arrow {
          .arrow-icon {
            color: var(--text-3);
          }
        }
      }
    }
    
    &.list--horizontal {
      display: flex;
      gap: var(--gap-md);
      overflow-x: auto;
      padding-bottom: var(--gap-sm);
      
      .action-item {
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        padding: var(--gap-md);
        border: 1px solid var(--border);
        border-radius: var(--radius-md);
        background: var(--surface);
        cursor: pointer;
        transition: all 0.3s ease;
        min-width: 100px;
        
        &:hover {
          border-color: var(--color-primary);
          box-shadow: var(--shadow-sm);
        }
        
        .action-icon {
          margin-bottom: var(--gap-sm);
        }
        
        .action-content {
          .action-label {
            font-size: var(--font-size-sm);
            font-weight: 500;
            color: var(--text);
          }
        }
      }
    }
  }
  
  .action-icon {
    position: relative;
    width: 48px;
    height: 48px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg);
    color: var(--text-2);
    transition: all 0.3s ease;
    
    .icon {
      font-size: 24px;
    }
    
    .icon-image {
      width: 32px;
      height: 32px;
      object-fit: contain;
    }
    
    .icon-text {
      font-size: var(--font-size-lg);
      font-weight: 600;
      color: var(--text-2);
    }
    
    &.icon--primary {
      background: var(--color-primary);
      color: white;
    }
    
    &.icon--success {
      background: var(--color-success);
      color: white;
    }
    
    &.icon--warning {
      background: var(--color-warning);
      color: white;
    }
    
    &.icon--danger {
      background: var(--color-error);
      color: white;
    }
    
    &.icon--info {
      background: var(--color-info);
      color: white;
    }
    
    .action-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      min-width: 18px;
      height: 18px;
      padding: 0 4px;
      border-radius: var(--radius-full);
      font-size: var(--font-size-xs);
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      
      &.badge--primary {
        background: var(--color-primary);
      }
      
      &.badge--success {
        background: var(--color-success);
      }
      
      &.badge--warning {
        background: var(--color-warning);
      }
      
      &.badge--danger {
        background: var(--color-error);
      }
    }
  }
  
  .action-loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.8);
    border-radius: var(--radius-lg);
    
    .loading-icon {
      font-size: 20px;
      color: var(--color-primary);
      animation: spin 1s linear infinite;
    }
  }
  
  .actions-more {
    margin-top: var(--gap-lg);
    text-align: center;
  }
}

// 尺寸变体
.actions-list {
  .action-item {
    &.item--small {
      .action-icon {
        width: 36px;
        height: 36px;
        
        .icon {
          font-size: 18px;
        }
        
        .icon-image {
          width: 24px;
          height: 24px;
        }
      }
    }
    
    &.item--large {
      .action-icon {
        width: 60px;
        height: 60px;
        
        .icon {
          font-size: 30px;
        }
        
        .icon-image {
          width: 40px;
          height: 40px;
        }
      }
    }
  }
}

// 动画
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .quick-actions-container {
    .actions-list {
      &.list--grid {
        grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
        gap: var(--gap-md);
      }
      
      &.list--horizontal {
        .action-item {
          min-width: 80px;
        }
      }
    }
  }
}
</style>
