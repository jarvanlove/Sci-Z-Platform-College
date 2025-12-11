# 高校科研项目管理平台

> 基于 Vue.js 3 + Dify AI 的智能化科研项目全生命周期管理系统

## 📋 项目概述

高校科研项目管理平台是一个基于 **Vue.js 3**、**前后端分离**、**现代化前端架构**的智能化科研项目全生命周期管理系统。平台以 Dify 工作流为核心，实现科研项目从申报、执行到验收的智能化、自动化管理。

### 🎯 核心特性

- 🎯 **现代化前端架构**：Vue 3 + Vite + Element Plus，高性能开发体验
- 🚀 **组件化开发**：高度可复用的组件库，提升开发效率
- 🤖 **AI 智能化**：集成 Dify 平台，实现智能工作流编排
- 📊 **全生命周期管理**：覆盖申报、执行、报告全流程
- 🔒 **安全可靠**：基于 Token 的前端认证体系（Bearer Token）
- ⚡ **Vue 3 新特性**：Composition API、响应式系统、Teleport 等

## 🛠 前端技术栈

### 核心技术

- **Vue.js 3.4.0+** - 渐进式 JavaScript 框架
- **Vue Router 4.2.5+** - 官方路由管理器
- **Pinia 2.1.7+** - 新一代状态管理
- **Element Plus 2.3.14** - Vue 3 企业级组件库
- **Axios 1.6.2+** - HTTP 客户端
- **Vite 5.0.8+** - 新一代前端构建工具
- **Vue I18n 9.14.5+** - 国际化解决方案
- **SCSS** - CSS 预处理器
- **ESLint + Prettier** - 代码质量工具

### AI 平台

- **Dify** - AI 工作流编排与知识库管理
- **Vector Database** - 向量数据库（语义搜索）

### 架构模式

本项目采用 **分层架构 + 组件化设计** 的现代化前端架构模式，实现了高度的模块化、可复用性和可维护性。

