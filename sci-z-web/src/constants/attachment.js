/**
 * 附件关联对象枚举
 * 对齐后端 AttachmentRelationStatus，用于声明当前上传文件要挂在哪类业务实体下
 */
export const ATTACHMENT_RELATION = Object.freeze({
  PROJECT: 'project',
  DECLARATION: 'declaration',
  REPORT: 'report',
  USER: 'user',
  KNOWLEDGE: 'knowledge'
})

/**
 * 附件类别枚举
 * 对齐后端 AttachmentCategoryStatus，标识文件内容类型，便于服务端分类处理
 */
export const ATTACHMENT_CATEGORY = Object.freeze({
  DOCUMENT: 'document',
  IMAGE: 'image',
  EXPORT: 'export',
  OTHER: 'other'
})

export const IMAGE_FILE_EXTENSIONS = Object.freeze([
  'png',
  'jpg',
  'jpeg',
  'gif',
  'bmp',
  'webp',
  'svg'
])

/**
 * 上传文件类型白名单（扩展名）
 * 与后端 AttachmentUploadConfig.SUPPORTED_FILE_TYPES 保持一致
 */
export const SUPPORTED_FILE_EXTENSIONS = Object.freeze([
  'pdf',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'ppt',
  'pptx',
  'txt',
  'md',
  'png',
  'jpg',
  'jpeg',
  'gif',
  'bmp',
  'webp',
  'svg'
])

/**
 * 上传文件 MIME 白名单
 * 与后端 AttachmentUploadConfig.SUPPORTED_MIME_TYPES 保持一致
 */
export const SUPPORTED_MIME_TYPES = Object.freeze([
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'application/vnd.ms-powerpoint',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  'text/plain',
  'text/markdown',
  'image/png',
  'image/jpeg',
  'image/gif',
  'image/bmp',
  'image/webp',
  'image/svg+xml'
])

/**
 * 文件类型校验工具
 * @param {File} file - 待检测文件
 * @param {string[]} allowedExtensions - 允许的扩展名（默认全量白名单）
 * @returns {{passed: boolean, reason?: string}}
 */
export const validateFileType = (file, allowedExtensions = SUPPORTED_FILE_EXTENSIONS) => {
  const extension = file.name.split('.').pop()?.toLowerCase()
  const mimeType = (file.type || '').toLowerCase()

  if (!extension || !allowedExtensions.includes(extension)) {
    return {
      passed: false,
      reason: `仅支持以下文件格式：${allowedExtensions.join(', ')}`
    }
  }

  if (mimeType && !SUPPORTED_MIME_TYPES.includes(mimeType)) {
    return {
      passed: false,
      reason: '文件 MIME 类型不被支持，请检查文件是否损坏或扩展名是否正确'
    }
  }

  return { passed: true }
}

/**
 * 文件大小校验工具
 * @param {File} file - 待检测文件
 * @param {number} limitMB - 大小上限（单位 MB）
 * @returns {{passed: boolean, reason?: string}}
 */
export const validateFileSize = (file, limitMB) => {
  if (!limitMB || Number.isNaN(limitMB)) {
    return { passed: true }
  }
  const sizeLimit = limitMB * 1024 * 1024
  if (file.size > sizeLimit) {
    return {
      passed: false,
      reason: `文件大小不能超过 ${limitMB}MB`
    }
  }
  return { passed: true }
}

export const DEFAULT_AVATAR_MAX_SIZE_MB = 5

