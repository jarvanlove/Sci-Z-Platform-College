<template>
  <div class="layout-sidebar" :class="{ 'is-collapsed': sidebarCollapsed }">
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      :collapse="sidebarCollapsed"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><House /></el-icon>
        <template #title>{{ $t('menu.dashboard') }}</template>
      </el-menu-item>
      
      <el-sub-menu index="declaration">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>{{ $t('menu.declaration') }}</span>
        </template>
        <el-menu-item index="/declaration/list">{{ $t('menu.declarationList') }}</el-menu-item>
        <el-menu-item index="/declaration/create">{{ $t('menu.declarationCreate') }}</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="project">
        <template #title>
          <el-icon><Folder /></el-icon>
          <span>{{ $t('menu.project') }}</span>
        </template>
        <el-menu-item index="/project/list">{{ $t('menu.projectList') }}</el-menu-item>
        <el-menu-item index="/project/detail">{{ $t('menu.projectDetail') }}</el-menu-item>
        <el-menu-item index="/project/progress">{{ $t('menu.projectProgress') }}</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="report">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>{{ $t('menu.report') }}</span>
        </template>
        <el-menu-item index="/report/list">{{ $t('menu.reportList') }}</el-menu-item>
        <el-menu-item index="/report/generate">{{ $t('menu.reportGenerate') }}</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="knowledge">
        <template #title>
          <el-icon><Collection /></el-icon>
          <span>{{ $t('menu.knowledge') }}</span>
        </template>
        <el-menu-item index="/knowledge/list">{{ $t('menu.knowledgeList') }}</el-menu-item>
      </el-sub-menu>
      
      <el-menu-item index="/ai/chat">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>{{ $t('menu.ai') }}</template>
      </el-menu-item>
      
      <el-sub-menu index="user">
        <template #title>
          <el-icon><User /></el-icon>
          <span>{{ $t('menu.user') }}</span>
        </template>
        <el-menu-item index="/user/profile">{{ $t('menu.userProfile') }}</el-menu-item>
        <el-menu-item index="/user/security">{{ $t('menu.userSecurity') }}</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>{{ $t('menu.system') }}</span>
        </template>
        <el-menu-item index="/system/user">{{ $t('menu.systemUser') }}</el-menu-item>
        <el-menu-item index="/system/role">{{ $t('menu.systemRole') }}</el-menu-item>
        <el-menu-item index="/system/config">{{ $t('menu.systemConfig') }}</el-menu-item>
        <el-menu-item index="/system/logs">{{ $t('menu.systemLogs') }}</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/store/modules/app'

const route = useRoute()
const { t } = useI18n()
const appStore = useAppStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 侧边栏折叠状态
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
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

    :deep(.el-menu-item) {
      color: var(--text);
      
      &:hover {
        background-color: var(--hover);
        color: var(--color-primary);
      }
      
      &.is-active {
        background-color: var(--color-primary-light);
        color: var(--color-primary);
        border-right: 3px solid var(--color-primary);
      }
    }

    :deep(.el-sub-menu__title) {
      color: var(--text);
      
      &:hover {
        background-color: var(--hover);
        color: var(--color-primary);
      }
    }

    :deep(.el-sub-menu .el-menu-item) {
      background-color: var(--bg);
      
      &:hover {
        background-color: var(--hover);
        color: var(--color-primary);
      }
      
      &.is-active {
        background-color: var(--color-primary-light);
        color: var(--color-primary);
        border-right: 3px solid var(--color-primary);
      }
    }
  }
}
</style>
