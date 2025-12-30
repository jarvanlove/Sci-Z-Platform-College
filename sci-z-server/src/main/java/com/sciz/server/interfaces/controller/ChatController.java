package com.sciz.server.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.sciz.server.application.service.ai.ChatService;
import com.sciz.server.domain.pojo.dto.request.chat.ChatWorkflowRunReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI对话控制器
 *
 * @author shihangshang
 * @className ChatController
 * @date 2025-01-27 10:00
 */
@Slf4j
@Tag(name = "AI对话控制器", description = "AI对话相关接口")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 执行 Dify 工作流或直接调用 Chatbot 流式对话
     *
     * 请求参数：
     * - query: 用户问题（必填）
     * - knowledgeId: 知识库ID（可选，支持多个，用逗号分隔或传数组，不传则不使用知识库）
     * - workflowId: 工作流ID（可选，仅在传文件时使用）
     * - files: 上传的文件列表（可选，不传则直接调用 chatbot）
     * - conversationId: 会话ID（可选）
     * - user: 用户标识（可选）
     *
     * 执行流程：
     * 1. 如果没有文件：
     *    - 如果有 knowledgeId，调用 /knowledge/chatbot/stream 接口（基于知识库提问）
     *    - 如果没有 knowledgeId，直接调用 chatbot 流式接口（不使用知识库）
     * 2. 如果有文件：
     *    - 必须有 knowledgeId（用于上传文件）
     *    - 上传文件到 Dify，获取文件ID
     *    - 构建工作流 inputs，将文件ID填入
     *    - 执行工作流
     *    - 从工作流 outputs.text 中获取数据
     *    - 使用 outputs 数据调用 chatbot 流式接口
     * 3. 流式返回给前端
     *
     * @param query 用户问题（必填）
     * @param knowledgeIds 知识库ID（可选，支持多个，用逗号分隔或传数组）
     * @param workflowId 工作流ID（可选，仅在传文件时使用）
     * @param files 上传的文件列表（可选）
     * @param conversationId 会话ID（可选）
     * @param user 用户标识（可选）
     * @return 流式响应（SSE格式）
     */
    @Operation(summary = "执行 Dify 工作流或直接调用 Chatbot 流式对话", 
               description = "支持两种模式：1. 不传文件时直接调用 chatbot 流式接口；2. 传文件时执行工作流后再调用 chatbot。支持多个知识库ID，用逗号分隔或传数组")
    @PostMapping(value = "/workflow/run", produces = "text/event-stream")
    public SseEmitter runWorkflow(
            @RequestParam("query") String query,
            @RequestParam(value = "knowledgeId", required = false) String[] knowledgeIds,
            @RequestParam(value = "workflowId", required = false) String workflowId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "user", required = false) String user) {
        
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 2. 构建请求对象
        ChatWorkflowRunReq req = new ChatWorkflowRunReq();
        req.setQuery(query);
        req.setKnowledgeIds(knowledgeIds);
        req.setWorkflowId(workflowId);
        req.setFiles(files);
        req.setConversationId(conversationId);
        req.setUser(String.valueOf(userId));
        
        // 3. 调用 Service 处理业务逻辑
        return chatService.runWorkflow(req, userId);
    }
}
