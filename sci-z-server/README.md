# 高校科研项目管理平台

## 🎯 项目概述

高校科研项目管理平台是一个基于 **DDD 领域驱动设计**、**前后端分离**、**单体应用架构**的智能化科研项目全生命周期管理系统。平台以 Dify 工作流为核心，实现科研项目从申报、执行到验收的智能化、自动化管理。

### ✨ 核心特性

- 🎯 **DDD 领域驱动**：8 大领域，清晰的边界，高内聚低耦合
- 🚀 **单体应用架构**：简化部署，易于维护，支持未来拆分为微服务
- 🤖 **AI 智能化**：集成 Dify 平台，实现智能工作流编排
- 📊 **全生命周期管理**：覆盖申报、执行、报告全流程
- 🔒 **安全可靠**：基于 Sa-Token 的 JWT 认证体系
- ⚡ **Java 21 新特性**：虚拟线程、记录模式、模式匹配等

## 🏗️ 技术架构

### 后端技术栈

| 技术             | 版本     | 说明                           |
| ---------------- | -------- | ------------------------------ |
| **Java**         | 21 (LTS) | 编程语言，支持虚拟线程等新特性 |
| **Spring Boot**  | 3.2.x    | 应用框架，原生支持 Java 21     |
| **PostgreSQL**   | 14+      | 关系型数据库                   |
| **Redis**        | 7.x      | 缓存、会话、分布式锁           |
| **Apache Kafka** | 3.x      | 消息队列（领域事件发布）       |
| **MinIO**        | Latest   | 对象存储（文件存储）           |
| **Sa-Token**     | 1.37.0+  | 轻量级权限认证框架             |
| **MyBatis-Plus** | 3.5.5+   | ORM 框架                       |
| **LangChain4j**  | 0.34.0+  | LLM 集成框架                   |

### AI 平台

- **Dify** - AI 工作流编排与知识库管理
- **Vector Database** - 向量数据库（语义搜索）

### 架构模式

- **DDD 领域驱动设计** - 单体应用，清晰的领域边界
- **CQRS** - 命令查询职责分离
- **事件驱动** - 基于领域事件的异步处理
- **分层架构** - 接口层、应用层、领域层、基础设施层

## 📁 项目结构

