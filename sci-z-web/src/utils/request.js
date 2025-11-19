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
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    
    return data
  },
  (error) => {
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
          // 避免重复调用 logout，如果已经在跳转中则跳过
          if (!authStore.isLoggingOut) {
            authStore.isLoggingOut = true
            authStore.logout().finally(() => {
              authStore.isLoggingOut = false
            })
          }
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
