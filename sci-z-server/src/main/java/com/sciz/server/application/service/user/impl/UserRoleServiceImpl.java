package com.sciz.server.application.service.user.impl;

import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<Long> listUserRoleIds(Long userId, String industryType) {
        var userRoles = userRoleRepo.findNotDeletedByUserId(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return List.of();
        }
        var roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        var roles = roleRepo.findByIds(roleIds);
        return roles.stream()
                .filter(role -> role.getIsDeleted() == null
                        || DeleteStatus.NOT_DELETED.getCode().equals(role.getIsDeleted()))
                .filter(role -> industryType == null || industryType.equals(role.getIndustryType()))
                .map(SysRole::getId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(UserRoleUpdateReq req) {
        var userId = req.userId();
        var industryType = req.industryType();
        if (req.roleIdList() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色列表不能为空");
        }
        var roleIdList = new ArrayList<>(req.roleIdList());

        // 1. 查询角色并校验行业、状态
        var roleList = roleRepo.findByIds(roleIdList);
        Map<Long, SysRole> roleMap = new HashMap<>();
        for (SysRole role : roleList) {
            roleMap.put(role.getId(), role);
        }
        for (Long roleId : roleIdList) {
            SysRole role = roleMap.get(roleId);
            if (role == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, String.format("角色不存在: roleId=%s", roleId));
            }
            if (!industryType.equals(role.getIndustryType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        String.format("角色行业不匹配: roleId=%s, industryType=%s", roleId, role.getIndustryType()));
            }
            if (role.getStatus() == null || !EnableStatus.ENABLED.getCode().equals(role.getStatus())) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        String.format("角色未启用: roleId=%s", roleId));
            }
            if (role.getIsDeleted() != null && DeleteStatus.DELETED.getCode().equals(role.getIsDeleted())) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        String.format("角色已删除: roleId=%s", roleId));
            }
        }

        // 2. 查询当前用户已有角色
        var currentUserRoles = userRoleRepo.findNotDeletedByUserId(userId);
        Map<Long, SysUserRole> currentByRoleId = new HashMap<>();
        for (SysUserRole userRole : currentUserRoles) {
            SysRole role = roleMap.get(userRole.getRoleId());
            if (role == null) {
                // 非本行业角色，略过但保留
                continue;
            }
            currentByRoleId.put(userRole.getRoleId(), userRole);
        }

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
