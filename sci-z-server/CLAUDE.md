# Sci-Z Server 项目开发规则

> **注意**: 本文档整合了项目的所有开发规则，Cursor 2.0 会自动识别并应用此文件中的规则。

---

## 一、角色定位与前置条件

### 角色定位

Java 专家

### 回复规范

#### 1. 理解需求

- 仔细分析用户提出的问题或需求
- 识别核心技术点和关键问题
- 考虑前后端交互的完整流程

#### 2. 回复结构

- 回复结果始终为中文
- 显示当前回复使用的模型信息
- 列出一个执行计划然后依次执行
- 每次回复之前都要将你理解的需求向我描述一遍，我确认后才可以进行编码
- 提供清晰的解决方案
- 简明扼要地总结问题

---

## 二、DDD 分层架构

### 分层结构

```
interfaces/          → controller, converter
application/        → service (接口 + impl)
domain/pojo/        → entity, mapper, repository, dto/request|response
infrastructure/      → config, shared, external, common
```

### 依赖方向

`interfaces` → `application` → `domain` ← `infrastructure`

### 各层职责

- **Controller**: 入参校验(@Valid)、鉴权注解、调用 Service、返回 Result<resp>
- **Converter**: MapStruct 转换，只做 req↔entity↔resp 转换
- **Service**: 事务(@Transactional)、业务编排、发布事件
- **Repository**: 组合 Mapper 实现数据访问，禁止业务逻辑
- **Entity**: 继承 BaseEntity，与表字段一一对应

---

## 三、命名规范

### 文件命名

- Entity: `Xxx.java` (如 `SysUser.java`)
- Mapper: `XxxMapper.java`
- Repository: `XxxRepo.java` + `XxxRepoImpl.java`
- Service: `XxxService.java` + `XxxServiceImpl.java`
- Controller: `XxxController.java`
- Converter: `XxxConverter.java`
- Request: `XxxCreateReq.java`, `XxxUpdateReq.java`, `XxxQueryReq.java`
- Response: `XxxResp.java`, `XxxDetailResp.java`, `XxxPageResp.java`
- Event: `XxxCreatedEvent.java`, `XxxUpdatedEvent.java`, `XxxDeletedEvent.java`
- Handler: `XxxEventHandler.java`

### 方法命名

- Controller: `createXxx`, `updateXxx`, `deleteXxx`, `getXxxDetail`, `listXxx`, `pageXxx`, `batchCreateXxx`, `batchDeleteXxx`
- Service: `create`, `update`, `deleteById`, `findDetail`, `list`, `page`, `saveBatch`, `updateBatch`, `deleteBatch`
- Repository:
  - 单个操作：`save`, `update`, `deleteById`, `findById`, `findByCondition`, `pageByCondition`
  - 批量操作：`saveBatch`, `updateBatchById`, `updateStatusByIds`, `deleteBatchByIds`
- Converter: `toEntity(req)`, `toResp(entity)`, `toRespList(list)`, `updateEntity(@MappingTarget, req)`

### 变量命名

- 统一使用驼峰命名（camelCase），禁止使用单字母变量名（如 `a`, `b`, `c`）
- 变量名要有意义，能清晰表达其用途
- 局部变量：`userId`, `userName`, `createTime`
- 循环变量：`item`, `user`, `index`（允许简短的循环变量，但要有意义）
- 布尔变量：`isDeleted`, `hasPermission`, `canEdit`
- 集合变量：`userList`, `userIdSet`, `userMap`
- 临时变量：避免使用 `temp`, `tmp`, `data` 等无意义名称，应使用具体含义的名称

**Stream API Lambda 参数命名规范**：

```java
// ✅ 正确：使用语义化的 lambda 参数名（单数形式）
List<String> usernames = users.stream()
    .filter(user -> user.getStatus() == 1)  // users -> user
    .map(user -> user.getUsername())
    .toList();

List<String> roleCodes = roles.stream()
    .filter(role -> role.getIsDeleted() == 0)  // roles -> role
    .map(SysRole::getRoleCode)
    .toList();

List<String> codes = permissions.stream()
    .filter(perm -> perm.getStatus() == 1)  // permissions -> perm
    .map(SysPermission::getPermissionCode)
    .toList();

// ❌ 错误：使用单字母变量名
users.stream().filter(u -> u.getStatus() == 1)  // 禁止使用 u
roles.stream().filter(r -> r.getIsDeleted() == 0)  // 禁止使用 r
permissions.stream().filter(p -> p.getStatus() == 1)  // 禁止使用 p

// ⚠️ 例外：非常简单的操作可以使用方法引用
users.stream().map(User::getId)  // ✅ 方法引用，无需 lambda 参数
```

**Lambda 参数命名原则**：

- 使用集合名称的单数形式（`users -> user`, `roles -> role`, `permissions -> perm`）
- 简短但要有意义（`perm` 比 `p` 好，`permission` 也可以）
- 保持一致性（同一个 stream 链中使用相同的参数名）
- 复杂逻辑优先使用完整单词（`permission` 而非 `perm`）

---

## 四、注释模板

### 类注释

```java
/**
 * 类功能描述
 *
 * @author JiaWen.Wu
 * @className 当前类名
 * @date 中国北京当前时间 (yyyy-MM-dd HH:mm)
 */
```

