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
        <h1 class="page-title">报告配置</h1>
      </div>
      <div class="header-actions">
        <BaseButton @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          {{ $t('common.back') }}
        </BaseButton>
        <BaseButton 
          type="primary" 
          @click="handleGenerate" 
          :loading="generating"
          :disabled="!canGenerate"
        >
          <el-icon v-if="!generating"><MagicStick /></el-icon>
          {{ $t('report.generateReport') }}
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
            报告配置
          </h2>
        </div>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          class="config-form"
        >
          <!-- 第一行：项目选择和工作流选择并排 -->
          <el-form-item label="选择项目 *" prop="projectId" class="form-item-half">
            <el-select
              v-model="formData.projectId"
              placeholder="请选择要生成报告的项目"
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

          <!-- 工作流选择 -->
          <el-form-item label="选择工作流 *" prop="workflowId" class="form-item-half">
            <WorkflowSelect
              v-model="formData.workflowId"
              :options="workflowOptions"
              :loading="workflowsLoading"
              placeholder="请选择工作流"
              :show-info="false"
              @change="handleWorkflowChange"
            />
          </el-form-item>

          <!-- 第二行：项目信息和工作流信息（选中后显示，左右分列，固定位置） -->
          <el-form-item v-if="selectedProject || selectedWorkflow" class="form-item-full info-row-item">
            <div class="info-row">
              <!-- 左侧：项目信息（固定位置） -->
              <div class="info-item info-item-left">
                <div v-if="selectedProject" class="project-info">
                  <h3 class="project-info-title">
                    <el-icon><InfoFilled /></el-icon>
                    项目知识库信息
                  </h3>
                  <div class="project-stats">
                    <div class="stat-item">
                      <p class="stat-value">{{ projectStats.docCount || '-' }}</p>
                      <p class="stat-label">文档数量</p>
                    </div>
                    <div class="stat-item">
                      <p class="stat-value">{{ projectStats.wordCount || '-' }}</p>
                      <p class="stat-label">总字数</p>
                    </div>
                    <div class="stat-item">
                      <p class="stat-value">{{ projectStats.progress || '-' }}</p>
                      <p class="stat-label">项目进度</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 右侧：工作流信息（固定位置） -->
              <div class="info-item info-item-right">
                <div v-if="selectedWorkflow" class="workflow-info-card">
                  <div class="workflow-info-title">{{ selectedWorkflow.name }}</div>
                  <div class="workflow-info-description">{{ selectedWorkflow.description }}</div>
                </div>
              </div>
            </div>
          </el-form-item>

          <!-- 报告类型选择 -->
          <el-form-item label="报告类型 *" prop="reportType" class="form-item-full">
            <div class="type-cards">
              <div
                class="type-card"
                :class="{ selected: formData.reportType === 'tech' }"
                @click="formData.reportType = 'tech'"
              >
                <el-icon class="type-icon"><Document /></el-icon>
                <h3 class="type-title">科技报告</h3>
                <p class="type-description">描述项目的科研成果、技术创新、学术价值</p>
                <span v-if="formData.reportType === 'tech'" class="type-badge">
                  <el-icon><Check /></el-icon>
                  已选择
                </span>
              </div>
              <div
                class="type-card"
                :class="{ selected: formData.reportType === 'self' }"
                @click="formData.reportType = 'self'"
              >
                <el-icon class="type-icon"><EditPen /></el-icon>
                <h3 class="type-title">自评报告</h3>
                <p class="type-description">对项目执行情况、完成质量进行自我评价</p>
                <span v-if="formData.reportType === 'self'" class="type-badge">
                  <el-icon><Check /></el-icon>
                  已选择
                </span>
              </div>
            </div>
          </el-form-item>

          <!-- 高级配置（可折叠） -->
          <el-form-item class="form-item-full advanced-config-item">
            <div class="advanced-config-wrapper">
              <div class="advanced-config-header" @click="toggleAdvancedConfig">
                <span class="advanced-config-title">高级配置 (可选)</span>
                <el-icon class="advanced-config-icon" :class="{ rotated: showAdvancedConfig }">
                  <ArrowUp />
                </el-icon>
              </div>
              <el-collapse-transition>
                <div v-show="showAdvancedConfig" class="advanced-config-content">
                  <!-- 报告风格 -->
                  <div class="advanced-config-group">
                    <label class="advanced-config-label">报告风格</label>
                    <el-radio-group v-model="formData.reportStyle" class="radio-group">
                      <el-radio label="formal">正式</el-radio>
                      <el-radio label="academic">学术</el-radio>
                      <el-radio label="concise">简洁</el-radio>
                    </el-radio-group>
                  </div>

                  <!-- 详细程度 -->
                  <div class="advanced-config-group">
                    <label class="advanced-config-label">详细程度</label>
                    <el-radio-group v-model="formData.detailLevel" class="radio-group">
                      <el-radio label="brief">简要</el-radio>
                      <el-radio label="standard">标准</el-radio>
                      <el-radio label="detailed">详细</el-radio>
                    </el-radio-group>
                  </div>

                  <!-- 特殊要求 -->
                  <div class="advanced-config-group">
                    <label class="advanced-config-label">特殊要求</label>
                    <el-input
                      v-model="formData.specialRequirements"
                      type="textarea"
                      :rows="3"
                      placeholder="您可以输入对报告的特殊要求,如侧重点、格式要求等..."
                      maxlength="500"
                      show-word-limit
                    />
                  </div>
                </div>
              </el-collapse-transition>
            </div>
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
  MagicStick,
  ArrowUp
} from '@element-plus/icons-vue'
import BaseButton from '@/components/Common/BaseButton.vue'
import BaseCard from '@/components/Common/BaseCard.vue'
import WorkflowSelect from '@/components/Business/Form/WorkflowSelect.vue'
import { getProjectList } from '@/api/Project/project'
import { getReportWorkflows } from '@/api/Report/report'
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
  workflowId: '',
  reportType: '',
  // 高级配置
  reportStyle: 'formal', // 报告风格：formal/academic/concise
  detailLevel: 'standard', // 详细程度：brief/standard/detailed
  specialRequirements: '' // 特殊要求
})

