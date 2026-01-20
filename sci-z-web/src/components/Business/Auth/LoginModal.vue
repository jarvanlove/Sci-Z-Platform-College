<!--
/**
 * @description 登录弹窗组件
 * 以弹窗形式显示登录表单，背景虚化
 */
-->
<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="login-modal-overlay">
        <div class="login-modal-wrapper">
          <!-- 关闭按钮 -->
          <button class="login-modal-close" @click="handleClose">
            <el-icon><Close /></el-icon>
          </button>
          
          <!-- 登录卡片内容 -->
          <div class="login-modal-content">
            <!-- Logo 区域 -->
            <div class="logo-section">
              <img src="@/assets/images/logo.svg" alt="Logo" />
              <h1>{{ $t('app.title') }}</h1>
              <p>{{ $t('auth.welcomeBack') }}</p>
            </div>
            
            <!-- 表单区域 -->
            <div class="form-section">
              <LoginForm 
                ref="loginFormRef"
                :active-tab="activeTab"
                :sms-agreement="smsAgreement"
                @update:active-tab="activeTab = $event"
                @login-success="handleLoginSuccess"
                @forgot-password="handleForgotPassword"
                @tab-change="handleTabChange"
                @sms-login-attempt="handleSmsLoginAttempt"
              />
            </div>
            
            <!-- 底部区域 - 根据当前 Tab 显示不同内容 -->
            <div class="register-section">
              <!-- 短信登录时显示：用户协议和隐私政策 -->
              <template v-if="activeTab === 'sms'">
                <AgreementNotice
                  v-model="smsAgreement"
                  :is-sms-login="true"
                  @view-user-agreement="showUserAgreement"
                  @view-privacy-policy="showPrivacyPolicy"
                />
              </template>
              <!-- 账号登录时显示：还没有账号？立即注册 -->
              <template v-else>
                <span>{{ $t('auth.noAccount') }}</span>
                <el-button type="text" @click="handleGoToRegister">
                  {{ $t('auth.registerNow') }}
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, watch, ref } from 'vue'
import { Teleport, Transition } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { LoginForm } from '@/components/Business/Auth'
import AgreementNotice from '@/components/Common/AgreementNotice.vue'
import { useAuthStore } from '@/store/modules/auth'
import { useIndustryStore } from '@/store/modules/industry'
import { useAuthModal } from '@/composables/useAuthModal'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const authStore = useAuthStore()
const industryStore = useIndustryStore()
const { closeLoginModal, openRegisterModal, openResetPasswordModal } = useAuthModal()

// 当前激活的 Tab（默认短信登录）
const activeTab = ref('sms')
const loginFormRef = ref(null)

// 短信登录时的协议勾选状态
const smsAgreement = ref(false)

// 控制弹窗显示
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 监听登录状态，登录成功后自动关闭弹窗
watch(() => authStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn && visible.value) {
    handleClose()
  }
})

// 处理关闭弹窗
const handleClose = () => {
  visible.value = false
  closeLoginModal()
  // 🔥 修复：如果当前路由是 /login 且用户未登录，跳转到 AI 对话页面（避免停留在登录页面）
  if (route.path === '/login' && !authStore.isLoggedIn) {
    router.push('/ai/chat').catch(() => {
      // 忽略路由冗余导航错误
    })
  }
}

// 处理登录成功
const handleLoginSuccess = async (userData) => {
  try {
    // 显示登录成功消息
    ElMessage.success(t('auth.loginSuccess'))
    
    // 等待权限初始化完成，并预热行业配置
    await Promise.all([
      authStore.initPermissions(),
      industryStore.fetchIndustryConfig()
    ])
    
    // 关闭弹窗
    handleClose()
    
    // 🔥 修复：登录成功后跳转到 AI 对话页面
    const redirect = route.query.redirect
    const targetPath = redirect && redirect !== '/login' ? redirect : '/ai/chat'
    await router.push(targetPath)
    
    // 打印调试信息（开发环境）
    if (import.meta.env.DEV) {
      console.log('✅ 登录成功，已跳转到:', targetPath)
      console.log('📊 用户权限:', authStore.permissions)
      console.log('📋 用户菜单:', authStore.menus)
    }
  } catch (error) {
    console.error('登录后处理失败:', error)
    ElMessage.error('登录成功但初始化失败，请刷新页面')
  }
}

// 处理忘记密码
const handleForgotPassword = () => {
  handleClose()
  // 更新路由并打开重置密码弹窗
  router.push('/reset-password').then(() => {
    openResetPasswordModal()
  })
}

// 处理 Tab 切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  // 切换 Tab 时重置协议勾选状态
  if (tabName === 'sms') {
    smsAgreement.value = false
  }
}

// 处理短信登录尝试（检查协议）
const handleSmsLoginAttempt = () => {
  // 协议校验在 SmsLoginForm 中通过 props 进行，这里不需要额外处理
  // 如果协议未勾选，SmsLoginForm 会显示提示并阻止登录
}

// 查看用户协议
const showUserAgreement = () => {
  ElMessageBox.alert(t('legal.userAgreementContent'), t('legal.userAgreement'), {
    confirmButtonText: t('common.confirm'),
    customClass: 'agreement-modal',
    dangerouslyUseHTMLString: true,
    callback: () => {}
  })
}

// 查看隐私政策
const showPrivacyPolicy = () => {
  ElMessageBox.alert(t('legal.privacyPolicyContent'), t('legal.privacyPolicy'), {
    confirmButtonText: t('common.confirm'),
    customClass: 'agreement-modal',
    dangerouslyUseHTMLString: true,
    callback: () => {}
  })
}

// 处理跳转到注册
const handleGoToRegister = () => {
  handleClose()
  // 更新路由并打开注册弹窗
  router.push('/register').then(() => {
    openRegisterModal()
  })
}
</script>

<style lang="scss" scoped>
// 弹窗遮罩层（背景虚化）
.login-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  animation: fadeIn 0.3s ease;
}

// 弹窗包装器
.login-modal-wrapper {
  position: relative;
  width: 100%;
  max-width: 420px;
  margin: 20px;
}

// 关闭按钮
.login-modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: #f3f4f6;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 18px;
  z-index: 10;
  transition: all 0.2s ease;

  &:hover {
    background: #e5e7eb;
    color: #374151;
  }

  &:active {
    background: #d1d5db;
  }
}

// 登录卡片内容
.login-modal-content {
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 24px 72px rgba(0, 0, 0, 0.35);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
  
  img {
    width: 160px;
    height: auto;
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    color: #1e3a8a;
    margin: 0 0 8px 0;
  }
  
  p {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
  }
}

.form-section {
  margin-bottom: 0;
  min-height: 0;
  flex: 1;
  overflow-y: auto;
  padding-bottom: 8px;
}

.register-section {
  text-align: center;
  padding-top: 20px;
  margin-top: 20px;
  border-top: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 14px;
  flex-shrink: 0;
  
  .el-button {
    color: #1e3a8a;
    font-weight: 500;
    padding: 0 4px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 淡入动画
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

// 响应式设计
@media (max-width: 480px) {
  .login-modal-wrapper {
    max-width: 100%;
    margin: 16px;
  }
  
  .login-modal-content {
    padding: 24px;
  }
  
  .logo-section {
    img {
      width: 120px;
    }
    
    h1 {
      font-size: 20px;
    }
  }
  
  .login-modal-close {
    top: 12px;
    right: 12px;
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