**注意**: `@date` 字段需要手动填写创建该文件时的北京时间（格式：yyyy-MM-dd HH:mm），不能自动生成。

### 方法注释

```java
/**
 * 方法是做什么的
 *
 * @param 参数类型 参数描述
 * @return 返回结果
 */
public ReturnType methodName(ParamType param) {
    // 实现
}
```

**注意**：当方法有 `@Override` 注解时，注释必须写在 `@Override` **上面**

```java
/**
 * 方法功能描述
 *
 * @param param 参数描述
 * @return 返回结果
 */
@Override  // ✅ 注释在 @Override 上面
public ReturnType methodName(ParamType param) {
    // 实现
}

// ❌ 错误示例：注释在 @Override 下面
@Override
/**
 * 方法功能描述  ❌ 错误位置
 */
public ReturnType methodName(ParamType param) {
    // 实现
}
```

示例：

```java
/**
 * 获取审批流程
 *
 * @param jsonObject 请求参数
 * @return 审批流程信息
 */
@Override
public ProcessInfo getApprovalProcess(JSONObject jsonObject) {
    // 实现
}
```

### 字段注释

```java
/**
 * 字段注释
 */
```

---

## 五、开发规范

### 日志规范

统一使用 `String.format("%s", ...)` 占位符

```java
log.info(String.format("创建用户: userId=%s, username=%s", userId, username));
log.error(String.format("创建用户失败: err=%s", e.getMessage()), e);
```

### 异常处理

仅抛 `BusinessException(ResultCode.X)`

```java
throw new BusinessException(ResultCode.USER_NOT_FOUND);
throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在: " + userId);
```

### 返回规范

Controller 返回`Result<T>`，Service 返回领域对象

```java
// Controller
return Result.success(resp);

// Service
public XxxResp create(XxxCreateReq req) { return resp; }
```

### 事务规范

Service 写操作加`@Transactional(rollbackFor = Exception.class)`

### Java 版本规范 ⚠️ 重中之重

**项目使用 Java 21，所有新编写的代码必须严格遵循 Java 21 最佳实践**

> **重要说明**：
>
> - ✅ **从现在开始**：所有新代码必须使用 Java 21 新特性和最佳实践
> - ✅ **已有代码**：保持现状，不强制重构
> - ✅ **优先级**：Java 21 规范是最高优先级，优先于其他所有编码规范

---

#### 1. Record 类（优先使用）

**用途**：替代简单的数据传输对象（DTO）、值对象

```java
// ✅ 推荐：使用 Record（Java 21）
public record UserInfo(Long id, String username, String email) {
    // 自动生成：构造方法、getter、equals、hashCode、toString
}

// ❌ 避免：传统的 POJO 类（除非需要可变性）
@Getter
@Setter
public class UserInfo {
    private Long id;
    private String username;
    private String email;
    // ... 大量样板代码
}
```

**适用场景**：

- 简单的 Response DTO（字段少、不可变）
- 不可变配置对象
- 返回多个值的场景
- 缓存对象

**不适用场景**：

- Entity（需要与数据库表对应，使用传统类）
- Request DTO（可能需要 @Valid 等复杂校验）
- 字段过多的 DTO（> 10 个字段，使用传统类更清晰）

**Record 注释规范**：

```java
/**
 * 验证码响应
 *
 * @param captchaKey   String 验证码唯一标识（UUID）
 * @param captchaImage String 验证码图片（Base64 编码）
 * @param expiresIn    Long 验证码过期时间（秒）
 * @author JiaWen.Wu
 * @date 2025-11-07 19:00
 */
public record CaptchaResp(
        String captchaKey,
        String captchaImage,
        Long expiresIn) {
}
```

**注意**：

- ✅ Record 的字段注释使用 `@param` 标签写在类级别的 JavaDoc 中
- ❌ 不在字段旁边写注释（Record 不支持）
- ✅ Record 是不可变的（所有字段都是 final）
- ✅ 使用构造方法创建：`new CaptchaResp(key, image, expire)`
- ❌ 不能使用 setter（Record 没有 setter 方法）

---

**Record 静态工厂方法**：

当需要简化创建或使用默认值时，推荐使用静态工厂方法。

**命名规范**：

```java
of(...)         - 使用部分参数创建（最常用）
empty()         - 创建空对象
withDefaults()  - 使用默认值创建
from(...)       - 从其他对象转换创建
```

**示例**：

```java
public record CaptchaResp(
        String captchaKey,
        String captchaImage,
        Long expiresIn) {

    /**
     * 使用默认过期时间创建
     */
    public static CaptchaResp of(String captchaKey, String captchaImage) {
        return new CaptchaResp(captchaKey, captchaImage, 300L);
    }
}

// 使用
var resp = CaptchaResp.of(captchaKey, captchaImage);
```

---

#### 2. Pattern Matching（模式匹配）

**用途**：简化 instanceof 检查和类型转换

```java
// ✅ 推荐：使用 Pattern Matching（Java 21）
public String processObject(Object obj) {
    if (obj instanceof String s) {
        return s.toUpperCase();  // 直接使用 s，无需强转
    } else if (obj instanceof Integer i) {
        return "Number: " + i;
    }
    return "Unknown";
}

// ❌ 避免：传统的 instanceof + 强转
public String processObject(Object obj) {
    if (obj instanceof String) {
        String s = (String) obj;  // 需要强制转换
        return s.toUpperCase();
    }
    // ...
}
```

