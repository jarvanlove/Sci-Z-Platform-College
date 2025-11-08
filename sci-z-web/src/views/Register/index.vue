<!--
/**
 * @description 注册页面
 * 严格按照原型图实现的注册视图容器
 */
-->
<template>
  <BaseScrollbar class="register-page-scrollbar" size="medium">
    <div class="register-container">
      <div class="bg-layer dots"></div>
      <div class="bg-layer charts"></div>

      <div class="register-card">
        <div class="logo-section">
          <img src="@/assets/images/logo.svg" alt="Sci-Z Platform Logo" />
          <h1>{{ $t('auth.register.title') }}</h1>
          <p>{{ $t('auth.register.subtitle') }}</p>
        </div>

        <div class="form-section">
          <RegisterForm @register-success="handleRegisterSuccess" />
        </div>

        <div class="login-section">
          <span>{{ $t('auth.register.haveAccount') }}</span>
          <button class="login-link" type="button" @click="handleGoToLogin">
            {{ $t('auth.register.loginNow') }}
          </button>
        </div>
      </div>
    </div>
  </BaseScrollbar>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { BaseScrollbar } from '@/components/Common'
import { RegisterForm } from '@/components/Business/Auth'

// 路由和国际化
const router = useRouter()
const { t } = useI18n()

// 处理注册成功
const handleRegisterSuccess = (userData) => {
  ElMessage.success(t('auth.register.registerSuccess'))
  router.push('/login')
}

// 跳转到登录
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
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 50%, #cbd5e1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.45;
}

.bg-layer.dots {
  background-image: url("data:image/svg+xml,%3Csvg width='120' height='120' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3Cpattern id='dots' width='20' height='20' patternUnits='userSpaceOnUse'%3E%3Ccircle cx='10' cy='10' r='1.5' fill='%23cbd5e1' opacity='0.4'/%3E%3C/pattern%3E%3C/defs%3E%3Crect width='120' height='120' fill='url(%23dots)'/%3E%3C/svg%3E");
  animation: dotsMove 25s linear infinite;
}

.bg-layer.charts {
  background-image: url("data:image/svg+xml,%3Csvg width='300' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3ClinearGradient id='chart1' x1='0%25' y1='0%25' x2='100%25' y2='0%25'%3E%3Cstop offset='0%25' style='stop-color:%233b82f6;stop-opacity:0.1'/%3E%3Cstop offset='100%25' style='stop-color:%233b82f6;stop-opacity:0'/%3E%3C/linearGradient%3E%3ClinearGradient id='chart2' x1='0%25' y1='0%25' x2='100%25' y2='0%25'%3E%3Cstop offset='0%25' style='stop-color:%238b5cf6;stop-opacity:0.08'/%3E%3Cstop offset='100%25' style='stop-color:%238b5cf6;stop-opacity:0'/%3E%3C/linearGradient%3E%3C/defs%3E%3Cpath d='M20,150 L50,120 L80,100 L110,80 L140,60 L170,40 L200,20' stroke='%233b82f6' stroke-width='2' fill='none' opacity='0.3'/%3E%3Cpath d='M20,160 L50,140 L80,120 L110,100 L140,90 L170,70 L200,50' stroke='%238b5cf6' stroke-width='2' fill='none' opacity='0.3'/%3E%3Ccircle cx='50' cy='120' r='3' fill='%233b82f6' opacity='0.4'/%3E%3Ccircle cx='110' cy='80' r='3' fill='%233b82f6' opacity='0.4'/%3E%3Ccircle cx='170' cy='40' r='3' fill='%233b82f6' opacity='0.4'/%3E%3Ccircle cx='80' cy='120' r='3' fill='%238b5cf6' opacity='0.4'/%3E%3Ccircle cx='140' cy='90' r='3' fill='%238b5cf6' opacity='0.4'/%3E%3C/svg%3E");
  animation: chartFloat 18s ease-in-out infinite;
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
  max-width: 520px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(25px);
  border-radius: 24px;
  box-shadow:
    0 22px 40px rgba(15, 23, 42, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  padding: 32px 28px 28px;
  position: relative;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.45);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow:
      0 26px 48px rgba(15, 23, 42, 0.16),
      0 0 0 1px rgba(255, 255, 255, 0.35),
      inset 0 1px 0 rgba(255, 255, 255, 0.5);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 22px;

  img {
    width: 80px;
    height: 80px;
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
  margin-bottom: 24px;
}

.login-section {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.35);
  color: #64748b;
  font-size: 14px;

  .login-link {
    background: transparent;
    border: none;
    color: #1e3a8a;
    font-weight: 600;
    cursor: pointer;
    margin-left: 6px;
    padding: 0;
    transition: color 0.2s ease;
  }

  .login-link:hover {
    color: #2563eb;
    text-decoration: underline;
  }
}

@media (max-width: 640px) {
  .register-container {
    padding: 20px;
  }

  .register-card {
    margin: 0 12px;
    padding: 26px 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .bg-layer {
    animation: none;
  }
}

@keyframes dotsMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20px, 20px);
  }
}
</style>

