package com.sciz.server.domain.pojo.entity.knowledge;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className KnowledgeFile
 * @date 2025-10-29 10:00
 */
@Data
public class KnowledgeFile {
    private Long id;
    private Long knowledgeBaseId;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
