# Sci-Z Web 规则说明

> 本目录规则与 `sci-z-web/CLAUDE.md` 配合使用。CLAUDE.md 仅保留顶层指令，具体规范拆分为以下 .mdc 文件。

---

## 规则文件

| 文件 | 说明 | 触发 |
|------|------|------|
| **vue-reply-and-flow.mdc** | 角色、回复结构、理解需求 | alwaysApply: true |
| **current-project-rules.mdc** | 严格约束与规则入口 | 智能/文件匹配 |
| **vue-prompt-templates.mdc** | 各模块提示词模板路径与使用建议 | globs: **/*.vue、**/相关文档/** |
| **vue-industry-and-api.mdc** | 行业配置、部门标签、接口调用节奏 | globs: api/**、store/**、components/** |
| **vue-architecture.mdc** | 目录与职责、开发流程、代码规范 | globs: views/**、components/**、**/*.vue |
| **vue-components-and-upload.mdc** | 通用组件目录与文件上传规范 | globs: components/**、constants/**、composables/** |

## 使用方式

- Cursor 会根据 description 与 globs 自动加载相关规则
- 回复规范（vue-reply-and-flow.mdc）始终应用
- 其他规则在编辑/讨论对应文件时按需应用
- 提示词模板路径见 vue-prompt-templates.mdc，实现前请先读对应模块模板
