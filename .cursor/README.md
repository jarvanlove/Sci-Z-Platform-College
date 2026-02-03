# Cursor 配置说明

本目录为 Sci-Z-Platform-College 的 **前后端通用** Cursor 配置，与根目录 `CLAUDE.md`、`sci-z-web/.cursor/`、`sci-z-server/.cursor/` 配合使用。  
**本项目采用项目级配置、团队共享**：Rules、Skills、Agents、Commands 均放在仓库内，无需同步到 Cursor 的 User 设置，clone 即生效。  
逐项配置时可参考项目根目录 **《Cursor 配置操作清单.md》**；功能说明见 **《Cursor 功能设置指南.md》**、**《项目配置方案.md》**。

---

## 目录一览

| 目录          | 说明                                                                                                                                                            |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **rules/**    | 通用规则：编码标准、文档规范、Git 工作流。详见 [rules/README.md](rules/README.md)。                                                                             |
| **skills/**   | 通用技能：代码审查、Git 助手等；如何从 [skills.sh](https://skills.sh/)、[SkillsMP](https://skillsmp.com/) 使用外部技能见 [skills/README.md](skills/README.md)。 |
| **agents/**   | 子代理（Subagents）：验证、调试等，主 Agent 可委派任务。详见 [agents/README.md](agents/README.md)。                                                             |
| **commands/** | 斜杠命令：如 `/code-review`、`/run-tests`，用户输入 `/` 触发。详见 [commands/README.md](commands/README.md)。                                                   |
| **hooks/**    | Hooks 脚本与配置：会话注入、审计、敏感文件拦截等，通过 `hooks.json` 配置。详见 [hooks/README.md](hooks/README.md)。                                             |

---

## 根目录与前后端 .cursor 的分工

本仓库为前后端同仓：根目录 `.cursor` 放**前后端共用**规范，前后端各自**专属**规范放在对应子项目的 `.cursor` 下。

| 位置                            | 作用                 | 内容示例                                                                                                        |
| ------------------------------- | -------------------- | --------------------------------------------------------------------------------------------------------------- |
| **根目录 `.cursor/`**（本目录） | 前后端通用，团队共享 | 编码范式、文档规范、Git 工作流、通用 code-review/git-helper、verifier/debugger、/code-review、/run-tests、hooks |
| **sci-z-web/.cursor/**          | 仅前端（Vue）        | Vue 架构与组件规范、前端 API/流程、Vue 组件生成器技能、create-vue-component 命令等                              |
| **sci-z-server/.cursor/**       | 仅后端（Java）       | DDD 架构、Java 命名与开发规范、仓储/服务层约定、Java DDD 生成器技能、create-java-module 命令等                  |

**怎么放：**

- **通用、希望前后端都遵守的**（如编码风格、提交规范、文档要求、通用审查命令）→ 放在 **根目录 `.cursor/`**。
- **只和前端相关的**（Vue、组件、前端 API、前端提示词模板）→ 放在 **sci-z-web/.cursor/**，详见 `sci-z-web/.cursor/rules/README.md`。
- **只和后端相关的**（Java、DDD、仓储/服务、后端模块生成）→ 放在 **sci-z-server/.cursor/**，详见 `sci-z-server/.cursor/rules/README.md`。

Cursor 会合并多级配置：打开根项目时，根 `.cursor` + 当前编辑文件所在子项目的 `.cursor` 会一起生效（用户级 Rules 若存在则优先于项目级）。

---

## User 与 Project 作用域（参考）

| 配置方式                                                     | 作用域      | 生效范围               |
| ------------------------------------------------------------ | ----------- | ---------------------- |
| **Settings 里「+ New」**（User 标签）                        | **User**    | 整个 Cursor，所有项目  |
| **项目 `.cursor/` 下编写**（本仓库采用此方式，便于团队共享） | **Project** | 仅当前项目，随仓库共享 |

本项目规则、技能、子代理、命令均放在 **Project**（仓库内），团队 clone 即用；若你同时希望**在其它项目里**也有一致的编码/文档/Git/审查能力，见下方「希望在其他项目也能用？」。

---

## 希望在其他项目也能用？

**问题**：`.cursor/` 是**项目级**配置，只在打开本仓库时生效；用 Cursor 打开别的项目时用不到。若把本目录整份复制到其它项目，里面的路径、sci-z-web/sci-z-server 等引用又不通用。

**做法**：把「通用」能力放到 **User 级**，把「仅本项目」的留在 **.cursor/**。

| 配置           | 放哪里                                                                          | 说明                                                                  |
| -------------- | ------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| **通用**       | **User 级**（Cursor Settings → Rules / Skills / Subagents / Commands，选 User） | 无项目路径、无本项目专属引用，在**任意项目**打开都生效。              |
| **本项目专用** | **本仓库 `.cursor/`**                                                           | 含 sci-z-web/sci-z-server、本仓库目录结构、hooks 等，只在本项目生效。 |

- **Rules**：本仓库 `rules/` 下三条（通用编码、文档规范、Git 工作流）**内容已通用**，可直接把每条规则正文复制到 Settings → Rules → User → New，这样其它项目也会加载。
- **Skills / Commands**：本仓库技能里引用了「当前项目 .cursor 路径」或「sci-z-web / sci-z-server」的，若要在其它项目用，请用**通用版**（无路径、改为「遵循当前项目/仓库规范」）。通用版已整理在 [.cursor/docs/user-level-generic.md](docs/user-level-generic.md)，可复制到 User 或新项目的 `.cursor/skills/`。
- **Hooks**：必须跟项目走，无法放到 User；只有打开本仓库时生效。

详见 [.cursor/docs/user-level-generic.md](docs/user-level-generic.md)。

---

## 配置优先级（从高到低）

1. 用户级 Rules（Cursor Settings，若存在）
2. 项目根 `CLAUDE.md`
3. **本目录 `.cursor/rules/`**（前后端通用）
4. 子项目 CLAUDE.md（sci-z-web / sci-z-server）
5. **子项目 `.cursor/rules/`**（前端或后端专属）
6. Skills、Subagents、Commands（按需）
7. Hooks（事件触发）
