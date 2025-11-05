## 基于 Sa-Token 的通用权限管理对接说明（可复用方案）

> 适用场景：后端基于 Java + Spring（Boot/Cloud）集成 [Sa-Token] 实现可复用的认证与权限（RBAC）、会话、风控与多端设备管理能力。

### 目标与范围

- **目标**：沉淀一套可复用的权限管理对接规范，覆盖认证、授权、会话、接口规范、测试与运维。
- **范围**：后端集成、接口约定、数据模型建议、前后端对接约定、安全基线与测试要求。

### 术语速览

- **Token**：登录态凭证，默认放在 `Authorization: Bearer <token>` 或 `satoken` 头/参数中。
- **StpUtil**：Sa-Token 提供的登录与权限校验工具类。
- **注解**：`@SaCheckLogin`、`@SaCheckRole`、`@SaCheckPermission`、`@SaIgnore` 等。
- **逻辑类**：`StpLogic`（可自定义多账号体系，如 admin、app、mini 等）。
- **会话**：`SaSession`，支持用户会话、Token 会话与自定义会话。

---

## 一、接入清单（最小可用到可复用）

### 1. 依赖与版本

```xml
<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-spring-boot3-starter</artifactId>
  <version>latest</version>
</dependency>
<!-- 如需 Redis 持久化会话/黑名单等 -->
<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-dao-redis-jackson</artifactId>
  <version>latest</version>
</dependency>
```

### 2. 基础配置（application.yml 示例）

```yaml
sa-token:
  token-name: Authorization # Token 名（也可用 satoken）
  token-prefix: Bearer # HTTP 头前缀
  timeout: 2592000 # 登录有效期（秒），默认 30 天
  activity-timeout: 1800 # 临时有效期（秒），开启续期场景
  is-concurrent: false # 是否同一账号并发登录
  is-share: false # 同端是否共享 Token
  token-style: uuid # uuid | random-32 | simple-uuid | tik 等
  is-log: false # 生产建议 false
  is-read-cookie: false # 前后端分离常设为 false
  is-read-body: false
  is-read-header: true
  jwt-secret-key: "" # 若使用 jwt 模式需设置
```

### 3. Web 安全拦截

- 全局拦截器/过滤器：放行登录、静态资源、健康检查；其余路径按需鉴权。

```java
@Configuration
public class SaTokenConfigure {
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
            .addExclude("/auth/login", "/actuator/**", "/public/**")
            .setAuth(obj -> { /* 可做统一前置校验与日志 */ })
            .setError(e -> SaResult.error(e.getMessage()));
    }
}
```

### 4. 登录与鉴权基础流程

1. 账号密码/第三方凭证校验通过后：`StpUtil.login(userId, device)` 生成 Token；
2. 返回 Token 与用户基础资料；
3. 业务接口使用注解或 `StpUtil` 做登录/角色/权限校验；
4. 续签：访问期间若超出 `activity-timeout` 会自动刷新临时有效期；
5. 登出/踢人/封禁、黑名单可按需开启。

### 5. 注解与 AOP

- `@SaCheckLogin`：要求已登录。
- `@SaCheckRole("admin")`：要求角色。
- `@SaCheckPermission("user:read")`：要求权限码。
- `@SaIgnore`：忽略 Sa-Token 校验（如回调/公用接口）。

### 6. RBAC 数据模型建议

- 表：`sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`、`sys_menu`、`sys_tenant`（如需多租户）。
- 可参考项目内种子脚本 `src/main/resources/db/seed/init_all_tables.sql` 进行落表与索引优化。

---

## 二、标准接口规范（后端可复用模板）

> 返回体建议统一为强类型 DTO，且状态码/提示语来自 `ResultCode` 枚举，避免硬编码。

### 1) 认证类

- `POST /auth/login`
  - 入参：`username`、`password`、`device`、`captchaId`、`captchaCode`
  - 出参：`token`、`expireIn`、`refreshIn`、`userProfile`
- `POST /auth/logout`
- `POST /auth/refresh`
  - 场景：短 Token + 长刷新期；或主动续签通知
- `GET /auth/profile`：获取当前用户资料、角色、权限码、菜单树
- `GET /auth/permissions`：仅拉权限码列表（前端快速权限渲染）

### 2) 鉴权便捷接口（可选）

- `GET /auth/check/login`：是否登录
- `GET /auth/check/role?code=admin`
- `GET /auth/check/perm?code=user:query`

### 3) 会话与风控

- `GET /session/current`：当前 `SaSession` 与 Token 信息
- `POST /session/kickout`：按用户或 Token 踢下线
- `POST /session/disable`：封禁（含时长、原因）
- `GET /session/devices`：多端设备列表与状态

### 4) 用户与组织（管理端）

- `POST /users`、`PUT /users/{id}`、`DELETE /users/{id}`、`GET /users/{id}`、`GET /users`
- 绑定/解绑角色：`POST /users/{id}/roles`、`DELETE /users/{id}/roles/{roleId}`

