package com.sciz.server.application.service.user.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.application.service.user.UserService;
import com.sciz.server.domain.pojo.dto.request.system.RoleCreateReq;
import com.sciz.server.domain.pojo.dto.request.system.RoleListQueryReq;
import com.sciz.server.domain.pojo.dto.request.system.RoleUpdateReq;
import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
import com.sciz.server.domain.pojo.dto.response.system.RoleResp;
import com.sciz.server.domain.pojo.dto.response.user.RoleListResp;
import com.sciz.server.domain.pojo.dto.response.user.UserListResp;
import com.sciz.server.domain.pojo.dto.request.user.UserListQueryReq;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.enums.OperationLogRecorderStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.infrastructure.shared.utils.OperationLogRecorderUtil;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 用户角色管理服务实现
 *
 * @author JiaWen.Wu
 * @className UserRoleServiceImpl
 * @date 2025-11-09 02:10
 */
@Slf4j
@Service
public class UserRoleServiceImpl implements UserRoleService {

        private final SysUserRoleRepo userRoleRepo;
        private final SysRoleRepo roleRepo;
        private final SysUserRepo sysUserRepo;
        private final PermissionService permissionService;
        private final IndustryConfigCache industryConfigCache;
        private final OperationLogRecorderUtil operationLogRecorderUtil;
        private final UserService userService;

        public UserRoleServiceImpl(
                        SysUserRoleRepo userRoleRepo,
                        SysRoleRepo roleRepo,
                        SysUserRepo sysUserRepo,
                        PermissionService permissionService,
                        IndustryConfigCache industryConfigCache,
                        OperationLogRecorderUtil operationLogRecorderUtil,
                        @Lazy UserService userService) {
                this.userRoleRepo = userRoleRepo;
                this.roleRepo = roleRepo;
                this.sysUserRepo = sysUserRepo;
                this.permissionService = permissionService;
                this.industryConfigCache = industryConfigCache;
                this.operationLogRecorderUtil = operationLogRecorderUtil;
                this.userService = userService;
        }

        private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

        @Override
        @Transactional(rollbackFor = Exception.class)
        public RoleResp create(RoleCreateReq req) {
                var startTime = DateUtil.now();
                var operationType = OperationLogRecorderStatus.ROLE_CREATE;
                var operation = operationType.getCode();
                log.info(String.format("%s开始: roleName=%s, roleCode=%s", operation, req.roleName(), req.roleCode()));

                try {
                        // 1. 获取行业类型
                        var industryType = industryConfigCache.get().getType();

                        // 2. 校验角色编码唯一性
                        var existingRole = roleRepo.findByCode(req.roleCode(), industryType);
                        if (existingRole != null) {
                                log.warn(String.format("%s失败，角色编码已存在: roleCode=%s", operation, req.roleCode()));
                                throw new BusinessException(ResultCode.BAD_REQUEST, "角色编码已存在");
                        }

                        // 3. 获取当前登录用户ID
                        var currentUserId = LoginUserUtil.requireCurrentUserId();

                        // 4. 构建角色实体
                        var role = new SysRole();
                        role.setRoleName(req.roleName());
                        role.setRoleCode(req.roleCode());
                        role.setDescription(req.description());
                        role.setIndustryType(industryType);
                        role.setRoleType("custom");
                        role.setSortOrder(req.sortOrder());
                        role.setStatus(EnableStatus.ENABLED.getCode());
                        role.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
                        role.setCreatedTime(LocalDateTime.now(DEFAULT_ZONE));
                        role.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));
                        role.setCreatedBy(currentUserId);
                        role.setUpdatedBy(currentUserId);

                        // 5. 保存角色
                        var roleId = Optional.ofNullable(roleRepo.save(role))
                                        .orElseThrow(() -> {
                                                log.error(String.format("%s失败，保存数据库失败: roleCode=%s", operation,
                                                                req.roleCode()));
                                                return new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
                                        });

                        // 5. 记录操作日志（成功）
                        var endTime = DateUtil.now();
                        var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
                        var detail = String.format("%s：%s（编码：%s）", operationType.getDescription(), req.roleName(),
                                        req.roleCode());
                        operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

                        log.info(String.format("%s成功: roleId=%s, roleName=%s, roleCode=%s", operation, roleId,
                                        req.roleName(), req.roleCode()));

