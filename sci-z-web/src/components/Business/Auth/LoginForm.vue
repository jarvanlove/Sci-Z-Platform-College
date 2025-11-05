<!--
/**
 * @description 登录表单组件
 * 基于原型图设计的登录表单，包含用户名、密码、验证码、记住我等功能
 */
-->
<template>
  <el-form
    ref="loginFormRef"
    :model="loginForm"
    :rules="loginRules"
    class="login-form"
    @submit.prevent="handleLogin"
  >
    <!-- 用户名输入 -->
    <el-form-item prop="username">
      <el-input
        v-model="loginForm.username"
        :placeholder="$t('auth.username')"
        size="large"
        prefix-icon="User"
        clearable
      />
    </el-form-item>

    <!-- 密码输入 -->
    <el-form-item prop="password">
      <el-input
        v-model="loginForm.password"
        type="password"
        :placeholder="$t('auth.password')"
        size="large"
        prefix-icon="Lock"
        show-password
        clearable
      />
    </el-form-item>

    <!-- 验证码输入（失败3次后显示） -->
    <el-form-item v-if="showCaptcha" prop="captcha">
      <div class="captcha-container">
        <el-input
          v-model="loginForm.captcha"
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

    <!-- 记住我和忘记密码 -->
    <el-form-item>
      <div class="form-options">
        <el-checkbox v-model="loginForm.rememberMe">
          {{ $t('auth.rememberMe') }}
        </el-checkbox>
        <el-button type="text" @click="handleForgotPassword">
          {{ $t('auth.forgotPassword') }}
        </el-button>
      </div>
    </el-form-item>

    <!-- 登录按钮 -->
    <el-form-item>
      <BaseButton
        type="primary"
        size="large"
        :loading="loading"
        :disabled="!isFormValid"
        class="login-button"
        @click="handleLogin"
      >
        {{ $t('auth.login') }}
      </BaseButton>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { BaseButton } from '@/components/Common'
import { getCaptcha } from '@/api/Auth'
import { useAuthStore } from '@/store/modules/auth'

// 定义事件
const emit = defineEmits(['login-success', 'forgot-password'])

// 路由和国际化
const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref()

// 加载状态
const loading = ref(false)

// 验证码相关
const showCaptcha = ref(false)
const captchaUrl = ref('')
const loginFailCount = ref(0)

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: t('auth.username'), trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.password'), trigger: 'blur' },
    { min: 3, max: 20, message: '密码长度为3-20个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: t('auth.captcha'), trigger: 'blur' }
  ]
}

// 表单是否有效
const isFormValid = computed(() => {
  return loginForm.username && loginForm.password && (!showCaptcha.value || loginForm.captcha)
})

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      rememberMe: loginForm.rememberMe
    })

    // 登录成功
    ElMessage.success(t('auth.loginSuccess'))
    emit('login-success', response.data)
    
  } catch (error) {
    // 错误日志已在auth store中记录
    
    // 增加失败次数
    loginFailCount.value++
    
    // 失败3次后显示验证码
    if (loginFailCount.value >= 3) {
      showCaptcha.value = true
      await refreshCaptcha()
    }
    
    // 显示错误信息
    const errorMessage = error.response?.data?.message || t('auth.loginFailed')
    ElMessage.error(errorMessage)
    
  } finally {
    loading.value = false
  }
}

// 处理忘记密码
const handleForgotPassword = () => {
  emit('forgot-password')
  router.push('/reset-password')
}

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await getCaptcha()
    captchaUrl.value = response.data.captchaUrl
  } catch (error) {
    // 验证码获取失败，静默处理
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 可以在这里添加一些初始化逻辑
})
</script>

<style lang="scss" scoped>
.login-form {
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
  
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  
  .login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}
</style>
