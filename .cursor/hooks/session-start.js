#!/usr/bin/env node
/**
 * sessionStart Hook：在 Agent/Composer 会话开始时注入额外上下文，让模型一开场就了解项目约定。
 *
 * 输入（stdin JSON）：
 *   - session_id, is_background_agent, composer_mode
 * 输出（stdout JSON）：
 *   - additional_context（可选）：追加到会话 system 的文本
 *   - env（可选）：会话环境变量
 *   - continue（可选，默认 true）：是否允许创建会话
 *   - user_message（可选）：若 continue 为 false 时展示给用户
 *
 * 上下文来源：.cursor/hooks/context/session-prompt.txt（若存在），否则使用内置默认提示。
 */
const fs = require("fs");
const path = require("path");

const DEFAULT_CONTEXT =
  "当前项目为 Sci-Z 高校科研管理平台。请遵循项目内 CLAUDE.md 与 .cursor/rules 规范。";

function readStdin() {
  try {
    return fs.readFileSync(0, "utf8");
  } catch (_) {
    return "";
  }
}

function loadSessionPrompt() {
  const cwd = process.cwd();
  const possiblePaths = [
    path.join(cwd, ".cursor", "hooks", "context", "session-prompt.txt"),
    path.join(cwd, ".cursor", "hooks", "session-prompt.txt"),
  ];
  for (const p of possiblePaths) {
    try {
      if (fs.existsSync(p)) {
        const text = fs.readFileSync(p, "utf8").trim();
        if (text.length > 0) return text;
      }
    } catch (_) {}
  }
  return DEFAULT_CONTEXT;
}

function main() {
  let input = "";
  try {
    input = readStdin();
  } catch (_) {
    console.log(JSON.stringify({ continue: true }));
    process.exit(0);
  }

  let payload = {};
  if (input.trim()) {
    try {
      payload = JSON.parse(input);
    } catch (_) {}
  }

  const additional_context = loadSessionPrompt();
  const output = {
    continue: true,
    additional_context,
  };

  console.log(JSON.stringify(output));
  process.exit(0);
}

main();
