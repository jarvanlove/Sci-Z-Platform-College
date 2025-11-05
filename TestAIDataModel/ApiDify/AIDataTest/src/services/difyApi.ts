import axios from 'axios'

// Dify API 响应类型定义
export interface DifyDataset {
  id: string
  name: string
  description?: string
  permission: string
  data_source_type: string
  indexing_technique: string
  app_count: number
  document_count: number
  word_count: number
  created_by: string
  created_at: number
  updated_by: string
  updated_at: number
}

export interface DifyApiResponse<T> {
  data: T
  has_more: boolean
  limit: number
  total: number
}

export interface DifyRetrievalRecord {
  segment: {
    id: string
    position: number
    document_id: string
    content: string
    answer: string | null
    word_count: number
    tokens: number
    keywords: string[]
    index_node_id: string
    index_node_hash: string
    hit_count: number
    enabled: boolean
    disabled_at: number | null
    disabled_by: string | null
    status: string
    created_by: string
    created_at: number
    indexing_at: number
    completed_at: number
    error: string | null
    stopped_at: number | null
    document: {
      id: string
      data_source_type: string
      name: string
    }
  }
  score: number
  tsne_position: number[] | null
}

export interface DifyRetrievalResponse {
  query: {
    content: string
    hit_count: number
  }
  records: DifyRetrievalRecord[]
}

export interface DifyRetrievalRequest {
  query: string
  retrieval_model?: {
    search_method?: 'keyword_search' | 'semantic_search' | 'full_text_search' | 'hybrid_search'
    reranking_enable?: boolean
    reranking_mode?: {
      reranking_provider_name?: string
      reranking_model_name?: string
    }
    weights?: number
    top_k?: number
    score_threshold_enabled?: boolean
    score_threshold?: number
    metadata_filtering_conditions?: {
      logical_operator?: 'and' | 'or'
      conditions?: Array<{
        name: string
        comparison_operator: string
        value: string | number | null
      }>
    }
  }
  external_retrieval_model?: any
}

// 文件上传相关类型定义
export interface DocumentUploadData {
  original_document_id?: string
  indexing_technique: 'high_quality' | 'economy'
  doc_form?: 'text_model' | 'hierarchical_model' | 'qa_model'
  doc_language?: string
  process_rule: {
    mode: 'automatic' | 'custom' | 'hierarchical'
    rules?: {
      pre_processing_rules?: Array<{
        id: 'remove_extra_spaces' | 'remove_urls_emails'
        enabled: boolean
      }>
      segmentation?: {
        separator?: string
        max_tokens?: number
        parent_mode?: 'full-doc' | 'paragraph'
        subchunk_segmentation?: {
          separator?: string
          max_tokens?: number
        }
        chunk_overlap?: number
      }
    }
  }
  retrieval_model?: {
    search_method: 'hybrid_search' | 'semantic_search' | 'full_text_search'
    reranking_enable: boolean
    reranking_model?: {
      reranking_provider_name?: string
      reranking_model_name?: string
    }
    top_k: number
    score_threshold_enabled: boolean
    score_threshold: number
  }
  embedding_model?: string
  embedding_model_provider?: string
}

export interface DocumentUploadResponse {
  document: {
    id: string
    position: number
    data_source_type: string
    data_source_info: {
      upload_file_id: string
    }
    dataset_process_rule_id: string
    name: string
    created_from: string
    created_by: string
    created_at: number
    tokens: number
    indexing_status: string
    error: string | null
    enabled: boolean
    disabled_at: number | null
    disabled_by: string | null
    archived: boolean
    display_status: string
    word_count: number
    hit_count: number
    doc_form: string
  }
  batch: string
}

// Dify API 服务类
export class DifyApiService {
  private apiClient: any
  private baseURL: string = 'http://192.168.1.203'
  private apiKey: string = 'dataset-XfEGbHDJ9C3koNrbyN0sYcrl'

