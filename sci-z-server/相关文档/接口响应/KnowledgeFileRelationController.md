# 知识库文件关联接口文档

## 接口基础信息

- **Controller**: `KnowledgeFileRelationController`
- **基础路径**: `/api/knowledge/file-relation`
- **描述**: 知识库文件关联管理相关接口

---

## 1. 创建知识库文件关联

### 接口信息

- **请求方式**: `POST`
- **接口地址**: `/api/knowledge/file-relation`
- **接口描述**: 创建新的知识库文件关联记录

### 请求参数

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
{
  "knowledgeId": 1,
  "folderId": 0,
  "attachmentId": 100,
  "fileName": "项目文档.pdf",
  "sortOrder": 0,
  "callback": "{\"document\":{\"id\":\"0453e46d-263a-46f3-8c8d-669981218921\",\"name\":\"项目文档.pdf\",\"indexing_status\":\"completed\"},\"batch\":\"batch-123\"}"
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| knowledgeId | Long | 是 | 知识库ID |
| folderId | Long | 否 | 文件夹ID，默认为0（根目录） |
| attachmentId | Long | 是 | 附件ID |
| fileName | String | 否 | 文件显示名称，最大255个字符 |
| sortOrder | Integer | 否 | 排序号，默认为0 |
| callback | String | 否 | 回调数据（Dify API返回的完整JSON数据） |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "knowledgeId": 1,
    "folderId": 0,
    "attachmentId": 100,
    "fileName": "项目文档.pdf",
    "sortOrder": 0,
    "callback": "{\"document\":{\"id\":\"0453e46d-263a-46f3-8c8d-669981218921\",\"name\":\"项目文档.pdf\",\"indexing_status\":\"completed\"},\"batch\":\"batch-123\"}",
    "createdTime": "2025-01-28T16:00:00",
    "updatedTime": "2025-01-28T16:00:00"
  },
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**失败响应：**
```json
{
  "code": 1002,
  "message": "数据不存在",
  "data": null,
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 2. 更新知识库文件关联

### 接口信息

- **请求方式**: `PUT`
- **接口地址**: `/api/knowledge/file-relation/{id}`
- **接口描述**: 更新知识库文件关联信息

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 关联ID |

**请求头：**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**
```json
{
  "folderId": 1,
  "fileName": "更新后的文件名.pdf",
  "sortOrder": 1
}
```

**参数说明：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| folderId | Long | 否 | 文件夹ID |
| fileName | String | 否 | 文件显示名称，最大255个字符 |
| sortOrder | Integer | 否 | 排序号 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "knowledgeId": 1,
    "folderId": 1,
    "attachmentId": 100,
    "fileName": "更新后的文件名.pdf",
    "sortOrder": 1,
    "callback": "{\"document\":{\"id\":\"0453e46d-263a-46f3-8c8d-669981218921\",\"name\":\"项目文档.pdf\",\"indexing_status\":\"completed\"},\"batch\":\"batch-123\"}",
    "createdTime": "2025-01-28T16:00:00",
    "updatedTime": "2025-01-28T16:30:00"
  },
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 3. 删除知识库文件关联

### 接口信息

- **请求方式**: `DELETE`
- **接口地址**: `/api/knowledge/file-relation/{id}`
- **接口描述**: 删除知识库文件关联记录（软删除）

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 关联ID |

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
  "data": null,
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**说明：** 此接口为软删除，不会真正删除数据，只是将 `is_deleted` 字段设置为1

---

## 4. 查询知识库文件关联详情

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/knowledge/file-relation/{id}`
- **接口描述**: 根据ID查询知识库文件关联详细信息

### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 关联ID |

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
  "data": {
    "id": 1,
    "knowledgeId": 1,
    "folderId": 0,
    "attachmentId": 100,
    "fileName": "项目文档.pdf",
    "sortOrder": 0,
    "callback": "{\"document\":{\"id\":\"0453e46d-263a-46f3-8c8d-669981218921\",\"name\":\"项目文档.pdf\",\"indexing_status\":\"completed\"},\"batch\":\"batch-123\"}",
    "createdTime": "2025-01-28T16:00:00",
    "updatedTime": "2025-01-28T16:00:00"
  },
  "timestamp": 1762496509666,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 5. 分页查询知识库文件关联列表

### 接口信息

- **请求方式**: `GET`
- **接口地址**: `/api/knowledge/file-relation`
- **接口描述**: 根据知识库ID分页查询文件关联列表

### 请求参数

**查询参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| knowledgeId | Long | 是 | 知识库ID |
| folderId | Long | 否 | 文件夹ID，如果指定则只查询该文件夹下的文件 |
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页大小，默认为10 |

**请求头：**
```
Authorization: Bearer {token}
```

**请求示例：**
```
GET /api/knowledge/file-relation?knowledgeId=1&folderId=0&page=1&size=10
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
        "knowledgeId": 1,
        "folderId": 0,
        "attachmentId": 100,
        "fileName": "项目文档.pdf",
        "sortOrder": 0,
        "callback": "{\"document\":{\"id\":\"0453e46d-263a-46f3-8c8d-669981218921\",\"name\":\"项目文档.pdf\",\"indexing_status\":\"completed\"},\"batch\":\"batch-123\"}",
        "createdTime": "2025-01-28T16:00:00",
        "updatedTime": "2025-01-28T16:00:00"
      },
      {
        "id": 2,
        "knowledgeId": 1,
        "folderId": 0,
        "attachmentId": 101,
        "fileName": "技术方案.docx",
        "sortOrder": 1,
        "callback": "{\"document\":{\"id\":\"0453e46d-263a-46f3-8c8d-669981218922\",\"name\":\"技术方案.docx\",\"indexing_status\":\"processing\"},\"batch\":\"batch-124\"}",
        "createdTime": "2025-01-28T15:00:00",
        "updatedTime": "2025-01-28T15:00:00"
      }
    ],
    "total": 2,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "timestamp": 1762496509666,
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
- 按 `sortOrder` 升序排列
- 相同排序号按 `createdTime` 倒序排列

---

## 统一响应格式

所有接口都遵循统一的响应格式：

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
| 1002 | 数据不存在 |
| 1004 | 操作失败 |
| 12001 | 数据库操作失败 |

