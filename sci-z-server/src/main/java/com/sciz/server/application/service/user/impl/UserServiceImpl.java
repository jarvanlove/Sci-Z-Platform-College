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
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.enums.UserStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.interfaces.converter.UserConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        // 1. 规格化并提取入参
        var username = req.username().trim();
        var realName = req.realName().trim();
        var email = req.email().trim().toLowerCase(Locale.ROOT);
        var phone = req.phone().trim();
        var industryType = req.industryType().trim().toLowerCase(Locale.ROOT);
        var departmentId = req.departmentId();

        log.info(String.format("创建用户开始: username=%s, email=%s, phone=%s, departmentId=%s, industryType=%s",
                username, email, phone, departmentId, industryType));

        // 2. 校验账号唯一性
        ensureAccountUniqueness(username, email, phone);

        // 3. 校验部门有效性
        validateDepartment(departmentId, industryType);

        // 4. 构建用户实体
        var user = buildUserEntity(username, realName, email, phone, req.password(), industryType, departmentId);

        // 5. 保存用户
        var userId = Optional.ofNullable(sysUserRepo.save(user))
                .orElseThrow(() -> {
                    log.error(String.format("创建用户失败，保存数据库失败: username=%s", username));
                    return new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
                });

        log.info(
                String.format("创建用户成功: userId=%s, username=%s, employeeId=%s", userId, username, user.getEmployeeId()));

        // 6. 转换为响应
        return userConverter.toUserCreateResp(user);
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
     * 校验部门有效性
     *
     * @param departmentId Long 部门ID
     * @param industryType String 行业类型
     * @return SysDepartment 部门实体
     */
    private SysDepartment validateDepartment(Long departmentId, String industryType) {
        var department = Optional.ofNullable(sysDepartmentRepo.findById(departmentId))
                .orElseThrow(() -> {
                    log.warn(String.format("创建用户失败，部门不存在: departmentId=%s", departmentId));
                    return new BusinessException(ResultCode.BAD_REQUEST, "部门不存在");
                });

        if (!industryType.equals(department.getIndustryType())) {
            log.warn(String.format("创建用户失败，部门行业不匹配: departmentId=%s, departmentIndustry=%s, reqIndustry=%s",
                    departmentId, department.getIndustryType(), industryType));
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门行业类型不匹配");
        }

        if (!EnableStatus.ENABLED.getCode().equals(department.getStatus())) {
            log.warn(String.format("创建用户失败，部门已禁用: departmentId=%s", departmentId));
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门已禁用");
        }

        if (DeleteStatus.DELETED.getCode().equals(department.getIsDeleted())) {
            log.warn(String.format("创建用户失败，部门已删除: departmentId=%s", departmentId));
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

        // 6. 批量查询用户角色信息（用于填充角色名称）
        Map<Long, String> roleNameMap = new HashMap<>();
        var userIds = userPage.getRecords().stream()
                .map(SysUser::getId)
                .toList();

        if (!userIds.isEmpty()) {
            userIds.forEach(userId -> {
                var roleIds = userRoleService.listUserRoleIds(userId, industryType);
                if (!roleIds.isEmpty()) {
                    var roles = sysRoleRepo.findByIds(roleIds);
                    var primaryRole = roles.stream()
                            .filter(role -> Optional.ofNullable(role.getIsDeleted())
                                    .map(DeleteStatus.DELETED.getCode()::equals)
                                    .map(isDeleted -> !isDeleted)
                                    .orElse(true))
                            .filter(role -> Optional.ofNullable(industryType)
                                    .map(type -> type.equals(role.getIndustryType()))
                                    .orElse(true))
                            .findFirst();
                    primaryRole.ifPresent(role -> roleNameMap.put(userId, role.getRoleName()));
                }
            });
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
                        roleNameMap.get(user.getId()),
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
        // 1. 查询并校验用户
        var user = Optional.ofNullable(sysUserRepo.findById(req.id()))
                .orElseThrow(() -> {
                    log.warn(String.format("更新用户失败，用户不存在: userId=%s", req.id()));
                    return new BusinessException(ResultCode.USER_NOT_FOUND);
                });

        // 2. 规格化入参
        var realName = req.realName().trim();
        var email = req.email().trim().toLowerCase(Locale.ROOT);
        var phone = req.phone().trim();
        var industryType = req.industryType().trim().toLowerCase(Locale.ROOT);
        var departmentId = req.departmentId();

        log.info(String.format("更新用户开始: userId=%s, email=%s, phone=%s, departmentId=%s, industryType=%s",
                req.id(), email, phone, departmentId, industryType));

        // 3. 校验邮箱/手机号唯一性（排除当前用户）
        ensureAccountUniquenessForUpdate(req.id(), email, phone);

        // 4. 校验部门有效性
        validateDepartment(departmentId, industryType);

        // 5. 更新用户信息
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setDepartmentId(departmentId);
        user.setIndustryType(industryType);
        user.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));

        if (!sysUserRepo.updateById(user)) {
            log.error(String.format("更新用户失败，保存数据库失败: userId=%s", req.id()));
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("更新用户成功: userId=%s", req.id()));

        // 6. 转换为响应
        return new UserUpdateResp(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getEmployeeId(),
                user.getDepartmentId(),
                user.getIndustryType());
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
     * 禁用/启用用户：查询并校验用户 → 更新用户状态
     *
     * @param userId   Long 用户ID
     * @param disabled boolean true=禁用，false=启用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableById(Long userId, boolean disabled) {
        log.info(String.format("%s用户开始: userId=%s", disabled ? "禁用" : "启用", userId));

        // 1. 查询并校验用户
        var user = Optional.ofNullable(sysUserRepo.findById(userId))
                .orElseThrow(() -> {
                    log.warn(String.format("%s用户失败，用户不存在: userId=%s", disabled ? "禁用" : "启用", userId));
                    return new BusinessException(ResultCode.USER_NOT_FOUND);
                });

        // 2. 更新用户状态
        var newStatus = disabled ? UserStatus.DISABLED.getCode() : UserStatus.NORMAL.getCode();
        user.setStatus(newStatus);
        user.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));

        if (!sysUserRepo.updateById(user)) {
            log.error(String.format("%s用户失败，保存数据库失败: userId=%s", disabled ? "禁用" : "启用", userId));
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("%s用户成功: userId=%s, status=%s", disabled ? "禁用" : "启用", userId, newStatus));
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
