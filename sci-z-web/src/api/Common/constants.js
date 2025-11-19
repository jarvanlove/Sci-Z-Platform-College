/**
 * API 接口常量定义
 *
 */

// ================================
// 1. API 基础配置
// ================================

/**
 * API 基础路径（与后端真实接口保持一致）
 * 注意：baseURL 在 request.js 中已设置为 /api，这里只返回路径本身
 */
export const API_BASE_URL = '/api'

/**
 * 构建 API 路径（不包含 /api 前缀，因为 baseURL 已经包含）
 * @param {string} path 模块相对路径，需以 / 开头
 * @returns {string} 返回路径本身，不包含 /api 前缀
 */
const buildApiUrl = (path) => path

// ================================
// 2. 认证模块 API 路径
// ================================

export const AUTH_API = {
  BASE_PATH: buildApiUrl('/auth'),
  LOGIN: buildApiUrl('/auth/login'),
  REGISTER: buildApiUrl('/auth/register'),
  RESET_PASSWORD: buildApiUrl('/auth/reset-password'),
  CAPTCHA: buildApiUrl('/auth/captcha'),
  SEND_EMAIL_CODE: buildApiUrl('/auth/email-code'),
  SEND_SMS_CODE: buildApiUrl('/auth/sms-code'),
  REFRESH_TOKEN: buildApiUrl('/auth/refresh-token'),
  PROFILE: buildApiUrl('/auth/profile'),
  CHECK_LOGIN: buildApiUrl('/auth/check/login'),
  CHECK_ROLE: buildApiUrl('/auth/check/role'),
  CHECK_PERMISSION: buildApiUrl('/auth/check/perm'),
  PERMISSIONS: buildApiUrl('/auth/permissions'),  // 获取用户权限列表（保留占位，后端未实现）
  MENUS: buildApiUrl('/auth/menus'),              // 获取用户菜单列表（保留占位，后端未实现）
  LOGOUT: buildApiUrl('/auth/logout'),
  DEPARTMENT_LABEL: buildApiUrl('/auth/department/label')
}

// ================================
// 3. 申报模块 API 路径
// ================================

export const DECLARATION_API = {
  BASE_PATH: buildApiUrl('/declaration'),
  LIST: buildApiUrl('/declaration/list'),
  CREATE: buildApiUrl('/declaration'),
  DETAIL: (id) => buildApiUrl(`/declaration/${id}`),
  UPDATE: (id) => buildApiUrl(`/declaration/${id}`),
  DELETE: (id) => buildApiUrl(`/declaration/${id}`),
  SUBMIT: (id) => buildApiUrl(`/declaration/${id}/submit`)
}

// ================================
// 4. 项目模块 API 路径
// ================================

export const PROJECT_API = {
  BASE_PATH: buildApiUrl('/project'),
  LIST: buildApiUrl('/project/list'),
  DETAIL: (id) => buildApiUrl(`/project/detail/${id}`),
  CREATE: buildApiUrl('/project/create'),
  UPDATE: (id) => buildApiUrl(`/project/update/${id}`),
  MEMBERS: (id) => buildApiUrl(`/project/members/${id}`),
  ADD_MEMBER: buildApiUrl('/project/members/add'),
  REMOVE_MEMBER: buildApiUrl('/project/members/remove'),
  PROGRESS: (id) => buildApiUrl(`/project/progress/${id}`),
  ADD_PROGRESS: buildApiUrl('/project/progress/add')
}

// ================================
// 5. 验收模块 API 路径
// ================================

export const ACCEPTANCE_API = {
  BASE_PATH: buildApiUrl('/acceptance'),
  APPLY: buildApiUrl('/acceptance/apply'),
  LIST: buildApiUrl('/acceptance/list'),
  DETAIL: (id) => buildApiUrl(`/acceptance/detail/${id}`),
  REPORT_GENERATE: buildApiUrl('/acceptance/report/generate'),
  REPORT_LIST: (id) => buildApiUrl(`/acceptance/report/list/${id}`),
  REPORT_PREVIEW: (id) => buildApiUrl(`/acceptance/report/preview/${id}`),
  REPORT_DOWNLOAD: (id) => buildApiUrl(`/acceptance/report/download/${id}`),
  SUBMIT: (id) => buildApiUrl(`/acceptance/submit/${id}`),
  REVIEW: (id) => buildApiUrl(`/acceptance/review/${id}`)
}

