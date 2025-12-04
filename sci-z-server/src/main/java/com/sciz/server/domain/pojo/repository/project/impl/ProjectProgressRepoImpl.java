package com.sciz.server.domain.pojo.repository.project.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.sciz.server.domain.pojo.entity.project.ProjectProgress;
import com.sciz.server.domain.pojo.mapper.project.ProjectProgressMapper;
import com.sciz.server.domain.pojo.repository.project.ProjectProgressRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, Integer> findLatestProgressByProjectIds(List<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Map.of();
        }

        // 批量查询所有项目的最新进度记录
        var queryWrapper = new LambdaQueryWrapper<ProjectProgress>()
                .in(ProjectProgress::getProjectId, projectIds)
                .eq(ProjectProgress::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        var allProgress = mapper.selectList(queryWrapper);

        // 按项目ID分组，每组内按 record_time 倒序，取第一条（最新进度）
        return allProgress.stream()
                .collect(Collectors.groupingBy(
                        ProjectProgress::getProjectId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .max((p1, p2) -> {
                                            if (p1.getRecordTime() == null && p2.getRecordTime() == null) {
                                                return 0;
                                            }
                                            if (p1.getRecordTime() == null) {
                                                return -1;
                                            }
                                            if (p2.getRecordTime() == null) {
                                                return 1;
                                            }
                                            return p1.getRecordTime().compareTo(p2.getRecordTime());
                                        })
                                        .map(ProjectProgress::getProgress)
                                        .orElse(null))))
                .entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<ProjectProgress> findMilestonesByProjectId(Long projectId) {
        if (projectId == null) {
            return List.of();
        }
        return mapper.selectList(new LambdaQueryWrapper<ProjectProgress>()
                .eq(ProjectProgress::getProjectId, projectId)
                .eq(ProjectProgress::getIsMilestone, 1)
                .eq(ProjectProgress::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(ProjectProgress::getMilestoneStartTime));
    }

    @Override
    public ProjectProgress findById(Long id) {
        if (id == null) {
            return null;
        }
        return mapper.selectOne(new LambdaQueryWrapper<ProjectProgress>()
                .eq(ProjectProgress::getId, id)
                .eq(ProjectProgress::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
    }

    @Override
    public boolean updateById(ProjectProgress entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteBatchByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return new LambdaUpdateChainWrapper<>(mapper)
                .in(ProjectProgress::getId, ids)
                .set(ProjectProgress::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }
}
