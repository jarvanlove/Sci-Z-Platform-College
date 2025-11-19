const sizeValidation = validateFileSize(file, AVATAR_MAX_SIZE_MB)
if (!sizeValidation.passed) {
  ElMessage.error(sizeValidation.reason)
  return
}
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
    </div>
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
          <BaseButton type="primary" size="large" :loading="saving" @click="handleSave">
            {{ t('user.profile.save') }}
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
        <div class="avatar-preview" @click="handlePreviewAvatar">
          <img :src="avatarPreview || defaultAvatar" alt="avatar" />
          <div class="avatar-overlay">
            <span>{{ t('user.profile.clickPreview') }}</span>
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

<el-dialog
  v-model="cropperVisible"
  width="480px"
  :close-on-click-modal="false"
  :destroy-on-close="true"
  class="avatar-crop-dialog"
  :title="t('user.profile.avatarCropTitle')"
>
  <div class="avatar-cropper">
    <div class="avatar-cropper__preview" ref="cropperContainerRef">
      <img v-if="cropperImageSrc" :src="cropperImageSrc" alt="avatar preview" />
    </div>
    <div class="avatar-cropper__controls">
      <el-button :icon="ZoomOut" circle @click="handleCropZoom(-0.1)" />
      <el-button :icon="ZoomIn" circle @click="handleCropZoom(0.1)" />
    </div>
    <p class="avatar-cropper__tips">{{ t('user.profile.avatarCropTips') }}</p>
  </div>
  <template #footer>
    <div class="avatar-cropper__actions">
      <el-button type="primary" @click="handleCropConfirm">
        {{ t('user.profile.cropConfirm') }}
      </el-button>
    </div>
  </template>
</el-dialog>

<el-dialog
  v-model="previewVisible"
  width="420px"
  :destroy-on-close="true"
  class="avatar-preview-dialog"
  :title="t('user.profile.avatarPreviewTitle')"
>
  <div class="avatar-preview-wrapper">
    <img :src="avatarPreview || defaultAvatar" alt="avatar preview" />
  </div>
  <template #footer>
    <el-button type="primary" @click="previewVisible = false">
      {{ t('common.confirm') }}
    </el-button>
  </template>
</el-dialog>
</template>

<script setup>
import { computed, reactive, ref, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Upload, Loading, ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { BaseButton, BaseCard } from '@/components/Common'
import { getUserInfo, updateUserInfo, getProfileFields } from '@/api/User'
import { previewFile } from '@/api/File'
import { useAuthStore } from '@/store/modules/auth'
import { useIndustryStore } from '@/store/modules/industry'
import { setUserInfo } from '@/utils/auth'
import { validateChineseName, validateEmail, validatePhone } from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'
import {
ATTACHMENT_CATEGORY,
ATTACHMENT_RELATION,
IMAGE_FILE_EXTENSIONS,
validateFileType,
validateFileSize,
DEFAULT_AVATAR_MAX_SIZE_MB
} from '@/constants/attachment'
import { useFileUpload } from '@/composables/useFileUpload'
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

const formData = reactive({
username: '',
realName: '',
email: '',
phone: '',
department: '',
title: '',
avatar: '',
avatarFileId: null
})

const verification = reactive({
email: { loading: false, success: false, error: false, message: '' },
phone: { loading: false, success: false, error: false, message: '' }
})

const departmentOptions = ref([])

const avatarPreview = ref('')

const profileFieldMap = ref({})

const titleOptions = computed(() => {
const field = profileFieldMap.value?.title
if (!field?.options?.length) return []
return field.options.map((option) => ({
  ...option,
  value: option.value ?? option.optionValue
}))
})

const departmentLabel = computed(() => {
const key = industryStore.departmentLabelKey
return key ? t(key) : t('user.profile.department')
})

const departmentPlaceholder = computed(() => {
const key = industryStore.departmentPlaceholderKey
return key ? t(key) : t('user.profile.departmentPlaceholder')
})

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
      value: item.code || item.value || item.id || item.label
    }))
    syncDepartmentValue()
  }
} catch (error) {
  logger.warn('fetch departments failed', { error: error.message })
}
}

