# Nginx 部署配置说明

## 快速部署

### 1. 复制文件到服务器

将 `dist` 目录中的所有文件复制到 Nginx 的网站根目录，例如：
```bash
# 假设 Nginx 网站根目录为 /usr/share/nginx/html
cp -r dist/* /usr/share/nginx/html/
```

### 2. 配置 Nginx

#### 方式一：使用项目提供的配置文件

```bash
# 复制配置文件到 Nginx 配置目录
cp nginx.conf /etc/nginx/sites-available/sci-z-platform
ln -s /etc/nginx/sites-available/sci-z-platform /etc/nginx/sites-enabled/

# 修改配置文件中的路径和后端地址
vim /etc/nginx/sites-available/sci-z-platform
```

**需要修改的配置项：**
- `root`: 修改为实际的网站根目录路径
- `server_name`: 修改为实际的域名
- `proxy_pass`: 修改为实际的后端服务器地址和端口

#### 方式二：手动配置

在 Nginx 配置文件中添加以下配置：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/sci-z-web/dist;
    index index.html;

    # 启用 gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API 代理 - 后端服务
    location /api {
        proxy_pass http://your-backend-server:8808;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 前端路由支持（Vue Router history 模式）
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 3. 测试配置

```bash
# 测试 Nginx 配置是否正确
nginx -t

# 重新加载 Nginx 配置
nginx -s reload
# 或
systemctl reload nginx
```

### 4. 验证部署

访问以下 URL 验证是否正常：

- ✅ `http://your-domain.com/` - 首页
- ✅ `http://your-domain.com/login` - 登录页
- ✅ `http://your-domain.com/ai/chat` - AI对话页（关键测试）
- ✅ `http://your-domain.com/dashboard` - 仪表板

**重要：** 直接访问 `/ai/chat` 路由时不应该返回 404，应该正常显示页面。

## 配置说明

### 关键配置项

1. **前端路由支持**
   ```nginx
   location / {
       try_files $uri $uri/ /index.html;
   }
   ```
   这是解决 Vue Router history 模式的关键配置，确保所有前端路由都能正确访问。

2. **API 代理**
   ```nginx
   location /api {
       proxy_pass http://your-backend-server:8808;
   }
   ```
   将 `/api` 开头的请求代理到后端服务器。

3. **静态资源缓存**
   ```nginx
   location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
       expires 1y;
       add_header Cache-Control "public, immutable";
   }
   ```
   静态资源长期缓存，提高加载速度。

4. **index.html 不缓存**
   ```nginx
   location = /index.html {
       add_header Cache-Control "no-cache, no-store, must-revalidate";
   }
   ```
   确保 `index.html` 不被缓存，以便及时获取最新版本。

## Docker 部署示例

如果使用 Docker 部署，可以使用以下 Dockerfile：

```dockerfile
FROM nginx:alpine

# 复制前端文件
COPY dist/ /usr/share/nginx/html/

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

构建和运行：
```bash
docker build -t sci-z-web .
docker run -d -p 80:80 sci-z-web
```

## 常见问题

### 1. 刷新页面返回 404

**原因：** Nginx 未配置 `try_files` 规则

**解决：** 确保配置文件中包含：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 2. API 请求失败

**原因：** 代理配置不正确或后端服务未启动

**解决：** 
- 检查 `proxy_pass` 配置是否正确
- 检查后端服务是否正常运行
- 检查防火墙是否开放端口

### 3. 静态资源加载失败

**原因：** 资源路径配置不正确

**解决：** 检查 `vite.config.js` 中的 `base` 配置（如果使用子路径部署）

### 4. 跨域问题

**原因：** 后端未配置 CORS 或代理配置不正确

**解决：** 
- 确保 Nginx 代理配置正确
- 或后端配置 CORS 允许前端域名

## 生产环境建议

1. **启用 HTTPS**
   ```nginx
   server {
       listen 443 ssl;
       ssl_certificate /path/to/cert.pem;
       ssl_certificate_key /path/to/key.pem;
       # ... 其他配置
   }
   ```

2. **配置日志轮转**
   使用 `logrotate` 管理 Nginx 日志文件

3. **性能优化**
   - 启用 HTTP/2
   - 配置缓存策略
   - 使用 CDN 加速静态资源

4. **安全加固**
   - 配置安全头
   - 限制请求大小
   - 配置访问限制

