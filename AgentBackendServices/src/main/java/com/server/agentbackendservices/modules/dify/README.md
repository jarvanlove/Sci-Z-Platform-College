# Dify API 集成模块

## 📋 模块概述

Dify API 集成模块提供了与 Dify 知识库平台的完整集成功能，包括数据集管理、文档上传、知识库检索等核心功能。该模块采用 Spring Boot 架构，支持配置化管理，提供 RESTful API 接口。

## 🏗️ 模块架构

```
dify/
├── config/                 # 配置类
│   ├── DifyConfig.java    # Dify API 基础配置
│   └── DifyDocumentConfig.java  # 文档处理配置
├── controller/            # 控制器层
│   └── DifyApiController.java
├── dto/                  # 数据传输对象
│   ├── DifyDatasetRequest.java
│   └── DifyRetrieveRequest.java
├── service/              # 服务层
│   └── DifyApiService.java
├── util/                 # 工具类
│   └── DifyApiClient.java
└── README.md            # 本文档
```

## 🚀 核心功能

### 1. 数据集管理
- **获取数据集列表** - 分页查询所有数据集
- **创建数据集** - 创建新的知识库数据集
- **获取数据集详情** - 查看指定数据集的详细信息
- **更新数据集** - 修改数据集配置
- **删除数据集** - 删除指定的数据集

### 2. 文档处理
- **文档上传** - 支持多种格式文档上传到数据集
- **文件存储** - 本地文件存储管理
- **文件验证** - 文件大小和格式验证
- **配置化处理** - 可配置的文档处理参数

### 3. 知识库检索
- **内容检索** - 基于关键词的知识库内容检索
- **智能搜索** - 支持混合搜索模式
- **结果排序** - 可配置的搜索结果排序

## 📡 API 接口

### 基础路径
```
/api/dify
```

### 通用规则
- 非上传接口：`application/json`，请求体包含 `BaseDifyRequest` 或其子类（统一包含 userId、resourceId、keyType）。
- 上传接口：`multipart/form-data`，使用表单字段传递 `file`、`userId`、`resourceId`、`keyType`。

---

### 数据集 APIs

#### 获取数据集列表
- 路由：`POST /api/dify/datasets/list?page=1&limit=10`
- 请求体：`BaseDifyRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/datasets/list?page=1&limit=10" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"knowledge_base_001",
    "keyType":"dataset"
  }'
```

#### 创建数据集
- 路由：`POST /api/dify/datasets`
- 请求体：`DifyDatasetRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/datasets" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"knowledge_base_001",
    "keyType":"dataset",
    "name":"测试数据集",
    "description":"这是一个测试数据集",
    "permission":"only_me",
    "indexing_technique":"high_quality"
  }'
```

#### 获取数据集详情
- 路由：`POST /api/dify/datasets/{datasetId}`
- 请求体：`BaseDifyRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/datasets/dataset_123" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"knowledge_base_001",
    "keyType":"dataset"
  }'
```

#### 更新数据集
- 路由：`PUT /api/dify/datasets/{datasetId}`
- 请求体：`DifyDatasetRequest`
```bash
curl -X PUT "http://localhost:8081/api/dify/datasets/dataset_123" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"knowledge_base_001",
    "keyType":"dataset",
    "name":"测试数据集-更新",
    "description":"更新描述"
  }'
```

#### 删除数据集
- 路由：`POST /api/dify/datasets/{datasetId}/delete`
- 请求体：`BaseDifyRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/datasets/dataset_123/delete" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"knowledge_base_001",
    "keyType":"dataset"
  }'
```

#### 检索知识库
- 路由：`POST /api/dify/datasets/{datasetId}/retrieve`
- 请求体：`DifyRetrieveRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/datasets/dataset_123/retrieve" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"knowledge_base_001",
    "keyType":"dataset",
    "query":"工业互联网的核心挑战是什么？"
  }'
```

#### 上传文档到数据集（multipart/form-data）
- 路由：`POST /api/dify/datasets/{datasetId}/document/upload`
- 表单字段：`file`、`userId`、`resourceId`、`keyType`
```bash
curl -X POST "http://localhost:8081/api/dify/datasets/dataset_123/document/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/absolute/path/to/file.pdf" \
  -F "userId=admin" \
  -F "resourceId=knowledge_base_001" \
  -F "keyType=dataset"
```

---

### 工作流 APIs

#### 执行工作流
- 路由：`POST /api/dify/workflows/run`
- 请求体：`DifyWorkflowRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/workflows/run" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"workflow_001",
    "keyType":"workflow",
    "inputs": { "technology_report": "请总结这段文字：..." },
    "responseMode":"blocking",
    "user":"workflow_user_001"
  }'
```

