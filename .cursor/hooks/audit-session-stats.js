#!/usr/bin/env node
/**
 * sessionEnd Hook（与 audit-session-end.js 可同时挂）：将会话级统计写入 session-stats.log，
 * 便于按日/月汇总（会话数、总时长等）。若 Cursor 未来在 payload 中提供 token 相关字段，会一并记录。
 *
 * 输入（stdin JSON）：sessionEnd 的完整 payload，常见字段包括：
 *   - session_id, reason, duration_ms, is_background_agent, final_status, error_message
 *   - 以及通用字段: model, conversation_id 等（若 Cursor 提供）
 *
 * 输出文件：reports/hooks/session-stats.log，首行为用途说明，后续每行一条会话记录（TSV 便于解析）。
 */
const fs = require("fs");
const path = require("path");

const LOG_DIR = "reports/hooks";
const LOG_FILE = "session-stats.log";
const LOG_HEADER =
  "# 本文件用途: 记录每次会话的模型、时长、结束原因等，用于按日/月统计使用量与 token 估算。每行: date\tsession_id\tmodel\treason\tduration_ms\t[其他字段 JSON]";

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

  const now = new Date();
  const date = now.toISOString().slice(0, 10);
  const session_id = payload.session_id || "";
  const model = payload.model || "";
  const reason = payload.reason || "";
  const duration_ms = payload.duration_ms != null ? payload.duration_ms : 0;
  const rest = { ...payload };
  delete rest.session_id;
  delete rest.model;
  delete rest.reason;
  delete rest.duration_ms;
  const restStr = JSON.stringify(rest);

  const logDir = path.join(process.cwd(), LOG_DIR);
  const logPath = path.join(logDir, LOG_FILE);
  try {
    fs.mkdirSync(logDir, { recursive: true });
    ensureLogHeader(logPath);
    const line = `${date}\t${session_id}\t${model}\t${reason}\t${duration_ms}\t${restStr}\n`;
    fs.appendFileSync(logPath, line, "utf8");
  } catch (_) {}

  process.exit(0);
}

main();
