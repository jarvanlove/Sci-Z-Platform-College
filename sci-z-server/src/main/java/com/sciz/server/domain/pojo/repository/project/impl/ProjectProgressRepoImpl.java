package com.sciz.server.domain.pojo.repository.project.impl;

import com.sciz.server.domain.pojo.entity.project.ProjectProgress;
import com.sciz.server.domain.pojo.mapper.project.ProjectProgressMapper;
import com.sciz.server.domain.pojo.repository.project.ProjectProgressRepo;
import org.springframework.stereotype.Repository;

/**
 * 项目进度仓储实现
 * 
 * @author JiaWen.Wu
 * @className ProjectProgressRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class ProjectProgressRepoImpl implements ProjectProgressRepo {

    private final ProjectProgressMapper mapper;

    public ProjectProgressRepoImpl(ProjectProgressMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(ProjectProgress entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
