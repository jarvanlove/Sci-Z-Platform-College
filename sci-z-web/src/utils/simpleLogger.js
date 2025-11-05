/**
 * 简单日志系统
 * 只在开发环境显示日志，生产环境静默
 */

// 日志级别
const LOG_LEVELS = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3
}

// 颜色映射
const COLORS = {
  [LOG_LEVELS.DEBUG]: '#6b7280',
  [LOG_LEVELS.INFO]: '#2563eb', 
  [LOG_LEVELS.WARN]: '#f59e0b',
  [LOG_LEVELS.ERROR]: '#dc2626'
}

// 简单日志类
class SimpleLogger {
  constructor(module = 'App') {
    this.module = module
  }

  _log(level, message, data = null) {
    // 只在开发环境显示
    if (!import.meta.env.DEV) return

    const timestamp = new Date().toLocaleTimeString()
    const levelName = Object.keys(LOG_LEVELS)[level]
    const color = COLORS[level]
    
    const prefix = `[${timestamp}][${this.module}][${levelName}]`
    
    if (data) {
      console.log(`%c${prefix} ${message}`, `color: ${color}; font-weight: bold;`, data)
    } else {
      console.log(`%c${prefix} ${message}`, `color: ${color}; font-weight: bold;`)
    }
  }

  debug(message, data = null) {
    this._log(LOG_LEVELS.DEBUG, message, data)
  }

  info(message, data = null) {
    this._log(LOG_LEVELS.INFO, message, data)
  }

  warn(message, data = null) {
    this._log(LOG_LEVELS.WARN, message, data)
  }

  error(message, data = null) {
    this._log(LOG_LEVELS.ERROR, message, data)
  }
}

// 创建默认日志实例
const logger = new SimpleLogger('App')

// 创建模块日志器
const createLogger = (module) => new SimpleLogger(module)

export { logger, createLogger }
