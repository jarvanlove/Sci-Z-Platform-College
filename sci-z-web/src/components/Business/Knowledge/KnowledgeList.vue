<!--
/**
 * @description 知识库列表页面
 * 展示所有知识库，支持搜索、筛选、分页等功能
 * 三栏布局：左侧知识库列表、中间内容区域、右侧AI助手
 */
-->
<template>
  <div class="knowledge-base-container">
    <!-- 左侧导航栏 -->
    <div class="kb-list-sidebar">
      <div class="kb-list-header">
        <div class="kb-header-title">
          <el-icon><FolderOpened /></el-icon>
          {{ $t('knowledge.sharedKnowledgeBase') }}
        </div>
        <div class="kb-header-actions">
          <button
            class="action-icon"
            :title="isContentExpanded ? $t('knowledge.collapseContent') : $t('knowledge.expandContent')"
            @click="toggleContentCollapse"
          >
            <el-icon>
              <component :is="isContentExpanded ? ArrowLeft : ArrowRight" />
            </el-icon>
          </button>
          <button
            class="action-icon"
            :title="$t('knowledge.newSharedKnowledgeBase')"
            @click="showCreateDialog = true"
          >
            <el-icon><Plus /></el-icon>
          </button>
        </div>
      </div>

      <div class="kb-list">
        <div
          v-for="kb in knowledgeBases"
          :key="kb.id"
          class="kb-item"
          :class="{ active: selectedKnowledgeBase && selectedKnowledgeBase.id === kb.id }"
          @click="selectKnowledgeBase(kb)"
        >
          <div class="kb-item-icon">
            <img
              v-if="kb.coverUrl"
              :src="kb.coverUrl"
              alt="cover"
            />
            <div v-else class="kb-item-icon-default">
              <el-icon><Document /></el-icon>
            </div>
          </div>
          <div class="kb-item-info">
            <div class="kb-item-name">{{ kb.name }}</div>
          </div>
          <div class="kb-item-actions" @click.stop>
            <button
              class="kb-action-icon"
              :title="$t('knowledge.deleteKnowledgeBase')"
              @click="deleteKnowledgeBase(kb)"
            >
              <el-icon><Delete /></el-icon>
            </button>
          </div>
        </div>

        <div v-if="knowledgeBases.length === 0" class="kb-empty-tip">
          {{ $t('knowledge.noKnowledgeBase') }}
        </div>
      </div>
    </div>

    <!-- 中间内容区域 -->
    <transition name="main-content-fade">
      <div v-show="isContentExpanded && knowledgeBases.length > 0" class="main-content">
        <div class="content-header">
            <div class="content-title">
              <div class="content-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div>
                <div>
                  {{ selectedKnowledgeBase ? (selectedKnowledgeBase.shortName || selectedKnowledgeBase.name) : $t('knowledge.research') }}
                </div>
                <div class="content-meta">
                  Sci-Z | {{ selectedKnowledgeBase ? getCurrentFileCount() : 0 }}{{ $t('knowledge.contentCount') }}
                </div>
                <div class="content-description">
                  {{ selectedKnowledgeBase ? selectedKnowledgeBase.description : $t('knowledge.researchProjectKb') }}
                </div>
              </div>
            </div>
          </div>

          <div class="content-body">
            <div v-if="selectedKnowledgeBase" class="content-section">
              <div class="section-header">
                <div class="section-title">
                  <template v-if="currentFolder">
                    <span class="kb-breadcrumb">
                      <span class="kb-crumb-link" @click="backToParent">{{ $t('knowledge.content') }}</span>
                      <span>></span>
                      <span>{{ currentFolder.name }}</span>
                    </span>
                  </template>
                  <template v-else>{{ $t('knowledge.content') }}</template>
                </div>
                <div class="section-actions">
                  <button
                    class="action-icon"
                    @click="toggleSearch"
                    :title="$t('knowledge.query')"
                  >
                    <el-icon><Search /></el-icon>
                  </button>
                  <button
                    class="action-icon"
                    @click="createFolder"
                    :title="$t('knowledge.createFolder')"
                  >
                    <el-icon><FolderAdd /></el-icon>
                  </button>
                  <button
                    class="action-icon"
                    @click="triggerKbUpload"
                    :title="$t('knowledge.uploadLocalFile')"
                  >
                    <el-icon><Upload /></el-icon>
                  </button>
                  <input
                    ref="kbUploadInput"
                    type="file"
                    multiple
                    style="display: none"
                    @change="handleKbUpload"
                  />
                </div>
              </div>

              <div
                v-if="showSearch"
                class="search-input-wrap"
                style="margin: 0 0 12px 0"
              >
                <span class="search-left-icon">
                  <el-icon><Search /></el-icon>
                </span>
                <input
                  class="form-input"
                  v-model="kbSearchQuery"
                  :placeholder="$t('knowledge.searchInKb')"
                  style="width: 100%; padding: 10px 40px 10px 36px"
                />
                <div
                  class="search-dismiss"
                  :title="$t('knowledge.closeSearch')"
                  @click="toggleSearch"
                >
                  <el-icon><Close /></el-icon>
                </div>
              </div>

              <div class="content-list">
                <template v-for="item in currentKbDisplayItems" :key="item.id">
                  <div class="content-item" v-if="item.type === 'file'">
                    <div class="content-item-icon">
                      <el-icon><Document /></el-icon>
                    </div>
                    <div class="content-item-info">
                      <div class="content-item-name" :title="item.name">{{ truncateFileName(item.name, 20) }}</div>
                      <div class="content-item-meta">
                        <span>{{ item.ext }}</span>
                        <span>|</span>
                        <span>{{ item.time }}</span>
                      </div>
                    </div>
                    <div class="content-item-actions">
                      <button
                        class="action-icon"
                        :title="$t('common.preview')"
                        @click.stop="handlePreviewFile(item)"
                      >
                        <el-icon><View /></el-icon>
                      </button>
                      <button
                        class="action-icon"
                        :title="$t('knowledge.renameTitle')"
                        @click.stop="renameItem(item)"
                      >
                        <el-icon><Edit /></el-icon>
                      </button>
                      <button
                        class="action-icon"
                        :title="$t('common.delete')"
                        @click.stop="deleteItem(item)"
                      >
                        <el-icon><Delete /></el-icon>
                      </button>
                      <el-dropdown
                        class="content-actions-dropdown"
                        trigger="click"
                        @command="(cmd) => handleContentAction(cmd, item)"
                      >
                        <button class="action-icon more-action" :title="$t('knowledge.moreActions')">
                          <el-icon><MoreFilled /></el-icon>
                        </button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="preview">
                              {{ $t('common.preview') }}
                            </el-dropdown-item>
                            <el-dropdown-item command="rename">
                              {{ $t('knowledge.renameTitle') }}
                            </el-dropdown-item>
                            <el-dropdown-item command="delete" divided>
                              {{ $t('common.delete') }}
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>

                  <div class="content-item" v-else @click="enterFolder(item)">
                    <div class="content-item-icon folder-icon">
                      <el-icon><Folder /></el-icon>
                    </div>
                    <div class="content-item-info">
                      <div class="content-item-name">{{ item.name }}</div>
                      <div class="content-item-meta">
                        <span>{{ (item.files || []).length }} {{ $t('knowledge.fileCount') }}</span>
                      </div>
                    </div>
                    <div class="content-item-actions">
                      <span
                        v-if="(item.files || []).length > 2"
                        style="color: #9ca3af"
                      >▼</span>
                      <button
                        class="action-icon"
                        :title="$t('knowledge.renameTitle')"
                        @click.stop="renameItem(item)"
                      >
                        <el-icon><Edit /></el-icon>
                      </button>
                      <button
                        class="action-icon"
                        :title="$t('common.delete')"
                        @click.stop="deleteItem(item)"
                      >
                        <el-icon><Delete /></el-icon>
                      </button>
                      <el-dropdown
                        class="content-actions-dropdown"
                        trigger="click"
                        @command="(cmd) => handleContentAction(cmd, item)"
                      >
                        <button class="action-icon more-action" :title="$t('knowledge.moreActions')">
                          <el-icon><MoreFilled /></el-icon>
                        </button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="rename">
                              {{ $t('knowledge.renameTitle') }}
                            </el-dropdown-item>
                            <el-dropdown-item command="delete" divided>
                              {{ $t('common.delete') }}
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                </template>
              </div>

              <div
                v-if="currentFolder && currentKbDisplayItems.length === 0"
                class="content-empty"
              >
                <div>{{ $t('knowledge.emptyFolder') }}</div>
              </div>
              <div
                v-else-if="!currentFolder && currentKbDisplayItems.length === 0"
                class="content-empty"
              >
                {{ $t('knowledge.noMoreContent') }}
              </div>
            </div>

            <div v-else class="empty-knowledge-base">
              <div class="empty-icon-large"></div>
              <div class="empty-title">{{ $t('knowledge.emptyKnowledgeBase') }}</div>
              <div class="empty-description">
                {{ $t('knowledge.uploadHint') }}
              </div>
              <button class="upload-files-btn" @click="showCreateDialog = true">
                <el-icon><Plus /></el-icon>
                {{ $t('knowledge.createKnowledgeBase') }}
              </button>
            </div>
          </div>
      </div>
    </transition>

    <!-- 右侧AI助手区域 -->
    <div v-show="knowledgeBases.length > 0" class="ai-assistant" :class="{ 'ai-expanded': !isContentExpanded }">
      <div class="ai-header">
        <div class="ai-header-left">
          <div class="ai-header-title">{{ $t('knowledge.askKnowledgeBase') }}</div>
        </div>
      </div>

      <!-- 对话区 -->
      <div
        class="kb-messages-container"
        :class="{ 'has-messages': kbMessagesList.length > 1 }"
        ref="kbMessages"
      >
        <!-- 空状态显示 -->
        <div v-if="kbMessagesList.length <= 1" class="empty-chat-message">
          {{ $t('knowledge.aiWelcomeMessage') }}
        </div>

        <!-- 消息列表 -->
        <template v-if="kbMessagesList.length > 1">
          <div class="kb-messages">
            <div
              v-for="msg in uniqueMessages"
              :key="msg.id"
              class="kb-message"
              :class="msg.type"
            >
              <div class="kb-message-avatar" :class="msg.type">
                {{ msg.type === 'user' ? $t('knowledge.userLabel') : $t('knowledge.assistantLabel') }}
              </div>
              <div class="kb-message-bubble">
                <div class="kb-message-content-wrapper">
                  <div
                    class="kb-message-content"
                    v-html="formatKbContent(msg.content)"
                  ></div>
                  <!-- 文档片段展示 -->
                  <div v-if="msg.documents && msg.documents.length > 0" class="kb-message-documents">
                    <div class="kb-documents-title">{{ $t('knowledge.referenceDocuments') }}</div>
                    <div class="kb-documents-list">
                      <div
                        v-for="(doc, index) in msg.documents"
                        :key="doc.id || index"
                        class="kb-document-item"
                        :title="doc.content ? doc.content.substring(0, 100) : ''"
                      >
                        <el-icon class="kb-document-icon"><Document /></el-icon>
                        <div class="kb-document-info">
                          <span class="kb-document-name">{{ doc.name || $t('knowledge.documentNumber', { index: index + 1 }) }}</span>
                          <span v-if="doc.datasetName && doc.datasetName !== doc.name" class="kb-document-dataset">（{{ doc.datasetName }}）</span>
                        </div>
                        <span v-if="doc.score" class="kb-document-score">{{ $t('knowledge.relevance') }} {{ (doc.score * 100).toFixed(0) }}%</span>
                      </div>
                    </div>
                  </div>
                  <!-- 流式生成指示器 -->
                  <div v-if="msg.streaming" class="kb-streaming-indicator">
                    <span class="kb-streaming-dot"></span>
                    <span class="kb-streaming-text">{{ $t('knowledge.generating') }}</span>
                  </div>
                </div>
                <div class="kb-message-meta">
                  <span>{{ formatTime(msg.timestamp) }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <div class="kb-input-area">
        <div class="kb-input-container">
          <textarea
            v-model="kbInput"
            class="kb-message-input"
            :placeholder="$t('knowledge.queryPlaceholder')"
            @keydown.enter.exact.prevent="handleEnterKey"
            @input="autoResizeInput"
          ></textarea>

          <!-- 底部控制栏 -->
          <div class="kb-input-bottom-bar">
            <button
              class="kb-send-btn"
              :class="{ active: kbInput.trim() && !isSendingMessage }"
              :disabled="isSendingMessage || isGenerating"
              @click.stop.prevent="() => { console.log('[按钮点击] 发送按钮被点击'); sendKbMessage(); }"
            >
              <el-icon><ArrowUp /></el-icon>
            </button>
          </div>
        </div>
        <div class="ai-footer">{{ $t('knowledge.aiContentHint') }}</div>
      </div>
    </div>

    <!-- 创建知识库弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="$t('knowledge.createSharedKnowledgeBase')"
      width="700px"
      @close="resetCreateForm"
    >
      <el-form :model="newKbForm" label-width="100px">
        <el-form-item :label="$t('knowledge.nameLabel')" required>
          <el-input
            v-model="newKbForm.name"
            :placeholder="$t('knowledge.namePlaceholder')"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item :label="$t('knowledge.coverLabel')">
          <div class="cover-upload">
            <div class="cover-preview" @click="triggerCoverUpload">
              <img
                v-if="newKbForm.coverUrl"
                :src="newKbForm.coverUrl"
                :alt="$t('knowledge.coverAlt')"
              />
              <el-icon v-else><Picture /></el-icon>
              <div class="cover-edit-icon">
                <el-icon><Edit /></el-icon>
              </div>
            </div>
            <div class="cover-upload-text">
              {{ $t('knowledge.coverUploadHint') }}
            </div>
            <input
              ref="coverInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleCoverSelect"
            />
          </div>
        </el-form-item>

        <el-form-item :label="$t('knowledge.descriptionLabel')">
          <el-input
            v-model="newKbForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('knowledge.sharedKbDescriptionPlaceholder')"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="createKnowledgeBase"
          :disabled="!newKbForm.name.trim()"
        >
          {{ $t('knowledge.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 重命名弹窗 -->
    <el-dialog
      v-model="showRenameDialog"
      :title="$t('knowledge.renameTitle')"
      width="500px"
    >
      <el-form>
        <el-form-item :label="$t('knowledge.nameLabel')" required>
          <el-input
            v-model="renameForm"
            :placeholder="$t('knowledge.renamePlaceholder')"
            @keydown.enter.prevent="confirmRename"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelRename">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="confirmRename"
          :disabled="!renameForm.trim()"
        >
          {{ $t('knowledge.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog
      v-model="showDeleteDialog"
      :title="$t('knowledge.deleteConfirmTitle')"
      width="500px"
    >
      <div style="text-align: center; padding: 20px 0">
        <div style="font-size: 16px; color: #374151; margin-bottom: 8px">
          {{ deletingKnowledgeBase ? $t('knowledge.deleteKbConfirm', { name: deletingKnowledgeBase.name }) : $t('knowledge.deleteItemConfirm') }}
        </div>
        <div style="font-size: 14px; color: #6b7280">
          {{ deletingKnowledgeBase ? $t('knowledge.deleteKbWarning') : $t('knowledge.deleteItemWarning') }}
        </div>
      </div>
      <template #footer>
        <el-button @click="cancelDelete">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="danger"
          @click="confirmDelete"
        >
          {{ $t('common.delete') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 文件预览弹窗 - 使用 FilePreview 组件 -->
    <FilePreview
      v-model="showPreviewDialog"
      :file-info="previewFileInfo"
      @close="closePreview"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened,
  Plus,
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  Document,
  Search,
  FolderAdd,
  Upload,
  Close,
  Edit,
  Delete,
  Folder,
  Picture,
  ArrowUp,
  MoreFilled,
  View,
  Loading,
  Warning
} from '@element-plus/icons-vue'
import FilePreview from '@/components/Common/FilePreview.vue'
import {
  getKnowledgeList,
  createKnowledge,
  getKnowledgeFiles,
  createKnowledgeFolder,
  uploadKnowledgeFile,
  uploadFileToKnowledge,
  uploadFilesToKnowledge,
  renameKnowledgeFile,
  deleteKnowledgeFile,
  deleteKnowledgeFolder,
  deleteKnowledge,
  searchKnowledge,
  // 文件关联接口
  getKnowledgeFileRelationList,
  createKnowledgeFileRelation,
  updateKnowledgeFileRelation,
  deleteKnowledgeFileRelation,
  // 流式对话接口
  streamKnowledgeChatbot
} from '@/api/Knowledge/knowledge'
import { createLogger } from '@/utils/simpleLogger'

const logger = createLogger('KnowledgeList')
const { t } = useI18n()

// 响应式数据
const isContentExpanded = ref(true)
const knowledgeBases = ref([])
const selectedKnowledgeBase = ref(null)
const kbItems = ref([])
const kbContents = ref({}) // kbId -> items
const currentFolder = ref(null)
const kbSearchQuery = ref('')
const showSearch = ref(false)
const kbUploadInput = ref(null)

// 创建知识库相关
const showCreateDialog = ref(false)
const newKbForm = ref({
  name: '',
  description: '',
  coverUrl: ''
})
const coverInput = ref(null)

// 重命名和删除相关
const showRenameDialog = ref(false)
const showDeleteDialog = ref(false)
const editingItem = ref(null)
const deletingItem = ref(null)
const deletingKnowledgeBase = ref(null) // 要删除的知识库
const renameForm = ref('')

// 文件预览相关 - 使用 FilePreview 组件
const showPreviewDialog = ref(false)
const previewFileInfo = ref(null)

// 右侧AI助手相关
const kbMessagesList = ref([
  {
    id: 1,
    type: 'ai',
    content: t('knowledge.aiWelcomeMessage'),
    timestamp: new Date()
  }
])
const kbInput = ref('')
const kbMessages = ref(null)
const isGenerating = ref(false)
const isSendingMessage = ref(false) // 发送消息的锁，防止重复调用
const currentAbortController = ref(null)
const currentConversationId = ref(null)
const currentDocuments = ref([]) // 当前回答使用的文档片段
const lastUserMessageId = ref(null) // 最后发送的用户消息ID，用于清除重复
const sendingMessageKey = ref(null) // 当前正在发送的消息唯一标识（内容+时间戳），用于防重复

// 计算属性
const currentKbDisplayItems = computed(() => {
  const q = kbSearchQuery.value.trim().toLowerCase()
  const list = currentFolder.value
    ? currentFolder.value.files || []
    : kbItems.value
  if (!q) return list
  return list.filter((it) =>
    (it.name || '').toLowerCase().includes(q)
  )
})

// 去重消息列表（基于ID和内容去重，保留最后一个）
const uniqueMessages = computed(() => {
  const messages = kbMessagesList.value.slice(1) // 跳过第一条初始消息
  const seenById = new Map() // 基于ID去重
  const seenByContent = new Map() // 基于内容和时间戳去重（防止相同内容重复）
  const result = []
  
  // 从后往前遍历，保留最后一个出现的消息（最新的）
  for (let i = messages.length - 1; i >= 0; i--) {
    const msg = messages[i]
    
    // 首先检查ID是否重复
    if (seenById.has(msg.id)) {
      logger.warn('发现重复ID的消息，已过滤', { 
        id: msg.id, 
        type: msg.type,
        content: msg.content?.substring(0, 50) 
      })
      continue
    }
    
    // 对于用户消息，还要检查内容和时间戳（防止相同内容在短时间内重复）
    if (msg.type === 'user' && msg.content) {
      const timestamp = msg.timestamp ? new Date(msg.timestamp).getTime() : 0
      const timeWindow = Math.floor(timestamp / 1000) // 按秒分组
      const contentKey = `${msg.type}-${msg.content}-${timeWindow}`
      if (seenByContent.has(contentKey)) {
        logger.warn('发现重复内容的用户消息，已过滤', { 
          id: msg.id, 
          content: msg.content?.substring(0, 50),
          timestamp: msg.timestamp
        })
        continue
      }
      seenByContent.set(contentKey, true)
    }
    
    seenById.set(msg.id, true)
    result.unshift(msg) // 使用 unshift 保持顺序
  }
  
  return result
})

// 方法
const loadKnowledgeBases = async () => {
  try {
    logger.info('加载知识库列表')
    const response = await getKnowledgeList({ page: 1, size: 100 })
    if (response.code === 200 && response.data) {
      knowledgeBases.value = response.data.records || response.data.list || []
      logger.info('知识库列表加载成功', { 
        count: knowledgeBases.value.length,
        items: knowledgeBases.value.map(kb => ({
          id: kb.id,
          name: kb.name,
          difyKnowdataId: kb.difyKnowdataId,
          difyKbId: kb.difyKbId
        }))
      })
      
      // 默认选中第一条数据
      if (knowledgeBases.value.length > 0) {
        selectKnowledgeBase(knowledgeBases.value[0])
      }
    } else {
      ElMessage.warning('获取知识库列表失败，请稍后重试')
      knowledgeBases.value = []
    }
  } catch (error) {
    logger.error('加载知识库列表失败', error)
    ElMessage.error('加载知识库列表失败')
    knowledgeBases.value = []
  }
}

const selectKnowledgeBase = (kb) => {
  selectedKnowledgeBase.value = kb
  const kbId = Number(kb.id)
  if (!kbContents.value[kbId]) {
    kbContents.value[kbId] = []
  }
  kbItems.value = kbContents.value[kbId]
  currentFolder.value = null
  kbSearchQuery.value = ''
  
  // 记录选中的知识库信息，用于调试
  logger.info('选中知识库', {
    id: kb.id,
    name: kb.name,
    difyKnowdataId: kb.difyKnowdataId,
    difyKbId: kb.difyKbId,
    allFields: Object.keys(kb) // 记录所有字段，便于调试
  })
  
  // 检查 Dify KB ID 是否存在
  const difyKbId = kb.difyKnowdataId || kb.difyKbId
  if (!difyKbId) {
    logger.warn('选中的知识库缺少 Dify KB ID', {
      kbId: kb.id,
      kbName: kb.name,
      allFields: Object.keys(kb),
      fieldValues: {
        difyKnowdataId: kb.difyKnowdataId,
        difyKbId: kb.difyKbId,
        dify_knowdata_id: kb.dify_knowdata_id,
        dify_kb_id: kb.dify_kb_id
      }
    })
    ElMessage.warning(t('knowledge.kbMissingDifyId'))
  }
  
  // 加载知识库文件列表，确保 ID 是数字类型
  loadKnowledgeFiles(kbId)
}

/**
 * 加载知识库文件列表（使用文件关联接口）
 * @param {number} knowledgeId - 知识库ID
 * @param {number} [folderId] - 文件夹ID，可选
 */
const loadKnowledgeFiles = async (knowledgeId, folderId = null) => {
  if (!knowledgeId) return

  try {
    // 确保 ID 是数字类型
    const kbId = Number(knowledgeId)
    const fId = folderId !== null ? Number(folderId) : null
    
    logger.info('加载知识库文件列表', { knowledgeId: kbId, folderId: fId })
    const params = {
      knowledgeId: kbId,
      page: 1,
      size: 1000 // 获取所有文件
    }
    if (fId !== null) {
      params.folderId = fId
    }

    const response = await getKnowledgeFileRelationList(params)
    
    if (response.code === 200 && response.data) {
      const records = response.data.records || []
      
      // 转换为页面显示格式
      const items = records.map(record => ({
        id: record.id,
        type: 'file',
        name: record.fileName || t('knowledge.unnamedFile'),
        attachmentId: record.attachmentId,
        folderId: record.folderId,
        sortOrder: record.sortOrder,
        time: record.createdTime || record.createdTime || '',
        ext: (record.fileName || '').split('.').pop() || '',
        callback: record.callback ? (typeof record.callback === 'string' ? JSON.parse(record.callback) : record.callback) : null
      }))

      if (fId !== null) {
        // 更新文件夹内的文件列表
        if (currentFolder.value) {
          currentFolder.value.files = items
        }
      } else {
        // 更新根目录文件列表
        kbContents.value[kbId] = items
        kbItems.value = items
      }
      
      logger.info('文件列表加载成功', { count: items.length })
    } else {
      logger.warn('获取文件列表失败', response)
      if (fId !== null && currentFolder.value) {
        currentFolder.value.files = []
      } else {
        kbContents.value[kbId] = []
        kbItems.value = []
      }
    }
  } catch (error) {
    logger.error('加载文件列表失败', error)
    ElMessage.error(t('knowledge.loadFilesError'))
    if (fId !== null && currentFolder.value) {
      currentFolder.value.files = []
    } else {
      kbContents.value[kbId] = []
      kbItems.value = []
    }
  }
}

const createKnowledgeBase = async () => {
  if (!newKbForm.value.name.trim()) {
    ElMessage.warning(t('knowledge.enterKbName'))
    return
  }

  try {
    logger.info('创建知识库', newKbForm.value)
    const response = await createKnowledge({
      name: newKbForm.value.name,
      description: newKbForm.value.description,
      permission: 'only_me',
      indexingTechnique: 'high_quality'
    })

    if (response.code === 200 && response.data) {
      const newKb = {
        id: response.data.id,
        name: response.data.name,
        description: response.data.description,
        coverUrl: newKbForm.value.coverUrl,
        docCount: 0,
        updatedAt: new Date(),
        shortName: response.data.name,
        // 确保设置 Dify KB ID 字段
        difyKnowdataId: response.data.difyKnowdataId,
        difyKbId: response.data.difyKbId
      }
      logger.info('创建知识库成功', {
        id: newKb.id,
        name: newKb.name,
        difyKnowdataId: newKb.difyKnowdataId,
        difyKbId: newKb.difyKbId
      })
      knowledgeBases.value.unshift(newKb)
      kbContents.value[newKb.id] = []
      selectedKnowledgeBase.value = newKb
      kbItems.value = []
      showCreateDialog.value = false
      resetCreateForm()
      ElMessage.success(t('knowledge.knowledgeCreated'))
    } else {
      ElMessage.error(response.message || t('knowledge.createKbFailed'))
    }
  } catch (error) {
    logger.error('创建知识库失败', error)
    ElMessage.error(t('knowledge.createKbFailedRetry'))
  }
}

const resetCreateForm = () => {
  newKbForm.value = {
    name: '',
    description: '',
    coverUrl: ''
  }
}

const triggerCoverUpload = () => {
  coverInput.value && coverInput.value.click()
}

// 截断文件名
const truncateFileName = (fileName, maxLength = 20) => {
  if (!fileName) return ''
  if (fileName.length <= maxLength) return fileName
  return fileName.substring(0, maxLength) + '...'
}

const handleCoverSelect = (e) => {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning(t('knowledge.selectImageFile'))
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    newKbForm.value.coverUrl = reader.result
  }
  reader.readAsDataURL(file)
}

const toggleSearch = () => {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    kbSearchQuery.value = ''
  }
}

const toggleContentCollapse = () => {
  isContentExpanded.value = !isContentExpanded.value
}

const handleContentAction = (command, item) => {
  if (command === 'preview') {
    handlePreviewFile(item)
  } else if (command === 'rename') {
    renameItem(item)
  } else if (command === 'delete') {
    deleteItem(item)
  }
}

/**
 * 预览文件 - 使用 FilePreview 组件
 * @param {Object} item - 文件项
 */
const handlePreviewFile = (item) => {
  if (!item || !item.attachmentId) {
    ElMessage.warning(t('knowledge.fileIdNotExists'))
    return
  }

  // 🔥 修复：使用 FilePreview 组件，直接设置文件信息即可
  previewFileInfo.value = {
    name: item.name,
    attachmentId: item.attachmentId
  }
  showPreviewDialog.value = true
  
  logger.info('打开文件预览', { 
    fileName: item.name, 
    attachmentId: item.attachmentId 
  })
}

/**
 * 关闭预览
 */
const closePreview = () => {
  showPreviewDialog.value = false
  previewFileInfo.value = null
}

const createFolder = () => {
  // TODO: 后端接口待实现
  ElMessage.info(t('knowledge.createFolderPending'))
}

const triggerKbUpload = () => {
  kbUploadInput.value && kbUploadInput.value.click()
}

const handleKbUpload = async (e) => {
  const files = e.target.files || []
  if (!files.length || !selectedKnowledgeBase.value) return

  // 获取当前文件夹ID（如果有），确保是数字类型
  const folderId = currentFolder.value ? Number(currentFolder.value.id) : 0
  const knowledgeId = String(selectedKnowledgeBase.value.id) // 后端接口使用 String 类型的知识库ID

  try {
    logger.info('开始批量上传文件', { 
      fileCount: files.length, 
      knowledgeId,
      folderId
    })

    // 使用批量上传接口（后端已处理文件关联的创建）
    const response = await uploadFilesToKnowledge(knowledgeId, files, folderId)
    
    if (response.code === 200) {
      ElMessage.success(t('knowledge.uploadSuccess', { count: files.length }))
      logger.info('文件批量上传成功', { fileCount: files.length })
      // 上传成功后刷新文件列表
      await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id), folderId || null)
    } else {
      throw new Error(response.message || t('knowledge.uploadFailed'))
    }
  } catch (error) {
    logger.error('批量上传文件失败', error)
    ElMessage.error(error.message || t('knowledge.uploadFailedRetry'))
  } finally {
    e.target.value = ''
  }
}

const enterFolder = async (folder) => {
  if (!folder || folder.type !== 'folder') return
  currentFolder.value = folder
  
  // 加载文件夹内的文件列表，确保 ID 是数字类型
  if (selectedKnowledgeBase.value) {
    await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id), Number(folder.id))
  }
}

const backToParent = async () => {
  currentFolder.value = null
  
  // 返回根目录时重新加载文件列表，确保 ID 是数字类型
  if (selectedKnowledgeBase.value) {
    await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id))
  }
}

