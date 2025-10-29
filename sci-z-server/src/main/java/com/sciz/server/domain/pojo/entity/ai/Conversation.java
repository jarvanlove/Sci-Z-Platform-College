package com.sciz.server.domain.pojo.entity.ai;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className Conversation
 * @date 2025-10-29 10:00
 */
@Data
public class Conversation {
    private Long id;
    private String conversationId;
    private Long userId;
    private String title;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
