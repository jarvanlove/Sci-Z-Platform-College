#!/usr/bin/env node
/**
 * afterShellExecution Hook：将 Agent 执行的 Shell 命令与输出摘要追加到 reports/hooks/audit-shell.log
 * 便于追溯 Agent 在会话中执行过哪些命令、结果如何。
 */
const fs = require("fs");
const path = require("path");

const LOG_HEADER =
  "# 本文件用途: 记录 Agent 每次执行 Shell 命令时的命令内容与输出，便于审计与排错。每行格式: [ISO时间] JSON";

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
  const logFile = path.join(logDir, "audit-shell.log");
  try {
    fs.mkdirSync(logDir, { recursive: true });
    ensureLogHeader(logFile);
    const ts = new Date().toISOString();
    fs.appendFileSync(logFile, `[${ts}] ${input}\n`, "utf8");
  } catch (_) {}
  process.exit(0);
}
main();
