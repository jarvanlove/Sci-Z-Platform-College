package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysPermission;
import com.sciz.server.domain.pojo.mapper.user.SysPermissionMapper;
import com.sciz.server.domain.pojo.repository.user.SysPermissionRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 权限仓储实现
 *
 * @author JiaWen.Wu
 * @className SysPermissionRepoImpl
 * @date 2025-10-31 12:00
 */
@Repository
public class SysPermissionRepoImpl implements SysPermissionRepo {

    private final SysPermissionMapper mapper;

    public SysPermissionRepoImpl(SysPermissionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存权限
     *
     * @param entity SysPermission 权限实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysPermission entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 根据ID列表批量查询权限（过滤逻辑删除）
     *
     * @param ids List<Long> 权限ID列表
     * @return List<SysPermission> 权限列表（仅返回未删除的权限）
     */
    @Override
    public List<SysPermission> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return new LambdaQueryChainWrapper<>(mapper)
                .in(SysPermission::getId, ids)
                .eq(SysPermission::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .list();
    }

    /**
     * 查询指定行业下的权限列表
     *
     * @param industryType String 行业类型
     * @return List<SysPermission> 权限列表
     */
    @Override
    public List<SysPermission> listByIndustryType(String industryType) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysPermission::getIndustryType, industryType)
                .eq(SysPermission::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .eq(SysPermission::getStatus, EnableStatus.ENABLED.getCode())
                .orderByAsc(SysPermission::getSortOrder)
                .orderByAsc(SysPermission::getId)
                .list();
    }
}
