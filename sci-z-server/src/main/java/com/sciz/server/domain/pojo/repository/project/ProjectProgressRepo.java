package com.sciz.server.domain.pojo.repository.project;

import com.sciz.server.domain.pojo.entity.project.ProjectProgress;

import java.util.List;
import java.util.Map;

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

    /**
     * 根据项目ID列表批量查询最新进度
     * 返回 Map<项目ID, 最新进度百分比>，如果没有进度记录则返回 null
     *
     * @param projectIds List<Long> 项目ID列表
     * @return Map<Long, Integer> 项目ID -> 最新进度百分比
     */
    Map<Long, Integer> findLatestProgressByProjectIds(List<Long> projectIds);

    /**
     * 根据项目ID查询里程碑列表（is_milestone = 1）
     *
     * @param projectId Long 项目ID
     * @return List<ProjectProgress> 里程碑列表
     */
    List<ProjectProgress> findMilestonesByProjectId(Long projectId);

    /**
     * 根据ID查询里程碑
     *
     * @param id Long 里程碑ID
     * @return ProjectProgress 或 null
     */
    ProjectProgress findById(Long id);

    /**
     * 更新里程碑
     *
     * @param entity ProjectProgress 实体
     * @return boolean 是否更新成功
     */
    boolean updateById(ProjectProgress entity);

    /**
     * 批量软删除里程碑
     *
     * @param ids List<Long> 里程碑ID列表
     * @return boolean 是否删除成功
     */
    boolean deleteBatchByIds(List<Long> ids);
}
