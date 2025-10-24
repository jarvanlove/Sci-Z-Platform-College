<!--
/**
 * @description 进度条组件
 * 用于展示进度信息，支持多种样式和状态
 * 包含百分比显示、状态指示、动画效果等功能
 */
-->
<template>
  <div class="progress-bar-container" :class="[customClass]">
    <!-- 进度条头部 -->
    <div v-if="showHeader" class="progress-header">
      <div class="header-left">
        <h4 v-if="title" class="progress-title">{{ title }}</h4>
        <p v-if="description" class="progress-description">{{ description }}</p>
      </div>
      <div class="header-right">
        <span class="progress-percentage">{{ displayPercentage }}%</span>
        <StatusTag
          v-if="status"
          :status="status"
          :status-map="statusMap"
          :size="'small'"
        />
      </div>
    </div>
    
    <!-- 进度条主体 -->
    <div class="progress-bar" :class="[`progress-bar--${type}`, `progress-bar--${size}`]">
      <div
        class="progress-track"
        :class="[
          `track--${status || 'default'}`,
          { 'is-animated': animated }
        ]"
        :style="trackStyle"
      >
        <div
          class="progress-fill"
          :class="[
            `fill--${status || 'default'}`,
            { 'is-animated': animated }
          ]"
          :style="fillStyle"
        >
          <!-- 条纹效果 -->
          <div v-if="striped" class="progress-stripes"></div>
          
          <!-- 内部文本 -->
          <span v-if="showText && textInside" class="progress-text">
            {{ displayPercentage }}%
          </span>
        </div>
      </div>
      
      <!-- 外部文本 -->
      <div v-if="showText && !textInside" class="progress-text-external">
        {{ displayPercentage }}%
      </div>
    </div>
    
    <!-- 进度条底部信息 -->
    <div v-if="showFooter" class="progress-footer">
      <div class="footer-left">
        <slot name="footer-left">
          <span v-if="currentLabel" class="current-label">{{ currentLabel }}: {{ current }}</span>
          <span v-if="totalLabel" class="total-label">{{ totalLabel }}: {{ total }}</span>
        </slot>
      </div>
      <div class="footer-right">
        <slot name="footer-right">
          <span v-if="showTime" class="time-info">{{ timeInfo }}</span>
        </slot>
      </div>
    </div>
    
    <!-- 步骤进度条 -->
    <div v-if="type === 'steps'" class="progress-steps">
      <div
        v-for="(step, index) in steps"
        :key="step.key || index"
        class="step-item"
        :class="[
          `step--${step.status || 'default'}`,
          { 'is-active': step.active },
          { 'is-completed': step.completed }
        ]"
      >
        <div class="step-node">
          <el-icon v-if="step.icon" class="step-icon">
            <component :is="step.icon" />
          </el-icon>
          <span v-else class="step-number">{{ index + 1 }}</span>
        </div>
        <div class="step-content">
          <div class="step-title">{{ step.title }}</div>
          <div v-if="step.description" class="step-description">{{ step.description }}</div>
        </div>
        <div v-if="index < steps.length - 1" class="step-connector"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { StatusTag } from '@/components/Common'

