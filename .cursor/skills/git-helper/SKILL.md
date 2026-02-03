---
name: git-helper
description: "Executes Git workflows: run status, suggest branch names, generate commit messages, or resolve conflicts. Use when user asks for branch naming, commit message, or conflict resolution. 执行体：按步骤执行 Git 操作并输出可执行命令。"
---

# Git 操作助手（执行体）

## Instructions

**按用户意图选择并执行对应流程，最后输出可直接执行的命令或操作说明。**

1. **获取当前 Git 状态（必做）**

   - 在项目根目录执行：`git status`，并视需要执行 `git branch -a`。
   - 向用户汇报：当前分支、是否有未提交变更、是否有冲突、是否在预期分支上。

2. **根据用户请求执行对应子流程**

   - **分支命名 / 创建分支**
     - 按 `.cursor/rules/git-workflow.mdc`：`feature/xxx`、`bugfix/xxx`、`hotfix/xxx`，小写+短横线。
     - 输出一条可直接执行的命令，例如：`git checkout -b feature/user-login`。
   - **提交信息**
     - 使用 Conventional Commits：`type(scope): subject`。
     - 类型：feat / fix / docs / style / refactor / perf / test / build / chore。
     - 输出一条可直接执行的命令，例如：`git commit -m "feat(auth): add phone login"`。
     - 若项目提供 `scripts/suggest-commit-msg.sh`，可传入 type/scope 调用并采纳建议。
   - **冲突解决**
     - 说明冲突文件与原因；给出逐条解决步骤（保留哪侧、如何合并）；最后给出验证命令（如 `git status`、`npm run build`）。

3. **输出可执行内容**
   - 所有给出的命令必须可在项目根或指定子目录直接执行。
   - Windows PowerShell 下用 `;` 替代 `&&` 连接多条命令。
   - 涉及强制推送、批量删除等敏感操作时，先提示风险并请用户确认后再给出命令。

## Scripts

- **scripts/suggest-commit-msg.sh**（可选）：接收参数（如 type、scope），输出建议的 commit message 一行。若不存在则由 Agent 按规范直接生成。

## Guidelines

- 分支与提交规范以 `.cursor/rules/git-workflow.mdc` 为准。
- 命令以用户当前环境为准（Windows 下用 PowerShell 语法）。