const loadProfileFields = async () => {
try {
  const params = {}
  if (industryStore.industryCode) {
    params.industry = industryStore.industryCode
  }
  const response = await getProfileFields(params)
  const payload = response?.data?.data || response?.data || response || {}
  const list = Array.isArray(payload) ? payload : payload.profileFieldList || []
  const normalizedMap = {}
  list.forEach((field) => {
    const code = field.fieldCode || field.code
    if (!code) return
    const options = Array.isArray(field.options)
      ? field.options.map((option) => ({
          label: option.optionLabel || option.label || option.name || option.value || '',
          value: option.optionValue ?? option.value ?? option.code ?? option.label ?? '',
          isDefault:
            option.isDefault === 1 ||
            option.isDefault === true ||
            option.isDefault === '1'
        }))
      : []
    normalizedMap[code] = {
      label: field.fieldLabel || field.label || '',
      type: field.fieldType || field.type || '',
      required: field.isRequired === 1 || field.isRequired === true,
      options
    }
  })
  profileFieldMap.value = normalizedMap
  logger.info('个人信息字段配置加载完成', normalizedMap)
  syncTitleValue()
} catch (error) {
  logger.warn('获取个人信息字段配置失败，使用默认配置', { error: error.message })
  profileFieldMap.value = {}
}
}

const applyFieldDefaults = () => {
if (!formData.title && titleOptions.value.length) {
  const defaultOption = titleOptions.value.find((item) => item.isDefault)
  if (defaultOption) {
    formData.title = defaultOption.value
  }
}
}

