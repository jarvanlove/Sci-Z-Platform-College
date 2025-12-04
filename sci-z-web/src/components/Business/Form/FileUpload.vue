<!--
/**
 * @description 文件上传组件
 * 用于文件上传功能，支持多种文件类型、大小限制、预览等功能
 * 包含拖拽上传、进度显示、文件列表管理等功能
 */
-->
<template>
  <div class="file-upload-container">
    <!-- 上传区域 -->
    <el-upload
      ref="uploadRef"
      :action="uploadAction"
      :headers="uploadHeaders"
      :data="uploadData"
      :multiple="multiple"
      :accept="accept"
      :limit="limit"
      :file-list="fileList"
      :auto-upload="actualAutoUpload"
      :show-file-list="showFileList"
      :drag="drag"
      :disabled="disabled"
      :http-request="props.customUpload ? customUpload : undefined"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-progress="handleUploadProgress"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :on-preview="handlePreview"
      :on-change="handleChange"
      class="file-upload"
      :class="{ 'is-drag': drag, 'is-disabled': disabled }"
    >
      <template v-if="drag">
        <div class="upload-dragger">
          <el-icon class="upload-icon">
            <UploadFilled />
          </el-icon>
          <div class="upload-text">
            <p class="upload-title">将文件拖到此处，或<em>点击上传</em></p>
            <p class="upload-tip">{{ uploadTip }}</p>
          </div>
        </div>
      </template>
      <template v-else>
        <BaseButton type="primary" :disabled="disabled">
          <el-icon><Plus /></el-icon>
          {{ buttonText }}
        </BaseButton>
      </template>
    </el-upload>
    
    <!-- 文件列表（立即上传模式 & 批量模式公用展示） -->
    <div v-if="showFileList && fileList.length > 0" class="file-list">
      <div
        v-for="(file, index) in fileList"
        :key="file.uid || index"
        class="file-item"
        :class="{ 'is-uploading': file.status === 'uploading' }"
      >
        <div class="file-info">
          <el-icon class="file-icon">
            <component :is="getFileIcon(file)" />
          </el-icon>
          <div class="file-details">
            <div class="file-name" :title="file.name">{{ file.name }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <span v-if="file.status" class="file-status" :class="`status-${file.status}`">
                {{ getStatusText(file.status) }}
              </span>
            </div>
          </div>
        </div>
        
        <!-- 上传进度（仅立即上传模式使用） -->
        <div v-if="file.status === 'uploading'" class="upload-progress">
          <el-progress
            :percentage="file.percentage || 0"
            :stroke-width="4"
            :show-text="false"
          />
        </div>
        
        <!-- 操作按钮 -->
        <div class="file-actions">
          <el-button
            v-if="file.status === 'success' && showPreview"
            type="text"
            size="small"
            @click="handlePreview(file)"
          >
            <el-icon><View /></el-icon>
          </el-button>
          <el-button
            v-if="!disabled"
            type="danger"
            text
            size="small"
            @click="handleRemove(file)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 批量模式：待上传说明与按钮 -->
    <div
      v-if="isBatchMode && batchRawFiles.length > 0 && showBatchUploadButton"
      class="batch-upload-panel"
    >
      <div class="batch-header">
        <span>待上传文件（{{ batchRawFiles.length }}/{{ maxBatchCount }}）</span>
      </div>
      <div class="batch-hint">
        单次最多上传 {{ maxBatchCount }} 个文件，点击下方按钮开始上传。
      </div>
      <div class="batch-actions">
        <button
          class="action-btn btn-primary"
          :disabled="batchRawFiles.length === 0"
          @click="handleBatchUploadClick"
        >
          开始上传
        </button>
      </div>
    </div>
    
    <!-- 上传提示 -->
    <div v-if="showTips" class="upload-tips">
      <el-alert
        :title="tipsTitle"
        :description="tipsDescription"
        type="info"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { BaseButton } from '@/components/Common'
import { SUPPORTED_FILE_EXTENSIONS, validateFileType, validateFileSize } from '@/constants/attachment'
import {
  UploadFilled,
  Plus,
  View,
  Delete,
  Document,
  Picture,
  VideoPlay,
  Folder
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 上传地址
  action: {
    type: String,
    default: '/api/upload'
  },
  // 请求头
  headers: {
    type: Object,
    default: () => ({})
  },
  // 上传数据
  data: {
    type: Object,
    default: () => ({})
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: ''
  },
  // 文件数量限制
  limit: {
    type: Number,
    default: 10
  },
  // 文件列表
  modelValue: {
    type: Array,
    default: () => []
  },
  // 是否自动上传
  autoUpload: {
    type: Boolean,
    default: true
  },
  // 是否显示文件列表
  showFileList: {
    type: Boolean,
    default: true
  },
  // 是否拖拽上传
  drag: {
    type: Boolean,
    default: false
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 按钮文本
  buttonText: {
    type: String,
    default: '选择文件'
  },
  // 上传提示
  uploadTip: {
    type: String,
    default: '支持单个或批量上传'
  },
  // 是否显示预览
  showPreview: {
    type: Boolean,
    default: true
  },
  // 是否显示提示
  showTips: {
    type: Boolean,
    default: true
  },
  // 提示标题
  tipsTitle: {
    type: String,
    default: '上传说明'
  },
  // 提示描述
  tipsDescription: {
    type: String,
    default: '支持常见文件格式，单个文件不超过10MB'
  },
  // 文件大小限制（MB）
  maxSize: {
    type: Number,
    default: 10
  },
  // 文件类型限制
  allowedTypes: {
    type: Array,
    default: () => []
  },
  // 上传模式：immediate=选中立刻上传（默认）、batch=仅收集文件，外部统一触发上传
  mode: {
    type: String,
    default: 'immediate' // 'immediate' | 'batch'
  },
  // 批量模式下，单次最大文件数
  maxBatchCount: {
    type: Number,
    default: 10
  },
  // 批量模式下，是否显示「开始上传」按钮
  showBatchUploadButton: {
    type: Boolean,
    default: true
  },
  // 自定义上传函数
  customUpload: {
    type: Function,
    default: null
  }
})

// Emits
const emit = defineEmits([
  'update:modelValue',
  'success',
  'error',
  'remove',
  'preview',
  // 批量模式相关事件
  'batch-change',
  'batch-upload'
])

// Refs
const uploadRef = ref()

// 计算属性
const fileList = computed({
  get: () => Array.isArray(props.modelValue) ? props.modelValue : [],
  set: (value) => emit('update:modelValue', value)
})

const uploadAction = computed(() => props.action)
const uploadHeaders = computed(() => props.headers)
const uploadData = computed(() => props.data)
const isBatchMode = computed(() => props.mode === 'batch')
// 批量模式下强制关闭自动上传
const actualAutoUpload = computed(() => (isBatchMode.value ? false : props.autoUpload))

const resolvedAllowedTypes = computed(() => {
  return props.allowedTypes.length > 0
    ? props.allowedTypes.map((item) => String(item).toLowerCase())
    : SUPPORTED_FILE_EXTENSIONS
})

// 批量模式：原始 File 列表
const batchRawFiles = computed(() => {
  if (!isBatchMode.value) return []
  // el-upload 的 file 对象上通常有 raw 字段指向原始 File
  return fileList.value
    .map((f) => f.raw || f)
    .filter((f) => !!f)
})

// 监听批量文件变化，通知外部
watch(
  () => batchRawFiles.value,
  (val) => {
    if (isBatchMode.value) {
      emit('batch-change', val)
    }
  }
)

// 获取文件图标
const getFileIcon = (file) => {
  const name = file.name || ''
  const extension = name.split('.').pop()?.toLowerCase()
  
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)) {
    return Picture
  } else if (['mp4', 'avi', 'mov', 'wmv', 'flv'].includes(extension)) {
    return VideoPlay
  } else if (['zip', 'rar', '7z', 'tar', 'gz'].includes(extension)) {
    return Folder
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'ready': '准备上传',
    'uploading': '上传中',
    'success': '上传成功',
    'fail': '上传失败'
  }
  return statusMap[status] || status
}

