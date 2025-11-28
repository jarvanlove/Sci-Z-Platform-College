<!--
/**
 * @description 通用文件预览组件
 * 支持 PDF、Word、图片等多种文件格式的浏览器内预览
 * 使用方式：
 *   <FilePreview
 *     v-model="showPreview"
 *     :file-info="{ name: '文件名', attachmentId: 123 }"
 *     @close="handleClose"
 *   />
 */
-->
<template>
  <el-dialog
    :model-value="modelValue"
    :title="fileInfo?.name || $t('common.preview')"
    width="90%"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    @update:model-value="handleClose"
    class="file-preview-dialog"
  >
    <div class="file-preview-container">
      <div class="file-preview-header">
        <div class="file-preview-title">{{ fileInfo?.name || '文件预览' }}</div>
        <div class="file-preview-actions">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleDownload"
          >
            {{ $t('common.download')}}
          </el-button>
        </div>
      </div>
      <div class="file-preview-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="preview-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>{{ $t('common.loading')}}</span>
        </div>
        
        <!-- 错误状态 -->
        <div v-else-if="error" class="preview-error">
          <el-icon><Warning /></el-icon>
          <span>{{ error }}</span>
          <el-button type="primary" @click="loadPreview" style="margin-top: 16px">
            {{ $t('common.retry')}}
          </el-button>
        </div>
        
        <!-- PDF 预览 -->
        <div v-else-if="previewType === 'pdf' && previewUrl" class="preview-iframe-container">
          <iframe
            :src="previewUrl"
            class="preview-iframe"
            frameborder="0"
            @load="handleIframeLoad"
            @error="handleIframeError"
          ></iframe>
        </div>
        
        <!-- 图片预览 -->
        <div v-else-if="previewType === 'image' && previewUrl" class="preview-image-container">
          <img
            :src="previewUrl"
            :alt="fileInfo?.name"
            class="preview-image"
            @load="handleImageLoad"
            @error="handleImageError"
          />
        </div>
        
        <!-- Word/Office 文档预览 - 使用 docx-preview 在浏览器中直接渲染 -->
        <div v-else-if="previewType === 'office' && previewUrl" class="preview-office-container">
          <div ref="docxPreviewContainer" class="docx-preview-wrapper"></div>
        </div>
        
        <!-- 文本文件预览 -->
        <div v-else-if="previewType === 'text' && previewUrl" class="preview-text-container">
          <pre class="preview-text" v-text="textContent"></pre>
        </div>
        
        <!-- 不支持的类型 -->
        <div v-else-if="previewType === 'unsupported'" class="preview-unsupported">
          <el-icon><Document /></el-icon>
          <p>{{ $t('filePreview.unsupportedType') }}</p>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Loading, Warning, Document } from '@element-plus/icons-vue'
import { previewFile } from '@/api/File'
import { createLogger } from '@/utils/simpleLogger'
import { renderAsync } from 'docx-preview'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  fileInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const { t } = useI18n()
const logger = createLogger('FilePreview')

// 响应式数据
const loading = ref(false)
const error = ref('')
const previewUrl = ref('')
const previewType = ref('')
const iframeLoaded = ref(false)
const textContent = ref('')
const docxPreviewContainer = ref(null) // docx-preview 渲染容器

// 根据文件名或 URL 判断文件类型
const detectFileType = (fileName, fileUrl = '') => {
  // 优先从文件名判断
  if (fileName) {
    const name = fileName.toLowerCase()
    
    // PDF
    if (name.endsWith('.pdf')) {
      return 'pdf'
    }
    
    // 图片
    const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg']
    if (imageExts.some(ext => name.endsWith(ext))) {
      return 'image'
    }
    
    // Word/Office 文档
    const officeExts = ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx']
    if (officeExts.some(ext => name.endsWith(ext))) {
      return 'office'
    }
    
    // 文本文件
    const textExts = ['.txt', '.md', '.json', '.xml', '.csv']
    if (textExts.some(ext => name.endsWith(ext))) {
      return 'text'
    }
  }
  
  // 如果文件名无法判断，尝试从 URL 中提取文件扩展名
  if (fileUrl) {
    const url = fileUrl.toLowerCase()
    
    // 从 URL 中提取文件扩展名（支持查询参数中的文件名）
    const urlMatch = url.match(/\.(pdf|jpg|jpeg|png|gif|bmp|webp|svg|doc|docx|xls|xlsx|ppt|pptx|txt|md|json|xml|csv)(\?|$|#)/i)
    if (urlMatch) {
      const ext = urlMatch[1].toLowerCase()
      
      if (ext === 'pdf') return 'pdf'
      if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)) return 'image'
      if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)) return 'office'
      if (['txt', 'md', 'json', 'xml', 'csv'].includes(ext)) return 'text'
    }
    
    // 检查 URL 中是否包含文件类型关键词
    if (url.includes('.pdf') || url.includes('application/pdf')) return 'pdf'
    if (url.includes('.doc') || url.includes('.docx') || url.includes('word') || url.includes('application/vnd.openxmlformats-officedocument.wordprocessingml')) return 'office'
    if (url.includes('.xls') || url.includes('.xlsx') || url.includes('excel') || url.includes('application/vnd.openxmlformats-officedocument.spreadsheetml')) return 'office'
    if (url.includes('.ppt') || url.includes('.pptx') || url.includes('powerpoint') || url.includes('application/vnd.openxmlformats-officedocument.presentationml')) return 'office'
    if (url.includes('image/')) return 'image'
  }
  
  return 'unsupported'
}

