<!--
/**
 * @description 附件列表组件
 * 用于展示和管理附件文件
 * 支持预览、下载、删除、上传等功能
 */
-->
<template>
  <div class="attachment-list-container" :class="customClass">
    <!-- 附件列表头部 -->
    <div v-if="showHeader" class="attachment-header">
      <div class="header-left">
        <h4 v-if="title" class="attachment-title">
          <el-icon class="title-icon">
            <Paperclip />
          </el-icon>
          {{ title }}
        </h4>
        <p v-if="description" class="attachment-description">{{ description }}</p>
      </div>
      <div class="header-right">
        <BaseButton
          v-if="showUpload"
          type="primary"
          size="small"
          @click="handleUpload"
        >
          <el-icon><Plus /></el-icon>
          上传附件
        </BaseButton>
      </div>
    </div>
    
    <!-- 附件列表 -->
    <div v-if="attachments.length > 0" class="attachment-list">
      <div
        v-for="(attachment, index) in attachments"
        :key="attachment.id || index"
        class="attachment-item"
        :class="{ 'is-uploading': attachment.status === 'uploading' }"
      >
        <!-- 文件图标 -->
        <div class="file-icon">
          <el-icon class="icon">
            <component :is="getFileIcon(attachment)" />
          </el-icon>
        </div>
        
        <!-- 文件信息 -->
        <div class="file-info">
          <div class="file-name" :title="attachment.name">
            {{ attachment.name }}
          </div>
          <div class="file-meta">
            <span class="file-size">{{ formatFileSize(attachment.size) }}</span>
            <span v-if="attachment.uploadTime" class="file-time">
              {{ formatTime(attachment.uploadTime) }}
            </span>
            <span v-if="attachment.uploader" class="file-uploader">
              上传者: {{ attachment.uploader }}
            </span>
          </div>
        </div>
        
        <!-- 上传进度 -->
        <div v-if="attachment.status === 'uploading'" class="upload-progress">
          <el-progress
            :percentage="attachment.progress || 0"
            :stroke-width="4"
            :show-text="false"
          />
        </div>
        
        <!-- 文件状态 -->
        <div class="file-status">
          <StatusTag
            v-if="attachment.status"
            :status="attachment.status"
            :status-map="statusMap"
            :size="'small'"
          />
        </div>
        
        <!-- 操作按钮 -->
        <div class="file-actions">
          <el-tooltip content="预览" placement="top">
            <el-button
              v-if="canPreview(attachment)"
              type="text"
              size="small"
              @click="handlePreview(attachment)"
            >
              <el-icon><View /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="下载" placement="top">
            <el-button
              v-if="attachment.url"
              type="text"
              size="small"
              @click="handleDownload(attachment)"
            >
              <el-icon><Download /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="删除" placement="top">
            <el-button
              v-if="showDelete"
              type="text"
              size="small"
              @click="handleDelete(attachment, index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty :description="emptyText">
        <BaseButton
          v-if="showUpload"
          type="primary"
          @click="handleUpload"
        >
          <el-icon><Plus /></el-icon>
          上传附件
        </BaseButton>
      </el-empty>
    </div>
    
    <!-- 上传对话框 -->
    <BaseDialog
      v-model="uploadDialogVisible"
      title="上传附件"
      width="600px"
    >
      <FileUpload
        :action="uploadAction"
        :headers="uploadHeaders"
        :data="uploadData"
        :multiple="true"
        :limit="uploadLimit"
        :max-size="uploadMaxSize"
        :allowed-types="uploadAllowedTypes"
        @success="handleUploadSuccess"
        @error="handleUploadError"
      />
    </BaseDialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BaseButton, BaseDialog, StatusTag } from '@/components/Common'
