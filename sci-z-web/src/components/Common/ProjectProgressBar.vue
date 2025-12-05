<!--
/**
 * @description 项目进度条组件
 * 用于展示项目进度，根据进度百分比自动显示不同颜色
 * 支持在项目列表、项目详情、项目进度页面等场景使用
 */
-->
<template>
  <div class="project-progress-bar" :class="[customClass]">
    <div class="progress-bar-wrapper">
      <div class="progress-track" :style="trackStyle">
        <div
          class="progress-fill"
          :class="progressFillClass"
          :style="fillStyle"
        />
      </div>
    </div>
    <span v-if="showText" class="progress-text">{{ displayProgress }}%</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  // 进度值 (0-100)
  progress: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0 && value <= 100
  },
  // 进度条高度
  height: {
    type: [Number, String],
    default: 6
  },
  // 是否显示百分比文本
  showText: {
    type: Boolean,
    default: true
  },
  // 是否显示动画
  animated: {
    type: Boolean,
    default: true
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// 计算属性
const displayProgress = computed(() => {
  const value = Number(props.progress)
  if (isNaN(value) || value < 0) return 0
  if (value > 100) return 100
  return Math.round(value)
})

// 根据进度计算颜色类型
const getProgressColorType = (progress) => {
  if (progress === 0) return 'empty'
  if (progress === 100) return 'success'
  if (progress >= 60) return 'success'
  if (progress >= 30) return 'warning'
  return 'danger'
}

// 进度条填充类名
const progressFillClass = computed(() => {
  const colorType = getProgressColorType(displayProgress.value)
  return `progress-fill--${colorType}`
})

// 进度条轨道样式
const trackStyle = computed(() => {
  return {
    height: typeof props.height === 'number' ? `${props.height}px` : props.height
  }
})

// 进度条填充样式
const fillStyle = computed(() => {
  const styles = {
    width: `${displayProgress.value}%`
  }
  
  if (props.animated) {
    styles.transition = 'width 0.3s ease'
  }
  
  return styles
})
</script>

<style lang="scss" scoped>
.project-progress-bar {
  display: flex;
  align-items: center;
  gap: var(--gap-xs);
  width: 100%;
  
  .progress-bar-wrapper {
    flex: 1;
    min-width: 0;
    
    .progress-track {
      width: 100%;
      background: var(--border);
      border-radius: var(--radius-full);
      overflow: hidden;
      position: relative;
      
      .progress-fill {
        height: 100%;
        border-radius: var(--radius-full);
        position: relative;
        overflow: hidden;
        
        // 空进度（0%）
        &.progress-fill--empty {
          background: var(--border);
        }
        
        // 成功/高进度（>= 60% 或 100%）
        &.progress-fill--success {
          background: #16a34a; // 绿色
        }
        
        // 警告/中等进度（30% - 60%）
        &.progress-fill--warning {
          background: #f59e0b; // 橙色
        }
        
        // 危险/低进度（< 30%）
        &.progress-fill--danger {
          background: #dc2626; // 红色
        }
      }
    }
  }
  
  .progress-text {
    font-size: 12px;
    color: var(--text-secondary);
    min-width: 35px;
    text-align: center;
    flex-shrink: 0;
    font-weight: 500;
  }
}

// 暗色主题适配
:global(.dark) {
  .project-progress-bar {
    .progress-bar-wrapper {
      .progress-track {
        background: rgba(255, 255, 255, 0.1);
        
        .progress-fill {
          &.progress-fill--empty {
            background: rgba(255, 255, 255, 0.1);
          }
          
          &.progress-fill--success {
            background: #22c55e; // 暗色主题下的绿色
          }
          
          &.progress-fill--warning {
            background: #fbbf24; // 暗色主题下的橙色
          }
          
          &.progress-fill--danger {
            background: #ef4444; // 暗色主题下的红色
          }
        }
      }
    }
    
    .progress-text {
      color: var(--text-2);
    }
  }
}
</style>

