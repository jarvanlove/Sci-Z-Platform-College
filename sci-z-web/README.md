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

本项目采用 **分层架构 + 组件化设计** 的现代化前端架构模式，实现了高度的模块化、可复用性和可维护性。

#### 🏗️ 分层架构设计

```
┌─────────────────────────────────────────┐
│                Views 层                 │  ← 页面布局层
│  (页面结构、路由导航、组件组合)          │
├─────────────────────────────────────────┤
│              Business 层                │  ← 业务逻辑层
│  (业务组件、数据处理、用户交互)          │
├─────────────────────────────────────────┤
│               Common 层                 │  ← 通用组件层
│  (基础组件、布局组件、工具组件)          │
├─────────────────────────────────────────┤
│                API 层                   │  ← 数据接口层
│  (接口封装、数据适配、错误处理)          │
├─────────────────────────────────────────┤
│               Store 层                   │  ← 状态管理层
│  (全局状态、模块状态、持久化)            │
└─────────────────────────────────────────┘
```

#### 📋 各层职责详解

##### **1. Views 层 - 页面布局层**
- **职责**：页面结构、路由导航、组件组合
- **特点**：不包含业务逻辑，专注于页面布局和导航
- **示例**：`src/views/Login/index.vue` - 登录页面布局

```vue
<!-- Views 层示例 -->
<template>
  <div class="login-page">
    <!-- 页面布局 -->
    <div class="login-container">
      <!-- 组合 Business 组件 -->
      <LoginForm @login-success="handleLoginSuccess" />
    </div>
  </div>
</template>

<script setup>
// 页面级逻辑：路由导航
const handleLoginSuccess = () => {
  router.push('/dashboard')  // ✅ Views 层处理路由
}
</script>
```

##### **2. Business 层 - 业务逻辑层**
- **职责**：业务逻辑、数据处理、用户交互、API调用
- **特点**：高度可复用，可在多个页面中使用
- **示例**：`src/components/Business/Auth/LoginForm.vue` - 登录表单组件

```vue
<!-- Business 层示例 -->
<template>
  <el-form @submit.prevent="handleLogin">
    <!-- 业务UI -->
    <el-form-item>
      <el-input v-model="form.username" />
    </el-form-item>
    <el-button @click="handleLogin">登录</el-button>
  </el-form>
</template>

<script setup>
// 业务逻辑：API调用、数据处理
const handleLogin = async () => {
  try {
    const response = await login(form)  // ✅ Business 层处理API
    ElMessage.success('登录成功')        // ✅ Business 层处理提示
    emit('login-success', response)     // ✅ 向 Views 层发送事件
  } catch (error) {
    ElMessage.error('登录失败')          // ✅ Business 层处理错误
  }
}
</script>
```

##### **3. Common 层 - 通用组件层**
- **职责**：基础组件、布局组件、工具组件
- **特点**：纯UI组件，无业务逻辑，高度可复用
- **示例**：`src/components/Common/BaseButton.vue` - 基础按钮组件

```vue
<!-- Common 层示例 -->
<template>
  <button 
    :class="buttonClass" 
    :disabled="disabled"
    @click="handleClick"
  >
    <slot />
  </button>
</template>

<script setup>
// 纯UI逻辑：样式、状态
const buttonClass = computed(() => ({
  'base-button': true,
  [`base-button--${type}`]: true,
  'base-button--disabled': disabled
}))
</script>
```

##### **4. API 层 - 数据接口层**
- **职责**：接口封装、数据适配、错误处理、请求拦截
- **特点**：统一的API调用方式，集中管理接口
- **示例**：`src/api/Auth/auth.js` - 认证相关接口

```javascript
// API 层示例
import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

export const getUserInfo = () => {
  return request({
    url: '/auth/user',
    method: 'GET'
  })
}
```

##### **5. Store 层 - 状态管理层**
- **职责**：全局状态、模块状态、数据持久化
- **特点**：集中式状态管理，响应式数据更新
- **示例**：`src/store/modules/auth.js` - 认证状态管理

