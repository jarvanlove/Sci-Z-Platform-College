import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.js'
import enUS from './en-US.js'
import koKR from './ko-KR.js'
import jaJP from './ja-JP.js'

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
    
    // 设置 Element Plus 语言
    import(`element-plus/dist/locale/${locale}.mjs`).then(module => {
      // 这里需要根据实际的 Element Plus 语言包路径调整
      console.log('Element Plus locale loaded:', locale)
    }).catch(err => {
      console.warn('Failed to load Element Plus locale:', err)
    })
  }
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
