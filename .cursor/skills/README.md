# Skills 目录说明

本目录存放 **Cursor 通用技能**（每个技能一个子目录，内含 `SKILL.md`）。Agent 在接到相关任务时会按需加载对应技能。

---

## 本目录技能列表

| 技能目录         | 说明                                                                                   | 典型用法                                   |
| ---------------- | -------------------------------------------------------------------------------------- | ------------------------------------------ |
| **code-review/** | 按功能性、质量、性能、可维护性、安全性逐项审查，结合项目 Vue/Java 规范给出具体建议。   | 「帮我审查这段代码」或 `/code-review` 命令 |
| **git-helper/**  | 分支命名、Conventional Commits 提交、冲突解决，遵循 `.cursor/rules/git-workflow.mdc`。 | 分支、提交、合并、冲突处理等 Git 操作      |

上述两个技能已写清审查维度与操作步骤，并与项目规则（编码范式、Git 工作流）对齐，便于 Agent 给出具体、可执行的建议。

---

## 使用方式

- **自动**：Agent 根据用户请求判断是否加载某技能。
- **显式**：在对话中 @ 对应技能（若 Cursor 支持 @ skills）。
- 与 **commands** 配合：如 `/code-review` 会引导 Agent 按 code-review 技能执行。

---

## 使用外部技能（skills.sh / SkillsMP）

项目内技能放在本目录即可；若要从 **技能市场** 使用现成技能，可采用以下方式。

### [skills.sh](https://skills.sh/)（The Agent Skills Directory）

- **浏览**：[https://skills.sh/](https://skills.sh/) 按热度/趋势搜索技能（如 vue-best-practices、code-review、next-best-practices 等）。
- **安装（用户级，多项目可用）**：
  ```bash
  npx skills add <owner/repo>
  ```
  例如：`npx skills add antfu/vue-skills`。安装后技能会进入 Cursor 可识别的技能目录（如用户目录下的 skills），对所有项目生效。
- **安装到本项目（团队共享）**：若希望技能仅在本仓库生效、且随 Git 共享，可手动把技能内容拷到本目录：
  1. 在 skills.sh 找到技能对应的 GitHub 仓库（如 `owner/repo`）。
  2. 从该仓库拿到 `SKILL.md` 或技能包，在本目录新建子目录如 `third-party-skill-name/`，放入 `SKILL.md`（若仓库为多技能集合，则按仓库结构取对应子目录的 SKILL.md）。
  3. 确保 YAML front matter 含 `name`、`description`，Cursor 会自动发现。

### [SkillsMP](https://skillsmp.com/)（技能市场）

- **浏览**：[https://skillsmp.com/](https://skillsmp.com/) 按关键词、类别、热度搜索技能（SKILL.md 格式，支持 Claude Code、Codex 等）。
- **在本项目中使用**：SkillsMP 上的技能多为单文件或仓库中的 SKILL.md，**配置方式**：
  1. 在 SkillsMP 打开技能详情，获取 SKILL.md 内容或对应仓库链接。
  2. 在本目录新建子目录，如 `skillsmp-xxx/`，将 `SKILL.md` 放入其中（格式需含 `name`、`description` 等前置元数据）。
  3. 保存后 Cursor 会从 `.cursor/skills/` 发现该技能，Agent 即可按需调用。

### 注意

- **用户级 vs 项目级**：`npx skills add` 安装到的是用户级目录，适合「所有项目都用」；拷贝到 `.cursor/skills/` 为项目级，适合团队共享、仅本仓库使用。
- **格式**：外部技能需为 Cursor 可识别的 SKILL.md（通常含 `name`、`description` 及 Instructions/Guidelines）。若从 GitHub 克隆的是多技能仓库，需取其中单个技能的 SKILL.md 放入本目录的一个子目录中。

---

## 添加本项目自定义技能

1. 在本目录下新建子目录，如 `my-skill/`。
2. 在子目录中创建 `SKILL.md`，包含 `name`、`description` 及 Instructions/Guidelines。
3. Agent 即可在相关任务中按需使用该技能。
