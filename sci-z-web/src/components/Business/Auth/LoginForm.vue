<!--
/**
 * @description 登录表单组件
 * 支持账号密码登录和短信验证码登录两种方式
 */
-->
<template>
  <div class="login-form-wrapper">
    <!-- 登录方式切换 Tab -->
    <el-tabs v-model="activeTab" class="login-tabs" @tab-change="handleTabChange">
      <el-tab-pane :label="$t('auth.smsLogin')" name="sms">
        <SmsLoginForm
          ref="smsFormRef"
          :agreement-checked="props.smsAgreement"
          @login-success="handleLoginSuccess"
          @login-attempt="handleSmsLoginAttempt"
        />
      </el-tab-pane>
      <el-tab-pane :label="$t('auth.passwordLogin')" name="password">
        <PasswordLoginForm
          ref="passwordFormRef"
          @login-success="handleLoginSuccess"
          @forgot-password="handleForgotPassword"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PasswordLoginForm from './PasswordLoginForm.vue'
import SmsLoginForm from './SmsLoginForm.vue'

// 定义 props（接收父组件传递的协议状态）
const props = defineProps({
  smsAgreement: {
    type: Boolean,
    default: false
  },
  activeTab: {
    type: String,
    default: 'sms'
  }
})

// 定义 emits
const emit = defineEmits(['update:activeTab', 'login-success', 'forgot-password', 'tab-change', 'sms-login-attempt'])

// 路由和国际化
const router = useRouter()
const { t } = useI18n()

// Tab 切换（使用 v-model 与父组件同步）
const activeTab = ref(props.activeTab)

// 监听 props 变化，同步 activeTab
watch(() => props.activeTab, (newVal) => {
  activeTab.value = newVal
})

// 表单引用
const passwordFormRef = ref()
const smsFormRef = ref()

// 暴露 activeTab 给父组件
defineExpose({
  activeTab
})

// 处理 Tab 切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  // 同步到父组件
  emit('update:activeTab', tabName)
  // 切换 Tab 时重置表单
  if (tabName === 'password' && passwordFormRef.value?.resetForm) {
    passwordFormRef.value.resetForm()
  } else if (tabName === 'sms' && smsFormRef.value?.resetForm) {
    smsFormRef.value.resetForm()
  }
  // 通知父组件 Tab 切换
  emit('tab-change', tabName)
}

// 处理登录成功
const handleLoginSuccess = (userData) => {
  emit('login-success', userData)
}

// 处理短信登录尝试（用于协议校验）
const handleSmsLoginAttempt = () => {
  emit('sms-login-attempt')
}

// 处理忘记密码
const handleForgotPassword = () => {
  emit('forgot-password')
  router.push('/reset-password')
}
</script>

<style lang="scss" scoped>
.login-form-wrapper {
  width: 100%;
  
  .login-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 24px;
    }
    
    :deep(.el-tabs__item) {
      font-size: 16px;
      font-weight: 500;
      padding: 0 20px;
      color: var(--text-2);
      transition: color 0.2s ease;
      
      &:hover {
        color: var(--color-primary);
      }
      
      &.is-active {
        color: var(--color-primary);
        font-weight: 600;
      }
    }
    
    :deep(.el-tabs__active-bar) {
      height: 3px;
      background-color: var(--color-primary);
    }
    
    // 确保激活的 tab 使用主题色
    :deep(.el-tabs__item.is-active) {
      color: var(--color-primary) !important;
    }
  }
}
</style>
