<template>
  <div class="new-declaration-container">
    <div class="page-header">
      <div class="brand">
        <img :src="brandLogo" :alt="$t('app.title')" class="brand-logo" />
      </div>
      <h1 class="page-title">{{ $t('declaration.newDeclaration') }}</h1>
    </div>

    <div class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <div class="form-group">
          <div class="group-title">{{ $t('declaration.documentUpload') }}</div>

          <div
            v-if="!form.documentFile"
            class="upload-area"
            :class="{ dragover: isDragOver }"
            @click="openFileDialog"
            @dragover.prevent="handleDragOver"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
          >
            <div class="upload-icon">📁</div>
            <div class="upload-text">{{ $t('declaration.clickUpload') }}</div>
            <div class="upload-hint">{{ $t('declaration.uploadHint') }}</div>
            <input
              ref="fileInputRef"
              type="file"
              accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
              class="hidden-input"
              @change="handleFileChange"
            />
          </div>

          <div v-else>
            <div class="file-info">
              <span class="file-name">{{ form.documentFile.name }}</span>
              <div class="file-actions">
                <el-button size="small" type="danger" @click="clearUploadedFile">
                  {{ $t('common.delete') }}
                </el-button>
              </div>
            </div>

            <div v-if="uploadStatus === 'uploading'" class="upload-progress">
              <el-progress :percentage="uploadProgress" />
            </div>

            <div v-if="uploadStatus === 'analyzing'" class="upload-status analyzing">
              <el-icon class="is-loading"><Loading /></el-icon>
              {{ $t('declaration.analyzing') }}
            </div>

            <div v-if="uploadStatus === 'success'" class="upload-status success">
              <el-icon><Check /></el-icon>
              {{ $t('declaration.analysisComplete') }}
            </div>

            <div v-if="uploadStatus === 'error'" class="upload-status error">
              <el-icon><Close /></el-icon>
              {{ $t('declaration.analysisFailed') }}
            </div>

            <div
              v-if="analysisResult && uploadStatus === 'success'"
              class="analysis-result"
            >
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

        <div class="form-group">
          <div class="group-title">{{ $t('declaration.basicInfo') }}</div>
          <div class="form-row">
            <div class="form-item">
              <el-form-item :label="$t('declaration.department')" prop="department">
                <el-select
                  v-model="form.department"
                  :placeholder="$t('declaration.departmentPlaceholder')"
                >
                  <el-option
                    v-for="option in departmentOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </div>
            <div class="form-item">
              <el-form-item :label="$t('declaration.projectLeader')" prop="projectLeader">
                <el-input
                  v-model="form.projectLeader"
                  :placeholder="$t('declaration.projectLeaderPlaceholder')"
                />
              </el-form-item>
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <el-form-item :label="$t('declaration.documentPublishTime')" prop="documentPublishTime">
                <el-date-picker
                  v-model="form.documentPublishTime"
                  type="date"
                  :placeholder="$t('declaration.documentPublishTimePlaceholder')"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </div>
            <div class="form-item"></div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <el-form-item :label="$t('declaration.projectStartTime')" prop="projectStartTime">
                <el-date-picker
                  v-model="form.projectStartTime"
                  type="date"
                  :placeholder="$t('declaration.projectStartTimePlaceholder')"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </div>
            <div class="form-item">
              <el-form-item :label="$t('declaration.projectEndTime')" prop="projectEndTime">
                <el-date-picker
                  v-model="form.projectEndTime"
                  type="date"
                  :placeholder="$t('declaration.projectEndTimePlaceholder')"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </div>
          </div>
        </div>

        <div class="form-group">
          <div class="group-title">{{ $t('declaration.researchInfo') }}</div>
          <div class="form-row single">
            <el-form-item :label="$t('declaration.direction')" prop="researchDirection" class="form-item">
              <el-input
                v-model="form.researchDirection"
                type="textarea"
                :rows="4"
                :placeholder="$t('declaration.directionPlaceholder')"
              />
              <div
                v-if="analysisResult && uploadStatus === 'success'"
                class="auto-filled-tip"
              >
                ✨ {{ $t('declaration.autoFilled') }}
              </div>
            </el-form-item>
          </div>

          <div class="form-row single">
            <el-form-item :label="$t('declaration.topic')" prop="researchTopic" class="form-item">
              <el-input
                v-model="form.researchTopic"
                type="textarea"
                :rows="3"
                :placeholder="$t('declaration.topicPlaceholder')"
              />
              <div
                v-if="analysisResult && uploadStatus === 'success'"
                class="auto-filled-tip"
              >
                ✨ {{ $t('declaration.autoFilled') }}
              </div>
            </el-form-item>
          </div>
        </div>

        <div class="form-group">
          <div class="group-title">
            {{ $t('declaration.fields') }}
            <span class="required">*</span>
          </div>
          <div class="form-item">
            <div class="tag-input-container">
              <div
                v-for="(tag, index) in form.researchField"
                :key="`${tag}-${index}`"
                class="tag-item"
              >
                {{ tag }}
                <span class="tag-remove" @click="removeTag(index)">×</span>
              </div>
              <input
                ref="tagInputElementRef"
                v-model="tagInput"
                class="tag-input"
                :placeholder="$t('declaration.fieldPlaceholder')"
                @keydown.enter.prevent="addTag"
                @keydown.backspace="handleBackspace"
                @input="handleTagInput"
              />
            </div>
            <div class="tag-helper">
              {{ $t('declaration.fieldCount', { current: form.researchField.length, max: 10 }) }}
            </div>
            <div v-if="formErrors.researchField" class="error-message">
              {{ formErrors.researchField }}
            </div>
            <div v-if="formSuccess.researchField" class="success-message">
              ✓ {{ $t('declaration.fieldComplete') }}
            </div>
          </div>
        </div>

        <div class="form-group">
          <div class="group-title">
            {{ $t('declaration.workflow') }}
            <span class="required">*</span>
          </div>
          <div class="form-item">
            <el-form-item :label="$t('declaration.selectWorkflow')" prop="workflow">
              <el-select
                v-model="form.workflow"
                :placeholder="$t('declaration.workflowPlaceholder')"
                :loading="workflowLoading"
                filterable
                :filter-method="filterWorkflow"
                :no-match-text="$t('declaration.noWorkflowMatch')"
                :no-data-text="$t('declaration.noWorkflowData')"
              >
                <el-option
                  v-for="workflow in filteredWorkflowOptions"
                  :key="workflow.id"
                  :label="workflow.name"
                  :value="workflow.id"
                >
                  <div class="workflow-option">
                    <div
                      class="workflow-name"
                      v-html="highlightText(workflow.name, workflowSearchQuery)"
                    ></div>
                    <div
                      class="workflow-description"
                      v-html="highlightText(workflow.description, workflowSearchQuery)"
                    ></div>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            <div v-if="selectedWorkflow" class="workflow-info">
              <div class="workflow-info-title">{{ selectedWorkflow.name }}</div>
              <div class="workflow-info-description">{{ selectedWorkflow.description }}</div>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            {{ submitting ? $t('declaration.submitting') : $t('declaration.submit') }}
          </el-button>
          <el-button size="large" @click="handleBack">
            <el-icon><Close /></el-icon>
            {{ $t('common.cancel') }}
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Check, Close, MagicStick } from '@element-plus/icons-vue'
import { DECLARATION_DEPARTMENT_OPTIONS } from '@/utils/constants'
import { validateResearchFieldCount, validateFileSize as validateFileSizeUtil, validateFileType as validateFileTypeUtil } from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'
import brandLogo from '@/assets/images/logo.svg'

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('DeclarationCreate')

