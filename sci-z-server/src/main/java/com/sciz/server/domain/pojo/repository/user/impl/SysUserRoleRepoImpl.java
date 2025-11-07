package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.mapper.user.SysUserRoleMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色仓储实现
 *
 * @author JiaWen.Wu
 * @className SysUserRoleRepoImpl
 * @date 2025-10-31 12:00
 */
@Repository
public class SysUserRoleRepoImpl implements SysUserRoleRepo {

    private final SysUserRoleMapper mapper;

    public SysUserRoleRepoImpl(SysUserRoleMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存用户角色关联
     *
     * @param entity SysUserRole 用户角色实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysUserRole entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 查询未删除的用户角色关联
     *
     * @param userId Long 用户ID
     * @return List<SysUserRole> 用户角色列表
     */
    @Override
    public List<SysUserRole> findNotDeletedByUserId(Long userId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .list();
    }
}
