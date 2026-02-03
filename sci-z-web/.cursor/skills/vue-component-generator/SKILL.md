---
name: vue-component-generator
description: "Executes Vue component creation: confirm requirement, choose path, create .vue file, update i18n if needed, run lints and fix. Use when user asks to create Vue component (创建 Vue 组件) or /create-vue-component. 执行体：按步骤创建文件并校验。"
---

# Vue 组件生成器（执行体）

## Instructions

**按下列步骤依次执行，完成从需求到可用的组件文件。**

1. **确认需求与放置位置**
   - 向用户确认或从上下文提取：组件功能、Props、Events、所属模块。
   - 确定类型与路径：
     - 业务组件：`src/components/Business/<模块>/<ComponentName>.vue`
     - 通用组件：`src/components/Common/<ComponentName>.vue` 或 `Base<Name>.vue`
   - 文件名使用 PascalCase，与组件名一致。

2. **创建组件文件**
   - 在确定路径下**创建** `.vue` 文件（使用 Write 工具或等价能力）。
   - 内容必须包含：`<script setup>`、Composition API、Props/Emits 定义（含类型或 JSDoc）；`<template>`；`<style lang="scss" scoped>`，间距等优先使用 `var(--gap-*)`。
   - 文案使用 `$t('key')`；若新增文案，则进入步骤 3。

3. **（若新增文案）更新国际化**
   - 在 `src/locales/` 下为 zh-CN、en-US、ja-JP、ko-KR 四个文件添加同一 key 的翻译，保证键一致。

4. **校验与修复**
   - 对新建或修改的文件运行 read_lints（或项目中的 `npm run lint`）；若有报错则逐条修复直至通过。
   - 确认接口从 `api/<模块>` 导入，状态用 Pinia（如适用）。

5. **向用户汇报**
   - 列出已创建/修改的文件路径；若有 i18n 新增键，列出 key 与文件；若有脚本可跑，可建议用户执行 `npm run dev` 自测。

## Scripts

- **scripts/scaffold-component.js**（可选）：接收组件名、类型（business|common）、模块名，在正确路径下生成最小 .vue 骨架。若不存在则由 Agent 直接写入完整内容。

## Guidelines

- 组件与架构规范以 `sci-z-web/.cursor/rules/` 及 `sci-z-web/CLAUDE.md` 为准；本 Skill 只规定执行顺序与产出物，不重复罗列约束细节。
