/**
 * API 接口类型定义
 * 设计理念：只定义稳定的核心类型，支持动态扩展
 */

// ================================
// 1. 基础响应类型（稳定不变）
// ================================

/**
 * 统一响应格式
 */
export const BaseResponse = {
  code: Number,      // 响应码
  message: String,   // 响应消息
  data: Object       // 响应数据（动态）
}

/**
 * 分页响应格式
 */
export const PageResponse = {
  code: Number,
  message: String,
  data: {
    records: Array,     // 数据列表
    total: Number,      // 总记录数
    current: Number,    // 当前页码
    size: Number,       // 每页大小
    pages: Number       // 总页数
  }
}

// ================================
// 2. 认证相关类型
// ================================

/**
 * 登录请求参数
 */
export const LoginRequest = {
  username: String,
  password: String,
  captcha: String,
  captchaId: String
}

/**
 * 登录响应数据
 */
export const LoginResponse = {
  token: String,
  refreshToken: String,
  user: Object
}

/**
 * 注册请求参数
 */
export const RegisterRequest = {
  username: String,
  password: String,
  email: String,
  realName: String,
  captcha: String,
  captchaId: String
}

/**
 * 重置密码请求参数
 */
export const ResetPasswordRequest = {
  email: String,
  captcha: String,
  emailCode: String,
  newPassword: String
}

// ================================
// 3. 核心业务类型（只定义稳定字段）
// ================================

/**
 * 用户核心信息（稳定字段）
 */
export const User = {
  id: Number,
  username: String,
  email: String,
  realName: String,
  avatar: String
  // 其他字段通过 data 动态传递
}

/**
 * 项目核心信息（稳定字段）
 */
export const Project = {
  id: Number,
  name: String,
  status: String,
  leaderId: Number,
  leaderName: String
  // 其他字段通过 data 动态传递
}

/**
 * 申报核心信息（稳定字段）
 */
export const Declaration = {
  id: Number,
  title: String,
  status: String,
  applicantId: Number,
  applicantName: String
  // 其他字段通过 data 动态传递
}

/**
 * 报告核心信息（稳定字段）
 */
export const Report = {
  id: Number,
  title: String,
  type: String,
  status: String,
  projectId: Number
  // 其他字段通过 data 动态传递
}

/**
 * 知识库核心信息（稳定字段）
 */
export const Knowledge = {
  id: Number,
  name: String,
  type: String,
  ownerId: Number,
  ownerName: String
  // 其他字段通过 data 动态传递
}

// ================================
// 3. 查询参数类型（通用结构）
// ================================

/**
 * 通用分页查询参数
 */
export const PageParams = {
  page: Number,         // 页码
  size: Number,         // 每页数量
  keyword: String       // 关键词搜索
}

/**
 * 通用时间范围查询参数
 */
export const DateRangeParams = {
  startDate: String,    // 开始日期
  endDate: String       // 结束日期
}

// ================================
// 4. 状态常量（稳定不变）
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

export const USER_ROLES = {
  ADMIN: 'admin',
  TEACHER: 'teacher',
  STUDENT: 'student',
  RESEARCHER: 'researcher'
}

export const PROJECT_TYPES = {
  NATIONAL: 'national',
  PROVINCIAL: 'provincial',
  MUNICIPAL: 'municipal',
  SCHOOL: 'school'
}

// ================================
// 5. 类型工具函数
// ================================

/**
 * 创建动态类型（用于运行时类型检查）
 */
export const createDynamicType = (baseType, additionalFields = {}) => {
  return {
    ...baseType,
    ...additionalFields
  }
}

/**
 * 类型验证函数
 */
export const validateType = (data, type) => {
  const errors = []
  
  for (const [key, expectedType] of Object.entries(type)) {
    if (data[key] !== undefined) {
      const actualType = typeof data[key]
      if (actualType !== expectedType.name.toLowerCase()) {
        errors.push(`${key} 期望类型 ${expectedType.name}，实际类型 ${actualType}`)
      }
    }
  }
  
  return {
    isValid: errors.length === 0,
    errors
  }
}

// ================================
// 6. 导出所有类型
// ================================

export default {
  // 基础类型
  BaseResponse,
  PageResponse,
  
  // 核心业务类型
  User,
  Project,
  Declaration,
  Report,
  Knowledge,
  
  // 查询参数类型
  PageParams,
  DateRangeParams,
  
  // 状态常量
  STATUS,
  USER_ROLES,
  PROJECT_TYPES,
  
  // 工具函数
  createDynamicType,
  validateType
}