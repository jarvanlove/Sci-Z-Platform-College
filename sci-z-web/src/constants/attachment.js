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
  'svg',
  'ico',
  'tiff',
  'tif',
  'heic',
  'heif',
  'raw',
  'cr2',
  'nef',
  'orf',
  'sr2'
])

/**
 * 上传文件类型白名单（扩展名）
 * 与后端 SystemConstant.SUPPORTED_FILE_TYPES 保持一致
 * 包含：文档、图片、视频、音频、代码、工业文件、日志、压缩包等常见格式
 */
export const SUPPORTED_FILE_EXTENSIONS = Object.freeze([
  // Office 文档
  'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx',
  'rtf', 'odt', 'ods', 'odp', 'pages', 'numbers', 'key',
  // 文本文件
  'txt', 'md', 'csv', 'json', 'xml', 'yaml', 'yml', 'ini', 'conf', 'cfg',
  'properties', 'log', 'out', 'err',
  // 代码文件
  'java', 'js', 'ts', 'jsx', 'tsx', 'py', 'pyw', 'cpp', 'c', 'h', 'hpp',
  'cs', 'php', 'rb', 'go', 'rs', 'swift', 'kt', 'scala', 'clj',
  'html', 'htm', 'css', 'scss', 'sass', 'less', 'vue', 'svelte',
  'sh', 'bash', 'bat', 'cmd', 'ps1', 'sql', 'pl', 'lua', 'r',
  'dart', 'elm', 'ex', 'exs', 'erl', 'hrl',
  // 图片文件
  'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg', 'ico', 'tiff', 'tif',
  'heic', 'heif', 'raw', 'cr2', 'nef', 'orf', 'sr2',
  // 视频文件
  'mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm', 'm4v', '3gp', '3g2',
  'mpg', 'mpeg', 'vob', 'ogv', 'asf', 'rm', 'rmvb', 'ts', 'mts',
  // 音频文件
  'mp3', 'wav', 'ogg', 'aac', 'flac', 'm4a', 'wma', 'opus', 'amr',
  'aiff', 'au', 'ra', 'ape', 'ac3', 'dts',
  // 压缩文件
  'zip', 'rar', '7z', 'tar', 'gz', 'bz2', 'xz', 'lz', 'lzma',
  'cab', 'iso', 'dmg', 'pkg', 'deb', 'rpm',
  // 工业文件
  'dwg', 'dxf', 'step', 'stp', 'iges', 'igs', '3dm', 'obj', 'stl',
  'fbx', 'dae', 'x3d', 'ply', 'off', 'wrl', 'vrml',
  // 其他常见格式
  'epub', 'mobi', 'azw', 'azw3', 'fb2', 'lit', 'prc',
  'ps', 'eps', 'ai', 'sketch', 'fig', 'xd'
])

/**
 * 上传文件 MIME 白名单
 * 与后端 SystemConstant.SUPPORTED_MIME_TYPES 保持一致
 * 包含：文档、图片、视频、音频、代码、工业文件、日志、压缩包等常见格式的 MIME 类型
 */
export const SUPPORTED_MIME_TYPES = Object.freeze([
  // Office 文档
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'application/vnd.ms-powerpoint',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  'application/rtf',
  'application/vnd.oasis.opendocument.text',
  'application/vnd.oasis.opendocument.spreadsheet',
  'application/vnd.oasis.opendocument.presentation',
  'application/vnd.apple.pages',
  'application/vnd.apple.numbers',
  'application/vnd.apple.keynote',
  // 文本文件
  'text/plain',
  'text/csv',
  'text/markdown',
  'text/x-markdown',
  'application/json',
  'application/xml',
  'text/xml',
  'application/x-yaml',
  'text/yaml',
  'text/x-ini',
  'text/x-properties',
  'text/x-log',
  // 代码文件
  'text/x-java-source',
  'text/javascript',
  'application/javascript',
  'text/typescript',
  'application/typescript',
  'text/x-python',
  'text/x-c++src',
  'text/x-csrc',
  'text/x-chdr',
  'text/x-c++hdr',
  'text/x-csharp',
  'text/x-php',
  'application/x-ruby',
  'text/x-go',
  'text/x-rust',
  'text/x-swift',
  'text/x-kotlin',
  'text/x-scala',
  'text/x-clojure',
  'text/html',
  'text/css',
  'text/x-scss',
  'text/x-sass',
  'text/less',
  'text/x-vue',
  'text/x-shellscript',
  'application/x-sh',
  'application/x-bat',
  'application/x-sql',
  'text/x-perl',
  'text/x-lua',
  'text/x-r',
  'text/x-dart',
  'text/x-elm',
  'text/x-erlang',
  // 图片文件
  'image/png',
  'image/jpeg',
  'image/gif',
  'image/bmp',
  'image/webp',
  'image/svg+xml',
  'image/x-icon',
  'image/tiff',
  'image/heic',
  'image/heif',
  'image/x-canon-cr2',
  'image/x-nikon-nef',
  'image/x-olympus-orf',
  'image/x-sony-sr2',
  // 视频文件
  'video/mp4',
  'video/x-msvideo',
  'video/quicktime',
  'video/x-ms-wmv',
  'video/x-flv',
  'video/x-matroska',
  'video/webm',
  'video/3gpp',
  'video/mpeg',
  'video/dvd',
  'video/ogg',
  'video/x-ms-asf',
  'application/vnd.rn-realmedia',
  'video/mp2t',
  // 音频文件
  'audio/mpeg',
  'audio/x-wav',
  'audio/wav',
  'audio/ogg',
  'audio/aac',
  'audio/flac',
  'audio/mp4',
  'audio/x-ms-wma',
  'audio/opus',
  'audio/amr',
  'audio/x-aiff',
  'audio/basic',
  'audio/vnd.rn-realaudio',
  'audio/x-ape',
  'audio/ac3',
  'audio/vnd.dts',
  // 压缩文件
  'application/zip',
  'application/x-rar-compressed',
  'application/x-7z-compressed',
  'application/x-tar',
  'application/gzip',
  'application/x-bzip2',
  'application/x-xz',
  'application/x-lzip',
  'application/x-lzma',
  'application/vnd.ms-cab-compressed',
  'application/x-iso9660-image',
  'application/x-apple-diskimage',
  'application/x-debian-package',
  'application/x-rpm',
  // 工业文件
  'application/acad',
  'application/dxf',
  'application/step',
  'application/iges',
  'model/obj',
  'model/stl',
  'model/x3d+xml',
  'model/vrml',
  // 电子书
  'application/epub+zip',
  'application/x-mobipocket-ebook',
  'application/vnd.amazon.ebook',
  'application/x-fictionbook+xml',
  'application/x-ms-reader',
  // 其他
  'application/postscript',
  'application/illustrator',
  'application/vnd.sketchup.skp',
  'application/fig',
  'application/vnd.adobe.xd'
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