const renameItem = (item) => {
  editingItem.value = item
  renameForm.value = item.name
  showRenameDialog.value = true
}

const confirmRename = async () => {
  if (!renameForm.value.trim() || !editingItem.value) {
    ElMessage.warning(t('knowledge.enterNewName'))
    return
  }

  try {
    if (editingItem.value.type === 'file') {
      // 使用文件关联接口更新文件名
      await updateKnowledgeFileRelation(editingItem.value.id, {
        fileName: renameForm.value.trim()
      })
      editingItem.value.name = renameForm.value.trim()
      ElMessage.success(t('knowledge.renameSuccess'))
    } else {
      // TODO: 文件夹重命名接口待实现
      ElMessage.info(t('knowledge.renameFolderPending'))
      editingItem.value.name = renameForm.value.trim()
      ElMessage.success(t('knowledge.renameSuccess'))
    }
    showRenameDialog.value = false
    editingItem.value = null
    renameForm.value = ''
  } catch (error) {
    logger.error('重命名失败', error)
    ElMessage.error(error.message || t('knowledge.renameFailed'))
  }
}

const cancelRename = () => {
  showRenameDialog.value = false
  editingItem.value = null
  renameForm.value = ''
}

const deleteItem = (item) => {
  deletingItem.value = item
  deletingKnowledgeBase.value = null
  showDeleteDialog.value = true
}

