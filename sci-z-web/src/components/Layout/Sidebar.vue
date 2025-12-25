<template>
  <div class="layout-sidebar" :class="{ 'is-collapsed': sidebarCollapsed }">
    <el-menu
      :default-active="activeMenuKey"
      :active-text-color="'var(--color-primary)'"
      class="sidebar-menu"
      :collapse="sidebarCollapsed"
      :unique-opened="true"
      :default-openeds="defaultOpeneds"
      @select="handleMenuSelect"
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
import { computed, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/store/modules/app'
import { useAuthStore } from '@/store/modules/auth'
import { translateMenus } from '@/utils/menuI18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()

// 🔥 修复：使用 ref 存储当前激活的菜单，确保响应式更新
// Element Plus 的 el-menu 的 default-active 只在初始化时生效，所以需要使用 ref 并手动更新
const activeMenuKey = ref('')

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
// 🔥 修复：支持动态路由匹配（如 /project/detail/:id 匹配 /project/list）
const findMenuByPath = (menus, path) => {
  for (const menu of menus) {
    // 精确匹配当前菜单路径
    if (menu.path === path) {
      return menu.path
    }
    
    // 🔥 修复：支持前缀匹配，用于动态路由（如 /project/detail/123 匹配 /project/list）
    // 检查当前路径是否以菜单路径开头（用于详情页匹配列表页菜单）
    if (menu.path && path.startsWith(menu.path + '/')) {
      // 进一步检查是否是动态路由的情况
      // 例如：当前路径 /project/detail/123，菜单路径 /project/list
      // 这种情况应该匹配到 /project/list 菜单
      const pathParts = path.split('/')
      const menuPathParts = menu.path.split('/')
      // 如果路径层级相同或更深，且前几级路径匹配，则认为是匹配的
      if (pathParts.length >= menuPathParts.length) {
        const isMatch = menuPathParts.every((part, index) => {
          return part === pathParts[index]
        })
        if (isMatch) {
          return menu.path
        }
      }
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
// 🔥 修复：改进菜单匹配逻辑，支持动态路由和详情页匹配
const calculateActiveMenu = () => {
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
  
  // 如果都匹配不到，返回当前路径
  return currentPath
}

// 🔥 修复：使用 computed 确保响应式更新
const activeMenu = computed(() => {
  return calculateActiveMenu()
})

// 🔥 修复：监听路由变化，更新激活菜单（确保菜单选中状态实时更新）
// 使用 nextTick 确保在路由变化后，Element Plus 菜单组件能够正确响应更新
watch(() => route.path, async () => {
  const newActiveMenu = calculateActiveMenu()
  activeMenuKey.value = newActiveMenu
  // 使用 nextTick 确保 Element Plus 菜单组件能够正确更新选中状态
  await nextTick()
  // 如果 nextTick 后菜单仍未正确选中，强制更新一次
  if (activeMenuKey.value !== newActiveMenu) {
    activeMenuKey.value = newActiveMenu
  }
}, { immediate: true })

// 🔥 修复：同时监听路由的 meta 变化（permission 可能变化）
watch(() => route.meta?.permission, async () => {
  const newActiveMenu = calculateActiveMenu()
  activeMenuKey.value = newActiveMenu
  await nextTick()
  if (activeMenuKey.value !== newActiveMenu) {
    activeMenuKey.value = newActiveMenu
  }
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

// 根据权限过滤菜单并应用 i18n 翻译
const filteredMenus = computed(() => {
  const filtered = authStore.menus.filter(menu => {
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
  
  // 应用 i18n 翻译
  return translateMenus(filtered, t)
})

// 🔥 修复：手动处理菜单点击，避免冗余导航，但确保菜单选中状态正确更新
const handleMenuSelect = async (index) => {
  // 🔥 关键修复：无论是否导航，都要立即更新菜单选中状态
  // 这样可以确保菜单点击后立即高亮，不需要等待路由跳转完成
  activeMenuKey.value = index
  
  // 如果点击的是当前已激活的菜单，不执行导航（避免冗余导航）
  if (index === route.path || index === activeMenu.value) {
    // 使用 nextTick 确保 Element Plus 菜单组件能够正确更新选中状态
    await nextTick()
    return
  }
  
  // 执行路由跳转
  try {
    await router.push(index)
    // 路由跳转成功后，再次确保菜单选中状态正确（防止路由跳转过程中状态丢失）
    await nextTick()
    activeMenuKey.value = index
  } catch (error) {
    // 如果是冗余导航错误，静默处理（这是正常的优化行为）
    if (error.message?.includes('Avoided redundant navigation')) {
      // 即使导航被阻止，菜单选中状态已经在上面更新了，这里只需要确保状态正确
      await nextTick()
      activeMenuKey.value = index
      return
    }
    // 其他错误才抛出
    throw error
  }
}
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
