<!--
/**
 * @description 系统管理 - 系统配置业务组件
 * 负责系统配置的展示、编辑和保存，包括Dify配置
 * 按照原型图设计实现
 */
-->
<template>
  <div class="system-config-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">{{ t('system.config.title') }}</h1>
    </div>

    <!-- 内容卡片 -->
    <BaseCard>
      <div class="config-form">
        <div class="config-section">
          <el-form
            :model="difyConfig"
            :rules="difyRules"
            ref="difyFormRef"
            label-width="140px"
          >
            <el-form-item :label="t('system.config.difyApiUrl')" prop="apiUrl">
              <el-input
                v-model="difyConfig.apiUrl"
                :placeholder="t('system.config.difyApiUrlPlaceholder')"
              />
              <div class="help-text">{{ t('system.config.difyApiUrlHelp') }}</div>
            </el-form-item>

            <el-form-item :label="t('system.config.apiKey')" prop="apiKey">
              <el-input
                v-model="difyConfig.apiKey"
                type="password"
                :placeholder="t('system.config.apiKeyPlaceholder')"
                show-password
              />
              <div class="help-text">{{ t('system.config.apiKeyHelp') }}</div>
            </el-form-item>

            <el-form-item :label="t('system.config.workflowId')" prop="workflowId">
              <el-input
                v-model="difyConfig.workflowId"
                :placeholder="t('system.config.workflowIdPlaceholder')"
              />
              <div class="help-text">{{ t('system.config.workflowIdHelp') }}</div>
            </el-form-item>

            <el-form-item :label="t('system.config.timeout')" prop="timeout">
              <el-input
                v-model.number="difyConfig.timeout"
                type="number"
              >
                <template #append>{{ t('system.config.seconds') }}</template>
              </el-input>
              <div class="help-text">{{ t('system.config.timeoutHelp') }}</div>
            </el-form-item>

            <el-form-item :label="t('system.config.workflowName')" prop="workflowName">
              <el-input
                v-model="difyConfig.workflowName"
                :placeholder="t('system.config.workflowNamePlaceholder')"
              />
              <div class="help-text">{{ t('system.config.workflowNameHelp') }}</div>
            </el-form-item>

            <el-form-item :label="t('system.config.workflowDescription')" prop="workflowDescription">
              <el-input
                v-model="difyConfig.workflowDescription"
                type="textarea"
                :rows="3"
                :placeholder="t('system.config.workflowDescriptionPlaceholder')"
              />
              <div class="help-text">{{ t('system.config.workflowDescriptionHelp') }}</div>
            </el-form-item>
          </el-form>
        </div>

        <div class="action-bar">
          <el-button
            type="primary"
            @click="handleSave"
            :loading="saving"
          >
            {{ t('system.config.saveConfig') }}
          </el-button>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import BaseCard from '@/components/Common/BaseCard.vue'
import {
  validateApiUrl
} from '@/utils/validate'
import { createLogger } from '@/utils/simpleLogger'
import { getDifyConfig, saveDifyConfig } from '@/api/System/system'

const { t } = useI18n()
const logger = createLogger('SystemConfig')

// 响应式数据
const saving = ref(false)
const loading = ref(false)

// 表单引用
const difyFormRef = ref()

// Dify配置
const difyConfig = reactive({
  apiUrl: '',
  apiKey: '',
  workflowId: '',
  timeout: 60,
  workflowName: '',
  workflowDescription: ''
})

