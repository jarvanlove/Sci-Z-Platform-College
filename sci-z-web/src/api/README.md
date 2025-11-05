# API 模块结构说明

## 📁 **目录结构**

```
src/api/
├── index.js              # 统一导出入口
├── Auth/                 # 认证模块
│   ├── index.js         # 模块导出
│   └── auth.js          # 认证相关接口
├── User/                 # 用户模块
│   ├── index.js         # 模块导出
│   └── user.js          # 用户相关接口
├── Declaration/          # 申报模块
│   ├── index.js         # 模块导出
│   └── declaration.js   # 申报相关接口
├── Project/              # 项目模块
│   ├── index.js         # 模块导出
│   └── project.js       # 项目相关接口
├── Report/               # 报告模块
│   ├── index.js         # 模块导出
│   └── report.js        # 报告相关接口
├── Knowledge/            # 知识库模块
│   ├── index.js         # 模块导出
│   └── knowledge.js     # 知识库相关接口
├── AI/                   # AI助手模块
│   ├── index.js         # 模块导出
│   └── ai.js            # AI相关接口
├── System/               # 系统管理模块
│   ├── index.js         # 模块导出
│   └── system.js        # 系统管理相关接口
├── File/                 # 文件管理模块
│   ├── index.js         # 模块导出
│   └── file.js          # 文件管理相关接口
└── Common/               # 通用模块
    ├── index.js         # 模块导出
    ├── types.js         # 类型定义
    ├── constants.js     # 常量定义
    └── adapters.js      # 数据适配器
```

## 🎯 **模块说明**

### **1. Auth - 认证模块**
- **功能**：用户登录、注册、重置密码、验证码等
- **对应页面**：Login, Register, ResetPassword
- **主要接口**：login, register, resetPassword, getCaptcha

### **2. User - 用户模块**
- **功能**：个人信息管理、安全设置等
- **对应页面**：User/Profile, User/Security
- **主要接口**：getUserInfo, updateUserInfo, changePassword

### **3. Declaration - 申报模块**
- **功能**：科研项目申报相关功能
- **对应页面**：Declaration/List, Declaration/Create, Declaration/Detail
- **主要接口**：getDeclarationList, createDeclaration, getDeclarationDetail

### **4. Project - 项目模块**
- **功能**：项目管理、成员管理、进度跟踪等
- **对应页面**：Project/List, Project/Detail, Project/Progress
- **主要接口**：getProjectList, getProjectDetail, addProjectMember

### **5. Report - 报告模块**
- **功能**：报告生成、导出、管理等
- **对应页面**：Report/List, Report/Generate
- **主要接口**：getReportList, generateReport, exportReport

### **6. Knowledge - 知识库模块**
- **功能**：知识库管理、文件操作、问答等
- **对应页面**：Knowledge/List, Knowledge/Detail
- **主要接口**：getKnowledgeList, uploadKnowledgeFile, askKnowledgeQuestion

### **7. AI - AI助手模块**
- **功能**：AI对话、工作流执行等
- **对应页面**：AI/Chat
- **主要接口**：getConversations, sendMessage, executeWorkflow

### **8. System - 系统管理模块**
- **功能**：用户管理、角色权限、系统配置等
- **对应页面**：System/User, System/Role, System/Config, System/Logs
- **主要接口**：getUsers, createRole, getPermissionsTree

### **9. File - 文件管理模块**
- **功能**：文件上传、下载、预览等
- **对应页面**：各模块的文件操作功能
- **主要接口**：uploadFile, downloadFile, previewFile

### **10. Common - 通用模块**
- **功能**：类型定义、常量定义、数据适配器等
- **用途**：为其他模块提供通用工具和类型支持
- **主要文件**：types.js, constants.js, adapters.js

## 🚀 **使用方式**

### **统一导入**
```javascript
import { login, getUserInfo, getProjectList } from '@/api'
```

### **按模块导入**
```javascript
import { login, register } from '@/api/Auth'
import { getUserInfo, updateUserInfo } from '@/api/User'
import { getProjectList, createProject } from '@/api/Project'
```

### **导入通用工具**
```javascript
import { DataAdapter, STATUS, User } from '@/api/Common'
```

## 📋 **设计优势**

1. **模块化**：按功能模块组织，结构清晰
2. **可维护性**：每个模块独立，便于维护和扩展
3. **可复用性**：通用模块可被其他模块复用
4. **类型安全**：完整的类型定义和适配器支持
5. **一致性**：与 views 目录结构保持一致

## 🔄 **扩展指南**

### **添加新模块**
1. 在 `src/api/` 下创建新的模块目录
2. 创建模块的 `index.js` 和具体实现文件
3. 在 `src/api/index.js` 中添加模块导出

### **添加新接口**
1. 在对应模块的实现文件中添加新接口
2. 更新模块的 `index.js` 导出
3. 如需要，更新类型定义和适配器
