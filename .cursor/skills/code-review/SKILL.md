---
name: code-review
description: "Executes a code review workflow: determine scope, run lint/tests (optional script), check by dimensions, output report. Use when user asks for code review (代码审查) or /code-review. 执行体：按步骤跑检查并输出报告。"
---

# 代码审查（执行体）

## Instructions

**按下列步骤依次执行，不可跳过。**

1. **确定审查范围**

   - 若用户指定了文件/目录，则仅审查该范围；若未指定，则审查当前打开文件或最近变更（如 git diff）。
   - 明确列出将要审查的文件路径。

2. **（可选）执行自动化检查**

   - 若项目根或 `sci-z-web` / `sci-z-server` 下有可用的 lint/test 命令，则执行并解析结果：
     - 前端：在 `sci-z-web` 下执行 `npm run lint`（若有），捕获输出。
     - 后端：在 `sci-z-server` 下执行 `mvn test -q` 或 `mvn verify -q`（若有），捕获失败用例。
   - 将自动化结果纳入下方「输出报告」的对应维度（如代码质量、功能性）。

3. **按维度逐项执行人工检查**

   - 阅读代码，对每个维度做出通过/待改进判断，并记录 `文件:行号` 或代码片段与具体建议：
     - **功能性**：是否实现预期功能，边界与异常是否处理，是否有明显 bug。
     - **代码质量**：可读性、结构、命名、单一职责、重复代码、是否遵循项目规范（Vue 见 sci-z-web，Java 见 sci-z-server）。
     - **性能**：多余计算、重复请求、N+1、分页/懒加载、阻塞操作、资源释放。
     - **可维护性与扩展性**：职责清晰度、配置/常量集中、扩展性、关键逻辑注释。
     - **安全性**：漏洞、输入校验、敏感数据处理、硬编码密钥。

4. **输出报告**
   - 必须按本 Skill 下方「输出模板」生成一份完整报告，包含每个维度的通过/待改进、具体位置与建议，以及优先级汇总。

## Scripts

- 无强制脚本。若存在 `scripts/run-checks.sh` 或 `scripts/run-checks.ps1`，可在步骤 2 中调用以统一执行 lint + test 并解析输出；若无则仅用步骤 3 的人工检查。

## 输出模板

```markdown
# 代码审查报告

## 审查范围

- 文件列表：...

## 功能性

- [ ] 通过 / [ ] 待改进
      （若待改进）`文件:行号` 或片段 + **具体建议**

## 代码质量

- [ ] 通过 / [ ] 待改进
      ...

## 性能

- [ ] 通过 / [ ] 待改进
      ...

## 可维护性与扩展性

- [ ] 通过 / [ ] 待改进
      ...

## 安全性

- [ ] 通过 / [ ] 待改进
      ...

## 优先级汇总

- 🔴 必须修复：（安全、正确性、性能）
- 🟡 建议改进：（风格、可维护性）
```

## Guidelines

- 规范细节以项目为准：前端见 `sci-z-web/CLAUDE.md` 与 `.cursor/rules/`，后端见 `sci-z-server/CLAUDE.md` 与 `.cursor/rules/`。
- 优先标注关键问题（安全、正确性、性能），再提风格与可维护性。
