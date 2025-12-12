<!--
/**
 * @description 报告生成业务组件
 * 根据原型图实现报告生成功能，包括项目选择、报告类型选择、配置和生成
 */
-->
<template>
  <div class="report-generate-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <BackButton @click="handleBack" />
      <h1 class="page-title">{{ $t('report.title') }}</h1>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 配置区域 -->
      <BaseCard class="config-section">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          :label-width="formLabelWidth"
          class="config-form"
        >
          <!-- 项目选择 -->
          <el-form-item :label="$t('report.generatePage.selectProject')" prop="projectId" class="form-item-full">
            <el-select
              v-model="formData.projectId"
              :placeholder="$t('report.generatePage.projectPlaceholder')"
              filterable
              clearable
              style="width: 100%"
              :loading="projectsLoading"
              popper-class="report-project-select-dropdown"
              @change="handleProjectChange"
            >
              <el-option
                v-for="project in projectList"
                :key="project.id"
                :label="project.name"
                :value="project.id"
              >
                <div class="project-option">
                  <span class="project-name">{{ project.name }}</span>
                  <el-tag
                    v-if="project.statusDescription"
                    :type="getStatusTagType(project.statusDescription)"
                    size="small"
                    class="status-tag"
                  >
                    {{ project.statusDescription }}
                  </el-tag>
                </div>
              </el-option>
            </el-select>
            
            <!-- 项目信息卡片 -->
            <div v-if="selectedProject" class="project-info-wrapper">
              <div class="project-info">
                <h3 class="project-info-title">
                  {{ $t('report.generatePage.projectKnowledgeInfo') }}
                </h3>
                <div class="project-stats">
                  <div class="stat-item">
                    <p class="stat-label">{{ $t('report.generatePage.docCount') }}</p>
                    <p class="stat-value">{{ projectStats.docCount || '-' }}</p>
                  </div>
                  <div class="stat-item">
                    <p class="stat-label">{{ $t('report.generatePage.downloadCount') }}</p>
                    <p class="stat-value">{{ projectStats.downloadCount != null ? projectStats.downloadCount : '-' }}</p>
                  </div>
                  <div class="stat-item">
                    <p class="stat-label">{{ $t('report.generatePage.wordCount') }}</p>
                    <p class="stat-value">{{ projectStats.wordCount || '-' }}</p>
                  </div>
                  <div class="stat-item">
                    <p class="stat-label">{{ $t('report.generatePage.projectProgress') }}</p>
                    <p class="stat-value">{{ projectStats.progress != null ? (projectStats.progress + '%') : '-' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>

          <!-- 报告类型选择 -->
          <el-form-item :label="$t('report.type')" prop="reportType" class="form-item-full">
            <div class="type-cards">
              <div
                class="type-card"
                :class="{ selected: formData.reportType === 'tech' }"
                @click="formData.reportType = 'tech'"
              >
                <el-icon class="type-icon"><Document /></el-icon>
                <h3 class="type-title">{{ $t('report.typeTech') }}</h3>
                <p class="type-description">{{ $t('report.generatePage.typeTechDesc') }}</p>
                <span v-if="formData.reportType === 'tech'" class="type-badge">
                  <el-icon><Check /></el-icon>
                  {{ $t('report.generatePage.selected') }}
                </span>
              </div>
              <div
                class="type-card"
                :class="{ selected: formData.reportType === 'self' }"
                @click="formData.reportType = 'self'"
              >
                <el-icon class="type-icon"><DocumentChecked /></el-icon>
                <h3 class="type-title">{{ $t('report.typeSelf') }}</h3>
                <p class="type-description">{{ $t('report.generatePage.typeSelfDesc') }}</p>
                <span v-if="formData.reportType === 'self'" class="type-badge">
                  <el-icon><Check /></el-icon>
                  {{ $t('report.generatePage.selected') }}
                </span>
              </div>
            </div>
          </el-form-item>

          <!-- 高级配置（可折叠） -->
          <el-form-item :label="$t('report.generatePage.advancedConfig')" class="form-item-full advanced-config-item">
            <el-collapse v-model="activeCollapseNames" class="advanced-config-collapse">
              <el-collapse-item :name="'advanced'">
                <div class="advanced-config-content">
                  <!-- 报告风格和详细程度在一行 -->
                  <div class="advanced-config-row">
                    <!-- 报告风格 -->
                    <div class="advanced-config-group">
                      <label class="advanced-config-label">{{ $t('report.style') }}</label>
                      <el-radio-group v-model="formData.reportStyle" class="radio-group">
                        <el-radio label="formal">{{ $t('report.generatePage.styleFormal') }}</el-radio>
                        <el-radio label="academic">{{ $t('report.generatePage.styleAcademic') }}</el-radio>
                        <el-radio label="concise">{{ $t('report.generatePage.styleConcise') }}</el-radio>
                      </el-radio-group>
                    </div>

                    <!-- 详细程度 -->
                    <div class="advanced-config-group">
                      <label class="advanced-config-label">{{ $t('report.detailLevel') }}</label>
                      <el-radio-group v-model="formData.detailLevel" class="radio-group">
                        <el-radio label="brief">{{ $t('report.detailBrief') }}</el-radio>
                        <el-radio label="standard">{{ $t('report.detailNormal') }}</el-radio>
                        <el-radio label="detailed">{{ $t('report.detailDetailed') }}</el-radio>
                      </el-radio-group>
                    </div>
                  </div>

                  <!-- 特殊要求 -->
                  <div class="advanced-config-group">
                    <label class="advanced-config-label">{{ $t('report.generatePage.specialRequirements') }}</label>
                    <el-input
                      v-model="formData.specialRequirements"
                      type="textarea"
                      :rows="3"
                      :placeholder="$t('report.generatePage.specialRequirementsPlaceholder')"
                      maxlength="500"
                      show-word-limit
                    />
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-form-item>
        </el-form>

        <!-- 生成按钮 -->
        <div class="generate-section">
          <BaseButton
            type="primary"
            size="large"
            :loading="generating"
            :disabled="!canGenerate"
            @click="handleGenerate"
          >
            <el-icon v-if="!generating"><MagicStick /></el-icon>
            {{ $t('report.generateReport') }}
          </BaseButton>
          <p class="generate-hint">
            <el-icon><InfoFilled /></el-icon>
            {{ $t('report.generatePage.generateHint') }}
          </p>
        </div>
      </BaseCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  InfoFilled,
  Document,
  DocumentChecked,
  Check,
  MagicStick
} from '@element-plus/icons-vue'
import { BaseButton, BaseCard, BackButton } from '@/components/Common'
import { getProjectReportSelect } from '@/api/Project/project'
import { createReportManagement } from '@/api/Report/report'
import { createLogger } from '@/utils/simpleLogger'

const logger = createLogger('ReportGenerate')
const router = useRouter()
const { t } = useI18n()

// 表单标签宽度（用于对齐）
const formLabelWidth = '120px'

// 表单引用
const formRef = ref(null)

// 表单数据
const formData = reactive({
  projectId: null,
  projectName: '',
  projectCode: '',
  projectKnowledgeId: '',
  reportType: 'tech', // 默认选中科技报告
  // 高级配置
  reportStyle: 'formal', // 报告风格：formal/academic/concise
  detailLevel: 'standard', // 详细程度：brief/standard/detailed
  specialRequirements: '' // 特殊要求
})

// 表单验证规则
const formRules = computed(() => ({
  projectId: [
    { required: true, message: t('report.generatePage.projectRequired'), trigger: 'change' }
  ],
  reportType: [
    { required: true, message: t('report.generatePage.reportTypeRequired'), trigger: 'change' }
  ]
}))

// 项目列表
const projectList = ref([])
const projectsLoading = ref(false)
const selectedProject = computed(() => {
  if (!formData.projectId) return null
  return projectList.value.find(p => p.id === formData.projectId)
})

// 项目统计信息
const projectStats = computed(() => {
  if (!selectedProject.value) {
    return { docCount: '-', wordCount: '-', progress: null, downloadCount: null }
  }
  const progress = selectedProject.value.progress
  // 如果progress是字符串且包含%，提取数字部分；否则直接使用
  let progressValue = null
  if (progress != null) {
    if (typeof progress === 'string' && progress.includes('%')) {
      progressValue = progress.replace('%', '')
    } else {
      progressValue = String(progress).replace('%', '')
    }
  }
  return {
    docCount: selectedProject.value.documentCount || '0',
    wordCount: selectedProject.value.totalWords || '0',
    progress: progressValue,
    downloadCount: selectedProject.value.totalDownloadCount != null ? selectedProject.value.totalDownloadCount : 0
  }
})

// 高级配置折叠状态（使用 el-collapse 的 v-model）
const activeCollapseNames = ref([])

// 生成状态
const generating = ref(false)

// 是否可以生成
const canGenerate = computed(() => {
  return formData.projectId && formData.reportType && !generating.value
})

// 加载项目列表
const loadProjects = async () => {
  projectsLoading.value = true
  try {
    logger.info('开始加载报告生成可选项目列表')
    const response = await getProjectReportSelect()
    
    if (response.code === 200 && response.data) {
      projectList.value = Array.isArray(response.data) ? response.data : []
      logger.info(`加载项目列表成功: ${projectList.value.length} 个项目`, {
        projects: projectList.value.map(p => ({ id: p.id, name: p.name }))
      })
    } else {
      throw new Error(response.message || t('report.generatePage.loadProjectsError'))
    }
  } catch (error) {
    logger.error(`加载项目列表失败: ${error.message}`, error)
    ElMessage.error(error.message || t('report.generatePage.loadProjectsError'))
  } finally {
    projectsLoading.value = false
  }
}

// 获取状态标签类型（根据状态描述返回对应的 Element Plus tag type）
const getStatusTagType = (statusDescription) => {
  if (!statusDescription) return 'info'
  const status = statusDescription.toLowerCase()
  if (status.includes('已完成') || status.includes('完成')) {
    return 'success'
  } else if (status.includes('进行中') || status.includes('进行')) {
    return 'warning'
  } else if (status.includes('已取消') || status.includes('取消')) {
    return 'info'
  } else if (status.includes('已拒绝') || status.includes('拒绝')) {
    return 'danger'
  }
  return 'info'
}

// 项目选择变化
const handleProjectChange = (projectId) => {
  const project = projectList.value.find(p => p.id === projectId)
  if (project) {
    formData.projectName = project.name || ''
    formData.projectCode = project.number || ''
    formData.projectKnowledgeId = project.knowledgeId || ''
    logger.info(`选择项目: ${project.name} (ID: ${projectId})`)
  } else {
    formData.projectName = ''
    formData.projectCode = ''
    formData.projectKnowledgeId = ''
  }
}

// 生成报告
const handleGenerate = async () => {
  // 表单验证
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    logger.warn('表单验证失败', error)
    return
  }

  generating.value = true
  
  try {
    const requestData = {
      projectId: formData.projectId,
      projectName: formData.projectName,
      projectCode: formData.projectCode || undefined,
      projectKnowledgeId: formData.projectKnowledgeId || undefined,
      reportType: formData.reportType,
      // 高级配置
      reportStyle: formData.reportStyle,
      detailLevel: formData.detailLevel,
      specialRequirements: formData.specialRequirements || undefined
    }

    logger.info('开始创建报告', requestData)
    
    const response = await createReportManagement(requestData)
    
    if (response.code === 200) {
      ElMessage.success(t('report.generatePage.generateSuccess'))
      logger.info(`报告创建成功: ID=${response.data}`)
      
      // 跳转到报告列表
      router.push('/report/list')
    } else {
      throw new Error(response.message || t('report.generatePage.generateError'))
    }
  } catch (error) {
    logger.error(`创建报告失败: ${error.message}`, error)
    ElMessage.error(error.message || t('report.generatePage.generateError'))
  } finally {
    generating.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 初始化
onMounted(() => {
  loadProjects()
})
</script>

<style lang="scss" scoped>
.report-generate-container {
  padding: var(--gap-lg);
  background: var(--bg-secondary);
  min-height: calc(100vh - 56px);
  width: 100%;
  box-sizing: border-box;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: var(--gap-lg);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
}

/* 主内容区域 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: var(--gap-lg);
}

/* 配置区域 */
.config-section {
  background: var(--surface);
  border-radius: 12px;
  padding: var(--gap-xl);
  width: 100%;
  box-sizing: border-box;
}

.config-form {
  // 🔥 表单项上下排列
  display: flex;
  flex-direction: column;
  gap: var(--gap-xl, 24px);
  margin-bottom: var(--gap-xl, 24px);
  
  // 🔥 表单标签样式统一（按照 @页面修改.md 要求）
  :deep(.el-form-item__label) {
    font-size: 14px !important;
    color: var(--text-2) !important;
    font-weight: 600 !important;
  }
  
  // 🔥 表单内容样式统一（按照 @页面修改.md 要求）
  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    font-size: 14px !important;
    color: var(--text-3) !important;
    font-weight: 400 !important;
  }
  
  // 🔥 下拉框样式
  :deep(.el-select .el-input__inner) {
    font-size: 14px !important;
    color: var(--text-3) !important;
    font-weight: 400 !important;
  }
}

.form-item-full {
  // 🔥 全宽表单项（用于高级配置等）
  width: 100%;
  
  // 🔥 确保内容区域内的元素对齐
  :deep(.el-form-item__content) {
    display: flex;
    flex-direction: column;
    align-items: stretch;
  }
}


/* 项目选项样式 */
.project-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--gap-sm, 8px);
}

