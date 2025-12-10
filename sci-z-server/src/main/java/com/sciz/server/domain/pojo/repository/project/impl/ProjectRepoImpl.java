package com.sciz.server.domain.pojo.repository.project.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.domain.pojo.mapper.project.ProjectMapper;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import java.util.List;

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

    @Override
    public Project findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(Project::getId, id)
                .eq(Project::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public IPage<Project> page(Page<Project> page, String keyword, String status, String sortBy, boolean asc,
            List<Long> declarationIds) {
        var queryWrapper = new LambdaQueryWrapper<Project>();
        queryWrapper.eq(Project::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 关键字搜索（项目编号/项目名称）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Project::getNumber, keyword)
                    .or()
                    .like(Project::getName, keyword));
        }

        // 项目状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Project::getStatus, status);
        }

        // 时间范围筛选（通过申报ID列表）
        // 如果提供了申报ID列表，则只查询这些申报关联的项目
        // 这样可以避免连表查询，利用索引提高性能
        if (declarationIds != null && !declarationIds.isEmpty()) {
            queryWrapper.in(Project::getDeclarationId, declarationIds);
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            if (asc) {
                queryWrapper.orderByAsc(getSortField(sortBy));
            } else {
                queryWrapper.orderByDesc(getSortField(sortBy));
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(Project::getCreatedTime);
        }

        return mapper.selectPage(page, queryWrapper);
    }

    /**
     * 获取排序字段
     *
     * @param sortBy String 排序字段名
     * @return SFunction<Project, ?> 排序字段函数
     */
    private SFunction<Project, ?> getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "number" -> Project::getNumber;
            case "name" -> Project::getName;
            case "status" -> Project::getStatus;
            case "progress" -> Project::getProgress;
            case "budget" -> Project::getBudget;
            case "createdtime", "created_time" -> Project::getCreatedTime;
            case "updatedtime", "updated_time" -> Project::getUpdatedTime;
            default -> Project::getCreatedTime;
        };
    }

    @Override
    public boolean updateById(Project entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(Project::getId, id)
                .set(Project::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    @Override
    public Long countByStatus(String status) {
        var queryWrapper = new LambdaQueryWrapper<Project>();
        queryWrapper.eq(Project::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 如果指定了状态，则按状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Project::getStatus, status);
        }

        return mapper.selectCount(queryWrapper);
    }
}