import FileUpload from '../Form/FileUpload.vue'
import {
  Paperclip,
  Plus,
  View,
  Download,
  Delete,
  Document,
  Picture,
  VideoPlay,
  Folder,
  FileText
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 附件列表
  attachments: {
    type: Array,
    default: () => []
  },
  // 标题
  title: {
    type: String,
    default: '附件列表'
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
  // 是否显示上传按钮
  showUpload: {
    type: Boolean,
    default: true
  },
  // 是否显示删除按钮
  showDelete: {
    type: Boolean,
    default: true
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无附件'
  },
  // 状态映射
  statusMap: {
    type: Object,
    default: () => ({
      'uploading': { text: '上传中', type: 'primary' },
      'success': { text: '上传成功', type: 'success' },
      'failed': { text: '上传失败', type: 'danger' }
    })
  },
  // 上传配置
  uploadAction: {
    type: String,
    default: '/api/upload'
  },
  uploadHeaders: {
    type: Object,
    default: () => ({})
  },
  uploadData: {
    type: Object,
    default: () => ({})
  },
  uploadLimit: {
    type: Number,
    default: 10
  },
  uploadMaxSize: {
    type: Number,
    default: 10
  },
  uploadAllowedTypes: {
    type: Array,
    default: () => []
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['upload', 'preview', 'download', 'delete', 'upload-success', 'upload-error'])

// 响应式数据
const uploadDialogVisible = ref(false)

// 获取文件图标
const getFileIcon = (attachment) => {
  const name = attachment.name || ''
  const extension = name.split('.').pop()?.toLowerCase()
  
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)) {
    return Picture
  } else if (['mp4', 'avi', 'mov', 'wmv', 'flv'].includes(extension)) {
    return VideoPlay
  } else if (['zip', 'rar', '7z', 'tar', 'gz'].includes(extension)) {
    return Folder
  } else if (['pdf', 'doc', 'docx', 'txt'].includes(extension)) {
    return FileText
  } else {
    return Document
  }
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  let fileSize = size
  
  while (fileSize >= 1024 && unitIndex < units.length - 1) {
    fileSize /= 1024
    unitIndex++
  }
  
  return `${fileSize.toFixed(1)} ${units[unitIndex]}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  if (isNaN(date.getTime())) return time
  
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 判断是否可以预览
const canPreview = (attachment) => {
  const name = attachment.name || ''
  const extension = name.split('.').pop()?.toLowerCase()
  const previewableTypes = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'pdf', 'txt']
  return previewableTypes.includes(extension)
}

// 处理上传
const handleUpload = () => {
  uploadDialogVisible.value = true
  emit('upload')
}

// 处理预览
const handlePreview = (attachment) => {
  emit('preview', attachment)
}

// 处理下载
const handleDownload = (attachment) => {
  if (attachment.url) {
    window.open(attachment.url, '_blank')
  }
  emit('download', attachment)
}

// 处理删除
const handleDelete = async (attachment, index) => {
  try {
    await ElMessageBox.confirm(
      `确认删除附件 "${attachment.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    emit('delete', { attachment, index })
    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消操作
  }
}

// 处理上传成功
const handleUploadSuccess = (response, file, fileList) => {
  emit('upload-success', { response, file, fileList })
  uploadDialogVisible.value = false
}

// 处理上传失败
const handleUploadError = (error, file, fileList) => {
  emit('upload-error', { error, file, fileList })
}
</script>

<style lang="scss" scoped>
.attachment-list-container {
  .attachment-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--gap-lg);
    
    .header-left {
      flex: 1;
      
      .attachment-title {
        display: flex;
        align-items: center;
        gap: var(--gap-sm);
        margin: 0 0 var(--gap-xs) 0;
        font-size: var(--font-size-base);
        font-weight: 600;
        color: var(--text);
        
        .title-icon {
          color: var(--color-primary);
        }
      }
      
      .attachment-description {
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
  
  .attachment-list {
    .attachment-item {
      display: flex;
      align-items: center;
      gap: var(--gap-md);
      padding: var(--gap-md);
      border: 1px solid var(--border);
      border-radius: var(--radius-md);
      margin-bottom: var(--gap-sm);
      background: var(--surface);
      transition: all 0.3s ease;
      
      &:hover {
        border-color: var(--color-primary);
        box-shadow: var(--shadow-sm);
      }
      
      &.is-uploading {
        border-color: var(--color-primary);
        background: var(--hover);
      }
      
      .file-icon {
        flex-shrink: 0;
        
        .icon {
          font-size: 32px;
          color: var(--color-primary);
        }
      }
      
      .file-info {
        flex: 1;
        min-width: 0;
        
        .file-name {
          font-size: var(--font-size-base);
          font-weight: 500;
          color: var(--text);
          margin-bottom: var(--gap-xs);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .file-meta {
          display: flex;
          align-items: center;
          gap: var(--gap-md);
          font-size: var(--font-size-sm);
          color: var(--text-3);
          
          .file-size {
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          }
        }
      }
      
      .upload-progress {
        flex: 1;
        margin: 0 var(--gap-md);
      }
      
      .file-status {
        flex-shrink: 0;
      }
      
      .file-actions {
        display: flex;
        gap: var(--gap-xs);
        flex-shrink: 0;
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: var(--gap-3xl) var(--gap-lg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .attachment-list-container {
    .attachment-header {
      flex-direction: column;
      gap: var(--gap-sm);
      align-items: stretch;
      
      .header-right {
        justify-content: flex-end;
      }
    }
    
    .attachment-list {
      .attachment-item {
        flex-direction: column;
        align-items: stretch;
        gap: var(--gap-sm);
        
        .file-info {
          .file-meta {
            flex-direction: column;
            align-items: flex-start;
            gap: var(--gap-xs);
          }
        }
        
        .upload-progress {
          margin: 0;
        }
        
        .file-actions {
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>
