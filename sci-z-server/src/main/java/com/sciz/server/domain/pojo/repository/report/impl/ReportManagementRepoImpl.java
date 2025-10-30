package com.sciz.server.domain.pojo.repository.report.impl;

import com.sciz.server.domain.pojo.entity.report.ReportManagement;
import com.sciz.server.domain.pojo.mapper.report.ReportManagementMapper;
import com.sciz.server.domain.pojo.repository.report.ReportManagementRepo;
import org.springframework.stereotype.Repository;

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
}
