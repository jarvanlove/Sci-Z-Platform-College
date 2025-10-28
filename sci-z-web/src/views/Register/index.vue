<!--
/**
 * @description 注册页面
 * 新用户注册页面
 * 基于原型图设计的科研数据流背景注册界面
 */
-->
<template>
  <BaseScrollbar class="register-page-scrollbar" size="medium">
    <div class="register-container">
      <!-- 注册卡片 -->
      <BaseCard class="register-card">
        <!-- Logo 区域 -->
        <div class="logo-section">
          <img src="@/assets/images/logo.svg" alt="Logo" />
          <h1>{{ $t('auth.createAccount') }}</h1>
          <p>{{ $t('auth.joinPlatform') }}</p>
        </div>
        
        <!-- 表单区域 -->
        <div class="form-section">
          <RegisterForm 
            @register-success="handleRegisterSuccess"
            @go-to-login="handleGoToLogin"
          />
        </div>
        
        <!-- 底部区域 -->
        <div class="login-section">
          <span>{{ $t('auth.haveAccount') }}</span>
          <el-button type="text" @click="handleGoToLogin">
            {{ $t('auth.loginNow') }}
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
import { RegisterForm } from '@/components/Business/Auth'

// 路由和国际化
const router = useRouter()
const { t } = useI18n()

// 处理注册成功
const handleRegisterSuccess = (userData) => {
  ElMessage.success(t('auth.registerSuccess'))
  
  // 跳转到登录页面
  router.push('/login')
}

// 处理跳转到登录
const handleGoToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.register-page-scrollbar {
  height: 100vh;
}

.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg) 0%, var(--surface) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--gap-lg);
  position: relative;
  overflow: hidden;
}

/* 科研数据流SVG背景 */
.register-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='120' height='120' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3Cpattern id='dots' width='20' height='20' patternUnits='userSpaceOnUse'%3E%3Ccircle cx='10' cy='10' r='1.5' fill='%23cbd5e1' opacity='0.4'/%3E%3C/pattern%3E%3C/defs%3E%3Crect width='120' height='120' fill='url(%23dots)'/%3E%3C/svg%3E");
  opacity: 0.4;
  animation: dotsMove 25s linear infinite;
}

@keyframes dotsMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20px, 20px);
  }
}

/* 科研图表SVG */
.register-container::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='300' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3ClinearGradient id='chart1' x1='0%25' y1='0%25' x2='100%25' y2='0%25'%3E%3Cstop offset='0%25' style='stop-color:%233b82f6;stop-opacity:0.1'/%3E%3Cstop offset='100%25' style='stop-color:%233b82f6;stop-opacity:0'/%3E%3C/linearGradient%3E%3ClinearGradient id='chart2' x1='0%25' y1='0%25' x2='100%25' y2='0%25'%3E%3Cstop offset='0%25' style='stop-color:%238b5cf6;stop-opacity:0.08'/%3E%3Cstop offset='100%25' style='stop-color:%238b5cf6;stop-opacity:0'/%3E%3C/linearGradient%3E%3C/defs%3E%3Cpath d='M20,150 L50,120 L80,100 L110,80 L140,60 L170,40 L200,20' stroke='%233b82f6' stroke-width='2' fill='none' opacity='0.3'/%3E%3Cpath d='M20,160 L50,140 L80,120 L110,100 L140,90 L170,70 L200,50' stroke='%238b5cf6' stroke-width='2' fill='none' opacity='0.3'/%3E%3Ccircle cx='50' cy='120' r='3' fill='%233b82f6' opacity='0.4'/%3E%3Ccircle cx='110' cy='80' r='3' fill='%233b82f6' opacity='0.4'/%3E%3Ccircle cx='170' cy='40' r='3' fill='%233b82f6' opacity='0.4'/%3E%3Ccircle cx='80' cy='120' r='3' fill='%238b5cf6' opacity='0.4'/%3E%3Ccircle cx='140' cy='90' r='3' fill='%238b5cf6' opacity='0.4'/%3E%3C/svg%3E");
  animation: chartFloat 18s ease-in-out infinite;
  opacity: 0.5;
}

@keyframes chartFloat {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-8px) rotate(1deg);
  }
}

.register-card {
  width: 100%;
  max-width: 500px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(25px);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--gap-2xl);
  position: relative;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-3px);
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

.login-section {
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
@media (max-width: 640px) {
  .register-container {
    padding: var(--gap-md);
  }
  
  .register-card {
    margin: 0 var(--gap-lg);
    padding: var(--gap-lg);
  }
}

// 减少动画（用户偏好）
@media (prefers-reduced-motion: reduce) {
  .register-container::before,
  .register-container::after {
    animation: none;
  }
}
</style>