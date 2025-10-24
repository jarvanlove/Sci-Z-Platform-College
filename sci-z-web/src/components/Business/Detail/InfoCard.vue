<!--
/**
 * @description 信息卡片组件
 * 用于展示详细信息，支持多种布局和样式
 * 包含标题、内容、操作按钮、状态显示等功能
 */
-->
<template>
  <BaseCard class="info-card" :class="[`info-card--${variant}`, customClass]">
    <!-- 卡片头部 -->
    <div v-if="showHeader" class="card-header">
      <div class="header-left">
        <h3 v-if="title" class="card-title">
          <el-icon v-if="icon" class="title-icon">
            <component :is="icon" />
          </el-icon>
          {{ title }}
        </h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-right">
        <slot name="header-actions">
          <StatusTag
            v-if="status"
            :status="status"
            :status-map="statusMap"
            :show-icon="showStatusIcon"
          />
          <BaseButton
            v-if="showEdit"
            type="primary"
            size="small"
            @click="handleEdit"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </BaseButton>
        </slot>
      </div>
    </div>
    
    <!-- 卡片内容 -->
    <div class="card-content">
      <!-- 信息列表 -->
      <div v-if="showInfoList" class="info-list">
        <div
          v-for="item in infoItems"
          :key="item.key"
          class="info-item"
          :class="{ 'is-highlight': item.highlight }"
        >
          <div class="info-label">
            <el-icon v-if="item.icon" class="label-icon">
              <component :is="item.icon" />
            </el-icon>
            {{ item.label }}
          </div>
          <div class="info-value">
            <span v-if="item.type === 'text'">{{ item.value }}</span>
            <StatusTag
              v-else-if="item.type === 'status'"
              :status="item.value"
              :status-map="item.statusMap"
              :size="item.tagSize || 'small'"
            />
            <el-tag
              v-else-if="item.type === 'tag'"
              :type="item.tagType"
              :size="item.tagSize || 'small'"
            >
              {{ item.value }}
            </el-tag>
            <el-link
              v-else-if="item.type === 'link'"
              :href="item.href"
              :target="item.target || '_blank'"
              :type="item.linkType"
            >
              {{ item.value }}
            </el-link>
            <span v-else-if="item.type === 'date'" class="date-value">
              {{ formatDate(item.value, item.dateFormat) }}
            </span>
            <span v-else-if="item.type === 'number'" class="number-value">
              {{ formatNumber(item.value, item.numberFormat) }}
            </span>
            <slot
              v-else-if="item.type === 'slot'"
              :name="item.slotName"
              :item="item"
              :value="item.value"
            />
          </div>
        </div>
      </div>
      
      <!-- 自定义内容 -->
      <div v-if="$slots.default" class="custom-content">
        <slot></slot>
      </div>
    </div>
    
    <!-- 卡片底部 -->
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import { BaseCard, BaseButton, StatusTag } from '@/components/Common'
import { Edit } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 卡片标题
  title: {
    type: String,
    default: ''
  },
  // 卡片副标题
  subtitle: {
    type: String,
    default: ''
  },
  // 卡片图标
  icon: {
    type: String,
    default: ''
  },
  // 卡片变体
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'danger', 'info'].includes(value)
  },
  // 状态信息
  status: {
    type: [String, Number],
    default: ''
  },
  // 状态映射
  statusMap: {
    type: Object,
    default: () => ({})
  },
  // 是否显示状态图标
  showStatusIcon: {
    type: Boolean,
    default: true
  },
  // 信息项配置
  infoItems: {
    type: Array,
    default: () => []
  },
  // 是否显示信息列表
  showInfoList: {
    type: Boolean,
    default: true
  },
  // 是否显示头部
  showHeader: {
    type: Boolean,
    default: true
  },
  // 是否显示编辑按钮
  showEdit: {
    type: Boolean,
    default: false
  },
  // 布局方式
  layout: {
    type: String,
    default: 'vertical',
    validator: (value) => ['vertical', 'horizontal', 'grid'].includes(value)
  },
  // 网格列数
  gridColumns: {
    type: Number,
    default: 2
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['edit'])

// 处理编辑
const handleEdit = () => {
  emit('edit')
}

// 格式化日期
const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return '-'
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return date
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 格式化数字
const formatNumber = (number, format = {}) => {
  if (number === null || number === undefined) return '-'
  
  const { decimals = 0, separator = ',', prefix = '', suffix = '' } = format
  
  let formatted = Number(number).toFixed(decimals)
  
  if (separator) {
    formatted = formatted.replace(/\B(?=(\d{3})+(?!\d))/g, separator)
  }
  
  return `${prefix}${formatted}${suffix}`
}
</script>

<style lang="scss" scoped>
.info-card {
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
  
  &--info {
    border-left: 4px solid var(--color-info);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--gap-lg);
    
    .header-left {
      flex: 1;
      
      .card-title {
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
      
      .card-subtitle {
        margin: 0;
        color: var(--text-2);
        font-size: var(--font-size-sm);
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: var(--gap-sm);
    }
  }
  
  .card-content {
    .info-list {
      display: grid;
      gap: var(--gap-md);
      
      &.layout-vertical {
        grid-template-columns: 1fr;
      }
      
      &.layout-horizontal {
        grid-template-columns: repeat(2, 1fr);
      }
      
      &.layout-grid {
        grid-template-columns: repeat(v-bind(gridColumns), 1fr);
      }
      
      .info-item {
        display: flex;
        align-items: flex-start;
        gap: var(--gap-sm);
        padding: var(--gap-sm);
        border-radius: var(--radius-sm);
        transition: background-color 0.3s ease;
        
        &:hover {
          background-color: var(--hover);
        }
        
        &.is-highlight {
          background-color: var(--hover);
          border: 1px solid var(--color-primary);
        }
        
        .info-label {
          display: flex;
          align-items: center;
          gap: var(--gap-xs);
          min-width: 100px;
          font-size: var(--font-size-sm);
          color: var(--text-2);
          font-weight: 500;
          
          .label-icon {
            color: var(--color-primary);
            font-size: 14px;
          }
        }
        
        .info-value {
          flex: 1;
          font-size: var(--font-size-sm);
          color: var(--text);
          
          .date-value,
          .number-value {
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          }
        }
      }
    }
    
    .custom-content {
      // 自定义内容样式可以在这里添加
    }
  }
  
  .card-footer {
    margin-top: var(--gap-lg);
    padding-top: var(--gap-lg);
    border-top: 1px solid var(--border);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .info-card {
    .card-header {
      flex-direction: column;
      gap: var(--gap-sm);
      align-items: stretch;
      
      .header-right {
        justify-content: flex-end;
      }
    }
    
    .card-content {
      .info-list {
        &.layout-horizontal,
        &.layout-grid {
          grid-template-columns: 1fr;
        }
        
        .info-item {
          flex-direction: column;
          align-items: stretch;
          gap: var(--gap-xs);
          
          .info-label {
            min-width: auto;
          }
        }
      }
    }
  }
}
</style>
