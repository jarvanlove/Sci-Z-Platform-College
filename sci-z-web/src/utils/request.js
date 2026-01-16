import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { createLogger } from './simpleLogger'

// 创建请求模块日志器
const requestLogger = createLogger('Request')
// 请求超时时间(5分钟)
const REQUEST_TIMEOUT = 300000

// 创建axios实例
// baseURL 设置为 /api，所有 API 路径定义中不再包含 /api 前缀
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: REQUEST_TIMEOUT
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const skipAuth = config.skipAuth === true
    if (skipAuth) {
      Reflect.deleteProperty(config, 'skipAuth')
      return config
    }
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    requestLogger.error('请求错误', { error: error.message, url: error.config?.url })
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const { data } = response
    
    // 如果是文件下载等特殊响应，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 统一处理业务错误
    if (data.code && data.code !== 200) {
      const error = new Error(data.message || '请求失败')
      // 🔥 标记：表示错误信息已经在拦截器中显示，组件中不需要再次显示
      error._messageShown = true
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(error)
    }
    
    return data
  },
  (error) => {
    const isNetworkError = !error.response && (
      error.code === 'ECONNREFUSED' || 
      error.code === 'ECONNABORTED' || 
      error.message?.includes('timeout') ||
      error.message?.includes('Network Error')
    )
    
    // 对于网络连接错误（如后端服务未启动），在开发环境下静默处理
    // 避免频繁弹出错误提示干扰开发体验
    if (isNetworkError) {
      const authStore = useAuthStore()
      const now = Date.now()
      
      // 记录网络错误时间，用于路由守卫判断
      if (!authStore.lastNetworkError || (now - authStore.lastNetworkError) > 5 * 60 * 1000) {
        authStore.lastNetworkError = now
      }
      
      // 仅记录日志，不弹出错误提示（开发环境）
      const isDev = import.meta.env.DEV
      if (isDev) {
        requestLogger.warn('网络连接错误（后端服务可能未启动）', { 
          url: error.config?.url,
          code: error.code,
          message: error.message 
        })
        // 开发环境下静默处理，不弹出错误提示
      } else {
        // 生产环境仍然提示
        error._messageShown = true // 标记已显示
        if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
          ElMessage.error('请求超时，请稍后重试或联系管理员')
        } else {
          ElMessage.error('网络错误，请检查网络连接')
        }
      }
      
      return Promise.reject(error)
    }
    
    // 对于有响应的错误，正常处理
    requestLogger.error('响应错误', { 
      status: error.response?.status, 
      url: error.config?.url,
      message: error.message 
    })
    
    if (error.response) {
      const { status, data } = error.response
      
      // 🔥 如果错误信息已经在响应拦截器中显示过（业务错误），不再重复显示
      if (error._messageShown) {
        return Promise.reject(error)
      }
      
      // 🔥 优先使用后端返回的错误信息（如果有的话），其次才使用状态码对应的默认信息
      let errorMessage = null
      
      // 检查响应体中是否有错误信息（后端返回的 Result 格式：{ code, message, data }）
      if (data && (data.message || data.error)) {
        errorMessage = data.message || data.error
      }
      
      switch (status) {
        case 401:
          const authStore = useAuthStore()
          // 🔥 修复：如果正在退出登录，不显示错误消息（避免退出时出现多个错误提示）
          if (authStore.isLoggingOut) {
            // 正在退出登录，静默处理 401 错误
            return Promise.reject(error)
          }
          
          // 🔥 修复：对于公开页面（如 /ai/chat），如果用户未登录，401 错误应该静默处理
          const currentPath = typeof window !== 'undefined' ? window.location.pathname : ''
          const publicPages = ['/ai/chat', '/literature/search', '/login', '/register', '/reset-password']
          const isPublicPage = publicPages.includes(currentPath)
          
          if (isPublicPage && !authStore.isLoggedIn) {
            // 在公开页面且用户未登录时，401 错误是正常的（可能是退出登录后的残留请求），静默处理
            error._messageShown = true // 标记已显示（虽然不显示，但标记避免重复处理）
            return Promise.reject(error)
          }
          
          // 避免重复调用 logout
          error._messageShown = true // 标记已显示
          ElMessage.error(errorMessage || '登录已过期，请重新登录')
          authStore.isLoggingOut = true
          authStore.logout().finally(() => {
            authStore.isLoggingOut = false
          })
          break
        case 403:
          error._messageShown = true // 标记已显示
          ElMessage.error(errorMessage || '没有权限访问该资源')
          break
        case 404:
          error._messageShown = true // 标记已显示
          ElMessage.error(errorMessage || '请求的资源不存在')
          break
        case 500:
          error._messageShown = true // 标记已显示
          // 🔥 优先显示后端返回的错误信息（如"用户名不存在"），如果没有才显示默认信息
          ElMessage.error(errorMessage || '服务器内部错误')
          break
        default:
          error._messageShown = true // 标记已显示
          // 🔥 优先显示后端返回的错误信息
          ElMessage.error(errorMessage || '请求失败')
      }
    } else {
      // 🔥 如果错误信息已经显示过，不再重复显示
      if (error._messageShown) {
        return Promise.reject(error)
      }
      
      if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        error._messageShown = true // 标记已显示
        ElMessage.error('请求超时，请稍后重试或联系管理员')
      } else if (!isNetworkError) {
        // 网络错误已经在上面处理过了，这里只处理其他未标记的错误
        error._messageShown = true // 标记已显示
        ElMessage.error('网络错误，请检查网络连接')
      }
    }
    
    return Promise.reject(error)
  }
)

export default service
