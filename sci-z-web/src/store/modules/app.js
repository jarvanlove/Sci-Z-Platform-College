import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    // 侧边栏状态
    sidebarCollapsed: false,
    // 主题模式
    theme: 'light',
    // 语言
    language: 'zh-CN',
    // 页面加载状态
    loading: false,
    // 页面标题
    pageTitle: '高校科研项目管理平台'
  }),

  getters: {
    isDark: (state) => state.theme === 'dark'
  },

  actions: {
    // 切换侧边栏
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    // 设置侧边栏状态
    setSidebarCollapsed(collapsed) {
      this.sidebarCollapsed = collapsed
    },

    // 切换主题
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },

    // 设置主题
    setTheme(theme) {
      this.theme = theme
    },

    // 设置语言
    setLanguage(language) {
      this.language = language
    },

    // 设置加载状态
    setLoading(loading) {
      this.loading = loading
    },

    // 设置页面标题
    setPageTitle(title) {
      this.pageTitle = title
    }
  }
})