const cancelDelete = () => {
  showDeleteDialog.value = false
  deletingItem.value = null
  deletingKnowledgeBase.value = null
}

/**
 * 删除知识库
 * @param {Object} kb - 知识库对象
 */
const deleteKnowledgeBase = (kb) => {
  deletingKnowledgeBase.value = kb
  showDeleteDialog.value = true
}

/**
 * 确认删除（知识库或文件）
 */
const confirmDelete = async () => {
  // 删除知识库
  if (deletingKnowledgeBase.value) {
    try {
      await deleteKnowledge(deletingKnowledgeBase.value.id)
      
      // 从列表中移除
      const idx = knowledgeBases.value.findIndex(
        (kb) => kb.id === deletingKnowledgeBase.value.id
      )
      if (idx > -1) {
        knowledgeBases.value.splice(idx, 1)
      }
      
      // 如果删除的是当前选中的知识库，清空选中状态
      if (selectedKnowledgeBase.value && selectedKnowledgeBase.value.id === deletingKnowledgeBase.value.id) {
        selectedKnowledgeBase.value = null
        kbItems.value = []
        currentFolder.value = null
        kbContents.value = {}
        // 清空对话记录
        kbMessagesList.value = [{
          id: 1,
          type: 'ai',
          content: t('knowledge.aiWelcomeMessage'),
          timestamp: new Date()
        }]
        currentConversationId.value = null
      }
      
      // 从缓存中移除
      delete kbContents.value[deletingKnowledgeBase.value.id]
      
      ElMessage.success(t('knowledge.knowledgeDeleted'))
      logger.info('删除知识库成功', { id: deletingKnowledgeBase.value.id })
    } catch (error) {
      logger.error('删除知识库失败', error)
      ElMessage.error(error.message || t('knowledge.knowledgeDeleted'))
    }
    showDeleteDialog.value = false
    deletingKnowledgeBase.value = null
    return
  }
  
  // 删除文件（原有逻辑）
  if (!deletingItem.value) return

  try {
    if (deletingItem.value.type === 'file') {
      // 使用文件关联接口删除文件
      await deleteKnowledgeFileRelation(deletingItem.value.id)
      
      // 从列表中移除
      const targetList = currentFolder.value
        ? currentFolder.value.files
        : kbItems.value
      const idx = targetList.findIndex(
        (it) => it.id === deletingItem.value.id
      )
      if (idx > -1) {
        targetList.splice(idx, 1)
      }
      
      // 如果是在文件夹内，也需要更新根目录的缓存
      if (currentFolder.value && selectedKnowledgeBase.value) {
        const rootItems = kbContents.value[selectedKnowledgeBase.value.id] || []
        const rootIdx = rootItems.findIndex(
          (it) => it.id === deletingItem.value.id
        )
        if (rootIdx > -1) {
          rootItems.splice(rootIdx, 1)
        }
      }
      
      ElMessage.success(t('knowledge.deleted'))
    } else {
      // TODO: 文件夹删除接口待实现
      ElMessage.info(t('knowledge.deleteFolderPending'))
      const targetList = currentFolder.value
        ? currentFolder.value.files
        : kbItems.value
      const idx = targetList.findIndex(
        (it) => it.id === deletingItem.value.id
      )
      if (idx > -1) {
        targetList.splice(idx, 1)
        ElMessage.success(t('knowledge.deleted'))
      }
    }
    showDeleteDialog.value = false
    deletingItem.value = null
  } catch (error) {
    logger.error('删除失败', error)
    ElMessage.error(error.message || t('knowledge.deleteFailed'))
  }
}

