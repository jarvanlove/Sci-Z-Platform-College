<template>
  <div class="file-upload">
    <div class="upload-area" 
         :class="{ 'drag-over': isDragOver, 'uploading': uploading }"
         @dragover.prevent="handleDragOver"
         @dragleave.prevent="handleDragLeave"
         @drop.prevent="handleDrop"
         @click="triggerFileInput">
      
      <input 
        ref="fileInput"
        type="file" 
        @change="handleFileSelect"
        :accept="acceptedTypes"
        style="display: none"
      />
      
      <div v-if="!uploading" class="upload-content">
        <div class="upload-icon">📁</div>
        <h3>拖拽文件到此处或点击选择文件</h3>
        <p>支持的文件类型: {{ acceptedTypesText }}</p>
        <p>最大文件大小: {{ maxSizeText }}</p>
      </div>
      
      <div v-else class="uploading-content">
        <div class="spinner"></div>
        <h3>正在上传...</h3>
        <p>{{ uploadProgress }}%</p>
      </div>
    </div>
    
    <!-- 文件预览 -->
    <div v-if="selectedFile" class="file-preview">
      <div class="file-info">
        <div class="file-icon">📄</div>
        <div class="file-details">
          <h4>{{ selectedFile.name }}</h4>
          <p>{{ formatFileSize(selectedFile.size) }}</p>
        </div>
        <button @click="removeFile" class="remove-btn">×</button>
      </div>
    </div>
    
    <!-- 错误信息 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  acceptedTypes?: string
  maxSize?: number // 字节
  uploading?: boolean
  uploadProgress?: number
}

const props = withDefaults(defineProps<Props>(), {
  acceptedTypes: '.txt,.pdf,.doc,.docx,.md',
  maxSize: 10 * 1024 * 1024, // 10MB
  uploading: false,
  uploadProgress: 0
})

const emit = defineEmits<{
  fileSelected: [file: File]
  fileRemoved: []
}>()

const fileInput = ref<HTMLInputElement>()
const selectedFile = ref<File | null>(null)
const isDragOver = ref(false)
const error = ref('')

// 计算属性
const acceptedTypesText = computed(() => {
  return props.acceptedTypes.split(',').map(type => type.trim()).join(', ')
})

const maxSizeText = computed(() => {
  const size = props.maxSize
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)}MB`
  } else if (size >= 1024) {
    return `${(size / 1024).toFixed(1)}KB`
  }
  return `${size}B`
})

// 方法
const triggerFileInput = () => {
  if (!props.uploading) {
    fileInput.value?.click()
  }
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    validateAndSelectFile(file)
  }
}

const handleDragOver = (event: DragEvent) => {
  if (!props.uploading) {
    event.preventDefault()
    isDragOver.value = true
  }
}

const handleDragLeave = (event: DragEvent) => {
  if (!props.uploading) {
    event.preventDefault()
    isDragOver.value = false
  }
}

const handleDrop = (event: DragEvent) => {
  if (props.uploading) return
  
  event.preventDefault()
  isDragOver.value = false
  
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file) {
      validateAndSelectFile(file)
    }
  }
}

const validateAndSelectFile = (file: File) => {
  error.value = ''
  
  // 检查文件大小
  if (file.size > props.maxSize) {
    error.value = `文件大小超过限制 (${maxSizeText.value})`
    return
  }
  
  // 检查文件类型
  const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()
  const acceptedTypes = props.acceptedTypes.split(',').map(type => type.trim().toLowerCase())
  
  if (!acceptedTypes.includes(fileExtension)) {
    error.value = `不支持的文件类型: ${fileExtension}`
    return
  }
  
  selectedFile.value = file
  emit('fileSelected', file)
}

const removeFile = () => {
  selectedFile.value = null
  error.value = ''
  emit('fileRemoved')
  
  // 清空 input 值
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-area:hover:not(.uploading) {
  border-color: #667eea;
  background: #f8f9ff;
}

.upload-area.drag-over {
  border-color: #667eea;
  background: #f0f4ff;
  transform: scale(1.02);
}

.upload-area.uploading {
  cursor: not-allowed;
  opacity: 0.7;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 8px;
}

.upload-content h3 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.2rem;
}

.upload-content p {
  color: #7f8c8d;
  margin: 4px 0;
  font-size: 0.9rem;
}

.uploading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.uploading-content h3 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.2rem;
}

.uploading-content p {
  color: #667eea;
  margin: 0;
  font-weight: 600;
}

.file-preview {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 2rem;
}

.file-details {
  flex: 1;
}

.file-details h4 {
  margin: 0 0 4px 0;
  color: #2c3e50;
  font-size: 1rem;
}

.file-details p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.remove-btn {
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.remove-btn:hover {
  background: #c0392b;
}

.error-message {
  margin-top: 12px;
  padding: 12px;
  background: #fdf2f2;
  color: #e53e3e;
  border: 1px solid #fecaca;
  border-radius: 6px;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .upload-area {
    padding: 30px 16px;
  }
  
  .upload-icon {
    font-size: 2.5rem;
  }
  
  .upload-content h3 {
    font-size: 1.1rem;
  }
}
</style>
