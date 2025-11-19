package com.sciz.server.domain.pojo.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 会话更新请求
 *
 * @author shihangshang
 * @className AiConversationUpdateReq
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiConversationUpdateReq {

    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    private String id;

    /**
     * 会话标题（可选）
     */
    @Size(max = 255, message = "会话标题长度不能超过255个字符")
    private String title;

    /**
     * 是否置顶(0:否,1:是)（可选）
     */
    private Integer isPinned;
}
