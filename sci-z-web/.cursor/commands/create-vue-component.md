# 创建 Vue 组件

## 概述

按项目规范快速创建 Vue 组件。

## 步骤

1. **确定组件类型**
   - 业务组件：`components/Business/<模块>/`（如 User、Declaration、Project）
   - 通用组件：`components/Common/`

2. **生成结构**
   - `<script setup>` + Composition API
   - Props/Emits 定义清晰
   - 样式 `lang="scss" scoped`，优先 `var(--gap-*)`
   - 文案用 `$t()`，新增键值同步四语言

3. **验证**
   - 命名符合 CamelCase/PascalCase
   - 接口从 `api/<模块>` 导入
   - 无 read_lints 警告
