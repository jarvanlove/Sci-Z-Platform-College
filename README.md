# Sci-Z Platform College

## 📋 项目简介

高校科研项目管理平台 - 专为高校科研项目管理设计的综合性平台系统，集成了 AI 知识库管理、文档上传、项目管理等核心功能。

## 🏗️ 项目结构

```
Sci-Z-Platform-College/
├── AgentBackendServices/           # Java Spring Boot 后端服务
│   ├── src/main/java/              # Java 源代码
│   ├── src/main/resources/         # 配置文件
│   └── target/                     # 编译输出
├── sci-z-server/                   # 后端服务（原项目）
├── sci-z-web/                      # 前端项目（原项目）
│   ├── 原型图/                     # UI原型设计
│   ├── 开发计划.md                 # 开发计划
│   └── 高校科研项目管理平台需求文档.md
├── TestAIDataModel/                # AI 数据模型测试项目
│   └── ApiDify/AIDataTest/         # Dify API 集成测试
│       ├── src/                    # Vue 3 + TypeScript 源码
│       ├── public/                 # 静态资源
│       └── dist/                   # 构建输出
├── university-ai-projects/         # 大学 AI 项目（子模块）
├── 流程图/                         # 架构设计图
├── testApi.py                      # Python API 测试脚本
├── dify.txt                        # Dify 配置信息
└── 工业互联网使用说明.docx         # 项目文档
```

## 🚀 核心功能

### 🎯 主要功能模块

- 🔐 **认证模块**：登录、注册、密码重置
- 📊 **仪表板**：数据可视化展示
- 👤 **用户中心**：个人信息管理
- 📝 **申报模块**：科研项目申报
- 📁 **项目模块**：项目管理与跟踪
- ✅ **验收模块**：项目验收与报告
- 🤖 **AI 助手**：智能问答与知识库
- ⚙️ **系统管理**：用户权限与系统配置

### 🤖 AI 知识库功能（新增）

- 📚 **知识库管理**：创建、编辑、删除知识库
- 📤 **文档上传**：支持多种格式文档上传
- 🔍 **智能检索**：混合检索、语义检索、全文检索
- 🎯 **精准匹配**：Rerank 重排序优化
- 📊 **数据分析**：文档统计与使用分析

## 🛠️ 技术栈

### 后端技术

- **Java Spring Boot** - 主后端框架
- **MySQL** - 关系型数据库
- **Redis** - 缓存与会话管理
- **Sa-Token** - 权限认证框架
- **MyBatis** - ORM 框架

### 前端技术

- **Vue.js 3** - 前端框架
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **Element UI** - UI 组件库
- **Axios** - HTTP 客户端

### AI 集成

- **Dify API** - AI 知识库服务
- **文件上传** - 支持 PDF、Word、TXT 等格式
- **智能检索** - 多模式检索算法
- **知识库管理** - 完整的 CRUD 操作

## 📦 子项目说明

### 1. AgentBackendServices

Java Spring Boot 后端服务，提供核心业务逻辑和 API 接口。

### 2. TestAIDataModel/ApiDify/AIDataTest

Vue 3 + TypeScript 前端项目，专门用于测试和演示 Dify AI 知识库功能。

**主要功能：**

- ✅ 知识库创建与管理
- ✅ 文档上传与处理
- ✅ 智能检索与问答
- ✅ 用户界面优化

**快速启动：**

```bash
cd TestAIDataModel/ApiDify/AIDataTest
npm install
npm run dev
```

### 3. sci-z-web

原项目前端，包含完整的科研项目管理界面原型。

## 🔧 开发环境

### 后端开发

```bash
# 启动 Java 后端服务
cd AgentBackendServices
mvn spring-boot:run
```

### 前端开发

```bash
# 启动 Vue 前端服务
cd TestAIDataModel/ApiDify/AIDataTest
npm install
npm run dev
```

### AI 功能测试

```bash
# Python API 测试
python testApi.py
```

## 📚 API 文档

### Dify API 集成

- **知识库管理**：`POST /v1/datasets` - 创建知识库
- **文档上传**：`POST /v1/datasets/{id}/document/create-by-file` - 上传文档
- **智能检索**：`POST /v1/datasets/{id}/retrieve` - 检索知识库

### 配置信息

- **API 地址**：`http://192.168.1.203/v1`
- **API Key**：`dataset-XfEGbHDJ9C3koNrbyN0sYcrl`
- **知识库 ID**：`6866e4ef-91ba-492c-a91b-5a76dabea0f9`

## 🎨 界面预览

### 知识库管理界面

- 📊 数据集列表展示
- ➕ 创建新知识库
- 📤 文档上传功能
- 🔍 智能检索界面

### 项目管理界面

- 📋 项目列表管理
- 📝 申报流程跟踪
- 📊 数据可视化展示
- 👥 用户权限管理

## 🚀 部署说明

### 开发环境

1. 克隆项目：`git clone <repository-url>`
2. 安装依赖：`npm install / mvn install`
3. 配置数据库：修改 `application.yml`
4. 启动服务：`npm run dev / mvn spring-boot:run`

### 生产环境

1. 构建前端：`npm run build`
2. 打包后端：`mvn package`
3. 部署到服务器
4. 配置 Nginx 反向代理

## 📝 更新日志

### v1.2.0 (最新)

- ✅ 新增 AI 知识库管理功能
- ✅ 集成 Dify API 服务
- ✅ 支持文档上传与智能检索
- ✅ 优化用户界面和交互体验
- ✅ 完善 TypeScript 类型支持

### v1.1.0

- ✅ 完善认证模块
- ✅ 优化项目管理功能
- ✅ 增强系统安全性

### v1.0.0

- ✅ 基础平台架构
- ✅ 核心功能模块
- ✅ 用户界面设计

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 LICENSE 文件了解详情。

## 👥 开发者

- **@jarvanlove** - 项目负责人
- **AI Assistant** - 功能开发与优化

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 📧 **邮箱**：jarvanlove@example.com
- 🐛 **问题反馈**：GitHub Issues
- 📖 **文档**：项目文档
