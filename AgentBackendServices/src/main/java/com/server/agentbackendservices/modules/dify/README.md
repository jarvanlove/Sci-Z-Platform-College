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

### 接口列表

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/datasets` | 获取数据集列表 | page, limit |
| POST | `/datasets` | 创建数据集 | DifyDatasetRequest |
| GET | `/datasets/{datasetId}` | 获取数据集详情 | datasetId |
| PUT | `/datasets/{datasetId}` | 更新数据集 | datasetId, DifyDatasetRequest |
| DELETE | `/datasets/{datasetId}` | 删除数据集 | datasetId |
| POST | `/datasets/{datasetId}/retrieve` | 检索知识库 | datasetId, DifyRetrieveRequest |
| POST | `/datasets/{datasetId}/document/upload` | 上传文档 | datasetId, file |

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
DIFY_API_KEY=dataset-MwOxGbIDhZmg6bUdHEid0rhX
DIFY_TIMEOUT=30000
DIFY_CONNECT_TIMEOUT=30000
DIFY_RETRY_COUNT=3
DIFY_ENABLE_RETRY=true
```

## 📝 使用示例

### 1. 获取数据集列表

```bash
GET /api/dify/datasets?page=1&limit=10
```

**响应示例：**
```json
{
  "data": [
    {
      "id": "dataset-123",
      "name": "知识库1",
      "description": "测试知识库",
      "created_at": "2024-10-24T10:00:00Z"
    }
  ],
  "total": 1,
  "page": 1,
  "limit": 10
}
```

### 2. 创建数据集

```bash
POST /api/dify/datasets
Content-Type: application/json

{
  "name": "新知识库",
  "description": "这是一个新的知识库",
  "permission": "only_me"
}
```

### 3. 上传文档

```bash
POST /api/dify/datasets/{datasetId}/document/upload
Content-Type: multipart/form-data

file: [选择文件]
```

**支持的文件格式：**
- `.txt` - 文本文件
- `.pdf` - PDF文档
- `.doc/.docx` - Word文档
- `.md` - Markdown文件
- `.csv` - CSV数据文件
- `.json` - JSON数据文件

### 4. 检索知识库

```bash
POST /api/dify/datasets/{datasetId}/retrieve
Content-Type: application/json

{
  "query": "人工智能"
}
```

**响应示例：**
```json
{
  "query": {
    "content": "人工智能",
    "top_k": 3
  },
  "records": [
    {
      "segment": {
        "content": "人工智能是计算机科学的一个分支...",
        "word_count": 150
      },
      "score": 0.95
    }
  ]
}
```

## 🔧 技术特性

### 1. 配置化管理
- 所有配置项都通过 YAML 文件管理
- 支持环境变量覆盖
- 类型安全的配置类

### 2. 错误处理
- 统一的异常处理机制
- 直接返回 Dify API 错误信息
- 详细的日志记录

### 3. 文件管理
- 本地文件存储
- 文件大小和格式验证
- 自动创建上传目录

### 4. HTTP 客户端
- 统一的 HTTP 请求封装
- 自动添加认证头
- 支持多种请求方法

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
