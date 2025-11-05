package com.sciz.server.domain.pojo.repository.report;

import com.sciz.server.domain.pojo.entity.report.ReportContent;

/**
 * 报告内容仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ReportContentRepo
 * @date 2025-10-30 11:00
 */
public interface ReportContentRepo {

    /**
     * 保存报告内容
     *
     * @param entity ReportContent 实体
     * @return 生成的主键ID
     */
    Long save(ReportContent entity);
}
