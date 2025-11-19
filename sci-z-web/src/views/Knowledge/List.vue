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
          共享知识库
        </div>
        <div class="kb-header-actions">
          <button
            class="action-icon"
            :title="isContentExpanded ? '收起内容区' : '展开内容区'"
            @click="toggleContentCollapse"
          >
            <el-icon>
              <component :is="isContentExpanded ? ArrowLeft : ArrowRight" />
            </el-icon>
          </button>
          <button
            class="action-icon"
            title="新建共享知识库"
            @click="showCreateDialog = true"
          >
            <el-icon><Plus /></el-icon>
          </button>
        </div>
      </div>

      <div class="kb-list">
        <!-- 我创建的 -->
        <div class="kb-section">
          <div class="kb-section-title">
            <span>我创建的</span>
            <el-icon class="kb-section-toggle"><ArrowDown /></el-icon>
          </div>

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
                title="删除知识库"
                @click="deleteKnowledgeBase(kb)"
              >
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </div>

          <div v-if="knowledgeBases.length === 0" class="kb-empty-tip">
            暂无知识库，点击上方 + 创建
          </div>
        </div>
      </div>
    </div>

    <!-- 中间内容区域 -->
    <transition name="main-content-fade">
      <div v-show="isContentExpanded" class="main-content">
        <div class="content-wrapper">
          <div class="content-header">
            <div class="content-title">
              <div class="content-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div>
                <div>
                  {{ selectedKnowledgeBase ? (selectedKnowledgeBase.shortName || selectedKnowledgeBase.name) : '科研' }}
                </div>
                <div class="content-meta">
                  Sci-Z | {{ selectedKnowledgeBase ? getCurrentFileCount() : 0 }}个内容
                </div>
                <div class="content-description">
                  {{ selectedKnowledgeBase ? selectedKnowledgeBase.description : '科研项目知识库' }}
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
                      <span class="kb-crumb-link" @click="backToParent">内容</span>
                      <span>></span>
                      <span>{{ currentFolder.name }}</span>
                    </span>
                  </template>
                  <template v-else>内容</template>
                </div>
                <div class="section-actions">
                  <button
                    class="action-icon"
                    @click="toggleSearch"
                    title="查询"
                  >
                    <el-icon><Search /></el-icon>
                  </button>
                  <button
                    class="action-icon"
                    @click="createFolder"
                    title="创建文件夹"
                  >
                    <el-icon><FolderAdd /></el-icon>
                  </button>
                  <button
                    class="action-icon"
                    @click="triggerKbUpload"
                    title="上传本地文件"
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
                  placeholder="在知识库中搜索"
                  style="width: 100%; padding: 10px 40px 10px 36px"
                />
                <div
                  class="search-dismiss"
                  title="关闭搜索"
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
                      <div class="content-item-name">{{ item.name }}</div>
                      <div class="content-item-meta">
                        <span>{{ item.ext }}</span>
                        <span>|</span>
                        <span>{{ item.time }}</span>
                      </div>
                    </div>
                    <div class="content-item-actions">
                      <button
                        class="action-icon"
                        title="重命名"
                        @click.stop="renameItem(item)"
                      >
                        <el-icon><Edit /></el-icon>
                      </button>
                      <button
                        class="action-icon"
                        title="删除"
                        @click.stop="deleteItem(item)"
                      >
                        <el-icon><Delete /></el-icon>
                      </button>
                      <el-dropdown
                        class="content-actions-dropdown"
                        trigger="click"
                        @command="(cmd) => handleContentAction(cmd, item)"
                      >
                        <button class="action-icon more-action" title="更多操作">
                          <el-icon><MoreFilled /></el-icon>
                        </button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="rename">
                              重命名
                            </el-dropdown-item>
                            <el-dropdown-item command="delete" divided>
                              删除
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
                        <span>{{ (item.files || []).length }} 个文件</span>
                      </div>
                    </div>
                    <div class="content-item-actions">
                      <span
                        v-if="(item.files || []).length > 2"
                        style="color: #9ca3af"
                      >▼</span>
                      <button
                        class="action-icon"
                        title="重命名"
                        @click.stop="renameItem(item)"
                      >
                        <el-icon><Edit /></el-icon>
                      </button>
                      <button
                        class="action-icon"
                        title="删除"
                        @click.stop="deleteItem(item)"
                      >
                        <el-icon><Delete /></el-icon>
                      </button>
                      <el-dropdown
                        class="content-actions-dropdown"
                        trigger="click"
                        @command="(cmd) => handleContentAction(cmd, item)"
                      >
                        <button class="action-icon more-action" title="更多操作">
                          <el-icon><MoreFilled /></el-icon>
                        </button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="rename">
                              重命名
                            </el-dropdown-item>
                            <el-dropdown-item command="delete" divided>
                              删除
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
                <div>文件夹什么也没有，去这里添加</div>
              </div>
              <div
                v-else-if="!currentFolder && currentKbDisplayItems.length === 0"
                class="content-empty"
              >
                没有更多内容了
              </div>
            </div>

            <div v-else class="empty-knowledge-base">
              <div class="empty-icon-large"></div>
              <div class="empty-title">知识库什么也没有，去这里添加</div>
              <div class="empty-description">
                上传文档、图片、视频等文件，让AI助手帮你整理和回答问题
              </div>
              <button class="upload-files-btn" @click="showCreateDialog = true">
                <el-icon><Plus /></el-icon>
                创建知识库
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 右侧AI助手区域 -->
    <div class="ai-assistant" :class="{ 'ai-expanded': !isContentExpanded }">
      <div class="ai-header">
        <div class="ai-header-left">
          <div class="ai-header-title">问知识库</div>
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
          Hi，任何关于这个知识库的问题都可以问我
        </div>

        <!-- 消息列表 -->
        <template v-if="kbMessagesList.length > 1">
          <div class="kb-messages">
            <div
              v-for="msg in kbMessagesList.slice(1)"
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
            placeholder="基于知识库提问"
            @keydown.enter.prevent="sendKbMessage"
            @input="autoResizeInput"
          ></textarea>

          <!-- 底部控制栏 -->
          <div class="kb-input-bottom-bar">
            <button
              class="kb-send-btn"
              :class="{ active: kbInput.trim() }"
              @click="sendKbMessage"
            >
              <el-icon><ArrowUp /></el-icon>
            </button>
          </div>
        </div>
        <div class="ai-footer">内容由AI生成仅供参考</div>
      </div>
    </div>

    <!-- 创建知识库弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建共享知识库"
      width="700px"
      @close="resetCreateForm"
    >
      <el-form :model="newKbForm" label-width="100px">
        <el-form-item label="名称" required>
          <el-input
            v-model="newKbForm.name"
            placeholder="请输入知识库名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面">
          <div class="cover-upload">
            <div class="cover-preview" @click="triggerCoverUpload">
              <img
                v-if="newKbForm.coverUrl"
                :src="newKbForm.coverUrl"
                alt="知识库封面"
              />
              <el-icon v-else><Picture /></el-icon>
              <div class="cover-edit-icon">
                <el-icon><Edit /></el-icon>
              </div>
            </div>
            <div class="cover-upload-text">
              点击上传知识库封面图片，建议尺寸 200x200px
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

        <el-form-item label="描述">
          <el-input
            v-model="newKbForm.description"
            type="textarea"
            :rows="3"
            placeholder="为你的共享知识库填写描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="createKnowledgeBase"
          :disabled="!newKbForm.name.trim()"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 重命名弹窗 -->
    <el-dialog
      v-model="showRenameDialog"
      title="重命名"
      width="500px"
    >
      <el-form>
        <el-form-item label="名称" required>
          <el-input
            v-model="renameForm"
            placeholder="请输入新名称"
            @keydown.enter.prevent="confirmRename"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelRename">取消</el-button>
        <el-button
          type="primary"
          @click="confirmRename"
          :disabled="!renameForm.trim()"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog
      v-model="showDeleteDialog"
      title="删除确认"
      width="500px"
    >
      <div style="text-align: center; padding: 20px 0">
        <div style="font-size: 16px; color: #374151; margin-bottom: 8px">
          {{ deletingKnowledgeBase ? `确定要删除知识库"${deletingKnowledgeBase.name}"吗？` : '确定要删除该项吗？' }}
        </div>
        <div style="font-size: 14px; color: #6b7280">
          {{ deletingKnowledgeBase ? '删除知识库将同时删除其中的所有文件和文件夹，此操作不可恢复' : '此操作不可恢复' }}
        </div>
      </div>
      <template #footer>
        <el-button @click="cancelDelete">取消</el-button>
        <el-button
          type="danger"
          @click="confirmDelete"
        >
          删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
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
  MoreFilled
} from '@element-plus/icons-vue'
import {
  getKnowledgeList,
  createKnowledge,
  getKnowledgeFiles,
  createKnowledgeFolder,
  uploadKnowledgeFile,
  uploadFileToKnowledge,
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

// 右侧AI助手相关
const kbMessagesList = ref([
  {
    id: 1,
    type: 'ai',
    content: 'Hi，任何关于这个知识库的问题都可以问我～',
    timestamp: new Date()
  }
])
const kbInput = ref('')
const kbMessages = ref(null)
const isGenerating = ref(false)
const currentAbortController = ref(null)
const currentConversationId = ref(null)
const currentDocuments = ref([]) // 当前回答使用的文档片段

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
    difyKbId: kb.difyKbId
  })
  
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
        name: record.fileName || '未命名文件',
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
    ElMessage.error('加载文件列表失败')
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
    ElMessage.warning('请输入知识库名称')
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
        shortName: response.data.name
      }
      knowledgeBases.value.unshift(newKb)
      kbContents.value[newKb.id] = []
      selectedKnowledgeBase.value = newKb
      kbItems.value = []
      showCreateDialog.value = false
      resetCreateForm()
      ElMessage.success('知识库创建成功')
    } else {
      ElMessage.error(response.message || '创建知识库失败')
    }
  } catch (error) {
    logger.error('创建知识库失败', error)
    ElMessage.error('创建知识库失败，请稍后重试')
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

const handleCoverSelect = (e) => {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
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
  if (command === 'rename') {
    renameItem(item)
  } else if (command === 'delete') {
    deleteItem(item)
  }
}

const createFolder = () => {
  // TODO: 后端接口待实现
  ElMessage.info('创建文件夹功能待后端接口实现')
}

const triggerKbUpload = () => {
  kbUploadInput.value && kbUploadInput.value.click()
}

const handleKbUpload = async (e) => {
  const files = e.target.files || []
  if (!files.length || !selectedKnowledgeBase.value) return

  // 检查是否有 Dify KB ID（优先使用 difyKnowdataId，兼容 difyKbId）
  const difyKbId = selectedKnowledgeBase.value.difyKnowdataId || selectedKnowledgeBase.value.difyKbId
  if (!difyKbId) {
    logger.warn('知识库缺少 Dify KB ID', {
      knowledgeBase: selectedKnowledgeBase.value,
      availableFields: Object.keys(selectedKnowledgeBase.value)
    })
    ElMessage.warning('知识库缺少 Dify KB ID，无法上传文件。请检查知识库是否已正确创建。')
    e.target.value = ''
    return
  }

  // 获取当前文件夹ID（如果有），确保是数字类型
  const folderId = currentFolder.value ? Number(currentFolder.value.id) : 0
  const knowledgeId = Number(selectedKnowledgeBase.value.id)

  try {
    logger.info('开始上传文件', { 
      fileCount: files.length, 
      difyKbId, 
      folderId,
      knowledgeId
    })

    // 逐个上传文件
    const uploadPromises = Array.from(files).map(file => 
      uploadFileToKnowledge(difyKbId, file, folderId)
    )

    const uploadResults = await Promise.all(uploadPromises)
    
    // 上传成功后，需要创建文件关联记录
    // 注意：这里假设上传接口返回的数据中包含 attachmentId 和 callback 信息
    // 如果后端上传接口返回的数据结构不同，需要根据实际情况调整
    try {
      for (let i = 0; i < uploadResults.length; i++) {
        const result = uploadResults[i]
        if (result.code === 200 && result.data) {
          // 创建文件关联记录，确保所有 ID 都是数字类型
          await createKnowledgeFileRelation({
            knowledgeId: knowledgeId,
            folderId: folderId,
            attachmentId: Number(result.data.attachmentId || result.data.id), // 根据实际返回结构调整
            fileName: files[i].name,
            sortOrder: 0,
            callback: result.data.callback ? JSON.stringify(result.data.callback) : null
          })
        }
      }
    } catch (relationError) {
      logger.warn('创建文件关联失败', relationError)
      // 即使关联创建失败，也提示上传成功（文件已上传到Dify）
    }
    
    ElMessage.success(`成功上传 ${files.length} 个文件`)
    logger.info('文件上传成功', { fileCount: files.length })

    // 上传成功后刷新文件列表
    await loadKnowledgeFiles(knowledgeId, folderId || null)
  } catch (error) {
    logger.error('上传文件失败', error)
    ElMessage.error(error.message || '上传文件失败，请稍后重试')
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
    ElMessage.warning('请输入新名称')
    return
  }

  try {
    if (editingItem.value.type === 'file') {
      // 使用文件关联接口更新文件名
      await updateKnowledgeFileRelation(editingItem.value.id, {
        fileName: renameForm.value.trim()
      })
      editingItem.value.name = renameForm.value.trim()
      ElMessage.success('重命名成功')
    } else {
      // TODO: 文件夹重命名接口待实现
      ElMessage.info('重命名文件夹功能待后端接口实现')
      editingItem.value.name = renameForm.value.trim()
      ElMessage.success('重命名成功')
    }
    showRenameDialog.value = false
    editingItem.value = null
    renameForm.value = ''
  } catch (error) {
    logger.error('重命名失败', error)
    ElMessage.error(error.message || '重命名失败')
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
          content: 'Hi，任何关于这个知识库的问题都可以问我～',
          timestamp: new Date()
        }]
        currentConversationId.value = null
      }
      
      // 从缓存中移除
      delete kbContents.value[deletingKnowledgeBase.value.id]
      
      ElMessage.success('知识库已删除')
      logger.info('删除知识库成功', { id: deletingKnowledgeBase.value.id })
    } catch (error) {
      logger.error('删除知识库失败', error)
      ElMessage.error(error.message || '删除知识库失败')
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
      
      ElMessage.success('已删除')
    } else {
      // TODO: 文件夹删除接口待实现
      ElMessage.info('删除文件夹功能待后端接口实现')
      const targetList = currentFolder.value
        ? currentFolder.value.files
        : kbItems.value
      const idx = targetList.findIndex(
        (it) => it.id === deletingItem.value.id
      )
      if (idx > -1) {
        targetList.splice(idx, 1)
        ElMessage.success('已删除')
      }
    }
    showDeleteDialog.value = false
    deletingItem.value = null
  } catch (error) {
    logger.error('删除失败', error)
    ElMessage.error(error.message || '删除失败')
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

const sendKbMessage = async () => {
  if (!kbInput.value.trim()) return
  if (isGenerating.value) {
    ElMessage.warning('正在生成回答，请稍候...')
    return
  }
  if (!selectedKnowledgeBase.value) {
    ElMessage.warning('请先选择知识库')
    return
  }

  // 检查是否有 Dify KB ID（优先使用 difyKnowdataId，兼容 difyKbId）
  const difyKbId = selectedKnowledgeBase.value.difyKnowdataId || selectedKnowledgeBase.value.difyKbId
  if (!difyKbId) {
    logger.warn('知识库缺少 Dify KB ID', selectedKnowledgeBase.value)
    ElMessage.warning('知识库缺少 Dify KB ID，无法使用问答功能')
    return
  }

  const text = kbInput.value.trim()
  const userMessageId = Date.now()
  
  // 添加用户消息
  kbMessagesList.value.push({
    id: userMessageId,
    type: 'user',
    content: text,
    timestamp: new Date()
  })
  kbInput.value = ''

  // 创建AI消息占位
  const aiMessageId = Date.now() + 1
  const aiMessage = {
    id: aiMessageId,
    type: 'ai',
    content: '',
    timestamp: new Date(),
    streaming: true,
    documents: [] // 文档片段数据
  }
  kbMessagesList.value.push(aiMessage)

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

  isGenerating.value = true

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
        const message = kbMessagesList.value.find(m => m.id === aiMessageId)
        if (message) {
          message.streaming = false
          message.documents = data.documents || []
          currentDocuments.value = data.documents || []
        }
        if (data.conversationId) {
          currentConversationId.value = data.conversationId
        }
        isGenerating.value = false
        currentAbortController.value = null
        nextTick(scrollKbToBottom)
        logger.info('流式问答完成', { 
          conversationId: data.conversationId,
          messageId: data.messageId,
          documentCount: (data.documents || []).length
        })
      },
      onError: (error) => {
        isGenerating.value = false
        currentAbortController.value = null
        
        // 处理错误
        const message = kbMessagesList.value.find(m => m.id === aiMessageId)
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
    const message = kbMessagesList.value.find(m => m.id === aiMessageId)
    if (message) {
      message.content = '抱歉，回答生成失败，请稍后再试。'
      message.streaming = false
    }
    logger.error('流式问答异常', error)
    ElMessage.error(error.message || '回答生成失败')
    nextTick(scrollKbToBottom)
  }
}

