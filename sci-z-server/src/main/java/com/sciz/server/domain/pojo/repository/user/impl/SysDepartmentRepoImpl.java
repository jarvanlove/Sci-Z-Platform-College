package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import com.sciz.server.domain.pojo.mapper.user.SysDepartmentMapper;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import org.springframework.stereotype.Repository;

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

    @Override
    public Long save(SysDepartment entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public SysDepartment findById(Long id) {
        return mapper.selectById(id);
    }
}
