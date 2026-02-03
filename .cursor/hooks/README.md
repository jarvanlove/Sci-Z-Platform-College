# Hooks 配置说明

Cursor 的 Hooks 通过 **项目内的配置文件 + 脚本** 来配置，**设置界面里没有“新增”按钮**，也不会在空白处编辑。配置好后，Settings → Hooks 会显示已加载的 Hooks 数量与执行日志。

- 官方文档：[Hooks](https://cursor.com/cn/docs/agent/hooks)
- 第三方/Claude Code 兼容：[Third Party Hooks](https://cursor.com/cn/docs/agent/third-party-hooks)

---

## 本项目的 Hooks 是如何配置的

- **配置文件**：项目根目录 **`.cursor/hooks.json`**（与 `.cursor/hooks/` 同级，不是本目录下的 `hooks.json`）。
- **脚本目录**：**`.cursor/hooks/`**，所有脚本放在此目录，在 `hooks.json` 里用相对**项目根**的路径引用（如 `node .cursor/hooks/audit-edit.js`、`python .cursor/hooks/xxx.py`、PowerShell/`.cmd` 等，见下方「脚本语言与运行环境」）。
- **运行环境**：脚本从**项目根目录**作为当前工作目录运行；支持 Node.js、Python、Bash、Windows（PowerShell/.cmd）等，按业务需求选择。

---

## 当前已配置的 Hooks

| 事件                     | 脚本                                             | 作用                                                                                                   |
| ------------------------ | ------------------------------------------------ | ------------------------------------------------------------------------------------------------------ |
| **sessionStart**         | `session-start.js`                               | 会话开始时从 `.cursor/hooks/context/session-prompt.txt` 注入项目约定到上下文。                         |
| **sessionEnd**           | `audit-session-end.js`、`audit-session-stats.js` | 会话结束时写完整 JSON 到 `audit-session-end.log`，并写结构化记录到 `session-stats.log` 供按日/月汇总。 |
| **beforeSubmitPrompt**   | `audit-prompt.js`                                | 发送前记录提示词到 `reports/hooks/audit-prompts.log`，并拦截空提示。                                   |
| **afterFileEdit**        | `audit-edit.js`                                  | Agent 编辑文件后写入 `reports/hooks/audit-edits.log`。                                                 |
| **beforeReadFile**       | `guard-read-file.js`                             | 读取前拦截敏感文件（.env、密钥、password 等），其余放行。                                              |
| **beforeShellExecution** | `guard-shell.js`                                 | 仅当命令匹配 `matcher` 时要求用户确认高危命令。                                                        |
| **afterShellExecution**  | `audit-shell.js`                                 | Shell 执行后将命令与输出写入 `reports/hooks/audit-shell.log`。                                         |
| **beforeMCPExecution**   | `audit-mcp-before.js`                            | MCP 调用前记录到 `reports/hooks/audit-mcp-before.log`，当前一律放行。                                  |
| **afterMCPExecution**    | `audit-mcp-after.js`                             | MCP 调用后记录到 `reports/hooks/audit-mcp-after.log`。                                                 |
| **subagentStop**         | `save-subagent-result.js`                        | 子代理结束时将结果保存到 `reports/subagents/`。                                                        |

脚本均为完整实现（非片段），详见 `.cursor/hooks/` 下各文件头部注释。  
查看执行情况：**Cursor Settings → Tools & MCP → Hooks**，看 **Configured Hooks** 数量与 **Execution Log**。

---

## 脚本实用性分类（便于按需保留或精简）

| 分类                   | 脚本                                        | 说明                                                             |
| ---------------------- | ------------------------------------------- | ---------------------------------------------------------------- |
| **高实用性，建议保留** | `session-start.js`                          | 会话开场注入项目约定，模型一进来就带上下文。                     |
|                        | `audit-prompt.js`                           | 记录每次提示 + 拦截空提示，审计与防误触。                        |
|                        | `audit-edit.js`                             | 记录 Agent 改了哪些文件，便于追溯。                              |
|                        | `guard-read-file.js`                        | 禁止读取 .env、密钥等，安全必备。                                |
|                        | `guard-shell.js`                            | 高危命令前弹确认，防误删/误执行。                                |
|                        | `audit-shell.js`                            | 记录 Agent 执行过的命令与输出，审计必备。                        |
|                        | `audit-session-stats.js`                    | 会话结构化统计，供按日/月汇总与估算用量。                        |
|                        | `save-subagent-result.js`                   | 子代理（verifier、debugger）结果落盘，便于复查。                 |
|                        | `aggregate-session-stats.js`                | 非 Hook，命令行工具：按日/月汇总 session-stats.log。             |
| **中等 / 按需保留**    | `audit-session-end.js`                      | 会话结束写完整 JSON；做统计用 session-stats 即可，保留可作调试。 |
|                        | `audit-mcp-before.js`、`audit-mcp-after.js` | 仅在使用 MCP 且需审计时有用；不用 MCP 可关。                     |
| **已移除**             | `audit-agent-response.js`                   | 已从 hooks 中去掉（日志涨得快、与对话重复）。                    |

若希望继续精简：可从 `hooks.json` 中移除 **audit-session-end**（若只关心汇总不关心原始 JSON）、以及 **audit-mcp-before/after**（若不用 MCP），并删除或保留对应脚本文件即可。

---

## 各 Hook 详解（含义与触发时机）

| 脚本                        | 事件                 | 何时调用                                                                                                    |
| --------------------------- | -------------------- | ----------------------------------------------------------------------------------------------------------- |
| **session-start.js**        | sessionStart         | **每次新建 Agent/Composer 会话时**；向会话注入 `.cursor/hooks/context/session-prompt.txt` 中的项目约定。    |
| **audit-session-end.js**    | sessionEnd           | **每次关闭或结束当前会话时**；写完整 JSON 到 `audit-session-end.log`。                                      |
| **audit-session-stats.js**  | sessionEnd           | **每次关闭或结束当前会话时**；写 date、model、reason、duration_ms 等到 `session-stats.log`，供按日/月汇总。 |
| **audit-prompt.js**         | beforeSubmitPrompt   | **每次你点击发送提示前**；记录提示词到 `audit-prompts.log`，空提示会拦截并提示输入。                        |
| **audit-edit.js**           | afterFileEdit        | **每次 Agent 通过工具修改项目内文件后**；写编辑记录到 `audit-edits.log`。                                   |
| **guard-read-file.js**      | beforeReadFile       | **每次 Agent 要读取某个文件前**；路径命中敏感规则（.env、密钥等）则拒绝，否则放行。                         |
| **guard-shell.js**          | beforeShellExecution | **仅当 Agent 要执行的命令**匹配 `matcher` 时；对高危命令弹确认，其它不经过本脚本。                          |
| **audit-shell.js**          | afterShellExecution  | **每次 Agent 执行完一条 Shell 命令后**；写命令与输出到 `audit-shell.log`。                                  |
| **audit-mcp-before.js**     | beforeMCPExecution   | **每次 Agent 调用 MCP 工具前**；记录到 `audit-mcp-before.log` 并放行。                                      |
| **audit-mcp-after.js**      | afterMCPExecution    | **每次 MCP 工具执行完成后**；记录到 `audit-mcp-after.log`。                                                 |
| **save-subagent-result.js** | subagentStop         | **子代理（verifier、debugger 等）结束时**；将 result 保存到 `reports/subagents/`。                          |

---

## 已实现的扩展 Hooks（完整脚本）

以下 Hook 均已实现为**完整脚本**（非片段），可直接使用；修改行为请编辑 `.cursor/hooks/` 下对应文件。

| 事件                                           | 脚本                                        | 说明                                                                                      |
| ---------------------------------------------- | ------------------------------------------- | ----------------------------------------------------------------------------------------- |
| **sessionStart**                               | `session-start.js`                          | 从 `.cursor/hooks/context/session-prompt.txt` 注入项目约定；可编辑该 txt 自定义开场提示。 |
| **beforeSubmitPrompt**                         | `audit-prompt.js`                           | 记录提示到 `audit-prompts.log`，空提示拦截。                                              |
| **beforeReadFile**                             | `guard-read-file.js`                        | 拒绝 .env、密钥、password 等敏感路径；可改脚本内 `DENY_PATTERNS`。                        |
| **beforeMCPExecution** / **afterMCPExecution** | `audit-mcp-before.js`、`audit-mcp-after.js` | MCP 调用前后记录到 `audit-mcp-before.log`、`audit-mcp-after.log`。                        |

---

## 如何自行添加新的 Hook

### 1. 编辑 `.cursor/hooks.json`

在项目根目录打开 **`.cursor/hooks.json`**，在 `hooks` 里增加或修改事件与命令。例如新增「在每次执行 Shell 命令后做审计」：

```json
{
  "version": 1,
  "hooks": {
    "afterFileEdit": [{ "command": "node .cursor/hooks/audit-edit.js" }],
    "sessionEnd": [{ "command": "node .cursor/hooks/audit-session-end.js" }],
    "afterShellExecution": [
      { "command": "node .cursor/hooks/your-new-script.js" }
    ]
  }
}
```

- **事件名**必须与 Cursor 支持的 Hook 事件一致（见下方「常用事件一览」）。
- **command**：从项目根可调用的命令，可以是任意可执行脚本（见下方「脚本语言与运行环境」）。

### 2. 编写脚本并放在 `.cursor/hooks/`

- 脚本从 **stdin** 接收 JSON 输入，根据事件类型格式不同（见官方文档 [参考 - 钩子事件](https://cursor.com/cn/docs/agent/hooks)）。
- 若该事件需要**输出**（如 `beforeShellExecution` 需返回 `permission`），将 JSON 写到 **stdout**，然后 `process.exit(0)`；不需要输出的事件（如 `afterFileEdit`、`sessionEnd`）直接退出即可。
- **退出码**：`0` 表示成功；`2` 表示阻止操作（相当于 `permission: "deny"`）。

### 3. 重启 Cursor

保存 `hooks.json` 和脚本后，**重启 Cursor**，新 Hook 才会被加载。之后在 Settings → Hooks 中可看到数量变化及 Execution Log。

---

## 常用事件一览（便于自行扩展）

| 事件                                           | 说明             | 典型用途                 |
| ---------------------------------------------- | ---------------- | ------------------------ |
| **sessionStart**                               | 会话开始         | 注入环境变量、额外上下文 |
| **sessionEnd**                                 | 会话结束         | 审计、统计、清理         |
| **afterFileEdit**                              | Agent 编辑文件后 | 格式化、记录编辑日志     |
| **beforeReadFile**                             | Agent 读文件前   | 访问控制、脱敏           |
| **beforeShellExecution**                       | 执行 Shell 前    | 高危命令门控、审批       |
| **afterShellExecution**                        | 执行 Shell 后    | 审计命令与输出           |
| **beforeMCPExecution** / **afterMCPExecution** | MCP 调用前后     | MCP 审计、限权           |
| **subagentStart** / **subagentStop**           | 子代理启停       | 子代理审批、结果落盘     |
| **beforeSubmitPrompt**                         | 用户发送提示前   | 校验、拦截不当提示       |
| **stop**                                       | Agent 循环结束   | 自动续发消息、统计       |

完整事件与入参/出参见：[官方文档 - 钩子事件](https://cursor.com/cn/docs/agent/hooks)。

---

## 脚本语言与运行环境

脚本**不限于 Node.js**，可根据**业务需求**选择最合适的语言与形式，在 `hooks.json` 的 `command` 里写清调用方式即可。

| 语言/环境   | 示例 command                                                            | 说明                                                                       |
| ----------- | ----------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| **Node.js** | `node .cursor/hooks/audit-edit.js`                                      | 跨平台、本项目当前示例；需本机已安装 `node`。                              |
| **Python**  | `python .cursor/hooks/audit.py` 或 `python3 ...`                        | 适合复杂解析、YAML/多文档处理等；需本机已安装 Python。                     |
| **Bash**    | `.cursor/hooks/audit.sh`                                                | Linux/macOS 或 Windows 上 Git Bash；脚本需可执行（`chmod +x`）。           |
| **macOS**   | `bash .cursor/hooks/audit.sh`、`zsh .cursor/hooks/audit.sh`             | macOS 自带 Bash/Zsh，`.sh` 可直接用；也可用 `python3`/`node`（若已安装）。 |
| **Windows** | `powershell -File .cursor/hooks/audit.ps1` 或 `.cursor/hooks/audit.cmd` | 在 Windows 上可直接用 PowerShell、`.cmd`/`.bat`，按业务需要设计脚本逻辑。  |

- **设计原则**：根据业务需求来设计脚本——例如仅记日志用轻量脚本即可；需要解析 YAML/做复杂判断时可选用 Python；**macOS** 上可优先 Bash/Zsh 或 Python/Node；**Windows** 环境下可优先 PowerShell 或 .cmd，保证 Cursor 能通过 `command` 正常唤起即可。
- 脚本统一从 **stdin** 读 JSON，需要返回结果时向 **stdout** 输出 JSON；退出码 `0` 为成功，`2` 为阻止操作。

---

## 输出路径说明（报告类 Hook 的落盘位置）

- **会话开场**：`session-start.js` 从 **`.cursor/hooks/context/session-prompt.txt`** 读取并注入。该文件用途：每次新建 Agent/Composer 会话时，其内容会作为 `additional_context` 注入到对话中，让模型一开场就了解项目约定；可直接编辑该 txt 修改开场说明。
- **审计日志**：均在 **`reports/hooks/`** 下，路径相对**项目根**。**每个日志文件在首次写入时会在第一行写入一行注释**，说明该文件用途与每行格式，便于你查看时区分。所有日志与报告文件均以 **UTF-8（带 BOM）** 写入，避免在 Windows 下用记事本等打开时出现乱码。
- **子代理结果**：`save-subagent-result.js` 写入 **`reports/subagents/`**。
- 若不想纳入版本控制，可将 `reports/hooks/`、`reports/subagents/` 加入 `.gitignore`。

### reports/hooks 下各日志文件用途（首行注释会写明）

| 文件名                | 用途                                                                                                                                                             |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| audit-edits.log       | Agent 每次编辑文件的路径与修改摘要                                                                                                                               |
| audit-session-end.log | 每次会话结束的完整 JSON（原因、时长等）                                                                                                                          |
| session-stats.log     | 会话级统计（日期、session_id、model、reason、duration_ms），供按日/月汇总                                                                                        |
| audit-prompts.log     | 用户每次发送的提示词摘要与附件数（**每发一条消息记一行**；若某行里出现脚本名如 audit-agent-response.js，那是你当时输入并发送的那句话的内容，不是该脚本仍在运行） |
| audit-shell.log       | Agent 执行的 Shell 命令与输出                                                                                                                                    |
| audit-mcp-before.log  | MCP 调用前的工具名与参数摘要                                                                                                                                     |
| audit-mcp-after.log   | MCP 调用后的工具名、耗时与结果摘要                                                                                                                               |

### 日志文件大小与长期使用

- **单文件能存多少**：脚本未设上限，理论上受磁盘与 Node 限制；单文件几百 MB 一般没问题，再大建议归档或轮转。
- **时间久了文件很大怎么办**：可 (1) 定期手动将旧日志移到 `reports/hooks/archive/` 或删除；(2) 或在本目录下自行加「按大小/按天轮转」逻辑（例如超过 10MB 时重命名为 `.log.old` 再新建空文件）。需要时可扩展脚本实现自动轮转。

---

## 会话统计与 Token 用量（按日/月汇总）

- **会话级记录**：`audit-session-stats.js` 在 **sessionEnd** 时写入 `reports/hooks/session-stats.log`，记录当次会话的 **model、reason、duration_ms** 等（若 Cursor 在 payload 中提供 token 相关字段，会一并写入）。
- **在 Cursor 对话里每句显示 token**：目前 Cursor 未向 Hooks 暴露「每句请求的 token 数」或「上下文 K 数」，因此 **Hooks 无法在 AI 对话界面中直接显示 token**；若 Cursor 后续在设置或 API 中提供，再可对接。Rules 仅能写静态说明，无法拿到运行时 token 数据，故**无法用 Rules 实现该需求**。
- **按日/月统计**：在项目根目录执行  
  `node .cursor/hooks/aggregate-session-stats.js`  
  可汇总 **session-stats.log** 的会话次数与总时长（按日、按月）。用于估算使用量；若 Cursor 未来在 sessionEnd 中提供 token 字段，可在 `aggregate-session-stats.js` 中扩展汇总 token。
  - 参数可选：`日` 或 `day` 仅按日汇总，`月` 或 `month` 仅按月汇总，不传或 `全部` 则两者都输出。

---

## 故障排查

- **Configured Hooks 仍为 0**：确认 `.cursor/hooks.json` 在**项目根**、JSON 格式正确，且路径为 `.cursor/hooks/xxx.js`（从项目根可执行）；保存后**重启 Cursor**。
- **Hook 未触发**：在 Settings → Hooks 的 **Execution Log** 查看是否有执行记录或报错；可打开 Cursor 的 **Hooks 输出通道** 查看脚本 stderr。
- **脚本无法执行**：确认 `command` 中的解释器或可执行文件在本机可用（如 `node`、`python`/`python3`、`powershell`）；若用 `.sh`，在 Windows 上需 Cursor 能调用到 Bash（如 Git Bash）。
