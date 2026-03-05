package com.sciz.server.domain.pojo.dto.response.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内消息详情（含接受/拒绝所需字段）
 *
 * @author Sci-Z
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDetailResp {

    private Long id;
    private String type;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String title;
    private String content;
    private String status;
    private String extraJson;
    private LocalDateTime createdTime;
    private LocalDateTime readTime;
}
