/**
 * 菜单 i18n 映射工具
 * 将菜单路径和权限编码映射到 i18n 键值
 * 支持通过 path 或 permission 字段进行匹配
 */

/**
 * 菜单路径到 i18n key 的映射表
 * 如果后端返回的菜单路径发生变化，需要同步更新此映射表
 */
const menuPathToI18nKey = {
  // 一级菜单
  '/dashboard': 'menu.dashboard',
  '/academic': 'menu.academic',
  '/declaration': 'menu.declaration',
  '/project': 'menu.project',
  '/acceptance': 'menu.acceptance',
  '/report': 'menu.report',
  '/knowledge': 'menu.knowledge',
  '/ai': 'menu.ai',
  '/user': 'menu.user',
  '/system': 'menu.system',
  
  // 二级菜单 - 学术搜索
  '/literature/search': 'menu.literature',
  
  // 二级菜单 - 申报管理
  '/declaration/list': 'menu.declarationList',
  '/declaration/create': 'menu.declarationCreate',
  
  // 二级菜单 - 项目管理
  '/project/list': 'menu.projectList',
  '/project/detail': 'menu.projectDetail',
  '/project/progress': 'menu.projectProgress',
  
  // 二级菜单 - 报告管理
  '/report/list': 'menu.reportList',
  '/report/generate': 'menu.reportGenerate',
  
  // 二级菜单 - 知识库
  '/knowledge/list': 'menu.knowledgeList',
  
  // 二级菜单 - AI助手
  '/ai/chat': 'menu.aiChat',
  
  // 二级菜单 - 用户中心
  '/user/profile': 'menu.userProfile',
  '/user/security': 'menu.userSecurity',
  
  // 二级菜单 - 系统管理
  '/system/user': 'menu.systemUser',
  '/system/role': 'menu.systemRole',
  '/system/config': 'menu.systemConfig',
  '/system/logs': 'menu.systemLogs',
  '/system/apikey': 'menu.apiKey'
}

/**
 * 菜单权限编码到 i18n key 的映射表
 * 根据后端返回的 permission 字段进行匹配
 * 格式：permission_code -> i18n_key
 */
const menuPermissionToI18nKey = {
  // 一级菜单权限
  'menu:dashboard': 'menu.dashboard',
  'menu:dashboard:view': 'menu.dashboard',
  'menu:academic:search': 'menu.academic',
  'menu:declaration': 'menu.declaration',
  'menu:declaration:list': 'menu.declaration',
  'menu:project': 'menu.project',
  'menu:project:list': 'menu.project',
  'menu:acceptance': 'menu.acceptance',
  'menu:report': 'menu.report',
  'menu:report:list': 'menu.acceptance',
  'menu:knowledge': 'menu.knowledge',
  'menu:ai': 'menu.ai',
  'menu:ai:chat': 'menu.ai',
  'menu:user': 'menu.user',
  'menu:user:profile': 'menu.user',
  'menu:system': 'menu.system',
  'menu:system:user': 'menu.system',
  
  // 二级菜单权限 - 学术搜索
  'menu:literature:search': 'menu.literature',
  
  // 二级菜单权限 - 申报管理
  'menu:declaration:list:sub': 'menu.declarationList',
  'menu:declaration:create': 'menu.declarationCreate',
  
  // 二级菜单权限 - 项目管理
  'menu:project:list:sub': 'menu.projectList',
  'menu:project:detail': 'menu.projectDetail',
  'menu:project:progress': 'menu.projectProgress',
  
  // 二级菜单权限 - 报告管理
  'menu:report:list:sub': 'menu.reportList',
  'menu:report:generate': 'menu.reportGenerate',
  
  // 二级菜单权限 - 知识库
  'menu:knowledge:list': 'menu.knowledgeList',
  
  // 二级菜单权限 - AI助手
  'menu:ai:chat:sub': 'menu.aiChat',
  
  // 二级菜单权限 - 用户中心
  'menu:user:profile:sub': 'menu.userProfile',
  'menu:user:security': 'menu.userSecurity',
  
  // 二级菜单权限 - 系统管理
  'menu:system:user:sub': 'menu.systemUser',
  'menu:system:role': 'menu.systemRole',
  'menu:system:config': 'menu.systemConfig',
  'menu:system:logs': 'menu.systemLogs',
  'menu:system:apikey': 'menu.apiKey'
}

/**
 * 根据菜单路径获取 i18n key
 * @param {string} path - 菜单路径
 * @returns {string|null} i18n key，如果找不到则返回 null
 */
export function getMenuI18nKeyByPath(path) {
  if (!path) return null
  return menuPathToI18nKey[path] || null
}

/**
 * 根据菜单权限编码获取 i18n key
 * @param {string} permission - 菜单权限编码
 * @returns {string|null} i18n key，如果找不到则返回 null
 */
export function getMenuI18nKeyByPermission(permission) {
  if (!permission) return null
  return menuPermissionToI18nKey[permission] || null
}

/**
 * 根据菜单对象获取 i18n key（优先使用 permission，其次使用 path）
 * @param {Object} menu - 菜单对象，包含 path、permission 等属性
 * @returns {string|null} i18n key，如果找不到则返回 null
 */
export function getMenuI18nKey(menu) {
  if (!menu) return null
  
  // 优先使用 permission 字段匹配
  if (menu.permission) {
    const permissionKey = getMenuI18nKeyByPermission(menu.permission)
    if (permissionKey) return permissionKey
  }
  
  // 如果 permission 匹配不到，使用 path 匹配
  if (menu.path) {
    const pathKey = getMenuI18nKeyByPath(menu.path)
    if (pathKey) return pathKey
  }
  
  return null
}

/**
 * 根据菜单对象获取翻译后的标题
 * @param {Object} menu - 菜单对象，包含 path、permission 和 title 属性
 * @param {Function} t - i18n 翻译函数
 * @returns {string} 翻译后的标题，如果找不到 i18n key 则返回原始 title
 */
export function getMenuTitle(menu, t) {
  if (!menu || !t) return menu?.title || ''
  
  // 使用改进后的 getMenuI18nKey，支持 permission 和 path 两种匹配方式
  const i18nKey = getMenuI18nKey(menu)
  if (i18nKey) {
    // 尝试翻译，如果翻译失败（返回 key 本身），则使用原始 title
    const translated = t(i18nKey)
    return translated !== i18nKey ? translated : menu.title
  }
  
  // 如果没有找到 i18n key，返回原始 title
  return menu.title || ''
}

/**
 * 递归处理菜单树，为每个菜单项添加翻译后的标题
 * @param {Array} menus - 菜单数组
 * @param {Function} t - i18n 翻译函数
 * @returns {Array} 处理后的菜单数组
 */
export function translateMenus(menus, t) {
  if (!menus || !Array.isArray(menus) || !t) return menus
  
  return menus.map(menu => {
    const translatedMenu = {
      ...menu,
      title: getMenuTitle(menu, t)
    }
    
    // 如果有子菜单，递归处理
    if (menu.children && Array.isArray(menu.children) && menu.children.length > 0) {
      translatedMenu.children = translateMenus(menu.children, t)
    }
    
    return translatedMenu
  })
}

export default {
  menuPathToI18nKey,
  menuPermissionToI18nKey,
  getMenuI18nKeyByPath,
  getMenuI18nKeyByPermission,
  getMenuI18nKey,
  getMenuTitle,
  translateMenus
}