```
sci-z-server/
├── pom.xml                              # Maven 项目配置
└── src/main/
    ├── java/com/sciz/server/
    │   ├── ServerApplication.java       # 应用启动类
    │   │
    │   ├── interfaces/                  # 接口层 - 对外接口
    │   │   ├── controller/              # 控制器
    │   │   │   ├── AuthController.java          # 认证控制器
    │   │   │   ├── UserController.java          # 用户控制器
    │   │   │   ├── ProjectController.java       # 项目控制器
    │   │   │   ├── DeclarationController.java   # 申报控制器
    │   │   │   ├── ReportController.java        # 报告控制器
    │   │   │   ├── KnowledgeController.java     # 知识库控制器
    │   │   │   └── ChatController.java          # AI对话控制器
    │   │   └── converter/                # 统一转换器（单次转换）
    │   │       ├── UserConverter.java
    │   │       ├── ProjectConverter.java
    │   │       └── DeclarationConverter.java
    │   │
    │   ├── application/                 # 应用层 - 业务编排
    │   │   ├── service/                 # 应用服务
    │   │   │   ├── AuthService.java             # 认证服务接口
    │   │   │   ├── UserService.java             # 用户服务接口
    │   │   │   ├── ProjectService.java          # 项目服务接口
    │   │   │   ├── DeclarationService.java      # 申报服务接口
    │   │   │   ├── ReportService.java           # 报告服务接口
    │   │   │   ├── KnowledgeService.java        # 知识库服务接口
    │   │   │   └── ChatService.java             # AI对话服务接口
    │   │   ├── impl/                    # 应用服务实现
    │   │   │   ├── AuthServiceImpl.java         # 认证服务实现
    │   │   │   ├── UserServiceImpl.java         # 用户服务实现
    │   │   │   ├── ProjectServiceImpl.java      # 项目服务实现
    │   │   │   ├── DeclarationServiceImpl.java  # 申报服务实现
    │   │   │   ├── ReportServiceImpl.java       # 报告服务实现
    │   │   │   ├── KnowledgeServiceImpl.java    # 知识库服务实现
    │   │   │   └── ChatServiceImpl.java         # AI对话服务实现
    │   │   └── task/                    # 任务调度
    │   │       ├── ScheduledTask.java           # 定时任务
    │   │       ├── AsyncTask.java               # 异步任务
    │   │       └── WorkflowTask.java            # 工作流任务
    │   │
    │   ├── domain/                      # 领域层 - 核心业务
    │   │   ├── pojo/                    # 对象模型
    │   │   │   ├── entity/              # 领域实体
    │   │   │   │   ├── user/            # 用户相关实体
    │   │   │   │   │   ├── User.java            # 用户实体
    │   │   │   │   │   ├── Role.java            # 角色实体
    │   │   │   │   │   └── Permission.java      # 权限实体
    │   │   │   │   ├── project/         # 项目相关实体
    │   │   │   │   │   ├── Project.java         # 项目实体
    │   │   │   │   │   ├── ProjectMember.java   # 项目成员实体
    │   │   │   │   │   └── ProjectDocument.java # 项目文档实体
    │   │   │   │   ├── declaration/     # 申报相关实体
    │   │   │   │   │   ├── Declaration.java     # 申报实体
    │   │   │   │   │   └── DeclarationTemplate.java # 申报模板实体
    │   │   │   │   ├── report/          # 报告相关实体
    │   │   │   │   │   ├── Report.java          # 报告实体
    │   │   │   │   │   └── ReportTemplate.java  # 报告模板实体
    │   │   │   │   ├── knowledge/       # 知识库相关实体
    │   │   │   │   │   ├── KnowledgeBase.java   # 知识库实体
    │   │   │   │   │   └── KnowledgeFile.java   # 知识库文件实体
    │   │   │   │   └── ai/              # AI相关实体
    │   │   │   │       ├── Conversation.java    # 对话实体
    │   │   │   │       └── Message.java         # 消息实体
    │   │   │   ├── dto/                 # 数据传输对象
    │   │   │   │   ├── request/         # 请求DTO
    │   │   │   │   │   ├── LoginReq.java
    │   │   │   │   │   ├── RegisterReq.java
    │   │   │   │   │   ├── ProjectCreateReq.java
    │   │   │   │   │   ├── DeclarationSubmitReq.java
    │   │   │   │   │   └── ReportGenerateReq.java
    │   │   │   │   └── response/        # 响应DTO
    │   │   │   │       ├── LoginResp.java
    │   │   │   │       ├── UserInfoResp.java
    │   │   │   │       ├── ProjectInfoResp.java
    │   │   │   │       ├── DeclarationInfoResp.java
    │   │   │   │       └── ReportInfoResp.java
    │   │   ├── mapper/                  # 数据映射接口
    │   │   │   ├── UserMapper.java
    │   │   │   ├── ProjectMapper.java
    │   │   │   ├── DeclarationMapper.java
    │   │   │   ├── ReportMapper.java
    │   │   │   ├── KnowledgeMapper.java
    │   │   │   └── ConversationMapper.java
    │   │   └── repository/             # 仓储接口
    │   │       ├── UserRepository.java
    │   │       ├── ProjectRepository.java
    │   │       ├── DeclarationRepository.java
    │   │       ├── ReportRepository.java
    │   │       ├── KnowledgeRepository.java
    │   │       └── ConversationRepository.java
    │   │
    │   └── infrastructure/               # 基础设施层 - 技术实现
    │       ├── config/                  # 配置类
    │       │   ├── database/            # 数据库配置
    │       │   │   ├── DatabaseConfig.java
    │       │   │   └── MyBatisConfig.java
    │       │   ├── cache/               # 缓存配置
    │       │   │   └── RedisConfig.java
    │       │   ├── mq/                  # 消息队列配置
    │       │   │   └── KafkaConfig.java
    │       │   ├── security/           # 安全配置
    │       │   │   ├── SecurityConfig.java
    │       │   │   └── JwtConfig.java
    │       │   ├── storage/            # 存储配置
    │       │   │   └── MinioConfig.java
    │       │   ├── ai/                 # AI配置
    │       │   │   └── DifyConfig.java
    │       │   ├── web/                # Web配置
    │       │   │   ├── WebConfig.java
    │       │   │   └── SwaggerConfig.java
    │       │   ├── idempotent/         # 幂等性配置
    │       │   │   └── IdempotentConfig.java
    │       │   ├── retry/              # 重试配置
    │       │   │   └── RetryConfig.java
    │       │   └── trace/              # 链路追踪配置
    │       │       └── TraceConfig.java
    │       ├── external/               # 外部服务集成
    │       │   ├── dify/               # Dify平台集成
    │       │   │   ├── DifyService.java
    │       │   │   ├── WorkflowService.java
    │       │   │   ├── KnowledgeBaseService.java
    │       │   │   ├── dto/            # Dify API模型
    │       │   │   │   ├── request/    # 请求模型
    │       │   │   │   │   ├── DifyChatReq.java
    │       │   │   │   │   ├── DifyWorkflowReq.java
    │       │   │   │   │   ├── DifyKnowledgeReq.java
    │       │   │   │   │   └── DifyFileUploadReq.java
    │       │   │   │   └── response/   # 响应模型
    │       │   │   │       ├── DifyChatResp.java
    │       │   │   │       ├── DifyWorkflowResp.java
    │       │   │   │       ├── DifyKnowledgeResp.java
    │       │   │   │       └── DifyFileUploadResp.java
    │       │   │   └── converter/      # Dify模型转换器
    │       │   │       └── DifyConverter.java
    │       │   ├── minio/              # MinIO存储集成
    │       │   │   ├── MinioService.java
    │       │   │   ├── FileUploadService.java
    │       │   │   ├── FileDownloadService.java
    │       │   │   └── dto/            # MinIO API模型
    │       │   │       ├── request/    # 请求模型
    │       │   │       │   ├── MinioUploadReq.java
    │       │   │       │   ├── MinioDownloadReq.java
    │       │   │       │   └── MinioDeleteReq.java
    │       │   │       └── response/   # 响应模型
    │       │   │           ├── MinioUploadResp.java
    │       │   │           ├── MinioDownloadResp.java
    │       │   │           └── MinioDeleteResp.java
    │       │   ├── kafka/              # Kafka消息集成
    │       │   │   ├── KafkaProducer.java
    │       │   │   ├── KafkaConsumer.java
    │       │   │   └── dto/            # Kafka消息模型
    │       │   │       ├── request/    # 请求模型
    │       │   │       │   ├── KafkaMessageReq.java
    │       │   │       │   └── KafkaEventReq.java
    │       │   │       └── response/   # 响应模型
    │       │   │           ├── KafkaMessageResp.java
    │       │   │           └── KafkaEventResp.java
    │       │   └── redis/              # Redis缓存集成
    │       │       ├── RedisService.java
    │       │       └── dto/            # Redis缓存模型
    │       │           ├── request/    # 请求模型
    │       │           │   ├── RedisSetReq.java
    │       │           │   ├── RedisGetReq.java
    │       │           │   └── RedisDeleteReq.java
    │       │           └── response/   # 响应模型
    │       │               ├── RedisSetResp.java
    │       │               ├── RedisGetResp.java
    │       │               └── RedisDeleteResp.java
    │       ├── common/                 # 通用组件
    │       │   ├── annotation/         # 自定义注解
    │       │   │   ├── Log.java
    │       │   │   ├── Idempotent.java
    │       │   │   └── Retry.java
    │       │   ├── interceptor/        # 拦截器
    │       │   │   ├── AuthInterceptor.java
    │       │   │   ├── LogInterceptor.java
    │       │   │   └── IdempotentInterceptor.java
    │       │   ├── aspect/             # 切面
    │       │   │   ├── LogAspect.java
    │       │   │   ├── IdempotentAspect.java
    │       │   │   └── RetryAspect.java
    │       │   ├── handler/            # 处理器
    │       │   │   ├── GlobalExceptionHandler.java
    │       │   │   ├── GlobalEventHandler.java
    │       │   │   └── SecurityHandler.java
    │       │   └── filter/             # 过滤器
    │       │       ├── AuthFilter.java
    │       │       ├── LogFilter.java
    │       │       └── TraceFilter.java
    │       └── shared/                 # 共享组件
    │           ├── result/             # 统一返回结果
    │           │   ├── Result.java
    │           │   ├── ResultCode.java
    │           │   └── PageResult.java
    │           ├── exception/           # 异常处理
    │           │   ├── BusinessException.java
    │           │   ├── ValidationException.java
    │           │   └── NotFoundException.java
    │           ├── event/               # 事件
    │           │   ├── EventPublisher.java        # 事件发布器
    │           │   ├── DomainEvent.java
    │           │   ├── UserLoginEvent.java
    │           │   ├── UserLogoutEvent.java
    │           │   ├── UserRegisteredEvent.java
    │           │   └── ProjectCreatedEvent.java
    │           ├── constant/           # 常量
    │           │   ├── SystemConstant.java
    │           │   ├── CacheConstant.java
    │           │   └── MessageConstant.java
    │           ├── enums/              # 枚举
    │           │   ├── UserStatus.java
    │           │   ├── ProjectStatus.java
    │           │   └── DeclarationStatus.java
    │           └── utils/              # 工具类
    │               ├── JsonUtils.java
    │               ├── DateUtils.java
    │               ├── FileUtils.java
    │               └── ValidationUtils.java
    │
    └── resources/                       # 资源文件
        ├── application.yml              # 应用配置
        ├── application-dev.yml          # 开发环境配置
        ├── application-prod.yml        # 生产环境配置
        ├── mapper/                     # MyBatis映射文件
        │   ├── user/                   # 用户相关映射文件
        │   │   └── UserMapper.xml
        │   ├── project/                # 项目相关映射文件
        │   │   └── ProjectMapper.xml
        │   ├── declaration/            # 申报相关映射文件
        │   │   └── DeclarationMapper.xml
        │   ├── report/                 # 报告相关映射文件
        │   │   └── ReportMapper.xml
        │   ├── knowledge/              # 知识库相关映射文件
        │   │   └── KnowledgeMapper.xml
        │   └── ai/                     # AI相关映射文件
        │       └── ConversationMapper.xml
        ├── static/                     # 静态资源
        └── templates/                  # 模板文件
```

