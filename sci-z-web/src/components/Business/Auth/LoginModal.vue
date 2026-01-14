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
                @login-success="handleLoginSuccess"
                @forgot-password="handleForgotPassword"
              />
            </div>
            
            <!-- 底部区域 -->
            <div class="register-section">
              <span>{{ $t('auth.noAccount') }}</span>
              <el-button type="text" @click="handleGoToRegister">
                {{ $t('auth.registerNow') }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, watch } from 'vue'
import { Teleport, Transition } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { LoginForm } from '@/components/Business/Auth'
import { useAuthStore } from '@/store/modules/auth'
import { useIndustryStore } from '@/store/modules/industry'
import { useLoginModal } from '@/composables/useLoginModal'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const industryStore = useIndustryStore()
const { closeLoginModal } = useLoginModal()

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
    
    // 打印调试信息（开发环境）
    if (import.meta.env.DEV) {
      console.log('✅ 登录成功，弹窗已关闭')
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
  router.push('/reset-password')
}

// 处理跳转到注册
const handleGoToRegister = () => {
  handleClose()
  router.push('/register')
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
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 24px 72px rgba(0, 0, 0, 0.35);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
  
  img {
    width: 120px;
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
  margin-bottom: 24px;
}

.register-section {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 14px;
  
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
      width: 80px;
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

