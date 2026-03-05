---
title: Claude Code CLI 使用指南
编写时间: 2026-02-09
维护人: 研发
---

# Claude Code CLI 使用指南（新用户）

Claude Code 是 Anthropic 推出的命令行 AI 编程助手，可在终端中与 Claude 对话、执行代码与文件操作。本文为常用命令与使用方式的速查与入门说明。

---

## 一、安装与前置

- **安装**：参见 [Quickstart - Claude Code](https://docs.anthropic.com/en/docs/claude-code/quickstart)。
- **账号**：需具备 Claude 订阅（Pro / Max / Teams / Enterprise）、Claude Console 账号或支持的云厂商接入。
- **常用入口**：在项目目录下打开终端，使用下方命令。

---

## 二、常用命令速查

| 命令 | 说明 | 示例 |
|------|------|------|
| `claude` | 启动**交互式对话**（REPL） | `claude` |
| `claude "你的问题"` | 带初始问题的交互对话 | `claude "解释这段代码"` |
| `claude -p "你的问题"` | **非交互**执行一次请求后退出（适合脚本/CI） | `claude -p "列出未使用的导入"` |
| `claude -c` | **继续**当前目录下最近一次对话 | `claude -c` |
| `claude -r "会话名或ID" "问题"` | **恢复**指定会话并提问 | `claude -r "auth-refactor" "完成这个 PR"` |
| `claude update` | 更新到最新版本 | `claude update` |
| `claude mcp` | 配置 MCP（Model Context Protocol）服务器 | `claude mcp` |
| `claude -v` | 查看版本号 | `claude -v` |

**管道用法**：把文件内容交给 Claude 处理：

```bash
cat 文件路径 | claude -p "总结/解释/检查"
```

---

## 三、常用参数（Flags）

| 参数 | 说明 | 示例 |
|------|------|------|
| `-p` / `--print` | 非交互模式，输出后直接退出 | `claude -p "query"` |
| `-c` / `--continue` | 加载当前目录最近一次对话 | `claude -c` |
| `-r` / `--resume` | 恢复指定会话（ID 或名称） | `claude -r "session-name"` |
| `--add-dir` | 让 Claude 可访问的额外目录 | `claude --add-dir ../lib ./tools` |
| `--model` | 指定模型（如 `sonnet`、`opus` 或完整型号名） | `claude --model sonnet` |
| `--append-system-prompt` | 在默认系统提示后**追加**说明（不改默认能力） | `claude --append-system-prompt "用 TypeScript"` |
| `--system-prompt` | **完全替换**系统提示（慎用） | `claude --system-prompt "你是 Python 专家"` |
| `--tools` | 限制可用工具（如 `Bash,Edit,Read`），或 `"default"` / 空 | `claude --tools "Read,Grep"` |
| `--debug` | 开启调试（可指定类别，如 `api,mcp`） | `claude --debug "api"` |
| `--verbose` | 输出更详细日志 | `claude --verbose` |
| `--output-format json` | 非交互时以 JSON 输出（便于脚本解析） | `claude -p "query" --output-format json` |

---

## 四、典型使用场景

### 4.1 在项目里随便聊聊 / 问代码

```bash
cd 你的项目目录
claude
# 或带第一句问题
claude "这个项目是做什么的？入口在哪？"
```

### 4.2 一次性任务（不进入交互）

```bash
claude -p "检查 src 下未使用的导出"
claude -p "给这个函数写单元测试"
```

### 4.3 接着上次的对话做

```bash
claude -c
# 或带新指令
claude -c -p "按刚才的方案继续改"
```

### 4.4 指定会话继续

```bash
claude -r "auth-refactor" "把登录逻辑收口到 AuthService"
```

### 4.5 用管道处理文件

```bash
cat package.json | claude -p "列出所有依赖并标出可能过时的"
cat error.log | claude -p "分析错误原因并给出修复建议"
```

### 4.6 固定项目规范（追加系统提示）

```bash
claude --append-system-prompt "本项目用 Vue 3 + TypeScript，遵循 .cursor/rules 中的规范"
```

---

## 五、权限与安全

- 执行写文件、跑命令等操作时，CLI 会**询问是否允许**。
- `--allowedTools`：指定**自动放行**的工具（需了解 [权限规则语法](https://docs.anthropic.com/en/docs/claude-code/settings#permission-rule-syntax)）。
- `--permission-mode plan`：先只做规划，不直接执行。
- 不建议在不可信环境使用 `--dangerously-skip-permissions`。

---

## 六、参考链接

| 内容 | 链接 |
|------|------|
| 官方 CLI 参考 | [CLI reference](https://docs.anthropic.com/en/docs/claude-code/cli-reference) |
| 快速开始 | [Quickstart](https://docs.anthropic.com/en/docs/claude-code/quickstart) |
| 交互模式说明 | [Interactive mode](https://docs.anthropic.com/en/docs/claude-code/interactive-mode) |
| 配置与设置 | [Settings](https://docs.anthropic.com/en/docs/claude-code/settings) |
| 无头/程序化调用 | [Run Claude Code programmatically](https://docs.anthropic.com/en/docs/claude-code/headless) |

---

## 修订记录

| 日期       | 修订要点 |
|------------|----------|
| 2026-02-09 | 初稿：常用命令、参数、场景与参考链接 |