### DDD 分层职责说明

#### 📡 接口层（Interfaces Layer）

- **职责**：处理用户请求、参数校验、权限验证、响应返回
- **核心组件**：
  - Controller：RESTful API 端点
  - Converter：统一转换器（单次转换）
  - DTO：数据传输对象（Request/Response）

#### ⚙️ 应用层（Application Layer）

- **职责**：业务用例编排、事务管理、跨聚合协调
- **核心组件**：
  - Service：应用服务接口，定义业务契约
  - ServiceImpl：应用服务实现，具体业务逻辑
  - Task：任务调度（定时任务、异步任务、工作流任务）

#### 🎯 领域层（Domain Layer）

- **职责**：核心业务逻辑、业务规则、领域模型
- **核心组件**：
  - Entity：领域实体，具有唯一标识的业务对象
  - DTO：数据传输对象（Request/Response）
  - Mapper：数据映射接口
  - Repository：仓储接口，定义数据访问契约

#### 🔧 基础设施层（Infrastructure Layer）

- **职责**：技术实现、外部系统集成、数据持久化
- **核心组件**：
  - Config：配置类（database、cache、mq、security、storage、ai、web、idempotent、retry、trace）
  - External：外部服务集成（dify、minio、kafka、redis）
  - Common：通用组件（annotation、interceptor、aspect、handler、filter）
  - Shared：共享组件（result、exception、event、constant、enums、utils）