const loadProfile = async (force = false) => {
logger.info('开始加载个人信息', { force })
try {
  if (loading.value && !force) return
  loading.value = true
  await industryStore.fetchIndustryConfig(force)
  await loadProfileFields()
  await loadDepartments()
  const response = await getUserInfo()
  logger.info('获取个人信息接口返回', response)
  const payload = response?.data?.data || response?.data || response || {}
  const data = payload.userInfo || payload

  formData.username = data.username || data.account || ''
  formData.realName = data.realName || data.name || ''
  formData.email = data.email || ''
  formData.phone = data.phone || data.mobile || ''
  formData.department = data.departmentCode || data.department || ''
  formData.title = data.title || data.titleCode || data.position || ''
  formData.avatar = data.avatar || ''
  formData.avatarFileId = data.avatarFileId || data.avatarId || null

  syncDepartmentValue()
  syncTitleValue()

  // 🔥 关键修复：加载时添加时间戳，避免浏览器缓存导致头像不更新
  if (formData.avatar) {
    const timestamp = Date.now()
    const urlWithTimestamp = formData.avatar.includes('?') 
      ? `${formData.avatar}&t=${timestamp}` 
      : `${formData.avatar}?t=${timestamp}`
    avatarPreview.value = urlWithTimestamp
  } else {
    avatarPreview.value = formData.avatar
  }
  resetVerification('email')
  resetVerification('phone')
  applyFieldDefaults()
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
    avatar: formData.avatar,
    avatarFileId: formData.avatarFileId,
    industryCode: industryStore.industryCode
  }
  logger.info('提交个人信息 payload', payload)
  console.table?.(payload)
  await updateUserInfo(payload)
  
  // 🔥 关键修复：保存成功后，强制刷新用户信息，确保所有组件都能获取到最新数据
  await authStore.getUserInfo(true)
  
  // 🔥 关键修复：更新预览 URL 时添加时间戳，避免浏览器缓存导致头像不更新
  if (formData.avatar) {
    const timestamp = Date.now()
    const urlWithTimestamp = formData.avatar.includes('?') 
      ? `${formData.avatar}&t=${timestamp}` 
      : `${formData.avatar}?t=${timestamp}`
    avatarPreview.value = urlWithTimestamp
    logger.info('保存成功，已更新头像预览 URL', { 
      avatar: formData.avatar,
      avatarFileId: formData.avatarFileId,
      preview: urlWithTimestamp
    })
  }
  
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

const openFileDialog = () => {
logger.info('触发头像选择对话框')
if (!fileInputRef.value) return
fileInputRef.value.value = ''
fileInputRef.value.click()
}

const AVATAR_MAX_SIZE_MB = DEFAULT_AVATAR_MAX_SIZE_MB
const previewVisible = ref(false)
const previewObjectUrl = ref('')

const fileUploader = useFileUpload({
maxSizeMB: AVATAR_MAX_SIZE_MB,
allowedExtensions: IMAGE_FILE_EXTENSIONS,
getExtraFormData: () => {
  const extra = {}
  const userId = authStore.userInfo?.id || authStore.userInfo?.userId
  if (userId) {
    extra.relationType = ATTACHMENT_RELATION.USER
    extra.relationId = userId
    const username = authStore.userInfo?.username
    const realName = authStore.userInfo?.realName || authStore.userInfo?.name
    const relationName = [username, realName].filter(Boolean).join(' / ')
    if (relationName) {
      extra.relationName = relationName
    }
  }
  extra.attachmentType = ATTACHMENT_CATEGORY.IMAGE
  extra.isPublic = '0'
  return extra
}
})
const uploading = fileUploader.uploading

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

const handleSelectAvatar = async (event) => {
const file = event.target?.files?.[0]
logger.info('选择头像文件', { hasFile: !!file })
if (!file) return

const typeValidation = validateFileType(file, IMAGE_FILE_EXTENSIONS)
if (!typeValidation.passed) {
  ElMessage.error(typeValidation.reason || t('user.profile.invalidFormat'))
  return
}

const sizeValidation = validateFileSize(file, AVATAR_MAX_SIZE_MB)
if (!sizeValidation.passed) {
  ElMessage.error(sizeValidation.reason)
  return
}

await openCropperDialog(file)
}

const uploadAvatarFile = async (file) => {
const result = await fileUploader.uploadWithCheck(file)
if (!result) {
  return
}

const { fileInfo, reused } = result
const url = fileInfo?.previewUrl || fileInfo?.fileUrl || formData.avatar
if (!url) {
  ElMessage.error(t('user.profile.uploadError'))
  return
}

// 添加时间戳避免浏览器缓存
const timestamp = Date.now()
const urlWithTimestamp = url.includes('?') ? `${url}&t=${timestamp}` : `${url}?t=${timestamp}`

formData.avatar = url  // 保存原始 URL（用于提交给后端）
avatarPreview.value = urlWithTimestamp  // 显示带时间戳的 URL（用于预览，避免缓存）
formData.avatarFileId = fileInfo.id || fileInfo.fileId || fileInfo.attachmentId || null
pendingAvatarFile.value = null

// 🔥 关键修复：同步更新 authStore 的 userInfo，确保右上角头像实时更新
if (authStore.userInfo) {
  // 保存原始 URL 到 authStore（不带时间戳），在 Header 中显示时动态添加时间戳
  authStore.userInfo.avatar = url
  authStore.userInfo.avatarFileId = formData.avatarFileId
  // 立即保存到 localStorage，确保刷新页面后也能看到最新头像
  setUserInfo(authStore.userInfo)
  logger.info('头像上传成功，已同步更新 authStore 和 localStorage', {
    avatar: url,
    avatarFileId: formData.avatarFileId
  })
}

ElMessage.success(reused ? t('user.profile.avatarReused') : t('user.profile.uploadSuccess'))
ElMessage.info(t('user.profile.avatarRememberSave'))
}

const handleCropConfirm = async () => {
if (!cropperInstance || !pendingAvatarFile.value) return
try {
  const canvas = cropperInstance.getCroppedCanvas({
    width: 400,
    height: 400,
    imageSmoothingQuality: 'high'
  })
  if (!canvas) {
    throw new Error('cropper canvas missing')
  }
  const blob = await new Promise((resolve) =>
    canvas.toBlob(resolve, pendingAvatarFile.value.type || 'image/png')
  )
  if (!blob) {
    throw new Error('cropper toBlob failed')
  }
  const croppedFile = new File([blob], pendingAvatarFile.value.name, {
    type: pendingAvatarFile.value.type || 'image/png'
  })

  await uploadAvatarFile(croppedFile)
  cropperVisible.value = false
} catch (error) {
  logger.error('crop avatar failed', { error: error.message })
  ElMessage.error(t('user.profile.uploadError'))
}
}

watch(cropperVisible, (visible) => {
if (!visible) {
  destroyCropper()
  cropperImageSrc.value = ''
  pendingAvatarFile.value = null
}
})

watch(previewVisible, (visible) => {
if (!visible) {
  resetPreviewUrl()
}
})

watch(() => departmentOptions.value, () => {
syncDepartmentValue()
})

watch(titleOptions, () => {
syncTitleValue()
})

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

onBeforeUnmount(() => {
destroyCropper()
resetPreviewUrl()
})

const handlePreviewAvatar = async () => {
if (previewVisible.value) return

// 🔥 关键修复：添加详细的调试日志，确保使用正确的头像 ID
logger.info('开始预览头像', {
  avatarFileId: formData.avatarFileId,
  avatar: formData.avatar,
  username: authStore.userInfo?.username,
  storeAvatarFileId: authStore.userInfo?.avatarFileId
})

try {
  // 🔥 关键修复：优先使用 authStore 中的 avatarFileId（确保是最新的）
  const avatarIdToUse = formData.avatarFileId || authStore.userInfo?.avatarFileId
  
  if (avatarIdToUse) {
    logger.info('使用文件 ID 预览头像', { avatarFileId: avatarIdToUse })
    const response = await previewFile(avatarIdToUse)
    const raw = response?.data ?? response
    resetPreviewUrl()
    let previewUrl = ''
    if (raw instanceof Blob) {
      if (raw.size === 0) {
        throw new Error('empty preview blob')
      }
      const contentType = (raw.type || '').toLowerCase()
      if (contentType.includes('application/json')) {
        const text = await raw.text()
        try {
          const parsed = JSON.parse(text)
          if (typeof parsed?.data === 'string') {
            previewUrl = parsed.data
          } else if (typeof parsed?.url === 'string') {
            previewUrl = parsed.url
          } else {
            throw new Error('preview json missing url')
          }
        } catch (parseError) {
          throw new Error('preview json parse failed')
        }
      } else {
        previewUrl = URL.createObjectURL(raw)
        previewObjectUrl.value = previewUrl
      }
    } else if (typeof raw === 'string') {
      previewUrl = raw
    } else if (raw?.data) {
      if (raw.data instanceof Blob) {
        if (raw.data.size === 0) {
          throw new Error('empty preview blob')
        }
        previewUrl = URL.createObjectURL(raw.data)
        previewObjectUrl.value = previewUrl
      } else if (typeof raw.data === 'string') {
        previewUrl = raw.data
      }
    }
    if (!previewUrl) {
      throw new Error('preview url not resolved')
    }
    // 添加时间戳避免缓存
    const timestamp = Date.now()
    avatarPreview.value = previewUrl.includes('?') 
      ? `${previewUrl}&t=${timestamp}` 
      : `${previewUrl}?t=${timestamp}`
    logger.info('头像预览成功', { previewUrl: avatarPreview.value })
  } else if (formData.avatar) {
    logger.info('使用 avatar URL 预览头像', { avatar: formData.avatar })
    // 添加时间戳避免缓存
    const timestamp = Date.now()
    avatarPreview.value = formData.avatar.includes('?') 
      ? `${formData.avatar}&t=${timestamp}` 
      : `${formData.avatar}?t=${timestamp}`
  } else {
    logger.info('使用默认头像预览')
    avatarPreview.value = defaultAvatar.value
  }
  previewVisible.value = true
} catch (error) {
  logger.error('avatar preview failed', { 
    error: error.message,
    avatarFileId: formData.avatarFileId,
    avatar: formData.avatar
  })
  ElMessage.error(t('user.profile.previewError'))
  avatarPreview.value = formData.avatar || defaultAvatar.value
  previewVisible.value = true
}
}

const syncDepartmentValue = () => {
if (!formData.department || departmentOptions.value.length === 0) return
const current = formData.department
const match = departmentOptions.value.find((option) => option.value === current)
if (match) return
const fallback = departmentOptions.value.find((option) => option.label === current)
if (fallback) {
  formData.department = fallback.value
}
}

const syncTitleValue = () => {
if (!formData.title || titleOptions.value.length === 0) return
const current = formData.title
const match = titleOptions.value.find((option) => option.value === current || option.optionValue === current)
if (match) {
  formData.title = match.value
  return
}
const fallback = titleOptions.value.find((option) => option.label === current || option.optionLabel === current)
if (fallback) {
  formData.title = fallback.value
}
}
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
background-color: #f3f4f6 !important;
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

:deep(.avatar-crop-dialog) {
.el-dialog__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}
}

.avatar-cropper {
display: flex;
flex-direction: column;
align-items: center;
gap: 12px;

&__preview {
  width: 320px;
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-md);

  img {
    max-width: 100%;
    display: block;
  }
}

&__controls {
  display: flex;
  gap: 12px;
}

&__tips {
  font-size: 12px;
  color: var(--text-3);
  margin: 0;
}

&__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}
}

:deep(.avatar-preview-dialog) {
.el-dialog__body {
  display: flex;
  justify-content: center;
}
}

.avatar-preview-wrapper {
width: 320px;
height: 320px;
border-radius: 12px;
overflow: hidden;
box-shadow: var(--shadow-md);

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
}
</style>