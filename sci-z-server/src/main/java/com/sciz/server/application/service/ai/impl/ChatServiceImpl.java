package com.sciz.server.application.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.ai.AiConversationService;
import com.sciz.server.application.service.ai.ChatService;
import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.domain.pojo.dto.request.chat.ChatWorkflowRunReq;
import com.sciz.server.domain.pojo.dto.request.file.FileSyncDifyReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeChatbotStreamReq;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotMessageRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotModelConfigRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyFileUploadResponse;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowRequest;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.context.AsyncUserContext;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserContext;
import com.sciz.server.infrastructure.shared.utils.MinioUtil;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 对话应用服务实现类
 * 
 * @author JiaWen.Wu
 * @className ChatServiceImpl
 * @date 2025-10-29 10:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final DifyApiService difyApiService;
    private final DifyApiKeyServiceImpl difyApiKeyService;
    private final KnowledgeService knowledgeService;
    private final AiConversationService aiConversationService;
    private final SysKnowledgeBaseRepo knowledgeBaseRepo;
    private final DifyConfig difyConfig;
    private final ObjectMapper objectMapper;
    private final SysAttachmentRepo sysAttachmentRepo;
    private final DifyWorkflowService difyWorkflowService;
    private final MinioClient minioClient;
    
    @org.springframework.beans.factory.annotation.Value("${minio.bucket-name:sciz-files}")
    private String bucketName;

    /**
     * 执行 Dify 工作流或直接调用 Chatbot 流式对话
     *
     * @param req    工作流执行请求
     * @param userId 当前登录用户ID
     * @return 流式响应（SSE格式）
     */
    @Override
    public SseEmitter runWorkflow(ChatWorkflowRunReq req, Long userId) {
        String query = req.getQuery();
        String[] knowledgeIds = req.getKnowledgeIds();
        String workflowId = req.getWorkflowId();
        List<org.springframework.web.multipart.MultipartFile> files = req.getFiles();
        String conversationId = req.getConversationId();
        String user = req.getUser();
        Long attachmentId = req.getAttachmentId();
        
        // 1. 如果提供了 attachmentId，从 MinIO 获取文件并解析
        if (attachmentId != null) {
            log.info(String.format("检测到 attachmentId，开始处理文件解析: attachmentId=%s, userId=%s", attachmentId, userId));
            try {
                // 1.1 查询附件信息
                SysAttachment attachment = sysAttachmentRepo.findById(attachmentId);
                if (attachment == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_FOUND, "附件不存在: " + attachmentId);
                }
                
                // 1.2 从 MinIO 下载文件
                GetObjectResponse fileResponse = MinioUtil.download(minioClient, bucketName, attachment.getFilePath());
                
                // 1.3 读取文件内容并转换为 MultipartFile
                byte[] fileBytes;
                try (InputStream inputStream = fileResponse) {
                    fileBytes = inputStream.readAllBytes();
                }
                MultipartFile multipartFile = new ByteArrayMultipartFile(
                        attachment.getOriginalName(),
                        fileBytes,
                        attachment.getMimeType()
                );
                
                // 1.4 上传文件到 Dify（使用 file key）
                String fileResourceId = workflowId != null ? workflowId : "work_file"; // 如果没有 workflowId，使用默认值
                FileSyncDifyReq syncReq = new FileSyncDifyReq(multipartFile, fileResourceId, "file");
                var syncResp = difyWorkflowService.syncFileToDify(syncReq);
                String difyFileId = syncResp.difyFileId();
                
                log.info(String.format("文件已上传到 Dify: difyFileId=%s, attachmentId=%s", difyFileId, attachmentId));
                
                // 1.5 调用 Dify 文件解析工作流（使用传入的 workflowId 或默认值）
                String parseWorkflowId = workflowId != null ? workflowId : fileResourceId;
                String parsedText = executeFileParseWorkflow(difyFileId, userId, parseWorkflowId, "file");
                
                // 1.6 如果解析结果不为空，拼接到 query 中
                if (StringUtils.hasText(parsedText)) {
                    query = query + "\n\n" + parsedText;
                    log.info(String.format("文件解析结果已拼接到 query，长度: %d 字符", parsedText.length()));
                } else {
                    log.warn(String.format("文件解析结果为空: attachmentId=%s, difyFileId=%s", attachmentId, difyFileId));
                }
                
            } catch (Exception e) {
                log.error(String.format("处理 attachmentId 失败: attachmentId=%s, err=%s", attachmentId, e.getMessage()), e);
                throw new BusinessException(ResultCode.SERVER_ERROR, "处理附件文件失败: " + e.getMessage());
            }
        }

        boolean hasFiles = files != null && !files.isEmpty() && files.stream().anyMatch(file -> !file.isEmpty());

        // 1.1 解析 knowledgeIds（支持数组或逗号分隔的字符串）
        List<String> knowledgeIdList = parseKnowledgeIds(knowledgeIds);

        log.info(String.format("执行 Dify 工作流或 Chatbot 流式对话，用户: %s, 问题: %s, 知识库ID: %s, 工作流ID: %s, 文件数量: %d",
                userId, query, knowledgeIdList, workflowId, hasFiles ? files.size() : 0));

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
                log.info(String.format("无文件，直接调用知识库 Chatbot 流式接口: knowledgeId=%s, query=%s", firstKnowledgeId, query));
                KnowledgeChatbotStreamReq knowledgeReq = new KnowledgeChatbotStreamReq();
                knowledgeReq.setKnowledgeId(firstKnowledgeId);
                knowledgeReq.setQuery(query);
                if (StringUtils.hasText(difyConversationId)) {
                    knowledgeReq.setConversationId(difyConversationId);
                }
                if (StringUtils.hasText(user)) {
                    knowledgeReq.setUser(user);
                }
                return knowledgeService.chatbotStream(knowledgeReq);
            } else {
                // 2.2 如果没有 knowledgeId，直接调用 chatbot（不使用知识库）
                log.info(String.format("无文件且无知识库ID，直接调用 Chatbot 流式接口: query=%s", query));
                return callChatbotDirectly(query, difyConversationId, user, userId);
            }
        }

        // 3. 如果有文件，执行工作流流程
        // 3.1 如果没有 knowledgeId，使用 key_type=file 的API key 上传文件并执行工作流
        if (knowledgeIdList.isEmpty()) {
            return handleWorkflowWithFileKey(userId, query, workflowId, files, conversationId, user);
        }

        // 3.2 如果有 knowledgeId，使用知识库上传文件并执行工作流
        // 3.2.1 更新chatbot的多个知识库
        updateChatbotKnowledgeBases(userId, knowledgeIdList);

        // 使用第一个知识库ID上传文件（文件上传到第一个知识库）
        String firstKnowledgeId = knowledgeIdList.get(0);

        // 3.2.2 创建 SSE Emitter（超时时间设置为60秒）
        SseEmitter emitter = new SseEmitter(60000L);

        // 3.2.3 异步处理
        // 构建用户上下文，用于异步线程
        LoginUserContext userContext = LoginUserContext.of(
                userId,
                user != null ? user : String.valueOf(userId),
                user != null ? user : String.valueOf(userId),
                null, null, null, null, null);
        new Thread(() -> {
            try {
                // 设置异步用户上下文，使 LoginUserUtil 和 DataPermissionUtil 在异步线程中也能正常工作
                AsyncUserContext.set(userContext);

                // 上传文件到 Dify，获取文件ID列表
                List<String> fileIds = uploadFilesToDify(userId, files, firstKnowledgeId);

                // 构建工作流 inputs，将文件ID填入
                Map<String, Object> inputs = buildWorkflowInputs(fileIds);

                // 构建工作流请求
                DifyWorkflowRequest workflowRequest = buildWorkflowRequest(userId, workflowId, firstKnowledgeId, inputs,
                        user);

                // 执行工作流
                log.info(String.format("执行 Dify 工作流: workflowId=%s", workflowRequest.getResourceId()));
                ResponseEntity<String> workflowResponse = difyApiService.runWorkflowWithDynamicKey(
                        workflowRequest, userId, workflowRequest.getResourceId());

                if (!workflowResponse.getStatusCode().is2xxSuccessful() || workflowResponse.getBody() == null) {
                    throw new BusinessException(ResultCode.SERVER_ERROR,
                            "工作流执行失败: " + workflowResponse.getBody());
                }

                // 解析工作流响应，获取 outputs.text 数据
                String workflowQuery = parseWorkflowResponse(workflowResponse.getBody());

                log.info(String.format("工作流执行成功，获取到输出文本，长度: %d 字符", workflowQuery.length()));

                // 使用工作流输出的文本作为最终的 query（声明为 final 供 lambda 使用）
                final String finalQuery = workflowQuery;

                // 使用 outputs 数据调用 chatbot 流式接口
                String difyConversationId = convertToDifyConversationId(conversationId, userId);
                callChatbotStreamWithQuery(emitter, userId, finalQuery, difyConversationId, user);

                log.info(String.format("Dify 工作流+Chatbot 流式对话完成: userId=%s, knowledgeIds=%s", userId, knowledgeIdList));

            } catch (Exception e) {
                log.error(String.format("Dify 工作流+Chatbot 流式对话失败: userId=%s, knowledgeIds=%s, err=%s",
                        userId, knowledgeIdList, e.getMessage()), e);
                handleStreamError(emitter, e);
            } finally {
                // 清理异步用户上下文（防止内存泄漏）
                AsyncUserContext.clear();
            }
        }).start();

        return emitter;
    }

    /**
     * 处理使用 file key 的工作流执行（无知识库ID的情况）
     *
     * @param userId         用户ID
     * @param query          用户问题
     * @param workflowId     工作流ID
     * @param files          文件列表
     * @param conversationId 会话ID
     * @param user           用户标识
     * @return 流式响应
     */
    private SseEmitter handleWorkflowWithFileKey(Long userId, String query, String workflowId,
            List<MultipartFile> files,
            String conversationId, String user) {
        // 获取 key_type=file 的API key
        List<DifyApiKey> fileKeys = difyApiKeyService.getUserApiKeysByType(userId, "file");
        if (fileKeys == null || fileKeys.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 file 类型的 API Key");
        }
        DifyApiKey fileKey = fileKeys.get(0);
        String fileResourceId = fileKey.getResourceId();

        log.info(String.format("有文件但无知识库ID，使用 key_type=file 的API key 上传文件并执行工作流: resourceId=%s, query=%s",
                fileResourceId, query));

        // 创建 SSE Emitter（超时时间设置为60秒）
        SseEmitter emitter = new SseEmitter(60000L);

        // 异步处理
        // 构建用户上下文，用于异步线程
        LoginUserContext userContext = LoginUserContext.of(
                userId,
                user != null ? user : String.valueOf(userId),
                user != null ? user : String.valueOf(userId),
                null, null, null, null, null);
        new Thread(() -> {
            try {
                // 设置异步用户上下文，使 LoginUserUtil 和 DataPermissionUtil 在异步线程中也能正常工作
                AsyncUserContext.set(userContext);

                // 上传文件到 Dify，获取文件ID列表（使用 key_type=file）
                List<String> fileIds = new ArrayList<>();
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue;
                    }
                    log.info(String.format("上传文件到 Dify（使用 file key）: 文件名=%s, 大小=%d bytes",
                            file.getOriginalFilename(), file.getSize()));

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
                        log.info(String.format("文件上传成功: 文件ID=%s", uploadResult.getId()));
                    }
                }

                if (fileIds.isEmpty()) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "没有成功上传的文件");
                }

                // 构建工作流 inputs，将文件ID填入
                Map<String, Object> inputs = buildWorkflowInputs(fileIds);

                // 构建工作流请求（使用 key_type=file）
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

                // 执行工作流（使用 key_type=file）
                log.info(String.format("执行 Dify 工作流（使用 file key）: workflowId=%s", workflowRequest.getResourceId()));
                ResponseEntity<String> workflowResponse = difyApiService.runWorkflowWithDynamicKey(
                        workflowRequest, userId, workflowRequest.getResourceId(), "file");

                if (!workflowResponse.getStatusCode().is2xxSuccessful() || workflowResponse.getBody() == null) {
                    throw new BusinessException(ResultCode.SERVER_ERROR,
                            "工作流执行失败: " + workflowResponse.getBody());
                }

                // 解析工作流响应，获取 outputs.text 数据
                String workflowQuery = parseWorkflowResponse(workflowResponse.getBody());

                log.info(String.format("工作流执行成功，获取到输出文本，长度: %d 字符", workflowQuery.length()));

                // 使用工作流输出的文本加用户提问词为作为最终的 query（声明为 final 供 lambda 使用）
                final String finalQuery = query + workflowQuery;

                // 使用工作流输出的 query 调用 chatbot 流式接口
                String difyConversationId = convertToDifyConversationId(conversationId, userId);
                // 查找用户的 Chatbot
                List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
                if (chatbotKeys == null || chatbotKeys.isEmpty()) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 Chatbot 应用");
                }
                DifyApiKey chatbotKey = chatbotKeys.get(0);
                String chatbotAppId = chatbotKey.getResourceId();
                log.info(String.format("找到用户Chatbot: userId=%s, appId=%s", userId, chatbotAppId));
                // 更新chatbot的知识库ID为空（不使用知识库）
                updateChatbotKnowledgeBaseToEmpty(chatbotAppId);

                // 调用 chatbot 流式接口并转发响应
                callChatbotStreamWithQuery(emitter, userId, finalQuery, difyConversationId, user, chatbotAppId);

                log.info(String.format("使用 file key 执行工作流+Chatbot 流式对话完成: userId=%s, query=%s", userId, finalQuery));

            } catch (Exception e) {
                log.error(String.format("使用 file key 执行工作流+Chatbot 流式对话失败: userId=%s, err=%s",
                        userId, e.getMessage()), e);
                handleStreamError(emitter, e);
            } finally {
                // 清理异步用户上下文（防止内存泄漏）
                AsyncUserContext.clear();
            }
        }).start();

        return emitter;
    }

    /**
     * 上传文件到 Dify
     *
     * @param userId      用户ID
     * @param files       文件列表
     * @param knowledgeId 知识库ID
     * @return 文件ID列表
     */
    private List<String> uploadFilesToDify(Long userId, List<MultipartFile> files, String knowledgeId) {
        List<String> fileIds = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            log.info(String.format("上传文件到 Dify: 文件名=%s, 大小=%d bytes", file.getOriginalFilename(), file.getSize()));

            ResponseEntity<String> uploadResponse = difyApiService.uploadFileWithDynamicKey(
                    userId, file, userId, knowledgeId);

            if (!uploadResponse.getStatusCode().is2xxSuccessful() || uploadResponse.getBody() == null) {
                throw new BusinessException(ResultCode.SERVER_ERROR,
                        "文件上传失败: " + uploadResponse.getBody());
            }

            // 解析上传响应，获取文件ID
            try {
                DifyFileUploadResponse uploadResult = objectMapper.readValue(
                        uploadResponse.getBody(), DifyFileUploadResponse.class);
                if (uploadResult.getId() != null) {
                    fileIds.add(uploadResult.getId());
                    log.info(String.format("文件上传成功: 文件ID=%s", uploadResult.getId()));
                }
            } catch (Exception e) {
                log.error(String.format("解析文件上传响应失败: err=%s", e.getMessage()), e);
                throw new BusinessException(ResultCode.SERVER_ERROR, "解析文件上传响应失败");
            }
        }

        if (fileIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "没有成功上传的文件");
        }

        return fileIds;
    }

    /**
     * 构建工作流输入参数
     *
     * @param fileIds 文件ID列表
     * @return 工作流输入参数
     */
    private Map<String, Object> buildWorkflowInputs(List<String> fileIds) {
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
        return inputs;
    }

    /**
     * 构建工作流请求
     *
     * @param userId      用户ID
     * @param workflowId  工作流ID
     * @param knowledgeId 知识库ID
     * @param inputs      输入参数
     * @param user        用户标识
     * @return 工作流请求
     */
    private DifyWorkflowRequest buildWorkflowRequest(Long userId, String workflowId, String knowledgeId,
            Map<String, Object> inputs, String user) {
        DifyWorkflowRequest workflowRequest = new DifyWorkflowRequest();
        workflowRequest.setUserId(userId);
        workflowRequest.setResourceId(workflowId != null ? workflowId : knowledgeId);
        workflowRequest.setKeyType("workflow");
        workflowRequest.setInputs(inputs);
        workflowRequest.setResponseMode("blocking");
        if (StringUtils.hasText(user)) {
            workflowRequest.setUser(user);
        } else {
            workflowRequest.setUser(String.valueOf(userId));
        }
        return workflowRequest;
    }

    /**
     * 解析工作流响应，获取输出文本
     *
     * @param responseBody 响应体
     * @return 输出文本
     */
    private String parseWorkflowResponse(String responseBody) {
        try {
            JsonNode workflowResult = objectMapper.readTree(responseBody);
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

            return workflowQuery;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("解析工作流响应失败: err=%s", e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "解析工作流响应失败: " + e.getMessage());
        }
    }

    /**
     * 调用 Chatbot 流式接口并转发响应
     *
     * @param emitter            SSE Emitter
     * @param userId             用户ID
     * @param query              查询文本
     * @param difyConversationId Dify 会话ID
     * @param user               用户标识
     */
    private void callChatbotStreamWithQuery(SseEmitter emitter, Long userId, String query,
            String difyConversationId, String user) {
        // 查找用户的 Chatbot
        List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
        if (chatbotKeys == null || chatbotKeys.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 Chatbot 应用");
        }
        DifyApiKey chatbotKey = chatbotKeys.get(0);
        String chatbotAppId = chatbotKey.getResourceId();
        log.info(String.format("找到用户Chatbot: userId=%s, appId=%s", userId, chatbotAppId));

        callChatbotStreamWithQuery(emitter, userId, query, difyConversationId, user, chatbotAppId);
    }

    /**
     * 调用 Chatbot 流式接口并转发响应（指定 Chatbot App ID）
     *
     * @param emitter            SSE Emitter
     * @param userId             用户ID
     * @param query              查询文本
     * @param difyConversationId Dify 会话ID
     * @param user               用户标识
     * @param chatbotAppId       Chatbot 应用ID
     */
    private void callChatbotStreamWithQuery(SseEmitter emitter, Long userId, String query,
            String difyConversationId, String user, String chatbotAppId) {
        // 构建 Chatbot 消息请求
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
                    log.debug(String.format("收到SSE事件: %s", eventType));
                } else {
                    // 如果不是标准 SSE 格式，直接发送原始数据
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(trimmedLine));
                }
            } catch (Exception e) {
                log.warn(String.format("处理流式数据行失败: line=%s, err=%s", line, e.getMessage()));
            }
        });

        // 发送完成事件
        try {
            emitter.send(SseEmitter.event()
                    .name("message_end")
                    .data("{}"));
            emitter.complete();
        } catch (Exception e) {
            log.error("发送完成事件失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 直接调用 Chatbot 流式接口（不使用知识库）
     *
     * @param query          用户问题
     * @param conversationId 会话ID（可选）
     * @param user           用户标识（可选）
     * @param userId         当前登录用户ID
     * @return 流式响应（SSE格式）
     */
    private SseEmitter callChatbotDirectly(String query, String conversationId, String user, Long userId) {
        // 1. 创建 SSE Emitter（超时时间设置为60秒）
        SseEmitter emitter = new SseEmitter(60000L);

        // 2. 异步处理
        // 构建用户上下文，用于异步线程
        LoginUserContext userContext = LoginUserContext.of(
                userId,
                user != null ? user : String.valueOf(userId),
                user != null ? user : String.valueOf(userId),
                null, null, null, null, null);
        new Thread(() -> {
            try {
                // 设置异步用户上下文，使 LoginUserUtil 和 DataPermissionUtil 在异步线程中也能正常工作
                AsyncUserContext.set(userContext);

                // 2.1 查找用户的 Chatbot
                List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
                if (chatbotKeys == null || chatbotKeys.isEmpty()) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建 Chatbot 应用");
                }
                DifyApiKey chatbotKey = chatbotKeys.get(0);
                String chatbotAppId = chatbotKey.getResourceId();
                log.info(String.format("找到用户Chatbot: userId=%s, appId=%s", userId, chatbotAppId));

                // 更新chatbot的知识库ID为空（不使用知识库）
                updateChatbotKnowledgeBaseToEmpty(chatbotAppId);

                // 转换系统内部的 conversationId 为 Dify 的 UUID（如果存在）
                String difyConversationId = convertToDifyConversationId(conversationId, userId);

                // 调用 chatbot 流式接口并转发响应
                callChatbotStreamWithQuery(emitter, userId, query, difyConversationId, user, chatbotAppId);

                log.info(String.format("Chatbot 流式对话完成: userId=%s, query=%s", userId, query));

            } catch (Exception e) {
                log.error(String.format("Chatbot 流式对话失败: userId=%s, query=%s, err=%s",
                        userId, query, e.getMessage()), e);
                handleStreamError(emitter, e);
            } finally {
                // 清理异步用户上下文（防止内存泄漏）
                AsyncUserContext.clear();
            }
        }).start();

        return emitter;
    }

    /**
     * 处理流式响应错误
     *
     * @param emitter SSE Emitter
     * @param e       异常
     */
    private void handleStreamError(SseEmitter emitter, Exception e) {
        try {
            String errorMessage = String.format("{\"error\": true, \"message\": \"%s\"}", e.getMessage());
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorMessage));
        } catch (Exception sendException) {
            log.error("发送错误消息失败", sendException);
        }
        emitter.completeWithError(e);
    }

    /**
     * 将系统内部的 conversationId（数字）转换为 Dify 的 UUID
     * 
     * @param conversationId 系统内部的会话ID（可能是数字字符串或 UUID）
     * @param userId         当前用户ID
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
                    log.debug(String.format("找到 Dify conversationId: internalId=%s, difyConversationId=%s",
                            internalId, conversationResp.getDifyConversationId()));
                    return conversationResp.getDifyConversationId();
                } else {
                    log.debug(String.format("会话存在但未关联 Dify conversationId: internalId=%s", internalId));
                    return null;
                }
            } catch (Exception e) {
                log.warn(String.format("查询会话失败，将不传递 conversationId 给 Dify: internalId=%s, err=%s",
                        internalId, e.getMessage()));
                return null;
            }
        } catch (NumberFormatException e) {
            // 如果不是数字，可能是 UUID 格式，直接返回
            log.debug(String.format("conversationId 不是数字，可能是 UUID 格式: conversationId=%s", conversationId));
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
     * @param userId       用户ID
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
                log.debug(String.format("用户未创建Chatbot: userId=%s", userId));
                return;
            }

            DifyApiKey chatbotKey = chatbotKeys.get(0);
            String chatbotAppId = chatbotKey.getResourceId();
            log.info(String.format("找到用户Chatbot: userId=%s, appId=%s", userId, chatbotAppId));

            // 2. 查询所有知识库的 Dify ID
            List<String> difyKnowledgeIds = new ArrayList<>();
            for (String knowledgeId : knowledgeIds) {
                try {
                    Long id = Long.parseLong(knowledgeId);
                    // 查询知识库实体获取 Dify ID
                    SysKnowledgeBase knowledgeBase = knowledgeBaseRepo.findById(id);
                    if (knowledgeBase != null && StringUtils.hasText(knowledgeBase.getDifyKnowdataId())) {
                        difyKnowledgeIds.add(knowledgeBase.getDifyKnowdataId());
                        log.debug(String.format("找到知识库 Dify ID: knowledgeId=%s, difyKnowledgeId=%s",
                                id, knowledgeBase.getDifyKnowdataId()));
                    } else {
                        log.warn(String.format("知识库不存在或 Dify ID 为空: knowledgeId=%s", id));
                    }
                } catch (NumberFormatException e) {
                    // 如果不是数字，可能是 Dify ID，直接使用
                    difyKnowledgeIds.add(knowledgeId);
                    log.debug(String.format("使用传入的 Dify ID: knowledgeId=%s", knowledgeId));
                }
            }

            // 3. 更新 Chatbot 的知识库配置
            if (!difyKnowledgeIds.isEmpty()) {
                updateChatbotKnowledgeBases(chatbotAppId, difyKnowledgeIds);
            }
        } catch (Exception e) {
            log.warn(String.format("更新Chatbot知识库失败: userId=%s, knowledgeIds=%s, err=%s",
                    userId, knowledgeIds, e.getMessage()));
            // 不抛出异常，允许继续执行
        }
    }

    /**
     * 更新 Chatbot 的多个知识库ID（Dify ID）
     * 
     * @param chatbotAppId     Chatbot 应用ID
     * @param difyKnowledgeIds Dify 知识库ID列表
     */
    private void updateChatbotKnowledgeBases(String chatbotAppId, List<String> difyKnowledgeIds) {
        try {
            // 1. 构建多个数据集配置
            List<DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper> datasetList = new ArrayList<>();
            for (String difyKnowledgeId : difyKnowledgeIds) {
                DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset dataset = new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset();
                dataset.setEnabled(true);
                dataset.setId(difyKnowledgeId);

                DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper datasetWrapper = new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper();
                datasetWrapper.setDataset(dataset);
                datasetList.add(datasetWrapper);
            }

            DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection datasetCollection = new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection();
            datasetCollection.setDatasets(datasetList);

            // 2. 构建数据集配置（使用默认配置）
            DifyChatbotModelConfigRequest.DatasetConfigs datasetConfigs = DifyChatbotModelConfigRequest.DatasetConfigs
                    .defaultConfig();
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
            ResponseEntity<String> updateResponse = difyApiService.updateChatbotModelConfig(chatbotAppId,
                    configRequest);

            if (!updateResponse.getStatusCode().is2xxSuccessful()) {
                String errorBody = updateResponse.getBody() != null ? updateResponse.getBody() : "Unknown error";
                log.warn(String.format("更新Chatbot多个知识库ID失败: chatbotAppId=%s, knowledgeIds=%s, status=%s, body=%s",
                        chatbotAppId, difyKnowledgeIds, updateResponse.getStatusCode(), errorBody));
                // 不抛出异常，允许继续执行
            } else {
                log.info(String.format("更新Chatbot多个知识库ID成功: chatbotAppId=%s, knowledgeIds=%s", chatbotAppId,
                        difyKnowledgeIds));
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 网络连接异常（如连接被拒绝、超时等）
            log.warn(String.format("更新Chatbot多个知识库ID失败（网络连接异常）: chatbotAppId=%s, knowledgeIds=%s, err=%s",
                    chatbotAppId, difyKnowledgeIds, e.getMessage()));
            // 不抛出异常，允许继续执行
        } catch (Exception e) {
            // 其他异常
            log.warn(String.format("更新Chatbot多个知识库ID失败（未知异常）: chatbotAppId=%s, knowledgeIds=%s, err=%s",
                    chatbotAppId, difyKnowledgeIds, e.getMessage()));
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

            DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection datasetCollection = new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection();
            datasetCollection.setDatasets(datasetList);

            // 2. 构建数据集配置（使用默认配置，但数据集列表为空）
            DifyChatbotModelConfigRequest.DatasetConfigs datasetConfigs = DifyChatbotModelConfigRequest.DatasetConfigs
                    .defaultConfig();
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
            ResponseEntity<String> updateResponse = difyApiService.updateChatbotModelConfig(chatbotAppId,
                    configRequest);

            if (!updateResponse.getStatusCode().is2xxSuccessful()) {
                String errorBody = updateResponse.getBody() != null ? updateResponse.getBody() : "Unknown error";
                log.warn(String.format("更新Chatbot知识库ID为空失败: chatbotAppId=%s, status=%s, body=%s",
                        chatbotAppId, updateResponse.getStatusCode(), errorBody));
                // 不抛出异常，允许继续执行，因为这不是关键操作
            } else {
                log.info(String.format("更新Chatbot知识库ID为空成功: chatbotAppId=%s", chatbotAppId));
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 网络连接异常（如连接被拒绝、超时等）
            log.warn(String.format("更新Chatbot知识库ID为空失败（网络连接异常）: chatbotAppId=%s, err=%s",
                    chatbotAppId, e.getMessage()));
            // 不抛出异常，允许继续执行
        } catch (Exception e) {
            // 其他异常
            log.warn(String.format("更新Chatbot知识库ID为空失败（未知异常）: chatbotAppId=%s, err=%s",
                    chatbotAppId, e.getMessage()));
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
            model.setProvider(
                    configModel.getProvider() != null ? configModel.getProvider() : "langgenius/tongyi/tongyi");
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
        DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel rerankingModel = new DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel();

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

    /**
     * 执行文件解析工作流
     *
     * @param difyFileId String Dify 文件ID
     * @param userId     Long 用户ID
     * @param workflowId String 工作流ID
     * @param keyType    String 密钥类型（workflow/file）
     * @return String 解析后的文本内容
     */
    private String executeFileParseWorkflow(String difyFileId, Long userId, String workflowId, String keyType) {
        try {
            log.info(String.format("开始执行文件解析工作流: difyFileId=%s, workflowId=%s", difyFileId, workflowId));
            
            // 1. 构建工作流输入参数（使用文件ID，参考 buildWorkflowInputs 方法的格式）
            List<String> fileIds = List.of(difyFileId);
            Map<String, Object> inputs = buildWorkflowInputs(fileIds);
            
            // 2. 构建工作流请求
            DifyWorkflowRequest workflowRequest = new DifyWorkflowRequest();
            workflowRequest.setUserId(userId);
            workflowRequest.setResourceId(workflowId);
            workflowRequest.setKeyType(keyType);
            workflowRequest.setInputs(inputs);
            workflowRequest.setResponseMode("blocking");
            workflowRequest.setUser(String.valueOf(userId));
            
            // 3. 执行工作流
            ResponseEntity<String> workflowResponse = difyApiService.runWorkflowWithDynamicKey(
                    workflowRequest, userId, workflowId, keyType);
            
            if (!workflowResponse.getStatusCode().is2xxSuccessful() || workflowResponse.getBody() == null) {
                throw new BusinessException(ResultCode.SERVER_ERROR, 
                        "文件解析工作流执行失败: " + workflowResponse.getBody());
            }
            
            // 4. 解析工作流响应，获取 outputs.text 数据
            String parsedText = parseWorkflowResponse(workflowResponse.getBody());
            
            log.info(String.format("文件解析工作流执行成功: difyFileId=%s, 解析文本长度=%d", 
                    difyFileId, parsedText != null ? parsedText.length() : 0));
            
            return parsedText;
            
        } catch (Exception e) {
            log.error(String.format("执行文件解析工作流失败: difyFileId=%s, workflowId=%s, err=%s", 
                    difyFileId, workflowId, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "文件解析工作流执行失败: " + e.getMessage());
        }
    }

    /**
     * 字节数组 MultipartFile 实现（用于从 MinIO 下载的文件）
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String fileName;
        private final byte[] content;
        private final String contentType;

        public ByteArrayMultipartFile(String fileName, byte[] content, String contentType) {
            this.fileName = fileName;
            this.content = content;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content != null ? content.length : 0;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public org.springframework.core.io.Resource getResource() {
            return new org.springframework.core.io.ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
        }

        @Override
        public void transferTo(java.io.File dest) {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }
}
