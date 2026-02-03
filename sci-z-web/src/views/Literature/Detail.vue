<!--
/**
 * @description 文献详情页面
 * 展示文献详细信息，支持PDF预览、下载、收藏到知识库等功能
 * 包含右侧AI对话侧边栏，可直接与文献进行对话
 */
-->
<template>
  <div class="literature-detail-container">
    <div class="main-content-wrapper" :class="{ 'chat-expanded': isChatExpanded }">
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
      <div class="chat-sidebar" :class="{ expanded: isChatExpanded }">
        <div class="chat-sidebar-content">
          <!-- 布局切换按钮 -->
          <div class="chat-header">
            <el-button
              text
              @click="toggleChatLayout"
              class="layout-toggle-btn"
              :title="isChatExpanded ? '还原布局' : '展开对话'"
            >
              <el-icon>
                <ArrowRight v-if="!isChatExpanded" />
                <ArrowLeft v-else />
              </el-icon>
            </el-button>
          </div>
          
          <!-- 问候语 -->
          <div v-if="chatMessages.length === 0" class="chat-greeting">
            <h2>你好, 科学家 👋</h2>
          </div>

          <!-- 文档交互卡片 -->
          <!-- 只在没有PDF和附件时显示文档卡片 -->
          <div v-if="chatMessages.length === 0 && !hasPdfFile && chatAttachments.length === 0" class="document-card">
            <div class="document-card-header">
              <el-icon><Document /></el-icon>
              <span class="document-title">当前文件尚未加载</span>
            </div>
            <div class="document-card-label">文献对话</div>
            <div class="document-card-hint">当前文件尚未加载，要手动上传</div>
          </div>

          <!-- 附件预览：只在有对话消息后显示 -->
          <div v-if="chatAttachments.length > 0 && chatMessages.length > 0" class="chat-attachments">
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
              <el-icon><Link /></el-icon>
              <span>核心研究方法</span>
            </div>
            <div class="action-item" @click="handleSuggestedAction('concepts')">
              <el-icon><StarFilled /></el-icon>
              <span>核心学术概念</span>
            </div>
          </div>

          <!-- 未对话时的输入区域：显示在问候语下边 -->
          <div v-if="chatMessages.length === 0" class="chat-input-area" :class="{ 'has-status': hasPdfFile || chatAttachments.length > 0 }">
            <!-- 会话状态提示 -->
            <div v-if="(hasPdfFile || chatAttachments.length > 0)" class="conversation-status">
              <el-icon class="collapse-icon"><ArrowDown /></el-icon>
              <span>{{ conversationDocumentName }}</span>
            </div>
            
            <!-- 输入框容器 -->
            <div class="input-container">
              <div class="input-wrapper">
                <el-input
                  v-model="chatInputMessage"
                  type="textarea"
                  :rows="3"
                  :disabled="isGeneratingChat"
                  @keydown.enter.exact.prevent="sendChatMessage"
                  @keydown.shift.enter.exact.prevent="() => {}"
                  @focus="isInputFocused = true"
                  @blur="isInputFocused = false"
                  class="chat-textarea"
                  placeholder=" "
                />
                
                <!-- 自定义占位符 -->
                <div 
                  v-if="!chatInputMessage && !isInputFocused" 
                  class="custom-placeholder"
                >
                  <span class="placeholder-label">文献对话</span>
                  <span class="placeholder-hint">上传文件或对当前文献，询问任何问题</span>
                </div>
                
                <!-- 操作按钮 -->
                <div class="input-actions">
                  <el-button
                    class="attach-btn"
                    @click="handleChatFileUpload"
                    :disabled="isGeneratingChat"
                    title="文件上传"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button
                    class="send-btn"
                    :loading="isGeneratingChat"
                    :disabled="!chatInputMessage.trim()"
                    @click="sendChatMessage"
                  >
                    <el-icon><ArrowUp /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            
            <input
              ref="chatFileInput"
              type="file"
              accept=".pdf,.doc,.docx,.txt,.md"
              style="display: none"
              @change="handleChatFileSelect"
            />
          </div>

          <!-- 消息列表 -->
          <div class="chat-messages" ref="chatMessagesRef">
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
                    <div v-else class="message-text" v-html="formatMessageContent(message.content)"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 对话后的输入区域：显示在消息列表下边 -->
          <div v-if="chatMessages.length > 0" class="chat-input-area" :class="{ 'has-status': hasPdfFile || chatAttachments.length > 0 }">
            <!-- 会话状态提示 -->
            <div v-if="(hasPdfFile || chatAttachments.length > 0)" class="conversation-status">
              <el-icon class="collapse-icon"><ArrowDown /></el-icon>
              <span>{{ conversationDocumentName }}</span>
            </div>
            
            <!-- 输入框容器 -->
            <div class="input-container">
              <div class="input-wrapper">
                <el-input
                  v-model="chatInputMessage"
                  type="textarea"
                  :rows="3"
                  :disabled="isGeneratingChat"
                  @keydown.enter.exact.prevent="sendChatMessage"
                  @keydown.shift.enter.exact.prevent="() => {}"
                  @focus="isInputFocused = true"
                  @blur="isInputFocused = false"
                  class="chat-textarea"
                  placeholder=" "
                />
                
                <!-- 自定义占位符 -->
                <div 
                  v-if="!chatInputMessage && !isInputFocused" 
                  class="custom-placeholder"
                >
                  <span class="placeholder-label">文献对话</span>
                  <span class="placeholder-hint">上传文件或对当前文献，询问任何问题</span>
                </div>
                
                <!-- 操作按钮 -->
                <div class="input-actions">
                  <el-button
                    class="attach-btn"
                    @click="handleChatFileUpload"
                    :disabled="isGeneratingChat"
                    title="文件上传"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button
                    class="send-btn"
                    :loading="isGeneratingChat"
                    :disabled="!chatInputMessage.trim()"
                    @click="sendChatMessage"
                  >
                    <el-icon><ArrowUp /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            
            <input
              ref="chatFileInput"
              type="file"
              accept=".pdf,.doc,.docx,.txt,.md"
              style="display: none"
              @change="handleChatFileSelect"
            />
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
            filterable
            @visible-change="handleSelectVisibleChange"
            popper-class="knowledge-select-dropdown"
          >
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            >
              <template #default>
                <div class="kb-select-option">
                  <!-- 🔥 新增：显示知识库封面 -->
                  <div class="kb-select-icon">
                    <img
                      v-if="getKbCoverUrl(kb)"
                      :src="getKbCoverUrl(kb)"
                      alt="cover"
                      class="kb-cover-img"
                    />
                    <div v-else class="kb-select-icon-default">
                      <el-icon><Collection /></el-icon>
                    </div>
                  </div>
                  <span class="kb-select-name">{{ kb.name }}</span>
                </div>
              </template>
            </el-option>
            <!-- 🔥 新增：加载更多提示 -->
            <el-option v-if="kbListPagination.loading" disabled>
              <div class="kb-loading-more">{{ $t('common.loading') }}...</div>
            </el-option>
            <el-option v-else-if="!kbListPagination.hasMore && knowledgeBases.length > 0" disabled>
              <div class="kb-no-more">{{ $t('common.noMoreData') }}</div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCollectDialog = false">取消</el-button>
        <el-button :icon="Upload" :loading="manualUploading" @click="handleSelectFileForUpload">手动上传</el-button>
        <el-button type="primary" :loading="collecting" @click="handleConfirmCollect">确定</el-button>
      </template>
    </el-dialog>

    <!-- 隐藏的文件输入框 -->
    <input
      ref="manualUploadFileInput"
      type="file"
      accept=".pdf,.doc,.docx,.txt"
      multiple
      style="display: none"
      @change="handleFileSelectedForUpload"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  ArrowDown,
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
  View,
  Upload,
  Plus,
  Search
} from '@element-plus/icons-vue'
import { getKnowledgeList, getKnowledgeListPage, uploadFilesToKnowledge, getKnowledgeFileRelationList } from '@/api/Knowledge/knowledge'
import { runWorkflowStream } from '@/api/AI/ai'
import { cachePdf } from '@/api/File/file'
import BaseCard from '@/components/Common/BaseCard.vue'
import { createLogger } from '@/utils/simpleLogger'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const router = useRouter()
const route = useRoute()
const logger = createLogger('LiteratureDetail')