**Switch 模式匹配**：

```java
// ✅ 推荐：使用增强的 Switch 表达式
public String formatValue(Object obj) {
    return switch (obj) {
        case String s -> s.toUpperCase();
        case Integer i -> String.valueOf(i * 2);
        case null -> "NULL";
        default -> "Unknown";
    };
}
```

---

#### 3. Text Blocks（文本块）

**用途**：编写多行字符串（SQL、JSON、HTML 等）

```java
// ✅ 推荐：使用 Text Blocks（Java 21）
String sql = """
    SELECT u.id, u.username, u.email
    FROM sys_user u
    WHERE u.status = %s
      AND u.is_deleted = 0
    ORDER BY u.create_time DESC
    """.formatted(EnableStatus.ENABLED.getCode());

// ❌ 避免：传统的字符串拼接
String sql = "SELECT u.id, u.username, u.email\n" +
             "FROM sys_user u\n" +
             "WHERE u.status = " + status + "\n" +
             "  AND u.is_deleted = 0\n" +
             "ORDER BY u.create_time DESC";
```

**JSON 示例**：

```java
String json = """
    {
        "userId": %d,
        "username": "%s",
        "status": "%s"
    }
    """.formatted(userId, username, status);
```

---

#### 4. var 关键字（类型推断）

**用途**：简化局部变量声明（当类型明显时）

```java
// ✅ 推荐：类型明显时使用 var
var user = userRepo.findById(userId);  // 类型明显：SysUser
var list = new ArrayList<String>();    // 类型明显：ArrayList<String>
var mapper = new ObjectMapper();       // 类型明显：ObjectMapper

// ❌ 避免：类型不明显时使用 var
var result = process();  // 不清楚返回类型，不推荐

// ✅ 保持：类型不明显时显式声明
Optional<SysUser> result = findUser(userId);
```

**使用原则**：

- ✅ 类型从右侧表达式一眼就能看出来
- ✅ 提高代码简洁性，不降低可读性
- ❌ 不要为了使用 var 而使用 var

---

#### 5. Stream API 和 Optional

**Stream API（集合处理）**：

```java
// ✅ 推荐：使用 Stream API
List<String> usernames = users.stream()
    .filter(u -> EnableStatus.ENABLED.getCode().equals(u.getStatus()))
    .map(SysUser::getUsername)
    .sorted()
    .toList();  // Java 21：直接使用 toList()

// ❌ 避免：传统的 for 循环
List<String> usernames = new ArrayList<>();
for (SysUser u : users) {
    if (EnableStatus.ENABLED.getCode().equals(u.getStatus())) {
        usernames.add(u.getUsername());
    }
}
Collections.sort(usernames);
```

**Optional（空值处理）**：

```java
// ✅ 推荐：使用 Optional
return Optional.ofNullable(user)
    .map(SysUser::getEmail)
    .orElse("unknown@example.com");

// ✅ 推荐：使用 Optional 避免空指针
Optional.ofNullable(user)
    .filter(u -> EnableStatus.ENABLED.getCode().equals(u.getStatus()))
    .ifPresent(u -> log.info("Active user: {}", u.getUsername()));

// ❌ 避免：传统的 null 检查
if (user != null) {
    String email = user.getEmail();
    if (email != null) {
        return email;
    }
}
return "unknown@example.com";
```

---

#### 6. 现代日期时间 API（java.time）

**强制要求**：必须使用 `java.time` 包，禁止使用 `java.util.Date` 和 `java.sql.Date`

```java
// ✅ 推荐：使用 java.time（Java 8+，Java 21 标准）
LocalDateTime now = LocalDateTime.now();
LocalDate today = LocalDate.now();
ZonedDateTime beijingTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));

// 格式化
String formatted = now.format(DateTimeFormatter.ofPattern(SystemConstant.DATE_TIME_FORMAT));

// 计算
LocalDateTime tomorrow = now.plusDays(1);
Duration duration = Duration.between(start, end);

// ❌ 禁止：使用旧的 Date API
Date date = new Date();  // 禁止！
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // 禁止！
```

**Entity 中的日期字段**：

```java
// ✅ 推荐：使用 LocalDateTime
@TableField("create_time")
private LocalDateTime createTime;

@TableField("last_login_time")
private LocalDateTime lastLoginTime;
```

---

#### 7. 并发工具类

**CompletableFuture（异步编程）**：

```java
// ✅ 推荐：使用 CompletableFuture
CompletableFuture<UserResp> userFuture = CompletableFuture.supplyAsync(
    () -> userService.findById(userId)
);

CompletableFuture<List<Role>> roleFuture = CompletableFuture.supplyAsync(
    () -> roleService.findByUserId(userId)
);

// 组合结果
CompletableFuture.allOf(userFuture, roleFuture)
    .thenRun(() -> {
        UserResp user = userFuture.join();
        List<Role> roles = roleFuture.join();
        // 处理结果
    });
```

**Virtual Thread（虚拟线程，Java 21）**：

```java
// ✅ 推荐：使用 Virtual Thread（适用于高并发 I/O 场景）
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        // 执行任务
    });
}

// 异步处理配置
@Configuration
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

---

#### 8. 避免使用过时的 API

**禁止使用的 API**：

```java
// ❌ 禁止：使用 Date、Calendar
java.util.Date date = new Date();
Calendar calendar = Calendar.getInstance();

