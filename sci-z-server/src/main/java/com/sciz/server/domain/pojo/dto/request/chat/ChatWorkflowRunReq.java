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
     * 用户问题（必填）
     */
    @Schema(description = "用户问题", example = "你好")
    @NotBlank(message = "用户问题不能为空")
    private String query;

    /**
     * 知识库ID（可选，支持多个，用逗号分隔或传数组）
     */
    @Schema(description = "知识库ID列表", example = "knowledge_base_001,knowledge_base_002")
    private String[] knowledgeIds;

    /**
     * 工作流ID（可选，仅在传文件时使用）
     */
    @Schema(description = "工作流ID", example = "workflow-id")
    private String workflowId;

    /**
     * 上传的文件列表（可选，不传则直接调用 chatbot）
     */
    @Schema(description = "上传的文件列表")
    private List<MultipartFile> files;

    /**
     * 会话ID（可选，用于 chatbot 流式对话）
     */
    @Schema(description = "会话ID", example = "conversation-001")
    private String conversationId;

    /**
     * 用户标识（可选）
     */
    @Schema(description = "用户标识", example = "user-001")
    private String user;
}



