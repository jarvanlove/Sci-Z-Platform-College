<!--
/**
 * @description 短信登录表单组件
 * 包含手机号、短信验证码、图形验证码（发送前）、记住我等功能
 */
-->
<template>
  <el-form
    ref="smsLoginFormRef"
    :model="smsLoginForm"
    :rules="smsLoginRules"
    class="sms-login-form"
    @submit.prevent="handleSmsLogin"
  >
    <!-- 手机号输入 -->
    <el-form-item prop="phone">
      <el-input
        v-model="smsLoginForm.phone"
        :placeholder="$t('auth.phone')"
        size="large"
        clearable
        maxlength="11"
        class="phone-input"
      >
        <template #prefix>
          <el-icon class="phone-icon"><Phone /></el-icon>
          <span class="phone-prefix">+86</span>
        </template>
      </el-input>
    </el-form-item>

    <!-- 图形验证码（发送短信前需要） -->
    <el-form-item v-if="showCaptchaForSms" prop="captcha">
      <div class="captcha-container">
        <el-input
          v-model="smsLoginForm.captcha"
          :placeholder="$t('auth.captcha')"
          size="large"
          prefix-icon="Picture"
          clearable
        />
        <div class="captcha-image" @click="refreshCaptcha">
          <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
          <span v-else>{{ $t('common.loading') }}</span>
        </div>
      </div>
    </el-form-item>

    <!-- 短信验证码输入 -->
    <el-form-item prop="smsCode">
      <div class="sms-code-container">
        <el-input
          v-model="smsLoginForm.smsCode"
          :placeholder="$t('auth.smsCode')"
          size="large"
          clearable
          maxlength="6"
          class="sms-code-input"
        >
          <template #prefix>
            <el-icon class="sms-icon"><Message /></el-icon>
          </template>
        </el-input>
        <el-button
          type="primary"
          size="large"
          :disabled="!canSendSmsCode || smsCountdown > 0"
          :loading="sendingSmsCode"
          class="send-sms-button"
          @click="handleSendSmsCode"
        >
          {{ smsCountdown > 0 ? `${smsCountdown}s` : $t('auth.sendSmsCode') }}
        </el-button>
      </div>
    </el-form-item>

    <!-- 记住我 -->
    <el-form-item>
      <el-checkbox v-model="smsLoginForm.rememberMe">
        {{ $t('auth.rememberMe') }}
      </el-checkbox>
    </el-form-item>

    <!-- 登录按钮 -->
    <el-form-item>
      <BaseButton
        type="primary"
        size="large"
        :loading="loading"
        :disabled="!isFormValid"
        class="login-button"
        @click="handleSmsLogin"
      >
        {{ $t('auth.login') }}
      </BaseButton>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Message, Phone } from '@element-plus/icons-vue'
import { BaseButton } from '@/components/Common'
import { getCaptcha, sendSmsVerificationCode } from '@/api/Auth'
import { useAuthStore } from '@/store/modules/auth'
import { createLogger } from '@/utils/simpleLogger'

// 创建日志器
const authLogger = createLogger('SmsLoginForm')

// 定义事件
const emit = defineEmits(['login-success', 'login-attempt'])

// 协议校验状态（由父组件控制）
const props = defineProps({
  agreementChecked: {
    type: Boolean,
    default: false
  }
})

// 国际化
const { t } = useI18n()
const authStore = useAuthStore()

// 表单引用
const smsLoginFormRef = ref()

// 加载状态
const loading = ref(false)
const sendingSmsCode = ref(false)

// 验证码相关
const showCaptchaForSms = ref(false)
const captchaUrl = ref('')
const captchaKey = ref('')
const smsFailCount = ref(0)

// 短信验证码倒计时
const smsCountdown = ref(0)
let smsCountdownTimer = null

// 表单数据
const smsLoginForm = reactive({
  phone: '',
  smsCode: '',
  captcha: '',
  rememberMe: false
})

// 手机号验证规则
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error(t('auth.phoneRequired')))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error(t('auth.phoneInvalid')))
  } else {
    callback()
  }
}

// 表单验证规则
const smsLoginRules = {
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: t('auth.smsCodeRequired'), trigger: 'blur' },
    { len: 6, message: t('auth.smsCodeLength'), trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: t('auth.captcha'), trigger: 'blur' }
  ]
}

// 是否可以发送短信验证码
const canSendSmsCode = computed(() => {
  return smsLoginForm.phone && 
         /^1[3-9]\d{9}$/.test(smsLoginForm.phone) &&
         (!showCaptchaForSms.value || smsLoginForm.captcha) &&
         smsCountdown.value === 0
})

// 表单是否有效（协议校验在父组件 LoginModal 中处理）
const isFormValid = computed(() => {
  return smsLoginForm.phone && 
         smsLoginForm.smsCode && 
         smsLoginForm.smsCode.length === 6 &&
         (!showCaptchaForSms.value || smsLoginForm.captcha)
})

