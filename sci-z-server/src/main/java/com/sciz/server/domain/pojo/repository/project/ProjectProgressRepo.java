package com.sciz.server.domain.pojo.repository.project;

import com.sciz.server.domain.pojo.entity.project.ProjectProgress;

/**
 * 项目进度仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ProjectProgressRepo
 * @date 2025-10-30 11:00
 */
public interface ProjectProgressRepo {

    /**
     * 保存项目进度
     *
     * @param entity ProjectProgress 实体
     * @return 生成的主键ID
     */
    Long save(ProjectProgress entity);
}
