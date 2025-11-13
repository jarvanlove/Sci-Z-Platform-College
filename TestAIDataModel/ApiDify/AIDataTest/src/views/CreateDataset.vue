<template>
  <div class="create-dataset">
    <div class="container">
      <h1>创建知识库</h1>
      
      <form @submit.prevent="createDataset" class="dataset-form">
        <div class="form-group">
          <label for="name">知识库名称 *</label>
          <input
            id="name"
            v-model="datasetForm.name"
            type="text"
            required
            placeholder="请输入知识库名称"
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label for="description">知识库描述</label>
          <textarea
            id="description"
            v-model="datasetForm.description"
            placeholder="请输入知识库描述（可选）"
            class="form-control"
            rows="3"
          ></textarea>
        </div>

        <div class="form-group">
          <label for="indexing_technique">索引模式</label>
          <select
            id="indexing_technique"
            v-model="datasetForm.indexing_technique"
            class="form-control"
          >
            <option value="high_quality">高质量</option>
            <option value="economy">经济</option>
          </select>
        </div>

        <div class="form-group">
          <label for="permission">权限设置</label>
          <select
            id="permission"
            v-model="datasetForm.permission"
            class="form-control"
          >
            <option value="only_me">仅自己</option>
            <option value="all_team_members">所有团队成员</option>
            <option value="partial_members">部分团队成员</option>
          </select>
        </div>

        <div class="form-group">
          <label for="provider">数据源类型</label>
          <select
            id="provider"
            v-model="datasetForm.provider"
            class="form-control"
          >
            <option value="vendor">上传文件</option>
            <option value="external">外部知识库</option>
          </select>
        </div>

        <!-- 外部知识库配置 -->
        <div v-if="datasetForm.provider === 'external'" class="external-config">
          <div class="form-group">
            <label for="external_knowledge_api_id">外部知识库 API ID</label>
            <input
              id="external_knowledge_api_id"
              v-model="datasetForm.external_knowledge_api_id"
              type="text"
              placeholder="请输入外部知识库 API ID"
              class="form-control"
            />
          </div>

          <div class="form-group">
            <label for="external_knowledge_id">外部知识库 ID</label>
            <input
              id="external_knowledge_id"
              v-model="datasetForm.external_knowledge_id"
              type="text"
              placeholder="请输入外部知识库 ID"
              class="form-control"
            />
          </div>
        </div>

        <!-- Embedding 模型配置 -->
        <div class="form-group">
          <label for="embedding_model">Embedding 模型名称</label>
          <input
            id="embedding_model"
            v-model="datasetForm.embedding_model"
            type="text"
            placeholder="请输入 Embedding 模型名称（可选）"
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label for="embedding_model_provider">Embedding 模型供应商</label>
          <input
            id="embedding_model_provider"
            v-model="datasetForm.embedding_model_provider"
            type="text"
            placeholder="请输入 Embedding 模型供应商（可选）"
            class="form-control"
          />
        </div>

        <!-- 检索模型配置 -->
        <div class="retrieval-config">
          <h3>检索模型配置</h3>
          
          <div class="form-group">
            <label for="search_method">检索方法</label>
            <select
              id="search_method"
              v-model="retrievalModelSearchMethod"
              class="form-control"
            >
              <option value="hybrid_search">混合检索</option>
              <option value="semantic_search">语义检索</option>
              <option value="full_text_search">全文检索</option>
            </select>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="retrievalModelRerankingEnable"
              />
              启用 Rerank
            </label>
          </div>

          <div v-if="retrievalModelRerankingEnable" class="reranking-config">
            <div class="form-group">
              <label for="reranking_provider_name">Rerank 模型供应商</label>
              <input
                id="reranking_provider_name"
                v-model="rerankingProviderName"
                type="text"
                placeholder="请输入 Rerank 模型供应商"
                class="form-control"
              />
            </div>

            <div class="form-group">
              <label for="reranking_model_name">Rerank 模型名称</label>
              <input
                id="reranking_model_name"
                v-model="rerankingModelName"
                type="text"
                placeholder="请输入 Rerank 模型名称"
                class="form-control"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="top_k">召回条数</label>
            <input
              id="top_k"
              v-model="retrievalModelTopK"
              type="number"
              min="1"
              max="100"
              placeholder="召回条数"
              class="form-control"
            />
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="retrievalModelScoreThresholdEnabled"
              />
              启用召回分数限制
            </label>
          </div>

          <div v-if="retrievalModelScoreThresholdEnabled" class="form-group">
            <label for="score_threshold">召回分数限制</label>
            <input
              id="score_threshold"
              v-model="retrievalModelScoreThreshold"
              type="number"
              min="0"
              max="1"
              step="0.1"
              placeholder="召回分数限制 (0-1)"
              class="form-control"
            />
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="resetForm" class="btn btn-secondary">
            重置
          </button>
          <button type="submit" :disabled="isCreating" class="btn btn-primary">
            {{ isCreating ? '创建中...' : '创建知识库' }}
          </button>
        </div>
      </form>

      <!-- 创建结果 -->
      <div v-if="createResult" class="result-section">
        <h3>创建结果</h3>
        <div class="result-content">
          <p><strong>知识库 ID:</strong> {{ createResult.id }}</p>
          <p><strong>知识库名称:</strong> {{ createResult.name }}</p>
          <p><strong>权限:</strong> {{ createResult.permission }}</p>
          <p><strong>创建时间:</strong> {{ formatTimestamp(createResult.created_at) }}</p>
        </div>
      </div>

      <!-- 错误信息 -->
      <div v-if="errorMessage" class="error-message">
        <h3>创建失败</h3>
        <p>{{ errorMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { difyApiService } from '../services/difyApi'

const router = useRouter()

// 表单数据
const datasetForm = ref({
  name: '',
  description: '',
  indexing_technique: 'high_quality' as 'high_quality' | 'economy',
  permission: 'only_me' as 'only_me' | 'all_team_members' | 'partial_members',
  provider: 'vendor' as 'vendor' | 'external',
  external_knowledge_api_id: '',
  external_knowledge_id: '',
  embedding_model: '',
  embedding_model_provider: '',
  retrieval_model: {
    search_method: 'hybrid_search' as 'hybrid_search' | 'semantic_search' | 'full_text_search',
    reranking_enable: false,
    reranking_model: {
      reranking_provider_name: '',
      reranking_model_name: ''
    },
    top_k: 5,
    score_threshold_enabled: false,
    score_threshold: 0.5
  }
})

// 状态管理
const isCreating = ref(false)
const createResult = ref<any>(null)
const errorMessage = ref('')

// 检索模型的计算属性
const retrievalModelSearchMethod = computed({
  get: () => datasetForm.value.retrieval_model?.search_method ?? 'hybrid_search',
  set: (value: string) => {
    if (datasetForm.value.retrieval_model) {
      datasetForm.value.retrieval_model.search_method = value as any
    }
  }
})

const retrievalModelRerankingEnable = computed({
  get: () => datasetForm.value.retrieval_model?.reranking_enable ?? false,
  set: (value: boolean) => {
    if (datasetForm.value.retrieval_model) {
      datasetForm.value.retrieval_model.reranking_enable = value
    }
  }
})

const rerankingProviderName = computed({
  get: () => datasetForm.value.retrieval_model?.reranking_model?.reranking_provider_name ?? '',
  set: (value: string) => {
    if (datasetForm.value.retrieval_model?.reranking_model) {
      datasetForm.value.retrieval_model.reranking_model.reranking_provider_name = value
    }
  }
})

const rerankingModelName = computed({
  get: () => datasetForm.value.retrieval_model?.reranking_model?.reranking_model_name ?? '',
  set: (value: string) => {
    if (datasetForm.value.retrieval_model?.reranking_model) {
      datasetForm.value.retrieval_model.reranking_model.reranking_model_name = value
    }
  }
})

const retrievalModelTopK = computed({
  get: () => datasetForm.value.retrieval_model?.top_k ?? 5,
  set: (value: number) => {
    if (datasetForm.value.retrieval_model) {
      datasetForm.value.retrieval_model.top_k = value
    }
  }
})

const retrievalModelScoreThresholdEnabled = computed({
  get: () => datasetForm.value.retrieval_model?.score_threshold_enabled ?? false,
  set: (value: boolean) => {
    if (datasetForm.value.retrieval_model) {
      datasetForm.value.retrieval_model.score_threshold_enabled = value
    }
  }
})

const retrievalModelScoreThreshold = computed({
  get: () => datasetForm.value.retrieval_model?.score_threshold ?? 0.5,
  set: (value: number) => {
    if (datasetForm.value.retrieval_model) {
      datasetForm.value.retrieval_model.score_threshold = value
    }
  }
})

// 创建知识库
const createDataset = async () => {
  if (!datasetForm.value.name.trim()) {
    errorMessage.value = '请输入知识库名称'
    return
  }

  isCreating.value = true
  errorMessage.value = ''
  createResult.value = null

  try {
    console.log('创建知识库请求数据:', datasetForm.value)
    
    const result = await difyApiService.createDataset(datasetForm.value)
    createResult.value = result
    
    console.log('知识库创建成功:', result)
    
    // 3秒后跳转到数据集列表
    setTimeout(() => {
      router.push('/datasets')
    }, 3000)
    
  } catch (error: any) {
    console.error('创建知识库失败:', error)
    errorMessage.value = error.response?.data?.message || error.message || '创建知识库失败'
  } finally {
    isCreating.value = false
  }
}

// 重置表单
const resetForm = () => {
  datasetForm.value = {
    name: '',
    description: '',
    indexing_technique: 'high_quality',
    permission: 'only_me',
    provider: 'vendor',
    external_knowledge_api_id: '',
    external_knowledge_id: '',
    embedding_model: '',
    embedding_model_provider: '',
    retrieval_model: {
      search_method: 'hybrid_search',
      reranking_enable: false,
      reranking_model: {
        reranking_provider_name: '',
        reranking_model_name: ''
      },
      top_k: 5,
      score_threshold_enabled: false,
      score_threshold: 0.5
    }
  }
  createResult.value = null
  errorMessage.value = ''
}

// 格式化时间戳
const formatTimestamp = (timestamp: number) => {
  return new Date(timestamp * 1000).toLocaleString('zh-CN')
}
</script>

<style scoped>
.create-dataset {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.container {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.dataset-form {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

textarea.form-control {
  resize: vertical;
  min-height: 80px;
}

.external-config {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.retrieval-config {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.retrieval-config h3 {
  margin-top: 0;
  color: #333;
  margin-bottom: 15px;
}

.reranking-config {
  background: white;
  padding: 15px;
  border-radius: 4px;
  margin-top: 10px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  margin-right: 8px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-primary:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.result-section {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  border-radius: 4px;
  padding: 20px;
  margin-top: 20px;
}

.result-section h3 {
  color: #155724;
  margin-top: 0;
}

.result-content p {
  margin: 5px 0;
  color: #155724;
}

.error-message {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  padding: 20px;
  margin-top: 20px;
}

.error-message h3 {
  color: #721c24;
  margin-top: 0;
}

.error-message p {
  color: #721c24;
  margin: 5px 0;
}
</style>
