package com.sciz.server.domain.pojo.entity.project;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className ProjectMember
 * @date 2025-10-29 10:00
 */
@Data
public class ProjectMember {
    private Long id;
    private Long projectId;
    private Long userId;
    private String role;
    private Integer status;
    private LocalDateTime joinTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
