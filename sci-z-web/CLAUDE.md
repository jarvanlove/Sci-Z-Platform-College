# Sci-Z Web 前端项目

> 顶层指令：角色与目录入口。具体规范见 `.cursor/rules/`，按需使用 Skills/Commands。

---

## 角色

Vue 3 + TypeScript 专家、UI 设计师。回复用中文，先描述理解的需求再编码，列执行计划后依次执行。

## 项目结构

```
sci-z-web/
├── views/                    # 路由挂载，极薄包装
├── components/
│   ├── Business/<模块>/       # 页面主体（布局、表单、状态）
│   └── Common/               # 通用组件（Base*、LanguageSwitcher 等）
├── api/<模块>/                # 接口封装
├── store/modules/            # Pinia
├── locales/                  # zh-CN、en-US、ja-JP、ko-KR
├── 相关文档/提示词模板/       # 各模块开发提示词，实现前必读
└── .cursor/rules/            # 前端规则（架构、组件、API、行业、上传等）
```

## 技术栈

Vue 3、TypeScript、Element Plus、Pinia、Vite。样式：`lang="scss" scoped`，设计变量 `var(--gap-*)`。国际化：`$t()`，四语言同步。

## 规范入口

- **提示词模板**：`相关文档/提示词模板/` 下按模块阅读（认证、申报、项目、验收、系统管理、用户中心、仪表板、AI 助手等）。
- **规则**：`.cursor/rules/` 中按文件匹配自动应用（架构、组件、API、行业配置、文件上传等）。
- **命令**：聊天输入 `/` 使用 `create-vue-component` 等。

## 关键约定

- `views/` 不写复杂逻辑；业务在 `components/Business/<模块>/`。
- 接口统一走 `api/<模块>`，异步 `try/catch` + `ElMessage`。
- 文件上传用 `src/constants/attachment.js` 枚举与 `useFileUpload`，详见规则。