.project-name {
  font-weight: 500;
  flex: 1;
}

// 项目状态标签样式（圆角处理）
.status-tag {
  margin-left: auto;
  border-radius: 12px !important;
  font-size: 12px !important;
  padding: 2px 8px !important;
}

/* 项目信息卡片 */
.project-info-item {
  margin-top: 0;
}

.project-info {
  background: linear-gradient(135deg, #f0f9ff 0%, #dbeafe 100%);
  border: 2px solid var(--primary-color-light, #0ea5e9);
  border-radius: 12px;
  padding: var(--gap-lg, 20px);
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.1);
  position: relative;
  overflow: hidden;
  width: 100%;
  min-height: 120px; /* 改为最小高度，自适应内容 */
  max-width: 100%; /* 确保不超过容器宽度 */
  box-sizing: border-box;
  margin-top: var(--gap-md, 12px);
  display: flex;
  flex-direction: column;
}

.project-info::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #0ea5e9 0%, #1e3a8a 100%);
}

.project-info-title {
  font-size: 14px; /* 与表单标签保持一致 */
  font-weight: 600; /* 与表单标签保持一致 */
  color: var(--text-2, #4b5563); /* 与表单标签保持一致 */
  margin: 0 0 var(--gap-md, 16px) 0;
  display: flex;
  align-items: center;
  gap: var(--gap-sm, 8px);
}

.project-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--gap-lg, 20px);
}

