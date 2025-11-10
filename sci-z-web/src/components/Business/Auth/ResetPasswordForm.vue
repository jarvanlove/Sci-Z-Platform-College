<!--
/**
 * @description 重置密码表单组件
 * 基于原型图设计的重置密码表单，包含邮箱、验证码、新密码等功能
 */
-->
<template>
  <el-form
    ref="resetFormRef"
    :model="resetForm"
    :rules="resetRules"
    class="reset-form"
    autocomplete="off"
    @submit.prevent="handleReset"
  >
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

    <!-- 验证码输入 -->
    <el-form-item prop="captcha">
      <div class="captcha-container">
        <el-input
          v-model="resetForm.captcha"
          :placeholder="$t('auth.captcha')"
          size="large"
          prefix-icon="Picture"
          clearable
          autocomplete="off"
          name="reset-captcha"
        />
        <div class="captcha-image" @click="refreshCaptcha">
          <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
          <span v-else>{{ $t('common.loading') }}</span>
        </div>
      </div>
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
          autocomplete="one-time-code"
          name="reset-email-code"
        />
        <BaseButton
          type="primary"
          :disabled="!canSendEmailCode || emailCodeCountdown > 0"
          @click="sendEmailCode"
        >
          {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : $t('auth.sendCode') }}
        </BaseButton>
      </div>
    </el-form-item>

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
        {{ $t('auth.resetPassword') }}
      </BaseButton>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { BaseButton } from '@/components/Common'
import { resetPassword, getCaptcha, sendEmailCode as sendEmailCodeApi } from '@/api/Auth'

// 定义事件
const emit = defineEmits(['reset-success', 'go-to-login'])

// 路由和国际化
const router = useRouter()
const { t } = useI18n()

// 表单引用
const resetFormRef = ref()

// 加载状态
const loading = ref(false)

// 验证码相关
const captchaUrl = ref('')

// 邮箱验证码相关
const emailCodeCountdown = ref(0)
let emailCodeTimer = null
const canSendEmailCode = computed(() => {
  return (
    resetForm.email &&
    resetForm.captcha &&
    resetForm.captchaKey &&
    emailCodeCountdown.value === 0
  )
})

// 表单数据
const resetForm = reactive({
  email: '',
  captcha: '',
  captchaKey: '', // 图形验证码唯一标识
  emailCode: '',
  newPassword: '',
  confirmPassword: ''
})

const blankFormFields = {
  email: '',
  captcha: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: ''
}

const clearFormValues = (preserveCaptchaKey = true) => {
  const currentCaptchaKey = preserveCaptchaKey ? resetForm.captchaKey : ''
  Object.assign(resetForm, {
    ...blankFormFields,
    captchaKey: currentCaptchaKey
  })
  if (resetFormRef.value && typeof resetFormRef.value.clearValidate === 'function') {
    resetFormRef.value.clearValidate()
  }
}

// 表单验证规则
const resetRules = {
  email: [
    { required: true, message: t('auth.email'), trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: t('auth.captcha'), trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: t('auth.emailCode'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('auth.newPassword'), trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('auth.confirmPassword'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 表单是否有效
const isFormValid = computed(() => {
  return resetForm.email && 
         resetForm.captcha && 
         resetForm.emailCode && 
         resetForm.newPassword && 
         resetForm.confirmPassword
})

// 处理重置密码
const handleReset = async () => {
  if (!resetFormRef.value) return
  
  try {
    await resetFormRef.value.validate()
    loading.value = true

    const response = await resetPassword({
      email: resetForm.email,
      captcha: resetForm.captcha,
      captchaKey: resetForm.captchaKey, // 传递验证码唯一标识
      emailCode: resetForm.emailCode,
      newPassword: resetForm.newPassword
    })

    // 重置成功
    ElMessage.success(t('auth.resetSuccess'))
    emit('reset-success', response.data)
    
    // 跳转到登录页面
    router.push('/login')
    
  } catch (error) {
    console.error('重置密码失败:', error)
    
    // 显示错误信息
    const errorMessage = error.response?.data?.message || t('auth.resetFailed')
    ElMessage.error(errorMessage)
    
    // 刷新验证码
    await refreshCaptcha()
    
  } finally {
    loading.value = false
  }
}

// 处理跳转到登录
const handleGoToLogin = () => {
  emit('go-to-login')
  router.push('/login')
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!canSendEmailCode.value) return
  
  try {
    const email = (resetForm.email || '').trim()
    const captcha = (resetForm.captcha || '').trim()
    resetForm.email = email
    resetForm.captcha = captcha
    await sendEmailCodeApi({
      email,
      captcha,
      captchaKey: resetForm.captchaKey
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
    const errorMessage = error.response?.data?.message || t('auth.sendCodeFailed')
    ElMessage.error(errorMessage)
    
    // 刷新验证码
    await refreshCaptcha()
  }
}

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await getCaptcha()
    // 根据后端 CaptchaResp 定义，使用 captchaImage 和 captchaKey
    captchaUrl.value = response.data.captchaImage || response.data.captchaUrl // 兼容两种字段名
    resetForm.captchaKey = response.data.captchaKey || ''
    resetForm.captcha = ''
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

// 组件挂载时初始化
onMounted(async () => {
  clearFormValues(false)
  await refreshCaptcha()
  await nextTick()
  // 避免浏览器自动填充继承登录信息
  requestAnimationFrame(() => {
    clearFormValues(true)
  })
})

onUnmounted(() => {
  if (emailCodeTimer) {
    clearInterval(emailCodeTimer)
    emailCodeTimer = null
  }
})
</script>

<style lang="scss" scoped>
.reset-form {
  width: 100%;
  
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
  
  .email-code-container {
    display: flex;
    gap: var(--gap-sm);
    
    .el-input {
      flex: 1;
    }
  }
  
  .reset-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
  
  .login-link {
    text-align: center;
    color: var(--text-2);
    font-size: 14px;
  }
}
</style>
