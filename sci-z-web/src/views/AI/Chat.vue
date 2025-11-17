<!--
/**
 * @description AI对话页面
 * 与AI助手进行对话交互，支持知识库增强对话
 */
-->
<template>
  <div class="ai-chat-container">
    <!-- 左侧对话列表 -->
    <div class="chat-list-sidebar">
      <div class="chat-list-header">
        <button class="new-chat-btn" @click="createNewChat">
          <el-icon><Plus /></el-icon>
          {{ $t('ai.chat.newChat') }}
        </button>
  </div>
      <div class="chat-list">
        <div
          v-for="chat in chats"
          :key="chat.id"
          class="chat-item"
          :class="{ 
            active: currentChat && currentChat.id === chat.id,
            pinned: chat.pinned 
          }"
          @click="selectChat(chat)"
        >
          <div class="chat-item-content">
            <div class="chat-title">
              <el-icon v-if="chat.pinned" class="pin-icon"><StarFilled /></el-icon>
              {{ chat.title || $t('ai.chat.newChat') }}
            </div>
            <div class="chat-preview">{{ chat.lastMessage || '' }}</div>
            <div class="chat-meta">
              <span class="chat-time">{{ formatTime(chat.updatedAt) }}</span>
            </div>
          </div>

          <div class="chat-item-actions" @click.stop>
            <el-dropdown @command="(cmd) => handleChatAction(cmd, chat)" trigger="click">
              <button class="chat-more-btn">
                <el-icon><MoreFilled /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    {{ $t('common.edit') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="pin">
                    <el-icon><Star /></el-icon>
                    {{ chat.pinned ? $t('ai.chat.unpin') : $t('ai.chat.pin') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    {{ $t('common.delete') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧对话内容 -->
    <div class="chat-content">
      <div v-if="!currentChat" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-text">{{ $t('ai.chat.startNewChat') }}</div>
        <div class="empty-desc">{{ $t('ai.chat.selectOrCreateChat') }}</div>
      </div>

      <template v-else>
        <!-- 对话头部 -->
        <div class="chat-header">
          <div class="chat-title-header">{{ currentChat.title || $t('ai.chat.newChat') }}</div>
        </div>

        <!-- 对话区 -->
        <div
          class="kb-messages-container"
          :class="{ 'has-messages': messages.length > 0 }"
          ref="kbMessages"
        >
          <!-- 空状态显示 -->
          <div v-if="messages.length === 0" class="empty-chat-message">
            Hi，任何问题都可以问我
          </div>

          <!-- 消息列表 -->
          <template v-if="messages.length > 0">
            <div class="kb-messages">
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="kb-message"
                :class="msg.type"
              >
                <div class="kb-message-avatar" :class="msg.type">
                  {{ msg.type === 'user' ? '我' : 'AI' }}
                </div>
                <div class="kb-message-bubble">
                  <div class="kb-message-content-wrapper">
                    <div
                      class="kb-message-content"
                      v-html="formatKbContent(msg.content)"
                    ></div>
                    <!-- 文档片段展示 -->
                    <div v-if="msg.documents && msg.documents.length > 0" class="kb-message-documents">
                      <div class="kb-documents-title">参考文档：</div>
                      <div class="kb-documents-list">
                        <div
                          v-for="(doc, index) in msg.documents"
                          :key="doc.id || index"
                          class="kb-document-item"
                          :title="doc.content ? doc.content.substring(0, 100) : ''"
                        >
                          <el-icon class="kb-document-icon"><Document /></el-icon>
                          <div class="kb-document-info">
                            <span class="kb-document-name">{{ doc.name || `文档 ${index + 1}` }}</span>
                            <span v-if="doc.datasetName && doc.datasetName !== doc.name" class="kb-document-dataset">（{{ doc.datasetName }}）</span>
                          </div>
                          <span v-if="doc.score" class="kb-document-score">相关度: {{ (doc.score * 100).toFixed(0) }}%</span>
                        </div>
                      </div>
                    </div>
                    <!-- 流式生成指示器 -->
                    <div v-if="msg.streaming" class="kb-streaming-indicator">
                      <span class="kb-streaming-dot"></span>
                      <span class="kb-streaming-text">正在生成...</span>
                    </div>
                  </div>
                  <div class="kb-message-meta">
                    <div class="kb-message-actions">
                      <!-- AI消息的复制和重试按钮 -->
                      <template v-if="msg.type === 'ai'">
                        <button
                          class="kb-copy-btn"
                          @click="copyKbMessage(msg.content)"
                        >
                          <el-icon><DocumentCopy /></el-icon>
                          <span>复制</span>
                        </button>
                        <button
                          class="kb-retry-btn"
                          @click="retryKbMessage(msg)"
                        >
                          <el-icon><Refresh /></el-icon>
                          <span>重试</span>
                        </button>
                      </template>

                      <!-- 用户消息的复制按钮 -->
                      <template v-if="msg.type === 'user'">
                        <button
                          class="kb-copy-btn"
                          @click="copyKbMessage(msg.content)"
                        >
                          <el-icon><DocumentCopy /></el-icon>
                          <span>复制</span>
                        </button>
                      </template>
                    </div>
                    <span>{{ formatTime(msg.timestamp) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 输入区域 -->
        <div class="kb-input-area">
          <div class="kb-input-container">
            <!-- 附件预览区域 -->
            <div v-if="attachments.length > 0" class="attachment-preview">
              <div
                v-for="(attachment, index) in attachments"
                :key="index"
                class="attachment-item"
              >
                <span class="attachment-icon">{{ getFileIcon(attachment.type) }}</span>
                <div class="attachment-name">{{ attachment.name }}</div>
                <div class="attachment-size">{{ attachment.size }}</div>
                <div
                  class="attachment-remove"
                  @click="removeAttachment(index)"
                >
                  <el-icon><Close /></el-icon>
                </div>
              </div>
            </div>

            <textarea
              v-model="inputMessage"
              class="kb-message-input"
              placeholder="@知识库或直接提问"
              @keydown.enter.prevent="handleEnterKey"
              @keydown.up.prevent="navigateKbList('up')"
              @keydown.down.prevent="navigateKbList('down')"
              @keydown.escape="hideKnowledgeBaseList"
              @keydown.backspace="handleBackspace"
              @input="handleInputChange"
              ref="messageInput"
            ></textarea>

            <!-- 知识库选择下拉框 -->
            <div
              v-if="showKnowledgeBaseList"
              class="knowledge-base-dropdown"
            >
              <div class="kb-dropdown-header">
                <span class="kb-dropdown-title">共享知识库</span>
              </div>
              <div class="kb-dropdown-section">
                <div class="kb-list">
                  <div
                    v-for="(kb, index) in filteredKnowledgeBases"
                    :key="kb.id"
                    class="kb-item"
                    :class="{ selected: selectedKbIndex === index }"
                    @click="selectKnowledgeBase(kb)"
                  >
                    <div class="kb-icon">{{ getKbIcon(kb) }}</div>
                    <div class="kb-name">{{ kb.name }}</div>
                    <div v-if="isKbSelected(kb.id)" class="kb-selected-mark">✓</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部控制栏 -->
            <div class="kb-input-bottom-bar">
              <!-- 模型选择 -->
              <div
                class="custom-model-selector"
                @click.stop="toggleModelDropdown"
              >
                <div class="model-display">
                  {{ getSelectedModelName(selectedModel) }}
                  <el-icon><ArrowDown /></el-icon>
                </div>
                <div v-if="showModelDropdown" class="model-dropdown">
                  <div
                    v-for="model in modelOptions"
                    :key="model.value"
                    class="model-option"
                    :class="{ active: selectedModel === model.value }"
                    @click.stop="selectModel(model.value)"
                  >
                    <div class="model-name">{{ model.name }}</div>
                    <div class="model-desc">{{ model.description }}</div>
                    <div
                      v-if="selectedModel === model.value"
                      class="check-icon"
                    >
                      ✓
                    </div>
                  </div>
                </div>
              </div>

              <div class="kb-right-controls">
                <!-- 附件按钮 -->
                <button
                  class="kb-attachment-btn"
                  :class="{ disabled: hasKnowledgeBaseSelected }"
                  @click="handleAttachmentClick"
                  :title="hasKnowledgeBaseSelected ? '知识库模式下不支持附件' : '添加附件'"
                >
                  <el-icon><Paperclip /></el-icon>
                </button>

                <!-- 隐藏的文件输入 -->
                <input
                  ref="fileInput"
                  type="file"
                  multiple
                  accept=".pdf,.doc,.docx,.xlsx,.ppt,.pptx,.txt,.md,.jpg,.jpeg,.png"
                  style="display: none"
                  @change="handleFileUpload"
                />

                <!-- 发送/停止按钮 -->
                <button
                  v-if="!isGenerating"
                  class="kb-send-btn"
                  :class="{ active: inputMessage.trim() || selectedKnowledgeBases.length > 0 }"
                  @click="sendKbMessage"
                >
                  <el-icon><ArrowUp /></el-icon>
                </button>

                <!-- 停止按钮 -->
                <button
                  v-if="isGenerating"
                  class="kb-stop-btn"
                  @click="stopGeneration"
                >
                  <el-icon><Close /></el-icon>
                </button>
              </div>
            </div>
          </div>
          <div class="input-footer">内容由AI生成仅供参考</div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  MoreFilled,
  StarFilled,
  Star,
  Edit,
  Delete,
  Document,
  DocumentCopy,
  Refresh,
  Close,
  ArrowDown,
  ArrowUp,
  Paperclip
} from '@element-plus/icons-vue'
import {
  getKnowledgeList
} from '@/api/Knowledge/knowledge'
import {
  streamKnowledgeChatbot
} from '@/api/Knowledge/knowledge'
import {
  // 新接口
  createAiConversation,
  updateAiConversation,
  deleteAiConversation,
  pageAiConversations,
  listAiConversations,
  listAiMessages,
  createAiMessage,
  updateAiConversationPinnedStatus,
  // 旧接口（保留兼容）
  getConversations,
  createConversation,
  updateConversation,
  deleteConversation,
  getConversationMessages
} from '@/api/AI/ai'
import { createLogger } from '@/utils/simpleLogger'

const { t } = useI18n()
const logger = createLogger('AIChat')

// 响应式数据
const chats = ref([])
const currentChat = ref(null)
const messages = ref([])
const inputMessage = ref('')
const kbMessages = ref(null)
const messageInput = ref(null) // 添加缺失的 messageInput ref
const isGenerating = ref(false)
const currentAbortController = ref(null)
const currentConversationId = ref(null)

// 对话列表相关
const contextMenu = ref({ chat: null, x: 0, y: 0 })

// 知识库选择功能
const showKnowledgeBaseList = ref(false)
const selectedKnowledgeBases = ref([])
const selectedKbIndex = ref(-1)
const knowledgeBaseList = ref([])
const kbSearchQuery = ref('')

// 附件功能
const attachments = ref([])
const fileInput = ref(null)

// 模型选择
const showModelDropdown = ref(false)
const selectedModel = ref('qwen3-max')
const modelOptions = ref([
  {
    value: 'qwen3-max',
    name: 'Qwen3-Max',
    description: '通义千问最新模型，擅长中文理解和生成'
  },
  {
    value: 'deepseek-v3.1',
    name: 'Deepseek-V3.1',
    description: '深度求索V3.1，强大的代码和推理能力'
  },
  {
    value: 'deepseek-r1',
    name: 'Deepseek-R1',
    description: '深度求索R1，强化学习模型'
  }
])

// 计算属性
const filteredKnowledgeBases = computed(() => {
  if (!kbSearchQuery.value.trim()) return knowledgeBaseList.value
  return knowledgeBaseList.value.filter((kb) =>
    kb.name.toLowerCase().includes(kbSearchQuery.value.toLowerCase())
  )
})

const hasKnowledgeBaseSelected = computed(() => {
  return selectedKnowledgeBases.value.length > 0
})

// 方法
const loadChats = async () => {
  try {
    logger.info('加载对话列表')
    // 使用新接口：分页查询会话列表
    const response = await pageAiConversations({ pageNo: 1, pageSize: 100 })
    if (response.code === 200 && response.data) {
      chats.value = response.data.records || response.data.list || []
      // 按置顶状态和更新时间排序
      sortChats()
      logger.info('对话列表加载成功', chats.value.length)
    } else {
      chats.value = []
    }
  } catch (error) {
    logger.error('加载对话列表失败', error)
    // 如果新接口失败，尝试使用旧接口
    try {
      const fallbackResponse = await getConversations({ page: 1, size: 100 })
      if (fallbackResponse.code === 200 && fallbackResponse.data) {
        chats.value = fallbackResponse.data.records || fallbackResponse.data.list || []
        sortChats()
      } else {
        chats.value = []
      }
    } catch (fallbackError) {
      logger.error('使用旧接口加载对话列表也失败', fallbackError)
      chats.value = []
    }
  }
}

const sortChats = () => {
  chats.value.sort((a, b) => {
    // 置顶的排在前面
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1
    // 同置顶状态下按更新时间排序
    return new Date(b.updatedAt) - new Date(a.updatedAt)
  })
}

const loadKnowledgeBases = async () => {
  try {
    logger.info('加载知识库列表')
    const response = await getKnowledgeList({ page: 1, size: 100 })
    if (response.code === 200 && response.data) {
      knowledgeBaseList.value = response.data.records || response.data.list || []
      logger.info('知识库列表加载成功', knowledgeBaseList.value.length)
    }
  } catch (error) {
    logger.error('加载知识库列表失败', error)
    knowledgeBaseList.value = []
  }
}

const createNewChat = async () => {
  try {
    const newChat = {
      id: Date.now(),
      title: '',
      lastMessage: '',
      updatedAt: new Date(),
      unreadCount: 0,
      isNew: true,
      pinned: false,
      messages: []
    }

    // 调用API创建对话
    try {
      // 使用新接口：创建AI会话
      const response = await createAiConversation({ title: '' })
      if (response.code === 200 && response.data) {
        newChat.id = response.data.id
        newChat.createdAt = response.data.createdTime || response.data.createdAt
        newChat.pinned = response.data.isPinned === 1
        newChat.isNew = false // 创建成功，标记为非新会话
        logger.info('创建新会话成功', { conversationId: response.data.id })
      }
    } catch (error) {
      logger.warn('创建对话API调用失败，尝试使用旧接口', error)
      // 如果新接口失败，尝试使用旧接口
      try {
        const fallbackResponse = await createConversation({ title: '' })
        if (fallbackResponse.code === 200 && fallbackResponse.data) {
          newChat.id = fallbackResponse.data.id
          newChat.createdAt = fallbackResponse.data.createdAt
          newChat.isNew = false // 创建成功，标记为非新会话
        }
      } catch (fallbackError) {
        logger.warn('使用旧接口创建对话也失败，使用本地ID', fallbackError)
        // 如果都失败，保持 isNew = true，在发送第一条消息时会再次尝试创建
      }
    }

    chats.value.unshift(newChat)
    saveChatsToStorage()
    selectChat(newChat)
    ElMessage.success(t('ai.chat.chatCreated'))
  } catch (error) {
    logger.error('创建对话失败', error)
    ElMessage.error(t('ai.chat.createError'))
  }
}

const selectChat = async (chat) => {
  currentChat.value = chat
  currentConversationId.value = null
  await loadMessages(chat.id)
  chat.unreadCount = 0
  saveChatsToStorage()
}

const loadMessages = async (chatId) => {
  try {
    // 从本地存储加载
    const chat = chats.value.find((c) => c.id === chatId)
    if (chat && chat.messages) {
      messages.value = [...chat.messages]
      // 恢复会话ID（如果有）
      if (chat.messages.length > 0) {
        const lastAiMessage = [...chat.messages].reverse().find(m => m.type === 'ai')
        if (lastAiMessage && lastAiMessage.conversationId) {
          currentConversationId.value = lastAiMessage.conversationId
        }
      }
    } else {
      // 尝试从API加载
      try {
        // 使用新接口：查询会话的所有消息列表
        const response = await listAiMessages(chatId)
        if (response.code === 200 && response.data) {
          const apiMessages = (response.data.records || response.data.list || response.data || []).map(msg => ({
            id: msg.id,
            type: msg.role === 'user' ? 'user' : 'ai',
            content: msg.content || '',
            timestamp: new Date(msg.sendTime || msg.createdTime || msg.createdAt || msg.timestamp),
            documents: msg.sources ? (typeof msg.sources === 'string' ? JSON.parse(msg.sources) : msg.sources) : [],
            conversationId: msg.conversationId
          }))
          messages.value = apiMessages
          // 更新本地存储
          if (chat) {
            chat.messages = apiMessages
            saveChatsToStorage()
          }
        }
      } catch (error) {
        logger.warn('从新API加载消息失败，尝试使用旧接口', error)
        // 如果新接口失败，尝试使用旧接口
        try {
          const fallbackResponse = await getConversationMessages(chatId, { page: 1, size: 100 })
          if (fallbackResponse.code === 200 && fallbackResponse.data) {
            const apiMessages = (fallbackResponse.data.records || fallbackResponse.data.list || []).map(msg => ({
              id: msg.id,
              type: msg.role === 'user' ? 'user' : 'ai',
              content: msg.content || '',
              timestamp: new Date(msg.createdAt || msg.timestamp),
              documents: msg.documents || []
            }))
            messages.value = apiMessages
            if (chat) {
              chat.messages = apiMessages
              saveChatsToStorage()
            }
          }
        } catch (fallbackError) {
          logger.warn('使用旧接口加载消息也失败，使用本地存储', fallbackError)
          messages.value = []
        }
      }
    }
    nextTick(() => {
      scrollKbToBottom()
    })
  } catch (error) {
    logger.error('加载消息失败', error)
    messages.value = []
  }
}

const saveChatsToStorage = () => {
  localStorage.setItem('ai-chat-chats', JSON.stringify(chats.value))
}

const loadChatsFromStorage = () => {
  const savedChats = localStorage.getItem('ai-chat-chats')
  if (savedChats) {
    try {
      const parsed = JSON.parse(savedChats)
      chats.value = parsed.map(chat => ({
        ...chat,
        updatedAt: new Date(chat.updatedAt),
        messages: chat.messages || []
      }))
      sortChats()
    } catch (error) {
      logger.error('解析本地存储失败', error)
      chats.value = []
    }
  }
}

// 格式化消息内容
const formatKbContent = (content) => {
  if (!content || typeof content !== 'string') {
    return ''
  }
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

const formatTime = (date) => {
  const d = typeof date === 'string' || typeof date === 'number'
    ? new Date(date)
    : date
  const now = new Date()
  const diff = now - d
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  return `${days}天前`
}

const scrollKbToBottom = () => {
  if (kbMessages.value) {
    kbMessages.value.scrollTop = kbMessages.value.scrollHeight
  }
}

// 发送消息
const sendKbMessage = async () => {
  if (!inputMessage.value.trim() && selectedKnowledgeBases.value.length === 0) return
  if (isGenerating.value) {
    ElMessage.warning('正在生成回答，请稍候...')
    return
  }

  const text = inputMessage.value.trim()
  
  // 如果没有选择知识库，knowledgeId为null，可以正常对话
  // 如果有选择知识库，使用第一个知识库的difyKbId（支持多个知识库选择，但接口只传第一个）
  let knowledgeId = null
  if (selectedKnowledgeBases.value.length > 0) {
    // 如果有多个知识库，使用第一个
    const firstKb = selectedKnowledgeBases.value[0]
    knowledgeId = firstKb.difyKbId || firstKb.difyKnowledgeId
    if (!knowledgeId) {
      ElMessage.warning('所选知识库缺少 Dify KB ID，将使用普通对话模式')
      // 继续执行，使用普通对话
    }
  }

  // 构建用户消息内容（包含@知识库标记）
  let userMessageContent = text
  if (selectedKnowledgeBases.value.length > 0) {
    const kbNames = selectedKnowledgeBases.value.map(kb => `@${kb.name}`).join(' ')
    userMessageContent = `${kbNames} ${text}`
  }

  const userMessageId = Date.now()
  
  // 确保有会话ID
  let conversationId = currentChat.value?.id
  if (!conversationId || currentChat.value?.isNew) {
    // 如果没有会话ID或是新会话，需要先创建会话
    try {
      const createResp = await createAiConversation({ title: '' })
      if (createResp.code === 200 && createResp.data) {
        conversationId = createResp.data.id
        if (currentChat.value) {
          currentChat.value.id = conversationId
          currentChat.value.isNew = false
          currentChat.value.createdAt = createResp.data.createdTime || createResp.data.createdAt
          currentChat.value.pinned = createResp.data.isPinned === 1
        }
        logger.info('创建新会话成功', { conversationId })
      }
    } catch (error) {
      logger.error('创建会话失败', error)
      ElMessage.error('创建会话失败，消息将仅保存在本地')
    }
  }
  
  // 添加用户消息
  const userMessage = {
    id: userMessageId,
    type: 'user',
    content: userMessageContent,
    timestamp: new Date(),
    attachments: attachments.value.length > 0 ? [...attachments.value] : [],
    conversationId: conversationId
  }

  messages.value.push(userMessage)
  
  // 保存用户消息到后端
  if (conversationId) {
    try {
      const messageResp = await createAiMessage({
        conversationId: String(conversationId),
        role: 'user',
        content: text, // 保存原始文本，不包含@知识库标记
        sendTime: new Date().toISOString()
      })
      if (messageResp.code === 200 && messageResp.data) {
        // 更新消息ID为后端返回的ID
        userMessage.id = messageResp.data.id
        userMessage.timestamp = new Date(messageResp.data.sendTime || messageResp.data.createdTime)
        logger.info('保存用户消息成功', { messageId: messageResp.data.id })
      }
    } catch (error) {
      logger.warn('保存用户消息到后端失败', error)
      // 继续执行，不影响前端显示
    }
  }
  
  // 保存到当前对话
  if (currentChat.value) {
    if (!currentChat.value.messages) {
      currentChat.value.messages = []
    }
    currentChat.value.messages.push(userMessage)
    currentChat.value.lastMessage = text
    currentChat.value.updatedAt = new Date()
    
    // 更新对话标题（如果是新对话且第一条消息）
    if (currentChat.value.isNew && !currentChat.value.title && text) {
      currentChat.value.title = text.length > 20 ? text.substring(0, 20) + '...' : text
      currentChat.value.isNew = false
      
      // 调用API更新对话标题
      if (conversationId) {
        try {
          // 使用新接口：更新AI会话
          await updateAiConversation({ id: String(conversationId), title: currentChat.value.title })
        } catch (error) {
          logger.warn('更新对话标题失败，尝试使用旧接口', error)
          // 如果新接口失败，尝试使用旧接口
          try {
            await updateConversation(conversationId, { title: currentChat.value.title })
          } catch (fallbackError) {
            logger.warn('使用旧接口更新对话标题也失败', fallbackError)
          }
        }
      }
    }
    
    saveChatsToStorage()
  }

  // 清空输入框和附件
  inputMessage.value = ''
  attachments.value = []
  selectedKnowledgeBases.value = []
  hideKnowledgeBaseList()

  // 创建AI消息占位
  const aiMessageId = Date.now() + 1
  const aiMessage = {
    id: aiMessageId,
    type: 'ai',
    content: '',
    timestamp: new Date(),
    streaming: true,
    documents: []
  }
  messages.value.push(aiMessage)

  nextTick(() => {
    scrollKbToBottom()
  })

  isGenerating.value = true

  try {
    logger.info('开始流式问答', { 
      knowledgeId: knowledgeId || '无知识库',
      query: text,
      conversationId: currentConversationId.value,
      selectedKbCount: selectedKnowledgeBases.value.length
    })

    // 调用流式对话接口
    // 如果没有选择知识库，knowledgeId为null，后端应该支持普通AI对话
    // 如果选择了知识库，使用第一个知识库的ID
    const abortController = await streamKnowledgeChatbot({
      knowledgeId: knowledgeId || null, // 无知识库时传null
      query: text,
      conversationId: currentConversationId.value,
      onMessage: (answer) => {
        // 追加回答内容
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.content += answer
          nextTick(scrollKbToBottom)
        }
      },
      onEnd: async (data) => {
        // 消息结束，保存会话ID和文档片段
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.streaming = false
          message.documents = data.documents || []
          message.conversationId = data.conversationId || currentChat.value?.id
        }
        
        // 更新会话ID（Dify返回的会话ID）
        if (data.conversationId) {
          currentConversationId.value = data.conversationId
        }
        
        // 保存AI消息到后端
        const conversationId = currentChat.value?.id || data.conversationId
        if (conversationId && message && message.content) {
          try {
            // 将文档片段转换为JSON字符串
            const sourcesJson = message.documents && message.documents.length > 0
              ? JSON.stringify(message.documents.map(doc => ({
                  document_id: doc.documentId || doc.id,
                  document_name: doc.name,
                  dataset_name: doc.datasetName,
                  content: doc.content,
                  score: doc.score,
                  segment_id: doc.segmentId
                })))
              : null
            
            const messageResp = await createAiMessage({
              conversationId: String(conversationId),
              role: 'assistant',
              content: message.content,
              difyMessageId: data.messageId || null,
              sources: sourcesJson,
              confidence: null,
              sendTime: new Date().toISOString()
            })
            
            if (messageResp.code === 200 && messageResp.data) {
              // 更新消息ID为后端返回的ID
              message.id = messageResp.data.id
              message.timestamp = new Date(messageResp.data.sendTime || messageResp.data.createdTime)
              logger.info('保存AI消息成功', { messageId: messageResp.data.id })
            }
          } catch (error) {
            logger.warn('保存AI消息到后端失败', error)
            // 继续执行，不影响前端显示
          }
        }
        
        isGenerating.value = false
        currentAbortController.value = null
        
        // 保存消息到当前对话
        if (currentChat.value && currentChat.value.messages) {
          const messageIndex = currentChat.value.messages.findIndex(m => m.id === aiMessageId)
          if (messageIndex !== -1) {
            currentChat.value.messages[messageIndex] = message
            saveChatsToStorage()
          }
        }
        
        nextTick(scrollKbToBottom)
        logger.info('流式问答完成', { 
          conversationId: data.conversationId || conversationId,
          messageId: data.messageId,
          documentCount: (data.documents || []).length
        })
      },
      onError: (error) => {
        isGenerating.value = false
        currentAbortController.value = null
        
        // 处理错误
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          if (error.code === 'CHATBOT_NOT_CREATED') {
            message.content = error.hint || '请先创建 Chatbot 应用才能使用知识库问答功能'
            ElMessage.warning(error.message || '请先创建 Chatbot 应用')
          } else {
            message.content = '抱歉，回答生成失败，请稍后再试。'
            ElMessage.error(error.message || '回答生成失败')
          }
          message.streaming = false
        }
        logger.error('流式问答失败', error)
        nextTick(scrollKbToBottom)
      }
    })

    currentAbortController.value = abortController
  } catch (error) {
    isGenerating.value = false
    currentAbortController.value = null
    const message = messages.value.find(m => m.id === aiMessageId)
    if (message) {
      message.content = '抱歉，回答生成失败，请稍后再试。'
      message.streaming = false
    }
    logger.error('流式问答异常', error)
    ElMessage.error(error.message || '回答生成失败')
    nextTick(scrollKbToBottom)
  }
}

// 停止生成
const stopGeneration = () => {
  if (currentAbortController.value) {
    currentAbortController.value.abort()
    currentAbortController.value = null
  }
  isGenerating.value = false
  
  // 标记当前正在生成的消息为已停止
  const streamingMessage = messages.value.find(m => m.streaming)
  if (streamingMessage) {
    streamingMessage.streaming = false
    streamingMessage.content += '\n\n[已停止生成]'
  }
}

// 复制消息
const copyKbMessage = async (content) => {
  try {
    // 移除HTML标签，获取纯文本
    const textContent = content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/<br>/g, '\n')
    await navigator.clipboard.writeText(textContent)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
    logger.error('复制失败', error)
  }
}

// 重试消息
const retryKbMessage = (msg) => {
  // 找到对应的用户消息
  const messageIndex = messages.value.findIndex(m => m.id === msg.id)
  if (messageIndex > 0) {
    const userMessage = messages.value[messageIndex - 1]
    if (userMessage && userMessage.type === 'user') {
      // 移除当前AI回复
      messages.value.splice(messageIndex, 1)
      
      // 同时从持久化存储中移除
      if (currentChat.value && currentChat.value.messages) {
        const persistentIndex = currentChat.value.messages.findIndex(m => m.id === msg.id)
        if (persistentIndex !== -1) {
          currentChat.value.messages.splice(persistentIndex, 1)
          saveChatsToStorage()
        }
      }
      
      // 提取用户原始输入（移除@知识库标记）
      let userInput = userMessage.content
      const kbRegex = /@[\w\s]+/g
      userInput = userInput.replace(kbRegex, '').trim()
      
      // 重新发送
      inputMessage.value = userInput
      nextTick(() => {
        sendKbMessage()
      })
    }
  }
}

// @知识库选择相关方法
const handleInputChange = (event) => {
  // 自动调整输入框高度
  autoResizeInput(event)
  
  const value = event.target.value
  const cursorPos = event.target.selectionStart
  
  // 检查光标前是否有@符号且没有空格
  const lastAtIndex = value.lastIndexOf('@', cursorPos - 1)
  if (lastAtIndex !== -1) {
    const textAfterAt = value.substring(lastAtIndex + 1, cursorPos)
    // 如果@后面没有空格或换行，且没有选择知识库，显示列表
    if (!textAfterAt.includes(' ') && !textAfterAt.includes('\n')) {
      // 检查是否已经选择了知识库
      const hasSelectedKb = knowledgeBaseList.value.some((kb) => {
        const kbText = `@${kb.name}`
        return value.substring(lastAtIndex, cursorPos).includes(kbText)
      })
      
      if (!hasSelectedKb) {
        showKnowledgeBaseList.value = true
        selectedKbIndex.value = 0 // 默认选中第一个
      } else {
        showKnowledgeBaseList.value = false
        selectedKbIndex.value = -1
      }
    } else {
      // @后面有空格，隐藏列表
      showKnowledgeBaseList.value = false
      selectedKbIndex.value = -1
    }
  } else {
    // 光标前没有@符号，隐藏知识库列表
    showKnowledgeBaseList.value = false
    selectedKbIndex.value = -1
  }
}

const selectKnowledgeBase = (kb) => {
  const textarea = document.querySelector('.kb-message-input')
  if (textarea) {
    const cursorPos = textarea.selectionStart
    const value = textarea.value
    const lastAtIndex = value.lastIndexOf('@', cursorPos - 1)
    
    if (lastAtIndex !== -1) {
      // 检查是否已经选择过这个知识库
      const kbText = `@${kb.name}`
      const alreadySelected = selectedKnowledgeBases.value.find(k => k.id === kb.id)
      
      if (!alreadySelected) {
        // 添加到已选择列表
        selectedKnowledgeBases.value.push(kb)
      }
      
      // 替换@符号为知识库名称
      const newValue = value.substring(0, lastAtIndex) + `${kbText} ` + value.substring(cursorPos)
      inputMessage.value = newValue
      
      // 更新光标位置
      nextTick(() => {
        const newCursorPos = lastAtIndex + kbText.length + 1 // +1 for space
        textarea.setSelectionRange(newCursorPos, newCursorPos)
        textarea.focus()
      })
    }
  }
  
  showKnowledgeBaseList.value = false
  selectedKbIndex.value = -1
  ElMessage.success(`已选择知识库：${kb.name}`)
}

const isKbSelected = (kbId) => {
  return selectedKnowledgeBases.value.some(kb => kb.id === kbId)
}

const getKbIcon = (kb) => {
  // 可以根据知识库类型返回不同的图标
  return kb.icon || '📚'
}

// 键盘导航知识库列表
const navigateKbList = (direction) => {
  if (!showKnowledgeBaseList.value) return
  
  const list = filteredKnowledgeBases.value
  if (direction === 'up') {
    selectedKbIndex.value = selectedKbIndex.value > 0
      ? selectedKbIndex.value - 1
      : list.length - 1
  } else if (direction === 'down') {
    selectedKbIndex.value = selectedKbIndex.value < list.length - 1
      ? selectedKbIndex.value + 1
      : 0
  }
}

// 处理Enter键
const handleEnterKey = (event) => {
  if (showKnowledgeBaseList.value && selectedKbIndex.value >= 0) {
    // 如果有选中的知识库，选择它
    const selectedKb = filteredKnowledgeBases.value[selectedKbIndex.value]
    selectKnowledgeBase(selectedKb)
  } else {
    // 否则发送消息
    sendKbMessage()
  }
}

// 隐藏知识库列表
const hideKnowledgeBaseList = () => {
  showKnowledgeBaseList.value = false
  selectedKbIndex.value = -1
}

// 智能删除处理
const handleBackspace = (event) => {
  const textarea = event.target
  const cursorPos = textarea.selectionStart
  const value = textarea.value
  
  // 检查光标前是否有知识库文本
  for (const kb of selectedKnowledgeBases.value) {
    const kbText = `@${kb.name}`
    const kbStartPos = value.lastIndexOf(kbText, cursorPos - 1)
    
    if (kbStartPos !== -1 && kbStartPos + kbText.length === cursorPos) {
      // 如果光标正好在知识库文本的末尾，整体删除
      event.preventDefault()
      const newValue = value.substring(0, kbStartPos) + value.substring(cursorPos)
      inputMessage.value = newValue
      
      // 从已选择列表中移除
      const index = selectedKnowledgeBases.value.findIndex(k => k.id === kb.id)
      if (index > -1) {
        selectedKnowledgeBases.value.splice(index, 1)
      }
      
      nextTick(() => {
        textarea.setSelectionRange(kbStartPos, kbStartPos)
        textarea.focus()
      })
      return
    }
  }
}

// 自动调整输入框高度
const autoResizeInput = (event) => {
  if (event && event.target) {
    const textarea = event.target
    textarea.style.height = 'auto'
    const scrollHeight = textarea.scrollHeight
    const minHeight = 44
    const maxHeight = 200
    const newHeight = Math.min(Math.max(scrollHeight, minHeight), maxHeight)
    textarea.style.height = newHeight + 'px'
    if (scrollHeight > maxHeight) {
      textarea.style.overflowY = 'auto'
    } else {
      textarea.style.overflowY = 'hidden'
    }
  }
}

// 模型选择器相关方法
const toggleModelDropdown = () => {
  showModelDropdown.value = !showModelDropdown.value
}

const getSelectedModelName = (modelValue) => {
  const model = modelOptions.value.find(m => m.value === modelValue)
  return model ? model.name : modelValue
}

const selectModel = (model) => {
  selectedModel.value = model
  showModelDropdown.value = false
  ElMessage.success(`已切换到${getSelectedModelName(model)}`)
}

// 附件相关方法
const handleAttachmentClick = () => {
  if (hasKnowledgeBaseSelected.value) {
    ElMessage.warning('知识库模式下不支持附件')
    return
  }
  fileInput.value && fileInput.value.click()
}

const handleFileUpload = (event) => {
  const files = Array.from(event.target.files || [])
  
  // 检查文件数量限制
  if (attachments.value.length + files.length > 10) {
    ElMessage.warning('最多只能上传10个文件')
    return
  }
  
  files.forEach((file) => {
    // 检查文件大小限制 (100MB)
    if (file.size > 100 * 1024 * 1024) {
      ElMessage.warning(`文件 ${file.name} 超过100MB限制`)
      return
    }
    
    // 检查文件类型
    const allowedTypes = ['.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.csv', '.jpg', '.jpeg', '.png', '.md', '.txt']
    const fileExtension = '.' + file.name.split('.').pop().toLowerCase()
    if (!allowedTypes.includes(fileExtension)) {
      ElMessage.warning(`文件 ${file.name} 格式不支持`)
      return
    }
    
    // 添加到附件列表
    attachments.value.push({
      name: file.name,
      size: formatFileSize(file.size),
      file: file,
      type: fileExtension
    })
  })
  
  // 清空文件输入
  event.target.value = ''
  ElMessage.success(`已添加 ${files.length} 个文件`)
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileIcon = (fileType) => {
  const iconMap = {
    '.pdf': '📕',
    '.doc': '📘',
    '.docx': '📘',
    '.ppt': '📗',
    '.pptx': '📗',
    '.xls': '📊',
    '.xlsx': '📊',
    '.csv': '📋',
    '.jpg': '🖼️',
    '.jpeg': '🖼️',
    '.png': '🖼️',
    '.md': '📝',
    '.txt': '📄'
  }
  return iconMap[fileType] || '📄'
}

const removeAttachment = (index) => {
  attachments.value.splice(index, 1)
  ElMessage.success('已移除附件')
}

// 对话管理相关方法
const handleChatAction = async (command, chat) => {
  switch (command) {
    case 'edit':
      await editChatTitle(chat)
      break
    case 'pin':
      await togglePinChat(chat)
      break
    case 'delete':
      await deleteChatConfirm(chat)
      break
  }
}

const editChatTitle = async (chat) => {
  try {
    const { value: newTitle } = await ElMessageBox.prompt(
      '请输入新的对话标题',
      '编辑标题',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: chat.title || ''
      }
    )
    
    if (newTitle && newTitle.trim()) {
      chat.title = newTitle.trim()
      // 调用API更新对话标题
      try {
        // 使用新接口：更新AI会话
        await updateAiConversation({ id: chat.id, title: chat.title })
      } catch (error) {
        logger.warn('更新对话标题失败，尝试使用旧接口', error)
        // 如果新接口失败，尝试使用旧接口
        try {
          await updateConversation(chat.id, { title: chat.title })
        } catch (fallbackError) {
          logger.warn('使用旧接口更新对话标题也失败', fallbackError)
        }
      }
      saveChatsToStorage()
      ElMessage.success('标题修改成功')
    }
  } catch (error) {
    // 用户取消
  }
}

const togglePinChat = async (chat) => {
  chat.pinned = !chat.pinned
  sortChats()
  saveChatsToStorage()
  
  // 调用API更新置顶状态
  try {
    // 使用新接口：更新AI会话置顶状态
    await updateAiConversationPinnedStatus(chat.id, chat.pinned ? 1 : 0)
  } catch (error) {
    logger.warn('更新置顶状态失败，尝试使用旧接口', error)
    // 如果新接口失败，尝试使用旧接口
    try {
      await updateConversation(chat.id, { pinned: chat.pinned })
    } catch (fallbackError) {
      logger.warn('使用旧接口更新置顶状态也失败', fallbackError)
    }
  }
  
  ElMessage.success(chat.pinned ? '对话已置顶' : '已取消置顶')
}

const deleteChatConfirm = async (chat) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该对话吗？此操作不可恢复。',
      '删除对话',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API删除对话
    try {
      // 使用新接口：删除AI会话
      await deleteAiConversation(chat.id)
    } catch (error) {
      logger.warn('删除对话API调用失败，尝试使用旧接口', error)
      // 如果新接口失败，尝试使用旧接口
      try {
        await deleteConversation(chat.id)
      } catch (fallbackError) {
        logger.warn('使用旧接口删除对话也失败', fallbackError)
      }
    }
    
    const index = chats.value.findIndex(c => c.id === chat.id)
    if (index !== -1) {
      chats.value.splice(index, 1)
    }
    
    if (currentChat.value && currentChat.value.id === chat.id) {
      currentChat.value = null
      messages.value = []
    }
    
    saveChatsToStorage()
    ElMessage.success('对话删除成功')
  } catch (error) {
    // 用户取消
  }
}

// 点击外部关闭下拉框
const handleClickOutside = (event) => {
  if (!event.target.closest('.custom-model-selector')) {
    showModelDropdown.value = false
  }
  if (!event.target.closest('.knowledge-base-dropdown')) {
    hideKnowledgeBaseList()
  }
}

// 生命周期钩子
onMounted(async () => {
  try {
    logger.info('AI对话页面初始化开始')
    
    // 加载对话列表（先尝试本地存储，再尝试API）
    loadChatsFromStorage()
    
    // 并行加载对话列表和知识库列表
    await Promise.all([
      loadChats().catch(err => {
        logger.warn('加载对话列表失败，继续使用本地数据', err)
      }),
      loadKnowledgeBases().catch(err => {
        logger.warn('加载知识库列表失败，继续初始化', err)
      })
    ])
    
    // 如果没有当前对话，自动选择第一个对话或创建新对话
    if (!currentChat.value) {
      if (chats.value.length > 0) {
        // 如果有已有对话，选择第一个
        await selectChat(chats.value[0])
      } else {
        // 如果没有对话，自动创建一个新对话
        await createNewChat()
      }
    }
    
    document.addEventListener('click', handleClickOutside)
    
    logger.info('AI对话页面初始化完成', {
      chatsCount: chats.value.length,
      kbCount: knowledgeBaseList.value.length,
      hasCurrentChat: !!currentChat.value
    })
  } catch (error) {
    logger.error('AI对话页面初始化失败', error)
    ElMessage.error('页面初始化失败，请刷新页面重试')
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  // 停止正在进行的请求
  if (currentAbortController.value) {
    currentAbortController.value.abort()
  }
})
</script>

<style lang="scss" scoped>
// AI对话页面需要全屏显示，覆盖MainLayout的限制
.ai-chat-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  max-width: 100%;
  margin: 0;
  padding: 0;
  background: #f7f9fc;
  overflow: hidden;
  position: relative;
}

// 覆盖MainLayout的样式限制（使用深度选择器）
// MainLayout 的结构：.main-layout > .layout-content > .main-content > .content-wrapper
:deep(.main-content) {
  padding: 0 !important;
  max-width: 100% !important;
  height: 100% !important;
  
  .content-wrapper {
    max-width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    height: 100% !important;
    min-height: 100% !important;
  }
}

// 确保 layout-content 也占满高度
:deep(.layout-content) {
  height: 100% !important;
}

/* 左侧对话列表 */
.chat-list-sidebar {
  width: 280px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.chat-list-header {
  padding: 16px;
  background: linear-gradient(135deg, #fafbfc 0%, #f7f9fc 100%);
  border-bottom: 1px solid #f3f4f6;
}

.new-chat-btn {
  width: 100%;
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  color: white;
  border: none;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.15);

  &:hover {
    background: linear-gradient(135deg, #1e40af 0%, #2563eb 100%);
    box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.chat-list::-webkit-scrollbar {
  width: 6px;
}

.chat-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.chat-list::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

.chat-item {
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &:hover {
    background: #f9fafb;
  }

  &.active {
    background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
    box-shadow: 0 2px 8px rgba(30, 58, 138, 0.08);
  }

  &.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 24px;
    background: linear-gradient(180deg, #1e3a8a 0%, #0ea5e9 100%);
    border-radius: 0 2px 2px 0;
  }

  &.pinned {
    background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  }
}

.chat-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.chat-item-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
  display: flex;
  align-items: center;
  margin-left: 8px;
  flex-shrink: 0;
}

.chat-item:hover .chat-item-actions,
.chat-item.active .chat-item-actions {
  opacity: 1;
}

.chat-more-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 16px;
  font-weight: bold;

  &:hover {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.chat-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 6px;

  .pin-icon {
    color: #1e3a8a;
    flex-shrink: 0;
  }
}

.chat-item.active .chat-title,
.chat-item.pinned .chat-title {
  color: #1e3a8a;
  font-weight: 600;
}

.chat-preview {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.chat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-time {
  font-size: 11px;
  color: #9ca3af;
}

.unread-badge {
  background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
  color: white;
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.3);
}

/* 右侧对话内容 */
.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  position: relative;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafbfc;
}

.chat-title-header {
  font-size: 18px;
  font-weight: 600;
  color: #1e3a8a;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
  padding: 40px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-text {
  font-size: 18px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 12px;
}

.empty-desc {
  font-size: 14px;
  color: #9ca3af;
}

// 消息容器样式（复用知识库页面样式）
.kb-messages-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: visible;
  padding: 24px;
  background: linear-gradient(180deg, #fafbfc 0%, #f7f9fc 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;

  &.has-messages {
    align-items: stretch;
    justify-content: flex-start;
  }
}

.kb-messages-container::-webkit-scrollbar {
  width: 4px;
}

.kb-messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.kb-messages-container::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.kb-messages-container:hover::-webkit-scrollbar-thumb {
  background: #e5e7eb;
}

.kb-messages-container::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}

.empty-chat-message {
  font-size: 16px;
  color: #6b7280;
  text-align: center;
  line-height: 1.6;
}

.kb-messages {
  width: 100%;
  max-width: 100%;
  overflow: visible;
}

// 消息样式（复用知识库页面样式）
.kb-message {
  margin-bottom: 24px;
  display: flex;
  align-items: flex-start;
  gap: 12px;

  &.user {
    flex-direction: row-reverse;
    gap: 16px;
  }

  &.ai {
    justify-content: flex-start;
  }
}

.kb-message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &.user {
    background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
    color: #fff;
  }

  &.ai {
    background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);
    color: #fff;
  }
}

.kb-message-bubble {
  max-width: 70%;
  position: relative;
}

.kb-message-content-wrapper {
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #374151;
}

.kb-message.user .kb-message-content-wrapper {
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  color: #fff;
  border-color: transparent;
  border-radius: 16px 16px 4px 16px;
}

.kb-message.ai .kb-message-content-wrapper {
  border-radius: 16px 16px 16px 4px;
}

.kb-message-content {
  font-size: 14px;
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-word;
  overflow: visible;

  :deep(code) {
    background: rgba(0, 0, 0, 0.1);
    padding: 2px 4px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
  }

  :deep(strong) {
    font-weight: 600;
  }
}

.kb-message.user .kb-message-content {
  color: #fff !important;
}

.kb-message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 11px;
  color: #9ca3af;
  justify-content: space-between;
}

.kb-message-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.kb-copy-btn,
.kb-retry-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  color: #9ca3af;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #e5e7eb;
    color: #374151;
    border-color: #d1d5db;
  }

  &:active {
    background: #d1d5db;
  }
}

