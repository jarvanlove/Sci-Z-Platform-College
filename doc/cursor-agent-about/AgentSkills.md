# Anthropic Agent Skills 完整指南

> 本文档基于 Anthropic 官方仓库 https://github.com/anthropics/skills 及官方文档整理
> 
> 更新时间：2026年1月21日

---

## 目录

**第一部分：基础概念**
1. [什么是 Agent Skills](#1-什么是-agent-skills)
2. [Skills、MCP、Agent 三者详细介绍与对比](#2-skillsmcpagent-三者详细介绍与对比)
3. [Skills 与 Rules 的区别](#3-skills-与-rules-的区别)
4. [Rules 到 CLAUDE.md 的演变](#4-rules-到-claudemd-的演变)

**第二部分：全球工具支持情况**
5. [全球 AI 编程工具 Skills 支持对比](#5-全球-ai-编程工具-skills-支持对比)

**第三部分：实践指南**
6. [在 Cursor 中的使用方法](#6-在-cursor-中的使用方法)
7. [工作流程与整合方案](#7-工作流程与整合方案)
8. [常见问题与冲突处理](#8-常见问题与冲突处理)

**第四部分：进阶内容**
9. [如何定义 Skill 内容（官方规范详解）](#9-如何定义-skill-内容官方规范详解)
10. [Sci-Z-Platform-College 项目 Skills 设计](#10-sci-z-platform-college-项目-skills-设计)

**附录**
- [附录 A：快速参考卡片](#附录-a快速参考卡片)
- [附录 B：参考资源](#附录-b参考资源)
- [附录 C：Skill 编写检查清单](#附录-cskill-编写检查清单)

---

# 第一部分：基础概念

---

## 1. 什么是 Agent Skills

### 1.1 官方定义

**Agent Skills** 是 Anthropic 于 2025 年 10 月推出的开放标准，旨在为 AI Agent 提供模块化、可复用的专业能力。

- **官方仓库**：https://github.com/anthropics/skills
- **官方文档**：https://docs.anthropic.com/en/docs/agents-and-tools/agent-skills/overview
- **标准托管**：agentskills.io

### 1.2 核心特点

| 特性 | 说明 |
|------|------|
| **模块化** | 每个 Skill 是独立的能力单元，可组合使用 |
| **可移植** | 跨平台兼容（Claude.ai、Claude Code、Cursor、API） |
| **自主调用** | Agent 根据任务上下文自动决定是否加载 Skill |
| **渐进式加载** | 三级加载机制，节省 Context Window |
| **可执行** | 可包含脚本，支持代码执行 |
| **开放标准** | 2025年12月成为开放标准，多家厂商采用 |

### 1.3 Skill 目录结构（官方规范）

```
skill-name/
├── SKILL.md          # 必需：技能定义文件
├── scripts/          # 可选：可执行脚本
│   ├── run.py
│   └── helper.sh
├── references/       # 可选：参考文档
│   └── style-guide.md
└── assets/           # 可选：模板和配置
    └── template.json
```

### 1.4 SKILL.md 文件格式（官方规范）

```markdown
---
name: skill-name
description: 技能描述，Claude 根据此描述决定何时调用该技能
---

# 技能名称

## Instructions
详细的执行指令，使用祈使语气

## Examples
具体的输入输出示例

## Guidelines
特殊规则和边界情况处理
```

#### YAML Frontmatter 字段说明

| 字段 | 必需 | 说明 |
|------|------|------|
| `name` | ✅ | 唯一标识符，1-64字符，小写字母+数字+连字符 |
| `description` | ✅ | 技能描述，最多1024字符，Claude 用此判断是否调用 |

### 1.5 渐进式加载机制（Progressive Disclosure）

```
Level 1: 仅加载 name + description（预加载到系统提示）
    ↓
Level 2: 任务匹配时，加载完整 SKILL.md 内容
    ↓
Level 3: 需要时，读取 scripts/ 或 references/ 中的文件
```

**优势**：高效利用 Context Window，按需加载

### 1.6 官方示例 Skills

| Skill 名称 | 用途 |
|------------|------|
| `skill-creator` | 创建新的 Skill |
| `code-review` | 代码审查 |
| `commit` | 生成提交信息 |
| `create-pr` | 创建 Pull Request |
| `implementation-plan` | 复杂任务规划 |

---

## 2. Skills、MCP、Agent 三者详细介绍与对比

### 2.1 Agent（代理）

**定义**：能够自主理解任务、调用工具、生成结果的 AI 系统

**特点**：
- 是执行主体
- 具备推理和决策能力
- 可以使用工具完成任务

**类比**：一个具备专业技能的员工

### 2.2 MCP（Model Context Protocol）

**定义**：模型上下文协议，用于扩展 AI Agent 的工具调用能力

**特点**：
- 提供标准化的工具接口
- 允许 Agent 与外部系统交互
- 支持文件操作、浏览器控制、API 调用等
- 2025年12月捐赠给 Linux Foundation，成立 Agentic AI Foundation (AAIF)

**类比**：员工使用的工具箱

**MCP 示例**：
- `cursor-browser-extension`：浏览器控制
- `user-github`：GitHub 操作
- `user-filesystem`：文件系统操作

### 2.3 Skills（技能）

**定义**：预定义的知识包和行为模式，指导 Agent 执行特定任务

**特点**：
- 包含专业知识和操作步骤
- 可包含可执行脚本
- Agent 自主决定何时调用

**类比**：员工的专业培训手册

### 2.4 三者对比表

| 维度 | Agent | MCP | Skills |
|------|-------|-----|--------|
| **角色** | 执行者 | 能力扩展通道 | 知识/规则提供者 |
| **本质** | AI 系统 | 协议/接口 | 能力包 |
| **功能** | 理解、推理、执行 | 提供工具调用 | 提供专业知识 |
| **可执行代码** | 通过工具执行 | 提供执行接口 | ✅ 可包含脚本 |
| **自主性** | 高 | 被动响应 | 被动但可自动触发 |
| **跨平台** | 依赖平台 | 标准协议 | ✅ 开放标准 |

### 2.5 三者关系图

```
┌─────────────────────────────────────────────────────────┐
│                      Agent（执行者）                      │
│                                                         │
│  ┌─────────────────────┐    ┌─────────────────────────┐ │
│  │      Skills         │    │         MCP             │ │
│  │    (知识/规则)       │    │      (工具能力)          │ │
│  │                     │    │                         │ │
│  │  - 编码规范          │    │  - Shell 执行           │ │
│  │  - 代码审查流程       │    │  - 文件读写             │ │
│  │  - PR 创建规范       │    │  - 浏览器控制           │ │
│  │  - 测试策略          │    │  - GitHub 操作          │ │
│  │                     │    │  - 数据库查询           │ │
│  └─────────────────────┘    └─────────────────────────┘ │
│                                                         │
│  Agent 根据 Skills 中的知识，通过 MCP 提供的工具执行任务    │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Skills 与 Rules 的区别

### 3.1 核心区别

| 维度 | Rules（规则） | Skills（技能） |
|------|--------------|---------------|
| **来源** | Cursor IDE 原生 | Anthropic 开放标准 |
| **本质** | 静态指令文本 | 可执行能力包 |
| **功能** | 告诉 AI "应该怎么做" | 让 AI "能够做某些事" |
| **可执行代码** | ❌ 不支持 | ✅ 支持脚本 |
| **触发方式** | 文件匹配/手动/始终 | Agent 自主决定 |
| **跨平台** | 仅 Cursor | Claude.ai、Claude Code、Cursor 等 |
| **文件格式** | `.mdc` | `SKILL.md` |
| **存放位置** | `.cursor/rules/` | `.cursor/skills/` 或 `.claude/skills/` |

### 3.2 触发机制对比

#### Rules 的四种触发模式

| 模式 | 说明 |
|------|------|
| **Always** | 始终加载到每个请求 |
| **Auto Attached** | 当活动文件匹配 glob 时自动加载 |
| **Agent Requested** | Agent 根据描述决定是否读取 |
| **Manual** | 用户手动 @ 引用时才加载 |

#### Skills 的触发机制

```
1. Agent 预加载所有 Skills 的 name + description
2. 根据当前任务分析是否需要某个 Skill
3. 自主决定加载完整 Skill 内容
4. 按需读取 scripts/references 目录
```

### 3.3 使用场景建议

| 场景 | 推荐使用 |
|------|----------|
| 代码风格规范 | Rules |
| 文件命名约定 | Rules |
| 项目特定配置 | Rules |
| 复杂多步骤任务（如代码审查） | Skills |
| 需要执行脚本的任务 | Skills |
| 跨工具共享的能力 | Skills |

---

## 4. Rules 到 CLAUDE.md 的演变

### 4.1 演变历程

```
阶段 1: .cursorrules（单文件）
    ↓
阶段 2: .cursor/rules/*.mdc（目录结构，支持多种触发模式）
    ↓
阶段 3: CLAUDE.md + .cursor/rules/ + Skills（多层次协同）
```

### 4.2 CLAUDE.md 的角色

**定义**：项目根目录的顶层指令文件，作为项目的"宪法"

**特点**：
- 来源于 Claude Code CLI 工具
- 作为项目的单一真相源（Single Source of Truth）
- 跨工具兼容（Cursor、Claude Code、GitHub Copilot）
- 推荐大小：10-25KB

**内容建议**：
- 项目架构概述
- 核心技术栈
- 环境配置说明
- 关键 API 说明
- 常见问题修复记录（防止 AI 遗忘）

### 4.3 现状说明

**重要澄清**：Cursor 并没有完全废弃 `.cursor/rules/`，两者是共存关系：

| 文件 | 状态 | 用途 |
|------|------|------|
| `.cursor/rules/*.mdc` | ✅ 仍然支持 | Cursor 特定的精细化规则 |
| `CLAUDE.md` | ✅ 推荐使用 | 跨工具的项目级指令 |
| `.cursor/skills/` | ✅ 新增支持 | Agent Skills 存放位置 |

---

# 第二部分：全球工具支持情况

---

## 5. 全球 AI 编程工具 Skills 支持对比

### 5.1 支持 Anthropic Agent Skills 标准的工具

以下工具**原生支持** Anthropic Agent Skills 开放标准（`SKILL.md` 格式）：

| 工具名称 | 开发商 | Skills 支持 | MCP 支持 | 配置文件位置 | 备注 |
|----------|--------|-------------|----------|--------------|------|
| **Claude Code** | Anthropic | ✅ 完整支持 | ✅ 原生 | `.claude/skills/` | 官方 CLI 工具，Skills 发源地 |
| **Cursor** | Cursor Inc | ✅ 完整支持 | ✅ 原生 | `.cursor/skills/` | 最早采用 Skills 的 IDE |
| **VS Code + Copilot** | Microsoft/GitHub | ✅ 支持 (2026.1) | ✅ v1.101+ | `.vscode/skills/` | Insiders 版本已支持 |
| **Codex CLI** | OpenAI | ✅ 兼容格式 | ✅ 支持 | `.codex/skills/` | 复刻 Skills 架构 |
| **Goose** | Block | ✅ 支持 | ✅ 支持 | `.goose/skills/` | 开源 Agent 框架 |
| **Amp** | Sourcegraph | ✅ 支持 | ✅ 支持 | `.amp/skills/` | 代码搜索集成 |
| **OpenCode** | 社区 | ✅ 支持 | ✅ 支持 | `.opencode/skills/` | 开源实现 |
| **DeepAgent-CLI** | 社区 | ✅ 支持 | ✅ 支持 | 自定义 | 命令行工具 |

### 5.2 使用类似机制但格式不兼容的工具

以下工具有**类似 Skills 的功能**，但使用自己的配置格式：

| 工具名称 | 开发商 | 类似功能 | MCP 支持 | 配置文件 | 与 Skills 区别 |
|----------|--------|----------|----------|----------|----------------|
| **Trae** | 字节跳动 | `.rules` 配置 | ✅ v1.3.0+ | `.trae/rules/*.md` | 使用 Markdown，无 YAML frontmatter |
| **Windsurf** | Codeium | Cascade Flow | ⚠️ 部分 | 内置配置 | 专有系统，不可移植 |
| **Google Antigravity** | Google | Agent 系统 | ✅ 支持 | `.antigravity/` | 新兴工具，格式待定 |
| **JetBrains AI** | JetBrains | Junie Agent | ⚠️ ACP 协议 | IDE 内置 | 使用 ACP 而非 MCP |
| **Zed** | Zed Industries | Agentic Editing | ⚠️ ACP 协议 | 内置 | 主推 ACP 协议 |
| **Replit Agent** | Replit | 自治能力 | ❌ | 云端配置 | 云端闭环，无本地配置 |
| **Continue.dev** | Continue | MCP Blocks | ✅ 支持 | `continue.json` | 配置驱动而非 Skill 文件 |
| **Aider** | 社区 | CLI Agent | ⚠️ 开发中 | `.aider/` | 正在添加 MCP 支持 |

### 5.3 详细对比表：支持方式

| 工具 | Skills 格式 | 触发机制 | 脚本执行 | 渐进式加载 | 跨平台移植 |
|------|-------------|----------|----------|------------|------------|
| **Claude Code** | `SKILL.md` | Agent 自主 | ✅ | ✅ 三级 | ✅ |
| **Cursor** | `SKILL.md` | Agent 自主 | ✅ via Shell | ✅ 三级 | ✅ |
| **VS Code Copilot** | `SKILL.md` | Agent 自主 | ✅ | ✅ | ✅ |
| **Trae** | `.md` (无 frontmatter) | 语义注入 | ✅ via Agent | ❌ | ⚠️ 需转换 |
| **Windsurf** | 专有格式 | 自动上下文 | ✅ | ❌ | ❌ |
| **JetBrains AI** | IDE 配置 | 手动/自动 | ✅ | ❌ | ❌ |
| **Zed** | 内置 | Agent Panel | ✅ | ❌ | ⚠️ ACP |

### 5.4 详细对比表：协议支持

| 工具 | MCP 支持 | ACP 支持 | AgentHQ | Skills 标准 |
|------|----------|----------|---------|-------------|
| **Claude Code** | ✅ 原生 | ❌ | ❌ | ✅ 官方 |
| **Cursor** | ✅ 完整 | ❌ | ❌ | ✅ 早期采用 |
| **VS Code Copilot** | ✅ v1.101+ | ❌ | ✅ | ✅ 2026.1 |
| **Trae** | ✅ v1.3.0+ | ❌ | ❌ | ⚠️ 不兼容 |
| **Windsurf** | ⚠️ 部分 | ❌ | ❌ | ❌ |
| **JetBrains AI** | ⚠️ 计划中 | ✅ 联合开发 | ✅ 计划 | ⚠️ 计划中 |
| **Zed** | ⚠️ 通过 ACP | ✅ 主导 | ❌ | ⚠️ 通过 ACP |
| **Google Antigravity** | ✅ | ❌ | ❌ | ⚠️ 待定 |

### 5.5 协议说明

#### MCP (Model Context Protocol)
- **发起者**：Anthropic（2025年捐赠给 Linux Foundation）
- **功能**：工具调用标准，类似"AI 的 USB-C 接口"
- **采用者**：Anthropic、OpenAI、Microsoft、Google、AWS、Block

#### ACP (Agent Client Protocol)
- **发起者**：Zed Industries + JetBrains（2025年10月）
- **功能**：Agent 与 IDE 解耦的通信协议
- **特点**：类似 LSP（语言服务协议），允许任意 Agent 连接任意 IDE

#### AgentHQ
- **发起者**：GitHub/Microsoft
- **功能**：Agent 生态系统，允许多个专业 Agent 协同工作
- **支持**：在 GitHub Copilot 订阅内使用 Anthropic、OpenAI、Google 等多家 Agent

### 5.6 企业合作伙伴

Anthropic Agent Skills 开放标准的首批企业合作伙伴：

| 合作伙伴 | 领域 | 提供的 Skills |
|----------|------|---------------|
| **Atlassian** | 项目管理 | Jira 任务创建、Confluence 文档 |
| **Figma** | 设计工具 | 设计稿转代码、组件提取 |
| **Canva** | 设计工具 | 图像生成、文档设计 |
| **Stripe** | 支付 | 支付集成、账单管理 |
| **Notion** | 知识管理 | 文档创建、数据库操作 |
| **Zapier** | 自动化 | 工作流触发、应用集成 |
| **Cloudflare** | 云服务 | 部署配置、CDN 管理 |
| **Vercel** | 部署平台 | 项目部署、预览环境 |

### 5.7 生态数据（截至 2026 年 1 月）

| 指标 | 数据 |
|------|------|
| GitHub Stars (anthropics/skills) | 20,000+ |
| 社区 Skills 数量 | 77,000+ |
| Skills 市场平台 | SkillsMP.com、MCP Market 等 |
| 采用企业数量 | 500+ |

### 5.8 如何选择工具

#### 推荐使用原生支持 Skills 的工具

如果您需要**跨平台移植** Skills 或利用**开放生态**：

| 场景 | 推荐工具 |
|------|----------|
| 需要最完整的 Skills 支持 | Claude Code + Cursor |
| 已有 VS Code 习惯 | VS Code + GitHub Copilot |
| 命令行工作流 | Claude Code / Codex CLI |
| 开源优先 | Goose / OpenCode |

#### 使用类似功能但不兼容的工具

如果您使用 **Trae** 或其他不兼容工具，可以：

1. **手动转换格式**：将 `SKILL.md` 转换为工具特定格式
2. **维护两套配置**：
   - Cursor/Claude Code: `.cursor/skills/SKILL.md`
   - Trae: `.trae/rules/skill-name.md`
3. **使用 MCP 作为桥梁**：通过 MCP 协议实现工具间的能力共享

#### 格式转换示例

**Anthropic Skills 格式** → **Trae Rules 格式**

```markdown
# 原始 SKILL.md
---
name: code-review
description: 执行代码审查
---

# Code Review
## Instructions
1. 分析代码
2. 生成报告

# 转换为 Trae .rules 格式
# code-review.md (无 YAML frontmatter)

# 代码审查规则

当用户要求审查代码时，按以下步骤执行：

1. 分析代码
2. 生成报告
```

---

# 第三部分：实践指南

---

## 6. 在 Cursor 中的使用方法

### 6.1 目录结构总览

```
项目根目录/
├── CLAUDE.md                    # 项目级顶层指令
├── .cursor/
│   ├── rules/                   # Cursor 规则
│   │   ├── coding-style.mdc     # 编码风格
│   │   ├── vue-components.mdc   # Vue 组件规范
│   │   └── api-design.mdc       # API 设计规范
│   └── skills/                  # Agent Skills
│       ├── code-review/
│       │   └── SKILL.md
│       ├── create-pr/
│       │   └── SKILL.md
│       └── test-runner/
│           ├── SKILL.md
│           └── scripts/
│               └── run-tests.sh
└── src/
    └── ...
```

### 6.2 创建 Rules（.mdc 文件）

#### 文件格式

```markdown
---
description: 规则描述，用于 Agent Requested 模式
globs: ["src/**/*.vue", "src/**/*.ts"]  # 用于 Auto Attached 模式
alwaysApply: false  # 是否始终应用
---

# 规则标题

## 具体规范内容
...
```

#### 示例：Vue 组件规范

```markdown
---
description: Vue 3 组件开发规范
globs: ["src/**/*.vue"]
---

# Vue 组件开发规范

## 命名规范
- 组件文件名使用 PascalCase
- 组件名与文件名保持一致

## Composition API
- 优先使用 <script setup>
- ref 用于基本类型
- reactive 用于对象类型

## 组件结构
1. script setup
2. template
3. style scoped
```

### 6.3 创建 Skills（SKILL.md 文件）

#### 基础示例

```markdown
---
name: code-review
description: 执行代码审查，检查代码质量、安全性和最佳实践
---

# Code Review Skill

## Instructions

1. 分析代码变更的范围和影响
2. 检查以下方面：
   - 代码风格一致性
   - 潜在的 bug 和边界情况
   - 性能问题
   - 安全漏洞
   - 可读性和可维护性

3. 生成审查报告，包含：
   - 问题等级（Critical/Major/Minor/Suggestion）
   - 问题位置
   - 具体建议

## Guidelines

- 保持客观专业的语气
- 提供具体的改进建议而非模糊的批评
- 优先关注关键问题
```

#### 带脚本的示例

```markdown
---
name: test-runner
description: 运行项目测试并分析结果
---

# Test Runner Skill

## Instructions

1. 识别项目的测试框架
2. 执行 `scripts/run-tests.sh`
3. 解析测试结果
4. 生成测试报告

## Scripts

- `scripts/run-tests.sh`: 执行测试的脚本

## Guidelines

- 失败的测试优先处理
- 提供失败原因分析
```

### 6.4 创建 CLAUDE.md

在项目根目录创建：

```markdown
# Project: [项目名称]

## Overview
[项目简介]

## Tech Stack
- Frontend: [技术栈]
- Backend: [技术栈]
- Database: [数据库]

## Directory Structure
[目录说明]

## Development Guidelines
- See `.cursor/rules/` for detailed coding standards
- See `.cursor/skills/` for automated tasks

## Environment Setup
[环境配置说明]

## Common Issues & Solutions
[常见问题记录]
```

### 6.5 MCP 配置

MCP 服务器在 Cursor 设置中配置：

**路径**：`File > Preferences > Cursor Settings > MCP`

常用 MCP 服务器：
- `cursor-browser-extension`: 浏览器控制
- `user-github`: GitHub 操作
- `user-filesystem`: 文件系统（通常内置）

---

## 7. 工作流程与整合方案

### 7.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Cursor IDE                            │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                  Claude Agent                        │   │
│  │                                                      │   │
│  │   读取顺序：                                          │   │
│  │   1. CLAUDE.md (项目级上下文)                         │   │
│  │   2. .cursor/rules/ (规则约束)                       │   │
│  │   3. .cursor/skills/ (按需加载)                      │   │
│  │                                                      │   │
│  │   执行能力：                                          │   │
│  │   - MCP Tools (Shell, File, Browser, GitHub...)      │   │
│  │                                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 加载优先级

```
1. 系统提示 (Cursor 内置)
   ↓
2. User Rules (用户全局规则)
   ↓
3. CLAUDE.md (项目顶层指令)
   ↓
4. .cursor/rules/ 中的 Always 规则
   ↓
5. .cursor/rules/ 中的 Auto Attached 规则 (根据当前文件)
   ↓
6. .cursor/skills/ (Agent 按需加载)
   ↓
7. MCP Tools (执行时调用)
```

### 7.3 典型工作流程

#### 场景：开发一个新功能

```
用户请求: "帮我在 sci-z-web 中添加一个用户管理页面"

Agent 执行流程:
1. [读取 CLAUDE.md] 了解项目架构和技术栈
2. [读取 Rules] 加载 Vue 组件规范
3. [分析任务] 判断是否需要 Skills
4. [可能加载 Skill] 如果有 create-component skill
5. [使用 MCP] 通过 Shell 创建文件，通过 Read/Write 编辑代码
6. [遵循 Rules] 确保代码符合规范
7. [完成] 返回结果
```

#### 场景：代码审查

```
用户请求: "帮我审查这个 PR 的代码"

Agent 执行流程:
1. [读取 CLAUDE.md] 了解项目背景
2. [加载 code-review Skill] 获取审查流程
3. [使用 GitHub MCP] 获取 PR 变更
4. [按 Skill 指令] 执行审查步骤
5. [遵循 Rules] 参照编码规范检查
6. [生成报告] 输出审查结果
```

### 7.4 推荐的项目配置

```
project-root/
├── CLAUDE.md                        # 10-25KB，项目概述
├── .cursor/
│   ├── rules/
│   │   ├── general.mdc              # 通用规范 (Always)
│   │   ├── frontend.mdc             # 前端规范 (globs: ["*.vue", "*.ts"])
│   │   ├── backend.mdc              # 后端规范 (globs: ["*.java"])
│   │   └── testing.mdc              # 测试规范 (Agent Requested)
│   └── skills/
│       ├── code-review/
│       │   └── SKILL.md
│       ├── create-component/
│       │   └── SKILL.md
│       └── deploy/
│           ├── SKILL.md
│           └── scripts/
│               └── deploy.sh
├── sci-z-web/
│   └── ...
└── sci-z-server/
    └── .cursor/
        └── rules/
            └── notes-rules.mdc      # 后端专用规范
```

---

## 8. 常见问题与冲突处理

### 8.1 是否存在冲突？

**结论**：设计上不存在冲突，它们是互补关系

| 组件 | 作用层次 | 关系 |
|------|----------|------|
| CLAUDE.md | 顶层指导 | 提供全局上下文 |
| Rules | 约束层 | 定义必须遵守的规范 |
| Skills | 能力层 | 提供可复用的专业能力 |
| MCP | 执行层 | 提供工具调用能力 |

### 8.2 潜在问题与解决方案

#### 问题 1：规则冲突

**场景**：CLAUDE.md 和 Rules 中定义了矛盾的规范

**解决**：
- 保持 CLAUDE.md 简洁，只放高层次指导
- 具体规范放在 Rules 中
- 建立清晰的职责划分

#### 问题 2：Skill 与 Rules 重复

**场景**：Skill 中的指令与 Rules 有重叠

**解决**：
- Skill 专注于"做什么"和"怎么做"
- Rules 专注于"必须遵守什么"
- Skill 可以引用 Rules 文件

#### 问题 3：Context Window 超限

**场景**：加载太多内容导致上下文溢出

**解决**：
- CLAUDE.md 控制在 10-25KB
- Rules 使用 glob 精确匹配，避免全部加载
- Skills 依赖渐进式加载机制
- 单个 SKILL.md 控制在 500 行以内

### 8.3 最佳实践清单

- [ ] 项目根目录创建 `CLAUDE.md`
- [ ] 将通用规范放入 `.cursor/rules/` 并设置合适的触发模式
- [ ] 复杂任务创建 Skills 而非冗长的 Rules
- [ ] 定期清理不再使用的 Rules 和 Skills
- [ ] 使用 `/rules` 命令查看当前加载的规则
- [ ] 在 PR 中包含 Rules/Skills 的更新说明

### 8.4 当前支持状态

| 功能 | Cursor 支持 | 说明 |
|------|-------------|------|
| `.cursor/rules/*.mdc` | ✅ 完整支持 | Cursor 原生功能 |
| `CLAUDE.md` | ✅ 支持 | 作为项目级上下文 |
| `.cursor/skills/SKILL.md` | ✅ 支持 | Agent 可自主调用 |
| Skills 脚本执行 | ⚠️ 部分支持 | 通过 Shell MCP 间接执行 |
| MCP 工具 | ✅ 完整支持 | 需要配置 |

---

# 第四部分：进阶内容

---

## 9. 如何定义 Skill 内容（官方规范详解）

### 9.1 SKILL.md 文件结构详解

根据 Anthropic 官方仓库 https://github.com/anthropics/skills 的规范，每个 Skill 必须包含以下结构：

#### 必需部分

```markdown
---
name: skill-name
description: 技能描述（Claude 根据此描述决定是否调用）
---

# 技能标题

## Instructions
详细的执行指令（使用祈使语气）
```

#### 可选部分

```markdown
## Examples
具体的输入输出示例

## Guidelines
特殊规则和边界情况

## References
引用的参考文档路径

## Scripts
可执行脚本说明
```

### 9.2 官方规范要点

| 规范项 | 要求 |
|--------|------|
| **name 字段** | 1-64字符，小写字母+数字+连字符，如 `code-review` |
| **description 字段** | 最多1024字符，清晰描述触发场景 |
| **SKILL.md 行数** | 建议不超过500行，超长内容放入 references/ |
| **祈使语气** | Instructions 使用命令式语句，如 "分析..."、"检查..." |
| **目录结构** | 保持扁平，scripts/、references/、assets/ 按需创建 |

### 9.3 渐进式加载设计原则

```
编写 Skill 时应考虑三级加载：

Level 1 (预加载): description 必须精准概括能力，便于 Agent 判断
Level 2 (主体加载): Instructions 写核心步骤，控制在合理长度
Level 3 (按需加载): 复杂内容放入 references/，在 Instructions 中引用
```

---

## 10. Sci-Z-Platform-College 项目 Skills 设计

### 10.1 项目分析

#### 项目概述

| 项目 | 技术栈 | 主要功能 |
|------|--------|----------|
| **sci-z-web** | Vue 3 + Element Plus + Vite + Pinia | 前端 SPA 应用 |
| **sci-z-server** | Spring Boot 3.2 + MyBatis Plus + Java 21 | 后端 DDD 架构 |

#### 前端架构特点

- **目录结构**：views/ (路由入口) + components/Business/ (业务组件) + components/Common/ (通用组件)
- **状态管理**：Pinia (useAuthStore, useIndustryStore 等)
- **API 封装**：按模块组织 (api/User/, api/Project/ 等)
- **国际化**：vue-i18n 支持 zh-CN/en-US/ja-JP/ko-KR

#### 后端架构特点

- **DDD 分层**：interfaces → application → domain ← infrastructure
- **命名规范**：严格的文件命名和方法命名规范
- **Java 21**：强制使用 Record、Pattern Matching、Text Blocks 等新特性
- **注释模板**：统一的类注释、方法注释、字段注释格式

### 10.2 为项目设计的 Skills

以下是针对 Sci-Z-Platform-College 项目设计的 5 个核心 Skills：

---

#### Skill 1: create-vue-page（创建 Vue 页面）

**适用场景**：需要创建新的前端页面时

```markdown
---
name: create-vue-page
description: 在 sci-z-web 项目中创建新的 Vue 页面，包含路由配置、业务组件、API 调用和国际化支持。当用户要求创建新页面、新功能页面、新模块页面时触发。
---

# 创建 Vue 页面

## Instructions

1. **确认需求**
   - 询问页面所属模块（如 Project、Declaration、User 等）
   - 确认页面类型（列表页 List / 详情页 Detail / 表单页 Form）
   - 了解主要数据字段和交互需求

2. **创建目录结构**
   - views/<Module>/<PageName>.vue（路由入口，极薄包装）
   - components/Business/<Module>/<PageName>.vue（主业务组件）
   - api/<Module>/<module>.js（API 封装，如已有则复用）
   - 更新 locales/ 下四种语言文件

3. **编写业务组件**
   - 使用 <script setup> + Composition API
   - 复用 components/Common/ 中的基础组件
   - 接口调用通过 api/<Module> 导入
   - 状态管理使用 Pinia store
   - 样式使用 lang="scss" scoped

4. **配置路由**
   - 在 router/index.js 中添加路由配置
   - 使用路由懒加载
   - 配置权限验证（如需要）

## Guidelines

- views/ 只做包装，业务逻辑全部在 components/Business/ 中
- 优先复用 Common 组件，不要重复造轮子
- 接口错误必须使用 ElMessage.error() 提示
- 参考 相关文档/提示词模板/ 中对应模块的开发提示词
```

---

#### Skill 2: create-java-module（创建 Java 模块）

**适用场景**：需要创建新的后端功能模块时

```markdown
---
name: create-java-module
description: 在 sci-z-server 项目中按照 DDD 架构创建完整的 Java 模块，包含 Entity、Mapper、Repository、Service、Controller 全套代码。当用户要求创建新接口、新模块、新实体时触发。
---

# 创建 Java 模块

## Instructions

1. **确认需求**
   - 确认模块名称（如 Knowledge, Literature, Report 等）
   - 确认数据库表结构和字段
   - 确认需要的 CRUD 操作

2. **按 DDD 分层创建文件**（必须按此顺序）
   - Entity (domain/pojo/entity/<module>/)
   - Mapper (domain/pojo/mapper/<module>/)
   - Repository (domain/pojo/repository/<module>/)
   - DTO (domain/pojo/dto/request/ 和 response/)
   - Converter (interfaces/converter/)
   - Service (application/service/<module>/)
   - Controller (interfaces/controller/)

3. **代码规范要求**
   - Java 21 特性：Record、Text Blocks、var、Stream API、Optional
   - 注释规范：@author JiaWen.Wu、@className、@date
   - 日志规范：log.info(String.format(...))
   - 异常规范：throw new BusinessException(ResultCode.XXX)

## Guidelines

- 严格遵循 DDD 分层，禁止跨层调用
- Repository 只做数据访问，禁止业务逻辑
- 禁止使用全限定类名，必须使用 import
- Lambda 参数使用语义化名称，禁止单字母
```

---

#### Skill 3: code-review-sciz（代码审查）

**适用场景**：需要审查代码质量时

```markdown
---
name: code-review-sciz
description: 对 Sci-Z-Platform-College 项目代码进行审查，检查是否符合项目规范、Java 21 最佳实践、Vue 3 规范和安全性要求。当用户要求审查代码、检查代码、review 代码时触发。
---

# Sci-Z 代码审查

## Instructions

1. **识别代码类型**
   - 前端代码（.vue, .js）→ 使用前端审查标准
   - 后端代码（.java）→ 使用后端审查标准

2. **前端审查清单**
   - 架构：views/ 是否只做路由包装
   - Composition API：是否使用 <script setup>
   - 国际化：所有文案是否使用 $t()
   - 错误处理：异步操作是否有 try/catch

3. **后端审查清单**
   - Java 21：是否使用 java.time、Record、Pattern Matching
   - DDD 分层：是否遵循依赖方向
   - 命名规范：方法命名、Lambda 参数
   - 异常处理：是否只抛 BusinessException

4. **生成审查报告**
   - 🔴 Critical：必须修复
   - 🟡 Major：建议修复
   - 🔵 Minor：可选优化
   - ✅ Good Practices：亮点

## Guidelines

- 审查时参考 sci-z-web/CLAUDE.md 和 sci-z-server/CLAUDE.md
- 问题必须指明具体文件和行号
- Critical 问题必须阻止代码合并
```

---

#### Skill 4: api-integration（前后端接口对接）

**适用场景**：需要进行前后端 API 对接时

```markdown
---
name: api-integration
description: 在 Sci-Z 项目中进行前后端 API 对接，包括后端接口定义、前端 API 封装、请求响应类型定义和错误处理。当用户要求对接接口、联调、前后端联调时触发。
---

# 前后端 API 对接

## Instructions

1. **后端接口确认**
   - 检查 Controller 是否已定义接口
   - 确认请求方法、路径、参数、返回值

2. **前端 API 封装**
   - 在 api/<Module>/ 中创建或更新
   - 添加 JSDoc 注释
   - 使用 request 工具函数

3. **在业务组件中调用**
   - 添加 loading 状态
   - 处理成功响应（code === 200）
   - 处理错误响应（ElMessage.error）
   - 处理网络错误（catch）

4. **响应结构**
   - 统一格式：{ code, message, data }
   - 分页格式：{ records, total, pageNo, pageSize }

## Guidelines

- API 方法必须添加 JSDoc 注释
- 所有请求必须有 loading 状态
- 错误提示必须用户友好
```

---

#### Skill 5: debug-issue（问题诊断）

**适用场景**：需要排查和修复问题时

```markdown
---
name: debug-issue
description: 诊断和修复 Sci-Z-Platform-College 项目中的 bug，包括前端报错、后端异常、接口问题等。当用户报告 bug、错误、异常、问题需要排查时触发。
---

# 问题诊断与修复

## Instructions

1. **收集问题信息**
   - 错误现象描述
   - 错误信息/堆栈
   - 复现步骤
   - 相关代码位置

2. **前端问题诊断**
   - 检查浏览器控制台错误
   - 检查网络请求状态
   - 检查 Vue Devtools 组件状态
   - 检查 Pinia store 数据

3. **后端问题诊断**
   - 查看服务器日志
   - 检查请求参数
   - 跟踪代码执行流程
   - 检查数据库数据

4. **修复并验证**
   - 定位问题根因
   - 编写修复代码
   - 本地验证修复
   - 检查是否引入新问题

5. **输出诊断报告**
   - 问题描述
   - 根因分析
   - 修复方案
   - 验证方法
   - 预防措施

## Guidelines

- 先复现问题，再定位原因
- 修复时遵循项目规范
- 记录问题和解决方案，防止重复
```

---

### 10.3 Skills 目录结构建议

```
Sci-Z-Platform-College/
├── .cursor/
│   └── skills/                           # 项目级 Skills
│       ├── create-vue-page/
│       │   └── SKILL.md
│       ├── create-java-module/
│       │   └── SKILL.md
│       ├── code-review-sciz/
│       │   └── SKILL.md
│       ├── api-integration/
│       │   └── SKILL.md
│       └── debug-issue/
│           └── SKILL.md
├── sci-z-web/
│   ├── CLAUDE.md                         # 前端项目规范（已存在）
│   └── .cursor/rules/
└── sci-z-server/
    ├── CLAUDE.md                         # 后端项目规范（已存在）
    └── .cursor/rules/
```

### 10.4 如何创建和使用这些 Skills

#### 创建步骤

1. **创建目录**：在项目根目录创建 `.cursor/skills/` 目录
2. **创建 Skill 子目录**：每个 Skill 一个目录，如 `create-vue-page/`
3. **编写 SKILL.md**：按照上述模板编写 Skill 内容
4. **（可选）添加脚本**：如需要自动化脚本，放入 `scripts/` 子目录

#### 使用方式

在 Cursor 中与 Agent 对话时：
- Agent 会自动根据任务匹配合适的 Skill
- 也可以手动提示，如 "使用 create-vue-page skill 帮我创建..."
- Skill 加载后，Agent 会按照 Instructions 执行

---

# 附录

---

## 附录 A：快速参考卡片

### 创建 Rule 模板

```markdown
---
description: [规则描述]
globs: ["**/*.ext"]
alwaysApply: false
---

# [规则名称]

[规则内容]
```

### 创建 Skill 模板

```markdown
---
name: [skill-name]
description: [技能描述，Claude 用此判断是否调用]
---

# [Skill 名称]

## Instructions
[执行指令]

## Examples
[示例]

## Guidelines
[注意事项]
```

### 常用命令

| 命令 | 说明 |
|------|------|
| `/rules` | 查看当前加载的规则 |
| `/mcp list` | 查看可用的 MCP 服务器 |
| `Shift+Tab` | 切换到 Plan 模式 |

---

## 附录 B：参考资源

### 官方资源

| 资源 | 链接 |
|------|------|
| Anthropic Skills 官方仓库 | https://github.com/anthropics/skills |
| Anthropic Skills 文档 | https://docs.anthropic.com/en/docs/agents-and-tools/agent-skills/overview |
| Claude Code Skills 文档 | https://code.claude.com/docs/en/skills |
| MCP 协议规范 | https://modelcontextprotocol.io |
| Cursor 规则指南 | https://cursor.com/blog/agent-best-practices |

### 社区资源

| 资源 | 链接 |
|------|------|
| Skills 市场 | https://skillsmp.com |
| MCP Market | https://mcpmarket.com |
| Awesome Agent Skills | https://github.com/skillmatic-ai/awesome-agent-skills |

### 协议组织

| 组织 | 说明 |
|------|------|
| Agentic AI Foundation (AAIF) | MCP 协议管理组织，由 Anthropic、OpenAI、Microsoft、Google、AWS、Block 共同成立 |
| ACP Working Group | Agent Client Protocol 工作组，由 Zed 和 JetBrains 主导 |

---

## 附录 C：Skill 编写检查清单

在创建新 Skill 时，确保满足以下要求：

- [ ] `name` 字段符合规范（小写、连字符，1-64字符）
- [ ] `description` 清晰描述触发场景（最多1024字符）
- [ ] `Instructions` 使用祈使语气
- [ ] 步骤编号清晰，有逻辑顺序
- [ ] 包含具体的 `Examples`
- [ ] `Guidelines` 说明边界情况
- [ ] 总行数不超过 500 行
- [ ] 引用的文件路径正确
- [ ] 复杂内容已拆分到 `references/` 目录

---

*文档版本：1.2*
*最后更新：2026年1月21日*
*维护：Jiawen.Wu*

**更新日志**：
- v1.2: 新增第5章（全球工具支持对比）、重新组织目录结构
- v1.1: 新增第9章（Skill编写规范）、第10章（项目专属Skills设计）、附录C
- v1.0: 初始版本
