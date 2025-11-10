<!--
/**
 * @description 注册表单组件
 * 严格遵循注册页面提示词模板实现
 */
-->
<template>
  <el-form
    ref="registerFormRef"
    :model="registerForm"
    :rules="registerRules"
    label-position="top"
    class="register-form"
  >
    <!-- 基本信息 -->
    <div class="form-group">
      <div class="group-title">{{ $t('auth.register.basicInfo') }}</div>

      <div class="form-row">
        <div class="form-item">
          <el-form-item :label="$t('auth.register.usernameLabel')" prop="username">
            <el-input
              v-model="registerForm.username"
              :placeholder="$t('auth.register.usernamePlaceholder')"
              size="large"
              clearable
              @blur="validateUsername"
            />
            <div v-if="usernameError" class="status-message error">
              {{ usernameError }}
            </div>
            <div v-else-if="usernameSuccess" class="status-message success">
              <span class="status-icon">✓</span>{{ $t('auth.register.usernameAvailable') }}
            </div>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item :label="$t('auth.register.realNameLabel')" prop="realName">
            <el-input
              v-model="registerForm.realName"
              :placeholder="$t('auth.register.realNamePlaceholder')"
              size="large"
              clearable
              @blur="validateRealName"
            />
            <div v-if="realNameError" class="status-message error">
              {{ realNameError }}
            </div>
            <div v-else-if="realNameSuccess" class="status-message success">
              <span class="status-icon">✓</span>
            </div>
          </el-form-item>
        </div>
      </div>

      <div class="form-row">
        <div class="form-item">
          <el-form-item :label="$t('auth.register.emailLabel')" prop="email">
            <el-input
              v-model="registerForm.email"
              :placeholder="$t('auth.register.emailPlaceholder')"
              size="large"
              clearable
              @blur="validateEmailField"
            />
            <div v-if="emailError" class="status-message error">
              {{ emailError }}
            </div>
            <div v-else-if="emailSuccess" class="status-message success">
              <span class="status-icon">✓</span>
            </div>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item :label="$t('auth.register.phoneLabel')" prop="phone">
            <el-input
              v-model="registerForm.phone"
              :placeholder="$t('auth.register.phonePlaceholder')"
              size="large"
              clearable
              @blur="validatePhoneField"
            />
            <div v-if="phoneError" class="status-message error">
              {{ phoneError }}
            </div>
            <div v-else-if="phoneSuccess" class="status-message success">
              <span class="status-icon">✓</span>
            </div>
          </el-form-item>
        </div>
      </div>

      <div class="form-item">
        <el-form-item :label="departmentLabel" prop="department">
          <el-select
            v-model="registerForm.department"
            :placeholder="departmentPlaceholder"
            size="large"
            :loading="departmentsLoading"
            filterable
            clearable
          >
            <el-option
              v-for="dept in departments"
              :key="dept.value"
              :label="dept.label"
              :value="dept.value"
            />
          </el-select>
          <div v-if="departmentError" class="status-message error">
            {{ departmentError }}
          </div>
        </el-form-item>
      </div>
    </div>

    <!-- 密码设置 -->
    <div class="form-group">
      <div class="group-title">{{ $t('auth.register.passwordSettings') }}</div>

      <div class="form-item">
        <el-form-item :label="$t('auth.register.passwordLabel')" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            :placeholder="$t('auth.register.passwordPlaceholder')"
            size="large"
            show-password
            clearable
            @input="checkPasswordStrength"
          />
          <div v-if="registerForm.password" class="password-strength">
            <div class="password-strength-bar" :class="passwordStrengthClass"></div>
          </div>
          <div v-if="registerForm.password" class="password-strength-text">
            {{ $t('auth.register.passwordStrengthLabel') }}：{{ passwordStrengthText }}
          </div>
          <div v-if="passwordError" class="status-message error">
            {{ passwordError }}
          </div>
        </el-form-item>
      </div>

      <div class="form-item">
        <el-form-item :label="$t('auth.register.confirmPasswordLabel')" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            :placeholder="$t('auth.register.confirmPasswordPlaceholder')"
            size="large"
            show-password
            clearable
            @blur="validateConfirmPassword"
          />
          <div v-if="confirmPasswordError" class="status-message error">
            {{ confirmPasswordError }}
          </div>
          <div v-else-if="confirmPasswordSuccess" class="status-message success">
            <span class="status-icon">✓</span>{{ $t('auth.register.passwordMatch') }}
          </div>
        </el-form-item>
      </div>
    </div>

    <!-- 身份验证 -->
    <div class="form-group">
      <div class="group-title">{{ $t('auth.register.identityVerification') }}</div>

      <div class="form-row single">
        <div class="form-item">
          <el-form-item :label="$t('auth.register.captchaLabel')" prop="captcha">
            <div class="captcha-wrapper">
              <el-input
                v-model="registerForm.captcha"
                :placeholder="$t('auth.register.captchaPlaceholder')"
                size="large"
                clearable
              />
              <div class="captcha-image" @click="refreshCaptcha">
                <img v-if="captchaUrl" :src="captchaUrl" :alt="$t('auth.register.captchaAlt')" />
                <span v-else>{{ $t('common.loading') }}</span>
              </div>
            </div>
            <div v-if="captchaError" class="status-message error">
              {{ captchaError }}
            </div>
          </el-form-item>
        </div>
      </div>

      <!--
        TODO: 开通短信验证码服务后启用以下模块，并同步恢复发送逻辑。
        <div class="form-row single">
          <div class="form-item">
            <el-form-item :label="$t('auth.register.verificationCodeLabel')" prop="verificationCode">
              <el-input
                v-model="registerForm.verificationCode"
                :placeholder="$t('auth.register.verificationCodePlaceholder')"
                maxlength="6"
                size="large"
                clearable
              />
              <div class="verification-hint">
                {{ $t('auth.register.smsCodeHint') }}
              </div>
            </el-form-item>
          </div>
        </div>
      -->
    </div>

    <!-- 用户协议 -->
    <div class="form-group agreement-group">
      <AgreementNotice
        v-model="registerForm.agreement"
        @view-user-agreement="showUserAgreement"
        @view-privacy-policy="showPrivacyPolicy"
      />
      <div v-if="agreementError" class="status-message error">
        {{ agreementError }}
      </div>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-section">
      <BaseButton
        type="primary"
        size="large"
        :loading="loading"
        :disabled="!isFormValid"
        class="register-button"
        @click="handleRegister"
      >
        {{ loading ? $t('auth.register.registering') : $t('auth.register.registerNow') }}
      </BaseButton>
    </div>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { BaseButton } from '@/components/Common'
