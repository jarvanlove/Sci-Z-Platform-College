#!/usr/bin/env node
/**
 * sessionEnd Hook：将会话结束信息追加到 reports/hooks/audit-session-end.log
 * 便于统计会话时长、结束原因等。
 */
const fs = require("fs");
const path = require("path");

const LOG_HEADER =
  "# 本文件用途: 记录每次 Agent/Composer 会话结束时的信息（结束原因、时长等），便于统计使用情况。每行格式: [ISO时间] JSON";

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
  const logDir = path.join(process.cwd(), "reports", "hooks");
  const logFile = path.join(logDir, "audit-session-end.log");
  try {
    fs.mkdirSync(logDir, { recursive: true });
    ensureLogHeader(logFile);
    const ts = new Date().toISOString();
    fs.appendFileSync(logFile, `[${ts}] ${input}\n`, "utf8");
  } catch (_) {}
  process.exit(0);
}
main();
