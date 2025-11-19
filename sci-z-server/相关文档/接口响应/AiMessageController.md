# AI消息管理接口文档

## 接口基础信息

- **Controller**: `AiMessageController`
- **基础路径**: `/api/ai/message`
- **描述**: AI消息管理相关接口

---

## 1. 创建消息

### 接口信息

- **请求方式**: `POST`
- **接口地址**: `/api/ai/message`
- **接口描述**: 创建新的AI消息

### 请求参数

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
{
  "conversationId": "1",
  "role": "user",
  "content": "你好，我想了解一下项目管理的最佳实践",
  "difyMessageId": "msg-123456",
  "sources": "[{\"document_id\":\"doc-123\",\"document_name\":\"项目管理指南.pdf\"}]",
  "confidence": 0.95,
  "sendTime": "2025-11-14T10:00:00"
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | String | 是 | 会话ID |
| role | String | 是 | 角色(user/assistant)，最大10个字符 |
| content | String | 是 | 消息内容 |
| difyMessageId | String | 否 | Dify消息ID，最大100个字符 |
| sources | String | 否 | 知识来源(JSON格式) |
| confidence | BigDecimal | 否 | 置信度 |
| sendTime | LocalDateTime | 否 | 发送时间，默认为当前时间 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "conversationId": "1",
    "role": "user",
    "content": "你好，我想了解一下项目管理的最佳实践",
    "difyMessageId": "msg-123456",
    "sources": "[{\"document_id\":\"doc-123\",\"document_name\":\"项目管理指南.pdf\"}]",
    "confidence": null,
    "sendTime": "2025-11-14T10:00:00",
    "createdTime": "2025-11-14T10:00:00",
    "updatedTime": "2025-11-14T10:00:00"
  },
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**失败响应：**
```json
{
  "code": 1001,
  "message": "数据验证失败",
  "data": null,
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 2. 更新消息

### 接口信息

- **请求方式**: `PUT`
- **接口地址**: `/api/ai/message`
- **接口描述**: 更新AI消息信息

### 请求参数

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
{
  "id": "1",
  "content": "更新后的消息内容",
  "sources": "[{\"document_id\":\"doc-456\",\"document_name\":\"更新文档.pdf\"}]",
  "confidence": 0.98
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 消息ID |
| conversationId | String | 否 | 会话ID |
| role | String | 否 | 角色(user/assistant)，最大10个字符 |
| content | String | 否 | 消息内容 |
| difyMessageId | String | 否 | Dify消息ID，最大100个字符 |
| sources | String | 否 | 知识来源(JSON格式) |
| confidence | BigDecimal | 否 | 置信度 |
| sendTime | LocalDateTime | 否 | 发送时间 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "conversationId": "1",
    "role": "user",
    "content": "更新后的消息内容",
    "difyMessageId": "msg-123456",
    "sources": "[{\"document_id\":\"doc-456\",\"document_name\":\"更新文档.pdf\"}]",
    "confidence": 0.98,
    "sendTime": "2025-11-14T10:00:00",
    "createdTime": "2025-11-14T10:00:00",
    "updatedTime": "2025-11-14T10:30:00"
  },
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 3. 查询消息详情

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/ai/message/{id}`
- **接口描述**: 根据ID查询AI消息详情

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 消息ID |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
GET /api/ai/message/1
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "conversationId": "1",
    "role": "user",
    "content": "你好，我想了解一下项目管理的最佳实践",
    "difyMessageId": "msg-123456",
    "sources": "[{\"document_id\":\"doc-123\",\"document_name\":\"项目管理指南.pdf\"}]",
    "confidence": 0.95,
    "sendTime": "2025-11-14T10:00:00",
    "createdTime": "2025-11-14T10:00:00",
    "updatedTime": "2025-11-14T10:00:00"
  },
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 4. 分页查询消息列表

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/ai/message/page`
- **接口描述**: 分页查询指定会话的消息列表

### 请求参数

**查询参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | String | 是 | 会话ID |
| pageNo | Integer | 否 | 页码，默认为1 |
| pageSize | Integer | 否 | 每页数量，默认为10 |
| sortBy | String | 否 | 排序字段 |
| sortOrder | String | 否 | 排序方式（ASC/DESC），默认为ASC |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
GET /api/ai/message/page?conversationId=1&pageNo=1&pageSize=10
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
        "id": "1",
        "conversationId": "1",
        "role": "user",
        "content": "你好，我想了解一下项目管理的最佳实践",
        "difyMessageId": "msg-123456",
        "sources": null,
        "confidence": null,
        "sendTime": "2025-11-14T10:00:00",
        "createdTime": "2025-11-14T10:00:00",
        "updatedTime": "2025-11-14T10:00:00"
      },
      {
        "id": "2",
        "conversationId": "1",
        "role": "assistant",
        "content": "项目管理的最佳实践包括：1. 明确项目目标和范围 2. 制定详细的项目计划 3. 建立有效的沟通机制...",
        "difyMessageId": "msg-123457",
        "sources": "[{\"document_id\":\"doc-123\",\"document_name\":\"项目管理指南.pdf\"}]",
        "confidence": 0.95,
        "sendTime": "2025-11-14T10:01:00",
        "createdTime": "2025-11-14T10:01:00",
        "updatedTime": "2025-11-14T10:01:00"
      }
    ],
    "total": 2,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**响应字段说明：**
- `records`: 数据列表
- `total`: 总记录数
- `current`: 当前页码
- `size`: 每页大小
- `pages`: 总页数

**排序规则：**
- 默认按 `sendTime` 升序排列（最早的消息在前）

---

## 5. 查询消息列表

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/ai/message/list/{conversationId}`
- **接口描述**: 查询指定会话的所有消息列表

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | String | 是 | 会话ID |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
GET /api/ai/message/list/1
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "1",
      "conversationId": "1",
      "role": "user",
      "content": "你好，我想了解一下项目管理的最佳实践",
      "difyMessageId": "msg-123456",
      "sources": null,
      "confidence": null,
      "sendTime": "2025-11-14T10:00:00",
      "createdTime": "2025-11-14T10:00:00",
      "updatedTime": "2025-11-14T10:00:00"
    },
    {
      "id": "2",
      "conversationId": "1",
      "role": "assistant",
      "content": "项目管理的最佳实践包括：1. 明确项目目标和范围 2. 制定详细的项目计划 3. 建立有效的沟通机制...",
      "difyMessageId": "msg-123457",
      "sources": "[{\"document_id\":\"doc-123\",\"document_name\":\"项目管理指南.pdf\"}]",
      "confidence": 0.95,
      "sendTime": "2025-11-14T10:01:00",
      "createdTime": "2025-11-14T10:01:00",
      "updatedTime": "2025-11-14T10:01:00"
    }
  ],
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 6. 删除消息