## 🚀 功能模块

本系统共包含 **9 大核心功能模块**，覆盖科研项目全生命周期管理。

### 2.1 🔐 认证模块

- **用户登录**：支持用户名/邮箱登录，可选记住登录状态
- **用户注册**：新用户注册，支持邮箱/手机验证
- **密码重置**：通过邮箱/手机验证码重置密码
- **验证码**：图形验证码防止暴力破解
- **自动登录**：支持 7 天免登录

### 2.2 📊 仪表板模块

- **统计卡片**：项目总数、进行中、待验收、已完成
- **项目进度概览**：可视化展示所有项目的进度
- **最近申报列表**：最新提交的申报记录
- **快捷操作**：快速创建申报、项目、报告等

### 2.3 📝 申报模块

- **申报列表**：查看所有申报记录，支持搜索、筛选、分页
- **新建申报**：填写申报信息（研究方向、领域、课题等）
- **申报详情**：查看申报详细信息和工作流执行结果
- **申报状态**：草稿、已提交、审核中、已通过、已拒绝
- **Dify 工作流**：智能辅助申报信息填写和优化建议

### 2.4 📊 项目模块

- **项目列表**：查看所有项目，支持搜索、筛选、分页
- **项目详情**：查看项目基本信息、成员、进度、文档
- **成员管理**：添加/移除项目成员，分配成员角色
- **进度跟踪**：项目里程碑、任务分配、进度更新
- **文档管理**：项目相关文档的上传、分类、版本控制
- **知识库集成**：项目专属知识库，支持 AI 问答