const getCurrentFileCount = () => {
  if (!selectedKnowledgeBase.value) return 0
  const currentItems = kbContents.value[selectedKnowledgeBase.value.id]
  return currentItems ? currentItems.length : 0
}

// AI助手相关方法
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

const autoResizeInput = (event) => {
  if (event && event.target) {
    const textarea = event.target
    textarea.style.height = 'auto'
    const scrollHeight = textarea.scrollHeight
    const minHeight = 44
    const maxHeight = 200
    const newHeight = Math.min(
      Math.max(scrollHeight, minHeight),
      maxHeight
    )
    textarea.style.height = newHeight + 'px'
    if (scrollHeight > maxHeight) {
      textarea.style.overflowY = 'auto'
    } else {
      textarea.style.overflowY = 'hidden'
    }
  }
}

// 处理 Enter 键事件（防止与按钮点击冲突）
const handleEnterKey = (event) => {
  console.log('[handleEnterKey] Enter键被按下', { 
    shiftKey: event.shiftKey,
    isSending: isSendingMessage.value, 
    isGenerating: isGenerating.value 
  })
  // 如果按的是 Shift+Enter，允许换行
  if (event.shiftKey) {
    console.log('[handleEnterKey] Shift+Enter，允许换行')
    return
  }
  // 阻止默认行为并停止冒泡
  event.preventDefault()
  event.stopPropagation()
  event.stopImmediatePropagation() // 立即停止事件传播
  console.log('[handleEnterKey] 调用 sendKbMessage')
  // 调用发送消息
  sendKbMessage()
  return false // 额外返回 false 确保阻止
}

