// 状态管理类型定义

// 用户信息类型
export interface UserInfo {
  id: number
  username: string
  email: string
  phone?: string
  avatar?: string
  role: string
  permissions: string[]
  industry?: string // 多行业支持
  department?: string
  createdAt: string
  updatedAt: string
}

// 认证状态类型
export interface AuthState {
  token: string | null
  userInfo: UserInfo | null
  permissions: string[]
}

// 应用状态类型
export interface AppState {
  sidebarCollapsed: boolean
  theme: 'light' | 'dark'
  language: 'zh-CN' | 'en-US' | 'ko-KR' | 'ja-JP'
  loading: boolean
  pageTitle: string
}

// 申报信息类型
export interface Declaration {
  id: number
  title: string
  description: string
  researchDirection: string // 研究方向
  researchField: string // 研究领域
  status: 'draft' | 'pending' | 'approved' | 'rejected'
  workflowStatus?: string // Dify 工作流状态
  workflowId?: string // Dify 工作流 ID
  createdAt: string
  updatedAt: string
}

// 项目信息类型
export interface Project {
  id: number
  name: string
  description: string
  status: 'pending' | 'in_progress' | 'completed' | 'cancelled'
  startDate: string
  endDate: string
  declarationId?: number // 关联申报
  members: ProjectMember[]
  progress: number // 进度百分比
  createdAt: string
  updatedAt: string
}

// 项目成员类型
export interface ProjectMember {
  id: number
  userId: number
  projectId: number
  role: 'leader' | 'core' | 'member' // 负责人、核心成员、普通成员
  joinedAt: string
  user?: UserInfo
}

// 报告信息类型
export interface Report {
  id: number
  title: string
  type: 'tech' | 'self' // 科技报告、自评报告
  status: 'generating' | 'completed' | 'failed'
  projectId?: number
  content?: string // HTML 格式内容
  config: ReportConfig // 报告配置
  workflowId?: string // Dify 工作流 ID
  createdAt: string
  updatedAt: string
}

// 报告配置类型
export interface ReportConfig {
  style: string // 报告风格
  detailLevel: 'brief' | 'normal' | 'detailed' // 详细程度
  includeCharts: boolean // 是否包含图表
}

// 知识库信息类型
export interface Knowledge {
  id: number
  name: string
  description: string
  type: 'project' | 'independent'
  projectId?: number
  visibility: 'private' | 'public' // 私有、公开
  syncStatus: 'pending' | 'syncing' | 'completed' | 'failed' // 同步状态
  difyKnowledgeId?: string // Dify 知识库 ID
  createdAt: string
  updatedAt: string
}

// 文件信息类型
export interface FileInfo {
  id: number
  name: string
  originalName: string
  size: number
  type: string
  mimeType: string
  md5: string // 文件去重
  url: string
  minioPath: string
  knowledgeId?: number
  projectId?: number
  uploadedBy: number
  createdAt: string
  updatedAt: string
}

// AI 助手相关类型
export interface Conversation {
  id: number
  title: string
  userId: number
  knowledgeIds: number[] // 关联的知识库
  messageCount: number
  isPinned: boolean
  createdAt: string
  updatedAt: string
}

export interface Message {
  id: number
  conversationId: number
  role: 'user' | 'assistant'
  content: string
  sources?: MessageSource[] // 知识来源
  createdAt: string
}

export interface MessageSource {
  knowledgeId: number
  fileName: string
  confidence: number // 置信度
  excerpt: string // 相关片段
}

// 系统管理相关类型
export interface Role {
  id: number
  name: string
  code: string
  description: string
  permissions: string[]
  industry?: string // 按行业分类
  createdAt: string
  updatedAt: string
}

export interface Permission {
  id: number
  name: string
  code: string
  type: 'menu' | 'button' | 'api'
  parentId?: number
  path?: string
  icon?: string
  sort: number
  industry?: string // 按行业分类
}

export interface SystemConfig {
  id: number
  key: string
  value: string
  description: string
  type: 'string' | 'number' | 'boolean' | 'json'
  category: 'basic' | 'dify' | 'minio' | 'industry'
}

// 工作流相关类型
export interface WorkflowExecution {
  id: number
  workflowId: string
  status: 'pending' | 'running' | 'completed' | 'failed'
  progress: number // 0-100
  result?: any
  error?: string
  startedAt: string
  completedAt?: string
}

// 行业配置类型
export interface Industry {
  id: number
  name: string
  code: string
  fields: IndustryField[] // 动态字段
  permissions: string[] // 行业权限
  createdAt: string
  updatedAt: string
}

export interface IndustryField {
  name: string
  label: string
  type: 'text' | 'select' | 'textarea' | 'date'
  required: boolean
  options?: string[] // 下拉选项
}