// ❌ 禁止：使用 Vector、Hashtable
Vector<String> vector = new Vector<>();
Hashtable<String, String> hashtable = new Hashtable<>();

// ❌ 禁止：使用 StringBuffer（单线程场景）
StringBuffer sb = new StringBuffer();  // 使用 StringBuilder

// ❌ 禁止：使用 @Deprecated 标记的方法
Thread.stop();  // 已废弃，不要使用
```

**推荐的替代方案**：

```java
// ✅ 使用 LocalDateTime 替代 Date
LocalDateTime now = LocalDateTime.now();

// ✅ 使用 ArrayList、HashMap 替代 Vector、Hashtable
List<String> list = new ArrayList<>();
Map<String, String> map = new HashMap<>();

// ✅ 使用 StringBuilder（单线程）
StringBuilder sb = new StringBuilder();
```

---

#### 9. Java 21 新特性优先级

**优先级排序**（从高到低）：

1. **必须使用**：

   - ✅ `java.time` API（替代 Date）
   - ✅ Stream API 和 Optional（集合和空值处理）
   - ✅ Text Blocks（多行字符串）

2. **强烈推荐**：

   - ✅ Record（简单 DTO 和值对象）
   - ✅ Pattern Matching（类型检查和转换）
   - ✅ var 关键字（类型明显时）

3. **视场景使用**：
   - ✅ Sealed Classes（继承控制）
   - ✅ Virtual Thread（高并发 I/O）
   - ✅ CompletableFuture（异步编程）

---

#### 10. 编码风格（步骤化与函数封装）

- **入口步骤化**：业务入口方法应按照清晰的步骤顺序执行，每一步使用注释或日志标识，并保持与实现顺序一致（参考 `AuthServiceImpl#getCaptcha`）。
- **私有方法职责单一**：每个步骤封装为命名清晰的私有方法，方法仅关注自身逻辑，避免在入口方法内出现长链路或嵌套 `if`。
- **校验与转换函数化**：通用校验、数据转换优先抽离为私有方法或转换器，并配合 `Optional`、`Stream` 等链式 API 处理空值与集合。
- **日志与异常归口**：入口方法负责记录关键日志与抛出业务异常，私有方法返回布尔值或数据对象，用返回值驱动流程。

---

#### 11. 代码审查检查点

**新代码提交前检查**：

- [ ] 是否使用了 `java.util.Date` 或 `Calendar`？（应使用 `java.time`）
- [ ] 简单的 DTO 是否可以改为 Record？
- [ ] 是否有冗余的 instanceof + 强转？（应使用 Pattern Matching）
- [ ] 多行字符串是否使用了拼接？（应使用 Text Blocks）
- [ ] 是否有明显的类型推断场景未使用 var？
- [ ] 集合处理是否可以用 Stream API 简化？
- [ ] 空值处理是否使用了 Optional？
- [ ] 是否使用了过时的 API（@Deprecated）？

### 接口规范

- 所有接口要使用统一返回结果 Result.java 以及 ResultCode.java 的枚举定义使用

---

## 六、实现顺序

1. Entity → 继承 BaseEntity，与表字段一一对应
2. Mapper → 继承 BaseMapper<Xxx>
3. Repository → 接口+实现(组合 Mapper)
4. DTO → request(CreateReq/UpdateReq/QueryReq) + response(Resp/DetailResp/PageResp)
5. Converter → MapStruct(componentModel="spring")
6. Service → 接口+实现(事务、编排、发布事件)
7. Controller → 入参校验、鉴权、调用 Service、返回 Result
8. Event & Handler → (按需)发布事件、创建 Handler 处理

---

## 七、代码模板

### Entity

```java
package com.sciz.server.domain.pojo.entity.xxx;

import com.sciz.server.domain.pojo.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Xxx 实体类
 *
 * @author JiaWen.Wu
 * @className Xxx
 * @date 2025-01-XX 14:30
 */
@Getter
@Setter
@TableName("xxx_table")
public class Xxx extends BaseEntity {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
}
```

### Mapper

```java
package com.sciz.server.domain.pojo.mapper.xxx;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.xxx.Xxx;

/**
 * Xxx Mapper
 *
 * @author JiaWen.Wu
 * @className XxxMapper
 * @date 2025-01-XX 14:30
 */
public interface XxxMapper extends BaseMapper<Xxx> {
}
```

### Repository 接口

```java
package com.sciz.server.domain.pojo.repository.xxx;

import com.sciz.server.domain.pojo.entity.xxx.Xxx;

/**
 * Xxx 仓储接口
 *
 * @author JiaWen.Wu
 * @className XxxRepo
 * @date 2025-01-XX 14:30
 */
public interface XxxRepo {
    /**
     * 保存
     *
     * @param entity 实体
     * @return 主键ID
     */
    Long save(Xxx entity);

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 实体
     */
    Xxx findById(Long id);
}
```

### Repository 实现

