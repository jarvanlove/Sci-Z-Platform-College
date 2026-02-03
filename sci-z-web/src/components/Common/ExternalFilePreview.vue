<!--
/**
 * @description 外部文件预览组件
 * 支持PDF等外部文件的预览
 */
-->
<template>
  <div class="external-file-preview">
    <div v-if="loading" class="loading-mask">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-if="error" class="error-message">
      <el-alert
        :title="error"
        type="error"
        :closable="false"
        show-icon
      />
      <div class="error-actions">
        <el-button type="primary" @click="handleGoToOfficial">前往官网下载</el-button>
        <el-button @click="handleDownload">下载</el-button>
      </div>
    </div>
    <iframe
      v-if="!error && !loading"
      ref="previewIframe"
      :src="previewUrl"
      class="preview-iframe"
      frameborder="0"
      @load="handleIframeLoad"
      @error="handleIframeError"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { createLogger } from '@/utils/simpleLogger'

const logger = createLogger('ExternalFilePreview')

const props = defineProps({
  fileUrl: {
    type: String,
    required: true
  },
  fileId: {
    type: String,
    default: ''
  },
  landingPageUrl: {
    type: String,
    default: ''
  },
  fileType: {
    type: String,
    default: 'pdf'
  }
})

const loading = ref(true)
const error = ref('')
const previewIframe = ref(null)
const useProxy = ref(true)
const loadTimeout = ref(null)

// 构建预览URL
const previewUrl = computed(() => {
  if (!props.fileUrl) return ''
  if (useProxy.value) {
    if (props.fileId) {
      return `/api/proxy/pdf?id=${encodeURIComponent(props.fileId)}&url=${encodeURIComponent(props.fileUrl)}`
    }
    // 如果没有fileId，使用URL的hash作为临时ID
    const tempId = btoa(props.fileUrl).replace(/[+/=]/g, '').substring(0, 16)
    return `/api/proxy/pdf?id=${encodeURIComponent(tempId)}&url=${encodeURIComponent(props.fileUrl)}`
  }
  return props.fileUrl
})

// 验证代理响应
const verifyProxyResponse = async () => {
  if (!props.fileUrl) return false
  
  try {
    // 构建ID参数
    const fileId = props.fileId || btoa(props.fileUrl).replace(/[+/=]/g, '').substring(0, 16)
    const proxyUrl = `/api/proxy/pdf?id=${encodeURIComponent(fileId)}&url=${encodeURIComponent(props.fileUrl)}`
    const response = await fetch(proxyUrl)
    
    if (!response.ok) {
      logger.warn('代理接口失败，尝试直接使用原始链接', { status: response.status })
      return false
    }
    
    const blob = await response.blob()
    
    // 检查文件大小（至少10KB）
    if (blob.size < 10 * 1024) {
      logger.warn('代理返回的文件过小，尝试直接使用原始链接', { size: blob.size })
      return false
    }
    
    // 检查是否是PDF格式
    const arrayBuffer = await blob.slice(0, 4).arrayBuffer()
    const uint8Array = new Uint8Array(arrayBuffer)
    const pdfSignature = [0x25, 0x50, 0x44, 0x46] // %PDF
    const isPdf = uint8Array.length >= 4 && 
                  uint8Array[0] === pdfSignature[0] && 
                  uint8Array[1] === pdfSignature[1] && 
                  uint8Array[2] === pdfSignature[2] && 
                  uint8Array[3] === pdfSignature[3]
    
    if (!isPdf) {
      const contentType = response.headers.get('content-type') || ''
      if (!contentType.includes('pdf') && !contentType.includes('octet-stream') && !contentType.includes('binary')) {
        logger.warn('代理返回的文件可能不是PDF格式，尝试直接使用原始链接', { contentType, size: blob.size })
        return false
      }
    }
    
    return true
  } catch (proxyError) {
    logger.warn('代理接口调用失败，尝试直接使用原始链接', proxyError)
    return false
  }
}

