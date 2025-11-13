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
      <h1>{{ currentMenuTitle }}</h1>
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
          <el-avatar :size="32" :src="avatarUrl">
            {{ userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
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
import { computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useAppStore } from '@/store/modules/app'
import { useAuthStore } from '@/store/modules/auth'
import LanguageSwitcher from '@/components/Common/LanguageSwitcher.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()

// 使用 storeToRefs 确保响应式
const { userInfo: storeUserInfo } = storeToRefs(authStore)

// 侧边栏状态
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)

// 主题状态
const isDark = computed(() => appStore.theme === 'dark')

// 根据当前路由获取菜单标题
const currentMenuTitle = computed(() => {
  const currentPath = route.path
  const menus = authStore.menus || []
  
  // 递归查找匹配的菜单项
  const findMenuByPath = (menuList, path) => {
    for (const menu of menuList) {
      // 精确匹配
      if (menu.path === path) {
        return menu.title
      }
      // 如果菜单有子项，递归查找
      if (menu.children && menu.children.length > 0) {
        const childMatch = findMenuByPath(menu.children, path)
        if (childMatch) {
          return childMatch
        }
      }
    }
    return null
  }
  
  const matchedTitle = findMenuByPath(menus, currentPath)
  
  // 如果找到匹配的菜单，返回标题；否则返回默认的"仪表板"
  return matchedTitle || t('menu.dashboard')
})

// 用户信息（使用 storeToRefs 确保响应式）
const userInfo = computed(() => storeUserInfo.value)

// 头像 URL（处理文件 ID 和相对路径）
const avatarUrl = computed(() => {
  const info = userInfo.value
  if (!info) {
    return null
  }
  
  const avatar = info.avatar
  const avatarFileId = info.avatarFileId || info.avatarId
  
  // 开发环境调试
  if (import.meta.env.DEV) {
    console.log('[Header] 头像信息:', { avatar, avatarFileId, userInfo: info })
  }
  
  // 如果有完整的 URL（http/https），直接使用
  if (avatar && (avatar.startsWith('http://') || avatar.startsWith('https://'))) {
    return avatar
  }
  
  // 如果是相对路径（以 / 开头），直接使用
  if (avatar && avatar.startsWith('/')) {
    return avatar
  }
  
  // 如果有文件 ID，使用预览接口
  if (avatarFileId) {
    return `/api/file/preview/${avatarFileId}`
  }
  
  // 如果 avatar 是纯数字，可能是文件 ID
  if (avatar && /^\d+$/.test(String(avatar))) {
    return `/api/file/preview/${avatar}`
  }
  
  // 其他情况返回原值（可能是相对路径但不以 / 开头）
  return avatar || null
})

// 监听 userInfo 变化，用于调试
if (import.meta.env.DEV) {
  watch(userInfo, (newVal) => {
    console.log('[Header] userInfo 变化:', { 
      hasUserInfo: !!newVal, 
      avatar: newVal?.avatar,
      username: newVal?.username 
    })
  }, { immediate: true, deep: true })
}

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

// 切换主题
const toggleTheme = () => {
  appStore.toggleTheme()
}

// 监听主题变化，确保 DOM 更新
watch(() => appStore.theme, (newTheme) => {
  const html = document.documentElement
  if (newTheme === 'dark') {
    html.classList.add('dark')
    html.setAttribute('data-theme', 'dark')
  } else {
    html.classList.remove('dark')
    html.setAttribute('data-theme', 'light')
  }
}, { immediate: true })

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
        background-color: var(--hover);
        color: var(--color-primary);
      }

      .username {
        font-size: 14px;
        color: var(--text);
        font-weight: 500;
      }
    }
  }
}

/* 下拉菜单容器样式 */
:deep(.el-dropdown-menu) {
  background-color: var(--surface) !important;
  border: 1px solid var(--border) !important;
  border-radius: 8px !important;
  padding: 4px 0 !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

/* 下拉菜单项样式（与一级菜单保持一致） */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
  font-size: 15px !important;
  font-weight: 400 !important;
  color: var(--text-2) !important;
  letter-spacing: 0.01em !important;
  padding: 0 20px !important;
  height: 48px !important;
  line-height: 48px !important;
  margin: 2px 8px !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;

  .el-icon {
    font-size: 16px !important;
    margin-right: 8px !important;
    color: var(--text-2) !important;
    transition: color 0.2s ease !important;
  }

  &:hover {
    background-color: var(--hover) !important;
    color: var(--color-primary) !important;

    .el-icon {
      color: var(--color-primary) !important;
    }
  }

  /* 分隔线样式 */
  &.is-divided {
    border-top: 1px solid var(--border) !important;
    margin-top: 4px !important;
    padding-top: 4px !important;
  }
}
</style>
