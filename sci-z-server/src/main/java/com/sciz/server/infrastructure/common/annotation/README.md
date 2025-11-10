# 增强注解系统

## 概述

本系统提供了 3 个功能强大的自定义注解，用于简化常见的横切关注点处理：

- `@Log` - 日志记录
- `@Retry` - 重试机制
- `@Idempotent` - 幂等性控制

## 1. @Log 注解

### 功能特性

- 支持多种日志级别（DEBUG、INFO、WARN、ERROR）
- 可控制是否记录方法参数、返回值、执行时间
- 支持敏感参数脱敏
- 可记录用户信息和 IP 地址
- 支持异常信息记录

### 使用示例

```java
// 基础使用
@Log("用户登录")
public void login(String username, String password) {
    // 业务逻辑
}

// 详细配置
@Log(
    value = "创建项目",
    level = Log.LogLevel.INFO,
    logArgs = true,           // 记录参数
    logResult = true,         // 记录返回值
    logExecutionTime = true,  // 记录执行时间
    logUser = true,          // 记录用户信息
    logIp = true,            // 记录IP地址
    sensitiveParams = {"password", "token"}  // 敏感参数脱敏
)
public String createProject(String projectName, String description, String password) {
    return "project-123";
}
```

### 参数说明

- `value`: 日志描述信息
- `level`: 日志级别（DEBUG、INFO、WARN、ERROR）
- `logArgs`: 是否记录方法参数
- `logResult`: 是否记录返回值
- `logExecutionTime`: 是否记录执行时间
- `logException`: 是否记录异常信息
- `logUser`: 是否记录用户信息
- `logIp`: 是否记录 IP 地址
- `sensitiveParams`: 敏感参数名称数组

## 2. @Retry 注解

### 功能特性

- 支持多种重试策略（固定间隔、指数退避、线性递增、随机退避）
- 可指定需要重试或不需要重试的异常类型
- 支持 SpEL 表达式条件重试
- 可配置最大延迟时间
- 支持重试日志记录

### 使用示例

```java
// 基础重试
@Retry(maxAttempts = 3, delay = 1000)
public void callExternalApi() {
    // 调用外部API
}

// 指数退避重试
@Retry(
    maxAttempts = 5,
    delay = 1000,
    strategy = Retry.RetryStrategy.EXPONENTIAL,
    multiplier = 2.0,
    maxDelay = 10000,
    retryFor = {RuntimeException.class},
    noRetryFor = {IllegalArgumentException.class}
)
public void callUnreliableService() {
    // 调用不可靠的服务
}

// 条件重试
@Retry(
    maxAttempts = 3,
    delay = 2000,
    strategy = Retry.RetryStrategy.LINEAR,
    condition = "T(java.util.Optional).ofNullable(#result).map(T(java.lang.String)::isEmpty).orElse(true)"
)
public String fetchData(String key) {
    return null; // 如果返回空则重试
}
```

### 参数说明

- `maxAttempts`: 最大重试次数
- `delay`: 重试间隔时间（毫秒）
- `strategy`: 重试策略（FIXED、EXPONENTIAL、LINEAR、RANDOM）
- `multiplier`: 退避倍数（用于指数退避）
- `maxDelay`: 最大延迟时间（毫秒）
- `retryFor`: 需要重试的异常类型
- `noRetryFor`: 不需要重试的异常类型
- `logRetry`: 是否记录重试日志
- `condition`: 重试条件表达式（SpEL）

### 重试策略说明

- **FIXED**: 固定间隔重试
- **EXPONENTIAL**: 指数退避重试（延迟时间 = delay \* multiplier^attempt）
- **LINEAR**: 线性递增重试（延迟时间 = delay \* attempt）
- **RANDOM**: 随机退避重试（在 0 到 delay 之间随机）

## 3. @Idempotent 注解

### 功能特性

- 支持多种幂等性策略（拒绝重复、返回缓存、等待执行）
- 支持 SpEL 表达式生成幂等性键
- 可配置键过期时间
- 支持自定义键生成器
- 可控制键的删除时机

### 使用示例

```java
// 基础幂等性（拒绝重复请求）
@Idempotent(key = "#userId + '_' + #orderId", expire = 300)
public void createOrder(Long userId, String orderId) {
    // 创建订单
}

// 返回缓存结果
@Idempotent(
    key = "#projectId + '_get_project'",
    expire = 600,
    strategy = Idempotent.IdempotentStrategy.RETURN_CACHED,
    message = "项目信息正在处理中，请稍后查看"
)
public String getProject(Long projectId) {
    return "project-data";
}

// 等待正在执行的请求完成
@Idempotent(
    key = "#userId + '_' + #reportType",
    expire = 1800,
    strategy = Idempotent.IdempotentStrategy.WAIT_AND_RETURN,
    deleteAfterExecution = true,
    deleteOnException = true
)
public String generateReport(Long userId, String reportType) {
    return "report-data";
}
```

### 参数说明

- `key`: 幂等性键（支持 SpEL 表达式）
- `expire`: 键过期时间（秒）
- `strategy`: 幂等性策略（REJECT、RETURN_CACHED、WAIT_AND_RETURN）
- `prefix`: 键前缀
- `keyGenerator`: 键生成器类名
- `message`: 重复请求时的提示信息
- `logIdempotent`: 是否记录幂等性日志
- `deleteAfterExecution`: 是否在方法执行完成后删除键
- `deleteOnException`: 异常时是否删除键