// 文档片段展示样式（复用知识库页面样式）
.kb-message-documents {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.kb-documents-title {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  font-weight: 500;
}

.kb-documents-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kb-document-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f9fafb;
  border-radius: 6px;
  font-size: 12px;
  transition: background 0.2s ease;
}

.kb-document-item:hover {
  background: #f3f4f6;
}

.kb-document-icon {
  color: #3b82f6;
  font-size: 14px;
  flex-shrink: 0;
}

.kb-document-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
}

.kb-document-name {
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kb-document-dataset {
  color: #9ca3af;
  font-size: 11px;
  flex-shrink: 0;
}

.kb-document-score {
  color: #9ca3af;
  font-size: 11px;
  flex-shrink: 0;
}

// 流式生成指示器（复用知识库页面样式）
.kb-streaming-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.kb-streaming-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3b82f6;
  animation: kb-streaming-pulse 1.5s ease-in-out infinite;
}

@keyframes kb-streaming-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

.kb-streaming-text {
  font-size: 12px;
  color: #6b7280;
}

// 输入区域样式（复用知识库页面样式）
.kb-input-area {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  position: relative;
  z-index: 10;
}

.kb-input-container {
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 0;
  transition: all 0.2s ease;
  overflow: visible;
}

.kb-message-input {
  width: 100%;
  min-height: 44px;
  max-height: 200px;
  padding: 12px 16px 8px 16px;
  border: none;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  outline: none;
  background: transparent;
  color: #333;
  overflow-y: hidden;
  font-family: inherit;
  word-wrap: break-word;
  white-space: pre-wrap;

  &::placeholder {
    color: #9ca3af;
  }
}

