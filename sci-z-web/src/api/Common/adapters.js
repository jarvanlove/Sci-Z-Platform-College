/**
 * API 接口适配器
 * 处理前后端数据结构差异，提供统一的数据格式
 */

// ================================
// 1. 响应数据适配器
// ================================

export class ResponseAdapter {
  /**
   * 标准化响应数据
   * @param {Object} response - 原始响应数据
   * @returns {Object} 标准化后的响应数据
   */
  static normalize(response) {
    // 处理不同的响应格式
    if (response.code !== undefined) {
      // 标准格式：{code, message, data}
      return {
        code: response.code,
        message: response.message || 'success',
        data: response.data || null
      }
    } else if (response.status !== undefined) {
      // 某些 API 使用 status 字段
      return {
        code: response.status,
        message: response.message || 'success',
        data: response.data || response.result || null
      }
    } else {
      // 直接返回数据的情况
      return {
        code: 200,
        message: 'success',
        data: response
      }
    }
  }

  /**
   * 标准化分页响应数据
   * @param {Object} response - 原始分页响应数据
   * @returns {Object} 标准化后的分页响应数据
   */
  static normalizePage(response) {
    const normalized = this.normalize(response)
    const data = normalized.data || {}
    
    return {
      ...normalized,
      data: {
        records: data.records || data.list || data.items || [],
        total: data.total || data.totalCount || 0,
        current: data.current || data.page || data.pageNum || 1,
        size: data.size || data.pageSize || data.limit || 10,
        pages: data.pages || data.totalPages || Math.ceil((data.total || 0) / (data.size || 10))
      }
    }
  }
}

// ================================
// 2. 业务数据适配器
// ================================

export class DataAdapter {
  /**
   * 标准化用户数据
   * @param {Object} userData - 原始用户数据
   * @returns {Object} 标准化后的用户数据
   */
  static normalizeUser(userData) {
    if (!userData) return null
    
    return {
      id: userData.id,
      username: userData.username || userData.userName || userData.account,
      email: userData.email || userData.emailAddress,
      realName: userData.realName || userData.real_name || userData.name,
      avatar: userData.avatar || userData.avatarUrl || userData.headImg,
      phone: userData.phone || userData.phoneNumber,
      departmentId: userData.departmentId || userData.deptId,
      departmentName: userData.departmentName || userData.deptName,
      roleId: userData.roleId,
      roleName: userData.roleName || userData.role,
      status: userData.status || userData.state,
      createdAt: userData.createdAt || userData.createTime,
      updatedAt: userData.updatedAt || userData.updateTime,
      // 保留其他字段
      ...userData
    }
  }

  /**
   * 标准化项目数据
   * @param {Object} projectData - 原始项目数据
   * @returns {Object} 标准化后的项目数据
   */
  static normalizeProject(projectData) {
    if (!projectData) return null
    
    return {
      id: projectData.id,
      name: projectData.name || projectData.title,
      description: projectData.description || projectData.desc,
      status: projectData.status || projectData.state,
      type: projectData.type || projectData.category,
      leaderId: projectData.leaderId || projectData.leader_id,
      leaderName: projectData.leaderName || projectData.leader_name,
      departmentId: projectData.departmentId || projectData.deptId,
      departmentName: projectData.departmentName || projectData.deptName,
      budget: projectData.budget || projectData.amount,
      startDate: projectData.startDate || projectData.start_date,
      endDate: projectData.endDate || projectData.end_date,
      progress: projectData.progress || projectData.completion,
      members: projectData.members || projectData.memberList || [],
      attachments: projectData.attachments || projectData.files || [],
      createdAt: projectData.createdAt || projectData.createTime,
      updatedAt: projectData.updatedAt || projectData.updateTime,
      // 保留其他字段
      ...projectData
    }
  }

  /**
   * 标准化申报数据
   * @param {Object} declarationData - 原始申报数据
   * @returns {Object} 标准化后的申报数据
   */
  static normalizeDeclaration(declarationData) {
    if (!declarationData) return null
    
    return {
      id: declarationData.id,
      title: declarationData.title || declarationData.name,
      description: declarationData.description || declarationData.desc,
      type: declarationData.type || declarationData.category,
      status: declarationData.status || declarationData.state,
      applicantId: declarationData.applicantId || declarationData.applicant_id,
      applicantName: declarationData.applicantName || declarationData.applicant_name,
      departmentId: declarationData.departmentId || declarationData.deptId,
      departmentName: declarationData.departmentName || declarationData.deptName,
      budget: declarationData.budget || declarationData.amount,
      startDate: declarationData.startDate || declarationData.start_date,
      endDate: declarationData.endDate || declarationData.end_date,
      attachments: declarationData.attachments || declarationData.files || [],
      createdAt: declarationData.createdAt || declarationData.createTime,
      updatedAt: declarationData.updatedAt || declarationData.updateTime,
      // 保留其他字段
      ...declarationData
    }
  }

