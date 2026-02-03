#!/usr/bin/env node
/**
 * afterFileEdit Hook：将 Agent 文件编辑记录追加到 reports/hooks/audit-edits.log
 * 便于追溯哪些文件被 Agent 修改过。
 */
const fs = require("fs");
const path = require("path");

const LOG_HEADER =
  "# 本文件用途: 记录 Agent 每次编辑文件时的文件路径与修改摘要，便于追溯谁改了什么。每行格式: [ISO时间] JSON";

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
  const logFile = path.join(logDir, "audit-edits.log");
  try {
    fs.mkdirSync(logDir, { recursive: true });
    ensureLogHeader(logFile);
    const ts = new Date().toISOString();
    fs.appendFileSync(logFile, `[${ts}] ${input}\n`, "utf8");
  } catch (_) {}
  process.exit(0);
}
main();
