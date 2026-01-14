import { ref } from 'vue'

// 全局登录弹窗状态
const showLoginModal = ref(false)

/**
 * 登录弹窗管理 composable
 * 提供显示/隐藏登录弹窗的方法
 */
export function useLoginModal() {
  const openLoginModal = () => {
    showLoginModal.value = true
  }

  const closeLoginModal = () => {
    showLoginModal.value = false
  }

  return {
    showLoginModal,
    openLoginModal,
    closeLoginModal
  }
}

