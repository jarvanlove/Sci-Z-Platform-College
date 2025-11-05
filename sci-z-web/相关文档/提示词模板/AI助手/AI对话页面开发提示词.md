# AI对话页面开发提示词

## 📋 页面信息
- **原型图路径**：`原型图/AI助手/AI对话.html`
- **页面类型**：对话页面
- **页面名称**：AI对话
- **主要功能**：AI对话交互、对话历史管理、流式响应、代码高亮、多模态内容渲染
- **对应文件路径**：`src/views/AI/Chat.vue`

## 🎨 原型图视觉分析

### **整体布局**
- 双栏布局：左侧对话列表 + 右侧对话内容
- 背景色：`#f7f9fc`
- 高度：`100vh`，全屏显示

### **左侧对话列表区域**
- 宽度：`280px`
- 背景：`#ffffff`
- 边框：`1px solid #e5e7eb`
- 头部：新建对话按钮，渐变背景
- 列表：对话历史，支持滚动

### **右侧对话内容区域**
- 头部：对话标题、操作按钮（清空、设置）
- 消息区域：滚动容器，支持消息流式显示
- 输入区域：多行输入框、发送按钮、附件上传

### **消息样式**
- 用户消息：右对齐，蓝色背景
- AI消息：左对齐，白色背景，支持代码高亮
- 流式响应：打字机效果
- 代码块：深色主题，语法高亮

## 🔧 技术实现要求

### 1. 页面结构
```vue
<template>
  <div class="ai-chat-container">
    <!-- 左侧对话列表 -->
    <div class="chat-list-sidebar">
      <div class="chat-list-header">
        <el-button 
          type="primary" 
          class="new-chat-btn"
          @click="createNewChat"
        >
          <el-icon><Plus /></el-icon>
          {{ $t('ai.chat.newChat') }}
        </el-button>
      </div>
      
      <div class="chat-list">
        <div
          v-for="chat in chatList"
          :key="chat.id"
          class="chat-item"
          :class="{ active: currentChatId === chat.id }"
          @click="selectChat(chat.id)"
        >
          <div class="chat-item-content">
            <div class="chat-title">{{ chat.title }}</div>
            <div class="chat-preview">{{ chat.lastMessage }}</div>
            <div class="chat-time">{{ formatTime(chat.updatedAt) }}</div>
          </div>
          <div class="chat-item-actions" @click.stop>
            <el-dropdown @command="(action) => handleChatAction(action, chat)">
              <el-button class="chat-more-btn" text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">{{ $t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="pin">
                    {{ chat.pinned ? $t('ai.chat.unpin') : $t('ai.chat.pin') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
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
      <!-- 对话头部 -->
      <div class="chat-header">
        <div class="chat-title-header">
          {{ currentChat?.title || $t('ai.chat.newChat') }}
        </div>
        <div class="chat-actions">
          <el-button text @click="clearMessages">
            <el-icon><Delete /></el-icon>
            {{ $t('ai.chat.clear') }}
          </el-button>
          <el-button text @click="showSettings">
            <el-icon><Setting /></el-icon>
            {{ $t('common.settings') }}
          </el-button>
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-chat-message">
          <div class="empty-icon">🤖</div>
          <div class="empty-title">{{ $t('ai.chat.welcome') }}</div>
          <div class="empty-subtitle">{{ $t('ai.chat.welcomeSubtitle') }}</div>
          <div class="suggestions">
            <div
              v-for="suggestion in suggestions"
              :key="suggestion"
              class="suggestion-item"
              @click="sendSuggestion(suggestion)"
            >
              {{ suggestion }}
            </div>
          </div>
        </div>

        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="message.role"
        >
          <div class="message-avatar">
            <div v-if="message.role === 'user'" class="user-avatar">
              <el-icon><User /></el-icon>
            </div>
            <div v-else class="ai-avatar">
              <el-icon><Robot /></el-icon>
            </div>
          </div>
          
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">
                {{ message.role === 'user' ? $t('ai.chat.user') : $t('ai.chat.assistant') }}
              </span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            
            <div class="message-body">
              <!-- 文本内容 -->
              <div v-if="message.contentType === 'text'" class="text-content">
                {{ message.content }}
              </div>
              
              <!-- Markdown内容 -->
              <div v-else-if="message.contentType === 'markdown'" class="markdown-content">
                <div v-html="renderMarkdown(message.content)"></div>
              </div>
              
              <!-- 代码内容 -->
              <div v-else-if="message.contentType === 'code'" class="code-content">
                <div class="code-header">
                  <span class="code-language">{{ message.payload.language }}</span>
                  <el-button text size="small" @click="copyCode(message.payload.content)">
                    <el-icon><CopyDocument /></el-icon>
                    {{ $t('common.copy') }}
                  </el-button>
                </div>
                <pre><code :class="message.payload.language">{{ message.payload.content }}</code></pre>
              </div>
              
              <!-- 流式响应 -->
              <div v-if="message.streaming" class="streaming-indicator">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>{{ $t('ai.chat.thinking') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <div class="input-actions">
            <el-button text @click="toggleAttachment">
              <el-icon><Paperclip /></el-icon>
            </el-button>
            <el-button text @click="toggleVoice">
              <el-icon><Microphone /></el-icon>
            </el-button>
          </div>
          
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 6 }"
            :placeholder="$t('ai.chat.inputPlaceholder')"
            class="message-input"
            @keydown.enter.exact="handleSend"
            @keydown.enter.shift.exact="handleNewLine"
          />
          
          <el-button
            type="primary"
            class="send-button"
            :disabled="!inputMessage.trim() || isGenerating"
            :loading="isGenerating"
            @click="handleSend"
          >
            <el-icon v-if="!isGenerating"><Position /></el-icon>
            <el-icon v-else class="is-loading"><Loading /></el-icon>
          </el-button>
        </div>
        
        <div class="input-footer">
          <span class="input-tip">{{ $t('ai.chat.inputTip') }}</span>
          <div class="model-info">
            <el-tag size="small">{{ currentModel }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
```

