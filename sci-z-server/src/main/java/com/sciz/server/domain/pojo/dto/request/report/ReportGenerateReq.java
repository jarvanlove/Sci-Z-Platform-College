package com.sciz.server.domain.pojo.dto.request.report;

import lombok.Data;

/**
 * @author JiaWen.Wu
 * @className ReportGenerateReq
 * @date 2025-10-29 10:00
 */
@Data
public class ReportGenerateReq {
    private String reportName;
    private String reportType;
    private Long projectId;
    private String content;
}