```java
package com.sciz.server.domain.pojo.repository.xxx.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.sciz.server.domain.pojo.entity.xxx.Xxx;
import com.sciz.server.domain.pojo.mapper.xxx.XxxMapper;
import com.sciz.server.domain.pojo.repository.xxx.XxxRepo;
import org.springframework.stereotype.Repository;

/**
 * Xxx 仓储实现
 *
 * @author JiaWen.Wu
 * @className XxxRepoImpl
 * @date 2025-01-XX 14:30
 */
@Repository
public class XxxRepoImpl implements XxxRepo {
    private final XxxMapper mapper;

    public XxxRepoImpl(XxxMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(Xxx entity) {
        // 新增：直接使用 mapper.insert 方法
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public boolean updateById(Xxx entity) {
        // 更新：使用 mapper.updateById 方法（根据ID更新）
        return mapper.updateById(entity) > 0;
    }

    /**
     * 根据条件更新指定字段
     *
     * @param id 主键ID
     * @param name 名称
     * @return 是否更新成功
     */
    public boolean updateNameById(Long id, String name) {
        // 更新指定字段：使用 LambdaUpdateChainWrapper
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(Xxx::getId, id)
                .set(Xxx::getName, name)
                .update();
    }

    @Override
    public Xxx findById(Long id) {
        // 查询：使用 LambdaQueryChainWrapper
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(Xxx::getId, id)
                .eq(Xxx::getIsDeleted, 0)
                .one();
    }

    @Override
    public boolean deleteById(Long id) {
        // 软删除（相当于更新）：使用 LambdaUpdateChainWrapper
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(Xxx::getId, id)
                .set(Xxx::getIsDeleted, 1)
                .update();
    }
}
```

**说明**:

- **查询操作**：使用 `LambdaQueryChainWrapper`，支持链式调用，类型安全
- **新增操作**：直接使用 `mapper.insert(entity)`，简单高效
- **更新操作**：
  - 根据 ID 更新整个实体：使用 `mapper.updateById(entity)`
  - 根据条件更新指定字段：使用 `LambdaUpdateChainWrapper` 的 `.set()` 方法
- **删除操作**：软删除实际上是通过更新 `is_deleted` 字段实现，使用 `LambdaUpdateChainWrapper` 的 `.set()` 方法

---

### Repository 批量操作

> **详细内容请参考**：[批量操作最佳实践.md](../相关文档/批量操作最佳实践.md)

#### 核心规范

**批量方法命名**：

- `saveBatch(List<T>)` - 批量新增
- `updateBatchById(List<T>)` - 批量更新整个实体（慎用）
- `updateStatusByIds(List<Long>, Integer)` - 批量更新指定字段（推荐）
- `deleteBatchByIds(List<Long>)` - 批量软删除

**性能原则**：

- ✅ **批量更新指定字段**：优先使用 `LambdaUpdateChainWrapper`（一条 SQL，性能最高）
- ✅ **批量新增**：小批量用 `forEach`，大批量分批处理（每批 500 条）
- ✅ **批量删除**：使用 `LambdaUpdateChainWrapper` 实现软删除
- ⚠️ **批量更新整个实体**：会产生 N 条 SQL，性能较低，慎用

#### 典型示例

```java
/**
 * 批量更新状态（只生成 1 条 SQL）⭐⭐⭐⭐⭐
 *
 * @param ids List<Long> ID列表
 * @param status Integer 新状态
 * @return boolean 是否更新成功
 */
public boolean updateStatusByIds(List<Long> ids, Integer status) {
    if (CollectionUtils.isEmpty(ids)) {
        return true;
    }

    return new LambdaUpdateChainWrapper<>(mapper)
            .in(Xxx::getId, ids)
            .set(Xxx::getStatus, status)
            .update();
}
```

**批量软删除（推荐）**：

```java
/**
 * 批量软删除（只生成 1 条 SQL）⭐⭐⭐⭐⭐
 *
 * @param ids List<Long> ID列表
 * @return boolean 是否删除成功
 */
public boolean deleteBatchByIds(List<Long> ids) {
    if (CollectionUtils.isEmpty(ids)) {
        return true;
    }

    return new LambdaUpdateChainWrapper<>(mapper)
            .in(Xxx::getId, ids)
            .set(Xxx::getIsDeleted, DeleteStatus.DELETED.getCode())
            .update();
}
```

**批量新增**：

```java
/**
 * 批量保存实体
 *
 * @param entities List<Xxx> 实体列表
 * @return boolean 是否全部保存成功
 */
public boolean saveBatch(List<Xxx> entities) {
    if (CollectionUtils.isEmpty(entities)) {
        return true;
    }

    try {
        // 小批量：直接插入
        entities.forEach(mapper::insert);

        // 大批量（> 1000条）：分批处理（每批 500 条）
        // 详见：批量操作最佳实践.md

        log.info(String.format("批量保存成功: size=%d", entities.size()));
        return true;
    } catch (Exception e) {
        log.error(String.format("批量保存失败: size=%d, err=%s", entities.size(), e.getMessage()), e);
        return false;
    }
}
```

---

### Request DTO

```java
package com.sciz.server.domain.pojo.dto.request.xxx;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Xxx 创建请求
 *
 * @author JiaWen.Wu
 * @className XxxCreateReq
 * @date 2025-01-XX 14:30
 */
@Getter
@Setter
public class XxxCreateReq {
    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    private String name;
}
```

### Response DTO

```java
package com.sciz.server.domain.pojo.dto.response.xxx;

import lombok.Getter;
import lombok.Setter;

/**
 * Xxx 响应
 *
 * @author JiaWen.Wu
 * @className XxxResp
 * @date 2025-01-XX 14:30
 */
@Getter
@Setter
public class XxxResp {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
}
```

