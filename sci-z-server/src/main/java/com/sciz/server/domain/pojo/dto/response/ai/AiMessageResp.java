package com.sciz.server.domain.pojo.dto.response.ai;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 消息响应
 *
 * @author shihangshang
 * @className AiMessageResp
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiMessageResp {
    /**
     * 消息ID
     */
    private String id;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 角色(user/assistant)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * Dify消息ID
     */
    private String difyMessageId;

    /**
     * 知识来源(JSON)
     */
    private String sources;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
