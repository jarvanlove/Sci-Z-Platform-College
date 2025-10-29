package com.sciz.server.domain.pojo.entity.project;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className Project
 * @date 2025-10-29 10:00
 */
@Data
public class Project {
    private Long id;
    private String projectName;
    private String projectCode;
    private String description;
    private Long leaderId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
