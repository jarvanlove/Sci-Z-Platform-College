package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.mapper.user.SysUserMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysUserRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysUserRepoImpl implements SysUserRepo {

    private final SysUserMapper mapper;

    public SysUserRepoImpl(SysUserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysUser entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public SysUser findByUsername(String username) {
        return mapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0));
    }
}
