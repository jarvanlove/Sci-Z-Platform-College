# Dify API 密钥管理接口文档

## 目录

- [接口概述](#接口概述)
- [基础信息](#基础信息)
- [接口列表](#接口列表)
  - [1. 获取密钥列表](#1-获取密钥列表)
  - [2. 新增密钥](#2-新增密钥)
  - [3. 更新密钥](#3-更新密钥)
  - [4. 删除密钥](#4-删除密钥)
  - [5. 获取密钥详情](#5-获取密钥详情)
- [数据模型](#数据模型)
- [错误码说明](#错误码说明)
- [使用示例](#使用示例)

---

## 接口概述

Dify API 密钥管理接口提供了对 Dify 平台 API 密钥的完整 CRUD 操作，支持知识库密钥、工作流密钥和文件密钥的管理。

**功能特性**：
- ✅ 条件查询密钥列表
- ✅ 新增 API 密钥
- ✅ 更新密钥信息
- ✅ 删除密钥（逻辑删除）
- ✅ 根据 ID 获取密钥详情

---

## 基础信息

| 项目 | 说明 |
|------|------|
| **基础路径** | `/api/dify/keys` |
| **接口版本** | v1 |
| **认证方式** | 需要登录认证（Token） |
| **Content-Type** | `application/json` |
| **返回格式** | JSON |

---

## 接口列表

### 1. 获取密钥列表

根据条件查询密钥列表，支持多字段组合查询。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `POST` |
| **请求路径** | `/api/dify/keys/list` |
| **接口描述** | 根据条件查询密钥列表 |

**请求参数**

请求体为 `DifyApiKey` 对象，所有字段均为可选，用于条件查询：

```json
{
  "userId": 1,
  "keyType": "dataset",
  "resourceId": "knowledge-base-123",
  "keyName": "我的密钥",
  "isActive": true
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 否 | 用户ID，精确匹配 |
| keyType | String | 否 | 密钥类型：`dataset`（知识库）、`workflow`（工作流）、`file`（文件） |
| resourceId | String | 否 | 资源ID（知识库ID或工作流ID），精确匹配 |
| keyName | String | 否 | 密钥名称，模糊匹配 |
| description | String | 否 | 密钥描述，模糊匹配 |
| isActive | Boolean | 否 | 是否激活：`true`（激活）、`false`（禁用） |
| id | Long | 否 | 主键ID |
| createdTime | String | 否 | 创建时间 |
| updatedTime | String | 否 | 更新时间 |

**响应示例**

```json
{
  "flag": true,
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "keyType": "dataset",
      "resourceId": "knowledge-base-123",
      "apiKey": "app-xxxxxxxxxxxxx",
      "keyName": "知识库密钥",
      "description": "用于访问知识库的API密钥",
      "isActive": true,
      "createdTime": "2025-01-28T12:30:00",
      "updatedTime": "2025-01-28T12:30:00",
      "createdBy": "admin",
      "updatedBy": "admin",
      "deleted": 0
    }
  ]
}
```

---

### 2. 新增密钥

创建新的 API 密钥。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `POST` |
| **请求路径** | `/api/dify/keys/save` |
| **接口描述** | 新增 API 密钥 |

**请求参数**

```json
{
  "userId": 1,
  "keyType": "dataset",
  "resourceId": "knowledge-base-123",
  "apiKey": "app-xxxxxxxxxxxxx",
  "keyName": "知识库密钥",
  "description": "用于访问知识库的API密钥",
  "isActive": true
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| keyType | String | 是 | 密钥类型：`dataset`、`workflow`、`file` |
| resourceId | String | 是 | 资源ID（知识库ID或工作流ID） |
| apiKey | String | 是 | API密钥值 |
| keyName | String | 否 | 密钥名称 |
| description | String | 否 | 密钥描述 |
| isActive | Boolean | 否 | 是否激活，默认 `true` |

**响应示例**

```json
{
  "flag": true,
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

### 3. 更新密钥

根据 ID 更新密钥信息。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `PUT` |
| **请求路径** | `/api/dify/keys/update` |
| **接口描述** | 根据ID更新密钥信息 |

**请求参数**

```json
{
  "id": 1,
  "keyName": "更新后的密钥名称",
  "description": "更新后的描述",
  "isActive": false
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 主键ID（必填，用于定位要更新的记录） |
| keyName | String | 否 | 密钥名称 |
| description | String | 否 | 密钥描述 |
| isActive | Boolean | 否 | 是否激活 |
| apiKey | String | 否 | API密钥值 |
| userId | Long | 否 | 用户ID |
| keyType | String | 否 | 密钥类型 |
| resourceId | String | 否 | 资源ID |

**响应示例**

```json
{
  "flag": true,
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

### 4. 删除密钥

根据 ID 删除密钥（逻辑删除）。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `DELETE` |
| **请求路径** | `/api/dify/keys/delete/{id}` |
| **接口描述** | 根据ID删除密钥（逻辑删除） |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 密钥主键ID |

**请求示例**

```
DELETE /api/dify/keys/delete/1
```

**响应示例**

```json
{
  "flag": true,
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

### 5. 获取密钥详情

根据 ID 获取密钥详细信息。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `GET` |
| **请求路径** | `/api/dify/keys/get/{id}` |
| **接口描述** | 根据ID获取密钥详情 |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 密钥主键ID |

**请求示例**

```
GET /api/dify/keys/get/1
```

**响应示例**

```json
{
  "flag": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "keyType": "dataset",
    "resourceId": "knowledge-base-123",
    "apiKey": "app-xxxxxxxxxxxxx",
    "keyName": "知识库密钥",
    "description": "用于访问知识库的API密钥",
    "isActive": true,
    "createdTime": "2025-01-28T12:30:00",
    "updatedTime": "2025-01-28T12:30:00",
    "createdBy": "admin",
    "updatedBy": "admin",
    "deleted": 0
  }
}
```

---

## 数据模型

### DifyApiKey 实体

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 主键ID，自增 | 1 |
| userId | Long | 用户ID | 1 |
| keyType | String | 密钥类型 | `dataset`、`workflow`、`file` |
| resourceId | String | 资源ID（知识库ID或工作流ID） | `knowledge-base-123` |
| apiKey | String | API密钥值 | `app-xxxxxxxxxxxxx` |
| keyName | String | 密钥名称 | `知识库密钥` |
| description | String | 密钥描述 | `用于访问知识库的API密钥` |
| isActive | Boolean | 是否激活 | `true`、`false` |
| createdTime | LocalDateTime | 创建时间 | `2025-01-28T12:30:00` |
| updatedTime | LocalDateTime | 更新时间 | `2025-01-28T12:30:00` |
| createdBy | String | 创建人 | `admin` |
| updatedBy | String | 更新人 | `admin` |
| deleted | Integer | 逻辑删除标识：`0`（未删除）、`1`（已删除） | 0 |

### 密钥类型枚举（KeyType）

| 值 | 说明 |
|----|------|
| `dataset` | 知识库密钥 |
| `workflow` | 工作流密钥 |
| `file` | 文件密钥 |

---

## 错误码说明

### ResultVO 响应结构

| 字段 | 类型 | 说明 |
|------|------|------|
| flag | Boolean | 操作是否成功：`true`（成功）、`false`（失败） |
| code | Integer | 状态码：`200`（成功）、其他（失败） |
| message | String | 响应消息 |
| data | Object | 响应数据 |

### 常见错误

| 状态码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 检查 Token 是否有效 |
| 404 | 资源不存在 | 检查 ID 是否正确 |
| 500 | 服务器内部错误 | 联系管理员 |

---

## 使用示例

### 示例 1：创建知识库密钥

```bash
curl -X POST "http://localhost:8080/api/dify/keys/save" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": 1,
    "keyType": "dataset",
    "resourceId": "knowledge-base-123",
    "apiKey": "app-xxxxxxxxxxxxx",
    "keyName": "知识库密钥",
    "description": "用于访问知识库的API密钥",
    "isActive": true
  }'
```

### 示例 2：查询指定用户的所有激活密钥

```bash
curl -X POST "http://localhost:8080/api/dify/keys/list" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": 1,
    "isActive": true
  }'
```

### 示例 3：更新密钥状态

```bash
curl -X PUT "http://localhost:8080/api/dify/keys/update" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "id": 1,
    "isActive": false
  }'
```

### 示例 4：获取密钥详情

```bash
curl -X GET "http://localhost:8080/api/dify/keys/get/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 示例 5：删除密钥

```bash
curl -X DELETE "http://localhost:8080/api/dify/keys/delete/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### JavaScript/TypeScript 示例

```typescript
// 创建密钥
async function createApiKey() {
  const response = await fetch('/api/dify/keys/save', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      userId: 1,
      keyType: 'dataset',
      resourceId: 'knowledge-base-123',
      apiKey: 'app-xxxxxxxxxxxxx',
      keyName: '知识库密钥',
      description: '用于访问知识库的API密钥',
      isActive: true
    })
  });
  
  const result = await response.json();
  if (result.flag) {
    console.log('创建成功');
  }
}

// 查询密钥列表
async function listApiKeys(userId: number) {
  const response = await fetch('/api/dify/keys/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      userId: userId,
      isActive: true
    })
  });
  
  const result = await response.json();
  if (result.flag) {
    return result.data;
  }
  return [];
}

// 更新密钥
async function updateApiKey(id: number, updates: Partial<DifyApiKey>) {
  const response = await fetch('/api/dify/keys/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      id: id,
      ...updates
    })
  });
  
  const result = await response.json();
  return result.flag;
}

// 删除密钥
async function deleteApiKey(id: number) {
  const response = await fetch(`/api/dify/keys/delete/${id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  return result.flag;
}

// 获取密钥详情
async function getApiKey(id: number) {
  const response = await fetch(`/api/dify/keys/get/${id}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  if (result.flag) {
    return result.data;
  }
  return null;
}
```

---

## 注意事项

1. **认证要求**：所有接口都需要在请求头中携带有效的 Token
2. **逻辑删除**：删除操作是逻辑删除，不会真正删除数据库记录，只是将 `deleted` 字段设置为 `1`
3. **密钥类型**：`keyType` 字段必须使用枚举值：`dataset`、`workflow`、`file`
4. **资源ID**：`resourceId` 必须与 `keyType` 对应（知识库ID对应 `dataset`，工作流ID对应 `workflow`）
5. **查询条件**：列表查询接口支持多字段组合查询，未提供的字段不参与查询条件
6. **时间格式**：时间字段使用 ISO 8601 格式：`yyyy-MM-ddTHH:mm:ss`

---

## 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2025-01-28 | v1.0 | 初始版本，提供完整的 CRUD 接口 |

---

**文档维护者**：JiaWen.Wu  
**最后更新**：2025-01-28