// 配置 marked 选项
marked.setOptions({
  breaks: true, // 支持 GitHub 风格的换行
  gfm: true, // 启用 GitHub Flavored Markdown
  headerIds: false, // 不生成标题 ID
  mangle: false // 不混淆邮箱地址
})

// 响应式数据
const loading = ref(false)
const literature = ref(null)
const showCollectDialog = ref(false)
// 🔥 修复：监听弹窗打开，在打开时加载知识库列表
watch(showCollectDialog, (newVal) => {
  if (newVal) {
    // 弹窗打开时，重置搜索和分页，加载知识库列表
    kbSearchQuery.value = ''
    kbListPagination.value.pageNo = 1
    collectForm.knowledgeId = ''
    // 点击收藏按钮时才调用接口
    loadKnowledgeBases(false)
  }
})

const collecting = ref(false)
const knowledgeBases = ref([])
// 🔥 新增：知识库列表分页相关
const kbListPagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  hasMore: true,
  loading: false
})
const kbSearchQuery = ref('')
const kbSearchTimer = ref(null)
const collectForm = reactive({
  knowledgeId: ''
})

// 手动上传相关
const manualUploading = ref(false)
const manualUploadFileInput = ref(null)

// AI对话相关
const chatMessages = ref([])
const chatInputMessage = ref('')
const isGeneratingChat = ref(false)
const isInputFocused = ref(false)
const chatAttachments = ref([])
const pdfFile = ref(null)
const hasPdfFile = ref(false)
const chatMessagesRef = ref(null)
const chatFileInput = ref(null)
const isChatExpanded = ref(false) // 对话区域是否展开（详情30%，对话70%）

// 计算对话的论文名字
const conversationDocumentName = computed(() => {
  // 优先显示上传的文件名
  if (chatAttachments.value.length > 0) {
    return truncateTitle(chatAttachments.value[0].name, 30)
  }
  // 如果有PDF文件，显示当前文献的标题
  if (hasPdfFile.value && literature.value?.paperInfo?.title) {
    return truncateTitle(literature.value.paperInfo.title, 30)
  }
  return ''
})

