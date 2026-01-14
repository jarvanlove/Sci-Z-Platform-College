<!--
/**
 * @description 系统设置索引页面
 * 展示系统管理的所有子功能模块
 */
-->
<template>
  <div class="system-container">
    <div class="system-grid">
      <!-- 用户管理 -->
      <div class="system-card" @click="goToUserManagement">
        <div class="card-icon user-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.systemUser') }}</h3>
          <p class="card-desc">{{ $t('system.index.userDesc') }}</p>
        </div>
      </div>

      <!-- 角色权限 -->
      <div class="system-card" @click="goToRoleManagement">
        <div class="card-icon role-icon">
          <el-icon><Key /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.systemRole') }}</h3>
          <p class="card-desc">{{ $t('system.index.roleDesc') }}</p>
        </div>
      </div>

      <!-- 日志管理 -->
      <div class="system-card" @click="goToLogManagement">
        <div class="card-icon log-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.systemLogs') }}</h3>
          <p class="card-desc">{{ $t('system.index.logDesc') }}</p>
        </div>
      </div>

      <!-- API Key 配置 -->
      <div class="system-card" @click="goToApiKeyManagement">
        <div class="card-icon apikey-icon">
          <el-icon><Key /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.apiKey') }}</h3>
          <p class="card-desc">{{ $t('system.index.apikeyDesc') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  User,
  Key,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()

const goToUserManagement = () => {
  router.push('/system/user')
}

const goToRoleManagement = () => {
  router.push('/system/role')
}

const goToLogManagement = () => {
  router.push('/system/logs')
}

const goToApiKeyManagement = () => {
  router.push('/system/apikey')
}
</script>

<style lang="scss" scoped>
.system-container {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.system-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.system-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  flex: 0 0 calc(33.333% - 14px);
  min-width: 280px;
  max-width: 380px;

  &:hover {
    border-color: #1e3a8a;
    box-shadow: 0 4px 12px rgba(30, 58, 138, 0.1);
    transform: translateY(-2px);
  }
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;

  &.user-icon {
    background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
    color: #1e40af;
  }

  &.role-icon {
    background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
    color: #1e3a8a;
  }

  &.log-icon {
    background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
    color: #92400e;
  }

  &.apikey-icon {
    background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
    color: #065f46;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.card-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}
</style>

