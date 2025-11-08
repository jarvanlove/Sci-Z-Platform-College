package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysRolePermission;
import com.sciz.server.domain.pojo.mapper.user.SysRolePermissionMapper;
import com.sciz.server.domain.pojo.repository.user.SysRolePermissionRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限仓储实现
 *
 * @author JiaWen.Wu
 * @className SysRolePermissionRepoImpl
 * @date 2025-10-31 12:00
 */
@Repository
public class SysRolePermissionRepoImpl implements SysRolePermissionRepo {

    private final SysRolePermissionMapper mapper;

    public SysRolePermissionRepoImpl(SysRolePermissionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存角色权限关联
     *
     * @param entity SysRolePermission 角色权限实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysRolePermission entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 查询未删除的角色权限关联
     *
     * @param roleIds List<Long> 角色ID列表
     * @return List<SysRolePermission> 角色权限列表
     */
    @Override
    public List<SysRolePermission> findNotDeletedByRoleIds(List<Long> roleIds) {
        return new LambdaQueryChainWrapper<>(mapper)
                .in(SysRolePermission::getRoleId, roleIds)
                .eq(SysRolePermission::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .list();
    }

    /**
     * 查询指定角色未删除的权限关联
     *
     * @param roleId Long 角色ID
     * @return List<SysRolePermission> 角色权限列表
     */
    @Override
    public List<SysRolePermission> findNotDeletedByRoleId(Long roleId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysRolePermission::getRoleId, roleId)
                .eq(SysRolePermission::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .list();
    }

    /**
     * 根据ID集合标记删除
     *
     * @param ids List<Long> 主键ID集合
     */
    @Override
    public void markDeletedByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        new LambdaUpdateChainWrapper<>(mapper)
                .in(SysRolePermission::getId, ids)
                .set(SysRolePermission::getIsDeleted, DeleteStatus.DELETED.getCode())
                .set(SysRolePermission::getUpdatedTime, LocalDateTime.now())
                .update();
    }
}
