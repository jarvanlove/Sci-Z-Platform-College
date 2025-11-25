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
      <div class="header-left">
        <h1 class="page-title">{{ $t('report.generatePage.title') }}</h1>
      </div>
      <div class="header-actions">
        <BaseButton @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          {{ $t('common.back') }}
        </BaseButton>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 配置区域 -->
      <BaseCard class="config-section">
        <div class="config-header">
          <h2 class="config-title">
            <el-icon class="config-title-icon"><Setting /></el-icon>
            {{ $t('report.generatePage.configTitle') }}
          </h2>
        </div>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
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
                  <span v-if="project.code" class="project-code">{{ project.code }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 项目信息卡片 -->
          <div v-if="selectedProject" class="project-info">
            <h3 class="project-info-title">
              <el-icon><InfoFilled /></el-icon>
              {{ $t('report.generatePage.projectInfo') }}
            </h3>
            <div class="project-stats">
              <div class="stat-item">
                <p class="stat-value">{{ selectedProject.code || '-' }}</p>
                <p class="stat-label">{{ $t('report.generatePage.projectCode') }}</p>
              </div>
              <div class="stat-item">
                <p class="stat-value">{{ selectedProject.leaderName || '-' }}</p>
                <p class="stat-label">{{ $t('report.generatePage.projectLeader') }}</p>
              </div>
              <div class="stat-item">
                <p class="stat-value">{{ selectedProject.departmentName || '-' }}</p>
                <p class="stat-label">{{ $t('report.generatePage.department') }}</p>
              </div>
            </div>
          </div>

          <!-- 报告类型选择 -->
          <div class="report-type-selector form-item-full">
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
                  </span>
                </div>
                <div
                  class="type-card"
                  :class="{ selected: formData.reportType === 'self' }"
                  @click="formData.reportType = 'self'"
                >
                  <el-icon class="type-icon"><EditPen /></el-icon>
                  <h3 class="type-title">{{ $t('report.typeSelf') }}</h3>
                  <p class="type-description">{{ $t('report.generatePage.typeSelfDesc') }}</p>
                  <span v-if="formData.reportType === 'self'" class="type-badge">
                    <el-icon><Check /></el-icon>
                  </span>
                </div>
              </div>
            </el-form-item>
          </div>

          <!-- 报告摘要 -->
          <el-form-item :label="$t('report.generatePage.summary')" prop="summary" class="form-item-full">
            <el-input
              v-model="formData.summary"
              type="textarea"
              :rows="4"
              :placeholder="$t('report.generatePage.summaryPlaceholder')"
              maxlength="500"
              show-word-limit
            />
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
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Setting,
  InfoFilled,
  Document,
  EditPen,
  Check,
  MagicStick
} from '@element-plus/icons-vue'
import BaseButton from '@/components/Common/BaseButton.vue'
import BaseCard from '@/components/Common/BaseCard.vue'
import { getProjectList } from '@/api/Project/project'
import { createReportManagement } from '@/api/Report/report'
import { createLogger } from '@/utils/simpleLogger'

const logger = createLogger('ReportGenerate')
const router = useRouter()

// 表单引用
const formRef = ref(null)

// 表单数据
const formData = reactive({
  projectId: null,
  projectName: '',
  projectCode: '',
  projectKnowledgeId: '',
  reportType: '',
  summary: ''
})

// 表单验证规则
const formRules = {
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ],
  reportType: [
    { required: true, message: '请选择报告类型', trigger: 'change' }
  ]
}

// 项目列表
const projectList = ref([])
const projectsLoading = ref(false)
const selectedProject = computed(() => {
  if (!formData.projectId) return null
  return projectList.value.find(p => p.id === formData.projectId)
})

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
    const response = await getProjectList({
      page: 1,
      size: 1000,
      status: 'active' // 只加载活跃项目
    })
    
    if (response.code === 200 && response.data) {
      projectList.value = response.data.records || response.data.list || []
      logger.info(`加载项目列表成功: ${projectList.value.length} 个项目`)
    } else {
      throw new Error(response.message || '加载项目列表失败')
    }
  } catch (error) {
    logger.error(`加载项目列表失败: ${error.message}`, error)
    ElMessage.error('加载项目列表失败，请稍后重试')
  } finally {
    projectsLoading.value = false
  }
}

