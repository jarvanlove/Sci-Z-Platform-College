package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysUserProfile;

/**
 * 用户档案仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysUserProfileRepo
 * @date 2025-10-30 11:00
 */
public interface SysUserProfileRepo {

    /**
     * 保存用户档案
     *
     * @param entity SysUserProfile 实体
     * @return 生成的主键ID
     */
    Long save(SysUserProfile entity);
}
