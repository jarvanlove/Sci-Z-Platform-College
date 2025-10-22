# AgentBackendServices

## 项目简介

AgentBackendServices 是一个基于 Spring Boot 3.5.6 的高校科研项目管理平台后端服务，采用现代化的微服务架构设计，为科研项目申报、管理、验收等业务场景提供稳定可靠的后端支撑。

## 技术栈

### 核心框架
- **Java 21** - 使用最新的 LTS 版本，享受现代 Java 特性
- **Spring Boot 3.5.6** - 企业级应用开发框架
- **Spring Web** - RESTful API 开发
- **Spring Validation** - 参数校验

### 数据存储
- **PostgreSQL** - 主数据库，支持复杂查询和事务
- **Redis** - 缓存数据库，提升系统性能
- **MyBatis 3.0.5** - ORM 框架，简化数据库操作

### 消息队列
- **RabbitMQ** - 异步消息处理，支持任务队列和事件驱动

### 开发工具
- **Lombok** - 减少样板代码
- **Spring Boot DevTools** - 开发时热重载
- **Maven** - 项目构建和依赖管理

## 项目结构

```
AgentBackendServices/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/server/agentbackendservices/
│   │   │       ├── AgentBackendServicesApplication.java
│   │   │       ├── controller/          # 控制器层
│   │   │       ├── service/            # 业务逻辑层
│   │   │       ├── mapper/             # 数据访问层
│   │   │       ├── entity/             # 实体类
│   │   │       ├── dto/                # 数据传输对象
│   │   │       ├── config/             # 配置类
│   │   │       └── util/               # 工具类
│   │   └── resources/
│   │       ├── application.properties  # 应用配置
│   │       └── mapper/                 # MyBatis XML 映射文件
│   └── test/                           # 测试代码
├── pom.xml                            # Maven 配置
└── README.md                          # 项目说明
```

## 环境要求

### 开发环境
- **JDK 21+** - Java 开发环境
- **Maven 3.6+** - 项目构建工具
- **PostgreSQL 13+** - 数据库
- **Redis 6+** - 缓存服务
- **RabbitMQ 3.8+** - 消息队列

### 推荐 IDE
- IntelliJ IDEA 2023.3+
- Eclipse 2023-09+
- VS Code with Java Extension Pack

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd AgentBackendServices
```

### 2. 配置数据库

#### PostgreSQL 配置
```sql
-- 创建数据库
CREATE DATABASE sci_z_platform;

-- 创建用户（可选）
CREATE USER sci_z_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE sci_z_platform TO sci_z_user;
```

#### Redis 配置
```bash
# 启动 Redis 服务
redis-server

# 验证连接
redis-cli ping
```

#### RabbitMQ 配置
```bash
# 启动 RabbitMQ 服务
rabbitmq-server

# 启用管理界面（可选）
rabbitmq-plugins enable rabbitmq_management
```

### 3. 环境配置
项目使用 YAML 格式的配置文件，支持多环境配置：

#### 配置文件结构
```
src/main/resources/
├── application.yml          # 基础配置
├── application-dev.yml      # 开发环境配置
├── application-test.yml     # 测试环境配置
└── application-prod.yml     # 生产环境配置
```

#### 环境切换
```bash
# 开发环境（默认）
java -jar app.jar

# 测试环境
java -jar app.jar --spring.profiles.active=test

# 生产环境
java -jar app.jar --spring.profiles.active=prod
```

#### 环境变量配置（生产环境）
生产环境支持环境变量配置，提高安全性：
```bash
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
export REDIS_PASSWORD=your_redis_password
export RABBITMQ_PASSWORD=your_rabbitmq_password
export JWT_SECRET=your_jwt_secret
```

### 4. 编译和运行
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/AgentBackendServices-0.0.1-SNAPSHOT.jar
```

### 5. 验证运行
访问 `http://localhost:8080` 确认服务启动成功。

## 配置说明

### 环境配置详情

#### 开发环境 (dev)
- **数据库**: 本地 PostgreSQL，连接池 10
- **Redis**: 本地 Redis，连接池 8
- **RabbitMQ**: 本地 RabbitMQ，手动确认
- **日志**: DEBUG 级别，控制台输出
- **热重载**: 启用 DevTools

#### 测试环境 (test)
- **数据库**: 测试服务器 PostgreSQL，连接池 20
- **Redis**: 测试服务器 Redis，连接池 16
- **RabbitMQ**: 测试服务器 RabbitMQ
- **日志**: INFO 级别，文件输出
- **数据清理**: 自动清理测试数据

#### 生产环境 (prod)
- **数据库**: 生产集群 PostgreSQL，连接池 50
- **Redis**: 生产集群 Redis，连接池 32
- **RabbitMQ**: 生产集群 RabbitMQ
- **日志**: WARN 级别，滚动日志
- **监控**: 启用 Prometheus 监控
- **安全**: 环境变量配置敏感信息

### 数据库配置
- **连接池**: HikariCP
- **事务管理**: 声明式事务
- **连接超时**: 30秒
- **最大连接数**: 开发(10) / 测试(20) / 生产(50)

### Redis 配置
- **连接池**: Lettuce 连接池
- **超时时间**: 开发(2s) / 测试(3s) / 生产(5s)
- **序列化**: JSON 序列化
- **集群支持**: 生产环境支持集群模式

