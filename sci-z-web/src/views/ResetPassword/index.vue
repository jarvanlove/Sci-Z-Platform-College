<!--
/**
 * @description 重置密码页面
 * 用户密码重置页面
 * 基于原型图设计的安全锁背景重置界面
 */
-->
<template>
  <BaseScrollbar class="reset-page-scrollbar" size="medium">
    <div class="reset-container">
      <!-- 重置卡片 -->
      <BaseCard class="reset-card">
        <!-- Logo 区域 -->
        <div class="logo-section">
          <img src="@/assets/images/logo.svg" alt="Logo" />
          <h1>{{ $t('auth.resetPassword') }}</h1>
          <p>{{ $t('auth.resetPasswordDesc') }}</p>
        </div>
        
        <!-- 表单区域 -->
        <div class="form-section">
          <ResetPasswordForm 
            @reset-success="handleResetSuccess"
            @go-to-login="handleGoToLogin"
          />
        </div>
        
        <!-- 底部区域 -->
        <div class="login-section">
          <span>{{ $t('auth.rememberPassword') }}</span>
          <el-button type="text" @click="handleGoToLogin">
            {{ $t('auth.backToLogin') }}
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
import { ResetPasswordForm } from '@/components/Business/Auth'

// 路由和国际化
const router = useRouter()
const { t } = useI18n()

// 处理重置成功
const handleResetSuccess = (userData) => {
  ElMessage.success(t('auth.resetSuccess'))
  
  // 跳转到登录页面
  router.push('/login')
}

// 处理跳转到登录
const handleGoToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.reset-page-scrollbar {
  height: 100vh;
}

.reset-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg) 0%, var(--surface) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--gap-lg);
  position: relative;
  overflow: hidden;
}

/* 安全锁图案SVG背景 */
.reset-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='80' height='80' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3Cpattern id='locks' width='40' height='40' patternUnits='userSpaceOnUse'%3E%3Cpath d='M20,10 C25,10 30,15 30,20 L30,30 C30,32 28,34 26,34 L14,34 C12,34 10,32 10,30 L10,20 C10,15 15,10 20,10 Z' fill='none' stroke='%23cbd5e1' stroke-width='1' opacity='0.3'/%3E%3Ccircle cx='20' cy='22' r='2' fill='%23cbd5e1' opacity='0.2'/%3E%3C/pattern%3E%3C/defs%3E%3Crect width='80' height='80' fill='url(%23locks)'/%3E%3C/svg%3E");
  opacity: 0.3;
  animation: lockMove 30s linear infinite;
}

@keyframes lockMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(40px, 40px);
  }
}

/* 安全盾牌SVG */
.reset-container::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='200' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3CradialGradient id='shield1' cx='50%25' cy='50%25' r='50%25'%3E%3Cstop offset='0%25' style='stop-color:%233b82f6;stop-opacity:0.08'/%3E%3Cstop offset='100%25' style='stop-color:%233b82f6;stop-opacity:0'/%3E%3C/radialGradient%3E%3CradialGradient id='shield2' cx='50%25' cy='50%25' r='50%25'%3E%3Cstop offset='0%25' style='stop-color:%238b5cf6;stop-opacity:0.06'/%3E%3Cstop offset='100%25' style='stop-color:%238b5cf6;stop-opacity:0'/%3E%3C/radialGradient%3E%3C/defs%3E%3Cpath d='M100,20 L150,50 L150,120 C150,140 130,160 100,160 C70,160 50,140 50,120 L50,50 Z' fill='url(%23shield1)'/%3E%3Cpath d='M100,30 L140,55 L140,115 C140,130 125,145 100,145 C75,145 60,130 60,115 L60,55 Z' fill='url(%23shield2)'/%3E%3Ccircle cx='100' cy='90' r='8' fill='%233b82f6' opacity='0.1'/%3E%3Cpath d='M95,85 L100,90 L105,85' stroke='%233b82f6' stroke-width='2' fill='none' opacity='0.2'/%3E%3C/svg%3E");
  animation: shieldFloat 20s ease-in-out infinite;
  opacity: 0.4;
}

@keyframes shieldFloat {
  0%, 100% {
    transform: translateY(0px) scale(1);
  }
  50% {
    transform: translateY(-5px) scale(1.02);
  }
}

.reset-card {
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
@media (max-width: 480px) {
  .reset-container {
    padding: var(--gap-md);
  }
  
  .reset-card {
    margin: 0 var(--gap-lg);
    padding: var(--gap-lg);
  }
}

// 减少动画（用户偏好）
@media (prefers-reduced-motion: reduce) {
  .reset-container::before,
  .reset-container::after {
    animation: none;
  }
}
</style>