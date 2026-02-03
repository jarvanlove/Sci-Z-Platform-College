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
        <!-- 用户名（可编辑） -->
        <el-form-item :label="t('user.profile.username')" prop="username">
          <el-input
            v-model="formData.username"
            :placeholder="t('user.profile.usernamePlaceholder')"
            clearable
          />
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
          />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item :label="t('user.profile.phone')" prop="phone">
          <el-input
            v-model="phoneModel"
            @focus="phoneFocused = true"
            @blur="handlePhoneBlur"
            @input="handlePhoneChange"
            :placeholder="t('user.profile.phonePlaceholder')"
            clearable
            maxlength="11"
          />
        </el-form-item>
        
        <!-- 手机验证码（与登录页验证码设计一致：输入框+按钮并排，对齐美观） -->
        <el-form-item 
          v-if="phoneChanged" 
          :label="t('user.profile.smsCode')" 
          prop="smsCode"
        >
          <div class="sms-code-container">
            <el-input
              v-model="formData.smsCode"
              :placeholder="t('user.profile.smsCodePlaceholder')"
              clearable
              maxlength="6"
              class="sms-code-input"
            />
            <el-button
              type="primary"
              :disabled="smsCountdown > 0 || sendingSmsCode"
              :loading="sendingSmsCode"
              class="send-sms-button"
              @click="handleSendSmsCode"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}秒` : t('user.profile.sendSmsCode') }}
            </el-button>
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

  <!-- 头像裁剪对话框 -->
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

  <!-- 头像预览对话框 -->
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
</div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { BaseButton, BaseCard } from '@/components/Common'
import { getUserInfo, updateUserInfo, getProfileFields, uploadAvatar } from '@/api/User'
import { previewFile } from '@/api/File'
import { sendSmsVerificationCode } from '@/api/Auth/auth'
import { useAuthStore } from '@/store/modules/auth'
import { useIndustryStore } from '@/store/modules/industry'
import { validateChineseName, validateEmail, validatePhone, formatPhoneDisplay } from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'
import { setUserInfo } from '@/utils/auth'
import {
ATTACHMENT_RELATION,
ATTACHMENT_CATEGORY,
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

const formData = reactive({
username: '',
realName: '',
email: '',
phone: '',
department: '',
title: '',
avatar: '',
avatarFileId: null,
smsCode: '' // 🔥 手机验证码
})

// 🔥 原始手机号（用于检测是否变化）
const originalPhone = ref('')

// 🔥 手机号是否变化
const phoneChanged = computed(() => {
  return originalPhone.value && formData.phone && formData.phone !== originalPhone.value
})

// 🔥 短信验证码相关状态
const sendingSmsCode = ref(false)
const smsCountdown = ref(0)
let smsCountdownTimer = null


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

// 🔥 手机号聚焦状态
const phoneFocused = ref(false)

// 🔥 手机号双向绑定（使用计算属性的 getter/setter）
const phoneModel = computed({
  get() {
    if (phoneFocused.value) {
      // 聚焦时显示完整手机号，方便编辑
      return formData.phone
    }
    // 未聚焦时显示脱敏手机号
    return formatPhoneDisplay(formData.phone)
  },
  set(value) {
    // 用户输入时，直接更新原始数据
    formData.phone = value
  }
})

// 🔥 处理手机号失去焦点
const handlePhoneBlur = () => {
  phoneFocused.value = false
}

// 🔥 处理手机号变化
const handlePhoneChange = () => {
  // 如果手机号变化，清空验证码
  if (phoneChanged.value) {
    formData.smsCode = ''
  }
}


const rules = reactive({
username: [
  { required: true, message: t('user.profile.usernameRequired'), trigger: 'blur' },
  { min: 3, max: 20, message: t('user.profile.usernameLength'), trigger: 'blur' },
  {
    validator: (_, value, callback) => {
      if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        callback(new Error(t('user.profile.usernameFormat')))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
],
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
smsCode: [
  {
    validator: (_, value, callback) => {
      // 🔥 如果手机号变化，验证码必填
      if (phoneChanged.value && !value) {
        callback(new Error(t('user.profile.smsCodeRequired')))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  },
  {
    validator: (_, value, callback) => {
      // 🔥 如果手机号变化，验证码格式校验
      if (phoneChanged.value && value && !/^\d{6}$/.test(value)) {
        callback(new Error(t('user.profile.smsCodeFormat')))
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
  // 🔥 保存原始手机号，用于检测是否变化
  originalPhone.value = formData.phone
  formData.department = data.departmentCode || data.department || ''
  formData.title = data.title || data.titleCode || data.position || ''
  formData.avatar = data.avatar || ''
  formData.avatarFileId = data.avatarFileId || data.avatarId || null
  // 🔥 清空验证码
  formData.smsCode = ''

  syncDepartmentValue()
  syncTitleValue()

  avatarPreview.value = formData.avatar
  applyFieldDefaults()

  // 同步头像到 authStore，使左侧栏/顶部栏立即显示最新头像，无需重新登录
  if (authStore.userInfo) {
    authStore.userInfo.avatar = formData.avatar
    authStore.userInfo.avatarFileId = formData.avatarFileId ?? authStore.userInfo.avatarFileId
    setUserInfo(authStore.userInfo)
    logger.info('个人信息加载完成，已同步头像到全局状态', { hasAvatar: !!formData.avatar, avatarFileId: formData.avatarFileId })
  }
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
  // 🔥 修复：只传基本信息，不传头像相关字段（头像通过独立的上传接口更新）
  const payload = {
    username: formData.username,
    realName: formData.realName,
    email: formData.email,
    phone: formData.phone,
    department: formData.department,
    title: formData.title
  }
  
  // 🔥 如果手机号变化，需要传递验证码
  if (phoneChanged.value) {
    if (!formData.smsCode) {
      ElMessage.error(t('user.profile.smsCodeRequired'))
      saving.value = false
      return
    }
    payload.smsCode = formData.smsCode
  }
  
  logger.info('提交个人信息 payload', payload)
  console.table?.(payload)
  await updateUserInfo(payload)
  
  // 🔥 更新成功后，更新原始手机号
  if (phoneChanged.value) {
    originalPhone.value = formData.phone
    formData.smsCode = ''
  }
  await authStore.getUserInfo(true)
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
try {
  uploading.value = true
  const uploadFormData = new FormData()
  
  // 🔥 关键修复：字段名必须是 'file'，与后端 FileUploadReq.file 对应
  uploadFormData.append('file', file)
  
  // 添加其他可选字段，与 file/upload 接口保持一致
  const userId = authStore.userInfo?.id || authStore.userInfo?.userId
  if (userId) {
    uploadFormData.append('relationType', ATTACHMENT_RELATION.USER)
    uploadFormData.append('relationId', userId.toString())
    const username = authStore.userInfo?.username
    const realName = authStore.userInfo?.realName || authStore.userInfo?.name
    const relationName = [username, realName].filter(Boolean).join(' / ')
    if (relationName) {
      uploadFormData.append('relationName', relationName)
    }
  }
  uploadFormData.append('attachmentType', ATTACHMENT_CATEGORY.IMAGE)
  uploadFormData.append('isPublic', '0')
  
  const response = await uploadAvatar(uploadFormData)
  const payload = response?.data?.data || response?.data || response || {}
  
  // 处理返回结果，兼容不同的返回格式
  // 优先使用 previewUrl（上传接口返回的预览URL），其次使用其他字段
  const url = payload.previewUrl || payload.avatar || payload.avatarUrl || payload.url || payload.fileUrl
  const fileId = payload.avatarFileId || payload.fileId || payload.attachmentId || payload.id || null
  
  if (!url) {
    throw new Error('上传接口未返回头像URL')
  }
  
  // 🔥 关键修复：上传成功后立即从服务器获取最新用户信息，确保 Header 显示最新头像
  // 更新个人信息页面的头像显示
  formData.avatar = url
  avatarPreview.value = url
  formData.avatarFileId = fileId
  pendingAvatarFile.value = null
  
  // 立即从服务器获取最新用户信息，确保 Header 和所有组件都能获取到最新的头像
  try {
    await authStore.getUserInfo(true)
    logger.info('头像上传成功，已从服务器获取最新用户信息', { 
      avatar: authStore.userInfo?.avatar,
      avatarFileId: authStore.userInfo?.avatarFileId 
    })
  } catch (getUserInfoError) {
    // 如果获取失败，至少更新本地 store，确保当前页面显示正确
    logger.warn('获取最新用户信息失败，使用上传接口返回的数据', { error: getUserInfoError.message })
    if (authStore.userInfo) {
      authStore.userInfo.avatar = url
      if (fileId) {
        authStore.userInfo.avatarFileId = fileId
      }
      // 保存到 localStorage，确保刷新后也能显示
      setUserInfo(authStore.userInfo)
    }
  }
  
  ElMessage.success(t('user.profile.uploadSuccess'))
} catch (error) {
  logger.error('upload avatar failed', { error: error.message })
  ElMessage.error(t('user.profile.uploadError'))
  throw error
} finally {
  uploading.value = false
}
}

// 🔥 处理裁剪缩放（放大/缩小）
const handleCropZoom = (ratio) => {
  if (!cropperInstance) {
    logger.warn('Cropper instance not initialized')
    return
  }
  try {
    // Cropper.js 的 zoom 方法接受相对缩放值
    // ratio > 0 表示放大，ratio < 0 表示缩小
    cropperInstance.zoom(ratio)
  } catch (error) {
    logger.error('crop zoom failed', { error: error.message })
  }
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


onMounted(() => {
logger.info('UserProfile component mounted，开始加载数据')
loadProfile()
})

onBeforeUnmount(() => {
destroyCropper()
resetPreviewUrl()
if (smsCountdownTimer) {
  clearInterval(smsCountdownTimer)
  smsCountdownTimer = null
}
})

const handlePreviewAvatar = async () => {
if (previewVisible.value) return
try {
  // 🔥 关键修复：优先使用 avatar URL，避免使用已删除的文件 ID
  // 如果 avatar 是完整的 URL（http/https 或 / 开头），直接使用
  if (formData.avatar && (formData.avatar.startsWith('http://') || formData.avatar.startsWith('https://') || formData.avatar.startsWith('/'))) {
    avatarPreview.value = formData.avatar
    previewVisible.value = true
    return
  }
  
  // 如果有 avatarFileId 且没有有效的 avatar URL，才使用文件预览接口
  if (formData.avatarFileId) {
    try {
      const response = await previewFile(formData.avatarFileId)
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
      if (previewUrl) {
        avatarPreview.value = previewUrl
        previewVisible.value = true
        return
      }
    } catch (fileError) {
      logger.warn('文件预览失败，尝试使用 avatar URL', { error: fileError.message, fileId: formData.avatarFileId })
      // 如果文件预览失败，继续尝试使用 avatar URL
    }
  }
  
  // 最后尝试使用 avatar 字段（可能是相对路径）
  if (formData.avatar) {
    avatarPreview.value = formData.avatar
    previewVisible.value = true
    return
  }
  
  // 都没有则使用默认头像
  avatarPreview.value = defaultAvatar.value
  previewVisible.value = true
} catch (error) {
  logger.error('avatar preview failed', { error: error.message })
  ElMessage.error(t('user.profile.previewError'))
  // 出错时尝试使用 avatar URL 或默认头像
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

// 🔥 发送手机验证码
const handleSendSmsCode = async () => {
  if (!formData.phone) {
    ElMessage.warning(t('user.profile.phoneRequired'))
    return
  }
  
  if (!validatePhone(formData.phone)) {
    ElMessage.warning(t('user.profile.phoneFormat'))
    return
  }
  
  try {
    sendingSmsCode.value = true
    await sendSmsVerificationCode({
      phone: formData.phone
    })
    ElMessage.success(t('user.profile.smsCodeSent'))
    startSmsCountdown()
  } catch (error) {
    logger.error('发送短信验证码失败', error)
    if (!error._messageShown) {
      ElMessage.error(error.response?.data?.message || t('user.profile.smsCodeSendFailed'))
    }
  } finally {
    sendingSmsCode.value = false
  }
}

// 🔥 启动短信验证码倒计时
const startSmsCountdown = () => {
  smsCountdown.value = 60
  if (smsCountdownTimer) {
    clearInterval(smsCountdownTimer)
  }
  smsCountdownTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      clearInterval(smsCountdownTimer)
      smsCountdownTimer = null
    }
  }, 1000)
}

// 🔥 清理倒计时
onBeforeUnmount(() => {
destroyCropper()
resetPreviewUrl()
if (smsCountdownTimer) {
  clearInterval(smsCountdownTimer)
  smsCountdownTimer = null
}
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
color: var(--color-primary); // 🔥 与"个人信息"页面标题颜色保持一致
}

// 🔥 头像设置卡片标题和文字颜色：与个人信息字体颜色一致
.avatar-card {
  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-primary); // 🔥 与"个人信息"页面标题颜色保持一致
  }
  
  // 🔥 头像设置部分的文字颜色统一
  .avatar-tip {
    font-size: 12px;
    color: var(--text-3) !important; // 与个人信息表单内容颜色一致
  }
  
  // 🔥 头像裁剪提示文字颜色
  .avatar-cropper__tips {
    font-size: 12px;
    color: var(--text-3) !important; // 与个人信息表单内容颜色一致
  }
}

.profile-form {
max-width: 640px;
  
  // 🔥 表单项内容区始终占满标签右侧，保证手机验证码行与其它输入列左缘对齐
  :deep(.el-form-item__content) {
    flex: 1;
    min-width: 0;
    width: 100%;
  }
  
  // 🔥 表单标签样式：按照页面修改.md规范
  :deep(.el-form-item__label) {
    font-size: 14px !important;
    color: var(--text-2) !important; // #4b5563
    font-weight: 600 !important; // 加粗
  }
  
  // 🔥 表单内容样式：按照页面修改.md规范
  :deep(.el-input__inner),
  :deep(.el-textarea__inner),
  :deep(.el-select__wrapper .el-select__selected-item),
  :deep(.el-select__placeholder) {
    font-size: 14px !important; // 与 placeholder 一致
    color: var(--text-3) !important; // #6b7280，比 placeholder 稍深一点
    font-weight: 400 !important; // normal
  }
  
  // 🔥 只读输入框内容颜色
  :deep(.readonly-input .el-input__inner) {
    color: var(--text-3) !important;
    font-weight: 400 !important;
  }
  
  // 🔥 状态标签样式：按照页面修改.md规范
  :deep(.el-tag) {
    padding: 4px 8px !important;
    border-radius: 12px !important;
    font-size: 12px !important;
    font-weight: 500 !important;
  }
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

// 选择图片按钮悬浮效果 - 文字和图标变白色
:deep(.base-button.el-button--primary) {
  transition: all 0.2s ease;
  cursor: pointer;
  
  &:hover:not(:disabled):not(.is-loading) {
    background-color: var(--color-primary-dark) !important;
    border-color: var(--color-primary-dark) !important;
    transform: translateY(-1px) !important;
    box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3) !important;
    
    // 悬浮时文字和图标变白色
    color: #ffffff !important;
    
    .el-icon,
    svg {
      color: #ffffff !important;
      fill: #ffffff !important;
    }
    
    span {
      color: #ffffff !important;
    }
  }
  
  &:active:not(:disabled):not(.is-loading) {
    transform: translateY(0) !important;
    box-shadow: 0 2px 4px rgba(30, 58, 138, 0.3) !important;
  }
}

// 暗色主题下的悬浮效果
[data-theme='dark'] &,
.dark & {
  :deep(.base-button.el-button--primary:hover:not(:disabled):not(.is-loading)) {
    background-color: var(--color-primary-light) !important;
    border-color: var(--color-primary-light) !important;
    box-shadow: 0 4px 12px rgba(96, 165, 250, 0.3) !important;
    
    // 暗色主题下悬浮时文字和图标也变白色
    color: #ffffff !important;
    
    .el-icon,
    svg {
      color: #ffffff !important;
      fill: #ffffff !important;
    }
    
    span {
      color: #ffffff !important;
    }
  }
}
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
color: #ffffff;
opacity: 0;
transition: opacity 0.3s ease;
font-weight: 500;
text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);

span {
  color: #ffffff;
  font-weight: 500;
}
}

.avatar-tip {
margin: 0;
font-size: 12px;
color: var(--text-3) !important; // 🔥 与个人信息表单内容颜色一致
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
    align-items: stretch;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 4px;
  }

  :deep(.el-form-item__content) {
    width: 100%;
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
  color: var(--text-3) !important; // 🔥 与个人信息表单内容颜色一致
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

// 手机验证码：与登录页（SmsLoginForm）一致——输入框与按钮并排、对齐美观、按钮完整显示
.sms-code-container {
  display: flex;
  gap: 12px;
  align-items: stretch;
  width: 100%;
  min-width: 0;
  
  .sms-code-input {
    flex: 1;
    min-width: 0;
  }
  
  .send-sms-button {
    min-width: 120px;
    flex-shrink: 0;
    white-space: nowrap;
    border-radius: 8px;
    font-weight: 500;
    transition: all 0.2s ease;
    
    &:not(:disabled):not(.is-loading):hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.35);
    }
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    
    &:hover {
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
    }
    
    &.is-focus {
      box-shadow: 0 0 0 2px rgba(var(--el-color-primary-rgb, 59, 130, 246), 0.2);
    }
  }
}
</style>