import AgreementNotice from '@/components/Common/AgreementNotice.vue'
import { register, getCaptcha } from '@/api/Auth'
import { validateEmail, validatePhone, validatePassword } from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'
import { useIndustryStore } from '@/store/modules/industry'

const emit = defineEmits(['register-success'])

const router = useRouter()
const { t } = useI18n()
const logger = createLogger('RegisterForm')
const industryStore = useIndustryStore()

const registerFormRef = ref()
const loading = ref(false)
const captchaUrl = ref('')
const passwordStrength = ref(0)

const usernameError = ref('')
const usernameSuccess = ref(false)
const realNameError = ref('')
const realNameSuccess = ref(false)
const emailError = ref('')
const emailSuccess = ref(false)
const phoneError = ref('')
const phoneSuccess = ref(false)
const departmentError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')
const confirmPasswordSuccess = ref(false)
const captchaError = ref('')
const agreementError = ref('')

const registerForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  captchaKey: '',
  // verificationCode: '',
  agreement: false
})

const departments = ref([])
const departmentsLoading = ref(false)
const departmentLabel = computed(() => t(industryStore.departmentLabelKey))
const departmentPlaceholder = computed(() => t(industryStore.departmentPlaceholderKey))
// let countdownTimer = null

const registerRules = {
  username: [
    { required: true, message: t('auth.register.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 20, message: t('auth.register.usernameLength'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: t('auth.register.usernamePattern'),
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: t('auth.register.realNameRequired'), trigger: 'blur' },
    { min: 2, max: 10, message: t('auth.register.realNameLength'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('auth.register.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('auth.register.emailFormat'), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('auth.register.phoneRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const phone = (value || '').trim()
        if (!phone) {
          callback()
          return
        }
        if (!validatePhone(phone)) {
          callback(new Error(t('auth.register.phoneFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  department: [
    { required: true, message: departmentPlaceholder.value, trigger: 'change' }
  ],
  password: [
    { required: true, message: t('auth.register.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('auth.register.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('auth.register.confirmPasswordRequired'), trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: t('auth.register.captchaRequired'), trigger: 'blur' }
  ]
  // verificationCode: [
  //   {
  //     validator: (rule, value, callback) => {
  //       if (!value) {
  //         callback()
  //         return
  //       }
  //       if (!/^\d{6}$/.test(value)) {
  //         callback(new Error(t('auth.register.verificationCodeLength')))
  //       } else {
  //         callback()
  //       }
  //     },
  //     trigger: 'blur'
  //   }
  // ]
}

const isFormValid = computed(() => {
  return (
    registerForm.username &&
    registerForm.realName &&
    registerForm.email &&
    registerForm.phone &&
    registerForm.department &&
    registerForm.password &&
    registerForm.confirmPassword &&
    registerForm.captcha
  )
})

// const canSendCode = computed(() => {
//   return (
//     !!registerForm.email &&
//     !!registerForm.phone &&
//     !!registerForm.captcha &&
//     !emailError.value &&
//     !phoneError.value
//   )
// })

// const maskedPhone = computed(() => {
//   const phone = registerForm.phone
//  if (!phone || phone.length < 7) {
//     return '****'
//   }
//   return `${phone.slice(0, 3)}****${phone.slice(-4)}`
// })

const passwordStrengthClass = computed(() => {
  if (passwordStrength.value <= 2) return 'weak'
  if (passwordStrength.value <= 4) return 'medium'
  return 'strong'
})

const passwordStrengthText = computed(() => {
  if (passwordStrength.value <= 2) return t('auth.register.passwordWeak')
  if (passwordStrength.value <= 4) return t('auth.register.passwordMedium')
  return t('auth.register.passwordStrong')
})

const fetchDepartments = async () => {
  try {
    departmentsLoading.value = true
    logger.info('Fetching departments for register form')
    const list = await industryStore.fetchDepartmentLabels()
    departments.value = list
      .map((item) => ({
        label: item?.label || item?.name || item?.title || item?.value || '',
        value: item?.value ?? item?.code ?? item?.id ?? item?.label ?? ''
      }))
      .filter((item) => item.label && item.value)

    if (!departments.value.some((item) => item.value === registerForm.department)) {
      registerForm.department = ''
    }

    if (departments.value.length === 0) {
      logger.warn('Department labels list is empty')
      ElMessage.warning(t('auth.register.departmentsLoadFailed'))
    }
  } catch (error) {
    logger.error('fetchDepartments error', error)
    ElMessage.error(t('auth.register.departmentsLoadFailed'))
  } finally {
    departmentsLoading.value = false
  }
}

const refreshCaptcha = async () => {
  try {
    const response = await getCaptcha()
    captchaUrl.value = response.data?.captchaImage || response.data?.captchaUrl || ''
    registerForm.captchaKey = response.data?.captchaKey || ''
    captchaError.value = ''
  } catch (error) {
    logger.error('refreshCaptcha error', error)
    captchaError.value = t('auth.register.captchaLoadFailed')
  }
}

const validateUsername = async () => {
  usernameError.value = ''
  usernameSuccess.value = false

  if (!registerForm.username) {
    usernameError.value = t('auth.register.usernameRequired')
    return false
  }

  if (!/^[a-zA-Z0-9_]{3,20}$/.test(registerForm.username)) {
    usernameError.value = t('auth.register.usernamePattern')
    return false
  }

  // 模拟异步唯一性校验
  await new Promise((resolve) => setTimeout(resolve, 400))

  usernameSuccess.value = true
  return true
}

const validateRealName = () => {
  realNameError.value = ''
  realNameSuccess.value = false

  if (!registerForm.realName) {
    realNameError.value = t('auth.register.realNameRequired')
    return false
  }

  if (registerForm.realName.length < 2 || registerForm.realName.length > 10) {
    realNameError.value = t('auth.register.realNameLength')
    return false
  }

  realNameSuccess.value = true
  return true
}

const validateEmailField = async () => {
  emailError.value = ''
  emailSuccess.value = false

  if (!registerForm.email) {
    emailError.value = t('auth.register.emailRequired')
    return false
  }

  if (!validateEmail(registerForm.email)) {
    emailError.value = t('auth.register.emailFormat')
    return false
  }

  await new Promise((resolve) => setTimeout(resolve, 400))

  emailSuccess.value = true
  return true
}

const validatePhoneField = async () => {
  phoneError.value = ''
  phoneSuccess.value = false

  const phone = (registerForm.phone || '').trim()
  registerForm.phone = phone

  if (!phone) {
    phoneError.value = t('auth.register.phoneRequired')
    return false
  }

  if (!validatePhone(phone)) {
    phoneError.value = t('auth.register.phoneFormat')
    return false
  }

  await new Promise((resolve) => setTimeout(resolve, 400))

  phoneSuccess.value = true
  return true
}

const checkPasswordStrength = () => {
  passwordError.value = ''

  const password = registerForm.password
  if (!password) {
    passwordStrength.value = 0
    return
  }

  let strength = 0
  if (password.length >= 6) strength += 1
  if (password.length >= 8) strength += 1
  if (/[a-z]/.test(password)) strength += 1
  if (/[A-Z]/.test(password)) strength += 1
  if (/[0-9]/.test(password)) strength += 1
  if (/[^a-zA-Z0-9]/.test(password)) strength += 1

  passwordStrength.value = strength

  if (!validatePassword(password)) {
    passwordError.value = t('auth.register.passwordLength')
  }
}

const validateConfirmPassword = () => {
  confirmPasswordError.value = ''
  confirmPasswordSuccess.value = false

  if (!registerForm.confirmPassword) {
    confirmPasswordError.value = t('auth.register.confirmPasswordRequired')
    return false
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    confirmPasswordError.value = t('auth.register.passwordMismatch')
    return false
  }

  confirmPasswordSuccess.value = true
  return true
}

const ensureAgreement = () => {
  agreementError.value = ''
  if (!registerForm.agreement) {
    agreementError.value = t('auth.register.agreementRequired')
    ElMessage.warning(t('auth.register.agreementRequired'))
    registerForm.agreement = true
    setTimeout(() => {
      agreementError.value = ''
    }, 0)
    return false
  }
  return true
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    const agreementOk = ensureAgreement()
    if (!agreementOk) return

    loading.value = true

    const payload = {
      username: registerForm.username,
      realName: registerForm.realName,
      email: registerForm.email,
      phone: registerForm.phone,
      department: registerForm.department,
      password: registerForm.password,
      captcha: registerForm.captcha,
      captchaKey: registerForm.captchaKey
      // verificationCode: registerForm.verificationCode
    }

    const response = await register(payload)

    ElMessage.success(t('auth.register.registerSuccess'))
    emit('register-success', response.data)
    router.push('/login')
  } catch (error) {
    logger.error('handleRegister error', error)
    const message = error.response?.data?.message || t('auth.register.registerFailed')
    ElMessage.error(message)
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

const showUserAgreement = () => {
  ElMessageBox.alert(t('legal.userAgreementContent'), t('auth.register.userAgreement'), {
    confirmButtonText: t('common.confirm'),
    customClass: 'agreement-modal',
    dangerouslyUseHTMLString: true,
    callback: () => {}
  })
}

const showPrivacyPolicy = () => {
  ElMessageBox.alert(t('legal.privacyPolicyContent'), t('auth.register.privacyPolicy'), {
    confirmButtonText: t('common.confirm'),
    customClass: 'agreement-modal',
    dangerouslyUseHTMLString: true,
    callback: () => {}
  })
}

watch(
  () => industryStore.industryCode,
  async (newCode, oldCode) => {
    if (newCode && newCode !== oldCode) {
      await fetchDepartments()
    }
  }
)

watch(
  departmentPlaceholder,
  (newPlaceholder) => {
    if (registerRules.department && registerRules.department[0]) {
      registerRules.department[0].message = newPlaceholder
    }
  },
  { immediate: true }
)

watch(
  () => registerForm.username,
  () => {
    usernameSuccess.value = false
    if (usernameError.value) {
      usernameError.value = ''
    }
  }
)

watch(
  () => registerForm.email,
  () => {
    emailSuccess.value = false
    emailError.value = ''
  }
)

watch(
  () => registerForm.phone,
  () => {
    phoneSuccess.value = false
    phoneError.value = ''
  }
)

watch(
  () => registerForm.agreement,
  (value) => {
    if (value) {
      agreementError.value = ''
    }
  }
)

onMounted(async () => {
  await Promise.all([refreshCaptcha(), fetchDepartments()])
})

onUnmounted(() => {
  // if (countdownTimer) {
  //   clearInterval(countdownTimer)
  // }
})
</script>

<style lang="scss" scoped>
.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;

  :deep(.el-form-item__label) {
    font-weight: 400;
    color: #334155;
    font-size: 14px;
  }
}

.form-group {
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.75);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.group-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-row.single {
  grid-template-columns: 1fr;
}

.form-item {
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }
}

.status-message {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;

  &.error {
    color: #dc2626;
  }

  &.success {
    color: #16a34a;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.status-icon {
  font-weight: 700;
}

.password-strength {
  margin-top: 8px;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.password-strength-bar {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;

  &.weak {
    width: 33%;
    background: #dc2626;
  }

  &.medium {
    width: 66%;
    background: #f97316;
  }

  &.strong {
    width: 100%;
    background: #16a34a;
  }
}

.password-strength-text {
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
}

.captcha-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;

  .el-input {
    flex: 1;
  }
}

.captcha-image {
  width: 120px;
  height: 44px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  border-radius: 8px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px;
  }

  span {
    font-size: 12px;
    color: #64748b;
  }

  &:hover {
    border-color: #3b82f6;
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
  }
}

.verification-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;

  .el-input {
    flex: 1;
  }
}

.verification-button {
  min-width: 130px;
}

.verification-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.success-text {
  color: #16a34a;
}

.agreement-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submit-section {
  margin-top: 8px;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>

