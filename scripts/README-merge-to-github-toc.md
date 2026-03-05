# 将 GitLab toc 合并到 GitHub toc（保持配置与 GitHub 一致）

## 目的

把 GitLab 的 `toc` 分支合并到 GitHub 的 `toc` 分支，同时**不把含 API Key 等敏感信息的配置推送到 GitHub**。合并后以下内容与 **GitHub toc 当前版本保持一致**：

- `sci-z-server/src/main/resources/config/` 整个目录
- `sci-z-server/src/main/resources/application.yml`
- `sci-z-server/src/main/resources/application-dev.yml`
- `sci-z-server/src/main/resources/application-local.yml`
- `sci-z-server/src/main/resources/application-prod.yml`

## 前置条件

1. 能访问 GitHub（`git fetch origin` 可成功）
2. 当前本地 `toc` 分支已与 GitLab toc 同步（例如刚执行过 `git pull gitlab toc` 或已从 GitLab 合并过）

## 执行方式

在项目根目录执行其一即可：

```bash
# Git Bash / WSL / Linux
bash scripts/merge-gitlab-toc-to-github-toc.sh
```

```powershell
# PowerShell（项目根目录）
.\scripts\merge-gitlab-toc-to-github-toc.ps1
```

## 脚本步骤说明

1. `git fetch origin`：拉取 GitHub 最新（含 `origin/toc`）
2. `git checkout toc`：切换到本地 toc
3. `git merge origin/toc --no-edit`：把 GitHub toc 合并进当前 toc
4. `git checkout origin/toc -- <config 及 4 个 yml>`：用 **GitHub toc 上的版本**覆盖上述 config 目录和 4 个配置文件
5. 若有变更则提交，提交信息：`chore: keep config and application yml consistent with github toc (no api keys)`
6. `git push origin toc`：推送到 GitHub toc

## 若 fetch origin 失败

若当前环境无法访问 GitHub（如内网），可在能访问 GitHub 的机器或网络下执行上述脚本；或按脚本内顺序手动执行各条命令。