#### 获取工作流运行状态
- 路由：`POST /api/dify/workflows/run/status`
- 请求体：`DifyWorkflowStatusRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/workflows/run/status" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"workflow_001",
    "keyType":"workflow",
    "workflowRunId":"3c90c3cc-0d44-4b50-8888-8dd25736052a"
  }'
```

#### 获取工作流日志
- 路由：`POST /api/dify/workflows/logs`
- 请求体：`DifyWorkflowLogsRequest`
```bash
curl -X POST "http://localhost:8081/api/dify/workflows/logs" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":"admin",
    "resourceId":"workflow_001",
    "keyType":"workflow",
    "page":1,
    "limit":20
  }'
```

---

### 通用文件上传（multipart/form-data）
- 路由：`POST /api/dify/files/upload`
- 表单字段：`file`、`userId`、`resourceId`、`keyType`
```bash
curl -X POST "http://localhost:8081/api/dify/files/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/absolute/path/to/file.pdf" \
  -F "userId=admin" \
  -F "resourceId=workflow_001" \
  -F "keyType=workflow"
```

## ⚙️ 配置说明

### 基础配置 (application-dev.yml)

```yaml
dify:
  # API 基础配置
  base-url: http://192.168.1.203/v1
  api-key: dataset-MwOxGbIDhZmg6bUdHEid0rhX
  timeout: 30000
  connect-timeout: 30000
  retry-count: 3
  enable-retry: true
  
  # 文档处理配置
  document:
    indexing-technique: high_quality
    doc-form: text_model
    doc-language: Chinese
    embedding-model: text-embedding-v3
    embedding-model-provider: tongyi
    retrieval-model:
      search-method: hybrid_search
      reranking-enable: false
      top-k: 3
      score-threshold-enabled: false
      score-threshold: 0.5
    process-rule:
      mode: automatic
  
  # 文件上传配置
  upload:
    dir: D:/uploads/dify/
    max-file-size: 52428800  # 50MB
    allowed-extensions: .txt,.pdf,.doc,.docx,.md,.csv,.json
```

### 环境变量支持

所有配置项都支持环境变量覆盖：

```bash
# 环境变量示例
DIFY_BASE_URL=http://192.168.1.203/v1
#DIFY_API_KEY=dataset-MwOxGbIDhZmg6bUdHEid0rhX
DIFY_TIMEOUT=30000
DIFY_CONNECT_TIMEOUT=30000
DIFY_RETRY_COUNT=3
DIFY_ENABLE_RETRY=true
```


## 🛠️ 开发指南

### 添加新的 API 接口

1. **在 DifyApiService 中添加方法：**
```java
public ResponseEntity<String> newApiMethod(String param) {
    try {
        return difyApiClient.request("GET", "/new-endpoint", param);
    } catch (HttpClientErrorException e) {
        log.error("API调用失败: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
}
```

2. **在 DifyApiController 中添加接口：**
```java
@GetMapping("/new-endpoint")
@Operation(summary = "新接口说明")
public ResponseEntity<String> newApi(@RequestParam String param) {
    return difyApiService.newApiMethod(param);
}
```

### 配置新的参数

1. **在配置类中添加字段：**
```java
@Data
@ConfigurationProperties(prefix = "dify.new-config")
public class DifyNewConfig {
    private String newParam = "default-value";
}
```

2. **在 YAML 文件中添加配置：**
```yaml
dify:
  new-config:
    new-param: "custom-value"
```

## 📊 监控和日志

### 日志级别
```yaml
logging:
  level:
    com.server.agentbackendservices.modules.dify: DEBUG
```

### 关键日志信息
- API 调用成功/失败
- 文件上传进度
- 配置加载状态
- 错误详情

## 🔒 安全考虑

### 1. API 密钥管理
- 使用环境变量存储敏感信息
- 不在代码中硬编码密钥

### 2. 文件上传安全
- 文件类型验证
- 文件大小限制
- 上传目录隔离

### 3. 错误信息处理
- 不暴露内部实现细节
- 统一错误响应格式

## 🚀 部署说明

### 1. 环境要求
- Java 21+
- Spring Boot 3.5.6
- Maven 3.6+

### 2. 配置文件
- 开发环境：`application-dev.yml`
- 测试环境：`application-test.yml`
- 生产环境：`application-prod.yml`

### 3. 启动命令
```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
java -jar target/AgentBackendServices-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 📚 相关文档

- [Dify API 官方文档](https://docs.dify.ai/)
- [Spring Boot 配置文档](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [Swagger UI 接口文档](http://localhost:8081/swagger-ui.html)

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

**版本：** 1.0.0
