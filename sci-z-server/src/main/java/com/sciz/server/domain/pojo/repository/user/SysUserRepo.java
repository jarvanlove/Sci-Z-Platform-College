package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysUser;

/**
 * 用户仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysUserRepo
 * @date 2025-10-30 11:00
 */
public interface SysUserRepo {

    /**
     * 保存用户
     *
     * @param entity SysUser 实体
     * @return 生成的主键ID
     */
    Long save(SysUser entity);

    /**
     * 按用户名查询
     *
     * @param username 用户名
     * @return SysUser 或 null
     */
    SysUser findByUsername(String username);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return SysUser 或 null
     */
    SysUser findById(Long id);

    /**
     * 更新用户
     *
     * @param entity SysUser 实体
     * @return 是否更新成功
     */
    boolean updateById(SysUser entity);
}