// 发送短信验证码
const handleSendSmsCode = async () => {
  if (!smsLoginFormRef.value) return

  try {
    // 校验手机号
    await smsLoginFormRef.value.validateField('phone')
    
    // 如果需要图形验证码，先校验
    if (showCaptchaForSms.value) {
      await smsLoginFormRef.value.validateField('captcha')
      if (!smsLoginForm.captcha || !captchaKey.value) {
        ElMessage.warning(t('auth.captchaRequired'))
        return
      }
    }

    sendingSmsCode.value = true

    // 调用发送短信验证码接口
    // 只有在需要图形验证码时才传递 captcha 和 captchaKey
    const requestData = {
      phone: smsLoginForm.phone
    }
    if (showCaptchaForSms.value && smsLoginForm.captcha && captchaKey.value) {
      requestData.captcha = smsLoginForm.captcha
      requestData.captchaKey = captchaKey.value
    }
    await sendSmsVerificationCode(requestData)

    ElMessage.success(t('auth.smsCodeSent'))
    
    // 启动倒计时
    startSmsCountdown()
    
    // 发送成功后清除图形验证码（如果需要）
    if (showCaptchaForSms.value) {
      smsLoginForm.captcha = ''
      captchaKey.value = ''
      showCaptchaForSms.value = false
    }

  } catch (error) {
    // 发送失败3次后显示图形验证码
    smsFailCount.value++
    if (smsFailCount.value >= 3) {
      showCaptchaForSms.value = true
      await refreshCaptcha()
    }
    
    // 🔥 修复：如果错误信息已经在请求拦截器中显示过，不再重复显示
    if (!error._messageShown) {
      const errorMessage = error.response?.data?.message || t('auth.smsCodeSendFailed')
      ElMessage.error(errorMessage)
    }
  } finally {
    sendingSmsCode.value = false
  }
}

// 启动短信验证码倒计时
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

// 处理短信登录
const handleSmsLogin = async () => {
  if (!smsLoginFormRef.value) return
  
  // 先触发登录尝试事件，让父组件检查协议
  emit('login-attempt')
  
  // 检查协议是否勾选（由父组件通过 props 传递）
  if (!props.agreementChecked) {
    ElMessage.warning(t('auth.register.agreementRequired'))
    return
  }
  
  try {
    await smsLoginFormRef.value.validate()
    loading.value = true

    const response = await authStore.login({
      loginType: 'sms',
      phone: smsLoginForm.phone,
      smsCode: smsLoginForm.smsCode,
      rememberMe: smsLoginForm.rememberMe
    })

    emit('login-success', response.data)
    
  } catch (error) {
    // 🔥 修复：如果错误信息已经在请求拦截器中显示过，不再重复显示
    if (!error._messageShown) {
      const errorMessage = error.response?.data?.message || t('auth.loginFailed')
      ElMessage.error(errorMessage)
    }
  } finally {
    loading.value = false
  }
}

// 刷新图形验证码
const refreshCaptcha = async () => {
  try {
    const response = await getCaptcha()
    captchaUrl.value = response.data.captchaImage || response.data.captchaUrl
    captchaKey.value = response.data.captchaKey || ''
  } catch (error) {
    // 验证码获取失败，静默处理
  }
}

// 处理键盘事件（Enter 键提交登录）
const handleKeyDown = (event) => {
  if (event.key === 'Enter' && isFormValid.value && !loading.value) {
    handleSmsLogin()
  }
}

// 组件挂载时初始化
onMounted(() => {
  const rememberedFlag = localStorage.getItem('auth_remember_me')
  const defaultRemember = rememberedFlag === null ? true : rememberedFlag === '1'
  smsLoginForm.rememberMe = authStore.rememberMe ?? defaultRemember
  authStore.rememberMe = smsLoginForm.rememberMe
  
  // 为表单添加键盘事件监听
  document.addEventListener('keydown', handleKeyDown)
})

// 组件卸载时清理
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  if (smsCountdownTimer) {
    clearInterval(smsCountdownTimer)
    smsCountdownTimer = null
  }
})

// 暴露重置方法供父组件调用
defineExpose({
  resetForm: () => {
    smsLoginForm.phone = ''
    smsLoginForm.smsCode = ''
    smsLoginForm.captcha = ''
    smsLoginForm.rememberMe = false
    showCaptchaForSms.value = false
    smsFailCount.value = 0
    smsCountdown.value = 0
    if (smsCountdownTimer) {
      clearInterval(smsCountdownTimer)
      smsCountdownTimer = null
    }
    if (smsLoginFormRef.value) {
      smsLoginFormRef.value.clearValidate()
    }
  }
})
</script>

<style lang="scss" scoped>
.sms-login-form {
  width: 100%;
  
  // 手机号输入框样式
  :deep(.phone-input) {
    .el-input__wrapper {
      padding-left: 100px;
    }
    
    .phone-icon {
      position: absolute;
      left: 16px;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-3);
      font-size: 16px;
      margin-right: 8px;
    }
    
    .phone-prefix {
      position: absolute;
      left: 40px;
      top: 50%;
      transform: translateY(-50%);
      color: var(--el-input-text-color, var(--el-text-color-regular));
      font-size: 14px;
      font-weight: 500;
      border-right: 1px solid var(--border);
      padding-right: 12px;
      margin-right: 8px;
    }
  }
  
  // 验证码输入框样式
  :deep(.sms-code-input) {
    .sms-icon {
      color: var(--text-3);
      font-size: 16px;
    }
  }
  
  .captcha-container {
    display: flex;
    gap: var(--gap-sm);
    
    .el-input {
      flex: 1;
    }
    
    .captcha-image {
      width: 120px;
      height: 40px;
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      background: var(--bg);
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: var(--radius-sm);
      }
      
      span {
        font-size: 12px;
        color: var(--text-3);
      }
      
      &:hover {
        border-color: var(--color-primary);
      }
    }
  }
  
  .sms-code-container {
    display: flex;
    gap: 12px;
    align-items: flex-start;
    
    .sms-code-input {
      flex: 1;
    }
    
    .send-sms-button {
      min-width: 120px;
      flex-shrink: 0;
      white-space: nowrap;
    }
  }
  
  .login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
  
  // 优化输入框整体样式，参考图2的设计
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    
    &:hover {
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
    }
    
    &.is-focus {
      box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.2);
    }
  }
}
</style>

