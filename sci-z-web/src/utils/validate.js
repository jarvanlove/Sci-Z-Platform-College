// 表单验证工具函数

// 邮箱验证
export const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

// 手机号验证
export const validatePhone = (phone) => {
  const re = /^1[3-9]\d{9}$/
  return re.test(phone)
}

// 密码长度验证（6-20位）
export const validatePassword = (password) => {
  return typeof password === 'string' && password.length >= 6 && password.length <= 20
}

// 身份证号验证
export const validateIdCard = (idCard) => {
  const re = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return re.test(idCard)
}

// URL验证
export const validateUrl = (url) => {
  const re = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/
  return re.test(url)
}

// 数字验证
export const validateNumber = (value) => {
  return !isNaN(value) && !isNaN(parseFloat(value))
}

// 正整数验证
export const validatePositiveInteger = (value) => {
  const re = /^[1-9]\d*$/
  return re.test(value)
}

// 中文姓名验证
export const validateChineseName = (name) => {
  const re = /^[\u4e00-\u9fa5]{2,10}$/
  return re.test(name)
}

// 文件大小验证
export const validateFileSize = (file, maxSize = 10 * 1024 * 1024) => {
  return file.size <= maxSize
}

// 文件类型验证
export const validateFileType = (file, allowedTypes = ['jpg', 'png', 'pdf']) => {
  const extension = file.name.split('.').pop().toLowerCase()
  return allowedTypes.includes(extension)
}

// 用户名验证（字母数字下划线，3-20位）
export const validateUsername = (username) => {
  const re = /^[a-zA-Z0-9_]{3,20}$/
  return re.test(username)
}

// 验证码验证（6位数字）
export const validateVerificationCode = (code) => {
  const re = /^\d{6}$/
  return re.test(code)
}

// 金额验证（最多两位小数）
export const validateAmount = (amount) => {
  const re = /^(0|[1-9]\d*)(\.\d{1,2})?$/
  return re.test(amount)
}

// 日期格式验证（YYYY-MM-DD）
export const validateDate = (date) => {
  const re = /^\d{4}-\d{2}-\d{2}$/
  return re.test(date)
}

// 时间格式验证（HH:mm:ss）
export const validateTime = (time) => {
  const re = /^([01]?\d|2[0-3]):[0-5]\d:[0-5]\d$/
  return re.test(time)
}

// 邮政编码验证
export const validatePostalCode = (code) => {
  const re = /^\d{6}$/
  return re.test(code)
}

// 银行卡号验证（16-19位数字）
export const validateBankCard = (cardNumber) => {
  const re = /^\d{16,19}$/
  return re.test(cardNumber)
}

// 非空字符串验证
export const validateNotEmpty = (value) => {
  return value && value.trim().length > 0
}

// 长度范围验证
export const validateLength = (value, min = 0, max = 100) => {
  return value && value.length >= min && value.length <= max
}

// 纯数字验证（包含小数）
export const validateDecimal = (value) => {
  const re = /^\d+(\.\d+)?$/
  return re.test(value)
}

// 邮箱或手机号验证
export const validateEmailOrPhone = (value) => {
  return validateEmail(value) || validatePhone(value)
}

// 密码确认验证
export const validatePasswordConfirm = (password, confirmPassword) => {
  return password === confirmPassword
}

// 年龄验证（1-120岁）
export const validateAge = (age) => {
  const num = parseInt(age)
  return !isNaN(num) && num >= 1 && num <= 120
}

// 百分比验证（0-100）
export const validatePercentage = (value) => {
  const num = parseFloat(value)
  return !isNaN(num) && num >= 0 && num <= 100
}

// 项目名称验证（2-100个字符）
export const validateProjectName = (name) => {
  return name && name.trim().length >= 2 && name.trim().length <= 100
}

// 预算金额验证（非负数）
export const validateBudget = (amount) => {
  const num = parseFloat(amount)
  return !isNaN(num) && num >= 0
}

// 端口号验证（1-65535）
export const validatePort = (port) => {
  const num = parseInt(port)
  return !isNaN(num) && num >= 1 && num <= 65535
}

// 文件大小验证（MB单位）
export const validateFileSizeMB = (size, maxSizeMB = 10) => {
  const num = parseFloat(size)
  return !isNaN(num) && num >= 0 && num <= maxSizeMB
}

// 密码长度验证（指定范围）
export const validatePasswordLength = (password, minLength = 6, maxLength = 20) => {
  return password && password.length >= minLength && password.length <= maxLength
}

// 角色名称验证（2-20个字符）
export const validateRoleName = (name) => {
  return name && name.trim().length >= 2 && name.trim().length <= 20
}

// 系统名称验证（1-50个字符）
export const validateSystemName = (name) => {
  return name && name.trim().length >= 1 && name.trim().length <= 50
}

// 系统描述验证（1-200个字符）
export const validateSystemDescription = (description) => {
  return description && description.trim().length >= 1 && description.trim().length <= 200
}

// API URL验证
export const validateApiUrl = (url) => {
  const re = /^https?:\/\/.+\..+/
  return re.test(url)
}

// SMTP主机验证
export const validateSmtpHost = (host) => {
  const re = /^[a-zA-Z0-9.-]+$/
  return re.test(host)
}

// 上传路径验证
export const validateUploadPath = (path) => {
  const re = /^\/[a-zA-Z0-9\/_-]*$/
  return re.test(path)
}

// 最大登录尝试次数验证（1-100）
export const validateMaxLoginAttempts = (attempts) => {
  const num = parseInt(attempts)
  return !isNaN(num) && num >= 1 && num <= 100
}

// 会话超时时间验证（30-480分钟）
export const validateSessionTimeout = (timeout) => {
  const num = parseInt(timeout)
  return !isNaN(num) && num >= 30 && num <= 480
}

// 报告字数验证（1000-50000字）
export const validateReportWordCount = (count) => {
  const num = parseInt(count)
  return !isNaN(num) && num >= 1000 && num <= 50000
}

// 研究领域数量验证（最多10个）
export const validateResearchFieldCount = (fields) => {
  return Array.isArray(fields) && fields.length <= 10
}

// 文件数量验证（最多指定数量）
export const validateFileCount = (files, maxCount = 5) => {
  return Array.isArray(files) && files.length <= maxCount
}