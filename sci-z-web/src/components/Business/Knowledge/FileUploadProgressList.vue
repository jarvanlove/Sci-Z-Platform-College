<!--
/**
 * @description 文件上传进度列表组件
 * 用于显示批量文件上传的进度和状态，符合行业设计标准
 * 支持多阶段进度显示（MinIO上传、Dify上传、完成等）
 */
-->
<template>
  <div class="file-upload-progress-list">
    <div class="progress-header">
      <div class="header-title">
        <el-icon class="title-icon"><Upload /></el-icon>
        <span>{{ $t('knowledge.uploadingFiles') }}</span>
      </div>
      <div class="header-right">
        <div class="header-stats">
          <span class="stat-item success">
            <el-icon><CircleCheck /></el-icon>
            {{ successCount }}/{{ totalCount }}
          </span>
          <span class="stat-item error">
            <el-icon><CircleClose /></el-icon>
            {{ errorCount }}/{{ totalCount }}
          </span>
        </div>
      </div>
    </div>

    <div class="progress-list">
      <div
        v-for="(item, index) in visibleResults"
        :key="item.fileName || index"
        class="progress-item"
        :class="getItemClass(item)"
      >
        <!-- 文件图标和名称 -->
        <div class="file-info">
          <div class="file-icon-wrapper">
            <!-- 🔥 使用封装好的 FileTypeIcon 组件 -->
            <FileTypeIcon
              :extension="getFileExtension(item.fileName)"
              :fileName="item.fileName"
              size="small"
            />
            <div v-if="item.success" class="status-badge success">
              <el-icon><Check /></el-icon>
            </div>
            <div v-else-if="item.errorMessage" class="status-badge error">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div v-else class="status-badge loading">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
          </div>
          <div class="file-details">
            <div class="file-name" :title="item.fileName">
              {{ item.fileName }}
            </div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(item.fileSize) }}</span>
              <span class="file-stage">{{ getStageText(item) }}</span>
            </div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-bar-wrapper">
          <el-progress
            :percentage="getProgressPercentage(item)"
            :status="getProgressStatus(item)"
            :stroke-width="6"
            :show-text="false"
            :indeterminate="isIndeterminate(item)"
          />
          <div class="progress-text">
            <span v-if="item.success" class="text-success">
              {{ $t('knowledge.uploadComplete') }}
            </span>
            <span v-else-if="item.errorMessage" class="text-error">
              {{ item.errorMessage }}
            </span>
            <span v-else class="text-progress">
              {{ getStageDescription(item) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div v-if="allCompleted" class="progress-actions">
      <el-button type="primary" @click="handleClose">
        {{ $t('common.confirm') }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Upload,
  CircleCheck,
  CircleClose,
  Check,
  Loading
} from '@element-plus/icons-vue'
import { FileTypeIcon } from '@/components/Common'

const props = defineProps({
  /**
   * 上传结果列表
   * @type {Array<{
   *   fileName: string,
   *   success: boolean,
   *   errorMessage?: string,
   *   attachmentId?: number,
   *   fileSize: number,
   *   stage: number,
   *   stageDescription: string
   * }>}
   */
  uploadResults: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close'])

const { t } = useI18n()

// 🔥 显示所有文件（包括成功的），让用户能看到完整的上传状态
const visibleResults = computed(() => {
  return props.uploadResults
})

// 计算属性
const totalCount = computed(() => props.uploadResults.length)
const successCount = computed(() => 
  props.uploadResults.filter(item => item.success).length
)
const errorCount = computed(() => 
  props.uploadResults.filter(item => item.errorMessage).length
)
const allCompleted = computed(() => 
  props.uploadResults.every(item => item.success || item.errorMessage)
)
// 🔥 全部成功（没有失败的文件）
const allSuccess = computed(() => 
  props.uploadResults.length > 0 && 
  props.uploadResults.every(item => item.success) &&
  errorCount.value === 0
)

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 获取文件扩展名
const getFileExtension = (fileName) => {
  if (!fileName) return ''
  const parts = fileName.split('.')
  return parts.length > 1 ? parts[parts.length - 1] : ''
}

// 获取项目类名
const getItemClass = (item) => {
  if (item.success) return 'item-success'
  if (item.errorMessage) return 'item-error'
  return 'item-loading'
}

// 获取进度百分比
const getProgressPercentage = (item) => {
  if (item.success) return 100
  if (item.errorMessage) return 0
  
  // 根据阶段计算进度
  // Stage 0: 0% (校验失败)
  // Stage 1: 20% (MinIO上传中)
  // Stage 2: 40% (MinIO完成)
  // Stage 3: 60% (Dify上传中)
  // Stage 4: 80% (Dify完成，处理中)
  // Stage 5: 100% (全部完成)
  const stagePercentages = {
    0: 0,
    1: 20,
    2: 40,
    3: 60,
    4: 80,
    5: 100
  }
  return stagePercentages[item.stage] || 0
}

// 获取进度条状态
const getProgressStatus = (item) => {
  if (item.success) return 'success'
  if (item.errorMessage) return 'exception'
  return null
}

// 是否显示不确定进度（加载动画）
const isIndeterminate = (item) => {
  return !item.success && !item.errorMessage && item.stage > 0
}

// 获取阶段文本
const getStageText = (item) => {
  if (item.success) return t('knowledge.uploadComplete')
  if (item.errorMessage) return t('knowledge.uploadFailed')
  
  const stageTexts = {
    0: t('knowledge.stageValidation'),
    1: t('knowledge.stageMinIO'),
    2: t('knowledge.stageMinIOComplete'),
    3: t('knowledge.stageDify'),
    4: t('knowledge.stageProcessing'),
    5: t('knowledge.stageComplete')
  }
  return stageTexts[item.stage] || t('knowledge.stageUnknown')
}

// 获取阶段描述
const getStageDescription = (item) => {
  return item.stageDescription || getStageText(item)
}

// 关闭处理
const handleClose = () => {
  emit('close')
}

// 🔥 监听全部成功，自动关闭（只有全部成功且没有失败时才自动关闭）
watch(allSuccess, (newVal) => {
  if (newVal && props.uploadResults.length > 0) {
    // 延迟一下，让用户看到成功状态
    setTimeout(() => {
      handleClose()
    }, 500)
  }
})
</script>

<style scoped lang="scss">
.file-upload-progress-list {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  
  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    
    .title-icon {
      font-size: 20px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .header-stats {
    display: flex;
    gap: 16px;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      padding: 4px 12px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      backdrop-filter: blur(10px);
      
      &.success {
        background: rgba(103, 194, 58, 0.3);
      }
      
      &.error {
        background: rgba(245, 108, 108, 0.3);
      }
    }
  }
  
}

.progress-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  
  .progress-item {
    padding: 16px;
    margin-bottom: 12px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e9ecef;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
    
    &.item-success {
      background: #f0f9ff;
      border-color: #1a5f1a; // 🔥 墨绿色
    }
    
    &.item-error {
      background: #fef0f0;
      border-color: #f56c6c;
    }
    
    &.item-loading {
      background: #fffbf0;
      border-color: #e6a23c;
    }
  }
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  
  .file-icon-wrapper {
    position: relative;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    // 🔥 FileTypeIcon 组件自带样式，不需要额外的 wrapper 样式
    :deep(.file-type-icon) {
      width: 40px;
      height: 40px;
    }
    
    .status-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid #fff;
      font-size: 12px;
      
      &.success {
        background: #1a5f1a; // 🔥 墨绿色
        color: #fff;
      }
      
      &.error {
        background: #f56c6c;
        color: #fff;
      }
      
      &.loading {
        background: #409eff;
        color: #fff;
      }
    }
  }
  
  .file-details {
    flex: 1;
    min-width: 0;
    
    .file-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .file-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 12px;
      color: #909399;
      
      .file-size {
        &::after {
          content: ' • ';
          margin: 0 4px;
        }
      }
      
      .file-stage {
        color: #606266;
      }
    }
  }
}

.progress-bar-wrapper {
  // 🔥 覆盖 el-progress 组件的成功状态颜色为墨绿色
  :deep(.el-progress-bar__outer) {
    .el-progress-bar__inner {
      &.is-success {
        background-color: #1a5f1a !important; // 墨绿色
      }
    }
  }
  
  :deep(.el-progress.is-success) {
    .el-progress-bar__inner {
      background-color: #1a5f1a !important; // 墨绿色
    }
  }
  
  .progress-text {
    margin-top: 8px;
    font-size: 12px;
    text-align: center;
    
    .text-success {
      color: #1a5f1a; // 🔥 墨绿色
    }
    
    .text-error {
      color: #f56c6c;
    }
    
    .text-progress {
      color: #409eff;
    }
  }
}

.progress-actions {
  padding: 16px 20px;
  border-top: 1px solid #e9ecef;
  text-align: right;
}

// 滚动条样式
.progress-list {
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
}
</style>