const formRef = ref()
const fileInputRef = ref()
const tagInputElementRef = ref()

const form = reactive({
  documentFile: null,
  department: '',
  projectLeader: '',
  documentPublishTime: '',
  projectStartTime: '',
  projectEndTime: '',
  researchDirection: '',
  researchField: [],
  researchTopic: '',
  workflow: ''
})

const rules = reactive({
  department: [{ required: true, message: t('declaration.departmentRequired'), trigger: 'change' }],
  projectLeader: [{ required: true, message: t('declaration.projectLeaderRequired'), trigger: 'blur' }],
  documentPublishTime: [{ required: true, message: t('declaration.documentPublishTimeRequired'), trigger: 'change' }],
  projectStartTime: [{ required: true, message: t('declaration.projectStartTimeRequired'), trigger: 'change' }],
  projectEndTime: [{ required: true, message: t('declaration.projectEndTimeRequired'), trigger: 'change' }],
  researchDirection: [{ required: true, message: t('declaration.researchDirectionRequired'), trigger: 'blur' }],
  researchTopic: [{ required: true, message: t('declaration.researchTopicRequired'), trigger: 'blur' }],
  workflow: [{ required: true, message: t('declaration.workflowRequired'), trigger: 'change' }]
})

const formErrors = reactive({
  researchField: ''
})

