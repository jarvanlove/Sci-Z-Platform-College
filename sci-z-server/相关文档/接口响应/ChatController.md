# AI对话控制器接口文档

## 接口基础信息

- **Controller**: `ChatController`
- **基础路径**: `/api/chat`
- **描述**: AI对话相关接口，包括工作流执行和流式对话

---

## 1. 执行 Dify 工作流或直接调用 Chatbot 流式对话

### 接口信息

- **请求方式**: `POST`
- **接口地址**: `/api/chat/workflow/run`
- **接口描述**: 支持两种模式：1. 不传文件时直接调用 chatbot 流式接口（可基于知识库或直接提问）；2. 传文件时执行工作流后再调用 chatbot
- **响应类型**: `text/event-stream` (SSE流式响应)

### 执行流程

**模式1：不传文件**
- 如果有 `knowledgeId`：调用 `/knowledge/chatbot/stream` 接口（基于知识库提问）
- 如果没有 `knowledgeId`：直接调用 chatbot 流式接口（不使用知识库）

**模式2：传文件**
1. 上传文件到 Dify，获取文件ID
2. 构建工作流 inputs，将文件ID填入
3. 执行工作流（阻塞模式）
4. 从工作流 `outputs.text` 中获取数据
5. 使用 outputs 数据调用 chatbot 流式接口
6. 流式返回给前端

### 请求参数

**请求头：**
```
Content-Type: multipart/form-data
Authorization: Bearer {token}
Accept: text/event-stream
```

**表单参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| query | String | 是 | 用户问题 |
| knowledgeId | String | 否 | 知识库ID（可选，不传则不使用知识库；传文件时必须提供） |
| workflowId | String | 否 | 工作流ID（可选，仅在传文件时使用） |
| files | MultipartFile[] | 否 | 上传的文件列表（可选，不传则直接调用 chatbot） |
| conversationId | String | 否 | 会话ID（可选，用于 chatbot 流式对话） |
| user | String | 否 | 用户标识（可选，默认使用当前登录用户ID） |

**请求示例1：不传文件，基于知识库提问**
```bash
POST /api/chat/workflow/run
Content-Type: multipart/form-data

query=什么是项目管理？
knowledgeId=knowledge_base_001
conversationId=conversation-001
```

**请求示例2：不传文件，不使用知识库**
```bash
POST /api/chat/workflow/run
Content-Type: multipart/form-data

query=你好，介绍一下你自己
conversationId=conversation-001
```

**请求示例3：传文件，执行工作流**
```bash
POST /api/chat/workflow/run
Content-Type: multipart/form-data

query=请分析这个文档
knowledgeId=knowledge_base_001
workflowId=workflow-id
files=file1.pdf
files=file2.docx
conversationId=conversation-001
```

**cURL 示例1：不传文件，基于知识库**
```bash
curl -X POST "http://localhost:8808/api/chat/workflow/run" \
  -H "Authorization: Bearer {token}" \
  -H "Accept: text/event-stream" \
  -F "query=什么是项目管理？" \
  -F "knowledgeId=knowledge_base_001" \
  -F "conversationId=conversation-001"
```

**cURL 示例2：不传文件，不使用知识库**
```bash
curl -X POST "http://localhost:8808/api/chat/workflow/run" \
  -H "Authorization: Bearer {token}" \
  -H "Accept: text/event-stream" \
  -F "query=你好，介绍一下你自己" \
  -F "conversationId=conversation-001"
```

**cURL 示例3：传文件，执行工作流**
```bash
curl -X POST "http://localhost:8808/api/chat/workflow/run" \
  -H "Authorization: Bearer {token}" \
  -H "Accept: text/event-stream" \
  -F "query=请分析这个文档" \
  -F "knowledgeId=knowledge_base_001" \
  -F "workflowId=workflow-id" \
  -F "files=@file1.pdf" \
  -F "files=@file2.docx" \
  -F "conversationId=conversation-001"
```

### 工作流请求格式

接口内部会将文件ID构建成以下格式的工作流请求：

```json
{
  "inputs": {
    "file": [
      {
        "type": "document",
        "transfer_method": "local_file",
        "upload_file_id": "0c3b29df-def6-4850-aa68-693fc55e5413"
      },
      {
        "type": "document",
        "transfer_method": "local_file",
        "upload_file_id": "1d4c30ef-ef7-5961-bc79-7a04g66b6524"
      }
    ]
  },
  "response_mode": "blocking",
  "user": "user-001"
}
```

### 工作流响应格式

工作流执行成功后，会返回以下格式的响应：

```json
{
  "task_id": "2e790183-22a2-4af8-a732-78fb3a86dbcb",
  "workflow_run_id": "eaba515c-2628-4408-ba33-cfdc9c941271",
  "data": {
    "id": "eaba515c-2628-4408-ba33-cfdc9c941271",
    "workflow_id": "c0f06d07-fde1-46cc-9cb1-c444d0a5852a",
    "status": "succeeded",
    "outputs": {
      "text": [
        "工作流处理后的文本内容..."
      ]
    },
    "error": "",
    "elapsed_time": 0.255612,
    "total_tokens": 0,
    "total_steps": 3,
    "created_at": 1763444002,
    "finished_at": 1763444003
  }
}
```

接口会从 `data.outputs.text[0]` 中提取文本内容，作为 chatbot 的 query 参数。

### 流式响应格式

接口使用 SSE（Server-Sent Events）格式返回流式数据：

