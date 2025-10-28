// 常量定义

// 应用配置
export const APP_CONFIG = {
  TITLE: import.meta.env.VITE_APP_TITLE || '生成式高校科研管理平台',
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

// 申报状态配置
export const DECLARATION_STATUS_CONFIG = {
  submitting: { text: '申报中', type: 'warning' },
  success: { text: '申报成功', type: 'success' },
  failed: { text: '申报失败', type: 'danger' }
}

// 申报部门选项
export const DECLARATION_DEPARTMENT_OPTIONS = [
  { label: '国自然-青年基金', value: '国自然-青年基金' },
  { label: '国自然-面上基金', value: '国自然-面上基金' },
  { label: '国自然-地区项目', value: '国自然-地区项目' },
  { label: '省市级项目', value: '省市级项目' },
  { label: '其他', value: '其他' }
]

// 日志级别配置
export const LOG_LEVELS = {
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR',
  DEBUG: 'DEBUG'
}

// 日志级别选项
export const LOG_LEVEL_OPTIONS = [
  { label: 'INFO', value: 'INFO' },
  { label: 'WARN', value: 'WARN' },
  { label: 'ERROR', value: 'ERROR' },
  { label: 'DEBUG', value: 'DEBUG' }
]

// 日志级别标签类型配置
export const LOG_LEVEL_TAG_CONFIG = {
  [LOG_LEVELS.INFO]: { type: 'success', text: 'INFO' },
  [LOG_LEVELS.WARN]: { type: 'warning', text: 'WARN' },
  [LOG_LEVELS.ERROR]: { type: 'danger', text: 'ERROR' },
  [LOG_LEVELS.DEBUG]: { type: 'info', text: 'DEBUG' }
}

// 登录状态标签类型配置（用户中心-安全设置）
export const LOGIN_STATUS_TAG_CONFIG = {
  success: { type: 'success' },
  failed: { type: 'danger' },
  warning: { type: 'warning' }
}

// 项目状态常量
export const PROJECT_STATUS = {
  PLANNED: 'planned',
  PROGRESS: 'progress', 
  COMPLETED: 'completed',
  DELAYED: 'delayed'
}

// 项目状态配置
export const PROJECT_STATUS_CONFIG = {
  [PROJECT_STATUS.PLANNED]: { text: '未开始', type: 'info', color: '#2563eb' },
  [PROJECT_STATUS.PROGRESS]: { text: '进行中', type: 'warning', color: '#f59e0b' },
  [PROJECT_STATUS.COMPLETED]: { text: '已完成', type: 'success', color: '#16a34a' },
  [PROJECT_STATUS.DELAYED]: { text: '已延期', type: 'danger', color: '#dc2626' }
}

// 里程碑状态常量
export const MILESTONE_STATUS = {
  PLANNED: 'planned',
  PROGRESS: 'progress',
  COMPLETED: 'completed', 
  DELAYED: 'delayed'
}

// 里程碑状态配置
export const MILESTONE_STATUS_CONFIG = {
  [MILESTONE_STATUS.PLANNED]: { text: '未开始', type: 'info', color: '#2563eb' },
  [MILESTONE_STATUS.PROGRESS]: { text: '进行中', type: 'warning', color: '#f59e0b' },
  [MILESTONE_STATUS.COMPLETED]: { text: '已完成', type: 'success', color: '#16a34a' },
  [MILESTONE_STATUS.DELAYED]: { text: '已延期', type: 'danger', color: '#dc2626' }
}

// 里程碑选项常量
export const MILESTONE_OPTIONS = [
  '项目启动：建设方案通过审核并备案',
  '任务分解与资源准备：项目任务书下达，资源配置完成，进入实施阶段',
  '项目实施与过程管理：中期检查',
  '成果整合与结题准备：结题验收',
  '后续管理与成果转化：项目归档完成，成果转化'
]


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