// PDF预览和下载相关
const showPdfPreview = ref(false)
const pdfPreviewUrl = ref('')
const pdfFileName = ref('')
const pdfLoading = ref(false)
const pdfDownloading = ref(false)
const cachedPdfBlob = ref(null) // 缓存的PDF Blob
const isDownloadingPdf = ref(false) // 防止重复下载的标志位

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
      
      // 自动下载PDF用于对话和预览（统一处理）
      // PDF缓存由watch监听自动处理
      if (literature.value?.accessInfo?.pdfLink) {
        downloadPdfUnified()
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

// 统一的PDF下载方法（用于预览和对话）
const downloadPdfUnified = async () => {
  if (!literature.value?.accessInfo?.pdfLink) {
    logger.warn('PDF链接不存在，无法下载')
    return false
  }

  // 如果已经有缓存的PDF，直接返回成功
  if (cachedPdfBlob.value) {
    logger.info('PDF已缓存，跳过下载')
    return true
  }

  // 防止重复下载
  if (isDownloadingPdf.value) {
    logger.info('PDF正在下载中，跳过重复请求')
    return false
  }

  isDownloadingPdf.value = true
  const pdfUrl = literature.value.accessInfo.pdfLink
  const paperId = literature.value.paperInfo?.id
  const fileName = `${literature.value.paperInfo?.title || '文献'}.pdf`

  if (!paperId) {
    logger.warn('文献ID不存在，无法下载PDF')
    ElMessage.error('文献ID不存在')
    isDownloadingPdf.value = false
    return false
  }

  try {
    const proxyUrl = `/api/proxy/pdf?id=${encodeURIComponent(paperId)}&url=${encodeURIComponent(pdfUrl)}`
    const response = await fetch(proxyUrl)
    
    if (!response.ok) {
      // 检查是否需要授权访问
      const authError = await checkRequiresAuth(response)
      if (authError) {
        showRequiresAuthMessage(authError.message)
        return false
      }
      
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
    
    if (blob.size < 10 * 1024) { // 10KB
      throw new Error('PDF文件大小过小，可能不是有效的PDF文件')
    }

    const file = new File([blob], sanitizeFileName(fileName), { type: 'application/pdf' })
    
    // 缓存PDF Blob用于预览和下载
    const pdfBlob = new Blob([blob], { type: 'application/pdf' })
    cachedPdfBlob.value = pdfBlob
    pdfFileName.value = file.name
    
    // 添加到聊天附件
    chatAttachments.value = [{
      name: file.name,
      size: formatFileSize(file.size),
      file: file,
      type: 'pdf'
    }]
    
    pdfFile.value = file
    hasPdfFile.value = true
    
    ElMessage.success('PDF文件已加载，可以开始对话或预览')
    logger.info('PDF文件下载成功并添加到聊天附件', { fileName, size: file.size })
    return true
  } catch (error) {
    logger.error('下载PDF文件失败', error)
    ElMessage.warning('PDF文件无法自动下载，您可以手动上传文件进行对话')
    return false
  } finally {
    isDownloadingPdf.value = false
  }
}

// 检查响应是否为需要授权访问的错误（包括超时和403）
const checkRequiresAuth = async (response) => {
  // 处理超时（408）和需要授权（403）的情况
  if (response.status === 403 || response.status === 408) {
    const contentType = response.headers.get('content-type') || ''
    if (contentType.includes('application/json')) {
      try {
        // 使用 clone() 避免消耗原始响应
        const clonedResponse = response.clone()
        const errorData = await clonedResponse.json()
        // 检查 requiresAuth 字段或超时状态码
        if (errorData.requiresAuth === true || response.status === 408) {
          return {
            requiresAuth: true,
            message: errorData.message || '当前连接需要认证，请求超时，请手动下载'
          }
        }
      } catch (e) {
        // JSON解析失败，继续处理
        logger.warn('解析错误响应失败', e)
      }
    }
  }
  return null
}

// 显示需要授权访问的提示
const showRequiresAuthMessage = (message) => {
  const landingPage = literature.value?.accessInfo?.landingPage
  if (landingPage) {
    ElMessage.warning({
      message: '当前文件需要认证无法预览，需要访问官网下载预览。收藏知识库两秒后自动跳转。如无法访问官网检查本地网络vpn。',
      duration: 2000, // 2秒后消失
      showClose: true
    })
    // 延迟2秒打开官网，让用户看到提示
    setTimeout(() => {
      window.open(landingPage, '_blank')
    }, 2000)
  } else {
    ElMessage.warning({
      message: '当前文件需要认证无法预览，需要访问官网下载预览。如无法访问官网检查本地网络vpn。',
      duration: 2000, // 2秒后消失
      showClose: true
    })
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

// 加载知识库列表
const loadKnowledgeBases = async (isLoadMore = false) => {
  if (kbListPagination.value.loading) {
    return
  }
  
  kbListPagination.value.loading = true
  
  try {
    if (!isLoadMore) {
      // 重置分页
      kbListPagination.value.pageNo = 1
      kbListPagination.value.hasMore = true
      knowledgeBases.value = []
    }
    
    logger.info('加载知识库列表', {
      pageNo: kbListPagination.value.pageNo,
      pageSize: kbListPagination.value.pageSize,
      keyword: kbSearchQuery.value
    })
    
    // 🔥 替换为新接口：使用分页查询接口
    const response = await getKnowledgeListPage({
      pageNo: kbListPagination.value.pageNo,
      pageSize: kbListPagination.value.pageSize,
      keyword: kbSearchQuery.value || undefined
    })
    
    if (response.code === 200 && response.data) {
      // 🔥 简化：根据接口返回格式，直接使用 response.data.records
      const list = response.data.records || []
      
      if (isLoadMore) {
        // 追加数据
        knowledgeBases.value.push(...list)
      } else {
        // 替换数据
        knowledgeBases.value = list
      }
      
      // 更新分页信息
      kbListPagination.value.total = response.data.total || 0
      kbListPagination.value.hasMore = 
        knowledgeBases.value.length < kbListPagination.value.total
      
      logger.info('知识库列表加载成功', {
        count: knowledgeBases.value.length,
        total: kbListPagination.value.total,
        hasMore: kbListPagination.value.hasMore
      })
    } else {
      logger.warn('知识库列表响应异常', { 
        code: response.code, 
        data: response.data,
        response: response
      })
      if (!isLoadMore) {
        knowledgeBases.value = []
      }
    }
  } catch (error) {
    logger.error('加载知识库列表失败', error)
    if (!isLoadMore) {
      knowledgeBases.value = []
    }
  } finally {
    kbListPagination.value.loading = false
  }
}


// 🔥 新增：获取知识库封面URL
const getKbCoverUrl = (kb) => {
  if (!kb) return null
  
  const coverUrl = kb.coverUrl
  const coverFileId = kb.coverFileId
  
  // 如果有完整的 URL（http/https），直接使用
  if (coverUrl && (coverUrl.startsWith('http://') || coverUrl.startsWith('https://'))) {
    return coverUrl
  }
  
  // 如果是相对路径（以 / 开头），直接使用
  if (coverUrl && coverUrl.startsWith('/')) {
    return coverUrl
  }
  
  // 如果有文件 ID，使用预览接口
  if (coverFileId) {
    return `/api/file/preview/${coverFileId}`
  }
  
  // 如果 coverUrl 是纯数字，可能是文件 ID
  if (coverUrl && /^\d+$/.test(String(coverUrl))) {
    return `/api/file/preview/${coverUrl}`
  }
  
  // 如果 coverUrl 是 MinIO 路径格式（bucketName/filePath），使用预览接口
  if (coverUrl && coverUrl.includes('/') && !coverUrl.startsWith('/')) {
    if (coverFileId) {
      return `/api/file/preview/${coverFileId}`
    }
  }
  
  return null
}

// 🔥 修改：处理el-select下拉框显示/隐藏事件
const handleSelectVisibleChange = (visible) => {
  if (visible) {
    // 下拉框打开时，如果列表为空，加载第一页
    if (knowledgeBases.value.length === 0) {
      kbListPagination.value.pageNo = 1
      loadKnowledgeBases(false)
    }
    
    // 监听下拉框的滚动事件和搜索输入
    nextTick(() => {
      const selectDropdown = document.querySelector('.knowledge-select-dropdown .el-scrollbar__wrap')
      if (selectDropdown) {
        selectDropdown.addEventListener('scroll', handleSelectScroll)
      }
      
      // 🔥 新增：监听 el-select 的搜索输入框
      const searchInput = document.querySelector('.knowledge-select-dropdown .el-select__input')
      if (searchInput) {
        // 移除之前的监听器（如果存在）
        searchInput.removeEventListener('input', handleSelectSearchInput)
        // 添加新的监听器
        searchInput.addEventListener('input', handleSelectSearchInput)
      }
    })
  } else {
    // 下拉框关闭时，移除滚动监听和搜索监听
    const selectDropdown = document.querySelector('.knowledge-select-dropdown .el-scrollbar__wrap')
    if (selectDropdown) {
      selectDropdown.removeEventListener('scroll', handleSelectScroll)
    }
    
    const searchInput = document.querySelector('.knowledge-select-dropdown .el-select__input')
    if (searchInput) {
      searchInput.removeEventListener('input', handleSelectSearchInput)
    }
  }
}

// 🔥 新增：处理 el-select 搜索输入（通过监听 DOM 事件）
const handleSelectSearchInput = (event) => {
  const query = event.target.value || ''
  kbSearchQuery.value = query
  
  // 防抖处理
  if (kbSearchTimer.value) {
    clearTimeout(kbSearchTimer.value)
  }
  
  kbSearchTimer.value = setTimeout(() => {
    // 重置分页并重新加载
    kbListPagination.value.pageNo = 1
    loadKnowledgeBases(false)
  }, 500) // 500ms 防抖
}

// 🔥 新增：处理el-select下拉框滚动事件（滚动加载更多）
const handleSelectScroll = (event) => {
  const target = event.target
  if (!target) return
  
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight
  
  // 距离底部50px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 50) {
    if (kbListPagination.value.hasMore && !kbListPagination.value.loading) {
      kbListPagination.value.pageNo++
      loadKnowledgeBases(true) // 传递isLoadMore=true
    }
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 预览PDF（使用缓存的PDF，如果没有则提示用户）
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

    // 如果没有缓存，检查是否正在下载
    if (isDownloadingPdf.value) {
      ElMessage.info('PDF正在加载中，请稍候...')
      pdfLoading.value = false
      return
    }

    // 如果没有缓存且没有在下载，提示用户去官网访问
    const landingPage = literature.value?.accessInfo?.landingPage
    if (landingPage) {
      ElMessage.warning({
        message: '当前文件需要认证无法预览，需要访问官网下载预览。收藏知识库两秒后自动跳转。如无法访问官网检查本地网络vpn。',
        duration: 2000 // 2秒后消失
      })
      // 延迟2秒打开官网，让用户看到提示
      setTimeout(() => {
        window.open(landingPage, '_blank')
      }, 2000)
    } else {
      ElMessage.warning({
        message: '当前文件需要认证无法预览，需要访问官网下载预览。如无法访问官网检查本地网络vpn。',
        duration: 2000 // 2秒后消失
      })
    }
    pdfLoading.value = false
  } catch (error) {
    logger.error('预览PDF失败', error)
    ElMessage.error('预览失败：' + (error.message || '未知错误'))
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
    const rawFileName = `${literature.value.paperInfo?.title || '文献'}.pdf`
    const fileName = sanitizeFileName(rawFileName)
    
    // 检查知识库中是否已存在同名文件
    try {
      const fileListResponse = await getKnowledgeFileRelationList({
        knowledgeId: selectedKb.id,
        folderId: 0, // 根目录
        page: 1,
        size: 1000 // 查询足够多的文件以检查重复
      })
      
      if (fileListResponse.code === 200 && fileListResponse.data) {
        const existingFiles = fileListResponse.data.records || []
        const duplicateFile = existingFiles.find(file => file.fileName === fileName)
        
        if (duplicateFile) {
          // 发现重复文件，询问用户是否继续
          collecting.value = false
          try {
            await ElMessageBox.confirm(
              `知识库中已存在同名文件"${fileName}"，是否继续收藏？`,
              '文件重复提示',
              {
                confirmButtonText: '继续收藏',
                cancelButtonText: '取消',
                type: 'warning'
              }
            )
            // 用户确认继续，重新设置loading状态
            collecting.value = true
          } catch {
            // 用户取消，直接返回
            return
          }
        }
      }
    } catch (error) {
      logger.warn('检查文件重复失败，继续执行收藏', error)
      // 检查失败不影响收藏流程，继续执行
    }
    
    let pdfFile = null
    
    // 优先使用缓存的PDF，如果没有则尝试下载
    if (cachedPdfBlob.value) {
      pdfFile = new File([cachedPdfBlob.value], fileName, { type: 'application/pdf' })
      logger.info('使用缓存的PDF进行收藏')
    } else {
      // 如果没有缓存，尝试下载
      ElMessage.info('正在下载PDF文件...')
      const downloadSuccess = await downloadPdfUnified()
      if (!downloadSuccess || !cachedPdfBlob.value) {
        ElMessage.error('PDF文件下载失败，无法收藏')
        collecting.value = false
        return
      }
      pdfFile = new File([cachedPdfBlob.value], fileName, { type: 'application/pdf' })
      logger.info('PDF文件下载成功，准备收藏', { fileName })
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

// 选择文件进行上传（在收藏对话框中）
const handleSelectFileForUpload = () => {
  if (!collectForm.knowledgeId) {
    ElMessage.warning('请先选择知识库')
    return
  }
  
  // 触发文件选择器
  if (manualUploadFileInput.value) {
    manualUploadFileInput.value.click()
  }
}

// 文件选择后的处理
const handleFileSelectedForUpload = async () => {
  const fileInput = manualUploadFileInput.value
  if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
    return
  }

  if (!collectForm.knowledgeId) {
    ElMessage.warning('请先选择知识库')
    return
  }

  const selectedKb = knowledgeBases.value.find(kb => kb.id === collectForm.knowledgeId)
  if (!selectedKb) {
    ElMessage.error('选择的知识库不存在')
    return
  }

  const files = Array.from(fileInput.files)
  if (files.length === 0) {
    return
  }

  // 检查知识库中是否已存在同名文件
  try {
    const fileListResponse = await getKnowledgeFileRelationList({
      knowledgeId: selectedKb.id,
      folderId: 0, // 根目录
      page: 1,
      size: 1000 // 查询足够多的文件以检查重复
    })
    
    if (fileListResponse.code === 200 && fileListResponse.data) {
      const existingFiles = fileListResponse.data.records || []
      const duplicateFiles = []
      
      // 检查每个文件是否重复
      files.forEach(file => {
        const duplicateFile = existingFiles.find(existing => existing.fileName === file.name)
        if (duplicateFile) {
          duplicateFiles.push(file.name)
        }
      })
      
      if (duplicateFiles.length > 0) {
        // 发现重复文件，询问用户是否继续
        try {
          await ElMessageBox.confirm(
            `知识库中已存在以下同名文件：\n${duplicateFiles.join('\n')}\n\n是否继续上传？`,
            '文件重复提示',
            {
              confirmButtonText: '继续上传',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          // 用户确认继续
        } catch {
          // 用户取消，清空文件选择并返回
          if (fileInput) {
            fileInput.value = ''
          }
          return
        }
      }
    }
  } catch (error) {
    logger.warn('检查文件重复失败，继续执行上传', error)
    // 检查失败不影响上传流程，继续执行
  }

  manualUploading.value = true
  try {
    ElMessage.info(`正在上传 ${files.length} 个文件到知识库...`)
    
    // 调用批量上传接口
    const response = await uploadFilesToKnowledge(
      selectedKb.id, // 使用知识库的数据库主键ID
      files, // 文件数组
      0 // folderId，默认根目录
    )
    
    if (response.code === 200) {
      ElMessage.success(`成功上传 ${files.length} 个文件到知识库`)
      showCollectDialog.value = false
      collectForm.knowledgeId = ''
      // 清空文件选择
      if (fileInput) {
        fileInput.value = ''
      }
    } else {
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error) {
    logger.error('手动上传文件到知识库失败', error)
    ElMessage.error('上传失败：' + (error.message || '未知错误'))
  } finally {
    manualUploading.value = false
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
const truncateTitle = (title, maxLength = 50) => {
  if (!title) return ''
  if (title.length <= maxLength) return title
  return title.substring(0, maxLength) + '...'
}

// AI对话相关方法
const toggleChatLayout = () => {
  isChatExpanded.value = !isChatExpanded.value
}

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

  if (file.size < 10 * 1024) {
    ElMessage.warning('文件大小过小，文件大小不得小于10KB')
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

// 格式化消息内容 - 使用 marked + DOMPurify 专业 Markdown 渲染
const formatMessageContent = (content) => {
  if (!content || typeof content !== 'string') {
    return ''
  }
  
  try {
    // 使用 marked 将 Markdown 转换为 HTML
    const rawHtml = marked.parse(content)
    
    // 使用 DOMPurify 清理 HTML，防止 XSS 攻击
    const cleanHtml = DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: [
        'p', 'br', 'strong', 'em', 'u', 's', 'del', 'code', 'pre',
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'ul', 'ol', 'li',
        'table', 'thead', 'tbody', 'tr', 'th', 'td',
        'blockquote', 'hr', 'a', 'img'
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class'],
      ALLOW_DATA_ATTR: false
    })
    
    // 为 HTML 元素添加自定义类名，以便应用样式
    // 先临时标记代码块，避免影响行内代码处理
    const codeBlockPlaceholder = '___CODE_BLOCK_PLACEHOLDER___'
    const codeBlocks = []
    let styledHtml = cleanHtml.replace(/<pre><code>([\s\S]*?)<\/code><\/pre>/g, (match, code) => {
      const placeholder = `${codeBlockPlaceholder}${codeBlocks.length}`
      codeBlocks.push(match.replace(/<pre><code>/, '<pre class="md-code-block"><code>'))
      return placeholder
    })
    
    // 处理其他元素
    styledHtml = styledHtml
      // 表格包装和类名
      .replace(/<table>/g, '<div class="markdown-table-wrapper"><table class="markdown-table">')
      .replace(/<\/table>/g, '</table></div>')
      .replace(/<th>/g, '<th class="table-header">')
      .replace(/<td>/g, '<td class="table-cell">')
      // 标题类名
      .replace(/<h1>/g, '<h1 class="md-heading h1">')
      .replace(/<h2>/g, '<h2 class="md-heading h2">')
      .replace(/<h3>/g, '<h3 class="md-heading h3">')
      // 行内代码（此时代码块已被替换为占位符）
      .replace(/<code>/g, '<code class="md-inline-code">')
      // 文本格式
      .replace(/<strong>/g, '<strong class="md-bold">')
      .replace(/<em>/g, '<em class="md-italic">')
      .replace(/<del>/g, '<del class="md-strikethrough">')
      // 列表
      .replace(/<ul>/g, '<ul class="md-list">')
      .replace(/<li>/g, '<li class="md-list-item">')
      // 分隔线
      .replace(/<hr>/g, '<hr class="md-divider">')
    
    // 恢复代码块
    codeBlocks.forEach((codeBlock, index) => {
      styledHtml = styledHtml.replace(`${codeBlockPlaceholder}${index}`, codeBlock)
    })
    
    // 为表格行添加交替行类名
    styledHtml = styledHtml.replace(/<tbody>([\s\S]*?)<\/tbody>/g, (match, tbodyContent) => {
      const rows = tbodyContent.match(/<tr>[\s\S]*?<\/tr>/g) || []
      return '<tbody>' + rows.map((row, index) => 
        row.replace(/<tr>/, `<tr class="${index % 2 === 0 ? 'odd-row' : 'even-row'}">`)
      ).join('') + '</tbody>'
    })
    
    return styledHtml
  } catch (error) {
    logger.error('Markdown 渲染错误', error)
    // 失败时回退到基础处理
    return content
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
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

  // 检查是否有文件（PDF或附件）
  if (!hasPdfFile.value && chatAttachments.value.length === 0) {
    // 添加用户消息
    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: message,
      timestamp: new Date()
    }
    chatMessages.value.push(userMessage)

    // 添加AI提示消息
    const aiMessageId = Date.now() + 1
    const aiMessage = {
      id: aiMessageId,
      type: 'ai',
      content: '你好，我是小域～\n\n不过你提到的"这篇文献"我还没看到具体内容哦！当前文献没有PDF文件，请手动下载文献PDF后，点击上方📎按钮上传文件，这样我才能帮你准确分析文献内容～\n\n等你上传文件后，我立马为你梳理重点、提炼核心观点，保证清晰易懂！📚✨',
      isGenerating: false,
      timestamp: new Date()
    }
    chatMessages.value.push(aiMessage)

    // 清空输入
    chatInputMessage.value = ''

    // 滚动到底部
    await nextTick()
    scrollChatToBottom()
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

// 监听PDF链接和文献ID变化，自动调用PDF缓存
watch(() => ({
  pdfLink: literature.value?.accessInfo?.pdfLink,
  paperId: literature.value?.paperInfo?.id
}), ({ pdfLink, paperId }) => {
  if (pdfLink && paperId) {
    logger.info('检测到PDF链接，开始请求代理缓存服务', { pdfLink, paperId })
    
    // 异步请求代理缓存，不阻塞页面加载
    cachePdf(pdfLink, paperId).then(response => {
      if (response.code === 200 && response.data) {
        logger.info('PDF代理缓存成功', { 
          id: response.data.id, 
          presignedUrl: response.data.presignedUrl 
        })
      } else {
        logger.warn('PDF代理缓存失败', { message: response.message })
      }
    }).catch(error => {
      logger.error('PDF代理缓存请求失败', error)
    })
  }
}, { immediate: true, deep: true }) // immediate: true 确保在组件挂载时立即执行一次

// 组件卸载时清理blob URL
onUnmounted(() => {
  if (pdfPreviewUrl.value && pdfPreviewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(pdfPreviewUrl.value)
  }
})

onMounted(() => {
  loadLiteratureDetail()
  // 🔥 修复：不在页面加载时调用知识库接口，改为点击收藏按钮时调用
  // loadKnowledgeBases()
})
</script>

<style scoped lang="scss">
.literature-detail-container {
  padding: 0;
  height: calc(100vh - 60px);
  background: #ffffff; // 雪白色背景
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .main-content-wrapper {
    display: flex;
    flex: 1;
    overflow: hidden;
    min-height: 0; // 确保 flex 子元素可以收缩
    transition: all 0.3s ease; // 添加过渡动画
  }

  .main-content {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 20px;
    background: #ffffff; // 雪白色背景
    min-height: 0; // 确保可以滚动
    max-height: 100%; // 限制最大高度，确保可以滚动
    transition: flex 0.3s ease, width 0.3s ease; // 添加过渡动画
    width: 100%; // 默认占满剩余空间
    height: 100%; // 确保高度填满父容器
    scroll-behavior: smooth; // 平滑滚动
    
    // 美化滚动条（Webkit浏览器）
    &::-webkit-scrollbar {
      width: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 4px;
      transition: background 0.2s ease;
      
      &:hover {
        background: #a8a8a8;
      }
    }
    
    // Firefox 滚动条样式
    scrollbar-width: thin;
    scrollbar-color: #c1c1c1 #f1f1f1;

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

    .detail-content {
      width: 100%;
      max-width: 100%;
      box-sizing: border-box;
      word-wrap: break-word;
      word-break: break-word;
      overflow-wrap: break-word;
    }

    .literature-info-card {
      width: 100%;
      max-width: 100%;
      box-sizing: border-box;
      
      // 移除卡片边框
      :deep(.base-card) {
        border: none !important;
        box-shadow: none !important;
      }
      
      // 确保卡片内容自适应和文字换行
      :deep(.base-card__content) {
        word-wrap: break-word;
        word-break: break-word;
        overflow-wrap: break-word;
        border: none !important;
      }
      
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
        // 移除分隔线

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
        // 移除分隔线

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
        // 移除分隔线

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
        // 移除分隔线
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
          // 移除边框
          border-radius: 8px;
          overflow: hidden;
          background: #ffffff; // 雪白色背景

          .links-table-row {
            display: flex;
            align-items: center;
            padding: 12px 16px;
            // 移除分隔线
            transition: background-color 0.2s;

            &:hover {
              background-color: #f8f8f8; // 轻微灰色，提供悬停反馈
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
        // 移除分隔线

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
        // 移除分隔线

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
            background: #ffffff; // 雪白色背景
            color: #4b5563;
            // 移除边框
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

  // 当对话展开时，详情区域自适应，最大30%
  .main-content-wrapper.chat-expanded {
    .main-content {
      flex: 0 1 auto; // 允许收缩，根据内容自适应
      max-width: 30%; // 最大不超过30%
      min-width: 300px; // 最小宽度保证内容可读性
      // 注意：不覆盖 padding，保持默认的 20px
    }
  }

  // 右侧AI对话侧边栏
  .chat-sidebar {
    width: 420px;
    background: #ffffff; // 雪白色背景
    // 移除边框
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    min-height: 0; // 确保可以收缩
    overflow: hidden;
    transition: width 0.3s ease, flex 0.3s ease; // 添加过渡动画

    // 展开状态：占70%
    &.expanded {
      width: auto;
      flex: 0 0 70%;
    }

      .chat-sidebar-content {
        display: flex;
        flex-direction: column;
        height: 100%;
        overflow: hidden;
        min-height: 0; // 确保可以收缩
        background: #ffffff; // 雪白色背景

      .chat-header {
        display: flex;
        align-items: center;
        justify-content: flex-end; // 按钮靠右显示
        padding: 16px 20px;
        background: #ffffff; // 雪白色背景
        flex-shrink: 0;

        .layout-toggle-btn {
          padding: 6px;
          color: #6b7280;
          border-radius: 50%; // 圆形按钮
          width: 32px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.2s ease;
          background: transparent;

          &:hover {
            color: #3b82f6;
            background: #f8f8f8; // 悬停背景
          }

          .el-icon {
            font-size: 16px;
          }
        }
      }

      .chat-greeting {
        padding: 32px 20px 24px;
        text-align: center;
        flex-shrink: 0; // 固定高度，不收缩

        h2 {
          font-size: 28px; // 更大字号
          font-weight: 700; // 更粗
          color: #111827;
          margin: 0;
          letter-spacing: -0.5px; // 紧凑字距
        }
      }

      .document-card {
        margin: 0 20px 24px;
        padding: 20px;
        background: #f8f8f8; // 浅灰背景，类似图片
        border-radius: 16px; // 更大的圆角
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04); // 轻微阴影，提升层次感
        flex-shrink: 0; // 固定高度，不收缩
        transition: box-shadow 0.2s ease;

        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06); // 悬停时阴影加深
        }

        .document-card-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 12px;
          padding: 10px 12px;
          background: #ffffff; // 内部小卡片白色背景
          border-radius: 10px; // 圆角

          .el-icon {
            font-size: 18px;
            color: #3b82f6; // 蓝色图标
          }

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
          font-size: 14px;
          font-weight: 500;
          color: #3b82f6; // 蓝色标签，类似图片
          margin-bottom: 8px;
        }

        .document-card-hint {
          font-size: 13px;
          color: #6b7280;
          line-height: 1.5;
        }
      }

      .chat-attachments {
        margin: 0 20px 16px;
        padding: 12px;
        background: #f8f8f8; // 浅灰背景
        border-radius: 12px; // 更大的圆角
        flex-shrink: 0; // 固定高度，不收缩

        .chat-attachment-item {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 10px 12px;
          background: #ffffff; // 白色背景
          border-radius: 8px; // 圆角
          margin-bottom: 8px;
          transition: all 0.2s;

          &:last-child {
            margin-bottom: 0;
          }

          &:hover {
            background: #f5f5f5; // 悬停效果
          }

          .el-icon {
            color: #3b82f6; // 蓝色图标
          }

          .attachment-name {
            flex: 1;
            font-size: 13px;
            color: #4b5563;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .remove-icon {
            cursor: pointer;
            color: #9ca3af;
            transition: color 0.2s;

            &:hover {
              color: #ef4444;
            }
          }
        }
      }

      .suggested-actions {
        margin: 0 20px 20px;
        display: flex;
        flex-direction: column;
        gap: 0; // 无间距，用分隔线
        flex-shrink: 0; // 固定高度，不收缩
        background: #ffffff; // 白色背景
        border-radius: 12px; // 圆角容器
        overflow: hidden; // 确保圆角生效

        .action-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 16px;
          background: #ffffff; // 雪白色背景
          cursor: pointer;
          transition: all 0.2s;
          font-size: 14px;
          color: #4b5563;
          border-bottom: 1px solid #f0f0f0; // 细线分隔，类似图片

          &:last-child {
            border-bottom: none; // 最后一项无分隔线
          }

          .el-icon {
            font-size: 18px;
            color: #6b7280; // 图标颜色
            transition: color 0.2s;
          }

          &:hover {
            background: #f8f8f8; // 轻微灰色，提供悬停反馈
            
            .el-icon {
              color: #3b82f6; // 悬停时图标变蓝
            }
          }
        }
      }

      .chat-messages {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        padding: 20px; // 正常的内边距
        display: flex;
        flex-direction: column;
        gap: 20px; // 更大的间距
        // 确保消息从上到下排列，新消息追加到底部
        justify-content: flex-start;
        align-items: stretch;
        min-height: 0; // 确保可以滚动
        background: #ffffff; // 雪白色背景

        .empty-chat-message {
          text-align: center;
          color: #9ca3af;
          padding: 40px 20px;

          p {
            font-size: 15px;
            color: #6b7280;
            margin: 0;
          }

          .no-file-tip {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;

            .tip-icon {
              font-size: 48px;
              color: #d1d5db;
              margin-bottom: 20px;
            }

            .tip-title {
              font-size: 16px;
              font-weight: 600;
              color: #374151;
              margin: 0 0 12px 0;
            }

            .tip-content {
              font-size: 14px;
              color: #6b7280;
              margin: 0;
              line-height: 1.6;
              max-width: 300px;
            }
          }
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
                border-radius: 16px 16px 4px 16px; // 更大的圆角，右侧更圆
                max-width: 80%;
                word-wrap: break-word;
                box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2); // 轻微阴影
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
                  width: 36px; // 稍大一些
                  height: 36px;
                  border-radius: 50%;
                  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                  color: #fff;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-weight: 600;
                  font-size: 13px;
                  flex-shrink: 0;
                  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3); // 轻微阴影
                }

                .ai-content {
                  background: #f8f8f8; // 浅灰背景，类似图片
                  padding: 12px 16px;
                  border-radius: 16px 16px 16px 4px; // 更大的圆角，左侧更圆
                  flex: 1;
                  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); // 轻微阴影

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
                    line-height: 1.7;
                    word-wrap: break-word;
                    word-break: break-word;
                    font-size: 14px; // 确保字体大小一致

                    // Markdown 表格样式
                    :deep(.markdown-table-wrapper) {
                      margin: 16px 0;
                      overflow-x: auto;
                      border-radius: 8px;
                      background: #ffffff; // 雪白色背景
                      // 移除边框和阴影
                    }

                    :deep(.markdown-table) {
                      width: 100%;
                      border-collapse: collapse;
                      font-size: 14px;
                      min-width: 100%;
                    }

                    :deep(.table-header) {
                      padding: 12px 16px;
                      text-align: left;
                      background: #ffffff; // 雪白色背景
                      color: #1e40af;
                      font-weight: 600;
                      font-size: 13px;
                      // 移除边框
                      white-space: nowrap;
                    }

                    :deep(.table-cell) {
                      padding: 12px 16px;
                      text-align: left;
                      background: #ffffff; // 雪白色背景
                      color: #374151;
                      font-size: 14px;
                      line-height: 1.6;
                      // 移除边框
                      transition: background-color 0.2s ease;
                    }

                    :deep(.even-row .table-cell) {
                      background: #ffffff; // 雪白色背景
                    }

                    :deep(.odd-row .table-cell:hover) {
                      background-color: #f8f8f8; // 轻微灰色，提供悬停反馈
                    }

                    // Markdown 标题样式
                    :deep(.md-heading) {
                      font-weight: 700;
                      margin: 18px 0 12px;
                      border-radius: 6px;
                      padding: 12px 16px;
                    }

                    :deep(.md-heading.h1) {
                      font-size: 24px;
                      color: #1e3a8a;
                      background: transparent; // 移除背景
                      // 移除边框
                    }

                    :deep(.md-heading.h2) {
                      font-size: 20px;
                      color: #1e40af;
                      background: transparent; // 移除背景
                      // 移除边框
                      padding-bottom: 8px;
                    }

                    :deep(.md-heading.h3) {
                      font-size: 18px;
                      color: #374151;
                      background: transparent; // 移除背景
                      // 移除边框
                    }

                    // Markdown 文本格式
                    :deep(.md-bold) {
                      font-weight: 700;
                      color: #1f2937;
                      background: transparent; // 保持雪白色主题
                      padding: 2px 4px;
                      border-radius: 4px;
                    }

                    :deep(.md-italic) {
                      font-style: italic;
                      color: #6b7280;
                      background: transparent; // 去掉背景色
                      padding: 1px 3px;
                      border-radius: 3px;
                    }

                    :deep(.md-strikethrough) {
                      text-decoration: line-through;
                      color: #9ca3af;
                      background: transparent; // 去掉背景色
                      padding: 1px 3px;
                      border-radius: 3px;
                    }

                    // Markdown 代码块
                    :deep(.md-code-block) {
                      background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
                      color: #38bdf8;
                      padding: 16px;
                      border-radius: 8px;
                      font-family: 'JetBrains Mono', Consolas, 'Courier New', monospace;
                      font-size: 13px;
                      line-height: 1.6;
                      margin: 12px 0;
                      border-left: 4px solid #f97316;
                      overflow-x: auto;
                      display: block;
                    }

                    :deep(.md-code-block code) {
                      background: transparent;
                      padding: 0;
                      border-radius: 0;
                      color: inherit;
                    }

                    :deep(.md-inline-code) {
                      background: #f8f8f8; // 轻微灰色，用于区分代码
                      padding: 3px 6px;
                      border-radius: 4px;
                      font-family: 'Monaco', 'Courier New', monospace;
                      font-size: 12px;
                      color: #d6336c;
                      // 移除边框
                      margin: 0 2px;
                    }

                    // Markdown 列表
                    :deep(.md-list) {
                      margin: 12px 0;
                      padding-left: 24px;
                      list-style-type: disc;
                    }

                    :deep(.md-list-item) {
                      margin: 6px 0;
                      line-height: 1.6;
                    }

                    // Markdown 分隔线
                    :deep(.md-divider) {
                      display: none; // 隐藏分隔线
                    }
                  }
                }
              }
            }
          }
        }
      }

      .chat-input-area {
        padding: 0;
        margin-left: auto; // 自动左边距，用于居中
        margin-right: auto; // 自动右边距，用于居中
        width: 94%; // 宽度占94%
        background: #ffffff; // 雪白色背景
        flex-shrink: 0; // 输入区域固定，不收缩
        border-radius: 12px; // 整体圆角
        border: 1px solid #e5e7eb; // 添加边框线
        overflow: hidden; // 确保圆角生效
        position: relative; // 确保定位正确
        z-index: 1; // 确保不被覆盖

        // 会话状态提示
        .conversation-status {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px 16px;
          margin: 12px 12px 0; // 添加外边距
          background: #f2f2f2; // 浅灰色背景
          border-radius: 8px; // 整体圆角
          font-size: 13px;
          color: #555555; // 深灰色文字

          .collapse-icon {
            font-size: 14px;
            color: #666666;
            cursor: pointer;
            transition: transform 0.2s;

            &:hover {
              color: #333333;
            }
          }
        }

        // 输入框容器
        .input-container {
          padding: 12px; // 减少内边距，降低整体高度
          background: #ffffff;
          
          // 默认情况下（没有状态提示），所有角都是圆角
          border-radius: 12px;

          .input-wrapper {
            position: relative;

            .chat-textarea {
              :deep(.el-textarea__inner) {
                resize: none;
                border-radius: 12px;
                border: none !important; // 无边框，使用 !important 确保覆盖默认样式
                padding: 10px 50px 10px 16px; // 减少上下内边距，右侧留出按钮空间
                font-size: 14px;
                line-height: 1.5;
                background: #ffffff;
                min-height: 60px; // 减少最小高度
                max-height: 200px; // 最大高度，超出后显示滚动条
                box-shadow: none;
                overflow-y: auto; // 允许垂直滚动

                // 自定义滚动条样式
                &::-webkit-scrollbar {
                  width: 6px;
                }

                &::-webkit-scrollbar-track {
                  background: #f5f5f5;
                  border-radius: 3px;
                }

                &::-webkit-scrollbar-thumb {
                  background: #d9d9d9;
                  border-radius: 3px;

                  &:hover {
                    background: #bfbfbf;
                  }
                }

                &:focus {
                  border: none;
                  box-shadow: none;
                  outline: none;
                }

                // 隐藏默认占位符
                &::placeholder {
                  color: transparent;
                }
              }
            }

            // 自定义占位符
            .custom-placeholder {
              position: absolute;
              top: 12px;
              left: 16px;
              pointer-events: none;
              display: flex;
              flex-direction: column;
              gap: 4px;
              z-index: 1;

              .placeholder-label {
                font-size: 14px;
                color: #1890ff; // 蓝色"文献对话"
                font-weight: 500;
              }

              .placeholder-hint {
                font-size: 14px;
                color: #999999; // 浅灰色提示文字
                line-height: 1.5;
              }
            }

            // 操作按钮
            .input-actions {
              position: absolute;
              bottom: 12px;
              right: 12px;
              display: flex;
              gap: 8px;
              z-index: 10;

              .attach-btn {
                width: 36px;
                height: 36px;
                padding: 0;
                border-radius: 50%;
                background: #ffffff;
                border: 1px solid #d9d9d9; // 浅灰色边框
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.2s;

                .el-icon {
                  font-size: 18px;
                  color: #000000; // 黑色加号
                }

                &:hover {
                  background: #f8f8f8;
                  border-color: #bfbfbf;
                }

                &:disabled {
                  opacity: 0.5;
                  cursor: not-allowed;
                }
              }

              .send-btn {
                width: 36px;
                height: 36px;
                padding: 0;
                border-radius: 50%;
                background: #e8e8e8; // 浅灰色背景
                border: none;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.2s;

                .el-icon {
                  font-size: 18px;
                  color: #666666; // 深灰色箭头
                }

                &:hover:not(.is-disabled) {
                  background: #d9d9d9;
                }

                &.is-disabled {
                  opacity: 0.5;
                  cursor: not-allowed;
                }

                // 当有内容且未禁用时，稍微加深背景
                &:not(.is-disabled) {
                  background: #e8e8e8;
                  
                  .el-icon {
                    color: #666666;
                  }
                }
              }
            }
          }
        }

        // 当有会话状态提示时，输入框容器只显示底部圆角
        &.has-status .input-container {
          border-radius: 0 0 12px 12px;
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
    background: #fafafa; // 统一背景色

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
// 🔥 新增：知识库选择下拉框样式（全局样式，不使用scoped）
</style>

<style lang="scss">
// 知识库选择下拉框样式（全局样式，因为el-select的popper-class在body下）
.knowledge-select-dropdown {
  .kb-select-option {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .kb-select-icon {
      width: 24px;
      height: 24px;
      min-width: 24px;
      min-height: 24px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      flex-shrink: 0;
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
      
      .kb-cover-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .kb-select-icon-default {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .el-icon {
          font-size: 14px;
          color: var(--color-primary, #3b82f6);
        }
      }
    }
    
    .kb-select-name {
      flex: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .kb-loading-more,
  .kb-no-more {
    text-align: center;
    padding: 8px;
    color: var(--text-3, #9ca3af);
    font-size: 12px;
  }
}

// 🔥 新增：搜索框样式（复用KnowledgeList.vue的样式）
.search-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  
  .search-left-icon {
    position: absolute;
    left: 12px;
    z-index: 1;
    color: var(--text-3, #9ca3af);
    display: flex;
    align-items: center;
  }
  
  .form-input {
    width: 100%;
    padding: 10px 40px 10px 36px;
    border: 1px solid var(--border, #e5e7eb);
    border-radius: 8px;
    font-size: 14px;
    background: var(--surface, #ffffff);
    color: var(--text, #111827);
    transition: all 0.2s ease;
    
    &:focus {
      border-color: var(--color-primary, #3b82f6);
      box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
      outline: none;
    }
  }
}
</style>


