<!--
/**
 * @description ToC 产品布局组件
 * 左侧主导航 + 主内容区的布局
 */
-->
<template>
  <div class="toc-layout">
    <!-- 左侧主导航 -->
    <ToCSidebar ref="sidebarRef" />
    
    <!-- 主内容区 -->
    <main class="toc-main-content">
      <!-- 右上角登录按钮（仅在侧边栏折叠且未登录时显示，不显示图标） -->
      <div v-if="showTopRightLogin" class="top-right-login">
        <button class="top-login-btn" @click="goToLogin">
          <span>{{ $t('user.login') }}</span>
        </button>
      </div>
      
      <slot>
        <router-view />
      </slot>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, provide } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/store/modules/auth'
import { useLoginModal } from '@/composables/useLoginModal'
import ToCSidebar from './ToCSidebar.vue'

const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const sidebarRef = ref(null)
const { openLoginModal } = useLoginModal()

// 计算是否显示右上角登录按钮
const showTopRightLogin = computed(() => {
  // 只在侧边栏折叠且用户未登录时显示
  const isCollapsed = sidebarRef.value?.isCollapsed ?? false
  return isCollapsed && !authStore.isLoggedIn
})

// 显示登录弹窗
const goToLogin = () => {
  openLoginModal()
}

// 提供侧边栏引用给子组件（如果需要）
provide('sidebarRef', sidebarRef)
</script>

<style lang="scss" scoped>
.toc-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  background: var(--bg);
  overflow: hidden;
}

.toc-main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto; // 🔥 修复：允许垂直滚动，确保所有页面内容都能完整显示
  overflow-x: hidden; // 防止水平滚动
  min-width: 0; // 防止内容溢出
  background: var(--surface);
  position: relative;
}

// 右上角登录按钮
.top-right-login {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 100;
}

.top-login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.15);

  &:hover {
    background: linear-gradient(135deg, #1e40af 0%, #2563eb 100%);
    box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }
}
</style>

