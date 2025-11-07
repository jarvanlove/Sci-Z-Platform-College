# Cursor 规则文件说明

## 重要提示

**Cursor 2.0 更新说明**：

在 Cursor 2.0 版本中，规则管理方式发生了变化：

1. **自动识别规则**：Cursor 2.0 会自动识别项目根目录下的 `CLAUDE.md` 或 `CLAUDE.local.md` 文件
2. **不再支持 `@` 引用**：在对话框中无法通过 `@` 符号直接引用 `.cursor/rules/` 目录下的 `.mdc` 文件
3. **统一规则文件**：建议将所有规则合并到项目根目录的 `CLAUDE.md` 文件中

## 当前规则文件

本目录下的规则文件已合并到项目根目录的 `CLAUDE.md` 文件中：

- `common-rules.mdc` - 通用技术回复规则
- `notes-rules.mdc` - 注释模板规则
- `current-project-rules.mdc` - 项目特定规则

## 使用方式

1. **推荐方式**：使用项目根目录的 `CLAUDE.md` 文件（Cursor 2.0 会自动识别）
2. **保留原有文件**：`.cursor/rules/` 目录下的 `.mdc` 文件已保留作为备份参考

## 如何验证规则是否生效

1. 打开 Cursor 设置 → Rules, Memories, Commands
2. 确认 "Include CLAUDE.md in context" 开关已启用
3. 在对话框中输入问题，AI 应该会按照规则回复

## 注意事项

- `CLAUDE.md` 文件会随项目一起提交到版本控制系统
- `CLAUDE.local.md` 文件是本地文件，不会被提交（适合个人定制规则）
- 如果同时存在 `CLAUDE.md` 和 `CLAUDE.local.md`，两者都会生效