// ================================
// 6. 报告模块 API 路径
// ================================

export const REPORT_API = {
  BASE_PATH: buildApiUrl('/report'),
  LIST: buildApiUrl('/report/list'),
  CREATE: buildApiUrl('/report/create'),
  DETAIL: (id) => buildApiUrl(`/report/detail/${id}`),
  GENERATE: buildApiUrl('/report/generate'),
  STATUS: (id) => buildApiUrl(`/report/status/${id}`),
  EXPORT: buildApiUrl('/report/export'),
  EXPORTS: (id) => buildApiUrl(`/report/${id}/exports`),
  REGENERATE: buildApiUrl('/report/regenerate'),
  DELETE: (id) => buildApiUrl(`/report/${id}`)
}

// ================================
// 7. AI助手模块 API 路径
// ================================

export const AI_API = {
  BASE_PATH: buildApiUrl('/ai'),
  // AI会话管理接口
  CONVERSATION_BASE: buildApiUrl('/ai/conversation'),
  CONVERSATION_CREATE: buildApiUrl('/ai/conversation'),
  CONVERSATION_UPDATE: buildApiUrl('/ai/conversation'),
  CONVERSATION_DETAIL: (id) => buildApiUrl(`/ai/conversation/${id}`),
  CONVERSATION_DELETE: (id) => buildApiUrl(`/ai/conversation/${id}`),
  CONVERSATION_BATCH_DELETE: buildApiUrl('/ai/conversation/batch'),
  CONVERSATION_PAGE: buildApiUrl('/ai/conversation/page'),
  CONVERSATION_LIST: buildApiUrl('/ai/conversation/list'),
  CONVERSATION_PINNED: (id) => buildApiUrl(`/ai/conversation/${id}/pinned`),
  // AI消息管理接口
  MESSAGE_BASE: buildApiUrl('/ai/message'),
  MESSAGE_CREATE: buildApiUrl('/ai/message'),
  MESSAGE_UPDATE: buildApiUrl('/ai/message'),
  MESSAGE_DETAIL: (id) => buildApiUrl(`/ai/message/${id}`),
  MESSAGE_DELETE: (id) => buildApiUrl(`/ai/message/${id}`),
  MESSAGE_BATCH_DELETE: buildApiUrl('/ai/message/batch'),
  MESSAGE_PAGE: buildApiUrl('/ai/message/page'),
  MESSAGE_LIST: (conversationId) => buildApiUrl(`/ai/message/list/${conversationId}`),
  MESSAGE_DELETE_BY_CONVERSATION: (conversationId) => buildApiUrl(`/ai/message/conversation/${conversationId}`),
  // Chat 工作流接口
  CHAT_WORKFLOW_RUN: buildApiUrl('/chat/workflow/run'),
  // 旧接口（保留兼容）
  CONVERSATIONS: buildApiUrl('/ai/chat/conversations'),
  CONVERSATION_DETAIL_OLD: (id) => buildApiUrl(`/ai/chat/conversations/${id}`),
  MESSAGES: buildApiUrl('/ai/chat/messages'),
  MESSAGES_BY_CONVERSATION: (id) => buildApiUrl(`/ai/chat/messages/${id}`),
  DELETE_MESSAGE: (id) => buildApiUrl(`/ai/chat/messages/${id}`),
  WORKFLOW_EXECUTE: buildApiUrl('/ai/workflow/execute'),
  KNOWLEDGE_SYNC: buildApiUrl('/ai/knowledge/sync'),
  WORKFLOW_STATUS: (id) => buildApiUrl(`/ai/workflow/status/${id}`)
}

// ================================
// 8. 知识库模块 API 路径
// ================================

