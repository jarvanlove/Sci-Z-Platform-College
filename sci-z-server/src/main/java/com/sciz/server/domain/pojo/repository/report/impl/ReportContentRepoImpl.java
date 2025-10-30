package com.sciz.server.domain.pojo.repository.report.impl;

import com.sciz.server.domain.pojo.entity.report.ReportContent;
import com.sciz.server.domain.pojo.mapper.report.ReportContentMapper;
import com.sciz.server.domain.pojo.repository.report.ReportContentRepo;
import org.springframework.stereotype.Repository;

/**
 * 报告内容仓储实现
 * 
 * @author JiaWen.Wu
 * @className ReportContentRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class ReportContentRepoImpl implements ReportContentRepo {

    private final ReportContentMapper mapper;

    public ReportContentRepoImpl(ReportContentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(ReportContent entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
