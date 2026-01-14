<!--
/**
 * @description 实践页面
 * 整合项目管理、申报管理、报告管理、验收管理、知识库管理
 */
-->
<template>
  <div class="practice-container">
    <div class="practice-grid">
      <!-- 申报管理 -->
      <div class="practice-card" @click="goToDeclarationList">
        <div class="card-icon declaration-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.declaration') }}</h3>
          <p class="card-desc">{{ $t('practice.declarationDesc') }}</p>
        </div>
      </div>

      <!-- 项目管理 -->
      <div class="practice-card" @click="goToProjectList">
        <div class="card-icon project-icon">
          <el-icon><FolderOpened /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.project') }}</h3>
          <p class="card-desc">{{ $t('practice.projectDesc') }}</p>
        </div>
      </div>

      <!-- 报告管理 -->
      <div class="practice-card" @click="goToReportList">
        <div class="card-icon report-icon">
          <el-icon><DocumentCopy /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.report') }}</h3>
          <p class="card-desc">{{ $t('practice.reportDesc') }}</p>
        </div>
      </div>

      <!-- 仪表板 -->
      <div class="practice-card" @click="goToDashboard">
        <div class="card-icon dashboard-icon">
          <el-icon><Odometer /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ $t('menu.dashboard') }}</h3>
          <p class="card-desc">{{ $t('practice.dashboardDesc') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  Odometer,
  FolderOpened,
  Document,
  DocumentCopy
} from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()

const goToDashboard = () => {
  router.push('/dashboard')
}

const goToProjectList = () => {
  router.push('/project/list')
}

const goToDeclarationList = () => {
  router.push('/declaration/list')
}

const goToReportList = () => {
  router.push('/report/list')
}
</script>

<style lang="scss" scoped>
.practice-container {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.practice-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.practice-card {
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

  &.project-icon {
    background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
    color: #1e3a8a;
  }

  &.declaration-icon {
    background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
    color: #92400e;
  }

  &.report-icon {
    background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
    color: #1e40af;
  }

  &.dashboard-icon {
    background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
    color: #1e40af;
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

