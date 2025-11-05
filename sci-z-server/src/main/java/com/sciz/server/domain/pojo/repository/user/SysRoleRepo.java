package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysRole;
import java.util.List;

/**
 * 角色仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysRoleRepo
 * @date 2025-10-30 11:00
 */
public interface SysRoleRepo {

    /**
     * 保存角色
     *
     * @param entity SysRole 实体
     * @return 生成的主键ID
     */
    Long save(SysRole entity);

    /**
     * 批量按ID查询角色
     *
     * @param ids List<Long> 角色ID集合
     * @return List<SysRole> 角色集合
     */
    List<SysRole> findByIds(List<Long> ids);
}
