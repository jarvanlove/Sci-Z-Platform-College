<!--
/**
 * @description 新建申报业务组件
 * 包含红头文件上传、AI分析、表单填写、工作流选择、申报提交等功能
 */
-->
<template>
    <div class="declaration-create-container">
      <div class="page-header">
        <BackButton @click="handleBack" />
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
                :accept="redHeaderAccept"
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
                  <strong>{{ $t('declaration.fields') }}：</strong>
                  <template v-if="analysisResult.researchField">
                    {{ typeof analysisResult.researchField === 'string' ? analysisResult.researchField : analysisResult.researchField.join('、') }}
                  </template>
                  <template v-else>（空）</template>
                </p>
                <p>
                  <strong>{{ $t('declaration.direction') }}：</strong>
                  {{ analysisResult.researchDirection || '（空）' }}
                </p>
                <p>
                  <strong>{{ $t('declaration.topic') }}：</strong>
                  {{ analysisResult.researchTopic || '（空）' }}
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
                v-if="analysisResult && uploadStatus === 'success' && analysisResult.researchDirection !== undefined"
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
                v-if="analysisResult && uploadStatus === 'success' && analysisResult.researchTopic !== undefined"
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
              <div
                v-if="analysisResult && analysisResult.researchField !== undefined && uploadStatus === 'success'"
                class="auto-filled-hint"
              >
                ✨ {{ $t('declaration.autoFilled') }}
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
              <WorkflowSelect
                v-model="form.workflow"
                :options="workflowOptions"
                :placeholder="$t('declaration.workflowPlaceholder')"
                :loading="workflowLoading"
                :no-match-text="$t('declaration.workflowNoMatch')"
                :no-data-text="$t('declaration.workflowNoData')"
              />
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
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    Loading,
    MagicStick,
    UploadFilled,
    Document
  } from '@element-plus/icons-vue'
  import { BaseCard, BaseButton, BaseDatePicker, BackButton } from '@/components/Common'
  import { WorkflowSelect } from '@/components/Business/Form'
  import { DECLARATION_DEPARTMENT_OPTIONS } from '@/utils/constants'
  import { createLogger } from '@/utils/simpleLogger'
  import { uploadRedHeaderFile, createDeclaration } from '@/api/Declaration/declaration'
  import { getWorkflows } from '@/api/User/user'
  import { validateFileSize, validateFileType } from '@/constants/attachment'
  
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
  
  // 部门选项
  const departmentOptions = DECLARATION_DEPARTMENT_OPTIONS
  
  // 工作流选项
  const workflowOptions = ref([])
  const workflowLoading = ref(false)
  
  // 标签输入
  const tagInput = ref('')
  const formErrors = reactive({
    researchField: ''
  })
  
  // 触发文件选择
  const triggerFileInput = () => {
    fileInputRef.value?.click()
  }
  
  // 红头文件允许的文件类型（扩展名）
  const RED_HEADER_ALLOWED_EXTENSIONS = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png']
  // 红头文件大小限制（MB）
  const RED_HEADER_MAX_SIZE_MB = 10
  // 文件选择器的 accept 属性值
  const redHeaderAccept = computed(() => {
    return RED_HEADER_ALLOWED_EXTENSIONS.map(ext => `.${ext}`).join(',')
  })

  // 文件选择处理
  const handleFileChange = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return
  
    // 使用封装好的文件大小校验函数
    const sizeValidation = validateFileSize(file, RED_HEADER_MAX_SIZE_MB)
    if (!sizeValidation.passed) {
      ElMessage.error(sizeValidation.reason || t('declaration.uploadError'))
      return
    }
  
    // 使用封装好的文件类型校验函数
    const typeValidation = validateFileType(file, RED_HEADER_ALLOWED_EXTENSIONS)
    if (!typeValidation.passed) {
      ElMessage.error(typeValidation.reason || t('declaration.uploadError'))
      return
    }
  
    try {
      // 接口同时处理上传和分析，直接显示分析状态
      uploadStatus.value = 'analyzing'
      uploadProgress.value = 0
  
      logger.info('Starting file upload and analysis', { fileName: file.name, fileSize: file.size })
  
      // 调用红头文件上传接口（上传和分析合并在一个接口中，只需要传 file 对象）
      const response = await uploadRedHeaderFile(file)
      
      // 处理响应数据
      const analysisData = response?.data || {}
      
      logger.info('Received analysis data from API', { 
        response, 
        analysisData,
        hasResearchField: 'researchField' in analysisData,
        hasResearchDirection: 'researchDirection' in analysisData,
        hasResearchTopic: 'researchTopic' in analysisData
      })
      
      // 保存文件信息
      uploadedFile.value = file
      form.documentFile = file
      
      // 处理分析结果
      // 无论字段是否为空，只要接口返回了数据就处理
      const analysisResultData = {
        researchDirection: analysisData.researchDirection ?? '',
        researchTopic: analysisData.researchTopic ?? '',
        researchField: analysisData.researchField ?? ''
      }
      
      logger.info('Processed analysis result data', { analysisResultData })
      
      analysisResult.value = analysisResultData
      
      // 自动填充表单字段
      // 研究方向：无论是否为空都填充（接口返回空字符串也填充，表示AI分析结果是空的）
      if (analysisData.researchDirection !== undefined) {
        form.researchDirection = analysisData.researchDirection ? String(analysisData.researchDirection).trim() : ''
        logger.info('Auto-filled researchDirection', { value: form.researchDirection })
      }
      
      // 研究课题：无论是否为空都填充
      if (analysisData.researchTopic !== undefined) {
        form.researchTopic = analysisData.researchTopic ? String(analysisData.researchTopic).trim() : ''
        logger.info('Auto-filled researchTopic', { value: form.researchTopic })
      }
      
      // 研究领域：在接口返回中是字符串（如："人工智能、具身智能、工业软件"），需要转换为数组
      // 无论接口是否返回了 researchField 字段，都进行处理（即使为空也要清空表单，表示AI分析结果是空的）
      if (analysisData.researchField !== undefined) {
        if (analysisData.researchField && String(analysisData.researchField).trim()) {
          // 如果返回的是字符串，尝试按分隔符拆分（支持顿号、逗号、中英文逗号）
          const fieldValue = String(analysisData.researchField).trim()
          const fields = typeof analysisData.researchField === 'string'
            ? fieldValue.split(/[、，,]/).map(f => f.trim()).filter(f => f)
            : Array.isArray(analysisData.researchField)
            ? analysisData.researchField.map(f => typeof f === 'string' ? f.trim() : String(f)).filter(f => f)
            : []
          
          // 清空现有字段，替换为接口返回的字段（避免重复）
          form.researchField = fields.slice(0, 10) // 最多保留10个字段
          logger.info('Auto-filled researchField', { fields, count: form.researchField.length })
        } else {
          // 如果接口返回了 researchField 但值为空，清空表单字段
          form.researchField = []
          logger.info('Cleared researchField (empty value from API)')
        }
      }
      
      uploadStatus.value = 'success'
      ElMessage.success(t('declaration.analysisSuccess'))
      logger.info('File upload and analysis completed successfully', { analysisData })
    } catch (error) {
      logger.error('File upload or analysis failed', error)
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
  
  // 加载工作流选项
  const loadWorkflowOptions = async () => {
    try {
      workflowLoading.value = true
      logger.info('Loading workflow options')

      // 调用工作流列表接口
      const response = await getWorkflows()
      
      // 处理响应数据：支持多种响应格式
      // 标准响应格式：{ code, message, data: [...] } 或直接是数组
      let workflowsData = []
      
      // 检查 response.data 是否为数组（标准格式：{ data: [...] }）
      if (Array.isArray(response?.data)) {
        workflowsData = response.data
      } 
      // 检查 response.data.data 是否为数组（嵌套格式：{ data: { data: [...] } }）
      else if (Array.isArray(response?.data?.data)) {
        workflowsData = response.data.data
      }
      // 如果 response 本身就是数组（直接返回数组）
      else if (Array.isArray(response)) {
        workflowsData = response
      }
      // 如果都没有，尝试从 data 字段获取
      else {
        workflowsData = response?.data || []
      }
      
      logger.info('Raw workflows data received', { 
        rawData: workflowsData, 
        count: workflowsData.length,
        responseType: typeof response,
        isArray: Array.isArray(response),
        hasData: !!response?.data,
        isDataArray: Array.isArray(response?.data)
      })
      
      // 转换数据格式：将后端返回的格式转换为组件需要的格式
      // 后端格式：{ id, userId, keyType, resourceId, keyName, description }
      // 组件格式：{ id, name, description }
      // 注意：后端已经过滤了 keyType === 'workflow' 的工作流，前端不需要再过滤
      workflowOptions.value = workflowsData.map(workflow => ({
        id: workflow.resourceId, // 使用 resourceId 作为工作流ID
        name: workflow.keyName || '',
        description: workflow.description || ''
      }))
      
      logger.info('Workflow options loaded successfully', { 
        total: workflowsData.length,
        loaded: workflowOptions.value.length,
        options: workflowOptions.value
      })
    } catch (error) {
      logger.error('Failed to load workflow options', error)
      ElMessage.error(t('declaration.workflowLoadError'))
      // 加载失败时设置为空数组，避免组件报错
      workflowOptions.value = []
    } finally {
      workflowLoading.value = false
    }
  }
  
  // 提交申报
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

      logger.info('Submitting declaration (calling createDeclaration API)', { form })

      // 构建提交数据
      const submitData = {
        department: form.department,
        projectLeader: form.projectLeader,
        documentPublishTime: form.documentPublishTime,
        projectStartTime: form.projectStartTime,
        projectEndTime: form.projectEndTime,
        researchFields: form.researchField, // 数组格式
        researchDirection: form.researchDirection,
        researchTopic: form.researchTopic,
        workflowId: form.workflow // workflowId 对应表单中的 workflow 字段
      }

      // 调用创建申报接口
      const response = await createDeclaration(submitData)
      
      // 处理响应数据，获取申报ID
      const declarationId = response?.data
      
      logger.info('Declaration created', { 
        response, 
        declarationId 
      })
  
      ElMessage.success(t('declaration.submitSuccess'))
  
      // 跳转到申报详情页面
      if (declarationId) {
        logger.info('Redirecting to declaration detail', { id: declarationId })
        router.push(`/declaration/detail/${declarationId}`)
      } else {
        // 如果没有返回ID，记录警告并跳转到列表页
        logger.warn('Declaration ID not found in response, redirecting to list', { response })
        ElMessage.warning(t('declaration.idNotFound') || '未获取到申报ID，已跳转到列表页')
        router.push('/declaration/list')
      }
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
    white-space: nowrap !important;
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