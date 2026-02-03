# Cursor IDE 功能设置指南

> 本指南说明如何在 Cursor IDE 中设置和使用 Rules、Skills、Subagents、Commands、Hooks、Indexing & Docs 六项功能，以满足不同工作场景的需求。

---

## 📋 目录

1. [设置入口说明](#设置入口说明)
2. [快速概览](#快速概览)
3. [CLAUDE.md 配置说明](#claudemd-配置说明)
4. [Rules（规则）](#1-rules规则)
5. [Skills（技能）](#2-skills技能)
6. [Subagents（子代理）](#3-subagents子代理)
7. [Commands（命令）](#4-commands命令)
8. [Hooks（钩子）](#5-hooks钩子)
9. [Indexing & Docs（索引与文档）](#6-indexing--docs索引与文档)
10. [场景化配置建议](#场景化配置建议)
11. [项目特定配置](#项目特定配置)
12. [配置关系与优先级](#配置关系与优先级)

---

## 设置入口说明

### Tools & MCP 按钮的作用

在 Cursor Settings 中，**`Tools & MCP`** 按钮（左侧导航栏中带扳手图标的按钮）是访问所有 Agent 配置功能的统一入口。

### 六项设置的关联关系

这六项设置（Rules、Skills、Subagents、Commands、Hooks、Indexing & Docs）都位于 **`Tools & MCP`** 主类别下，它们共同构成了 Cursor Agent 的完整配置体系：

```
Tools & MCP (主入口)
├── Rules, Skills, Subagents (子入口)
│   ├── Rules (规则)          ← 系统级指令和编码规范
│   ├── Skills (技能)         ← 扩展 Agent 的专业能力
│   └── Subagents (子代理)    ← 专业化的 AI 助手
├── Commands (命令)            ← 快速执行常用工作流
├── Hooks (钩子)               ← 处理 Agent 输出的结构化文件
├── Indexing & Docs (索引与文档) ← 代码库索引与自定义文档
└── Network                    ← 网络配置
```

### 访问路径

**统一访问路径**：

1. 打开 Cursor Settings：
   - Mac: `Cmd + Shift + J`
   - Windows/Linux: `Ctrl + Shift + J`
2. 点击左侧导航栏的 **`Tools & MCP`** 按钮
3. 在左侧子项中选择要配置的入口：
   - **Rules、Skills、Subagents**：选择 **`Rules, Skills, Subagents`**，在右侧管理规则、技能、子代理
   - **Commands**：选择 **`Commands`**，管理命令
   - **Hooks**：选择 **`Hooks`**，管理钩子及查看执行日志
   - **Indexing & Docs**：选择 **`Indexing & Docs`**，管理代码库索引与自定义文档

### 六项设置的整合关系

| 设置项              | 在界面中的位置                                                | 配置方式            | 作用范围       |
| ------------------- | ------------------------------------------------------------- | ------------------- | -------------- |
| **Rules**           | `Tools & MCP` → `Rules, Skills, Subagents` → `Rules` 部分     | 设置界面 + 文件系统 | 项目/用户/团队 |
| **Skills**          | `Tools & MCP` → `Rules, Skills, Subagents` → `Skills` 部分    | 文件系统 + 远程导入 | 项目/用户      |
| **Subagents**       | `Tools & MCP` → `Rules, Skills, Subagents` → `Subagents` 部分 | 文件系统            | 项目/用户      |
| **Commands**        | `Tools & MCP` → `Commands`（独立子项）                        | 文件系统            | 项目/用户/团队 |
| **Hooks**           | `Tools & MCP` → `Hooks`（独立子项）                           | 设置界面 + 文件系统 | 项目/用户      |
| **Indexing & Docs** | `Tools & MCP` → `Indexing & Docs`（独立子项）                 | 设置界面            | 项目           |

### 为什么需要整合？

这六项设置虽然功能不同，但都服务于同一个目标：**增强和定制 Cursor Agent 的能力**。它们可以协同工作：

- **Rules** 提供基础规范 → **Skills** 扩展专业能力 → **Subagents** 处理复杂任务 → **Commands** 快速执行工作流
- **Indexing & Docs** 为上述能力提供代码库理解和自定义文档上下文
- **Hooks** 在 Agent/Subagent 生成结构化输出时，统一处理并保存结果
- 例如：使用 `/code-review` 命令 → 触发代码审查流程 → Agent 应用 Rules 规范 → 调用 `code-reviewer` Subagent → 使用相关 Skills；若子代理生成报告文件，可由 Hooks 统一保存

### 设置建议

**推荐配置顺序**：

1. ✅ 先开启 **Indexing & Docs**（代码库索引，提升 AI 对项目的理解）
2. ✅ 再设置 **Rules**（基础规范）
3. ✅ 再创建 **Commands**（提高效率）
4. ✅ 根据需要添加 **Skills**（扩展能力）
5. ✅ 再创建 **Subagents**（处理复杂任务）
6. ✅ 若子代理或命令会生成结构化文件，再配置 **Hooks**（统一保存与后处理）

---

## CLAUDE.md 配置说明

### 什么是 CLAUDE.md

`CLAUDE.md` 是项目根目录的顶层指令文件，作为项目的"宪法"，提供项目架构概述、技术栈、环境配置等全局上下文。

### CLAUDE.md 与 Rules 的关系

| 文件                | 作用                              | 优先级     | 适用场景                                          |
| ------------------- | --------------------------------- | ---------- | ------------------------------------------------- |
| **CLAUDE.md**       | 项目顶层指导，提供全局上下文      | 高         | 跨工具兼容（Cursor、Claude Code、GitHub Copilot） |
| **Rules**           | 具体规范约束，定义必须遵守的规则  | 中         | Cursor 特定的精细化规则                           |
| **Skills**          | 专业能力扩展，提供可复用的能力    | 低（按需） | Agent 自主调用                                    |
| **Subagents**       | 专业化 AI 助手                    | 低（按需） | 复杂任务分解                                      |
| **Commands**        | 快速执行工作流                    | 低（手动） | 通过 `/` 前缀调用                                 |
| **Indexing & Docs** | 代码库索引与自定义文档            | 底层上下文 | 提升 AI 对代码和项目文档的理解                    |
| **Hooks**           | 统一处理并保存 Agent 的结构化输出 | 输出时触发 | 子代理报告、生成文件的后处理与落盘                |

### 如何配置 CLAUDE.md

#### 位置

- **项目根目录**：`CLAUDE.md`（推荐）
- **子项目目录**：`sci-z-web/CLAUDE.md`、`sci-z-server/CLAUDE.md`（项目特定）

#### 内容建议

```markdown
# 项目名称

## 项目简介

简要描述项目的用途和目标。

## 技术栈

- 前端：Vue 3、TypeScript、Element Plus
- 后端：Java 21、Spring Boot、MyBatis Plus

## 项目结构
```

project/
├── sci-z-web/ # 前端项目
└── sci-z-server/ # 后端项目

```

## 开发规范
详细规范请参考：
- 前端：`sci-z-web/CLAUDE.md`
- 后端：`sci-z-server/CLAUDE.md`

## 快速开始
1. 前端：`cd sci-z-web && npm install && npm run dev`
2. 后端：`cd sci-z-server && mvn spring-boot:run`
```

#### 当前项目状态

你的项目中已有：

- ✅ `sci-z-web/CLAUDE.md` - 前端项目顶层指令
- ✅ `sci-z-server/CLAUDE.md` - 后端项目顶层指令

这些文件已经包含了详细的开发规范，Cursor 会自动识别并应用。

### CLAUDE.md 与 Rules 的配合使用

**推荐配置方式**：

1. **CLAUDE.md** 提供高层次指导：

   - 项目架构概述
   - 核心技术栈
   - 环境配置说明

2. **Rules** 提供具体规范：

   - 代码风格规范
   - 文件命名约定
   - 项目特定配置

3. **两者互补**：
   - CLAUDE.md 告诉 AI "项目是什么"
   - Rules 告诉 AI "应该怎么做"

**示例**：

```markdown
# CLAUDE.md（项目根目录）

## 技术栈

- 前端：Vue 3 + TypeScript

# .cursor/rules/vue-frontend.mdc

## 编码规范

- 使用 Composition API
- 组件使用 PascalCase 命名
```

### 在 Cursor 中启用 CLAUDE.md

1. **自动识别**：Cursor 2.0+ 会自动识别项目根目录和子项目目录的 `CLAUDE.md`
2. **设置验证**：
   - 打开 `Cursor Settings` → `Rules`
   - 确认 "Include CLAUDE.md in context" 开关已启用（如果存在）
3. **优先级**：CLAUDE.md 会在 Rules 之前加载，提供全局上下文

---

## 快速概览

| 功能                | 用途                              | 适用场景                       | 优先级     |
| ------------------- | --------------------------------- | ------------------------------ | ---------- |
| **Rules**           | 提供系统级指令和编码规范          | 代码风格、架构规范、团队标准   | ⭐⭐⭐⭐⭐ |
| **Skills**          | 扩展 Agent 的专业能力             | 特定领域知识、可复用工作流     | ⭐⭐⭐⭐   |
| **Subagents**       | 专业化的 AI 助手                  | 复杂任务分解、并行工作         | ⭐⭐⭐     |
| **Commands**        | 快速执行常用工作流                | 代码审查、测试、部署等         | ⭐⭐⭐⭐   |
| **Hooks**           | 统一处理并保存 Agent 的结构化输出 | 子代理报告、生成文件的后处理   | ⭐⭐⭐     |
| **Indexing & Docs** | 代码库索引与自定义文档            | 提升 AI 对代码和项目文档的理解 | ⭐⭐⭐⭐⭐ |

---

## 1. Rules（规则）

### 1.1 什么是 Rules

Rules 为 Agent 提供系统级指令，用于指导代码生成、代码审查和开发工作流。它们将提示词、脚本等内容打包在一起，便于在团队内管理和共享。

### 1.2 规则类型

Cursor 支持四种类型的规则：

| 类型          | 位置             | 作用范围     | 版本控制      |
| ------------- | ---------------- | ------------ | ------------- |
| **项目规则**  | `.cursor/rules/` | 仅当前项目   | ✅ 是         |
| **用户规则**  | Cursor Settings  | 全局生效     | ❌ 否         |
| **团队规则**  | Cursor Dashboard | 团队所有项目 | ✅ 是（云端） |
| **AGENTS.md** | 项目根目录       | 仅当前项目   | ✅ 是         |

### 1.3 如何在 Cursor IDE 中设置

#### 方法一：通过设置界面创建

1. **打开设置**：

   - Mac: `Cmd + Shift + J`
   - Windows/Linux: `Ctrl + Shift + J`
   - 或点击左下角齿轮图标 → `Settings`

2. **导航到规则设置**：

   - 左侧导航栏 → `Tools & MCP` → `Rules, Skills, Subagents`
   - 或直接搜索 "Rules"

3. **创建项目规则**：

   - 点击 `Project Rules` 旁的 `+ New` 按钮
   - 输入规则名称（如 `vue-frontend-rules.md`）
   - 选择规则类型：
     - `Always Apply` - 每个聊天会话都应用
     - `Apply Intelligently` - Agent 根据描述判断是否应用
     - `Apply to Specific Files` - 匹配特定文件模式时应用
     - `Apply Manually` - 通过 `@rule-name` 手动调用

4. **编写规则内容**：

   ```markdown
   ---
   description: "Vue 3 前端开发规范"
   alwaysApply: false
   globs:
     - "**/*.vue"
     - "**/*.js"
   ---

   # Vue 3 前端开发规范

   ## 编码要求

   - 使用 Composition API
   - 使用 TypeScript
   - 遵循项目架构规范
   ```

#### 方法二：手动创建文件

1. **创建规则目录**（如果不存在）：

   ```bash
   mkdir -p .cursor/rules
   ```

2. **创建规则文件**：

   ```bash
   # 项目根目录或子项目目录
   touch .cursor/rules/vue-frontend-rules.mdc
   touch sci-z-web/.cursor/rules/vue-frontend-rules.mdc
   touch sci-z-server/.cursor/rules/java-backend-rules.mdc
   ```

3. **编写规则内容**（参考现有规则）：
   - 查看 `sci-z-web/.cursor/rules/current-project-rules.mdc`
   - 查看 `sci-z-server/.cursor/rules/current-project-rules.mdc`

### 1.4 规则文件格式

```markdown
---
description: "规则描述，Agent 用此判断是否应用"
alwaysApply: false # true: 始终应用, false: 智能应用
globs: # 文件匹配模式（可选）
  - "**/*.vue"
  - "**/*.ts"
---

# 规则标题

## 规则内容

- 规则 1
- 规则 2
```

### 1.5 项目当前规则状态

根据项目结构，你已经有了以下规则：

- ✅ `sci-z-web/.cursor/rules/current-project-rules.mdc` - Vue 前端规则
- ✅ `sci-z-server/.cursor/rules/current-project-rules.mdc` - Java 后端规则
- ✅ `sci-z-server/.cursor/rules/common-rules.mdc` - 通用规则
- ✅ `sci-z-server/.cursor/rules/notes-rules.mdc` - 注释规则

### 1.6 建议的规则结构

```
项目根目录/
├── .cursor/
│   └── rules/
│       ├── project-overview.mdc      # 项目总览规则
│       └── common-standards.mdc     # 通用标准
├── sci-z-web/
│   └── .cursor/
│       └── rules/
│           ├── vue-frontend.mdc      # Vue 前端规则（已有）
│           ├── component-guidelines.mdc  # 组件规范
│           └── api-integration.mdc  # API 集成规范
└── sci-z-server/
    └── .cursor/
        └── rules/
            ├── java-backend.mdc      # Java 后端规则（已有）
            ├── ddd-architecture.mdc  # DDD 架构规则
            └── database-rules.mdc     # 数据库规则
```

---

## 2. Skills（技能）

### 2.1 什么是 Skills

Skills 是可移植、受版本控制的包，用于让 Agent 学会如何执行特定领域的任务。技能既可以包含说明性指令，也可以包含 Agent 可运行的脚本或代码。

### 2.2 Skills 的特点

- ✅ **可移植**：适用于任何支持 Agent Skills 标准的 Agent
- ✅ **受版本控制**：以文件形式存储，可在代码仓库中追踪变更
- ✅ **可执行**：可以包含脚本和代码，由 Agent 执行
- ✅ **渐进式加载**：按需加载资源，使上下文使用更加高效

### 2.3 如何在 Cursor IDE 中设置

#### 方法一：通过设置界面启用

1. **打开设置**：

   - `Cmd/Ctrl + Shift + J` → `Rules` → `Import Settings`

2. **启用 Agent Skills**：
   - 找到 `Agent Skills` 开关
   - 切换为开启状态

#### 方法二：手动创建 Skills

1. **创建技能目录**：

   ```bash
   # 项目级技能（推荐）
   mkdir -p .cursor/skills

   # 或用户级技能（全局）
   mkdir -p ~/.cursor/skills
   ```

2. **创建技能文件夹和文件**：

   ```bash
   mkdir -p .cursor/skills/ui-ux-pro-max
   touch .cursor/skills/ui-ux-pro-max/SKILL.md
   ```

3. **编写 SKILL.md**：

   ```markdown
   ---
   name: ui-ux-pro-max
   description: "UI/UX 设计智能。用于设计新组件、选择调色板、审查 UX 问题。"
   ---

   # UI/UX Pro Max

   ## 使用时机

   - 设计新的 UI 组件或页面
   - 选择颜色和排版
   - 审查代码中的 UX 问题

   ## 指令

   - 遵循无障碍性最佳实践
   - 使用响应式设计
   - 确保触摸目标至少 44x44px
   ```

### 2.4 Skills 目录结构

```
.cursor/skills/
└── skill-name/
    ├── SKILL.md          # 必需：技能定义文件
    ├── scripts/          # 可选：可执行脚本
    │   ├── deploy.sh
    │   └── validate.py
    ├── references/       # 可选：参考文档
    │   └── style-guide.md
    └── assets/           # 可选：模板和配置
        └── template.json
```

### 2.5 项目建议的 Skills

基于你的项目特点，建议创建以下 Skills：

```
.cursor/skills/
├── vue-component-generator/    # Vue 组件生成器
│   └── SKILL.md
├── java-ddd-generator/         # Java DDD 代码生成器
│   └── SKILL.md
├── api-integration-helper/     # API 集成助手
│   └── SKILL.md
└── code-review-assistant/      # 代码审查助手
    └── SKILL.md
```

### 2.6 从 SkillsMP 安装 Skills

1. **访问 SkillsMP 平台**：

   - 打开浏览器访问：https://skillsmp.com/
   - 浏览可用的 Skills 库

2. **安装 Skills**：

   - 在 SkillsMP 上找到需要的 Skill
   - 复制 Skill 的 GitHub 仓库 URL 或安装命令
   - 在 Cursor Settings 中添加：
     - `Cursor Settings` → `Rules` → `Project Rules`
     - 点击 `+ Add Rule` → `Remote Rule (GitHub)`
     - 粘贴从 SkillsMP 获取的仓库 URL

3. **自动同步**：
   - 导入的 Skills 会与源仓库保持同步
   - 远程 Skills 的更新会自动体现在项目中

---

## 3. Subagents（子代理）

### 3.1 什么是 Subagents

Subagents 是 Cursor 主代理可以将任务委派给的专业化 AI 助手。每个子代理都在自己的上下文窗口中运行，处理特定类型的工作，并将结果返回给父代理。

### 3.2 Subagents 的特点

- ✅ **上下文隔离**：每个子代理都有自己的上下文窗口
- ✅ **并行执行**：可同时启动多个子代理
- ✅ **专门领域能力**：通过自定义提示词配置特定领域的任务
- ✅ **可复用**：定义自定义子代理，并在多个项目之间复用

### 3.3 如何在 Cursor IDE 中设置

#### 方法一：让 Agent 帮你创建

在 Agent 聊天中输入：

```
在 .cursor/agents/verifier.md 创建一个子代理文件，文件开头使用 YAML frontmatter（name、description），后面紧跟提示内容。verifier 子代理应负责验证已完成的工作、检查实现是否可用、运行测试，并报告通过的部分和未完成的部分。
```

#### 方法二：手动创建

1. **创建子代理目录**：

   ```bash
   # 项目级子代理
   mkdir -p .cursor/agents

   # 或用户级子代理（全局）
   mkdir -p ~/.cursor/agents
   ```

2. **创建子代理文件**：

   ```bash
   touch .cursor/agents/verifier.md
   touch .cursor/agents/debugger.md
   touch .cursor/agents/test-runner.md
   ```

3. **编写子代理内容**：

   ```markdown
   ---
   name: verifier
   description: "验证已完成的工作。在任务标记为完成后使用，以确认实现功能正常。"
   model: fast
   ---

   你是一个严格的验证者。你的工作是验证声称已完成的工作是否真正可用。

   调用时:

   1. 识别声称已完成的内容
   2. 检查实现是否存在且功能正常
   3. 运行相关测试或验证步骤
   4. 查找可能遗漏的边界情况

   要彻底且保持怀疑态度。报告:

   - 已验证并通过的内容
   - 声称完成但不完整或损坏的内容
   - 需要解决的具体问题
   ```

### 3.4 子代理文件格式

```markdown
---
name: subagent-name # 唯一标识符
description: "何时使用此子代理" # Agent 用此判断是否委派
model: inherit # fast/inherit/具体模型ID
readonly: false # true: 只读模式
is_background: false # true: 后台运行
---

# 子代理提示词

详细的指令和指导...
```

### 3.5 项目建议的 Subagents

基于你的项目特点，建议创建以下 Subagents：

```
.cursor/agents/
├── verifier.md          # 验证代理：验证已完成的工作
├── debugger.md          # 调试代理：错误和测试失败的调试专家
├── test-runner.md       # 测试运行工具：主动运行测试并修复失败的测试
├── code-reviewer.md     # 代码审查代理：审查代码质量和规范
└── api-tester.md        # API 测试代理：测试 API 接口
```

### 3.6 使用子代理

#### 自动委派

Agent 会根据任务复杂度自动委派给合适的子代理。

#### 显式调用

在聊天中使用 `/name` 语法：

```
/verifier 确认身份验证流程已完成
/debugger 调查这个错误
/test-runner 运行所有测试并修复失败
```

---

## 4. Commands（命令）

### 4.1 什么是 Commands

Commands 是自定义的可复用工作流，可以通过 `/` 前缀在聊天输入框中快速触发。这些命令有助于在团队内标准化流程，并让常见任务执行得更加高效。

### 4.2 Commands 的特点

- ✅ **快速触发**：通过 `/` 前缀快速调用
- ✅ **可复用**：定义一次，多次使用
- ✅ **团队共享**：可以创建团队命令，所有成员自动可用
- ✅ **参数支持**：可以为命令提供额外的上下文

### 4.3 如何在 Cursor IDE 中设置

#### 方法一：手动创建

1. **创建命令目录**：

   ```bash
   # 项目级命令
   mkdir -p .cursor/commands

   # 或用户级命令（全局）
   mkdir -p ~/.cursor/commands
   ```

2. **创建命令文件**：

   ```bash
   touch .cursor/commands/code-review.md
   touch .cursor/commands/run-tests.md
   touch .cursor/commands/create-component.md
   ```

3. **编写命令内容**：

   ```markdown
   # 代码审查清单

   ## 概述

   全面的代码审查清单，用于确保代码质量、安全性和可维护性。

   ## 审查类别

   ### 功能性

   - [ ] 代码实现预期功能
   - [ ] 已处理边界情况
   - [ ] 错误处理恰当

   ### 代码质量

   - [ ] 代码可读性好且结构清晰
   - [ ] 函数简洁且职责单一
   - [ ] 遵循项目规范
   ```

#### 方法二：通过设置界面创建

1. **打开设置**：

   - `Cursor Settings` → `Rules` → `Commands`

2. **创建命令**：
   - 点击 `+ New` 按钮
   - 输入命令名称和内容

### 4.4 命令文件格式

命令文件是纯 Markdown 格式，不需要 frontmatter：

```markdown
# 命令名称

## 概述

命令的用途说明

## 步骤

1. 步骤 1
2. 步骤 2

## 检查清单

- [ ] 项目 1
- [ ] 项目 2
```

### 4.5 项目建议的 Commands

基于你的项目特点，建议创建以下 Commands：

```
.cursor/commands/
├── code-review.md              # 代码审查清单
├── run-tests.md                # 运行所有测试并修复失败
├── create-vue-component.md     # 创建 Vue 组件
├── create-java-module.md       # 创建 Java 模块（DDD）
├── api-integration-check.md    # API 集成检查
├── security-audit.md           # 安全审计
└── setup-new-feature.md        # 设置新功能
```

### 4.6 使用命令

1. **在聊天中输入 `/`**：

   - Cursor 会自动显示所有可用命令

2. **选择命令**：

   - 输入命令名称（如 `/code-review`）
   - 或输入部分名称进行搜索

3. **添加参数**（可选）：
   ```
   /code-review 审查 UserProfile.vue 组件
   /run-tests 运行用户模块的所有测试
   ```

---

## 5. Hooks（钩子）

### 5.1 什么是 Hooks

**Hooks** 是 Cursor 中用于**统一处理并保存 Agent 生成的结构化输出**的机制。当 Agent 或子代理（Subagents）生成报告、重构代码、文档草稿等结构化文件时，Hooks 可以按你定义的规则对这些输出进行格式化、保存到指定目录或按命名规范处理，从而把 AI 生成的内容稳定地接入你的工作流。

### 5.2 Hooks 的作用

- ✅ **输出统一处理**：对 Agent/Subagent 生成的文件做统一的后处理
- ✅ **保存位置可控**：将生成的文件保存到指定目录
- ✅ **命名与格式可控**：按约定命名、格式（如 Markdown、JSON）保存
- ✅ **与 Subagents 配合**：尤其适合子代理生成测试报告、审查报告、文档等场景

### 5.3 如何在 Cursor IDE 中配置

1. **打开设置**：

   - Mac: `Cmd + Shift + J`
   - Windows/Linux: `Ctrl + Shift + J`

2. **进入 Hooks 配置**：

   - 左侧导航栏 → **`Tools & MCP`** → **`Hooks`**

3. **查看与配置**：

   - **Configured Hooks (0)**：当前已配置的钩子数量，初始为 0
   - **Execution Log**：查看钩子的执行记录，可用 **Clear log** 清空
   - 若显示 "No hooks configured" / "No hook executions yet"，表示尚未配置或尚未执行过钩子

4. **配置方式**（根据 Cursor 文档）：
   - 通常在项目或用户配置中定义钩子（如 `.cursor/hooks/` 目录或设置界面）
   - 钩子可指定：触发条件（如“子代理生成某类文件”）、输出路径、命名规则、后处理脚本等

### 5.4 与 Rules、Skills、Subagents、Commands 的关系

| 关系项           | 说明                                                                                       |
| ---------------- | ------------------------------------------------------------------------------------------ |
| **与 Subagents** | 子代理生成结构化输出（报告、文档、测试结果）时，可用 Hooks 统一保存和处理，避免散落各处。  |
| **与 Commands**  | 通过 Commands 触发的流程若产生文件输出，也可由 Hooks 做统一后处理。                        |
| **与 Rules**     | Hooks 不改变 Rules 内容，但可约定“生成的文件需符合项目规范”，由 Hooks 保证落盘格式和位置。 |
| **与 Skills**    | Skills 若包含“生成文件”的能力，其输出可交给 Hooks 做一致化处理。                           |

**典型用法**：子代理完成代码审查后生成 `code-review-report.md`，通过 Hooks 自动保存到 `reports/` 并加上时间戳，方便追溯和团队共享。

---

## 6. Indexing & Docs（索引与文档）

### 6.1 什么是 Indexing & Docs

**Indexing & Docs** 用于两件事：

1. **Codebase Indexing（代码库索引）**：对当前项目的代码做语义嵌入和元数据索引，并存储在云端（代码本身仍只存本地），让 AI 对你的代码库有更好的**上下文理解**和**知识检索**。
2. **Docs（文档）**：爬取并索引**自定义资源与开发文档**（内部 Wiki、框架文档、项目说明等），把这些文档作为 AI 的额外上下文，回答问题时可以结合你的项目文档和规范。

### 6.2 Indexing & Docs 的作用

- ✅ **提升代码理解**：AI 能更准确理解项目结构、命名和架构
- ✅ **支持大代码库**：通过索引在云端做语义检索，避免把整库塞进上下文
- ✅ **自定义知识**：把内部文档、规范、API 说明纳入 AI 上下文
- ✅ **新文件夹自动索引**：可开启“自动索引新文件夹”，保持索引与项目同步

### 6.3 如何在 Cursor IDE 中配置

1. **打开设置**：

   - Mac: `Cmd + Shift + J`
   - Windows/Linux: `Ctrl + Shift + J`

2. **进入 Indexing & Docs**：

   - 左侧导航栏 → **`Tools & MCP`** → **`Indexing & Docs`**

3. **Codebase Indexing（代码库索引）**：

   - **状态**：进度条显示索引完成度（如 100%），以及已索引文件数（如 842 files）
   - **Sync**：手动触发重新同步索引
   - **Delete Index**：删除当前索引（慎用）
   - **Index New Folders**：建议开启。自动索引新加入的、文件数少于 50,000 的文件夹
   - **Ignore Files in .cursorignore**：与 `.gitignore` 类似，在此排除不需要被索引的文件；可点击 **Edit** 编辑，或 **View included files** 查看被索引内容

4. **Docs（自定义文档）**：
   - 当前若无文档会显示 "No Docs Added"
   - **+ Add Doc**：添加要爬取并索引的文档源（如 URL、本地路径、Markdown 等）
   - 在 Chat 或编辑时也可通过 **@Add** 将某个文档加入上下文

### 6.4 与 Rules、Skills、Subagents、Commands 的关系

| 关系项           | 说明                                                                                                  |
| ---------------- | ----------------------------------------------------------------------------------------------------- |
| **与 Rules**     | 索引让 AI 更懂你的代码和目录结构，应用 Rules 时能更贴合项目实际（命名、分层、规范）。                 |
| **与 Skills**    | Skills 的“何时用、怎么用”依赖对项目的理解；好的索引能提高 Skill 的触发准确度和执行质量。              |
| **与 Subagents** | 子代理在独立上下文中工作，索引和 Docs 提供的代码/文档知识能帮助子代理做出更准确的决策。               |
| **与 Commands**  | 执行如“代码审查”“安全审计”等命令时，AI 依赖对代码库和文档的理解，索引与 Docs 直接提升这些命令的效果。 |

**总结**：Indexing & Docs 为 Rules、Skills、Subagents、Commands 等提供**底层上下文**——代码库语义 + 自定义文档，使上述能力在你当前项目里表现更好。

### 6.5 使用建议

- **代码库索引**：对主要开发项目保持开启，并定期 **Sync** 以反映最新改动
- **.cursorignore**：对 `node_modules`、`dist`、大型二进制等无需语义理解的内容做排除，可加快索引、减少噪音
- **Docs**：把项目 README、架构说明、API 文档、内部规范等加入 Docs，便于 AI 回答“我们项目是怎么做的”类问题

---

## 场景化配置建议

### 场景 1：日常开发

**需求**：快速开发新功能，遵循项目规范

**配置**：

- ✅ **Indexing & Docs**：确认代码库索引完成，AI 能准确理解项目结构
- ✅ **Rules**：项目规则设置为 `Always Apply`，确保始终遵循规范
- ✅ **Commands**：创建 `/create-vue-component` 和 `/create-java-module` 命令
- ✅ **Skills**：创建组件生成器和代码生成器技能

**使用流程**：

1. 使用 `/create-vue-component` 创建组件
2. Agent 结合索引上下文自动应用项目规则
3. 使用 Skills 生成符合规范的代码

### 场景 2：代码审查

**需求**：系统化审查代码质量和规范

**配置**：

- ✅ **Indexing & Docs**：代码库已索引，便于审查时结合项目上下文；可添加 API 文档等 Docs
- ✅ **Commands**：创建 `/code-review` 命令
- ✅ **Subagents**：创建 `code-reviewer` 子代理
- ✅ **Rules**：代码质量规则设置为 `Apply Intelligently`
- ✅ **Hooks**（可选）：子代理生成审查报告时，用 Hooks 统一保存到 `reports/` 等目录

**使用流程**：

1. 使用 `/code-review` 命令
2. Agent 自动调用 `code-reviewer` 子代理
3. 子代理应用代码质量规则进行审查；若生成报告文件，由 Hooks 统一保存

### 场景 3：调试和测试

**需求**：快速定位问题并修复测试

**配置**：

- ✅ **Indexing & Docs**：代码库已索引，便于定位错误和理解测试结构
- ✅ **Subagents**：创建 `debugger` 和 `test-runner` 子代理
- ✅ **Commands**：创建 `/run-tests` 和 `/debug` 命令
- ✅ **Hooks**（可选）：若子代理生成测试报告或诊断结果文件，用 Hooks 统一保存

**使用流程**：

1. 使用 `/debug` 命令调查错误
2. Agent 调用 `debugger` 子代理
3. 使用 `/run-tests` 运行测试
4. `test-runner` 子代理自动修复失败的测试；若产生报告，由 Hooks 处理

### 场景 4：团队协作

**需求**：统一团队标准和流程

**配置**：

- ✅ **Indexing & Docs**：各成员保持代码库索引同步；可添加团队文档、Wiki 等为 Docs
- ✅ **团队 Rules**：在 Cursor Dashboard 创建团队规则
- ✅ **团队 Commands**：创建团队级命令
- ✅ **项目 Rules**：项目特定规则存储在 `.cursor/rules/`
- ✅ **Hooks**（可选）：若团队统一要求审查/测试报告落盘，配置 Hooks 统一输出路径

**使用流程**：

1. 团队规则自动应用到所有项目
2. 团队成员使用统一的命令和流程，结合索引与 Docs 理解项目
3. 项目特定规则覆盖团队规则；生成的结构化输出由 Hooks 统一保存（若已配置）

---

## 项目特定配置

### 针对 Sci-Z-Platform-College 项目的建议

#### 1. Rules 配置

**当前状态**：✅ 已有基础规则

**建议补充**：

```
sci-z-web/.cursor/rules/
├── current-project-rules.mdc      # ✅ 已有
├── component-guidelines.mdc       # 新增：组件开发指南
├── api-integration.mdc             # 新增：API 集成规范
└── i18n-guidelines.mdc            # 新增：国际化规范

sci-z-server/.cursor/rules/
├── current-project-rules.mdc       # ✅ 已有
├── ddd-architecture.mdc          # 新增：DDD 架构详细规则
├── database-rules.mdc             # 新增：数据库操作规范
└── event-handling.mdc             # 新增：事件处理规范
```

#### 2. Skills 配置

**建议创建**：

```
.cursor/skills/
├── vue-component-generator/       # Vue 组件生成器
│   └── SKILL.md
├── java-ddd-generator/            # Java DDD 代码生成器
│   ├── SKILL.md
│   └── scripts/
│       └── generate-module.sh
└── api-integration-helper/        # API 集成助手
    └── SKILL.md
```

#### 3. Subagents 配置

**建议创建**：

```
.cursor/agents/
├── verifier.md                    # 验证代理
├── debugger.md                   # 调试代理
├── test-runner.md                # 测试运行工具
└── code-reviewer.md              # 代码审查代理
```

#### 4. Commands 配置

**建议创建**：

```
.cursor/commands/
├── code-review.md                 # 代码审查清单
├── run-tests.md                  # 运行所有测试
├── create-vue-component.md       # 创建 Vue 组件
├── create-java-module.md         # 创建 Java 模块（DDD）
├── api-integration-check.md      # API 集成检查
└── security-audit.md             # 安全审计
```

#### 5. Indexing & Docs 配置

**配置位置**：Cursor Settings → **Tools & MCP** → **Indexing & Docs**（设置界面）

**建议**：

- **Codebase Indexing**：确认进度完成（如 100%），必要时 **Sync**；开启 **Index New Folders**
- **Ignore Files in .cursorignore**：在项目根目录维护 `.cursorignore`，排除 `node_modules`、`dist`、`target` 等不需索引的目录
- **Docs**：按需点击 **+ Add Doc**，添加项目 README、API 文档、架构说明等，便于 AI 结合文档回答

#### 6. Hooks 配置

**配置位置**：Cursor Settings → **Tools & MCP** → **Hooks**（设置界面）

**建议**：

- 当子代理或命令会生成结构化文件（如代码审查报告、测试报告）时，在 Hooks 中配置输出目录、命名规则等，统一保存到 `reports/` 或约定目录
- 当前无输出文件需求可暂不配置；需要时再在 **Configured Hooks** 中添加并查看 **Execution Log**

---

## 快速开始清单

### 第零步：确认 Indexing & Docs（建议优先）

- [ ] 打开 **Tools & MCP** → **Indexing & Docs**
- [ ] 确认代码库索引完成（如 100%），必要时点击 **Sync**
- [ ] 开启 **Index New Folders**
- [ ] 按需配置 **.cursorignore** 排除不需索引的目录
- [ ] 如需自定义文档，点击 **+ Add Doc** 添加

### 第一步：设置 Rules（必需）

- [ ] 确认现有规则文件存在
- [ ] 检查规则类型设置（Always Apply / Apply Intelligently）
- [ ] 根据需要补充新的规则文件

### 第二步：创建 Commands（推荐）

- [ ] 创建 `.cursor/commands/` 目录
- [ ] 创建常用的命令文件（如 `code-review.md`）
- [ ] 测试命令是否正常工作

### 第三步：创建 Skills（可选）

- [ ] 创建 `.cursor/skills/` 目录
- [ ] 创建项目特定的技能（如组件生成器）
- [ ] 在设置中启用 Agent Skills

### 第四步：创建 Subagents（可选）

- [ ] 创建 `.cursor/agents/` 目录
- [ ] 创建常用的子代理（如 `verifier.md`）
- [ ] 测试子代理是否正常工作

### 第五步：配置 Hooks（可选）

- [ ] 若子代理或命令会生成结构化文件，打开 **Tools & MCP** → **Hooks**
- [ ] 配置输出路径、命名规则等，并可通过 **Execution Log** 验证

---

## 常见问题

### Q1: Rules、Skills、Subagents、Commands、Hooks、Indexing & Docs 的区别是什么？

**A**:

- **Rules**：系统级指令，指导 Agent 的行为和代码生成
- **Skills**：可复用的专业能力，包含指令和可执行脚本
- **Subagents**：专业化的 AI 助手，用于处理复杂任务
- **Commands**：快速触发的工作流，通过 `/` 前缀调用
- **Hooks**：统一处理并保存 Agent/Subagent 生成的结构化输出（如报告、文档）
- **Indexing & Docs**：代码库语义索引 + 自定义文档，为上述能力提供更好的代码和文档上下文

### Q2: 我应该优先设置哪个？

**A**:

1. **Indexing & Docs**（建议优先）- 开启代码库索引，让 AI 更好理解你的项目
2. **Rules**（必需）- 确保代码质量和规范
3. **Commands**（推荐）- 提高日常工作效率
4. **Skills**（可选）- 扩展 Agent 能力
5. **Subagents**（可选）- 处理复杂任务
6. **Hooks**（可选）- 当子代理或命令产生结构化文件输出时再配置

### Q3: 项目规则和用户规则有什么区别？

**A**:

- **项目规则**：存储在 `.cursor/rules/`，受版本控制，仅作用于当前项目
- **用户规则**：存储在 Cursor Settings，全局生效，适用于所有项目

### Q4: 如何让团队共享配置？

**A**:

- **团队 Rules**：在 Cursor Dashboard 创建团队规则
- **团队 Commands**：在 Cursor Dashboard 创建团队命令
- **项目 Rules/Skills/Subagents/Commands**：提交到 Git 仓库
- **Indexing & Docs**：按项目在工作区中配置，代码库索引与 Docs 随项目使用
- **Hooks**：可在项目或用户级配置，输出路径等可纳入版本控制（若支持项目内配置文件）

---

## 参考资源

- [Rules 官方文档](https://cursor.com/cn/docs/context/rules)
- [Skills 官方文档](https://cursor.com/cn/docs/context/skills)
- [Subagents 官方文档](https://cursor.com/cn/docs/context/subagents)
- [Commands 官方文档](https://cursor.com/cn/docs/context/commands)
- **Hooks**：在 Cursor Settings → **Tools & MCP** → **Hooks** 中配置与管理；Subagents 文档中提及 [hooks](/docs/agent/hooks) 用于处理子代理的结构化输出
- **Indexing & Docs**：在 Cursor Settings → **Tools & MCP** → **Indexing & Docs** 中配置代码库索引与自定义 Docs
- [Agent Skills 标准](https://agentskills.io)

---

## 配置关系与优先级

### CLAUDE.md、Rules、Skills、Subagents、Commands、Hooks、Indexing & Docs 的关系

这七项配置共同构成了 Cursor Agent 的完整能力体系：

```
┌─────────────────────────────────────────────────────────┐
│                    Cursor Agent 配置体系                  │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────────┐                               │
│  │ Indexing & Docs      │  ← 代码库索引 + 自定义文档（底层上下文） │
│  └──────────┬───────────┘                               │
│             │                                            │
│  ┌──────────▼───────────┐                               │
│  │  CLAUDE.md           │  ← 项目顶层指令（全局上下文）  │
│  └──────────┬───────────┘                               │
│             │                                            │
│  ┌──────────▼───────────┐                               │
│  │    Rules             │  ← 系统级指令（规范约束）      │
│  └──────────┬───────────┘                               │
│             │                                            │
│  ┌──────────▼───────────┐                               │
│  │    Skills            │  ← 专业能力扩展（按需加载）    │
│  └──────────┬───────────┘                               │
│             │                                            │
│  ┌──────────▼───────────┐                               │
│  │  Subagents           │  ← 专业化 AI 助手（按需调用）  │
│  └──────────┬───────────┘                               │
│             │                                            │
│  ┌──────────▼───────────┐                               │
│  │   Commands           │  ← 快速工作流（手动触发）      │
│  └──────────┬───────────┘                               │
│             │                                            │
│  ┌──────────▼───────────┐                               │
│  │   Hooks              │  ← 输出后处理（统一保存/格式化） │
│  └──────────────────────┘                               │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 配置优先级（加载顺序）

```
0. Indexing & Docs（代码库索引 + 自定义文档）  ← 底层上下文，持续生效
   ↓
1. 用户级 Rules（Cursor Settings）          ← 最高优先级
   ↓
2. 项目根目录 CLAUDE.md（如果存在）
   ↓
3. 项目根目录 .cursor/rules/（通用规则）
   ↓
4. 子项目 CLAUDE.md（sci-z-web/CLAUDE.md）
   ↓
5. 子项目 .cursor/rules/（项目特定规则）
   ↓
6. Skills（Agent 按需加载）
   ↓
7. Subagents（Agent 按需调用）
   ↓
8. Commands（用户手动触发）
   ↓
9. Hooks（Agent/Subagent 生成结构化输出时触发）
```

### 当前项目配置状态

#### ✅ 已配置

**Rules**：

- `sci-z-web/.cursor/rules/current-project-rules.mdc` - Vue 前端规则
- `sci-z-server/.cursor/rules/current-project-rules.mdc` - Java 后端规则
- `sci-z-server/.cursor/rules/common-rules.mdc` - 通用规则
- `sci-z-server/.cursor/rules/notes-rules.mdc` - 注释规则（alwaysApply: true）

**CLAUDE.md**：

- `sci-z-web/CLAUDE.md` - 前端项目顶层指令 ✅
- `sci-z-server/CLAUDE.md` - 后端项目顶层指令 ✅

#### ⚠️ Indexing & Docs（索引与文档）

- **Codebase Indexing**：由 Cursor 根据工作区自动维护，在 **Tools & MCP** → **Indexing & Docs** 可查看进度；建议确认完成并开启 **Index New Folders**。
- **Docs**：若显示 "No Docs Added"，可按需通过 **+ Add Doc** 添加项目文档。

#### ❌ 未配置

- Skills（技能）
- Subagents（子代理）
- Commands（命令）
- Hooks（钩子，当前 Configured Hooks 为 0）
- 项目根目录通用配置

### 推荐配置方案

参考 `项目配置方案.md` 文档，建议：

1. **通用配置**（项目根目录 `.cursor/`）：

   - 通用 Rules（编码标准、Git 工作流、文档规范）
   - 通用 Skills（代码审查、Git 助手）
   - 通用 Subagents（验证代理、调试代理）
   - 通用 Commands（代码审查、运行测试）

2. **项目特定配置**（子项目 `.cursor/`）：

   - 前端：Vue 组件规范、API 集成规范
   - 后端：DDD 架构规范、数据库操作规范

3. **Indexing & Docs**：在 **Tools & MCP** → **Indexing & Docs** 确认代码库索引、按需添加 Docs、配置 `.cursorignore`。

4. **Hooks**：当子代理或命令会生成结构化文件时，在 **Tools & MCP** → **Hooks** 配置输出路径与命名规则。

详细配置方案请参考：`项目配置方案.md`

---

## 下一步行动

1. ✅ 阅读本指南
2. ✅ 阅读 `项目配置方案.md` 了解详细配置方案
3. ⬜ 在 **Indexing & Docs** 中确认代码库索引完成，必要时添加自定义 Docs
4. ⬜ 检查现有 Rules 配置
5. ⬜ 创建常用的 Commands
6. ⬜ 根据需要创建 Skills 和 Subagents
7. ⬜ 若需统一保存 Agent 输出，配置 **Hooks**
8. ⬜ 测试配置是否正常工作
9. ⬜ 与团队分享配置

---

**最后更新**：2026-02-02
