<!--
/**
 * @description 文献详情页面
 * 展示文献详细信息，支持PDF预览、下载、收藏到知识库等功能
 * 包含右侧AI对话侧边栏，可直接与文献进行对话
 */
-->
<template>
  <div class="literature-detail-container">
    <div class="main-content-wrapper">
      <!-- 左侧主内容区 -->
      <div class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else-if="literature" class="detail-content">
          <!-- 返回按钮 -->
          <div class="back-button">
            <el-button text :icon="ArrowLeft" @click="handleBack">← 返回</el-button>
          </div>
          <!-- 详情卡片 -->
          <BaseCard class="literature-info-card">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Document /></el-icon>
                  <span class="header-title">详情</span>
                </div>
              </div>
            </template>

            <!-- 基本信息 -->
            <div class="literature-basic-info">
              <div class="info-row">
                <span class="info-label">{{ formatDate(literature.paperInfo?.publicationDate) }}</span>
                <span class="info-divider">|</span>
                <span class="info-value">{{ literature.impactMetrics?.citationCount || 0 }}</span>
                <span class="info-label">被引</span>
                <span class="info-divider">|</span>
                <el-tag v-if="literature.accessInfo?.isOpenAccess" type="success" size="small" class="oa-tag">
                  <span class="oa-dot"></span>
                  Open Access
                </el-tag>
              </div>
            </div>

            <!-- 标题 -->
            <div class="literature-title-section">
              <h1 class="literature-title">{{ literature.paperInfo?.title || '' }}</h1>
              <div v-if="literature.paperInfo?.titleTranslated" class="title-translated">
                {{ literature.paperInfo.titleTranslated }}
              </div>
              <div class="title-actions">
                <el-button :icon="Collection" @click="showCollectDialog = true">收藏</el-button>
                <el-button :icon="DocumentCopy">
                  {{ literature.impactMetrics?.citationCount || 0 }} 引用
                </el-button>
                <el-button :icon="Share" @click="handleShare">分享</el-button>
              </div>
            </div>

            <!-- 发表信息 -->
            <div v-if="literature.sourceInfo" class="publication-info">
              <div class="publication-item">
                <el-icon class="publication-icon"><CircleCheck /></el-icon>
                <span class="publication-text">{{ literature.sourceInfo.journalName }}</span>
              </div>
            </div>

            <!-- 作者信息 -->
            <div v-if="literature.authorsInfo && literature.authorsInfo.length > 0" class="authors-section">
              <div
                v-for="(author, index) in literature.authorsInfo"
                :key="index"
                class="author-item"
              >
                <div class="author-avatar">{{ getAuthorInitial(author.name) }}</div>
                <span class="author-name">{{ author.name }}</span>
                <el-tag v-if="author.isCorresponding" type="success" size="small" class="corresponding-tag">
                  通讯
                </el-tag>
              </div>
            </div>

            <!-- 链接信息表格 -->
            <div class="links-section">
              <div class="links-table">
                <!-- 第一行：链接（官方网址） -->
                <div class="links-table-row">
                  <div class="links-table-label">
                    <el-icon class="link-icon"><Link /></el-icon>
                    <span>链接</span>
                  </div>
                  <div class="links-table-content">
                    <el-button
                      v-if="literature.accessInfo?.landingPage"
                      type="primary"
                      :icon="Link"
                      size="small"
                      @click="handleOpenOfficialUrl"
                    >
                      官方网址
                    </el-button>
                    <span v-else class="link-value-empty">-</span>
                  </div>
                </div>

                <!-- 第二行：IS、DOI -->
                <div class="links-table-row">
                  <div class="links-table-label">
                    <el-icon class="link-icon"><InfoFilled /></el-icon>
                    <span>IS</span>
                  </div>
                  <div class="links-table-content">
                    <span class="link-value">{{ literature.sourceInfo?.issn || '-' }}</span>
                    <span v-if="literature.sourceInfo?.issn && literature.paperInfo?.doi" class="link-divider">|</span>
                    <span v-if="literature.paperInfo?.doi" class="link-label-inline">DOI</span>
                    <span v-if="literature.paperInfo?.doi" class="link-value">{{ literature.paperInfo.doi }}</span>
                  </div>
                </div>

                <!-- 第三行：OA、研究分类 -->
                <div class="links-table-row">
                  <div class="links-table-label">
                    <el-icon class="link-icon"><InfoFilled /></el-icon>
                    <span>OA</span>
                  </div>
                  <div class="links-table-content">
                    <span class="link-value">{{ literature.accessInfo?.isOpenAccess ? '是' : '否' }}</span>
                    <span v-if="literature.taxonomy?.primaryField" class="link-divider">|</span>
                    <span v-if="literature.taxonomy?.primaryField" class="link-label-inline">研究分类</span>
                    <div v-if="literature.taxonomy?.primaryField" class="research-categories-inline">
                      <div v-if="literature.taxonomy.primaryField" class="category-item-inline">
                        [1] {{ literature.taxonomy.primaryField }}
                      </div>
                      <div v-if="literature.taxonomy.subField" class="category-item-inline">
                        [2] {{ literature.taxonomy.subField }}
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 第四行：PDF文档 -->
                <div class="links-table-row">
                  <div class="links-table-label">
                    <el-icon class="link-icon"><Document /></el-icon>
                    <span>PDF</span>
                  </div>
                  <div class="links-table-content">
                    <template v-if="literature.accessInfo?.pdfLink">
                      <el-button
                        type="primary"
                        :icon="View"
                        size="small"
                        :loading="pdfLoading"
                        @click="handlePreviewPdf"
                      >
                        预览
                      </el-button>
                      <el-button
                        :icon="Download"
                        size="small"
                        :loading="pdfDownloading"
                        @click="handleDownloadPdf"
                      >
                        下载
                      </el-button>
                    </template>
                    <template v-else>
                      <span class="link-value-empty">-</span>
                      <span class="pdf-tip-text">
                        （该文献暂无PDF链接，请前往
                        <a
                          v-if="literature.accessInfo?.landingPage"
                          :href="literature.accessInfo.landingPage"
                          target="_blank"
                          class="pdf-tip-link"
                        >
                          官方网址
                        </a>
                        <span v-else class="pdf-tip-link-disabled">官方网址</span>
                        下载）
                      </span>
                    </template>
                  </div>
                </div>
              </div>
            </div>


            <!-- 关键词 -->
            <div v-if="literature.taxonomy?.keywords && literature.taxonomy.keywords.length > 0" class="keywords-section">
              <div class="section-title">关键词</div>
              <div class="keywords-list">
                <el-tag
                  v-for="(keyword, index) in literature.taxonomy.keywords"
                  :key="index"
                  size="small"
                  class="keyword-tag"
                >
                  {{ keyword }}
                </el-tag>
              </div>
            </div>

            <!-- 摘要 -->
            <div v-if="literature.paperInfo?.abstractText" class="abstract-section">
              <div class="section-title">摘要</div>
              <p class="abstract-text">{{ literature.paperInfo.abstractText }}</p>
            </div>
          </BaseCard>
        </div>

        <div v-else class="empty-state">
          <el-empty description="文献数据不存在" />
        </div>
      </div>

      <!-- 右侧AI对话侧边栏 -->
      <div class="chat-sidebar">
        <div class="chat-sidebar-content">
          <!-- 问候语 -->
          <div v-if="chatMessages.length === 0" class="chat-greeting">
            <h2>你好, 科学家 👋</h2>
          </div>

          <!-- 文档交互卡片 -->
          <div v-if="chatMessages.length === 0" class="document-card">
            <div class="document-card-header">
              <el-icon><Document /></el-icon>
              <span class="document-title">{{ truncateTitle(literature?.paperInfo?.title || '') }}</span>
            </div>
            <div class="document-card-label">文献对话</div>
            <div class="document-card-hint">
              {{ hasPdfFile ? '当前文献PDF已加载，可直接提问' : '上传文件或对当前文献，询问任何问题' }}
            </div>
          </div>

          <!-- 附件预览 -->
          <div v-if="chatAttachments.length > 0" class="chat-attachments">
            <div
              v-for="(attachment, index) in chatAttachments"
              :key="index"
              class="chat-attachment-item"
            >
              <el-icon><Document /></el-icon>
              <span class="attachment-name">{{ attachment.name }}</span>
              <el-icon class="remove-icon" @click="removeChatAttachment(index)"><Close /></el-icon>
            </div>
          </div>

          <!-- 建议操作 -->
          <div v-if="chatMessages.length === 0" class="suggested-actions">
            <div class="action-item" @click="handleSuggestedAction('summarize')">
              <el-icon><DocumentCopy /></el-icon>
              <span>概括主要内容</span>
            </div>
            <div class="action-item" @click="handleSuggestedAction('findings')">
              <el-icon><StarFilled /></el-icon>
              <span>提炼主要发现</span>
            </div>
            <div class="action-item" @click="handleSuggestedAction('methods')">
              <el-icon><DocumentCopy /></el-icon>
              <span>核心研究方法</span>
            </div>
            <div class="action-item" @click="handleSuggestedAction('concepts')">
              <el-icon><StarFilled /></el-icon>
              <span>核心学术概念</span>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="chat-messages" ref="chatMessagesRef">
            <div v-if="chatMessages.length === 0" class="empty-chat-message">
              <p>Hi，任何问题都可以问我</p>
            </div>
            <div
              v-for="(message, index) in chatMessages"
              :key="message.id || index"
              class="chat-message"
              :class="message.type"
            >
              <div class="message-content">
                <div v-if="message.type === 'user'" class="user-message">
                  {{ message.content }}
                </div>
                <div v-else class="ai-message">
                  <div class="ai-avatar">AI</div>
                  <div class="ai-content">
                    <div v-if="message.isGenerating && !message.content" class="generating-indicator">
                      <span></span><span></span><span></span>
                    </div>
                    <div v-else class="message-text">{{ message.content }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
            <div class="input-toolbar">
              <el-button
                text
                :icon="Paperclip"
                @click="handleChatFileUpload"
                :disabled="isGeneratingChat"
              >
              </el-button>
              <input
                ref="chatFileInput"
                type="file"
                accept=".pdf,.doc,.docx,.txt,.md"
                style="display: none"
                @change="handleChatFileSelect"
              />
            </div>
            <div class="input-wrapper">
              <el-input
                v-model="chatInputMessage"
                type="textarea"
                :rows="3"
                placeholder="请概括这篇文献的主要内容"
                :disabled="isGeneratingChat"
                @keydown.enter.exact.prevent="sendChatMessage"
                @keydown.shift.enter.exact.prevent="() => {}"
              />
              <div class="input-actions">
                <el-button
                  type="primary"
                  :icon="ArrowUp"
                  :loading="isGeneratingChat"
                  :disabled="!chatInputMessage.trim()"
                  @click="sendChatMessage"
                >
                  发送
                </el-button>
              </div>
            </div>
            <div class="ai-disclaimer">
              <span>内容由AI生成</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- PDF预览弹窗 -->
    <el-dialog
      v-model="showPdfPreview"
      title="PDF预览"
      width="90%"
      :close-on-click-modal="false"
      class="pdf-preview-dialog"
      destroy-on-close
    >
      <template #header>
        <div class="pdf-dialog-header">
          <span class="pdf-dialog-title">{{ pdfFileName || 'PDF文档' }}</span>
        </div>
      </template>
      <div class="pdf-dialog-content">
        <iframe
          v-if="pdfPreviewUrl"
          :src="pdfPreviewUrl"
          class="pdf-preview-iframe-dialog"
          frameborder="0"
        />
        <div v-else class="pdf-preview-loading-dialog">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载PDF中...</span>
        </div>
      </div>
      <template #footer>
        <div class="pdf-dialog-footer">
          <el-button @click="closePdfPreview">关闭</el-button>
          <el-button
            type="primary"
            :icon="Download"
            :loading="pdfDownloading"
            @click="handleDownloadPdf"
          >
            下载PDF
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 收藏对话框 -->
    <el-dialog
      v-model="showCollectDialog"
      title="收藏到知识库"
      width="500px"
    >
      <el-form :model="collectForm" label-width="100px">
        <el-form-item label="选择知识库" required>
          <el-select
            v-model="collectForm.knowledgeId"
            placeholder="请选择知识库"
            style="width: 100%"
          >
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCollectDialog = false">取消</el-button>
        <el-button type="primary" :loading="collecting" @click="handleConfirmCollect">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Download,
  Share,
  Collection,
  Loading,
  Document,
  DocumentCopy,
  CircleCheck,
  Link,
  InfoFilled,
  Paperclip,
  ArrowUp,
  Close,
  StarFilled,
  View
} from '@element-plus/icons-vue'
import { getKnowledgeList, uploadFilesToKnowledge } from '@/api/Knowledge/knowledge'
import { runWorkflowStream } from '@/api/AI/ai'
import BaseCard from '@/components/Common/BaseCard.vue'
import { createLogger } from '@/utils/simpleLogger'

