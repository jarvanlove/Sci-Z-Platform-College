import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/Auth'
import { getToken, setToken, removeToken, getUserInfo as getLocalUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    userInfo: getLocalUserInfo(),
    permissions: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission) || state.permissions.includes('*')
    }
  },

  actions: {
    // 登录
    async login(loginForm) {
      try {
        const response = await login(loginForm)
        const { token, userInfo, permissions } = response.data
        
        this.token = token
        this.userInfo = userInfo
        this.permissions = permissions || []
        
        setToken(token)
        setUserInfo(userInfo)
        
        return response
      } catch (error) {
        throw error
      }
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await getUserInfo()
        const { userInfo, permissions } = response.data
        
        this.userInfo = userInfo
        this.permissions = permissions || []
        
        setUserInfo(userInfo)
        
        return response
      } catch (error) {
        throw error
      }
    },

    // 退出登录
    async logout() {
      try {
        if (this.token) {
          await logout()
        }
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
        this.token = null
        this.userInfo = null
        this.permissions = []
        
        removeToken()
        removeUserInfo()
      }
    },

    // 重置状态
    resetState() {
      this.token = null
      this.userInfo = null
      this.permissions = []
      
      removeToken()
      removeUserInfo()
    }
  }
})
