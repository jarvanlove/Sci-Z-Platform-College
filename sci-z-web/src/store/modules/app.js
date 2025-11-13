import { defineStore } from 'pinia'

const THEME_STORAGE_KEY = 'app_theme'

// 应用主题到 DOM
const applyTheme = (theme) => {
  const html = document.documentElement
  if (theme === 'dark') {
    html.classList.add('dark')
    html.setAttribute('data-theme', 'dark')
  } else {
    html.classList.remove('dark')
    html.setAttribute('data-theme', 'light')
  }
}

// 从 localStorage 读取主题
const getStoredTheme = () => {
  try {
    const stored = localStorage.getItem(THEME_STORAGE_KEY)
    if (stored === 'dark' || stored === 'light') {
      return stored
    }
  } catch (error) {
    console.warn('读取主题失败:', error)
  }
  // 默认使用系统偏好
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark'
  }
  return 'light'
}

// 初始化主题
const initTheme = getStoredTheme()
applyTheme(initTheme)

export const useAppStore = defineStore('app', {
  state: () => ({
    // 侧边栏状态
    sidebarCollapsed: false,
    // 主题模式（从 localStorage 或系统偏好读取）
    theme: initTheme,
    // 语言
    language: 'zh-CN',
    // 页面加载状态
    loading: false,
    // 页面标题
    pageTitle: '生成式高校科研管理平台'
  }),

  getters: {
    // 是否暗色主题
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
      const newTheme = this.theme === 'light' ? 'dark' : 'light'
      this.setTheme(newTheme)
    },

    // 设置主题
    setTheme(theme) {
      if (theme !== 'light' && theme !== 'dark') {
        console.warn('无效的主题值:', theme)
        return
      }
      this.theme = theme
      // 应用到 DOM
      applyTheme(theme)
      // 持久化到 localStorage
      try {
        localStorage.setItem(THEME_STORAGE_KEY, theme)
      } catch (error) {
        console.warn('保存主题失败:', error)
      }
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
