package com.sciz.server.domain.pojo.dto.request.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 聊天工作流执行请求
 *
 * @author shihangshang
 * @className ChatWorkflowRunReq
 * @date 2025-01-28 15:00
 */
@Data
@Schema(description = "聊天工作流执行请求")
public class ChatWorkflowRunReq {

    /**
     * 知识库ID（Dify知识库ID，String类型）
     */
    @Schema(description = "知识库ID", required = true, example = "knowledge_base_001")
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeId;

    /**
     * 工作流ID（可选，如果不指定则使用API Token绑定的默认工作流）
     */
    @Schema(description = "工作流ID", example = "workflow-id")
    private String workflowId;

    /**
     * 响应模式（blocking 或 streaming）
     */
    @Schema(description = "响应模式", example = "blocking", allowableValues = {"blocking", "streaming"})
    private String responseMode = "blocking";

    /**
     * 用户标识（可选）
     */
    @Schema(description = "用户标识", example = "user-001")
    private String user;

    /**
     * 上传的文件列表（支持单文件或多文件）
     */
    @Schema(description = "上传的文件列表", required = true)
    private List<MultipartFile> files;

    /**
     * 会话ID（可选，用于 chatbot 流式对话）
     */
    @Schema(description = "会话ID", example = "conversation-001")
    private String conversationId;
}



