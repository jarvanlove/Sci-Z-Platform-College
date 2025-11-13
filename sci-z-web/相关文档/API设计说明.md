# API 设计说明

## 🎯 **设计理念**

### **问题分析**
原来的设计存在以下问题：
1. **过度耦合**：前端类型定义与后端实现强耦合
2. **维护困难**：后端接口变化时，前端需要同步修改
3. **开发效率低**：前端开发被后端接口进度阻塞

### **解决方案**
采用 **轻量级类型 + 适配器模式** 的设计：

## 📋 **新的设计架构**

### **1. 轻量级类型定义**
```javascript
// 只定义稳定的核心字段
export const User = {
  id: Number,
  username: String,
  email: String,
  realName: String,
  avatar: String
  // 其他字段通过 data 动态传递
}
```

### **2. 接口适配器**
```javascript
// 处理前后端数据结构差异
export class DataAdapter {
  static normalizeUser(userData) {
    return {
      id: userData.id,
      username: userData.username || userData.userName,
      email: userData.email || userData.emailAddress,
      // 自动适配不同的字段名
      ...userData
    }
  }
}
```

## 🚀 **使用方式**

### **1. 基础 API 调用**
```javascript
import { getDeclarationList, DataAdapter } from '@/api'

// 调用 API
const response = await getDeclarationList({
  page: 1,
  size: 10,
  keyword: '搜索关键词'
})

// 自动适配数据格式
const declarations = response.data.records.map(item => 
  DataAdapter.normalizeDeclaration(item)
)
```

### **2. 动态字段处理**
```javascript
// 后端返回的字段名可能不同
const backendData = {
  id: 1,
  title: '项目标题',
  create_time: '2024-01-01',  // 后端使用下划线
  applicant_name: '申请人'     // 后端使用下划线
}

// 适配器自动处理
const normalized = DataAdapter.normalizeDeclaration(backendData)
// 结果：{ id: 1, title: '项目标题', createdAt: '2024-01-01', applicantName: '申请人' }
```

### **3. 类型验证（可选）**
```javascript
import { validateType, User } from '@/api'

const userData = { id: 1, username: 'test' }
const validation = validateType(userData, User)

if (!validation.isValid) {
  console.log('类型验证失败:', validation.errors)
}
```

## 🔧 **适配器功能**

### **1. 响应数据适配**
- 统一响应格式：`{code, message, data}`
- 自动处理不同的响应结构
- 标准化分页数据格式

### **2. 业务数据适配**
- 字段名映射：`userName` → `username`
- 时间格式统一：`create_time` → `createdAt`
- 保留原始字段，支持扩展

### **3. 请求参数适配**
- 分页参数标准化
- 时间范围参数处理
- 搜索关键词统一

### **4. 错误处理适配**
- 统一错误格式
- HTTP 状态码映射
- 业务错误处理

## 📈 **优势**

### **1. 解耦合**
- 前端不依赖后端具体实现
- 后端可以自由调整接口结构
- 前后端可以并行开发

### **2. 易维护**
- 接口变化时只需修改适配器
- 类型定义保持稳定
- 减少代码修改量

### **3. 高扩展性**
- 支持动态字段
- 自动处理字段名差异
- 保留原始数据

### **4. 开发效率**
- 前端可以基于 Mock 数据开发
- 后端接口变化影响最小
- 类型提示和验证支持

## 🎯 **最佳实践**

### **1. 类型定义原则**
- 只定义稳定的核心字段
- 使用通用查询参数类型
- 避免过度详细的类型定义

### **2. 适配器使用原则**
- 在 API 调用后立即使用适配器
- 保持适配器逻辑简单
- 优先使用标准化的数据格式

### **3. 错误处理原则**
- 统一使用错误适配器
- 提供友好的错误信息
- 支持错误重试机制

## 🔄 **迁移指南**

### **从旧版本迁移**
1. 保留现有的 API 调用代码
2. 在数据处理时添加适配器调用
3. 逐步移除详细的类型定义
4. 使用轻量级类型定义

### **示例迁移**
```javascript
// 旧版本
const declarations = response.data.records

// 新版本
const declarations = response.data.records.map(item => 
  DataAdapter.normalizeDeclaration(item)
)
```

这种设计既保持了类型安全，又提供了足够的灵活性来处理后端接口的变化。
