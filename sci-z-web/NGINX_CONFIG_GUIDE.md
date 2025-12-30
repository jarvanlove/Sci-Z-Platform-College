# Nginx 流式输出配置指南

## 你的配置问题

你的原始配置缺少流式输出支持，导致 AI 对话等待全部接收后才响应。

## 解决方案

### 方法一：完整替换配置（推荐）

将你的 Nginx 配置替换为 `nginx-user-config.conf` 中的配置。

### 方法二：最小修改（只添加流式接口配置）

在你的配置文件中，在 `location /api/` 之前添加以下配置：

```nginx
# 🔥 专门为流式接口配置（必须在 /api/ 之前，优先级更高）
location ~ ^/api/(chat/workflow/run|knowledge/chatbot/stream) {
    proxy_pass http://127.0.0.1:8808;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # 🔥 强制禁用所有缓冲和缓存（关键配置）
    proxy_buffering off;
    proxy_cache off;
    proxy_request_buffering off;
    proxy_no_cache 1;
    proxy_cache_bypass 1;
    
    # 禁用 gzip 压缩
    gzip off;
    
    # 设置流式响应头
    proxy_set_header Accept 'text/event-stream';
    add_header Cache-Control 'no-cache' always;
    add_header X-Accel-Buffering 'no' always;  # 禁用 Nginx 缓冲（关键）
    
    # 超时设置
    proxy_connect_timeout 60s;
    proxy_send_timeout 600s;
    proxy_read_timeout 600s;
    
    # 立即传递响应
    proxy_buffers 8 16k;
    proxy_buffer_size 32k;
    proxy_busy_buffers_size 64k;
}
```

同时，修改 `location /api/` 配置，添加禁用缓冲：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8808;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # 🔥 禁用缓冲（确保流式输出正常工作）
    proxy_buffering off;
    proxy_cache off;
    proxy_request_buffering off;
    gzip off;
    
    # 超时设置
    proxy_connect_timeout 60s;
    proxy_send_timeout 300s;
    proxy_read_timeout 300s;
}
```

## 应用配置

### Windows 环境

```powershell
# 1. 测试配置
nginx -t

# 2. 重新加载配置（不中断服务）
nginx -s reload

# 或者重启 Nginx
taskkill /F /IM nginx.exe
start nginx
```

### 配置文件位置

通常 Nginx 配置文件位于：
- `C:\nginx\conf\nginx.conf`
- 或者你自定义的配置文件路径

## 验证方法

1. **检查配置语法**：
   ```bash
   nginx -t
   ```

2. **测试流式输出**：
   - 打开浏览器开发者工具（F12）
   - 切换到 Network 标签
   - 发送 AI 对话请求
   - 检查响应头是否包含 `X-Accel-Buffering: no`
   - 观察消息是否实时流式输出

3. **检查响应头**：
   ```
   X-Accel-Buffering: no
   Cache-Control: no-cache
   ```

## 关键配置说明

| 配置项 | 作用 | 是否必需 |
|--------|------|---------|
| `proxy_buffering off` | 禁用代理缓冲，立即传递响应 | ✅ 必需 |
| `X-Accel-Buffering: no` | 通过响应头禁用缓冲 | ✅ 强烈推荐 |
| `gzip off` | 禁用 gzip 压缩（压缩会缓冲数据） | ✅ 必需 |
| `proxy_read_timeout 600s` | 增加读取超时时间 | ✅ 必需 |

## 常见问题

### Q: 配置后仍然不工作？
A: 检查以下几点：
1. 确认 Nginx 配置已重新加载：`nginx -s reload`
2. 检查配置语法：`nginx -t`
3. 检查后端是否正确返回流式响应
4. 检查浏览器控制台是否有错误
5. 清除浏览器缓存后重试

### Q: 如何确认配置生效？
A: 使用以下方法：
```bash
# 检查 Nginx 配置
nginx -t

# 查看当前配置
nginx -T | findstr "proxy_buffering"

# 检查响应头（使用 curl 或浏览器开发者工具）
```

### Q: 是否影响其他 API 接口？
A: 
- 如果只对流式接口禁用缓冲，其他接口不受影响
- 如果对所有 `/api/` 禁用缓冲，可能会略微增加服务器负载，但通常影响很小

## 注意事项

1. **配置顺序**：流式接口的配置必须在普通 `/api/` 配置之前，因为 Nginx 按顺序匹配 location
2. **性能影响**：禁用缓冲可能会略微增加服务器负载，但对于流式响应是必需的
3. **超时设置**：根据实际需求调整超时时间，避免流式响应被中断


