import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { createLogger } from './simpleLogger'

// 创建请求模块日志器
const requestLogger = createLogger('Request')
// 请求超时时间(5分钟)
const REQUEST_TIMEOUT = 300000

// 创建axios实例
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
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
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
      
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const authStore = useAuthStore()
          authStore.logout()
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        ElMessage.error('请求超时，请稍后重试或联系管理员')
      } else {
        ElMessage.error('网络错误，请检查网络连接')
      }
    }
    
    return Promise.reject(error)
  }
)

export default service