```javascript
// Store 层示例
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    userInfo: getUserInfo(),
    permissions: []
  }),
  
  actions: {
    async login(credentials) {
      const response = await login(credentials)
      this.token = response.token
      this.userInfo = response.user
      setToken(response.token)
    }
  }
})
```

#### 🔄 数据流向

```
用户操作 → Business组件 → API调用 → Store更新 → Views组件 → 路由导航
    ↓           ↓           ↓         ↓         ↓         ↓
  交互事件    业务逻辑    数据获取   状态管理   页面更新   导航跳转
```

#### 🎯 架构优势

##### **✅ 关注点分离**
- **Views层**：专注页面布局和导航
- **Business层**：专注业务逻辑和数据处理
- **Common层**：专注UI组件和样式
- **API层**：专注数据接口和网络请求
- **Store层**：专注状态管理和数据持久化

##### **✅ 高度可复用**
- **Business组件**：可在多个页面中复用
- **Common组件**：可在整个项目中复用
- **API接口**：统一的调用方式
- **Store模块**：模块化的状态管理

##### **✅ 易于维护**
- **分层清晰**：每层职责明确，便于定位问题
- **模块化**：按功能模块组织，便于扩展
- **标准化**：统一的开发规范和代码风格

##### **✅ 团队协作**
- **并行开发**：不同层可以并行开发
- **代码复用**：减少重复代码，提高效率
- **知识共享**：清晰的架构便于团队理解

#### 📊 组件分类

| 组件类型 | 位置 | 职责 | 复用性 | 示例 |
|----------|------|------|--------|------|
| **页面组件** | `views/` | 页面布局、路由导航 | 低 | `Login/index.vue` |
| **业务组件** | `components/Business/` | 业务逻辑、数据处理 | 高 | `LoginForm.vue` |
| **通用组件** | `components/Common/` | UI展示、基础功能 | 极高 | `BaseButton.vue` |
| **布局组件** | `components/Layout/` | 页面结构、导航 | 中 | `MainLayout.vue` |

#### 🚀 开发流程

1. **需求分析** → 确定功能模块和页面结构
2. **API设计** → 定义接口规范和数据结构
3. **组件设计** → 设计Business组件和Common组件
4. **页面开发** → 组合组件实现页面功能
5. **状态管理** → 管理全局状态和模块状态
6. **测试验证** → 功能测试和性能优化

## 📁 项目结构

