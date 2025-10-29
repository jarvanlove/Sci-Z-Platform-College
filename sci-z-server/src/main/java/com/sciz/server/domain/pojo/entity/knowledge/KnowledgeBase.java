package com.sciz.server.domain.pojo.entity.knowledge;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className KnowledgeBase
 * @date 2025-10-29 10:00
 */
@Data
public class KnowledgeBase {
    private Long id;
    private String knowledgeName;
    private String description;
    private Long ownerId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