const formSuccess = reactive({
  researchField: false
})

const submitting = ref(false)
const tagInput = ref('')
const isDragOver = ref(false)
const uploadStatus = ref('idle')
const analysisResult = ref(null)
const uploadProgress = ref(0)
const uploadTimer = ref(null)

const departmentOptions = DECLARATION_DEPARTMENT_OPTIONS

const workflowOptions = ref([])
const filteredWorkflowOptions = ref([])
const workflowSearchQuery = ref('')
const workflowLoading = ref(false)

const selectedWorkflow = computed(() =>
  workflowOptions.value.find(workflow => workflow.id === form.workflow)
)

const fileMaxSize = 10 * 1024 * 1024
const allowedFileTypes = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png']

const openFileDialog = () => {
  fileInputRef.value?.click()
}

const handleDragOver = () => {
  isDragOver.value = true
}

const handleDragLeave = () => {
  isDragOver.value = false
}

const handleDrop = (event) => {
  isDragOver.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) {
    handleFileUpload(file)
  }
}

const handleFileChange = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    handleFileUpload(file)
  }
}

const handleFileUpload = (file) => {
  if (!validateFileSizeUtil(file, fileMaxSize)) {
    ElMessage.error(t('declaration.fileSizeError'))
    return
  }

  if (!validateFileTypeUtil(file, allowedFileTypes)) {
    ElMessage.error(t('declaration.fileTypeError'))
    return
  }

  if (uploadTimer.value) {
    clearInterval(uploadTimer.value)
  }

  uploadStatus.value = 'uploading'
  uploadProgress.value = 0
  form.documentFile = file

  uploadTimer.value = setInterval(() => {
    if (uploadProgress.value >= 100) {
      clearInterval(uploadTimer.value)
      uploadTimer.value = null
      uploadStatus.value = 'analyzing'
      analyzeDocument(file)
    } else {
      uploadProgress.value += 10
    }
  }, 200)
}

const analyzeDocument = async (file) => {
  try {
    logger.info('Analyzing document', { file: file.name })
    await new Promise(resolve => setTimeout(resolve, 1500))

    const mockResult = {
      researchDirection: t('declaration.sampleDirection'),
      researchTopic: t('declaration.sampleTopic')
    }

    analysisResult.value = mockResult
    form.researchDirection = mockResult.researchDirection
    form.researchTopic = mockResult.researchTopic
    uploadStatus.value = 'success'
    ElMessage.success(t('declaration.analysisSuccess'))
  } catch (error) {
    logger.error('Document analysis failed', error)
    uploadStatus.value = 'error'
    ElMessage.error(t('declaration.analysisError'))
  }
}

const clearUploadedFile = () => {
  form.documentFile = null
  analysisResult.value = null
  uploadStatus.value = 'idle'
  uploadProgress.value = 0
  if (uploadTimer.value) {
    clearInterval(uploadTimer.value)
    uploadTimer.value = null
  }
}

const addTag = () => {
  const value = tagInput.value.trim()
  if (!value) return
  if (form.researchField.includes(value)) {
    ElMessage.warning(t('declaration.researchFieldDuplicate'))
    return
  }

  if (!validateResearchFieldCount([...form.researchField, value])) {
    ElMessage.warning(t('declaration.researchFieldCountExceeded'))
    return
  }

  form.researchField.push(value)
  tagInput.value = ''
}

const removeTag = (index) => {
  form.researchField.splice(index, 1)
}

const handleBackspace = () => {
  if (!tagInput.value && form.researchField.length) {
    form.researchField.pop()
  }
}

const handleTagInput = () => {
  // 保留空实现实现输入响应
}

const updateResearchFieldState = () => {
  if (form.researchField.length === 0) {
    formErrors.researchField = t('declaration.researchFieldRequired')
    formSuccess.researchField = false
  } else {
    formErrors.researchField = ''
    formSuccess.researchField = true
  }
}

watch(() => form.researchField.length, updateResearchFieldState, { immediate: true })

const filterWorkflow = (query) => {
  workflowSearchQuery.value = query
  if (!query) {
    filteredWorkflowOptions.value = [...workflowOptions.value]
    return
  }
  const lowerQuery = query.toLowerCase()
  filteredWorkflowOptions.value = workflowOptions.value.filter(workflow =>
    workflow.name.toLowerCase().includes(lowerQuery) ||
    workflow.description.toLowerCase().includes(lowerQuery)
  )
}

