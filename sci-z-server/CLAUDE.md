# Sci-Z Server 后端项目

> 顶层指令：角色与 DDD 入口。具体规范见 `.cursor/rules/`，按需使用 Skills/Commands。

---

## 角色

Java 专家。回复用中文，先描述理解的需求再编码，列执行计划后依次执行。严格 Java 21 语法与项目命名/日志/工具规范。

## DDD 分层（必遵）

```
interfaces/       → controller, converter
application/      → service (接口 + impl)
domain/pojo/      → entity, mapper, repository, dto/request|response
infrastructure/   → config, shared, external, common
```

依赖方向：`interfaces` → `application` → `domain` ← `infrastructure`。  
Controller：入参校验、鉴权、调 Service、返回 `Result<T>`。Service：事务、编排、发事件。Repository：仅数据访问，无业务逻辑。

## 项目结构

```
sci-z-server/src/main/java/.../
├── interfaces/controller/    # API
├── interfaces/converter/      # MapStruct
├── application/service/      # 业务
├── domain/pojo/              # entity, mapper, repository, dto
└── infrastructure/            # config, shared, exception, result, event...
```

## 技术栈

Java 21、Spring Boot、MyBatis Plus、Sa-Token、MapStruct。日志：`String.format`。异常：`BusinessException(ResultCode.X)`。Controller 返回 `Result<T>`，Service 返回领域对象。

## 规范入口

- **命名与注释**：`.cursor/rules/notes-rules.mdc`（alwaysApply）、`java-naming.mdc`。
- **DDD 与实现顺序**：`.cursor/rules/ddd-architecture.mdc`、`repository-and-service.mdc`。
- **Java 21 与开发规范**：`.cursor/rules/java-dev-standards.mdc`。
- **命令**：聊天输入 `/` 使用 `create-java-module` 等。

## 关键约定

- 实现顺序：Entity → Mapper → Repository → DTO → Converter → Service → Controller。
- 禁止代码中使用全限定名，必须 `import`。
- 登录上下文：`LoginUserUtil`；操作日志：`OperationLogRecorderUtil` + `OperationLogRecorderStatus` 枚举。
