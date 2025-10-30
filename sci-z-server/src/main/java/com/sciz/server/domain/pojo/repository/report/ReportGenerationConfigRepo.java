package com.sciz.server.domain.pojo.repository.report;

import com.sciz.server.domain.pojo.entity.report.ReportGenerationConfig;

/**
 * 报告生成配置仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ReportGenerationConfigRepo
 * @date 2025-10-30 11:00
 */
public interface ReportGenerationConfigRepo {

    /**
     * 保存报告生成配置
     *
     * @param entity ReportGenerationConfig 实体
     * @return 生成的主键ID
     */
    Long save(ReportGenerationConfig entity);
}
