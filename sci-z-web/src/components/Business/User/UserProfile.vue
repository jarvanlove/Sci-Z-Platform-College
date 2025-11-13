<!--
/**
 * @description 用户中心 - 个人信息业务组件
 * 负责展示和编辑用户个人信息、头像上传等功能
 */
-->
<template>
  <div class="profile-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ t('user.profile.title') }}</h1>
        <p class="page-subtitle">
          {{ t('user.profile.contactSection') }} · {{ t('user.profile.accountSection') }}
        </p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadProfile(true)">
        <el-icon><Refresh /></el-icon>
        {{ t('common.refresh') || 'Refresh' }}
      </el-button>
    </div>

    <div class="profile-content">
      <!-- 基础信息 -->
      <BaseCard class="info-card">
        <template #header>
          <div class="card-title">
            {{ t('user.profile.baseSection') }}
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="120px"
          class="profile-form"
          :disabled="loading"
        >
          <!-- 用户名（只读） -->
          <el-form-item :label="t('user.profile.username')" prop="username">
            <el-input v-model="formData.username" readonly class="readonly-input">
              <template #suffix>
                <el-tooltip :content="t('user.profile.usernameTip')" placement="top">
                  <el-icon><InfoFilled /></el-icon>
                </el-tooltip>
              </template>
            </el-input>
          </el-form-item>

          <!-- 真实姓名 -->
          <el-form-item :label="t('user.profile.realName')" prop="realName">
            <el-input
              v-model="formData.realName"
              :placeholder="t('user.profile.realNamePlaceholder')"
              clearable
            />
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item :label="t('user.profile.email')" prop="email">
            <el-input
              v-model="formData.email"
              :placeholder="t('user.profile.emailPlaceholder')"
              clearable
            >
              <template #append>
                <el-button
                  :disabled="!emailReady"
                  :loading="verification.email.loading"
                  @click="handleSendEmail"
                >
                  {{ t('user.profile.emailVerify') }}
                </el-button>
              </template>
            </el-input>
            <div class="field-status">
              <el-icon
                v-if="verification.email.loading"
                class="is-loading"
              ><Loading /></el-icon>
              <span v-if="verification.email.loading">
                {{ t('user.profile.emailValidating') }}
              </span>
              <span v-else-if="verification.email.success" class="status-success">
                {{ t('user.profile.emailSent') }}
              </span>
              <span v-else-if="verification.email.error" class="status-error">
                {{ verification.email.message }}
              </span>
            </div>
          </el-form-item>

          <!-- 手机号 -->
          <el-form-item :label="t('user.profile.phone')" prop="phone">
            <el-input
              v-model="formData.phone"
              :placeholder="t('user.profile.phonePlaceholder')"
              clearable
              maxlength="11"
            >
              <template #append>
                <el-button
                  :disabled="!phoneReady"
                  :loading="verification.phone.loading"
                  @click="handleSendSms"
                >
                  {{ t('user.profile.phoneVerify') }}
                </el-button>
              </template>
            </el-input>
            <div class="field-status">
              <el-icon
                v-if="verification.phone.loading"
                class="is-loading"
              ><Loading /></el-icon>
              <span v-if="verification.phone.loading">
                {{ t('user.profile.phoneValidating') }}
              </span>
              <span v-else-if="verification.phone.success" class="status-success">
                {{ t('user.profile.phoneSent') }}
              </span>
              <span v-else-if="verification.phone.error" class="status-error">
                {{ verification.phone.message }}
              </span>
            </div>
          </el-form-item>

          <!-- 部门 -->
          <el-form-item :label="departmentLabel" prop="department">
            <el-select
              v-model="formData.department"
              :placeholder="departmentPlaceholder"
              filterable
              clearable
            >
              <el-option
                v-for="item in departmentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <!-- 职称 -->
          <el-form-item :label="t('user.profile.titleLabel')" prop="title">
            <el-select
              v-model="formData.title"
              :placeholder="t('user.profile.titlePlaceholder')"
              filterable
              clearable
            >
              <el-option
                v-for="item in titleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <!-- 表单操作 -->
          <div class="form-actions">
            <BaseButton
              type="primary"
              size="large"
              :loading="saving"
              @click="handleSave"
            >
              {{ t('user.profile.save') }}
            </BaseButton>
            <BaseButton size="large" @click="handleReset">
              {{ t('user.profile.reset') }}
            </BaseButton>
          </div>
        </el-form>
      </BaseCard>

      <!-- 头像 -->
      <BaseCard class="avatar-card" :hoverable="false">
        <template #header>
          <div class="card-title">
            {{ t('user.profile.avatarSectionTitle') }}
          </div>
        </template>

        <div class="avatar-wrapper">
          <div class="avatar-preview" @click="openFileDialog">
            <img :src="avatarPreview || defaultAvatar" alt="avatar" />
            <div class="avatar-overlay">
              <el-icon size="24"><Upload /></el-icon>
              <span>{{ t('user.profile.clickUpload') }}</span>
            </div>
          </div>

          <input
            ref="fileInputRef"
            type="file"
            accept="image/png,image/jpeg,image/gif"
            class="file-input"
            @change="handleSelectAvatar"
          />

          <p class="avatar-tip">{{ t('user.profile.supportedFormats') }}</p>
          <p class="avatar-tip">{{ t('user.profile.maxSize') }}</p>
          <p class="avatar-tip">{{ t('user.profile.recommendedSize') }}</p>

          <BaseButton
            type="primary"
            :loading="uploading"
            @click="openFileDialog"
          >
            <el-icon><Upload /></el-icon>
            {{ t('user.profile.selectImage') }}
          </BaseButton>
        </div>
      </BaseCard>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Upload, Loading, Refresh } from '@element-plus/icons-vue'
