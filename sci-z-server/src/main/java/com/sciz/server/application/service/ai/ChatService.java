package com.sciz.server.application.service.ai;

import com.sciz.server.domain.pojo.dto.request.chat.ChatWorkflowRunReq;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话应用服务
 * 
 * @author JiaWen.Wu
 * @className ChatService
 * @date 2025-10-29 10:00
 */
public interface ChatService {
    
    /**
     * 执行 Dify 工作流或直接调用 Chatbot 流式对话
     *
     * @param req 工作流执行请求
     * @param userId 当前登录用户ID
     * @return 流式响应（SSE格式）
     */
    SseEmitter runWorkflow(ChatWorkflowRunReq req, Long userId);
}
