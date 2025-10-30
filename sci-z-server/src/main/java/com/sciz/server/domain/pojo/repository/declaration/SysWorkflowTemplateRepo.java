package com.sciz.server.domain.pojo.repository.declaration;

import com.sciz.server.domain.pojo.entity.declaration.SysWorkflowTemplate;

/**
 * 工作流模板仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysWorkflowTemplateRepo
 * @date 2025-10-30 11:00
 */
public interface SysWorkflowTemplateRepo {

    /**
     * 保存工作流模板
     *
     * @param entity SysWorkflowTemplate 实体
     * @return 生成的主键ID
     */
    Long save(SysWorkflowTemplate entity);
}
