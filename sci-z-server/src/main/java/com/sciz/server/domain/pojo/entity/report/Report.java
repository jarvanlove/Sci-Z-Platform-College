package com.sciz.server.domain.pojo.entity.report;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className Report
 * @date 2025-10-29 10:00
 */
@Data
public class Report {
    private Long id;
    private String reportName;
    private String reportType;
    private Long projectId;
    private Long authorId;
    private String content;
    private String filePath;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
