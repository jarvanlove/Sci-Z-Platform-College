package com.sciz.server.domain.pojo.repository.project;

import com.sciz.server.domain.pojo.entity.project.ProjectMember;

import java.util.List;

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

    /**
     * 根据项目ID查询项目成员列表
     *
     * @param projectId Long 项目ID
     * @return List<ProjectMember> 项目成员列表
     */
    List<ProjectMember> findByProjectId(Long projectId);

    /**
     * 批量保存项目成员
     *
     * @param entities List<ProjectMember> 成员列表
     * @return boolean 是否全部保存成功
     */
    boolean saveBatch(List<ProjectMember> entities);

    /**
     * 根据用户ID和项目ID查询成员
     *
     * @param projectId Long 项目ID
     * @param userId    Long 用户ID
     * @return ProjectMember 或 null
     */
    ProjectMember findByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 根据用户ID查询其作为成员的项目ID列表（用于知识库可见性：项目成员可见项目知识库）
     *
     * @param userId Long 用户ID
     * @return 项目ID列表，未删除的成员记录
     */
    List<Long> findProjectIdsByUserId(Long userId);

    /**
     * 更新项目成员
     *
     * @param entity ProjectMember 实体
     * @return boolean 是否更新成功
     */
    boolean updateById(ProjectMember entity);

    /**
     * 批量软删除项目成员
     *
     * @param ids List<Long> 成员ID列表
     * @return boolean 是否删除成功
     */
    boolean deleteBatchByIds(List<Long> ids);
}
