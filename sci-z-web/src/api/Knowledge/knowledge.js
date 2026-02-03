import request from '@/utils/request'
import { KNOWLEDGE_API, HTTP_METHODS, API_BASE_URL } from '../Common/constants'

/**
 * 知识库模块 API 接口
 *
 */
/**
 * 获取知识库列表（旧接口，保留兼容）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页数量，默认10
 * @returns {Promise} 知识库列表响应
 */
export const getKnowledgeList = (params = {}) => {
  return request({
    url: KNOWLEDGE_API.LIST,
    method: HTTP_METHODS.GET,
    params: {
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

/**
 * 分页查询知识库列表（新接口，支持关键字搜索和滚动加载）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码，默认1
 * @param {number} params.pageSize - 每页数量，默认10
 * @param {string} params.keyword - 搜索关键字（知识库名称/描述），非必传
 * @param {string} params.sortBy - 排序字段，可选
 * @param {string} params.sortOrder - 排序方式（ASC/DESC），默认DESC
 * @param {string} params.kbType - 知识库类型筛选：personal=个人知识库，project=项目知识库，不传=全部
 * @returns {Promise} 知识库列表响应
 */
export const getKnowledgeListPage = (params = {}) => {
  return request({
    url: KNOWLEDGE_API.LIST_PAGE,
    method: HTTP_METHODS.POST,
    data: {
      pageNo: params.pageNo || 1,
      pageSize: params.pageSize || 10,
      keyword: params.keyword || undefined,
      sortBy: params.sortBy || undefined,
      sortOrder: params.sortOrder || undefined,
      kbType: params.kbType || undefined
    }
  })
}

/**
 * 创建知识库
 * @param {Object} data - 知识库数据
 * @param {string} data.name - 知识库名称
 * @param {string} data.description - 知识库描述
 * @param {string} data.type - 知识库类型
 * @param {number} data.departmentId - 部门ID
 * @returns {Promise} 创建知识库响应
 */
export const createKnowledge = (data) => {
  return request({
    url: KNOWLEDGE_API.CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 获取知识库详情
 * @param {number} id - 知识库ID
 * @returns {Promise} 知识库详情响应
 */
export const getKnowledgeDetail = (id) => {
  return request({
    url: KNOWLEDGE_API.DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 更新知识库
 * @param {number} id - 知识库ID
 * @param {Object} data - 知识库数据
 * @returns {Promise} 更新知识库响应
 */
export const updateKnowledge = (id, data) => {
  return request({
    url: KNOWLEDGE_API.UPDATE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 上传知识库封面
 * @param {number} id - 知识库ID
 * @param {File} file - 封面图片文件
 * @returns {Promise} 上传封面响应
 */
export const uploadKnowledgeCover = (id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: KNOWLEDGE_API.UPLOAD_COVER(id),
    method: HTTP_METHODS.POST,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除知识库
 * @param {number} id - 知识库ID
 * @returns {Promise} 删除知识库响应
 */
export const deleteKnowledge = (id) => {
  return request({
    url: KNOWLEDGE_API.DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 获取文件夹树
 * @param {number} knowledgeId - 知识库ID
 * @returns {Promise} 文件夹树响应
 */
export const getKnowledgeFolderTree = (knowledgeId) => {
  return request({
    url: KNOWLEDGE_API.FOLDERS_TREE(knowledgeId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 获取文件夹列表（根据父文件夹ID）
 * @param {number} knowledgeId - 知识库ID
 * @param {number} [parentId=0] - 父文件夹ID，默认为0（根目录）
 * @returns {Promise} 文件夹列表响应
 */
export const getKnowledgeFolders = (knowledgeId, parentId = 0) => {
  return request({
    url: KNOWLEDGE_API.FOLDERS(knowledgeId),
    method: HTTP_METHODS.GET,
    params: {
      parentId: parentId
    }
  })
}

/**
 * 获取文件夹详情
 * @param {number} folderId - 文件夹ID
 * @returns {Promise} 文件夹详情响应
 */
export const getKnowledgeFolderDetail = (folderId) => {
  return request({
    url: KNOWLEDGE_API.FOLDER_DETAIL(folderId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 获取知识库文件夹和文件列表（按文件夹分组，支持分页）
 * @param {number} knowledgeId - 知识库ID
 * @param {Object} params - 分页参数
 * @param {number} [params.folderId] - 文件夹ID（可选，null或0表示根目录）
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页数量，默认10
 * @returns {Promise} 文件夹和文件列表响应（根目录时按文件夹分组，文件夹内时混合列表）
 */
export const getKnowledgeFoldersFiles = (knowledgeId, params = {}) => {
  return request({
    url: KNOWLEDGE_API.FOLDERS_FILES(knowledgeId),
    method: HTTP_METHODS.GET,
    params: {
      folderId: params.folderId !== undefined && params.folderId !== null ? params.folderId : undefined,
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

/**
 * 创建文件夹
 * @param {number} knowledgeId - 知识库ID
 * @param {Object} data - 文件夹数据
 * @param {string} data.folderName - 文件夹名称
 * @param {number} data.knowledgeId - 知识库ID
 * @param {number} data.parentId - 父文件夹ID
 * @returns {Promise} 创建文件夹响应
 */
export const createKnowledgeFolder = (knowledgeId, data) => {
  return request({
    url: KNOWLEDGE_API.CREATE_FOLDER(knowledgeId),
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 更新文件夹
 * @param {number} id - 文件夹ID
 * @param {Object} data - 文件夹数据
 * @returns {Promise} 更新文件夹响应
 */
export const updateKnowledgeFolder = (id, data) => {
  return request({
    url: KNOWLEDGE_API.UPDATE_FOLDER(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 删除文件夹
 * @param {number} id - 文件夹ID
 * @returns {Promise} 删除文件夹响应
 */
export const deleteKnowledgeFolder = (id) => {
  return request({
    url: KNOWLEDGE_API.DELETE_FOLDER(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 上传文件（旧接口，保留兼容）
 * @param {FormData} data - 文件数据
 * @param {File} data.file - 文件对象
 * @param {number} data.knowledgeId - 知识库ID
 * @param {number} data.folderId - 文件夹ID
 * @param {string} data.description - 文件描述
 * @returns {Promise} 上传文件响应
 */
export const uploadKnowledgeFile = (data) => {
  return request({
    url: KNOWLEDGE_API.UPLOAD_FILE,
    method: HTTP_METHODS.POST,
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传文件到知识库（单文件接口，保留兼容）
 * @param {string|number} difyKbId - 知识库的 Dify KB ID
 * @param {File} file - 要上传的文件
 * @param {number} [folderId=0] - 文件夹ID，默认为0（根目录）
 * @returns {Promise} 上传文件响应
 */
export const uploadFileToKnowledge = (difyKbId, file, folderId = 0) => {
  const formData = new FormData()
  formData.append('file', file)
  if (folderId && folderId !== 0) {
    formData.append('folderId', folderId)
  }
  
  return request({
    url: KNOWLEDGE_API.UPLOAD(difyKbId),
    method: HTTP_METHODS.POST,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传文件到知识库（多文件接口，异步分批上传）
 * @param {string|number} knowledgeId - 知识库ID（数据库主键ID）
 * @param {File[]} files - 要上传的文件列表
 * @param {number} [folderId=0] - 文件夹ID，默认为0（根目录）
 * @param {Function} [onProgress] - 进度回调函数，参数为 (loaded, total, fileIndex)
 * @returns {Promise} 上传文件响应
 */
export const uploadFilesToKnowledge = (knowledgeId, files, folderId = 0, onProgress = null) => {
  const formData = new FormData()
  
  // 添加多个文件（使用 files 字段名，后端接收 List<MultipartFile>）
  Array.from(files).forEach(file => {
    formData.append('files', file)
  })
  
  // 添加文件夹ID
  if (folderId && folderId !== 0) {
    formData.append('folderId', folderId)
  }
  
  // 如果提供了进度回调，使用 XMLHttpRequest 来监听上传进度
  if (onProgress && typeof onProgress === 'function') {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest()
      const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}${KNOWLEDGE_API.UPLOAD_BATCH(knowledgeId)}`
      
      // 监听上传进度
      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable) {
          const loaded = e.loaded
          const total = e.total
          onProgress(loaded, total)
        }
      })
      
      // 监听请求完成
      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const response = JSON.parse(xhr.responseText)
            // 🔥 修复：检查业务错误码，即使 HTTP 状态码是 200，也可能有业务错误
            if (response.code && response.code !== 200) {
              const error = new Error(response.message || '上传失败')
              error.response = response
              reject(error)
            } else {
              resolve(response)
            }
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        } else {
          try {
            const error = JSON.parse(xhr.responseText)
            const err = new Error(error.message || '上传失败')
            err.response = error
            reject(err)
          } catch (e) {
            reject(new Error(`上传失败: ${xhr.status}`))
          }
        }
      })
      
      // 监听错误
      xhr.addEventListener('error', () => {
        reject(new Error('网络错误'))
      })
      
      // 监听取消
      xhr.addEventListener('abort', () => {
        reject(new Error('上传已取消'))
      })
      
      // 设置请求头（包括认证token）
      // 注意：在 API 文件中不能直接使用 useAuthStore()，需要动态导入
      // 先获取 token，然后再打开和发送请求
      import('@/store/modules/auth').then(({ useAuthStore }) => {
        const authStore = useAuthStore()
        
        // 打开请求
        xhr.open('POST', url)
        
        // 设置请求头
        if (authStore.token) {
          xhr.setRequestHeader('Authorization', `Bearer ${authStore.token}`)
        }
        
        // 发送请求
        xhr.send(formData)
      }).catch(() => {
        // 如果导入失败，继续发送请求（可能未登录）
        xhr.open('POST', url)
        xhr.send(formData)
      })
    })
  }
  
  // 默认使用 axios（不监听进度）
  return request({
    url: KNOWLEDGE_API.UPLOAD_BATCH(knowledgeId),
    method: HTTP_METHODS.POST,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取文件列表
 * @param {number} knowledgeId - 知识库ID
 * @param {Object} params - 查询参数
 * @param {number} params.folderId - 文件夹ID
 * @param {string} params.keyword - 关键词搜索
 * @param {string} params.type - 文件类型
 * @returns {Promise} 文件列表响应
 */
export const getKnowledgeFiles = (knowledgeId, params) => {
  return request({
    url: KNOWLEDGE_API.FILES(knowledgeId),
    method: HTTP_METHODS.GET,
    params
  })
}

/**
 * 删除文件
 * @param {number} id - 文件ID
 * @returns {Promise} 删除文件响应
 */
export const deleteKnowledgeFile = (id) => {
  return request({
    url: KNOWLEDGE_API.DELETE_FILE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 重命名文件
 * @param {number} id - 文件ID
 * @param {Object} data - 重命名数据
 * @param {string} data.name - 新文件名
 * @returns {Promise} 重命名文件响应
 */
export const renameKnowledgeFile = (id, data) => {
  return request({
    url: KNOWLEDGE_API.RENAME_FILE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 移动文件
 * @param {number} id - 文件ID
 * @param {Object} data - 移动数据
 * @param {number} data.folderId - 目标文件夹ID
 * @returns {Promise} 移动文件响应
 */
export const moveKnowledgeFile = (id, data) => {
  return request({
    url: KNOWLEDGE_API.MOVE_FILE(id),
    method: HTTP_METHODS.PUT,
    data
  })
}

/**
 * 搜索知识库
 * @param {number} knowledgeId - 知识库ID
 * @param {string} query - 搜索关键词
 * @returns {Promise} 搜索结果响应
 */
export const searchKnowledge = (knowledgeId, query) => {
  return request({
    url: KNOWLEDGE_API.SEARCH(knowledgeId),
    method: HTTP_METHODS.GET,
    params: { query }
  })
}

/**
 * 知识库问答
 * @param {Object} data - 问答数据
 * @param {number} data.knowledgeId - 知识库ID
 * @param {string} data.question - 问题
 * @param {Object} data.options - 问答选项
 * @returns {Promise} 问答响应
 */
export const askKnowledgeQuestion = (data) => {
  return request({
    url: KNOWLEDGE_API.QA_ASK,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 基于知识库的Chatbot流式对话
 * @param {Object} params - 对话参数
 * @param {string} params.knowledgeId - 知识库ID（Dify知识库ID，String类型）
 * @param {string} params.query - 用户问题
 * @param {string} [params.conversationId] - 会话ID，用于保持对话上下文
 * @param {string} [params.user] - 用户标识，如果不提供则使用当前登录用户ID
 * @param {Function} params.onMessage - 消息片段回调函数 (answer: string) => void
 * @param {Function} params.onEnd - 消息结束回调函数 (data: {conversationId, messageId, documents}) => void
 * @param {Function} params.onError - 错误回调函数 (error: Error) => void
 * @returns {Promise} 返回AbortController用于取消请求
 */
export const streamKnowledgeChatbot = async (params) => {
  const { knowledgeId, query, conversationId, user, onMessage, onEnd, onError } = params
  
  // 获取token
  const authStore = await import('@/store/modules/auth').then(m => m.useAuthStore())
  const token = authStore.token
  
  if (!token) {
    const error = new Error('未登录')
    onError?.(error)
    throw error
  }

  // 使用正确的API路径（fetch 需要完整路径，需要手动添加 /api 前缀）
  const url = `${API_BASE_URL}${KNOWLEDGE_API.CHATBOT_STREAM}`
  
  const logger = (await import('@/utils/simpleLogger')).createLogger('KnowledgeChatbot')
  logger.info('流式对话请求URL', { url, knowledgeId, query })
  
  const abortController = new AbortController()
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        // 明确要求流式响应，避免打包后被缓冲
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({
        knowledgeId,
        query,
        conversationId,
        user
      }),
      signal: abortController.signal
    })

    // 检查是否是错误响应（非流式）
    if (!response.ok) {
      const contentType = response.headers.get('content-type')
      if (contentType && contentType.includes('application/json')) {
        // 处理JSON错误响应（如CHATBOT_NOT_CREATED）
        const errorData = await response.json()
        const error = new Error(errorData.message || '请求失败')
        error.code = errorData.code
        error.hint = errorData.hint
        onError?.(error)
        throw error
      } else {
        const error = new Error(`请求失败: ${response.status}`)
        onError?.(error)
        throw error
      }
    }

    // 处理流式响应
    if (!response.body) {
      const error = new Error('响应体为空')
      onError?.(error)
      throw error
    }

    // 检查响应类型，确保是流式响应
    const contentType = response.headers.get('content-type') || ''
    if (!contentType.includes('text/event-stream') && !contentType.includes('text/plain')) {
      logger.warn('响应类型可能不是流式', { contentType, url })
      // 不抛出错误，继续处理，因为某些服务器可能不设置正确的 Content-Type
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 保留最后不完整的行

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine || !trimmedLine.startsWith('data:')) continue

        const dataStr = trimmedLine.replace(/^data:\s*/, '')
        if (!dataStr || dataStr === '[DONE]') continue

        try {
          const data = JSON.parse(dataStr)
          
          if (data.event === 'message') {
            // 消息片段
            onMessage?.(data.answer || '')
          } else if (data.event === 'message_end') {
            // 消息结束，提取文档片段数据
            // 文档片段数据在 metadata.retriever_resources 中
            const retrieverResources = data.metadata?.retriever_resources || []
            const documents = retrieverResources.map((resource, index) => ({
              id: resource.segment_id || `doc-${index}`,
              name: resource.document_name || resource.dataset_name || `文档 ${index + 1}`,
              datasetName: resource.dataset_name,
              content: resource.content || '',
              score: resource.score || 0,
              position: resource.position || index + 1,
              documentId: resource.document_id,
              segmentId: resource.segment_id
            }))
            
            onEnd?.({
              conversationId: data.conversation_id,
              messageId: data.message_id,
              documents: documents, // 文档片段数据（从 retriever_resources 提取）
              metadata: data.metadata // 保留完整的元数据
            })
          }
        } catch (parseError) {
          // 忽略解析错误，继续处理下一行
          console.warn('解析SSE数据失败:', parseError, dataStr)
        }
      }
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      // 请求被取消，不处理
      return abortController
    }
    onError?.(error)
    throw error
  }

  return abortController
}

// ================================
// 知识库文件关联接口
// ================================

/**
 * 创建知识库文件关联
 * @param {Object} data - 文件关联数据
 * @param {number} data.knowledgeId - 知识库ID（必填）
 * @param {number} [data.folderId=0] - 文件夹ID，默认为0（根目录）
 * @param {number} data.attachmentId - 附件ID（必填）
 * @param {string} [data.fileName] - 文件显示名称，最大255个字符
 * @param {number} [data.sortOrder=0] - 排序号，默认为0
 * @param {string} [data.callback] - 回调数据（Dify API返回的完整JSON数据）
 * @returns {Promise} 创建文件关联响应
 */
export const createKnowledgeFileRelation = (data) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_CREATE,
    method: HTTP_METHODS.POST,
    data: {
      knowledgeId: Number(data.knowledgeId),
      folderId: data.folderId ? Number(data.folderId) : 0,
      attachmentId: Number(data.attachmentId),
      fileName: data.fileName,
      sortOrder: data.sortOrder || 0,
      callback: data.callback
    }
  })
}

/**
 * 更新知识库文件关联
 * @param {number} id - 关联ID
 * @param {Object} data - 更新数据
 * @param {number} [data.folderId] - 文件夹ID
 * @param {string} [data.fileName] - 文件显示名称，最大255个字符
 * @param {number} [data.sortOrder] - 排序号
 * @returns {Promise} 更新文件关联响应
 */
export const updateKnowledgeFileRelation = (id, data) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_UPDATE(Number(id)),
    method: HTTP_METHODS.PUT,
    data: {
      // 后端接口要求 folderId 为 String 类型，"0" 表示根目录，null 或 undefined 表示不更新
      folderId: data.folderId !== undefined && data.folderId !== null ? String(data.folderId) : undefined,
      fileName: data.fileName,
      sortOrder: data.sortOrder !== undefined ? Number(data.sortOrder) : undefined
    }
  })
}

/**
 * 删除知识库文件关联（软删除）
 * @param {number} id - 关联ID
 * @returns {Promise} 删除文件关联响应
 */
export const deleteKnowledgeFileRelation = (id) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_DELETE(Number(id)),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 查询知识库文件关联详情
 * @param {number} id - 关联ID
 * @returns {Promise} 文件关联详情响应
 */
export const getKnowledgeFileRelationDetail = (id) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_DETAIL(Number(id)),
    method: HTTP_METHODS.GET
  })
}

/**
 * 分页查询知识库文件关联列表
 * @param {Object} params - 查询参数
 * @param {number} params.knowledgeId - 知识库ID（必填）
 * @param {number} [params.folderId] - 文件夹ID，如果指定则只查询该文件夹下的文件
 * @param {number} [params.page=1] - 页码，默认为1
 * @param {number} [params.size=10] - 每页大小，默认为10
 * @returns {Promise} 文件关联列表响应（分页）
 */
export const getKnowledgeFileRelationList = (params) => {
  return request({
    url: KNOWLEDGE_API.FILE_RELATION_LIST,
    method: HTTP_METHODS.GET,
    params: {
      knowledgeId: Number(params.knowledgeId),
      folderId: params.folderId !== null && params.folderId !== undefined ? Number(params.folderId) : undefined,
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

/**
 * 从PDF URL收藏到知识库
 * @param {string} knowledgeId - 知识库ID（Dify知识库ID）
 * @param {string} pdfUrl - PDF文件URL
 * @param {string} fileName - 文件名
 * @param {number} folderId - 文件夹ID，默认为0
 * @returns {Promise} 收藏响应
 */
export const collectFromPdfUrl = (knowledgeId, pdfUrl, fileName, folderId = 0) => {
  return request({
    url: KNOWLEDGE_API.COLLECT_FROM_PDF_URL(knowledgeId),
    method: HTTP_METHODS.POST,
    data: {
      pdfUrl: pdfUrl,
      fileName: fileName,
      folderId: folderId
    }
  })
}