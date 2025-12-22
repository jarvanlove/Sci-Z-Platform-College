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
      :src="proxyUrl"
      class="preview-iframe"
      frameborder="0"
      @load="handleIframeLoad"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { createLogger } from '@/utils/simpleLogger'

const logger = createLogger('ExternalFilePreview')

const props = defineProps({
  fileUrl: {
    type: String,
    required: true
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

// 构建代理URL
const proxyUrl = computed(() => {
  if (!props.fileUrl) return ''
  return `/api/proxy/pdf?url=${encodeURIComponent(props.fileUrl)}`
})

// iframe加载完成
const handleIframeLoad = () => {
  loading.value = false
  logger.info('PDF预览iframe加载完成')
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
  loading.value = true
  error.value = ''
}, { immediate: true })
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

