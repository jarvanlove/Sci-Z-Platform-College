package com.sciz.server.application.service.user.impl;

import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
import com.sciz.server.domain.pojo.dto.response.user.RoleListResp;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

        private final SysUserRoleRepo userRoleRepo;
        private final SysRoleRepo roleRepo;
        private final PermissionService permissionService;
        private final IndustryConfigCache industryConfigCache;

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
                return roles.stream()
                                .map(role -> new RoleListResp(
                                                role.getId(),
                                                role.getRoleName(),
                                                role.getRoleCode(),
                                                role.getRoleType(),
                                                role.getDescription(),
                                                role.getStatus(),
                                                role.getSortOrder(),
                                                role.getCreatedTime(),
                                                role.getUpdatedTime()))
                                .toList();
        }

        @Override
        public List<Long> listUserRoleIds(Long userId, String industryType) {
                return Optional.ofNullable(userRoleRepo.findNotDeletedByUserId(userId))
                                .filter(list -> !CollectionUtils.isEmpty(list))
                                .map(list -> list.stream().map(SysUserRole::getRoleId).distinct().toList())
                                .filter(ids -> !CollectionUtils.isEmpty(ids))
                                .map(roleRepo::findByIds)
                                .map(roles -> roles.stream()
                                                .filter(role -> Optional.ofNullable(role.getIsDeleted())
                                                                .map(DeleteStatus.DELETED.getCode()::equals)
                                                                .map(isDeleted -> !isDeleted)
                                                                .orElse(true))
                                                .filter(role -> Optional.ofNullable(industryType)
                                                                .map(type -> type.equals(role.getIndustryType()))
                                                                .orElse(true))
                                                .map(SysRole::getId)
                                                .toList())
                                .orElseGet(List::of);
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

                // 2. 查询当前用户已有角色
                var currentByRoleId = Optional.ofNullable(userRoleRepo.findNotDeletedByUserId(userId))
                                .map(currentUserRoles -> {
                                        Map<Long, SysUserRole> current = new HashMap<>();
                                        currentUserRoles.forEach(userRole -> Optional
                                                        .ofNullable(roleMap.get(userRole.getRoleId()))
                                                        .ifPresent(role -> current.put(userRole.getRoleId(),
                                                                        userRole)));
                                        return current;
                                })
                                .orElseGet(HashMap::new);

                Set<Long> newRoleIds = new HashSet<>(roleIdList);
                Set<Long> existingRoleIds = new HashSet<>(currentByRoleId.keySet());

                // 3. 需要移除的角色
                Set<Long> removeRoleIds = new HashSet<>(existingRoleIds);
                removeRoleIds.removeAll(newRoleIds);
                if (!removeRoleIds.isEmpty()) {
                        var removeIds = removeRoleIds.stream()
                                        .map(roleId -> currentByRoleId.get(roleId).getId())
                                        .toList();
                        userRoleRepo.markDeletedByIds(removeIds);
                        log.info(String.format("移除用户角色: userId=%s, removeRoleIds=%s", userId, removeRoleIds));
                }

                // 4. 需要新增的角色
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

                // 5. 刷新聚合缓存
                permissionService.refreshUserAuthCache(userId, industryType);
        }
}
