package com.sciz.server.domain.pojo.dto.response.ai;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * AI 会话响应
 *
 * @author shihangshang
 * @className AiConversationResp
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiConversationResp {
    /**
     * 会话ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * Dify会话ID
     */
    private String difyConversationId;

    /**
     * 是否置顶(0:否,1:是)
     */
    private Integer isPinned;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
