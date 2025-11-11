package com.server.agentbackendservices.modules.dify.controller;

import com.server.agentbackendservices.modules.dify.dto.DifyChatbotAppRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyChatbotAppResponse;
import com.server.agentbackendservices.modules.dify.dto.DifyChatbotModelConfigRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyChatbotMessageRequest;
import com.server.agentbackendservices.modules.dify.service.DifyApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Dify Chatbot 应用控制器
 * @author
 * @since 2025-11-10
 */
@Slf4j
@RestController
@RequestMapping("/api/dify")
@RequiredArgsConstructor
@Tag(name = "Dify Chatbot API", description = "Dify Chatbot 应用管理接口")
public class DifyChatbotController {
    private final DifyApiService difyApiService;
    /**
     * 创建 Chatbot 应用
     */
    @PostMapping("/chatbot/apps")
    @Operation(summary = "创建 Dify Chatbot 应用")
    public ResponseEntity<DifyChatbotAppResponse> createChatbotApp(@Valid @RequestBody DifyChatbotAppRequest request) {
        log.info("开始创建 Dify Chatbot 应用: {}", request.getName());
        return difyApiService.createChatbotApp(request);
    }
    /**
     * 更新 Chatbot 应用模型配置
     */
    @PostMapping("/chatbot/apps/{appId}/model-config")
    @Operation(summary = "更新 Dify Chatbot 应用模型配置")
    public ResponseEntity<String> updateChatbotModelConfig(
            @PathVariable String appId,
            @Valid @RequestBody DifyChatbotModelConfigRequest config) {
        log.info("更新 Dify Chatbot 应用模型配置, appId={}", appId);
        return difyApiService.updateChatbotModelConfig(appId, config);
    }

    /**
     * Chatbot 阻塞式对话
     */
    @PostMapping("/chatbot/messages")
    @Operation(summary = "Chatbot 阻塞式对话")
    public ResponseEntity<String> sendChatbotMessage(@Valid @RequestBody DifyChatbotMessageRequest request) {
        request.setResponseMode("blocking");
        return difyApiService.sendChatbotMessage(request);
    }

    /**
     * Chatbot 流式对话
     */
    @PostMapping(value = "/chatbot/messages/stream")
    @Operation(summary = "Chatbot 流式对话（SSE）")
    public ResponseEntity<String> sendChatbotMessageStream(@Valid @RequestBody DifyChatbotMessageRequest request) {
        request.setResponseMode("streaming");
        return difyApiService.sendChatbotMessage(request);
    }
}

