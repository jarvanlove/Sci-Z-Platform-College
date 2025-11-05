# Dify HTTP 请求工具

## 📁 核心文件

```
modules/dify/
├── config/
│   ├── DifyConfig.java              # 配置类
│   └── DifyRestTemplateConfig.java  # HTTP 客户端配置
├── util/
│   └── DifyApiClient.java          # HTTP 请求工具类（核心）
├── service/
│   └── DifyApiService.java         # 业务服务类
├── controller/
│   └── DifyApiController.java      # API 控制器
└── dto/
    ├── DifyDatasetRequest.java     # 数据集请求 DTO
    └── DifySearchRequest.java      # 搜索请求 DTO
```

## 🚀 核心功能

### DifyApiClient 工具类
封装了 Authorization 认证和 HTTP 请求：

```java
@Autowired
private DifyApiClient difyApiClient;

// 统一请求方法 - 根据请求类型自动判断
ResponseEntity<String> response = difyApiClient.request("GET", "/datasets");
ResponseEntity<String> response = difyApiClient.request("POST", "/datasets", requestBody);
ResponseEntity<String> response = difyApiClient.request("PUT", "/datasets/123", requestBody);
ResponseEntity<String> response = difyApiClient.request("DELETE", "/datasets/123");

// 带查询参数的 GET 请求
Map<String, Object> params = new HashMap<>();
params.put("page", 1);
params.put("limit", 10);
ResponseEntity<String> response = difyApiClient.request("GET", "/datasets", params);
```

## 📚 API 接口

### 数据集管理
- `GET /api/dify/datasets` - 获取数据集列表
- `POST /api/dify/datasets` - 创建数据集
- `GET /api/dify/datasets/{id}` - 获取数据集详情
- `PUT /api/dify/datasets/{id}` - 更新数据集
- `DELETE /api/dify/datasets/{id}` - 删除数据集

### 搜索功能
- `POST /api/dify/datasets/{id}/search` - 搜索数据集

### 文档管理
- `POST /api/dify/datasets/{id}/document/create-by-file` - 上传文档到数据集

### 统计信息
- `GET /api/dify/datasets/{id}/stats` - 获取数据集统计

## 🔧 配置

```yaml
dify:
  base-url: http://192.168.1.203
  api-key: dataset-MwOxGbIDhZmg6bUdHEid0rhX
  timeout: 30000
  connect-timeout: 10000
  retry-count: 3
  enable-retry: true
```

## 💻 使用示例

### 创建数据集
```http
POST /api/dify/datasets
Content-Type: application/json

{
  "name": "我的知识库",
  "description": "用于存储项目文档",
  "permission": "only_me",
  "indexing_technique": "high_quality"
}
```

### 搜索数据集
```http
POST /api/dify/datasets/{datasetId}/search
Content-Type: application/json

{
  "query": "什么是人工智能？",
  "top_k": 5,
  "search_method": "hybrid_search"
}
```

### 上传文档
```http
POST /api/dify/datasets/{datasetId}/document/create-by-file
Content-Type: application/json

{
  "file": "<文件内容>",
  "indexingTechnique": "high_quality",
  "docForm": "text_model",
  "docLanguage": "Chinese",
  "embeddingModel": "text-embedding-v3",
  "embeddingModelProvider": "tongyi",
  "retrievalModel": {
    "searchMethod": "hybrid_search",
    "rerankingEnable": false,
    "topK": 3
  },
  "processRule": {
    "mode": "automatic",
    "rules": {
      "preProcessingRules": {
        "removeExtraSpaces": true,
        "removeUrlsEmails": false
      },
      "segmentation": {
        "separator": "\\n",
        "maxTokens": 1000
      }
    }
  }
}
```

---

**🎯 简洁高效的 Dify API 集成工具！**