#### 🏗️ 分层架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    Views 层                              │  ← 页面视图层（极薄包装层）
│  (页面路由挂载、业务组件引入、布局组合)                  │
├─────────────────────────────────────────────────────────┤
│                    Layout 层                             │  ← 布局组件层
│  (页面结构布局、导航菜单、头部侧边栏)                    │
├─────────────────────────────────────────────────────────┤
│                   Business 层                           │  ← 业务逻辑层
│  (业务组件、数据处理、用户交互、表单验证)                │
├─────────────────────────────────────────────────────────┤
│                    Common 层                             │  ← 通用组件层
│  (基础UI组件、按钮、卡片、表格、对话框等)                │
├─────────────────────────────────────────────────────────┤
│                  Composables 层                         │  ← 组合式函数层
│  (可复用逻辑封装、文件上传、状态处理等)                  │
├─────────────────────────────────────────────────────────┤
│                     API 层                               │  ← 数据接口层
│  (接口封装、数据适配、请求拦截、错误处理)                │
├─────────────────────────────────────────────────────────┤
│                    Store 层                              │  ← 状态管理层
│  (全局状态、模块状态、数据持久化、响应式更新)            │
├─────────────────────────────────────────────────────────┤
│                    Router 层                             │  ← 路由配置层
│  (路由定义、权限控制、导航守卫、懒加载)                  │
├─────────────────────────────────────────────────────────┤
│                    Utils 层                              │  ← 工具函数层
│  (工具方法、日期处理、文件处理、请求封装、日志)          │
├─────────────────────────────────────────────────────────┤
│                  Constants 层                            │  ← 常量定义层
│  (常量定义、枚举值、配置项)                              │
├─────────────────────────────────────────────────────────┤
│                  Directives 层                           │  ← 自定义指令层
│  (权限指令、DOM 操作指令)                                 │
├─────────────────────────────────────────────────────────┤
│                   Locales 层                             │  ← 国际化配置层
│  (多语言支持、语言包管理)                                │
├─────────────────────────────────────────────────────────┤
│                    Assets 层                             │  ← 静态资源层
│  (图片资源、全局样式、设计系统变量)                      │
└─────────────────────────────────────────────────────────┘
```

#### 📋 各层职责详解

##### **1. Views 层 - 页面视图层**
- **职责**：页面路由挂载、业务组件引入、布局组合
- **特点**：极薄包装层，不包含业务逻辑，专注于路由挂载和组件引入
- **位置**：`src/views/` 目录
- **说明**：Views 层是页面入口，仅负责从 Business 层引入业务组件并挂载到路由，实现页面与路由的映射关系

##### **2. Layout 层 - 布局组件层**
- **职责**：页面结构布局、导航菜单、头部侧边栏
- **特点**：提供统一的页面布局框架，支持多布局模式
- **位置**：`src/components/Layout/` 目录
- **说明**：Layout 层提供页面布局框架，包括主布局、头部导航、侧边栏菜单等，确保整个应用的布局一致性

##### **3. Business 层 - 业务逻辑层**
- **职责**：业务逻辑、数据处理、用户交互、API调用、表单验证
- **特点**：高度可复用，可在多个页面中使用，包含完整的业务逻辑
- **位置**：`src/components/Business/` 目录
- **说明**：Business 层是核心业务逻辑层，负责处理 API 调用、数据格式化、表单验证、错误处理、状态管理等业务相关操作，通过事件向 Views 层传递处理结果

##### **4. Common 层 - 通用组件层**
- **职责**：基础UI组件、按钮、卡片、表格、对话框等
- **特点**：纯UI组件，无业务逻辑，高度可复用
- **位置**：`src/components/Common/` 目录
- **说明**：Common 层提供基础UI组件库，这些组件只负责UI展示和样式，不包含任何业务逻辑，可在整个项目中复用

##### **5. Composables 层 - 组合式函数层**
- **职责**：可复用逻辑封装、状态处理、副作用管理
- **特点**：基于 Composition API，提供可复用的组合式函数
- **位置**：`src/composables/` 目录
- **说明**：Composables 层封装可复用的组合式函数，如文件上传、表单处理、状态管理等，供 Business 层和 Common 层使用

##### **6. API 层 - 数据接口层**
- **职责**：接口封装、数据适配、错误处理、请求拦截
- **特点**：统一的API调用方式，集中管理接口
- **位置**：`src/api/` 目录
- **说明**：API 层统一封装所有后端接口调用，提供统一的请求方式，处理数据格式转换、错误处理、请求拦截等功能，Business 层通过调用 API 层的方法来获取数据

##### **7. Store 层 - 状态管理层**
- **职责**：全局状态、模块状态、数据持久化、响应式更新
- **特点**：集中式状态管理，基于 Pinia，响应式数据更新
- **位置**：`src/store/modules/` 目录
- **说明**：Store 层使用 Pinia 进行状态管理，存储全局状态如用户信息、认证状态、行业配置等，提供响应式数据更新和数据持久化功能

##### **8. Router 层 - 路由配置层**
- **职责**：路由定义、权限控制、导航守卫、懒加载
- **特点**：集中管理路由配置，支持权限控制和懒加载
- **位置**：`src/router/` 目录
- **说明**：Router 层使用 Vue Router 管理应用路由，定义路由规则，实现权限控制、导航守卫、路由懒加载等功能

##### **9. Utils 层 - 工具函数层**
- **职责**：工具方法、日期处理、文件处理、请求封装、日志工具
- **特点**：纯函数工具集合，无副作用
- **位置**：`src/utils/` 目录
- **说明**：Utils 层提供各种工具函数，包括认证工具、日期处理、文件处理、请求封装、表单验证、日志工具等，供各个层级使用

##### **10. Constants 层 - 常量定义层**
- **职责**：常量定义、枚举值、配置项
- **特点**：集中管理项目常量，避免魔法值
- **位置**：`src/constants/` 目录
- **说明**：Constants 层定义项目中的常量、枚举值、配置项等，如附件类型、状态枚举等，确保常量使用的统一性

##### **11. Directives 层 - 自定义指令层**
- **职责**：权限指令、DOM操作指令
- **特点**：扩展 Vue 指令功能，实现特定DOM操作
- **位置**：`src/directives/` 目录
- **说明**：Directives 层提供自定义 Vue 指令，如权限控制指令 `v-permission`，实现基于权限的DOM元素显示控制

##### **12. Locales 层 - 国际化配置层**
- **职责**：多语言支持、语言包管理
- **特点**：支持多语言切换，集中管理语言包
- **位置**：`src/locales/` 目录
- **说明**：Locales 层使用 vue-i18n 实现国际化，支持中文、英文、日文、韩文等多种语言，提供语言包管理和语言切换功能

##### **13. Assets 层 - 静态资源层**
- **职责**：图片资源、全局样式、设计系统变量
- **特点**：静态资源管理，提供全局样式和设计系统
- **位置**：`src/assets/` 目录
- **说明**：Assets 层管理静态资源，包括图片、全局样式文件、SCSS变量、混入等，提供统一的设计系统和样式规范

#### 🔄 数据流向

```
用户操作
    ↓
Views 层（页面入口）
    ↓
Layout 层（布局框架）
    ↓
Business 层（业务逻辑）
    ↓                    ↓
Composables 层      Common 层（UI组件）
    ↓                    ↓
Utils 层（工具函数）  Directives 层（指令）
    ↓                    ↓
API 层（接口调用）   Constants 层（常量）
    ↓
Store 层（状态管理）
    ↓
Router 层（路由导航）
    ↓
页面更新反馈
```

**完整数据流示例：**
```
用户点击按钮 
  → Views 层接收事件 
  → Business 层处理业务逻辑 
  → 调用 Composables 组合函数 
  → 使用 Utils 工具函数处理数据 
  → 调用 API 层请求后端接口 
  → 更新 Store 状态 
  → 通过 Router 层导航 
  → Common 层UI组件响应状态变化 
  → 页面更新展示结果
