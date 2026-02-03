#!/usr/bin/env node
/**
 * beforeShellExecution Hook：对匹配到的高风险命令要求用户确认（permission: "ask"）。
 * 仅在 hooks.json 的 matcher 匹配时才会执行本脚本。
 */
const fs = require("fs");

function main() {
  let input = "";
  try {
    input = fs.readFileSync(0, "utf8");
  } catch (_) {
    console.log(JSON.stringify({ permission: "allow" }));
    process.exit(0);
  }
  let payload;
  try {
    payload = JSON.parse(input);
  } catch (_) {
    console.log(JSON.stringify({ permission: "allow" }));
    process.exit(0);
  }
  const cmd = (payload.command || "").trim();
  const dangerous =
    /rm\s+-rf|del\s+\/s|format\s+disk|mvn\s+clean\s+install/i.test(cmd);
  if (dangerous) {
    console.log(
      JSON.stringify({
        permission: "ask",
        user_message: "检测到可能的高风险命令，请确认后再执行：" + cmd,
        agent_message:
          "该命令已被标记为需用户确认。请向用户说明意图，待用户批准后再执行。",
      })
    );
  } else {
    console.log(JSON.stringify({ permission: "allow" }));
  }
  process.exit(0);
}
main();
