package com.sciz.server.domain.pojo.repository.project.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.sciz.server.domain.pojo.entity.project.ProjectMember;
import com.sciz.server.domain.pojo.mapper.project.ProjectMemberMapper;
import com.sciz.server.domain.pojo.repository.project.ProjectMemberRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

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

    @Override
    public List<ProjectMember> findByProjectId(Long projectId) {
        if (projectId == null) {
            return List.of();
        }
        return mapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(ProjectMember::getJoinTime));
    }

    @Override
    public boolean saveBatch(List<ProjectMember> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return true;
        }
        try {
            entities.forEach(mapper::insert);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ProjectMember findByProjectIdAndUserId(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return null;
        }
        return mapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .eq(ProjectMember::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
    }

    @Override
    public List<Long> findProjectIdsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return mapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                        .select(ProjectMember::getProjectId)
                        .eq(ProjectMember::getUserId, userId)
                        .eq(ProjectMember::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()))
                .stream()
                .map(ProjectMember::getProjectId)
                .distinct()
                .toList();
    }

    @Override
    public boolean updateById(ProjectMember entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteBatchByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return new LambdaUpdateChainWrapper<>(mapper)
                .in(ProjectMember::getId, ids)
                .set(ProjectMember::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }
}
