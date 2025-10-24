<!--
/**
 * @description 仪表板统计卡片组件
 * 基于原型图设计的仪表板统计卡片，专门用于仪表板页面
 * 包含项目数量、申报数量、报告数量等统计信息展示
 */
-->
<template>
  <BaseCard class="dashboard-stat-card" :class="[`stat--${type}`, customClass]">
    <div class="stat-content">
      <!-- 统计数值 -->
      <div class="stat-value">
        <span class="value-number">{{ formattedValue }}</span>
        <span v-if="unit" class="value-unit">{{ unit }}</span>
      </div>
      
      <!-- 统计标题 -->
      <div class="stat-title">{{ title }}</div>
      
      <!-- 趋势指示器 -->
      <div v-if="showTrend" class="stat-trend" :class="`trend--${trend.type}`">
        <el-icon class="trend-icon">
          <component :is="trend.type === 'up' ? 'ArrowUp' : 'ArrowDown'" />
        </el-icon>
        <span class="trend-text">{{ trend.text }}</span>
      </div>
    </div>
    
    <!-- 统计图标 -->
    <div class="stat-icon" :class="`icon--${type}`">
      <el-icon v-if="icon" class="icon">
        <component :is="icon" />
      </el-icon>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import { BaseCard } from '@/components/Common'

// Props 定义
const props = defineProps({
  // 统计类型
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'danger'].includes(value)
  },
  // 统计标题
  title: {
    type: String,
    required: true
  },
  // 统计数值
  value: {
    type: [Number, String],
    required: true
  },
  // 数值单位
  unit: {
    type: String,
    default: ''
  },
  // 图标
  icon: {
    type: String,
    default: ''
  },
  // 趋势信息
  trend: {
    type: Object,
    default: () => ({})
  },
  // 是否显示趋势
  showTrend: {
    type: Boolean,
    default: false
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// 格式化数值
const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})
</script>

<style lang="scss" scoped>
.dashboard-stat-card {
  position: relative;
  padding: var(--gap-lg);
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .stat-content {
    position: relative;
    z-index: 2;
  }

  .stat-value {
    display: flex;
    align-items: baseline;
    gap: 4px;
    margin-bottom: var(--gap-xs);

    .value-number {
      font-size: 32px;
      font-weight: 700;
      color: var(--text);
      line-height: 1;
    }

    .value-unit {
      font-size: 14px;
      color: var(--text-2);
      font-weight: 500;
    }
  }

  .stat-title {
    font-size: 14px;
    color: var(--text-2);
    margin-bottom: var(--gap-xs);
  }

  .stat-trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 500;

    &.trend--up {
      color: var(--color-success);
    }

    &.trend--down {
      color: var(--color-danger);
    }

    .trend-icon {
      font-size: 12px;
    }
  }

  .stat-icon {
    position: absolute;
    top: var(--gap-lg);
    right: var(--gap-lg);
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0.1;

    .icon {
      font-size: 24px;
    }

    &.icon--default {
      background: var(--color-primary);
      color: var(--color-primary);
    }

    &.icon--primary {
      background: var(--color-primary);
      color: var(--color-primary);
    }

    &.icon--success {
      background: var(--color-success);
      color: var(--color-success);
    }

    &.icon--warning {
      background: var(--color-warning);
      color: var(--color-warning);
    }

    &.icon--danger {
      background: var(--color-danger);
      color: var(--color-danger);
    }
  }

  // 不同类型样式
  &.stat--primary {
    border-left: 4px solid var(--color-primary);
  }

  &.stat--success {
    border-left: 4px solid var(--color-success);
  }

  &.stat--warning {
    border-left: 4px solid var(--color-warning);
  }

  &.stat--danger {
    border-left: 4px solid var(--color-danger);
  }
}
</style>
