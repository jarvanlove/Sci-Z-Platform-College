<template>
  <div class="layout-header">
    <div class="header-left">
      <el-button
        type="text"
        class="sidebar-toggle"
        @click="toggleSidebar"
        aria-label="toggle-sidebar"
      >
        <!-- 折叠时显示“菜单”图标（表示可展开）；展开时显示“折叠”图标（表示可收起） -->
        <el-icon><Menu v-if="sidebarCollapsed" /><Fold v-else /></el-icon>
      </el-button>
      <h1>{{ $t('menu.dashboard') }}</h1>
    </div>
    <div class="header-right">
      <!-- 语言切换 -->
      <LanguageSwitcher />
      
      <!-- 主题切换 -->
      <el-button type="text" @click="toggleTheme" class="theme-toggle">
        <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
      </el-button>
      
      <!-- 用户信息 -->
      <el-dropdown @command="handleUserCommand" trigger="click">
        <div class="user-info">
          <el-avatar :size="32" :src="userInfo?.avatar">
            {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <span class="username">{{ userInfo?.username || 'User' }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
            {{ $t('user.profile.menu') }}
            </el-dropdown-item>
            <el-dropdown-item command="security">
              <el-icon><Lock /></el-icon>
              {{ $t('user.security.menu') }}
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              {{ $t('auth.logout') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store/modules/app'
import { useAuthStore } from '@/store/modules/auth'
import LanguageSwitcher from '@/components/Common/LanguageSwitcher.vue'

const router = useRouter()
const { t } = useI18n()
const appStore = useAppStore()

// 侧边栏状态
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)

// 主题状态
const isDark = computed(() => appStore.theme === 'dark')

// 用户信息（这里应该从 store 获取）
const userInfo = computed(() => {
  const authStore = useAuthStore()
  return authStore.userInfo
})

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

// 切换主题
const toggleTheme = () => {
  appStore.toggleTheme()
}

// 处理用户菜单命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'security':
      router.push('/user/security')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    // 确认退出
    await ElMessageBox.confirm(
      t('auth.logoutConfirm'),
      t('common.tips'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    const authStore = useAuthStore()
    
    // 调用退出接口并清理本地数据
    await authStore.logout({ redirect: false })
    
    // 显示退出成功消息
    ElMessage.success(t('auth.logoutSuccess'))
    
    // 跳转到登录页
    await router.push('/login')
    
    // 开发环境日志
    if (import.meta.env.DEV) {
      console.log('✅ 用户已退出登录')
    }
  } catch (error) {
    // 用户取消退出
    if (error === 'cancel') {
      return
    }
    
    // 退出失败处理
    console.error('退出登录失败:', error)
    ElMessage.error(t('auth.logoutFailed') || '退出登录失败，请重试')
  }
}
</script>

<style lang="scss" scoped>
.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 24px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .sidebar-toggle {
      padding: 8px;
      color: var(--text);
      
      &:hover {
        color: var(--color-primary);
        background-color: var(--hover);
      }
    }

    h1 {
      font-size: 20px;
      font-weight: 600;
      color: var(--color-primary);
      margin: 0;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .theme-toggle {
      padding: 8px;
      color: var(--text);
      
      &:hover {
        color: var(--color-primary);
        background-color: var(--hover);
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background-color: #f0f9ff;
        color: #1e40af;
      }

      .username {
        font-size: 14px;
        color: var(--text);
        font-weight: 500;
      }
    }
  }
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    background-color: var(--hover);
    color: var(--color-primary);

    .el-icon {
      color: var(--color-primary);
    }
  }
}
</style>
