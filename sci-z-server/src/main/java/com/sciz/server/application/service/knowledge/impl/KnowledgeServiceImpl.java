package com.sciz.server.application.service.knowledge.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeChatbotStreamReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeCreateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeResp;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotMessageRequest;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.external.dify.dto.DifyDatasetRequest;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.interfaces.converter.KnowledgeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 知识库应用服务实现类
 * 
 * @author ShiHang.Shang
 * @className KnowledgeServiceImpl
 * @date 2025-01-28 14:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final SysKnowledgeBaseRepo knowledgeBaseRepo;
    private final SysUserRepo userRepo;
    private final DifyApiService difyApiService;
    private final ObjectMapper objectMapper;
    private final DifyApiKeyServiceImpl difyApiKeyService;
    private final KnowledgeConverter knowledgeConverter;
    private final SysAttachmentRepo attachmentRepo;
    private final SysKnowledgeFileRelationRepo fileRelationRepo;

    @Value("${dify.default-resource-id:default}")
    private String defaultResourceId;

    /**
     * 创建知识库
     *
     * @param req 创建请求
     * @return 知识库响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeResp create(KnowledgeCreateReq req) {
//        // 1. 获取当前登录用户ID
//        if (!StpUtil.isLogin()) {
//            throw new BusinessException(ResultCode.UNAUTHORIZED);
//        }
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("创建知识库: userId=%s, name=%s", userId, req.getName()));

//        // 2. 查询用户信息
        SysUser user = userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 构建 Dify API 请求
        DifyDatasetRequest difyRequest = new DifyDatasetRequest();
        difyRequest.setName(userId+"_"+req.getName());
        difyRequest.setDescription(req.getDescription());

        // 4. 获取用户的 Dify API Key（用于调用 Dify API）

        // 查询用户的 DifyApiKey 实体（如果存在）
        DifyApiKey difyApiKey = null;
        QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)

                   .eq("key_type", "dataset")
                   .eq("is_active", true)
                   .last("LIMIT 1");
        difyApiKey = difyApiKeyService.getOne(queryWrapper);
        

        
        // 5. 确定 resourceId（优先使用 API Key 的 resourceId，否则使用默认值）
        String resourceId = (difyApiKey != null && difyApiKey.getResourceId() != null) 
            ? difyApiKey.getResourceId() 
            : defaultResourceId;
        
        // 6. 调用 Dify API 创建数据集
        ResponseEntity<String> response = difyApiService.createDataset(
                difyRequest, userId, resourceId, "dataset");

        // 7. 检查响应状态
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error(String.format("Dify API调用失败: status=%s, body=%s", 
                    response.getStatusCode(), response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, "创建知识库失败: Dify API调用失败"+String.format("Dify API调用失败: status=%s, body=%s",
                    response.getStatusCode(), response.getBody()));
        }

        // 8. 解析返回的JSON
        JsonNode responseJson;
        try {
            responseJson = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error(String.format("解析Dify API响应失败: body=%s, error=%s", response.getBody(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "解析Dify API响应失败");
        }

        // 9. 提取关键信息
        String difyKnowdataId = responseJson.has("id") ? responseJson.get("id").asText() : null;
        String difyName = responseJson.has("name") ? responseJson.get("name").asText() : req.getName();
        if (difyKnowdataId == null) {
            log.error(String.format("Dify API返回数据缺少id字段: body=%s", response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, "创建知识库失败: Dify API返回数据异常");
        }
        // 10. 将完整的返回数据存入callback字段（JSON格式）
        String callbackJson = response.getBody();
        // 11. 创建知识库实体
        SysKnowledgeBase entity = new SysKnowledgeBase();
        entity.setName(difyName);
        entity.setDescription(req.getDescription()); // 使用用户输入的描述
        entity.setOwnerId(userId);
        entity.setOwnerName(user.getRealName());
        entity.setProjectId(req.getProjectId());
        entity.setDifyKbId(difyKnowdataId); // 保留原有字段，兼容旧数据
        entity.setDifyKnowdataId(difyKnowdataId); // 新增字段，存储Dify返回的id
        entity.setCallback(callbackJson); // 存储完整的Dify返回数据
        entity.setIsShared(req.getIsShared() != null ? req.getIsShared() : 0);
        entity.setStatus("active");
        entity.setFileCount(0);
        entity.setFolderCount(0);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        // 12. 保存到数据库
        Long id = knowledgeBaseRepo.save(entity);
        if (id == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }
        // 13. 构建响应
        KnowledgeResp resp = new KnowledgeResp();
        resp.setId(id);
        resp.setName(entity.getName());
        resp.setDescription(entity.getDescription());
        resp.setOwnerId(entity.getOwnerId());
        resp.setOwnerName(entity.getOwnerName());
        resp.setProjectId(entity.getProjectId());
        resp.setDifyKbId(entity.getDifyKbId());
        resp.setDifyKnowdataId(entity.getDifyKnowdataId());
        resp.setCallback(entity.getCallback());
        resp.setIsShared(entity.getIsShared());
        resp.setStatus(entity.getStatus());
        resp.setFileCount(entity.getFileCount());
        resp.setFolderCount(entity.getFolderCount());
        resp.setCreatedTime(entity.getCreatedTime());
        resp.setUpdatedTime(entity.getUpdatedTime());
        log.info(String.format("创建知识库成功: id=%s, difyKnowdataId=%s", id, difyKnowdataId));
        return resp;
    }

    /**
     * 分页查询知识库列表
     *
     * @param page 页码
     * @param size 页大小
     * @return 知识库分页结果
     */
    @Override
    public PageResult<KnowledgeResp> page(int page, int size) {
        // 1. 获取当前登录用户ID（如果已登录）
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
                log.info(String.format("分页查询知识库: userId=%s, page=%s, size=%s", userId, page, size));
            } else {
                log.info(String.format("分页查询知识库（未登录）: page=%s, size=%s", page, size));
            }
        } catch (Exception e) {
            log.debug("用户未登录，查询所有知识库");
        }

        // 2. 创建分页对象
        Page<SysKnowledgeBase> pageParam = new Page<>(page, size);
        
        // 3. 执行分页查询
        var pageResult = knowledgeBaseRepo.pageByCondition(pageParam, userId);
        
        // 4. 转换为响应DTO列表
        var respList = knowledgeConverter.toRespList(pageResult.getRecords());
        
        // 5. 构建分页结果
        var result = new PageResult<KnowledgeResp>(
                respList,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );
        
        log.info(String.format("分页查询知识库成功: total=%s, current=%s, size=%s", 
                result.getTotal(), result.getCurrent(), result.getSize()));
        return result;
    }

    /**
     * 上传文件到知识库
     *
     * @param knowledgeId 知识库ID（Dify知识库ID，String类型）
     * @param file 上传的文件
     * @param folderId 文件夹ID（可选，0为根目录）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String knowledgeId, MultipartFile file, Long folderId) {
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("上传文件到知识库: userId=%s, knowledgeId=%s, fileName=%s, folderId=%s", 
                userId, knowledgeId, file.getOriginalFilename(), folderId));

        // 2. 根据Dify知识库ID查询知识库信息
        SysKnowledgeBase knowledgeBase = knowledgeBaseRepo.findByDifyKnowdataId(knowledgeId);
        if (knowledgeBase == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库不存在");
        }

        // 3. 使用传入的 knowledgeId 作为 Dify 数据集ID
        String datasetId = knowledgeId;

        // 4. 查询用户信息
        SysUser user = userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 5. 获取用户的 Dify API Key
        DifyApiKey difyApiKey = null;
        QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("key_type", "dataset")
                .eq("is_active", true)
                .last("LIMIT 1");
        difyApiKey = difyApiKeyService.getOne(queryWrapper);

        // 6. 确定 resourceId
        String resourceId = Optional.ofNullable(difyApiKey)
                .map(DifyApiKey::getResourceId)
                .orElse(defaultResourceId);

        // 7. 调用 Dify API 上传文档
        ResponseEntity<String> response = difyApiService.uploadDocumentWithFileStorage(
                datasetId, file, userId, resourceId, "dataset");

        // 8. 检查响应状态
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error(String.format("Dify API上传文档失败: status=%s, body=%s", 
                    response.getStatusCode(), response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, 
                    String.format("上传文件失败: Dify API调用失败, status=%s", response.getStatusCode()));
        }

        // 9. 解析返回的JSON
        JsonNode responseJson;
        try {
            responseJson = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error(String.format("解析Dify API响应失败: body=%s, error=%s", response.getBody(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "解析Dify API响应失败");
        }

        // 10. 提取文档信息
        JsonNode documentNode = responseJson.get("document");
        if (documentNode == null) {
            log.error(String.format("Dify API返回数据缺少document字段: body=%s", response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, "上传文件失败: Dify API返回数据异常");
        }

        String difyDocId = documentNode.has("id") ? documentNode.get("id").asText() : null;
        String documentName = documentNode.has("name") ? documentNode.get("name").asText() : file.getOriginalFilename();
        String batch = responseJson.has("batch") ? responseJson.get("batch").asText() : null;

        if (difyDocId == null) {
            log.error(String.format("Dify API返回数据缺少document.id字段: body=%s", response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, "上传文件失败: Dify API返回数据异常");
        }

        // 11. 创建附件记录
        SysAttachment attachment = new SysAttachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setFileType(getFileType(file.getOriginalFilename()));
        attachment.setFileExtension(getFileExtension(file.getOriginalFilename()));
        attachment.setFileSize(file.getSize());
        attachment.setFileUrl(""); // Dify存储的文件，URL为空
        attachment.setFilePath(""); // Dify存储的文件，路径为空
        attachment.setMimeType(file.getContentType());
        attachment.setMd5Hash(""); // 如果需要MD5，可以后续计算
        attachment.setUploaderId(userId);
        attachment.setUploaderName(user.getRealName());
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setDownloadCount(0);
        attachment.setIsPublic(0);
        attachment.setDifyDocId(difyDocId); // 存储Dify文档ID
        attachment.setCreatedBy(userId);
        attachment.setUpdatedBy(userId);

        Long attachmentId = attachmentRepo.save(attachment);
        if (attachmentId == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "保存附件记录失败");
        }

        // 12. 创建知识库文件关联记录
        SysKnowledgeFileRelation fileRelation = new SysKnowledgeFileRelation();
        fileRelation.setKnowledgeId(knowledgeBase.getId()); // 使用数据库主键ID
        fileRelation.setFolderId(folderId != null ? folderId : 0L);
        fileRelation.setAttachmentId(attachmentId);
        fileRelation.setFileName(documentName);
        fileRelation.setSortOrder(0); // 默认排序号
        fileRelation.setCallback(response.getBody()); // 存储完整的Dify API回调数据
        fileRelation.setCreatedBy(userId);
        fileRelation.setUpdatedBy(userId);

        Long relationId = fileRelationRepo.save(fileRelation);
        if (relationId == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "保存文件关联记录失败");
        }

        log.info(String.format("上传文件成功: knowledgeId=%s, attachmentId=%s, relationId=%s, difyDocId=%s, batch=%s", 
                knowledgeId, attachmentId, relationId, difyDocId, batch));
    }

    /**
     * 获取文件类型
     *
     * @param filename 文件名
     * @return 文件类型
     */
    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "other";
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "pdf", "doc", "docx", "txt", "md" -> "document";
            case "jpg", "jpeg", "png", "gif", "bmp" -> "image";
            case "xls", "xlsx", "csv" -> "spreadsheet";
            default -> "other";
        };
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 基于知识库的 Chatbot 流式对话
     *
     * @param req 流式对话请求
     * @return 流式响应（SSE格式）
     */
    @Override
    public SseEmitter chatbotStream(KnowledgeChatbotStreamReq req) {
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("知识库Chatbot流式对话: userId=%s, knowledgeId=%s, query=%s", 
                userId, req.getKnowledgeId(), req.getQuery()));

        // 2. 创建 SSE Emitter（超时时间设置为30秒）
        SseEmitter emitter = new SseEmitter(30000L);

        // 3. 检查用户是否创建了 Chatbot
        List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
        if (chatbotKeys == null || chatbotKeys.isEmpty()) {
            log.warn(String.format("用户未创建Chatbot: userId=%s", userId));
            // 发送错误消息并完成
            try {
                String errorMessage = """
                        {
                            "error": true,
                            "code": "CHATBOT_NOT_CREATED",
                            "message": "请先创建 Chatbot 应用",
                            "hint": "您需要先创建 Chatbot 应用才能使用知识库问答功能"
                        }
                        """;
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(errorMessage));
                emitter.complete();
            } catch (Exception e) {
                log.error(String.format("发送错误消息失败: err=%s", e.getMessage()), e);
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 4. 获取第一个可用的 Chatbot API Key（通常一个用户只有一个 Chatbot）
        DifyApiKey chatbotKey = chatbotKeys.get(0);
        String chatbotAppId = chatbotKey.getResourceId(); // Chatbot 应用的 ID

        log.info(String.format("找到用户Chatbot: userId=%s, appId=%s", userId, chatbotAppId));

        // 5. 构建 Dify Chatbot 消息请求
        DifyChatbotMessageRequest difyRequest = new DifyChatbotMessageRequest();
        difyRequest.setUserId(userId);
        difyRequest.setResourceId(chatbotAppId);
        difyRequest.setKeyType("chatbot");
        difyRequest.setQuery(req.getQuery());
        difyRequest.setResponseMode("streaming"); // 设置为流式输出
        if (req.getConversationId() != null && !req.getConversationId().trim().isEmpty()) {
            difyRequest.setConversationId(req.getConversationId());
        }
        if (req.getUser() != null && !req.getUser().trim().isEmpty()) {
            difyRequest.setUser(req.getUser());
        } else {
            difyRequest.setUser(String.valueOf(userId)); // 默认使用用户ID作为用户标识
        }

        // 6. 异步调用 Dify API 并转发流式响应
        new Thread(() -> {
            try {
                // 调用 Dify API 进行流式对话（使用流式请求方法）
                difyApiService.sendChatbotMessageStream(difyRequest, line -> {
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
                emitter.send(SseEmitter.event()
                        .name("message_end")
                        .data("{}"));
                emitter.complete();
                
                log.info(String.format("知识库Chatbot流式对话完成: userId=%s, knowledgeId=%s", 
                        userId, req.getKnowledgeId()));
            } catch (Exception e) {
                log.error(String.format("知识库Chatbot流式对话失败: userId=%s, knowledgeId=%s, err=%s", 
                        userId, req.getKnowledgeId(), e.getMessage()), e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(String.format("{\"error\": true, \"message\": \"%s\"}", e.getMessage())));
                } catch (Exception sendException) {
                    log.error("发送错误消息失败", sendException);
                }
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
