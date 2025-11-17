package com.sciz.server.domain.pojo.dto.request.ai;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 会话创建请求
 *
 * @author shihangshang
 * @className AiConversationCreateReq
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiConversationCreateReq {

    /**
     * 会话标题（可选，为空时默认为"新建对话"）
     */
    @Size(max = 255, message = "会话标题长度不能超过255个字符")
    private String title;

    /**
     * Dify会话ID（可选）
     */
    @Size(max = 100, message = "Dify会话ID长度不能超过100个字符")
    private String difyConversationId;

    /**
     * 是否置顶(0:否,1:是)
     */
    private Integer isPinned = 0;
}