### 2. 核心功能实现
```javascript
<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, MoreFilled, Delete, Setting, User, Robot, CopyDocument,
  Loading, Paperclip, Microphone, Position
} from '@element-plus/icons-vue'
import { sendMessage, getChatList, createChat, updateChat, deleteChat } from '@/api/AI'
import { formatDate } from '@/utils/date'
import { createLogger } from '@/utils/simpleLogger'

const { t } = useI18n()
const logger = createLogger('AIChat')

// 响应式数据
const messagesContainer = ref()
const inputMessage = ref('')
const isGenerating = ref(false)
const currentChatId = ref(null)
const currentModel = ref('qwen3-max')

// 对话列表
const chatList = ref([])

// 当前对话消息
const messages = ref([])

// 当前对话
const currentChat = computed(() => {
  return chatList.value.find(chat => chat.id === currentChatId.value)
})

// 建议问题
const suggestions = ref([
  '帮我写一个Vue3组件',
  '解释一下什么是人工智能',
  '如何优化网站性能？',
  '推荐一些学习资源'
])

// 对话列表通过后端接口获取（预留接口）
// TODO: getChatList由后端提供，返回{ list: Chat[], ... }

// 方法
const createNewChat = async () => {
  try {
    const newChat = {
      id: Date.now().toString(),
      title: t('ai.chat.newChat'),
      lastMessage: '',
      updatedAt: new Date(),
      pinned: false
    }
    
    // TODO: 调用API创建对话
    // await createChat(newChat)
    
    chatList.value.unshift(newChat)
    currentChatId.value = newChat.id
    messages.value = []
    
    ElMessage.success(t('ai.chat.chatCreated'))
  } catch (error) {
    ElMessage.error(t('ai.chat.createError'))
  }
}

const selectChat = (chatId) => {
  currentChatId.value = chatId
  loadChatMessages(chatId)
}

const loadChatMessages = async (chatId) => {
  try {
    // TODO: 调用API获取对话消息（后端返回完整消息列表，含不同contentType）
    // const response = await getChatMessages(chatId)
    // messages.value = response.data
    messages.value = []
  } catch (error) {
    ElMessage.error(t('ai.chat.loadError'))
  }
}

const handleSend = async () => {
  if (!inputMessage.value.trim() || isGenerating.value) return
  
  const userMessage = {
    id: Date.now().toString(),
    role: 'user',
    content: inputMessage.value,
    contentType: 'text',
    timestamp: new Date()
  }
  
  messages.value.push(userMessage)
  const userInput = inputMessage.value
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 发送消息
  await sendAIMessage(userInput)
}

const sendAIMessage = async (userInput) => {
  isGenerating.value = true
  
  const aiMessage = {
    id: Date.now().toString(),
    role: 'assistant',
    content: '',
    contentType: 'text',
    timestamp: new Date(),
    streaming: true
  }
  
  messages.value.push(aiMessage)
  
  try {
    // TODO: 调用AI对话接口（流式）
    // 约定：sendMessage返回一个ReadableStream或SSE连接，数据为逐条事件：
    // { type: 'text' | 'markdown' | 'code' | 'image' | 'file' | 'metadata' | 'end' | 'error', payload: any }
    // 参考实现A：Fetch + ReadableStream（推荐）
    // const stream = await sendMessage({ chatId: currentChatId.value, message: userInput, model: currentModel.value })
    // const reader = stream.getReader()
    // const decoder = new TextDecoder()
    // while (true) {
    //   const { value, done } = await reader.read()
    //   if (done) break
    //   const chunk = decoder.decode(value, { stream: true })
    //   for (const line of chunk.split('\n')) {
    //     if (!line) continue
    //     const event = JSON.parse(line)
    //     switch (event.type) {
    //       case 'text':
    //         aiMessage.content += event.payload
    //         break
    //       case 'markdown':
    //         aiMessage.contentType = 'markdown'
    //         aiMessage.content += event.payload
    //         break
    //       case 'code':
    //         aiMessage.contentType = 'code'
    //         aiMessage.payload = { ...(aiMessage.payload || {}), ...event.payload }
    //         break
    //       case 'image':
    //         // TODO: 渲染图片消息
    //         break
    //       case 'file':
    //         // TODO: 渲染文件附件
    //         break
    //       case 'metadata':
    //         // TODO: 处理元数据，如token用量等
    //         break
    //       case 'end':
    //         // 结束标记
    //         break
    //       case 'error':
    //         throw new Error(event.payload?.message || 'AI error')
    //     }
    //     await nextTick(); scrollToBottom()
    //   }
    // }
    // 参考实现B：SSE（EventSource）由sendMessage封装
    // await sendMessage({ chatId: currentChatId.value, message: userInput, model: currentModel.value, onEvent: (event) => { /* 同上switch处理 */ } })
    
    // TODO: 更新对话列表的最后一条消息
    updateChatLastMessage(currentChatId.value, userInput)
  } catch (error) {
    aiMessage.content = t('ai.chat.responseError')
    ElMessage.error(t('ai.chat.sendError'))
  } finally {
    aiMessage.streaming = false
    isGenerating.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 流式处理由sendMessage实现，前端根据type增量渲染（见上方TODO示例）

const updateChatLastMessage = (chatId, message) => {
  const chat = chatList.value.find(c => c.id === chatId)
  if (chat) {
    chat.lastMessage = message
    chat.updatedAt = new Date()
  }
}

const handleChatAction = async (action, chat) => {
  switch (action) {
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
      t('ai.chat.editTitle'),
      t('common.edit'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        inputValue: chat.title
      }
    )
    
    chat.title = newTitle
    // TODO: 调用API更新对话标题
    // await updateChat(chat.id, { title: newTitle })
    
    ElMessage.success(t('ai.chat.titleUpdated'))
  } catch (error) {
    // 用户取消
  }
}

const togglePinChat = async (chat) => {
  chat.pinned = !chat.pinned
  // TODO: 调用API更新置顶状态
  // await updateChat(chat.id, { pinned: chat.pinned })
  
  ElMessage.success(
    chat.pinned ? t('ai.chat.pinned') : t('ai.chat.unpinned')
  )
}

const deleteChatConfirm = async (chat) => {
  try {
    await ElMessageBox.confirm(
      t('ai.chat.deleteConfirm'),
      t('common.confirm'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    // TODO: 调用API删除对话
    // await deleteChat(chat.id)
    
    const index = chatList.value.findIndex(c => c.id === chat.id)
    if (index > -1) {
      chatList.value.splice(index, 1)
    }
    
    if (currentChatId.value === chat.id) {
      currentChatId.value = null
      messages.value = []
    }
    
    ElMessage.success(t('ai.chat.deleted'))
  } catch (error) {
    // 用户取消
  }
}

const clearMessages = async () => {
  try {
    await ElMessageBox.confirm(
      t('ai.chat.clearConfirm'),
      t('common.confirm'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    messages.value = []
    ElMessage.success(t('ai.chat.cleared'))
  } catch (error) {
    // 用户取消
  }
}

const sendSuggestion = (suggestion) => {
  inputMessage.value = suggestion
  handleSend()
}

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success(t('common.copied'))
  } catch (error) {
    ElMessage.error(t('common.copyError'))
  }
}

const renderMarkdown = (content) => {
  // TODO: 实现Markdown渲染
  // 可以使用marked.js或markdown-it
  return content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleNewLine = () => {
  // Shift+Enter 换行，不发送
}

const toggleAttachment = () => {
  ElMessage.info(t('ai.chat.attachmentComingSoon'))
}

const toggleVoice = () => {
  ElMessage.info(t('ai.chat.voiceComingSoon'))
}

const showSettings = () => {
  ElMessage.info(t('ai.chat.settingsComingSoon'))
}

// 监听消息变化，自动滚动
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })

onMounted(async () => {
  // 加载对话列表
  try {
    // TODO: 调用API获取对话列表
    // const response = await getChatList()
    // chatList.value = response.data
  } catch (error) {
    ElMessage.error(t('ai.chat.loadListError'))
  }
})
</script>
```

