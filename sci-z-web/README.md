# 高校科研项目管理平台

> 基于 Vue.js 3 + Dify AI 的智能化科研项目全生命周期管理系统

## 📋 项目概述

高校科研项目管理平台是一个基于 **Vue.js 3**、**前后端分离**、**现代化前端架构**的智能化科研项目全生命周期管理系统。平台以 Dify 工作流为核心，实现科研项目从申报、执行到验收的智能化、自动化管理。

### 🎯 核心特性

- 🎯 **现代化前端架构**：Vue 3 + Vite + Element Plus，高性能开发体验
- 🚀 **组件化开发**：高度可复用的组件库，提升开发效率
- 🤖 **AI 智能化**：集成 Dify 平台，实现智能工作流编排
- 📊 **全生命周期管理**：覆盖申报、执行、报告全流程
- 🔒 **安全可靠**：基于 JWT 的前端认证体系
- ⚡ **Vue 3 新特性**：Composition API、响应式系统、Teleport 等

## 🛠 前端技术栈

### 核心技术

- **Vue.js 3.x** - 渐进式 JavaScript 框架
- **Vue Router 4.x** - 官方路由管理器
- **Pinia** - 新一代状态管理
- **Element Plus 2.3.14** - Vue 3 企业级组件库
- **Axios** - HTTP 客户端
- **Vite 5.x** - 新一代前端构建工具
- **SCSS** - CSS 预处理器
- **ESLint + Prettier** - 代码质量工具

### AI 平台

- **Dify** - AI 工作流编排与知识库管理
- **Vector Database** - 向量数据库（语义搜索）

### 架构模式

- **组件化架构** - 高度可复用的组件设计
- **模块化路由** - 按功能模块组织路由
- **状态管理** - 集中式状态管理
- **API 抽象** - 统一的 API 调用层

## 📁 项目结构

```
sci-z-web/
├── public/                     # 静态资源
│   ├── favicon.ico
│   └── index.html
├── src/
│   ├── api/                    # API 接口
│   │   ├── auth.js            # 认证相关
│   │   ├── user.js            # 用户中心相关
│   │   ├── declaration.js      # 申报相关
│   │   ├── project.js         # 项目相关
│   │   ├── report.js          # 报告管理相关
│   │   ├── file.js            # 文件管理相关
│   │   ├── knowledge.js       # 知识库相关
│   │   ├── ai.js              # AI助手相关
│   │   ├── system.js          # 系统管理相关
│   │   └── index.js           # API 统一导出
│   ├── assets/                # 静态资源
│   │   ├── images/            # 图片
│   │   ├── styles/            # 全局样式
│   │   └── icons/             # 图标
│   ├── components/            # 公共组件
│   │   ├── Layout/            # 布局组件
│   │   ├── FileUpload/        # 文件上传组件
│   │   ├── FilePreview/       # 文件预览组件
│   │   ├── SearchBox/        # 搜索框组件
│   │   ├── StatusTag/         # 状态标签组件
│   │   └── Common/            # 通用组件
│   ├── composables/           # 组合式函数
│   │   ├── useAuth.js         # 认证相关
│   │   ├── useUpload.js       # 上传相关
│   │   ├── useSearch.js       # 搜索相关
│   │   └── useTable.js        # 表格相关
│   ├── directives/             # 自定义指令
│   │   ├── permission.js      # 权限指令
│   │   └── loading.js         # 加载指令
│   ├── router/                # 路由配置
│   │   ├── index.js           # 路由主文件
│   │   ├── modules/           # 路由模块
│   │   └── guards.js          # 路由守卫
│   ├── store/                 # 状态管理（Pinia）
│   │   ├── index.js           # Store 入口
│   │   ├── modules/           # Store 模块
│   │   └── types.js           # 类型定义
│   ├── utils/                 # 工具函数
│   │   ├── request.js         # Axios 封装
│   │   ├── auth.js            # 认证工具
│   │   ├── validate.js        # 表单验证
│   │   ├── date.js            # 日期处理
│   │   ├── file.js            # 文件处理
│   │   └── constants.js       # 常量定义
│   ├── views/                 # 页面视图
│   │   ├── Login/             # 登录页
│   │   ├── Register/          # 注册页
│   │   ├── Dashboard/         # 仪表板
│   │   ├── User/              # 用户中心
│   │   ├── Declaration/       # 申报模块
│   │   ├── Project/           # 项目模块
│   │   ├── Report/            # 报告管理模块
│   │   ├── AI/                # AI助手模块
│   │   ├── Knowledge/         # 知识库管理模块
│   │   ├── System/            # 系统管理模块
│   │   └── Error/             # 错误页
│   ├── App.vue                # 根组件
│   └── main.js                # 入口文件
├── .env.development           # 开发环境配置
├── .env.production            # 生产环境配置
├── .eslintrc.js               # ESLint 配置
├── .prettierrc.js             # Prettier 配置
├── vite.config.js             # Vite 配置
└── package.json               # 项目依赖
```