// 使用一个标记来确保函数只执行一次（防止并发调用）
let isExecuting = false

const sendKbMessage = async () => {
  console.log('[sendKbMessage] 函数被调用', { 
    isSending: isSendingMessage.value, 
    isGenerating: isGenerating.value,
    isExecuting: isExecuting,
    hasInput: !!kbInput.value?.trim(),
    hasSelectedKb: !!selectedKnowledgeBase.value,
    stack: new Error().stack.split('\n').slice(0, 5).join('\n')
  })
  
  // 防止重复调用：使用同步锁机制，立即检查（必须在函数最开始）
  if (isExecuting) {
    console.warn('[sendKbMessage] 函数正在执行中，忽略重复请求', { 
      sendingMessageKey: sendingMessageKey.value,
      stack: new Error().stack.split('\n').slice(0, 5).join('\n')
    })
    return
  }
  
  if (isSendingMessage.value) {
    console.warn('[sendKbMessage] 正在发送消息，忽略重复请求', { 
      sendingMessageKey: sendingMessageKey.value,
      stack: new Error().stack.split('\n').slice(0, 5).join('\n')
    })
    logger.warn('正在发送消息，忽略重复请求', { 
      sendingMessageKey: sendingMessageKey.value,
      stack: new Error().stack 
    })
    return
  }
  
  // 立即设置执行标记（同步操作，防止并发）
  isExecuting = true
  
  if (isGenerating.value) {
    console.warn('[sendKbMessage] 正在生成回答，忽略重复请求')
    logger.warn('正在生成回答，忽略重复请求', { 
      stack: new Error().stack 
    })
    isExecuting = false
    return
  }
  
  if (!kbInput.value.trim()) {
    console.log('[sendKbMessage] 输入为空，退出')
    isExecuting = false
    return
  }
  
  if (!selectedKnowledgeBase.value) {
    console.warn('[sendKbMessage] 未选择知识库')
    ElMessage.warning(t('knowledge.selectKbFirst'))
    isExecuting = false
    return
  }

  // 检查是否有 Dify KB ID（优先使用 difyKnowdataId，兼容 difyKbId，也兼容下划线命名）
  const kb = selectedKnowledgeBase.value
  const difyKbId = kb.difyKnowdataId || kb.difyKbId || kb.dify_knowdata_id || kb.dify_kb_id
  if (!difyKbId) {
    console.warn('[sendKbMessage] 知识库缺少 Dify KB ID', {
      kbId: kb.id,
      kbName: kb.name,
      allFields: Object.keys(kb),
      fieldValues: {
        difyKnowdataId: kb.difyKnowdataId,
        difyKbId: kb.difyKbId,
        dify_knowdata_id: kb.dify_knowdata_id,
        dify_kb_id: kb.dify_kb_id
      }
    })
    logger.warn('知识库缺少 Dify KB ID', {
      kbId: kb.id,
      kbName: kb.name,
      allFields: Object.keys(kb)
    })
    ElMessage.warning(t('knowledge.kbMissingDifyId'))
    isExecuting = false
    return
  }

  const text = kbInput.value.trim()
  
  // 生成消息唯一标识（仅使用内容，用于防重复）
  const messageKey = text
  
  // 检查是否正在发送相同的消息（防止快速重复点击）
  if (sendingMessageKey.value === messageKey) {
    console.warn('[sendKbMessage] 正在发送相同的消息，忽略重复请求', { 
      messageKey, 
      isSending: isSendingMessage.value
    })
    logger.warn('正在发送相同的消息，忽略重复请求', { 
      messageKey, 
      isSending: isSendingMessage.value,
      stack: new Error().stack 
    })
    isExecuting = false
    return
  }
  
  // 立即设置锁和消息标识，防止并发调用（在所有验证通过后立即设置）
  console.log('[sendKbMessage] 设置锁，开始发送', { messageKey, text: text.substring(0, 50) })
  isSendingMessage.value = true
  sendingMessageKey.value = messageKey
  isGenerating.value = true
  
  console.log('[sendKbMessage] 开始发送消息', { 
    messageKey, 
    text: text.substring(0, 50),
    currentMessageCount: kbMessagesList.value.length,
    timestamp: Date.now()
  })
  logger.info('开始发送消息', { 
    messageKey, 
    text: text.substring(0, 50),
    currentMessageCount: kbMessagesList.value.length,
    timestamp: Date.now()
  })
  
  // 生成唯一的消息ID（使用时间戳 + 随机数，确保唯一性）
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 10000)
  const userMessageId = `${timestamp}-${random}`
  const aiMessageId = `${timestamp + 1}-${random}`
  
  // 检查是否已存在相同的用户消息（防止重复添加）
  const now = Date.now()
  const existingUserMessage = kbMessagesList.value.find(
    m => m.type === 'user' && m.content === text && 
    Math.abs((m.timestamp ? new Date(m.timestamp).getTime() : 0) - now) < 2000 // 2秒内的相同消息视为重复
  )
  
  if (existingUserMessage) {
    logger.warn('检测到重复消息，跳过添加', { 
      text, 
      existingId: existingUserMessage.id,
      timeDiff: Math.abs((existingUserMessage.timestamp ? new Date(existingUserMessage.timestamp).getTime() : 0) - now)
    })
    isSendingMessage.value = false
    isGenerating.value = false
    sendingMessageKey.value = null
    isExecuting = false
    return
  }
  
  // 检查是否已存在相同ID的消息（防止重复添加）
  const existingUserById = kbMessagesList.value.find(m => m.id === userMessageId)
  const existingAiById = kbMessagesList.value.find(m => m.id === aiMessageId)
  
  if (existingUserById || existingAiById) {
    logger.warn('检测到重复ID的消息，跳过添加', { 
      userMessageId, 
      aiMessageId,
      existingUser: !!existingUserById,
      existingAi: !!existingAiById
    })
    isSendingMessage.value = false
    isGenerating.value = false
    sendingMessageKey.value = null
    isExecuting = false
    return
  }
  
  // 在添加前再次检查，确保不会重复添加
  const existingUserBeforeAdd = kbMessagesList.value.find(m => m.id === userMessageId)
  const existingAiBeforeAdd = kbMessagesList.value.find(m => m.id === aiMessageId)
  
  if (existingUserBeforeAdd || existingAiBeforeAdd) {
    logger.warn('添加前检测到重复消息，跳过添加', { 
      userMessageId, 
      aiMessageId,
      existingUser: !!existingUserBeforeAdd,
      existingAi: !!existingAiBeforeAdd,
      stack: new Error().stack
    })
    isSendingMessage.value = false
    isGenerating.value = false
    sendingMessageKey.value = null
    isExecuting = false
    return
  }
  
  console.log('[sendKbMessage] 准备添加消息', { 
    userMessageId, 
    aiMessageId, 
    text: text.substring(0, 50),
    currentMessageCount: kbMessagesList.value.length
  })
  logger.info('添加新消息', { 
    userMessageId, 
    aiMessageId, 
    text: text.substring(0, 50),
    currentMessageCount: kbMessagesList.value.length
  })
  
  // 添加用户消息（只添加一次）
  const userMessage = {
    id: userMessageId,
    type: 'user',
    content: text,
    timestamp: new Date()
  }
  
  // 再次检查是否已存在（防止并发添加）
  const checkBeforePush = kbMessagesList.value.find(m => m.id === userMessageId)
  if (!checkBeforePush) {
    console.log('[sendKbMessage] 添加用户消息到列表', { id: userMessageId, content: text.substring(0, 50) })
    kbMessagesList.value.push(userMessage)
    console.log('[sendKbMessage] 用户消息已添加，当前消息数:', kbMessagesList.value.length)
    logger.info('用户消息已添加', { id: userMessageId, content: text.substring(0, 50) })
  } else {
    console.warn('[sendKbMessage] 用户消息已存在，跳过添加', { id: userMessageId })
    logger.warn('用户消息已存在，跳过添加', { id: userMessageId })
  }
  
  // 保存最后发送的用户消息ID，用于后续清除重复
  lastUserMessageId.value = userMessageId
  kbInput.value = ''

  // 创建AI消息占位
  const aiMessage = {
    id: aiMessageId,
    type: 'ai',
    content: '',
    timestamp: new Date(),
    streaming: true,
    documents: [] // 文档片段数据
  }
  
  // 再次检查是否已存在（防止并发添加）
  const checkAiBeforePush = kbMessagesList.value.find(m => m.id === aiMessageId)
  if (!checkAiBeforePush) {
    kbMessagesList.value.push(aiMessage)
    logger.info('AI消息占位已添加', { id: aiMessageId })
  } else {
    logger.warn('AI消息占位已存在，跳过添加', { id: aiMessageId })
  }
  
  logger.info('消息添加完成', { 
    totalMessages: kbMessagesList.value.length,
    userMessages: kbMessagesList.value.filter(m => m.type === 'user').length,
    aiMessages: kbMessagesList.value.filter(m => m.type === 'ai').length
  })

  // 重置文档列表
  currentDocuments.value = []

  nextTick(() => {
    const inputElement = document.querySelector('.kb-message-input')
    if (inputElement) {
      inputElement.style.height = '44px'
      inputElement.style.overflowY = 'hidden'
    }
    scrollKbToBottom()
  })

  try {
    logger.info('开始流式问答', { 
      knowledgeId: difyKbId, 
      query: text,
      conversationId: currentConversationId.value 
    })

    // 调用流式对话接口
    const abortController = await streamKnowledgeChatbot({
      knowledgeId: difyKbId,
      query: text,
      conversationId: currentConversationId.value,
      onMessage: (answer) => {
        // 追加回答内容
        const message = kbMessagesList.value.find(m => m.id === aiMessageId)
        if (message) {
          message.content += answer
          nextTick(scrollKbToBottom)
        }
      },
      onEnd: (data) => {
        // 消息结束，保存会话ID和文档片段
        // 注意：只更新已存在的消息，不要添加新消息
        const message = kbMessagesList.value.find(m => m.id === aiMessageId)
        if (message) {
          message.streaming = false
          message.documents = data.documents || []
          currentDocuments.value = data.documents || []
          
          // 如果后端返回了 messageId，更新消息ID（但不要添加新消息）
          if (data.messageId && data.messageId !== aiMessageId) {
            logger.info('更新AI消息ID', { oldId: aiMessageId, newId: data.messageId })
            message.id = data.messageId
          }
        } else {
          logger.warn('未找到AI消息，可能已被删除', { aiMessageId })
        }
        
        // AI返回消息后，检查并清除重复的用户消息（保留最后一条）
        if (lastUserMessageId.value) {
          const lastUserMessage = kbMessagesList.value.find(m => m.id === lastUserMessageId.value)
          if (lastUserMessage && lastUserMessage.content) {
            const content = lastUserMessage.content.trim()
            // 找出所有相同内容的用户消息
            const duplicateMessages = kbMessagesList.value.filter(
              m => m.type === 'user' && m.content?.trim() === content && m.id !== lastUserMessageId.value
            )
            
            if (duplicateMessages.length > 0) {
              // 删除重复的消息（保留最后一条）
              duplicateMessages.forEach(dupMsg => {
                const index = kbMessagesList.value.findIndex(m => m.id === dupMsg.id)
                if (index > -1) {
                  logger.info('清除重复的用户消息', { 
                    content: content.substring(0, 50), 
                    id: dupMsg.id,
                    index
                  })
                  kbMessagesList.value.splice(index, 1)
                }
              })
              logger.info('已清除重复的用户消息', { 
                removedCount: duplicateMessages.length,
                content: content.substring(0, 50)
              })
            }
          }
          // 清除标记
          lastUserMessageId.value = null
        }
        
        if (data.conversationId) {
          currentConversationId.value = data.conversationId
        }
        
        isSendingMessage.value = false
        isGenerating.value = false
        sendingMessageKey.value = null
        isExecuting = false
        currentAbortController.value = null
        nextTick(scrollKbToBottom)
        logger.info('流式问答完成', { 
          conversationId: data.conversationId,
          messageId: data.messageId,
          documentCount: (data.documents || []).length,
          totalMessages: kbMessagesList.value.length
        })
      },
      onError: (error) => {
        isSendingMessage.value = false
        isGenerating.value = false
        sendingMessageKey.value = null
        isExecuting = false
        currentAbortController.value = null
        
        // 处理错误
        const message = kbMessagesList.value.find(m => m.id === aiMessageId)
        if (message) {
          if (error.code === 'CHATBOT_NOT_CREATED') {
            message.content = error.hint || t('knowledge.chatbotNotCreated')
            ElMessage.warning(error.message || t('knowledge.chatbotNotCreatedShort'))
          } else {
            message.content = t('knowledge.responseFailed')
            ElMessage.error(error.message || t('knowledge.responseFailedShort'))
          }
          message.streaming = false
        }
        
        // 即使出错，也清除重复的用户消息
        if (lastUserMessageId.value) {
          const lastUserMessage = kbMessagesList.value.find(m => m.id === lastUserMessageId.value)
          if (lastUserMessage && lastUserMessage.content) {
            const content = lastUserMessage.content.trim()
            const duplicateMessages = kbMessagesList.value.filter(
              m => m.type === 'user' && m.content?.trim() === content && m.id !== lastUserMessageId.value
            )
            if (duplicateMessages.length > 0) {
              duplicateMessages.forEach(dupMsg => {
                const index = kbMessagesList.value.findIndex(m => m.id === dupMsg.id)
                if (index > -1) {
                  kbMessagesList.value.splice(index, 1)
                }
              })
              logger.info('错误处理中清除重复的用户消息', { removedCount: duplicateMessages.length })
            }
          }
          lastUserMessageId.value = null
        }
        
        logger.error('流式问答失败', error)
        nextTick(scrollKbToBottom)
      }
    })

    currentAbortController.value = abortController
  } catch (error) {
    isSendingMessage.value = false
    isGenerating.value = false
    sendingMessageKey.value = null
    isExecuting = false
    currentAbortController.value = null
    const message = kbMessagesList.value.find(m => m.id === aiMessageId)
    if (message) {
      message.content = t('knowledge.responseFailed')
      message.streaming = false
    }
    
    // 即使异常，也清除重复的用户消息
    if (lastUserMessageId.value) {
      const lastUserMessage = kbMessagesList.value.find(m => m.id === lastUserMessageId.value)
      if (lastUserMessage && lastUserMessage.content) {
        const content = lastUserMessage.content.trim()
        const duplicateMessages = kbMessagesList.value.filter(
          m => m.type === 'user' && m.content?.trim() === content && m.id !== lastUserMessageId.value
        )
        if (duplicateMessages.length > 0) {
          duplicateMessages.forEach(dupMsg => {
            const index = kbMessagesList.value.findIndex(m => m.id === dupMsg.id)
            if (index > -1) {
              kbMessagesList.value.splice(index, 1)
            }
          })
          logger.info('异常处理中清除重复的用户消息', { removedCount: duplicateMessages.length })
        }
      }
      lastUserMessageId.value = null
    }
    
    logger.error('流式问答异常', error)
    ElMessage.error(error.message || t('knowledge.responseFailedShort'))
    nextTick(scrollKbToBottom)
  }
}

