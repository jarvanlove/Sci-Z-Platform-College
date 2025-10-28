/**
 * API 接口常量定义
 *
 */

// ================================
// 1. API 基础配置
// ================================

/**
 * API 基础路径
 */
export const API_BASE_URL = '/api'

/**
 * API 版本
 */
export const API_VERSION = 'v1'

// ================================
// 2. 认证模块 API 路径
// ================================

export const AUTH_API = {
  BASE_PATH: `${API_BASE_URL}/auth`,
  LOGIN: `${API_BASE_URL}/auth/login`,
  REGISTER: `${API_BASE_URL}/auth/register`,
  RESET_PASSWORD: `${API_BASE_URL}/auth/reset-password`,
  CAPTCHA: `${API_BASE_URL}/auth/captcha`,
  SEND_EMAIL_CODE: `${API_BASE_URL}/auth/send-email-code`,
  REFRESH_TOKEN: `${API_BASE_URL}/auth/refresh`,
  USER_INFO: `${API_BASE_URL}/auth/user-info`,
  PERMISSIONS: `${API_BASE_URL}/auth/permissions`,  // 获取用户权限列表
  MENUS: `${API_BASE_URL}/auth/menus`,              // 获取用户菜单列表
  LOGOUT: `${API_BASE_URL}/auth/logout`
}

// ================================
// 3. 申报模块 API 路径
// ================================

export const DECLARATION_API = {
  BASE_PATH: `${API_BASE_URL}/declaration`,
  LIST: `${API_BASE_URL}/declaration/list`,
  CREATE: `${API_BASE_URL}/declaration`,
  DETAIL: (id) => `${API_BASE_URL}/declaration/${id}`,
  UPDATE: (id) => `${API_BASE_URL}/declaration/${id}`,
  DELETE: (id) => `${API_BASE_URL}/declaration/${id}`,
  SUBMIT: (id) => `${API_BASE_URL}/declaration/${id}/submit`
}

// ================================
// 4. 项目模块 API 路径
// ================================

export const PROJECT_API = {
  BASE_PATH: `${API_BASE_URL}/${API_VERSION}/project`,
  LIST: `${API_BASE_URL}/${API_VERSION}/project/list`,
  DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/project/detail/${id}`,
  CREATE: `${API_BASE_URL}/${API_VERSION}/project/create`,
  UPDATE: (id) => `${API_BASE_URL}/${API_VERSION}/project/update/${id}`,
  MEMBERS: (id) => `${API_BASE_URL}/${API_VERSION}/project/members/${id}`,
  ADD_MEMBER: `${API_BASE_URL}/${API_VERSION}/project/members/add`,
  REMOVE_MEMBER: `${API_BASE_URL}/${API_VERSION}/project/members/remove`,
  PROGRESS: (id) => `${API_BASE_URL}/${API_VERSION}/project/progress/${id}`,
  ADD_PROGRESS: `${API_BASE_URL}/${API_VERSION}/project/progress/add`
}

// ================================
// 5. 验收模块 API 路径
// ================================

export const ACCEPTANCE_API = {
  BASE_PATH: `${API_BASE_URL}/${API_VERSION}/acceptance`,
  APPLY: `${API_BASE_URL}/${API_VERSION}/acceptance/apply`,
  LIST: `${API_BASE_URL}/${API_VERSION}/acceptance/list`,
  DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/acceptance/detail/${id}`,
  REPORT_GENERATE: `${API_BASE_URL}/${API_VERSION}/acceptance/report/generate`,
  REPORT_LIST: (id) => `${API_BASE_URL}/${API_VERSION}/acceptance/report/list/${id}`,
  REPORT_PREVIEW: (id) => `${API_BASE_URL}/${API_VERSION}/acceptance/report/preview/${id}`,
  REPORT_DOWNLOAD: (id) => `${API_BASE_URL}/${API_VERSION}/acceptance/report/download/${id}`,
  SUBMIT: (id) => `${API_BASE_URL}/${API_VERSION}/acceptance/submit/${id}`,
  REVIEW: (id) => `${API_BASE_URL}/${API_VERSION}/acceptance/review/${id}`
}

// ================================
// 6. 报告模块 API 路径
// ================================

export const REPORT_API = {
  BASE_PATH: `${API_BASE_URL}/${API_VERSION}/report`,
  LIST: `${API_BASE_URL}/${API_VERSION}/report/list`,
  CREATE: `${API_BASE_URL}/${API_VERSION}/report/create`,
  DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/report/detail/${id}`,
  GENERATE: `${API_BASE_URL}/${API_VERSION}/report/generate`,
  STATUS: (id) => `${API_BASE_URL}/${API_VERSION}/report/status/${id}`,
  EXPORT: `${API_BASE_URL}/${API_VERSION}/report/export`,
  EXPORTS: (id) => `${API_BASE_URL}/${API_VERSION}/report/${id}/exports`,
  REGENERATE: `${API_BASE_URL}/${API_VERSION}/report/regenerate`,
  DELETE: (id) => `${API_BASE_URL}/${API_VERSION}/report/${id}`
}

// ================================
// 7. AI助手模块 API 路径
// ================================

export const AI_API = {
  BASE_PATH: `${API_BASE_URL}/${API_VERSION}/ai`,
  CONVERSATIONS: `${API_BASE_URL}/${API_VERSION}/ai/chat/conversations`,
  CONVERSATION_DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/ai/chat/conversations/${id}`,
  MESSAGES: `${API_BASE_URL}/${API_VERSION}/ai/chat/messages`,
  MESSAGES_BY_CONVERSATION: (id) => `${API_BASE_URL}/${API_VERSION}/ai/chat/messages/${id}`,
  DELETE_MESSAGE: (id) => `${API_BASE_URL}/${API_VERSION}/ai/chat/messages/${id}`,
  WORKFLOW_EXECUTE: `${API_BASE_URL}/${API_VERSION}/ai/workflow/execute`,
  KNOWLEDGE_SYNC: `${API_BASE_URL}/${API_VERSION}/ai/knowledge/sync`,
  WORKFLOW_STATUS: (id) => `${API_BASE_URL}/${API_VERSION}/ai/workflow/status/${id}`
}

