# 专业日志系统使用指南

## 📋 概述

本项目集成了完整的专业日志系统，提供统一的日志管理、格式化、持久化和查看功能。支持多级别日志、模块化管理、环境控制等特性。

## 🚀 快速开始

### 1. 基础使用

```javascript
// 导入日志系统
import { logger, createLogger } from '@/utils/logger'

// 使用全局日志
logger.info('这是一条信息日志')
logger.error('这是一条错误日志', { error: '详细信息' })

// 创建模块日志器
const authLogger = createLogger('Auth')
authLogger.debug('用户登录', { username: 'admin' })
```

### 2. 开发环境快捷使用

在开发环境中，可以通过 `window` 对象快速访问日志功能：

```javascript
// 全局日志方法
window.$log.info('全局日志')
window.$debug('快捷调试日志')
window.$warn('警告日志')
window.$error('错误日志')

// 查看日志
window.$logViewer.show()

// 创建模块日志器
const moduleLogger = window.$log.create('MyModule')
```

## 📊 日志级别

系统支持以下日志级别（按优先级排序）：

| 级别 | 数值 | 说明 | 使用场景 |
|------|------|------|----------|
| DEBUG | 0 | 调试信息 | 详细的调试信息，仅在开发环境显示 |
| INFO | 1 | 一般信息 | 程序正常运行的关键信息 |
| WARN | 2 | 警告信息 | 潜在问题或异常情况 |
| ERROR | 3 | 错误信息 | 程序错误和异常 |
| OFF | 4 | 关闭日志 | 不输出任何日志 |

### 环境控制

- **开发环境**：默认级别为 `DEBUG`，显示所有日志
- **生产环境**：默认级别为 `INFO`，只显示重要信息

## 🏗️ 模块化日志

### 创建模块日志器

```javascript
import { createLogger } from '@/utils/logger'

// 为不同模块创建专用日志器
const authLogger = createLogger('Auth')
const apiLogger = createLogger('API')
const routerLogger = createLogger('Router')
const storeLogger = createLogger('Store')
```

### 模块日志的优势

- **清晰的日志来源**：每条日志都标明来源模块
- **独立的日志管理**：不同模块的日志可以独立管理
- **更好的调试体验**：可以按模块过滤和查看日志

## 🎨 日志格式化

### 自动格式化

系统会自动为每条日志添加：

- **时间戳**：精确到毫秒的时间信息
- **模块名**：日志来源模块
- **级别标识**：清晰的级别标记
- **颜色区分**：不同级别使用不同颜色

### 示例输出

```
[14:30:25.123][Auth][INFO] 用户登录成功
[14:30:25.124][API][DEBUG] 请求参数: {username: "admin"}
[14:30:25.125][Router][WARN] 权限检查失败: menu:dashboard:view
```

## 💾 日志持久化

### 存储机制

- **开发环境**：自动启用 localStorage 存储
- **生产环境**：默认关闭存储，节省空间
- **存储限制**：最多保存 1000 条日志，自动清理旧日志

### 存储内容

每条日志包含：
- 时间戳
- 模块名
- 日志级别
- 消息内容
- 附加数据（如果有）
- 唯一ID

## 🔍 日志查看器

### 功能特性

- **实时查看**：实时显示最新日志
- **级别过滤**：按日志级别筛选
- **关键词搜索**：搜索日志内容
- **统计信息**：显示各级别日志数量
- **导出功能**：导出日志为 JSON 文件

### 使用方法

```vue
<template>
  <div>
    <!-- 触发按钮 -->
    <el-button @click="showLogViewer">查看日志</el-button>
    
    <!-- 日志查看器 -->
    <LogViewer v-model="showViewer" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { LogViewer } from '@/components/Common'

const showViewer = ref(false)
</script>
```

## 🛠️ 高级功能

### 1. 分组日志

```javascript
logger.group('用户操作流程')
logger.info('步骤1: 用户点击登录按钮')
logger.debug('验证表单数据')
logger.info('步骤2: 发送登录请求')
logger.groupEnd()
```

