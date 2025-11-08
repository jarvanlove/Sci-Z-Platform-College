package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.mapper.user.SysRoleMapper;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色仓储实现
 *
 * @author JiaWen.Wu
 * @className SysRoleRepoImpl
 * @date 2025-10-31 12:00
 */
@Repository
public class SysRoleRepoImpl implements SysRoleRepo {

    private final SysRoleMapper mapper;

    public SysRoleRepoImpl(SysRoleMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存角色
     *
     * @param entity SysRole 角色实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysRole entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 根据ID列表批量查询角色
     *
     * @param ids List<Long> 角色ID列表
     * @return List<SysRole> 角色列表
     */
    @Override
    public List<SysRole> findByIds(List<Long> ids) {
        return mapper.selectBatchIds(ids);
    }

    /**
     * 根据角色编码和行业类型查询角色
     *
     * @param roleCode     String 角色编码
     * @param industryType String 行业类型
     * @return SysRole 角色实体或 null
     */
    @Override
    public SysRole findByCode(String roleCode, String industryType) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getIndustryType, industryType)
                .eq(SysRole::getStatus, EnableStatus.ENABLED.getCode())
                .eq(SysRole::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }
}
