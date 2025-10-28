<template>
  <div class="layout-sidebar" :class="{ 'is-collapsed': sidebarCollapsed }">
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      :collapse="sidebarCollapsed"
      :unique-opened="true"
      router
    >
      <!-- 动态渲染菜单 -->
      <template v-for="menu in filteredMenus" :key="menu.path">
        <!-- 单级菜单 -->
        <el-menu-item 
          v-if="!menu.children"
          :index="menu.path"
        >
          <el-icon><component :is="menu.icon" /></el-icon>
          <template #title>{{ menu.title }}</template>
        </el-menu-item>
        
        <!-- 多级菜单 -->
        <el-sub-menu v-else :index="menu.path">
          <template #title>
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.title }}</span>
          </template>
          <el-menu-item 
            v-for="child in menu.children"
            :key="child.path"
            :index="child.path"
          >
            <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
            <template #title>{{ child.title }}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/store/modules/app'
import { useAuthStore } from '@/store/modules/auth'

const route = useRoute()
const { t } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 侧边栏折叠状态
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)

// 根据权限过滤菜单
const filteredMenus = computed(() => {
  return authStore.menus.filter(menu => {
    // 检查菜单权限
    if (menu.permission) {
      return authStore.hasPermission(menu.permission)
    }
    
    // 检查子菜单权限
    if (menu.children) {
      const hasAccessibleChildren = menu.children.some(child => {
        return !child.permission || authStore.hasPermission(child.permission)
      })
      return hasAccessibleChildren
    }
    
    return true
  })
})
</script>

<style lang="scss" scoped>
.layout-sidebar {
  width: 240px;
  height: 100vh;
  background: #ffffff;
  border-right: none;
  transition: width 0.3s ease;
  overflow: hidden;

  &.is-collapsed {
    width: 64px;
  }

  .sidebar-menu {
    border: none;
    height: 100%;
    background: transparent;
    overflow-x: hidden;

    /* 一级菜单项样式（如仪表板） */
    :deep(.el-menu-item:not(.el-sub-menu__title)) {
      font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
      font-size: 15px !important;
      font-weight: 400 !important;
      color: #374151 !important;
      padding: 0 20px !important;
      height: 48px !important;
      line-height: 48px !important;
      letter-spacing: 0.01em !important;
      margin: 2px 8px !important;
      border-radius: 6px !important;
      
      .el-icon {
        margin-right: 8px !important;
        font-size: 16px !important;
      }
      
      &:hover {
        background-color: #f8fafc !important;
        color: #1e3a8a !important;
      }
      
      &.is-active {
        background-color: #1e3a8a !important;
        color: #ffffff !important;
        font-weight: 600 !important;
        border-radius: 12px !important;
        margin: 2px 8px !important;
      }
    }

    /* 子菜单标题样式 */
    :deep(.el-sub-menu__title) {
      font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
      font-size: 15px !important;
      font-weight: 400 !important;
      color: #374151 !important;
      padding: 0 20px !important;
      letter-spacing: 0.01em !important;
      height: 48px !important;
      line-height: 48px !important;
      
      .el-icon {
        margin-right: 8px !important;
        font-size: 16px !important;
      }
      
      &:hover {
        background-color: #f8fafc !important;
        color: #1e3a8a !important;
      }
    }

    /* 二级菜单项样式 */
    :deep(.el-sub-menu .el-menu-item) {
      font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
      font-size: 14px !important;
      font-weight: 400 !important;
      color: #6b7280 !important;
      padding: 0 20px 0 56px !important;
      height: 40px !important;
      line-height: 40px !important;
      margin: 2px 8px !important;
      border-radius: 6px !important;
      background-color: transparent !important;
      
      .el-icon {
        margin-right: 8px !important;
        font-size: 16px !important;
      }
      
      &:hover {
        background-color: #f0f9ff !important;
        color: #1e3a8a !important;
      }
      
      &.is-active {
        background-color: #1e3a8a !important;
        color: #ffffff !important;
        font-weight: 600 !important;
        border-radius: 12px !important;
        margin: 2px 8px !important;
      }
    }

    /* 子菜单容器样式 */
    :deep(.el-sub-menu) {
      .el-menu {
        background-color: transparent !important;
      }
    }

    /* 折叠状态下的样式调整 */
    &.el-menu--collapse {
      overflow-x: hidden !important;
      
      :deep(.el-menu-item),
      :deep(.el-sub-menu__title) {
        padding: 0 20px !important;
        text-align: center !important;
      }
    }
  }
}
</style>