### 3. 样式实现
```scss
<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  height: 100vh;
  background: #f7f9fc;
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
  border-radius: 8px;
  padding: 12px 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3);
  }
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
  
  &:hover {
    background: #f8fafc;
  }
  
  &.active {
    background: #eff6ff;
    border: 1px solid #dbeafe;
  }
}

.chat-item-content {
  flex: 1;
  min-width: 0;
}

.chat-title {
  font-weight: 500;
  color: #111827;
  font-size: 14px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-preview {
  color: #6b7280;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.chat-time {
  color: #9ca3af;
  font-size: 11px;
}

.chat-item-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-item:hover .chat-item-actions {
  opacity: 1;
}

.chat-more-btn {
  padding: 4px;
  color: #6b7280;
  
  &:hover {
    color: #374151;
    background: #f3f4f6;
  }
}

/* 右侧对话内容 */
.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  background: #fafbfc;
}

.chat-title-header {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #ffffff;
}

.empty-chat-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.empty-subtitle {
  color: #6b7280;
  margin-bottom: 32px;
}

.suggestions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  max-width: 600px;
}

.suggestion-item {
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #374151;
  
  &:hover {
    background: #eff6ff;
    border-color: #dbeafe;
    transform: translateY(-1px);
  }
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  gap: 12px;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      background: #eff6ff;
      border: 1px solid #dbeafe;
    }
  }
  
  &.assistant {
    .message-content {
      background: #ffffff;
      border: 1px solid #e5e7eb;
    }
  }
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  .user-avatar {
    background: #3b82f6;
    color: white;
  }
  
  .ai-avatar {
    background: #10b981;
    color: white;
  }
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.message-role {
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
}

.message-time {
  font-size: 11px;
  color: #9ca3af;
}

.message-body {
  line-height: 1.6;
  color: #374151;
}

.text-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.markdown-content {
  :deep(h1), :deep(h2), :deep(h3) {
    margin: 16px 0 8px 0;
    color: #111827;
  }
  
  :deep(p) {
    margin: 8px 0;
  }
  
  :deep(code) {
    background: #f3f4f6;
    padding: 2px 4px;
    border-radius: 4px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  }
  
  :deep(ul), :deep(ol) {
    margin: 8px 0;
    padding-left: 20px;
  }
  
  :deep(li) {
    margin: 4px 0;
  }
}

.code-content {
  background: #1f2937;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 0;
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #374151;
  border-bottom: 1px solid #4b5563;
}

.code-language {
  color: #d1d5db;
  font-size: 12px;
  font-weight: 500;
}

.code-content pre {
  margin: 0;
  padding: 16px;
  overflow-x: auto;
  background: #1f2937;
  
  code {
    color: #f9fafb;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 14px;
    line-height: 1.5;
  }
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
  font-style: italic;
}

/* 输入区域 */
.input-area {
  padding: 20px;
  background: #fafbfc;
  border-top: 1px solid #f3f4f6;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.input-actions {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.message-input {
  flex: 1;
  
  :deep(.el-textarea__inner) {
    border: none;
    box-shadow: none;
    resize: none;
    font-size: 14px;
    line-height: 1.5;
    
    &:focus {
      box-shadow: none;
    }
  }
}

.send-button {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.input-tip {
  font-size: 12px;
  color: #9ca3af;
}

.model-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-chat-container {
    flex-direction: column;
  }
  
  .chat-list-sidebar {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .chat-content {
    height: calc(100vh - 200px);
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .suggestions {
    grid-template-columns: 1fr;
  }
}
</style>
```

## ⚠️ 重要注意事项

1. **流式响应**：实现打字机效果，支持实时显示AI回复
2. **代码高亮**：支持多种编程语言的语法高亮
3. **Markdown渲染**：支持富文本内容显示
4. **对话管理**：支持新建、编辑、删除、置顶对话
5. **响应式设计**：移动端适配，支持双栏和单栏布局
6. **国际化支持**：所有文本使用$t()函数
7. **Mock数据**：包含TODO注释，便于后端集成
8. **用户体验**：加载状态、错误处理、操作反馈等

**最终要求：生成的代码必须与原型图的视觉效果和交互逻辑完全一致！**
