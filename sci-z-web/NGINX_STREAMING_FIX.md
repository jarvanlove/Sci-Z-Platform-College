# Nginx 流式输出配置说明

## 问题描述

打包后通过 Nginx 代理，AI 对话的流式输出不工作，等待全部接收后才响应。

## 问题原因

Nginx 默认会缓冲代理响应，这会导致流式输出（SSE/EventStream）被缓冲，直到全部数据接收完毕才返回给客户端。

## 解决方案

### 1. 禁用代理缓冲

在 Nginx 配置中添加以下关键配置：

```nginx
# 禁用代理缓冲（关键配置）
proxy_buffering off;
proxy_cache off;
proxy_request_buffering off;

# 禁用 gzip 压缩（流式响应不应压缩）
gzip off;

# 设置响应头，禁用缓冲
add_header X-Accel-Buffering 'no' always;
```

### 2. 完整的流式接口配置

```nginx
# 专门为流式接口配置
location ~ ^/api/(chat/workflow/run|knowledge/chatbot/stream) {
    proxy_pass http://backend:8808;
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
    
    # 超时设置（流式响应需要更长的超时时间）
    proxy_connect_timeout 60s;
    proxy_send_timeout 600s;  # 10分钟
    proxy_read_timeout 600s;  # 10分钟
    
    # 立即传递响应
    proxy_buffers 8 16k;
    proxy_buffer_size 32k;
    proxy_busy_buffers_size 64k;
}
```

### 3. 应用配置

```bash
# 1. 测试配置
nginx -t

# 2. 重新加载配置（不中断服务）
nginx -s reload

# 或者重启 Nginx
systemctl restart nginx
```

## 关键配置说明

### proxy_buffering off
- **作用**：禁用代理缓冲，立即传递响应数据
- **必须**：流式响应必须禁用缓冲

### X-Accel-Buffering: no
- **作用**：通过响应头告诉 Nginx 不要缓冲
- **必须**：即使设置了 `proxy_buffering off`，也建议添加此响应头

### gzip off
- **作用**：禁用 gzip 压缩
- **原因**：gzip 压缩会缓冲数据，影响流式输出

### 超时设置
- **proxy_read_timeout**：增加读取超时时间（流式响应可能需要较长时间）
- **proxy_send_timeout**：增加发送超时时间

## 验证方法

1. **检查响应头**：
   - 打开浏览器开发者工具
   - 查看 Network 标签
   - 检查响应头是否包含 `X-Accel-Buffering: no`

2. **测试流式输出**：
   - 发送 AI 对话请求
   - 观察消息是否实时流式输出
   - 不应该等待全部接收后才显示

3. **检查 Nginx 日志**：
   ```bash
   tail -f /var/log/nginx/access.log
   tail -f /var/log/nginx/error.log
   ```

## 注意事项

1. **性能影响**：禁用缓冲可能会增加服务器负载，但对于流式响应是必需的
2. **超时设置**：根据实际需求调整超时时间，避免流式响应被中断
3. **其他接口**：普通 API 接口仍然可以使用缓冲和压缩，只有流式接口需要特殊配置

## 常见问题

### Q: 配置后仍然不工作？
A: 检查以下几点：
1. 确认 Nginx 配置已重新加载：`nginx -s reload`
2. 检查后端是否正确返回流式响应
3. 检查浏览器控制台是否有错误
4. 检查 Nginx 错误日志

### Q: 如何确认配置生效？
A: 使用以下命令检查：
```bash
# 检查 Nginx 配置
nginx -t

# 查看当前配置
nginx -T | grep -A 20 "location.*stream"

# 检查响应头
curl -I http://your-domain/api/chat/workflow/run
```

## 参考文档

- [Nginx 代理缓冲配置](http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_buffering)
- [Nginx 流式响应配置](http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_buffering)


