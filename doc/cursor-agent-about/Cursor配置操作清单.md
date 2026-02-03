# Cursor 配置操作清单

> 在 Cursor IDE 中按顺序勾选完成以下步骤，即可完成 Subagents、Indexing & Docs、Hooks 的配置。仅包含界面操作，无代码。

---

## 前置：打开设置

- [ ] 打开 Cursor，打开本项目工作区
- [ ] 打开设置：**File → Preferences → Cursor Settings**（或 `Ctrl + ,`），左侧选择 **Tools & MCP**

---

## 一、Subagents（子代理）

本项目已在仓库中配置子代理（`.cursor/agents/`），只需确认 Cursor 已识别。

- [ ] 在 **Tools & MCP** 下找到 **Rules, Skills, Subagents**
- [ ] 点击进入，切换到 **Subagents** 区域
- [ ] 确认列表中或项目内能看到 **verifier**、**debugger**（若界面显示“从项目加载”或项目路径下的 agents，即表示已识别）
- [ ] （可选）在聊天中让 Agent 委派任务给子代理，测试是否可用

---

## 二、Indexing & Docs（索引与文档）

### 2.1 代码库索引

- [ ] 在 **Tools & MCP** 下点击 **Indexing & Docs**
- [ ] 找到 **Codebase Indexing** 区域
- [ ] 确认索引进度为完成（如 100%）；若未完成，等待完成或点击 **Sync**
- [ ] 确认 **Index New Folders** 已开启（新文件夹会被自动索引）
- [ ] 索引忽略规则已由项目根目录 `.cursorignore` 控制，无需在界面重复配置

### 2.2 添加文档（Docs）

- [ ] 在同一 **Indexing & Docs** 页面找到 **Docs** 相关区域
- [ ] 点击 **+ Add Doc**（或类似按钮）
- [ ] 按需添加以下文档（可多选或分批添加）：

  | 建议添加 | 路径                               |
  | -------- | ---------------------------------- |
  | [ ]      | 根目录 `README.md`                 |
  | [ ]      | `开发计划.md`                      |
  | [ ]      | `项目配置方案.md`                  |
  | [ ]      | `Cursor功能设置指南.md`            |
  | [ ]      | `sci-z-web/README.md`（若存在）    |
  | [ ]      | `sci-z-server/README.md`（若存在） |

- [ ] 添加后，在聊天中可用 **@Add** 将已添加的文档加入上下文

---

## 三、Hooks（钩子）

仅在需要将 Agent/Subagent 生成的报告等**统一保存到指定目录**时配置。

- [ ] 在 **Tools & MCP** 下点击 **Hooks**
- [ ] 查看 **Configured Hooks**：若为 0，则尚未配置
- [ ] 若需配置：点击添加新 Hook，按界面提示填写：
  - **触发条件**：如“当 Agent 生成代码审查报告时”
  - **输出目录**：建议 `reports/code-review/`、`reports/verifier/`、`reports/debugger/`（详见 `.cursor/hooks/README.md`）
  - **命名规则**：如 `code-review-{date}.md`
- [ ] 保存后，可通过 **Execution Log** 查看执行记录

---

## 完成确认

- [ ] **Subagents**：verifier、debugger 已识别或可用
- [ ] **Indexing & Docs**：代码库索引已完成，已按需添加 Docs
- [ ] **Hooks**：若需要报告落盘，已配置并可在 Execution Log 中验证

更多说明见 `.cursor/README.md`、`.cursor/hooks/README.md`。