// Props
const props = defineProps({
  // 进度值 (0-100)
  percentage: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0 && value <= 100
  },
  // 当前值
  current: {
    type: Number,
    default: 0
  },
  // 总值
  total: {
    type: Number,
    default: 100
  },
  // 进度条类型
  type: {
    type: String,
    default: 'line',
    validator: (value) => ['line', 'circle', 'steps'].includes(value)
  },
  // 进度条尺寸
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  // 状态
  status: {
    type: String,
    default: '',
    validator: (value) => ['', 'success', 'warning', 'danger', 'info'].includes(value)
  },
  // 状态映射
  statusMap: {
    type: Object,
    default: () => ({})
  },
  // 标题
  title: {
    type: String,
    default: ''
  },
  // 描述
  description: {
    type: String,
    default: ''
  },
  // 是否显示头部
  showHeader: {
    type: Boolean,
    default: true
  },
  // 是否显示底部
  showFooter: {
    type: Boolean,
    default: false
  },
  // 是否显示文本
  showText: {
    type: Boolean,
    default: true
  },
  // 文本是否在内部
  textInside: {
    type: Boolean,
    default: false
  },
  // 是否显示条纹
  striped: {
    type: Boolean,
    default: false
  },
  // 是否显示动画
  animated: {
    type: Boolean,
    default: true
  },
  // 当前值标签
  currentLabel: {
    type: String,
    default: ''
  },
  // 总值标签
  totalLabel: {
    type: String,
    default: ''
  },
  // 是否显示时间信息
  showTime: {
    type: Boolean,
    default: false
  },
  // 时间信息
  timeInfo: {
    type: String,
    default: ''
  },
  // 步骤数据 (当type为steps时使用)
  steps: {
    type: Array,
    default: () => []
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// 计算属性
const displayPercentage = computed(() => {
  if (props.total > 0) {
    return Math.round((props.current / props.total) * 100)
  }
  return Math.round(props.percentage)
})

const trackStyle = computed(() => {
  const styles = {}
  
  if (props.type === 'line') {
    styles.height = props.size === 'small' ? '6px' : props.size === 'large' ? '12px' : '8px'
  }
  
  return styles
})

const fillStyle = computed(() => {
  const styles = {
    width: `${displayPercentage.value}%`
  }
  
  if (props.animated) {
    styles.transition = 'width 0.3s ease'
  }
  
  return styles
})
</script>

<style lang="scss" scoped>
.progress-bar-container {
  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--gap-md);
    
    .header-left {
      flex: 1;
      
      .progress-title {
        margin: 0 0 var(--gap-xs) 0;
        font-size: var(--font-size-base);
        font-weight: 600;
        color: var(--text);
      }
      
      .progress-description {
        margin: 0;
        font-size: var(--font-size-sm);
        color: var(--text-2);
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: var(--gap-sm);
      
      .progress-percentage {
        font-size: var(--font-size-sm);
        font-weight: 600;
        color: var(--text);
      }
    }
  }
  
  .progress-bar {
    position: relative;
    
    &--line {
      .progress-track {
        width: 100%;
        background: var(--border);
        border-radius: var(--radius-full);
        overflow: hidden;
        
        .progress-fill {
          height: 100%;
          border-radius: var(--radius-full);
          position: relative;
          overflow: hidden;
          
          .progress-stripes {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-image: linear-gradient(
              45deg,
              rgba(255, 255, 255, 0.2) 25%,
              transparent 25%,
              transparent 50%,
              rgba(255, 255, 255, 0.2) 50%,
              rgba(255, 255, 255, 0.2) 75%,
              transparent 75%,
              transparent
            );
            background-size: 20px 20px;
            animation: progress-stripes 1s linear infinite;
          }
          
          .progress-text {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            font-size: var(--font-size-xs);
            font-weight: 600;
            color: white;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
          }
        }
        
        &.track--success .progress-fill {
          background: var(--color-success);
        }
        
        &.track--warning .progress-fill {
          background: var(--color-warning);
        }
        
        &.track--danger .progress-fill {
          background: var(--color-error);
        }
        
        &.track--info .progress-fill {
          background: var(--color-info);
        }
        
        &.track--default .progress-fill {
          background: var(--color-primary);
        }
      }
      
      .progress-text-external {
        margin-top: var(--gap-xs);
        text-align: right;
        font-size: var(--font-size-sm);
        color: var(--text-2);
      }
    }
    
    &--small {
      .progress-track {
        height: 6px;
      }
    }
    
    &--default {
      .progress-track {
        height: 8px;
      }
    }
    
    &--large {
      .progress-track {
        height: 12px;
      }
    }
  }
  
  .progress-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: var(--gap-md);
    font-size: var(--font-size-sm);
    color: var(--text-2);
    
    .footer-left {
      display: flex;
      gap: var(--gap-md);
    }
  }
  
  .progress-steps {
    display: flex;
    align-items: flex-start;
    gap: var(--gap-sm);
    margin-top: var(--gap-lg);
    
    .step-item {
      flex: 1;
      position: relative;
      
      .step-node {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px solid var(--border);
        background: var(--surface);
        margin: 0 auto var(--gap-sm) auto;
        transition: all 0.3s ease;
        
        .step-icon {
          font-size: 16px;
          color: var(--text-2);
        }
        
        .step-number {
          font-size: var(--font-size-sm);
          font-weight: 600;
          color: var(--text-2);
        }
      }
      
      .step-content {
        text-align: center;
        
        .step-title {
          font-size: var(--font-size-sm);
          font-weight: 500;
          color: var(--text);
          margin-bottom: var(--gap-xs);
        }
        
        .step-description {
          font-size: var(--font-size-xs);
          color: var(--text-2);
          line-height: var(--line-height-tight);
        }
      }
      
      .step-connector {
        position: absolute;
        top: 16px;
        left: calc(50% + 16px);
        right: calc(-50% + 16px);
        height: 2px;
        background: var(--border);
        z-index: -1;
      }
      
      &.step--success {
        .step-node {
          background: var(--color-success);
          border-color: var(--color-success);
          color: white;
          
          .step-icon,
          .step-number {
            color: white;
          }
        }
        
        .step-connector {
          background: var(--color-success);
        }
      }
      
      &.step--warning {
        .step-node {
          background: var(--color-warning);
          border-color: var(--color-warning);
          color: white;
          
          .step-icon,
          .step-number {
            color: white;
          }
        }
      }
      
      &.step--danger {
        .step-node {
          background: var(--color-error);
          border-color: var(--color-error);
          color: white;
          
          .step-icon,
          .step-number {
            color: white;
          }
        }
      }
      
      &.is-active {
        .step-node {
          box-shadow: 0 0 0 4px rgba(30, 58, 138, 0.2);
        }
      }
    }
  }
}

// 动画
@keyframes progress-stripes {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 20px 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .progress-bar-container {
    .progress-header {
      flex-direction: column;
      gap: var(--gap-sm);
      align-items: stretch;
      
      .header-right {
        justify-content: flex-end;
      }
    }
    
    .progress-footer {
      flex-direction: column;
      gap: var(--gap-sm);
      align-items: stretch;
    }
    
    .progress-steps {
      flex-direction: column;
      gap: var(--gap-md);
      
      .step-item {
        .step-connector {
          display: none;
        }
      }
    }
  }
}
</style>