// 项目选择变化
const handleProjectChange = (projectId) => {
  const project = projectList.value.find(p => p.id === projectId)
  if (project) {
    formData.projectName = project.name || ''
    formData.projectCode = project.code || ''
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
      summary: formData.summary || undefined
    }

    logger.info('开始创建报告', requestData)
    
    const response = await createReportManagement(requestData)
    
    if (response.code === 200) {
      ElMessage.success('报告创建成功')
      logger.info(`报告创建成功: ID=${response.data}`)
      
      // 跳转到报告列表
      router.push('/report/list')
    } else {
      throw new Error(response.message || '创建报告失败')
    }
  } catch (error) {
    logger.error(`创建报告失败: ${error.message}`, error)
    ElMessage.error(error.message || '创建报告失败，请稍后重试')
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
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-color-secondary, #f7f9fc);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面头部 */
.page-header {
  background: var(--bg-color, #ffffff);
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  padding: var(--gap-lg, 20px) var(--gap-xl, 24px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--gap-md, 12px);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color, #1e3a8a);
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--gap-md, 12px);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: var(--gap-xl, 24px);
  gap: var(--gap-xl, 24px);
  overflow-y: auto;
  align-items: flex-start;
}

/* 配置区域 */
.config-section {
  background: var(--bg-color, #ffffff);
  border-radius: 12px;
  padding: var(--gap-xl, 32px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color, #e5e7eb);
  width: 100%;
  max-width: 100%;
  min-width: 0;
  flex-shrink: 0;
}

.config-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: var(--gap-lg, 20px);
  border-bottom: 2px solid var(--border-color-light, #f3f4f6);
}

.config-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color, #1e3a8a);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--gap-md, 12px);
}

.config-title-icon {
  font-size: 24px;
}

.config-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--gap-xl, 24px);
  margin-bottom: var(--gap-xl, 24px);
}

.form-item-full {
  grid-column: 1 / -1;
}

/* 项目选项样式 */
.project-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-name {
  font-weight: 500;
}

.project-code {
  font-size: 12px;
  color: var(--text-color-secondary, #6b7280);
  margin-left: var(--gap-sm, 8px);
}

/* 项目信息卡片 */
.project-info {
  background: linear-gradient(135deg, #f0f9ff 0%, #dbeafe 100%);
  border: 2px solid var(--primary-color-light, #0ea5e9);
  border-radius: 12px;
  padding: var(--gap-lg, 20px);
  margin-top: var(--gap-md, 12px);
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.1);
  position: relative;
  overflow: hidden;
  grid-column: 1 / -1;
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
  font-size: 15px;
  font-weight: 700;
  color: #0369a1;
  margin: 0 0 var(--gap-md, 16px) 0;
  display: flex;
  align-items: center;
  gap: var(--gap-sm, 8px);
}

.project-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--gap-lg, 20px);
}

.stat-item {
  text-align: center;
  background: rgba(255, 255, 255, 0.6);
  padding: var(--gap-md, 12px);
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #0369a1;
  margin: 0;
}

.stat-label {
  font-size: 12px;
  color: var(--text-color-secondary, #6b7280);
  margin: var(--gap-sm, 8px) 0 0 0;
  font-weight: 500;
}

/* 报告类型选择 */
.report-type-selector {
  grid-column: 1 / -1;
}

.type-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--gap-lg, 20px);
  width: 100%;
}

.type-card {
  border: 3px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  padding: 28px 24px;
  cursor: pointer;
  transition: all 0.3s;
  background: var(--bg-color-secondary, #fafbfc);
  position: relative;
  overflow: hidden;
}

.type-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #1e3a8a 0%, #0ea5e9 100%);
  transform: scaleX(0);
  transition: transform 0.3s;
}

.type-card:hover {
  border-color: var(--primary-color, #1e3a8a);
  background: #eef2ff;
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
  color: var(--primary-color, #1e3a8a);
}

.type-card.selected .type-icon {
  transform: scale(1.1);
}

.type-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color, #1e3a8a);
  margin: 0 0 var(--gap-md, 12px) 0;
}

.type-description {
  font-size: 14px;
  color: var(--text-color-secondary, #6b7280);
  line-height: 1.6;
  margin: 0;
}

.type-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #16a34a;
  color: #ffffff;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
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
    grid-template-columns: 1fr;
  }
}
</style>

