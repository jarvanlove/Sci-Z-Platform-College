<!--
/**
 * @description 时间线组件
 * 用于展示时间序列的事件或状态变化
 * 支持多种样式、图标、状态显示等功能
 */
-->
<template>
  <div class="timeline-container" :class="[`timeline--${direction}`, customClass]">
    <div
      v-for="(item, index) in timelineItems"
      :key="item.key || index"
      class="timeline-item"
      :class="[
        `timeline-item--${item.type || 'default'}`,
        { 'is-active': item.active },
        { 'is-completed': item.completed }
      ]"
    >
      <!-- 时间线节点 -->
      <div class="timeline-node">
        <div class="node-dot" :class="`node-dot--${item.type || 'default'}`">
          <el-icon v-if="item.icon" class="node-icon">
            <component :is="item.icon" />
          </el-icon>
          <span v-else-if="item.number" class="node-number">{{ item.number }}</span>
        </div>
        <div v-if="direction === 'vertical'" class="node-line"></div>
      </div>
      
      <!-- 时间线内容 -->
      <div class="timeline-content">
        <div class="content-header">
          <div class="content-title">
            <h4 v-if="item.title" class="item-title">{{ item.title }}</h4>
            <p v-if="item.subtitle" class="item-subtitle">{{ item.subtitle }}</p>
          </div>
          <div class="content-meta">
            <span v-if="item.time" class="item-time">{{ formatTime(item.time) }}</span>
            <StatusTag
              v-if="item.status"
              :status="item.status"
              :status-map="item.statusMap"
              :size="'small'"
            />
          </div>
        </div>
        
        <div v-if="item.description" class="content-description">
          {{ item.description }}
        </div>
        
        <div v-if="item.details" class="content-details">
          <div
            v-for="detail in item.details"
            :key="detail.key"
            class="detail-item"
          >
            <span class="detail-label">{{ detail.label }}:</span>
            <span class="detail-value">{{ detail.value }}</span>
          </div>
        </div>
        
        <!-- 自定义内容插槽 -->
        <div v-if="$slots[`content-${index}`]" class="content-custom">
          <slot :name="`content-${index}`" :item="item" :index="index"></slot>
        </div>
        
        <!-- 操作按钮 -->
        <div v-if="item.actions && item.actions.length > 0" class="content-actions">
          <BaseButton
            v-for="action in item.actions"
            :key="action.key"
            :type="action.type || 'primary'"
            :size="'small'"
            @click="handleAction(action, item, index)"
          >
            {{ action.label }}
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { BaseButton, StatusTag } from '@/components/Common'