```
sci-z-web/
├── public/                     # 静态资源
│   └── favicon.ico
├── src/
│   ├── api/                    # API 接口层
│   │   ├── AI/                # AI 相关接口
│   │   │   ├── ai.js          # AI 助手 API
│   │   │   └── index.js       # 模块导出
│   │   ├── Auth/              # 认证相关接口
│   │   │   ├── auth.js        # 登录、注册、重置密码
│   │   │   └── index.js       # 模块导出
│   │   ├── Common/            # 通用接口
│   │   │   ├── adapters.js    # 数据适配器
│   │   │   ├── constants.js   # API 常量
│   │   │   ├── types.js       # 类型定义
│   │   │   └── index.js       # 模块导出
│   │   ├── Declaration/       # 申报相关接口
│   │   │   ├── declaration.js # 申报管理 API
│   │   │   └── index.js       # 模块导出
│   │   ├── File/              # 文件管理接口
│   │   │   ├── file.js        # 文件上传、下载 API
│   │   │   └── index.js       # 模块导出
│   │   ├── Knowledge/         # 知识库接口
│   │   │   ├── knowledge.js   # 知识库管理 API
│   │   │   └── index.js       # 模块导出
│   │   ├── Project/           # 项目相关接口
│   │   │   ├── project.js     # 项目管理 API
│   │   │   └── index.js       # 模块导出
│   │   ├── Report/            # 报告管理接口
│   │   │   ├── report.js      # 报告生成 API
│   │   │   └── index.js       # 模块导出
│   │   ├── System/            # 系统管理接口
│   │   │   ├── system.js      # 系统配置 API
│   │   │   └── index.js       # 模块导出
│   │   ├── User/              # 用户相关接口
│   │   │   ├── user.js        # 用户管理 API
│   │   │   └── index.js       # 模块导出
│   │   ├── index.js           # API 统一导出
│   │   └── README.md          # API 文档
│   ├── assets/                # 静态资源
│   │   ├── images/            # 图片资源
│   │   │   └── logo.svg       # 项目 Logo
│   │   └── styles/            # 全局样式
│   │       ├── common.scss    # 通用样式
│   │       ├── design-system.scss # 设计系统
│   │       ├── mixins.scss    # SCSS 混入
│   │       └── variables.scss # SCSS 变量
│   ├── components/            # 组件库
│   │   ├── Business/          # 业务组件
│   │   │   ├── AI/            # AI 相关组件
│   │   │   ├── Auth/          # 认证相关组件
│   │   │   │   ├── LoginForm.vue      # 登录表单
│   │   │   │   ├── RegisterForm.vue  # 注册表单
│   │   │   │   ├── ResetPasswordForm.vue # 重置密码表单
│   │   │   │   └── index.js           # 模块导出
│   │   │   ├── Dashboard/     # 仪表板组件
│   │   │   │   ├── DashboardStatCard.vue # 统计卡片
│   │   │   │   └── index.js           # 模块导出
│   │   │   ├── Declaration/    # 申报相关组件
│   │   │   ├── Detail/        # 详情展示组件
│   │   │   │   ├── AttachmentList.vue  # 附件列表
│   │   │   │   ├── InfoCard.vue       # 信息卡片
│   │   │   │   ├── ProgressBar.vue    # 进度条
│   │   │   │   ├── Timeline.vue       # 时间线
│   │   │   │   └── index.js           # 模块导出
│   │   │   ├── Form/          # 表单相关组件
│   │   │   │   ├── FileUpload.vue     # 文件上传
│   │   │   │   ├── FormActions.vue    # 表单操作
│   │   │   │   ├── FormSection.vue    # 表单区块
│   │   │   │   └── index.js            # 模块导出
│   │   │   ├── Knowledge/      # 知识库组件
│   │   │   ├── Legacy/         # 遗留组件
│   │   │   │   ├── ChartContainer.vue # 图表容器
│   │   │   │   ├── QuickActions.vue   # 快捷操作
│   │   │   │   ├── RecentActivity.vue # 最近活动
│   │   │   │   ├── StatCard.vue       # 统计卡片
│   │   │   │   └── index.js           # 模块导出
│   │   │   ├── List/           # 列表相关组件
│   │   │   │   ├── ActionButtons.vue # 操作按钮
│   │   │   │   ├── DataTable.vue      # 数据表格
│   │   │   │   ├── SearchForm.vue     # 搜索表单
│   │   │   │   ├── StatusTag.vue      # 状态标签
│   │   │   │   └── index.js           # 模块导出
│   │   │   ├── Project/        # 项目相关组件
│   │   │   ├── Report/         # 报告相关组件
│   │   │   ├── System/         # 系统管理组件
│   │   │   ├── User/           # 用户相关组件
│   │   │   └── index.js        # 业务组件统一导出
│   │   ├── Common/             # 通用组件
│   │   │   ├── BaseButton.vue  # 基础按钮
│   │   │   ├── BaseCard.vue    # 基础卡片
│   │   │   ├── BaseDialog.vue  # 基础对话框
│   │   │   ├── BasePagination.vue # 基础分页
│   │   │   ├── BaseScrollbar.vue # 基础滚动条
│   │   │   ├── BaseTable.vue   # 基础表格
│   │   │   ├── LanguageSwitcher.vue # 语言切换器
│   │   │   └── index.js        # 通用组件导出
│   │   ├── Layout/             # 布局组件
│   │   │   ├── Header.vue       # 页面头部
│   │   │   ├── MainLayout.vue   # 主布局
│   │   │   └── Sidebar.vue      # 侧边栏
│   │   └── index.js            # 组件库统一导出
│   ├── locales/                # 国际化配置
│   │   ├── zh-CN.js           # 中文语言包
│   │   ├── en-US.js           # 英文语言包
│   │   ├── ko-KR.js           # 韩文语言包
│   │   ├── ja-JP.js           # 日文语言包
│   │   └── index.js            # i18n 配置
│   ├── router/                 # 路由配置
│   │   └── index.js           # 路由主文件
│   ├── store/                  # 状态管理（Pinia）
│   │   ├── modules/           # Store 模块
│   │   │   ├── app.js         # 应用状态
│   │   │   └── auth.js         # 认证状态
│   │   ├── types.ts            # 类型定义
│   │   └── index.js            # Store 入口
│   ├── directives/            # 自定义指令
│   │   └── permission.js      # 权限控制指令
│   ├── utils/                  # 工具函数
│   │   ├── auth.js            # 认证工具
│   │   ├── constants.js       # 常量定义
│   │   ├── date.js            # 日期处理
│   │   ├── file.js            # 文件处理
│   │   ├── request.js         # Axios 封装
│   │   └── validate.js        # 表单验证
│   ├── views/                  # 页面视图层
│   │   ├── AI/                # AI 助手页面
│   │   │   └── Chat.vue       # 聊天页面
│   │   ├── Dashboard/         # 仪表板页面
│   │   │   └── index.vue      # 仪表板首页
│   │   ├── Declaration/        # 申报模块页面
│   │   │   ├── Create.vue     # 创建申报
│   │   │   ├── Detail.vue     # 申报详情
│   │   │   └── List.vue       # 申报列表
│   │   ├── Error/             # 错误页面
│   │   │   └── 404.vue        # 404 页面
│   │   ├── Knowledge/          # 知识库页面
│   │   │   ├── Detail.vue     # 知识库详情
│   │   │   └── List.vue       # 知识库列表
│   │   ├── Login/              # 登录页面
│   │   │   └── index.vue      # 登录首页
│   │   ├── Project/            # 项目模块页面
│   │   │   ├── Detail.vue     # 项目详情
│   │   │   ├── List.vue       # 项目列表
│   │   │   └── Progress.vue   # 项目进度
│   │   ├── Register/           # 注册页面
│   │   │   └── index.vue      # 注册首页
│   │   ├── Report/             # 报告管理页面
│   │   │   ├── Generate.vue   # 生成报告
│   │   │   └── List.vue       # 报告列表
│   │   ├── ResetPassword/      # 重置密码页面
│   │   │   └── index.vue      # 重置密码首页
│   │   ├── System/             # 系统管理页面
│   │   │   ├── Config.vue     # 系统配置
│   │   │   ├── Logs.vue       # 日志管理
│   │   │   ├── Role.vue       # 角色管理
│   │   │   └── User.vue       # 用户管理
│   │   ├── User/               # 用户中心页面
│   │   │   ├── Profile.vue    # 个人资料
│   │   │   └── Security.vue   # 安全设置
│   │   └── ComponentTest.vue   # 组件测试页面
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── index.html                  # HTML 入口文件
├── .env.development            # 开发环境配置
├── .env.production             # 生产环境配置
├── .eslintrc.js                # ESLint 配置
├── .prettierrc.js              # Prettier 配置
├── vite.config.js              # Vite 配置
├── components.d.ts             # 组件类型声明
└── package.json                # 项目依赖
```

## 🚀 功能模块

本系统共包含 **9 大核心功能模块**，覆盖科研项目全生命周期管理。

### 1. 认证模块

- **用户登录**：支持用户名/邮箱登录，可选记住登录状态
- **用户注册**：新用户注册，支持邮箱/手机验证
- **密码重置**：通过邮箱/手机验证码重置密码
- **验证码**：图形验证码防止暴力破解
- **权限控制**：基于Sa-Token的细粒度权限管理
  - **菜单权限**：动态菜单渲染，根据用户权限显示
  - **按钮权限**：使用`v-permission`指令控制按钮显示
  - **角色权限**：使用`v-role`指令控制角色相关功能
  - **数据权限**：支持`data:own`、`data:all`等数据级权限

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
