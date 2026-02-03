# employee_id 和行业标签字段说明

## 一、sys_industry_config 表中3个标签字段的含义

### 1. 字段说明

`sys_industry_config` 表中的 `department_label`、`role_label`、`employee_id_label` 这3个字段是**前端展示标签**，用于在不同行业下显示不同的术语。

| 字段 | 含义 | 示例值（教育行业） | 示例值（医疗行业） | 示例值（电力行业） |
|------|------|-------------------|-------------------|-------------------|
| `department_label` | 部门标签 | "院系" | "科室" | "部门" |
| `role_label` | 角色标签 | "角色" | "职务" | "岗位" |
| `employee_id_label` | 员工ID标签 | "学工号" | "工号" | "员工号" |

### 2. 与 sys_user 表的关系

**重要：这些字段不是直接关联字段，而是用于前端展示的元数据配置。**

#### 2.1 关系说明

- **`employee_id_label`** ↔ **`sys_user.employee_id`**
  - `employee_id_label` 定义了在创建/编辑用户表单中，`employee_id` 字段应该显示的**标签名称**
  - 例如：教育行业显示"学工号"，医疗行业显示"工号"
  - **实际存储的值**在 `sys_user.employee_id` 字段中（如：`"2024001"`、`"EMP001"` 等）

- **`department_label`** ↔ **`sys_user.department_id`**
  - `department_label` 定义了在用户表单中，部门字段应该显示的**标签名称**
  - 例如：教育行业显示"院系"，医疗行业显示"科室"
  - **实际存储的值**是 `sys_user.department_id`（关联到 `sys_department` 表）

- **`role_label`** ↔ **`sys_user` 通过 `sys_user_role` 关联的角色**
  - `role_label` 定义了在用户表单中，角色字段应该显示的**标签名称**
  - 例如：教育行业显示"角色"，医疗行业显示"职务"

#### 2.2 使用场景

前端创建用户表单时，根据当前行业类型动态显示标签：

```typescript
// 前端伪代码
const industryConfig = await getIndustryConfig(); // 获取当前行业配置

<Form>
  <FormItem label={industryConfig.departmentLabel}>  {/* 显示"院系"或"科室" */}
    <Select name="departmentId" />
  </FormItem>
  
  <FormItem label={industryConfig.employeeIdLabel}>  {/* 显示"学工号"或"工号" */}
    <Input name="employeeId" />
  </FormItem>
  
  <FormItem label={industryConfig.roleLabel}>  {/* 显示"角色"或"职务" */}
    <Select name="roleId" />
  </FormItem>
</Form>
```

## 二、新建用户时 employee_id 的处理方式

### 1. 字段约束说明

- **数据库约束**：`sys_user.employee_id` 字段允许为 `NULL`
- **唯一性约束**：存在唯一索引 `uk_sys_user_employee_industry ON sys_user(employee_id, industry_type)`
  - 这意味着：**同一行业下，employee_id 必须唯一**（如果提供了值）
  - `NULL` 值不参与唯一性约束（多个 `NULL` 值可以共存）

### 2. 处理策略

#### 方案A：必填（推荐）

如果业务要求每个用户必须有唯一的员工ID：

1. **前端表单**：根据行业类型显示对应的标签（如"学工号"），并设置为必填
2. **后端验证**：
   - 验证 `employee_id` 不能为空
   - 验证同一行业下 `employee_id` 的唯一性
3. **提示信息**：根据行业类型动态显示错误提示（如"该学工号已存在"）

#### 方案B：可选

如果业务允许某些用户没有员工ID：

1. **前端表单**：`employee_id` 字段设置为可选
2. **后端处理**：
   - 如果提供了 `employee_id`，验证唯一性
   - 如果不提供，设置为 `NULL`
3. **业务逻辑**：某些功能可能需要员工ID，需要做相应判断

### 3. 代码实现示例

#### 3.1 创建用户DTO（包含 employee_id）

```java
@Data
public class CreateUserReq {
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    
    /**
     * 员工ID（学工号/工号/员工号）
     * 根据行业类型，前端会显示对应的标签名称
     */
    private String employeeId;
    
    /**
     * 所属部门ID
     */
    private Long departmentId;
    
    /**
     * 行业类型
     */
    private String industryType;
    
    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
```

