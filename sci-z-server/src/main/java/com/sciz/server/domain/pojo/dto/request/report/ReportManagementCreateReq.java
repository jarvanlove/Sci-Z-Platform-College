package com.sciz.server.domain.pojo.dto.request.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 报告管理创建请求
 *
 * @param projectId          Long 项目ID
 * @param projectName        String 项目名称
 * @param projectCode        String 项目编号
 * @param projectKnowledgeId String 项目知识库ID
 * @param reportType         String 报告类型(tech/self)
 * @param summary            String 报告摘要
 * @author JiaWen.Wu
 * @className ReportManagementCreateReq
 * @date 2025-01-24 14:30
 */
public record ReportManagementCreateReq(
        @NotNull(message = "项目ID不能为空") Long projectId,
        @NotBlank(message = "项目名称不能为空") String projectName,
        String projectCode,
        String projectKnowledgeId,
        @NotBlank(message = "报告类型不能为空") String reportType,
        String summary) {
}

