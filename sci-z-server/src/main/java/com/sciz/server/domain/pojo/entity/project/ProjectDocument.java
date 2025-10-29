package com.sciz.server.domain.pojo.entity.project;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className ProjectDocument
 * @date 2025-10-29 10:00
 */
@Data
public class ProjectDocument {
    private Long id;
    private Long projectId;
    private String documentName;
    private String documentType;
    private String filePath;
    private Long fileSize;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
