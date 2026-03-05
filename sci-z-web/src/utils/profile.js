/**
 * 用户资料相关工具
 * 用于判断个人信息是否已完善（与 UserProfile 表单必填项一致）
 */

const trim = (v) => (typeof v === 'string' ? v.trim() : '')

/** 系统自动生成的默认真实姓名前缀（如手机号登录即注册时生成的「用户_手机号」），此类用户必须完善个人信息 */
const DEFAULT_REAL_NAME_PREFIX = '用户_'

/**
 * 判断真实姓名是否为系统默认占位（未由用户填写）
 * @param {Object} userInfo - 用户信息对象
 * @returns {boolean} true 表示 realName 以「用户_」开头，需提示完善
 */
export function isDefaultRealName(userInfo) {
  if (!userInfo || typeof userInfo !== 'object') return false
  const realName = trim(userInfo.realName ?? userInfo.name ?? '')
  return realName.startsWith(DEFAULT_REAL_NAME_PREFIX)
}

/**
 * 判断用户资料是否未完善
 * 优先规则：realName 以「用户_」开头则一律视为未完善（登录即注册的默认占位）；
 * 否则与 UserProfile.vue 必填项一致：用户名、真实姓名、邮箱、手机号、部门任一缺失即为未完善。
 * @param {Object} userInfo - 用户信息对象（来自 authStore.userInfo 或接口）
 * @returns {boolean} true 表示未完善，需要提示用户去完善
 */
export function isProfileIncomplete(userInfo) {
  if (!userInfo || typeof userInfo !== 'object') {
    return true
  }
  if (isDefaultRealName(userInfo)) {
    return true
  }
  const username = trim(userInfo.username ?? userInfo.account ?? '')
  const realName = trim(userInfo.realName ?? userInfo.name ?? '')
  const email = trim(userInfo.email ?? '')
  const phone = trim(userInfo.phone ?? userInfo.mobile ?? '')
  const department = trim(userInfo.department ?? userInfo.departmentCode ?? '')
  return !username || !realName || !email || !phone || !department
}