.stat-item {
  text-align: center;
  background: rgba(255, 255, 255, 0.6);
  padding: var(--gap-md, 12px);
  border-radius: 8px;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 14px;
  color: var(--text-2);
  margin: 0;
  font-weight: 600;
}

.stat-value {
  font-size: 14px;
  font-weight: 400;
  color: var(--text-3);
  margin: 0;
}

/* 项目信息卡片包装器 */
.project-info-wrapper {
  margin-top: var(--gap-md, 12px);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}


/* 报告类型选择 */
.report-type-selector {
  grid-column: 1 / -1;
}

.type-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--gap-xl, 24px);
  width: 100%;
  box-sizing: border-box;
}

.type-card {
  border: 2px solid var(--primary-color-light, #0ea5e9); /* 未选中时使用与项目信息卡片相同的边框 */
  border-radius: 12px;
  padding: var(--gap-lg, 20px); /* 与项目信息卡片相同的padding */
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #f0f9ff 0%, #dbeafe 100%); /* 未选中时使用与项目信息卡片相同的背景 */
  position: relative;
  overflow: hidden;
  min-height: 120px; /* 改为最小高度，自适应内容 */
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.1); /* 与项目信息卡片相同的阴影 */
}

.type-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px; /* 与项目信息卡片相同的顶部条高度 */
  background: linear-gradient(90deg, #0ea5e9 0%, #1e3a8a 100%); /* 与项目信息卡片相同的渐变 */
  transform: scaleX(0); /* 未选中时隐藏顶部条 */
  transition: transform 0.3s;
}

