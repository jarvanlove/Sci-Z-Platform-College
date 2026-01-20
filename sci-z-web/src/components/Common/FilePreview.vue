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
    :draggable="true"
    @update:model-value="handleClose"
    class="file-preview-dialog"
  >
    <div class="file-preview-container">
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
        <div
          v-else-if="previewType === 'image' && previewUrl"
          class="preview-image-container"
        >
          <img
            :src="previewUrl"
            :alt="fileInfo?.name"
            class="preview-image"
            @load="handleImageLoad"
            @error="handleImageError"
          />
        </div>
        
        <!-- Word 文档预览 - 使用 docx-preview 在浏览器中直接渲染 -->
        <div
          v-else-if="previewType === 'word' && previewUrl"
          class="preview-office-container"
        >
          <div ref="docxPreviewContainer" class="docx-preview-wrapper"></div>
        </div>

        <!-- Excel/Word(.doc)/PPT 预览 - 使用 Office Online Viewer -->
        <div v-else-if="(previewType === 'excel' || previewType === 'word-old' || previewType === 'ppt') && previewUrl" class="preview-iframe-container">
          <iframe
            :src="previewUrl"
            class="preview-iframe"
            frameborder="0"
            @load="handleIframeLoad"
            @error="handleIframeError"
          ></iframe>
          <!-- Office Online Viewer 错误提示覆盖层 -->
          <div v-if="pptViewerError" class="ppt-viewer-error-overlay">
            <el-icon><Warning /></el-icon>
            <p class="error-title">{{ pptViewerErrorTitle }}</p>
            <p class="error-message">{{ pptViewerErrorMessage }}</p>
            <div class="error-actions">
              <el-button type="primary" @click="handleDownloadFile">
                {{ $t('filePreview.downloadFile') || '下载文件' }}
              </el-button>
              <el-button @click="loadPreview">
                {{ $t('common.retry') || '重试' }}
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 文本文件预览 -->
        <div
          v-else-if="previewType === 'text' && previewUrl"
          class="preview-text-container"
        >
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
const originalFileUrl = ref('') // 保存原始文件 URL，用于下载
const previewType = ref('')
const iframeLoaded = ref(false)
const textContent = ref('')
const docxPreviewContainer = ref(null) // docx-preview 渲染容器
const pptViewerError = ref(false) // PPT 预览器错误状态
const pptViewerErrorTitle = ref('') // PPT 预览器错误标题
const pptViewerErrorMessage = ref('') // PPT 预览器错误消息
const isDownloading = ref(false) // 防止重复下载标记

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
    
    // Word 文档
    if (name.endsWith('.docx')) {
      return 'word' // 新版 Word，使用 docx-preview 渲染
    }
    if (name.endsWith('.doc')) {
      return 'word-old' // 旧版 Word，使用 Office Online Viewer
    }

    // Excel 文档（使用 Office Online Viewer 预览）
    const excelExts = ['.xls', '.xlsx']
    if (excelExts.some(ext => name.endsWith(ext))) {
      return 'excel'
    }

    // PPT 文档（使用 Office Online 预览）
    const pptExts = ['.ppt', '.pptx']
    if (pptExts.some(ext => name.endsWith(ext))) {
      return 'ppt'
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
      if (ext === 'docx') return 'word'
      if (ext === 'doc') return 'word-old'
      if (['xls', 'xlsx'].includes(ext)) return 'excel'
      if (['ppt', 'pptx'].includes(ext)) return 'ppt'
      if (['txt', 'md', 'json', 'xml', 'csv'].includes(ext)) return 'text'
    }
    
    // 检查 URL 中是否包含文件类型关键词
    if (url.includes('.pdf') || url.includes('application/pdf')) return 'pdf'
    if (url.includes('.docx') || url.includes('application/vnd.openxmlformats-officedocument.wordprocessingml.document')) return 'word'
    if (url.includes('.doc') || (url.includes('word') && !url.includes('.docx'))) return 'word-old'
    if (url.includes('.xls') || url.includes('.xlsx') || url.includes('excel') || url.includes('application/vnd.openxmlformats-officedocument.spreadsheetml')) return 'excel'
    if (url.includes('.ppt') || url.includes('.pptx') || url.includes('powerpoint') || url.includes('application/vnd.openxmlformats-officedocument.presentationml')) return 'ppt'
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
  originalFileUrl.value = '' // 清空原始文件 URL
  iframeLoaded.value = false
  textContent.value = ''
  isDownloading.value = false // 重置下载标记
  
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
    // 保存原始文件 URL，用于下载
    originalFileUrl.value = response.data
    // 从文件名和 URL 中判断文件类型
    previewType.value = detectFileType(props.fileInfo.name, previewUrl.value)
    
    logger.info('Preview URL obtained', { 
      url: previewUrl.value,
      fileName: props.fileInfo.name,
      type: previewType.value 
    })
    
    // 如果是 Word 文档（.docx），使用 docx-preview 在浏览器中直接渲染
    if (previewType.value === 'word') {
      try {
        // 先关闭 loading，让容器显示在 DOM 中
        loading.value = false
        
        // 等待 DOM 更新，确保容器已经渲染
        await nextTick()
        await nextTick()
        // 额外等待一帧，确保 Vue 完成条件渲染
        await new Promise(resolve => requestAnimationFrame(resolve))
        
        // 再次检查容器是否存在
        if (!docxPreviewContainer.value) {
          // 如果容器还不存在，再等待一段时间
          await new Promise(resolve => setTimeout(resolve, 100))
          if (!docxPreviewContainer.value) {
            throw new Error('Preview container not found in DOM')
          }
        }
        
        logger.info('Container found, downloading file for docx-preview', {
          containerExists: !!docxPreviewContainer.value,
          containerElement: docxPreviewContainer.value?.tagName,
          url: previewUrl.value
        })
        
        // 下载文件为 ArrayBuffer
        const fileResponse = await fetch(previewUrl.value)
        if (!fileResponse.ok) {
          throw new Error(`Failed to download file: ${fileResponse.statusText}`)
        }
        const arrayBuffer = await fileResponse.arrayBuffer()
        
        logger.info('File downloaded, rendering Word document with docx-preview', {
          fileSize: arrayBuffer.byteLength
        })
        
        // 确保容器仍然存在（防止异步操作期间被移除）
        if (!docxPreviewContainer.value) {
          throw new Error('Preview container was removed from DOM')
        }
        
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

    // Excel/旧版 Word(.doc)/PPT/PPTX 使用 Office Online Viewer 进行预览
    if (previewType.value === 'excel' || previewType.value === 'word-old' || previewType.value === 'ppt') {
      try {
        const originalUrl = previewUrl.value
        const fileTypeName = previewType.value === 'excel' ? 'Excel' : previewType.value === 'word-old' ? 'Word' : 'PPT'
        
        // 重置错误状态
        pptViewerError.value = false
        pptViewerErrorTitle.value = ''
        pptViewerErrorMessage.value = ''
        
        // 检查 URL 是否为 localhost 或内网地址
        // Office Online Viewer 无法访问本地服务器，需要公网可访问的 URL
        const isLocalhost = /^(https?:\/\/)?(localhost|127\.0\.0\.1|0\.0\.0\.0|::1)/i.test(originalUrl)
        const isPrivateIP = /^(https?:\/\/)?(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/i.test(originalUrl)
        
        if (isLocalhost || isPrivateIP) {
          // 本地或内网地址，无法使用在线预览服务
          logger.warn(`${fileTypeName} file URL is localhost or private IP, cannot use Office Online Viewer`, {
            originalUrl,
            isLocalhost,
            isPrivateIP,
            fileType: previewType.value
          })
          pptViewerError.value = true
          pptViewerErrorTitle.value = t('filePreview.officeLocalhostTitle') || `${fileTypeName} 预览服务不可用`
          pptViewerErrorMessage.value = t('filePreview.officeLocalhostNotSupported') || `由于文件地址为本地或内网地址，无法使用在线预览服务。请下载文件后使用本地 Office 软件打开查看。`
          loading.value = false
          return
        }
        
        // 将文件 URL 编码后，使用 Office Online Viewer 服务进行预览
        // Office Online Viewer 可以预览 Excel、Word、PPT 文件
        const encodedUrl = encodeURIComponent(originalUrl)
        previewUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`
        logger.info(`${fileTypeName} preview URL generated with Office Online Viewer`, {
          originalUrl,
          viewerUrl: previewUrl.value,
          fileType: previewType.value
        })
        loading.value = false
      } catch (err) {
        logger.error(`Failed to generate ${previewType.value} preview URL`, err)
        pptViewerError.value = true
        const fileTypeName = previewType.value === 'excel' ? 'Excel' : previewType.value === 'word-old' ? 'Word' : 'PPT'
        pptViewerErrorTitle.value = t('filePreview.officeViewerErrorTitle') || `${fileTypeName} 预览服务不可用`
        pptViewerErrorMessage.value = t('filePreview.officePreviewFailed') || `${fileTypeName} 预览失败，请尝试下载后查看`
        loading.value = false
      }
      return
    }
    
    // 如果无法识别文件类型，但 URL 存在，尝试使用 Office Online Viewer
    if (previewType.value === 'unsupported' && previewUrl.value) {
      logger.warn('File type cannot be detected, trying Office Online Viewer', {
        fileName: props.fileInfo.name,
        url: previewUrl.value
      })
      // 尝试使用 Office Online Viewer 预览（适用于未知的 Office 文档类型）
      const originalUrl = previewUrl.value
      const isLocalhost = /^(https?:\/\/)?(localhost|127\.0\.0\.1|0\.0\.0\.0|::1)/i.test(originalUrl)
      const isPrivateIP = /^(https?:\/\/)?(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/i.test(originalUrl)
      
      if (!isLocalhost && !isPrivateIP) {
        try {
          const encodedUrl = encodeURIComponent(originalUrl)
          previewUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`
          previewType.value = 'ppt' // 使用相同的预览逻辑
          loading.value = false
          return
        } catch (err) {
          logger.error('Failed to use Office Online Viewer for unknown file type', err)
        }
      }
      
      // 如果无法使用在线预览，显示不支持提示
      error.value = t('filePreview.unsupportedType') || '不支持预览此文件类型'
      loading.value = false
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
  
  // 对于 Office 文档预览（Excel/Word/PPT），延迟检查 iframe 内容是否真正加载成功
  // Office Online Viewer 可能会显示错误页面（如限额提示）
  if (previewType.value === 'excel' || previewType.value === 'word-old' || previewType.value === 'ppt') {
    // 延迟检查，等待 iframe 内容完全加载
    setTimeout(() => {
      // 由于跨域限制，无法直接访问 iframe 内容
      // 但可以通过检查 iframe 的 URL 变化或等待一段时间后判断
      // 如果 Office Online Viewer 显示错误，通常会在 URL 中包含错误信息
      const iframe = document.querySelector('.preview-iframe')
      if (iframe) {
        const currentSrc = iframe.src
        // 检查 URL 中是否包含错误相关的参数
        if (currentSrc.includes('error') || currentSrc.includes('failed')) {
          pptViewerError.value = true
          pptViewerErrorTitle.value = t('filePreview.pptViewerErrorTitle') || 'PPT 预览服务不可用'
          pptViewerErrorMessage.value = t('filePreview.pptViewerErrorMessage') || 'Office Online Viewer 服务可能已达到使用限额。如需升级 Pro+ 版本以获得更好的预览体验，请联系系统管理员。'
        }
      }
    }, 3000) // 等待 3 秒，给 Office Online Viewer 足够时间加载或显示错误
  }
}

// iframe 加载失败
const handleIframeError = () => {
  logger.error('Iframe load failed')
  
  // 如果是 Office 文档预览失败，提供更详细的错误信息
  if (previewType.value === 'excel' || previewType.value === 'word-old' || previewType.value === 'ppt') {
    pptViewerError.value = true
    const fileTypeName = previewType.value === 'excel' ? 'Excel' : previewType.value === 'word-old' ? 'Word' : 'PPT'
    pptViewerErrorTitle.value = t('filePreview.officeViewerErrorTitle') || `${fileTypeName} 预览服务不可用`
    pptViewerErrorMessage.value = t('filePreview.officeViewerErrorMessage') || 'Office Online Viewer 服务可能已达到使用限额。如需升级 Pro+ 版本以获得更好的预览体验，请联系系统管理员。'
  } else {
    error.value = t('filePreview.iframeLoadFailed') || '预览加载失败'
  }
}

// 下载文件（防止重复下载，无闪烁）
const handleDownloadFile = async () => {
  // 防止重复点击下载按钮
  if (isDownloading.value) {
    logger.warn('Download already in progress, ignoring duplicate request')
    return
  }
  
  // 优先使用保存的原始文件 URL
  const downloadUrl = originalFileUrl.value || previewUrl.value
  
  if (!downloadUrl) {
    ElMessage.warning(t('filePreview.downloadUrlNotReady') || '下载链接未就绪')
    return
  }
  
  // 如果是 Office Online Viewer 的 URL，需要获取原始文件 URL
  let finalUrl = downloadUrl
  if (downloadUrl.includes('view.officeapps.live.com')) {
    const match = downloadUrl.match(/src=([^&]+)/)
    if (match && match[1]) {
      finalUrl = decodeURIComponent(match[1])
    }
  }
  
  // 设置下载标记
  isDownloading.value = true
  
  try {
    const fileName = props.fileInfo?.name || 'download'
    
    // 方法1：优先尝试直接使用 <a> 标签下载（不打开新窗口，无闪烁）
    // 注意：如果 URL 跨域且服务器没有设置正确的 CORS 头，download 属性可能不生效
    try {
      const link = document.createElement('a')
      link.href = finalUrl
      link.download = fileName
      // 不设置 target="_blank"，避免打开新窗口
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      logger.info('File download initiated via anchor tag', {
        url: finalUrl,
        fileName
      })
      
      // 延迟重置下载标记
      setTimeout(() => {
        isDownloading.value = false
      }, 1000)
      return
    } catch (anchorError) {
      logger.warn('Anchor tag download failed, trying fetch method', anchorError)
    }
    
    // 方法2：如果直接下载失败（可能是跨域问题），使用 fetch 下载后创建 blob URL
    try {
      const response = await fetch(finalUrl)
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      
      const blob = await response.blob()
      const blobUrl = URL.createObjectURL(blob)
      
      const link = document.createElement('a')
      link.href = blobUrl
      link.download = fileName
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // 释放 blob URL 内存
      setTimeout(() => {
        URL.revokeObjectURL(blobUrl)
      }, 100)
      
      logger.info('File download initiated via fetch + blob', {
        url: finalUrl,
        fileName,
        blobSize: blob.size
      })
    } catch (fetchError) {
      logger.error('Fetch download failed, falling back to window.open', fetchError)
      // 方法3：最后的回退方案，使用 window.open（可能会闪烁，但至少能下载）
      // 这种情况应该很少发生
      window.open(finalUrl, '_blank')
    }
  } catch (err) {
    logger.error('Failed to download file', err)
    ElMessage.error(t('filePreview.downloadFailed') || '下载失败，请稍后重试')
  } finally {
    // 延迟重置下载标记，防止快速重复点击
    setTimeout(() => {
      isDownloading.value = false
    }, 1000)
  }
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


// 关闭预览
const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
  // 清理状态
  setTimeout(() => {
    previewUrl.value = ''
    originalFileUrl.value = ''
    previewType.value = ''
    error.value = ''
    iframeLoaded.value = false
    textContent.value = ''
    pptViewerError.value = false
    pptViewerErrorTitle.value = ''
    pptViewerErrorMessage.value = ''
    isDownloading.value = false
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

// PPT 预览器错误覆盖层
.ppt-viewer-error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  z-index: 10;
  padding: 40px;
  gap: 16px;
  
  .el-icon {
    font-size: 48px;
    color: var(--color-warning);
    margin-bottom: 8px;
  }
  
  .error-title {
    font-size: var(--font-size-lg);
    font-weight: 600;
    color: var(--text);
    margin: 0;
    text-align: center;
  }
  
  .error-message {
    font-size: var(--font-size-base);
    color: var(--text-2);
    margin: 0;
    text-align: center;
    line-height: 1.6;
    max-width: 600px;
  }
  
  .error-actions {
    display: flex;
    gap: 12px;
    margin-top: 8px;
  }
}


.preview-image-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: auto; // 🔥 修复：使用原生滚动，不使用 BaseScrollbar
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
  padding: 24px;
  overflow: auto; // 🔥 修复：使用原生滚动，不使用 BaseScrollbar
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
  padding: 24px;
  background: var(--bg);
  overflow: auto; // 🔥 修复：使用原生滚动，不使用 BaseScrollbar
}

.docx-preview-wrapper {
  width: 100%;
  max-width: 1200px;
  min-height: 100%;
  margin: 0 auto;
  background: var(--surface);
  padding: 40px 60px;
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

