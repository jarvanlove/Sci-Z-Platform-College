// 常量定义

// 应用配置
export const APP_CONFIG = {
  TITLE: import.meta.env.VITE_APP_TITLE || '高校科研项目管理平台',
  VERSION: import.meta.env.VITE_APP_VERSION || '1.0.0',
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || '/api',
  DIFY_API_URL: import.meta.env.VITE_DIFY_API_URL,
  DIFY_API_KEY: import.meta.env.VITE_DIFY_API_KEY
}

// 状态常量
export const STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  IN_PROGRESS: 'in_progress',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled'
}

// 状态标签配置
export const STATUS_CONFIG = {
  [STATUS.PENDING]: { text: '待审核', type: 'warning' },
  [STATUS.APPROVED]: { text: '已通过', type: 'success' },
  [STATUS.REJECTED]: { text: '已拒绝', type: 'danger' },
  [STATUS.IN_PROGRESS]: { text: '进行中', type: 'primary' },
  [STATUS.COMPLETED]: { text: '已完成', type: 'success' },
  [STATUS.CANCELLED]: { text: '已取消', type: 'info' }
}

// 文件类型配置
export const FILE_TYPES = {
  IMAGE: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'],
  DOCUMENT: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'],
  TEXT: ['txt', 'md', 'json', 'xml', 'csv'],
  VIDEO: ['mp4', 'avi', 'mov', 'wmv', 'flv'],
  AUDIO: ['mp3', 'wav', 'flac', 'aac']
}

// 分页配置
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100]
}

// 权限常量
export const PERMISSIONS = {
  // 菜单权限
  DASHBOARD_VIEW: 'menu:dashboard:view',
  DECLARATION_LIST: 'menu:declaration:list',
  DECLARATION_CREATE: 'menu:declaration:create',
  DECLARATION_DETAIL: 'menu:declaration:detail',
  PROJECT_LIST: 'menu:project:list',
  PROJECT_DETAIL: 'menu:project:detail',
  PROJECT_PROGRESS: 'menu:project:progress',
  REPORT_LIST: 'menu:report:list',
  REPORT_GENERATE: 'menu:report:generate',
  KNOWLEDGE_LIST: 'menu:knowledge:list',
  KNOWLEDGE_DETAIL: 'menu:knowledge:detail',
  AI_CHAT: 'menu:ai:chat',
  USER_PROFILE: 'menu:user:profile',
  USER_SECURITY: 'menu:user:security',
  SYSTEM_USER: 'menu:system:user',
  SYSTEM_ROLE: 'menu:system:role',
  SYSTEM_CONFIG: 'menu:system:config',
  SYSTEM_LOGS: 'menu:system:logs'
}

// 路由常量
export const ROUTES = {
  LOGIN: '/login',
  REGISTER: '/register',
  RESET_PASSWORD: '/reset-password',
  DASHBOARD: '/dashboard',
  DECLARATION_LIST: '/declaration/list',
  DECLARATION_CREATE: '/declaration/create',
  DECLARATION_DETAIL: '/declaration/detail',
  PROJECT_LIST: '/project/list',
  PROJECT_DETAIL: '/project/detail',
  PROJECT_PROGRESS: '/project/progress',
  REPORT_LIST: '/report/list',
  REPORT_GENERATE: '/report/generate',
  KNOWLEDGE_LIST: '/knowledge/list',
  KNOWLEDGE_DETAIL: '/knowledge/detail',
  AI_CHAT: '/ai/chat',
  USER_PROFILE: '/user/profile',
  USER_SECURITY: '/user/security',
  SYSTEM_USER: '/system/user',
  SYSTEM_ROLE: '/system/role',
  SYSTEM_CONFIG: '/system/config',
  SYSTEM_LOGS: '/system/logs'
}