### 2.5 📄 报告管理模块

- **报告列表**：查看所有报告，支持搜索、筛选、分页
- **创建报告**：选择项目和报告类型，配置生成参数
- **生成报告**：触发 Dify 工作流，实时显示生成进度
- **报告预览**：在线预览生成的报告内容
- **报告导出**：支持 PDF、Word、Excel 等格式导出
- **报告模板**：可配置的报告模板和样式

### 2.6 📁 文件管理模块

- **文件上传**：单文件/批量文件上传
- **文件列表**：查看文件列表，支持筛选、搜索
- **文件预览**：在线预览 PDF、图片等文件
- **文件下载**：支持单文件/批量下载
- **文件去重**：基于文件哈希值自动去重
- **MinIO 集成**：分布式文件存储，支持高可用

### 2.7 📚 知识库模块

- **知识库创建**：创建独立知识库或关联项目知识库
- **文件夹管理**：创建多级文件夹，支持重命名、删除
- **文件管理**：上传、重命名、删除、移动文件
- **AI 问答**：基于知识库内容的智能问答
- **语义搜索**：向量化搜索，支持语义理解
- **Dify 同步**：与 Dify 平台知识库双向同步

### 2.8 🤖 AI 助手模块

- **创建会话**：创建新的对话会话
- **发送消息**：发送用户消息，接收 AI 回复
- **会话列表**：查看所有对话会话
- **会话管理**：删除、重命名会话
- **上下文理解**：基于项目上下文的智能回答
- **多模态支持**：支持文本、图片等多种输入格式

### 2.9 👤 用户中心模块

- **个人信息**：查看和编辑个人基本信息
- **头像上传**：上传和更换用户头像
- **密码修改**：修改登录密码
- **安全设置**：绑定手机号、邮箱，设置安全选项
- **行业配置**：根据行业类型显示不同的字段配置
- **操作日志**：查看个人操作历史记录

### 2.10 ⚙️ 系统管理模块

- **用户管理**：用户列表查询、创建、编辑、删除用户
- **角色管理**：角色创建、权限分配、角色继承
- **权限管理**：权限点管理、菜单权限、数据权限
- **部门管理**：组织架构管理、部门层级设置
- **系统配置**：系统参数配置、行业配置管理
- **日志管理**：操作日志、登录日志、系统日志查询
- **数据统计**：系统使用情况统计、用户活跃度分析

## 🚀 快速开始

### 环境要求

**后端环境：**

- **JDK 21** (LTS, 推荐使用 Eclipse Temurin 或 Oracle OpenJDK)
- **Maven 3.9+** (支持 Java 21)
- **PostgreSQL 14+** (推荐 PostgreSQL 15 或 16)
- **Redis 7.x** (推荐 Redis 7.2+)
- **Kafka 3.x** (推荐 Kafka 3.6+)
- **MinIO** (最新稳定版)

### 本地开发

1. **克隆项目**

