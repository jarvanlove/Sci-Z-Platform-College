#!/usr/bin/env node
/**
 * 汇总 session-stats.log：按日、按月统计会话次数与总时长（毫秒），用于估算使用量。
 * 使用方式：在项目根目录执行 node .cursor/hooks/aggregate-session-stats.js [日|月|全部]
 * 参数可选：日 = 只输出按日汇总，月 = 只输出按月汇总，全部或不传 = 两者都输出。
 *
 * 说明：Cursor 目前未在 hooks 中暴露每会话的 token 数，本脚本仅能汇总会话数与时长；
 * 若后续 Cursor 在 sessionEnd 中提供 input_tokens/output_tokens 等字段，可在此脚本中扩展汇总。
 */
const fs = require("fs");
const path = require("path");

const LOG_PATH = path.join(
  process.cwd(),
  "reports",
  "hooks",
  "session-stats.log"
);

function readLines() {
  if (!fs.existsSync(LOG_PATH)) return [];
  const raw = fs.readFileSync(LOG_PATH, "utf8");
  const lines = raw
    .split(/\r?\n/)
    .filter((line) => line.trim() && !line.startsWith("#"));
  return lines;
}

function parseLine(line) {
  const tabs = line.split("\t");
  const date = tabs[0];
  const duration_ms = parseInt(tabs[4], 10) || 0;
  return { date, duration_ms };
}

function aggregate(lines, groupBy) {
  const map = {};
  for (const line of lines) {
    const { date, duration_ms } = parseLine(line);
    let key;
    if (groupBy === "day") key = date;
    else if (groupBy === "month") key = date.slice(0, 7);
    else continue;
    if (!map[key]) map[key] = { count: 0, duration_ms: 0 };
    map[key].count += 1;
    map[key].duration_ms += duration_ms;
  }
  return map;
}

function main() {
  const mode = (process.argv[2] || "all").toLowerCase();
  const lines = readLines();
  if (lines.length === 0) {
    console.log(
      "session-stats.log 为空或不存在，请先使用 Cursor 产生会话并确保 sessionEnd 已写入该文件。"
    );
    process.exit(0);
  }

  if (mode === "日" || mode === "day" || mode === "all") {
    const byDay = aggregate(lines, "day");
    const days = Object.keys(byDay).sort();
    console.log("\n=== 按日汇总 ===");
    for (const d of days) {
      const { count, duration_ms } = byDay[d];
      const sec = (duration_ms / 1000).toFixed(1);
      console.log(`${d}\t会话数: ${count}\t总时长: ${sec}s (${duration_ms}ms)`);
    }
  }

  if (mode === "月" || mode === "month" || mode === "all") {
    const byMonth = aggregate(lines, "month");
    const months = Object.keys(byMonth).sort();
    console.log("\n=== 按月汇总 ===");
    for (const m of months) {
      const { count, duration_ms } = byMonth[m];
      const sec = (duration_ms / 1000).toFixed(1);
      console.log(`${m}\t会话数: ${count}\t总时长: ${sec}s (${duration_ms}ms)`);
    }
  }

  console.log("");
  process.exit(0);
}

main();