  /**
   * 标准化报告数据
   * @param {Object} reportData - 原始报告数据
   * @returns {Object} 标准化后的报告数据
   */
  static normalizeReport(reportData) {
    if (!reportData) return null
    
    return {
      id: reportData.id,
      title: reportData.title || reportData.name,
      type: reportData.type || reportData.category,
      status: reportData.status || reportData.state,
      projectId: reportData.projectId || reportData.project_id,
      projectName: reportData.projectName || reportData.project_name,
      authorId: reportData.authorId || reportData.author_id,
      authorName: reportData.authorName || reportData.author_name,
      content: reportData.content || reportData.text,
      attachments: reportData.attachments || reportData.files || [],
      generateTime: reportData.generateTime || reportData.generate_time,
      createdAt: reportData.createdAt || reportData.createTime,
      updatedAt: reportData.updatedAt || reportData.updateTime,
      // 保留其他字段
      ...reportData
    }
  }

  /**
   * 标准化知识库数据
   * @param {Object} knowledgeData - 原始知识库数据
   * @returns {Object} 标准化后的知识库数据
   */
  static normalizeKnowledge(knowledgeData) {
    if (!knowledgeData) return null
    
    return {
      id: knowledgeData.id,
      name: knowledgeData.name || knowledgeData.title,
      description: knowledgeData.description || knowledgeData.desc,
      type: knowledgeData.type || knowledgeData.category,
      ownerId: knowledgeData.ownerId || knowledgeData.owner_id,
      ownerName: knowledgeData.ownerName || knowledgeData.owner_name,
      departmentId: knowledgeData.departmentId || knowledgeData.deptId,
      departmentName: knowledgeData.departmentName || knowledgeData.deptName,
      fileCount: knowledgeData.fileCount || knowledgeData.file_count,
      totalSize: knowledgeData.totalSize || knowledgeData.total_size,
      status: knowledgeData.status || knowledgeData.state,
      createdAt: knowledgeData.createdAt || knowledgeData.createTime,
      updatedAt: knowledgeData.updatedAt || knowledgeData.updateTime,
      // 保留其他字段
      ...knowledgeData
    }
  }
}

// ================================
// 3. 请求数据适配器
// ================================

export class RequestAdapter {
  /**
   * 标准化分页请求参数
   * @param {Object} params - 原始请求参数
   * @returns {Object} 标准化后的请求参数
   */
  static normalizePageParams(params) {
    return {
      page: params.page || params.pageNum || 1,
      size: params.size || params.pageSize || params.limit || 10,
      keyword: params.keyword || params.search || params.query,
      ...params
    }
  }

  /**
   * 标准化时间范围参数
   * @param {Object} params - 原始请求参数
   * @returns {Object} 标准化后的请求参数
   */
  static normalizeDateRange(params) {
    return {
      startDate: params.startDate || params.start_date || params.beginTime,
      endDate: params.endDate || params.end_date || params.endTime,
      ...params
    }
  }
}

// ================================
// 4. 错误处理适配器
// ================================

export class ErrorAdapter {
  /**
   * 标准化错误信息
   * @param {Error|Object} error - 原始错误对象
   * @returns {Object} 标准化后的错误信息
   */
  static normalize(error) {
    if (error.response) {
      // HTTP 错误
      const { status, data } = error.response
      return {
        code: status,
        message: data?.message || data?.error || this.getStatusMessage(status),
        data: data
      }
    } else if (error.code) {
      // 业务错误
      return {
        code: error.code,
        message: error.message || '请求失败',
        data: error.data
      }
    } else {
      // 网络错误或其他错误
      return {
        code: 0,
        message: error.message || '网络错误',
        data: null
      }
    }
  }

  /**
   * 获取 HTTP 状态码对应的错误信息
   * @param {number} status - HTTP 状态码
   * @returns {string} 错误信息
   */
  static getStatusMessage(status) {
    const messages = {
      400: '请求参数错误',
      401: '未授权，请重新登录',
      403: '禁止访问',
      404: '请求的资源不存在',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务不可用',
      504: '网关超时'
    }
    return messages[status] || '请求失败'
  }
}

// ================================
// 5. 导出所有适配器
// ================================

export default {
  ResponseAdapter,
  DataAdapter,
  RequestAdapter,
  ErrorAdapter
}