import { BaseButton, BaseCard } from '@/components/Common'
import { getUserInfo, updateUserInfo, getProfileFields, uploadAvatar } from '@/api/User'
import { previewFile } from '@/api/File'
import { useAuthStore } from '@/store/modules/auth'
import { useIndustryStore } from '@/store/modules/industry'
import { validateChineseName, validateEmail, validatePhone } from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'
import { setUserInfo } from '@/utils/auth'
import {
  ATTACHMENT_CATEGORY,
  ATTACHMENT_RELATION,
  IMAGE_FILE_EXTENSIONS,
  validateFileType,
  validateFileSize,
  DEFAULT_AVATAR_MAX_SIZE_MB
} from '@/constants/attachment'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

const { t } = useI18n()
const logger = createLogger('UserProfileComponent')

const authStore = useAuthStore()
const industryStore = useIndustryStore()

logger.info('UserProfile component setup initialized')

const formRef = ref()
const fileInputRef = ref()
const defaultAvatar = ref('https://dummyimage.com/160x160/f3f4f6/8c9eff&text=Avatar')

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)

const formData = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  title: '',
  avatar: ''
})

const initialSnapshot = ref({})

const verification = reactive({
  email: { loading: false, success: false, error: false, message: '' },
  phone: { loading: false, success: false, error: false, message: '' }
})

const departmentOptions = ref([])

const avatarPreview = ref('')

const departmentLabel = computed(() => {
  const key = industryStore.departmentLabelKey
  return key ? t(key) : t('user.profile.department')
})

const departmentPlaceholder = computed(() => {
  const key = industryStore.departmentPlaceholderKey
  return key ? t(key) : t('user.profile.departmentPlaceholder')
})

const titleOptions = computed(() => [
  { label: t('user.profile.titleOptionProfessor'), value: 'professor' },
  { label: t('user.profile.titleOptionAssociateProfessor'), value: 'associate_professor' },
  { label: t('user.profile.titleOptionLecturer'), value: 'lecturer' },
  { label: t('user.profile.titleOptionResearcher'), value: 'researcher' },
  { label: t('user.profile.titleOptionAssistantResearcher'), value: 'assistant_researcher' }
])

const emailReady = computed(() => validateEmail(formData.email) && !verification.email.loading)
const phoneReady = computed(() => validatePhone(formData.phone) && !verification.phone.loading)

