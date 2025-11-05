package com.sciz.server.domain.pojo.repository.declaration.impl;

import com.sciz.server.domain.pojo.entity.declaration.SysWorkflowTemplate;
import com.sciz.server.domain.pojo.mapper.declaration.SysWorkflowTemplateMapper;
import com.sciz.server.domain.pojo.repository.declaration.SysWorkflowTemplateRepo;
import org.springframework.stereotype.Repository;

/**
 * 工作流模板仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysWorkflowTemplateRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysWorkflowTemplateRepoImpl implements SysWorkflowTemplateRepo {

    private final SysWorkflowTemplateMapper mapper;

    public SysWorkflowTemplateRepoImpl(SysWorkflowTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysWorkflowTemplate entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