```bash
git clone https://github.com/your-org/sci-z-server.git
cd sci-z-server
```

2. **配置数据库**

```bash
# 创建数据库
createdb sci_z_platform

# 执行初始化脚本
psql -d sci_z_platform -f sql/init.sql
```

3. **配置应用**

```bash
# 复制配置文件
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

# 编辑配置文件，设置数据库连接等信息
vim src/main/resources/application-dev.yml
```

4. **启动服务**

```bash
# 启动 Redis
redis-server

# 启动 Kafka
kafka-server-start.sh config/server.properties

# 启动 MinIO
minio server /data

# 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

5. **访问应用**

- 应用地址：http://localhost:8080
- API 文档：http://localhost:8080/swagger-ui.html

## 🚀 部署说明

### 部署步骤

#### 1. 基础设施部署

**PostgreSQL 主从配置**

```bash
# 主库配置
# postgresql.conf
wal_level = replica
max_wal_senders = 3
wal_keep_segments = 64

# pg_hba.conf
host replication replicator 192.168.1.0/24 md5
```

**Redis 集群配置**

```bash
# redis.conf
port 7000
cluster-enabled yes
cluster-config-file nodes-7000.conf
cluster-node-timeout 5000
```

**Kafka 集群配置**

```bash
# server.properties
broker.id=1
listeners=PLAINTEXT://localhost:9092
num.network.threads=3
num.io.threads=8
```

#### 2. 应用部署

**构建应用**

```bash
# 编译打包
mvn clean package -Dmaven.test.skip=true

# 生成的 JAR 文件
# target/sci-z-server-1.0.0.jar
```

**启动应用**

```bash
# 方式 1: 直接启动（开发环境）
java -jar sci-z-server-1.0.0.jar --spring.profiles.active=dev

# 方式 2: 后台启动（生产环境）
nohup java -jar sci-z-server-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &

# 方式 3: 使用 JVM 参数优化（推荐）
java -Xms2g -Xmx4g \
     -XX:+UseZGC \
     -XX:+EnableVirtualThreads \
     -Dspring.profiles.active=prod \
     -jar sci-z-server-1.0.0.jar
```

#### 3. Nginx 配置

```nginx
upstream sci_z_backend {
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://sci_z_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 配置说明

#### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sci_z_platform
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
```

#### Redis 配置

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
```

#### MinIO 配置

```yaml
minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket-name: ${MINIO_BUCKET:sci-z-files}
```

#### Dify 配置

```yaml
dify:
  api-url: ${DIFY_API_URL:http://localhost:5001}
  api-key: ${DIFY_API_KEY:your-api-key}
  workspace-id: ${DIFY_WORKSPACE_ID:your-workspace-id}
```

## 🛠️ 开发指南

### 开发环境搭建

1. **安装 JDK 21**

```bash
# 使用 SDKMAN 安装
curl -s "https://get.sdkman.io" | bash
sdk install java 21.0.1-tem
```

2. **安装 Maven**

```bash
# 使用 SDKMAN 安装
sdk install maven 3.9.6
```

3. **安装 PostgreSQL**

```bash
# Ubuntu/Debian
sudo apt-get install postgresql-14

# macOS
brew install postgresql@14
```

4. **安装 Redis**

```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# macOS
brew install redis
```

### 代码规范

#### 类名注释模板

```java
/**
 * @author JiaWen.Wu
 * @className 当前类名
 * @date 当前时间(yyyy-MM-dd HH:mm)
 */
```

#### 方法名注释模板

```java
/**
 * 方法是做什么的
 *
 * @param 参数类型 参数描述
 * @return 返回结果
 */
```

### 统一返回格式

所有接口使用统一的返回结果格式：

```java
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
}
```

### 日志规范

```java
// 使用 SLF4J + Logback
private static final Logger logger = LoggerFactory.getLogger(YourClass.class);

// 日志级别使用
logger.debug("调试信息");
logger.info("一般信息");
logger.warn("警告信息");
logger.error("错误信息", exception);
```

### 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` 修复问题
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建过程或辅助工具的变动

## 📄 许可证

MIT License

## 📞 联系我们

如有问题，请联系开发团队。
