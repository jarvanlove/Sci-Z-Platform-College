<!--
/**
 * @description 重置密码表单组件
 * 支持邮箱和手机号两种方式重置密码
 */
-->
<template>
  <el-form
    ref="resetFormRef"
    :model="resetForm"
    :rules="resetRules"
    class="reset-form"
    autocomplete="off"
    :validate-on-rule-change="false"
    @submit.prevent="handleReset"
  >
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" class="reset-tabs" @tab-change="handleTabChange">
      <el-tab-pane :label="$t('auth.resetPassword.phoneReset')" name="phone">
        <!-- 手机号输入 -->
        <el-form-item prop="phone">
          <el-input
            v-model="resetForm.phone"
            :placeholder="$t('auth.phone')"
            size="large"
            clearable
            maxlength="11"
            autocomplete="off"
            name="reset-phone"
            class="phone-input"
          >
            <template #prefix>
              <el-icon class="phone-icon"><Phone /></el-icon>
              <span class="phone-prefix">+86</span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 短信验证码输入 -->
        <el-form-item prop="smsCode">
          <div class="sms-code-container">
            <el-input
              v-model="resetForm.smsCode"
              :placeholder="$t('auth.resetPassword.verificationCodePlaceholder')"
              size="large"
              prefix-icon="Message"
              clearable
              maxlength="6"
              autocomplete="one-time-code"
              name="reset-sms-code"
            />
            <BaseButton
              type="primary"
              size="large"
              :disabled="!canSendSmsCode || smsCodeCountdown > 0"
              @click="sendSmsCode"
            >
              {{ smsCodeCountdown > 0 ? `${smsCodeCountdown}s` : $t('auth.resetPassword.getVerificationCode') }}
            </BaseButton>
          </div>
        </el-form-item>
      </el-tab-pane>

      <el-tab-pane :label="$t('auth.resetPassword.emailReset')" name="email">
        <!-- 邮箱输入 -->
        <el-form-item prop="email">
          <el-input
            v-model="resetForm.email"
            :placeholder="$t('auth.email')"
            size="large"
            prefix-icon="Message"
            clearable
            autocomplete="off"
            name="reset-email"
          />
        </el-form-item>

        <!-- 邮箱验证码输入 -->
        <el-form-item prop="emailCode">
          <div class="email-code-container">
            <el-input
              v-model="resetForm.emailCode"
              :placeholder="$t('auth.emailCode')"
              size="large"
              prefix-icon="Message"
              clearable
              maxlength="6"
              autocomplete="one-time-code"
              name="reset-email-code"
            />
            <BaseButton
              type="primary"
              size="large"
              :disabled="!canSendEmailCode || emailCodeCountdown > 0"
              @click="sendEmailCode"
            >
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : $t('auth.sendCode') }}
            </BaseButton>
          </div>
        </el-form-item>
      </el-tab-pane>
    </el-tabs>

    <!-- 新密码输入 -->
    <el-form-item prop="newPassword">
      <el-input
        v-model="resetForm.newPassword"
        type="password"
        :placeholder="$t('auth.newPassword')"
        size="large"
        prefix-icon="Lock"
        show-password
        clearable
        autocomplete="new-password"
        name="reset-new-password"
      />
    </el-form-item>

    <!-- 确认新密码输入 -->
    <el-form-item prop="confirmPassword">
      <el-input
        v-model="resetForm.confirmPassword"
        type="password"
        :placeholder="$t('auth.confirmPassword')"
        size="large"
        prefix-icon="Lock"
        show-password
        clearable
        autocomplete="new-password"
        name="reset-confirm-password"
      />
    </el-form-item>

    <!-- 重置按钮 -->
    <el-form-item>
      <BaseButton
        type="primary"
        size="large"
        :loading="loading"
        :disabled="!isFormValid"
        class="reset-button"
        @click="handleReset"
      >
        {{ t('auth.resetPasswordButton') }}
      </BaseButton>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Phone } from '@element-plus/icons-vue'
import { BaseButton } from '@/components/Common'
import { resetPassword, sendEmailCode as sendEmailCodeApi, sendSmsVerificationCode } from '@/api/Auth'
import { validatePhone } from '@/utils/validate'

// 定义事件
const emit = defineEmits(['reset-success', 'go-to-login'])

// 路由和国际化
const router = useRouter()
const { t } = useI18n()

// 表单引用
const resetFormRef = ref()

