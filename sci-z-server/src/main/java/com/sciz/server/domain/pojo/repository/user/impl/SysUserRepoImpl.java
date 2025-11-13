package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.mapper.user.SysUserMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

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
    private final SysUserRoleRepo sysUserRoleRepo;

    public SysUserRepoImpl(SysUserMapper mapper, SysUserRoleRepo sysUserRoleRepo) {
        this.mapper = mapper;
        this.sysUserRoleRepo = sysUserRoleRepo;
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
     * 根据邮箱查询用户
     *
     * @param email String 邮箱
     * @return SysUser 用户实体或 null
     */
    @Override
    public SysUser findByEmail(String email) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysUser::getEmail, email)
                .eq(SysUser::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 根据手机号查询用户
     *
     * @param phone String 手机号
     * @return SysUser 用户实体或 null
     */
    @Override
    public SysUser findByPhone(String phone) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysUser::getPhone, phone)
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

    /**
     * 分页查询用户列表
     *
     * @param page    IPage<SysUser> 分页对象
     * @param keyword String 搜索关键字（用户名/邮箱/手机号）
     * @param roleId  Long 角色ID（null表示全部）
     * @param status  Integer 用户状态（null表示全部）
     * @param sortBy  String 排序字段
     * @param asc     boolean 是否升序
     * @return IPage<SysUser> 分页结果
     */
    @Override
    public IPage<SysUser> page(IPage<SysUser> page, String keyword, Long roleId, Integer status, String sortBy,
            boolean asc) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 只查询未删除的用户
        wrapper.eq(SysUser::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 关键字搜索（用户名/邮箱/手机号）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword)
                    .or()
                    .like(SysUser::getPhone, keyword));
        }

        // 状态筛选
        Optional.ofNullable(status)
                .ifPresent(value -> wrapper.eq(SysUser::getStatus, value));

        // 角色筛选（通过关联表查询用户ID列表，然后使用 IN 查询）
        if (roleId != null) {
            var userIds = sysUserRoleRepo.findUserIdsByRoleId(roleId);
            if (CollectionUtils.isEmpty(userIds)) {
                // 如果该角色下没有用户，返回空结果
                page.setRecords(List.of());
                page.setTotal(0);
                return page;
            }
            wrapper.in(SysUser::getId, userIds);
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "createTime", "createdTime" -> wrapper.orderBy(true, asc, SysUser::getCreatedTime);
                case "username" -> wrapper.orderBy(true, asc, SysUser::getUsername);
                case "realName" -> wrapper.orderBy(true, asc, SysUser::getRealName);
                case "status" -> wrapper.orderBy(true, asc, SysUser::getStatus);
                default -> wrapper.orderBy(true, false, SysUser::getCreatedTime);
            }
        } else {
            wrapper.orderBy(true, false, SysUser::getCreatedTime);
        }

        return mapper.selectPage(page, wrapper);
    }
}