// 附件预览区域
.attachment-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 12px;
  color: #374151;
  max-width: 200px;
}

.attachment-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.attachment-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.attachment-size {
  color: #9ca3af;
  font-size: 11px;
}

.attachment-remove {
  width: 16px;
  height: 16px;
  color: #9ca3af;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:hover {
    background: #f3f4f6;
    color: #dc2626;
  }
}

// 知识库选择下拉框
.knowledge-base-dropdown {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1002;
  margin-bottom: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.kb-dropdown-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

.kb-dropdown-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e3a8a;
}

.kb-dropdown-section {
  padding: 8px 0;
}

.kb-list {
  padding: 0 8px;
}

.kb-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  position: relative;

  &:hover {
    background: #e3f2fd;
    border-color: #bbdefb;
  }

  &.selected {
    background: #bbdefb;
    border-color: #90caf9;
    box-shadow: 0 2px 4px rgba(30, 58, 138, 0.1);
  }
}

.kb-icon {
  width: 24px;
  height: 24px;
  background: #1e3a8a;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.kb-name {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
  flex: 1;
}

.kb-selected-mark {
  color: #3b82f6;
  font-weight: bold;
  font-size: 14px;
}

// 底部控制栏
.kb-input-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px 12px 16px;
  border-top: 1px solid #e5e7eb;
  background: transparent;
}

