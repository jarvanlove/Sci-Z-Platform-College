package com.sciz.server.domain.pojo.dto.request.report;

import jakarta.validation.constraints.NotNull;

/**
 * 报告管理更新请求
 *
 * @param id                 Long 报告ID
 * @param projectName        String 项目名称
 * @param projectCode        String 项目编号
 * @param projectKnowledgeId String 项目知识库ID
 * @param reportType         String 报告类型(tech/self)
 * @param summary            String 报告摘要
 * @param status             String 状态
 * @author JiaWen.Wu
 * @className ReportManagementUpdateReq
 * @date 2025-01-24 14:30
 */
public record ReportManagementUpdateReq(
        @NotNull(message = "报告ID不能为空") Long id,
        String projectName,
        String projectCode,
        String projectKnowledgeId,
        String reportType,
        String summary,
        String status) {
}

