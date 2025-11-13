<template>
  <div class="datasets-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1>数据集管理</h1>
          <p>查看和管理所有可用的知识库数据集</p>
        </div>
        <div class="header-actions">
          <router-link to="/create-dataset" class="btn btn-primary">
            <span class="btn-icon">➕</span>
            创建知识库
          </router-link>
        </div>
      </div>
    </div>

    <div class="datasets-content">
      <!-- 筛选和搜索 -->
      <div class="filters-section">
        <div class="filters-row">
          <div class="filter-group">
            <label>分类筛选:</label>
            <select v-model="selectedCategory" @change="loadDatasets" class="select">
              <option value="">全部分类</option>
              <option value="api">API</option>
              <option value="document">文档</option>
              <option value="qa">问答</option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>搜索:</label>
            <input 
              v-model="searchQuery" 
              @input="filterDatasets"
              placeholder="搜索数据集名称..." 
              class="input"
            />
          </div>
          
          <button @click="loadDatasets" :disabled="loading" class="btn-primary">
            {{ loading ? '加载中...' : '刷新' }}
          </button>
        </div>
      </div>

      <!-- 数据集列表 -->
      <div class="datasets-grid">
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>正在加载数据集...</p>
        </div>
        
        <div v-else-if="filteredDatasets.length === 0" class="empty-state">
          <div class="empty-icon">📊</div>
          <h3>暂无数据集</h3>
          <p>没有找到匹配的数据集</p>
        </div>
        
        <div v-else>
          <div class="datasets-stats">
            <span>共找到 {{ filteredDatasets.length }} 个数据集</span>
          </div>
          
          <div class="datasets-list">
            <div v-for="dataset in filteredDatasets" :key="dataset.id" class="dataset-card">
              <div class="dataset-header">
                <h3 class="dataset-name">{{ dataset.name }}</h3>
                <span class="dataset-permission">{{ dataset.permission }}</span>
              </div>
              
              <p v-if="dataset.description" class="dataset-description">
                {{ dataset.description }}
              </p>
              
              <div class="dataset-stats">
                <div class="stat">
                  <span class="stat-label">文档数:</span>
                  <span class="stat-value">{{ dataset.document_count }}</span>
                </div>
                <div class="stat">
                  <span class="stat-label">字数:</span>
                  <span class="stat-value">{{ dataset.word_count.toLocaleString() }}</span>
                </div>
                <div class="stat">
                  <span class="stat-label">应用数:</span>
                  <span class="stat-value">{{ dataset.app_count }}</span>
                </div>
              </div>
              
              <div class="dataset-meta">
                <div class="meta-item">
                  <span class="meta-label">索引技术:</span>
                  <span class="meta-value">{{ dataset.indexing_technique }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">数据源类型:</span>
                  <span class="meta-value">{{ dataset.data_source_type }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">创建时间:</span>
                  <span class="meta-value">{{ formatDate(dataset.created_at) }}</span>
                </div>
              </div>
              
              <div class="dataset-actions">
                <button @click="viewDataset(dataset)" class="btn-secondary">
                  查看详情
                </button>
                <button @click="uploadToDataset(dataset)" class="btn-upload">
                  上传文档
                </button>
                <button @click="queryDataset(dataset)" class="btn-primary">
                  开始查询
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { difyApiService, type DifyDataset } from '../services/difyApi'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const error = ref('')
const datasets = ref<DifyDataset[]>([])
const selectedCategory = ref('')
const searchQuery = ref('')

// 计算属性
const filteredDatasets = computed(() => {
  let filtered = datasets.value
  
  if (selectedCategory.value) {
    filtered = filtered.filter(dataset => 
      dataset.data_source_type === selectedCategory.value
    )
  }
  
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(dataset => 
      dataset.name.toLowerCase().includes(query) ||
      (dataset.description && dataset.description.toLowerCase().includes(query))
    )
  }
  
  return filtered
})

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

// 过滤数据集
const filterDatasets = () => {
  // 计算属性会自动更新
}

// 查看数据集详情
const viewDataset = (dataset: DifyDataset) => {
  console.log('查看数据集详情:', dataset)
  // 可以添加详情页面或模态框
}

// 查询数据集
const queryDataset = (dataset: DifyDataset) => {
  router.push({
    name: 'Query',
    query: { datasetId: dataset.id, datasetName: dataset.name }
  })
}

// 上传文档到数据集
const uploadToDataset = (dataset: DifyDataset) => {
  router.push({
    name: 'DocumentUpload',
    query: { datasetId: dataset.id, datasetName: dataset.name }
  })
}

// 格式化日期
const formatDate = (timestamp: number) => {
  return new Date(timestamp * 1000).toLocaleDateString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadDatasets()
})
</script>

<style scoped>
.datasets-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 40px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-text {
  flex: 1;
}

.header-text h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.header-text p {
  font-size: 1.1rem;
  color: #7f8c8d;
}

.header-actions {
  flex-shrink: 0;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-icon {
  font-size: 16px;
}

.filters-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.filters-row {
  display: flex;
  gap: 20px;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.filter-group label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
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
  text-decoration: none;
  display: inline-block;
  text-align: center;
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

.btn-upload {
  background: #27ae60;
  color: white;
}

.btn-upload:hover {
  background: #229954;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.empty-state h3 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.empty-state p {
  color: #7f8c8d;
}

.datasets-stats {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  color: #7f8c8d;
  font-size: 14px;
}

.datasets-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.dataset-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
}

.dataset-card:hover {
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.dataset-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.dataset-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
}

.dataset-permission {
  background: #e8f5e8;
  color: #27ae60;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.dataset-description {
  color: #7f8c8d;
  margin-bottom: 16px;
  line-height: 1.5;
}

.dataset-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.dataset-meta {
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 14px;
}

.meta-label {
  color: #7f8c8d;
}

.meta-value {
  color: #2c3e50;
  font-weight: 500;
}

.dataset-actions {
  display: flex;
  gap: 12px;
}

.dataset-actions .btn-primary,
.dataset-actions .btn-secondary {
  flex: 1;
  padding: 8px 16px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .datasets-page {
    padding: 16px;
  }
  
  .filters-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    min-width: auto;
  }
  
  .datasets-list {
    grid-template-columns: 1fr;
  }
  
  .dataset-stats {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .dataset-actions {
    flex-direction: column;
  }
}
</style>
