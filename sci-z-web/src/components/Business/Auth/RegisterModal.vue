<!--
/**
 * @description 注册弹窗组件
 * 以弹窗形式显示注册表单，背景虚化
 */
-->
<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="register-modal-overlay">
        <div class="register-modal-wrapper">
          <!-- 关闭按钮 -->
          <button class="register-modal-close" @click="handleClose">
            <el-icon><Close /></el-icon>
          </button>
          
          <!-- 注册卡片内容 -->
          <div class="register-modal-content">
            <!-- Logo 区域 -->
            <div class="logo-section">
              <img src="@/assets/images/logo.svg" alt="Logo" />
              <h1>{{ $t('auth.register.title') }}</h1>
              <p>{{ $t('auth.register.subtitle') }}</p>
            </div>
            
            <!-- 表单区域 -->
            <div class="form-section">
              <RegisterForm @register-success="handleRegisterSuccess" />
            </div>
            
            <!-- 底部区域 -->
            <div class="login-section">
              <span>{{ $t('auth.register.haveAccount') }}</span>
              <el-button type="text" @click="handleGoToLogin">
                {{ $t('auth.register.loginNow') }}
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
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { RegisterForm } from '@/components/Business/Auth'
import { useAuthModal } from '@/composables/useAuthModal'
import { useAuthStore } from '@/store/modules/auth'

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
const { closeRegisterModal, openLoginModal } = useAuthModal()

// 控制弹窗显示
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 🔥 监听登录状态，登录成功后自动关闭弹窗
watch(() => authStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn && visible.value) {
    handleClose()
  }
})

// 处理关闭弹窗
const handleClose = () => {
  visible.value = false
  closeRegisterModal()
  // 如果当前路由是 /register，返回到之前的页面
  if (route.path === '/register') {
    router.back()
  }
}

// 处理注册成功
const handleRegisterSuccess = (userData) => {
  ElMessage.success(t('auth.register.registerSuccess'))
  handleClose()
  // 注册成功后，打开登录弹窗
  openLoginModal()
}

// 处理跳转到登录
const handleGoToLogin = () => {
  handleClose()
  // 🔥 修复：跳转到 /login 路由并打开登录弹窗（与登录按钮行为一致）
  router.push('/login').then(() => {
    openLoginModal()
  }).catch(() => {
    // 如果路由跳转失败，直接打开弹窗
    openLoginModal()
  })
}
</script>

<style lang="scss" scoped>
// 弹窗遮罩层（背景虚化）
.register-modal-overlay {
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
.register-modal-wrapper {
  position: relative;
  width: 100%;
  max-width: 520px;
  margin: 20px;
}

// 关闭按钮
.register-modal-close {
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

// 注册卡片内容
.register-modal-content {
  padding: 32px 28px 28px;
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
  margin-bottom: 22px;
  flex-shrink: 0;
  
  img {
    width: 160px;
    height: auto;
    margin-bottom: 14px;
  }
  
  h1 {
    font-size: 26px;
    font-weight: 700;
    color: #1e3a8a;
    margin: 0 0 8px 0;
  }
  
  p {
    font-size: 14px;
    color: #64748b;
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

.login-section {
  text-align: center;
  padding-top: 20px;
  margin-top: 20px;
  border-top: 1px solid #e5e7eb;
  color: #64748b;
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
@media (max-width: 640px) {
  .register-modal-wrapper {
    max-width: 100%;
    margin: 16px;
  }
  
  .register-modal-content {
    padding: 26px 18px;
  }
  
  .logo-section {
    img {
      width: 140px;
      height: auto;
    }
    
    h1 {
      font-size: 22px;
    }
  }
  
  .register-modal-close {
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

