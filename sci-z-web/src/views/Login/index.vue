<!--
/**
 * @description 登录页面
 * 用户身份认证页面，提供登录功能
 * 基于原型图设计的科技感登录界面
 */
-->
<template>
  <BaseScrollbar class="login-page-scrollbar" size="medium">
    <div class="login-container">
      <!-- 科技感背景 -->
      <div class="bg-scape"></div>
      
      <!-- 登录卡片 -->
      <BaseCard class="login-card">
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
      </BaseCard>
    </div>
  </BaseScrollbar>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { BaseCard, BaseScrollbar } from '@/components/Common'
import { LoginForm } from '@/components/Business/Auth'
import { useAuthStore } from '@/store/modules/auth'

// 路由和国际化
const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()

// 处理登录成功
const handleLoginSuccess = (userData) => {
  ElMessage.success(t('auth.loginSuccess'))
  
  // 跳转到仪表板
  router.push('/dashboard')
}

// 处理忘记密码
const handleForgotPassword = () => {
  router.push('/reset-password')
}

// 处理跳转到注册
const handleGoToRegister = () => {
  router.push('/register')
}
</script>

<style lang="scss" scoped>
.login-page-scrollbar {
  height: 100vh;
}

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg) 0%, var(--surface) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--gap-lg);
  position: relative;
  overflow: hidden;
}

/* 背景景观层：SVG几何+电路图+聚焦光晕+暗角 */
.bg-scape {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background-image: 
    url("data:image/svg+xml,%3Csvg width='220' height='220' viewBox='0 0 220 220' xmlns='http://www.w3.org/2000/svg'%3E%3Cg stroke='%2390a4ae' stroke-opacity='0.18' stroke-width='1' fill='none'%3E%3Cpath d='M10 40 L60 20 L90 70 L10 40 M140 40 L180 20 L210 70 L140 40 M70 140 L110 110 L160 160 L70 140'/%3E%3Ccircle cx='60' cy='20' r='2' fill='%2390a4ae' fill-opacity='0.25'/%3E%3Ccircle cx='90' cy='70' r='2' fill='%2390a4ae' fill-opacity='0.25'/%3E%3Ccircle cx='110' cy='110' r='2' fill='%2390a4ae' fill-opacity='0.25'/%3E%3C/g%3E%3C/svg%3E"),
    url("data:image/svg+xml,%3Csvg width='260' height='260' viewBox='0 0 260 260' xmlns='http://www.w3.org/2000/svg'%3E%3Cg stroke='%2389a6d1' stroke-opacity='0.12' stroke-width='1' fill='none' stroke-linecap='round'%3E%3Cpath d='M20 30 h60 v40 h40 v-20 h60'/%3E%3Cpath d='M30 200 h80 v-30 h50 v-40 h50'/%3E%3Ccircle cx='80' cy='70' r='3' fill='%2389a6d1' fill-opacity='0.18'/%3E%3Ccircle cx='160' cy='90' r='3' fill='%2389a6d1' fill-opacity='0.18'/%3E%3C/g%3E%3C/svg%3E"),
    radial-gradient(
      ellipse at 50% 42%,
      rgba(255, 255, 255, 0.75) 0%,
      rgba(255, 255, 255, 0.55) 22%,
      rgba(255, 255, 255, 0.25) 46%,
      rgba(255, 255, 255, 0) 62%
    ),
    radial-gradient(
      circle at 50% 50%,
      rgba(15, 23, 42, 0.06) 0%,
      rgba(15, 23, 42, 0.1) 65%,
      rgba(15, 23, 42, 0.16) 100%
    );
  background-size: 460px 460px, 520px 520px, 100% 100%, 100% 100%;
  background-position: left -80px top -40px, right -100px bottom -60px, center, center;
  animation: bgPan 40s linear infinite;
}

@keyframes bgPan {
  0% {
    background-position: left -80px top -40px, right -100px bottom -60px, center, center;
  }
  50% {
    background-position: left -40px top -20px, right -60px bottom -30px, center, center;
  }
  100% {
    background-position: left -80px top -40px, right -100px bottom -60px, center, center;
  }
}

/* SVG科技背景 */
.login-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3Cpattern id='grid' width='20' height='20' patternUnits='userSpaceOnUse'%3E%3Cpath d='M 20 0 L 0 0 0 20' fill='none' stroke='%23e2e8f0' stroke-width='0.5'/%3E%3C/pattern%3E%3C/defs%3E%3Crect width='100' height='100' fill='url(%23grid)'/%3E%3C/svg%3E");
  opacity: 0.3;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20px, 20px);
  }
}

/* 神经网络节点 */
.login-container::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='200' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3CradialGradient id='node1' cx='50%25' cy='50%25' r='50%25'%3E%3Cstop offset='0%25' style='stop-color:%233b82f6;stop-opacity:0.1'/%3E%3Cstop offset='100%25' style='stop-color:%233b82f6;stop-opacity:0'/%3E%3C/radialGradient%3E%3CradialGradient id='node2' cx='50%25' cy='50%25' r='50%25'%3E%3Cstop offset='0%25' style='stop-color:%238b5cf6;stop-opacity:0.08'/%3E%3Cstop offset='100%25' style='stop-color:%238b5cf6;stop-opacity:0'/%3E%3C/radialGradient%3E%3C/defs%3E%3Ccircle cx='50' cy='50' r='30' fill='url(%23node1)'/%3E%3Ccircle cx='150' cy='100' r='25' fill='url(%23node2)'/%3E%3Ccircle cx='100' cy='150' r='20' fill='url(%23node1)'/%3E%3Cline x1='50' y1='50' x2='150' y2='100' stroke='%23cbd5e1' stroke-width='1' opacity='0.3'/%3E%3Cline x1='150' y1='100' x2='100' y2='150' stroke='%23cbd5e1' stroke-width='1' opacity='0.3'/%3E%3Cline x1='100' y1='150' x2='50' y2='50' stroke='%23cbd5e1' stroke-width='1' opacity='0.3'/%3E%3C/svg%3E");
  animation: networkFloat 15s ease-in-out infinite;
  opacity: 0.6;
}

@keyframes networkFloat {
  0%, 100% {
    transform: translateY(0px) scale(1);
  }
  50% {
    transform: translateY(-10px) scale(1.05);
  }
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--gap-2xl);
  position: relative;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-xl);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: var(--gap-xl);
  
  img {
    width: 120px;
    height: auto;
    margin-bottom: var(--gap-md);
  }
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    color: var(--color-primary);
    margin: 0 0 var(--gap-xs) 0;
  }
  
  p {
    font-size: 14px;
    color: var(--text-2);
    margin: 0;
  }
}

.form-section {
  margin-bottom: var(--gap-lg);
}

.register-section {
  text-align: center;
  padding-top: var(--gap-lg);
  border-top: 1px solid var(--border);
  color: var(--text-2);
  font-size: 14px;
  
  .el-button {
    color: var(--color-primary);
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 响应式设计
@media (max-width: 480px) {
  .login-container {
    padding: var(--gap-md);
  }
  
  .login-card {
    margin: 0 var(--gap-lg);
    padding: var(--gap-lg);
  }
}

// 减少动画（用户偏好）
@media (prefers-reduced-motion: reduce) {
  .bg-scape,
  .login-container::before,
  .login-container::after {
    animation: none;
  }
}
</style>