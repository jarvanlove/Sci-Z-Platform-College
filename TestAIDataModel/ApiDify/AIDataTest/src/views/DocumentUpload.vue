<template>
  <div class="document-upload-page">
    <div class="page-header">
      <h1>文档上传</h1>
      <p>将文件上传到知识库中，支持多种文档格式</p>
    </div>

    <div class="upload-content">
      <!-- 数据集选择 -->
      <div class="section">
        <h2>选择知识库</h2>
        <div class="dataset-selector">
          <select v-model="selectedDatasetId" @change="loadDatasetInfo" class="select">
            <option value="">请选择知识库</option>
            <option v-for="dataset in datasets" :key="dataset.id" :value="dataset.id">
              {{ dataset.name }}
            </option>
          </select>
          <button @click="loadDatasets" :disabled="loading" class="btn-secondary">
            {{ loading ? '加载中...' : '刷新' }}
          </button>
        </div>
        
        <div v-if="selectedDataset" class="dataset-info">
          <h3>{{ selectedDataset.name }}</h3>
          <p v-if="selectedDataset.description">{{ selectedDataset.description }}</p>
          <div class="dataset-stats">
            <span>文档数: {{ selectedDataset.document_count }}</span>
            <span>字数: {{ selectedDataset.word_count.toLocaleString() }}</span>
            <span>索引技术: {{ selectedDataset.indexing_technique }}</span>
          </div>
        </div>
      </div>

      <!-- 文件上传 -->
      <div class="section" v-if="selectedDatasetId">
        <h2>上传文件</h2>
        <FileUpload
          :accepted-types="acceptedTypes"
          :max-size="maxFileSize"
          :uploading="uploading"
          :upload-progress="uploadProgress"
          @file-selected="handleFileSelected"
          @file-removed="handleFileRemoved"
        />
      </div>

      <!-- 上传配置 -->
      <div class="section" v-if="selectedFile && selectedDatasetId">
        <h2>上传配置</h2>
        
        <div class="config-grid">
          <!-- 索引技术 -->
          <div class="config-group">
            <label>索引技术:</label>
            <select v-model="uploadConfig.indexing_technique" class="select">
              <option value="high_quality">高质量 (向量索引)</option>
              <option value="economy">经济 (关键词索引)</option>
            </select>
          </div>

          <!-- 文档形式 - 由数据集决定，不可修改 -->
          <div class="config-group">
            <label>文档形式:</label>
            <div class="readonly-field">
              <span class="readonly-text">由数据集设置决定</span>
            </div>
          </div>

          <!-- 处理模式 -->
          <div class="config-group">
            <label>处理模式:</label>
            <select v-model="uploadConfig.process_rule.mode" class="select">
              <option value="automatic">自动</option>
              <option value="custom">自定义</option>
              <option value="hierarchical">层次</option>
            </select>
          </div>

          <!-- 语言设置 - 由数据集决定 -->
          <div class="config-group">
            <label>文档语言:</label>
            <div class="readonly-field">
              <span class="readonly-text">由数据集设置决定</span>
            </div>
          </div>
        </div>

        <!-- 自定义配置 -->
        <div v-if="uploadConfig.process_rule.mode === 'custom'" class="custom-config">
          <h3>自定义处理规则</h3>
          
          <div class="config-row">
            <div class="config-group">
              <label>分段分隔符:</label>
              <input v-model="customRules.segmentation.separator" class="input" placeholder="默认: \n" />
            </div>
            
            <div class="config-group">
              <label>最大Token数:</label>
              <input v-model.number="customRules.segmentation.max_tokens" type="number" class="input" placeholder="默认: 1000" />
            </div>
          </div>

          <div class="config-row">
            <div class="config-group">
              <label>分段重叠:</label>
              <input v-model.number="customRules.segmentation.chunk_overlap" type="number" class="input" placeholder="可选" />
            </div>
          </div>

          <div class="preprocessing-rules">
            <h4>预处理规则:</h4>
            <div class="rule-item">
              <label>
                <input type="checkbox" v-model="removeExtraSpacesEnabled" />
                移除多余空格和换行符
              </label>
            </div>
            <div class="rule-item">
              <label>
                <input type="checkbox" v-model="removeUrlsEmailsEnabled" />
                删除URL和邮箱地址
              </label>
            </div>
          </div>
        </div>

        <!-- 检索配置 -->
        <div class="retrieval-config">
          <h3>检索配置</h3>
          
          <div class="config-grid">
            <div class="config-group">
              <label>检索方法:</label>
              <select v-model="retrievalModelSearchMethod" class="select">
                <option value="hybrid_search">混合检索</option>
                <option value="semantic_search">语义检索</option>
                <option value="full_text_search">全文检索</option>
              </select>
            </div>

            <div class="config-group">
              <label>返回结果数:</label>
              <input v-model.number="retrievalModelTopK" type="number" min="1" max="20" class="input" />
            </div>

            <div class="config-group">
              <label>
                <input type="checkbox" v-model="retrievalModelRerankingEnable" />
                启用重排序
              </label>
            </div>

            <div class="config-group">
              <label>
                <input type="checkbox" v-model="retrievalModelScoreThresholdEnabled" />
                启用相关性阈值
              </label>
            </div>

            <div class="config-group" v-if="retrievalModelScoreThresholdEnabled">
              <label>相关性阈值:</label>
              <input v-model.number="retrievalModelScoreThreshold" type="number" min="0" max="1" step="0.1" class="input" />
            </div>
          </div>
        </div>

        <!-- 上传按钮 -->
        <div class="upload-actions">
          <button 
            @click="uploadDocument" 
            :disabled="!selectedFile || uploading"
            class="btn-primary"
          >
            {{ uploading ? '上传中...' : '开始上传' }}
          </button>
          <button @click="resetForm" class="btn-secondary">
            重置
          </button>
        </div>
      </div>

      <!-- 上传结果 -->
      <div v-if="uploadResult" class="section">
        <h2>上传结果</h2>
        <div class="result-card" :class="{ 'success': uploadResult.success, 'error': !uploadResult.success }">
          <div class="result-icon">
            {{ uploadResult.success ? '✅' : '❌' }}
          </div>
          <div class="result-content">
            <h3>{{ uploadResult.success ? '上传成功' : '上传失败' }}</h3>
            <p v-if="uploadResult.success">
              文档已成功上传到知识库，文档ID: {{ uploadResult.documentId }}
            </p>
            <p v-else>
              {{ uploadResult.error }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import FileUpload from '../components/FileUpload.vue'
import { difyApiService, type DifyDataset, type DocumentUploadData } from '../services/difyApi'

const route = useRoute()

// 响应式数据
const loading = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const datasets = ref<DifyDataset[]>([])
const selectedDatasetId = ref('')
const selectedFile = ref<File | null>(null)
const uploadResult = ref<{ success: boolean; documentId?: string; error?: string } | null>(null)

// 配置
const acceptedTypes = '.txt,.pdf,.doc,.docx,.md,.html'
const maxFileSize = 10 * 1024 * 1024 // 10MB

// 上传配置
const uploadConfig = ref<DocumentUploadData>({
  indexing_technique: 'high_quality',
  process_rule: {
    mode: 'automatic'
  },
  retrieval_model: {
    search_method: 'hybrid_search',
    reranking_enable: false,
    top_k: 5,
    score_threshold_enabled: true,
    score_threshold: 0.5
  }
})

// 自定义规则
const customRules = ref({
  segmentation: {
    separator: '\n',
    max_tokens: 1000,
    chunk_overlap: 0
  },
  pre_processing_rules: [
    { id: 'remove_extra_spaces' as const, enabled: true },
    { id: 'remove_urls_emails' as const, enabled: true }
  ]
})

// 计算属性
const selectedDataset = computed(() => {
  return datasets.value.find(d => d.id === selectedDatasetId.value)
})

// 预处理规则的计算属性
const removeExtraSpacesEnabled = computed({
  get: () => customRules.value.pre_processing_rules[0]?.enabled ?? true,
  set: (value: boolean) => {
    if (customRules.value.pre_processing_rules[0]) {
      customRules.value.pre_processing_rules[0].enabled = value
    }
  }
})

const removeUrlsEmailsEnabled = computed({
  get: () => customRules.value.pre_processing_rules[1]?.enabled ?? true,
  set: (value: boolean) => {
    if (customRules.value.pre_processing_rules[1]) {
      customRules.value.pre_processing_rules[1].enabled = value
    }
  }
})

// 检索模型的计算属性
const retrievalModelSearchMethod = computed({
  get: () => uploadConfig.value.retrieval_model?.search_method ?? 'hybrid_search',
  set: (value: string) => {
    if (uploadConfig.value.retrieval_model) {
      uploadConfig.value.retrieval_model.search_method = value as any
    }
  }
})

const retrievalModelTopK = computed({
  get: () => uploadConfig.value.retrieval_model?.top_k ?? 5,
  set: (value: number) => {
    if (uploadConfig.value.retrieval_model) {
      uploadConfig.value.retrieval_model.top_k = value
    }
  }
})

const retrievalModelRerankingEnable = computed({
  get: () => uploadConfig.value.retrieval_model?.reranking_enable ?? false,
  set: (value: boolean) => {
    if (uploadConfig.value.retrieval_model) {
      uploadConfig.value.retrieval_model.reranking_enable = value
    }
  }
})

const retrievalModelScoreThresholdEnabled = computed({
  get: () => uploadConfig.value.retrieval_model?.score_threshold_enabled ?? false,
  set: (value: boolean) => {
    if (uploadConfig.value.retrieval_model) {
      uploadConfig.value.retrieval_model.score_threshold_enabled = value
    }
  }
})

const retrievalModelScoreThreshold = computed({
  get: () => uploadConfig.value.retrieval_model?.score_threshold ?? 0.5,
  set: (value: number) => {
    if (uploadConfig.value.retrieval_model) {
      uploadConfig.value.retrieval_model.score_threshold = value
    }
  }
})

// 方法
const loadDatasets = async () => {
  loading.value = true
  try {
    const response = await difyApiService.getDatasets()
    datasets.value = response.data
  } catch (error) {
    console.error('加载数据集失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDatasetInfo = () => {
  if (selectedDatasetId.value) {
    // 可以在这里加载数据集详细信息
    console.log('选择数据集:', selectedDataset.value)
  }
}

const handleFileSelected = (file: File) => {
  selectedFile.value = file
  uploadResult.value = null
}

const handleFileRemoved = () => {
  selectedFile.value = null
  uploadResult.value = null
}

const uploadDocument = async () => {
  if (!selectedFile.value || !selectedDatasetId.value) return

  uploading.value = true
  uploadProgress.value = 0
  uploadResult.value = null

  try {
    // 构建上传数据 - 按照 API 文档格式
    const uploadData: DocumentUploadData = {
      indexing_technique: 'high_quality',
      process_rule: {
        mode: 'custom',
        rules: {
          pre_processing_rules: [
            { id: 'remove_extra_spaces', enabled: true },
            { id: 'remove_urls_emails', enabled: true }
          ],
          segmentation: {
            separator: '###',
            max_tokens: 500
          }
        }
      }
    }

    // 根据用户配置更新规则
    if (uploadConfig.value.process_rule.mode === 'custom') {
      uploadData.process_rule.rules = {
        pre_processing_rules: customRules.value.pre_processing_rules.map(rule => ({
          id: rule.id as 'remove_extra_spaces' | 'remove_urls_emails',
          enabled: rule.enabled
        })),
        segmentation: {
          separator: customRules.value.segmentation.separator,
          max_tokens: customRules.value.segmentation.max_tokens,
          chunk_overlap: customRules.value.segmentation.chunk_overlap
        }
      }
    } else if (uploadConfig.value.process_rule.mode === 'automatic') {
      // 自动模式，移除自定义规则
      uploadData.process_rule = {
        mode: 'automatic'
      }
    }

    console.log('上传数据:', uploadData)
    console.log('选择的文件:', selectedFile.value)
    console.log('文件详情:', {
      name: selectedFile.value?.name,
      size: selectedFile.value?.size,
      type: selectedFile.value?.type
    })

    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 200)

    const response = await difyApiService.createDocumentByFile(
      selectedDatasetId.value,
      selectedFile.value,
      uploadData
    )

    clearInterval(progressInterval)
    uploadProgress.value = 100

    uploadResult.value = {
      success: true,
      documentId: response.document.id
    }

    // 3秒后重置表单
    setTimeout(() => {
      resetForm()
    }, 3000)

  } catch (error: any) {
    uploadResult.value = {
      success: false,
      error: error.message || '上传失败'
    }
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

const resetForm = () => {
  selectedFile.value = null
  uploadResult.value = null
  uploadConfig.value = {
    indexing_technique: 'high_quality',
    process_rule: {
      mode: 'automatic'
    },
    retrieval_model: {
      search_method: 'hybrid_search',
      reranking_enable: false,
      top_k: 5,
      score_threshold_enabled: true,
      score_threshold: 0.5
    }
  }
}

// 组件挂载时加载数据集
onMounted(() => {
  loadDatasets()
  
  // 如果从数据集页面跳转过来，自动选择数据集
  if (route.query.datasetId) {
    selectedDatasetId.value = route.query.datasetId as string
  }
})
</script>

<style scoped>
.document-upload-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 1.1rem;
  color: #7f8c8d;
}

.upload-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.section h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.5rem;
}

.dataset-selector {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.select, .input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.select:focus, .input:focus {
  outline: none;
  border-color: #667eea;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5a6fd8;
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.dataset-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.dataset-info h3 {
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.dataset-info p {
  color: #7f8c8d;
  margin: 0 0 12px 0;
}

.dataset-stats {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #7f8c8d;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.config-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.config-group label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.readonly-field {
  padding: 10px 12px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  color: #6c757d;
  font-size: 14px;
}

.readonly-text {
  color: #6c757d;
  font-style: italic;
}

.config-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.custom-config {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.custom-config h3 {
  color: #2c3e50;
  margin: 0 0 16px 0;
  font-size: 1.2rem;
}

.preprocessing-rules {
  margin-top: 16px;
}

.preprocessing-rules h4 {
  color: #2c3e50;
  margin: 0 0 12px 0;
  font-size: 1rem;
}

.rule-item {
  margin-bottom: 8px;
}

.rule-item label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #2c3e50;
}

.retrieval-config {
  background: #f0f4ff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.retrieval-config h3 {
  color: #2c3e50;
  margin: 0 0 16px 0;
  font-size: 1.2rem;
}

.upload-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}

.result-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid;
}

.result-card.success {
  background: #f0f9ff;
  border-color: #0ea5e9;
}

.result-card.error {
  background: #fef2f2;
  border-color: #f87171;
}

.result-icon {
  font-size: 2rem;
}

.result-content h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.result-content p {
  margin: 0;
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .document-upload-page {
    padding: 16px;
  }
  
  .dataset-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .config-grid {
    grid-template-columns: 1fr;
  }
  
  .config-row {
    grid-template-columns: 1fr;
  }
  
  .upload-actions {
    flex-direction: column;
  }
}
</style>