// 加载状态
const loading = ref(false)

// Tab 切换（默认手机号重置）
const activeTab = ref('phone')

// 邮箱验证码相关
const emailCodeCountdown = ref(0)
let emailCodeTimer = null
const canSendEmailCode = computed(() => {
  return (
    resetForm.email &&
    !emailError.value &&
    emailCodeCountdown.value === 0
  )
})

// 短信验证码相关
const smsCodeCountdown = ref(0)
let smsCodeTimer = null
const canSendSmsCode = computed(() => {
  return (
    resetForm.phone &&
    !phoneError.value &&
    smsCodeCountdown.value === 0
  )
})

// 错误状态
const emailError = ref('')
const phoneError = ref('')

// 表单数据
const resetForm = reactive({
  phone: '',
  email: '',
  smsCode: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: ''
})

const blankFormFields = {
  phone: '',
  email: '',
  smsCode: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: ''
}

const clearFormValues = () => {
  Object.assign(resetForm, blankFormFields)
  emailError.value = ''
  phoneError.value = ''
  // 使用 nextTick 确保 DOM 更新后再清除校验
  nextTick(() => {
    if (resetFormRef.value && typeof resetFormRef.value.clearValidate === 'function') {
      resetFormRef.value.clearValidate()
    }
  })
}

