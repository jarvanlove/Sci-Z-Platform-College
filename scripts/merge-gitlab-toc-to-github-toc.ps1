# 将 gitlab toc 分支合并到 github toc 分支，并保持 config 与 4 个 application 配置文件与 github toc 一致
# 使用前请确保能访问 GitHub（git fetch origin 成功）
# 在项目根目录执行：.\scripts\merge-gitlab-toc-to-github-toc.ps1

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

Write-Host "=== 1. 拉取 origin (github) toc ===" -ForegroundColor Cyan
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "git fetch origin 失败，请检查网络或 GitHub 访问" }

Write-Host "=== 2. 切换到 toc 分支 ===" -ForegroundColor Cyan
git checkout toc

Write-Host "=== 3. 将 origin/toc 合并到当前 toc ===" -ForegroundColor Cyan
git merge origin/toc --no-edit

Write-Host "=== 4. 用 github toc 版本覆盖 config 与 4 个 application 配置文件 ===" -ForegroundColor Cyan
git checkout origin/toc -- `
  sci-z-server/src/main/resources/config `
  sci-z-server/src/main/resources/application.yml `
  sci-z-server/src/main/resources/application-dev.yml `
  sci-z-server/src/main/resources/application-local.yml `
  sci-z-server/src/main/resources/application-prod.yml

Write-Host "=== 5. 提交：保持与 github toc 一致的配置 ===" -ForegroundColor Cyan
git add -A
git diff --cached --quiet
if ($LASTEXITCODE -eq 0) {
  Write-Host "配置与 github toc 已一致，无需提交。"
} else {
  git commit -m "chore: keep config and application yml consistent with github toc (no api keys)"
}

Write-Host "=== 6. 推送到 github toc ===" -ForegroundColor Cyan
git push origin toc

Write-Host "=== 完成：github toc 已更新 ===" -ForegroundColor Green
