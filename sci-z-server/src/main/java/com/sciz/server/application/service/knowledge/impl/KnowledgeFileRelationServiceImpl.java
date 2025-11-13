package com.sciz.server.application.service.knowledge.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.knowledge.KnowledgeFileRelationService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileRelationResp;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotModelConfigRequest;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
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
    private final DifyApiKeyServiceImpl difyApiKeyService;
    private final DifyApiService difyApiService;
    private final DifyConfig difyConfig;

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

        // 3. 软删除
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

            // 4. 构建数据集配置（使用默认配置）
            DifyChatbotModelConfigRequest.DatasetConfigs datasetConfigs = 
                    DifyChatbotModelConfigRequest.DatasetConfigs.defaultConfig();
            datasetConfigs.setDatasets(datasetCollection);

            // 5. 从配置文件读取模型配置
            DifyChatbotModelConfigRequest.Model model = buildModelFromConfig();
            DifyChatbotModelConfigRequest.DatasetConfigs.RerankingModel rerankingModel = buildRerankingModelFromConfig();
            
            // 设置重排序模型到数据集配置
            datasetConfigs.setRerankingModel(rerankingModel);

            // 6. 构建模型配置请求
            DifyChatbotModelConfigRequest configRequest = new DifyChatbotModelConfigRequest();
            configRequest.setModel(model);
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