onMounted(() => {
  loadKnowledgeBases()
})
</script>

<style lang="scss" scoped>
// 知识库页面主容器 - 完全按照原型图实现：100vh 固定高度，内部模块独立滚动
.knowledge-base-container {
  display: flex;
  height: 100vh; // 原型图使用 100vh
  width: 100%;
  background: var(--bg);
  overflow: visible; // 原型图使用 visible
  position: relative; // 原型图使用 relative
}

/* 左侧导航栏 - 共享知识库列表 - 完全按照原型图实现 */
.kb-list-sidebar {
  width: 280px;
  background: var(--surface);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden; // 原型图：容器本身不滚动
}

.kb-list-header {
  padding: 14px 20px 14px 12px; // 对齐原型图
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0; // 防止被压缩
}

.kb-header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.kb-header-actions {
  display: flex;
  gap: 8px;
}

// .kb-list-sidebar:not(.is-open) .kb-toggle-text {
//   display: none;
// }
//
// .kb-list-sidebar:not(.is-open) .kb-toggle-button {
//   justify-content: center;
//   padding: 10px;
// }
//
// .kb-list-sidebar:not(.is-open) .kb-toggle-arrow {
//   transform: rotate(-90deg);
// }
// Legacy sidebar collapse selectors removed

.kb-section {
  margin-bottom: 24px;
  margin-top: 16px;
}

