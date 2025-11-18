package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.mapper.user.SysRoleMapper;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
     * 根据ID列表批量查询角色（过滤逻辑删除）
     *
     * @param ids List<Long> 角色ID列表
     * @return List<SysRole> 角色列表（仅返回未删除的角色）
     */
    @Override
    public List<SysRole> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return new LambdaQueryChainWrapper<>(mapper)
                .in(SysRole::getId, ids)
                .eq(SysRole::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .list();
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

    /**
     * 查询指定行业下的角色列表
     *
     * @param industryType String 行业类型
     * @return List<SysRole> 角色列表
     */
    @Override
    public List<SysRole> listByIndustryType(String industryType) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysRole::getIndustryType, industryType)
                .eq(SysRole::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(SysRole::getSortOrder)
                .orderByAsc(SysRole::getId)
                .list();
    }

    /**
     * 根据ID查询角色
     *
     * @param id Long 角色ID
     * @return SysRole 角色实体
     */
    @Override
    public SysRole findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysRole::getId, id)
                .eq(SysRole::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 根据ID更新角色
     *
     * @param entity SysRole 角色实体
     * @return boolean 是否更新成功
     */
    @Override
    public boolean updateById(SysRole entity) {
        return mapper.updateById(entity) > 0;
    }

    /**
     * 根据ID软删除角色
     *
     * @param id Long 角色ID
     * @return boolean 是否删除成功
     */
    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(SysRole::getId, id)
                .set(SysRole::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    /**
     * 分页查询角色列表
     *
     * @param page         IPage<SysRole> 分页对象
     * @param industryType String 行业类型
     * @param keyword      String 搜索关键字（角色名称/角色编码）
     * @param status       Integer 角色状态（null表示全部）
     * @param sortBy       String 排序字段
     * @param asc          boolean 是否升序
     * @return IPage<SysRole> 分页结果
     */
    @Override
    public IPage<SysRole> page(IPage<SysRole> page, String industryType, String keyword, Integer status, String sortBy,
            boolean asc) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        // 只查询未删除的角色
        wrapper.eq(SysRole::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 行业类型筛选
        if (StringUtils.hasText(industryType)) {
            wrapper.eq(SysRole::getIndustryType, industryType);
        }

        // 关键字搜索（角色名称/角色编码）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleName, keyword)
                    .or()
                    .like(SysRole::getRoleCode, keyword));
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            if (asc) {
                wrapper.orderByAsc(getSortField(sortBy));
            } else {
                wrapper.orderByDesc(getSortField(sortBy));
            }
        } else {
            // 默认排序：按 sortOrder 升序，然后按 id 升序
            wrapper.orderByAsc(SysRole::getSortOrder)
                    .orderByAsc(SysRole::getId);
        }

        return mapper.selectPage((Page<SysRole>) page, wrapper);
    }

    /**
     * 获取排序字段
     *
     * @param sortBy String 排序字段名
     * @return SFunction<SysRole, ?> 排序字段函数
     */
    private com.baomidou.mybatisplus.core.toolkit.support.SFunction<SysRole, ?> getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "rolename", "role_name" -> SysRole::getRoleName;
            case "rolecode", "role_code" -> SysRole::getRoleCode;
            case "sortorder", "sort_order" -> SysRole::getSortOrder;
            case "createdtime", "created_time" -> SysRole::getCreatedTime;
            case "updatedtime", "updated_time" -> SysRole::getUpdatedTime;
            default -> SysRole::getId;
        };
    }
}