### 2. 表格日志

```javascript
const tableData = [
  { id: 1, name: '用户1', role: 'admin' },
  { id: 2, name: '用户2', role: 'user' }
]
logger.table('用户列表', tableData)
```

### 3. 性能计时

```javascript
logger.time('数据处理')
// ... 执行一些操作
logger.timeEnd('数据处理')
```

### 4. 日志统计

```javascript
import logManager from '@/utils/logManager'

// 获取日志统计
const stats = logManager.getLogStats()
console.log(stats) // { total: 100, debug: 20, info: 50, warn: 20, error: 10 }

// 清空所有日志
logManager.clearAllLogs()

// 导出日志
logManager.exportAllLogs()
```

## ⚙️ 配置选项

### 全局配置

```javascript
import { LOG_CONFIG } from '@/utils/logger'

// 修改日志级别
LOG_CONFIG.level = LOG_LEVELS.DEBUG

// 启用/禁用控制台输出
LOG_CONFIG.enableConsole = true

// 启用/禁用持久化存储
LOG_CONFIG.enableStorage = true

// 设置最大存储条数
LOG_CONFIG.maxStorageSize = 1000
```

### 运行时配置

```javascript
import logManager from '@/utils/logManager'

// 动态设置日志级别
logManager.setLogLevel('DEBUG')
logManager.setLogLevel(LOG_LEVELS.INFO)

// 获取当前级别
const currentLevel = logManager.getLogLevel()
```

## 📱 移动端适配

日志系统在移动端也能正常工作：

- 控制台输出在移动端浏览器中正常显示
- 日志查看器组件响应式设计，适配移动端
- 触摸操作友好，支持滑动查看

## 🔧 调试技巧

### 1. 条件日志

```javascript
// 只在特定条件下记录日志
if (import.meta.env.DEV) {
  logger.debug('调试信息', { data: sensitiveData })
}
```

### 2. 性能敏感场景

```javascript
// 使用轻量级日志
logger.info('简单信息') // 推荐
logger.debug('复杂对象', largeObject) // 避免在循环中使用
```

### 3. 错误处理

```javascript
try {
  // 业务逻辑
} catch (error) {
  logger.error('操作失败', { 
    error: error.message, 
    stack: error.stack,
    context: { userId, action }
  })
}
```

## 📈 最佳实践

### 1. 日志级别选择

- **DEBUG**：详细的调试信息，变量值，执行流程
- **INFO**：重要的业务事件，用户操作，系统状态
- **WARN**：潜在问题，降级处理，非致命错误
- **ERROR**：系统错误，异常情况，需要关注的问题

### 2. 日志内容规范

```javascript
// ✅ 好的日志
logger.info('用户登录成功', { username: 'admin', loginTime: new Date() })
logger.error('数据库连接失败', { error: error.message, retryCount: 3 })

// ❌ 避免的日志
logger.info('登录') // 信息不够详细
logger.error('错误', error) // 直接输出错误对象
```

### 3. 模块划分

- 按功能模块创建日志器
- 保持模块名称简洁明了
- 避免在工具函数中创建过多日志器

## 🚨 注意事项

1. **性能考虑**：避免在高频操作中记录过多日志
2. **敏感信息**：不要在日志中记录密码、token等敏感信息
3. **日志大小**：控制单条日志的大小，避免内存溢出
4. **生产环境**：生产环境建议使用 INFO 或更高级别

## 🔗 相关文件

- `src/utils/logger.js` - 核心日志系统
- `src/utils/logManager.js` - 日志管理工具
- `src/components/Common/LogViewer.vue` - 日志查看器组件
- `src/views/LogDemo.vue` - 日志系统演示页面

## 📞 技术支持

如有问题或建议，请查看：
1. 日志查看器中的实时日志
2. 浏览器控制台的错误信息
3. 项目文档和代码注释
