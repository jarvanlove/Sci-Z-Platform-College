<!--
/**
 * @description 注册表单组件
 * 基于原型图设计的注册表单，包含用户名、邮箱、密码、确认密码等功能
 */
-->
<template>
  <el-form
    ref="registerFormRef"
    :model="registerForm"
    :rules="registerRules"
    class="register-form"
    @submit.prevent="handleRegister"
  >
    <!-- 用户名输入 -->
    <el-form-item prop="username">
      <el-input
        v-model="registerForm.username"
        :placeholder="$t('auth.username')"
        size="large"
        prefix-icon="User"
        clearable
      />
    </el-form-item>

    <!-- 邮箱输入 -->
    <el-form-item prop="email">
      <el-input
        v-model="registerForm.email"
        :placeholder="$t('auth.email')"
        size="large"
        prefix-icon="Message"
        clearable
      />
    </el-form-item>

    <!-- 密码输入 -->
    <el-form-item prop="password">
      <el-input
        v-model="registerForm.password"
        type="password"
        :placeholder="$t('auth.password')"
        size="large"
        prefix-icon="Lock"
        show-password
        clearable
      />
    </el-form-item>

    <!-- 确认密码输入 -->
    <el-form-item prop="confirmPassword">
      <el-input
        v-model="registerForm.confirmPassword"
        type="password"
        :placeholder="$t('auth.confirmPassword')"
        size="large"
        prefix-icon="Lock"
        show-password
        clearable
      />
    </el-form-item>

    <!-- 验证码输入 -->
    <el-form-item prop="captcha">
      <div class="captcha-container">
        <el-input
          v-model="registerForm.captcha"
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

    <!-- 用户协议 -->
    <el-form-item prop="agreement">
      <el-checkbox v-model="registerForm.agreement">
        {{ $t('auth.agreeTo') }}
        <el-button type="text" @click="showUserAgreement">
          {{ $t('auth.userAgreement') }}
        </el-button>
        {{ $t('auth.and') }}
        <el-button type="text" @click="showPrivacyPolicy">
          {{ $t('auth.privacyPolicy') }}
        </el-button>
      </el-checkbox>
    </el-form-item>

    <!-- 注册按钮 -->
    <el-form-item>
      <BaseButton
        type="primary"
        size="large"
        :loading="loading"
        :disabled="!isFormValid"
        class="register-button"
        @click="handleRegister"
      >
        {{ $t('auth.register') }}
      </BaseButton>
    </el-form-item>

    <!-- 登录链接 -->
    <el-form-item>
      <div class="login-link">
        {{ $t('auth.alreadyHaveAccount') }}
        <el-button type="text" @click="handleGoToLogin">
          {{ $t('auth.login') }}
        </el-button>
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { BaseButton } from '@/components/Common'
import { register, getCaptcha } from '@/api/Auth'

// 定义事件
const emit = defineEmits(['register-success', 'go-to-login'])

// 路由和国际化
const router = useRouter()
const { t } = useI18n()

// 表单引用
const registerFormRef = ref()

// 加载状态
const loading = ref(false)

// 验证码相关
const captchaUrl = ref('')

// 表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  agreement: false
})

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: t('auth.username'), trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('auth.email'), trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.password'), trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('auth.confirmPassword'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  captcha: [
    { required: true, message: t('auth.captcha'), trigger: 'blur' }
  ],
  agreement: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请同意用户协议和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 表单是否有效
const isFormValid = computed(() => {
  return registerForm.username && 
         registerForm.email && 
         registerForm.password && 
         registerForm.confirmPassword && 
         registerForm.captcha && 
         registerForm.agreement
})

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true

    const response = await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      captcha: registerForm.captcha
    })

    // 注册成功
    ElMessage.success(t('auth.registerSuccess'))
    emit('register-success', response.data)
    
    // 跳转到登录页面
    router.push('/login')
    
  } catch (error) {
    console.error('注册失败:', error)
    
    // 显示错误信息
    const errorMessage = error.response?.data?.message || t('auth.registerFailed')
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

// 显示用户协议
const showUserAgreement = () => {
  // 这里可以打开用户协议弹窗或跳转到协议页面
  ElMessage.info('用户协议功能待实现')
}

// 显示隐私政策
const showPrivacyPolicy = () => {
  // 这里可以打开隐私政策弹窗或跳转到政策页面
  ElMessage.info('隐私政策功能待实现')
}

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await getCaptcha()
    captchaUrl.value = response.data.captchaUrl
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

// 组件挂载时初始化
onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="scss" scoped>
.register-form {
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
  
  .register-button {
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