#### 3.2 用户服务实现（包含 employee_id 验证）

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final SysUserRepository userRepository;
    private final SysIndustryConfigRepository industryConfigRepository;
    
    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 创建结果
     */
    @Transactional
    public Result<Long> createUser(CreateUserReq request) {
        // 1. 参数验证
        validateCreateUser(request);
        
        // 2. 验证 employee_id 唯一性（如果提供了值）
        if (StringUtils.hasText(request.getEmployeeId())) {
            validateEmployeeIdUnique(request.getEmployeeId(), request.getIndustryType());
        }
        
        // 3. 创建用户实体
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEmployeeId(StringUtils.hasText(request.getEmployeeId()) 
            ? request.getEmployeeId() : null);  // 允许为NULL
        user.setDepartmentId(request.getDepartmentId());
        user.setIndustryType(request.getIndustryType());
        user.setStatus(1);
        
        // 4. 保存用户
        userRepository.save(user);
        
        // 5. 分配角色
        if (CollectionUtils.isNotEmpty(request.getRoleIds())) {
            assignRoles(user.getId(), request.getRoleIds());
        }
        
        return Result.success(user.getId());
    }
    
    /**
     * 验证 employee_id 唯一性
     *
     * @param employeeId 员工ID
     * @param industryType 行业类型
     */
    private void validateEmployeeIdUnique(String employeeId, String industryType) {
        boolean exists = userRepository.existsByEmployeeIdAndIndustryType(
            employeeId, industryType);
        if (exists) {
            // 获取行业配置，动态显示错误提示
            SysIndustryConfig config = industryConfigRepository
                .findByIndustryType(industryType);
            String label = config != null ? config.getEmployeeIdLabel() : "员工ID";
            throw new BusinessException(
                ResultCode.EMPLOYEE_ID_ALREADY_EXISTS, 
                String.format("该%s已存在", label));
        }
    }
    
    /**
     * 参数验证
     */
    private void validateCreateUser(CreateUserReq request) {
        // 验证用户名、邮箱等...
        // 如果业务要求 employee_id 必填，在这里验证
        // if (!StringUtils.hasText(request.getEmployeeId())) {
        //     throw new BusinessException(ResultCode.VALIDATION_ERROR, "员工ID不能为空");
        // }
    }
}
```

#### 3.3 Repository 方法

```java
public interface SysUserRepository extends BaseMapper<SysUser> {
    
    /**
     * 检查同一行业下 employee_id 是否已存在
     *
     * @param employeeId 员工ID
     * @param industryType 行业类型
     * @return 是否存在
     */
    default boolean existsByEmployeeIdAndIndustryType(String employeeId, String industryType) {
        return selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmployeeId, employeeId)
            .eq(SysUser::getIndustryType, industryType)
            .eq(SysUser::getIsDeleted, 0)
        ) > 0;
    }
}
```

#### 3.4 前端获取行业配置接口

```java
@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    @GetMapping("/industry/config")
    public Result<IndustryConfigVO> getIndustryConfig() {
        // 获取当前行业配置
        SysIndustryConfig config = industryConfigService.getCurrentIndustryConfig();
        
        IndustryConfigVO vo = new IndustryConfigVO();
        vo.setIndustryType(config.getIndustryType());
        vo.setIndustryName(config.getIndustryName());
        vo.setDepartmentLabel(config.getDepartmentLabel());
        vo.setRoleLabel(config.getRoleLabel());
        vo.setEmployeeIdLabel(config.getEmployeeIdLabel());
        
        return Result.success(vo);
    }
}
```

## 三、总结

### 1. sys_industry_config 的3个标签字段

- **作用**：前端展示标签，用于在不同行业下显示不同的术语
- **与 sys_user 的关系**：不是直接关联字段，而是展示层的元数据配置
- **使用方式**：前端根据行业类型动态获取标签，显示在表单中

### 2. employee_id 字段处理

- **数据库约束**：允许 `NULL`，但同一行业下如果提供了值，必须唯一
- **推荐方案**：
  - 如果业务要求必填：前端必填，后端验证唯一性
  - 如果业务允许可选：前端可选，后端如果提供了值则验证唯一性
- **错误提示**：根据行业类型动态显示标签（如"该学工号已存在"）

### 3. 最佳实践

1. **前端**：根据行业配置动态显示标签和验证提示
2. **后端**：验证 `employee_id` 的唯一性时，考虑行业类型
3. **业务逻辑**：根据实际需求决定 `employee_id` 是否必填

