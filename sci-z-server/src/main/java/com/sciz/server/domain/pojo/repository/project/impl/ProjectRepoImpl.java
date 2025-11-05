package com.sciz.server.domain.pojo.repository.project.impl;

import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.domain.pojo.mapper.project.ProjectMapper;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import org.springframework.stereotype.Repository;

/**
 * 项目仓储实现
 * 
 * @author JiaWen.Wu
 * @className ProjectRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class ProjectRepoImpl implements ProjectRepo {

    private final ProjectMapper mapper;

    public ProjectRepoImpl(ProjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(Project entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
