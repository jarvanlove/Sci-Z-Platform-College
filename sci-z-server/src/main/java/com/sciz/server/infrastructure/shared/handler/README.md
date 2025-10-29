# 事件处理器模块化设计

## 目录结构

```
shared/
├── event/                           # 领域事件目录
│   ├── DomainEvent.java            # 领域事件基类
│   ├── EventPublisher.java         # 事件发布器
│   └── {模块名}/                   # 各业务模块事件
└── handler/                        # 事件处理器目录
    └── {模块名}/                   # 各业务模块事件处理器
        └── {模块名}EventHandler.java
```

## 设计原则

### 1. 分离关注点

- **事件定义**：在 `event/` 目录下定义领域事件
- **事件处理**：在 `handler/` 目录下实现事件处理逻辑
- **清晰分离**：事件和处理逻辑物理分离，便于维护

### 2. 包命名规范

- **事件包名**：`com.sciz.server.infrastructure.shared.event.{模块名}`
- **处理器包名**：`com.sciz.server.infrastructure.shared.handler.{模块名}`

### 3. 模块化设计

- 每个业务模块有独立的事件处理器
- 处理器按模块分组，便于定位和维护
- 支持模块间松耦合通信

## 当前模块

### 用户模块 (user/)

- **事件处理器**：`UserEventHandler.java`
- **处理事件**：用户注册、登录、退出事件
- **主要功能**：发送通知、记录日志、更新统计

### 项目模块 (project/)

- **事件处理器**：`ProjectEventHandler.java`
- **处理事件**：项目创建事件
- **主要功能**：发送通知、初始化设置、记录审计

### 申报模块 (declaration/)

- **事件处理器**：`DeclarationEventHandler.java`
- **处理事件**：申报创建、更新事件
- **主要功能**：流程管理、状态通知、审计记录

### 报告模块 (report/)

- **事件处理器**：`ReportEventHandler.java`
- **处理事件**：报告生成事件
- **主要功能**：文件验证、分发流程、统计更新

### 知识库模块 (knowledge/)

- **事件处理器**：`KnowledgeEventHandler.java`
- **处理事件**：知识库创建事件
- **主要功能**：文件处理、索引建立、统计更新

### AI 模块 (ai/)

- **事件处理器**：`AiEventHandler.java`
- **处理事件**：聊天开始、工作流执行事件
- **主要功能**：会话管理、模型预热、结果处理

### 文件模块 (file/)

- **事件处理器**：`FileEventHandler.java`
- **处理事件**：文件上传、删除事件
- **主要功能**：文件验证、索引管理、缓存清理

## 使用方式

### 事件处理器自动注册

```java
@Component
public class UserEventHandler {

    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        // 自动处理用户注册事件
        processUserRegistration(event);
    }
}
```

### 事件发布

```java
@Service
public class UserServiceImpl {

    @Autowired
    private EventPublisher eventPublisher;

    public void registerUser(RegisterReq request) {
        // 1. 执行业务逻辑
        User user = createUser(request);

        // 2. 发布事件
        UserRegisteredEvent event = new UserRegisteredEvent(...);
        eventPublisher.publish(event);

        // 3. 事件处理器会自动处理
    }
}
```

## 扩展指南

### 添加新事件处理器

1. 在对应模块目录下创建处理器类
2. 使用 `@Component` 注解标记为 Spring 组件
3. 使用 `@EventListener` 注解监听特定事件
4. 使用 `@Async` 注解实现异步处理
5. 实现具体的事件处理逻辑

### 事件处理器模板

```java
package com.sciz.server.infrastructure.shared.handler.{模块名};

import com.sciz.server.infrastructure.shared.event.{模块名}.{事件名};
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class {模块名}EventHandler {

    @EventListener
    @Async
    public void handle{事件名}({事件名} event) {
        try {
            log.info("处理{事件名}: {}", event.getAggregateId());

            // 实现具体处理逻辑

            log.info("{事件名}处理完成: {}", event.getAggregateId());

        } catch (Exception e) {
            log.error("处理{事件名}失败: {}", event.getAggregateId(), e);
        }
    }
}
```

## 最佳实践

### 1. 异常处理

- 事件处理失败不应影响主业务流程
- 记录详细的错误日志
- 考虑实现重试机制

### 2. 性能优化

- 使用 `@Async` 实现异步处理
- 避免在事件处理器中执行耗时操作
- 合理使用线程池配置

### 3. 监控和告警

- 添加事件处理监控
- 实现处理失败告警
- 记录处理性能指标

### 4. 测试策略

- 为每个事件处理器编写单元测试
- 使用 Spring 的测试框架测试事件处理
- 模拟事件发布和处理流程
