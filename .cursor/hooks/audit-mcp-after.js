#!/usr/bin/env node
/**
 * afterMCPExecution Hook：在 MCP 工具执行完成后，将工具名、耗时、结果摘要写入日志，便于审计与排错。
 *
 * 输入（stdin JSON）：
 *   - tool_name, tool_input, result_json（工具返回的 JSON 字符串）, duration（毫秒）
 * 输出：无（本事件不消费输出）
 *
 * 日志位置：reports/hooks/audit-mcp-after.log
 */
const fs = require("fs");
const path = require("path");

const LOG_DIR = "reports/hooks";
const LOG_FILE = "audit-mcp-after.log";
const RESULT_PREVIEW_LEN = 400;
const LOG_HEADER =
  "# 本文件用途: 记录 MCP 工具每次执行完成后的工具名、耗时与结果摘要，便于审计与排错。每行格式: [ISO时间] tool=名称 duration_ms=毫秒 result=摘要";

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
    process.exit(0);
  }

  let payload = {};
  try {
    payload = JSON.parse(input);
  } catch (_) {
    process.exit(0);
  }

  const tool_name = payload.tool_name || "unknown";
  const duration = payload.duration != null ? payload.duration : 0;
  let resultPreview =
    typeof payload.result_json === "string"
      ? payload.result_json
      : JSON.stringify(payload.result_json || "");
  if (resultPreview.length > RESULT_PREVIEW_LEN)
    resultPreview = resultPreview.slice(0, RESULT_PREVIEW_LEN) + "...";
  resultPreview = resultPreview.replace(/\s+/g, " ");

  const ts = new Date().toISOString();
  const logDir = path.join(process.cwd(), LOG_DIR);
  const logPath = path.join(logDir, LOG_FILE);
  try {
    fs.mkdirSync(logDir, { recursive: true });
    ensureLogHeader(logPath);
    fs.appendFileSync(
      logPath,
      `[${ts}] tool=${tool_name} duration_ms=${duration} result=${resultPreview}\n`,
      "utf8"
    );
  } catch (_) {}

  process.exit(0);
}

main();