// 表单验证规则
const formRules = {
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ],
  workflowId: [
    { required: true, message: '请选择工作流', trigger: 'change' }
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

// 项目统计信息
const projectStats = computed(() => {
  if (!selectedProject.value) {
    return { docCount: '-', wordCount: '-', progress: '-' }
  }
  return {
    docCount: selectedProject.value.docCount || selectedProject.value.fileCount || '0',
    wordCount: selectedProject.value.wordCount || selectedProject.value.totalWords || '0',
    progress: selectedProject.value.progress || selectedProject.value.completionRate || '0%'
  }
})

// 工作流列表
const workflowOptions = ref([])
const workflowsLoading = ref(false)
const selectedWorkflow = computed(() => {
  if (!formData.workflowId) return null
  return workflowOptions.value.find(w => w.id === formData.workflowId)
})

// 高级配置显示状态
const showAdvancedConfig = ref(false)

// 生成状态
const generating = ref(false)

// 是否可以生成
const canGenerate = computed(() => {
  return formData.projectId && formData.workflowId && formData.reportType && !generating.value
})

// 加载项目列表
const loadProjects = async () => {
  projectsLoading.value = true
  try {
    logger.info('开始加载项目列表')
    const response = await getProjectList({
      pageNo: 1,
      pageSize: 1000,
      sortBy: 'createdTime',
      sortOrder: 'DESC'
      // 不传 status，获取所有项目（包括活跃和非活跃的）
    })
    
    if (response.code === 200 && response.data) {
      projectList.value = response.data.records || response.data.list || []
      logger.info(`加载项目列表成功: ${projectList.value.length} 个项目`, {
        total: response.data.total,
        records: projectList.value.length
      })
    } else {
      throw new Error(response.message || '加载项目列表失败')
    }
  } catch (error) {
    logger.error(`加载项目列表失败: ${error.message}`, error)
    ElMessage.error(error.message || '加载项目列表失败，请稍后重试')
  } finally {
    projectsLoading.value = false
  }
}

// 加载工作流列表
const loadWorkflows = async () => {
  workflowsLoading.value = true
  try {
    logger.info('开始加载工作流列表')
    const response = await getReportWorkflows()
    
    // 处理响应数据：支持多种响应格式
    let workflowsData = []
    if (Array.isArray(response?.data)) {
      workflowsData = response.data
    } else if (Array.isArray(response?.data?.data)) {
      workflowsData = response.data.data
    } else if (Array.isArray(response)) {
      workflowsData = response
    } else {
      workflowsData = response?.data || []
    }
    
    // 转换数据格式：将后端返回的格式转换为组件需要的格式
    // 后端格式：{ id (difyApiKeysId), resourceId, keyName, description }
    // id 是 dify_api_keys 表的主键 ID，即 difyApiKeysId
    workflowOptions.value = workflowsData.map(workflow => ({
      id: workflow.id, // 使用 dify_api_keys 表的主键 ID（difyApiKeysId）
      resourceId: workflow.resourceId, // Dify 工作流的 resourceId（保留用于显示）
      name: workflow.keyName || workflow.name || '',
      description: workflow.description || ''
    }))
    
    logger.info(`加载工作流列表成功: ${workflowOptions.value.length} 个工作流`, {
      workflows: workflowOptions.value.map(w => ({ id: w.id, name: w.name }))
    })
  } catch (error) {
    logger.error(`加载工作流列表失败: ${error.message}`, error)
    ElMessage.error('加载工作流列表失败，请稍后重试')
    workflowOptions.value = []
  } finally {
    workflowsLoading.value = false
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

// 工作流选择变化
const handleWorkflowChange = (workflowId) => {
  logger.info(`选择工作流: ID=${workflowId}`)
}

// 切换高级配置显示
const toggleAdvancedConfig = () => {
  showAdvancedConfig.value = !showAdvancedConfig.value
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
      difyApiKeysId: formData.workflowId, // 工作流ID就是difyApiKeysId
      workflowId: formData.workflowId,
      reportType: formData.reportType,
      // 高级配置
      reportStyle: formData.reportStyle,
      detailLevel: formData.detailLevel,
      specialRequirements: formData.specialRequirements || undefined
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
  loadWorkflows()
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

.form-item-half {
  grid-column: span 1;
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

/* 工作流信息卡片 */
.workflow-info-card {
  padding: var(--gap-lg, 20px);
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  margin-top: 0;
}

.workflow-info-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color, #1e3a8a);
  margin-top: 0;
  margin-bottom: 8px;
}

.workflow-info-description {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

/* 信息行容器 */
.info-row-item {
  margin-top: 0;
  margin-bottom: 0;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--gap-xl, 24px);
  width: 100%;
}

.info-item {
  width: 100%;
  min-height: 0;
}

/* 左侧信息项（固定位置） */
.info-item-left {
  grid-column: 1;
}

/* 右侧信息项（固定位置） */
.info-item-right {
  grid-column: 2;
  margin-top: 0;
  padding-top: 0;
}

/* 项目信息卡片 */
.project-info {
  background: linear-gradient(135deg, #f0f9ff 0%, #dbeafe 100%);
  border: 2px solid var(--primary-color-light, #0ea5e9);
  border-radius: 12px;
  padding: var(--gap-lg, 20px);
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.1);
  position: relative;
  overflow: hidden;
  height: 100%;
  box-sizing: border-box;
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

/* 高级配置样式 */
.advanced-config-item {
  margin-top: var(--gap-md, 12px);
}

.advanced-config-wrapper {
  width: 100%;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-color, #ffffff);
}

.advanced-config-header {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: background-color 0.2s;
  user-select: none;

  &:hover {
    background-color: var(--bg-color-secondary, #f9fafb);
  }
}

.advanced-config-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color, #374151);
}

.advanced-config-icon {
  font-size: 16px;
  color: var(--text-color-secondary, #6b7280);
  transition: transform 0.3s;

  &.rotated {
    transform: rotate(180deg);
  }
}

.advanced-config-content {
  padding: 20px;
  border-top: 1px solid var(--border-color-light, #f3f4f6);
  background: var(--bg-color-secondary, #fafbfc);
}

.advanced-config-group {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.advanced-config-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color, #374151);
  margin-bottom: 12px;
}

.radio-group {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

/* 工作流信息卡片样式（参考原型图） */
.workflow-info-card {

  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.workflow-info-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color, #1e3a8a);
  margin-bottom: 8px;
}

.workflow-info-description {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
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

    .info-row {
      grid-template-columns: 1fr;
    }
  }
</style>

