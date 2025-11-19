package com.sciz.server.domain.pojo.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 消息创建请求
 *
 * @author shihangshang
 * @className AiMessageCreateReq
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiMessageCreateReq {
    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    /**
     * 角色(user/assistant)
     */
    @NotBlank(message = "角色不能为空")
    private String role;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * Dify消息ID（可选）
     */
    private String difyMessageId;

    /**
     * 知识来源(JSON字符串，可选)
     */
    private String sources;

    /**
     * 置信度（可选）
     */
    private BigDecimal confidence;

    /**
     * 发送时间（可选，为空时使用当前时间）
     */
    private LocalDateTime sendTime;
}
