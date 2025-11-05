package com.sciz.server.infrastructure.shared.event.report;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 报告生成事件
 *
 * @author JiaWen.Wu
 * @className ReportGeneratedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class ReportGeneratedEvent extends DomainEvent {

    private String reportId;
    private String reportName;
    private String reportType;
    private String projectId;
    private String projectName;
    private String creatorId;
    private String creatorName;
    private String status;
    private String filePath;
    private String fileSize;

    public ReportGeneratedEvent(String reportId, String reportName, String reportType,
            String projectId, String projectName, String creatorId,
            String creatorName, String status, String filePath, String fileSize) {
        super();
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportType = reportType;
        this.projectId = projectId;
        this.projectName = projectName;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.status = status;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    @Override
    public String getAggregateId() {
        return reportId;
    }

    @Override
    public String getAggregateType() {
        return "Report";
    }
}
