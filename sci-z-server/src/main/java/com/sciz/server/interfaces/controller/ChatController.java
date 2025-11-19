package com.sciz.server.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.ai.AiConversationService;
import com.sciz.server.application.service.ai.ChatService;
import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeChatbotStreamReq;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotMessageRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotModelConfigRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyFileUploadResponse;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowRequest;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final DifyApiService difyApiService;
    private final DifyApiKeyServiceImpl difyApiKeyService;
    private final KnowledgeService knowledgeService;
    private final AiConversationService aiConversationService;
    private final SysKnowledgeBaseRepo knowledgeBaseRepo;
    private final DifyConfig difyConfig;
    private final ObjectMapper objectMapper;

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
     * @param knowledgeId 知识库ID（可选，支持多个，用逗号分隔或传数组）
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
        boolean hasFiles = files != null && !files.isEmpty() && files.stream().anyMatch(file -> !file.isEmpty());
        
        // 1.1 解析 knowledgeIds（支持数组或逗号分隔的字符串）
        List<String> knowledgeIdList = parseKnowledgeIds(knowledgeIds);
        
        log.info("执行 Dify 工作流或 Chatbot 流式对话，用户: {}, 问题: {}, 知识库ID: {}, 工作流ID: {}, 文件数量: {}", 
                userId, query, knowledgeIdList, workflowId, hasFiles ? files.size() : 0);

        // 2. 如果没有文件，直接调用 chatbot 流式接口
        if (!hasFiles) {
            // 2.0 转换系统内部的 conversationId 为 Dify 的 UUID（如果存在）
            String difyConversationId = convertToDifyConversationId(conversationId, userId);
            
            // 2.1 如果有 knowledgeId，更新chatbot的知识库并调用知识库 chatbot 流式接口
            if (!knowledgeIdList.isEmpty()) {
                // 更新chatbot的多个知识库
                updateChatbotKnowledgeBases(userId, knowledgeIdList);
                
                // 使用第一个知识库ID调用流式接口（Dify API 只支持单个知识库ID）
                String firstKnowledgeId = knowledgeIdList.get(0);
                log.info("无文件，直接调用知识库 Chatbot 流式接口: knowledgeId={}, query={}", firstKnowledgeId, query);
                KnowledgeChatbotStreamReq req = new KnowledgeChatbotStreamReq();
                req.setKnowledgeId(firstKnowledgeId);
                req.setQuery(query);
                if (StringUtils.hasText(difyConversationId)) {
                    req.setConversationId(difyConversationId);
                }
                if (StringUtils.hasText(user)) {
                    req.setUser(user);
                }
                return knowledgeService.chatbotStream(req);
            } else {
                // 2.2 如果没有 knowledgeId，直接调用 chatbot（不使用知识库）
                log.info("无文件且无知识库ID，直接调用 Chatbot 流式接口: query={}", query);
                return callChatbotDirectly(query, difyConversationId, user, userId);
            }
        }

        // 3. 如果有文件，执行工作流流程
        // 3.1 如果没有 knowledgeId，使用 key_type=file 的API key 上传文件并执行工作流
        if (knowledgeIdList.isEmpty()) {
            // 获取 key_type=file 的API key
            List<DifyApiKey> fileKeys = difyApiKeyService.getUserApiKeysByType(userId, "file");
            if (fileKeys == null || fileKeys.isEmpty()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 file 类型的 API Key");
            }
            DifyApiKey fileKey = fileKeys.get(0);
            String fileResourceId = fileKey.getResourceId();
            
            log.info("有文件但无知识库ID，使用 key_type=file 的API key 上传文件并执行工作流: resourceId={}, query={}", 
                    fileResourceId, query);
            
            // 创建 SSE Emitter（超时时间设置为60秒）
            SseEmitter emitter = new SseEmitter(60000L);
            
            // 异步处理
            new Thread(() -> {
                try {
                    // 3.1.1 上传文件到 Dify，获取文件ID列表（使用 key_type=file）
                    List<String> fileIds = new ArrayList<>();
                    for (MultipartFile file : files) {
                        if (file.isEmpty()) {
                            continue;
                        }
                        log.info("上传文件到 Dify（使用 file key）: 文件名={}, 大小={} bytes", 
                                file.getOriginalFilename(), file.getSize());
                        
                        ResponseEntity<String> uploadResponse = difyApiService.uploadFileWithDynamicKey(
                                userId, file, userId, fileResourceId, "file");
                        
                        if (!uploadResponse.getStatusCode().is2xxSuccessful() || uploadResponse.getBody() == null) {
                            throw new BusinessException(ResultCode.SERVER_ERROR, 
                                    "文件上传失败: " + uploadResponse.getBody());
                        }
                        
                        // 解析上传响应，获取文件ID
                        DifyFileUploadResponse uploadResult = objectMapper.readValue(
                                uploadResponse.getBody(), DifyFileUploadResponse.class);
                        if (uploadResult.getId() != null) {
                            fileIds.add(uploadResult.getId());
                            log.info("文件上传成功: 文件ID={}", uploadResult.getId());
                        }
                    }
                    
                    if (fileIds.isEmpty()) {
                        throw new BusinessException(ResultCode.BAD_REQUEST, "没有成功上传的文件");
                    }
                    
                    // 3.1.2 构建工作流 inputs，将文件ID填入
                    List<Map<String, Object>> fileInputs = new ArrayList<>();
                    for (String fileId : fileIds) {
                        Map<String, Object> fileInput = new HashMap<>();
                        fileInput.put("type", "document");
                        fileInput.put("transfer_method", "local_file");
                        fileInput.put("upload_file_id", fileId);
                        fileInputs.add(fileInput);
                    }
                    
                    Map<String, Object> inputs = new HashMap<>();
                    inputs.put("file", fileInputs);
                    
                    // 3.1.3 构建工作流请求（使用 key_type=file）
                    DifyWorkflowRequest workflowRequest = new DifyWorkflowRequest();
                    workflowRequest.setUserId(userId);
                    workflowRequest.setResourceId(workflowId != null ? workflowId : fileResourceId);
                    workflowRequest.setKeyType("file");
                    workflowRequest.setInputs(inputs);
                    workflowRequest.setResponseMode("blocking");
                    if (StringUtils.hasText(user)) {
                        workflowRequest.setUser(user);
                    } else {
                        workflowRequest.setUser(String.valueOf(userId));
                    }
                    
                    // 3.1.4 执行工作流（使用 key_type=file）
                    log.info("执行 Dify 工作流（使用 file key）: workflowId={}", workflowRequest.getResourceId());
                    ResponseEntity<String> workflowResponse = difyApiService.runWorkflowWithDynamicKey(
                            workflowRequest, userId, workflowRequest.getResourceId(), "file");
                    
                    if (!workflowResponse.getStatusCode().is2xxSuccessful() || workflowResponse.getBody() == null) {
                        throw new BusinessException(ResultCode.SERVER_ERROR, 
                                "工作流执行失败: " + workflowResponse.getBody());
                    }
                    
                    // 3.1.5 解析工作流响应，获取 outputs.text 数据
                    JsonNode workflowResult = objectMapper.readTree(workflowResponse.getBody());
                    JsonNode dataNode = workflowResult.get("data");
                    if (dataNode == null) {
                        throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：缺少 data 字段");
                    }
                    
                    JsonNode outputsNode = dataNode.get("outputs");
                    if (outputsNode == null) {
                        throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：缺少 outputs 字段");
                    }
                    
                    JsonNode textNode = outputsNode.get("text");
                    if (textNode == null || !textNode.isArray() || textNode.size() == 0) {
                        throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：outputs.text 为空或不是数组");
                    }
                    
                    // 获取第一个文本内容作为 workflowQuery
                    String workflowQuery = textNode.get(0).asText();
                    if (!StringUtils.hasText(workflowQuery)) {
                        throw new BusinessException(ResultCode.SERVER_ERROR, "工作流输出文本为空");
                    }
                    
                    log.info("工作流执行成功，获取到输出文本，长度: {} 字符", workflowQuery.length());
                    
                    // 使用工作流输出的文本加用户提问词为作为最终的 query（声明为 final 供 lambda 使用）
                    final String finalQuery =  query+ workflowQuery;
                    // 3.1.6 使用工作流输出的 query 调用 chatbot 流式接口
                    String difyConversationId = convertToDifyConversationId(conversationId, userId);
                    // 查找用户的 Chatbot
                    List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
                    if (chatbotKeys == null || chatbotKeys.isEmpty()) {
                        throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 Chatbot 应用");
                    }
                    DifyApiKey chatbotKey = chatbotKeys.get(0);
                    String chatbotAppId = chatbotKey.getResourceId();
                    log.info("找到用户Chatbot: userId={}, appId={}", userId, chatbotAppId);
                    // 更新chatbot的知识库ID为空（不使用知识库）
                    updateChatbotKnowledgeBaseToEmpty(chatbotAppId);
                    // 构建 Chatbot 消息请求
                    DifyChatbotMessageRequest chatbotRequest = new DifyChatbotMessageRequest();
                    chatbotRequest.setUserId(userId);
                    chatbotRequest.setResourceId(chatbotAppId);
                    chatbotRequest.setKeyType("chatbot");
                    chatbotRequest.setQuery(finalQuery);
                    chatbotRequest.setResponseMode("streaming");
                    if (StringUtils.hasText(difyConversationId)) {
                        chatbotRequest.setConversationId(difyConversationId);
                    }
                    if (StringUtils.hasText(user)) {
                        chatbotRequest.setUser(user);
                    } else {
                        chatbotRequest.setUser(String.valueOf(userId));
                    }
                    // 调用 chatbot 流式接口并转发响应
                    difyApiService.sendChatbotMessageStream(chatbotRequest, line -> {
                        try {
                            String trimmedLine = line.trim();
                            if (trimmedLine.isEmpty()) {
                                return;
                            }
                            
                            // 处理 SSE 格式的数据行
                            if (trimmedLine.startsWith("data:")) {
                                String data = trimmedLine.substring(5).trim();
                                if (!data.isEmpty() && !data.equals("[DONE]")) {
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(data));
                                }
                            } else if (trimmedLine.startsWith("event:")) {
                                // 处理事件类型
                                String eventType = trimmedLine.substring(6).trim();
                                log.debug("收到SSE事件: {}", eventType);
                            } else {
                                // 如果不是标准 SSE 格式，直接发送原始数据
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(trimmedLine));
                            }
                        } catch (Exception e) {
                            log.warn("处理流式数据行失败: line={}, err={}", line, e.getMessage());
                        }
                    });
                    
                    // 发送完成事件
                    emitter.send(SseEmitter.event()
                            .name("message_end")
                            .data("{}"));
                    emitter.complete();
                    
                    log.info("使用 file key 执行工作流+Chatbot 流式对话完成: userId={}, query={}", userId, finalQuery);
                    
                } catch (Exception e) {
                    log.error("使用 file key 执行工作流+Chatbot 流式对话失败: userId={}, err={}", 
                            userId, e.getMessage(), e);
                    try {
                        String errorMessage = String.format("{\"error\": true, \"message\": \"%s\"}", 
                                e.getMessage());
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(errorMessage));
                    } catch (Exception sendException) {
                        log.error("发送错误消息失败", sendException);
                    }
                    emitter.completeWithError(e);
                }
            }).start();
            
            return emitter;
        }
        
        // 3.1.1 更新chatbot的多个知识库
        updateChatbotKnowledgeBases(userId, knowledgeIdList);
        
        // 使用第一个知识库ID上传文件（文件上传到第一个知识库）
        String firstKnowledgeId = knowledgeIdList.get(0);

        // 3.2 创建 SSE Emitter（超时时间设置为60秒）
        SseEmitter emitter = new SseEmitter(60000L);

        // 3.3 异步处理
        new Thread(() -> {
            try {

                // 3.2 上传文件到 Dify，获取文件ID列表
                List<String> fileIds = new ArrayList<>();
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue;
                    }
                    log.info("上传文件到 Dify: 文件名={}, 大小={} bytes", file.getOriginalFilename(), file.getSize());
                    
                    ResponseEntity<String> uploadResponse = difyApiService.uploadFileWithDynamicKey(
                            userId, file, userId, firstKnowledgeId);
                    
                    if (!uploadResponse.getStatusCode().is2xxSuccessful() || uploadResponse.getBody() == null) {
                        throw new BusinessException(ResultCode.SERVER_ERROR, 
                                "文件上传失败: " + uploadResponse.getBody());
                    }
                    
                    // 解析上传响应，获取文件ID
                    DifyFileUploadResponse uploadResult = objectMapper.readValue(
                            uploadResponse.getBody(), DifyFileUploadResponse.class);
                    if (uploadResult.getId() != null) {
                        fileIds.add(uploadResult.getId());
                        log.info("文件上传成功: 文件ID={}", uploadResult.getId());
                    }
                }

                if (fileIds.isEmpty()) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "没有成功上传的文件");
                }

                // 3.3 构建工作流 inputs，将文件ID填入
                List<Map<String, Object>> fileInputs = new ArrayList<>();
                for (String fileId : fileIds) {
                    Map<String, Object> fileInput = new HashMap<>();
                    fileInput.put("type", "document");
                    fileInput.put("transfer_method", "local_file");
                    fileInput.put("upload_file_id", fileId);
                    fileInputs.add(fileInput);
                }
                
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("file", fileInputs);

                // 3.4 构建工作流请求
                DifyWorkflowRequest workflowRequest = new DifyWorkflowRequest();
                workflowRequest.setUserId(userId);
                workflowRequest.setResourceId(workflowId != null ? workflowId : firstKnowledgeId);
                workflowRequest.setKeyType("workflow");
                workflowRequest.setInputs(inputs);
                workflowRequest.setResponseMode("blocking");
                if (StringUtils.hasText(user)) {
                    workflowRequest.setUser(user);
                } else {
                    workflowRequest.setUser(String.valueOf(userId));
                }

                // 3.5 执行工作流
                log.info("执行 Dify 工作流: workflowId={}", workflowRequest.getResourceId());
                ResponseEntity<String> workflowResponse = difyApiService.runWorkflowWithDynamicKey(
                        workflowRequest, userId, workflowRequest.getResourceId());

                if (!workflowResponse.getStatusCode().is2xxSuccessful() || workflowResponse.getBody() == null) {
                    throw new BusinessException(ResultCode.SERVER_ERROR, 
                            "工作流执行失败: " + workflowResponse.getBody());
                }

                // 3.6 解析工作流响应，获取 outputs.text 数据
                JsonNode workflowResult = objectMapper.readTree(workflowResponse.getBody());
                JsonNode dataNode = workflowResult.get("data");
                if (dataNode == null) {
                    throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：缺少 data 字段");
                }
                
                JsonNode outputsNode = dataNode.get("outputs");
                if (outputsNode == null) {
                    throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：缺少 outputs 字段");
                }
                
                JsonNode textNode = outputsNode.get("text");
                if (textNode == null || !textNode.isArray() || textNode.size() == 0) {
                    throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：outputs.text 为空或不是数组");
                }

                // 获取第一个文本内容作为 workflowQuery
                String workflowQuery = textNode.get(0).asText();
                if (!StringUtils.hasText(workflowQuery)) {
                    throw new BusinessException(ResultCode.SERVER_ERROR, "工作流输出文本为空");
                }

                log.info("工作流执行成功，获取到输出文本，长度: {} 字符", workflowQuery.length());
                
                // 使用工作流输出的文本作为最终的 query（声明为 final 供 lambda 使用）
                final String finalQuery = workflowQuery;

                // 3.7 使用 outputs 数据调用 chatbot 流式接口
                // 查找用户的 Chatbot
                List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
                if (chatbotKeys == null || chatbotKeys.isEmpty()) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 Chatbot 应用");
                }
                DifyApiKey chatbotKey = chatbotKeys.get(0);

                String chatbotAppId = chatbotKey.getResourceId();
                log.info("找到用户Chatbot: userId={}, appId={}", userId, chatbotAppId);

                // 转换系统内部的 conversationId 为 Dify 的 UUID（如果存在）
                String difyConversationId = convertToDifyConversationId(conversationId, userId);
                
                // 构建 Chatbot 消息请求
                DifyChatbotMessageRequest chatbotRequest = new DifyChatbotMessageRequest();
                chatbotRequest.setUserId(userId);
                chatbotRequest.setResourceId(chatbotAppId);
                chatbotRequest.setKeyType("chatbot");
                chatbotRequest.setQuery(finalQuery);
                chatbotRequest.setResponseMode("streaming");
                if (StringUtils.hasText(difyConversationId)) {
                    chatbotRequest.setConversationId(difyConversationId);
                }
                if (StringUtils.hasText(user)) {
                    chatbotRequest.setUser(user);
                } else {
                    chatbotRequest.setUser(String.valueOf(userId));
                }

                // 3.8 调用 chatbot 流式接口并转发响应
                difyApiService.sendChatbotMessageStream(chatbotRequest, line -> {
                    try {
                        String trimmedLine = line.trim();
                        if (trimmedLine.isEmpty()) {
                            return;
                        }
                        
                        // 处理 SSE 格式的数据行
                        if (trimmedLine.startsWith("data:")) {
                            String data = trimmedLine.substring(5).trim();
                            if (!data.isEmpty() && !data.equals("[DONE]")) {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(data));
                            }
                        } else if (trimmedLine.startsWith("event:")) {
                            // 处理事件类型
                            String eventType = trimmedLine.substring(6).trim();
                            log.debug("收到SSE事件: {}", eventType);
                        } else {
                            // 如果不是标准 SSE 格式，直接发送原始数据
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(trimmedLine));
                        }
                    } catch (Exception e) {
                        log.warn("处理流式数据行失败: line={}, err={}", line, e.getMessage());
                    }
                });
                
                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("message_end")
                        .data("{}"));
                emitter.complete();
                
                log.info("Dify 工作流+Chatbot 流式对话完成: userId={}, knowledgeIds={}", userId, knowledgeIdList);
                
            } catch (Exception e) {
                log.error("Dify 工作流+Chatbot 流式对话失败: userId={}, knowledgeIds={}, err={}", 
                        userId, knowledgeIdList, e.getMessage(), e);
                try {
                    String errorMessage = String.format("{\"error\": true, \"message\": \"%s\"}", 
                            e.getMessage());
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(errorMessage));
                } catch (Exception sendException) {
                    log.error("发送错误消息失败", sendException);
                }
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 直接调用 Chatbot 流式接口（不使用知识库）
     *
     * @param query 用户问题
     * @param conversationId 会话ID（可选）
     * @param user 用户标识（可选）
     * @param userId 当前登录用户ID
     * @return 流式响应（SSE格式）
     */
    private SseEmitter callChatbotDirectly(String query, String conversationId, String user, Long userId) {
        // 1. 创建 SSE Emitter（超时时间设置为60秒）
        SseEmitter emitter = new SseEmitter(60000L);

        // 2. 异步处理
        new Thread(() -> {
            try {
                // 2.1 查找用户的 Chatbot
                List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
                if (chatbotKeys == null || chatbotKeys.isEmpty()) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 Chatbot 应用");
                }
                DifyApiKey chatbotKey = chatbotKeys.get(0);
                String chatbotAppId = chatbotKey.getResourceId();
                log.info("找到用户Chatbot: userId={}, appId={}", userId, chatbotAppId);

                // 更新chatbot的知识库ID为空（不使用知识库）
                updateChatbotKnowledgeBaseToEmpty(chatbotAppId);

                // 转换系统内部的 conversationId 为 Dify 的 UUID（如果存在）
                String difyConversationId = convertToDifyConversationId(conversationId, userId);
                
                // 2.2 构建 Chatbot 消息请求
                DifyChatbotMessageRequest chatbotRequest = new DifyChatbotMessageRequest();
                chatbotRequest.setUserId(userId);
                chatbotRequest.setResourceId(chatbotAppId);
                chatbotRequest.setKeyType("chatbot");
                chatbotRequest.setQuery(query);
                chatbotRequest.setResponseMode("streaming");
                if (StringUtils.hasText(difyConversationId)) {
                    chatbotRequest.setConversationId(difyConversationId);
                }
                if (StringUtils.hasText(user)) {
                    chatbotRequest.setUser(user);
                } else {
                    chatbotRequest.setUser(String.valueOf(userId));
                }

                // 2.3 调用 chatbot 流式接口并转发响应
                difyApiService.sendChatbotMessageStream(chatbotRequest, line -> {
                    try {
                        String trimmedLine = line.trim();
                        if (trimmedLine.isEmpty()) {
                            return;
                        }
                        
                        // 处理 SSE 格式的数据行
                        if (trimmedLine.startsWith("data:")) {
                            String data = trimmedLine.substring(5).trim();
                            if (!data.isEmpty() && !data.equals("[DONE]")) {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(data));
                            }
                        } else if (trimmedLine.startsWith("event:")) {
                            // 处理事件类型
                            String eventType = trimmedLine.substring(6).trim();
                            log.debug("收到SSE事件: {}", eventType);
                        } else {
                            // 如果不是标准 SSE 格式，直接发送原始数据
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(trimmedLine));
                        }
                    } catch (Exception e) {
                        log.warn("处理流式数据行失败: line={}, err={}", line, e.getMessage());
                    }
                });
                
                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("message_end")
                        .data("{}"));
                emitter.complete();
                
                log.info("Chatbot 流式对话完成: userId={}, query={}", userId, query);
                
            } catch (Exception e) {
                log.error("Chatbot 流式对话失败: userId={}, query={}, err={}", 
                        userId, query, e.getMessage(), e);
                try {
                    String errorMessage = String.format("{\"error\": true, \"message\": \"%s\"}", 
                            e.getMessage());
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(errorMessage));
                } catch (Exception sendException) {
                    log.error("发送错误消息失败", sendException);
                }
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 将系统内部的 conversationId（数字）转换为 Dify 的 UUID
     * 
     * @param conversationId 系统内部的会话ID（可能是数字字符串或 UUID）
     * @param userId 当前用户ID
     * @return Dify 的 UUID 格式的 conversationId，如果不存在则返回 null
     */
    private String convertToDifyConversationId(String conversationId, Long userId) {
        if (!StringUtils.hasText(conversationId)) {
            return null;
        }
        
        try {
            // 尝试解析为 Long（系统内部的会话ID）
            Long internalId = Long.parseLong(conversationId);
            
            // 查询会话实体，获取 difyConversationId
            try {
                var conversationResp = aiConversationService.findDetail(String.valueOf(internalId));
                if (conversationResp != null && StringUtils.hasText(conversationResp.getDifyConversationId())) {
                    log.debug("找到 Dify conversationId: internalId={}, difyConversationId={}", 
                            internalId, conversationResp.getDifyConversationId());
                    return conversationResp.getDifyConversationId();
                } else {
                    log.debug("会话存在但未关联 Dify conversationId: internalId={}", internalId);
                    return null;
                }
            } catch (Exception e) {
                log.warn("查询会话失败，将不传递 conversationId 给 Dify: internalId={}, err={}", 
                        internalId, e.getMessage());
                return null;
            }
        } catch (NumberFormatException e) {
            // 如果不是数字，可能是 UUID 格式，直接返回
            log.debug("conversationId 不是数字，可能是 UUID 格式: conversationId={}", conversationId);
            return conversationId;
        }
    }

    /**
     * 解析 knowledgeIds 参数（支持数组或逗号分隔的字符串）
     * 
     * @param knowledgeIds 知识库ID数组
     * @return 知识库ID列表
     */
    private List<String> parseKnowledgeIds(String[] knowledgeIds) {
        List<String> result = new ArrayList<>();
        if (knowledgeIds == null || knowledgeIds.length == 0) {
            return result;
        }
        
        for (String knowledgeId : knowledgeIds) {
            if (StringUtils.hasText(knowledgeId)) {
                // 支持逗号分隔的字符串
                String[] ids = knowledgeId.split(",");
                for (String id : ids) {
                    String trimmedId = id.trim();
                    if (!trimmedId.isEmpty() && !result.contains(trimmedId)) {
                        result.add(trimmedId);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 更新 Chatbot 的多个知识库ID
     * 
     * @param userId 用户ID
     * @param knowledgeIds 知识库ID列表（系统内部的ID）
     */
    private void updateChatbotKnowledgeBases(Long userId, List<String> knowledgeIds) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return;
        }
        
        try {
            // 1. 查询用户的 Chatbot
            List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
            if (chatbotKeys == null || chatbotKeys.isEmpty()) {
                log.debug("用户未创建Chatbot: userId={}", userId);
                return;
            }
            
            DifyApiKey chatbotKey = chatbotKeys.get(0);
            String chatbotAppId = chatbotKey.getResourceId();
            log.info("找到用户Chatbot: userId={}, appId={}", userId, chatbotAppId);
            
            // 2. 查询所有知识库的 Dify ID
            List<String> difyKnowledgeIds = new ArrayList<>();
            for (String knowledgeId : knowledgeIds) {
                try {
                    Long id = Long.parseLong(knowledgeId);
                    // 查询知识库实体获取 Dify ID
                    SysKnowledgeBase knowledgeBase = knowledgeBaseRepo.findById(id);
                    if (knowledgeBase != null && StringUtils.hasText(knowledgeBase.getDifyKnowdataId())) {
                        difyKnowledgeIds.add(knowledgeBase.getDifyKnowdataId());
                        log.debug("找到知识库 Dify ID: knowledgeId={}, difyKnowledgeId={}", id, knowledgeBase.getDifyKnowdataId());
                    } else {
                        log.warn("知识库不存在或 Dify ID 为空: knowledgeId={}", id);
                    }
                } catch (NumberFormatException e) {
                    // 如果不是数字，可能是 Dify ID，直接使用
                    difyKnowledgeIds.add(knowledgeId);
                    log.debug("使用传入的 Dify ID: knowledgeId={}", knowledgeId);
                }
            }
            
            // 3. 更新 Chatbot 的知识库配置
            if (!difyKnowledgeIds.isEmpty()) {
                updateChatbotKnowledgeBases(chatbotAppId, difyKnowledgeIds);
            }
        } catch (Exception e) {
            log.warn("更新Chatbot知识库失败: userId={}, knowledgeIds={}, err={}", 
                    userId, knowledgeIds, e.getMessage());
            // 不抛出异常，允许继续执行
        }
    }

    /**
     * 更新 Chatbot 的多个知识库ID（Dify ID）
     * 
     * @param chatbotAppId Chatbot 应用ID
     * @param difyKnowledgeIds Dify 知识库ID列表
     */
    private void updateChatbotKnowledgeBases(String chatbotAppId, List<String> difyKnowledgeIds) {
        try {
            // 1. 构建多个数据集配置
            List<DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper> datasetList = new ArrayList<>();
            for (String difyKnowledgeId : difyKnowledgeIds) {
                DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset dataset = 
                        new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset();
                dataset.setEnabled(true);
                dataset.setId(difyKnowledgeId);
                
                DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper datasetWrapper = 
                        new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper();
                datasetWrapper.setDataset(dataset);
                datasetList.add(datasetWrapper);
            }
            
            DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection datasetCollection = 
                    new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection();
            datasetCollection.setDatasets(datasetList);

            // 2. 构建数据集配置（使用默认配置）
            DifyChatbotModelConfigRequest.DatasetConfigs datasetConfigs = 
                    DifyChatbotModelConfigRequest.DatasetConfigs.defaultConfig();
            datasetConfigs.setDatasets(datasetCollection);

            // 3. 从配置文件读取模型配置
            DifyChatbotModelConfigRequest.Model model = buildModelFromConfig();
            DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel rerankingModel = buildRerankingModelFromConfig();
            
            // 设置重排序模型到数据集配置
            datasetConfigs.setRerankingModel(rerankingModel);

            // 4. 构建模型配置请求
            DifyChatbotModelConfigRequest configRequest = new DifyChatbotModelConfigRequest();
            configRequest.setModel(model);
            configRequest.setDatasetConfigs(datasetConfigs);

            // 5. 调用 Dify API 更新配置
            ResponseEntity<String> updateResponse = difyApiService.updateChatbotModelConfig(chatbotAppId, configRequest);
            
            if (!updateResponse.getStatusCode().is2xxSuccessful()) {
                String errorBody = updateResponse.getBody() != null ? updateResponse.getBody() : "Unknown error";
                log.warn("更新Chatbot多个知识库ID失败: chatbotAppId={}, knowledgeIds={}, status={}, body={}", 
                        chatbotAppId, difyKnowledgeIds, updateResponse.getStatusCode(), errorBody);
                // 不抛出异常，允许继续执行
            } else {
                log.info("更新Chatbot多个知识库ID成功: chatbotAppId={}, knowledgeIds={}", chatbotAppId, difyKnowledgeIds);
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 网络连接异常（如连接被拒绝、超时等）
            log.warn("更新Chatbot多个知识库ID失败（网络连接异常）: chatbotAppId={}, knowledgeIds={}, err={}", 
                    chatbotAppId, difyKnowledgeIds, e.getMessage());
            // 不抛出异常，允许继续执行
        } catch (Exception e) {
            // 其他异常
            log.warn("更新Chatbot多个知识库ID失败（未知异常）: chatbotAppId={}, knowledgeIds={}, err={}", 
                    chatbotAppId, difyKnowledgeIds, e.getMessage());
            // 不抛出异常，允许继续执行
        }
    }

    /**
     * 更新 Chatbot 的知识库ID为空（不使用知识库）
     * 
     * @param chatbotAppId Chatbot 应用ID
     */
    private void updateChatbotKnowledgeBaseToEmpty(String chatbotAppId) {
        try {
            // 1. 构建空的数据集集合（不添加任何数据集）
            List<DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper> datasetList = new ArrayList<>();
            
            DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection datasetCollection = 
                    new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection();
            datasetCollection.setDatasets(datasetList);

            // 2. 构建数据集配置（使用默认配置，但数据集列表为空）
            DifyChatbotModelConfigRequest.DatasetConfigs datasetConfigs = 
                    DifyChatbotModelConfigRequest.DatasetConfigs.defaultConfig();
            datasetConfigs.setDatasets(datasetCollection);

            // 3. 从配置文件读取模型配置
            DifyChatbotModelConfigRequest.Model model = buildModelFromConfig();
            DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel rerankingModel = buildRerankingModelFromConfig();
            
            // 设置重排序模型到数据集配置
            datasetConfigs.setRerankingModel(rerankingModel);

            // 4. 构建模型配置请求
            DifyChatbotModelConfigRequest configRequest = new DifyChatbotModelConfigRequest();
            configRequest.setModel(model);
            configRequest.setDatasetConfigs(datasetConfigs);

            // 5. 调用 Dify API 更新配置
            ResponseEntity<String> updateResponse = difyApiService.updateChatbotModelConfig(chatbotAppId, configRequest);
            
            if (!updateResponse.getStatusCode().is2xxSuccessful()) {
                String errorBody = updateResponse.getBody() != null ? updateResponse.getBody() : "Unknown error";
                log.warn("更新Chatbot知识库ID为空失败: chatbotAppId={}, status={}, body={}", 
                        chatbotAppId, updateResponse.getStatusCode(), errorBody);
                // 不抛出异常，允许继续执行，因为这不是关键操作
            } else {
                log.info("更新Chatbot知识库ID为空成功: chatbotAppId={}", chatbotAppId);
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 网络连接异常（如连接被拒绝、超时等）
            log.warn("更新Chatbot知识库ID为空失败（网络连接异常）: chatbotAppId={}, err={}", 
                    chatbotAppId, e.getMessage());
            // 不抛出异常，允许继续执行
        } catch (Exception e) {
            // 其他异常
            log.warn("更新Chatbot知识库ID为空失败（未知异常）: chatbotAppId={}, err={}", 
                    chatbotAppId, e.getMessage());
            // 不抛出异常，允许继续执行
        }
    }
    /**
     * 从配置文件构建模型配置
     *
     * @return 模型配置
     */
    private DifyChatbotModelConfigRequest.Model buildModelFromConfig() {
        DifyChatbotModelConfigRequest.Model model = new DifyChatbotModelConfigRequest.Model();
        
        // 从配置文件读取，如果配置不存在则使用默认值
        if (difyConfig.getChatbot() != null && difyConfig.getChatbot().getModel() != null) {
            DifyConfig.Chatbot.Model configModel = difyConfig.getChatbot().getModel();
            model.setProvider(configModel.getProvider() != null ? configModel.getProvider() : "langgenius/tongyi/tongyi");
            model.setName(configModel.getName() != null ? configModel.getName() : "qwen3-next-80b-a3b-instruct");
            model.setMode(configModel.getMode() != null ? configModel.getMode() : "chat");
        } else {
            // 使用默认值
            model.setProvider("langgenius/tongyi/tongyi");
            model.setName("qwen3-next-80b-a3b-instruct");
            model.setMode("chat");
        }
        
        model.setCompletionParams(new HashMap<>());
        return model;
    }

    /**
     * 从配置文件构建重排序模型配置
     *
     * @return 重排序模型配置
     */
    private DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel buildRerankingModelFromConfig() {
        DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel rerankingModel = 
                new DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel();
        
        // 从配置文件读取，如果配置不存在则使用默认值
        if (difyConfig.getChatbot() != null && difyConfig.getChatbot().getRerankingModel() != null) {
            DifyConfig.Chatbot.RerankingModel configRerankingModel = difyConfig.getChatbot().getRerankingModel();
            rerankingModel.setRerankingProviderName(
                    configRerankingModel.getRerankingProviderName() != null 
                            ? configRerankingModel.getRerankingProviderName() 
                            : "langgenius/tongyi/tongyi");
            rerankingModel.setRerankingModelName(
                    configRerankingModel.getRerankingModelName() != null 
                            ? configRerankingModel.getRerankingModelName() 
                            : "gte-rerank");
        } else {
            // 使用默认值
            rerankingModel.setRerankingProviderName("langgenius/tongyi/tongyi");
            rerankingModel.setRerankingModelName("gte-rerank");
        }
        
        return rerankingModel;
    }
}