const highlightText = (text, query) => {
  if (!query) return text
  const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escapedQuery})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

const loadWorkflowOptions = async () => {
  try {
    workflowLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 500))
    workflowOptions.value = [
      { id: 'workflow_001', name: t('declaration.workflowOption1'), description: t('declaration.workflowDesc1') },
      { id: 'workflow_002', name: t('declaration.workflowOption2'), description: t('declaration.workflowDesc2') },
      { id: 'workflow_003', name: t('declaration.workflowOption3'), description: t('declaration.workflowDesc3') },
      { id: 'workflow_004', name: t('declaration.workflowOption4'), description: t('declaration.workflowDesc4') }
    ]
    filteredWorkflowOptions.value = [...workflowOptions.value]
  } catch (error) {
    logger.error('Load workflow options failed', error)
    ElMessage.error(t('declaration.workflowLoadError'))
  } finally {
    workflowLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    updateResearchFieldState()
    if (form.researchField.length === 0) {
      throw new Error('fields-missing')
    }
    await formRef.value.validate()
  } catch (error) {
    if (error?.message === 'fields-missing') {
      ElMessage.error(t('declaration.researchFieldRequired'))
    } else {
      ElMessage.error(t('declaration.formIncomplete'))
    }
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
    await new Promise(resolve => setTimeout(resolve, 1500))
    ElMessage.success(t('declaration.submitSuccess'))
    clearDraft()
    router.push('/declaration/list')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('declaration.submitError'))
    }
  } finally {
    submitting.value = false
  }
}

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
    .catch(() => {})
}

const clearDraft = () => {
  form.documentFile = null
  form.department = ''
  form.projectLeader = ''
  form.documentPublishTime = ''
  form.projectStartTime = ''
  form.projectEndTime = ''
  form.researchDirection = ''
  form.researchField = []
  form.researchTopic = ''
  form.workflow = ''
  uploadStatus.value = 'idle'
  uploadProgress.value = 0
  analysisResult.value = null
  tagInput.value = ''
  updateResearchFieldState()
}

onMounted(() => {
  loadWorkflowOptions()
})

onUnmounted(() => {
  if (uploadTimer.value) {
    clearInterval(uploadTimer.value)
  }
})
</script>

<style scoped lang="scss">
.new-declaration-container {
  padding: 20px;
  background: #f7f9fc;
  min-height: calc(100vh - 56px);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.brand {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.brand-logo {
  width: 46px;
  height: 46px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e3a8a;
  margin: 0;
}

.form-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  max-width: 900px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 32px;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  padding: 24px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}

.group-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e3a8a;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.required {
  color: #dc2626;
}

.form-row {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;

  &.single {
    flex-direction: column;
  }
}

.form-item {
  flex: 1;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

.upload-area {
  border: 2px dashed #cbd5f5;
  border-radius: 12px;
  padding: 32px;
  text-align: center;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover,
  &.dragover {
    border-color: #1e3a8a;
    background: #eff6ff;
  }
}

.hidden-input {
  display: none;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 16px;
  color: #111827;
}

.upload-hint {
  font-size: 14px;
  color: #6b7280;
}

.file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f1f5f9;
  border-radius: 8px;
  margin-top: 12px;
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
  font-size: 14px;

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
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 16px;
  background: #f0fdf4;
}

.analysis-title {
  font-weight: 600;
  color: #166534;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.analysis-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
}

.auto-filled-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #16a34a;
}

.tag-input-container {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  min-height: 48px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;

  &:focus-within {
    border-color: #1e3a8a;
    box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.15);
  }
}

.tag-item {
  background: #e0e7ff;
  color: #1e3a8a;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tag-remove {
  cursor: pointer;
  font-weight: 700;
}

.tag-input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 160px;
}

.tag-helper {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
}

.error-message {
  margin-top: 4px;
  font-size: 12px;
  color: #dc2626;
}

.success-message {
  margin-top: 4px;
  font-size: 12px;
  color: #16a34a;
}

.workflow-option {
  padding: 4px 0;
}

.workflow-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.workflow-description {
  font-size: 12px;
  color: #6b7280;
}

.highlight {
  background: #fef3c7;
  color: #92400e;
  border-radius: 2px;
  padding: 0 2px;
}

.workflow-info {
  margin-top: 12px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.workflow-info-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e3a8a;
}

.workflow-info-description {
  font-size: 14px;
  color: #475569;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .form-card {
    padding: 16px;
  }

  .form-row {
    flex-direction: column;
  }

  .form-group {
    padding: 16px;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