```

#### 🎯 架构优势

##### **✅ 关注点分离**
- **Views层**：专注路由挂载和组件引入
- **Layout层**：专注页面布局框架
- **Business层**：专注业务逻辑和数据处理
- **Common层**：专注UI组件和样式
- **Composables层**：专注可复用逻辑封装
- **API层**：专注数据接口和网络请求
- **Store层**：专注状态管理和数据持久化
- **Router层**：专注路由配置和权限控制
- **Utils层**：专注工具函数提供
- **Constants层**：专注常量定义管理
- **Directives层**：专注自定义指令功能
- **Locales层**：专注国际化支持
- **Assets层**：专注静态资源管理

##### **✅ 高度可复用**
- **Business组件**：可在多个页面中复用
- **Common组件**：可在整个项目中复用
- **Composables函数**：可在多个组件中复用逻辑
- **API接口**：统一的调用方式，便于维护
- **Store模块**：模块化的状态管理
- **Utils工具**：通用工具函数，提高开发效率
- **Constants常量**：统一常量管理，避免魔法值

##### **✅ 易于维护**
- **分层清晰**：13层架构，每层职责明确，便于定位问题
- **模块化**：按功能模块组织，便于扩展和维护
- **标准化**：统一的开发规范和代码风格
- **类型安全**：TypeScript支持，提高代码质量

##### **✅ 团队协作**
- **并行开发**：不同层可以并行开发，减少冲突
- **代码复用**：减少重复代码，提高开发效率
- **知识共享**：清晰的架构便于团队理解和学习
- **职责明确**：每层职责清晰，便于分工协作


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
│   │   ├── Dify/              # Dify 平台接口
│   │   │   ├── dify.js        # Dify API 封装
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
│   │   │   │   ├── WorkflowSelect.vue # 工作流选择器
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
│   │   │   │   ├── ProjectDetail.vue   # 项目详情
│   │   │   │   ├── ProjectList.vue     # 项目列表
│   │   │   │   ├── ProjectProgress.vue # 项目进度
│   │   │   │   └── index.js            # 模块导出
│   │   │   ├── Report/         # 报告相关组件
│   │   │   │   ├── ReportGenerate.vue  # 报告生成
│   │   │   │   ├── ReportList.vue      # 报告列表
│   │   │   │   └── index.js            # 模块导出
│   │   │   ├── System/         # 系统管理组件
│   │   │   │   ├── ApiKeyManagement.vue # API 密钥管理
│   │   │   │   ├── LogManagement.vue    # 日志管理
│   │   │   │   ├── RoleManagement.vue   # 角色管理
│   │   │   │   ├── SystemConfig.vue     # 系统配置
│   │   │   │   ├── UserManagement.vue   # 用户管理
│   │   │   │   └── index.js             # 模块导出
│   │   │   ├── User/           # 用户相关组件
│   │   │   └── index.js        # 业务组件统一导出
│   │   ├── Common/             # 通用组件
│   │   │   ├── AgreementNotice.vue # 协议提醒组件
│   │   │   ├── BackButton.vue  # 返回按钮
│   │   │   ├── BaseButton.vue  # 基础按钮
│   │   │   ├── BaseCard.vue    # 基础卡片
│   │   │   ├── BaseDatePicker.vue # 基础日期选择器
│   │   │   ├── BaseDialog.vue  # 基础对话框
│   │   │   ├── BasePagination.vue # 基础分页
│   │   │   ├── BaseScrollbar.vue # 基础滚动条
│   │   │   ├── BaseSwitch.vue  # 基础开关
│   │   │   ├── BaseTable.vue   # 基础表格
│   │   │   ├── BaseTooltip.vue # 基础提示框
│   │   │   ├── FilePreview.vue # 文件预览组件
│   │   │   ├── LanguageSwitcher.vue # 语言切换器
│   │   │   ├── ProjectProgressBar.vue # 项目进度条
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
│   ├── composables/           # 组合式函数
│   │   └── useFileUpload.js   # 文件上传组合函数
│   ├── constants/             # 常量定义
│   │   └── attachment.js      # 附件相关常量
│   ├── store/                  # 状态管理（Pinia）
│   │   ├── modules/           # Store 模块
│   │   │   ├── app.js         # 应用状态
│   │   │   ├── auth.js         # 认证状态
│   │   │   └── industry.js     # 行业配置状态
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
│   │   ├── simpleLogger.js    # 简单日志工具
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
│   │   │   ├── ApiKey.vue     # API 密钥管理
│   │   │   ├── Config.vue     # 系统配置
│   │   │   ├── Logs.vue       # 日志管理
│   │   │   ├── Role.vue       # 角色管理
│   │   │   └── User.vue       # 用户管理
│   │   ├── User/               # 用户中心页面
│   │   │   ├── Profile.vue    # 个人资料
│   │   │   └── Security.vue   # 安全设置
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── index.html                  # HTML 入口文件
├── env.development             # 开发环境配置
├── env.production              # 生产环境配置
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
- **API 密钥管理**：Dify API 密钥的配置和管理

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

**开发环境 (env.development)**

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

**生产环境 (env.production)**

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


## 📄 许可证

MIT License

## 📞 联系方式

如有问题，请联系开发团队。
