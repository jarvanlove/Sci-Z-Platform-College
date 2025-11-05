<!--
/**
 * @description 状态标签组件
 * 用于显示各种状态信息，支持多种状态类型和样式
 * 包含状态映射、颜色配置、图标显示等功能
 */
-->
<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    :class="['status-tag', customClass]"
    :style="customStyle"
  >
    <el-icon v-if="showIcon && statusConfig.icon" class="status-icon">
      <component :is="statusConfig.icon" />
    </el-icon>
    <span class="status-text">{{ displayText }}</span>
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  // 状态值
  status: {
    type: [String, Number],
    required: true
  },
  // 状态配置映射
  statusMap: {
    type: Object,
    default: () => ({})
  },
  // 标签尺寸
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  // 标签效果
  effect: {
    type: String,
    default: 'light',
    validator: (value) => ['dark', 'light', 'plain'].includes(value)
  },
  // 是否显示图标
  showIcon: {
    type: Boolean,
    default: true
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },
  // 自定义样式
  customStyle: {
    type: Object,
    default: () => ({})
  },
  // 默认状态配置
  defaultConfig: {
    type: Object,
    default: () => ({
      text: '未知状态',
      type: 'info',
      icon: null
    })
  }
})

// 默认状态配置
const defaultStatusMap = {
  // 通用状态
  'active': { text: '激活', type: 'success', icon: 'Check' },
  'inactive': { text: '未激活', type: 'info', icon: 'Close' },
  'pending': { text: '待处理', type: 'warning', icon: 'Clock' },
  'processing': { text: '处理中', type: 'primary', icon: 'Loading' },
  'completed': { text: '已完成', type: 'success', icon: 'Check' },
  'failed': { text: '失败', type: 'danger', icon: 'Close' },
  'cancelled': { text: '已取消', type: 'info', icon: 'Close' },
  
  // 申报状态
  'draft': { text: '草稿', type: 'info', icon: 'Edit' },
  'submitted': { text: '已提交', type: 'primary', icon: 'Upload' },
  'reviewing': { text: '审核中', type: 'warning', icon: 'View' },
  'approved': { text: '已通过', type: 'success', icon: 'Check' },
  'rejected': { text: '已拒绝', type: 'danger', icon: 'Close' },
  
  // 项目状态
  'planning': { text: '规划中', type: 'info', icon: 'Calendar' },
  'in_progress': { text: '进行中', type: 'primary', icon: 'Loading' },
  'on_hold': { text: '暂停', type: 'warning', icon: 'VideoPause' },
  'finished': { text: '已完成', type: 'success', icon: 'Check' },
  'terminated': { text: '已终止', type: 'danger', icon: 'Close' },
  
  // 用户状态
  'enabled': { text: '启用', type: 'success', icon: 'Check' },
  'disabled': { text: '禁用', type: 'danger', icon: 'Close' },
  'locked': { text: '锁定', type: 'warning', icon: 'Lock' },
  
  // 系统状态
  'online': { text: '在线', type: 'success', icon: 'CircleCheck' },
  'offline': { text: '离线', type: 'info', icon: 'CircleClose' },
  'maintenance': { text: '维护中', type: 'warning', icon: 'Tools' }
}

// 计算状态配置
const statusConfig = computed(() => {
  const config = props.statusMap[props.status] || defaultStatusMap[props.status] || props.defaultConfig
  
  return {
    text: config.text || '未知状态',
    type: config.type || 'info',
    icon: config.icon || null
  }
})

// 计算标签类型
const tagType = computed(() => {
  const typeMap = {
    'success': 'success',
    'primary': 'primary',
    'warning': 'warning',
    'danger': 'danger',
    'info': 'info'
  }
  return typeMap[statusConfig.value.type] || 'info'
})

// 计算显示文本
const displayText = computed(() => {
  return statusConfig.value.text
})
</script>

<style lang="scss" scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  
  .status-icon {
    font-size: 12px;
  }
  
  .status-text {
    font-weight: 500;
  }
}

// 自定义状态样式
.status-tag {
  &.custom-success {
    background-color: var(--color-success);
    color: white;
    border-color: var(--color-success);
  }
  
  &.custom-warning {
    background-color: var(--color-warning);
    color: white;
    border-color: var(--color-warning);
  }
  
  &.custom-danger {
    background-color: var(--color-error);
    color: white;
    border-color: var(--color-error);
  }
  
  &.custom-info {
    background-color: var(--color-info);
    color: white;
    border-color: var(--color-info);
  }
}

// 动画效果
.status-tag {
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
}
</style>
