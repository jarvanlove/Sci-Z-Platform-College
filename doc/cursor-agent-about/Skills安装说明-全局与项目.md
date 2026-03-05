---
title: Skills CLI 安装说明 - 项目目录与全局安装
编写时间: 2026-02-09 修订
维护人: 项目组
---

# Skills CLI 安装说明：为何多出很多目录、作用范围与全局安装

## 当前本机状态（2026-02-09 修订）

- **find-skills** 已改为 **全局安装、所有 Agent**（Cursor、Claude Code、Codex、Gemini CLI、GitHub Copilot、Kiro CLI、OpenCode、Trae、Antigravity 等）。
- 安装命令：`npx skills add https://github.com/vercel-labs/skills --skill find-skills -g -y`
- 安装位置：用户目录 `~\.agents\skills\find-skills`，多端通过 universal 或符号链接使用；**项目内不再生成** 任何技能目录。

---

## 全局 skills 在电脑上的目录（以 Windows 为例）

| 内容 | 路径 |
|------|------|
| **全局 skills 根目录** | `C:\Users\<你的用户名>\.agents\skills\`（`~` = 当前用户主目录） |
| **find-skills** | `C:\Users\<你的用户名>\.agents\skills\find-skills\`（其下即有 `SKILL.md`） |
| **ui-ux-pro-max**（若已用 CLI 全局安装） | `C:\Users\<你的用户名>\.agents\skills\ui-ux-pro-max\` |

查看本机已安装的全局 skills：在任意目录执行 `npx skills list -g`。

---

## 做项目时如何使用（以 Cursor 为例）

1. **无需额外配置**  
   全局安装后，用 **Cursor 打开任意项目**（包括本仓库或其它项目），Cursor 会自动加载 `~\.agents\skills\` 下的技能，不需要在项目里再装一遍或改设置。

2. **在对话里触发 skill**  
   - **find-skills**：当你在 Cursor 的 Agent/对话里问例如「有没有做 X 的 skill」「find a skill for 某某」「帮我找个推荐相关的 skill」时，Agent 会按 find-skills 的说明去执行 `npx skills find ...` 并给出可安装的技能与命令。  
   - **ui-ux-pro-max**：当你在对话里做 UI/UX 相关需求（如设计组件、配色、布局、可访问性等）时，Agent 会参考该 skill 的规则与建议。

3. **不需要你手动“打开”目录**  
   使用方式就是：**正常用 Cursor 写项目、在对话里提需求**；符合 skill 描述的场景时，Agent 会自动引用对应 skill，无需你指定 `~\.agents\skills\` 路径。

## 1. 安装后为什么会出现这么多目录？

`npx skills add` 默认会为 **多种 AI 编程 Agent** 同时配置同一份技能，因此会在项目根目录下生成多个文件夹：

| 目录 | 含义 |
|------|------|
| **`.agents/`** | **通用技能库**（Universal），所有支持“通用协议”的 Agent 共用这里的 `skills/` |
| **`.agent/`** | Antigravity 等 Agent 的 skills 目录（多为符号链接指向 `.agents`） |
| **`.claude/`** | Claude Code 的 skills 目录 |
| **`.kiro/`** | Kiro CLI 的 skills 目录 |
| **`.trae/`** | Trae 的 skills 目录 |
| 其他 | 如 `.augment/`、`.cline/`、`.continue/` 等，依安装时勾选的 Agent 而定 |

您当前只用 **Cursor**，但 CLI 默认会勾选多种 Agent（Cursor、Claude Code、Codex、Gemini CLI 等），所以会一次性创建上述目录。  
**结论**：这些目录是 Skills CLI 的“多 Agent 支持”设计导致的，不是错误；只用 Cursor 时，实际被 Cursor 用到的通常是 `.agents/skills/` 或通过符号链接指向它的 Cursor 专用路径。

---

## 2. 安装后是全局生效还是仅当前项目？

- **未加 `-g`（您上次的安装方式）**：**仅当前项目**。技能装在 **当前项目根目录** 下的 `.agents/`、`.agent/` 等，只有打开这个项目时，Cursor 才会加载这些技能。
- **加 `-g`（全局安装）**：技能装在 **用户级目录**（如用户主目录下的 `.skills` 或 CLI 约定路径），**所有项目** 下的 Cursor 都会加载，且 **不会在项目里生成** `.agents`、`.agent`、`.claude` 等目录。

---

## 3. 与您手动配置的 `.cursor` 是否冲突？

- **一般不会冲突**：
  - 您手动创建的是 **`.cursor/`**：下面有 `agents/`、`rules/`、`commands/`、`skills/`（如 code-review、git-helper、ui-ux-pro-max）等，这是 Cursor 官方约定的项目配置。
  - Skills CLI 创建的是 **`.agents/`**（以及 `.agent/`、`.claude/` 等），技能实体在 `.agents/skills/find-skills/`。
- Cursor 会同时识别：
  - `.cursor/skills/` 下的技能（您自己放的），
  - 以及 CLI 为 Cursor 配置的路径（可能是 `.agents/skills/` 的符号链接或 Cursor 自己的 skills 目录）。
- 因此：**您原来的 agents、rules、commands 和自定义 skills 都保留；只是多了一个 find-skills 的安装来源，不会覆盖您已有配置。**

若仍希望“只保留 .cursor、不在项目里留 .agents 等目录”，可以改用 **全局安装**（见下）。

---

## 4. 能否自定义安装位置并全局生效？

- **自定义安装路径**：Skills CLI 当前没有在文档中暴露“自定义安装根目录”的选项（如 `SKILLS_HOME`），默认行为是：
  - 项目安装：当前项目根目录下的 `.agents/` 等；
  - 全局安装：用户主目录下的约定目录。
- **全局生效**：可以，使用 **全局安装** 即可，技能会对本机所有项目生效，且不在项目里创建 `.agents`、`.agent`、`.claude` 等目录。

### 使用全局安装的步骤（仅用 Cursor 时推荐）

1. **（可选）清理当前项目里的 CLI 目录**  
   若已用项目方式安装过 find-skills，可先删除项目根目录下由 CLI 生成的目录（避免与全局版本重复）：
   - 删除：`.agents/`、`.agent/`、`.claude/`、`.kiro/`、`.trae/` 等（根据您本地实际存在的目录删即可）。

2. **全局安装 find-skills**  
   在任意目录执行（不需要在项目里）：

   ```bash
   npx skills add https://github.com/vercel-labs/skills --skill find-skills -g -y
   ```

   - `-g`：安装到用户级目录，对本机所有项目生效。  
   - `-y`：跳过交互确认。

3. **验证**  
   重新打开 Cursor（或重载窗口），在任意项目中应都能使用 find-skills，且项目根目录下不再出现 `.agents` 等新目录。

4. **后续更新**  
   更新已安装的技能（包括全局安装的）：

   ```bash
   npx skills update
   ```

---

## 5. 小结

| 问题 | 简要回答 |
|------|----------|
| 为什么多出很多目录？ | CLI 默认为多种 Agent（Cursor、Claude、Codex、Kiro、Trae 等）各建一份配置目录，所以会看到 `.agents`、`.agent`、`.claude`、`.kiro`、`.trae` 等。 |
| 是全局还是当前项目？ | 不加 `-g` = 仅当前项目；加 `-g` = 用户级全局，所有项目可用。 |
| 和 .cursor 冲突吗？ | 不冲突；`.cursor` 是您的配置，CLI 主要用 `.agents` 等，两者并存。 |
| 能自定义位置且全局吗？ | 官方未提供自定义安装根目录；用 `-g` 即可全局生效且不在项目里生成目录。 |

若希望本机所有项目都能用 find-skills，且不想在项目里保留这些目录，建议使用：

```bash
# 仅为 Cursor 安装，不生成与 Cursor 无关的目录
npx skills add https://github.com/vercel-labs/skills --skill find-skills -g -y --agent cursor
```

并在项目中已把 `.agents/` 等加入 `.gitignore`，避免误提交 CLI 生成的文件（见项目根目录 `.gitignore`）。

---

## .cursor/skills 下 3 个 skill 能否用“安装方式”替代、能否删除？

| skill | 能否用 CLI 安装？ | 能否删掉 .cursor/skills 下该目录？ |
|-------|------------------|-----------------------------------|
| **code-review** | **不能**。内容与项目强绑定（sci-z-web、sci-z-server、.cursor/rules），生态里没有同名同内容的 skill。 | **不要删**。删了就没有代码审查 skill，只能保留在 .cursor/skills。 |
| **git-helper** | **不能**。内容与项目强绑定（.cursor/rules/git-workflow.mdc），生态里没有名为 git-helper 的等价 skill。 | **不要删**。删了就没有 Git 助手 skill，只能保留在 .cursor/skills。 |
| **ui-ux-pro-max** | **可以**。生态里有同名 skill，例如：`npx skills add sickn33/antigravity-awesome-skills --skill ui-ux-pro-max -g -y` 或 `davila7/claude-code-templates@ui-ux-pro-max`。 | **视情况**。若已用上面命令成功全局安装，且确认与本地 SKILL.md 内容一致，可以删掉 .cursor/skills/ui-ux-pro-max；若本地有 SKILL-ZH.md、data/、scripts/ 等自定义，建议保留或对比后再决定是否删。 |

**结论**：**code-review** 和 **git-helper** 请保留在 `.cursor/skills`，不要删。**ui-ux-pro-max** 可先手动执行全局安装（见上表命令），安装成功并确认无误后再决定是否删除本地目录。

---

## 其他编译工具 / CLI、其他项目能否使用已安装的 skills？

| 安装方式 | 其他项目（Cursor 打开） | 其他编译工具（VS Code、IDEA 等） | 其他 CLI（如 Codex、Claude Code） |
|----------|--------------------------|-----------------------------------|------------------------------------|
| **项目内安装**（无 `-g`） | 仅安装技能的那个项目可用 | 一般不可用（技能在项目目录，且这些工具不读 Cursor skills） | 仅当该 CLI 被配置为读该项目目录且支持 skills 时才可能用 |
| **全局安装**（`-g`，如当前 find-skills） | **本机任意项目用 Cursor 打开都能用** | 不可用（VS Code、IDEA 等不参与 Skills 生态，不会去读 `~/.agents/skills`） | 仅当该 CLI 也支持并配置读同一用户目录（如 `~/.agents/skills`）时才可用 |

**结论**：

1. **您说的“全局”**：安装一次后，**本机任意项目用 Cursor 打开**都能用——当前已通过 `-g --agent cursor` 实现；**其他编译工具**（VS Code、IDEA、WebStorm 等）**不会**自动使用这些 skills，因为它们是 Cursor / Skills 生态的约定，其它 IDE 不读该路径。
2. **其他 CLI**（如 Codex、Claude Code、Gemini CLI 等）：若您用 `npx skills add ... -g` 且**不**加 `--agent cursor`，CLI 会为多种 Agent 在用户目录下都建好链接，那些 CLI 打开项目时也可用；若只加了 `--agent cursor`，则只有 Cursor 能用，其它 CLI 不会用到这次安装的技能。
3. **其他项目**：全局安装后，**所有项目**只要用 Cursor 打开，都会加载用户目录下的 find-skills，无需每个项目再装一遍。
