# 前端部署说明

## 问题说明

前端使用 Vue Router 的 **history 模式**，当直接访问路由（如 `/ai/chat`）时，服务器会尝试查找对应的物理文件，但实际上这些路径是前端路由，不存在对应的文件，导致返回 404。

## 解决方案

### 方案一：Apache 服务器

1. 确保 `dist` 目录中包含 `.htaccess` 文件（已自动生成）
2. 确保 Apache 已启用 `mod_rewrite` 模块

**检查 mod_rewrite 是否启用：**
```bash
# 在 Apache 配置文件中查找
LoadModule rewrite_module modules/mod_rewrite.so
```

**如果未启用，需要：**
- 取消注释该行
- 重启 Apache 服务

### 方案二：Nginx 服务器

1. 参考 `nginx.conf.example` 文件配置 Nginx
2. 关键配置：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

**完整配置示例：**
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/sci-z-web/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 方案三：其他服务器

#### IIS (Windows)

在 `dist` 目录创建 `web.config` 文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <system.webServer>
    <rewrite>
      <rules>
        <rule name="Handle History Mode" stopProcessing="true">
          <match url="(.*)" />
          <conditions logicalGrouping="MatchAll">
            <add input="{REQUEST_FILENAME}" matchType="IsFile" negate="true" />
            <add input="{REQUEST_FILENAME}" matchType="IsDirectory" negate="true" />
          </conditions>
          <action type="Rewrite" url="/" />
        </rule>
      </rules>
    </rewrite>
  </system.webServer>
</configuration>
```

#### Node.js (Express)

```javascript
const express = require('express');
const path = require('path');
const app = express();

app.use(express.static(path.join(__dirname, 'dist')));

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/index.html'));
});

app.listen(3000);
```

#### Node.js (Koa)

```javascript
const Koa = require('koa');
const serve = require('koa-static');
const path = require('path');
const app = new Koa();

app.use(serve(path.join(__dirname, 'dist')));

app.use(async (ctx) => {
  ctx.type = 'html';
  ctx.body = require('fs').createReadStream(path.join(__dirname, 'dist/index.html'));
});

app.listen(3000);
```

## 验证部署

部署完成后，验证以下路径是否都能正常访问：

- ✅ `http://your-domain.com/` - 首页
- ✅ `http://your-domain.com/login` - 登录页
- ✅ `http://your-domain.com/ai/chat` - AI对话页
- ✅ `http://your-domain.com/dashboard` - 仪表板

**注意：** 直接访问路由时不应该返回 404，应该正常显示页面内容。

## 常见问题

### 1. 刷新页面返回 404

**原因：** 服务器未配置路由回退到 `index.html`

**解决：** 按照上述方案配置服务器

### 2. 静态资源加载失败

**原因：** 资源路径配置不正确

**解决：** 检查 `vite.config.js` 中的 `base` 配置，确保与部署路径一致

### 3. API 请求失败

**原因：** 跨域或代理配置问题

**解决：** 
- 开发环境：检查 `vite.config.js` 中的 `proxy` 配置
- 生产环境：配置 Nginx 反向代理或后端 CORS

## 环境变量配置

生产环境部署前，需要修改 `env.production` 文件：

```properties
VITE_API_BASE_URL=https://your-api-domain.com/api
VITE_DIFY_API_URL=https://api.dify.ai/v1
VITE_DIFY_API_KEY=your-production-dify-api-key
```

修改后重新打包：
```bash
npm run build
```

