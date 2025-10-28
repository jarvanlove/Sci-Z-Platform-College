/**
 * 权限指令
 * 用于控制元素的显示/隐藏基于用户权限
 * 
 * 使用方式：
 * v-permission="'button:user:create'"
 * v-permission="['button:user:create', 'button:user:edit']"
 */

import { useAuthStore } from '@/store/modules/auth'

export const permission = {
  mounted(el, binding) {
    const { value } = binding
    const authStore = useAuthStore()
    
    // 检查权限
    const hasPermission = checkPermission(value, authStore)
    
    if (!hasPermission) {
      // 没有权限时隐藏元素
      el.style.display = 'none'
      // 或者完全移除元素
      // el.parentNode?.removeChild(el)
    }
  },
  
  updated(el, binding) {
    const { value } = binding
    const authStore = useAuthStore()
    
    // 权限更新时重新检查
    const hasPermission = checkPermission(value, authStore)
    
    if (hasPermission) {
      el.style.display = ''
    } else {
      el.style.display = 'none'
    }
  }
}

/**
 * 检查权限
 * @param {string|string[]} permission - 权限码或权限码数组
 * @param {Object} authStore - 认证store
 * @returns {boolean} 是否有权限
 */
function checkPermission(permission, authStore) {
  if (!permission) return true
  
  // 如果是数组，需要所有权限都有
  if (Array.isArray(permission)) {
    return permission.every(p => authStore.hasPermission(p))
  }
  
  // 单个权限检查
  return authStore.hasPermission(permission)
}

/**
 * 角色指令
 * 用于控制元素的显示/隐藏基于用户角色
 * 
 * 使用方式：
 * v-role="'admin'"
 * v-role="['admin', 'user']"
 */
export const role = {
  mounted(el, binding) {
    const { value } = binding
    const authStore = useAuthStore()
    
    const hasRole = checkRole(value, authStore)
    
    if (!hasRole) {
      el.style.display = 'none'
    }
  },
  
  updated(el, binding) {
    const { value } = binding
    const authStore = useAuthStore()
    
    const hasRole = checkRole(value, authStore)
    
    if (hasRole) {
      el.style.display = ''
    } else {
      el.style.display = 'none'
    }
  }
}

/**
 * 检查角色
 * @param {string|string[]} role - 角色或角色数组
 * @param {Object} authStore - 认证store
 * @returns {boolean} 是否有角色
 */
function checkRole(role, authStore) {
  if (!role) return true
  
  if (Array.isArray(role)) {
    return role.some(r => authStore.hasRole(r))
  }
  
  return authStore.hasRole(role)
}

export default {
  permission,
  role
}
