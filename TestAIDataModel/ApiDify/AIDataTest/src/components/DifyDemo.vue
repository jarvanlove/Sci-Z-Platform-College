<template>
  <div class="dify-demo">
    <div class="demo-header">
      <h1>Dify 知识库 API 演示</h1>
      <p>使用 axios 调用 Dify API 进行知识库查询</p>
    </div>

    <div class="demo-content">
      <!-- 数据集列表 -->
      <div class="section">
        <h2>数据集列表</h2>
        <div class="controls">
          <button @click="loadDatasets" :disabled="loading" class="btn-primary">
            {{ loading ? '加载中...' : '获取数据集列表' }}
          </button>
          <select v-model="selectedCategory" @change="loadDatasets" class="select">
            <option value="">全部分类</option>
            <option value="api">API</option>
            <option value="document">文档</option>
            <option value="qa">问答</option>
          </select>
        </div>
        
        <div v-if="datasets.length > 0" class="dataset-list">
          <div v-for="dataset in datasets" :key="dataset.id" class="dataset-card">
            <h3>{{ dataset.name }}</h3>
            <p v-if="dataset.description">{{ dataset.description }}</p>
            <div class="dataset-info">
              <span>文档数: {{ dataset.document_count }}</span>
              <span>字数: {{ dataset.word_count }}</span>
              <span>应用数: {{ dataset.app_count }}</span>
            </div>
            <button @click="selectDataset(dataset)" class="btn-secondary">
              选择此数据集
            </button>
          </div>
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
      </div>

      <!-- 知识库查询 -->
      <div class="section" v-if="selectedDataset">
        <h2>知识库查询</h2>
        <div class="query-form">
          <div class="form-group">
            <label>选择数据集:</label>
            <select v-model="selectedDataset" class="select">
              <option v-for="dataset in datasets" :key="dataset.id" :value="dataset">
                {{ dataset.name }}
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label>查询内容:</label>
            <textarea 
              v-model="queryText" 
              placeholder="请输入您要查询的内容..."
              class="textarea"
              rows="3"
            ></textarea>
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
          
          <button 
            @click="performQuery" 
            :disabled="!queryText.trim() || queryLoading" 
            class="btn-primary"
          >
            {{ queryLoading ? '查询中...' : '开始查询' }}
          </button>
        </div>
        
        <!-- 查询结果 -->
        <div v-if="queryResults.length > 0" class="query-results">
          <h3>查询结果 (共 {{ queryResults.length }} 条)</h3>
          <div v-for="result in queryResults" :key="result.segment.id" class="result-card">
            <div class="result-header">
              <span class="result-title">{{ result.segment.document.name || '无标题' }}</span>
              <span class="result-score">相关性: {{ result.score.toFixed(3) }}</span>
            </div>
            <div class="result-content">{{ result.segment.content }}</div>
            <div class="result-meta">
              <span>文档ID: {{ result.segment.document_id }}</span>
              <span>字数: {{ result.segment.word_count }}</span>
              <span>状态: {{ result.segment.status }}</span>
            </div>
            <div v-if="result.segment.keywords && result.segment.keywords.length > 0" class="result-keywords">
              <strong>关键词:</strong> {{ result.segment.keywords.join(', ') }}
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
import { difyApiService, type DifyDataset, type DifyRetrievalRecord } from '../services/difyApi'

// 响应式数据
const loading = ref(false)
const queryLoading = ref(false)
const error = ref('')
const queryError = ref('')
const datasets = ref<DifyDataset[]>([])
const selectedDataset = ref<DifyDataset | null>(null)
const selectedCategory = ref('')
const queryText = ref('')
const queryResults = ref<DifyRetrievalRecord[]>([])
const topK = ref(5)
const scoreThreshold = ref(0.5)

// 加载数据集列表
const loadDatasets = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await difyApiService.getDatasets(selectedCategory.value || undefined)
    datasets.value = response.data
    console.log('数据集列表:', response)
  } catch (err: any) {
    error.value = `加载数据集失败: ${err.message || err}`
    console.error('加载数据集失败:', err)
  } finally {
    loading.value = false
  }
}

// 选择数据集
const selectDataset = (dataset: DifyDataset) => {
  selectedDataset.value = dataset
  console.log('选择数据集:', dataset)
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
        search_method: 'semantic_search',
        top_k: topK.value,
        score_threshold_enabled: true,
        score_threshold: scoreThreshold.value,
        reranking_enable: false
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

// 组件挂载时加载数据集
onMounted(() => {
  loadDatasets()
})
</script>

<style scoped>
.dify-demo {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.demo-header {
  text-align: center;
  margin-bottom: 40px;
}

.demo-header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.demo-header p {
  color: #7f8c8d;
  font-size: 16px;
}

.section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #e9ecef;
}

.section h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 20px;
}

.controls {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
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

.select, .input, .textarea {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.textarea {
  width: 100%;
  resize: vertical;
  font-family: inherit;
}

.dataset-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.dataset-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.dataset-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.dataset-card h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
}

.dataset-card p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0 0 12px 0;
}

.dataset-info {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 12px;
  color: #95a5a6;
}

.query-form {
  display: grid;
  gap: 16px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.query-results {
  margin-top: 24px;
}

.query-results h3 {
  color: #2c3e50;
  margin-bottom: 16px;
}

.result-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  transition: box-shadow 0.2s;
}

.result-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.result-title {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.result-score {
  background: #e8f5e8;
  color: #27ae60;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.result-content {
  color: #34495e;
  line-height: 1.5;
  margin-bottom: 8px;
  font-size: 14px;
}

.result-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #7f8c8d;
}

.result-meta a {
  color: #3498db;
  text-decoration: none;
}

.result-meta a:hover {
  text-decoration: underline;
}

.result-keywords {
  margin-top: 8px;
  font-size: 12px;
  color: #7f8c8d;
  background: #f8f9fa;
  padding: 6px 8px;
  border-radius: 4px;
  border-left: 3px solid #3498db;
}

.error-message {
  background: #fdf2f2;
  color: #e53e3e;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #fecaca;
  margin-top: 12px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .dify-demo {
    padding: 16px;
  }
  
  .controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .dataset-list {
    grid-template-columns: 1fr;
  }
  
  .result-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
