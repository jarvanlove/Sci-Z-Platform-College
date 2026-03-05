import request from '@/utils/request'
import { DECLARATION_API, HTTP_METHODS } from '../Common/constants'

/**
 * 申报模块 API 接口
 *
 */

/**
 * 获取申报列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码（从1开始）
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.sortBy - 排序字段（如：submitTime）
 * @param {string} params.sortOrder - 排序顺序（ASC/DESC）
 * @param {string} params.keyword - 关键词搜索
 * @param {number} params.status - 状态筛选（数字类型）
 * @returns {Promise} 申报列表响应
 */
export const getDeclarationList = (params) => {
  return request({
    url: DECLARATION_API.LIST,
    method: HTTP_METHODS.POST,
    data: params
  })
}

/**
 * 获取申报详情
 * @param {number} id - 申报ID
 * @returns {Promise} 申报详情响应
 */
export const getDeclarationDetail = (id) => {
  return request({
    url: DECLARATION_API.DETAIL(id),
    method: HTTP_METHODS.GET
  })
}

/**
 * 上传红头文件并分析
 * @param {File} file - 文件对象
 * @returns {Promise} 上传和分析响应，包含分析结果
 */
export const uploadRedHeaderFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: DECLARATION_API.RED_HEADER_FILE_UPLOAD,
    method: HTTP_METHODS.POST,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 创建申报
 * @param {Object} data - 申报数据
 * @param {string} data.department - 部门
 * @param {string} data.projectLeader - 项目负责人
 * @param {string} data.documentPublishTime - 红头文件发布时间 (YYYY-MM-DD)
 * @param {string} data.projectStartTime - 项目开始时间 (YYYY-MM-DD)
 * @param {string} data.projectEndTime - 项目结束时间 (YYYY-MM-DD)
 * @param {Array<string>} data.researchFields - 研究领域数组
 * @param {string} data.researchDirection - 研究方向（富文本）
 * @param {string} data.researchTopic - 研究课题
 * @param {string} data.workflowId - 工作流ID
 * @returns {Promise} 创建申报响应
 */
export const createDeclaration = (data) => {
  return request({
    url: DECLARATION_API.CREATE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 删除申报
 * @param {number} id - 申报ID
 * @returns {Promise} 删除申报响应
 */
export const deleteDeclaration = (id) => {
  return request({
    url: DECLARATION_API.DELETE(id),
    method: HTTP_METHODS.DELETE
  })
}

/**
 * 提交申报
 * @param {number} id - 申报ID
 * @returns {Promise} 提交申报响应
 */
export const submitDeclaration = (id) => {
  return request({
    url: DECLARATION_API.SUBMIT(id),
    method: HTTP_METHODS.POST
  })
}

/**
 * 更新申报状态
 * @param {Object} params - 更新参数
 * @param {number} params.id - 申报ID（必须与路径参数中的id一致）
 * @param {number} params.status - 新状态（数字：1=申报中，2=申报成功，3=申报失败）
 * @returns {Promise} 更新状态响应 
 */
export const updateDeclarationStatus = (params) => {
  return request({
    url: DECLARATION_API.UPDATE_STATUS(params.id),
    method: HTTP_METHODS.PUT,
    data: {
      id: params.id,
      status: params.status 
    }
  })
}

/**
 * 申报书编辑上传（覆盖当前申报书附件，下载时获取最新文件）
 * @param {number} declarationId - 申报ID
 * @param {File} file - 申报书文件
 * @returns {Promise} 新附件信息
 */
export const uploadDeclarationDocument = (declarationId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: DECLARATION_API.DOCUMENT_UPLOAD(declarationId),
    method: HTTP_METHODS.POST,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载申报文档
 * @param {Object} params - 下载参数
 * @param {number} params.id - 申报ID
 * @param {string} [params.format] - 下载格式 (pdf/word/markdown)，当有 attachmentId 时可传 'original' 表示源文件格式
 * @param {number} [params.attachmentId] - 附件ID，有附件时传递（返回源文件格式，不支持转换）
 * @returns {Promise} 下载响应（Blob）
 */
export const downloadDeclaration = (params) => {
  const requestParams = {}
  if (params.format) {
    requestParams.format = params.format
  }
  if (params.attachmentId) {
    requestParams.attachmentId = params.attachmentId
  }
  
  return request({
    url: `${DECLARATION_API.DETAIL(params.id)}/download`,
    method: HTTP_METHODS.GET,
    params: requestParams,
    responseType: 'blob'
  })
}

/**
 * 获取申报预览内容
 * @param {Object} params - 预览参数
 * @param {number} params.id - 申报ID
 * @returns {Promise} 预览内容响应
 */
export const getDeclarationPreview = (params) => {
  return request({
    url: `${DECLARATION_API.DETAIL(params.id)}/preview`,
    method: HTTP_METHODS.GET
  })
}

/**
 * 获取申报工作流状态
 * @param {number} id - 申报ID
 * @returns {Promise} 工作流状态响应
 * 响应数据结构：
 * {
 *   code: 200,
 *   message: "操作成功",
 *   data: {
 *     steps: [
 *       { name: "申报提交", status: "success", timestamp: "2025-01-15T10:30:00" },
 *       ...
 *     ],
 *     fileUrl: "https://...", // 工作流完成后有值，执行中为null
 *     fileFormat: "pdf", // 工作流完成后有值，执行中为null
 *     timestamp: 1706428800000,
 *     traceId: "trace-abc123"
 *   }
 * }
 */
export const getDeclarationWorkflowStatus = (id) => {
  return request({
    url: DECLARATION_API.WORKFLOW_STATUS(id),
    method: HTTP_METHODS.GET
  })
}