<template>
  <div class="query-page">
    <div class="page-header">
      <h1>知识库查询</h1>
      <p v-if="selectedDataset">正在查询: {{ selectedDataset.name }}</p>
      <p v-else>选择数据集进行知识库查询</p>
    </div>

    <div class="query-content">
      <!-- 数据集选择 -->
      <div class="dataset-selection" v-if="!selectedDataset">
        <h2>选择数据集</h2>
        <div class="datasets-grid">
          <div v-for="dataset in datasets" :key="dataset.id" class="dataset-card" @click="selectDataset(dataset)">
            <h3>{{ dataset.name }}</h3>
            <p v-if="dataset.description">{{ dataset.description }}</p>
            <div class="dataset-info">
              <span>文档数: {{ dataset.document_count }}</span>
              <span>字数: {{ dataset.word_count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 查询表单 -->
      <div v-if="selectedDataset" class="query-form-section">
        <div class="query-form">
          <div class="form-group">
            <label>查询内容:</label>
            <textarea 
              v-model="queryText" 
              placeholder="请输入您要查询的内容..."
              class="textarea"
              rows="4"
            ></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>检索方法:</label>
              <select v-model="searchMethod" class="select">
                <option value="semantic_search">语义检索</option>
                <option value="keyword_search">关键字检索</option>
                <option value="full_text_search">全文检索</option>
                <option value="hybrid_search">混合检索</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>返回结果数量:</label>
              <input 
                v-model.number="topK" 
                type="number" 
                min="1" 
                max="20" 
                class="input"
              />
            </div>
            
            <div class="form-group">
              <label>相关性阈值:</label>
              <input 
                v-model.number="scoreThreshold" 
                type="number" 
                min="0" 
                max="1" 
                step="0.1" 
                class="input"
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="enableReranking" />
                启用重排序
              </label>
            </div>
            
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="enableScoreThreshold" />
                启用分数阈值
              </label>
            </div>
          </div>
          
          <div class="form-actions">
            <button 
              @click="performQuery" 
              :disabled="!queryText.trim() || queryLoading" 
              class="btn-primary"
            >
              {{ queryLoading ? '查询中...' : '开始查询' }}
            </button>
            <button @click="clearResults" class="btn-secondary">
              清空结果
            </button>
          </div>
        </div>
        
        <!-- 查询结果 -->
        <div v-if="queryResults.length > 0" class="query-results">
          <div class="results-header">
            <h3>查询结果 (共 {{ queryResults.length }} 条)</h3>
            <div class="results-info">
              <span>查询内容: "{{ queryText }}"</span>
              <span>检索方法: {{ getSearchMethodName(searchMethod) }}</span>
            </div>
          </div>
          
          <div class="results-list">
            <div v-for="result in queryResults" :key="result.segment.id" class="result-card">
              <div class="result-header">
                <div class="result-title">
                  <h4>{{ result.segment.document.name || '无标题' }}</h4>
                  <span class="result-score">相关性: {{ result.score.toFixed(4) }}</span>
                </div>
              </div>
              
              <div class="result-content">
                {{ result.segment.content }}
              </div>
              
              <div class="result-meta">
                <div class="meta-row">
                  <span>文档ID: {{ result.segment.document_id }}</span>
                  <span>字数: {{ result.segment.word_count }}</span>
                  <span>Token数: {{ result.segment.tokens }}</span>
                </div>
                <div class="meta-row">
                  <span>状态: {{ result.segment.status }}</span>
                  <span>创建时间: {{ formatDate(result.segment.created_at) }}</span>
                </div>
              </div>
              
              <div v-if="result.segment.keywords && result.segment.keywords.length > 0" class="result-keywords">
                <strong>关键词:</strong>
                <div class="keywords-list">
                  <span v-for="keyword in result.segment.keywords" :key="keyword" class="keyword-tag">
                    {{ keyword }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="queryError" class="error-message">
          {{ queryError }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { difyApiService, type DifyDataset, type DifyRetrievalRecord } from '../services/difyApi'

const route = useRoute()

// 响应式数据
const datasets = ref<DifyDataset[]>([])
const selectedDataset = ref<DifyDataset | null>(null)
const queryText = ref('')
const queryResults = ref<DifyRetrievalRecord[]>([])
const queryLoading = ref(false)
const queryError = ref('')

// 查询参数
const searchMethod = ref<'semantic_search' | 'keyword_search' | 'full_text_search' | 'hybrid_search'>('semantic_search')
const topK = ref(5)
const scoreThreshold = ref(0.5)
const enableReranking = ref(false)
const enableScoreThreshold = ref(true)

// 加载数据集列表
const loadDatasets = async () => {
  try {
    const response = await difyApiService.getDatasets()
    datasets.value = response.data
    
    // 如果URL中有datasetId参数，自动选择该数据集
    const datasetId = route.query.datasetId as string
    if (datasetId) {
      const dataset = datasets.value.find(d => d.id === datasetId)
      if (dataset) {
        selectedDataset.value = dataset
      }
    }
  } catch (error) {
    console.error('加载数据集失败:', error)
  }
}

// 选择数据集
const selectDataset = (dataset: DifyDataset) => {
  selectedDataset.value = dataset
}

// 执行查询
const performQuery = async () => {
  if (!selectedDataset.value || !queryText.value.trim()) {
    return
  }
  
  queryLoading.value = true
  queryError.value = ''
  queryResults.value = []
  
  try {
    const response = await difyApiService.retrieval(selectedDataset.value.id, {
      query: queryText.value.trim(),
      retrieval_model: {
        search_method: searchMethod.value,
        top_k: topK.value,
        score_threshold_enabled: enableScoreThreshold.value,
        score_threshold: enableScoreThreshold.value ? scoreThreshold.value : undefined,
        reranking_enable: enableReranking.value
      }
    })
    
    queryResults.value = response.records
    console.log('查询结果:', response)
  } catch (err: any) {
    queryError.value = `查询失败: ${err.message || err}`
    console.error('查询失败:', err)
  } finally {
    queryLoading.value = false
  }
}

// 清空结果
const clearResults = () => {
  queryResults.value = []
  queryError.value = ''
}

// 获取检索方法名称
const getSearchMethodName = (method: string) => {
  const names: Record<string, string> = {
    semantic_search: '语义检索',
    keyword_search: '关键字检索',
    full_text_search: '全文检索',
    hybrid_search: '混合检索'
  }
  return names[method] || method
}

// 格式化日期
const formatDate = (timestamp: number) => {
  return new Date(timestamp * 1000).toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadDatasets()
})
</script>

<style scoped>
.query-page {
  max-width: 1200px;
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

.dataset-selection h2 {
  color: #2c3e50;
  margin-bottom: 20px;
}

.datasets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.dataset-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dataset-card:hover {
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
  transform: translateY(-2px);
  border-color: #667eea;
}

.dataset-card h3 {
  color: #2c3e50;
  margin-bottom: 8px;
  font-size: 1.2rem;
}

.dataset-card p {
  color: #7f8c8d;
  margin-bottom: 12px;
  line-height: 1.5;
}

.dataset-info {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #95a5a6;
}

.query-form-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0,0.1);
}

.query-form {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.textarea, .select, .input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.textarea {
  resize: vertical;
  font-family: inherit;
}

.textarea:focus, .select:focus, .input:focus {
  outline: none;
  border-color: #667eea;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
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
  transform: translateY(-1px);
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

.query-results {
  margin-top: 30px;
}

.results-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
}

.results-header h3 {
  color: #2c3e50;
  margin-bottom: 8px;
}

.results-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #7f8c8d;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-card {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  transition: box-shadow 0.2s;
}

.result-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.result-header {
  margin-bottom: 12px;
}

.result-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.result-title h4 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.1rem;
}

.result-score {
  background: #e8f5e8;
  color: #27ae60;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.result-content {
  color: #34495e;
  line-height: 1.6;
  margin-bottom: 12px;
  font-size: 14px;
}

.result-meta {
  margin-bottom: 12px;
}

.meta-row {
  display: flex;
  gap: 16px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #7f8c8d;
}

.result-keywords {
  margin-top: 12px;
}

.result-keywords strong {
  color: #2c3e50;
  font-size: 12px;
  margin-bottom: 8px;
  display: block;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.keyword-tag {
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
}

.error-message {
  background: #fdf2f2;
  color: #e53e3e;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #fecaca;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .query-page {
    padding: 16px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .results-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .meta-row {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
