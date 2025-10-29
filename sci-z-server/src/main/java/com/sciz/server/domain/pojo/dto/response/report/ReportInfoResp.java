package com.sciz.server.domain.pojo.dto.response.report;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className ReportInfoResp
 * @date 2025-01-27 10:00
 */
@Data
public class ReportInfoResp {
    private Long id;
    private String reportName;
    private String reportType;
    private Long projectId;
    private String projectName;
    private Long authorId;
    private String authorName;
    private String content;
    private String filePath;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
}