export const KNOWLEDGE_API = {
  BASE_PATH: buildApiUrl('/knowledge'),
  LIST: buildApiUrl('/knowledge'), // GET /api/knowledge?page=1&size=10
  CREATE: buildApiUrl('/knowledge'), // POST /api/knowledge
  DETAIL: (id) => buildApiUrl(`/knowledge/detail/${id}`),
  UPDATE: (id) => buildApiUrl(`/knowledge/update/${id}`),
  DELETE: (id) => buildApiUrl(`/knowledge/${id}`), // DELETE /api/knowledge/{id}
  FOLDERS: (id) => buildApiUrl(`/knowledge/folders/${id}`),
  CREATE_FOLDER: buildApiUrl('/knowledge/folders'),
  UPDATE_FOLDER: (id) => buildApiUrl(`/knowledge/folders/${id}`),
  DELETE_FOLDER: (id) => buildApiUrl(`/knowledge/folders/${id}`),
  UPLOAD_FILE: buildApiUrl('/knowledge/files/upload'),
  UPLOAD: (id) => buildApiUrl(`/knowledge/${id}/upload`), // POST /api/knowledge/{difyKbId}/upload
  FILES: (id) => buildApiUrl(`/knowledge/files/${id}`),
  DELETE_FILE: (id) => buildApiUrl(`/knowledge/files/${id}`),
  RENAME_FILE: (id) => buildApiUrl(`/knowledge/files/rename/${id}`),
  MOVE_FILE: (id) => buildApiUrl(`/knowledge/files/move/${id}`),
  SEARCH: (id) => buildApiUrl(`/knowledge/${id}/search`), // GET /api/knowledge/{id}/search?query=xxx
  QA_ASK: buildApiUrl('/knowledge/qa/ask'),
  // 知识库Chatbot流式对话接口
  CHATBOT_STREAM: buildApiUrl('/knowledge/chatbot/stream'), // POST /api/knowledge/chatbot/stream
  // 文件关联接口
  FILE_RELATION_BASE: buildApiUrl('/knowledge/file-relation'),
  FILE_RELATION_CREATE: buildApiUrl('/knowledge/file-relation'), // POST /api/knowledge/file-relation
  FILE_RELATION_UPDATE: (id) => buildApiUrl(`/knowledge/file-relation/${id}`), // PUT /api/knowledge/file-relation/{id}
  FILE_RELATION_DELETE: (id) => buildApiUrl(`/knowledge/file-relation/${id}`), // DELETE /api/knowledge/file-relation/{id}
  FILE_RELATION_DETAIL: (id) => buildApiUrl(`/knowledge/file-relation/${id}`), // GET /api/knowledge/file-relation/{id}
  FILE_RELATION_LIST: buildApiUrl('/knowledge/file-relation') // GET /api/knowledge/file-relation?knowledgeId=1&folderId=0&page=1&size=10
}

// ================================
// 9. 文件管理模块 API 路径
// ================================

export const FILE_API = {
  BASE_PATH: buildApiUrl('/file'),
  UPLOAD: buildApiUrl('/file/upload'),
  BATCH_UPLOAD: buildApiUrl('/file/batch-upload'),
  LIST: buildApiUrl('/file/list'),
  DOWNLOAD: (id) => buildApiUrl(`/file/download/${id}`),
  PREVIEW: (id) => buildApiUrl(`/file/preview/${id}`),
  DELETE: (id) => buildApiUrl(`/file/delete/${id}`),
  CHECK_DUPLICATE: buildApiUrl('/file/check-duplicate'),
  SYNC_DIFY: buildApiUrl('/file/sync-dify')
}

// ================================
// 10. 系统管理模块 API 路径
// ================================

export const SYSTEM_API = {
  BASE_PATH: buildApiUrl('/user'),
  INDUSTRY_CONFIG: buildApiUrl('/user/industry/config'),
  DEPARTMENTS: buildApiUrl('/user/departments'),
  DEPARTMENT_DETAIL: (id) => buildApiUrl(`/user/departments/${id}`),
  ROLES: buildApiUrl('/user/roles'),
  ROLE_DETAIL: (id) => buildApiUrl(`/user/roles/${id}`),
  PERMISSIONS_TREE: buildApiUrl('/user/permissions/tree'),
  PERMISSIONS: buildApiUrl('/user/permissions'),
  PERMISSION_DETAIL: (id) => buildApiUrl(`/user/permissions/${id}`),
  USERS: buildApiUrl('/user/users'),
  USER_DETAIL: (id) => buildApiUrl(`/user/users/${id}`)
}

// ================================
// 11. 用户模块 API 路径
// ================================

export const USER_API = {
  BASE_PATH: buildApiUrl('/user'),
  INFO: buildApiUrl('/auth/user/info'),
  UPDATE_INFO: buildApiUrl('/auth/user/info'),
  PROFILE_FIELDS: buildApiUrl('/user/profile/fields'),
  CHANGE_PASSWORD: buildApiUrl('/user/password'),
  UPLOAD_AVATAR: buildApiUrl('/user/avatar'),
  LOGIN_LOGS: buildApiUrl('/user/login-logs')
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