.type-card:hover {
  border-color: var(--primary-color, #1e3a8a);
  background: linear-gradient(135deg, #eef2ff 0%, #f0f9ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 58, 138, 0.15);
}

.type-card:hover::before {
  transform: scaleX(1);
}

.type-card.selected {
  border-color: var(--primary-color, #1e3a8a);
  background: linear-gradient(135deg, #eef2ff 0%, #f0f9ff 100%);
  box-shadow: 0 4px 16px rgba(30, 58, 138, 0.2);
}

.type-card.selected::before {
  transform: scaleX(1);
}

.type-icon {
  font-size: 48px;
  margin-bottom: var(--gap-md, 16px);
  transition: transform 0.3s;
  display: block;
  color: var(--text-3, #6b7280); /* 使用表单内容字体颜色 */
}

.type-card.selected .type-icon {
  transform: scale(1.1);
}

.type-title {
  font-size: 14px; /* 与表单标签保持一致 */
  font-weight: 600; /* 与表单标签保持一致 */
  color: var(--text-2, #4b5563); /* 与表单标签保持一致 */
  margin: 0 0 var(--gap-md, 12px) 0;
}

.type-description {
  font-size: 14px; /* 与表单内容保持一致 */
  color: var(--text-3, #6b7280); /* 与表单内容保持一致 */
  font-weight: 400; /* 与表单内容保持一致 */
  line-height: 1.6;
  margin: 0;
}

.type-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #16a34a;
  color: #ffffff;
  padding: 4px 10px;
  border-radius: 18px !important;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 高级配置样式 */
.advanced-config-item {
  margin-top: var(--gap-md, 12px);
}

.advanced-config-collapse {
  width: 100%;
  
  // 🔥 el-collapse 主题样式统一
  :deep(.el-collapse-item__header) {
    font-size: 14px !important;
    color: var(--text-2) !important;
    font-weight: 600 !important;
    background: var(--surface) !important;
    border-color: var(--border) !important;
    padding: 16px 20px !important;
    
    &:hover {
      background: var(--hover) !important;
      color: var(--color-primary) !important;
    }
  }
  
  :deep(.el-collapse-item__header.is-active) {
    color: var(--color-primary) !important;
    border-bottom-color: var(--border) !important;
  }
  
  :deep(.el-collapse-item__arrow) {
    color: var(--text-2) !important;
    font-size: 16px !important;
    
    &.is-active {
      color: var(--color-primary) !important;
    }
  }
  
  :deep(.el-collapse-item__wrap) {
    background: var(--surface) !important;
    border-color: var(--border) !important;
  }
  
  :deep(.el-collapse-item__content) {
    padding: 20px !important;
    background: var(--surface) !important;
  }
}

.advanced-config-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-2);
}

.advanced-config-content {
  padding: 20px;
}

.advanced-config-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--gap-xl, 24px);
  margin-bottom: 24px;
}

.advanced-config-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.advanced-config-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-2);
  margin: 0;
}

.radio-group {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  
  // 🔥 单选按钮选中后颜色为主题色
  :deep(.el-radio__input.is-checked .el-radio__inner) {
    background-color: var(--color-primary) !important;
    border-color: var(--color-primary) !important;
  }
  
  :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: var(--color-primary) !important;
  }
  
  :deep(.el-radio__label) {
    font-size: 14px !important;
    color: var(--text-3) !important;
    font-weight: 400 !important;
  }
}

/* 生成按钮区域 */
.generate-section {
  text-align: center;
  margin-top: var(--gap-xl, 32px);
}

.generate-hint {
  margin-top: var(--gap-md, 16px);
  color: var(--text-color-secondary, #9ca3af);
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--gap-sm, 8px);
}

  /* 响应式 */
  @media (max-width: 768px) {
    .config-form {
      grid-template-columns: 1fr;
    }

    .type-cards {
      grid-template-columns: 1fr;
    }

    .project-stats {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 480px) {
      .project-stats {
        grid-template-columns: 1fr;
      }
    }

    .info-row {
      grid-template-columns: 1fr;
    }
  }
</style>

<!-- 全局样式：确保下拉菜单中的状态标签有圆角（下拉菜单挂载在 body 上） -->
<style lang="scss">
.report-project-select-dropdown {
  .el-select-dropdown__item .el-tag.status-tag,
  .el-select-dropdown__item .status-tag.el-tag,
  .el-select-dropdown__item .el-tag {
    border-radius: 12px !important;
    font-size: 12px !important;
    padding: 2px 8px !important;
  }
}
</style>

