package com.sciz.server.infrastructure.external.dify.impl;

import com.sciz.server.infrastructure.external.dify.DifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基于 HTTP 的 Dify 实现（占位实现）
 * 
 * 说明：
 * - 实际项目中请通过配置（baseUrl、apiKey）+ RestTemplate/WebClient 实现调用
 * - 这里提供最小可用骨架，确保依赖注入与调用路径清晰
 * 
 * @author JiaWen.Wu
 * 
 * @className HttpDifyServiceImpl
 * @date 2025-10-29 10:00
 */
@Slf4j
@Service
public class HttpDifyServiceImpl implements DifyService {

    @Override
    public String invokeChat(String appId, String userId, String input) {
        log.info(String.format("[Dify] invokeChat appId=%s, userId=%s, input=%s...", appId, userId, input));
        // TODO 调用 Dify Chat API 并返回内容
        return "[mocked] dify response for: " + input;
    }

    @Override
    public String runWorkflow(String workflowId, String payload) {
        log.info(String.format("[Dify] runWorkflow workflowId=%s, payload=%s...", workflowId, payload));
        // TODO 调用 Dify Workflow API 并返回运行结果
        return "{\"status\":\"ok\"}";
    }

    @Override
    public void syncKnowledge(String knowledgeBaseId, String documentId, String content) {
        log.info(String.format("[Dify] syncKnowledge kbId=%s, docId=%s, contentLength=%s...", knowledgeBaseId,
                documentId,
                String.valueOf(content == null ? 0 : content.length())));
        // TODO 调用 Dify KB API 同步文档
    }
}
