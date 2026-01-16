# 生产环境配置文件说明

## 文件说明

- `production.env.properties.example` - 配置文件示例模板（可提交到代码库）
- `production.env.properties` - 实际生产环境配置（已加入 .gitignore，不会提交到公网仓库）

## 使用方法

### 1. 首次使用

```bash
# 复制示例文件
cp production.env.properties.example production.env.properties

# 编辑配置文件，填写实际的生产环境配置值
# 注意：请妥善保管此文件，不要提交到公网仓库（如 GitHub）
```

### 2. 配置说明

- **GitLab（内网）**：可以提交 `production.env.properties`，因为内网环境相对安全
- **GitHub（公网）**：**禁止提交** `production.env.properties`，已加入 `.gitignore` 防止泄露

### 3. 环境变量配置（本地/开发环境）

本地和开发环境需要通过环境变量设置阿里云配置，**不要**直接修改配置文件。

#### Windows (PowerShell)

```powershell
$env:SMS_ALIYUN_ACCESS_KEY_ID="your_key_id"
$env:SMS_ALIYUN_ACCESS_KEY_SECRET="your_key_secret"
$env:ALIYUN_TRANSLATION_ACCESS_KEY_ID="your_key_id"
$env:ALIYUN_TRANSLATION_ACCESS_KEY_SECRET="your_key_secret"
```

#### Windows (CMD)

```cmd
set SMS_ALIYUN_ACCESS_KEY_ID=your_key_id
set SMS_ALIYUN_ACCESS_KEY_SECRET=your_key_secret
set ALIYUN_TRANSLATION_ACCESS_KEY_ID=your_key_id
set ALIYUN_TRANSLATION_ACCESS_KEY_SECRET=your_key_secret
```

#### Linux/Mac

```bash
export SMS_ALIYUN_ACCESS_KEY_ID="your_key_id"
export SMS_ALIYUN_ACCESS_KEY_SECRET="your_key_secret"
export ALIYUN_TRANSLATION_ACCESS_KEY_ID="your_key_id"
export ALIYUN_TRANSLATION_ACCESS_KEY_SECRET="your_key_secret"
```

### 4. 安全建议

1. **生产环境**：使用 `production.env.properties` 文件，通过 `application-prod.yml` 自动加载
2. **本地/开发环境**：使用环境变量，不要将敏感信息写入配置文件
3. **版本控制**：
   - ✅ 可以提交：`production.env.properties.example`
   - ❌ 禁止提交：`production.env.properties`（已加入 .gitignore）
4. **密钥管理**：建议使用密钥管理服务（如阿里云 KMS、AWS Secrets Manager）管理生产环境密钥

## 配置项说明

### 阿里云短信服务

- `SMS_ALIYUN_ACCESS_KEY_ID` - 阿里云 AccessKey ID
- `SMS_ALIYUN_ACCESS_KEY_SECRET` - 阿里云 AccessKey Secret
- `SMS_ALIYUN_SIGN_NAME` - 短信签名名称（需要在阿里云短信服务中配置）
- `SMS_ALIYUN_TEMPLATE_CODE` - 短信模板代码（需要在阿里云短信服务中创建模板）

### 阿里云翻译服务

- `ALIYUN_TRANSLATION_ACCESS_KEY_ID` - 阿里云 AccessKey ID
- `ALIYUN_TRANSLATION_ACCESS_KEY_SECRET` - 阿里云 AccessKey Secret
- `ALIYUN_TRANSLATION_ENDPOINT` - 翻译服务端点（默认：mt.cn-hangzhou.aliyuncs.com）

## 注意事项

⚠️ **重要**：如果 `production.env.properties` 已经被提交到 Git 历史记录中，需要：

1. 从 Git 历史中删除该文件：

   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch sci-z-server/src/main/resources/config/production.env.properties" \
     --prune-empty --tag-name-filter cat -- --all
   ```

2. 强制推送（谨慎操作，需要团队协调）：

   ```bash
   git push origin --force --all
   ```

3. 通知团队成员重新克隆仓库或更新配置
