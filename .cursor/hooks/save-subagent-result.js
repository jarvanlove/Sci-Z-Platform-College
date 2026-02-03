#!/usr/bin/env node
/**
 * subagentStop Hook：子代理（verifier、debugger 等）结束时，将 result 保存到 reports/subagents/
 * 便于后续查看验证报告、调试报告等。
 */
const fs = require("fs");
const path = require("path");

function main() {
  let input = "";
  try {
    input = fs.readFileSync(0, "utf8");
  } catch (_) {
    process.exit(0);
  }
  let payload;
  try {
    payload = JSON.parse(input);
  } catch (_) {
    process.exit(0);
  }
  const type = (payload.subagent_type || "unknown").replace(
    /[^a-zA-Z0-9_-]/g,
    "_"
  );
  const status = payload.status || "unknown";
  const result = payload.result != null ? String(payload.result) : "";
  const duration = payload.duration != null ? payload.duration : 0;

  const dir = path.join(process.cwd(), "reports", "subagents");
  const ts = new Date().toISOString().replace(/[:.]/g, "-");
  const filename = `${type}-${status}-${ts}.md`;
  const filepath = path.join(dir, filename);

  try {
    fs.mkdirSync(dir, { recursive: true });
    const content = `# Subagent: ${type}\n\n**Status:** ${status}\n**Duration (ms):** ${duration}\n\n## Result\n\n${result}\n`;
    fs.writeFileSync(filepath, "\uFEFF" + content, "utf8");
  } catch (_) {}
  process.exit(0);
}
main();
