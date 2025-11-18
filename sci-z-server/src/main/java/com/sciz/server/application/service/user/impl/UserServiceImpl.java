package com.sciz.server.application.service.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.user.UserService;
import com.sciz.server.domain.pojo.dto.request.user.UserAdminResetPasswordReq;
import com.sciz.server.domain.pojo.dto.request.user.UserCreateReq;
import com.sciz.server.domain.pojo.dto.request.user.UserListQueryReq;
import com.sciz.server.domain.pojo.dto.request.user.UserUpdateReq;
import com.sciz.server.domain.pojo.dto.response.user.UserCreateResp;
import com.sciz.server.domain.pojo.dto.response.user.UserListResp;
import com.sciz.server.domain.pojo.dto.response.user.UserUpdateResp;
import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.enums.UserStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.enums.OperationLogRecorderStatus;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import com.sciz.server.infrastructure.shared.utils.OperationLogRecorderUtil;
import com.sciz.server.interfaces.converter.UserConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户应用服务实现类
 *
 * @author JiaWen.Wu
 * @className UserServiceImpl
 * @date 2025-10-29 10:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserRepo sysUserRepo;
    private final SysDepartmentRepo sysDepartmentRepo;
    private final SysRoleRepo sysRoleRepo;
    private final UserRoleService userRoleService;
    private final IndustryConfigCache industryConfigCache;
    private final UserConverter userConverter;
    private final OperationLogRecorderUtil operationLogRecorderUtil;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public Object getUser(Long userId) {
        // 实现获取用户信息的逻辑
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 创建用户：规格化入参 → 校验账号唯一性 → 校验部门有效性 → 构建实体并入库 → 返回创建结果
     *
     * @param req UserCreateReq 创建请求
     * @return UserCreateResp 创建响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCreateResp create(UserCreateReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.USER_CREATE;
        var operation = operationType.getCode();
        log.info(String.format("%s开始: username=%s", operation, req.username()));

        try {
            // 1. 规格化并提取入参
            var username = req.username().trim().toLowerCase(Locale.ROOT);
            var realName = req.realName().trim();
            var email = req.email().trim().toLowerCase(Locale.ROOT);
            var phone = req.phone().trim();
            var industryType = Optional.ofNullable(industryConfigCache.get())
                    .map(IndustryConfigCache.IndustryView::getType)
                    .map(type -> type.trim().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new BusinessException(ResultCode.SERVER_ERROR, "行业配置未初始化"));

            // 2. 校验账号唯一性
            ensureAccountUniqueness(username, email, phone);

            // 3. 根据部门编码查询部门ID并校验部门有效性
            var department = validateDepartmentByCode(req.departmentCode(), industryType);
            var departmentId = department.getId();

            // 4. 构建用户实体
            var user = buildUserEntity(username, realName, email, phone, req.password(), industryType, departmentId);

            // 5. 保存用户
            var userId = Optional.ofNullable(sysUserRepo.save(user))
                    .orElseThrow(() -> {
                        log.error(String.format("%s失败，保存数据库失败: username=%s", operation, username));
                        return new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
                    });

            // 6. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            // 使用枚举获取操作描述，格式：已创建用户：testuser（邮箱：test@example.com）
            var detail = String.format("%s：%s（邮箱：%s）", operationType.getDescription(), username, email);
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("%s成功: userId=%s, username=%s, employeeId=%s", operation, userId, username,
                    user.getEmployeeId()));

            // 7. 转换为响应
            return userConverter.toUserCreateResp(user);
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e instanceof BusinessException ? e.getMessage() : e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败: username=%s", operation, req.username()),
                    errorMessage, executionTime);
            throw e;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 校验账号唯一性
     *
     * @param username String 用户名
     * @param email    String 邮箱
     * @param phone    String 手机号
     */
    private void ensureAccountUniqueness(String username, String email, String phone) {
        Optional.ofNullable(sysUserRepo.findByUsername(username))
                .ifPresent(existing -> {
                    log.warn(String.format("创建用户失败，用户名已存在: username=%s", username));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "用户名已存在");
                });
        Optional.ofNullable(sysUserRepo.findByEmail(email))
                .ifPresent(existing -> {
                    log.warn(String.format("创建用户失败，邮箱已被注册: email=%s", email));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "邮箱已被注册");
                });
        Optional.ofNullable(sysUserRepo.findByPhone(phone))
                .ifPresent(existing -> {
                    log.warn(String.format("创建用户失败，手机号已被注册: phone=%s", phone));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "手机号已被注册");
                });
    }

    /**
     * 校验部门有效性（根据部门ID）
     *
     * @param departmentId Long 部门ID
     * @param industryType String 行业类型
     * @return SysDepartment 部门实体
     */
    private SysDepartment validateDepartment(Long departmentId, String industryType) {
        var department = Optional.ofNullable(sysDepartmentRepo.findById(departmentId))
                .orElseThrow(() -> {
                    log.warn(String.format("部门不存在: departmentId=%s", departmentId));
                    return new BusinessException(ResultCode.BAD_REQUEST, "部门不存在");
                });

        if (!industryType.equals(department.getIndustryType())) {
            log.warn(String.format("部门行业不匹配: departmentId=%s, departmentIndustry=%s, reqIndustry=%s",
                    departmentId, department.getIndustryType(), industryType));
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门行业类型不匹配");
        }

        if (!EnableStatus.ENABLED.getCode().equals(department.getStatus())) {
            log.warn(String.format("部门已禁用: departmentId=%s", departmentId));
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门已禁用");
        }

        if (DeleteStatus.DELETED.getCode().equals(department.getIsDeleted())) {
            log.warn(String.format("部门已删除: departmentId=%s", departmentId));
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门已删除");
        }

        return department;
    }

    /**
     * 根据部门编码校验部门有效性
     *
     * @param departmentCode String 部门编码
     * @param industryType   String 行业类型
     * @return SysDepartment 部门实体
     */
    private SysDepartment validateDepartmentByCode(String departmentCode, String industryType) {
        var department = Optional.ofNullable(sysDepartmentRepo.findByCode(industryType, departmentCode))
                .orElseThrow(() -> {
                    log.warn(String.format("部门不存在: departmentCode=%s, industryType=%s", departmentCode, industryType));
                    return new BusinessException(ResultCode.BAD_REQUEST, "部门不存在");
                });

        if (!EnableStatus.ENABLED.getCode().equals(department.getStatus())) {
            log.warn(String.format("部门已禁用: departmentCode=%s", departmentCode));
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门已禁用");
        }

        if (DeleteStatus.DELETED.getCode().equals(department.getIsDeleted())) {
            log.warn(String.format("部门已删除: departmentCode=%s", departmentCode));
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门已删除");
        }

        return department;
    }

    /**
     * 构建用户实体
     *
     * @param username     String 用户名
     * @param realName     String 真实姓名
     * @param email        String 邮箱
     * @param phone        String 手机号
     * @param rawPassword  String 明文密码
     * @param industryType String 行业类型
     * @param departmentId Long 部门ID
     * @return SysUser 用户实体
     */
    private SysUser buildUserEntity(String username, String realName, String email, String phone,
            String rawPassword, String industryType, Long departmentId) {
        var user = new SysUser();
        var now = LocalDateTime.now(DEFAULT_ZONE);
        user.setUsername(username);
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(hashPassword(rawPassword));
        user.setIndustryType(industryType);
        user.setDepartmentId(departmentId);
        user.setEmployeeId(generateEmployeeId(industryType, username));
        user.setStatus(EnableStatus.ENABLED.getCode());
        user.setLoginCount(0);
        user.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        user.setCreatedTime(now);
        user.setUpdatedTime(now);
        return user;
    }

    /**
     * 生成行业内唯一的员工ID
     *
     * @param industryType String 行业类型
     * @param username     String 用户名
     * @return String 员工ID
     */
    private String generateEmployeeId(String industryType, String username) {
        var normalizedIndustry = Optional.ofNullable(industryType)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .orElse("GLOBAL");
        var normalizedUsername = username.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
        if (normalizedUsername.length() > 16) {
            normalizedUsername = normalizedUsername.substring(0, 16);
        }
        return String.format("%s-%s-%s", normalizedIndustry, normalizedUsername,
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
    }

    /**
     * 分页查询用户列表：构建分页对象 → 调用仓储查询 → 填充部门信息 → 转换为响应
     *
     * @param req UserListQueryReq 查询请求
     * @return PageResult<UserListResp> 分页结果
     */
    @Override
    public PageResult<UserListResp> page(UserListQueryReq req) {
        log.debug(String.format("查询用户列表: keyword=%s, roleId=%s, status=%s", req.keyword(), req.roleId(), req.status()));

        // 1. 构建分页对象
        var baseQuery = req.toBaseQuery();
        var page = new Page<SysUser>(baseQuery.pageNo(), baseQuery.pageSize());

        // 2. 确定排序方式
        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("createdTime");

        // 3. 调用仓储查询
        IPage<SysUser> userPage = sysUserRepo.page(page, req.keyword(), req.roleId(), req.status(), sortBy, asc);

        // 4. 获取行业配置（用于获取部门标签）
        var industryView = industryConfigCache.get();
        var departmentLabel = Optional.ofNullable(industryView.getDepartmentLabel()).orElse("部门");
        var industryType = industryView.getType();

        // 5. 批量查询部门信息（用于填充部门名称）
        var departmentIds = userPage.getRecords().stream()
                .map(SysUser::getDepartmentId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> departmentNameMap = new HashMap<>();
        if (!departmentIds.isEmpty()) {
            departmentIds.forEach(departmentId -> {
                Optional.ofNullable(sysDepartmentRepo.findById(departmentId))
                        .ifPresent(department -> departmentNameMap.put(departmentId, department.getDepartmentName()));
            });
        }

        // 6. 批量查询用户角色信息（用于填充角色名称列表，遵循RBAC标准规则）
        Map<Long, List<String>> roleNamesMap = new HashMap<>();
        var userIds = userPage.getRecords().stream()
                .map(SysUser::getId)
                .toList();

        if (!userIds.isEmpty()) {
            // 批量查询所有用户的角色关联（优化：一次查询所有用户角色关系）
            Map<Long, List<Long>> userRoleIdsMap = new HashMap<>();
            userIds.forEach(userId -> {
                // 查询用户在当前行业下的所有角色ID（已按RBAC规则过滤）
                var roleIds = userRoleService.listUserRoleIds(userId, industryType);
                log.debug(String.format("查询用户角色ID: userId=%s, roleIds=%s", userId, roleIds));
                if (!roleIds.isEmpty()) {
                    userRoleIdsMap.put(userId, roleIds);
                }
            });

            // 批量查询所有角色实体（优化：一次查询所有角色）
            if (!userRoleIdsMap.isEmpty()) {
                var allRoleIds = userRoleIdsMap.values().stream()
                        .flatMap(List::stream)
                        .distinct()
                        .toList();

                if (!allRoleIds.isEmpty()) {
                    var allRoles = sysRoleRepo.findByIds(allRoleIds);
                    // 构建角色ID到角色名称的映射（过滤行业类型）
                    Map<Long, String> roleIdToNameMap = allRoles.stream()
                            .filter(role -> Optional.ofNullable(industryType)
                                    .map(type -> type.equals(role.getIndustryType()))
                                    .orElse(true))
                            .filter(role -> Optional.ofNullable(role.getIsDeleted())
                                    .map(DeleteStatus.DELETED.getCode()::equals)
                                    .map(isDeleted -> !isDeleted)
                                    .orElse(true))
                            .collect(Collectors.toMap(
                                    SysRole::getId,
                                    SysRole::getRoleName,
                                    (existing, replacement) -> existing)); // 如果有重复的ID，保留第一个

                    // 为每个用户构建角色名称列表
                    userRoleIdsMap.forEach((userId, roleIds) -> {
                        var roleNames = roleIds.stream()
                                .map(roleIdToNameMap::get)
                                .filter(java.util.Objects::nonNull)
                                .distinct()
                                .toList();
                        log.debug(String.format("构建用户角色名称列表: userId=%s, roleIds=%s, roleNames=%s", userId, roleIds,
                                roleNames));
                        if (!roleNames.isEmpty()) {
                            roleNamesMap.put(userId, roleNames);
                        }
                    });
                }
            }
        }

        // 7. 转换为响应
        var records = userPage.getRecords().stream()
                .map(user -> new UserListResp(
                        user.getId(),
                        user.getUsername(),
                        user.getRealName(),
                        user.getEmployeeId(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getDepartmentId(),
                        Optional.ofNullable(user.getDepartmentId())
                                .map(departmentNameMap::get)
                                .orElse(null),
                        departmentLabel,
                        Optional.ofNullable(roleNamesMap.get(user.getId()))
                                .orElseGet(List::of),
                        user.getStatus(),
                        user.getCreatedTime()))
                .toList();

        Page<UserListResp> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(userPage.getTotal());

        log.debug(String.format("查询用户列表完成: total=%d, current=%d, size=%d", resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize()));

        return PageResult.of(resultPage);
    }

    /**
     * 更新用户：查询并校验用户 → 校验邮箱/手机号唯一性 → 校验部门有效性 → 更新用户信息 → 返回更新结果
     *
     * @param req UserUpdateReq 更新请求
     * @return UserUpdateResp 更新响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserUpdateResp update(UserUpdateReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.USER_UPDATE;
        var operation = operationType.getCode();
        log.info(String.format("%s开始: userId=%s", operation, req.id()));

        try {
            // 1. 查询并校验用户
            var user = Optional.ofNullable(sysUserRepo.findById(req.id()))
                    .orElseThrow(() -> {
                        log.warn(String.format("%s失败，用户不存在: userId=%s", operation, req.id()));
                        return new BusinessException(ResultCode.USER_NOT_FOUND);
                    });

            // 2. 规格化入参
            var realName = req.realName().trim();
            var email = req.email().trim().toLowerCase(Locale.ROOT);
            var phone = req.phone().trim();
            var industryType = Optional.ofNullable(industryConfigCache.get())
                    .map(IndustryConfigCache.IndustryView::getType)
                    .map(type -> type.trim().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new BusinessException(ResultCode.SERVER_ERROR, "行业配置未初始化"));
            var departmentCode = req.departmentCode();

            // 3. 校验邮箱/手机号唯一性（排除当前用户）
            ensureAccountUniquenessForUpdate(req.id(), email, phone);

            // 4. 根据部门编码查询部门ID并校验部门有效性
            var department = validateDepartmentByCode(departmentCode, industryType);
            var departmentId = department.getId();

            // 5. 更新用户信息
            user.setRealName(realName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setDepartmentId(departmentId);
            user.setIndustryType(industryType);
            user.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));

            if (!sysUserRepo.updateById(user)) {
                log.error(String.format("%s失败，保存数据库失败: userId=%s", operation, req.id()));
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
            }

            // 6. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            // 使用枚举获取操作描述，格式：已更新用户：testuser（ID: 5）
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), user.getUsername(), req.id());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("%s成功: userId=%s", operation, req.id()));

            // 7. 转换为响应
            return new UserUpdateResp(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getEmployeeId(),
                    user.getDepartmentId(),
                    user.getIndustryType());
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e instanceof BusinessException ? e.getMessage() : e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation, String.format("%s失败: userId=%s", operation, req.id()),
                    errorMessage, executionTime);
            throw e;
        }
    }

    /**
     * 删除用户：查询并校验用户 → 软删除用户
     *
     * @param userId Long 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long userId) {
        log.info(String.format("删除用户开始: userId=%s", userId));

        // 1. 查询并校验用户
        var user = Optional.ofNullable(sysUserRepo.findById(userId))
                .orElseThrow(() -> {
                    log.warn(String.format("删除用户失败，用户不存在: userId=%s", userId));
                    return new BusinessException(ResultCode.USER_NOT_FOUND);
                });

        // 2. 软删除用户
        user.setIsDeleted(DeleteStatus.DELETED.getCode());
        user.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));

        if (!sysUserRepo.updateById(user)) {
            log.error(String.format("删除用户失败，保存数据库失败: userId=%s", userId));
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("删除用户成功: userId=%s", userId));
    }

    /**
     * 禁用/启用用户：查询并校验用户 → 更新用户状态 → 记录操作日志
     *
     * @param userId   Long 用户ID
     * @param disabled boolean true=禁用，false=启用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableById(Long userId, boolean disabled) {
        var startTime = DateUtil.now();
        var operationType = disabled ? OperationLogRecorderStatus.USER_DISABLE : OperationLogRecorderStatus.USER_ENABLE;
        var operation = operationType.getCode();
        log.info(String.format("%s开始: userId=%s", operation, userId));

        try {
            // 1. 查询并校验用户
            var user = Optional.ofNullable(sysUserRepo.findById(userId))
                    .orElseThrow(() -> {
                        log.warn(String.format("%s失败，用户不存在: userId=%s", operation, userId));
                        return new BusinessException(ResultCode.USER_NOT_FOUND);
                    });

            // 2. 更新用户状态
            var newStatus = disabled ? UserStatus.DISABLED.getCode() : UserStatus.NORMAL.getCode();
            user.setStatus(newStatus);
            user.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));

            if (!sysUserRepo.updateById(user)) {
                log.error(String.format("%s失败，保存数据库失败: userId=%s", operation, userId));
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
            }

            // 3. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            // 使用枚举获取操作描述，格式：已禁用用户：wjw（ID: 5）
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), user.getUsername(), userId);
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("%s成功: userId=%s, status=%s", operation, userId, newStatus));
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e instanceof BusinessException ? e.getMessage() : e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation, String.format("%s失败: userId=%s", operation, userId),
                    errorMessage, executionTime);
            throw e;
        }
    }

    /**
     * 管理员重置用户密码：查询并校验用户 → 更新密码
     *
     * @param req UserAdminResetPasswordReq 重置密码请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminResetPassword(UserAdminResetPasswordReq req) {
        log.info(String.format("管理员重置用户密码开始: userId=%s", req.userId()));

        // 1. 查询并校验用户
        var user = Optional.ofNullable(sysUserRepo.findById(req.userId()))
                .orElseThrow(() -> {
                    log.warn(String.format("管理员重置密码失败，用户不存在: userId=%s", req.userId()));
                    return new BusinessException(ResultCode.USER_NOT_FOUND);
                });

        // 2. 更新密码
        user.setPassword(hashPassword(req.newPassword()));
        user.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));

        if (!sysUserRepo.updateById(user)) {
            log.error(String.format("管理员重置密码失败，保存数据库失败: userId=%s", req.userId()));
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("管理员重置用户密码成功: userId=%s", req.userId()));
    }

    /**
     * 校验账号唯一性（更新场景，排除当前用户）
     *
     * @param userId Long 当前用户ID
     * @param email  String 邮箱
     * @param phone  String 手机号
     */
    private void ensureAccountUniquenessForUpdate(Long userId, String email, String phone) {
        Optional.ofNullable(sysUserRepo.findByEmail(email))
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    log.warn(String.format("更新用户失败，邮箱已被其他用户注册: userId=%s, email=%s", userId, email));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "邮箱已被其他用户注册");
                });
        Optional.ofNullable(sysUserRepo.findByPhone(phone))
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    log.warn(String.format("更新用户失败，手机号已被其他用户注册: userId=%s, phone=%s", userId, phone));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "手机号已被其他用户注册");
                });
    }

    /**
     * 密码哈希
     *
     * @param rawPassword String 明文密码
     * @return String bcrypt 哈希
     */
    private String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
}
