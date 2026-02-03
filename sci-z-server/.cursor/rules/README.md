# Sci-Z Server 规则说明

> 本目录规则与 `sci-z-server/CLAUDE.md` 配合使用。CLAUDE.md 仅保留顶层指令，具体规范拆分为以下 .mdc 文件。

---

## 规则文件

| 文件                           | 说明                                                                                | 触发                                 |
| ------------------------------ | ----------------------------------------------------------------------------------- | ------------------------------------ |
| **notes-rules.mdc**            | 类/方法/字段注释模板                                                                | alwaysApply: true                    |
| **common-rules.mdc**           | 回复与前置条件、编码要点                                                            | 智能/文件匹配                        |
| **current-project-rules.mdc**  | 规则入口，指向其他 .mdc                                                             | 智能/文件匹配                        |
| **ddd-architecture.mdc**       | DDD 分层、依赖、职责、实现顺序、文件命名                                            | globs: \*_/_.java                    |
| **java-naming.mdc**            | 方法命名、变量命名、Stream Lambda 命名                                              | globs: \*_/_.java                    |
| **java-dev-standards.mdc**     | 导入、日志、异常、返回、事务、LoginUserUtil、Record/BaseQueryReq、Java 21、接口规范 | globs: \*_/_.java                    |
| **repository-and-service.mdc** | Repository 查询/更新/软删除、Service、操作日志                                      | globs: repository/**、application/** |

## 使用方式

- Cursor 会根据 description 与 globs 自动加载相关规则
- 注释模板（notes-rules.mdc）始终应用
- 其他规则在编辑/讨论 Java 文件时按需应用
- 详细开发规范（代码模板、事件处理、枚举/常量等）可参考已备份的 CLAUDE.md 或《项目配置方案.md》