                        // 7. 转换为响应
                        return new RoleResp(
                                        role.getId(),
                                        role.getRoleName(),
                                        role.getRoleCode(),
                                        role.getRoleType(),
                                        role.getDescription(),
                                        role.getStatus(),
                                        role.getSortOrder(),
                                        role.getCreatedTime(),
                                        role.getUpdatedTime());
                } catch (Exception e) {
                        // 记录操作日志（失败）
                        var endTime = DateUtil.now();
                        var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
                        var errorMessage = e instanceof BusinessException ? e.getMessage()
                                        : e.getClass().getSimpleName();
                        operationLogRecorderUtil.recordFailure(operation,
                                        String.format("%s失败: roleCode=%s", operation, req.roleCode()),
                                        errorMessage, executionTime);
                        throw e;
                }
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public RoleResp update(RoleUpdateReq req) {
                var startTime = DateUtil.now();
                var operationType = OperationLogRecorderStatus.ROLE_UPDATE;
                var operation = operationType.getCode();
                log.info(String.format("%s开始: roleId=%s", operation, req.id()));

                try {
                        // 1. 查询并校验角色
                        var role = Optional.ofNullable(roleRepo.findById(req.id()))
                                        .orElseThrow(() -> {
                                                log.warn(String.format("%s失败，角色不存在: roleId=%s", operation, req.id()));
                                                return new BusinessException(ResultCode.BAD_REQUEST, "角色不存在");
                                        });

                        // 2. 获取当前登录用户ID
                        var currentUserId = LoginUserUtil.requireCurrentUserId();

                        // 3. 更新角色信息
                        role.setRoleName(req.roleName());
                        role.setDescription(req.description());
                        role.setSortOrder(req.sortOrder());
                        if (req.status() != null) {
                                role.setStatus(req.status());
                        }
                        role.setUpdatedTime(LocalDateTime.now(DEFAULT_ZONE));
                        role.setUpdatedBy(currentUserId);

                        if (!roleRepo.updateById(role)) {
                                log.error(String.format("%s失败，保存数据库失败: roleId=%s", operation, req.id()));
                                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
                        }

                        // 4. 记录操作日志（成功）
                        var endTime = DateUtil.now();
                        var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
                        var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), role.getRoleName(),
                                        req.id());
                        operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

                        log.info(String.format("%s成功: roleId=%s", operation, req.id()));

                        // 5. 转换为响应
                        return new RoleResp(
                                        role.getId(),
                                        role.getRoleName(),
                                        role.getRoleCode(),
                                        role.getRoleType(),
                                        role.getDescription(),
                                        role.getStatus(),
                                        role.getSortOrder(),
                                        role.getCreatedTime(),
                                        role.getUpdatedTime());
                } catch (Exception e) {
                        // 记录操作日志（失败）
                        var endTime = DateUtil.now();
                        var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
                        var errorMessage = e instanceof BusinessException ? e.getMessage()
                                        : e.getClass().getSimpleName();
                        operationLogRecorderUtil.recordFailure(operation,
                                        String.format("%s失败: roleId=%s", operation, req.id()),
                                        errorMessage, executionTime);
                        throw e;
                }
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void deleteById(Long id) {
                var startTime = DateUtil.now();
                var operationType = OperationLogRecorderStatus.ROLE_DELETE;
                var operation = operationType.getCode();
                log.info(String.format("%s开始: roleId=%s", operation, id));

                try {
                        // 1. 查询并校验角色
                        var role = Optional.ofNullable(roleRepo.findById(id))
                                        .orElseThrow(() -> {
                                                log.warn(String.format("%s失败，角色不存在: roleId=%s", operation, id));
                                                return new BusinessException(ResultCode.BAD_REQUEST, "角色不存在");
                                        });

                        // 2. 检查是否为系统角色（系统角色不允许删除）
                        if ("system".equals(role.getRoleType())) {
                                log.warn(String.format("%s失败，系统角色不允许删除: roleId=%s", operation, id));
                                throw new BusinessException(ResultCode.BAD_REQUEST, "系统角色不允许删除");
                        }

                        // 3. 检查是否有用户绑定此角色
                        var userRoles = userRoleRepo.findNotDeletedByRoleId(id);
                        if (!CollectionUtils.isEmpty(userRoles)) {
                                log.warn(String.format("%s失败，角色已被用户使用: roleId=%s, userCount=%d", operation, id,
                                                userRoles.size()));
                                throw new BusinessException(ResultCode.BAD_REQUEST, "角色已被用户使用，无法删除");
                        }

                        // 4. 软删除角色
                        if (!roleRepo.deleteById(id)) {
                                log.error(String.format("%s失败，保存数据库失败: roleId=%s", operation, id));
                                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
                        }

                        // 5. 记录操作日志（成功）
                        var endTime = DateUtil.now();
                        var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
                        var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), role.getRoleName(),
                                        id);
                        operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

                        log.info(String.format("%s成功: roleId=%s", operation, id));
                } catch (Exception e) {
                        // 记录操作日志（失败）
                        var endTime = DateUtil.now();
                        var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
                        var errorMessage = e instanceof BusinessException ? e.getMessage()
                                        : e.getClass().getSimpleName();
                        operationLogRecorderUtil.recordFailure(operation,
                                        String.format("%s失败: roleId=%s", operation, id),
                                        errorMessage, executionTime);
                        throw e;
                }
        }

        @Override
        public List<RoleListResp> listRoles() {
                var industryType = industryConfigCache.get().getType();
                var roles = Optional.ofNullable(roleRepo.listByIndustryType(industryType))
                                .filter(roleList -> !CollectionUtils.isEmpty(roleList))
                                .map(roleList -> roleList.stream()
                                                .filter(role -> Optional.ofNullable(role.getIsDeleted())
                                                                .map(DeleteStatus.DELETED.getCode()::equals)
                                                                .map(isDeleted -> !isDeleted)
                                                                .orElse(true))
                                                .sorted(Comparator
                                                                .comparing(SysRole::getSortOrder,
                                                                                Comparator.nullsLast(
                                                                                                Integer::compareTo))
                                                                .thenComparing(SysRole::getId,
                                                                                Comparator.nullsLast(Long::compareTo)))
                                                .toList())
                                .orElseGet(List::of);
                log.info(String.format("查询角色列表: industryType=%s, size=%s", industryType, roles.size()));

                // 批量统计每个角色的用户数量
                Map<Long, Integer> userCountMap = new HashMap<>();
                if (!roles.isEmpty()) {
                        var roleIds = roles.stream().map(SysRole::getId).toList();
                        roleIds.forEach(roleId -> {
                                var userIds = userRoleRepo.findUserIdsByRoleId(roleId);
                                userCountMap.put(roleId, userIds != null ? userIds.size() : 0);
                        });
                }

                return roles.stream()
                                .map(role -> new RoleListResp(
                                                role.getId(),
                                                role.getRoleName(),
                                                role.getRoleCode(),
                                                role.getRoleType(),
                                                role.getDescription(),
                                                Optional.ofNullable(userCountMap.get(role.getId())).orElse(0),
                                                role.getStatus(),
                                                role.getSortOrder(),
                                                role.getCreatedTime(),
                                                role.getUpdatedTime()))
                                .toList();
        }

        /**
         * 分页查询角色列表：构建分页对象 → 调用仓储查询 → 批量统计用户数量 → 转换为响应
         *
         * @param req RoleListQueryReq 查询请求
         * @return PageResult<RoleListResp> 分页结果
         */
        @Override
        public PageResult<RoleListResp> page(RoleListQueryReq req) {
                log.debug(String.format("分页查询角色列表: keyword=%s, status=%s", req.keyword(), req.status()));

                // 1. 获取行业类型
                var industryType = industryConfigCache.get().getType();

                // 2. 构建分页对象
                var baseQuery = req.toBaseQuery();
                var page = new Page<SysRole>(baseQuery.pageNo(), baseQuery.pageSize());

                // 3. 确定排序方式
                var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
                var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("sortOrder");

                // 4. 调用仓储查询
                var rolePage = roleRepo.page(page, industryType, req.keyword(), req.status(), sortBy, asc);

                // 5. 批量统计每个角色的用户数量
                Map<Long, Integer> userCountMap = new HashMap<>();
                if (!rolePage.getRecords().isEmpty()) {
                        var roleIds = rolePage.getRecords().stream().map(SysRole::getId).toList();
                        roleIds.forEach(roleId -> {
                                var userIds = userRoleRepo.findUserIdsByRoleId(roleId);
                                userCountMap.put(roleId, userIds != null ? userIds.size() : 0);
                        });
                }

                // 6. 转换为响应
                var respList = rolePage.getRecords().stream()
                                .map(role -> new RoleListResp(
                                                role.getId(),
                                                role.getRoleName(),
                                                role.getRoleCode(),
                                                role.getRoleType(),
                                                role.getDescription(),
                                                Optional.ofNullable(userCountMap.get(role.getId())).orElse(0),
                                                role.getStatus(),
                                                role.getSortOrder(),
                                                role.getCreatedTime(),
                                                role.getUpdatedTime()))
                                .toList();

                // 7. 构建分页结果
                return new PageResult<>(respList, rolePage.getTotal(), rolePage.getCurrent(), rolePage.getSize());
        }

        @Override
        public List<Long> listUserRoleIds(Long userId, String industryType) {
                // 1. 查询用户的所有角色关联（包括所有行业）
                var allUserRoles = Optional.ofNullable(userRoleRepo.findNotDeletedByUserId(userId))
                                .orElseGet(List::of);
                if (CollectionUtils.isEmpty(allUserRoles)) {
                        return List.of();
                }

                // 2. 提取所有角色ID（去重）
                var allRoleIds = allUserRoles.stream()
                                .map(SysUserRole::getRoleId)
                                .distinct()
                                .toList();
                if (CollectionUtils.isEmpty(allRoleIds)) {
                        return List.of();
                }

                // 3. 批量查询角色实体
                var roles = roleRepo.findByIds(allRoleIds);
                if (CollectionUtils.isEmpty(roles)) {
                        return List.of();
                }

                // 4. 过滤：只返回当前行业下的角色ID（遵循RBAC标准规则）
                return roles.stream()
                                .filter(role -> Optional.ofNullable(role.getIsDeleted())
                                                .map(DeleteStatus.DELETED.getCode()::equals)
                                                .map(isDeleted -> !isDeleted)
                                                .orElse(true))
                                .filter(role -> Optional.ofNullable(industryType)
                                                .map(type -> type.equals(role.getIndustryType()))
                                                .orElse(true))
                                .map(SysRole::getId)
                                .distinct()
                                .toList();
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void updateUserRoles(UserRoleUpdateReq req) {
                var userId = req.userId();
                var industryType = industryConfigCache.get().getType();
                var roleIdList = new ArrayList<>(Optional.ofNullable(req.roleIdList())
                                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "角色列表不能为空")));

                // 1. 查询角色并校验行业、状态
                var roleList = roleRepo.findByIds(roleIdList);
                Map<Long, SysRole> roleMap = new HashMap<>();
                for (SysRole role : roleList) {
                        roleMap.put(role.getId(), role);
                }
                for (Long roleId : roleIdList) {
                        SysRole role = Optional.ofNullable(roleMap.get(roleId))
                                        .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST,
                                                        String.format("角色不存在: roleId=%s", roleId)));
                        if (!industryType.equals(role.getIndustryType())) {
                                throw new BusinessException(ResultCode.BAD_REQUEST,
                                                String.format("角色行业不匹配: roleId=%s, industryType=%s", roleId,
                                                                role.getIndustryType()));
                        }
                        Optional.ofNullable(role.getStatus())
                                        .filter(status -> EnableStatus.ENABLED.getCode().equals(status))
                                        .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST,
                                                        String.format("角色未启用: roleId=%s", roleId)));
                        if (Optional.ofNullable(role.getIsDeleted())
                                        .map(DeleteStatus.DELETED.getCode()::equals)
                                        .orElse(false)) {
                                throw new BusinessException(ResultCode.BAD_REQUEST,
                                                String.format("角色已删除: roleId=%s", roleId));
                        }
                }

                // 2. 查询当前用户在当前行业下的已有角色（只处理当前行业下的角色，不影响其他行业的角色）
                var currentIndustryRoleIds = listUserRoleIds(userId, industryType);
                var allCurrentUserRoles = Optional.ofNullable(userRoleRepo.findNotDeletedByUserId(userId))
                                .orElseGet(List::of);
                Map<Long, SysUserRole> currentByRoleId = new HashMap<>();
                for (SysUserRole userRole : allCurrentUserRoles) {
                        if (currentIndustryRoleIds.contains(userRole.getRoleId())) {
                                currentByRoleId.put(userRole.getRoleId(), userRole);
                        }
                }

                Set<Long> newRoleIds = new HashSet<>(roleIdList);
                Set<Long> existingRoleIds = new HashSet<>(currentByRoleId.keySet());

                // 3. 计算需要移除的角色（全量替换模式：删除不在新列表中的已有角色）
                Set<Long> removeRoleIds = new HashSet<>(existingRoleIds);
                removeRoleIds.removeAll(newRoleIds);
                if (!removeRoleIds.isEmpty()) {
                        var removeIds = removeRoleIds.stream()
                                        .map(roleId -> currentByRoleId.get(roleId).getId())
                                        .toList();
                        userRoleRepo.markDeletedByIds(removeIds);
                        log.info(String.format("移除用户角色: userId=%s, removeRoleIds=%s", userId, removeRoleIds));
                }

                // 4. 计算需要新增的角色（全量替换模式：添加不在已有列表中的新角色）
                Set<Long> addRoleIds = new HashSet<>(newRoleIds);
                addRoleIds.removeAll(existingRoleIds);
                if (!addRoleIds.isEmpty()) {
                        var now = LocalDateTime.now();
                        for (Long roleId : addRoleIds) {
                                var entity = new SysUserRole();
                                entity.setUserId(userId);
                                entity.setRoleId(roleId);
                                entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
                                entity.setCreatedTime(now);
                                entity.setUpdatedTime(now);
                                userRoleRepo.save(entity);
                        }
                        log.info(String.format("添加用户角色: userId=%s, addRoleIds=%s", userId, addRoleIds));
                }

                // 5. 如果没有新增也没有删除，说明角色列表没有变化
                if (removeRoleIds.isEmpty() && addRoleIds.isEmpty()) {
                        log.debug(String.format("用户角色无需更新: userId=%s, 请求角色: %s, 已有角色: %s", userId,
                                        newRoleIds, existingRoleIds));
                }

                // 6. 刷新聚合缓存
                permissionService.refreshUserAuthCache(userId, industryType);
        }

        /**
         * 分页查询角色下的所有用户：校验角色存在 → 调用用户服务查询
         *
         * @param roleId Long 角色ID
         * @param req    UserListQueryReq 查询请求（分页参数）
         * @return PageResult<UserListResp> 分页结果
         */
        @Override
        public PageResult<UserListResp> pageUsersByRoleId(Long roleId, UserListQueryReq req) {
                log.debug(String.format("查询角色下的用户列表: roleId=%s", roleId));

                // 1. 校验角色是否存在
                Optional.ofNullable(roleRepo.findById(roleId))
                                .orElseThrow(() -> {
                                        log.warn(String.format("查询角色下的用户失败，角色不存在: roleId=%s", roleId));
                                        return new BusinessException(ResultCode.BAD_REQUEST, "角色不存在");
                                });

                // 2. 构建查询请求（设置 roleId 参数）
                var queryReq = new UserListQueryReq(
                                req.pageNo(),
                                req.pageSize(),
                                req.sortBy(),
                                req.sortOrder(),
                                req.keyword(),
                                roleId, // 设置角色ID，用于过滤
                                req.status());

                // 3. 调用用户服务查询
                return userService.page(queryReq);
        }

        /**
         * 查询角色在当前行业下绑定的用户ID列表（用于绑定用户页面的回显）
         *
         * @param roleId Long 角色ID
         * @return List<Long> 用户ID集合
         */
        @Override
        public List<Long> listUserIdsByRoleId(Long roleId) {
                log.debug(String.format("查询角色绑定的用户ID列表: roleId=%s", roleId));

                // 1. 校验角色是否存在
                var role = Optional.ofNullable(roleRepo.findById(roleId))
                                .orElseThrow(() -> {
                                        log.warn(String.format("查询角色绑定的用户ID列表失败，角色不存在: roleId=%s", roleId));
                                        return new BusinessException(ResultCode.BAD_REQUEST, "角色不存在");
                                });

                // 2. 校验行业类型
                var industryType = industryConfigCache.get().getType();
                if (!industryType.equals(role.getIndustryType())) {
                        log.warn(String.format("查询角色绑定的用户ID列表失败，角色行业不匹配: roleId=%s, industryType=%s", roleId,
                                        role.getIndustryType()));
                        throw new BusinessException(ResultCode.BAD_REQUEST,
                                        String.format("角色行业不匹配: roleId=%s, industryType=%s", roleId,
                                                        role.getIndustryType()));
                }

                // 3. 查询角色绑定的用户ID列表（仅查询当前行业下的用户）
                var allUserIds = userRoleRepo.findUserIdsByRoleId(roleId);
                if (CollectionUtils.isEmpty(allUserIds)) {
                        return List.of();
                }

                // 4. 过滤：只返回当前行业下的用户ID
                var users = sysUserRepo.findByIds(allUserIds);
                return users.stream()
                                .filter(user -> Optional.ofNullable(user.getIsDeleted())
                                                .map(DeleteStatus.DELETED.getCode()::equals)
                                                .map(isDeleted -> !isDeleted)
                                                .orElse(true))
                                .filter(user -> Optional.ofNullable(industryType)
                                                .map(type -> type.equals(user.getIndustryType()))
                                                .orElse(true))
                                .map(SysUser::getId)
                                .distinct()
                                .toList();
        }
}