// 表单验证规则
const resetRules = computed(() => {
  const rules = {
    newPassword: [
      { required: true, message: t('auth.newPassword'), trigger: 'blur' },
      { min: 6, max: 20, message: t('auth.resetPassword.passwordLength'), trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: t('auth.confirmPassword'), trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== resetForm.newPassword) {
            callback(new Error(t('auth.resetPassword.passwordMismatch')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }

  // 根据当前 Tab 添加不同的验证规则
  if (activeTab.value === 'phone') {
    rules.phone = [
      { required: true, message: t('auth.phoneRequired'), trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          const phone = (value || '').trim()
          if (!phone) {
            callback()
            return
          }
          if (!validatePhone(phone)) {
            phoneError.value = t('auth.phoneInvalid')
            callback(new Error(t('auth.phoneInvalid')))
          } else {
            phoneError.value = ''
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
    rules.smsCode = [
      { required: true, message: t('auth.smsCodeRequired'), trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (!value) {
            callback()
            return
          }
          if (!/^\d{6}$/.test(value)) {
            callback(new Error(t('auth.smsCodeLength')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  } else {
    rules.email = [
      { required: true, message: t('auth.email'), trigger: 'blur' },
      { type: 'email', message: t('auth.resetPassword.emailFormat'), trigger: 'blur' }
    ]
    rules.emailCode = [
      { required: true, message: t('auth.emailCode'), trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (!value) {
            callback()
            return
          }
          if (!/^\d{6}$/.test(value)) {
            callback(new Error(t('auth.resetPassword.emailCodeLength')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }

  return rules
})

// 表单是否有效
const isFormValid = computed(() => {
  if (activeTab.value === 'phone') {
    return resetForm.phone &&
           resetForm.smsCode &&
           resetForm.newPassword &&
           resetForm.confirmPassword &&
           !phoneError.value
  } else {
    return resetForm.email &&
           resetForm.emailCode &&
           resetForm.newPassword &&
           resetForm.confirmPassword &&
           !emailError.value
  }
})

// 处理 Tab 切换
const handleTabChange = (tabName) => {
  clearFormValues()
}

// 处理重置密码
const handleReset = async () => {
  if (!resetFormRef.value) return
  
  try {
    await resetFormRef.value.validate()
    loading.value = true

    let payload
    if (activeTab.value === 'phone') {
      // 手机号重置
      payload = {
        phone: resetForm.phone,
        smsCode: resetForm.smsCode,
        newPassword: resetForm.newPassword
      }
    } else {
      // 邮箱重置
      payload = {
        email: resetForm.email,
        emailCode: resetForm.emailCode,
        newPassword: resetForm.newPassword
      }
    }

    const response = await resetPassword(payload)

    // 重置成功
    ElMessage.success(t('auth.resetSuccess'))
    emit('reset-success', response.data)
    
    // 跳转到登录页面
    router.push('/login')
    
  } catch (error) {
    console.error('重置密码失败:', error)
    
    // 显示错误信息
    if (!error._messageShown) {
      const errorMessage = error.response?.data?.message || t('auth.resetFailed')
      ElMessage.error(errorMessage)
    }
    
  } finally {
    loading.value = false
  }
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!canSendEmailCode.value) return
  
  try {
    const email = (resetForm.email || '').trim().toLowerCase()
    resetForm.email = email
    
    // 先验证邮箱格式
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      emailError.value = t('auth.resetPassword.emailFormat')
      ElMessage.warning(t('auth.resetPassword.emailFormat'))
      return
    }
    emailError.value = ''
    
    // 后端需要 captcha、captchaKey 和 provider，但前端不显示图形验证码
    // TODO: 后端需要修改，移除图形验证码要求
    await sendEmailCodeApi({
      email,
      captcha: '',
      captchaKey: '',
      provider: 'NETEASE' // 默认使用网易邮箱
    })
    
    ElMessage.success(t('auth.emailCodeSent'))
    
    // 开始倒计时
    emailCodeCountdown.value = 60
    if (emailCodeTimer) {
      clearInterval(emailCodeTimer)
    }
    emailCodeTimer = setInterval(() => {
      emailCodeCountdown.value--
      if (emailCodeCountdown.value <= 0) {
        clearInterval(emailCodeTimer)
        emailCodeTimer = null
      }
    }, 1000)
    
  } catch (error) {
    console.error('发送邮箱验证码失败:', error)
    if (!error._messageShown) {
      const errorMessage = error.response?.data?.message || t('auth.sendCodeFailed')
      ElMessage.error(errorMessage)
    }
  }
}

// 发送短信验证码
const sendSmsCode = async () => {
  if (!canSendSmsCode.value) return
  
  try {
    const phone = (resetForm.phone || '').trim()
    resetForm.phone = phone
    
    // 先验证手机号格式
    if (!validatePhone(phone)) {
      phoneError.value = t('auth.phoneInvalid')
      ElMessage.warning(t('auth.phoneInvalid'))
      return
    }
    phoneError.value = ''
    
    await sendSmsVerificationCode({
      phone
    })
    
    ElMessage.success(t('auth.smsCodeSent'))
    
    // 开始倒计时
    smsCodeCountdown.value = 60
    if (smsCodeTimer) {
      clearInterval(smsCodeTimer)
    }
    smsCodeTimer = setInterval(() => {
      smsCodeCountdown.value--
      if (smsCodeCountdown.value <= 0) {
        clearInterval(smsCodeTimer)
        smsCodeTimer = null
      }
    }, 1000)
    
  } catch (error) {
    console.error('发送短信验证码失败:', error)
    if (!error._messageShown) {
      const errorMessage = error.response?.data?.message || t('auth.smsCodeSendFailed')
      ElMessage.error(errorMessage)
    }
  }
}

// 组件挂载时初始化
onMounted(() => {
  clearFormValues()
})

onUnmounted(() => {
  if (emailCodeTimer) {
    clearInterval(emailCodeTimer)
    emailCodeTimer = null
  }
  if (smsCodeTimer) {
    clearInterval(smsCodeTimer)
    smsCodeTimer = null
  }
})
</script>

<style lang="scss" scoped>
.reset-form {
  width: 100%;
  
  .reset-tabs {
    margin-bottom: 20px;
    
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }
    
    :deep(.el-tabs__nav-wrap::after) {
      height: 0;
    }
    
    :deep(.el-tabs__item) {
      font-size: 16px;
      font-weight: 500;
      color: var(--text-2);
      
      &.is-active {
        color: var(--color-primary);
        font-weight: 600;
      }
      
      &:hover {
        color: var(--color-primary);
      }
    }
    
    :deep(.el-tabs__active-bar) {
      height: 3px;
      border-radius: 2px;
      background-color: var(--color-primary);
    }
  }
  
  .phone-input {
    :deep(.el-input__prefix) {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
  
  .phone-icon {
    color: var(--el-text-color-placeholder);
    font-size: 16px;
  }
  
  .phone-prefix {
    display: inline-flex;
    align-items: center;
    padding-right: 8px;
    border-right: 1px solid var(--el-border-color);
    margin-right: 8px;
    color: var(--el-text-color-placeholder);
    font-size: 14px;
  }
  
  .sms-code-container,
  .email-code-container {
    display: flex;
    gap: var(--gap-sm);
    
    .el-input {
      flex: 1;
    }
    
    :deep(.base-button) {
      min-width: 130px;
      flex-shrink: 0;
    }
  }
  
  .reset-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}
</style>
