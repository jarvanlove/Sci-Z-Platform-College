package com.sciz.server.domain.pojo.repository.project;

import com.sciz.server.domain.pojo.entity.project.ProjectMember;

/**
 * 项目成员仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ProjectMemberRepo
 * @date 2025-10-30 11:00
 */
public interface ProjectMemberRepo {

    /**
     * 保存项目成员
     *
     * @param entity ProjectMember 实体
     * @return 生成的主键ID
     */
    Long save(ProjectMember entity);
}
