<!--
/**
 * @description 报告生成表单组件
 * 用于在弹窗中显示的报告生成表单
 */
-->
<template>
  <div class="report-generate-form">
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
        <el-form-item :label="$t('report.type')" prop="difyApiKeysId" class="form-item-full">
          <div v-if="reportTypesLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ $t('common.loading') }}</span>
          </div>
          <div v-else-if="reportTypes.length === 0" class="empty-types">
            <p>{{ $t('report.generatePage.noReportTypes') || '暂无报告类型' }}</p>
          </div>
          <div v-else class="type-cards">
            <div
              v-for="reportType in reportTypes"
              :key="reportType.id"
              class="type-card"
              :class="{ selected: formData.difyApiKeysId === String(reportType.id) }"
              @click="handleReportTypeChange(reportType)"
            >
              <el-icon class="type-icon"><Document /></el-icon>
              <h3 class="type-title">{{ reportType.keyName }}</h3>
              <p class="type-description">{{ reportType.description || $t('report.generatePage.typeDefaultDesc') }}</p>
              <span v-if="formData.difyApiKeysId === String(reportType.id)" class="type-badge">
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, defineExpose } from 'vue'
import { ElMessage } from 'element-plus'
import {
  InfoFilled,
  Document,
  Check,
  Loading
} from '@element-plus/icons-vue'
import { getProjectList } from '@/api/Project/project'
import { getReportTypes } from '@/api/Report/report'
import { createLogger } from '@/utils/simpleLogger'

const logger = createLogger('ReportGenerateForm')

// 表单引用
const formRef = ref(null)

// 表单数据
const formData = reactive({
  projectId: null,
  projectName: '',
  projectCode: '',
  projectKnowledgeId: '',
  reportType: '', // 报告类型（保留用于显示和筛选）
  difyApiKeysId: '', // Dify API Keys 表 ID（从报告类型选择中获取的 id）
  summary: ''
})

// 表单验证规则
const formRules = {
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ],
  difyApiKeysId: [
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

// 报告类型列表
const reportTypes = ref([])
const reportTypesLoading = ref(false)

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

// 报告类型选择变化
const handleReportTypeChange = (reportType) => {
  formData.difyApiKeysId = String(reportType.id)
  // 根据 keyName 判断报告类型（如果包含"科技"则为 tech，包含"自评"则为 self）
  if (reportType.keyName && reportType.keyName.includes('科技')) {
    formData.reportType = 'tech'
  } else if (reportType.keyName && reportType.keyName.includes('自评')) {
    formData.reportType = 'self'
  } else {
    // 默认使用 keyName 作为 reportType
    formData.reportType = reportType.keyName || ''
  }
  logger.info(`选择报告类型: ${reportType.keyName} (Dify API Keys ID: ${reportType.id})`)
}

// 重置表单
const resetForm = () => {
  formData.projectId = null
  formData.projectName = ''
  formData.projectCode = ''
  formData.projectKnowledgeId = ''
  formData.reportType = ''
  formData.difyApiKeysId = ''
  formData.summary = ''
  formRef.value?.clearValidate()
}

// 验证表单
const validate = async () => {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch (error) {
    logger.warn('表单验证失败', error)
    return false
  }
}

// 获取表单数据
const getFormData = () => {
  return {
    projectId: formData.projectId,
    projectName: formData.projectName,
    projectCode: formData.projectCode || undefined,
    projectKnowledgeId: formData.projectKnowledgeId || undefined,
    reportType: formData.reportType,
    difyApiKeysId: formData.difyApiKeysId,
    summary: formData.summary || undefined
  }
}

// 暴露方法给父组件
defineExpose({
  validate,
  resetForm,
  getFormData
})

// 加载报告类型列表
const loadReportTypes = async () => {
  reportTypesLoading.value = true
  try {
    const response = await getReportTypes()
    
    if (response.code === 200 && response.data) {
      reportTypes.value = response.data || []
      logger.info(`加载报告类型列表成功: ${reportTypes.value.length} 个类型`)
    } else {
      throw new Error(response.message || '加载报告类型列表失败')
    }
  } catch (error) {
    logger.error(`加载报告类型列表失败: ${error.message}`, error)
    ElMessage.error('加载报告类型列表失败，请稍后重试')
  } finally {
    reportTypesLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadProjects()
  loadReportTypes()
})
</script>

<style lang="scss" scoped>
.report-generate-form {
  .config-form {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--gap-xl, 24px);
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

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: var(--text-color-secondary, #6b7280);
    font-size: 14px;
    gap: var(--gap-sm, 8px);

    .el-icon {
      font-size: 20px;
    }
  }

  .empty-types {
    text-align: center;
    padding: 40px 20px;
    color: var(--text-color-secondary, #9ca3af);
    font-size: 14px;
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
}
</style>

