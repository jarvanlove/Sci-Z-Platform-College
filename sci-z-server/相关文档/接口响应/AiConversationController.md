# AI会话管理接口文档

## 接口基础信息

- **Controller**: `AiConversationController`
- **基础路径**: `/api/ai/conversation`
- **描述**: AI会话管理相关接口

---

## 1. 创建会话

### 接口信息

- **请求方式**: `POST`
- **接口地址**: `/api/ai/conversation`
- **接口描述**: 创建新的AI会话

### 请求参数

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
{
  "title": "关于项目管理的讨论",
  "difyConversationId": "conv-123456",
  "isPinned": 0
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 会话标题，最大255个字符 |
| difyConversationId | String | 否 | Dify会话ID，最大100个字符 |
| isPinned | Integer | 否 | 是否置顶(0:否,1:是)，默认为0 |
### 响应示例
**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "userId": "100",
    "title": "关于项目管理的讨论",
    "difyConversationId": "conv-123456",
    "isPinned": 0,
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
## 2. 更新会话
### 接口信息
- **请求方式**: `PUT`
- **接口地址**: `/api/ai/conversation`
- **接口描述**: 更新AI会话信息
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
  "title": "更新后的会话标题",
  "difyConversationId": "conv-123456-updated",
  "isPinned": 1
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 会话ID |
| title | String | 否 | 会话标题，最大255个字符 |
| difyConversationId | String | 否 | Dify会话ID，最大100个字符 |
| isPinned | Integer | 否 | 是否置顶(0:否,1:是) |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "userId": "100",
    "title": "更新后的会话标题",
    "difyConversationId": "conv-123456-updated",
    "isPinned": 1,
    "createdTime": "2025-11-14T10:00:00",
    "updatedTime": "2025-11-14T10:30:00"
  },
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 3. 查询会话详情

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/ai/conversation/{id}`
- **接口描述**: 根据ID查询AI会话详情

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 会话ID |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
GET /api/ai/conversation/1
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "userId": "100",
    "title": "关于项目管理的讨论",
    "difyConversationId": "conv-123456",
    "isPinned": 0,
    "createdTime": "2025-11-14T10:00:00",
    "updatedTime": "2025-11-14T10:00:00"
  },
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 4. 分页查询会话列表

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/ai/conversation/page`
- **接口描述**: 分页查询当前用户的AI会话列表

### 请求参数

**查询参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 否 | 页码，默认为1 |
| pageSize | Integer | 否 | 每页数量，默认为10 |
| sortBy | String | 否 | 排序字段 |
| sortOrder | String | 否 | 排序方式（ASC/DESC），默认为DESC |
| keyword | String | 否 | 关键字搜索（标题） |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
GET /api/ai/conversation/page?pageNo=1&pageSize=10&keyword=项目
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
        "userId": "100",
        "title": "关于项目管理的讨论",
        "difyConversationId": "conv-123456",
        "isPinned": 1,
        "createdTime": "2025-11-14T10:00:00",
        "updatedTime": "2025-11-14T10:00:00"
      },
      {
        "id": "2",
        "userId": "100",
        "title": "技术问题咨询",
        "difyConversationId": "conv-123457",
        "isPinned": 0,
        "createdTime": "2025-11-14T09:00:00",
        "updatedTime": "2025-11-14T09:00:00"
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
- 优先按 `isPinned` 倒序排列（置顶的在前）
- 然后按 `createdTime` 倒序排列

---

## 5. 查询会话列表

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/ai/conversation/list`
- **接口描述**: 查询当前用户的所有AI会话列表

### 请求参数

**请求头：**
```
Authorization: Bearer {token}
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
      "userId": "100",
      "title": "关于项目管理的讨论",
      "difyConversationId": "conv-123456",
      "isPinned": 1,
      "createdTime": "2025-11-14T10:00:00",
      "updatedTime": "2025-11-14T10:00:00"
    },
    {
      "id": "2",
      "userId": "100",
      "title": "技术问题咨询",
      "difyConversationId": "conv-123457",
      "isPinned": 0,
      "createdTime": "2025-11-14T09:00:00",
      "updatedTime": "2025-11-14T09:00:00"
    }
  ],
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 6. 删除会话

### 接口信息

- **请求方式**: `DELETE`
- **接口地址**: `/api/ai/conversation/{id}`
- **接口描述**: 根据ID删除AI会话（软删除）

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 会话ID |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
DELETE /api/ai/conversation/1
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

## 7. 批量删除会话

### 接口信息

- **请求方式**: `DELETE`
- **接口地址**: `/api/ai/conversation/batch`
- **接口描述**: 批量删除AI会话（软删除）

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
| - | List<String> | 是 | 会话ID列表 |

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

## 8. 更新置顶状态

### 接口信息

- **请求方式**: `PUT`
- **接口地址**: `/api/ai/conversation/{id}/pinned`
- **接口描述**: 更新AI会话的置顶状态

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 会话ID |

**查询参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| isPinned | Integer | 是 | 是否置顶(0:否,1:是) |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
PUT /api/ai/conversation/1/pinned?isPinned=1
```

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "userId": "100",
    "title": "关于项目管理的讨论",
    "difyConversationId": "conv-123456",
    "isPinned": 1,
    "createdTime": "2025-11-14T10:00:00",
    "updatedTime": "2025-11-14T10:30:00"
  },
  "timestamp": 1734235200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

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
| 1001 | 数据验证失败 |
| 1004 | 操作失败 |
| 12001 | 数据库操作失败 |

