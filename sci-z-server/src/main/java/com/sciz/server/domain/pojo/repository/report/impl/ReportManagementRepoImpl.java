package com.sciz.server.domain.pojo.repository.report.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.report.ReportManagement;
import com.sciz.server.domain.pojo.mapper.report.ReportManagementMapper;
import com.sciz.server.domain.pojo.repository.report.ReportManagementRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 报告管理仓储实现
 * 
 * @author JiaWen.Wu
 * @className ReportManagementRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class ReportManagementRepoImpl implements ReportManagementRepo {

    private final ReportManagementMapper mapper;

    public ReportManagementRepoImpl(ReportManagementMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(ReportManagement entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public ReportManagement findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(ReportManagement::getId, id)
                .eq(ReportManagement::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public IPage<ReportManagement> page(Page<ReportManagement> page, String keyword, String status, String reportType, String sortBy, boolean asc) {
        var queryWrapper = new LambdaQueryWrapper<ReportManagement>();
        queryWrapper.eq(ReportManagement::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 关键字搜索（报告编号/项目名称/创建人）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ReportManagement::getNumber, keyword)
                    .or()
                    .like(ReportManagement::getProjectName, keyword)
                    .or()
                    .like(ReportManagement::getCreatorName, keyword));
        }

        // 报告状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(ReportManagement::getStatus, status);
        }

        // 报告类型筛选
        if (StringUtils.hasText(reportType)) {
            queryWrapper.eq(ReportManagement::getReportType, reportType);
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            if (asc) {
                queryWrapper.orderByAsc(getSortField(sortBy));
            } else {
                queryWrapper.orderByDesc(getSortField(sortBy));
            }
        } else {
            // 默认按生成时间倒序
            queryWrapper.orderByDesc(ReportManagement::getGenerateTime);
        }

        return mapper.selectPage(page, queryWrapper);
    }

    /**
     * 获取排序字段
     *
     * @param sortBy String 排序字段名
     * @return SFunction<ReportManagement, ?> 排序字段函数
     */
    private SFunction<ReportManagement, ?> getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "number" -> ReportManagement::getNumber;
            case "projectname", "project_name" -> ReportManagement::getProjectName;
            case "creatortime", "creator_time" -> ReportManagement::getCreatedTime;
            case "generatetime", "generate_time" -> ReportManagement::getGenerateTime;
            case "createdtime", "created_time" -> ReportManagement::getCreatedTime;
            case "updatedtime", "updated_time" -> ReportManagement::getUpdatedTime;
            default -> ReportManagement::getGenerateTime;
        };
    }

    @Override
    public boolean updateById(ReportManagement entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(ReportManagement::getId, id)
                .set(ReportManagement::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }
}