// 自定义上传函数（如果提供）
const customUpload = (options) => {
  if (props.customUpload && typeof props.customUpload === 'function') {
    return props.customUpload(options)
  }
  // 如果没有提供自定义上传函数，el-upload 会使用默认的 action、headers、data 等配置
  // 这里返回 undefined，让 el-upload 使用默认上传
  return undefined
}

// 上传前检查
const handleBeforeUpload = (file) => {
  // 批量模式下，先检查数量限制
  const currentCount = Array.isArray(fileList.value) ? fileList.value.length : 0
  if (isBatchMode.value && currentCount >= props.maxBatchCount) {
    ElMessage.warning(`单次最多只能选择 ${props.maxBatchCount} 个文件`)
    return false
  }

  // 检查文件大小
  const sizeValidation = validateFileSize(file, props.maxSize)
  if (!sizeValidation.passed) {
    ElMessage.error(sizeValidation.reason || `文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  
  // 检查文件类型
  const { passed, reason } = validateFileType(file, resolvedAllowedTypes.value)
  if (!passed) {
    ElMessage.error(reason || '文件类型不被支持')
    return false
  }
  
  return true
}

// 批量模式：点击「开始上传」按钮时，通知外部
const handleBatchUploadClick = () => {
  if (!isBatchMode.value) return
  if (batchRawFiles.value.length === 0) {
    ElMessage.warning('暂无可上传的文件')
    return
  }
  emit('batch-upload', batchRawFiles.value)
}

// 上传成功
const handleUploadSuccess = (response, file, fileList) => {
  ElMessage.success('文件上传成功')
  emit('success', { response, file, fileList })
}

// 上传失败
const handleUploadError = (error, file, fileList) => {
  ElMessage.error('文件上传失败')
  emit('error', { error, file, fileList })
}

// 上传进度
const handleUploadProgress = (event, file, fileList) => {
  // 进度更新
}

// 文件列表变化（批量模式下需要同步到 v-model）
const handleChange = (file, currentFileList) => {
  // 批量模式下，同步 fileList 到 v-model
  if (isBatchMode.value) {
    emit('update:modelValue', currentFileList)
  }
}

// 移除文件
const handleRemove = (file, currentFileList) => {
  // 批量模式下，同步更新 fileList
  if (isBatchMode.value) {
    emit('update:modelValue', currentFileList)
  }
  emit('remove', { file, fileList: currentFileList })
}

// 超出限制
const handleExceed = (files, fileList) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
}

// 预览文件
const handlePreview = (file) => {
  emit('preview', file)
}

// 暴露方法
defineExpose({
  uploadRef,
  submit: () => uploadRef.value?.submit(),
  clearFiles: () => uploadRef.value?.clearFiles(),
  abort: () => uploadRef.value?.abort()
})
</script>

<style lang="scss" scoped>
.file-upload-container {
  .file-upload {
    &.is-drag {
      .upload-dragger {
        width: 100%;
        height: 180px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border: 2px dashed var(--border);
        border-radius: var(--radius-lg);
        background: var(--bg);
        transition: all 0.3s ease;
        
        &:hover {
          border-color: var(--color-primary);
          background: var(--hover);
        }
        
        .upload-icon {
          font-size: 48px;
          color: var(--text-3);
          margin-bottom: var(--gap-md);
        }
        
        .upload-text {
          text-align: center;
          
          .upload-title {
            margin: 0 0 var(--gap-xs) 0;
            font-size: var(--font-size-base);
            color: var(--text);
            
            em {
              color: var(--color-primary);
              font-style: normal;
            }
          }
          
          .upload-tip {
            margin: 0;
            font-size: var(--font-size-sm);
            color: var(--text-3);
          }
        }
      }
    }
    
    &.is-disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
  
  .file-list {
    margin-top: var(--gap-lg);
    
    .file-item {
      display: flex;
      align-items: center;
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
      }
      
      .file-info {
        display: flex;
        align-items: center;
        flex: 1;
        gap: var(--gap-sm);
        
        .file-icon {
          font-size: 24px;
          color: var(--color-primary);
        }
        
        .file-details {
          flex: 1;
          
          .file-name {
            font-size: var(--font-size-base);
            color: var(--text);
            font-weight: 500;
            margin-bottom: 2px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .file-meta {
            display: flex;
            align-items: center;
            gap: var(--gap-sm);
            font-size: var(--font-size-sm);
            color: var(--text-3);
            
            .file-status {
              &.status-success {
                color: var(--color-success);
              }
              
              &.status-fail {
                color: var(--color-error);
              }
              
              &.status-uploading {
                color: var(--color-primary);
              }
            }
          }
        }
      }
      
      .upload-progress {
        flex: 1;
        margin: 0 var(--gap-md);
      }
      
      .file-actions {
        display: flex;
        gap: var(--gap-xs);
      }
    }
  }
  
  .upload-tips {
    margin-top: var(--gap-lg);
  }
  
  // 批量上传面板样式
  .batch-upload-panel {
    margin-top: var(--gap-md);
    padding: var(--gap-md);
    border: 1px solid var(--border);
    border-radius: var(--radius-md);
    background: var(--bg-tertiary);
    
    .batch-header {
      font-size: var(--font-size-base);
      font-weight: 600;
      color: var(--color-primary); // 与主题主色保持一致
      margin-bottom: var(--gap-xs);
    }
    
    .batch-hint {
      font-size: var(--font-size-sm);
      color: var(--text-secondary);
      margin-bottom: var(--gap-sm);
    }
    
    .batch-actions {
      display: flex;
      justify-content: flex-end;

      // 统一“开始上传”等动作按钮的细边框主题样式（与申报列表、用户管理一致）
      .action-btn {
        padding: 5px 12px;
        border: 1px solid var(--color-primary);
        border-radius: 4px;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s;
        background: none;
        white-space: nowrap;
        color: var(--color-primary);

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;

          &:hover {
            background: none;
            color: inherit;
          }
        }

        &.btn-primary {
          color: var(--color-primary);
          border-color: var(--color-primary);

          &:hover:not(:disabled) {
            background: var(--color-primary);
            color: var(--surface);
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .file-upload-container {
    .file-list {
      .file-item {
        flex-direction: column;
        align-items: stretch;
        gap: var(--gap-sm);
        
        .file-info {
          .file-details {
            .file-meta {
              flex-direction: column;
              align-items: flex-start;
              gap: 2px;
            }
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