// 加载预览
const loadPreview = async () => {
  if (!props.fileInfo?.attachmentId) {
    error.value = t('filePreview.attachmentIdRequired') || '文件ID不存在'
    return
  }
  
  loading.value = true
  error.value = ''
  previewUrl.value = ''
  iframeLoaded.value = false
  textContent.value = ''
  
  // 清空 docx-preview 容器
  if (docxPreviewContainer.value) {
    docxPreviewContainer.value.innerHTML = ''
  }
  
  try {
    logger.info('Loading preview', { 
      attachmentId: props.fileInfo.attachmentId,
      fileName: props.fileInfo.name 
    })
    
    // 调用预览接口获取预签名 URL
    const response = await previewFile(props.fileInfo.attachmentId, {
      expireSeconds: 3600
    })
    
    if (!response?.data) {
      throw new Error(response?.message || t('filePreview.urlNotFound') || '获取预览链接失败')
    }
    
    previewUrl.value = response.data
    // 从文件名和 URL 中判断文件类型
    previewType.value = detectFileType(props.fileInfo.name, previewUrl.value)
    
    logger.info('Preview URL obtained', { 
      url: previewUrl.value,
      fileName: props.fileInfo.name,
      type: previewType.value 
    })
    
    // 如果是 Word/Office 文档，使用 docx-preview 在浏览器中直接渲染
    if (previewType.value === 'office') {
      try {
        // 先关闭 loading，让容器显示在 DOM 中
        loading.value = false
        
        // 等待 DOM 更新，确保容器已经渲染
        await nextTick()
        await nextTick()
        // 额外等待一帧，确保 Vue 完成条件渲染
        await new Promise(resolve => requestAnimationFrame(resolve))
        
        // 检查容器是否存在
        if (!docxPreviewContainer.value) {
          throw new Error('Preview container not found in DOM')
        }
        
        logger.info('Container found, downloading file for docx-preview', {
          containerExists: !!docxPreviewContainer.value,
          url: previewUrl.value
        })
        
        // 下载文件为 ArrayBuffer
        const fileResponse = await fetch(previewUrl.value)
        if (!fileResponse.ok) {
          throw new Error(`Failed to download file: ${fileResponse.statusText}`)
        }
        const arrayBuffer = await fileResponse.arrayBuffer()
        
        logger.info('File downloaded, rendering Word document with docx-preview')
        
        // 使用 docx-preview 渲染
        await renderAsync(arrayBuffer, docxPreviewContainer.value, undefined, {
          className: 'docx-preview',
          inWrapper: true,
          ignoreWidth: false,
          ignoreHeight: false,
          ignoreFonts: false,
          breakPages: true,
          ignoreLastRenderedPageBreak: true,
          experimental: false,
          trimXml: false,
          useBase64URL: false,
          useMathMLPolyfill: false,
          showChanges: false,
          showInsertions: false,
          showDeletions: false
        })
        
        logger.info('Word document rendered successfully')
      } catch (err) {
        logger.error('Failed to render Word document with docx-preview', err)
        error.value = t('filePreview.docxRenderFailed') || 'Word 文档渲染失败，请尝试下载后查看'
        loading.value = false
      }
      return
    }
    
    // 如果无法识别文件类型，但 URL 存在，默认尝试作为 Office 文档处理
    if (previewType.value === 'unsupported' && previewUrl.value) {
      logger.warn('File type cannot be detected, defaulting to office type', {
        fileName: props.fileInfo.name,
        url: previewUrl.value
      })
      previewType.value = 'office' // 默认尝试作为 Office 文档预览
      // 重新加载预览
      await loadPreview()
      return
    }
    
    // 如果是文本文件，需要获取内容
    if (previewType.value === 'text') {
      try {
        const textResponse = await fetch(previewUrl.value)
        textContent.value = await textResponse.text()
      } catch (e) {
        logger.warn('Failed to load text content', e)
        // 如果获取文本失败，尝试使用 docx-preview
        previewType.value = 'office'
        await loadPreview()
        return
      }
    }
    
  } catch (err) {
    logger.error('Preview load failed', err)
    error.value = err.message || t('filePreview.loadFailed') || '加载预览失败'
  } finally {
    loading.value = false
  }
}