onMounted(() => {
  loadKnowledgeBases()
})
</script>

<style lang="scss" scoped>
.knowledge-base-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  max-width: 100%;
  margin: 0 auto;
  background: #f7f9fc;
  overflow: hidden;
}

/* 左侧导航栏 */
.kb-list-sidebar {
  width: 280px;
  min-width: 280px;
  background: white;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.kb-list-header {
  padding: 14px 20px 14px 16px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.kb-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e3a8a;
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
  color: #6b7280;
  margin-bottom: 12px;
  margin-left: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kb-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;
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
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #fff;
  transition: all 0.2s ease;

  &:hover {
    background: #f9fafb;
    border-color: #d1d5db;
  }

  &.active {
    background: #eef2ff;
    border-color: #1e3a8a;
  }
}

.kb-item-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.kb-item-icon-default {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b82f6;
  color: #fff;
}

.kb-item-info {
  flex: 1;
}

.kb-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
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
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
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

/* 中间内容区域 */
.main-content {
  flex: 1 1 0%;
  max-width: 100%;
  background: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-content .content-wrapper {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.content-header {
  padding: 24px;
  border-bottom: 1px solid #f3f4f6;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.content-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e3a8a;
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
  color: #6b7280;
  margin-bottom: 4px;
}

.content-description {
  font-size: 14px;
  color: #6b7280;
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
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
  color: #111827;
}

.kb-breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.kb-crumb-link {
  color: #1e3a8a;
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
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #374151;
  transition: all 0.2s ease;

  &:hover {
    background: #eef2ff;
    color: #1e3a8a;
    border-color: #1e3a8a;
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
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  transition: all 0.2s ease;
  align-items: center;

  &:hover {
    background: #f3f4f6;
    border-color: #d1d5db;
  }
}

.content-item-icon {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1e40af;
  font-size: 18px;

  &.folder-icon {
    background: #eef2ff;
    border-color: #dbeafe;
    color: #3b82f6;
  }
}

.content-item-info {
  flex: 1;
}

.content-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 4px;
}

.content-item-meta {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
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
  color: #111827;
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
  color: #6b7280;
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
  color: #6b7280;
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
  color: #6b7280;
}

/* 右侧AI助手区域 */
.ai-assistant {
  width: 600px;
  flex: 0 0 600px;
  background: white;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-assistant.ai-expanded {
  flex: 1 1 auto;
  width: auto;
  min-width: 0;
}

.ai-header {
  padding: 20px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ai-header-left {
  display: flex;
  align-items: center;
}

.ai-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e3a8a;
}

.kb-messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(180deg, #fafbfc 0%, #f7f9fc 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  &.has-messages {
    align-items: stretch;
    justify-content: flex-start;
  }
}

.empty-chat-message {
  font-size: 16px;
  color: #6b7280;
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
  background: #fff;
}

.kb-input-container {
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border: 1px solid #e5e7eb;
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
  color: #6b7280;
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
    height: auto;
    min-height: 100vh;
    width: 100%;
    max-width: none;
  }

  .kb-list-sidebar {
    width: 100%;
    min-width: 0;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }

  .kb-list {
    padding: 0 16px 16px;
    max-height: 50vh;
  }

  .main-content {
    order: 2;
    width: 100%;
    flex: 1 1 auto;
  }

  .ai-assistant {
    order: 3;
    width: 100%;
    border-left: none;
    border-top: 1px solid #e5e7eb;
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