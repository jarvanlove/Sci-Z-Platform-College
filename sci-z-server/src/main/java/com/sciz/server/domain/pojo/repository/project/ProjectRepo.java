package com.sciz.server.domain.pojo.repository.project;

import com.sciz.server.domain.pojo.entity.project.Project;

/**
 * 项目仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ProjectRepo
 * @date 2025-10-30 11:00
 */
public interface ProjectRepo {

    /**
     * 保存项目
     *
     * @param entity Project 实体
     * @return 生成的主键ID
     */
    Long save(Project entity);
}
