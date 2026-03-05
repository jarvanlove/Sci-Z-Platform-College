package com.sciz.server.domain.pojo.dto.response.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内消息列表项
 *
 * @author Sci-Z
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageListResp {

    private Long id;
    private String type;
    private Long senderId;
    private String senderName;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime readTime;
    /** 扩展 JSON 原始串，前端可解析 */
    private String extraJson;
}