.kb-section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-3);
  margin-bottom: 12px;
  margin-left: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kb-list {
  flex: 1;
  overflow-y: auto; // 原型图：列表内容过多时滚动
  padding: 0 20px;
  min-width: 0; // 防止 flex 子元素溢出
  width: 100%; // 保持宽度不变
  box-sizing: border-box; // 包含 padding
  // 防止容器大小变化导致字体变形
  min-height: 0; // 允许 flex 子元素收缩
  max-width: 100%; // 限制最大宽度
}

// 自定义滚动条样式 - 对齐原型图
.kb-list::-webkit-scrollbar {
  width: 6px;
}

.kb-list::-webkit-scrollbar-track {
  background: transparent;
}

.kb-list::-webkit-scrollbar-thumb {
  background: var(--border-hover);
  border-radius: 3px;
}

// .kb-collapse-enter-active,
// .kb-collapse-leave-active {
//   transition: opacity 0.2s ease, transform 0.2s ease;
// }

// .kb-collapse-enter-from,
// .kb-collapse-leave-to {
//   opacity: 0;
//   transform: translateY(-8px);
// }
// Transition helpers for the old sidebar collapse removed

.kb-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 8px;
  background: var(--surface);
  transition: all 0.2s ease;
  width: 100%; // 占满父容器宽度
  box-sizing: border-box; // 包含 padding 和 border
  min-width: 0; // 允许 flex 子元素收缩
  max-width: 100%; // 限制最大宽度，防止溢出
  // 防止字体变形
  font-size: 14px; // 固定字体大小
  line-height: 1.5; // 固定行高
  // 防止缩放时布局变形
  transform: scale(1); // 确保缩放基准
  transform-origin: left center; // 设置缩放原点

  &:hover {
    background: var(--hover-light);
    border-color: var(--border-hover);
  }

  &.active {
    background: var(--hover);
    border-color: var(--color-primary);
  }
}

.kb-item-icon {
  width: 32px;
  height: 32px;
  min-width: 32px; // 固定最小宽度，防止缩放时变小
  min-height: 32px; // 固定最小高度，防止缩放时变小
  max-width: 32px; // 固定最大宽度，防止缩放时变大
  max-height: 32px; // 固定最大高度，防止缩放时变大
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0; // 防止图标被压缩
  box-sizing: border-box; // 包含边框

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block; // 防止图片下方出现空隙
  }
}

.kb-item-icon-default {
  width: 100%;
  height: 100%;
  min-width: 32px; // 固定最小宽度
  min-height: 32px; // 固定最小高度
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b82f6;
  color: #fff;
  // 固定图标大小，不受缩放影响
  font-size: 18px;
  line-height: 1;

  :deep(.el-icon) {
    font-size: 18px;
    width: 18px;
    height: 18px;
    flex-shrink: 0;
  }
}

.kb-item-info {
  flex: 1;
  min-width: 0; // 允许 flex 子元素收缩
  overflow: hidden; // 防止内容溢出
}

.kb-item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  overflow: hidden; // 防止文本溢出
  text-overflow: ellipsis; // 文本过长显示省略号
  white-space: nowrap; // 单行显示
  min-width: 0; // 允许文本截断
  flex-shrink: 1; // 允许收缩
  max-width: 100%; // 限制最大宽度
  display: block; // 确保文本截断生效
}

.kb-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.kb-item:hover .kb-item-actions {
  opacity: 1;
}

.kb-action-icon {
  width: 24px;
  height: 24px;
  min-width: 24px; // 固定最小宽度
  min-height: 24px; // 固定最小高度
  max-width: 24px; // 固定最大宽度
  max-height: 24px; // 固定最大高度
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  flex-shrink: 0; // 防止按钮被压缩
  // 固定图标大小
  font-size: 16px;
  line-height: 1;

  :deep(.el-icon) {
    font-size: 16px;
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }
  transition: all 0.2s ease;

  &:hover {
    background: #fee2e2;
    color: #dc2626;
  }
}

