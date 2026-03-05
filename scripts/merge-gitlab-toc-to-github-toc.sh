#!/bin/bash
# 将 gitlab toc 分支合并到 github toc 分支，并保持 config 与 4 个 application 配置文件与 github toc 一致（避免 API key 等敏感信息被推送）
# 使用前请确保能访问 GitHub（git fetch origin 成功）
# 在项目根目录执行：bash scripts/merge-gitlab-toc-to-github-toc.sh

set -e
cd "$(dirname "$0")/.."

echo "=== 1. 拉取 origin (github) toc ==="
git fetch origin

echo "=== 2. 切换到 toc 分支（当前应为 gitlab toc 内容）==="
git checkout toc

echo "=== 3. 将 origin/toc 合并到当前 toc（若有冲突需先解决）==="
git merge origin/toc --no-edit

echo "=== 4. 用 github toc 版本覆盖：config 目录与 4 个 application 配置文件 ==="
git checkout origin/toc -- \
  sci-z-server/src/main/resources/config \
  sci-z-server/src/main/resources/application.yml \
  sci-z-server/src/main/resources/application-dev.yml \
  sci-z-server/src/main/resources/application-local.yml \
  sci-z-server/src/main/resources/application-prod.yml

echo "=== 5. 提交：保持与 github toc 一致的配置（避免敏感信息）==="
git add -A
if git diff --cached --quiet; then
  echo "配置与 github toc 已一致，无需提交。"
else
  git commit -m "chore: keep config and application yml consistent with github toc (no api keys)"
fi

echo "=== 6. 推送到 github toc ==="
git push origin toc

echo "=== 完成：github toc 已更新，config 与 4 个 yml 与 github toc 保持一致 ==="
