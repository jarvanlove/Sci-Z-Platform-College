# 知识库管理接口文档

## 接口基础信息

- **Controller**: `KnowledgeController`
- **基础路径**: `/api/knowledge`
- **描述**: 知识库管理相关接口
---

## 1. 创建知识库

### 接口信息

- **请求方式**: `POST`
- **接口地址**: `/api/knowledge`
- **接口描述**: 创建新的知识库，调用Dify API创建数据集并保存到数据库

### 请求参数

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
{
  "name": "我的知识库",
  "description": "这是一个测试知识库",
  "projectId": 1,
  "isShared": 0
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 知识库名称 |
| description | String | 否 | 知识库描述 |
| projectId | Long | 否 | 关联的项目ID |
| isShared | Integer | 否 | 是否共享，0-不共享，1-共享，默认为0 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "我的知识库",
    "description": "这是一个测试知识库",
    "ownerId": 1,
    "ownerName": "张三",
    "projectId": 1,
    "difyKbId": "0453e46d-263a-46f3-8c8d-669981218921",
    "difyKnowdataId": "0453e46d-263a-46f3-8c8d-669981218921",
    "callback": "{\"id\":\"0453e46d-263a-46f3-8c8d-669981218921\",\"name\":\"我的知识库\",...}",
    "isShared": 0,
    "status": "active",
    "fileCount": 0,
    "folderCount": 0,
    "createdTime": "2025-01-28T16:00:00",
    "updatedTime": "2025-01-28T16:00:00"
  },
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 2. 分页获取知识库列表

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/knowledge`
- **接口描述**: 分页获取知识库列表，如果用户已登录则只返回该用户的知识库

### 请求参数

**查询参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页大小，默认为10 |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
GET /api/knowledge?page=1&size=10
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "我的知识库",
        "description": "这是一个测试知识库",
        "ownerId": 1,
        "ownerName": "张三",
        "projectId": 1,
        "difyKbId": "0453e46d-263a-46f3-8c8d-669981218921",
        "difyKnowdataId": "0453e46d-263a-46f3-8c8d-669981218921",
        "callback": "{\"id\":\"0453e46d-263a-46f3-8c8d-669981218921\",...}",
        "isShared": 0,
        "status": "active",
        "fileCount": 0,
        "folderCount": 0,
        "createdTime": "2025-01-28T16:00:00",
        "updatedTime": "2025-01-28T16:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 3. 上传文件到知识库

### 接口信息

- **请求方式**: `POST`
- **接口地址**: `/api/knowledge/{id}/upload`
- **接口描述**: 向知识库上传文件，调用Dify API上传文档并保存关联关系

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 知识库ID（Dify知识库ID） |

**查询参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| folderId | Long | 否 | 文件夹ID，默认为0（根目录） |

**请求头：**
```
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**请求体：**
- `file`: 文件（multipart/form-data）

**请求示例：**
```
POST /api/knowledge/0453e46d-263a-46f3-8c8d-669981218921/upload?folderId=0
Content-Type: multipart/form-data

file: [文件内容]
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 4. 基于知识库的 Chatbot 流式对话

### 接口信息

- **请求方式**: `POST`
- **接口地址**: `/api/knowledge/chatbot/stream`
- **接口描述**: 基于知识库的Chatbot流式问答，如果用户未创建Chatbot则返回提示

### 请求参数

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
{
  "knowledgeId": "0453e46d-263a-46f3-8c8d-669981218921",
  "query": "什么是人工智能？",
  "conversationId": "conv-123456",
  "user": "user-001"
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| knowledgeId | String | 是 | 知识库ID（Dify知识库ID） |
| query | String | 是 | 用户问题 |
| conversationId | String | 否 | 会话ID，用于保持对话上下文 |
| user | String | 否 | 用户标识，如果不提供则使用当前登录用户ID |

### 响应示例

#### 情况1：用户未创建 Chatbot（HTTP 400）

**响应头：**
```
Content-Type: application/json
```

**响应体：**
```json
{
  "error": true,
  "code": "CHATBOT_NOT_CREATED",
  "message": "请先创建 Chatbot 应用",
  "hint": "您需要先创建 Chatbot 应用才能使用知识库问答功能"
}
```

**说明：** 当用户未创建 Chatbot 应用时，接口会返回此错误提示，前端可以根据此提示引导用户去创建 Chatbot。

#### 情况2：用户已创建 Chatbot（流式响应）

**响应头：**
```
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive
```

**响应体（SSE格式）：**
```
data: {"event": "message", "answer": "人工智能（AI）是..."}

data: {"event": "message", "answer": "一门研究如何让计算机..."}

data: {"event": "message", "answer": "模拟人类智能的技术。"}

data: {"event": "message_end", "conversation_id": "conv-123456", "message_id": "msg-789012"}

```

**说明：** 
- 响应格式为 Server-Sent Events (SSE)
- 每个 `data:` 行包含一个 JSON 对象
- `event` 字段表示事件类型：
  - `message`: 消息片段（流式输出）
  - `message_end`: 消息结束
- `answer` 字段包含回答内容
- `conversation_id` 和 `message_id` 在消息结束时返回，可用于后续对话

**前端处理示例（JavaScript）：**
```javascript
const eventSource = new EventSource('/api/knowledge/chatbot/stream', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({
    knowledgeId: '0453e46d-263a-46f3-8c8d-669981218921',
    query: '什么是人工智能？'
  })
});

eventSource.onmessage = function(event) {
  const data = JSON.parse(event.data);
  if (data.event === 'message') {
    // 追加回答内容
    appendAnswer(data.answer);
  } else if (data.event === 'message_end') {
    // 保存会话ID和消息ID
    saveConversationId(data.conversation_id);
    eventSource.close();
  }
};

eventSource.onerror = function(error) {
  console.error('流式响应错误:', error);
  eventSource.close();
};
```

**注意：** 
- 由于 SSE 的限制，实际实现中可能需要使用 `fetch` API 配合 `ReadableStream` 来处理 POST 请求的流式响应
- 或者使用 WebSocket 来实现双向流式通信

---

## 统一响应格式

所有非流式接口都遵循统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**响应字段说明：**
- `code`: 响应码，200表示成功
- `message`: 响应消息
- `data`: 响应数据（根据接口不同而变化）
- `timestamp`: 时间戳
- `traceId`: 追踪ID

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或token过期） |
| 1002 | 数据不存在 |
| 1004 | 操作失败 |
| 12001 | 数据库操作失败 |
| CHATBOT_NOT_CREATED | 用户未创建Chatbot应用 |

---

## 注意事项

1. **知识库ID格式**：知识库ID使用 Dify 返回的 UUID 格式（String类型），不是数据库自增ID
2. **Chatbot检查**：流式对话接口会自动检查用户是否创建了 Chatbot，未创建时会返回明确的提示信息
3. **流式响应**：Chatbot流式对话接口返回的是 SSE 格式的流式数据，需要使用专门的客户端处理
4. **会话管理**：建议保存 `conversationId` 以便在后续对话中保持上下文