// Props
const props = defineProps({
  // 时间线数据
  items: {
    type: Array,
    default: () => []
  },
  // 时间线方向
  direction: {
    type: String,
    default: 'vertical',
    validator: (value) => ['vertical', 'horizontal'].includes(value)
  },
  // 是否显示时间
  showTime: {
    type: Boolean,
    default: true
  },
  // 时间格式
  timeFormat: {
    type: String,
    default: 'YYYY-MM-DD HH:mm'
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['action', 'item-click'])

// 计算属性
const timelineItems = computed(() => {
  return props.items.map((item, index) => ({
    key: item.key || `timeline-${index}`,
    type: item.type || 'default',
    title: item.title || '',
    subtitle: item.subtitle || '',
    description: item.description || '',
    time: item.time || '',
    status: item.status || '',
    statusMap: item.statusMap || {},
    icon: item.icon || '',
    number: item.number || '',
    active: item.active || false,
    completed: item.completed || false,
    details: item.details || [],
    actions: item.actions || []
  }))
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  if (isNaN(date.getTime())) return time
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return props.timeFormat
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
}

// 处理操作
const handleAction = (action, item, index) => {
  emit('action', { action, item, index })
}
</script>

<style lang="scss" scoped>
.timeline-container {
  position: relative;
  
  &.timeline--vertical {
    .timeline-item {
      display: flex;
      align-items: flex-start;
      gap: var(--gap-lg);
      margin-bottom: var(--gap-xl);
      
      &:last-child {
        margin-bottom: 0;
        
        .node-line {
          display: none;
        }
      }
      
      .timeline-node {
        position: relative;
        flex-shrink: 0;
        
        .node-dot {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 2px solid var(--border);
          background: var(--surface);
          transition: all 0.3s ease;
          
          .node-icon {
            font-size: 16px;
            color: var(--text-2);
          }
          
          .node-number {
            font-size: var(--font-size-sm);
            font-weight: 600;
            color: var(--text-2);
          }
          
          &--primary {
            border-color: var(--color-primary);
            background: var(--color-primary);
            color: white;
            
            .node-icon,
            .node-number {
              color: white;
            }
          }
          
          &--success {
            border-color: var(--color-success);
            background: var(--color-success);
            color: white;
            
            .node-icon,
            .node-number {
              color: white;
            }
          }
          
          &--warning {
            border-color: var(--color-warning);
            background: var(--color-warning);
            color: white;
            
            .node-icon,
            .node-number {
              color: white;
            }
          }
          
          &--danger {
            border-color: var(--color-error);
            background: var(--color-error);
            color: white;
            
            .node-icon,
            .node-number {
              color: white;
            }
          }
        }
        
        .node-line {
          position: absolute;
          top: 32px;
          left: 50%;
          transform: translateX(-50%);
          width: 2px;
          height: calc(100% + var(--gap-xl));
          background: var(--border);
        }
      }
      
      &.is-active {
        .node-dot {
          box-shadow: 0 0 0 4px rgba(30, 58, 138, 0.2);
        }
      }
      
      &.is-completed {
        .node-dot {
          background: var(--color-success);
          border-color: var(--color-success);
          color: white;
          
          .node-icon,
          .node-number {
            color: white;
          }
        }
      }
      
      .timeline-content {
        flex: 1;
        padding-top: 4px;
        
        .content-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: var(--gap-sm);
          
          .content-title {
            flex: 1;
            
            .item-title {
              margin: 0 0 var(--gap-xs) 0;
              font-size: var(--font-size-base);
              font-weight: 600;
              color: var(--text);
            }
            
            .item-subtitle {
              margin: 0;
              font-size: var(--font-size-sm);
              color: var(--text-2);
            }
          }
          
          .content-meta {
            display: flex;
            align-items: center;
            gap: var(--gap-sm);
            
            .item-time {
              font-size: var(--font-size-sm);
              color: var(--text-3);
              white-space: nowrap;
            }
          }
        }
        
        .content-description {
          margin-bottom: var(--gap-sm);
          font-size: var(--font-size-sm);
          color: var(--text-2);
          line-height: var(--line-height-normal);
        }
        
        .content-details {
          margin-bottom: var(--gap-sm);
          
          .detail-item {
            display: flex;
            gap: var(--gap-sm);
            margin-bottom: var(--gap-xs);
            font-size: var(--font-size-sm);
            
            .detail-label {
              color: var(--text-2);
              font-weight: 500;
              min-width: 80px;
            }
            
            .detail-value {
              color: var(--text);
            }
          }
        }
        
        .content-actions {
          display: flex;
          gap: var(--gap-sm);
        }
      }
    }
  }
  
  &.timeline--horizontal {
    display: flex;
    align-items: flex-start;
    gap: var(--gap-lg);
    overflow-x: auto;
    padding-bottom: var(--gap-md);
    
    .timeline-item {
      flex-shrink: 0;
      width: 200px;
      
      .timeline-node {
        text-align: center;
        margin-bottom: var(--gap-md);
        
        .node-dot {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          border: 2px solid var(--border);
          background: var(--surface);
          margin-bottom: var(--gap-sm);
          
          .node-icon {
            font-size: 12px;
            color: var(--text-2);
          }
          
          .node-number {
            font-size: var(--font-size-xs);
            font-weight: 600;
            color: var(--text-2);
          }
        }
      }
      
      .timeline-content {
        text-align: center;
        
        .content-header {
          flex-direction: column;
          align-items: center;
          gap: var(--gap-xs);
          
          .content-meta {
            flex-direction: column;
            gap: var(--gap-xs);
          }
        }
        
        .content-actions {
          justify-content: center;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .timeline-container {
    &.timeline--vertical {
      .timeline-item {
        .timeline-content {
          .content-header {
            flex-direction: column;
            align-items: stretch;
            gap: var(--gap-xs);
            
            .content-meta {
              justify-content: flex-start;
            }
          }
        }
      }
    }
    
    &.timeline--horizontal {
      .timeline-item {
        width: 150px;
      }
    }
  }
}
</style>
