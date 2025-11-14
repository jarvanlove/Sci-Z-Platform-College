package com.sciz.server.application.service.user.impl;

import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.application.service.user.RolePermissionService;
import com.sciz.server.domain.pojo.dto.request.system.RolePermissionUpdateReq;
import com.sciz.server.domain.pojo.entity.user.SysPermission;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.entity.user.SysRolePermission;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.repository.user.SysPermissionRepo;
import com.sciz.server.domain.pojo.repository.user.SysRolePermissionRepo;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.time.LocalDateTime;
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
 * 角色权限管理服务实现
 *
 * @author JiaWen.Wu
 * @className RolePermissionServiceImpl
 * @date 2025-11-09 02:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final SysRolePermissionRepo rolePermissionRepo;
    private final SysPermissionRepo permissionRepo;
    private final SysRoleRepo roleRepo;
    private final SysUserRoleRepo userRoleRepo;
    private final PermissionService permissionService;
    private final IndustryConfigCache industryConfigCache;

    @Override
    public List<Long> listPermissionIds(Long roleId) {
        var industryType = industryConfigCache.get().getType();
        var roleList = roleRepo.findByIds(List.of(roleId));
        if (CollectionUtils.isEmpty(roleList)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, String.format("角色不存在: roleId=%s", roleId));
        }
        SysRole role = roleList.getFirst();
        if (!industryType.equals(role.getIndustryType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    String.format("角色行业不匹配: roleId=%s, industryType=%s", roleId, role.getIndustryType()));
        }
        var relations = rolePermissionRepo.findNotDeletedByRoleId(roleId);
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        return relations.stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(RolePermissionUpdateReq req) {
        var roleId = req.roleId();
        var industryType = industryConfigCache.get().getType();
        var permissionIdList = Optional.ofNullable(req.permissionIdList())
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "权限列表不能为空"));

        // 1. 校验角色
        var roleList = roleRepo.findByIds(List.of(roleId));
        if (CollectionUtils.isEmpty(roleList)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, String.format("角色不存在: roleId=%s", roleId));
        }
        SysRole role = roleList.getFirst();
        if (!industryType.equals(role.getIndustryType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    String.format("角色行业不匹配: roleId=%s, industryType=%s", roleId, role.getIndustryType()));
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

        // 2. 查询并校验权限
        var permissionIds = new HashSet<>(permissionIdList);
        var permissionList = permissionRepo.findByIds(permissionIdList);
        Map<Long, SysPermission> permissionMap = new HashMap<>();
        for (SysPermission permission : permissionList) {
            permissionMap.put(permission.getId(), permission);
        }
        for (Long permissionId : permissionIds) {
            SysPermission permission = Optional.ofNullable(permissionMap.get(permissionId))
                    .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST,
                            String.format("权限不存在: permissionId=%s", permissionId)));
            if (!industryType.equals(permission.getIndustryType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        String.format("权限行业不匹配: permissionId=%s, industryType=%s", permissionId,
                                permission.getIndustryType()));
            }
            Optional.ofNullable(permission.getStatus())
                    .filter(status -> EnableStatus.ENABLED.getCode().equals(status))
                    .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST,
                            String.format("权限未启用: permissionId=%s", permissionId)));
            if (Optional.ofNullable(permission.getIsDeleted())
                    .map(DeleteStatus.DELETED.getCode()::equals)
                    .orElse(false)) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        String.format("权限已删除: permissionId=%s", permissionId));
            }
        }

        // 3. 查询当前角色已有权限关系
        var currentRelations = rolePermissionRepo.findNotDeletedByRoleId(roleId);
        Map<Long, SysRolePermission> currentByPermissionId = new HashMap<>();
        for (SysRolePermission relation : currentRelations) {
            currentByPermissionId.put(relation.getPermissionId(), relation);
        }

        Set<Long> newPermissionIds = new HashSet<>(permissionIds);
        Set<Long> existingPermissionIds = new HashSet<>(currentByPermissionId.keySet());

        // 4. 标记需要移除的关系
        Set<Long> removePermissionIds = new HashSet<>(existingPermissionIds);
        removePermissionIds.removeAll(newPermissionIds);
        if (!removePermissionIds.isEmpty()) {
            var removeIds = removePermissionIds.stream()
                    .map(permissionId -> currentByPermissionId.get(permissionId).getId())
                    .toList();
            rolePermissionRepo.markDeletedByIds(removeIds);
            log.info(String.format("移除角色权限: roleId=%s, permissionIds=%s", roleId, removePermissionIds));
        }

        // 5. 添加新的关系
        Set<Long> addPermissionIds = new HashSet<>(newPermissionIds);
        addPermissionIds.removeAll(existingPermissionIds);
        if (!addPermissionIds.isEmpty()) {
            var now = LocalDateTime.now();
            for (Long permissionId : addPermissionIds) {
                var entity = new SysRolePermission();
                entity.setRoleId(roleId);
                entity.setPermissionId(permissionId);
                entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
                entity.setCreatedTime(now);
                entity.setUpdatedTime(now);
                rolePermissionRepo.save(entity);
            }
            log.info(String.format("新增角色权限: roleId=%s, permissionIds=%s", roleId, addPermissionIds));
        }

        // 6. 刷新关联用户的权限缓存
        var userRoles = userRoleRepo.findNotDeletedByRoleId(roleId);
        for (SysUserRole userRole : userRoles) {
            permissionService.refreshUserAuthCache(userRole.getUserId(), industryType);
        }
    }
}
