package com.sciz.server.domain.pojo.entity.ai;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className Message
 * @date 2025-10-29 10:00
 */
@Data
public class Message {
    private Long id;
    private String conversationId;
    private String messageId;
    private String content;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