### Converter

#### 接口层（`interfaces/converter`）规范

- 统一使用 **MapStruct**（`@Mapper(componentModel = "spring")`），仅承担 **DTO ↔ 领域实体/响应对象** 的字段映射，严禁包含业务逻辑、校验、数据库访问。
- 命名规则沿用 `XxxConverter`，所在包固定为 `com.sciz.server.interfaces.converter`。
- 方法命名遵循 `toEntity(...)`、`toResp(...)`、`toLoginUserInfoResp(...)` 等语义化名称；若需要组合数据，可在 Service 中调用转换器生成基础对象后再补充业务字段。
- 当字段无法直接映射时使用 `@Mapping` 标明忽略或自定义来源，保持 MapStruct 生成代码可读、可维护。
- 转换器实例通过构造器注入到应用服务（`application/service`）或控制层，禁止在 Controller 中手写 `new XxxResp()` 等重复赋值。
- 根据业务需要提供单向或双向映射方法：例如请求 DTO → 领域实体（创建、更新场景），实体 → 响应 DTO（查询、返回场景）；无需刻意补齐未使用的方向。

#### 外部接口（`infrastructure/external/**/converter`）规范

- 作用：对接三方系统时，将第三方请求/响应转换为项目内部的 **领域命令 / DTO**，或将内部对象转换回三方协议格式，实现 **边界解耦**。
- 命名与目录：按照外部系统划分包路径（如 `infrastructure.external.dify.converter`），转换器命名为 `XxxExternalConverter` 或具备语义的名称。
- 转换策略：
  - **入站**：三方请求 → 外部 DTO → 转换器 → 内部 DTO/命令 → 应用服务；
  - **出站**：应用服务返回内部 DTO/领域对象 → 转换器 → 三方响应模型 → 外部系统。
- 外部转换器同样推荐使用 MapStruct，如需进行格式化/枚举映射等，保持逻辑纯粹（无资源访问）；必要的异常映射在应用服务或外部适配层处理。
- 应用服务层只依赖内部 DTO，所有与第三方格式的细节均在 `infrastructure.external` 层完成，确保领域模型不被外部字段污染。

```java
package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.xxx.XxxCreateReq;
import com.sciz.server.domain.pojo.dto.response.xxx.XxxResp;
import com.sciz.server.domain.pojo.entity.xxx.Xxx;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * Xxx 转换器
 *
 * @author JiaWen.Wu
 * @className XxxConverter
 * @date 2025-01-XX 14:30
 */
@Mapper(componentModel = "spring")
public interface XxxConverter {
    /**
     * req → entity
     */
    Xxx toEntity(XxxCreateReq req);

    /**
     * entity → resp
     */
    XxxResp toResp(Xxx entity);
}
```

### Service 接口

```java
package com.sciz.server.application.service.xxx;

import com.sciz.server.domain.pojo.dto.request.xxx.XxxCreateReq;
import com.sciz.server.domain.pojo.dto.response.xxx.XxxResp;

/**
 * Xxx 应用服务接口
 *
 * @author JiaWen.Wu
 * @className XxxService
 * @date 2025-01-XX 14:30
 */
public interface XxxService {
    /**
     * 创建
     *
     * @param req 创建请求
     * @return 响应
     */
    XxxResp create(XxxCreateReq req);
}
```

### Service 实现

```java
package com.sciz.server.application.service.xxx.impl;

import com.sciz.server.application.service.xxx.XxxService;
import com.sciz.server.domain.pojo.dto.request.xxx.XxxCreateReq;
import com.sciz.server.domain.pojo.dto.response.xxx.XxxResp;
import com.sciz.server.domain.pojo.entity.xxx.Xxx;
import com.sciz.server.domain.pojo.repository.xxx.XxxRepo;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.interfaces.converter.XxxConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xxx 应用服务实现类
 *
 * @author JiaWen.Wu
 * @className XxxServiceImpl
 * @date 2025-01-XX 14:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XxxServiceImpl implements XxxService {
    private final XxxRepo xxxRepo;
    private final XxxConverter xxxConverter;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public XxxResp create(XxxCreateReq req) {
        log.info(String.format("创建Xxx: name=%s", req.getName()));

        // 1. 转换为实体
        Xxx entity = xxxConverter.toEntity(req);

        // 2. 保存
        Long id = xxxRepo.save(entity);
        if (id == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 3. 发布事件（按需）
        // XxxCreatedEvent event = new XxxCreatedEvent(id, entity.getName());
        // eventPublisher.publish(event);

        // 4. 转换为响应
        XxxResp resp = xxxConverter.toResp(entity);

        log.info(String.format("创建Xxx成功: id=%s", id));
        return resp;
    }
}
```

### Controller

```java
package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.xxx.XxxService;
import com.sciz.server.domain.pojo.dto.request.xxx.XxxCreateReq;
import com.sciz.server.domain.pojo.dto.response.xxx.XxxResp;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Xxx 控制器
 *
 * @author JiaWen.Wu
 * @className XxxController
 * @date 2025-01-XX 14:30
 */
@RestController
@RequestMapping("/api/xxx")
@Tag(name = "模块 - Xxx", description = "Xxx 管理")
@RequiredArgsConstructor
public class XxxController {
    private final XxxService xxxService;

    @PostMapping
    @Operation(summary = "创建Xxx", description = "创建Xxx")
    // @SaCheckPermission("api:module:xxx:create")
    public Result<XxxResp> createXxx(@Valid @RequestBody XxxCreateReq req) {
        XxxResp resp = xxxService.create(req);
        return Result.success(resp);
    }
}
```

