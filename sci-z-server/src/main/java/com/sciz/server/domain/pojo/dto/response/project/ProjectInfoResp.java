package com.sciz.server.domain.pojo.dto.response.project;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className ProjectInfoResp
 * @date 2025-01-27 10:00
 */
@Data
public class ProjectInfoResp {
    private Long id;
    private String projectName;
    private String projectCode;
    private String description;
    private Long leaderId;
    private String leaderName;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