### 幂等性策略说明

- **REJECT**: 拒绝重复请求，直接返回错误
- **RETURN_CACHED**: 返回缓存的结果
- **WAIT_AND_RETURN**: 等待正在执行的请求完成，返回其结果

## 4. 组合使用

### 典型组合场景

```java
// 支付处理：日志 + 重试 + 幂等性
@Log(
    value = "支付处理",
    level = Log.LogLevel.INFO,
    logArgs = true,
    logResult = true,
    logExecutionTime = true,
    logUser = true,
    sensitiveParams = {"cardNumber", "cvv"}
)
@Retry(
    maxAttempts = 3,
    delay = 2000,
    strategy = Retry.RetryStrategy.EXPONENTIAL,
    retryFor = {RuntimeException.class},
    logRetry = true
)
@Idempotent(
    key = "#paymentId",
    expire = 600,
    strategy = Idempotent.IdempotentStrategy.REJECT,
    message = "支付请求正在处理中，请勿重复提交"
)
public String processPayment(String paymentId, String cardNumber, String cvv, Long amount) {
    // 支付处理逻辑
    return "payment-success";
}
```

## 5. 最佳实践

### 1. 日志记录

- 对于敏感操作，启用用户信息和 IP 记录
- 对敏感参数进行脱敏处理
- 根据业务重要性选择合适的日志级别

### 2. 重试机制

- 对于外部服务调用，使用指数退避策略
- 明确指定需要重试的异常类型
- 设置合理的最大延迟时间

### 3. 幂等性控制

- 对于关键业务操作，使用幂等性控制
- 根据业务场景选择合适的策略
- 合理设置键的过期时间

### 4. 性能考虑

- 避免在高频调用的方法上使用复杂的日志记录
- 重试机制会增加响应时间，需要权衡
- 幂等性控制会增加 Redis 操作，注意性能影响

## 6. 注意事项

1. **AOP 代理前提**

   - 仅对 Spring 容器管理的 Bean 生效（需被扫描为 `@Component`/`@Service` 等）
   - 通过代理拦截方法调用：JDK 动态代理（接口）或 CGLIB 代理（类）。若使用 `final` 类/方法可能导致拦截失效
   - 自调用问题：同类内部 `this.method()` 的调用不会被 AOP 拦截，需通过注入自身代理或将方法拆到独立 Bean

2. **方法可见性与签名限制**

   - 仅支持 `public` 方法。`private/protected` 方法不会被代理
   - 不建议在构造函数中使用，代理尚未生效
   - 对于 `@Async` 方法，AOP 与异步代理叠加时需确保方法签名受 Spring 支持

3. **执行顺序与组合使用**

   - 建议切面顺序（从外到内）：幂等性 -> 重试 -> 日志
     - 幂等性优先：先拦截重复请求，减少下游压力
     - 重试其次：对可重试的下游失败进行退避重试
     - 日志最后：统一记录一次成功/失败与耗时
   - 可通过 `@Order` 显式控制切面顺序，例如：`IdempotentAspect(Ordered.HIGHEST_PRECEDENCE) < RetryAspect < LogAspect`

4. **异常传播与事务交互**

   - 重试策略通常基于“抛出异常”来触发下一次尝试，若业务层吞掉异常将导致重试不生效
   - 与 `@Transactional` 结合时：
     - 若重试包裹在事务外层，需考虑每次重试是否开启新事务（建议在重试内部分步调用，避免长事务）
     - 幂等拦截应在事务外触发，避免重复写入；可在成功后删除幂等键（看业务需要）

5. **并发与分布式环境**

   - 幂等实现通常依赖缓存/锁（如 Redis）：
     - 键的设计需唯一且可复现；过期时间要覆盖最长处理时间，避免重复放行
     - 分布式锁需考虑续期/失效与异常释放（`deleteOnException` 参数）
   - 重试在并发场景下可能放大请求风暴，建议配合熔断/限流

6. **性能与日志安全**

   - 日志打印参数与返回值会引入序列化/拼接开销，高频方法建议关闭或降级记录
   - 对敏感字段（密码、密钥、身份证等）务必开启脱敏：`sensitiveParams`
   - 设置最大日志长度或对象白名单，避免大量 payload 打爆日志

7. **SpEL 表达式与键生成**

   - SpEL 中引用参数名需与方法形参一致；若编译器未保留参数名，需使用 `@Param` 或开启 `-parameters`
   - 键的组成建议包含业务维度（用户、租户、资源 ID），避免跨租户/跨用户冲突
   - 复杂键可委托自定义 KeyGenerator 以统一规范

8. **HTTP/接口层行为**

   - 若依赖 `NoHandlerFoundException` 捕获 404，需要配置 `spring.mvc.throw-exception-if-no-handler-found=true` 与静态资源开关
   - `HttpMessageNotReadableException` 多为 JSON 反序列化失败，日志中避免打印完整报文

9. **可观测性与治理**
   - 重试应携带 attempt 序号与退避时间，便于排查
   - 幂等命中情况应有命中率指标；重复提交来源应可审计
   - 日志与指标（成功率、P99 耗时）建议接入统一观测平台

## 7. 扩展说明

如需扩展功能，可以：

1. 修改注解定义，添加新的参数
2. 更新对应的 Aspect 实现
3. 添加新的策略枚举
4. 实现自定义的键生成器或条件判断器
