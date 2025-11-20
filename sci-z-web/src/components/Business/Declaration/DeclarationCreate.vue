<!--
/**
 * @description 新建申报业务组件
 * 包含红头文件上传、AI分析、表单填写、工作流选择、申报提交等功能
 */
-->
<template>
    <div class="declaration-create-container">
      <div class="page-header">
        <el-tooltip :content="$t('common.backToList')" placement="bottom">
          <div class="back-icon" @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
          </div>
        </el-tooltip>
        <h1 class="page-title">{{ $t('declaration.newDeclaration') }}</h1>
      </div>
  
      <BaseCard class="form-card">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
        >
          <!-- 红头文件上传 -->
          <div class="form-group">
            <div class="group-title">{{ $t('declaration.documentUpload') }}</div>
            
            <div v-if="!uploadedFile" class="upload-area" @click="triggerFileInput">
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">{{ $t('declaration.clickUpload') }}</div>
              <div class="upload-hint">{{ $t('declaration.uploadHint') }}</div>
              <input
                ref="fileInputRef"
                type="file"
                accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                style="display: none"
                @change="handleFileChange"
              />
            </div>
  
            <div v-else class="file-info-section">
              <div class="file-info">
                <el-icon class="file-icon"><Document /></el-icon>
                <span class="file-name">{{ uploadedFile.name }}</span>
                <BaseButton
                  type="danger"
                  size="small"
                  @click="clearUploadedFile"
                >
                  {{ $t('common.delete') }}
                </BaseButton>
              </div>
  
              <!-- 上传进度 -->
              <div v-if="uploadStatus === 'uploading'" class="upload-progress">
                <el-progress :percentage="uploadProgress" />
              </div>
  
              <!-- 分析状态 -->
              <div v-if="uploadStatus === 'analyzing'" class="upload-status analyzing">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>{{ $t('declaration.analyzing') }}</span>
              </div>
  
              <div v-if="uploadStatus === 'success'" class="upload-status success">
                <el-icon><Check /></el-icon>
                <span>{{ $t('declaration.analysisComplete') }}</span>
              </div>
  
              <div v-if="uploadStatus === 'error'" class="upload-status error">
                <el-icon><Close /></el-icon>
                <span>{{ $t('declaration.analysisFailed') }}</span>
              </div>
  
              <!-- 分析结果预览 -->
              <div v-if="analysisResult && uploadStatus === 'success'" class="analysis-result">
                <div class="analysis-title">
                  <el-icon><MagicStick /></el-icon>
                  {{ $t('declaration.aiAnalysisResult') }}
                </div>
                <p>
                  <strong>{{ $t('declaration.direction') }}：</strong>
                  {{ analysisResult.researchDirection }}
                </p>
                <p>
                  <strong>{{ $t('declaration.topic') }}：</strong>
                  {{ analysisResult.researchTopic }}
                </p>
                <div class="analysis-hint">
                  💡 {{ $t('declaration.aiHint') }}
                </div>
              </div>
            </div>
          </div>
  
          <!-- 基本信息 -->
          <div class="form-group">
            <div class="group-title">{{ $t('declaration.basicInfo') }}</div>
  
            <div class="form-row">
              <el-form-item :label="$t('declaration.department')" prop="department">
                <el-select
                  v-model="form.department"
                  :placeholder="$t('declaration.departmentPlaceholder')"
                  style="width: 100%"
                >
                  <el-option
                    v-for="option in departmentOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
  
              <el-form-item :label="$t('declaration.projectLeader')" prop="projectLeader">
                <el-input
                  v-model="form.projectLeader"
                  :placeholder="$t('declaration.projectLeaderPlaceholder')"
                />
              </el-form-item>
            </div>
  
            <div class="form-row">
              <el-form-item
                :label="$t('declaration.documentPublishTime')"
                prop="documentPublishTime"
              >
                <BaseDatePicker
                  v-model="form.documentPublishTime"
                  type="date"
                  :placeholder="$t('declaration.documentPublishTimePlaceholder')"
                  format="YYYY年MM月DD日"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <div class="form-item-empty"></div>
            </div>
  
            <div class="form-row">
              <el-form-item :label="$t('declaration.projectStartTime')" prop="projectStartTime">
                <BaseDatePicker
                  v-model="form.projectStartTime"
                  type="date"
                  :placeholder="$t('declaration.projectStartTimePlaceholder')"
                  format="YYYY年MM月DD日"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
  
              <el-form-item :label="$t('declaration.projectEndTime')" prop="projectEndTime">
                <BaseDatePicker
                  v-model="form.projectEndTime"
                  type="date"
                  :placeholder="$t('declaration.projectEndTimePlaceholder')"
                  format="YYYY年MM月DD日"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
          </div>
  
          <!-- 研究信息 -->
          <div class="form-group">
            <div class="group-title">{{ $t('declaration.researchInfo') }}</div>
  
            <el-form-item
              :label="$t('declaration.direction')"
              prop="researchDirection"
              class="form-item-full"
            >
              <el-input
                v-model="form.researchDirection"
                type="textarea"
                :rows="4"
                :placeholder="$t('declaration.directionPlaceholder')"
              />
              <div
                v-if="analysisResult && uploadStatus === 'success'"
                class="auto-filled-hint"
              >
                ✨ {{ $t('declaration.autoFilled') }}
              </div>
            </el-form-item>
  
            <el-form-item
              :label="$t('declaration.topic')"
              prop="researchTopic"
              class="form-item-full"
            >
              <el-input
                v-model="form.researchTopic"
                type="textarea"
                :rows="3"
                :placeholder="$t('declaration.topicPlaceholder')"
              />
              <div
                v-if="analysisResult && uploadStatus === 'success'"
                class="auto-filled-hint"
              >
                ✨ {{ $t('declaration.autoFilled') }}
              </div>
            </el-form-item>
          </div>
  
          <!-- 研究领域 -->
          <div class="form-group">
            <div class="group-title">
              {{ $t('declaration.fields') }} <span class="required-mark">*</span>
            </div>
            <el-form-item prop="researchField" class="research-field-item">
              <div class="tag-input-container">
                <div
                  v-for="(tag, index) in form.researchField"
                  :key="index"
                  class="tag-item"
                >
                  {{ tag }}
                  <span class="tag-remove" @click="removeTag(index)">×</span>
                </div>
                <input
                  v-model="tagInput"
                  class="tag-input"
                  :placeholder="$t('declaration.fieldPlaceholder')"
                  @keydown.enter.prevent="addTag"
                  @keydown.backspace="handleBackspace"
                />
              </div>
              <div class="tag-hint">
                {{ $t('declaration.fieldCount', { current: form.researchField.length, max: 10 }) }}
              </div>
              <div v-if="formErrors.researchField" class="error-message">
                {{ formErrors.researchField }}
              </div>
            </el-form-item>
          </div>
  
          <!-- 工作流选择 -->
          <div class="form-group">
            <div class="group-title">
              {{ $t('declaration.workflow') }} <span class="required-mark">*</span>
            </div>
            <el-form-item :label="$t('declaration.selectWorkflow')" prop="workflow">
              <el-select
                v-model="form.workflow"
                :placeholder="$t('declaration.workflowPlaceholder')"
                :loading="workflowLoading"
                style="width: 100%"
                filterable
                :filter-method="filterWorkflow"
              >
                <el-option
                  v-for="workflow in filteredWorkflowOptions"
                  :key="workflow.id"
                  :label="workflow.name"
                  :value="workflow.id"
                >
                  <div class="workflow-option">
                    <div class="workflow-name">{{ workflow.name }}</div>
                    <div class="workflow-description">{{ workflow.description }}</div>
                  </div>
                </el-option>
              </el-select>
              <div v-if="selectedWorkflow" class="workflow-info">
                <div class="workflow-info-title">{{ selectedWorkflow.name }}</div>
                <div class="workflow-info-description">{{ selectedWorkflow.description }}</div>
              </div>
            </el-form-item>
          </div>
  
          <!-- 表单操作按钮 -->
          <div class="form-actions">
            <BaseButton
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ submitting ? $t('declaration.submitting') : $t('declaration.submit') }}
            </BaseButton>
            <BaseButton size="large" @click="handleBack">
              {{ $t('common.cancel') }}
            </BaseButton>
          </div>
        </el-form>
      </BaseCard>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox, ElTooltip } from 'element-plus'
  import {
    ArrowLeft,
    Loading,
    MagicStick,
    UploadFilled,
    Document
  } from '@element-plus/icons-vue'
  import { BaseCard, BaseButton, BaseDatePicker } from '@/components/Common'
  import { DECLARATION_DEPARTMENT_OPTIONS } from '@/utils/constants'
  import { useFileUpload } from '@/composables/useFileUpload'
  import { ATTACHMENT_RELATION, ATTACHMENT_CATEGORY } from '@/constants/attachment'
  import { createLogger } from '@/utils/simpleLogger'
  
  const router = useRouter()
  const { t } = useI18n()
  const logger = createLogger('DeclarationCreate')
  
  // 表单引用
  const formRef = ref()
  const fileInputRef = ref()
  
  // 表单数据
  const form = reactive({
    documentFile: null,
    department: '',
    projectLeader: '',
    documentPublishTime: '',
    projectStartTime: '',
    projectEndTime: '',
    researchDirection: '',
    researchTopic: '',
    researchField: [],
    workflow: ''
  })
  
  // 表单验证规则
  const rules = computed(() => ({
    department: [
      {
        required: true,
        message: t('declaration.departmentRequired'),
        trigger: 'change'
      }
    ],
    projectLeader: [
      {
        required: true,
        message: t('declaration.projectLeaderRequired'),
        trigger: 'blur'
      }
    ],
    documentPublishTime: [
      {
        required: true,
        message: t('declaration.documentPublishTimeRequired'),
        trigger: 'change'
      }
    ],
    projectStartTime: [
      {
        required: true,
        message: t('declaration.projectStartTimeRequired'),
        trigger: 'change'
      }
    ],
    projectEndTime: [
      {
        required: true,
        message: t('declaration.projectEndTimeRequired'),
        trigger: 'change'
      }
    ],
    researchDirection: [
      {
        required: true,
        message: t('declaration.researchDirectionRequired'),
        trigger: 'change'
      }
    ],
    researchTopic: [
      {
        required: true,
        message: t('declaration.researchTopicRequired'),
        trigger: 'change'
      }
    ],
    researchField: [
      {
        required: true,
        validator: (rule, value, callback) => {
          if (!value || value.length === 0) {
            callback(new Error(t('declaration.researchFieldRequired')))
          } else if (value.length > 10) {
            callback(new Error(t('declaration.researchFieldCountExceeded')))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ],
    workflow: [
      {
        required: true,
        message: t('declaration.workflowRequired'),
        trigger: 'change'
      }
    ]
  }))
  
  // 状态管理
  const submitting = ref(false)
  const uploadedFile = ref(null)
  const uploadStatus = ref('idle') // idle, uploading, analyzing, success, error
  const uploadProgress = ref(0)
  const analysisResult = ref(null)
  
  // 文件上传
  const fileUploader = useFileUpload({
    maxSizeMB: 10,
    allowedExtensions: ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png'],
    getExtraFormData: () => ({
      relationType: ATTACHMENT_RELATION.DECLARATION,
      attachmentType: ATTACHMENT_CATEGORY.DOCUMENT
    })
  })
  
  // 部门选项
  const departmentOptions = DECLARATION_DEPARTMENT_OPTIONS
  
  // 工作流选项
  const workflowOptions = ref([])
  const workflowLoading = ref(false)
  const workflowSearchQuery = ref('')
  const filteredWorkflowOptions = ref([])
  
  // 选中的工作流
  const selectedWorkflow = computed(() => {
    return workflowOptions.value.find(workflow => workflow.id === form.workflow)
  })
  
  // 标签输入
  const tagInput = ref('')
  const formErrors = reactive({
    researchField: ''
  })
  
  // 触发文件选择
  const triggerFileInput = () => {
    fileInputRef.value?.click()
  }
  
  // 文件选择处理
  const handleFileChange = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return
  
    try {
      uploadStatus.value = 'uploading'
      uploadProgress.value = 0
  
      logger.info('Starting file upload', { fileName: file.name, fileSize: file.size })
  
      // TODO: 后端接口开发完成后替换为实际接口调用
      // const result = await fileUploader.uploadWithCheck(file)
      // if (!result) return
      // uploadedFile.value = result.fileInfo
      // form.documentFile = result.fileInfo
  
      // 临时模拟文件上传过程（后端开发完成后删除）
      const uploadInterval = setInterval(() => {
        uploadProgress.value += 10
        if (uploadProgress.value >= 100) {
          clearInterval(uploadInterval)
          uploadedFile.value = file
          form.documentFile = file
          analyzeDocument(file)
        }
      }, 200)
  
      logger.info('File upload completed', { fileName: file.name })
    } catch (error) {
      logger.error('File upload failed', error)
      uploadStatus.value = 'error'
      ElMessage.error(t('declaration.uploadError'))
    }
  }
  
  // 文档分析处理
  const analyzeDocument = async (file) => {
    try {
      uploadStatus.value = 'analyzing'
      logger.info('Starting document analysis', { fileName: file.name })
  
      // TODO: 后端接口开发完成后替换为实际接口调用
      // const response = await analyzeDeclarationDocument(file.id || file.name)
      // analysisResult.value = response.data.analysis
      // form.researchDirection = response.data.analysis.researchDirection
      // form.researchTopic = response.data.analysis.researchTopic
  
      // 临时模拟AI分析过程（后端开发完成后删除）
      await new Promise(resolve => setTimeout(resolve, 3000))
  
      const mockResult = {
        researchDirection: t('declaration.sampleDirection') || '人工智能与机器学习研究',
        researchTopic: t('declaration.sampleTopic') || '基于深度学习的智能推荐系统研究'
      }
  
      analysisResult.value = mockResult
      form.researchDirection = mockResult.researchDirection
      form.researchTopic = mockResult.researchTopic
  
      uploadStatus.value = 'success'
      ElMessage.success(t('declaration.analysisSuccess'))
      logger.info('Document analysis completed successfully')
    } catch (error) {
      logger.error('Document analysis failed', error)
      uploadStatus.value = 'error'
      ElMessage.error(t('declaration.analysisError'))
    }
  }
  
  // 清除上传的文件
  const clearUploadedFile = () => {
    uploadedFile.value = null
    form.documentFile = null
    analysisResult.value = null
    uploadStatus.value = 'idle'
    uploadProgress.value = 0
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
  
  // 标签功能
  const addTag = () => {
    const value = tagInput.value.trim()
    if (!value) return
  
    if (form.researchField.includes(value)) {
      ElMessage.warning(t('declaration.researchFieldDuplicate'))
      tagInput.value = ''
      return
    }
  
    if (form.researchField.length >= 10) {
      ElMessage.warning(t('declaration.researchFieldCountExceeded'))
      tagInput.value = ''
      return
    }
  
    form.researchField.push(value)
    tagInput.value = ''
    formErrors.researchField = ''
  }
  
  const removeTag = (index) => {
    form.researchField.splice(index, 1)
    formErrors.researchField = ''
  }
  
  const handleBackspace = () => {
    if (tagInput.value === '' && form.researchField.length > 0) {
      form.researchField.pop()
    }
  }
  
  // 工作流过滤
  const filterWorkflow = (query) => {
    workflowSearchQuery.value = query
    if (!query) {
      filteredWorkflowOptions.value = [...workflowOptions.value]
      return
    }
  
    const lowerQuery = query.toLowerCase()
    filteredWorkflowOptions.value = workflowOptions.value.filter(workflow => {
      return (
        workflow.name.toLowerCase().includes(lowerQuery) ||
        workflow.description?.toLowerCase().includes(lowerQuery)
      )
    })
  }
  
  // 加载工作流选项
  const loadWorkflowOptions = async () => {
    try {
      workflowLoading.value = true
      logger.info('Loading workflow options')
  
      // TODO: 后端接口开发完成后替换为实际接口调用
      // const response = await getWorkflowOptions()
      // workflowOptions.value = response.data.options
      // filteredWorkflowOptions.value = [...workflowOptions.value]
  
      // 临时模拟工作流数据（后端开发完成后删除）
      await new Promise(resolve => setTimeout(resolve, 500))
  
      workflowOptions.value = [
        {
          id: 'workflow_001',
          name: t('declaration.workflowOption1') || '标准申报流程',
          description: t('declaration.workflowDesc1') || '适用于常规科研项目申报'
        },
        {
          id: 'workflow_002',
          name: t('declaration.workflowOption2') || '快速申报流程',
          description: t('declaration.workflowDesc2') || '适用于紧急项目申报'
        },
        {
          id: 'workflow_003',
          name: t('declaration.workflowOption3') || '专家评审流程',
          description: t('declaration.workflowDesc3') || '适用于需要专家评审的项目'
        },
        {
          id: 'workflow_004',
          name: t('declaration.workflowOption4') || '简化申报流程',
          description: t('declaration.workflowDesc4') || '适用于小型项目申报'
        }
      ]
  
      filteredWorkflowOptions.value = [...workflowOptions.value]
      logger.info('Workflow options loaded successfully', { count: workflowOptions.value.length })
    } catch (error) {
      logger.error('Failed to load workflow options', error)
      ElMessage.error(t('declaration.workflowLoadError'))
    } finally {
      workflowLoading.value = false
    }
  }
  
  // 提交处理
  const handleSubmit = async () => {
    try {
      await formRef.value.validate()
    } catch (error) {
      ElMessage.error(t('declaration.formIncomplete'))
      return
    }
  
    try {
      await ElMessageBox.confirm(
        t('declaration.confirmSubmit'),
        t('declaration.confirmTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel')
        }
      )
  
      submitting.value = true
  
      logger.info('Submitting declaration', { form })
  
      // TODO: 后端接口开发完成后替换为实际接口调用
      // const response = await createDeclaration({
      //   ...form,
      //   documentFileId: form.documentFile?.id
      // })
      // const declarationId = response.data.id
  
      // 临时模拟提交申报（后端开发完成后删除）
      await new Promise(resolve => setTimeout(resolve, 2000))
  
      ElMessage.success(t('declaration.submitSuccess'))
  
      // 跳转到申报详情页面
      setTimeout(() => {
        router.push('/declaration/detail/1')
      }, 1000)
    } catch (error) {
      if (error !== 'cancel') {
        logger.error('Submit failed', error)
        ElMessage.error(t('declaration.submitError'))
      }
    } finally {
      submitting.value = false
    }
  }
  
  // 返回处理
  const handleBack = () => {
    ElMessageBox.confirm(
      t('declaration.confirmLeave'),
      t('declaration.confirmLeaveTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('declaration.continueEdit'),
        type: 'warning'
      }
    )
      .then(() => {
        router.push('/declaration/list')
      })
      .catch(() => {
        // 用户取消离开
      })
  }
  
  // 生命周期
  onMounted(() => {
    loadWorkflowOptions()
  })
  </script>
  
  <style lang="scss" scoped>
  .declaration-create-container {
    padding: 20px;
    background: var(--bg);
    min-height: calc(100vh - 60px);
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    gap: 12px;
  
    .back-icon {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s ease;
      color: var(--text-2);
      background: var(--surface);
      border: 1px solid var(--border);
      
      &:hover {
        color: var(--color-primary);
        background: var(--hover);
        border-color: var(--color-primary);
      }
      
      .el-icon {
        font-size: 18px;
      }
    }
  
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--color-primary);
      margin: 0;
    }
  }
  
  .form-card {
    background: var(--surface);
    border-radius: 12px;
    padding: 24px;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    margin: 0 auto;
  }
  
  .form-group {
    margin-bottom: 40px;
  
    .group-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--color-primary);
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid var(--border);
      display: flex;
      align-items: center;
      gap: 8px;
  
      .required-mark {
        color: #dc2626;
      }
  
      &::before {
        content: '';
        width: 4px;
        height: 20px;
        background: linear-gradient(135deg, var(--color-primary), #3b82f6);
        border-radius: 2px;
      }
    }
  }
  
  .form-row {
    display: flex;
    gap: 24px;
    margin-bottom: 24px;
    align-items: flex-start;
  
    .el-form-item {
      flex: 1;
      margin-bottom: 0;
    }
  
    .form-item-empty {
      flex: 1;
    }
  }
  
  .form-item-full {
    width: 100%;
    margin-bottom: 24px;
  }
  
  .research-field-item {
    width: 100%;
    
    :deep(.el-form-item__content) {
      width: 100%;
      max-width: 100%;
    }
  }
  
  // 文件上传样式
  .upload-area {
    border: 2px dashed var(--border);
    border-radius: 12px;
    padding: 32px;
    text-align: center;
    background: var(--bg);
    transition: all 0.3s ease;
    cursor: pointer;
  
    &:hover {
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
  }
  
  .file-info-section {
    .file-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background: var(--bg);
      border-radius: 8px;
      margin-bottom: 12px;
  
      .file-icon {
        font-size: 24px;
        color: var(--color-primary);
      }
  
      .file-name {
        flex: 1;
        font-weight: 500;
        color: var(--text);
      }
    }
  
    .upload-progress {
      margin-top: 12px;
    }
  
    .upload-status {
      margin-top: 12px;
      padding: 12px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 8px;
  
      &.success {
        background: #f0fdf4;
        color: #166534;
        border: 1px solid #bbf7d0;
      }
  
      &.analyzing {
        background: #fffbeb;
        color: #92400e;
        border: 1px solid #fde68a;
      }
  
      &.error {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
      }
    }
  
    .analysis-result {
      margin-top: 16px;
      padding: 16px;
      background: #f0fdf4;
      border-radius: 8px;
      border: 1px solid #bbf7d0;
  
      .analysis-title {
        font-weight: 600;
        color: #166534;
        margin-bottom: 8px;
        display: flex;
        align-items: center;
        gap: 6px;
      }
  
      p {
        margin: 8px 0;
        color: var(--text);
      }
  
      .analysis-hint {
        margin-top: 8px;
        font-size: 12px;
        color: #6b7280;
      }
    }
  }
  
  .auto-filled-hint {
    margin-top: 4px;
    font-size: 12px;
    color: #16a34a;
  }
  
  // 标签输入样式
  .tag-input-container {
    border: 1px solid var(--border);
    border-radius: 6px;
    padding: 8px 12px;
    min-height: 40px;
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    position: relative;
    background: var(--surface);
    transition: all 0.2s ease;
    box-sizing: border-box;
  
    &:focus-within {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.1);
    }
  
    .tag-item {
      background: #e0e7ff;
      color: var(--color-primary);
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      line-height: 1.5;
      height: 24px;
      flex-shrink: 0;
  
      .tag-remove {
        cursor: pointer;
        color: var(--text-3);
        font-weight: bold;
        font-size: 14px;
        line-height: 1;
        margin-left: 2px;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 14px;
        height: 14px;
        border-radius: 50%;
        transition: all 0.2s ease;
  
        &:hover {
          color: #dc2626;
          background: rgba(220, 38, 38, 0.1);
        }
      }
    }
  
    .tag-input {
      border: none;
      outline: none;
      flex: 1;
      min-width: 200px;
      font-size: 14px;
      background: transparent;
      color: var(--text);
      line-height: 24px;
      height: 24px;
  
      &::placeholder {
        color: var(--text-3);
      }
    }
  }
  
  .tag-hint {
    margin-top: 8px;
    font-size: 12px;
    color: var(--text-3);
  }
  
  .error-message {
    color: #dc2626;
    font-size: 12px;
    margin-top: 4px;
  }
  
  // 工作流选择样式
  :deep(.el-select-dropdown__item) {
    height: auto !important;
    padding: 10px 20px !important;
    line-height: normal !important;
  }
  
  .workflow-option {
    padding: 0;
    width: 100%;
  
    .workflow-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--text);
      line-height: 1.5;
      margin-bottom: 4px;
    }
  
    .workflow-description {
      font-size: 12px;
      color: var(--text-3);
      line-height: 1.4;
      margin-top: 2px;
    }
  }
  
  .workflow-info {
    margin-top: 12px;
    padding: 16px;
    background: var(--bg);
    border-radius: 8px;
    border: 1px solid var(--border);
  
    .workflow-info-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-primary);
      margin-bottom: 8px;
    }
  
    .workflow-info-description {
      font-size: 14px;
      color: var(--text-2);
      line-height: 1.5;
    }
  }
  
  // 表单操作按钮
  .form-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid var(--border);
  }
  
  // Element Plus 表单项间距优化
  :deep(.el-form-item) {
    margin-bottom: 24px !important;
  }
  
  :deep(.el-form-item__label) {
    padding-bottom: 8px !important;
    font-weight: 500 !important;
    color: var(--text) !important;
  }
  
  :deep(.el-form-item__content) {
    line-height: 1.5 !important;
  }
  
  // 响应式设计
  @media (max-width: 768px) {
    .form-row {
      flex-direction: column;
      gap: 16px;
    }
  
    .form-row .el-form-item {
      flex: none;
      width: 100%;
    }
  
    .form-actions {
      flex-direction: column;
    }
  }
  </style>