---

## 八、事件处理（按需）

### Event

```java
package com.sciz.server.infrastructure.shared.event.xxx;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * Xxx 创建事件
 *
 * @author JiaWen.Wu
 * @className XxxCreatedEvent
 * @date 2025-01-XX 14:30
 */
@Getter
@Setter
public class XxxCreatedEvent extends DomainEvent {
    private Long xxxId;
    private String name;

    public XxxCreatedEvent(Long xxxId, String name) {
        super();
        this.xxxId = xxxId;
        this.name = name;
    }

    @Override
    public String getAggregateId() {
        return String.valueOf(xxxId);
    }

    @Override
    public String getAggregateType() {
        return "Xxx";
    }
}
```

### Handler

```java
package com.sciz.server.infrastructure.shared.handler.xxx;

import com.sciz.server.infrastructure.shared.event.xxx.XxxCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Xxx 事件处理器
 *
 * @author JiaWen.Wu
 * @className XxxEventHandler
 * @date 2025-01-XX 14:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XxxEventHandler {
    @EventListener
    @Async
    public void handleXxxCreated(XxxCreatedEvent event) {
        try {
            log.info(String.format("处理Xxx创建事件: xxxId=%s, name=%s", event.getXxxId(), event.getName()));
            // 调用应用服务入库或联动
            log.info(String.format("Xxx创建事件处理完成: xxxId=%s", event.getXxxId()));
        } catch (Exception e) {
            log.error(String.format("Xxx创建事件处理失败: xxxId=%s, err=%s", event.getXxxId(), e.getMessage()), e);
        }
    }
}
```

---

## 九、通用组件规范

### 常量类（Constant）

#### 1. 命名规范

- 文件命名：`XxxConstant.java`
- 包路径：`com.sciz.server.infrastructure.shared.constant`
- 常量命名：全大写，下划线分隔（`UPPER_SNAKE_CASE`）

#### 2. 现有常量类

| 常量类              | 用途           | 示例                                    |
| ------------------- | -------------- | --------------------------------------- |
| **CacheConstant**   | 缓存相关常量   | `AUTH_FAIL_KEY`、`MAX_LOGIN_FAIL_COUNT` |
| **MessageConstant** | 系统级通用消息 | `SUCCESS_MESSAGE`、`FAIL_MESSAGE`       |
| **SystemConstant**  | 系统配置常量   | `DEFAULT_PAGE_SIZE`、`DATE_TIME_FORMAT` |

#### 3. 使用规范

```java
// ✅ 正确：使用常量
String key = String.format(CacheConstant.AUTH_FAIL_KEY, username);
int maxRetry = CacheConstant.MAX_LOGIN_FAIL_COUNT;

// ❌ 错误：硬编码
String key = String.format("auth:fail:%s", username);
int maxRetry = 6;
```

#### 4. 新增常量规范

- 按功能分组，添加分组注释
- 每个常量必须添加 JavaDoc 注释说明用途
- 相关常量放在一起
- 复杂格式常量要写清楚占位符含义

```java
// ==================== 认证相关缓存常量 ====================
/**
 * 认证失败记录缓存前缀（用于记录登录失败次数）
 * 格式：auth:fail:{username}
 */
public static final String AUTH_FAIL_KEY = "auth:fail:%s";

/**
 * 最大登录失败次数（超过此次数将锁定账号）
 */
public static final int MAX_LOGIN_FAIL_COUNT = 6;
```

---

### 枚举类（Enum）

#### 1. 命名规范

- 文件命名：`XxxStatus.java` 或 `XxxFlag.java`
- 包路径：`com.sciz.server.infrastructure.shared.enums`
- 枚举值命名：全大写，下划线分隔
- 字段命名：`code`（Integer）、`description`（String）

#### 2. 现有枚举类

| 枚举类                | 用途          | 代码值   | 对应数据库字段       |
| --------------------- | ------------- | -------- | -------------------- |
| **EnableStatus**      | 启用/禁用状态 | 0/1      | `status`             |
| **DeleteStatus**      | 软删除标记    | 0/1      | `is_deleted`         |
| **LoginStatus**       | 登录状态      | 0/1      | `status`（登录日志） |
| **PermissionType**    | 权限类型      | 1/2/3    | `permission_type`    |
| **UserStatus**        | 用户状态      | -1/0/1/2 | `status`（用户表）   |
| **ProjectStatus**     | 项目状态      | 0~5      | `status`（项目表）   |
| **DeclarationStatus** | 申报状态      | 0~5      | `status`（申报表）   |

#### 3. 枚举模板

```java
package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * Xxx枚举
 * 用途说明
 *
 * @author JiaWen.Wu
 * @className XxxStatus
 * @date yyyy-MM-dd HH:mm (手动填写创建文件时的北京时间)
 */
@Getter
public enum XxxStatus {

    /**
     * 状态1
     */
    STATUS_1(0, "状态1描述"),

    /**
     * 状态2
     */
    STATUS_2(1, "状态2描述");

    private final Integer code;
    private final String description;

    XxxStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举
     */
    public static XxxStatus fromCode(Integer code) {
        for (XxxStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态代码: " + code);
    }

    /**
     * 判断是否为状态1
     *
     * @return 是否为状态1
     */
    public boolean isStatus1() {
        return this == STATUS_1;
    }
}
```

