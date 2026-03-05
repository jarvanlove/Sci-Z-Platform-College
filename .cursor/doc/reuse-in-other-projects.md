---
编写时间: 2026-02-03
维护人: 平台组
---

# 在其他项目复用本仓库的 .cursor 配置

本仓库根目录的 `.cursor/` 内含通用 Rules、Skills、Commands、Agents、Hooks。若希望**用 Cursor 打开别的项目时**也能用上同一套配置，可采用下面两种方式之一。

---

## 方式一：User 级配置（推荐：通用规则在所有项目生效）

把「与项目路径无关」的规则放到 Cursor 的**用户级 Rules**，这样打开任意项目都会加载。

| 步骤 | 操作                                                                                                                                                          |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | 打开 **Cursor → Settings（设置）→ General → Rules for AI**（或 Cursor Settings 里的「Rules」）                                                                |
| 2    | 选择 **User** 作用域，点击 **+ New** 新建规则                                                                                                                 |
| 3    | 将本仓库 `.cursor/rules/` 下需要全局生效的规则**内容复制进去**（如 `general-coding-standards.mdc`、`documentation-standards.mdc`、`git-workflow.mdc` 的正文） |
| 4    | 保存后，在**任意项目**中都会自动应用这些规则                                                                                                                  |

**适用**：编码规范、文档规范、Git 提交规范等**不依赖本项目路径**的规则。  
**不适用**：Skills / Commands / Hooks 目前无法以「User 级」方式统一配置，只能按下面方式二复用整份 `.cursor`，或在每个项目单独复制。

---

## 方式二：用「目录联接」或「符号链接」直接复用整份 .cursor

让**其他项目的 `.cursor` 目录**指向**本仓库的 `.cursor`**，这样打开其他项目时，Cursor 读取的仍是这一套 rules/skills/commands/agents；Hooks 若路径写死本项目则可能需单独处理。

### Windows（在「其他项目」根目录执行）

1. **先删掉或改名** 其他项目下已有的 `.cursor`（若有）。
2. 在 **PowerShell 或 CMD** 中执行（把路径改成你的实际路径）：

**目录联接（Junction，不需管理员权限）：**

```powershell
# 其他项目根目录，例如 D:\Work\OtherProject
cd "D:\Work\OtherProject"

# 创建 .cursor 目录联接，指向本仓库的 .cursor
cmd /c mklink /J ".cursor" "c:\Work\note\CursorWorkSpace\SALL\ZOE_Link_AI\Sci-Z-Platform-College\.cursor"
```

**或使用符号链接（Symbolic Link，需管理员权限或已开启「开发人员模式」）：**

```powershell
cd "D:\Work\OtherProject"
cmd /c mklink /D ".cursor" "c:\Work\note\CursorWorkSpace\SALL\ZOE_Link_AI\Sci-Z-Platform-College\.cursor"
```

3. 打开 **Cursor**，用 **File → Open Folder** 打开「其他项目」根目录，即可使用与本仓库相同的 `.cursor` 配置。

### macOS / Linux

```bash
cd /path/to/other-project
rm -rf .cursor   # 若已存在
ln -s "/path/to/Sci-Z-Platform-College/.cursor" .cursor
```

### 注意

- **路径**：链接目标必须使用**本仓库 `.cursor` 的绝对路径**；本仓库移动后需重做一次链接。
- **项目专属内容**：本仓库 `.cursor` 里部分规则/技能会提到 `sci-z-web`、`sci-z-server` 等，在其他项目中可能不适用，可忽略或在该项目下单独加一层 `.cursor/rules`（若 Cursor 支持多级合并）。
- **Hooks**：hooks 里若有基于「当前项目根路径」的假设，在其他项目里行为可能不同，可按需在本仓库或该项目中调整。

---

## 小结

| 需求                                                              | 建议                                                                              |
| ----------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| 只在「任意项目」里用上编码/文档/Git 等通用规则                    | **方式一**：把规则复制到 Cursor User Rules                                        |
| 希望其他项目**整份**用本仓库的 rules + skills + commands + agents | **方式二**：在其他项目根目录用 Junction/符号链接把 `.cursor` 指到本仓库 `.cursor` |

若你希望「一份配置、多项目共享」且能随本仓库一起更新，优先用**方式二**并保持本仓库路径稳定（例如固定放在 `Sci-Z-Platform-College` 下）。