.kb-empty-tip {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

/* 中间内容区域 - 知识库详情 - 完全按照原型图实现 */
.main-content {
  flex: 0 0 calc(100% - 280px - 600px); // 原型图：固定宽度计算
  background: var(--surface);
  display: flex;
  flex-direction: column;
}

.content-header {
  padding: 24px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  flex-shrink: 0; // 防止被压缩
}

.content-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.content-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.content-meta {
  font-size: 14px;
  color: var(--text-3);
  margin-bottom: 4px;
}

.content-description {
  font-size: 14px;
  color: var(--text-3);
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto; // 原型图：文件内容过多时滚动
}

.content-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.kb-breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.kb-crumb-link {
  color: var(--color-primary);
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.section-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.action-icon {
  width: 32px;
  height: 32px;
  min-width: 32px; // 固定最小宽度，防止被压缩
  min-height: 32px; // 固定最小高度，防止被压缩
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #374151;
  transition: all 0.2s ease;
  flex-shrink: 0; // 防止按钮被压缩
  // 确保图标大小固定，不受字体影响
  font-size: 16px;
  line-height: 1;

  :deep(.el-icon) {
    font-size: 16px;
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: var(--hover);
    color: var(--color-primary);
    border-color: var(--color-primary);
    box-shadow: 0 2px 6px rgba(30, 58, 138, 0.15);
  }
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-item {
  padding: 16px;
  background: var(--hover-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  transition: all 0.2s ease;
  align-items: center;

  &:hover {
    background: #f3f4f6;
    border-color: var(--border-hover);
  }
}

.content-item-icon {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1e40af;
  font-size: 18px;

  &.folder-icon {
    background: var(--hover);
    border-color: #dbeafe;
    color: #3b82f6;
  }
}

.content-item-info {
  flex: 1;
  min-width: 0; // 允许 flex 子元素收缩
  overflow: hidden; // 防止内容溢出
}

.content-item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  min-width: 0;
}

.content-item-meta {
  font-size: 12px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap; // 防止按钮换行
  justify-content: flex-end;
  flex-shrink: 0; // 防止按钮区域被压缩
  min-width: 0; // 允许收缩，但不会影响按钮大小
}

.content-actions-dropdown {
  display: none;
}

// .content-collapse-enter-active,
// .content-collapse-leave-active {
//   transition: all 0.3s ease;
//   overflow: hidden;
// }
//
// .content-collapse-enter-from,
// .content-collapse-leave-to {
//   opacity: 0;
//   max-height: 0;
// }
// Legacy content-collapse transitions removed

.content-empty {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
  font-size: 14px;
}

.search-input-wrap {
  position: relative;
}

.search-left-icon {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.search-dismiss {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #6b7280;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: #374151;
  }
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text);
  outline: none;
  transition: all 0.2s ease;

  &:focus {
    border-color: #0ea5e9;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
  }
}

.empty-knowledge-base {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: var(--text-3);
}

.empty-icon-large {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px dashed #0ea5e9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 48px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: var(--text-3);
  margin-bottom: 24px;
  line-height: 1.5;
}

.upload-files-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.15);

  &:hover {
    background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
    transform: translateY(-2px);
  }
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cover-preview {
  width: 96px;
  height: 96px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  border: 2px dashed #0ea5e9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0ea5e9;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &:hover {
    background: linear-gradient(135deg, #bae6fd 0%, #7dd3fc 100%);
    border-color: #0284c7;
  }
}

.cover-edit-icon {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 24px;
  height: 24px;
  background: #0ea5e9;
  border: 2px solid white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.cover-upload-text {
  flex: 1;
  font-size: 14px;
  color: var(--text-3);
}

/* 右侧AI助手区域 - 问知识库 - 完全按照原型图实现 */
.ai-assistant {
  width: 600px;
  background: var(--surface);
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  overflow: visible; // 原型图：使用 visible
}

.ai-assistant.ai-expanded {
  flex: 1 1 auto;
  width: auto;
  min-width: 0;
}

.ai-header {
  padding: 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0; // 防止被压缩
}

.ai-header-left {
  display: flex;
  align-items: center;
}

.ai-header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
}

.kb-messages-container {
  flex: 1;
  overflow-y: auto; // 原型图：对话内容过多时滚动
  overflow-x: visible; // 原型图：使用 visible
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

// 自定义滚动条样式 - 对齐原型图
.kb-messages-container::-webkit-scrollbar {
  width: 6px;
}

.kb-messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.kb-messages-container::-webkit-scrollbar-thumb {
  background: var(--border-hover);
  border-radius: 3px;
}

.empty-chat-message {
  font-size: 16px;
  color: var(--text-3);
  text-align: center;
  line-height: 1.6;
}

.kb-messages {
  width: 100%;
}

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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border);
  background: var(--surface);
  color: #374151;
}

.kb-message.user .kb-message-content-wrapper {
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  color: #fff;
  border-color: transparent;
  border-radius: 16px 16px 4px 16px;
}

.kb-message.user .kb-message-bubble {
  align-self: flex-end;
}

.kb-message.ai .kb-message-content-wrapper {
  border-radius: 16px 16px 16px 4px;
}

.kb-message-content {
  font-size: 14px;
  white-space: pre-wrap;
  word-wrap: break-word;
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
}

.kb-input-area {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: var(--surface);
  flex-shrink: 0; // 防止被压缩
}

.kb-input-container {
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 0;
  transition: all 0.2s ease;
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
}

.kb-input-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 8px 16px 12px 16px;
  border-top: 1px solid #e5e7eb;
  background: transparent;
}

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

.ai-footer {
  padding: 12px 20px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  border-top: 1px solid #f3f4f6;
}

/* 文档片段展示样式 */
.kb-message-documents {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.kb-documents-title {
  font-size: 12px;
  color: var(--text-3);
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
  background: var(--hover-light);
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
}

/* 🔥 修复：文件预览已改为使用 FilePreview 组件，移除旧的预览样式 */

/* 流式生成指示器 */
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
  color: var(--text-3);
}

.main-content-fade-enter-active,
.main-content-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.main-content-fade-enter-from,
.main-content-fade-leave-to {
  opacity: 0;
  transform: translateX(16px);
}

@media (max-width: 1440px) {
  .ai-assistant {
    width: 420px;
  }
}

@media (max-width: 1400px) {
  .knowledge-base-container {
    flex-direction: column;
    height: 100%; // 保持 100% 高度，不使用 min-height 避免产生滚动条
    width: 100%;
    max-width: none;
    overflow: hidden; // 确保不产生整体滚动条
  }

  .kb-list-sidebar {
    width: 100%;
    min-width: 0;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
    flex-shrink: 0; // 防止被压缩
  }

  .kb-list {
    padding: 0 16px 16px;
    max-height: 300px; // 固定最大高度，避免占用过多空间
    overflow-y: auto; // 内容过多时滚动
  }

  .main-content {
    order: 2;
    width: 100%;
    flex: 1 1 auto;
    min-height: 0; // 确保可以正确滚动
    overflow: hidden; // 容器不滚动
  }

  .ai-assistant {
    order: 3;
    width: 100%;
    border-left: none;
    border-top: 1px solid #e5e7eb;
    flex: 1 1 auto;
    min-height: 0; // 确保可以正确滚动
    overflow: hidden; // 容器不滚动
  }
}

@media (max-width: 768px) {
  .content-header {
    padding: 16px;
  }

  .ai-assistant {
    border-top: none;
  }

  .content-item {
    padding: 12px 10px;
  }

  .content-item-meta {
    font-size: 11px;
    flex-wrap: wrap;
    gap: 4px;
  }

  .content-item-actions {
    gap: 4px;
  }

  .action-icon {
    width: 28px;
    height: 28px;
  }

  .content-item-actions .action-icon:not(.more-action) {
    display: none;
  }

  .content-item-actions .action-icon.more-action {
    display: inline-flex;
  }
}
</style>