// iframe 加载成功
const handleIframeLoad = () => {
  iframeLoaded.value = true
  logger.info('Iframe loaded successfully')
}

// iframe 加载失败
const handleIframeError = () => {
  logger.error('Iframe load failed')
  error.value = t('filePreview.iframeLoadFailed') || '预览加载失败'
}

// 图片加载成功
const handleImageLoad = () => {
  logger.info('Image loaded successfully')
}

// 图片加载失败
const handleImageError = () => {
  error.value = t('filePreview.imageLoadFailed') || '图片加载失败'
  logger.error('Image load failed')
}

// 下载文件
const handleDownload = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  } else {
    ElMessage.warning(t('filePreview.downloadUrlNotReady') || '下载链接未就绪')
  }
}

// 关闭预览
const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
  // 清理状态
  setTimeout(() => {
    previewUrl.value = ''
    previewType.value = ''
    error.value = ''
    iframeLoaded.value = false
    textContent.value = ''
    // 清空 docx-preview 容器
    if (docxPreviewContainer.value) {
      docxPreviewContainer.value.innerHTML = ''
    }
  }, 300)
}

// 组件卸载时清理
onBeforeUnmount(() => {
  if (docxPreviewContainer.value) {
    docxPreviewContainer.value.innerHTML = ''
  }
})

// 监听 modelValue 变化，打开时加载预览
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.fileInfo?.attachmentId) {
    loadPreview()
  }
})

// 监听 fileInfo 变化
watch(() => props.fileInfo, (newVal) => {
  if (props.modelValue && newVal?.attachmentId) {
    loadPreview()
  }
}, { deep: true })
</script>

<style lang="scss" scoped>
.file-preview-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.file-preview-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 500px;
  background: var(--bg);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.file-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  background: var(--surface);
  flex-shrink: 0;
}

.file-preview-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16px;
}

.file-preview-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.file-preview-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: var(--bg);
  overflow: hidden;
}

.preview-loading,
.preview-error,
.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-2);
  font-size: var(--font-size-base);
  padding: 40px;
}

.preview-loading .el-icon {
  font-size: 32px;
  color: var(--color-primary);
}

.preview-error .el-icon,
.preview-unsupported .el-icon {
  font-size: 32px;
  color: var(--color-warning);
}

.preview-iframe-container {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: var(--bg);
}


.preview-image-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: auto;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: var(--radius-sm);
}

.preview-text-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 24px;
}

.preview-text {
  margin: 0;
  padding: 0;
  font-family: 'Courier New', monospace;
  font-size: var(--font-size-sm);
  line-height: 1.6;
  color: var(--text);
  white-space: pre-wrap;
  word-wrap: break-word;
}

.preview-office-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 24px;
  background: var(--bg);
}

.docx-preview-wrapper {
  width: 100%;
  min-height: 100%;
  background: var(--surface);
  padding: 40px;
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-md);
  
  // docx-preview 生成的样式覆盖
  :deep(.docx-preview) {
    font-family: 'Microsoft YaHei', 'SimSun', 'Arial', sans-serif;
    color: var(--text);
    line-height: 1.6;
    
    // 确保文档内容可见
    * {
      color: inherit;
    }
    
    // 表格样式
    table {
      border-collapse: collapse;
      width: 100%;
      margin: 16px 0;
      
      td, th {
        border: 1px solid var(--border);
        padding: 8px;
        text-align: left;
      }
      
      th {
        background: var(--bg);
        font-weight: 600;
      }
    }
    
    // 段落样式
    p {
      margin: 8px 0;
    }
    
    // 标题样式
    h1, h2, h3, h4, h5, h6 {
      margin: 16px 0 8px 0;
      font-weight: 600;
      color: var(--text);
    }
  }
}
</style>

