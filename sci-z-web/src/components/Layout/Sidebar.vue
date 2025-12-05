<template>
  <div class="layout-sidebar" :class="{ 'is-collapsed': sidebarCollapsed }">
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      :collapse="sidebarCollapsed"
      :unique-opened="true"
      router
      :default-openeds="defaultOpeneds"
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

// 递归查找菜单项（包括子菜单）
// 优先返回第一个匹配的菜单项（即使置灰），确保菜单始终激活同一个菜单项
const findMenuByPermission = (menus, permission) => {
  for (const menu of menus) {
    // 如果当前菜单项的 permission 匹配，立即返回其 path（不检查权限，因为可能是置灰的菜单）
    if (menu.permission === permission) {
      return menu.path
    }
    // 如果有子菜单，递归查找
    if (menu.children && menu.children.length > 0) {
      const found = findMenuByPermission(menu.children, permission)
      if (found) {
        return found
      }
    }
  }
  return null
}

// 递归查找菜单项（包括子菜单）通过路径匹配
const findMenuByPath = (menus, path) => {
  for (const menu of menus) {
    // 精确匹配当前菜单路径
    if (menu.path === path) {
      return menu.path
    }
    // 如果有子菜单，递归查找
    if (menu.children && menu.children.length > 0) {
      const found = findMenuByPath(menu.children, path)
      if (found) {
        return found
      }
    }
  }
  return null
}

// 递归查找父菜单路径
const findParentMenuPath = (menus, childPath) => {
  for (const menu of menus) {
    if (menu.children && menu.children.length > 0) {
      // 检查子菜单中是否有匹配的
      const childMatch = menu.children.find(child => child.path === childPath)
      if (childMatch) {
        return menu.path
      }
      // 递归查找
      const found = findParentMenuPath(menu.children, childPath)
      if (found) {
        return menu.path
      }
    }
  }
  return null
}

// 递归查找菜单项并获取其 permission
const getMenuPermission = (menus, path) => {
  for (const menu of menus) {
    if (menu.path === path) {
      return menu.permission
    }
    if (menu.children && menu.children.length > 0) {
      const found = getMenuPermission(menu.children, path)
      if (found !== null) {
        return found
      }
    }
  }
  return null
}

// 当前激活的菜单
// 优先通过路径匹配，但如果匹配到的菜单项 permission 与路由 permission 不一致，
// 说明是详情页/创建页，应该使用 permission 匹配来激活对应的列表页菜单
const activeMenu = computed(() => {
  const currentPath = route.path
  const routeMeta = route.meta
  
  // 首先尝试通过路径直接匹配菜单
  const pathMatched = findMenuByPath(authStore.menus, currentPath)
  if (pathMatched) {
    // 如果路径匹配成功，检查匹配到的菜单项的 permission 是否与路由的 permission 一致
    const menuPermission = getMenuPermission(authStore.menus, pathMatched)
    // 如果菜单项的 permission 与路由的 permission 不一致，说明是详情页/创建页，
    // 应该使用 permission 匹配来找到对应的列表页菜单
    if (routeMeta?.permission && menuPermission !== routeMeta.permission) {
      const permissionMatched = findMenuByPermission(authStore.menus, routeMeta.permission)
      if (permissionMatched) {
        return permissionMatched
      }
    }
    // 如果 permission 一致，或者没有 permission 配置，直接返回路径匹配的结果
    return pathMatched
  }
  
  // 如果路径匹配不到，尝试通过 permission 匹配
  if (routeMeta?.permission) {
    const permissionMatched = findMenuByPermission(authStore.menus, routeMeta.permission)
    if (permissionMatched) {
      return permissionMatched
    }
  }
  
  // 如果都匹配不到，返回当前路径（Element Plus 菜单会自动处理）
  return currentPath
})

// 默认展开的父菜单（当子菜单激活时，自动展开父菜单）
const defaultOpeneds = computed(() => {
  const currentPath = route.path
  const openedMenus = []
  
  // 查找当前路径对应的父菜单
  const parentPath = findParentMenuPath(authStore.menus, currentPath)
  if (parentPath) {
    openedMenus.push(parentPath)
  }
  
  return openedMenus
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
  background: var(--surface);
  border-right: 1px solid var(--border);
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
      color: var(--text-2) !important;
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
        background-color: var(--hover) !important;
        color: var(--color-primary) !important;
      }
      
      &.is-active {
        background-color: var(--color-primary) !important;
        color: var(--surface) !important;
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
      color: var(--text-2) !important;
      padding: 0 20px !important;
      letter-spacing: 0.01em !important;
      height: 48px !important;
      line-height: 48px !important;
      
      .el-icon {
        margin-right: 8px !important;
        font-size: 16px !important;
      }
      
      &:hover {
        background-color: var(--hover) !important;
        color: var(--color-primary) !important;
      }
    }

    /* 二级菜单项样式 */
    :deep(.el-sub-menu .el-menu-item) {
      font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
      font-size: 14px !important;
      font-weight: 400 !important;
      color: var(--text-3) !important;
      padding: 0 20px 0 44px !important;
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
        background-color: var(--hover) !important;
        color: var(--color-primary) !important;
      }
      
      &.is-active {
        background-color: var(--color-primary) !important;
        color: var(--surface) !important;
        font-weight: 600 !important;
        border-radius: 12px !important;
        margin: 2px 8px !important;
      }
    }

    /* 折叠后弹出层中的二级菜单项样式（与上面保持一致） */
    :deep(.el-menu--popup .el-menu-item) {
      font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
      font-size: 14px !important;
      font-weight: 400 !important;
      color: var(--text-3) !important;
      padding: 0 20px 0 44px !important;
      height: 40px !important;
      line-height: 40px !important;
      margin: 2px 8px !important;
      border-radius: 6px !important;
      background-color: transparent !important;

      &:hover {
        background-color: var(--hover) !important;
        color: var(--color-primary) !important;
      }

      &.is-active {
        background-color: var(--color-primary) !important;
        color: var(--surface) !important;
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