  constructor() {
    this.apiClient = axios.create({
      baseURL: this.baseURL,
      headers: {
        'Authorization': `Bearer ${this.apiKey}`,
        'Content-Type': 'application/json',
      },
      timeout: 30000, // 30秒超时
    })

    // 请求拦截器
    this.apiClient.interceptors.request.use(
      (config: any) => {
        console.log('发送请求:', config.method?.toUpperCase(), config.url)
        return config
      },
      (error: any) => {
        console.error('请求错误:', error)
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.apiClient.interceptors.response.use(
      (response: any) => {
        console.log('收到响应:', response.status, response.config.url)
        return response
      },
      (error: any) => {
        console.error('响应错误:', error.response?.status, error.response?.data)
        return Promise.reject(error)
      }
    )
  }

  /**
   * 获取数据集列表
   */
  async getDatasets(category?: string): Promise<DifyApiResponse<DifyDataset[]>> {
    try {
      const params = category ? { category } : {}
      const response = await this.apiClient.get('/v1/datasets', { params })
      return response.data
    } catch (error) {
      console.error('获取数据集列表失败:', error)
      throw error
    }
  }

  /**
   * 获取特定数据集详情
   */
  async getDataset(datasetId: string): Promise<DifyDataset> {
    try {
      const response = await this.apiClient.get(`/v1/datasets/${datasetId}`)
      return response.data
    } catch (error) {
      console.error('获取数据集详情失败:', error)
      throw error
    }
  }

  /**
   * 知识库检索
   */
  async retrieval(datasetId: string, request: DifyRetrievalRequest): Promise<DifyRetrievalResponse> {
    try {
      const response = await this.apiClient.post(`/v1/datasets/${datasetId}/retrieve`, request)
      return response.data
    } catch (error) {
      console.error('知识库检索失败:', error)
      throw error
    }
  }

  /**
   * 获取数据集文档列表
   */
  async getDatasetDocuments(datasetId: string, page: number = 1, limit: number = 20) {
    try {
      const response = await this.apiClient.get(`/v1/datasets/${datasetId}/documents`, {
        params: { page, limit }
      })
      return response.data
    } catch (error) {
      console.error('获取数据集文档失败:', error)
      throw error
    }
  }

  /**
   * 获取数据集段落列表
   */
  async getDatasetSegments(datasetId: string, page: number = 1, limit: number = 20) {
    try {
      const response = await this.apiClient.get(`/v1/datasets/${datasetId}/segments`, {
        params: { page, limit }
      })
      return response.data
    } catch (error) {
      console.error('获取数据集段落失败:', error)
      throw error
    }
  }

  /**
   * 创建数据集
   */
  async createDataset(data: {
    name: string
    description?: string
    indexing_technique?: 'high_quality' | 'economy'
    permission?: 'only_me' | 'all_team_members' | 'partial_members'
    provider?: 'vendor' | 'external'
    external_knowledge_api_id?: string
    external_knowledge_id?: string
    embedding_model?: string
    embedding_model_provider?: string
    retrieval_model?: {
      search_method?: 'hybrid_search' | 'semantic_search' | 'full_text_search'
      reranking_enable?: boolean
      reranking_model?: {
        reranking_provider_name?: string
        reranking_model_name?: string
      }
      top_k?: number
      score_threshold_enabled?: boolean
      score_threshold?: number
    }
  }) {
    try {
      console.log('创建数据集请求数据:', data)
      const response = await this.apiClient.post('/v1/datasets', data)
      console.log('创建数据集成功:', response.data)
      return response.data
    } catch (error: any) {
      console.error('创建数据集失败:', error)
      if (error.response) {
        console.error('响应状态:', error.response.status)
        console.error('响应数据:', error.response.data)
      }
      throw error
    }
  }

  /**
   * 更新数据集
   */
  async updateDataset(datasetId: string, data: {
    name?: string
    description?: string
    permission?: string
  }) {
    try {
      const response = await this.apiClient.post(`/v1/datasets/${datasetId}`, data)
      return response.data
    } catch (error) {
      console.error('更新数据集失败:', error)
      throw error
    }
  }

  /**
   * 删除数据集
   */
  async deleteDataset(datasetId: string) {
    try {
      const response = await this.apiClient.delete(`/v1/datasets/${datasetId}`)
      return response.data
    } catch (error) {
      console.error('删除数据集失败:', error)
      throw error
    }
  }

  /**
   * 通过文件创建文档
   */
  async createDocumentByFile(
    datasetId: string, 
    file: File, 
    data: DocumentUploadData
  ): Promise<DocumentUploadResponse> {
    try {
      const formData = new FormData()
      
      // 添加文件 - 确保文件存在且有效
      if (!file) {
        throw new Error('文件不能为空')
      }
      
      // 只添加一个文件
      formData.append('file', file)
      console.log('添加文件到 FormData:', file.name, file.size, file.type)
      
      // 添加数据配置 - 使用简单的字符串格式
      formData.append('data', JSON.stringify(data))
      console.log('添加数据配置到 FormData:', JSON.stringify(data))
      
      // 调试 FormData 内容
      console.log('FormData 内容:')
      let fileCount = 0
      for (const [key, value] of formData.entries()) {
        if (value instanceof File) {
          fileCount++
          console.log(key, `File: ${value.name} (${value.size} bytes)`)
        } else {
          console.log(key, value)
        }
      }
      console.log(`FormData 中文件数量: ${fileCount}`)
      
      if (fileCount > 1) {
        throw new Error(`检测到多个文件 (${fileCount} 个)，API 只允许上传一个文件`)
      }
      
      const response = await this.apiClient.post(
        `/v1/datasets/${datasetId}/document/create-by-file`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      )
      return response.data
    } catch (error: any) {
      console.error('文件上传失败:', error)
      if (error.response) {
        console.error('响应状态:', error.response.status)
        console.error('响应数据:', error.response.data)
      }
      throw error
    }
  }

  /**
   * 获取文档列表
   */
  async getDocuments(datasetId: string, page: number = 1, limit: number = 20) {
    try {
      const response = await this.apiClient.get(`/v1/datasets/${datasetId}/documents`, {
        params: { page, limit }
      })
      return response.data
    } catch (error) {
      console.error('获取文档列表失败:', error)
      throw error
    }
  }

  /**
   * 删除文档
   */
  async deleteDocument(datasetId: string, documentId: string) {
    try {
      const response = await this.apiClient.delete(`/v1/datasets/${datasetId}/documents/${documentId}`)
      return response.data
    } catch (error) {
      console.error('删除文档失败:', error)
      throw error
    }
  }
}

// 导出单例实例
export const difyApiService = new DifyApiService()
