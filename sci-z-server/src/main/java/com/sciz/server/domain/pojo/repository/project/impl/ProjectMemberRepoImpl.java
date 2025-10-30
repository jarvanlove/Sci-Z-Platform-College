package com.sciz.server.domain.pojo.repository.project.impl;

import com.sciz.server.domain.pojo.entity.project.ProjectMember;
import com.sciz.server.domain.pojo.mapper.project.ProjectMemberMapper;
import com.sciz.server.domain.pojo.repository.project.ProjectMemberRepo;
import org.springframework.stereotype.Repository;

/**
 * 项目成员仓储实现
 * 
 * @author JiaWen.Wu
 * @className ProjectMemberRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class ProjectMemberRepoImpl implements ProjectMemberRepo {

    private final ProjectMemberMapper mapper;

    public ProjectMemberRepoImpl(ProjectMemberMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(ProjectMember entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