## 🚀 功能模块

本系统共包含 **9 大核心功能模块**，覆盖科研项目全生命周期管理。

### 1. 认证模块

- **用户登录**：支持用户名/邮箱登录，可选记住登录状态
- **用户注册**：新用户注册，支持邮箱/手机验证
- **密码重置**：通过邮箱/手机验证码重置密码
- **验证码**：图形验证码防止暴力破解

### 2. 仪表板模块

- **统计卡片**：项目总数、进行中、待验收、已完成
- **项目进度概览**：可视化展示所有项目的进度
- **最近申报列表**：最新提交的申报记录
- **快捷操作**：快速跳转到各功能模块

### 3. 申报模块

- **申报列表**：查看所有申报记录，支持搜索、筛选、分页
- **新建申报**：填写申报信息（研究方向、领域、课题等）
- **申报详情**：查看申报详细信息和工作流执行结果
- **工作流触发**：提交后触发 Dify 申报工作流

### 4. 项目模块

- **项目列表**：查看所有项目，支持搜索、筛选、分页
- **项目详情**：查看项目基本信息、成员、进度、文档
- **成员管理**：添加/移除项目成员，分配成员角色
- **进度管理**：记录项目进度，设置里程碑
- **文档管理**：上传项目文档，自动同步到 Dify 知识库

### 5. 报告管理模块

- **报告列表**：查看所有报告，支持搜索、筛选、分页
- **创建报告**：选择项目和报告类型，配置生成参数
- **生成报告**：触发 Dify 工作流，实时显示生成进度
- **报告预览**：在线预览生成的 HTML 报告内容
- **报告导出**：导出为 PDF、Word、Markdown 等格式

### 6. 文件管理模块

- **文件上传**：单文件/批量文件上传
- **文件列表**：查看文件列表，支持筛选、搜索
- **文件预览**：在线预览 PDF、图片等文件
- **文件下载**：下载文件到本地
- **文件去重**：通过 MD5 哈希值避免重复存储

### 7. 知识库模块

- **知识库创建**：创建独立知识库或关联项目知识库
- **文件夹管理**：创建多级文件夹，支持重命名、删除
- **文件管理**：上传、重命名、删除、移动文件
- **AI 问答**：基于知识库内容进行实时问答（调用 Dify）

### 8. AI 助手模块

- **创建会话**：创建新的对话会话
- **发送消息**：发送用户消息，接收 AI 回复
- **会话列表**：查看所有对话会话
- **会话历史**：查看对话的完整消息历史
- **流式输出**：支持 AI 回复的流式输出

### 9. 用户中心模块

- **个人信息**：查看和编辑个人基本信息
- **头像上传**：上传和更换用户头像
- **密码修改**：修改登录密码
- **登录日志**：查看个人登录历史记录
- **安全设置**：密码强度、登录通知等安全配置

### 10. 系统管理模块

- **用户管理**：用户列表查询、新建用户、编辑用户信息
- **角色权限管理**：角色列表查询、新建角色、配置角色权限
- **系统配置**：行业配置、基本配置、Dify 配置、邮件配置
- **日志管理**：操作日志查询、登录日志查询、日志导出

## 🚀 启动部署

### 环境要求

- **Node.js 18+** (LTS, 推荐 Node.js 20)
- **npm 9+** 或 **yarn 1.22+** 或 **pnpm 8+**

### 开发环境启动

```bash
# 克隆项目
git clone <repository-url>
cd sci-z-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

### 生产环境部署

#### 1. 构建项目

```bash
# 生产环境构建
npm run build
```

#### 2. Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/sci-z-web;
    index index.html;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://backend-server;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 3. 环境变量配置

**开发环境 (.env.development)**

```env
# API 基础地址
VITE_API_BASE_URL=http://localhost:8080/api

# Dify 配置
VITE_DIFY_API_URL=https://api.dify.ai/v1
VITE_DIFY_API_KEY=your-dify-api-key

# 应用配置
VITE_APP_TITLE=高校科研项目管理平台
VITE_APP_VERSION=1.0.0
```

**生产环境 (.env.production)**

```env
# API 基础地址
VITE_API_BASE_URL=https://your-api-domain.com/api

# Dify 配置
VITE_DIFY_API_URL=https://api.dify.ai/v1
VITE_DIFY_API_KEY=your-production-dify-api-key

# 应用配置
VITE_APP_TITLE=高校科研项目管理平台
VITE_APP_VERSION=1.0.0
```

### 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` 修复问题
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建过程或辅助工具的变动

### 性能优化

- **代码分割**：路由懒加载，组件懒加载
- **静态资源优化**：CDN 加速，资源压缩
- **缓存策略**：浏览器缓存，HTTP 缓存
- **构建优化**：Tree-shaking，代码压缩

## 📄 许可证

MIT License

## 📞 联系方式

如有问题，请联系开发团队。