### RabbitMQ 配置
- **消息确认**: 手动确认模式
- **重试机制**: 开发(3次) / 测试(3次) / 生产(5次)
- **并发处理**: 生产环境支持 10-20 并发

## API 文档

### 基础信息
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **字符编码**: UTF-8

### 通用响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 错误码说明
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `500`: 服务器内部错误

## 开发指南

### 代码规范
- 使用 Lombok 减少样板代码
- 遵循 RESTful API 设计原则
- 使用统一的异常处理机制
- 添加必要的注释和文档

### 数据库操作
- 使用 MyBatis 进行数据库操作
- 遵循命名规范：表名小写+下划线，字段名小写+下划线
- 实体类使用驼峰命名

### 缓存策略
- 使用 Redis 进行数据缓存
- 设置合理的过期时间
- 注意缓存一致性

### 消息队列
- 使用 RabbitMQ 处理异步任务
- 定义清晰的消息格式
- 实现消息重试机制

## 测试

### 单元测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=TestClassName
```

### 集成测试
```bash
# 运行集成测试
mvn verify
```

## 部署

### Docker 部署
```dockerfile
FROM openjdk:21-jre-slim
COPY target/AgentBackendServices-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 生产环境配置
```properties
# 生产环境配置示例
spring.profiles.active=prod
server.port=8080
logging.level.root=INFO
```

## 监控和日志

### 日志配置
- 使用 Logback 作为日志框架
- 支持控制台和文件输出
- 可配置日志级别

### 健康检查
- Spring Boot Actuator 健康检查
- 数据库连接检查
- Redis 连接检查
- RabbitMQ 连接检查

## 常见问题

### Q: 如何修改数据库连接？
A: 修改 `application.properties` 中的 `spring.datasource.*` 配置项。

### Q: Redis 连接失败怎么办？
A: 检查 Redis 服务是否启动，确认连接配置是否正确。

### Q: RabbitMQ 消息处理失败？
A: 检查队列配置，确认消息格式是否正确。

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: [Your Name]
- 邮箱: [your.email@example.com]
- 项目链接: [https://github.com/yourusername/AgentBackendServices](https://github.com/yourusername/AgentBackendServices)

## 健康检查功能

### 启动时自动检查
应用启动完成后会自动检查所有外部服务的连接状态，并在控制台输出详细的检查报告。

### REST API 健康检查
- `GET /api/health/check` - 检查所有服务状态
- `GET /api/health/database` - 检查数据库连接
- `GET /api/health/redis` - 检查 Redis 连接  
- `GET /api/health/rabbitmq` - 检查 RabbitMQ 连接

### Spring Boot Actuator 健康检查
- `GET /api/actuator/health` - Spring Boot 内置健康检查端点

### 健康检查示例响应
```json
{
  "timestamp": 1703123456789,
  "status": "success",
  "services": {
    "database": {
      "status": "UP",
      "message": "数据库连接正常",
      "url": "jdbc:postgresql://localhost:5432/sci_z_platform_dev",
      "database": "PostgreSQL",
      "version": "15.4"
    },
    "redis": {
      "status": "UP",
      "message": "Redis 连接正常",
      "host": "localhost:6379"
    },
    "rabbitmq": {
      "status": "UP",
      "message": "RabbitMQ 连接正常",
      "host": "192.168.1.203:5672"
    }
  }
}
```

## API 文档

### 📚 Swagger/OpenAPI 3 文档

项目集成了 SpringDoc OpenAPI 3，提供完整的 API 文档和在线测试功能。

#### 访问地址：
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

#### 主要功能：
- 📖 **自动生成文档** - 基于注解自动生成 API 文档
- 🧪 **在线测试** - 直接在浏览器中测试 API 接口
- 📋 **参数说明** - 详细的请求参数和响应格式说明
- 🏷️ **分组管理** - 按功能模块分组展示 API

#### 使用示例：

##### 1. 基础注解
```java
@Tag(name = "用户管理", description = "用户相关的增删改查操作")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Operation(summary = "创建用户", description = "创建新用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @PostMapping
    public boolean create(@RequestBody User user) {
        return userService.save(user);
    }
}
```

##### 2. 参数说明
```java
@GetMapping("/{id}")
public User getById(
    @Parameter(description = "用户ID", required = true, example = "1") 
    @PathVariable Long id) {
    return userService.getById(id);
}
```

##### 3. 请求体说明
```java
@PostMapping
public boolean create(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "用户信息", 
        required = true,
        content = @Content(schema = @Schema(implementation = User.class))
    )
    @RequestBody User user) {
    return userService.save(user);
}
```

### 🔧 配置说明

#### Swagger 配置
```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
  show-actuator: true
```

#### 自定义配置
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agent Backend Services API")
                        .description("智能代理后端服务 API 文档")
                        .version("1.0.0"));
    }
}
```

## 更新日志

### v0.0.1-SNAPSHOT (2024-01-01)
- 初始版本发布
- 基础框架搭建
- 数据库和缓存配置
- 消息队列集成
- 多环境配置支持
- 健康检查功能
- Swagger/OpenAPI 3 文档集成