**成功响应示例：**
```
event: message
data: {"event": "message", "answer": "这是", "conversation_id": "conv-123", "message_id": "msg-456"}

event: message
data: {"event": "message", "answer": "AI", "conversation_id": "conv-123", "message_id": "msg-456"}

event: message
data: {"event": "message", "answer": "的回复", "conversation_id": "conv-123", "message_id": "msg-456"}

event: message_end
data: {}
```

**错误响应示例：**
```
event: error
data: {"error": true, "message": "文件上传失败: ..."}
```

### 响应事件说明

| 事件类型 | 说明 |
|----------|------|
| `message` | chatbot 流式消息片段 |
| `message_end` | 消息流结束 |
| `error` | 错误信息 |

### 前端处理示例

**JavaScript 示例1：不传文件，基于知识库**
```javascript
const formData = new FormData();
formData.append('query', '什么是项目管理？');
formData.append('knowledgeId', 'knowledge_base_001');
formData.append('conversationId', 'conversation-001');

const response = await fetch('/api/chat/workflow/run', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const reader = response.body.getReader();
const decoder = new TextDecoder();

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  
  const chunk = decoder.decode(value);
  const lines = chunk.split('\n');
  
  for (const line of lines) {
    if (line.startsWith('event: message')) {
      // 处理消息事件
    } else if (line.startsWith('data: ')) {
      const data = JSON.parse(line.substring(6));
      if (data.answer) {
        console.log('收到消息片段:', data.answer);
        // 更新UI显示消息
      }
    } else if (line.startsWith('event: message_end')) {
      console.log('消息流结束');
    } else if (line.startsWith('event: error')) {
      const error = JSON.parse(line.substring(6));
      console.error('错误:', error.message);
    }
  }
}
```

**JavaScript 示例2：不传文件，不使用知识库**
```javascript
const formData = new FormData();
formData.append('query', '你好，介绍一下你自己');
formData.append('conversationId', 'conversation-001');

// ... 同上处理流式响应
```

**Vue.js 示例：传文件，执行工作流**
```javascript
async function runWorkflow(query, files, knowledgeId, workflowId) {
  const formData = new FormData();
  formData.append('query', query); // 必填
  if (knowledgeId) {
    formData.append('knowledgeId', knowledgeId);
  }
  if (workflowId) {
    formData.append('workflowId', workflowId);
  }
  if (files && files.length > 0) {
    files.forEach(file => {
      formData.append('files', file);
    });
  }
  formData.append('conversationId', 'conversation-001');
  
  const response = await fetch('/api/chat/workflow/run', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    
    const chunk = decoder.decode(value);
    const lines = chunk.split('\n');
    
    for (const line of lines) {
      if (line.startsWith('data: ')) {
        const data = JSON.parse(line.substring(6));
        if (data.answer) {
          // 更新UI显示消息
          messageContent.value += data.answer;
        }
      }
    }
  }
}
```

### 错误处理

**常见错误码：**
| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误（如 query 为空） |
| 400 | 上传文件时必须提供知识库ID |
| 400 | 请先创建 Chatbot 应用 |
| 500 | 文件上传失败 |
| 500 | 工作流执行失败 |
| 500 | 工作流响应格式错误 |
| 500 | 工作流输出文本为空 |

**错误响应示例：**
```
event: error
data: {"error": true, "message": "上传文件时必须提供知识库ID"}
```

---

## 统一响应格式

### 流式响应

流式接口使用 SSE 格式，每个事件包含：
- `event`: 事件类型（message、message_end、error）
- `data`: 事件数据（JSON 字符串）

### 错误处理

所有错误都会通过 `error` 事件返回，格式：
```json
{
  "error": true,
  "message": "错误描述信息"
}
```

---

## 注意事项

1. **参数要求**：
   - `query` 参数必填，是用户的问题
   - `knowledgeId` 可选，不传则不使用知识库
   - `files` 可选，不传则直接调用 chatbot

2. **执行模式**：
   - **不传文件**：直接调用 chatbot 流式接口，功能等同于 `/knowledge/chatbot/stream`
   - **传文件**：必须先上传文件，然后执行工作流，最后调用 chatbot

3. **文件上传限制**（仅在传文件时）：
   - 传文件时必须提供 `knowledgeId`（用于上传文件）
   - 支持单文件或多文件上传
   - 文件格式需要符合 Dify 的要求
   - 文件大小受 Dify 配置限制

4. **工作流要求**（仅在传文件时）：
   - 工作流必须返回 `outputs.text` 数组
   - 接口会使用 `outputs.text[0]` 作为 chatbot 的 query
   - 工作流输出的文本会覆盖请求中的 `query` 参数

5. **Chatbot 要求**：
   - 用户必须先创建 Chatbot 应用
   - 系统会自动查找用户的第一个 Chatbot 应用

6. **流式响应**：
   - 响应超时时间设置为 60 秒
   - 前端需要正确处理 SSE 流式数据
   - 建议使用 `EventSource` 或 `fetch` + `ReadableStream` 处理

7. **会话管理**：
   - 如果提供了 `conversationId`，chatbot 会继续该会话
   - 如果不提供，chatbot 会创建新会话

8. **知识库使用**：
   - 不传 `knowledgeId` 时，chatbot 不使用知识库，直接回答问题
   - 传 `knowledgeId` 时，chatbot 会基于知识库内容回答问题

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功（流式响应中通过事件返回） |
| 400 | 请求参数错误 |
| 400 | 请先创建 Chatbot 应用 |
| 500 | 服务器内部错误 |

