import { ref } from 'vue'

// 全局认证弹窗状态
const showLoginModal = ref(false)
const showRegisterModal = ref(false)
const showResetPasswordModal = ref(false)

/**
 * 认证弹窗管理 composable
 * 提供显示/隐藏登录、注册、重置密码弹窗的方法
 */
export function useAuthModal() {
  // 登录弹窗
  const openLoginModal = () => {
    showLoginModal.value = true
    // 关闭其他弹窗
    showRegisterModal.value = false
    showResetPasswordModal.value = false
  }

  const closeLoginModal = () => {
    showLoginModal.value = false
  }

  // 注册弹窗
  const openRegisterModal = () => {
    showRegisterModal.value = true
    // 关闭其他弹窗
    showLoginModal.value = false
    showResetPasswordModal.value = false
  }

  const closeRegisterModal = () => {
    showRegisterModal.value = false
  }

  // 重置密码弹窗
  const openResetPasswordModal = () => {
    showResetPasswordModal.value = true
    // 关闭其他弹窗
    showLoginModal.value = false
    showRegisterModal.value = false
  }

  const closeResetPasswordModal = () => {
    showResetPasswordModal.value = false
  }

  // 关闭所有弹窗
  const closeAllModals = () => {
    showLoginModal.value = false
    showRegisterModal.value = false
    showResetPasswordModal.value = false
  }

  return {
    // 登录弹窗
    showLoginModal,
    openLoginModal,
    closeLoginModal,
    // 注册弹窗
    showRegisterModal,
    openRegisterModal,
    closeRegisterModal,
    // 重置密码弹窗
    showResetPasswordModal,
    openResetPasswordModal,
    closeResetPasswordModal,
    // 关闭所有
    closeAllModals
  }
}

