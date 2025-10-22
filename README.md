# Sci-Z Platform College

## 项目简介

高校科研项目管理平台，基于 DDD 分层架构与前后端分离设计，覆盖科研项目从申报、立项、执行到报告的全生命周期管理。平台集成 Dify 工作流与知识库，实现申报流程辅助、报告自动生成、语义检索与智能问答等能力，支持单体应用多实例部署，兼顾易用性与可扩展性。

---

## 功能模块

### 前端功能模块（sci-z-web）
- 🔐 认证：登录/注册/重置密码、验证码、保登录
- 📊 仪表板：统计卡片、趋势图、最近申报、快捷入口
- 📝 申报：列表/新建/详情、触发 Dify 工作流、状态跟踪、草稿
- 📁 项目：列表/详情、成员与进度管理、项目文档、知识库搜索
- 📄 报告管理：创建/生成/预览/导出（PDF/Word/Markdown）、进度显示
- 📚 知识库：多级文件夹、文件上传/管理、AI 问答（语义检索）
- 🤖 AI 助手：多会话管理、消息流式输出、知识来源和置信度
- 👤 用户中心：个人信息、头像、密码修改、登录日志
- ⚙️ 系统管理：用户/角色/权限/部门、行业配置、系统配置、日志

### 后端功能模块（sci-z-server）
- IAM 领域：用户、角色、权限、部门、行业配置、登录/操作日志
- 申报领域：申报创建与详情、Dify 工作流触发与状态跟踪
- 项目领域：项目、成员、进度、与申报衔接、知识库关联
- 报告领域：报告生成配置、进度与结果、导出文件管理
- 知识库领域：知识库、文件夹、文件关联与检索
- 文件领域：通用附件、去重、MinIO 存储与 Dify 同步
- AI 助手领域：会话与消息管理、对接 Dify
- 系统配置领域：系统级配置中心

---

## 技术栈

### 前端
- Vue.js 3、Vue Router 4、Pinia
- Element Plus、SCSS 设计系统
- Axios、Vite 5

### 后端
- Java 21、Spring Boot 3.2.x（虚拟线程/记录模式）
- PostgreSQL 14+、Redis 7.x、Kafka 3.x、MinIO
- MyBatis-Plus、Sa-Token、Resilience4j、LangChain4j

### AI 平台
- Dify（工作流 + 知识库）、向量数据库（语义检索）

---

## 部署方式

### 后端部署（sci-z-server）
1) 打包
```bash
mvn clean package -Dmaven.test.skip=true
```
2) 启动（开发）
```bash
java -jar target/sci-z-server-1.0.0.jar --spring.profiles.active=dev
```
3) 启动（生产，示例）
```bash
nohup java -jar target/sci-z-server-1.0.0.jar \
  --spring.profiles.active=prod > app.log 2>&1 &
```

必要外部组件：PostgreSQL、Redis、Kafka、MinIO（按需准备并在 application 配置中填写连接信息）。

### 前端部署（sci-z-web）
1) 安装与构建
```bash
npm i
npm run build
```
2) Nginx 托管（示例）
```nginx
server {
  listen 80;
  server_name your.domain.com;

  location / {
    root /usr/share/nginx/html;   # 指向 dist 产物目录
    index index.html;
    try_files $uri $uri/ /index.html;
  }

  location /api/ {
    proxy_pass http://backend:8080;  # 指向后端服务
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

---

## 目录结构
```
Sci-Z-Platform-College/
├── sci-z-server/   # 后端（Java 21 + Spring Boot 3）
└── sci-z-web/      # 前端（Vue 3 + Vite + Element Plus）
```

