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
        <div class="header-left">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
            {{ $t('common.back') }}
          </el-button>
          <h1 class="page-title">
            {{ isEditMode ? $t('project.detail.editTitle') : $t('project.detail.title') }}
          </h1>
        </div>
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
              <template v-if="isEditMode">
                <el-input
                  v-model="project.name"
                  :placeholder="$t('project.detail.namePlaceholder')"
                  maxlength="100"
                  show-word-limit
                />
              </template>
              <template v-else>
                <span class="info-value">{{ project.name }}</span>
              </template>
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
              <template v-if="isEditMode">
                <el-select v-model="project.status" :placeholder="$t('project.detail.statusPlaceholder')">
                  <el-option
                    v-for="option in statusOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </template>
              <template v-else>
                <el-tag :type="getStatusTagType(project.status)" size="small" round>
                  {{ getStatusText(project.status) }}
                </el-tag>
              </template>
            </div>
  
            <!-- 课题发布部门、研究方向 -->
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.department') }}</span>
              <template v-if="isEditMode">
                <el-input
                  v-model="project.department"
                  :placeholder="$t('project.detail.departmentPlaceholder')"
                />
              </template>
              <template v-else>
                <span class="info-value">{{ project.department || $t('common.none') }}</span>
              </template>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('project.detail.researchDirection') }}</span>
              <template v-if="isEditMode">
                <el-input
                  v-model="project.researchDirection"
                  :placeholder="$t('project.detail.researchDirectionPlaceholder')"
                />
              </template>
              <template v-else>
                <span class="info-value">{{ project.researchDirection || $t('common.none') }}</span>
              </template>
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
                <el-input-number
                  v-model="project.budget"
                  :min="0"
                  :precision="2"
                  :placeholder="$t('project.detail.budgetPlaceholder')"
                  style="width: 100%"
                />
                <span class="budget-unit">{{ $t('common.yuan') }}</span>
              </template>
              <template v-else>
                <span class="info-value">{{ Number(project.budget || 0).toFixed(2) }} {{ $t('common.yuan') }}</span>
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
                size="small"
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
                  {{ getRoleText(member.role) }} · {{ $t('project.detail.joinTime') }}: {{ member.joinTime }}
                </div>
              </div>
              <el-button
                v-if="isEditMode"
                type="danger"
                size="small"
                @click="handleRemoveMember(member.id)"
              >
                {{ $t('common.remove') }}
              </el-button>
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
            @keyup.enter="handleMemberSearch"
            @input="handleMemberSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <div v-if="memberSearchResults.length > 0" class="search-results">
            <div v-for="user in memberSearchResults" :key="user.id" class="search-result-item">
              <div class="search-result-info">
                <div class="search-result-avatar">
                  {{ user.name?.charAt(0) || '?' }}
                </div>
                <div class="search-result-details">
                  <div class="search-result-name">{{ user.name }}</div>
                </div>
              </div>
              <el-button
                type="primary"
                size="small"
                :disabled="members.some(m => m.id === user.id)"
                @click="handleAddMember(user)"
              >
                {{ members.some(m => m.id === user.id) ? $t('common.added') : $t('common.add') }}
              </el-button>
            </div>
          </div>
          <div v-else class="empty-state">
            <div class="empty-state-text">{{ $t('project.detail.searchHint') }}</div>
          </div>
          <template #footer>
            <el-button @click="closeMemberDialog">{{ $t('common.close') }}</el-button>
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
                size="small"
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
                <el-button
                  v-if="isEditMode"
                  type="danger"
                  size="small"
                  @click="handleRemoveMilestone(index)"
                >
                  {{ $t('common.delete') }}
                </el-button>
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
                    <div class="document-icon">📄</div>
                    <div class="document-info">
                      <div class="document-name">{{ doc.name }}</div>
                      <div class="document-meta">
                        {{ doc.type }} · {{ doc.uploader }} · {{ doc.uploadTime }} · {{ doc.size }}
                      </div>
                    </div>
                    <div class="document-actions">
                      <el-button size="small" @click="handlePreview(doc.id)">
                        {{ $t('common.preview') }}
                      </el-button>
                      <el-button size="small" @click="handleDownload(doc.id)">
                        {{ $t('common.download') }}
                      </el-button>
                      <el-button
                        v-if="isEditMode"
                        size="small"
                        type="danger"
                        @click="handleDeleteMilestoneDoc(index, doc.id)"
                      >
                        {{ $t('common.delete') }}
                      </el-button>
                    </div>
                  </div>
                </div>
  
                <div v-else class="empty-state">
                  <div class="empty-state-icon">📄</div>
                  <div class="empty-state-text">{{ $t('project.detail.noDocuments') }}</div>
                  <div class="empty-state-hint">{{ $t('project.detail.uploadDocumentsHint') }}</div>
                </div>
  
                <!-- 上传区域 -->
                <div v-if="isEditMode" class="upload-section">
                  <div
                    class="upload-area"
                    @click="triggerMilestoneUpload(index)"
                    @dragover.prevent="handleMilestoneDragOver(index)"
                    @dragleave.prevent="handleMilestoneDragLeave(index)"
                    @drop.prevent="handleMilestoneDrop($event, index)"
                    :class="{ dragover: milestonesDragOver[index] }"
                  >
                    <div class="upload-icon">
                      <el-icon><Upload /></el-icon>
                    </div>
                    <div class="upload-text">{{ $t('project.detail.uploadText') }}</div>
                    <div class="upload-hint">{{ $t('project.detail.uploadHint') }}</div>
                  </div>
                  <input
                    :id="`milestoneFileInput${index}`"
                    type="file"
                    multiple
                    style="display: none"
                    @change="handleMilestoneFileSelect($event, index)"
                    accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.md,.rtf,.jpg,.jpeg,.png,.gif,.bmp,.svg,.webp,.zip,.rar,.7z,.tar,.gz,.mp4,.avi,.mov,.wmv,.flv,.mp3,.wav,.flac,.aac"
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
              <el-form-item :label="$t('project.detail.startTime')" prop="startTime">
                <BaseDatePicker
                  v-model="milestoneDraft.startTime"
                  type="date"
                  :placeholder="$t('project.detail.selectStartTime')"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              
              <el-form-item :label="$t('project.detail.endTime')" prop="endTime">
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
              {{ $t('common.confirm') }}
            </el-button>
          </template>
        </el-dialog>
      </div>
  
      <!-- 操作按钮 -->
      <div v-if="isEditMode" class="footer-actions">
        <el-button @click="handleCancel">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon>
          {{ $t('project.detail.save') }}
        </el-button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted, computed, nextTick } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { ArrowLeft, Loading, Plus, Search, Upload, Check } from '@element-plus/icons-vue'
  import { BaseCard, BaseDatePicker } from '@/components/Common'
  import { PROJECT_STATUS, PROJECT_STATUS_CONFIG, MILESTONE_OPTIONS } from '@/utils/constants'
  import { getProjectDetail, updateProject, addProjectMember, removeProjectMember } from '@/api/Project'
  import { getUsers } from '@/api/System'
  import { useFileUpload } from '@/composables/useFileUpload'
  import { ATTACHMENT_RELATION, ATTACHMENT_CATEGORY } from '@/constants/attachment'
  import { createLogger } from '@/utils/simpleLogger'
  
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
    status: PROJECT_STATUS.PROGRESS,
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
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.PLANNED].text, value: PROJECT_STATUS.PLANNED },
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.PROGRESS].text, value: PROJECT_STATUS.PROGRESS },
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.COMPLETED].text, value: PROJECT_STATUS.COMPLETED },
    { label: PROJECT_STATUS_CONFIG[PROJECT_STATUS.DELAYED].text, value: PROJECT_STATUS.DELAYED }
  ])
  
  // 成员对话框相关
  const showMemberDialog = ref(false)
  const memberSearchKeyword = ref('')
  const memberSearchResults = ref([])
  
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
  
  // 里程碑拖拽状态管理
  const milestonesDragOver = ref({})
  
  // 文件上传
  const fileUploader = useFileUpload({
    maxSizeMB: 50,
    allowedExtensions: ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.txt', '.md', '.rtf', '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.svg', '.webp', '.zip', '.rar', '.7z', '.tar', '.gz', '.mp4', '.avi', '.mov', '.wmv', '.flv', '.mp3', '.wav', '.flac', '.aac'],
    getExtraFormData: () => ({
      relationType: ATTACHMENT_RELATION.PROJECT,
      attachmentType: ATTACHMENT_CATEGORY.DOCUMENT
    })
  })
  
  // 获取状态文本
  const getStatusText = (status) => {
    return PROJECT_STATUS_CONFIG[status]?.text || status
  }
  
  // 获取状态标签类型
  const getStatusTagType = (status) => {
    return PROJECT_STATUS_CONFIG[status]?.type || 'info'
  }
  
  // 获取角色文本
  const getRoleText = (role) => {
    const roleMap = {
      manager: t('project.leader'),
      core: t('project.coreMember'),
      member: t('project.member')
    }
    return roleMap[role] || t('project.member')
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
          manager: data.managerName || data.manager || '',
          status: mapStatus(data.status),
          startTime: data.startTime || data.projectStartTime || '',
          endTime: data.endTime || data.projectEndTime || '',
          budget: data.budget || 0,
          department: data.department || data.declarationInfo?.department || '',
          researchDirection: data.researchDirection || data.declarationInfo?.researchDirection || ''
        }
        
        // 成员数据映射
        members.value = (data.members || []).map(member => ({
          id: member.userId || member.id,
          name: member.userName || member.name || '',
          role: member.role || 'member',
          joinTime: member.joinTime || member.join_time || ''
        }))
        
        // 里程碑数据映射
        milestones.value = (data.milestones || []).map(milestone => ({
          name: milestone.name || milestone.title || '',
          description: milestone.description || milestone.content || '',
          startTime: milestone.startTime || milestone.milestoneStartTime || '',
          endTime: milestone.endTime || milestone.milestoneEndTime || '',
          documents: milestone.documents || []
        }))
        
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
  
  // 状态映射
  const mapStatus = (status) => {
    const statusMap = {
      'in_progress': PROJECT_STATUS.PROGRESS,
      'progress': PROJECT_STATUS.PROGRESS,
      'completed': PROJECT_STATUS.COMPLETED,
      'delayed': PROJECT_STATUS.DELAYED,
      'planned': PROJECT_STATUS.PLANNED
    }
    return statusMap[status] || status
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
        name: project.value.name,
        description: project.value.description,
        manager: project.value.manager,
        status: project.value.status,
        startTime: project.value.startTime,
        endTime: project.value.endTime,
        budget: project.value.budget,
        department: project.value.department,
        researchDirection: project.value.researchDirection,
        members: members.value.map(m => ({
          userId: m.id,
          role: m.role
        })),
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
  const openMemberDialog = () => {
    memberSearchKeyword.value = ''
    memberSearchResults.value = []
    showMemberDialog.value = true
  }
  
  const closeMemberDialog = () => {
    showMemberDialog.value = false
  }
  
  const handleMemberSearch = async () => {
    if (!memberSearchKeyword.value.trim()) {
      memberSearchResults.value = []
      return
    }
    
    try {
      const response = await getUsers({
        pageNo: 1,
        pageSize: 20,
        keyword: memberSearchKeyword.value
      })
      
      const data = response?.data?.data || response?.data || {}
      const users = data.records || data.list || []
      
      memberSearchResults.value = users.map(user => ({
        id: user.id,
        name: user.realName || user.username || ''
      }))
    } catch (error) {
      logger.error('Failed to search members', error)
      memberSearchResults.value = []
    }
  }
  
  const handleAddMember = async (user) => {
    const isAlreadyMember = members.value.some(member => member.id === user.id)
    if (isAlreadyMember) {
      ElMessage.warning(t('project.detail.memberAlreadyExists'))
      return
    }
    
    try {
      await addProjectMember({
        projectId: project.value.id,
        userId: user.id,
        role: 'member'
      })
      
      const newMember = {
        id: user.id,
        name: user.name,
        role: 'member',
        joinTime: new Date().toISOString().split('T')[0]
      }
      
      members.value.push(newMember)
      ElMessage.success(t('project.detail.memberAddedSuccess', { name: user.name }))
      
      memberSearchKeyword.value = ''
      memberSearchResults.value = []
      
      setTimeout(() => {
        showMemberDialog.value = false
      }, 1000)
    } catch (error) {
      logger.error('Failed to add member', error)
      ElMessage.error(error.message || t('project.detail.addMemberError'))
    }
  }
  
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
      
      await removeProjectMember({
        projectId: project.value.id,
        userId: memberId
      })
      
      const index = members.value.findIndex(m => m.id === memberId)
      if (index > -1) {
        members.value.splice(index, 1)
        ElMessage.success(t('project.detail.memberRemovedSuccess', { name: member.name }))
      }
    } catch (error) {
      if (error !== 'cancel') {
        logger.error('Failed to remove member', error)
        ElMessage.error(error.message || t('project.detail.removeMemberError'))
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
      
      milestones.value.push({
        name: d.name.trim(),
        startTime: d.startTime,
        endTime: d.endTime,
        description: d.description || '',
        documents: []
      })
      
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
  
  // 里程碑文档管理
  const triggerMilestoneUpload = (milestoneIndex) => {
    nextTick(() => {
      const fileInput = document.getElementById(`milestoneFileInput${milestoneIndex}`)
      if (fileInput) {
        fileInput.click()
      }
    })
  }
  
  const handleMilestoneFileSelect = async (event, milestoneIndex) => {
    const files = Array.from(event.target.files)
    if (files.length > 0) {
      await uploadMilestoneFiles(files, milestoneIndex)
      event.target.value = ''
    }
  }
  
  const uploadMilestoneFiles = async (files, milestoneIndex) => {
    if (!milestones.value[milestoneIndex].documents) {
      milestones.value[milestoneIndex].documents = []
    }
    
    const loadingMessage = ElMessage({
      message: t('project.detail.uploadingFiles', { count: files.length }),
      type: 'info',
      duration: 0,
      showClose: false
    })
    
    try {
      let successCount = 0
      for (const file of files) {
        const existingDoc = milestones.value[milestoneIndex].documents.find(
          doc => doc.name === file.name
        )
        
        if (existingDoc) {
          ElMessage.warning(t('project.detail.fileAlreadyExists', { name: file.name }))
          continue
        }
        
        // 使用 useFileUpload 上传文件
        const result = await fileUploader.uploadWithCheck(file)
        if (!result || !result.fileInfo) {
          continue
        }
        
        const newDoc = {
          id: result.fileInfo.id || Date.now() + Math.random(),
          name: file.name,
          type: file.name.split('.').pop().toUpperCase(),
          uploader: '当前用户',
          uploadTime: new Date().toISOString().split('T')[0],
          size: formatFileSize(file.size),
          fileInfo: result.fileInfo
        }
        
        milestones.value[milestoneIndex].documents.push(newDoc)
        successCount++
      }
      
      if (successCount > 0) {
        ElMessage.success(t('project.detail.documentsAddedSuccess', { 
          milestoneIndex: milestoneIndex + 1, 
          count: successCount 
        }))
      }
    } catch (error) {
      logger.error('Failed to upload milestone files', error)
      ElMessage.error(error.message || t('project.detail.uploadError'))
    } finally {
      loadingMessage.close()
    }
  }
  
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
  }
  
  const handleDeleteMilestoneDoc = (milestoneIndex, docId) => {
    const milestone = milestones.value[milestoneIndex]
    if (milestone.documents) {
      const docIndex = milestone.documents.findIndex(doc => doc.id === docId)
      if (docIndex > -1) {
        const docName = milestone.documents[docIndex].name
        milestone.documents.splice(docIndex, 1)
        ElMessage.success(t('project.detail.documentDeletedSuccess', { name: docName }))
      }
    }
  }
  
  // 里程碑拖拽事件处理
  const handleMilestoneDragOver = (milestoneIndex) => {
    milestonesDragOver.value[milestoneIndex] = true
  }
  
  const handleMilestoneDragLeave = (milestoneIndex) => {
    milestonesDragOver.value[milestoneIndex] = false
  }
  
  const handleMilestoneDrop = async (event, milestoneIndex) => {
    event.preventDefault()
    milestonesDragOver.value[milestoneIndex] = false
    const files = Array.from(event.dataTransfer.files)
    if (files.length > 0) {
      await uploadMilestoneFiles(files, milestoneIndex)
    }
  }
  
  // 文档操作
  const handlePreview = (docId) => {
    ElMessage.info(t('project.detail.previewDocument', { docId }))
  }
  
  const handleDownload = (docId) => {
    ElMessage.info(t('project.detail.downloadDocument', { docId }))
  }
  
  onMounted(() => {
    loadProjectDetail()
  })
  </script>
  
  <style lang="scss" scoped>
  .project-detail-container {
    padding: var(--gap-lg);
    background: var(--bg-secondary);
    min-height: calc(100vh - 56px);
  }
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--gap-lg);
  }
  
  .header-left {
    display: flex;
    align-items: center;
    gap: var(--gap-md);
  }
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
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
    color: var(--text-primary);
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
  
  .budget-unit {
    margin-left: var(--gap-xs);
    color: var(--text-secondary);
    font-size: 14px;
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
    justify-content: space-between;
    padding: var(--gap-sm);
    border-bottom: 1px solid var(--border);
  }
  
  .search-result-item:last-child {
    border-bottom: none;
  }
  
  .search-result-item:hover {
    background: var(--bg-tertiary);
  }
  
  .search-result-info {
    display: flex;
    align-items: center;
    gap: var(--gap-sm);
  }
  
  .search-result-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: var(--bg-tertiary);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-primary);
    font-weight: 500;
    font-size: 16px;
  }
  
  .search-result-details {
    flex: 1;
  }
  
  .search-result-name {
    font-weight: 500;
    color: var(--text-primary);
    font-size: 14px;
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
  
  .milestone-title {
    font-weight: 600;
    color: var(--text-primary);
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
    color: var(--text-primary);
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
  
  .upload-section {
    margin-top: var(--gap-sm);
  }
  
  .upload-area {
    border: 2px dashed var(--border);
    border-radius: var(--radius-md);
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: border-color 0.3s, background-color 0.3s;
  }
  
  .upload-area:hover {
    border-color: var(--primary);
  }
  
  .upload-area.dragover {
    border-color: var(--primary);
    background: var(--bg-primary);
  }
  
  .upload-icon {
    font-size: 24px;
    color: var(--text-secondary);
    margin-bottom: var(--gap-xs);
  }
  
  .upload-text {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 4px;
  }
  
  .upload-hint {
    font-size: 12px;
    color: var(--text-tertiary);
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
  