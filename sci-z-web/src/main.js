import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import './assets/styles/common.scss'
import i18n, { updatePageTitle, getCurrentLocale } from './locales'
import permission from './directives/permission' // 导入权限指令

// 导入简单日志系统
import { logger, createLogger } from './utils/simpleLogger'

// 创建应用启动日志器
const appLogger = createLogger('App')

// 应用启动日志
appLogger.info('🚀 应用开始启动')

const app = createApp(App)
appLogger.debug('Vue应用实例创建完成')

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
appLogger.debug('Element Plus图标注册完成')

// 注册权限指令
app.directive('permission', permission.permission)
app.directive('role', permission.role)
appLogger.debug('权限指令注册完成')

// 注册插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(i18n)
appLogger.debug('所有插件注册完成')

// 设置初始页面标题
updatePageTitle(getCurrentLocale())
appLogger.debug('页面标题设置完成')

// 全局错误处理
window.addEventListener('error', (event) => {
  appLogger.error('JavaScript错误', event.message)
})

window.addEventListener('unhandledrejection', (event) => {
  appLogger.error('Promise错误', event.reason)
})

// Vue全局错误处理
app.config.errorHandler = (err, instance, info) => {
  appLogger.error('Vue组件错误', err.message)
}

// 挂载应用
appLogger.info('开始挂载应用')
try {
  app.mount('#app')
  appLogger.info('✅ 应用挂载成功')
} catch (error) {
  appLogger.error('❌ 应用挂载失败', error.message)
  throw error
}