// Dify配置验证规则
const difyRules = {
  apiUrl: [
    { required: true, message: t('system.config.apiUrlRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validateApiUrl(value)) {
          callback(new Error(t('system.config.apiUrlFormat')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  apiKey: [
    { required: true, message: t('system.config.apiKeyRequired'), trigger: 'blur' }
  ],
  workflowId: [
    { required: true, message: t('system.config.workflowIdRequired'), trigger: 'blur' }
  ],
  timeout: [
    { required: true, message: t('system.config.timeoutRequired'), trigger: 'blur' },
    { type: 'number', message: t('system.config.timeoutFormat'), trigger: 'blur' }
  ],
  workflowName: [
    { required: true, message: t('system.config.workflowNameRequired'), trigger: 'blur' }
  ],
  workflowDescription: [
    { required: true, message: t('system.config.workflowDescriptionRequired'), trigger: 'blur' }
  ]
}

// 加载Dify配置
const loadDifyConfig = async () => {
  try {
    loading.value = true
    // TODO: 后端接口完成后取消注释
    // const response = await getDifyConfig()
    // const data = response?.data?.data || response?.data || {}
    // if (data) {
    //   difyConfig.apiUrl = data.apiUrl || ''
    //   difyConfig.apiKey = data.apiKey || ''
    //   difyConfig.workflowId = data.workflowId || ''
    //   difyConfig.timeout = data.timeout || 60
    //   difyConfig.workflowName = data.workflowName || ''
    //   difyConfig.workflowDescription = data.workflowDescription || ''
    // }
    
    // 占位数据（空值，用户需要自己填写）
    difyConfig.apiUrl = ''
    difyConfig.apiKey = ''
    difyConfig.workflowId = ''
    difyConfig.timeout = 60
    difyConfig.workflowName = ''
    difyConfig.workflowDescription = ''
    
    logger.info('Dify config loaded', difyConfig)
  } catch (error) {
    logger.error('Failed to load Dify config', error)
    ElMessage.error('加载Dify配置失败')
    // 使用默认值
    difyConfig.apiUrl = ''
    difyConfig.apiKey = ''
    difyConfig.workflowId = ''
    difyConfig.timeout = 60
    difyConfig.workflowName = ''
    difyConfig.workflowDescription = ''
  } finally {
    loading.value = false
  }
}

// 保存配置
const handleSave = async () => {
  try {
    // 验证表单
    await difyFormRef.value.validate()

    saving.value = true

    // TODO: 后端接口完成后取消注释
    // const response = await saveDifyConfig({
    //   apiUrl: difyConfig.apiUrl,
    //   apiKey: difyConfig.apiKey,
    //   workflowId: difyConfig.workflowId,
    //   timeout: difyConfig.timeout,
    //   workflowName: difyConfig.workflowName,
    //   workflowDescription: difyConfig.workflowDescription
    // })
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000))
    logger.info('Dify config saved', difyConfig)

    ElMessage.success(t('system.config.saveSuccess'))
    saving.value = false
  } catch (error) {
    saving.value = false
    if (error !== false) {
      logger.error('Failed to save system config', error)
      ElMessage.error(t('system.config.saveFailed'))
    }
  }
}

// 组件挂载时加载配置
onMounted(() => {
  loadDifyConfig()
})
</script>

<style lang="scss" scoped>
.system-config-container {
  padding: var(--gap-lg);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gap-lg);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
}

.config-form {
  margin-top: var(--gap-lg);
  max-width: 800px;
}

.config-form :deep(.el-form-item) {
  margin-bottom: var(--gap-lg);
}

.config-section {
  margin-bottom: var(--gap-xl);
}


.help-text {
  font-size: var(--font-size-sm);
  color: var(--text-3);
  margin-top: var(--gap-xs);
  line-height: 1.5;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: var(--gap-md);
  padding-top: var(--gap-lg);
  border-top: 1px solid var(--border-color);
  margin-top: var(--gap-xl);
}



:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-1);
}

:deep(.el-input__wrapper) {
  border-radius: var(--border-radius-sm);
}

:deep(.el-textarea__inner) {
  border-radius: var(--border-radius-sm);
}

:deep(.el-select .el-input__wrapper) {
  border-radius: var(--border-radius-sm);
}

:deep(.el-switch) {
  margin-right: var(--gap-sm);
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: var(--gap-sm);
}

:deep(.el-checkbox) {
  margin-right: 0;
}

:deep(.el-radio-group) {
  display: flex;
  gap: var(--gap-lg);
}

// 按钮样式统一 - 与系统主题保持一致
:deep(.el-button) {
  border-radius: var(--border-radius-md);
  font-weight: 500;
  font-size: var(--font-size-base);
  transition: all 0.2s ease;
}

:deep(.el-button--primary) {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--surface);
}

:deep(.el-button--primary:hover) {
  background-color: var(--color-primary-dark);
  border-color: var(--color-primary-dark);
}

:deep(.el-button:not(.el-button--primary)) {
  background-color: var(--bg-1);
  border-color: var(--border-color);
  color: var(--text-1);
}

:deep(.el-button:not(.el-button--primary):hover) {
  background-color: var(--bg-2);
  border-color: var(--border-color-hover);
  color: var(--text-1);
}
</style>
