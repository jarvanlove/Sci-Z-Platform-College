# 领域事件模块化设计

## 目录结构

```
shared/
├── event/                           # 领域事件目录
│   ├── DomainEvent.java            # 领域事件基类
│   ├── EventPublisher.java         # 事件发布器
│   ├── user/                       # 用户模块事件
│   │   ├── UserRegisteredEvent.java
│   │   ├── UserLoginEvent.java
│   │   └── UserLogoutEvent.java
│   ├── project/                    # 项目模块事件
│   │   └── ProjectCreatedEvent.java
│   ├── declaration/                # 申报模块事件
│   │   ├── DeclarationCreatedEvent.java
│   │   └── DeclarationUpdatedEvent.java
│   ├── report/                     # 报告模块事件
│   │   └── ReportGeneratedEvent.java
│   ├── knowledge/                  # 知识库模块事件
│   │   └── KnowledgeCreatedEvent.java
│   ├── ai/                         # AI模块事件
│   │   ├── ChatStartedEvent.java
│   │   └── WorkflowExecutedEvent.java
│   └── file/                       # 文件模块事件
│       ├── FileUploadedEvent.java
│       └── FileDeletedEvent.java
└── handler/                        # 事件处理器目录
    ├── user/                       # 用户模块事件处理器
    │   └── UserEventHandler.java
    ├── project/                    # 项目模块事件处理器
    │   └── ProjectEventHandler.java
    ├── declaration/                # 申报模块事件处理器
    │   └── DeclarationEventHandler.java
    ├── report/                     # 报告模块事件处理器
    │   └── ReportEventHandler.java
    ├── knowledge/                  # 知识库模块事件处理器
    │   └── KnowledgeEventHandler.java
    ├── ai/                         # AI模块事件处理器
    │   └── AiEventHandler.java
    └── file/                       # 文件模块事件处理器
        └── FileEventHandler.java
```

## 设计原则

### 1. 模块化分离

- 每个业务模块有独立的事件和处理器
- 事件和处理器按模块分组，便于维护
- 清晰的包结构，符合 DDD 分层架构

### 2. 事件命名规范

- 事件名称：`{业务对象}{动作}Event`
- 处理器名称：`{业务对象}EventHandler`
- 事件包名：`com.sciz.server.infrastructure.shared.event.{模块名}`
- 处理器包名：`com.sciz.server.infrastructure.shared.handler.{模块名}`

### 3. 事件设计模式

- 所有事件继承 `DomainEvent` 基类
- 事件包含聚合根 ID 和类型信息
- 事件数据不可变，通过构造函数设置

### 4. 处理器设计模式

- 使用 `@EventListener` 注解监听事件
- 使用 `@Async` 注解异步处理
- 异常处理不影响主业务流程

## 使用示例

### 在应用层发布事件

```java
@Service
public class UserServiceImpl {

    @Autowired
    private EventPublisher eventPublisher;

    @Transactional
    public Result<UserInfoResp> register(RegisterReq request) {
        // 1. 执行业务逻辑
        User user = createUser(request);
        userRepository.save(user);

        // 2. 发布领域事件
        UserRegisteredEvent event = new UserRegisteredEvent(
            String.valueOf(user.getId()),
            user.getUsername(),
            user.getEmail()
        );
        eventPublisher.publish(event);

        return Result.success("注册成功");
    }
}
```

### 事件处理器自动处理

```java
@Component
public class UserEventHandler {

    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        // 异步处理用户注册后的副作用
        sendWelcomeEmail(event);
        initializeUserSettings(event);
        logUserRegistration(event);
    }
}
```

## 扩展指南

### 添加新事件

1. 在对应模块目录下创建事件类
2. 继承 `DomainEvent` 基类
3. 实现 `getAggregateId()` 和 `getAggregateType()` 方法
4. 在应用层服务中发布事件

### 添加新处理器

1. 在对应模块目录下创建处理器类
2. 使用 `@EventListener` 和 `@Async` 注解
3. 实现具体的事件处理逻辑
4. 添加异常处理和日志记录

### 模块间通信

- 通过事件实现模块间松耦合通信
- 避免直接依赖，提高系统可维护性
- 支持事件的异步处理和重试机制

### 下一步建议

1. **完善事件处理逻辑**：在各个事件处理器中实现具体的业务逻辑
2. **添加事件重试机制**：为关键事件添加重试和失败处理
3. **事件监控和告警**：添加事件处理的监控和异常告警
4. **事件版本管理**：为事件添加版本号，支持向后兼容
5. **事件存储**：考虑将事件持久化存储，支持事件溯源
