package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.mapper.user.SysUserRoleMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
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

    @Override
    public Long save(SysUserRole entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public List<SysUserRole> findNotDeletedByUserId(Long userId) {
        return mapper.selectList(new QueryWrapper<SysUserRole>()
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }
}