#### 4. 使用规范

```java
// ✅ 正确：使用枚举
user.setStatus(EnableStatus.ENABLED.getCode());
entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
event.setStatus(LoginStatus.SUCCESS.getCode());

// 查询时使用枚举
.eq(SysUser::getStatus, EnableStatus.ENABLED.getCode())
.eq(Xxx::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())

// ❌ 错误：硬编码
user.setStatus(1);
entity.setIsDeleted(0);
event.setStatus(1);
```

#### 5. 枚举必备方法

每个枚举必须实现以下方法：

- `fromCode(Integer code)` - 根据代码值获取枚举
- `isXxx()` - 判断是否为某个状态（根据业务需要）

可选方法：

- `reverse()` - 获取相反状态（适用于 0/1 二值枚举）
- `getNextStatus()` - 获取下一个状态（适用于流程状态）

---

### 统一返回结果（Result）

#### 1. 结果类说明

| 类名              | 用途               | 位置                           |
| ----------------- | ------------------ | ------------------------------ |
| **Result<T>**     | 统一返回结果包装类 | `infrastructure.shared.result` |
| **ResultCode**    | 统一结果码枚举     | `infrastructure.shared.result` |
| **PageResult<T>** | 分页结果类         | `infrastructure.shared.result` |

#### 2. ResultCode 错误码分段

| 错误码范围      | 类型         | 说明                               |
| --------------- | ------------ | ---------------------------------- |
| **200-599**     | HTTP 状态码  | 系统级错误                         |
| **1000-1999**   | 业务通用错误 | 数据验证、操作失败等               |
| **2000-2199**   | 用户相关     | 2000-2099 错误码，2100-2199 成功码 |
| **3000-3199**   | 项目相关     | 3000-3099 错误码，3100-3199 成功码 |
| **4000-4199**   | 申报相关     | 4000-4099 错误码，4100-4199 成功码 |
| **5000-5199**   | 报告相关     | 5000-5099 错误码，5100-5199 成功码 |
| **6000-6199**   | 知识库相关   | 6000-6099 错误码，6100-6199 成功码 |
| **7000-7199**   | 对话相关     | 7000-7099 错误码，7100-7199 成功码 |
| **8000-8199**   | 文件相关     | 8000-8099 错误码，8100-8199 成功码 |
| **9000-9999**   | 验证相关     | 验证码、令牌等                     |
| **10000-10999** | 外部服务     | 外部服务调用                       |
| **11000-11999** | 缓存相关     | 缓存操作                           |
| **12000-12999** | 数据库相关   | 数据库操作                         |

#### 3. 新增错误码规范

```java
// ==================== Xxx相关错误码 (X000-X999) ====================
/**
 * Xxx不存在
 */
XXX_NOT_FOUND(X001, "Xxx不存在"),

/**
 * Xxx已存在
 */
XXX_ALREADY_EXISTS(X002, "Xxx已存在"),

/**
 * Xxx创建成功
 */
XXX_CREATE_SUCCESS(X100, "Xxx创建成功"),
```

#### 4. 使用规范

```java
// ✅ Controller 返回 Result<T>
@PostMapping
public Result<UserResp> createUser(@Valid @RequestBody UserCreateReq req) {
    UserResp resp = userService.create(req);
    return Result.success(resp);
}

// ✅ Service 抛出 BusinessException（使用 ResultCode）
if (user == null) {
    throw new BusinessException(ResultCode.USER_NOT_FOUND);
}

// ✅ 带参数的异常
throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在: " + userId);

// ❌ 错误：Service 返回 Result
public Result<UserResp> create(UserCreateReq req) {  // 错误
    return Result.success(resp);
}

// ❌ 错误：硬编码消息
throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
```

#### 5. Result 使用场景

- **Controller 层**：统一返回 `Result<T>`
- **Service 层**：返回领域对象，抛出 `BusinessException(ResultCode.X)`
- **前端调用**：通过 `code` 判断成功/失败，通过 `message` 显示提示信息

---

### 通用组件使用原则

#### 1. 优先级原则

常量 > 枚举 > 硬编码

```java
// ✅ 最佳：使用枚举（类型安全 + 业务语义）
user.setStatus(EnableStatus.ENABLED.getCode());

// ⚠️ 可接受：使用常量（有语义，但无类型检查）
public static final Integer ENABLED = 1;
user.setStatus(ENABLED);

// ❌ 禁止：硬编码（无语义，难维护）
user.setStatus(1);
```

#### 2. 何时创建新枚举

- 数据库字段有固定的几个状态值
- 状态值有明确的业务含义
- 状态值在多处使用
- 需要状态判断和转换逻辑

#### 3. 何时使用常量

- 配置值（如超时时间、重试次数）
- 格式化模板（如日期格式、Redis Key 模板）
- 系统级固定值（如默认分页大小）

#### 4. 何时定义新的 ResultCode

- 新增业务模块
- 新增特定的成功/失败场景
- 需要前端特殊处理的错误
