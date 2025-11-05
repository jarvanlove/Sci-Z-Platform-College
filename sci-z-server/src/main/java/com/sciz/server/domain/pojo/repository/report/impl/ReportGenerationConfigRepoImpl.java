package com.sciz.server.domain.pojo.repository.report.impl;

import com.sciz.server.domain.pojo.entity.report.ReportGenerationConfig;
import com.sciz.server.domain.pojo.mapper.report.ReportGenerationConfigMapper;
import com.sciz.server.domain.pojo.repository.report.ReportGenerationConfigRepo;
import org.springframework.stereotype.Repository;

/**
 * 报告生成配置仓储实现
 * 
 * @author JiaWen.Wu
 * @className ReportGenerationConfigRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class ReportGenerationConfigRepoImpl implements ReportGenerationConfigRepo {

    private final ReportGenerationConfigMapper mapper;

    public ReportGenerationConfigRepoImpl(ReportGenerationConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(ReportGenerationConfig entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
