# 报告管理接口文档

## 目录

- [接口概述](#接口概述)
- [基础信息](#基础信息)
- [接口列表](#接口列表)
  - [1. 创建报告管理](#1-创建报告管理)
  - [2. 分页查询报告列表](#2-分页查询报告列表)
  - [3. 获取报告详情](#3-获取报告详情)
  - [4. 更新报告管理](#4-更新报告管理)
  - [5. 删除报告管理](#5-删除报告管理)
- [数据模型](#数据模型)
- [错误码说明](#错误码说明)
- [使用示例](#使用示例)

---

## 接口概述

报告管理接口提供了对项目报告的完整 CRUD 操作，支持技术报告和自评报告的管理。

**功能特性**：
- ✅ 创建报告管理记录
- ✅ 分页查询报告列表（支持关键字搜索、状态筛选、类型筛选）
- ✅ 获取报告详细信息
- ✅ 更新报告信息
- ✅ 删除报告（软删除）

---

## 基础信息

| 项目 | 说明 |
|------|------|
| **基础路径** | `/api/report-management` |
| **接口版本** | v1 |
| **认证方式** | 需要登录认证（Token） |
| **Content-Type** | `application/json` |
| **返回格式** | JSON |

---

## 接口列表

### 1. 创建报告管理

创建新的报告管理记录，系统会自动生成报告编号。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `POST` |
| **请求路径** | `/api/report-management` |
| **接口描述** | 创建新的报告管理记录 |

**请求参数**

```json
{
  "projectId": 1,
  "projectName": "智能医疗诊断系统",
  "projectCode": "PRJ202501001",
  "projectKnowledgeId": "kb-123456",
  "reportType": "tech",
  "summary": "项目技术报告摘要"
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| projectId | Long | 是 | 项目ID |
| projectName | String | 是 | 项目名称 |
| projectCode | String | 否 | 项目编号 |
| projectKnowledgeId | String | 否 | 项目知识库ID |
| reportType | String | 是 | 报告类型：`tech`（技术报告）、`self`（自评报告） |
| summary | String | 否 | 报告摘要 |

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
| data | Long | 创建的报告ID |

---

### 2. 分页查询报告列表

根据条件分页查询报告列表，支持关键字搜索、状态筛选、类型筛选和自定义排序。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `GET` |
| **请求路径** | `/api/report-management` |
| **接口描述** | 分页查询报告列表 |

**请求参数**

**查询参数（Query Parameters）：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 否 | 页码，从 1 开始，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| sortBy | String | 否 | 排序字段：`number`、`projectName`、`generateTime`、`createdTime` 等 |
| sortOrder | String | 否 | 排序方式：`ASC`（升序）、`DESC`（降序），默认 `DESC` |
| keyword | String | 否 | 搜索关键字（报告编号/项目名称/创建人） |
| status | String | 否 | 报告状态（null表示全部） |
| reportType | String | 否 | 报告类型：`tech`（技术报告）、`self`（自评报告），null表示全部 |

**请求示例**

```
GET /api/report-management?pageNo=1&pageSize=10&keyword=智能医疗&status=pending&reportType=tech&sortBy=generateTime&sortOrder=DESC
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
        "number": "RPT20250124143025",
        "projectId": 1,
        "projectName": "智能医疗诊断系统",
        "projectCode": "PRJ202501001",
        "reportType": "tech",
        "creatorId": 100,
        "creatorName": "张三",
        "summary": "项目技术报告摘要",
        "status": "pending",
        "generateTime": null,
        "createdTime": "2025-01-24T14:30:25"
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
| records | Array | 报告列表 |
| total | Long | 总记录数 |
| current | Long | 当前页码 |
| size | Long | 每页数量 |
| pages | Long | 总页数 |

**列表项字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 报告ID |
| number | String | 报告编号（自动生成，格式：RPT + 时间戳） |
| projectId | Long | 项目ID |
| projectName | String | 项目名称 |
| projectCode | String | 项目编号 |
| reportType | String | 报告类型：`tech`（技术报告）、`self`（自评报告） |
| creatorId | Long | 创建人ID |
| creatorName | String | 创建人姓名 |
| summary | String | 报告摘要 |
| status | String | 状态：`pending`（待生成）、`generating`（生成中）、`completed`（已完成）、`failed`（失败） |
| generateTime | LocalDateTime | 生成完成时间（null表示未生成） |
| createdTime | LocalDateTime | 创建时间 |

---

### 3. 获取报告详情

根据 ID 获取报告详细信息。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `GET` |
| **请求路径** | `/api/report-management/{id}` |
| **接口描述** | 根据ID获取报告详细信息 |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 报告主键ID |

**请求示例**

```
GET /api/report-management/1
```

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "number": "RPT20250124143025",
    "projectId": 1,
    "projectName": "智能医疗诊断系统",
    "projectCode": "PRJ202501001",
    "projectKnowledgeId": "kb-123456",
    "reportType": "tech",
    "creatorId": 100,
    "creatorName": "张三",
    "summary": "项目技术报告摘要",
    "status": "pending",
    "generateTime": null,
    "createdTime": "2025-01-24T14:30:25",
    "updatedTime": "2025-01-24T14:30:25"
  },
  "timestamp": 1737691200000,
  "traceId": "5d0b8880becc47f5870b57aa3cef3a0c"
}
```

**响应数据说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 报告ID |
| number | String | 报告编号 |
| projectId | Long | 项目ID |
| projectName | String | 项目名称 |
| projectCode | String | 项目编号 |
| projectKnowledgeId | String | 项目知识库ID |
| reportType | String | 报告类型：`tech`（技术报告）、`self`（自评报告） |
| creatorId | Long | 创建人ID |
| creatorName | String | 创建人姓名 |
| summary | String | 报告摘要 |
| status | String | 状态 |
| generateTime | LocalDateTime | 生成完成时间 |
| createdTime | LocalDateTime | 创建时间 |
| updatedTime | LocalDateTime | 更新时间 |

---

### 4. 更新报告管理

更新报告管理信息。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `PUT` |
| **请求路径** | `/api/report-management` |
| **接口描述** | 更新报告管理信息 |

**请求参数**

```json
{
  "id": 1,
  "projectName": "智能医疗诊断系统（更新）",
  "projectCode": "PRJ202501001",
  "projectKnowledgeId": "kb-123456",
  "reportType": "tech",
  "summary": "更新后的报告摘要",
  "status": "completed"
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 报告ID（必填，用于定位要更新的记录） |
| projectName | String | 否 | 项目名称 |
| projectCode | String | 否 | 项目编号 |
| projectKnowledgeId | String | 否 | 项目知识库ID |
| reportType | String | 否 | 报告类型：`tech`（技术报告）、`self`（自评报告） |
| summary | String | 否 | 报告摘要 |
| status | String | 否 | 状态：`pending`（待生成）、`generating`（生成中）、`completed`（已完成）、`failed`（失败） |

**注意**：所有字段均为可选，只更新提供的字段。`id`、`number`、`projectId`、`creatorId`、`creatorName`、`generateTime` 等字段不可更新。

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

### 5. 删除报告管理

根据 ID 删除报告管理（软删除）。

**接口信息**

| 项目 | 说明 |
|------|------|
| **请求方式** | `DELETE` |
| **请求路径** | `/api/report-management/{id}` |
| **接口描述** | 根据ID删除报告管理（软删除） |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 报告主键ID |

**请求示例**

```
DELETE /api/report-management/1
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

### ReportManagement 实体

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 主键ID，自增 | 1 |
| number | String | 报告编号（自动生成） | `RPT20250124143025` |
| projectId | Long | 项目ID | 1 |
| projectName | String | 项目名称 | `智能医疗诊断系统` |
| projectCode | String | 项目编号 | `PRJ202501001` |
| projectKnowledgeId | String | 项目知识库ID | `kb-123456` |
| reportType | String | 报告类型 | `tech`、`self` |
| creatorId | Long | 创建人ID | 100 |
| creatorName | String | 创建人姓名 | `张三` |
| summary | String | 报告摘要 | `项目技术报告摘要` |
| status | String | 状态 | `pending`、`generating`、`completed`、`failed` |
| generateTime | LocalDateTime | 生成完成时间 | `2025-01-24T15:30:25` |
| createdTime | LocalDateTime | 创建时间 | `2025-01-24T14:30:25` |
| updatedTime | LocalDateTime | 更新时间 | `2025-01-24T14:30:25` |
| isDeleted | Integer | 逻辑删除标识：`0`（未删除）、`1`（已删除） | 0 |

### 报告类型枚举（ReportType）

| 值 | 说明 |
|----|------|
| `tech` | 技术报告 |
| `self` | 自评报告 |

### 报告状态枚举（Status）

| 值 | 说明 |
|----|------|
| `pending` | 待生成 |
| `generating` | 生成中 |
| `completed` | 已完成 |
| `failed` | 失败 |

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

---

## 使用示例

### 示例 1：创建技术报告

```bash
curl -X POST "http://localhost:8080/api/report-management" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "projectId": 1,
    "projectName": "智能医疗诊断系统",
    "projectCode": "PRJ202501001",
    "projectKnowledgeId": "kb-123456",
    "reportType": "tech",
    "summary": "项目技术报告摘要"
  }'
```

### 示例 2：分页查询报告列表

```bash
curl -X GET "http://localhost:8080/api/report-management?pageNo=1&pageSize=10&keyword=智能医疗&status=pending&reportType=tech&sortBy=generateTime&sortOrder=DESC" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 示例 3：获取报告详情

```bash
curl -X GET "http://localhost:8080/api/report-management/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 示例 4：更新报告信息

```bash
curl -X PUT "http://localhost:8080/api/report-management" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "id": 1,
    "summary": "更新后的报告摘要",
    "status": "completed"
  }'
```

### 示例 5：删除报告

```bash
curl -X DELETE "http://localhost:8080/api/report-management/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### JavaScript/TypeScript 示例

```typescript
// 创建报告
async function createReportManagement(data: {
  projectId: number;
  projectName: string;
  projectCode?: string;
  projectKnowledgeId?: string;
  reportType: 'tech' | 'self';
  summary?: string;
}) {
  const response = await fetch('/api/report-management', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data)
  });
  
  const result = await response.json();
  if (result.code === 200) {
    return result.data; // 返回报告ID
  }
  throw new Error(result.message);
}

// 分页查询报告列表
async function pageReportManagement(params: {
  pageNo?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  keyword?: string;
  status?: string;
  reportType?: 'tech' | 'self';
}) {
  const queryParams = new URLSearchParams();
  if (params.pageNo) queryParams.append('pageNo', params.pageNo.toString());
  if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
  if (params.sortBy) queryParams.append('sortBy', params.sortBy);
  if (params.sortOrder) queryParams.append('sortOrder', params.sortOrder);
  if (params.keyword) queryParams.append('keyword', params.keyword);
  if (params.status) queryParams.append('status', params.status);
  if (params.reportType) queryParams.append('reportType', params.reportType);
  
  const response = await fetch(`/api/report-management?${queryParams.toString()}`, {
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

// 获取报告详情
async function getReportManagementDetail(id: number) {
  const response = await fetch(`/api/report-management/${id}`, {
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

// 更新报告
async function updateReportManagement(data: {
  id: number;
  projectName?: string;
  projectCode?: string;
  projectKnowledgeId?: string;
  reportType?: 'tech' | 'self';
  summary?: string;
  status?: string;
}) {
  const response = await fetch('/api/report-management', {
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

// 删除报告
async function deleteReportManagement(id: number) {
  const response = await fetch(`/api/report-management/${id}`, {
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

// 创建报告
const createReport = async (data: any) => {
  const response = await request({
    url: '/api/report-management',
    method: 'POST',
    data
  })
  return response.data
}

// 分页查询
const pageReports = async (params: any) => {
  const response = await request({
    url: '/api/report-management',
    method: 'GET',
    params
  })
  return response.data
}

// 获取详情
const getReportDetail = async (id: number) => {
  const response = await request({
    url: `/api/report-management/${id}`,
    method: 'GET'
  })
  return response.data
}

// 更新报告
const updateReport = async (data: any) => {
  const response = await request({
    url: '/api/report-management',
    method: 'PUT',
    data
  })
  return response
}

// 删除报告
const deleteReport = async (id: number) => {
  const response = await request({
    url: `/api/report-management/${id}`,
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
3. **报告编号**：报告编号由系统自动生成，格式为 `RPT + 时间戳（yyyyMMddHHmmss）`，例如：`RPT20250124143025`
4. **报告类型**：`reportType` 字段必须使用枚举值：`tech`（技术报告）、`self`（自评报告）
5. **状态管理**：报告状态包括：`pending`（待生成）、`generating`（生成中）、`completed`（已完成）、`failed`（失败）
6. **分页查询**：分页查询支持多条件组合，未提供的条件不参与查询
7. **排序字段**：支持的排序字段包括：`number`、`projectName`、`generateTime`、`createdTime`、`updatedTime`，默认按 `generateTime` 降序排列
8. **关键字搜索**：关键字搜索会在报告编号、项目名称、创建人姓名中进行模糊匹配
9. **时间格式**：时间字段使用 ISO 8601 格式：`yyyy-MM-ddTHH:mm:ss`
10. **更新限制**：更新接口中，`id`、`number`、`projectId`、`creatorId`、`creatorName`、`generateTime` 等字段不可更新

---

## 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2025-01-24 | v1.0 | 初始版本，提供完整的 CRUD 接口 |

---

**文档维护者**：JiaWen.Wu  
**最后更新**：2025-01-24