// 模型选择器
.custom-model-selector {
  position: relative;
  min-width: 120px;
  flex-shrink: 0;
  z-index: 1000;
}

.model-display {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 12px;
  color: #6b7280;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-weight: bold;

  &:hover {
    border-color: #d1d5db;
    background: #f9fafb;
  }
}

.model-dropdown {
  position: fixed;
  bottom: auto;
  left: auto;
  right: auto;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  margin-bottom: 8px;
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
  width: 320px;
}

.model-option {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  position: relative;
  border-bottom: 1px solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #f8f9fa;
  }

  &.active {
    background: #eef2ff;
  }
}

.model-name {
  font-size: 13px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 2px;
}

.model-desc {
  font-size: 11px;
  color: #6b7280;
  line-height: 1.3;
}

.check-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #3b82f6;
  font-weight: bold;
  font-size: 14px;
}

// 右侧控制按钮组
.kb-right-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 附件按钮
.kb-attachment-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f3f4f6;
  color: #6b7280;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background: #e5e7eb;
    color: #374151;
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }

  &.disabled {
    background: #f9fafb;
    color: #d1d5db;
    cursor: not-allowed;

    &:hover {
      background: #f9fafb;
      color: #d1d5db;
      transform: none;
    }
  }
}

// 发送按钮
.kb-send-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #e5e7eb;
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &.active {
    background: #1e3a8a;
    color: #fff;

    &:hover {
      background: #1e40af;
    }
  }
}

// 停止按钮
.kb-stop-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #1e3a8a;
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background: #1e40af;
    transform: scale(1.05);
  }
}

.input-footer {
  margin-top: 12px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}

// 响应式设计
@media (max-width: 768px) {
  .chat-list-sidebar {
    width: 0;
    overflow: hidden;
  }

  .kb-message-bubble {
    max-width: 85%;
  }
}
</style>