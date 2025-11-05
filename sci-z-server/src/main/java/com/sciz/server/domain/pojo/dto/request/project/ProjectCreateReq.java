package com.sciz.server.domain.pojo.dto.request.project;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className ProjectCreateReq
 * @date 2025-10-29 10:00
 */
@Data
public class ProjectCreateReq {
    private String projectName;
    private String projectCode;
    private String description;
    private Long leaderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
