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
import { computed, reactive, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Upload, Loading, Refresh } from '@element-plus/icons-vue'
import { BaseButton, BaseCard } from '@/components/Common'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/User'
import { useAuthStore } from '@/store/modules/auth'
import { useIndustryStore } from '@/store/modules/industry'
import { validateChineseName, validateEmail, validatePhone } from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'

const { t } = useI18n()
const logger = createLogger('UserProfileComponent')

const authStore = useAuthStore()
const industryStore = useIndustryStore()

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
  try {
    if (loading.value && !force) return
    loading.value = true
    await industryStore.fetchIndustryConfig(force)
    await loadDepartments()
    const response = await getUserInfo()
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
      avatar: formData.avatar
    }
    await updateUserInfo(payload)
    await authStore.getUserInfo(true)
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
  if (!fileInputRef.value) return
  fileInputRef.value.value = ''
  fileInputRef.value.click()
}

const handleSelectAvatar = async (event) => {
  const file = event.target?.files?.[0]
  if (!file) return

  const allowedTypes = ['image/png', 'image/jpeg', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error(t('user.profile.invalidFormat'))
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error(t('user.profile.fileTooLarge'))
    return
  }

  const form = new FormData()
  form.append('avatar', file)

  try {
    uploading.value = true
    const response = await uploadAvatar(form)
    const payload = response?.data?.data || response?.data || response || {}
    const url = payload.url || payload.avatarUrl || payload.avatar || ''
    if (url) {
      formData.avatar = url
      avatarPreview.value = url
      await authStore.getUserInfo(true)
      ElMessage.success(t('user.profile.uploadSuccess'))
    } else {
      throw new Error('missing avatar url')
    }
  } catch (error) {
    logger.error('upload avatar failed', { error: error.message })
    ElMessage.error(t('user.profile.uploadError'))
  } finally {
    uploading.value = false
  }
}

const simulateVerify = async (type, successMessage, errorMessage) => {
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
</style>

