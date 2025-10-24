<!--
/**
 * @description 统计卡片组件
 * 用于展示统计数据，支持多种样式和交互
 * 包含数值显示、趋势指示、图标展示等功能
 */
-->
<template>
  <BaseCard
    class="stat-card"
    :class="[
      `stat-card--${variant}`,
      `stat-card--${size}`,
      { 'is-clickable': clickable },
      { 'is-loading': loading },
      customClass
    ]"
    @click="handleClick"
  >
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-left">
        <div class="stat-icon" :class="`icon--${variant}`">
          <el-icon v-if="icon" class="icon">
            <component :is="icon" />
          </el-icon>
          <img v-else-if="image" :src="image" :alt="title" class="icon-image" />
        </div>
      </div>
      <div class="header-right">
        <el-dropdown
          v-if="showMenu"
          trigger="click"
          @command="handleMenuCommand"
        >
          <el-button type="text" size="small">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="item in menuItems"
                :key="item.key"
                :command="item.key"
                :disabled="item.disabled"
              >
                <el-icon v-if="item.icon">
                  <component :is="item.icon" />
                </el-icon>
                {{ item.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 卡片内容 -->
    <div class="card-content">
      <div class="stat-title">{{ title }}</div>
      <div class="stat-value" :class="`value--${variant}`">
        <span class="value-number">{{ displayValue }}</span>
        <span v-if="unit" class="value-unit">{{ unit }}</span>
      </div>
      
      <!-- 趋势指示器 -->
      <div v-if="showTrend" class="stat-trend">
        <div class="trend-indicator" :class="`trend--${trend.type}`">
          <el-icon class="trend-icon">
            <component :is="getTrendIcon(trend.type)" />
          </el-icon>
          <span class="trend-text">{{ trend.text }}</span>
        </div>
        <div v-if="trend.period" class="trend-period">{{ trend.period }}</div>
      </div>
      
      <!-- 描述信息 -->
      <div v-if="description" class="stat-description">{{ description }}</div>
    </div>
    
    <!-- 卡片底部 -->
    <div v-if="showFooter" class="card-footer">
      <slot name="footer">
        <div class="footer-content">
          <span v-if="footerText" class="footer-text">{{ footerText }}</span>
          <el-button
            v-if="showAction"
            type="text"
            size="small"
            @click.stop="handleAction"
          >
            {{ actionText }}
            <el-icon class="el-icon--right">
              <ArrowRight />
            </el-icon>
          </el-button>
        </div>
      </slot>
    </div>
    
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon class="loading-icon">
        <Loading />
      </el-icon>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import { BaseCard, BaseButton } from '@/components/Common'
import {
  MoreFilled,
  ArrowRight,
  Loading,
  ArrowUp,
  ArrowDown,
  Minus
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 标题
  title: {
    type: String,
    default: ''
  },
  // 数值
  value: {
    type: [Number, String],
    default: 0
  },
  // 单位
  unit: {
    type: String,
    default: ''
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
  // 图片
  image: {
    type: String,
    default: ''
  },
  // 卡片变体
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'danger', 'info'].includes(value)
  },
  // 卡片尺寸
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  // 趋势数据
  trend: {
    type: Object,
    default: () => ({
      type: 'neutral', // up, down, neutral
      text: '',
      period: ''
    })
  },
  // 是否显示趋势
  showTrend: {
    type: Boolean,
    default: true
  },
  // 是否显示菜单
  showMenu: {
    type: Boolean,
    default: false
  },
  // 菜单项
  menuItems: {
    type: Array,
    default: () => []
  },
  // 是否显示底部
  showFooter: {
    type: Boolean,
    default: false
  },
  // 底部文本
  footerText: {
    type: String,
    default: ''
  },
  // 是否显示操作按钮
  showAction: {
    type: Boolean,
    default: false
  },
  // 操作按钮文本
  actionText: {
    type: String,
    default: '查看详情'
  },
  // 是否可点击
  clickable: {
    type: Boolean,
    default: false
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 数值格式化
  formatter: {
    type: Function,
    default: null
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['click', 'action', 'menu-command'])

// 计算属性
const displayValue = computed(() => {
  if (props.formatter && typeof props.formatter === 'function') {
    return props.formatter(props.value)
  }
  
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  
  return props.value
})

// 获取趋势图标
const getTrendIcon = (type) => {
  const iconMap = {
    'up': ArrowUp,
    'down': ArrowDown,
    'neutral': Minus
  }
  return iconMap[type] || Minus
}

// 处理点击
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}

// 处理操作
const handleAction = () => {
  emit('action')
}

// 处理菜单命令
const handleMenuCommand = (command) => {
  emit('menu-command', command)
}
</script>

<style lang="scss" scoped>
.stat-card {
  position: relative;
  transition: all 0.3s ease;
  
  &.is-clickable {
    cursor: pointer;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
    }
  }
  
  &.is-loading {
    pointer-events: none;
    
    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--radius-lg);
      
      .loading-icon {
        font-size: 24px;
        color: var(--color-primary);
        animation: spin 1s linear infinite;
      }
    }
  }
  
  // 变体样式
  &--primary {
    border-left: 4px solid var(--color-primary);
    
    .stat-icon.icon--primary {
      background: var(--color-primary);
      color: white;
    }
    
    .stat-value.value--primary {
      color: var(--color-primary);
    }
  }
  
  &--success {
    border-left: 4px solid var(--color-success);
    
    .stat-icon.icon--success {
      background: var(--color-success);
      color: white;
    }
    
    .stat-value.value--success {
      color: var(--color-success);
    }
  }
  
  &--warning {
    border-left: 4px solid var(--color-warning);
    
    .stat-icon.icon--warning {
      background: var(--color-warning);
      color: white;
    }
    
    .stat-value.value--warning {
      color: var(--color-warning);
    }
  }
  
  &--danger {
    border-left: 4px solid var(--color-error);
    
    .stat-icon.icon--danger {
      background: var(--color-error);
      color: white;
    }
    
    .stat-value.value--danger {
      color: var(--color-error);
    }
  }
  
  &--info {
    border-left: 4px solid var(--color-info);
    
    .stat-icon.icon--info {
      background: var(--color-info);
      color: white;
    }
    
    .stat-value.value--info {
      color: var(--color-info);
    }
  }
  
  // 尺寸样式
  &--small {
    .card-header {
      margin-bottom: var(--gap-sm);
    }
    
    .stat-title {
      font-size: var(--font-size-sm);
    }
    
    .stat-value {
      font-size: var(--font-size-xl);
    }
    
    .stat-icon {
      width: 32px;
      height: 32px;
      
      .icon {
        font-size: 16px;
      }
    }
  }
  
  &--default {
    .stat-value {
      font-size: var(--font-size-2xl);
    }
    
    .stat-icon {
      width: 40px;
      height: 40px;
      
      .icon {
        font-size: 20px;
      }
    }
  }
  
  &--large {
    .stat-value {
      font-size: var(--font-size-3xl);
    }
    
    .stat-icon {
      width: 48px;
      height: 48px;
      
      .icon {
        font-size: 24px;
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--gap-md);
    
    .stat-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg);
      color: var(--text-2);
      transition: all 0.3s ease;
      
      .icon {
        font-size: 20px;
      }
      
      .icon-image {
        width: 24px;
        height: 24px;
        object-fit: contain;
      }
    }
  }
  
  .card-content {
    .stat-title {
      font-size: var(--font-size-base);
      color: var(--text-2);
      margin-bottom: var(--gap-sm);
      font-weight: 500;
    }
    
    .stat-value {
      font-size: var(--font-size-2xl);
      font-weight: 700;
      color: var(--text);
      margin-bottom: var(--gap-sm);
      
      .value-number {
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      }
      
      .value-unit {
        font-size: var(--font-size-sm);
        font-weight: 500;
        margin-left: var(--gap-xs);
        color: var(--text-2);
      }
    }
    
    .stat-trend {
      display: flex;
      align-items: center;
      gap: var(--gap-sm);
      margin-bottom: var(--gap-sm);
      
      .trend-indicator {
        display: flex;
        align-items: center;
        gap: var(--gap-xs);
        font-size: var(--font-size-sm);
        font-weight: 500;
        
        .trend-icon {
          font-size: 12px;
        }
        
        &.trend--up {
          color: var(--color-success);
        }
        
        &.trend--down {
          color: var(--color-error);
        }
        
        &.trend--neutral {
          color: var(--text-3);
        }
      }
      
      .trend-period {
        font-size: var(--font-size-xs);
        color: var(--text-3);
      }
    }
    
    .stat-description {
      font-size: var(--font-size-sm);
      color: var(--text-2);
      line-height: var(--line-height-normal);
    }
  }
  
  .card-footer {
    margin-top: var(--gap-lg);
    padding-top: var(--gap-md);
    border-top: 1px solid var(--border);
    
    .footer-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .footer-text {
        font-size: var(--font-size-sm);
        color: var(--text-2);
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
  .stat-card {
    .card-footer {
      .footer-content {
        flex-direction: column;
        gap: var(--gap-sm);
        align-items: stretch;
      }
    }
  }
}
</style>
