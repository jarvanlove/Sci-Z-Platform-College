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

// 密码强度验证
export const validatePassword = (password) => {
  // 至少8位，包含字母和数字
  const re = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/
  return re.test(password)
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