// iframe加载完成
const handleIframeLoad = async () => {
  // 清除超时定时器
  if (loadTimeout.value) {
    clearTimeout(loadTimeout.value)
    loadTimeout.value = null
  }
  
  // 如果使用代理，验证响应
  if (useProxy.value) {
    try {
      // 等待一小段时间让iframe内容加载
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 尝试检查iframe内容是否加载成功
      if (previewIframe.value) {
        try {
          // 尝试访问iframe内容（可能会因为跨域失败，这是正常的）
          const iframeDoc = previewIframe.value.contentDocument || previewIframe.value.contentWindow?.document
          if (iframeDoc) {
            const bodyText = iframeDoc.body?.innerText || ''
            // 检查是否是错误响应（JSON错误响应）
            if (bodyText.includes('"code"') && bodyText.includes('"message"')) {
              logger.warn('检测到代理返回错误响应，尝试直接使用原始链接')
              useProxy.value = false
              loading.value = true
              return
            }
          }
        } catch (e) {
          // 跨域访问失败是正常的，继续
          logger.debug('无法访问iframe内容（可能是跨域限制）', e)
        }
      }
    } catch (e) {
      logger.warn('验证iframe内容失败', e)
    }
  }
  
  loading.value = false
  logger.info('PDF预览iframe加载完成', { useProxy: useProxy.value })
}

// iframe加载错误
const handleIframeError = () => {
  if (loadTimeout.value) {
    clearTimeout(loadTimeout.value)
    loadTimeout.value = null
  }
  
  if (useProxy.value) {
    // 代理失败，尝试直接使用原始链接
    logger.warn('iframe加载失败，尝试直接使用原始链接')
    useProxy.value = false
    loading.value = true
  } else {
    // 直接链接也失败
    loading.value = false
    error.value = '文件加载失败，请检查文件链接是否有效'
    logger.error('文件加载失败', { fileUrl: props.fileUrl })
  }
}

// 初始化加载
const initLoad = async () => {
  loading.value = true
  error.value = ''
  useProxy.value = true
  
  // 设置超时（30秒）
  loadTimeout.value = setTimeout(() => {
    if (loading.value) {
      logger.warn('文件加载超时')
      if (useProxy.value) {
        // 代理超时，尝试直接使用原始链接
        useProxy.value = false
        loading.value = true
        // 重新设置超时
        loadTimeout.value = setTimeout(() => {
          loading.value = false
          error.value = '文件加载超时，请检查网络连接或文件链接'
        }, 30000)
      } else {
        loading.value = false
        error.value = '文件加载超时，请检查网络连接或文件链接'
      }
    }
  }, 30000)
  
  // 验证代理响应
  if (props.fileUrl) {
    const proxyValid = await verifyProxyResponse()
    if (!proxyValid) {
      useProxy.value = false
    }
  }
}

// 前往官网
const handleGoToOfficial = () => {
  const url = props.landingPageUrl || props.fileUrl
  if (url) {
    window.open(url, '_blank')
  }
}

// 下载
const handleDownload = () => {
  if (props.fileUrl) {
    const link = document.createElement('a')
    link.href = props.fileUrl
    link.download = ''
    link.target = '_blank'
    link.rel = 'noopener noreferrer'
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('开始下载')
  }
}

watch(() => props.fileUrl, () => {
  if (loadTimeout.value) {
    clearTimeout(loadTimeout.value)
    loadTimeout.value = null
  }
  initLoad()
}, { immediate: true })

onBeforeUnmount(() => {
  if (loadTimeout.value) {
    clearTimeout(loadTimeout.value)
    loadTimeout.value = null
  }
})
</script>

<style scoped lang="scss">
.external-file-preview {
  position: relative;
  width: 100%;
  height: 800px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #f9fafb;

  .loading-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    background: rgba(255, 255, 255, 0.9);
    z-index: 10;

    .el-icon {
      font-size: 32px;
      color: var(--el-color-primary);
    }
  }

  .error-message {
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .error-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
    }
  }

  .preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
  }
}
</style>

