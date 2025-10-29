package com.sciz.server.domain.pojo.entity.report;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className ReportTemplate
 * @date 2025-10-29 10:00
 */
@Data
public class ReportTemplate {
    private Long id;
    private String templateName;
    private String templateType;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
