package com.sciz.server.infrastructure.external.dify.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dify聊天响应DTO
 *
 * @author JiaWen.Wu
 * @className DifyChatResp
 * @date 2025-10-29 10:30
 */
@Data
public class DifyChatResp {

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 元数据
     */
    private Object metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 使用情况
     */
    private Usage usage;

    /**
     * 建议问题
     */
    private List<String> suggestedQuestions;

    @Data
    public static class Usage {
        /**
         * 提示词tokens
         */
        private Integer promptTokens;

        /**
         * 完成tokens
         */
        private Integer completionTokens;

        /**
         * 总tokens
         */
        private Integer totalTokens;
    }
}