const router = useRouter()
const route = useRoute()
const logger = createLogger('LiteratureDetail')

// 响应式数据
const loading = ref(false)
const literature = ref(null)
const showCollectDialog = ref(false)
const collecting = ref(false)
const knowledgeBases = ref([])
const collectForm = reactive({
  knowledgeId: ''
})

// AI对话相关
const chatMessages = ref([])
const chatInputMessage = ref('')
const isGeneratingChat = ref(false)
const chatAttachments = ref([])
const pdfFile = ref(null)
const hasPdfFile = ref(false)
const chatMessagesRef = ref(null)
const chatFileInput = ref(null)

// PDF预览和下载相关
const showPdfPreview = ref(false)
const pdfPreviewUrl = ref('')
const pdfFileName = ref('')
const pdfLoading = ref(false)
const pdfDownloading = ref(false)
const cachedPdfBlob = ref(null) // 缓存的PDF Blob

// 加载文献详情（从 sessionStorage）
const loadLiteratureDetail = () => {
  const id = route.params.id

  if (!id) {
    ElMessage.error('文献ID不存在')
    router.back()
    return
  }

  loading.value = true

  // 尝试从 sessionStorage 获取（支持多种key格式）
  const possibleKeys = [
    `literature_detail_${id}`,
    `literature_detail_${id.split('/').pop()}`,
    `literature_detail_${id.replace(/^https?:\/\//, '').replace(/\//g, '_')}`
  ]

  let storedData = null
  for (const key of possibleKeys) {
    const data = sessionStorage.getItem(key)
    if (data) {
      storedData = data
      logger.info('从 sessionStorage 加载文献详情', { key, id })
      break
    }
  }

  if (storedData) {
    try {
      literature.value = JSON.parse(storedData)
      logger.info('加载文献详情成功', { id })
      
      // 自动下载PDF用于对话
      if (literature.value?.accessInfo?.pdfLink) {
        downloadPdfForChat()
      }
    } catch (e) {
      logger.warn('解析 sessionStorage 数据失败', e)
      ElMessage.error('文献数据格式错误')
      router.back()
    }
  } else {
    logger.warn('未找到文献数据', { id, possibleKeys })
    ElMessage.error('文献数据不存在，请返回搜索页面重新选择')
    router.back()
  }

  loading.value = false
}

