import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.js'
import enUS from './en-US.js'
import koKR from './ko-KR.js'
import jaJP from './ja-JP.js'

// 静态导入 Element Plus 语言包，避免动态导入警告
import zhCnLocale from 'element-plus/dist/locale/zh-cn.mjs'
import enLocale from 'element-plus/dist/locale/en.mjs'
import koKrLocale from 'element-plus/dist/locale/ko.mjs'
import jaJpLocale from 'element-plus/dist/locale/ja.mjs'

// Element Plus 语言包映射
const elementPlusLocales = {
  'zh-CN': zhCnLocale,
  'en-US': enLocale,
  'ko-KR': koKrLocale,
  'ja-JP': jaJpLocale
}

// 支持的语言列表
export const supportedLocales = [
  { code: 'zh-CN', name: '中文', flag: '🇨🇳' },
  { code: 'en-US', name: 'English', flag: '🇺🇸' },
  { code: 'ko-KR', name: '한국어', flag: '🇰🇷' },
  { code: 'ja-JP', name: '日本語', flag: '🇯🇵' }
]

// 获取默认语言
const getDefaultLocale = () => {
  const saved = localStorage.getItem('locale')
  if (saved && supportedLocales.find(locale => locale.code === saved)) {
    return saved
  }
  
  // 根据浏览器语言自动选择
  const browserLang = navigator.language || navigator.languages[0]
  const matchedLocale = supportedLocales.find(locale => 
    browserLang.startsWith(locale.code.split('-')[0])
  )
  
  return matchedLocale ? matchedLocale.code : 'zh-CN'
}

// 创建 i18n 实例
const i18n = createI18n({
  legacy: false, // 使用 Composition API
  locale: getDefaultLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
    'ko-KR': koKR,
    'ja-JP': jaJP
  }
})

// 设置语言
export const setLocale = (locale) => {
  if (supportedLocales.find(l => l.code === locale)) {
    i18n.global.locale.value = locale
    localStorage.setItem('locale', locale)
    
    // 设置 HTML lang 属性
    document.documentElement.lang = locale
    
    // 动态设置页面标题
    updatePageTitle(locale)
    
    // 设置 Element Plus 语言（使用静态导入映射）
    const elementPlusLocale = elementPlusLocales[locale]
    if (elementPlusLocale) {
      // 更新 Element Plus 的全局 locale
      // 注意：Element Plus 的 locale 需要通过 ConfigProvider 或在组件中设置
      console.log('Element Plus locale loaded:', locale)
    } else {
      console.warn('Element Plus locale not found for:', locale)
    }
  }
}

// 更新页面标题
export const updatePageTitle = (locale) => {
  const messages = i18n.global.messages.value[locale] || i18n.global.messages.value['zh-CN']
  const title = messages?.app?.htmlTitle || '生成式高校科研管理平台'
  document.title = title
}

// 获取当前语言
export const getCurrentLocale = () => {
  return i18n.global.locale.value
}

// 获取语言显示名称
export const getLocaleName = (code) => {
  const locale = supportedLocales.find(l => l.code === code)
  return locale ? locale.name : code
}

export default i18n
