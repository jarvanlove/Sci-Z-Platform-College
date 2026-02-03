package com.sciz.server.application.service.knowledge.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.knowledge.KnowledgeFileRelationService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileRelationResp;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFolderWithFilesResp;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFolder;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFolderRepo;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotModelConfigRequest;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.interfaces.converter.KnowledgeFileRelationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 知识库文件关联应用服务实现类
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationServiceImpl
 * @date 2025-01-28 16:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeFileRelationServiceImpl implements KnowledgeFileRelationService {
    private final SysKnowledgeFileRelationRepo fileRelationRepo;
    private final KnowledgeFileRelationConverter converter;
    private final SysKnowledgeBaseRepo knowledgeBaseRepo;
    private final SysKnowledgeFolderRepo folderRepo;
    private final DifyApiKeyServiceImpl difyApiKeyService;
    private final DifyApiService difyApiService;
    private final DifyConfig difyConfig;
    private final SysAttachmentRepo attachmentRepo;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @Value("${dify.default-resource-id:default}")
    private String defaultResourceId;

    /**
     * 创建知识库文件关联
     *
     * @param req 创建请求
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeFileRelationResp create(KnowledgeFileRelationCreateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("创建知识库文件关联: userId=%s, knowledgeId=%s, attachmentId=%s", 
                userId, req.getKnowledgeId(), req.getAttachmentId()));

        // 1. 转换 String 类型字段为 Long
        Long knowledgeId;
        Long folderId;
        Long attachmentId;
        try {
            knowledgeId = Long.parseLong(req.getKnowledgeId());
            folderId = req.getFolderId() != null ? Long.parseLong(req.getFolderId()) : 0L;
            attachmentId = Long.parseLong(req.getAttachmentId());
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的ID格式");
        }

        // 2. 创建实体
        SysKnowledgeFileRelation entity = new SysKnowledgeFileRelation();
        entity.setKnowledgeId(knowledgeId);
        entity.setFolderId(folderId);
        entity.setAttachmentId(attachmentId);
        entity.setFileName(req.getFileName());
        entity.setSortOrder(req.getSortOrder());
        entity.setCallback(req.getCallback());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        // 3. 保存
        Long id = fileRelationRepo.save(entity);
        if (id == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 4. 查询保存后的实体
        SysKnowledgeFileRelation savedEntity = fileRelationRepo.findById(id);
        if (savedEntity == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 5. 转换为响应
        KnowledgeFileRelationResp resp = converter.toResp(savedEntity);

        log.info(String.format("创建知识库文件关联成功: id=%s", id));
        return resp;
    }

    /**
     * 更新知识库文件关联
     *
     * @param id 关联ID
     * @param req 更新请求
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeFileRelationResp update(String id, KnowledgeFileRelationUpdateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("更新知识库文件关联: userId=%s, id=%s", userId, id));

        // 1. 转换 String ID 为 Long
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的ID格式: " + id);
        }

        // 2. 查询实体
        SysKnowledgeFileRelation entity = fileRelationRepo.findById(longId);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库文件关联不存在");
        }

        // 3. 更新实体
        converter.updateEntity(entity, req);
        // 处理 folderId 的 String 到 Long 转换
        if (req.getFolderId() != null && !req.getFolderId().trim().isEmpty()) {
            try {
                entity.setFolderId(Long.parseLong(req.getFolderId()));
            } catch (NumberFormatException e) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "无效的文件夹ID格式: " + req.getFolderId());
            }
        }
        entity.setUpdatedBy(userId);

        // 4. 保存更新
        boolean success = fileRelationRepo.updateById(entity);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 5. 查询更新后的实体
        SysKnowledgeFileRelation updatedEntity = fileRelationRepo.findById(longId);
        if (updatedEntity == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 6. 转换为响应
        KnowledgeFileRelationResp resp = converter.toResp(updatedEntity);

        log.info(String.format("更新知识库文件关联成功: id=%s", id));
        return resp;
    }

    /**
     * 删除知识库文件关联
     *
     * @param id 关联ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("删除知识库文件关联: userId=%s, id=%s", userId, id));

        // 1. 转换 String ID 为 Long
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的ID格式: " + id);
        }

        // 2. 检查是否存在
        SysKnowledgeFileRelation entity = fileRelationRepo.findById(longId);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库文件关联不存在");
        }

        // 3. 查询知识库实体，获取 Dify 数据集ID
        SysKnowledgeBase knowledgeBase = knowledgeBaseRepo.findById(entity.getKnowledgeId());
        if (knowledgeBase == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库不存在");
        }

        // 4. 查询附件实体，获取 Dify 文档ID
        SysAttachment attachment = attachmentRepo.findById(entity.getAttachmentId());
        String difyDocumentId = null;
        if (attachment != null && attachment.getDifyDocId() != null && !attachment.getDifyDocId().trim().isEmpty()) {
            difyDocumentId = attachment.getDifyDocId();
        } else if (entity.getCallback() != null && !entity.getCallback().trim().isEmpty()) {
            // 如果附件中没有 Dify 文档ID，尝试从 callback 字段解析
            try {
                JsonNode callbackJson = objectMapper.readTree(entity.getCallback());
                JsonNode documentNode = callbackJson.get("document");
                if (documentNode != null && documentNode.has("id")) {
                    difyDocumentId = documentNode.get("id").asText();
                }
            } catch (Exception e) {
                log.warn(String.format("解析 callback 字段失败: callback=%s, err=%s", entity.getCallback(), e.getMessage()));
            }
        }

        // 5. 如果存在 Dify 数据集ID和文档ID，调用 Dify API 删除文档
        if (knowledgeBase.getDifyKnowdataId() != null && !knowledgeBase.getDifyKnowdataId().trim().isEmpty()
                && difyDocumentId != null && !difyDocumentId.trim().isEmpty()) {
            try {
                // 获取用户的 Dify API Key
                QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", userId)
                           .eq("key_type", "dataset")
                           .eq("is_active", true)
                           .last("LIMIT 1");
                DifyApiKey difyApiKey = difyApiKeyService.getOne(queryWrapper);
                
                String resourceId = (difyApiKey != null && difyApiKey.getResourceId() != null) 
                    ? difyApiKey.getResourceId() 
                    : defaultResourceId;

                // 调用 Dify API 删除文档
                ResponseEntity<String> response = difyApiService.deleteDocument(
                        knowledgeBase.getDifyKnowdataId(), difyDocumentId, userId, resourceId, "dataset");
                
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.warn(String.format("Dify API删除文档失败: datasetId=%s, documentId=%s, status=%s, body=%s", 
                            knowledgeBase.getDifyKnowdataId(), difyDocumentId, response.getStatusCode(), response.getBody()));
                    // 即使 Dify API 删除失败，也继续删除本地数据（避免数据不一致）
                } else {
                    log.info(String.format("Dify API删除文档成功: datasetId=%s, documentId=%s", 
                            knowledgeBase.getDifyKnowdataId(), difyDocumentId));
                }
            } catch (Exception e) {
                log.error(String.format("调用 Dify API 删除文档异常: datasetId=%s, documentId=%s, err=%s", 
                        knowledgeBase.getDifyKnowdataId(), difyDocumentId, e.getMessage()), e);
                // 即使 Dify API 调用异常，也继续删除本地数据（避免数据不一致）
            }
        } else {
            log.warn(String.format("缺少 Dify 数据集ID或文档ID，跳过 Dify API 删除: datasetId=%s, documentId=%s", 
                    knowledgeBase.getDifyKnowdataId(), difyDocumentId));
        }

        // 6. 软删除本地关联记录
        boolean success = fileRelationRepo.deleteById(longId);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }


        log.info(String.format("删除知识库文件关联成功: id=%s", id));
    }


    /**
     * 根据ID查询知识库文件关联详情
     *
     * @param id 关联ID
     * @return 响应
     */
    @Override
    public KnowledgeFileRelationResp findDetail(String id) {
        log.info(String.format("查询知识库文件关联详情: id=%s", id));

        // 1. 转换 String ID 为 Long
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的ID格式: " + id);
        }

        // 2. 查询实体
        SysKnowledgeFileRelation entity = fileRelationRepo.findById(longId);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库文件关联不存在");
        }

        return converter.toResp(entity);
    }

    /**
     * 分页查询知识库文件关联列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<KnowledgeFileRelationResp> page(KnowledgeFileRelationQueryReq req) {
        log.info(String.format("分页查询知识库文件关联: knowledgeId=%s, folderId=%s, page=%s, size=%s", 
                req.getKnowledgeId(), req.getFolderId(), req.getPage(), req.getSize()));

        // 1. 检查知识库ID
        if (req.getKnowledgeId() == null || req.getKnowledgeId().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "知识库ID不能为空");
        }

        // 2. 转换 String 类型字段为 Long
        Long knowledgeId;
        Long folderId = null;
        try {
            knowledgeId = Long.parseLong(req.getKnowledgeId());
            if (req.getFolderId() != null && !req.getFolderId().trim().isEmpty()) {
                folderId = Long.parseLong(req.getFolderId());
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的ID格式");
        }

        // 3. 创建分页对象
        Page<SysKnowledgeFileRelation> pageParam = new Page<>(req.getPage(), req.getSize());

        // 4. 执行分页查询
        var pageResult = fileRelationRepo.pageByKnowledgeId(pageParam, knowledgeId, folderId);

        // 5. 转换为响应DTO列表
        var respList = converter.toRespList(pageResult.getRecords());

        // 5.1 预加载所有相关附件的文件大小，避免 N+1 查询
        Map<Long, Long> attachmentSizeMap = new HashMap<>();
        var attachmentIds = pageResult.getRecords().stream()
                .map(SysKnowledgeFileRelation::getAttachmentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (!attachmentIds.isEmpty()) {
            // 使用 findByIdsForRelation：不按上传人过滤，知识库可见性已在调用前校验，非 admin 用户也可看到他人上传文件的 fileSize
            List<SysAttachment> attachments = attachmentRepo.findByIdsForRelation(attachmentIds);
            attachmentSizeMap = attachments.stream()
                    .collect(Collectors.toMap(SysAttachment::getId, a -> a.getFileSize() != null ? a.getFileSize() : 0L));
        }
        // 5.2 填充文件大小
        for (var fileResp : respList) {
            if (fileResp.getAttachmentId() != null) {
                try {
                    Long attachId = Long.valueOf(fileResp.getAttachmentId());
                    Long sizeBytes = attachmentSizeMap.get(attachId);
                    fileResp.setFileSize(sizeBytes);
                } catch (NumberFormatException ignored) {
                    // 保持 fileSize 为 null
                }
            }
        }

        // 6. 构建分页结果
        var result = new PageResult<KnowledgeFileRelationResp>(
                respList,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );

        log.info(String.format("分页查询知识库文件关联成功: total=%s, current=%s, size=%s", 
                result.getTotal(), result.getCurrent(), result.getSize()));

        // 7. 更新 Chatbot 应用的知识库ID配置
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            updateChatbotKnowledgeBaseIdByUserId(userId, knowledgeId);
            log.info(String.format("更新Chatbot知识库ID成功: userId=%s, knowledgeId=%s", userId, knowledgeId));
        } catch (Exception updateException) {
            // 更新失败不影响查询，只记录日志
            log.warn(String.format("更新Chatbot知识库ID失败: knowledgeId=%s, err=%s", 
                    knowledgeId, updateException.getMessage()), updateException);
        }

        return result;
    }

    /**
     * 根据用户ID更新 Chatbot 应用的知识库ID配置
     *
     * @param userId 用户ID
     * @param knowledgeId 知识库ID（数据库主键ID）
     */
    private void updateChatbotKnowledgeBaseIdByUserId(Long userId, Long knowledgeId) {
        // 1. 查询知识库信息，获取Dify知识库ID
        SysKnowledgeBase knowledgeBase = knowledgeBaseRepo.findById(knowledgeId);
        if (knowledgeBase == null) {
            log.warn(String.format("知识库不存在: knowledgeId=%s", knowledgeId));
            return;
        }

        String difyKnowledgeId = knowledgeBase.getDifyKnowdataId();
        if (difyKnowledgeId == null || difyKnowledgeId.trim().isEmpty()) {
            log.warn(String.format("知识库Dify ID为空: knowledgeId=%s", knowledgeId));
            return;
        }

        // 2. 查询用户的 Chatbot
        List<DifyApiKey> chatbotKeys = difyApiKeyService.getUserApiKeysByType(userId, "chatbot");
        if (chatbotKeys == null || chatbotKeys.isEmpty()) {
            log.debug(String.format("用户未创建Chatbot: userId=%s", userId));
            return;
        }

        // 3. 获取第一个可用的 Chatbot API Key
        DifyApiKey chatbotKey = chatbotKeys.get(0);
        String chatbotAppId = chatbotKey.getResourceId(); // Chatbot 应用的 ID

        log.info(String.format("找到用户Chatbot: userId=%s, appId=%s", userId, chatbotAppId));

        // 4. 更新 Chatbot 应用的知识库ID配置
        updateChatbotKnowledgeBaseId(chatbotAppId, difyKnowledgeId);
    }

    /**
     * 更新 Chatbot 应用的知识库ID配置
     *
     * @param chatbotAppId Chatbot 应用ID
     * @param knowledgeId 知识库ID（Dify知识库ID）
     */
    private void updateChatbotKnowledgeBaseId(String chatbotAppId, String knowledgeId) {
        try {
            // 1. 构建数据集配置
            DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset dataset = 
                    new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset();
            dataset.setEnabled(true);
            dataset.setId(knowledgeId);

            // 2. 构建数据集包装器
            DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper datasetWrapper = 
                    new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper();
            datasetWrapper.setDataset(dataset);

            // 3. 构建数据集集合
            List<DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection.DatasetWrapper> datasetList = new ArrayList<>();
            datasetList.add(datasetWrapper);
            
            DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection datasetCollection = 
                    new DifyChatbotModelConfigRequest.DatasetConfigs.DatasetCollection();
            datasetCollection.setDatasets(datasetList);

            // 4. 从配置文件读取完整配置
            DifyChatbotModelConfigRequest configRequest = DifyChatbotModelConfigRequest.fromConfig(difyConfig.getChatbot());
            
            // 5. 覆盖数据集配置（使用动态的知识库ID）
            DifyChatbotModelConfigRequest.DatasetConfigs datasetConfigs = configRequest.getDatasetConfigs();
            if (datasetConfigs == null) {
                datasetConfigs = DifyChatbotModelConfigRequest.DatasetConfigs.defaultConfig();
            }
            datasetConfigs.setDatasets(datasetCollection);
            configRequest.setDatasetConfigs(datasetConfigs);

            // 7. 调用 Dify API 更新配置
            ResponseEntity<String> updateResponse = difyApiService.updateChatbotModelConfig(chatbotAppId, configRequest);
            
            if (!updateResponse.getStatusCode().is2xxSuccessful()) {
                String errorBody = updateResponse.getBody() != null ? updateResponse.getBody() : "Unknown error";
                throw new BusinessException(ResultCode.SERVER_ERROR, 
                        String.format("更新Chatbot知识库ID失败: status=%s, body=%s", 
                                updateResponse.getStatusCode(), errorBody));
            }
            
            log.info(String.format("更新Chatbot知识库ID配置成功: chatbotAppId=%s, knowledgeId=%s", 
                    chatbotAppId, knowledgeId));
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 网络连接异常（如连接被拒绝、超时等）
            log.warn(String.format("更新Chatbot知识库ID配置失败（网络连接异常）: chatbotAppId=%s, knowledgeId=%s, err=%s", 
                    chatbotAppId, knowledgeId, e.getMessage()));
            throw e; // 重新抛出，让上层处理
        } catch (BusinessException e) {
            // 业务异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 其他异常
            log.error(String.format("更新Chatbot知识库ID配置失败（未知异常）: chatbotAppId=%s, knowledgeId=%s, err=%s", 
                    chatbotAppId, knowledgeId, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, 
                    String.format("更新Chatbot知识库ID配置失败: %s", e.getMessage()));
        }
    }


    @Override
    public PageResult<KnowledgeFolderWithFilesResp> listFoldersWithFiles(Long knowledgeId, Long folderId, Integer page, Integer size) {
        log.info(String.format("分页查询知识库文件夹及文件列表: knowledgeId=%s, folderId=%s, page=%s, size=%s", knowledgeId, folderId, page, size));

        // 1. 校验知识库是否存在
        var knowledge = knowledgeBaseRepo.findById(knowledgeId);
        if (knowledge == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }

        // 2. 处理 folderId：null 或 0 表示根目录
        Long actualFolderId = (folderId == null || folderId == 0) ? null : folderId;
        
        // 3. 如果指定了 folderId，校验文件夹是否存在
        if (actualFolderId != null) {
            var folder = folderRepo.findById(actualFolderId);
            if (folder == null) {
                throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NOT_FOUND, "文件夹不存在");
            }
        }

        // 4. 查询子文件夹列表（如果指定了 folderId，只查询该文件夹下的子文件夹）
        List<SysKnowledgeFolder> subFolders;
        if (actualFolderId == null) {
            // 根目录：查询所有根目录下的文件夹
            subFolders = folderRepo.findByKnowledgeId(knowledgeId).stream()
                    .filter(folder -> folder.getParentId() == null || folder.getParentId() == 0)
                    .collect(Collectors.toList());
        } else {
            // 指定文件夹：查询该文件夹下的子文件夹
            subFolders = folderRepo.findByKnowledgeIdAndParentId(knowledgeId, actualFolderId);
        }

        // 5. 查询文件关联（如果指定了 folderId，只查询该文件夹下的文件）
        List<SysKnowledgeFileRelation> fileRelations;
        List<SysKnowledgeFileRelation> allFileRelations; // 用于根目录查询时获取所有文件
        if (actualFolderId == null) {
            // 根目录：查询所有文件（包括文件夹内的文件），用于构建响应和统计文件数量
            allFileRelations = fileRelationRepo.findByKnowledgeId(knowledgeId);
            // 只显示根目录下的文件（folderId 为 null 或 0），用于构建"未分类"文件夹
            fileRelations = allFileRelations.stream()
                    .filter(file -> file.getFolderId() == null || file.getFolderId() == 0)
                    .collect(Collectors.toList());
        } else {
            // 指定文件夹：查询该文件夹下的文件
            allFileRelations = null; // 子文件夹查询时不需要
            fileRelations = fileRelationRepo.findByKnowledgeId(knowledgeId).stream()
                    .filter(file -> actualFolderId.equals(file.getFolderId()))
                    .collect(Collectors.toList());
        }

        // 6. 预加载所有相关附件的文件大小，避免 N+1 查询
        Map<Long, Long> attachmentSizeMap = new HashMap<>();
        // 🔥 修复：根目录查询时，使用 allFileRelations 来预加载所有文件的文件大小（包括文件夹内的文件）
        List<SysKnowledgeFileRelation> filesForSizePreload = (actualFolderId == null && allFileRelations != null) 
                ? allFileRelations 
                : fileRelations;
        var attachmentIds = filesForSizePreload.stream()
                .map(SysKnowledgeFileRelation::getAttachmentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (!attachmentIds.isEmpty()) {
            // 使用 findByIdsForRelation：不按上传人过滤，知识库可见性已在 listFoldersWithFiles 前校验，非 admin 用户也可看到他人上传文件的 fileSize
            List<SysAttachment> attachments = attachmentRepo.findByIdsForRelation(attachmentIds);
            attachmentSizeMap = attachments.stream()
                    .collect(Collectors.toMap(SysAttachment::getId, a -> a.getFileSize() != null ? a.getFileSize() : 0L));
        }

        // 7. 构建混合列表（文件夹在前，文件在后）
        var mixedList = new ArrayList<KnowledgeFolderWithFilesResp>();

        if (actualFolderId == null) {
            // ========== 根目录：混合列表（文件夹在前，文件在后） ==========
            // 7.1 按 folder_id 分组所有文件（包括文件夹内的文件），用于构建文件列表
            // 根目录时 allFileRelations 已赋值（第596行），不会为 null，但为了消除警告添加检查
            if (allFileRelations == null) {
                allFileRelations = new ArrayList<>();
            }
            Map<Long, List<SysKnowledgeFileRelation>> filesByFolderId = allFileRelations.stream()
                    .filter(file -> file.getFolderId() != null && file.getFolderId() != 0)
                    .collect(Collectors.groupingBy(SysKnowledgeFileRelation::getFolderId));

            // 7.2 分离没有文件夹的文档（folderId为null或0）
            var filesWithoutFolder = fileRelations.stream()
                    .filter(file -> file.getFolderId() == null || file.getFolderId() == 0)
                    .collect(Collectors.toList());

            // 7.3 遍历根目录文件夹，为每个文件夹找到对应的文件列表
            for (var folder : subFolders) {
                var currentFolderId = folder.getId();
                var files = filesByFolderId.getOrDefault(currentFolderId, new ArrayList<>());

                var resp = new KnowledgeFolderWithFilesResp();
                resp.setType("folder");
                resp.setFolderId(currentFolderId);
                resp.setFolderName(folder.getFolderName());
                // 🔥 修复：使用 countByFolderId 查询该文件夹下的准确文件数量（包括所有子文件）
                var folderFileCount = fileRelationRepo.countByFolderId(currentFolderId);
                resp.setFileCount(folderFileCount != null ? folderFileCount.intValue() : 0);

                // 转换为响应DTO并填充文件大小
                var fileRespList = converter.toRespList(files);
                for (var fileResp : fileRespList) {
                    if (fileResp.getAttachmentId() != null) {
                        try {
                            Long attachId = Long.valueOf(fileResp.getAttachmentId());
                            Long sizeBytes = attachmentSizeMap.get(attachId);
                            fileResp.setFileSize(sizeBytes);
                        } catch (NumberFormatException ignored) {
                            // 保持 fileSize 为 null
                        }
                    }
                }
                resp.setFiles(fileRespList);
                mixedList.add(resp);
            }

            // 7.4 处理没有文件夹的文档（作为独立的文件项添加到 mixedList）
            var fileRespList = converter.toRespList(filesWithoutFolder);
            for (var fileResp : fileRespList) {
                // 填充文件大小
                if (fileResp.getAttachmentId() != null) {
                    try {
                        Long attachId = Long.valueOf(fileResp.getAttachmentId());
                        Long sizeBytes = attachmentSizeMap.get(attachId);
                        fileResp.setFileSize(sizeBytes);
                    } catch (NumberFormatException ignored) {
                        // 保持 fileSize 为 null
                    }
                }
                
                // 创建文件响应项
                var resp = new KnowledgeFolderWithFilesResp();
                resp.setType("file");
                resp.setFileId(fileResp.getId());
                resp.setFileName(fileResp.getFileName());
                resp.setAttachmentId(fileResp.getAttachmentId());
                resp.setFileSize(fileResp.getFileSize());
                resp.setFolderId(null); // 未分类文件的 folderId 为 null
                resp.setCallback(fileResp.getCallback());
                resp.setCreatedTime(fileResp.getCreatedTime());
                resp.setUpdatedTime(fileResp.getUpdatedTime());
                // 提取文件扩展名
                if (fileResp.getFileName() != null) {
                    var lastDotIndex = fileResp.getFileName().lastIndexOf('.');
                    if (lastDotIndex > 0 && lastDotIndex < fileResp.getFileName().length() - 1) {
                        resp.setExt(fileResp.getFileName().substring(lastDotIndex + 1).toLowerCase());
                    }
                }
                mixedList.add(resp);
            }

            // 7.5 排序：文件夹在前，文件在后；同类型按名称排序
            mixedList.sort((a, b) -> {
                // 文件夹在前
                if ("folder".equals(a.getType()) && "file".equals(b.getType())) {
                    return -1;
                }
                if ("file".equals(a.getType()) && "folder".equals(b.getType())) {
                    return 1;
                }
                // 同类型按名称排序
                String nameA = "folder".equals(a.getType()) ? a.getFolderName() : a.getFileName();
                String nameB = "folder".equals(b.getType()) ? b.getFolderName() : b.getFileName();
                if (nameA == null && nameB == null) {
                    return 0;
                }
                if (nameA == null) {
                    return 1;
                }
                if (nameB == null) {
                    return -1;
                }
                return nameA.compareTo(nameB);
            });
        } else {
            // ========== 文件夹内：混合列表（文件夹在前，文件在后） ==========
            // 7.1 转换子文件夹为响应DTO
            for (var folder : subFolders) {
                var resp = new KnowledgeFolderWithFilesResp();
                resp.setType("folder");
                resp.setFolderId(folder.getId());
                resp.setFolderName(folder.getFolderName());
                // 统计该文件夹下的文件数量
                var folderFileCount = fileRelationRepo.countByFolderId(folder.getId());
                resp.setFileCount(folderFileCount != null ? folderFileCount.intValue() : 0);
                resp.setFiles(new ArrayList<>()); // 文件夹内查询时，不返回文件列表
                mixedList.add(resp);
            }

            // 7.2 转换文件为响应DTO
            var fileRespList = converter.toRespList(fileRelations);
            for (var fileResp : fileRespList) {
                var resp = new KnowledgeFolderWithFilesResp();
                resp.setType("file");
                resp.setFileId(fileResp.getId());
                resp.setFileName(fileResp.getFileName());
                resp.setAttachmentId(fileResp.getAttachmentId());
                resp.setFileSize(fileResp.getFileSize());
                resp.setCreatedTime(fileResp.getCreatedTime());
                resp.setUpdatedTime(fileResp.getUpdatedTime());
                // 转换 folderId 从 String 到 Long
                if (fileResp.getFolderId() != null && !fileResp.getFolderId().trim().isEmpty()) {
                    try {
                        resp.setFolderId(Long.parseLong(fileResp.getFolderId()));
                    } catch (NumberFormatException ignored) {
                        // 保持 folderId 为 null
                    }
                }
                resp.setCallback(fileResp.getCallback());
                // 提取文件扩展名
                if (fileResp.getFileName() != null) {
                    var lastDotIndex = fileResp.getFileName().lastIndexOf('.');
                    if (lastDotIndex > 0 && lastDotIndex < fileResp.getFileName().length() - 1) {
                        resp.setExt(fileResp.getFileName().substring(lastDotIndex + 1).toLowerCase());
                    }
                }
                // 填充文件大小（如果还没有）
                if (resp.getFileSize() == null && fileResp.getAttachmentId() != null) {
                    try {
                        Long attachId = Long.valueOf(fileResp.getAttachmentId());
                        Long sizeBytes = attachmentSizeMap.get(attachId);
                        resp.setFileSize(sizeBytes);
                    } catch (NumberFormatException ignored) {
                        // 保持 fileSize 为 null
                    }
                }
                
                mixedList.add(resp);
            }

            // 7.3 排序：文件夹在前，文件在后；同类型按名称排序
            mixedList.sort((a, b) -> {
                // 文件夹在前
                if ("folder".equals(a.getType()) && "file".equals(b.getType())) {
                    return -1;
                }
                if ("file".equals(a.getType()) && "folder".equals(b.getType())) {
                    return 1;
                }
                // 同类型按名称排序
                String nameA = "folder".equals(a.getType()) ? a.getFolderName() : a.getFileName();
                String nameB = "folder".equals(b.getType()) ? b.getFolderName() : b.getFileName();
                if (nameA == null && nameB == null) {
                    return 0;
                }
                if (nameA == null) {
                    return 1;
                }
                if (nameB == null) {
                    return -1;
                }
                return nameA.compareTo(nameB);
            });
        }

        // 8. 混合分页处理（文件夹在前，文件在后）
        // 🔥 修复：统一 total 计算逻辑，根目录和子文件夹都使用 mixedList 的大小
        // 根目录时，mixedList 包含所有文件夹（每个文件夹作为一个项）
        // 子文件夹时，mixedList 包含子文件夹和文件（混合列表）
        long total = (long) mixedList.size();
        
        var start = (page - 1) * size;
        var end = Math.min(start + size, mixedList.size());
        
        var pagedItems = new ArrayList<KnowledgeFolderWithFilesResp>();
        if (start < mixedList.size()) {
            pagedItems = new ArrayList<>(mixedList.subList(start, end));
        }

        // 9. 为响应添加知识库封面信息（coverUrl 和 coverFileId）
        for (var item : pagedItems) {
            item.setCoverFileId(knowledge.getCoverFileId());
            if (knowledge.getCoverUrl() != null && !knowledge.getCoverUrl().trim().isEmpty()) {
                var presignedUrl = fileService.generatePresignedUrlFromFileUrl(knowledge.getCoverUrl(), null);
                if (presignedUrl != null) {
                    item.setCoverUrl(presignedUrl);
                } else {
                    item.setCoverUrl(knowledge.getCoverUrl());
                }
            }
        }

        log.info(String.format("分页查询知识库文件夹及文件列表成功: knowledgeId=%s, folderId=%s, total=%s, page=%s, size=%s, resultCount=%s", 
                knowledgeId, actualFolderId, total, page, size, pagedItems.size()));
        
        return new PageResult<>(pagedItems, total, page, size);
    }
}