// 自动下载PDF用于对话
const downloadPdfForChat = async () => {
  if (!literature.value?.accessInfo?.pdfLink) {
    logger.warn('PDF链接不存在，无法下载')
    return false
  }

  const pdfUrl = literature.value.accessInfo.pdfLink
  const fileName = `${literature.value.paperInfo?.title || '文献'}.pdf`

  try {
    ElMessage.info('正在下载PDF文件...')
    
    const proxyUrl = `/api/proxy/pdf?url=${encodeURIComponent(pdfUrl)}`
    const response = await fetch(proxyUrl)
    
    if (!response.ok) {
      const contentType = response.headers.get('content-type')
      if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'PDF下载失败')
      }
      throw new Error(`PDF下载失败: HTTP ${response.status}`)
    }

    const contentType = response.headers.get('content-type')
    if (!contentType || !contentType.includes('application/pdf')) {
      const text = await response.text()
      try {
        const errorData = JSON.parse(text)
        throw new Error(errorData.message || 'PDF下载失败')
      } catch {
        throw new Error('下载的文件不是PDF格式')
      }
    }

    const blob = await response.blob()
    
    if (blob.size < 100 * 1024) { // 100KB
      throw new Error('PDF文件大小过小，可能不是有效的PDF文件')
    }

    const file = new File([blob], sanitizeFileName(fileName), { type: 'application/pdf' })
    
    // 缓存PDF Blob用于预览和下载
    cachedPdfBlob.value = blob
    pdfFileName.value = file.name
    
    chatAttachments.value = [{
      name: file.name,
      size: formatFileSize(file.size),
      file: file,
      type: 'pdf'
    }]
    
    pdfFile.value = file
    hasPdfFile.value = true
    
    ElMessage.success('PDF文件已加载，可以开始对话')
    logger.info('PDF文件下载成功并添加到聊天附件', { fileName, size: file.size })
    return true
  } catch (error) {
    logger.error('下载PDF文件失败', error)
    ElMessage.warning('PDF文件无法自动下载，您可以手动上传文件进行对话')
    return false
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

// 加载知识库列表
const loadKnowledgeBases = async () => {
  try {
    const response = await getKnowledgeList()
    if (response.code === 200 && response.data) {
      knowledgeBases.value = response.data.records || []
    }
  } catch (error) {
    logger.error('加载知识库列表失败', error)
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 预览PDF（使用缓存的PDF）
const handlePreviewPdf = async () => {
  if (!literature.value?.accessInfo?.pdfLink) {
    ElMessage.warning('PDF链接不存在')
    return
  }

  pdfLoading.value = true

  try {
    // 优先使用缓存的PDF
    if (cachedPdfBlob.value) {
      // 创建blob URL用于预览
      if (pdfPreviewUrl.value && pdfPreviewUrl.value.startsWith('blob:')) {
        URL.revokeObjectURL(pdfPreviewUrl.value)
      }
      pdfPreviewUrl.value = URL.createObjectURL(cachedPdfBlob.value)
      showPdfPreview.value = true
      pdfLoading.value = false
      logger.info('使用缓存的PDF进行预览')
      return
    }

    // 如果没有缓存，先下载
    ElMessage.info('正在加载PDF...')
    const pdfUrl = literature.value.accessInfo.pdfLink
    const fileName = `${literature.value.paperInfo?.title || '文献'}.pdf`
    
    // 先尝试使用代理接口
    let blob = null
    let useProxy = true
    
    try {
      const proxyUrl = `/api/proxy/pdf?url=${encodeURIComponent(pdfUrl)}`
      const response = await fetch(proxyUrl)
      
      if (!response.ok) {
        // 代理失败，尝试直接使用原始链接
        logger.warn('代理接口失败，尝试直接使用PDF链接', { status: response.status })
        useProxy = false
      } else {
        blob = await response.blob()
        
        // 检查文件大小（至少10KB，放宽限制）
        if (blob.size < 10 * 1024) {
          // 文件太小，可能是错误响应，尝试直接使用原始链接
          logger.warn('代理返回的文件过小，尝试直接使用PDF链接', { size: blob.size })
          useProxy = false
        } else {
          // 检查是否是PDF格式（通过检查blob的前几个字节）
          const arrayBuffer = await blob.slice(0, 4).arrayBuffer()
          const uint8Array = new Uint8Array(arrayBuffer)
          const pdfSignature = [0x25, 0x50, 0x44, 0x46] // %PDF
          const isPdf = uint8Array.length >= 4 && 
                        uint8Array[0] === pdfSignature[0] && 
                        uint8Array[1] === pdfSignature[1] && 
                        uint8Array[2] === pdfSignature[2] && 
                        uint8Array[3] === pdfSignature[3]

          if (!isPdf) {
            // 检查Content-Type
            const contentType = response.headers.get('content-type') || ''
            if (!contentType.includes('pdf') && !contentType.includes('octet-stream') && !contentType.includes('binary')) {
              logger.warn('代理返回的文件可能不是PDF格式，尝试直接使用PDF链接', { contentType, size: blob.size })
              useProxy = false
            }
          }
        }
      }
    } catch (proxyError) {
      logger.warn('代理接口调用失败，尝试直接使用PDF链接', proxyError)
      useProxy = false
    }

    // 如果代理失败或返回的不是PDF，直接使用原始PDF链接
    if (!useProxy || !blob) {
      logger.info('使用原始PDF链接进行预览', { pdfUrl })
      // 直接使用原始PDF链接创建预览URL
      pdfPreviewUrl.value = pdfUrl
      pdfFileName.value = sanitizeFileName(fileName)
      showPdfPreview.value = true
      pdfLoading.value = false
      ElMessage.success('PDF加载成功')
      return
    }

    // 使用代理返回的blob
    // 缓存PDF Blob（强制设置为PDF类型）
    const pdfBlob = new Blob([blob], { type: 'application/pdf' })
    cachedPdfBlob.value = pdfBlob
    pdfFileName.value = sanitizeFileName(fileName)
    
    // 创建blob URL用于预览
    pdfPreviewUrl.value = URL.createObjectURL(pdfBlob)
    showPdfPreview.value = true
    
    ElMessage.success('PDF加载成功')
    logger.info('PDF加载成功并创建预览', { fileName, size: blob.size })
  } catch (error) {
    logger.error('预览PDF失败', error)
    ElMessage.error('预览失败：' + (error.message || '未知错误'))
  } finally {
    pdfLoading.value = false
  }
}

// 打开官方网址
const handleOpenOfficialUrl = () => {
  if (literature.value?.accessInfo?.landingPage) {
    window.open(literature.value.accessInfo.landingPage, '_blank')
  }
}

// 关闭PDF预览
const closePdfPreview = () => {
  showPdfPreview.value = false
  // 清理blob URL（但保留cachedPdfBlob用于下载）
  // 注意：不要在这里revoke URL，因为可能还需要预览
}

// 下载PDF（直接下载PDF链接）
const handleDownloadPdf = () => {
  if (!literature.value?.accessInfo?.pdfLink) {
    ElMessage.warning('PDF链接不存在')
    return
  }

  pdfDownloading.value = true

  try {
    const pdfUrl = literature.value.accessInfo.pdfLink
    const fileName = sanitizeFileName(`${literature.value.paperInfo?.title || '文献'}.pdf`)

    // 直接使用PDF链接下载
    const link = document.createElement('a')
    link.href = pdfUrl
    link.download = fileName
    link.target = '_blank'
    link.rel = 'noopener noreferrer'
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('PDF下载开始')
    logger.info('PDF下载开始', { pdfUrl, fileName })
    
    // 延迟重置loading状态，给用户反馈
    setTimeout(() => {
      pdfDownloading.value = false
    }, 500)
  } catch (error) {
    logger.error('下载PDF失败', error)
    ElMessage.error('下载失败：' + (error.message || '未知错误'))
    pdfDownloading.value = false
  }
}

// 分享
const handleShare = () => {
  ElMessage.info('分享功能待实现')
}

// 确认收藏
const handleConfirmCollect = async () => {
  if (!collectForm.knowledgeId) {
    ElMessage.warning('请选择知识库')
    return
  }

  collecting.value = true
  try {
    const selectedKb = knowledgeBases.value.find(kb => kb.id === collectForm.knowledgeId)
    if (!selectedKb) {
      ElMessage.error('选择的知识库不存在')
      return
    }

    const pdfLink = literature.value?.accessInfo?.pdfLink
    if (!pdfLink) {
      ElMessage.warning('该文献没有PDF链接，无法收藏')
      collecting.value = false
      return
    }

    // 下载PDF文件
    ElMessage.info('正在下载PDF文件...')
    const rawFileName = `${literature.value.paperInfo?.title || '文献'}.pdf`
    const fileName = sanitizeFileName(rawFileName)
    
    let pdfFile = null
    
    // 优先使用缓存的PDF
    if (cachedPdfBlob.value) {
      pdfFile = new File([cachedPdfBlob.value], fileName, { type: 'application/pdf' })
      logger.info('使用缓存的PDF进行收藏')
    } else {
      // 下载PDF文件
      try {
        const proxyUrl = `/api/proxy/pdf?url=${encodeURIComponent(pdfLink)}`
        const response = await fetch(proxyUrl)
        
        if (!response.ok) {
          throw new Error(`PDF下载失败: HTTP ${response.status}`)
        }

        const blob = await response.blob()
        
        if (blob.size < 10 * 1024) {
          throw new Error('PDF文件大小过小，可能不是有效的PDF文件')
        }

        // 转换为File对象
        pdfFile = new File([blob], fileName, { type: 'application/pdf' })
        
        // 缓存PDF Blob
        cachedPdfBlob.value = blob
        pdfFileName.value = fileName
        
        logger.info('PDF文件下载成功', { fileName, size: blob.size })
      } catch (downloadError) {
        logger.error('下载PDF文件失败', downloadError)
        ElMessage.error('下载PDF文件失败：' + (downloadError.message || '未知错误'))
        collecting.value = false
        return
      }
    }

    // 调用批量上传接口
    ElMessage.info('正在上传到知识库...')
    const response = await uploadFilesToKnowledge(
      selectedKb.id, // 使用知识库的数据库主键ID
      [pdfFile], // 文件数组
      0 // folderId，默认根目录
    )
    
    if (response.code === 200) {
      ElMessage.success('收藏成功')
      showCollectDialog.value = false
      collectForm.knowledgeId = ''
    } else {
      ElMessage.error(response.message || '收藏失败')
    }
  } catch (error) {
    logger.error('收藏到知识库失败', error)
    ElMessage.error('收藏失败：' + (error.message || '未知错误'))
  } finally {
    collecting.value = false
  }
}

// 清理文件名
const sanitizeFileName = (fileName) => {
  if (!fileName) return '文献.pdf'
  return fileName
    .replace(/[<>:"/\\|?*]/g, '_')
    .replace(/\s+/g, '_')
    .substring(0, 200)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 获取作者首字母
const getAuthorInitial = (name) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

// 截断标题
const truncateTitle = (title) => {
  if (!title) return ''
  if (title.length <= 50) return title
  return title.substring(0, 50) + '...'
}

// AI对话相关方法
const handleSuggestedAction = (action) => {
  const actionMap = {
    summarize: '请概括这篇文献的主要内容',
    findings: '请提炼这篇文献的主要发现和结论',
    methods: '请介绍这篇文献的核心研究方法',
    concepts: '请解释这篇文献的核心学术概念'
  }
  chatInputMessage.value = actionMap[action] || ''
  sendChatMessage()
}

const handleChatFileUpload = () => {
  chatFileInput.value?.click()
}

const handleChatFileSelect = (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  if (file.size < 100 * 1024) {
    ElMessage.warning('文件大小过小，文件大小不得小于100KB')
    return
  }

  chatAttachments.value = [{
    name: file.name,
    size: formatFileSize(file.size),
    file: file,
    type: file.type || 'unknown'
  }]

  ElMessage.success('文件已添加')
}

const removeChatAttachment = (index) => {
  chatAttachments.value.splice(index, 1)
  if (chatAttachments.value.length === 0) {
    pdfFile.value = null
    hasPdfFile.value = false
  }
}

const sendChatMessage = async () => {
  const message = chatInputMessage.value.trim()
  if (!message && chatAttachments.value.length === 0) {
    return
  }

  if (isGeneratingChat.value) {
    return
  }

  // 添加用户消息
  const userMessage = {
    id: Date.now(),
    type: 'user',
    content: message,
    timestamp: new Date()
  }
  chatMessages.value.push(userMessage)

  // 清空输入
  chatInputMessage.value = ''

  // 添加AI消息占位符
  const aiMessageId = Date.now() + 1
  const aiMessage = {
    id: aiMessageId,
    type: 'ai',
    content: '',
    isGenerating: true,
    timestamp: new Date()
  }
  chatMessages.value.push(aiMessage)

  // 滚动到底部，显示新消息
  await nextTick()
  scrollChatToBottom()

  isGeneratingChat.value = true

  try {
    // 准备文件
    const filesToUpload = chatAttachments.value.map(att => att.file).filter(Boolean)

    // 调用工作流流式接口
    await runWorkflowStream({
      query: message || '请分析这篇文献',
      knowledgeId: undefined,
      workflowId: undefined,
      files: filesToUpload.length > 0 ? filesToUpload : undefined,
      conversationId: undefined,
      onMessage: (answer) => {
        // 流式更新消息内容（追加到底部，不是滚动）
        const msg = chatMessages.value.find(m => m.id === aiMessageId)
        if (msg && answer) {
          // 直接追加内容，Vue会自动更新DOM
          msg.content += answer
          
          // 每次更新后立即滚动到底部，确保新内容可见
          // 不使用防抖，让用户能看到实时的流式输出
          nextTick(() => {
            scrollChatToBottom()
          })
        }
      },
      onEnd: (data) => {
        const msg = chatMessages.value.find(m => m.id === aiMessageId)
        if (msg) {
          msg.isGenerating = false
        }
        isGeneratingChat.value = false
        
        // 最终滚动到底部，确保完整内容可见
        nextTick(() => {
          scrollChatToBottom()
        })
      },
      onError: (error) => {
        logger.error('AI对话失败', error)
        ElMessage.error('对话失败：' + (error.message || '未知错误'))
        const msg = chatMessages.value.find(m => m.id === aiMessageId)
        if (msg) {
          msg.isGenerating = false
          msg.content = msg.content || '抱歉，对话过程中出现错误，请重试。'
        }
        isGeneratingChat.value = false
        
        nextTick(() => {
          scrollChatToBottom()
        })
      }
    })
  } catch (error) {
    logger.error('发送消息失败', error)
    ElMessage.error('发送消息失败：' + (error.message || '未知错误'))
    const msg = chatMessages.value.find(m => m.id === aiMessageId)
    if (msg) {
      msg.isGenerating = false
      msg.content = msg.content || '抱歉，发送消息时出现错误，请重试。'
    }
    isGeneratingChat.value = false
    
    nextTick(() => {
      scrollChatToBottom()
    })
  }
}

// 滚动到底部（使用平滑滚动，确保消息追加到底部）
const scrollChatToBottom = () => {
  if (chatMessagesRef.value) {
    // 使用 requestAnimationFrame 确保 DOM 更新后再滚动
    requestAnimationFrame(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    })
  }
}

// 监听PDF链接变化，自动下载
watch(() => literature.value?.accessInfo?.pdfLink, (newPdfLink) => {
  if (newPdfLink && !hasPdfFile.value) {
    downloadPdfForChat()
  }
}, { immediate: false })

// 组件卸载时清理blob URL
onUnmounted(() => {
  if (pdfPreviewUrl.value && pdfPreviewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(pdfPreviewUrl.value)
  }
})

onMounted(() => {
  loadLiteratureDetail()
  loadKnowledgeBases()
})
</script>

<style scoped lang="scss">
.literature-detail-container {
  padding: 0;
  min-height: calc(100vh - 60px);
  background: #f5f5f5;
  display: flex;
  flex-direction: column;

  .main-content-wrapper {
    display: flex;
    flex: 1;
    overflow: hidden;
  }

  .main-content {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background: #fff;

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      gap: 12px;
      color: var(--el-text-color-secondary);

      .el-icon {
        font-size: 32px;
      }
    }

    .back-button {
      margin-bottom: 16px;
    }

    .literature-info-card {
      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;

        .header-left {
          display: flex;
          align-items: center;
          gap: 8px;

          .header-icon {
            font-size: 18px;
            color: #6b7280;
          }

          .header-title {
            font-size: 16px;
            font-weight: 600;
            color: #111827;
          }
        }
      }

      .literature-basic-info {
        margin-bottom: 16px;
        padding-bottom: 16px;
        border-bottom: 1px solid #e5e7eb;

        .info-row {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #6b7280;

          .info-label {
            color: #6b7280;
          }

          .info-value {
            color: #111827;
            font-weight: 500;
          }

          .info-divider {
            color: #d1d5db;
          }

          .oa-tag {
            display: flex;
            align-items: center;
            gap: 4px;
            background: #d1fae5;
            color: #065f46;
            border: none;

            .oa-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background: #059669;
            }
          }
        }
      }

      .literature-title-section {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #e5e7eb;

        .literature-title {
          font-size: 24px;
          font-weight: 600;
          color: #111827;
          margin: 0 0 12px 0;
          line-height: 1.4;
        }

        .title-translated {
          font-size: 16px;
          color: #6b7280;
          margin-bottom: 16px;
          line-height: 1.5;
        }

        .title-actions {
          display: flex;
          gap: 8px;
        }
      }

      .publication-info {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #e5e7eb;

        .publication-item {
          display: flex;
          align-items: center;
          gap: 8px;

          .publication-icon {
            color: #059669;
            font-size: 18px;
          }

          .publication-text {
            font-size: 14px;
            color: #111827;
          }
        }
      }

      .authors-section {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #e5e7eb;
        display: flex;
        flex-wrap: wrap;
        gap: 12px;

        .author-item {
          display: flex;
          align-items: center;
          gap: 8px;

          .author-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 14px;
            flex-shrink: 0;
          }

          .author-name {
            font-size: 14px;
            color: #374151;
            font-weight: 500;
          }

          .corresponding-tag {
            font-size: 12px;
          }
        }
      }

      .links-section {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #e5e7eb;

        .links-table {
          display: flex;
          flex-direction: column;
          gap: 0;
          border: 1px solid #e5e7eb;
          border-radius: 8px;
          overflow: hidden;
          background: #fff;

          .links-table-row {
            display: flex;
            align-items: center;
            padding: 12px 16px;
            border-bottom: 1px solid #f3f4f6;
            transition: background-color 0.2s;

            &:last-child {
              border-bottom: none;
            }

            &:hover {
              background-color: #f9fafb;
            }

            .links-table-label {
              display: flex;
              align-items: center;
              gap: 8px;
              min-width: 80px;
              font-size: 14px;
              font-weight: 500;
              color: #6b7280;
              flex-shrink: 0;

              .link-icon {
                color: #3b82f6;
                font-size: 16px;
              }
            }

            .links-table-content {
              flex: 1;
              display: flex;
              align-items: center;
              gap: 8px;
              flex-wrap: wrap;
              font-size: 14px;

              .link-value {
                color: #111827;
              }

              .link-value-empty {
                color: #9ca3af;
              }

              .link-divider {
                color: #d1d5db;
                margin: 0 8px;
              }

              .link-label-inline {
                color: #6b7280;
                font-weight: 500;
              }

              .research-categories-inline {
                display: inline-flex;
                flex-direction: column;
                gap: 2px;
                margin-left: 8px;

                .category-item-inline {
                  font-size: 14px;
                  color: #4b5563;
                  line-height: 1.5;
                }
              }

              .pdf-tip-text {
                font-size: 13px;
                color: #6b7280;
                margin-left: 8px;

                .pdf-tip-link {
                  color: #3b82f6;
                  text-decoration: none;
                  font-weight: 500;

                  &:hover {
                    text-decoration: underline;
                  }
                }

                .pdf-tip-link-disabled {
                  color: #9ca3af;
                  font-weight: 500;
                }
              }
            }
          }
        }
      }

      .research-categories {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #e5e7eb;

        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #111827;
          margin-bottom: 12px;
        }

        .categories-list {
          display: flex;
          flex-direction: column;
          gap: 8px;

          .category-item {
            font-size: 14px;
            color: #4b5563;
          }
        }
      }

      .keywords-section {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #e5e7eb;

        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #111827;
          margin-bottom: 12px;
        }

        .keywords-list {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;

          .keyword-tag {
            background: #f3f4f6;
            color: #4b5563;
            border: 1px solid #e5e7eb;
          }
        }
      }

      .abstract-section {
        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #111827;
          margin-bottom: 12px;
        }

        .abstract-text {
          font-size: 14px;
          line-height: 1.8;
          color: #4b5563;
          margin: 0;
        }
      }

    }

    .empty-state {
      padding: 60px 20px;
      text-align: center;
    }
  }

  // 右侧AI对话侧边栏
  .chat-sidebar {
    width: 400px;
    background: #fff;
    border-left: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;

    .chat-sidebar-content {
      display: flex;
      flex-direction: column;
      height: 100%;
      overflow: hidden;

      .chat-greeting {
        padding: 20px;
        text-align: center;
        border-bottom: 1px solid #e5e7eb;

        h2 {
          font-size: 20px;
          font-weight: 600;
          color: #111827;
          margin: 0;
        }
      }

      .document-card {
        margin: 16px;
        padding: 16px;
        background: #f9fafb;
        border: 1px solid #e5e7eb;
        border-radius: 8px;

        .document-card-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .document-title {
            font-size: 14px;
            font-weight: 500;
            color: #111827;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .document-card-label {
          font-size: 12px;
          color: #6b7280;
          margin-bottom: 8px;
        }

        .document-card-hint {
          font-size: 12px;
          color: #9ca3af;
        }
      }

      .chat-attachments {
        margin: 0 16px 16px;
        padding: 12px;
        background: #f9fafb;
        border-radius: 8px;

        .chat-attachment-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px;
          background: #fff;
          border-radius: 4px;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .attachment-name {
            flex: 1;
            font-size: 12px;
            color: #4b5563;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .remove-icon {
            cursor: pointer;
            color: #9ca3af;

            &:hover {
              color: #ef4444;
            }
          }
        }
      }

      .suggested-actions {
        margin: 0 16px 16px;
        display: flex;
        flex-direction: column;
        gap: 8px;

        .action-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 12px;
          background: #f9fafb;
          border: 1px solid #e5e7eb;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s;
          font-size: 14px;
          color: #4b5563;

          &:hover {
            background: #f3f4f6;
            border-color: #d1d5db;
          }
        }
      }

      .chat-messages {
        flex: 1;
        overflow-y: auto;
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 16px;
        // 确保消息从上到下排列，新消息追加到底部
        justify-content: flex-start;
        align-items: stretch;

        .empty-chat-message {
          text-align: center;
          color: #9ca3af;
          padding: 40px 20px;
        }

        .chat-message {
          &.user {
            .message-content {
              display: flex;
              justify-content: flex-end;

              .user-message {
                background: #3b82f6;
                color: #fff;
                padding: 12px 16px;
                border-radius: 12px;
                max-width: 80%;
                word-wrap: break-word;
              }
            }
          }

          &.ai {
            .message-content {
              display: flex;
              justify-content: flex-start;

              .ai-message {
                display: flex;
                gap: 12px;
                max-width: 80%;

                .ai-avatar {
                  width: 32px;
                  height: 32px;
                  border-radius: 50%;
                  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                  color: #fff;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-weight: 600;
                  font-size: 12px;
                  flex-shrink: 0;
                }

                .ai-content {
                  background: #f3f4f6;
                  padding: 12px 16px;
                  border-radius: 12px;
                  flex: 1;

                  .generating-indicator {
                    display: flex;
                    gap: 4px;

                    span {
                      width: 8px;
                      height: 8px;
                      border-radius: 50%;
                      background: #9ca3af;
                      animation: pulse 1.4s ease-in-out infinite;

                      &:nth-child(2) {
                        animation-delay: 0.2s;
                      }

                      &:nth-child(3) {
                        animation-delay: 0.4s;
                      }
                    }
                  }

                  .message-text {
                    color: #111827;
                    line-height: 1.6;
                    white-space: pre-wrap;
                    word-wrap: break-word;
                  }
                }
              }
            }
          }
        }
      }

      .chat-input-area {
        border-top: 1px solid #e5e7eb;
        padding: 16px;
        background: #fff;

        .input-toolbar {
          margin-bottom: 8px;
        }

        .input-wrapper {
          margin-bottom: 8px;

          :deep(.el-textarea__inner) {
            resize: none;
          }
        }

        .input-actions {
          display: flex;
          justify-content: flex-end;
          margin-bottom: 8px;
        }

        .ai-disclaimer {
          text-align: center;
          font-size: 12px;
          color: #9ca3af;
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}

// PDF预览弹窗样式
:deep(.pdf-preview-dialog) {
  .el-dialog__body {
    padding: 0;
  }

  .pdf-dialog-header {
    display: flex;
    align-items: center;
    gap: 8px;

    .pdf-dialog-title {
      font-size: 16px;
      font-weight: 600;
      color: #111827;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .pdf-dialog-content {
    position: relative;
    height: calc(90vh - 120px);
    min-height: 600px;
    background: #f9fafb;

    .pdf-preview-iframe-dialog {
      width: 100%;
      height: 100%;
      border: none;
      display: block;
    }

    .pdf-preview-loading-dialog {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      gap: 12px;
      color: var(--el-text-color-secondary);

      .el-icon {
        font-size: 32px;
      }
    }
  }

  .pdf-dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>

      height: 100%;
      border: none;
      display: block;
    }

    .pdf-preview-loading-dialog {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      gap: 12px;
      color: var(--el-text-color-secondary);

      .el-icon {
        font-size: 32px;
      }
    }
  }

  .pdf-dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
