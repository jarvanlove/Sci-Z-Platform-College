---
name: java-ddd-generator
description: "Executes Java DDD module creation: confirm module and fields, create Entity→Mapper→Repository→DTO→Converter→Service→Controller in order, run lints and fix. Use when user asks to create Java module (创建 Java 模块) or /create-java-module. 执行体：按顺序创建各层文件并校验。"
---

# Java DDD 模块生成器（执行体）

## Instructions

**按下列步骤依次执行，完成从实体到 Controller 的完整模块。**

1. **确认模块与数据**

   - 向用户确认或从上下文提取：模块名（如 Knowledge、Report）、表结构或实体字段、需要的 CRUD 范围。
   - 确定包路径与类名前缀（符合 `sci-z-server` 包结构）。

2. **按 DDD 分层顺序创建文件（必须按此顺序）**

   - 在 `sci-z-server` 下依次**创建**以下文件，每层符合 `.cursor/rules/` 中 ddd-architecture、java-naming、notes-rules 等规范：
     - **Entity**：`domain/pojo/entity/<module>/`
     - **Mapper**：`domain/pojo/mapper/<module>/`，继承 BaseMapper
     - **Repository**：`domain/pojo/repository/<module>/`，仅数据访问
     - **DTO**：`domain/pojo/dto/request/` 与 `response/`，简单 DTO 用 Record
     - **Converter**：`interfaces/converter/`，MapStruct
     - **Service**：`application/service/<module>/`（接口 + impl），`@Transactional(rollbackFor = Exception.class)`
     - **Controller**：`interfaces/controller/`，返回 `Result<T>`
   - 每创建一层即使用 Read 确认无误，再进入下一层；依赖方向遵守 interfaces → application → domain ← infrastructure。

3. **校验与修复**

   - 对新建或修改的 Java 文件运行 read_lints；若有编译或规范问题则逐条修复。
   - 可选：在 `sci-z-server` 下执行 `mvn compile -q` 或 `mvn test -q` 验证。

4. **向用户汇报**
   - 列出已创建的文件与路径；说明后续步骤（如注册路由、配置权限等，若项目有约定）。

## Scripts

- **scripts/scaffold-module.sh**（可选）：接收模块名，按顺序生成各层空骨架文件。若不存在则由 Agent 直接按步骤 2 写入完整内容。

## Guidelines

- 命名、注释、日志、异常等以 `sci-z-server/.cursor/rules/` 及 `sci-z-server/CLAUDE.md` 为准；本 Skill 只规定执行顺序与分层，不重复罗列约束细节。
