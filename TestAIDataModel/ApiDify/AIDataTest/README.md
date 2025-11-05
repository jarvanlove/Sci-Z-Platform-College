# Dify AI 知识库管理平台

基于 Vue 3 + TypeScript + Vite 构建的 AI 知识库管理平台，集成 Dify API 服务，提供完整的知识库创建、文档上传、智能检索等功能。

## 🚀 功能特性

### 📚 知识库管理
- ✅ **创建知识库**：支持完整的知识库配置
- ✅ **权限管理**：仅自己、团队成员、部分成员权限
- ✅ **索引模式**：高质量、经济模式选择
- ✅ **数据源类型**：上传文件、外部知识库

### 📤 文档上传
- ✅ **多格式支持**：PDF、Word、TXT 等格式
- ✅ **拖拽上传**：直观的文件拖拽界面
- ✅ **进度跟踪**：实时上传进度显示
- ✅ **文件验证**：大小、类型自动验证

### 🔍 智能检索
- ✅ **混合检索**：关键词 + 语义检索
- ✅ **语义检索**：基于向量相似度
- ✅ **全文检索**：传统关键词匹配
- ✅ **Rerank 优化**：智能重排序提升精度

### 🎯 高级功能
- ✅ **Embedding 模型**：自定义向量模型
- ✅ **检索配置**：召回条数、分数阈值设置
- ✅ **处理规则**：自动、自定义文档处理
- ✅ **多语言支持**：中文、英文等语言

## 🛠️ 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全的 JavaScript
- **Vite** - 快速的构建工具
- **Axios** - HTTP 客户端
- **Dify API** - AI 知识库服务

## 📦 项目结构

```
src/
├── components/           # 组件
│   ├── FileUpload.vue   # 文件上传组件
│   ├── Navigation.vue   # 导航组件
│   └── DifyDemo.vue     # Dify 演示组件
├── views/               # 页面
│   ├── Home.vue         # 首页
│   ├── Datasets.vue     # 数据集管理
│   ├── CreateDataset.vue # 创建知识库
│   ├── DocumentUpload.vue # 文档上传
│   └── Query.vue        # 知识库查询
├── services/            # 服务
│   └── difyApi.ts       # Dify API 服务
├── router/              # 路由
│   └── index.ts         # 路由配置
└── assets/              # 静态资源
```

## 🚀 快速开始

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 开发环境
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 预览生产版本
```bash
npm run preview
```

## 🔧 配置说明

### Dify API 配置
在 `src/services/difyApi.ts` 中配置：

```typescript
const baseURL = 'http://192.168.1.203'
const apiKey = 'dataset-XfEGbHDJ9C3koNrbyN0sYcrl'
```

### 环境变量
创建 `.env.local` 文件：

```env
VITE_DIFY_API_URL=http://192.168.1.203
VITE_DIFY_API_KEY=dataset-XfEGbHDJ9C3koNrbyN0sYcrl
```

## 📚 API 接口

### 知识库管理
- `GET /v1/datasets` - 获取数据集列表
- `POST /v1/datasets` - 创建知识库
- `GET /v1/datasets/{id}` - 获取数据集详情
- `DELETE /v1/datasets/{id}` - 删除数据集

### 文档管理
- `POST /v1/datasets/{id}/document/create-by-file` - 上传文档
- `GET /v1/datasets/{id}/documents` - 获取文档列表
- `DELETE /v1/datasets/{id}/documents/{doc_id}` - 删除文档

### 智能检索
- `POST /v1/datasets/{id}/retrieve` - 检索知识库
- `GET /v1/datasets/{id}/segments` - 获取段落列表

## 🎨 界面预览

### 主要页面
- **首页**：平台概览和快速入口
- **数据集管理**：知识库列表和操作
- **创建知识库**：完整的知识库配置界面
- **文档上传**：文件上传和处理界面
- **知识库查询**：智能检索和问答界面

### 核心组件
- **FileUpload**：拖拽文件上传组件
- **Navigation**：响应式导航栏
- **DifyDemo**：API 功能演示组件

## 🔍 使用指南

### 创建知识库
1. 访问 `/create-dataset` 页面
2. 填写知识库基本信息
3. 配置索引模式和权限
4. 设置检索模型参数
5. 点击"创建知识库"

### 上传文档
1. 在数据集页面点击"上传文档"
2. 选择或拖拽文件到上传区域
3. 配置文档处理规则
4. 点击"上传文档"

### 智能检索
1. 访问 `/query` 页面
2. 选择目标知识库
3. 输入查询问题
4. 查看检索结果

## 🐛 问题排查

### 常见问题
1. **API 连接失败**：检查网络连接和 API 地址
2. **文件上传失败**：检查文件格式和大小限制
3. **检索无结果**：检查知识库是否有文档内容

### 调试模式
开启控制台日志：
```typescript
// 在 difyApi.ts 中已包含详细的调试日志
console.log('API 请求:', request)
console.log('API 响应:', response)
```

## 📝 更新日志

### v1.2.0 (最新)
- ✅ 新增知识库创建功能
- ✅ 完善文档上传功能
- ✅ 优化用户界面设计
- ✅ 增强错误处理机制

### v1.1.0
- ✅ 基础 API 集成
- ✅ 文件上传组件
- ✅ 知识库查询功能

### v1.0.0
- ✅ 项目初始化
- ✅ 基础框架搭建

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。

## 👥 开发者

- **@jarvanlove** - 项目负责人
- **AI Assistant** - 功能开发与优化

---

**⭐ 如果这个项目对您有帮助，请给我们一个 Star！**
