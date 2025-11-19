package com.sciz.server.domain.pojo.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * AI 消息更新请求
 *
 * @author shihangshang
 * @className AiMessageUpdateReq
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiMessageUpdateReq {
    /**
     * 消息ID
     */
    @NotBlank(message = "消息ID不能为空")
    private String id;

    /**
     * 会话ID（可选）
     */
    private String conversationId;

    /**
     * 角色(user/assistant)（可选）
     */
    private String role;

    /**
     * 消息内容（可选）
     */
    private String content;

    /**
     * 知识来源(JSON字符串，可选)
     */
    private String sources;

    /**
     * 置信度（可选）
     */
    private BigDecimal confidence;
}
