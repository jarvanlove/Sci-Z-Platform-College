<!--
/**
 * @description 项目详情/编辑业务组件
 * 支持查看和编辑模式，包含基本信息、成员管理、里程碑管理等功能
 */
-->
<template>
    <div class="project-detail-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <BackButton @click="handleBack" />
        <h1 class="page-title">
          {{ isEditMode ? $t('project.detail.editTitle') : $t('project.detail.title') }}
        </h1>
      </div>
  
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ $t('common.loading') }}</span>
      </div>
  
      <!-- 主要内容 -->
      <div v-else class="main-content-single">
        <!-- 基本信息 -->
        <BaseCard class="card">
          <template #header>
            <h2 class="card-title">{{ $t('project.detail.basicInfo') }}</h2>
          </template>
          <div class="info-grid">
            <!-- 项目编号、项目名称 -->
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.number') }}</span>
              <span class="info-value">{{ project.number }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.name') }}</span>
              <span class="info-value">{{ project.name }}</span>
            </div>
  
            <!-- 项目负责人、项目状态 -->
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.manager') }}</span>
              <template v-if="isEditMode">
                <el-input
                  v-model="project.manager"
                  :placeholder="$t('project.detail.managerPlaceholder')"
                />
              </template>
              <template v-else>
                <span class="info-value">{{ project.manager }}</span>
              </template>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.status') }}</span>
              <el-tag :type="getStatusTagType(project.status)" size="small" round class="status-tag">
                {{ getStatusText(project.status) }}
              </el-tag>
            </div>
  
            <!-- 课题发布部门、研究方向 -->
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.department') }}</span>
              <span class="info-value">{{ project.department || $t('common.none') }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.researchDirection') }}</span>
              <span class="info-value">{{ project.researchDirection || $t('common.none') }}</span>
            </div>
  
            <!-- 项目开始时间、项目结束时间 -->
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.startTime') }}</span>
              <template v-if="isEditMode">
                <BaseDatePicker
                  v-model="project.startTime"
                  type="date"
                  :placeholder="$t('project.detail.startTimePlaceholder')"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </template>
              <template v-else>
                <span class="info-value">{{ project.startTime }}</span>
              </template>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.endTime') }}</span>
              <template v-if="isEditMode">
                <BaseDatePicker
                  v-model="project.endTime"
                  type="date"
                  :placeholder="$t('project.detail.endTimePlaceholder')"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </template>
              <template v-else>
                <span class="info-value">{{ project.endTime }}</span>
              </template>
            </div>
  
            <!-- 项目预算 -->
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.budget') }}</span>
              <template v-if="isEditMode">
                <div class="budget-input-wrapper">
                  <span class="budget-currency">{{ $t('common.yuan') }}</span>
                  <el-input-number
                    v-model="project.budget"
                    :min="0"
                    :precision="2"
                    :placeholder="$t('project.detail.budgetPlaceholder')"
                    class="budget-input"
                  />
                </div>
              </template>
              <template v-else>
                <span class="info-value">￥{{ Number(project.budget || 0).toFixed(2) }}</span>
              </template>
            </div>
          </div>
  
          <!-- 项目描述 -->
          <div class="info-item" style="margin-top: 16px">
            <span class="info-label">{{ $t('project.detail.description') }}</span>
            <template v-if="isEditMode">
              <el-input
                type="textarea"
                :rows="4"
                v-model="project.description"
                :placeholder="$t('project.detail.descriptionPlaceholder')"
                maxlength="500"
                show-word-limit
              />
            </template>
            <template v-else>
              <span class="info-value">{{ project.description || $t('common.none') }}</span>
            </template>
          </div>
        </BaseCard>
  
        <!-- 成员管理 -->
        <BaseCard class="card">
          <template #header>
            <div class="card-header-content">
              <h2 class="card-title">{{ $t('project.detail.members') }}</h2>
              <el-button
                v-if="isEditMode"
                type="primary"
                @click="openMemberDialog"
              >
                <el-icon><Plus /></el-icon>
                {{ $t('project.detail.addMember') }}
              </el-button>
            </div>
          </template>
  
          <!-- 已选成员列表 -->
          <div v-if="members.length > 0" class="member-list">
            <div v-for="member in members" :key="member.id" class="member-item">
              <div class="member-avatar">
                {{ member.name?.charAt(0) || '?' }}
              </div>
              <div class="member-info">
                <div class="member-name">{{ member.name }}</div>
                <div class="member-role">
                  {{ getRoleText(member) }} · {{ $t('project.detail.joinTime') }}: {{ formatDate(member.joinTime) }}
                </div>
              </div>
              <button
                v-if="isEditMode"
                class="action-btn btn-danger"
                @click="handleRemoveMember(member.id)"
              >
                {{ $t('common.remove') }}
              </button>
            </div>
          </div>
  
          <div v-else class="empty-state">
            <div class="empty-state-icon">👥</div>
            <div class="empty-state-text">{{ $t('project.detail.noMembers') }}</div>
            <div class="empty-state-hint">{{ $t('project.detail.addMemberHint') }}</div>
          </div>
        </BaseCard>
  
        <!-- 添加成员对话框 -->
        <el-dialog
          v-model="showMemberDialog"
          :title="$t('project.detail.addMember')"
          width="600px"
          :close-on-click-modal="false"
        >
          <el-input
            v-model="memberSearchKeyword"
            :placeholder="$t('project.detail.searchMemberPlaceholder')"
            clearable
            @input="handleMemberSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <div v-if="loadingUsers" class="loading-users">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ $t('common.loading') }}</span>
          </div>
          <div v-else-if="memberSearchResults.length > 0" class="search-results">
            <div v-for="user in memberSearchResults" :key="user.id" class="search-result-item">
              <el-checkbox
                :model-value="selectedUserIds.has(user.id)"
                :disabled="members.some(m => m.id === user.id)"
                @change="(val) => handleUserCheckboxChange(user.id, val)"
              >
                <div class="search-result-details">
                  <div class="search-result-name">
                    <span class="username">{{ user.username || '' }}</span>
                    <span class="real-name">{{ user.name || '' }}</span>
                  </div>
                </div>
              </el-checkbox>
            </div>
          </div>
          <div v-else-if="!loadingUsers" class="empty-state">
            <div class="empty-state-text">
              {{ memberSearchKeyword.trim() ? '未找到匹配的用户' : $t('project.detail.searchHint') }}
            </div>
          </div>
          <template #footer>
            <span v-if="selectedUserIds.size > 0" class="selected-count">
              已选择 {{ selectedUserIds.size }} 人
            </span>
            <div class="dialog-footer-right">
              <el-button @click="closeMemberDialog">{{ $t('common.cancel') }}</el-button>
              <el-button 
                type="primary" 
                :disabled="selectedUserIds.size === 0"
                @click="confirmAddMembers"
              >
                {{ $t('project.detail.confirmAdd') }}
              </el-button>
            </div>
          </template>
        </el-dialog>
  
        <!-- 项目里程碑 -->
        <BaseCard class="card">
          <template #header>
            <div class="card-header-content">
              <h2 class="card-title">{{ $t('project.detail.milestones') }}</h2>
              <el-button
                v-if="isEditMode"
                type="primary"
                @click="openMilestoneDialog"
              >
                <el-icon><Plus /></el-icon>
                {{ $t('project.detail.addMilestone') }}
              </el-button>
            </div>
          </template>
  
          <!-- 里程碑列表 -->
          <div v-if="milestones.length > 0" class="milestone-list">
            <div v-for="(milestone, index) in milestones" :key="index" class="milestone-item">
              <div class="milestone-header">
                <span class="milestone-title">{{ $t('project.detail.milestone') }} {{ index + 1 }}</span>
                <div class="milestone-header-actions">
                  <button
                    v-if="isEditMode && milestone.id"
                    :class="['action-btn', milestone.progress === 100 ? 'btn-warning' : 'btn-success']"
                    @click="handleMilestoneCompleteToggle(milestone, index)"
                  >
                    {{ milestone.progress === 100 ? $t('project.detail.cancelComplete') : $t('project.detail.complete') }}
                  </button>
                  <button
                    v-if="isEditMode"
                    class="action-btn btn-danger"
                    @click="handleRemoveMilestone(index)"
                  >
                    {{ $t('common.delete') }}
                  </button>
                </div>
              </div>
              
              <div class="milestone-form">
                <el-form-item :label="$t('project.detail.milestoneName')">
                  <el-input
                    v-model="milestone.name"
                    :placeholder="$t('project.detail.milestoneNamePlaceholder')"
                    :disabled="!isEditMode"
                  />
                </el-form-item>
                
                <div class="form-row">
                  <el-form-item :label="$t('project.detail.startTime')">
                    <BaseDatePicker
                      v-model="milestone.startTime"
                      type="date"
                      :placeholder="$t('project.detail.selectStartTime')"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      :disabled="!isEditMode"
                    />
                  </el-form-item>
                  
                  <el-form-item :label="$t('project.detail.endTime')">
                    <BaseDatePicker
                      v-model="milestone.endTime"
                      type="date"
                      :placeholder="$t('project.detail.selectEndTime')"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      :disabled="!isEditMode"
                    />
                  </el-form-item>
                </div>
                
                <el-form-item :label="$t('project.detail.description')">
                  <el-input
                    v-model="milestone.description"
                    type="textarea"
                    :rows="2"
                    :placeholder="$t('project.detail.milestoneDescriptionPlaceholder')"
                    :disabled="!isEditMode"
                  />
                </el-form-item>
              </div>
  
              <!-- 里程碑文档管理 -->
              <div class="milestone-documents">
                <div class="milestone-documents-header">
                  <h4>{{ $t('project.detail.milestoneDocuments') }}</h4>
                </div>

                <!-- 文档列表 -->
                <div v-if="milestone.documents && milestone.documents.length > 0" class="document-list">
                  <div v-for="doc in milestone.documents" :key="doc.id" class="document-item">
                    <div class="document-icon">
                      <el-icon>
                        <component :is="getDocumentIcon(doc)" />
                      </el-icon>
                    </div>
                    <div class="document-info">
                      <div class="document-name">{{ doc.name }}</div>
                      <div class="document-meta">
                        {{ doc.type }} · {{ doc.uploader }} · {{ doc.uploadTime }} · {{ formatFileSizeDisplay(doc.size || doc.fileInfo?.fileSize || 0) }}
                      </div>
                    </div>
                    <div class="document-actions">
                      <button
                        class="action-btn btn-info"
                        @click="handleMilestoneDocPreview(doc)"
                      >
                        {{ $t('common.preview') }}
                      </button>
                      <button
                        class="action-btn btn-success"
                        @click="handleMilestoneDocDownload(doc)"
                      >
                        {{ $t('common.download') }}
                      </button>
                      <button
                        v-if="isEditMode"
                        class="action-btn btn-danger"
                        @click="handleMilestoneDocDelete(index, doc.id)"
                      >
                        {{ $t('common.delete') }}
                      </button>
                    </div>
                  </div>
                </div>

                <div v-else class="empty-state milestone-empty-state">
                  <div class="empty-state-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="empty-state-text">{{ $t('project.detail.noDocuments') }}</div>
                  <div class="empty-state-hint">{{ $t('project.detail.uploadDocumentsHint') }}</div>
                </div>

                <!-- 上传区域（批量模式，使用通用 FileUpload 组件） -->
                <div v-if="isEditMode" class="upload-section">
                  <FileUpload
                    v-model="milestonePendingFiles[index]"
                    :mode="'batch'"
                    :max-batch-count="10"
                    :multiple="true"
                    :drag="true"
                    :show-file-list="true"
                    :show-tips="false"
                    :allowed-types="allowedFileExtensions"
                    :max-size="200"
                    :button-text="$t('project.detail.uploadText')"
                    :upload-tip="$t('project.detail.uploadHint')"
                    @batch-upload="(files) => uploadMilestoneFiles(files, index)"
                  />
                </div>
              </div>
            </div>
          </div>
  
          <!-- 空状态 -->
          <div v-else class="empty-state">
            <div class="empty-state-icon">🎯</div>
            <div class="empty-state-text">{{ $t('project.detail.noMilestones') }}</div>
            <div class="empty-state-hint">{{ $t('project.detail.addMilestoneHint') }}</div>
          </div>
        </BaseCard>
  
        <!-- 新增里程碑对话框 -->
        <el-dialog
          v-model="showMilestoneDialog"
          :title="$t('project.detail.addMilestone')"
          width="700px"
          :close-on-click-modal="false"
        >
          <el-form ref="milestoneFormRef" :model="milestoneDraft" :rules="milestoneRules" label-width="120px">
            <el-form-item :label="$t('project.detail.milestoneName')" prop="name">
              <el-select
                v-model="milestoneDraft.name"
                :placeholder="$t('project.detail.selectMilestoneName')"
                style="width: 100%"
              >
                <el-option
                  v-for="opt in milestoneOptions"
                  :key="opt"
                  :label="opt"
                  :value="opt"
                  :disabled="addedMilestoneNames && addedMilestoneNames.includes(opt)"
                />
              </el-select>
            </el-form-item>
            
            <div class="form-row">
              <el-form-item :label="$t('project.detail.milestoneStartTime')" prop="startTime">
                <BaseDatePicker
                  v-model="milestoneDraft.startTime"
                  type="date"
                  :placeholder="$t('project.detail.selectStartTime')"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              
              <el-form-item :label="$t('project.detail.milestoneEndTime')" prop="endTime">
                <BaseDatePicker
                  v-model="milestoneDraft.endTime"
                  type="date"
                  :placeholder="$t('project.detail.selectEndTime')"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </div>
            
            <el-form-item :label="$t('project.detail.description')" prop="description">
              <el-input
                v-model="milestoneDraft.description"
                type="textarea"
                :rows="2"
                :placeholder="$t('project.detail.milestoneDescriptionPlaceholder')"
              />
            </el-form-item>
          </el-form>
          
          <template #footer>
            <el-button @click="closeMilestoneDialog">{{ $t('common.cancel') }}</el-button>
            <el-button type="primary" @click="confirmAddMilestone">
              {{ $t('project.detail.confirmAdd') }}
            </el-button>
          </template>
        </el-dialog>
      </div>
  
      <!-- 文件预览对话框 -->
      <FilePreview
        v-model="showPreviewDialog"
        :file-info="previewFileInfo"
      />
  
      <!-- 操作按钮 -->
      <div v-if="isEditMode" class="footer-actions">
        <el-button @click="handleCancel">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ $t('project.detail.update') }}
        </el-button>
      </div>
    </div>
  </template>
  
  <script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Plus, Search, Upload, UploadFilled, Document, Picture, VideoPlay, Folder } from '@element-plus/icons-vue'
  import { BaseCard, BaseDatePicker, BackButton, FilePreview } from '@/components/Common'
  import { FileUpload } from '@/components/Business/Form'
  import { PROJECT_STATUS, PROJECT_STATUS_CONFIG, MILESTONE_OPTIONS } from '@/utils/constants'
  import { getProjectDetail, updateProject, uploadMilestoneDocument, completeMilestone, cancelCompleteMilestone } from '@/api/Project'
  import { getUsers } from '@/api/System'
  import { deleteFile, downloadFile } from '@/api/File'
  import { ATTACHMENT_RELATION, ATTACHMENT_CATEGORY, IMAGE_FILE_EXTENSIONS, validateFileSize, validateFileType } from '@/constants/attachment'
  import { createLogger } from '@/utils/simpleLogger'
  import { formatFileSize } from '@/utils/file'
  
  const router = useRouter()
  const route = useRoute()
  const { t } = useI18n()
  const logger = createLogger('ProjectDetail')
  
  // 响应式数据
  const loading = ref(false)
  const saving = ref(false)
  const isEditMode = ref(false)
  const milestoneFormRef = ref()
  
  const project = ref({
    id: 0,
    number: '',
    name: '',
    description: '',
    manager: '',
    status: PROJECT_STATUS.PROGRESS, // 默认进行中
    statusDescription: '',
    startTime: '',
    endTime: '',
    budget: 0,
    department: '',
    researchDirection: ''
  })
  
  const members = ref([])
  const milestones = ref([])
  
  // 里程碑表单验证规则
  const milestoneRules = reactive({
    name: [
      { required: true, message: t('project.detail.milestoneNameRequired'), trigger: 'blur' }
    ],
    startTime: [
      { required: true, message: t('project.detail.milestoneStartTimeRequired'), trigger: 'change' }
    ],
    endTime: [
      { required: true, message: t('project.detail.milestoneEndTimeRequired'), trigger: 'change' }
    ]
  })
  
  // 状态选项
  const statusOptions = computed(() => [
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.PROGRESS].text, value: PROJECT_STATUS.PROGRESS },
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.COMPLETED].text, value: PROJECT_STATUS.COMPLETED },
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.DELAYED].text, value: PROJECT_STATUS.DELAYED },
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.CANCELLED].text, value: PROJECT_STATUS.CANCELLED }
  ])
  
  // 成员对话框相关
  const showMemberDialog = ref(false)
  const memberSearchKeyword = ref('')
  const memberSearchResults = ref([])
  const allUsers = ref([]) // 存储所有用户列表，用于前端搜索
  const loadingUsers = ref(false) // 加载用户列表状态
  const selectedUserIds = ref(new Set()) // 选中的用户ID集合（用于多选）
  
  // 里程碑对话框相关
  const showMilestoneDialog = ref(false)
  const milestoneDraft = ref({
    name: '',
    startTime: '',
    endTime: '',
    description: ''
  })
  
  // 里程碑选项
  const milestoneOptions = ref(MILESTONE_OPTIONS)
  
  const addedMilestoneNames = computed(() => {
    if (!milestones.value || !Array.isArray(milestones.value)) {
      return []
    }
    return milestones.value
      .map(m => m.name)
      .filter(name => name && name.trim())
  })
  
  // 里程碑文档上传：批量模式待上传文件（按里程碑索引存储）
  // 使用 reactive 确保 Vue 能追踪动态属性的变化（例如 milestonePendingFiles[0] = []）
  const milestonePendingFiles = reactive({})
  
  // 允许的文件扩展名（里程碑文档不支持图片类型）
  // 排除图片类型：jpg, jpeg, png, gif, bmp, svg, webp
  const allowedFileExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md', 'rtf', 'zip', 'rar', '7z', 'tar', 'gz', 'mp4', 'avi', 'mov', 'wmv', 'flv', 'mp3', 'wav', 'flac', 'aac']
  
  // 判断文件类型（document/image/other）- 使用常量避免重复定义
  const getAttachmentType = (file) => {
    const extension = file.name.split('.').pop()?.toLowerCase() || ''
    
    // 使用 attachment.js 中的 IMAGE_FILE_EXTENSIONS 常量
    if (IMAGE_FILE_EXTENSIONS.includes(extension)) {
      return ATTACHMENT_CATEGORY.IMAGE
    }
    
    // 文档类型：pdf、office文档、文本文件
    const documentExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md', 'rtf']
    if (documentExtensions.includes(extension)) {
      return ATTACHMENT_CATEGORY.DOCUMENT
    }
    
    // 其他类型
    return ATTACHMENT_CATEGORY.OTHER
  }
  
  const formatFileSizeDisplay = (bytes) => {
    return formatFileSize(bytes || 0)
  }

  // 通用日期格式化：后端可能返回 ISO 字符串，这里统一格式为 YYYY-MM-DD
  const formatDate = (value) => {
    if (!value) return ''
    try {
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return value
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    } catch {
      return value
    }
  }

  // 统一里程碑文档结构，便于列表展示（兼容详情接口与上传接口的返回）
  const normalizeMilestoneDocument = (fileInfo) => {
    if (!fileInfo) return null

    // 上传时间统一转成 YYYY-MM-DD
    const uploadTimeStr = formatDate(fileInfo.uploadTime || fileInfo.uploadDate)

    // 类型展示：按文件扩展名显示（例如 md、pptx），与接口字段 fileExtension 一致
    const extension = (fileInfo.fileExtension || '').toLowerCase()
    const typeLabel = extension || (fileInfo.fileTypeLabel || 'document')

    const sizeBytes = fileInfo.fileSize || 0
    const sizeLabel = fileInfo.fileSizeLabel || formatFileSizeDisplay(sizeBytes)

    return {
      id: fileInfo.id || fileInfo.attachmentId || Date.now() + Math.random(),
      // 第一行文件名：使用 originalName 优先，其次 fileName
      name: fileInfo.originalName || fileInfo.fileName || '未知文件',
      // 第二行第一个字段：类型标签（这里展示扩展名）
      type: typeLabel,
      // 第二行第二个字段：上传人
      uploader: fileInfo.uploaderName || fileInfo.uploader || '',
      // 第二行第三个字段：上传日期（已格式化）
      uploadTime: uploadTimeStr,
      // 第二行第四个字段：文件大小（已格式化字符串）
      sizeLabel,
      size: sizeBytes,
      // 保留原始文件信息，供预览 / 下载使用
      fileInfo: {
        id: fileInfo.id,
        fileName: fileInfo.fileName,
        originalName: fileInfo.originalName,
        fileType: fileInfo.fileType,
        fileExtension: fileInfo.fileExtension,
        fileSize: fileInfo.fileSize,
        fileSizeLabel: fileInfo.fileSizeLabel,
        fileUrl: fileInfo.fileUrl,
        previewUrl: fileInfo.previewUrl,
        uploaderName: fileInfo.uploaderName,
        uploadTime: fileInfo.uploadTime
      }
    }
  }

  // 根据文件类型选择合适的图标（与上传组件风格保持一致）
  const getDocumentIcon = (doc) => {
    const name = (doc.name || doc.fileInfo?.originalName || doc.fileInfo?.fileName || '').toLowerCase()

    if (name.endsWith('.jpg') || name.endsWith('.jpeg') || name.endsWith('.png') || name.endsWith('.gif') || name.endsWith('.bmp') || name.endsWith('.webp')) {
      return Picture
    }
    if (name.endsWith('.mp4') || name.endsWith('.avi') || name.endsWith('.mov') || name.endsWith('.wmv') || name.endsWith('.flv')) {
      return VideoPlay
    }
    if (name.endsWith('.zip') || name.endsWith('.rar') || name.endsWith('.7z') || name.endsWith('.tar') || name.endsWith('.gz')) {
      return Folder
    }
    return Document
  }

  const uploadMilestoneFiles = async (files, milestoneIndex) => {
    if (!milestones.value[milestoneIndex].documents) {
      milestones.value[milestoneIndex].documents = []
    }
    
    // 验证项目编号和名称
    if (!project.value.number || !project.value.name) {
      ElMessage.error('项目信息不完整，无法上传文件')
      return
    }
    
    const milestone = milestones.value[milestoneIndex]
    if (!milestone.name) {
      ElMessage.error('里程碑名称不能为空')
      return
    }
    
    // 前端校验：文件大小和类型
    const maxSizeMB = 200
    const validFiles = []
    const invalidFiles = []
    
    for (const file of files) {
      // 检查是否为图片类型（里程碑文档不支持图片）
      const fileExtension = file.name.split('.').pop()?.toLowerCase() || ''
      if (IMAGE_FILE_EXTENSIONS.includes(fileExtension)) {
        invalidFiles.push({ 
          file, 
          reason: t('project.detail.imageNotSupported', { name: file.name }) 
        })
        continue
      }
      
      // 文件大小校验
      const sizeValidation = validateFileSize(file, maxSizeMB)
      if (!sizeValidation.passed) {
        invalidFiles.push({ file, reason: `${file.name}: ${sizeValidation.reason}` })
        continue
      }
      
      // 文件类型校验
      const typeValidation = validateFileType(file, allowedFileExtensions)
      if (!typeValidation.passed) {
        invalidFiles.push({ file, reason: `${file.name}: ${typeValidation.reason}` })
        continue
      }
      
      // 检查是否已存在同名文件
      const existingDoc = milestones.value[milestoneIndex].documents.find(
        doc => doc.name === file.name
      )
      
      if (existingDoc) {
        invalidFiles.push({ file, reason: t('project.detail.fileAlreadyExists', { name: file.name }) })
        continue
      }
      
      validFiles.push(file)
    }
    
    // 显示无效文件的提示
    if (invalidFiles.length > 0) {
      invalidFiles.forEach(({ reason }) => {
        ElMessage.warning(reason)
      })
    }
    
    // 如果没有有效文件，直接返回
    if (validFiles.length === 0) {
      if (invalidFiles.length > 0) {
        ElMessage.error('没有可上传的文件')
      }
      return
    }
    
    const loadingMessage = ElMessage({
      message: t('project.detail.uploadingFiles', { count: validFiles.length }),
      type: 'info',
      duration: 0,
      showClose: false
    })
    
    try {
      // 构建批量上传的表单数据
      const formData = new FormData()
      
      // 添加文件数组（批量上传）
      validFiles.forEach(file => {
        formData.append('files', file)
      })
      
      // 添加其他参数
      formData.append('relationType', ATTACHMENT_RELATION.PROJECT)
      formData.append('attachmentType', ATTACHMENT_CATEGORY.DOCUMENT) // 固定值 "document"
      formData.append('relationId', project.value.id) // 传项目ID，后端会处理
      formData.append('relationName', `${project.value.number}/${milestone.name}`)
      formData.append('isPublic', '0') // 默认私有
      
      // 调用批量上传接口
      const response = await uploadMilestoneDocument(formData)
      
      if (response.code === 200 && response.data) {
        // 兼容后端返回单个对象或数组两种情况
        const uploadedFiles = Array.isArray(response.data) ? response.data : [response.data]
        
        // 将上传成功的文件添加到文档列表（统一结构）
        uploadedFiles
          .map(normalizeMilestoneDocument)
          .filter(Boolean)
          .forEach(newDoc => {
            milestones.value[milestoneIndex].documents.push(newDoc)
          })
        
        ElMessage.success(t('project.detail.documentsAddedSuccess', { 
          milestoneIndex: milestoneIndex + 1, 
          count: uploadedFiles.length 
        }))
        
        // 清空待上传文件列表
        if (milestonePendingFiles[milestoneIndex]) {
          milestonePendingFiles[milestoneIndex] = []
        }
      } else {
        throw new Error(response.message || '文件上传失败')
      }
    } catch (error) {
      logger.error('Failed to upload milestone files', error)
      ElMessage.error(error.message || t('project.detail.uploadError'))
    } finally {
      loadingMessage.close()
    }
  }
  
  // 文件预览相关
  const showPreviewDialog = ref(false)
  const previewFileInfo = ref(null)
  
  // 获取状态文本（优先使用 statusDescription）
  const getStatusText = (status) => {
    // 优先使用后端返回的 statusDescription
    if (project.value.statusDescription) {
      return project.value.statusDescription
    }
    // 回退到配置中的文本
    return PROJECT_STATUS_CONFIG[status]?.text || status
  }
  
  // 获取状态标签类型
  const getStatusTagType = (status) => {
    return PROJECT_STATUS_CONFIG[status]?.type || 'info'
  }
  
  // 获取角色文本（支持 roleNames 数组）
  const getRoleText = (member) => {
    // 如果成员有 roleNames 数组，优先使用
    if (member?.roleNames && Array.isArray(member.roleNames) && member.roleNames.length > 0) {
      return member.roleNames.join('、')
    }
    
    // 兼容旧的 role 字段
    if (typeof member === 'string') {
      const roleMap = {
        manager: t('project.leader'),
        core: t('project.coreMember'),
        member: t('project.member')
      }
      return roleMap[member] || t('project.member')
    }
    
    // 如果 member 是对象但没有 roleNames，使用 role 字段
    if (member?.role) {
      const roleMap = {
        manager: t('project.leader'),
        core: t('project.coreMember'),
        member: t('project.member')
      }
      return roleMap[member.role] || t('project.member')
    }
    
    return t('project.member')
  }
  
  // 解析URL参数确定模式
  const resolveMode = () => {
    const mode = route.query.mode
    if (mode === 'edit' || mode === 'view') return mode
    return 'view'
  }
  
  // 加载项目详情
  const loadProjectDetail = async () => {
    loading.value = true
    try {
      logger.info('Loading project detail', { projectId: route.params.id })
      
      const response = await getProjectDetail(route.params.id)
      
      if (response.code === 200 && response.data) {
        const data = response.data
        
        // 数据映射
        project.value = {
          id: data.id,
          number: data.number || '',
          name: data.name || '',
          description: data.description || '',
          manager: data.projectLeader || data.managerName || data.manager || '',
          status: mapStatus(data.status),
          statusDescription: data.statusDescription || '',
          startTime: data.startTime || data.projectStartTime || '',
          endTime: data.endTime || data.projectEndTime || '',
          budget: data.budget || 0,
          department: data.department || data.declarationInfo?.department || '',
          researchDirection: data.researchDirection || data.declarationInfo?.researchDirection || ''
        }
        
        // 成员数据映射
        members.value = (data.members || []).map(member => ({
          id: member.userId || member.id,
          name: member.userName || member.name || member.realName || '',
          username: member.username || '',
          role: member.role || 'member', // 旧字段，保持向后兼容
          roleNames: member.roleNames || [], // 使用接口返回的角色数组
          joinTime: member.joinTime || member.join_time || ''
        }))
        
        // 里程碑数据映射
        // 后端字段：milestoneName / attachments
        milestones.value = (data.milestones || []).map((milestone, index) => {
          // 初始化每个里程碑的待上传文件列表
          if (!milestonePendingFiles[index]) {
            milestonePendingFiles[index] = []
          }
          const documents = (milestone.attachments || milestone.documents || [])
            .map(normalizeMilestoneDocument)
            .filter(Boolean)

          return {
            id: milestone.id || milestone.milestoneId || null, // 里程碑ID
            name: milestone.milestoneName || milestone.name || milestone.title || '',
            description: milestone.description || milestone.content || '',
            startTime: milestone.startTime || milestone.milestoneStartTime || '',
            endTime: milestone.endTime || milestone.milestoneEndTime || '',
            progress: milestone.progress || milestone.milestoneProgress || 0, // 里程碑进度，100表示完成
            // 统一后的文档列表
            documents
          }
        })
        
        // 设置模式
        const mode = resolveMode()
        isEditMode.value = mode === 'edit'
        
        logger.info('Project detail loaded successfully', { 
          projectId: route.params.id, 
          mode: isEditMode.value ? 'edit' : 'view' 
        })
      } else {
        throw new Error(response.message || '获取项目详情失败')
      }
    } catch (error) {
      logger.error('Failed to load project detail', error)
      ElMessage.error(error.message || t('project.detail.loadError'))
    } finally {
      loading.value = false
    }
  }
  
  // 状态映射：将后端状态值映射到前端状态值（与后端枚举保持一致）
  const mapStatus = (status) => {
    // 如果状态是字符串数字，映射到前端状态值
    // 后端枚举：1-进行中, 2-已完成, 3-已延期, 4-已取消
    const statusNumberMap = {
      '1': PROJECT_STATUS.PROGRESS,  // 进行中
      '2': PROJECT_STATUS.COMPLETED, // 已完成
      '3': PROJECT_STATUS.DELAYED,   // 已延期
      '4': PROJECT_STATUS.CANCELLED  // 已取消
    }
    
    // 如果状态是字符串描述，映射到前端状态值
    const statusTextMap = {
      'in_progress': PROJECT_STATUS.PROGRESS,
      'progress': PROJECT_STATUS.PROGRESS,
      '进行中': PROJECT_STATUS.PROGRESS,
      'completed': PROJECT_STATUS.COMPLETED,
      '已完成': PROJECT_STATUS.COMPLETED,
      'delayed': PROJECT_STATUS.DELAYED,
      '已延期': PROJECT_STATUS.DELAYED,
      'cancelled': PROJECT_STATUS.CANCELLED,
      '已取消': PROJECT_STATUS.CANCELLED
    }
    
    // 优先匹配数字状态，再匹配文本状态
    return statusNumberMap[String(status)] || statusTextMap[status] || status
  }
  
  // 状态反向映射：将前端状态值转换为后端 API 期望的值
  // 后端枚举：1-进行中, 2-已完成, 3-已延期, 4-已取消
  const mapStatusToBackend = (status) => {
    const frontendToBackendMap = {
      [PROJECT_STATUS.PROGRESS]: '1',  // 进行中
      [PROJECT_STATUS.COMPLETED]: '2', // 已完成
      [PROJECT_STATUS.DELAYED]: '3',   // 已延期
      [PROJECT_STATUS.CANCELLED]: '4'  // 已取消
    }
    return frontendToBackendMap[status] || status
  }
  
  // 返回列表
  const handleBack = () => {
    router.push('/project/list')
  }
  
  // 取消编辑
  const handleCancel = () => {
    ElMessageBox.confirm(
      t('project.detail.cancelConfirm'),
      t('project.detail.cancelTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('project.detail.continueEdit'),
        type: 'warning'
      }
    ).then(() => {
      handleBack()
    }).catch(() => {
      // 用户选择继续编辑
    })
  }
  
  // 保存项目
  const handleSave = async () => {
    try {
      // 验证里程碑时间
      for (let i = 0; i < milestones.value.length; i++) {
        const milestone = milestones.value[i]
        if (milestone.startTime && milestone.endTime) {
          if (new Date(milestone.startTime) >= new Date(milestone.endTime)) {
            ElMessage.error(t('project.milestoneTimeError', { index: i + 1 }))
            return
          }
        }
      }
      
      saving.value = true
      logger.info('Saving project', { projectId: project.value.id })
      
      const updateData = {
        id: project.value.id,
        // 只提交允许编辑的字段：项目负责人、项目开始时间、项目结束时间、项目预算、项目描述
        manager: project.value.manager,
        startTime: project.value.startTime,
        endTime: project.value.endTime,
        budget: project.value.budget,
        description: project.value.description,
        // 状态不允许编辑，但需要传递当前状态
        status: mapStatusToBackend(project.value.status),
        members: members.value.map(m => {
          const roleText = Array.isArray(m.roleNames) && m.roleNames.length
            ? m.roleNames.join('、')
            : (m.role || 'member')
          return {
            userId: m.id,
            role: roleText
          }
        }),
        milestones: milestones.value
      }
      
      const response = await updateProject(project.value.id, updateData)
      
      if (response.code === 200) {
        ElMessage.success(t('project.detail.saveSuccess'))
        logger.info('Project saved successfully', { projectId: project.value.id })
        handleBack()
      } else {
        throw new Error(response.message || '保存失败')
      }
    } catch (error) {
      logger.error('Failed to save project', error)
      ElMessage.error(error.message || t('project.detail.saveError'))
    } finally {
      saving.value = false
    }
  }
  
  // 成员管理
  const openMemberDialog = async () => {
    memberSearchKeyword.value = ''
    memberSearchResults.value = []
    selectedUserIds.value = new Set() // 清空选中状态
    showMemberDialog.value = true
    
    // 如果还没有加载过用户列表，则加载
    if (allUsers.value.length === 0) {
      await loadAllUsers()
    }
    
    // 显示所有用户
    filterUsers()
  }
  
  const closeMemberDialog = () => {
    showMemberDialog.value = false
    selectedUserIds.value = new Set() // 关闭时清空选中状态
  }
  
  // 处理用户复选框变化
  const handleUserCheckboxChange = (userId, checked) => {
    if (checked) {
      selectedUserIds.value.add(userId)
    } else {
      selectedUserIds.value.delete(userId)
    }
  }
  
  // 加载所有用户列表
  const loadAllUsers = async () => {
    loadingUsers.value = true
    try {
      // 获取所有用户，使用较大的 pageSize 获取全部数据
      const response = await getUsers({
        pageNo: 1,
        pageSize: 1000, // 获取足够多的用户
        sortBy: 'createTime',
        sortOrder: 'DESC'
      })
      
      const data = response?.data?.data || response?.data || {}
      const users = data.records || data.list || []
      
      // 存储所有用户，过滤掉系统用户（admin 和 user）
      allUsers.value = users
        .filter(user => {
          const username = (user.username || '').toLowerCase()
          return username !== 'admin' && username !== 'user'
        })
        .map(user => ({
          id: user.id,
          name: user.realName || user.name || '',
          username: user.username || '',
          email: user.email || '',
          phone: user.phone || '',
          roleNames: user.roleNames || [] // 保存角色数组
        }))
      
      logger.info('Loaded all users', { count: allUsers.value.length })
    } catch (error) {
      logger.error('Failed to load users', error)
      ElMessage.error('加载用户列表失败')
      allUsers.value = []
    } finally {
      loadingUsers.value = false
    }
  }
  
  // 前端搜索过滤用户
  const filterUsers = () => {
    const keyword = memberSearchKeyword.value.trim().toLowerCase()
    
    if (!keyword) {
      // 如果没有关键词，显示所有用户（排除已添加的成员）
      memberSearchResults.value = allUsers.value.filter(user => 
        !members.value.some(m => m.id === user.id)
      )
    } else {
      // 根据关键词过滤：搜索姓名、用户名、邮箱、手机号
      memberSearchResults.value = allUsers.value.filter(user => {
        const nameMatch = user.name?.toLowerCase().includes(keyword)
        const usernameMatch = user.username?.toLowerCase().includes(keyword)
        const emailMatch = user.email?.toLowerCase().includes(keyword)
        const phoneMatch = user.phone?.includes(keyword)
        
        return (nameMatch || usernameMatch || emailMatch || phoneMatch) &&
               !members.value.some(m => m.id === user.id)
      })
    }
  }
  
  // 处理搜索输入
  const handleMemberSearch = () => {
    filterUsers()
  }
  
  // 确认添加选中的成员（前端添加，不调用接口）
  const confirmAddMembers = () => {
    if (selectedUserIds.value.size === 0) {
      ElMessage.warning('请至少选择一个成员')
      return
    }
    
    let addedCount = 0
    const selectedIds = Array.from(selectedUserIds.value)
    
    selectedIds.forEach(userId => {
      // 检查是否已经是成员
      const isAlreadyMember = members.value.some(member => member.id === userId)
      if (isAlreadyMember) {
        return
      }
      
      // 从 allUsers 中找到对应的用户信息
      const user = allUsers.value.find(u => u.id === userId)
      if (!user) {
        return
      }
      
      // 前端添加到成员列表
      const newMember = {
        id: user.id,
        name: user.realName || user.name || user.username || '',
        username: user.username || '',
        role: 'member', // 保留向后兼容
        roleNames: user.roleNames || [], // 使用接口返回的角色数组
        joinTime: new Date().toISOString().split('T')[0]
      }
      
      members.value.push(newMember)
      addedCount++
    })
    
    if (addedCount > 0) {
      ElMessage.success(`成功添加 ${addedCount} 位成员`)
      // 清空选中状态和搜索关键词
      selectedUserIds.value = new Set()
      memberSearchKeyword.value = ''
      // 关闭弹窗
      showMemberDialog.value = false
    } else {
      ElMessage.warning('所选成员已全部添加，无需重复添加')
    }
  }
  
  // 移除成员（前端移除，不调用接口）
  const handleRemoveMember = async (memberId) => {
    const member = members.value.find(m => m.id === memberId)
    if (!member) return
    
    try {
      await ElMessageBox.confirm(
        t('project.detail.removeMemberConfirm', { name: member.name }),
        t('project.detail.removeMemberTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
      
      // 前端移除，不调用接口
      const index = members.value.findIndex(m => m.id === memberId)
      if (index > -1) {
        members.value.splice(index, 1)
        ElMessage.success(t('project.detail.memberRemovedSuccess', { name: member.name }))
      }
    } catch (error) {
      // 用户取消操作
      if (error !== 'cancel') {
        logger.error('Failed to remove member', error)
      }
    }
  }
  
  // 里程碑管理
  const openMilestoneDialog = () => {
    milestoneDraft.value = {
      name: '',
      startTime: '',
      endTime: '',
      description: ''
    }
    showMilestoneDialog.value = true
  }
  
  const closeMilestoneDialog = () => {
    showMilestoneDialog.value = false
  }
  
  const confirmAddMilestone = async () => {
    try {
      await milestoneFormRef.value.validate()
      
      const d = milestoneDraft.value
      if (addedMilestoneNames.value.includes(d.name.trim())) {
        ElMessage.warning(t('project.detail.milestoneAlreadyExists'))
        return
      }
      if (new Date(d.startTime) >= new Date(d.endTime)) {
        ElMessage.warning(t('project.detail.milestoneTimeInvalid'))
        return
      }
      
      const newIndex = milestones.value.length
      milestones.value.push({
        name: d.name.trim(),
        startTime: d.startTime,
        endTime: d.endTime,
        description: d.description || '',
        documents: []
      })
      // 初始化新里程碑的待上传文件列表
      if (!milestonePendingFiles[newIndex]) {
        milestonePendingFiles[newIndex] = []
      }
      
      showMilestoneDialog.value = false
      ElMessage.success(t('project.detail.milestoneAddedSuccess'))
    } catch (error) {
      // 表单验证失败
    }
  }
  
  const handleRemoveMilestone = async (index) => {
    try {
      await ElMessageBox.confirm(
        t('project.detail.deleteMilestoneConfirm'),
        t('project.detail.deleteMilestoneTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
      
      milestones.value.splice(index, 1)
      ElMessage.success(t('project.detail.milestoneDeletedSuccess'))
    } catch (error) {
      // 用户取消操作
    }
  }
  
  // 处理里程碑完成/取消完成
  const handleMilestoneCompleteToggle = async (milestone, index) => {
    if (!milestone.id) {
      ElMessage.warning(t('project.detail.milestoneIdRequired'))
      return
    }
    
    const isCompleted = milestone.progress === 100
    const today = new Date()
    const endDate = milestone.endTime ? new Date(milestone.endTime) : null
    const isEarlyComplete = endDate && today < endDate
    
    try {
      if (!isCompleted) {
        // 完成里程碑：检查是否提前完成
        if (isEarlyComplete) {
          await ElMessageBox.confirm(
            t('project.detail.earlyCompleteConfirm', { 
              name: milestone.name,
              endTime: milestone.endTime 
            }),
            t('project.detail.earlyCompleteTitle'),
            {
              confirmButtonText: t('common.confirm'),
              cancelButtonText: t('common.cancel'),
              type: 'warning'
            }
          )
        }
        
        // 调用完成接口
        logger.info('Completing milestone', { milestoneId: milestone.id, milestoneName: milestone.name })
        const response = await completeMilestone(milestone.id)
        
        if (response.code === 200) {
          // 更新本地状态
          milestones.value[index].progress = 100
          ElMessage.success(t('project.detail.milestoneCompletedSuccess', { name: milestone.name }))
          logger.info('Milestone completed successfully', { milestoneId: milestone.id })
        } else {
          throw new Error(response.message || '完成里程碑失败')
        }
      } else {
        // 取消完成里程碑
        await ElMessageBox.confirm(
          t('project.detail.cancelCompleteConfirm', { name: milestone.name }),
          t('project.detail.cancelCompleteTitle'),
          {
            confirmButtonText: t('common.confirm'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          }
        )
        
        // 调用取消完成接口
        logger.info('Canceling milestone completion', { milestoneId: milestone.id, milestoneName: milestone.name })
        const response = await cancelCompleteMilestone(milestone.id)
        
        if (response.code === 200) {
          // 更新本地状态（进度会由后端重新计算，这里先设为0，实际应该重新加载数据）
          milestones.value[index].progress = 0
          ElMessage.success(t('project.detail.milestoneCancelCompleteSuccess', { name: milestone.name }))
          logger.info('Milestone completion canceled successfully', { milestoneId: milestone.id })
          
          // 重新加载项目详情以获取最新的进度
          await loadProjectDetail()
        } else {
          throw new Error(response.message || '取消完成里程碑失败')
        }
      }
    } catch (error) {
      // 用户取消操作
      if (error === 'cancel') {
        return
      }
      logger.error('Failed to toggle milestone completion', error)
      ElMessage.error(error.message || t('project.detail.milestoneToggleError'))
    }
  }
  
  // 处理里程碑文档预览
  const handleMilestoneDocPreview = (doc) => {
    const attachmentId = doc.fileInfo?.id || doc.id
    if (attachmentId) {
      previewFileInfo.value = {
        name: doc.name,
        attachmentId: attachmentId
      }
      showPreviewDialog.value = true
    } else {
      ElMessage.warning('文件预览信息不完整')
    }
  }
  
  // 处理里程碑文档下载
  const handleMilestoneDocDownload = async (doc) => {
    const attachmentId = doc.fileInfo?.id || doc.id
    if (!attachmentId) {
      ElMessage.warning('文件ID不存在，无法下载')
      return
    }
    
    try {
      logger.info('Downloading milestone document', { attachmentId, fileName: doc.name })
      
      // 调用后端下载接口
      const response = await downloadFile(attachmentId)
      
      // 处理 Blob 响应
      if (response.data instanceof Blob) {
        // 创建下载链接
        const url = window.URL.createObjectURL(response.data)
        const link = document.createElement('a')
        link.href = url
        link.download = doc.name || 'download'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success(t('project.detail.downloadSuccess', { name: doc.name }))
        logger.info('File downloaded successfully', { attachmentId, fileName: doc.name })
      } else {
        throw new Error('下载响应格式错误')
      }
    } catch (error) {
      logger.error('Failed to download file', error)
      ElMessage.error(error.message || t('project.detail.downloadError'))
    }
  }
  
  // 处理里程碑文档删除
  const handleMilestoneDocDelete = async (milestoneIndex, docId) => {
    const milestone = milestones.value[milestoneIndex]
    if (!milestone.documents) return
    
    const docIndex = milestone.documents.findIndex(doc => doc.id === docId)
    if (docIndex === -1) return
    
    const doc = milestone.documents[docIndex]
    const docName = doc.name
    const attachmentId = doc.fileInfo?.id || doc.id
    
    if (!attachmentId) {
      ElMessage.warning('文件ID不存在，无法删除')
      return
    }
    
    try {
      // 确认删除
      await ElMessageBox.confirm(
        t('project.detail.deleteDocumentConfirm', { name: docName }),
        t('project.detail.deleteDocumentTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
      
      logger.info('Deleting milestone document', { attachmentId, fileName: docName })
      
      // 调用后端删除接口
      const response = await deleteFile(attachmentId)
      
      if (response.code === 200) {
        // 删除成功，从列表中移除
        milestone.documents.splice(docIndex, 1)
        ElMessage.success(t('project.detail.documentDeletedSuccess', { name: docName }))
        logger.info('Document deleted successfully', { attachmentId, fileName: docName })
      } else {
        throw new Error(response.message || '删除失败')
      }
    } catch (error) {
      // 用户取消操作
      if (error === 'cancel') {
        return
      }
      logger.error('Failed to delete document', error)
      ElMessage.error(error.message || t('project.detail.deleteError'))
    }
  }
  
  onMounted(() => {
    loadProjectDetail()
  })
  </script>
  
  <style lang="scss" scoped>
  .project-detail-container {
    padding: var(--gap-lg);
    background: var(--bg);
    min-height: calc(100vh - 60px);
  }
  
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    gap: 12px;
  }
  
  .page-title {
    font-size: var(--font-size-2xl);
    font-weight: 600;
    color: var(--color-primary);
    margin: 0;
  }
  
  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    gap: var(--gap-sm);
    color: var(--text-secondary);
  }
  
  .main-content-single {
    display: flex;
    flex-direction: column;
    gap: var(--gap-lg);
    margin-bottom: var(--gap-lg);
  }
  
  
  .card-header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-primary); // 与页面标题保持一致的主题色
    margin: 0;
  }
  
  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--gap-md);
  }
  
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  
  .info-label {
    font-size: 14px;
    color: var(--text-secondary);
    font-weight: 500;
  }
  
  .info-value {
    font-size: 14px;
    color: var(--text-primary);
  }
  
  .budget-input-wrapper {
    display: flex;
    align-items: center;
    width: 100%;
    gap: var(--gap-xs);
  }
  
  .budget-currency {
    color: var(--text-primary);
    font-size: 16px;
    font-weight: 500;
    flex-shrink: 0;
  }
  
  .budget-input {
    flex: 1;
  }
  
  // 状态标签样式
  :deep(.status-tag) {
    width: 40.0%;
    max-width: 120px;
    min-width: 60px;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    text-align: center;
  }
  
  // 成员管理样式
  .member-list {
    display: flex;
    flex-direction: column;
    gap: var(--gap-sm);
  }
  
  .member-item {
    display: flex;
    align-items: center;
    gap: var(--gap-sm);
    padding: var(--gap-sm);
    background: var(--bg-tertiary);
    border-radius: var(--radius-md);
  }
  
  .member-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: var(--border);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-secondary);
    font-weight: 600;
  }
  
  .member-info {
    flex: 1;
  }
  
  .member-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 2px;
  }
  
  .member-role {
    font-size: 12px;
    color: var(--text-secondary);
  }
  
  .search-results {
    max-height: 200px;
    overflow-y: auto;
    border: 1px solid var(--border);
    border-radius: var(--radius-md);
    background: var(--surface);
    margin-top: var(--gap-sm);
  }
  
  .search-result-item {
    display: flex;
    align-items: center;
    padding: var(--gap-sm);
    border-bottom: 1px solid var(--border);
  }
  
  .search-result-item:last-child {
    border-bottom: none;
  }
  
  .search-result-item:hover {
    background: var(--bg-tertiary);
  }
  
  .search-result-item :deep(.el-checkbox) {
    width: 100%;
    margin-right: 0;
  }
  
  .search-result-item :deep(.el-checkbox__label) {
    width: 100%;
    padding-left: 8px;
  }
  
  .search-result-details {
    flex: 1;
  }
  
  .search-result-name {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    font-weight: 500;
    color: var(--text-primary);
    font-size: 14px;
  }
  
  .search-result-name .username {
    color: var(--text-secondary);
  }
  
  .search-result-name .real-name {
    color: var(--text-primary);
    text-align: right;
    flex: 1;
    margin-left: 16px;
  }
  
  // 添加成员弹窗底部样式
  :deep(.el-dialog__footer) {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: var(--gap-md);
  }
  
  .selected-count {
    color: var(--text-secondary);
    font-size: 14px;
    margin-right: auto;
  }
  
  .dialog-footer-right {
    display: flex;
    gap: var(--gap-sm);
  }
  
  .loading-users {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    gap: var(--gap-sm);
    color: var(--text-secondary);
  }
  
  // 里程碑管理样式
  .milestone-list {
    display: flex;
    flex-direction: column;
    gap: var(--gap-md);
  }
  
  .milestone-item {
    border: 1px solid var(--border);
    border-radius: var(--radius-md);
    padding: var(--gap-md);
    background: var(--bg-tertiary);
  }
  
  .milestone-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--gap-sm);
  }
  
  .milestone-header-actions {
    display: flex;
    gap: var(--gap-xs);
    align-items: center;
  }
  
  .milestone-title {
    font-weight: 600;
    color: var(--color-primary); // 里程碑标题使用主题色
  }
  
  .milestone-form {
    display: flex;
    flex-direction: column;
    gap: var(--gap-md);
  }
  
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--gap-md);
  }
  
  .milestone-documents {
    margin-top: var(--gap-md);
    padding-top: var(--gap-md);
    border-top: 1px solid var(--border);
  }
  
  .milestone-documents-header {
    margin-bottom: var(--gap-sm);
  }
  
  .milestone-documents-header h4 {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-primary); // 「里程碑文档」标题使用主题色
  }
  
  .document-list {
    display: flex;
    flex-direction: column;
    gap: var(--gap-sm);
  }
  
  .document-item {
    display: flex;
    align-items: center;
    gap: var(--gap-sm);
    padding: var(--gap-sm);
    background: var(--bg-tertiary);
    border-radius: var(--radius-md);
  }
  
  .document-icon {
    width: 32px;
    height: 32px;
    border-radius: 4px;
    background: var(--bg-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-primary);
  }
  
  .document-info {
    flex: 1;
  }
  
  .document-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 2px;
  }
  
  .document-meta {
    font-size: 12px;
    color: var(--text-secondary);
  }
  
  .document-actions {
    display: flex;
    gap: var(--gap-xs);
  }

  // 操作按钮样式，复用申报列表 / 用户管理的细边框主题按钮
  .action-btn {
    padding: 5px 12px;
    border: 1px solid transparent;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
    background: none;
    white-space: nowrap;
  
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      
      &:hover {
        background: none;
        color: inherit;
      }
    }
  
    &.btn-success {
      color: #16a34a;
      border-color: #16a34a;
      
      &:hover:not(:disabled) {
        background: #16a34a;
        color: var(--surface);
      }
    }
  
    &.btn-info {
      color: var(--text-3);
      border-color: var(--text-3);
      
      &:hover:not(:disabled) {
        background: var(--text-3);
        color: var(--surface);
      }
    }
  
    &.btn-danger {
      color: #dc2626;
      border-color: #dc2626;
      
      &:hover:not(:disabled) {
        background: #dc2626;
        color: var(--surface);
      }
    }
    
    &.btn-warning {
      color: #f59e0b;
      border-color: #f59e0b;
      
      &:hover:not(:disabled) {
        background: #f59e0b;
        color: var(--surface);
      }
    }
  }
  
  .upload-section {
    margin-top: var(--gap-sm);
  }
  
  .upload-area {
    border: 2px dashed var(--border);
    border-radius: 12px;
    padding: 32px;
    text-align: center;
    background: var(--bg);
    transition: all 0.3s ease;
    cursor: pointer;
  }
  
  .upload-area:hover {
    border-color: var(--color-primary);
    background: var(--hover);
  }
  
  .upload-area.dragover {
    border-color: var(--color-primary);
    background: var(--hover);
  }
  
  .upload-icon {
    font-size: 48px;
    color: var(--text-3);
    margin-bottom: 16px;
  }
  
  .upload-text {
    font-size: 16px;
    color: var(--text);
    margin-bottom: 8px;
  }
  
  .upload-hint {
    font-size: 14px;
    color: var(--text-3);
  }
  
  // 空状态样式
  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: var(--text-secondary);
  }
  
  .empty-state-icon {
    font-size: 48px;
    margin-bottom: var(--gap-md);
    opacity: 0.5;
  }
  
  .empty-state-text {
    font-size: 16px;
    margin-bottom: var(--gap-xs);
  }
  
  .empty-state-hint {
    font-size: 14px;
    color: var(--text-tertiary);
  }
  
  // 操作按钮样式
  .footer-actions {
    display: flex;
    justify-content: flex-end;
    gap: var(--gap-sm);
    margin-top: var(--gap-lg);
    padding-top: var(--gap-lg);
    border-top: 1px solid var(--border);
    position: sticky;
    bottom: 0;
    background: var(--surface);
    padding-bottom: var(--gap-sm);
    z-index: 10;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.03);
  }
  
  // 响应式设计
  @media (max-width: 768px) {
    .info-grid {
      grid-template-columns: 1fr;
    }
    
    .form-row {
      grid-template-columns: 1fr;
    }
    
    .footer-actions {
      flex-direction: column;
      gap: var(--gap-xs);
    }
  }
  </style>
  