### 接口信息

- **请求方式**: `DELETE`
- **接口地址**: `/api/ai/message/{id}`
- **接口描述**: 根据ID删除AI消息（软删除）

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 消息ID |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
DELETE /api/ai/message/1
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**说明：** 此接口为软删除，不会真正删除数据，只是将 `is_deleted` 字段设置为1

---

## 7. 批量删除消息

### 接口信息

- **请求方式**: `DELETE`
- **接口地址**: `/api/ai/message/batch`
- **接口描述**: 批量删除AI消息（软删除）

### 请求参数

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
["1", "2", "3"]
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | List<String> | 是 | 消息ID列表 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 8. 删除会话所有消息

### 接口信息

- **请求方式**: `DELETE`
- **接口地址**: `/api/ai/message/conversation/{conversationId}`
- **接口描述**: 删除指定会话的所有消息（软删除）

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | String | 是 | 会话ID |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
DELETE /api/ai/message/conversation/1
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**说明：** 此接口会删除指定会话下的所有消息，为软删除操作

---

## 统一响应格式

所有接口都遵循统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1734235200000,
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
| 403 | 禁止访问（无权操作） |
| 7002 | AI会话不存在 |
| 7003 | AI消息不存在 |
| 1001 | 数据验证失败 |
| 1004 | 操作失败 |
| 12001 | 数据库操作失败 |