// ================================
// 8. 知识库模块 API 路径
// ================================

export const KNOWLEDGE_API = {
  BASE_PATH: `${API_BASE_URL}/${API_VERSION}/knowledge`,
  LIST: `${API_BASE_URL}/${API_VERSION}/knowledge/list`,
  CREATE: `${API_BASE_URL}/${API_VERSION}/knowledge/create`,
  DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/detail/${id}`,
  UPDATE: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/update/${id}`,
  DELETE: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/delete/${id}`,
  FOLDERS: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/folders/${id}`,
  CREATE_FOLDER: `${API_BASE_URL}/${API_VERSION}/knowledge/folders`,
  UPDATE_FOLDER: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/folders/${id}`,
  DELETE_FOLDER: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/folders/${id}`,
  UPLOAD_FILE: `${API_BASE_URL}/${API_VERSION}/knowledge/files/upload`,
  FILES: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/files/${id}`,
  DELETE_FILE: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/files/${id}`,
  RENAME_FILE: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/files/rename/${id}`,
  MOVE_FILE: (id) => `${API_BASE_URL}/${API_VERSION}/knowledge/files/move/${id}`,
  QA_ASK: `${API_BASE_URL}/${API_VERSION}/knowledge/qa/ask`
}

// ================================
// 9. 文件管理模块 API 路径
// ================================

export const FILE_API = {
  BASE_PATH: `${API_BASE_URL}/${API_VERSION}/file`,
  UPLOAD: `${API_BASE_URL}/${API_VERSION}/file/upload`,
  BATCH_UPLOAD: `${API_BASE_URL}/${API_VERSION}/file/batch-upload`,
  LIST: `${API_BASE_URL}/${API_VERSION}/file/list`,
  DOWNLOAD: (id) => `${API_BASE_URL}/${API_VERSION}/file/download/${id}`,
  PREVIEW: (id) => `${API_BASE_URL}/${API_VERSION}/file/preview/${id}`,
  DELETE: (id) => `${API_BASE_URL}/${API_VERSION}/file/delete/${id}`,
  CHECK_DUPLICATE: `${API_BASE_URL}/${API_VERSION}/file/check-duplicate`,
  SYNC_DIFY: `${API_BASE_URL}/${API_VERSION}/file/sync-dify`
}

// ================================
// 10. 系统管理模块 API 路径
// ================================

export const SYSTEM_API = {
  BASE_PATH: `${API_BASE_URL}/${API_VERSION}/system`,
  INDUSTRY_CONFIG: `${API_BASE_URL}/${API_VERSION}/system/industry/config`,
  DEPARTMENTS: `${API_BASE_URL}/${API_VERSION}/system/departments`,
  DEPARTMENT_DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/system/departments/${id}`,
  ROLES: `${API_BASE_URL}/${API_VERSION}/system/roles`,
  ROLE_DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/system/roles/${id}`,
  PERMISSIONS_TREE: `${API_BASE_URL}/${API_VERSION}/system/permissions/tree`,
  PERMISSIONS: `${API_BASE_URL}/${API_VERSION}/system/permissions`,
  PERMISSION_DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/system/permissions/${id}`,
  USERS: `${API_BASE_URL}/${API_VERSION}/system/users`,
  USER_DETAIL: (id) => `${API_BASE_URL}/${API_VERSION}/system/users/${id}`
}

// ================================
// 11. 用户模块 API 路径
// ================================

export const USER_API = {
  BASE_PATH: `${API_BASE_URL}/user`,
  INFO: `${API_BASE_URL}/user/info`,
  UPDATE_INFO: `${API_BASE_URL}/user/info`,
  CHANGE_PASSWORD: `${API_BASE_URL}/user/password`,
  UPLOAD_AVATAR: `${API_BASE_URL}/user/avatar`,
  LOGIN_LOGS: `${API_BASE_URL}/user/login-logs`
}

// ================================
// 12. HTTP 方法常量
// ================================

export const HTTP_METHODS = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE',
  PATCH: 'PATCH'
}

// ================================
// 13. 响应状态码常量
// ================================

export const RESPONSE_CODES = {
  SUCCESS: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500
}

// ================================
// 14. 文件类型常量
// ================================

export const FILE_TYPES = {
  IMAGE: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'],
  DOCUMENT: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'],
  TEXT: ['txt', 'md', 'json', 'xml', 'csv'],
  VIDEO: ['mp4', 'avi', 'mov', 'wmv', 'flv'],
  AUDIO: ['mp3', 'wav', 'flac', 'aac']
}

// ================================
// 15. 状态常量
// ================================

export const STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  IN_PROGRESS: 'in_progress',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled',
  ACTIVE: 'active',
  INACTIVE: 'inactive',
  DELETED: 'deleted'
}

// ================================
// 16. 导出所有常量
// ================================

export default {
  // API 基础配置
  API_BASE_URL,
  API_VERSION,
  
  // 模块 API 路径
  AUTH_API,
  DECLARATION_API,
  PROJECT_API,
  ACCEPTANCE_API,
  REPORT_API,
  AI_API,
  KNOWLEDGE_API,
  FILE_API,
  SYSTEM_API,
  USER_API,
  
  // HTTP 相关
  HTTP_METHODS,
  RESPONSE_CODES,
  
  // 业务常量
  FILE_TYPES,
  STATUS
}
