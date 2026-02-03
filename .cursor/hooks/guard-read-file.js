#!/usr/bin/env node
/**
 * beforeReadFile Hook：在 Agent 读取文件前做访问控制，禁止读取敏感文件（如 .env、密钥、密码等）。
 *
 * 输入（stdin JSON）：
 *   - file_path（绝对路径）, content, attachments
 * 输出（stdout JSON）：
 *   - permission: "allow" | "deny"
 *   - user_message（可选）：deny 时展示给用户的说明
 *
 * 采用 fail-closed：脚本异常或超时时，Cursor 会阻止读取。因此本脚本在解析失败时返回 deny。
 */
const fs = require("fs");
const path = require("path");

/** 命中任一则拒绝读取（小写匹配，支持 * 通配） */
const DENY_PATTERNS = [
  ".env",
  ".env.",
  ".env.local",
  ".env.production",
  ".env.development",
  "secret",
  "secrets",
  ".pem",
  ".key",
  ".p12",
  ".pfx",
  "password",
  "credential",
  "credentials",
  "api_key",
  "apikey",
  "private.key",
  "id_rsa",
  "id_dsa",
  ".npmrc", // 可能含 token
  ".yarnrc",
  ".netrc",
];

/** 路径是否命中拒绝规则（不区分大小写，按片段匹配） */
function shouldDeny(filePath) {
  if (!filePath || typeof filePath !== "string") return true;
  const normalized = filePath.replace(/\\/g, "/").toLowerCase();
  const fileName = path.basename(normalized).toLowerCase();
  for (const p of DENY_PATTERNS) {
    const low = p.toLowerCase();
    if (fileName.includes(low) || normalized.includes(low)) return true;
  }
  return false;
}

function main() {
  let input = "";
  try {
    input = fs.readFileSync(0, "utf8");
  } catch (_) {
    console.log(
      JSON.stringify({
        permission: "deny",
        user_message: "Hook 无法读取输入，出于安全已阻止本次文件读取。",
      })
    );
    process.exit(0);
  }

  let payload = {};
  try {
    payload = JSON.parse(input);
  } catch (_) {
    console.log(
      JSON.stringify({
        permission: "deny",
        user_message: "Hook 输入格式异常，出于安全已阻止本次文件读取。",
      })
    );
    process.exit(0);
  }

  const file_path = payload.file_path;
  if (shouldDeny(file_path)) {
    console.log(
      JSON.stringify({
        permission: "deny",
        user_message: `出于安全策略，不允许 Agent 读取该文件：${file_path}。若确需使用其中信息，请脱敏后复制到非敏感文件再引用。`,
      })
    );
    process.exit(0);
  }

  console.log(JSON.stringify({ permission: "allow" }));
  process.exit(0);
}

main();
