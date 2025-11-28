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
}
