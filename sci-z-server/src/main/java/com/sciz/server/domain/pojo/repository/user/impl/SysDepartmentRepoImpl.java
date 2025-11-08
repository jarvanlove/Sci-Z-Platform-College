package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import com.sciz.server.domain.pojo.mapper.user.SysDepartmentMapper;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysDepartmentRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysDepartmentRepoImpl implements SysDepartmentRepo {

    private final SysDepartmentMapper mapper;

    public SysDepartmentRepoImpl(SysDepartmentMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存部门
     *
     * @param entity SysDepartment 部门实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysDepartment entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 根据ID查询部门
     *
     * @param id Long 部门ID
     * @return SysDepartment 部门实体
     */
    @Override
    public SysDepartment findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysDepartment::getId, id)
                .eq(SysDepartment::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 根据行业类型查询部门列表
     *
     * @param industryType String 行业类型
     * @return java.util.List<SysDepartment> 部门列表
     */
    @Override
    public List<SysDepartment> listByIndustryType(String industryType) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysDepartment::getIndustryType, industryType)
                .eq(SysDepartment::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .eq(SysDepartment::getStatus, EnableStatus.ENABLED.getCode())
                .orderByAsc(SysDepartment::getSortOrder)
                .list();
    }

    /**
     * 根据行业类型与部门编码查询
     *
     * @param industryType   String 行业类型
     * @param departmentCode String 部门编码
     * @return SysDepartment 部门实体或 null
     */
    @Override
    public SysDepartment findByCode(String industryType, String departmentCode) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysDepartment::getIndustryType, industryType)
                .eq(SysDepartment::getDepartmentCode, departmentCode)
                .eq(SysDepartment::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .eq(SysDepartment::getStatus, EnableStatus.ENABLED.getCode())
                .one();
    }
}
