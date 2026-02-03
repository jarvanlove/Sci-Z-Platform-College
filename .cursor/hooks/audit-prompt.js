#!/usr/bin/env node
/**
 * beforeSubmitPrompt Hook：在用户点击发送前记录提示词，并可选择拦截空提示或敏感内容。
 *
 * 输入（stdin JSON）：
 *   - prompt（用户输入的文本）, attachments（附件列表）
 * 输出（stdout JSON）：
 *   - continue: true | false
 *   - user_message（可选）：continue 为 false 时展示给用户
 *
 * 行为：
 *   1. 将每次提交的 prompt 与时间戳追加到 reports/hooks/audit-prompts.log
 *   2. 若 prompt 为空或仅空白，返回 continue: false 并提示用户
 *   3. 其余情况放行
 */
const fs = require("fs");
const path = require("path");

const LOG_DIR = "reports/hooks";
const LOG_FILE = "audit-prompts.log";
const LOG_HEADER =
  "# 本文件用途: 记录用户每次发送给 Agent 的提示词（前 500 字）与附件数量，便于复盘与统计。每行格式: [ISO时间][附件: N 个] 提示摘要";

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
    console.log(JSON.stringify({ continue: true }));
    process.exit(0);
  }

  let payload = {};
  try {
    payload = JSON.parse(input);
  } catch (_) {
    console.log(JSON.stringify({ continue: true }));
    process.exit(0);
  }

  const prompt =
    typeof payload.prompt === "string" ? payload.prompt.trim() : "";
  const attachments = payload.attachments || [];
  const ts = new Date().toISOString();

  const logDir = path.join(process.cwd(), LOG_DIR);
  const logPath = path.join(logDir, LOG_FILE);
  try {
    fs.mkdirSync(logDir, { recursive: true });
    ensureLogHeader(logPath);
    const attachmentSummary =
      attachments.length > 0 ? ` [附件: ${attachments.length} 个]` : "";
    const raw = `${prompt.slice(0, 500)}${prompt.length > 500 ? "..." : ""}`;
    const safe = Buffer.from(raw, "utf8").toString("utf8");
    fs.appendFileSync(logPath, `[${ts}]${attachmentSummary} ${safe}\n`, "utf8");
  } catch (_) {}

  if (prompt.length === 0) {
    console.log(
      JSON.stringify({
        continue: false,
        user_message: "提示内容为空，请输入问题或任务说明后再发送。",
      })
    );
    process.exit(0);
  }

  console.log(JSON.stringify({ continue: true }));
  process.exit(0);
}

main();
