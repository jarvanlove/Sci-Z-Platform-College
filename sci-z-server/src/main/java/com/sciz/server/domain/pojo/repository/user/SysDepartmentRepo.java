package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysDepartment;

/**
 * 部门仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysDepartmentRepo
 * @date 2025-10-30 11:00
 */
public interface SysDepartmentRepo {

    /**
     * 保存部门
     *
     * @param entity SysDepartment 实体
     * @return 生成的主键ID
     */
    Long save(SysDepartment entity);
}