const rules = reactive({
  realName: [
    { required: true, message: t('user.profile.realNameRequired'), trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!validateChineseName(value)) {
          callback(new Error(t('user.profile.realNameFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: t('user.profile.emailRequired'), trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!validateEmail(value)) {
          callback(new Error(t('user.profile.emailFormat')))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change']
    }
  ],
  phone: [
    { required: true, message: t('user.profile.phoneRequired'), trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!validatePhone(value)) {
          callback(new Error(t('user.profile.phoneFormat')))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change']
    }
  ],
  department: [
    { required: true, message: t('user.profile.departmentRequired'), trigger: 'change' }
  ]
})

const resetVerification = (type) => {
  verification[type].loading = false
  verification[type].success = false
  verification[type].error = false
  verification[type].message = ''
}

const loadDepartments = async () => {
  try {
    const list = await industryStore.fetchDepartmentLabels()
    if (Array.isArray(list) && list.length) {
      departmentOptions.value = list.map((item) => ({
        label: item.label || item.name || item.title || item.value,
        value: item.value || item.code || item.id || item.label
      }))
    }
  } catch (error) {
    logger.warn('fetch departments failed', { error: error.message })
  }
}

const loadProfile = async (force = false) => {
  logger.info('开始加载个人信息', { force })
  try {
    if (loading.value && !force) return
    loading.value = true
    await industryStore.fetchIndustryConfig(force)
    await loadDepartments()
    const response = await getUserInfo()
    logger.info('获取个人信息接口返回', response)
    const payload = response?.data?.data || response?.data || response || {}
    const data = payload.userInfo || payload

    formData.username = data.username || data.account || ''
    formData.realName = data.realName || data.name || ''
    formData.email = data.email || ''
    formData.phone = data.phone || data.mobile || ''
    formData.department = data.department || data.departmentCode || ''
    formData.title = data.title || data.position || ''
    formData.avatar = data.avatar || ''

    avatarPreview.value = formData.avatar
    
    // 🔥 关键修复：同步更新 authStore 中的用户头像，确保 Header 中的头像也能正常显示
    if (formData.avatar) {
      // 确保 userInfo 存在
      if (!authStore.userInfo) {
        authStore.userInfo = {}
      }
      // 更新头像
      authStore.userInfo.avatar = formData.avatar
      // 同时更新本地存储的用户信息
      setUserInfo(authStore.userInfo)
      logger.debug('loadProfile 已同步头像到 authStore', { avatar: formData.avatar })
    }
    
    initialSnapshot.value = { ...formData }

    resetVerification('email')
    resetVerification('phone')
  } catch (error) {
    logger.error('load profile failed', { error: error.message })
    ElMessage.error(t('user.profile.loadError'))
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  logger.info('提交个人信息更新')
  try {
    await formRef.value.validate()

    await ElMessageBox.confirm(
      t('user.profile.saveConfirmMessage'),
      t('user.profile.saveConfirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    saving.value = true
    const payload = {
      realName: formData.realName,
      email: formData.email,
      phone: formData.phone,
      department: formData.department,
      title: formData.title,
      industryCode: industryStore.industryCode,
      avatar: formData.avatar
    }
    await updateUserInfo(payload)
    await authStore.getUserInfo(true)
    
    // 确保头像同步更新到 authStore
    if (authStore.userInfo && formData.avatar) {
      authStore.userInfo.avatar = formData.avatar
      setUserInfo(authStore.userInfo)
    }
    
    initialSnapshot.value = { ...formData }
    ElMessage.success(t('user.profile.saveSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('save profile failed', { error: error.message })
      ElMessage.error(t('user.profile.saveError'))
    }
  } finally {
    saving.value = false
  }
}

const handleReset = async () => {
  logger.info('重置个人信息表单')
  try {
    await ElMessageBox.confirm(
      t('user.profile.resetConfirmMessage'),
      t('user.profile.resetConfirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    Object.assign(formData, initialSnapshot.value)
    formRef.value?.clearValidate()
    resetVerification('email')
    resetVerification('phone')
    ElMessage.info(t('user.profile.resetSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      logger.warn('reset cancelled', { error })
    }
  }
}

const openFileDialog = () => {
  logger.info('触发头像选择对话框')
  if (!fileInputRef.value) return
  fileInputRef.value.value = ''
  fileInputRef.value.click()
}

const AVATAR_MAX_SIZE_MB = DEFAULT_AVATAR_MAX_SIZE_MB
const previewVisible = ref(false)
const previewObjectUrl = ref('')

// 头像上传状态
const uploading = ref(false)

const cropperVisible = ref(false)
const cropperImageSrc = ref('')
const cropperContainerRef = ref(null)
let cropperInstance = null
const pendingAvatarFile = ref(null)

const destroyCropper = () => {
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
}

const resetPreviewUrl = () => {
  if (previewObjectUrl.value) {
    URL.revokeObjectURL(previewObjectUrl.value)
    previewObjectUrl.value = ''
  }
}

const initCropper = () => {
  destroyCropper()
  if (cropperContainerRef.value && cropperImageSrc.value) {
    const imageElement = cropperContainerRef.value.querySelector('img')
    if (!imageElement) return
    cropperInstance = new Cropper(imageElement, {
      viewMode: 1,
      aspectRatio: 1,
      dragMode: 'move',
      background: false,
      movable: true,
      scalable: false,
      zoomable: true,
      autoCropArea: 1
    })
  }
}

const openCropperDialog = async (file) => {
  const reader = new FileReader()
  reader.onload = async () => {
    cropperImageSrc.value = reader.result
    pendingAvatarFile.value = file
    cropperVisible.value = true
    await nextTick()
    initCropper()
  }
  reader.readAsDataURL(file)
}

const uploadAvatarFile = async (file) => {
  try {
    // 1. 文件校验
    const sizeValidation = validateFileSize(file, AVATAR_MAX_SIZE_MB)
    if (!sizeValidation.passed) {
      ElMessage.error(sizeValidation.reason)
      return
    }
    const typeValidation = validateFileType(file, IMAGE_FILE_EXTENSIONS)
    if (!typeValidation.passed) {
      ElMessage.error(typeValidation.reason || t('user.profile.invalidFormat'))
      return
    }

    // 2. 计算 MD5
    uploading.value = true
    const SparkMD5 = (await import('spark-md5')).default
    const computeFileMd5 = (file) =>
      new Promise((resolve, reject) => {
        const chunkSize = 2 * 1024 * 1024
        const chunks = Math.ceil(file.size / chunkSize)
        let currentChunk = 0
        const spark = new SparkMD5.ArrayBuffer()
        const reader = new FileReader()

        reader.onload = (event) => {
          const result = event?.target?.result
          if (result) {
            spark.append(result)
          }
          currentChunk += 1
          if (currentChunk < chunks) {
            loadNextChunk()
          } else {
            resolve(spark.end())
          }
        }

        reader.onerror = () => reject(new Error('md5 compute failed'))

        const loadNextChunk = () => {
          const start = currentChunk * chunkSize
          const end = Math.min(start + chunkSize, file.size)
          reader.readAsArrayBuffer(file.slice(start, end))
        }

        loadNextChunk()
      })

    let md5 = ''
    let reused = false
    let fileInfo = null

    try {
      md5 = await computeFileMd5(file)
      // 3. 检查重复（调用 api/file/check-duplicate）
      const { checkFileDuplicate } = await import('@/api/File')
      const duplicateResponse = await checkFileDuplicate({
        md5,
        fileSize: file.size,
        originalName: file.name
      })
      const duplicatePayload = duplicateResponse?.data ?? duplicateResponse
      const duplicateData = duplicatePayload?.data ?? duplicatePayload
      if (duplicateData?.exists && duplicateData.fileInfo) {
        fileInfo = duplicateData.fileInfo
        reused = true
        ElMessage.success('文件已存在，已复用历史上传记录')
      }
    } catch (error) {
      // MD5 计算或去重检查失败，继续上传
      logger.debug('MD5 计算或去重检查失败，继续上传', { error: error.message })
    }

    // 4. 如果没有复用，调用 api/user/avatar 上传
    if (!fileInfo) {
      const form = new FormData()
      form.append('file', file)
      if (md5) {
        form.append('md5', md5)
      }
      
      const response = await uploadAvatar(form)
      const payload = response?.data ?? response ?? {}
      fileInfo = payload
      reused = false
    }

    // 5. 处理上传结果
    const url = fileInfo?.previewUrl || fileInfo?.fileUrl || fileInfo?.avatar || formData.avatar
    if (!url) {
      ElMessage.error(t('user.profile.uploadError'))
      return
    }

    formData.avatar = url
    avatarPreview.value = url
    formData.avatarFileId = fileInfo.id || fileInfo.fileId || fileInfo.attachmentId || null
    pendingAvatarFile.value = null
    
    // 🔥 关键修复：上传头像后立即同步到 authStore，确保 Header 中的头像也能显示
    if (authStore.userInfo) {
      authStore.userInfo.avatar = url
      authStore.userInfo.avatarFileId = formData.avatarFileId
      setUserInfo(authStore.userInfo)
      logger.debug('头像上传后已同步到 authStore', { avatar: url, avatarFileId: formData.avatarFileId })
    } else {
      // 如果 userInfo 不存在，先初始化
      authStore.userInfo = { avatar: url, avatarFileId: formData.avatarFileId }
      setUserInfo(authStore.userInfo)
      logger.debug('userInfo 不存在，已初始化并同步头像', { avatar: url, avatarFileId: formData.avatarFileId })
    }
    
    ElMessage.success(reused ? t('user.profile.avatarReused') : t('user.profile.uploadSuccess'))
    ElMessage.info(t('user.profile.avatarRememberSave'))
  } catch (error) {
    logger.error('upload avatar failed', { error: error.message })
    ElMessage.error(error.response?.data?.message || t('user.profile.uploadError'))
  } finally {
    uploading.value = false
  }
}

const handleCropConfirm = async () => {
  if (!cropperInstance || !pendingAvatarFile.value) return
  try {
    const canvas = cropperInstance.getCroppedCanvas({
      width: 200,
      height: 200,
      imageSmoothingEnabled: true,
      imageSmoothingQuality: 'high'
    })
    const blob = await new Promise((resolve) => {
      canvas.toBlob(resolve, 'image/jpeg', 0.9)
    })
    const file = new File([blob], pendingAvatarFile.value.name, {
      type: 'image/jpeg',
      lastModified: Date.now()
    })
    cropperVisible.value = false
    destroyCropper()
    await uploadAvatarFile(file)
  } catch (error) {
    logger.error('crop and upload failed', { error: error.message })
    ElMessage.error(t('user.profile.uploadError'))
  }
}

const handleSelectAvatar = async (event) => {
  const file = event.target?.files?.[0]
  logger.info('选择头像文件', { hasFile: !!file })
  if (!file) return

  // 使用新的上传流程
  await uploadAvatarFile(file)
}

const simulateVerify = async (type, successMessage, errorMessage) => {
  logger.info('执行模拟验证', { type })
  resetVerification(type)
  verification[type].loading = true
  try {
    // TODO: 替换为真实接口验证逻辑
    await new Promise((resolve) => setTimeout(resolve, 800))
    verification[type].success = true
    verification[type].message = successMessage
    ElMessage.success(successMessage)
  } catch (error) {
    verification[type].error = true
    verification[type].message = errorMessage
    ElMessage.error(errorMessage)
  } finally {
    verification[type].loading = false
  }
}

const handleSendEmail = () => {
  simulateVerify('email', t('user.profile.emailSent'), t('user.profile.emailSendError'))
}

const handleSendSms = () => {
  simulateVerify('phone', t('user.profile.phoneSent'), t('user.profile.phoneSendError'))
}

onMounted(() => {
  logger.info('UserProfile component mounted，开始加载数据')
  loadProfile()
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 24px;
  background: var(--bg);
  min-height: calc(100vh - 56px);

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--color-primary);
    margin: 0;
  }

  .page-subtitle {
    margin: 4px 0 0;
    color: var(--text-3);
    font-size: 14px;
  }
}

.profile-content {
  display: grid;
  grid-template-columns: 3fr 1fr;
  gap: 24px;
}

.info-card,
.avatar-card {
  background: var(--surface);
  border-radius: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-1);
}

.profile-form {
  max-width: 640px;
}

.readonly-input {
  background-color: var(--hover) !important;
  cursor: not-allowed;
}

.field-status {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 20px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-3);

  .status-success {
    color: var(--el-color-success);
  }

  .status-error {
    color: var(--el-color-error);
  }
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
}

.avatar-preview {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(59, 130, 246, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: rgba(30, 64, 175, 0.65);
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-tip {
  margin: 0;
  font-size: 12px;
  color: var(--text-3);
}

.file-input {
  display: none;
}

@media (max-width: 1024px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .profile-form {
    :deep(.el-form-item) {
      flex-direction: column;
      align-items: flex-start;
    }

    :deep(.el-form-item__label) {
      padding-bottom: 4px;
    }
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

