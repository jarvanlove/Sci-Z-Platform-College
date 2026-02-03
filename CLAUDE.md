# Sci-Z-Platform-College 项目总览

> 顶层指令：仅保留项目概览与入口。具体规范见各子项目 CLAUDE.md 与 `.cursor/rules/`、Skills、Commands。

---

## 项目简介

高校科研项目管理平台，前后端分离：Vue 3 前端 + Java 21 后端（DDD 架构）。

## 项目结构

```
Sci-Z-Platform-College/
├── sci-z-web/       # 前端：Vue 3 + TypeScript + Element Plus + Pinia
├── sci-z-server/    # 后端：Java 21 + Spring Boot + MyBatis Plus + Sa-Token
├── .cursor/         # 通用 Cursor 配置（Rules、Skills、Agents、Commands）
└── CLAUDE.md        # 本文件
```

## 技术栈

| 端   | 技术                                                    |
| ---- | ------------------------------------------------------- |
| 前端 | Vue 3、TypeScript、Element Plus、Pinia、Vite            |
| 后端 | Java 21、Spring Boot、MyBatis Plus、Sa-Token、MapStruct |

## 开发规范入口

- **前端**：`sci-z-web/CLAUDE.md` + `sci-z-web/.cursor/rules/`
- **后端**：`sci-z-server/CLAUDE.md` + `sci-z-server/.cursor/rules/`
- **通用**：`.cursor/rules/`、`.cursor/skills/`、`.cursor/commands/`

## 快速开始

```bash
# 前端
cd sci-z-web ; npm install ; npm run dev

# 后端
cd sci-z-server ; mvn spring-boot:run
```
