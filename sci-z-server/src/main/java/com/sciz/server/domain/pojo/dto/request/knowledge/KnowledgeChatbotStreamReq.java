package com.sciz.server.domain.pojo.dto.request.knowledge;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库 Chatbot 流式对话请求
 *
 * @author ShiHang.Shang
 * @className KnowledgeChatbotStreamReq
 * @date 2025-01-28 17:00
 */
@Data
public class KnowledgeChatbotStreamReq {

    /**
     * 知识库ID（Dify知识库ID，String类型）
     */
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeId;

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String query;

    /**
     * 会话ID（可选）
     */
    private String conversationId;

    /**
     * 用户标识（可选）
     */
    private String user;
}

