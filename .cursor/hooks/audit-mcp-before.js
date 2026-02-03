#!/usr/bin/env node
/**
 * beforeMCPExecution Hook：在 Agent 调用 MCP 工具之前记录调用信息（工具名、参数摘要），并决定是否放行。
 *
 * 输入（stdin JSON）：
 *   - tool_name, tool_input（JSON 字符串或对象）, 以及 url 或 command 等
 * 输出（stdout JSON）：
 *   - permission: "allow" | "deny" | "ask"
 *   - user_message（可选）
 *   - agent_message（可选）
 *
 * 当前策略：仅审计、一律放行。若需对特定 MCP 做白名单或审批，可在此扩展。
 */
const fs = require("fs");
const path = require("path");

const LOG_DIR = "reports/hooks";
const LOG_FILE = "audit-mcp-before.log";
const LOG_HEADER =
  "# 本文件用途: 记录 Agent 每次调用 MCP 工具前的工具名与参数摘要，便于审计。每行格式: [ISO时间] tool=名称 input=摘要";

const UTF8_BOM = "\uFEFF";
function ensureLogHeader(logPath) {
  try {
    if (!fs.existsSync(logPath) || fs.statSync(logPath).size === 0) {
      fs.writeFileSync(logPath, UTF8_BOM + LOG_HEADER + "\n", "utf8");
    }
  } catch (_) {}
}

function main() {
  let input = "";
  try {
    input = fs.readFileSync(0, "utf8");
  } catch (_) {
    console.log(JSON.stringify({ permission: "allow" }));
    process.exit(0);
  }

  let payload = {};
  try {
    payload = JSON.parse(input);
  } catch (_) {
    console.log(JSON.stringify({ permission: "allow" }));
    process.exit(0);
  }

  const tool_name = payload.tool_name || "unknown";
  let tool_input_str = payload.tool_input;
  if (typeof tool_input_str === "object")
    tool_input_str = JSON.stringify(tool_input_str);
  else if (typeof tool_input_str !== "string") tool_input_str = "";
  const inputPreview =
    tool_input_str.length > 300
      ? tool_input_str.slice(0, 300) + "..."
      : tool_input_str;

  const ts = new Date().toISOString();
  const logDir = path.join(process.cwd(), LOG_DIR);
  const logPath = path.join(logDir, LOG_FILE);
  try {
    fs.mkdirSync(logDir, { recursive: true });
    ensureLogHeader(logPath);
    fs.appendFileSync(
      logPath,
      `[${ts}] tool=${tool_name} input=${inputPreview.replace(/\s+/g, " ")}\n`,
      "utf8"
    );
  } catch (_) {}

  console.log(JSON.stringify({ permission: "allow" }));
  process.exit(0);
}

main();
