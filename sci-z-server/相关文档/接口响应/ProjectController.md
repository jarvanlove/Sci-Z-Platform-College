# 项目管理接口文档

## 目录

- [接口概述](#接口概述)
- [基础信息](#基础信息)
- [接口列表](#接口列表)
  - [1. 创建项目](#1-创建项目)
  - [2. 分页查询项目列表](#2-分页查询项目列表)
  - [3. 获取项目详情](#3-获取项目详情)
  - [4. 更新项目](#4-更新项目)
  - [5. 删除项目](#5-删除项目)
- [数据模型](#数据模型)
- [错误码说明](#错误码说明)
- [使用示例](#使用示例)

---

## 接口概述

项目管理接口提供了对科研项目的完整 CRUD 操作，支持项目的创建、查询、更新和删除。

**功能特性**：
- ✅ 创建项目（自动生成项目编号）
- ✅ 分页查询项目列表（支持关键字搜索、状态筛选）
- ✅ 获取项目详细信息
- ✅ 更新项目信息
- ✅ 删除项目（软删除）

---

## 基础信息

| 项目 | 说明 |
|------|------|
| **基础路径** | `/api/project` |
| **接口版本** | v1 |
| **认证方式** | 需要登录认证（Token） |
| **Content-Type** | `application/json` |
| **返回格式** | JSON |

---

## 接口列表

### 1. 创建项目

创建新的科研项目，系统会自动生成项目编号。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `POST` |
| **请求路径** | `/api/project` |
| **接口描述** | 创建新的科研项目 |

**请求参数**

```json
{
  "name": "智能医疗诊断系统",
  "description": "基于深度学习的医疗诊断系统，用于辅助医生进行疾病诊断",
  "declarationId": 1,
  "budget": 500000.00,
  "progress": 0,
  "status": "0",
  "difyKnowledgeId": "kb-123456"
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 项目名称 |
| description | String | 否 | 项目描述 |
| declarationId | Long | 否 | 关联申报ID |
| budget | BigDecimal | 否 | 项目预算（不能为负数） |
| progress | Integer | 否 | 进度百分比（0-100，默认0） |
| status | String | 否 | 项目状态（默认草稿，0=草稿） |
| difyKnowledgeId | String | 否 | Dify知识库ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1,
  "timestamp": 1737691200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**响应数据说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| data | Long | 创建的项目ID |

---

### 2. 分页查询项目列表

根据条件分页查询项目列表，支持关键字搜索、状态筛选和自定义排序。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `GET` |
| **请求路径** | `/api/project` |
| **接口描述** | 分页查询项目列表 |

**请求参数**

**查询参数（Query Parameters）：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 否 | 页码，从 1 开始，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| sortBy | String | 否 | 排序字段：`number`、`name`、`status`、`progress`、`budget`、`createdTime`、`updatedTime` 等 |
| sortOrder | String | 否 | 排序方式：`ASC`（升序）、`DESC`（降序），默认 `DESC` |
| keyword | String | 否 | 搜索关键字（项目编号/项目名称） |
| status | String | 否 | 项目状态（null表示全部） |

**请求示例**

```
GET /api/project?pageNo=1&pageSize=10&keyword=智能医疗&status=3&sortBy=createdTime&sortOrder=DESC
```

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "number": "PRJ20250124143025",
        "name": "智能医疗诊断系统",
        "description": "基于深度学习的医疗诊断系统",
        "declarationId": 1,
        "budget": 500000.00,
        "progress": 30,
        "status": "3",
        "difyKnowledgeId": "kb-123456",
        "createdTime": "2025-01-24T14:30:25",
        "updatedTime": "2025-01-24T14:30:25"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "timestamp": 1737691200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**响应数据说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| records | Array | 项目列表 |
| total | Long | 总记录数 |
| current | Long | 当前页码 |
| size | Long | 每页数量 |
| pages | Long | 总页数 |

**列表项字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 项目ID |
| number | String | 项目编号（自动生成，格式：PRJ + 时间戳） |
| name | String | 项目名称 |
| description | String | 项目描述 |
| declarationId | Long | 关联申报ID |
| budget | BigDecimal | 项目预算 |
| progress | Integer | 进度百分比（0-100） |
| status | String | 项目状态 |
| difyKnowledgeId | String | Dify知识库ID |
| createdTime | LocalDateTime | 创建时间 |
| updatedTime | LocalDateTime | 更新时间 |

---

### 3. 获取项目详情

根据 ID 获取项目详细信息。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `GET` |
| **请求路径** | `/api/project/{id}` |
| **接口描述** | 根据ID获取项目详细信息 |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 项目主键ID |

**请求示例**

```
GET /api/project/1
```

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "number": "PRJ20250124143025",
    "name": "智能医疗诊断系统",
    "description": "基于深度学习的医疗诊断系统，用于辅助医生进行疾病诊断",
    "declarationId": 1,
    "budget": 500000.00,
    "progress": 30,
    "status": "3",
    "difyKnowledgeId": "kb-123456",
    "createdBy": 100,
    "updatedBy": 100,
    "createdTime": "2025-01-24T14:30:25",
    "updatedTime": "2025-01-24T15:30:25"
  },
  "timestamp": 1737691200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**响应数据说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 项目ID |
| number | String | 项目编号 |
| name | String | 项目名称 |
| description | String | 项目描述 |
| declarationId | Long | 关联申报ID |
| budget | BigDecimal | 项目预算 |
| progress | Integer | 进度百分比 |
| status | String | 项目状态 |
| difyKnowledgeId | String | Dify知识库ID |
| createdBy | Long | 创建人ID |
| updatedBy | Long | 更新人ID |
| createdTime | LocalDateTime | 创建时间 |
| updatedTime | LocalDateTime | 更新时间 |

---

### 4. 更新项目

更新项目信息。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `PUT` |
| **请求路径** | `/api/project` |
| **接口描述** | 更新项目信息 |

**请求参数**

```json
{
  "id": 1,
  "name": "智能医疗诊断系统（更新）",
  "description": "更新后的项目描述",
  "declarationId": 1,
  "budget": 600000.00,
  "progress": 50,
  "status": "3",
  "difyKnowledgeId": "kb-123456"
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 项目ID（必填，用于定位要更新的记录） |
| name | String | 否 | 项目名称 |
| description | String | 否 | 项目描述 |
| declarationId | Long | 否 | 关联申报ID |
| budget | BigDecimal | 否 | 项目预算（不能为负数） |
| progress | Integer | 否 | 进度百分比（0-100） |
| status | String | 否 | 项目状态 |
| difyKnowledgeId | String | 否 | Dify知识库ID |

**注意**：所有字段均为可选，只更新提供的字段。`id`、`number`、`createdBy`、`createdTime` 等字段不可更新。

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1737691200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

### 5. 删除项目

根据 ID 删除项目（软删除）。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `DELETE` |
| **请求路径** | `/api/project/{id}` |
| **接口描述** | 根据ID删除项目（软删除） |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 项目主键ID |

**请求示例**

```
DELETE /api/project/1
```

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1737691200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

---

## 数据模型

### Project 实体

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 主键ID，自增 | 1 |
| number | String | 项目编号（自动生成） | `PRJ20250124143025` |
| name | String | 项目名称 | `智能医疗诊断系统` |
| description | String | 项目描述 | `基于深度学习的医疗诊断系统` |
| declarationId | Long | 关联申报ID | 1 |
| budget | BigDecimal | 项目预算 | `500000.00` |
| progress | Integer | 进度百分比（0-100） | 30 |
| status | String | 项目状态 | `0`、`1`、`2`、`3`、`4`、`5`、`6`、`7` |
| difyKnowledgeId | String | Dify知识库ID | `kb-123456` |
| createdBy | Long | 创建人ID | 100 |
| updatedBy | Long | 更新人ID | 100 |
| createdTime | LocalDateTime | 创建时间 | `2025-01-24T14:30:25` |
| updatedTime | LocalDateTime | 更新时间 | `2025-01-24T14:30:25` |
| isDeleted | Integer | 逻辑删除标识：`0`（未删除）、`1`（已删除） | 0 |

### 项目状态枚举（ProjectStatus）

| 值 | 说明 |
|----|------|
| `0` | 草稿 |
| `1` | 待审批 |
| `2` | 已审批 |
| `3` | 进行中 |
| `4` | 已完成 |
| `5` | 已暂停 |
| `6` | 已取消 |
| `7` | 已驳回 |

---

## 错误码说明

### Result 响应结构

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码：`200`（成功）、其他（失败） |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| timestamp | Long | 时间戳 |
| traceId | String | 追踪ID |

### 常见错误

| 状态码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 检查 Token 是否有效 |
| 404 | 资源不存在 | 检查 ID 是否正确 |
| 500 | 服务器内部错误 | 联系管理员 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 数据验证失败 |
| 1002 | 数据不存在 |
| 1201 | 数据库操作失败 |
| 3001 | 项目不存在 |
| 3002 | 项目已存在 |
| 3003 | 项目状态错误 |
| 3004 | 项目权限不足 |

---

## 使用示例

### 示例 1：创建项目

```bash
curl -X POST "http://localhost:8080/api/project" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "智能医疗诊断系统",
    "description": "基于深度学习的医疗诊断系统",
    "budget": 500000.00,
    "progress": 0,
    "status": "0"
  }'
```

### 示例 2：分页查询项目列表

```bash
curl -X GET "http://localhost:8080/api/project?pageNo=1&pageSize=10&keyword=智能医疗&status=3&sortBy=createdTime&sortOrder=DESC" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 示例 3：获取项目详情

```bash
curl -X GET "http://localhost:8080/api/project/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 示例 4：更新项目信息

```bash
curl -X PUT "http://localhost:8080/api/project" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "id": 1,
    "name": "智能医疗诊断系统（更新）",
    "progress": 50,
    "status": "3"
  }'
```

### 示例 5：删除项目

```bash
curl -X DELETE "http://localhost:8080/api/project/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### JavaScript/TypeScript 示例

```typescript
// 创建项目
async function createProject(data: {
  name: string;
  description?: string;
  declarationId?: number;
  budget?: number;
  progress?: number;
  status?: string;
  difyKnowledgeId?: string;
}) {
  const response = await fetch('/api/project', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data)
  });
  
  const result = await response.json();
  if (result.code === 200) {
    return result.data; // 返回项目ID
  }
  throw new Error(result.message);
}

// 分页查询项目列表
async function pageProject(params: {
  pageNo?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  keyword?: string;
  status?: string;
}) {
  const queryParams = new URLSearchParams();
  if (params.pageNo) queryParams.append('pageNo', params.pageNo.toString());
  if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
  if (params.sortBy) queryParams.append('sortBy', params.sortBy);
  if (params.sortOrder) queryParams.append('sortOrder', params.sortOrder);
  if (params.keyword) queryParams.append('keyword', params.keyword);
  if (params.status) queryParams.append('status', params.status);
  
  const response = await fetch(`/api/project?${queryParams.toString()}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  if (result.code === 200) {
    return result.data; // 返回分页结果
  }
  throw new Error(result.message);
}

// 获取项目详情
async function getProjectDetail(id: number) {
  const response = await fetch(`/api/project/${id}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  if (result.code === 200) {
    return result.data;
  }
  throw new Error(result.message);
}

// 更新项目
async function updateProject(data: {
  id: number;
  name?: string;
  description?: string;
  declarationId?: number;
  budget?: number;
  progress?: number;
  status?: string;
  difyKnowledgeId?: string;
}) {
  const response = await fetch('/api/project', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data)
  });
  
  const result = await response.json();
  if (result.code === 200) {
    return true;
  }
  throw new Error(result.message);
}

// 删除项目
async function deleteProject(id: number) {
  const response = await fetch(`/api/project/${id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  if (result.code === 200) {
    return true;
  }
  throw new Error(result.message);
}
```

### Vue 3 示例

```vue
<script setup lang="ts">
import { ref } from 'vue'
import request from '@/utils/request'

// 创建项目
const createProject = async (data: any) => {
  const response = await request({
    url: '/api/project',
    method: 'POST',
    data
  })
  return response.data
}

// 分页查询
const pageProjects = async (params: any) => {
  const response = await request({
    url: '/api/project',
    method: 'GET',
    params
  })
  return response.data
}

// 获取详情
const getProjectDetail = async (id: number) => {
  const response = await request({
    url: `/api/project/${id}`,
    method: 'GET'
  })
  return response.data
}

// 更新项目
const updateProject = async (data: any) => {
  const response = await request({
    url: '/api/project',
    method: 'PUT',
    data
  })
  return response
}

// 删除项目
const deleteProject = async (id: number) => {
  const response = await request({
    url: `/api/project/${id}`,
    method: 'DELETE'
  })
  return response
}
</script>
```

---

## 注意事项

1. **认证要求**：所有接口都需要在请求头中携带有效的 Token
2. **逻辑删除**：删除操作是软删除，不会真正删除数据库记录，只是将 `isDeleted` 字段设置为 `1`
3. **项目编号**：项目编号由系统自动生成，格式为 `PRJ + 时间戳（yyyyMMddHHmmss）`，例如：`PRJ20250124143025`
4. **项目状态**：`status` 字段使用枚举值：`0`（草稿）、`1`（待审批）、`2`（已审批）、`3`（进行中）、`4`（已完成）、`5`（已暂停）、`6`（已取消）、`7`（已驳回）
5. **分页查询**：分页查询支持多条件组合，未提供的条件不参与查询
6. **排序字段**：支持的排序字段包括：`number`、`name`、`status`、`progress`、`budget`、`createdTime`、`updatedTime`，默认按 `createdTime` 降序排列
7. **关键字搜索**：关键字搜索会在项目编号、项目名称中进行模糊匹配
8. **时间格式**：时间字段使用 ISO 8601 格式：`yyyy-MM-ddTHH:mm:ss`
9. **更新限制**：更新接口中，`id`、`number`、`createdBy`、`createdTime` 等字段不可更新
10. **预算和进度**：预算不能为负数，进度百分比必须在 0-100 之间

---

## 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2025-01-24 | v1.0 | 初始版本，提供完整的 CRUD 接口 |

---

**文档维护者**：JiaWen.Wu  
**最后更新**：2025-01-24

