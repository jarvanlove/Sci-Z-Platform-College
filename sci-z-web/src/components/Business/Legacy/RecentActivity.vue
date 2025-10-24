<!--
/**
 * @description 最近活动组件
 * 用于展示最近的活动记录，支持多种样式和交互
 * 包含时间线、状态显示、操作按钮等功能
 */
-->
<template>
  <BaseCard class="recent-activity-container" :class="customClass">
    <!-- 组件头部 -->
    <div v-if="showHeader" class="activity-header">
      <div class="header-left">
        <h3 class="activity-title">
          <el-icon v-if="icon" class="title-icon">
            <component :is="icon" />
          </el-icon>
          {{ title }}
        </h3>
        <p v-if="description" class="activity-description">{{ description }}</p>
      </div>
      <div class="header-right">
        <slot name="header-actions">
          <BaseButton
            v-if="showRefresh"
            type="text"
            size="small"
            :loading="loading"
            @click="handleRefresh"
          >
            <el-icon><Refresh /></el-icon>
          </BaseButton>
          <BaseButton
            v-if="showMore"
            type="text"
            size="small"
            @click="handleViewMore"
          >
            查看更多
            <el-icon class="el-icon--right">
              <ArrowRight />
            </el-icon>
          </BaseButton>
        </slot>
      </div>
    </div>
    
    <!-- 活动列表 -->
    <div v-if="activities.length > 0" class="activity-list">
      <div
        v-for="(activity, index) in displayActivities"
        :key="activity.id || index"
        class="activity-item"
        :class="{ 'is-unread': activity.unread }"
        @click="handleActivityClick(activity, index)"
      >
        <!-- 活动图标 -->
        <div class="activity-icon" :class="`icon--${activity.type || 'default'}`">
          <el-icon v-if="activity.icon" class="icon">
            <component :is="activity.icon" />
          </el-icon>
          <img v-else-if="activity.avatar" :src="activity.avatar" :alt="activity.user" class="avatar" />
          <div v-else class="default-avatar">
            {{ activity.user ? activity.user.charAt(0) : 'A' }}
          </div>
        </div>
        
        <!-- 活动内容 -->
        <div class="activity-content">
          <div class="activity-text">
            <span class="user-name">{{ activity.user }}</span>
            <span class="action-text">{{ activity.action }}</span>
            <span class="target-text">{{ activity.target }}</span>
          </div>
          <div class="activity-meta">
            <span class="activity-time">{{ formatTime(activity.time) }}</span>
            <StatusTag
              v-if="activity.status"
              :status="activity.status"
              :status-map="statusMap"
              :size="'small'"
            />
          </div>
        </div>
        
        <!-- 活动操作 -->
        <div v-if="activity.actions && activity.actions.length > 0" class="activity-actions">
          <BaseButton
            v-for="action in activity.actions"
            :key="action.key"
            :type="action.type || 'text'"
            :size="'small'"
            @click.stop="handleActionClick(action, activity, index)"
          >
            {{ action.label }}
          </BaseButton>
        </div>
        
        <!-- 未读标识 -->
        <div v-if="activity.unread" class="unread-indicator"></div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty :description="emptyText">
        <BaseButton v-if="showRefresh" @click="handleRefresh">
          刷新
        </BaseButton>
      </el-empty>
    </div>
    
    <!-- 加载更多 -->
    <div v-if="showLoadMore && hasMore" class="load-more">
      <BaseButton
        type="text"
        :loading="loadingMore"
        @click="handleLoadMore"
      >
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </BaseButton>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import { BaseCard, BaseButton, StatusTag } from '@/components/Common'
import { Refresh, ArrowRight } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 标题
  title: {
    type: String,
    default: '最近活动'
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
  // 活动列表
  activities: {
    type: Array,
    default: () => []
  },
  // 显示数量
  displayCount: {
    type: Number,
    default: 5
  },
  // 是否显示头部
  showHeader: {
    type: Boolean,
    default: true
  },
  // 是否显示刷新按钮
  showRefresh: {
    type: Boolean,
    default: true
  },
  // 是否显示查看更多
  showMore: {
    type: Boolean,
    default: true
  },
  // 是否显示加载更多
  showLoadMore: {
    type: Boolean,
    default: false
  },
  // 是否还有更多数据
  hasMore: {
    type: Boolean,
    default: false
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 加载更多状态
  loadingMore: {
    type: Boolean,
    default: false
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无活动记录'
  },
  // 状态映射
  statusMap: {
    type: Object,
    default: () => ({
      'success': { text: '成功', type: 'success' },
      'warning': { text: '警告', type: 'warning' },
      'error': { text: '错误', type: 'danger' },
      'info': { text: '信息', type: 'info' }
    })
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits([
  'refresh',
  'view-more',
  'load-more',
  'activity-click',
  'action-click'
])

// 计算属性
const displayActivities = computed(() => {
  return props.activities.slice(0, props.displayCount)
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const activityTime = new Date(time)
  const diff = now.getTime() - activityTime.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return activityTime.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 处理刷新
const handleRefresh = () => {
  emit('refresh')
}

// 处理查看更多
const handleViewMore = () => {
  emit('view-more')
}

// 处理加载更多
const handleLoadMore = () => {
  emit('load-more')
}

// 处理活动点击
const handleActivityClick = (activity, index) => {
  emit('activity-click', { activity, index })
}

// 处理操作点击
const handleActionClick = (action, activity, index) => {
  emit('action-click', { action, activity, index })
}
</script>

<style lang="scss" scoped>
.recent-activity-container {
  .activity-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--gap-lg);
    
    .header-left {
      flex: 1;
      
      .activity-title {
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
      
      .activity-description {
        margin: 0;
        font-size: var(--font-size-sm);
        color: var(--text-2);
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: var(--gap-sm);
    }
  }
  
  .activity-list {
    .activity-item {
      position: relative;
      display: flex;
      align-items: flex-start;
      gap: var(--gap-md);
      padding: var(--gap-md);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: var(--hover);
      }
      
      &.is-unread {
        background: var(--hover);
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 20px;
          background: var(--color-primary);
          border-radius: 0 2px 2px 0;
        }
      }
      
      .activity-icon {
        flex-shrink: 0;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--bg);
        color: var(--text-2);
        
        .icon {
          font-size: 20px;
        }
        
        .avatar {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          object-fit: cover;
        }
        
        .default-avatar {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: var(--color-primary);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: var(--font-size-base);
          font-weight: 600;
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
      }
      
      .activity-content {
        flex: 1;
        min-width: 0;
        
        .activity-text {
          font-size: var(--font-size-sm);
          color: var(--text);
          margin-bottom: var(--gap-xs);
          line-height: var(--line-height-normal);
          
          .user-name {
            font-weight: 600;
            color: var(--color-primary);
          }
          
          .action-text {
            margin: 0 var(--gap-xs);
          }
          
          .target-text {
            font-weight: 500;
          }
        }
        
        .activity-meta {
          display: flex;
          align-items: center;
          gap: var(--gap-sm);
          
          .activity-time {
            font-size: var(--font-size-xs);
            color: var(--text-3);
          }
        }
      }
      
      .activity-actions {
        flex-shrink: 0;
        display: flex;
        gap: var(--gap-xs);
      }
      
      .unread-indicator {
        position: absolute;
        top: var(--gap-md);
        right: var(--gap-md);
        width: 8px;
        height: 8px;
        background: var(--color-primary);
        border-radius: 50%;
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: var(--gap-3xl) var(--gap-lg);
  }
  
  .load-more {
    margin-top: var(--gap-lg);
    text-align: center;
    padding-top: var(--gap-md);
    border-top: 1px solid var(--border);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .recent-activity-container {
    .activity-header {
      flex-direction: column;
      gap: var(--gap-sm);
      align-items: stretch;
      
      .header-right {
        justify-content: flex-end;
      }
    }
    
    .activity-list {
      .activity-item {
        .activity-actions {
          flex-direction: column;
          gap: var(--gap-xs);
        }
      }
    }
  }
}
</style>
