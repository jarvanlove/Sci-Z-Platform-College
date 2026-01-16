import { useAuthModal } from './useAuthModal'

/**
 * 登录弹窗管理 composable（向后兼容）
 * 实际使用 useAuthModal，保持 API 兼容性
 */
export function useLoginModal() {
  const { showLoginModal, openLoginModal, closeLoginModal } = useAuthModal()
  
  return {
    showLoginModal,
    openLoginModal,
    closeLoginModal
  }
}

// 重新导出 useAuthModal，方便新代码使用
export { useAuthModal }

