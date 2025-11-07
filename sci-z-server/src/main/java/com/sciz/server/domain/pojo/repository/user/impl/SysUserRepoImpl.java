package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.mapper.user.SysUserMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
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

    /**
     * 保存用户
     *
     * @param entity SysUser 用户实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysUser entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username String 用户名
     * @return SysUser 用户实体
     */
    @Override
    public SysUser findByUsername(String username) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 根据ID查询用户
     *
     * @param id Long 用户ID
     * @return SysUser 用户实体
     */
    @Override
    public SysUser findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysUser::getId, id)
                .eq(SysUser::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 根据ID更新用户
     *
     * @param entity SysUser 用户实体
     * @return boolean 是否更新成功
     */
    @Override
    public boolean updateById(SysUser entity) {
        return mapper.updateById(entity) > 0;
    }
}