### 5) 角色管理

- `POST /roles`、`PUT /roles/{id}`、`DELETE /roles/{id}`、`GET /roles/{id}`、`GET /roles`
- 角色授权：`POST /roles/{id}/permissions`、`DELETE /roles/{id}/permissions/{permId}`

### 6) 权限点/菜单

- `POST /permissions`、`PUT /permissions/{id}`、`DELETE /permissions/{id}`、`GET /permissions`
- `POST /menus`、`PUT /menus/{id}`、`DELETE /menus/{id}`、`GET /menus/tree`

### 7) 多租户/多应用（可选）

- `POST /tenants`、`GET /tenants`、`POST /tenants/{id}/users`
- `POST /apps`、`GET /apps`、`POST /apps/{id}/roles`

---

## 三、控制层示例（片段）

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/login")
    public LoginResp login(@RequestBody LoginReq req) {
        // 1. 校验账号密码与验证码
        // 2. 登录并指定设备类型（pc/app/mini 等）
        StpUtil.login(req.getUserId(), req.getDevice());
        // 3. 组装 Token 与用户资料返回
        return LoginResp.from(StpUtil.getTokenValue(), StpUtil.getTokenTimeout());
    }

    @PostMapping("/logout")
    public VoidResp logout() {
        StpUtil.logout();
        return VoidResp.success();
    }

    @GetMapping("/profile")
    @SaCheckLogin
    public ProfileResp profile() {
        String loginId = String.valueOf(StpUtil.getLoginId());
        // 加载角色、权限码、菜单树
        return profileService.load(loginId);
    }
}
```

---

## 四、返回体与状态码规范

- 统一响应：`code`、`message`、`data`、`traceId`。
- `code/message` 必须来源于 `ResultCode` 枚举；`data` 使用强类型 DTO（位于 `sso` 目录或统一 dto 包）。

```json
{
  "code": "OK",
  "message": "操作成功",
  "data": { "token": "...", "expireIn": 2592000 },
  "traceId": "3f1f..."
}
```

---

## 五、前后端约定

- **认证头**：`Authorization: Bearer <token>`；未登录/过期返回特定错误码，前端统一跳转登录。
- **幂等与重试**：登录、授权敏感接口建议加幂等键与失败重试上限。
- **菜单与权限加载**：登录后首次拉取 `profile`（含角色、权限码、菜单树）；页面级动态校验权限码。
- **时间与签名**：重要写操作可增加时间戳与签名校验，防止重放。

---

## 六、安全基线

- 密码加密：`BCrypt`/`Argon2` 存储；登录失败次数与冷却；图形/滑块验证码。
- CORS 与 CSRF：前后端分离默认使用 Token，CSRF 关闭；严控跨域源与方法。
- XSS/SQL 注入：参数校验、白名单、ORM 预编译；输出编码。
- 速率限制：登录与权限管理接口限流；IP 与账号维度风控。
- 会话治理：并发登录开关、黑名单、设备隔离、离线通知。

---

## 七、可复用实现要点

1. 模块化：抽象 `auth`、`user`、`role`、`permission`、`menu`、`session`、`tenant` 模块，统一 DTO 与错误码。
2. 可配置：Token 风格、有效期、并发策略、设备枚举、登录源、权限注解白名单由配置驱动。
3. 存储解耦：会话、黑名单可切 Redis；权限数据来自 DB；提供接口以便替换。
4. 审计日志：登录、授权变更、踢人、封禁、角色/权限绑定均记录。

---

## 八、测试与验收

- 接口契约测试：在 `test/java` 目录为每个接口编写独立的 Mock 用例，覆盖成功/失败/越权场景。
- 集成测试：登录—>访问受限接口—>续签—>登出—>再次访问 的端到端链路。
- 数据权限：基于角色和组织的行/列级控制校验（如有数据权限需求）。

---

## 九、快速对接清单（Checklist）

- [ ] 引入 Sa-Token 与 Redis 依赖
- [ ] 补齐 `application.yml` 中 Sa-Token 配置
- [ ] 配置全局过滤器与放行路径
- [ ] 实现登录/登出/续签与 `profile` 接口
- [ ] 打通角色、权限、菜单、绑定关系 CRUD
- [ ] 统一响应 DTO 与 `ResultCode` 枚举
- [ ] 开启日志审计与限流
- [ ] 完成单测与集成测试

---

## 十、常见问题 FAQ

- Q: 需要 JWT 吗？
  - A: Sa-Token 自带多种 Token 风格，非必须。如需前端离线校验可选 JWT，并设置 `jwt-secret-key`。
- Q: 并发登录如何控制？
  - A: `is-concurrent=false` 禁止并发；结合设备维度与 `is-share` 控制同端共享。
- Q: 如何做多端设备隔离？
  - A: `StpUtil.login(userId, device)`，不同 `device` 生成独立 Token 与会话。
