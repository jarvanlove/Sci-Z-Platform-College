package com.sciz.server.domain.pojo.repository.report;

import com.sciz.server.domain.pojo.entity.report.ReportManagement;

/**
 * 报告管理仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ReportManagementRepo
 * @date 2025-10-30 11:00
 */
public interface ReportManagementRepo {

    /**
     * 保存报告管理
     *
     * @param entity ReportManagement 实体
     * @return 生成的主键ID
     */
    Long save(ReportManagement entity);
}
