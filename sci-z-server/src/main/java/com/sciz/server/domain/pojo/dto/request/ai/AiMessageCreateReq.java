package com.sciz.server.domain.pojo.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "会话ID不能为空")
    private String conversationId;

    /**
     * 角色(user/assistant)
     */
    @NotBlank(message = "角色不能为空")
    @Size(max = 10, message = "角色长度不能超过10个字符")
    private String role;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * Dify消息ID（可选）
     */
    @Size(max = 100, message = "Dify消息ID长度不能超过100个字符")
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
}

