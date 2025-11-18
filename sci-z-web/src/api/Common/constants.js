/**
 * API 接口常量定义
 *
 */

// ================================
// 1. API 基础配置
// ================================

/**
 * API 基础路径（与后端真实接口保持一致）
 */
export const API_BASE_URL = '/api'

/**
 * 拼接完整 API 地址
 * @param {string} path 模块相对路径，需以 / 开头
 * @returns {string}
 */
const buildApiUrl = (path) => `${API_BASE_URL}${path}`

// ================================
// 2. 认证模块 API 路径
// ================================

export const AUTH_API = {
  BASE_PATH: buildApiUrl('/auth'), // 认证模块基础路径
  LOGIN: buildApiUrl('/auth/login'), // 登录
  REGISTER: buildApiUrl('/auth/register'), // 注册
  RESET_PASSWORD: buildApiUrl('/auth/reset-password'), // 重置密码
  CAPTCHA: buildApiUrl('/auth/captcha'), // 获取验证码
  SEND_EMAIL_CODE: buildApiUrl('/auth/email-code'), // 发送邮箱验证码
  SEND_SMS_CODE: buildApiUrl('/auth/sms-code'), // 发送短信验证码
  REFRESH_TOKEN: buildApiUrl('/auth/refresh-token'), // 刷新令牌
  PROFILE: buildApiUrl('/auth/profile'), // 获取用户信息
  CHANGE_PASSWORD: buildApiUrl('/auth/change-password'),  // 修改密码
  CHECK_LOGIN: buildApiUrl('/auth/check/login'), // 检查登录
  CHECK_ROLE: buildApiUrl('/auth/check/role'), // 检查角色
  CHECK_PERMISSION: buildApiUrl('/auth/check/perm'),  // 检查权限
  PERMISSIONS: buildApiUrl('/auth/permissions'),  // 获取用户权限列表（保留占位，后端未实现）
  MENUS: buildApiUrl('/auth/menus'),              // 获取用户菜单列表（保留占位，后端未实现）
  LOGOUT: buildApiUrl('/auth/logout'), // 登出
  DEPARTMENT_LABEL: buildApiUrl('/auth/department/label') // 获取部门标签
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
  CONVERSATIONS: buildApiUrl('/ai/chat/conversations'),
  CONVERSATION_DETAIL: (id) => buildApiUrl(`/ai/chat/conversations/${id}`),
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
  DELETE: (id) => buildApiUrl(`/knowledge/delete/${id}`),
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
  BASE_PATH: buildApiUrl('/file'), // 文件管理模块基础路径
  UPLOAD: buildApiUrl('/file/upload'), // 上传文件
  BATCH_UPLOAD: buildApiUrl('/file/batch-upload'), // 批量上传文件
  LIST: buildApiUrl('/file/list'), // 获取文件列表
  DOWNLOAD: (id) => buildApiUrl(`/file/download/${id}`), // 下载文件
  PREVIEW: (id) => buildApiUrl(`/file/preview/${id}`), // 预览文件
  DELETE: (id) => buildApiUrl(`/file/delete/${id}`), // 删除文件
  CHECK_DUPLICATE: buildApiUrl('/file/check-duplicate'), // 检查文件是否重复
  SYNC_DIFY: buildApiUrl('/file/sync-dify') // 同步Dify文件
}

// ================================
// 10. 系统管理模块 API 路径
// ================================

export const SYSTEM_API = {
  BASE_PATH: buildApiUrl('/user'), // 系统管理模块基础路径
  INDUSTRY_CONFIG: buildApiUrl('/user/industry/config'), // 获取当前行业配置（从sys_config表）
  INDUSTRY_CONFIG_LIST: buildApiUrl('/user/industry/config/list'), // 获取所有行业配置模板（从sys_industry_config表）
  SYSTEM_CONFIG: buildApiUrl('/system/config'), // 获取系统配置（从sys_config表，key-value格式）
  SYSTEM_CONFIG_UPDATE: buildApiUrl('/system/config'), // 更新系统配置（更新sys_config表）
  DIFY_CONFIG: buildApiUrl('/system/config/dify'), // 获取Dify配置（从sys_config表）
  SAVE_DIFY_CONFIG: buildApiUrl('/system/config/dify'), // 保存Dify配置
  SAVE_INDUSTRY_CONFIG: buildApiUrl('/system/config/industry'), // 保存行业配置
  ROLES: buildApiUrl('/user/roles'), // 获取角色列表（创建/更新用）
  ROLES_LIST: buildApiUrl('/user/roles/list'), // 分页查询角色列表
  ROLE_DETAIL: (id) => buildApiUrl(`/user/roles/${id}`), // 获取角色详情
  ROLE_USERS: (id) => buildApiUrl(`/user/roles/${id}/users`), // 分页查询指定角色下的所有用户（用于点击用户数量时显示）
  ROLE_USER_IDS: (id) => buildApiUrl(`/user/roles/${id}/user-ids`), // 获取角色绑定的用户ID集合（用于回显）
  ROLE_PERMISSIONS: (id) => buildApiUrl(`/user/roles/${id}/permissions`), // 获取角色权限ID集合（用于回显）
  ROLE_PERMISSIONS_UPDATE: buildApiUrl('/user/roles/permissions'), // 更新角色权限
  USER_ROLES_UPDATE: buildApiUrl('/user/users/roles'), // 更新用户角色（全量替换）
  USER_ROLES: (id) => buildApiUrl(`/user/users/${id}/roles`), // 获取用户已绑定的角色ID集合（用于回显）
  PERMISSIONS_TREE: buildApiUrl('/user/permissions/tree'), // 获取权限树
  PERMISSIONS: buildApiUrl('/user/permissions'), // 获取权限列表
  USER_LIST: buildApiUrl('/user/users/list'), // 获取用户列表
  USER_CREATE: buildApiUrl('/user/users/create'), // 创建用户
  USER_DETAIL: (id) => buildApiUrl(`/user/users/${id}`), // 获取用户详情
  USER_STATUS: (id) => buildApiUrl(`/user/users/${id}/status`),  // 禁用/启用用户
  RESET_PASSWORD: buildApiUrl('/user/users/password/reset'),  // 管理员重置用户密码
  OPERATION_LOGS: buildApiUrl('/user/operation/logs')
}

// ================================
// 11. 用户模块 API 路径
// ================================

export const USER_API = {
  BASE_PATH: buildApiUrl('/user'),
  INFO: buildApiUrl('/user/info'),  // 更新用户信息
  PROFILE_FIELDS: buildApiUrl('/user/profile/fields'), // 获取用户信息字段
  UPLOAD_AVATAR: buildApiUrl('/user/avatar'), // 上传头像
  LOGIN_LOGS: buildApiUrl('/user/login/logs')  // 登录日志查询
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
