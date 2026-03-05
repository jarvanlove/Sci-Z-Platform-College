package com.sciz.server.application.service.knowledge.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.file.FileConvertService;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeChatbotStreamReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeListQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeUpdateReq;
import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.dto.response.file.FileUploadResp;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileUploadResp;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeResp;
import com.sciz.server.infrastructure.external.dify.dto.DifyChatbotMessageRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyDatasetPatchRequest;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectMemberRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.enums.KnowledgeStatus;
import com.sciz.server.infrastructure.external.dify.config.DifyDocumentConfig;
import com.sciz.server.infrastructure.external.dify.dto.DifyDatasetRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyDatasetRetrievalModelDto;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
import com.sciz.server.infrastructure.shared.enums.AttachmentCategoryStatus;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.context.AsyncUserContext;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserContext;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.DataPermissionUtil;
import com.sciz.server.infrastructure.shared.utils.FileUtil;
import com.sciz.server.interfaces.converter.KnowledgeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.file.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectRepo projectRepo;
    private final DifyApiService difyApiService;
    private final DifyDocumentConfig difyDocumentConfig;
    private final ObjectMapper objectMapper;
    private final DifyApiKeyServiceImpl difyApiKeyService;
    private final KnowledgeConverter knowledgeConverter;
    private final SysAttachmentRepo attachmentRepo;
    private final SysKnowledgeFileRelationRepo fileRelationRepo;
    private final FileService fileService;
    private final FileConvertService fileConvertService;

    @Autowired
    @Qualifier("globalTaskExecutor")
    private Executor globalTaskExecutor;

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
        // 1. 校验知识库名称是否重复
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "知识库名称不能为空");
        }
        var existingKnowledge = knowledgeBaseRepo.findByName(req.getName().trim());
        if (existingKnowledge != null) {
            log.warn(String.format("知识库名称已存在: name=%s, existingId=%s", req.getName(), existingKnowledge.getId()));
            throw BusinessException.of(ResultCode.KNOWLEDGE_NAME_DUPLICATE, "知识库名称已存在: %s", req.getName());
        }
        // 2. 获取用户ID（优先从请求参数获取，否则从上下文获取）
        Long userId = req.getUserId() != null ? req.getUserId() : StpUtil.getLoginIdAsLong();
        log.info(String.format("创建知识库: userId=%s, name=%s", userId, req.getName()));
        // 3. 查询用户信息
        SysUser user = userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 4. 构建 Dify API 请求（名称、描述 + 与图2 一致的索引/嵌入/检索策略）
        DifyDatasetRequest difyRequest = new DifyDatasetRequest();
        difyRequest.setName(req.getName());
        difyRequest.setDescription(req.getDescription());
        difyRequest.setIndexingTechnique(difyDocumentConfig.getIndexingTechnique());
        difyRequest.setEmbeddingModel(difyDocumentConfig.getEmbeddingModel());
        difyRequest.setEmbeddingModelProvider(difyDocumentConfig.getEmbeddingModelProvider());
        difyRequest.setRetrievalModel(buildDatasetRetrievalModelFromConfig());

        // 5. 获取用户的 Dify API Key（用于调用 Dify API）

        // 查询用户的 DifyApiKey 实体（如果存在）
        DifyApiKey difyApiKey = null;
        QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("key_type", "dataset")
                .eq("is_active", true)
                .last("LIMIT 1");
        difyApiKey = difyApiKeyService.getOne(queryWrapper);

        // 6. 确定 resourceId（优先使用 API Key 的 resourceId，否则使用默认值）
        String resourceId = (difyApiKey != null && difyApiKey.getResourceId() != null)
                ? difyApiKey.getResourceId()
                : defaultResourceId;
        // 7. 调用 Dify API 创建数据集
        ResponseEntity<String> response = difyApiService.createDataset(
                difyRequest, userId, resourceId, "dataset");
        // 8. 检查响应状态
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            // 检查是否是409冲突错误（知识库名称重复）
            if (response.getStatusCode().value() == 409 && response.getBody() != null) {
                try {
                    JsonNode errorBody = objectMapper.readTree(response.getBody());
                    String errorCode = errorBody.has("code") ? errorBody.get("code").asText() : "";
                    if ("dataset_name_duplicate".equals(errorCode)) {
                        log.warn(String.format("知识库名称已存在: name=%s, userId=%s", req.getName(), userId));
                        throw new BusinessException(ResultCode.KNOWLEDGE_NAME_DUPLICATE, "已存在该知识库");
                    }
                } catch (BusinessException e) {
                    throw e; // 重新抛出业务异常
                } catch (Exception e) {
                    // JSON解析失败，继续使用通用错误处理
                    log.warn(String.format("解析Dify错误响应失败: body=%s", response.getBody()));
                }
            }
            log.error(String.format("Dify API调用失败: status=%s, body=%s",
                    response.getStatusCode(), response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR,
                    "创建知识库失败: Dify API调用失败" + String.format("Dify API调用失败: status=%s, body=%s",
                            response.getStatusCode(), response.getBody()));
        }
        // 9. 解析返回的JSON
        JsonNode responseJson;
        try {
            responseJson = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error(String.format("解析Dify API响应失败: body=%s, error=%s", response.getBody(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "解析Dify API响应失败");
        }

        // 10. 提取关键信息
        String difyKnowdataId = responseJson.has("id") ? responseJson.get("id").asText() : null;
        // String difyName = responseJson.has("name") ?
        // responseJson.get("name").asText() : req.getName();
        if (difyKnowdataId == null) {
            log.error(String.format("Dify API返回数据缺少id字段: body=%s", response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, "创建知识库失败: Dify API返回数据异常");
        }
        // 11. 将完整的返回数据存入callback字段（JSON格式）
        String callbackJson = response.getBody();
        // 12. 创建知识库实体
        SysKnowledgeBase entity = new SysKnowledgeBase();
        // entity.setName(difyName);
        entity.setName(req.getName());
        entity.setDescription(req.getDescription()); // 使用用户输入的描述
        entity.setOwnerId(userId);
        entity.setOwnerName(user.getRealName());
        entity.setProjectId(req.getProjectId());
        entity.setProjectName(req.getProjectName());
        entity.setKbType(req.getProjectId() != null ? KnowledgeStatus.PROJECT.getCode() : KnowledgeStatus.PERSONAL.getCode());
        if (req.getProjectId() == null) {
            entity.setProjectId(null);
            entity.setProjectName(null);
        }
        entity.setDifyKbId(difyKnowdataId); // 保留原有字段，兼容旧数据
        entity.setDifyKnowdataId(difyKnowdataId); // 新增字段，存储Dify返回的id
        entity.setCallback(callbackJson); // 存储完整的Dify返回数据
        entity.setIsShared(req.getIsShared() != null ? req.getIsShared() : 0);
        entity.setStatus("active");
        entity.setFileCount(0);
        entity.setFolderCount(0);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        if (req.getCoverFileId() != null) {
            entity.setCoverFileId(req.getCoverFileId());
        }
        if (req.getCoverUrl() != null && !req.getCoverUrl().isBlank()) {
            entity.setCoverUrl(req.getCoverUrl());
        }
        // 13. 保存到数据库
        Long id = knowledgeBaseRepo.save(entity);
        if (id == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }
        // 14. 构建响应
        KnowledgeResp resp = new KnowledgeResp();
        resp.setId(id);
        resp.setName(entity.getName());
        resp.setDescription(entity.getDescription());
        resp.setOwnerId(entity.getOwnerId());
        resp.setOwnerName(entity.getOwnerName());
        resp.setProjectId(entity.getProjectId());
        resp.setProjectName(entity.getProjectName());
        resp.setKbType(entity.getKbType());
        resp.setDifyKbId(entity.getDifyKbId());
        resp.setDifyKnowdataId(entity.getDifyKnowdataId());
        resp.setCallback(entity.getCallback());
        resp.setIsShared(entity.getIsShared());
        resp.setStatus(entity.getStatus());
        resp.setFileCount(entity.getFileCount());
        resp.setFolderCount(entity.getFolderCount());
        resp.setCoverFileId(entity.getCoverFileId());
        resp.setCoverUrl(entity.getCoverUrl());
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

        // 2. 判断是否是管理员：管理员可以查看所有知识库，普通用户只能查看自己创建的知识库
        if (DataPermissionUtil.isAdmin()) {
            // 管理员：不传递 userId，让 Repository 查询所有知识库
            userId = null;
            log.info("管理员查询所有知识库");
        } else if (userId != null) {
            // 普通用户：只能查看自己创建的知识库
            log.info(String.format("普通用户查询自己的知识库: userId=%s", userId));
        }

        // 3. 创建分页对象
        Page<SysKnowledgeBase> pageParam = new Page<>(page, size);

        // 4. 执行分页查询
        var pageResult = knowledgeBaseRepo.pageByCondition(pageParam, userId);

        // 5. 转换为响应DTO列表
        var respList = knowledgeConverter.toRespList(pageResult.getRecords());

        // 6. 构建分页结果
        var result = new PageResult<KnowledgeResp>(
                respList,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize());

        log.info(String.format("分页查询知识库成功: total=%s, current=%s, size=%s",
                result.getTotal(), result.getCurrent(), result.getSize()));
        return result;
    }

    /**
     * 分页查询知识库列表（支持关键字搜索）
     *
     * @param req 查询请求（包含分页参数和关键字）
     * @return 知识库分页结果
     */
    @Override
    public PageResult<KnowledgeResp> pageKnowledgeBases(KnowledgeListQueryReq req) {
        // 1. 获取当前登录用户ID（如果已登录）
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
                log.info(String.format("分页查询知识库（支持关键字）: userId=%s, pageNo=%s, pageSize=%s, keyword=%s",
                        userId, req.pageNo(), req.pageSize(), req.keyword()));
            } else {
                log.info(String.format("分页查询知识库（支持关键字，未登录）: pageNo=%s, pageSize=%s, keyword=%s",
                        req.pageNo(), req.pageSize(), req.keyword()));
            }
        } catch (Exception e) {
            log.debug("用户未登录，查询所有知识库");
        }

        // 2. 可见性：管理员查全部；普通用户 = 本人创建的 + 作为项目成员或项目负责人可见的项目知识库
        //    点击「个人/项目」Tab 时前端会传 ownerId，此时必须按该用户算参与项目列表，否则「项目」Tab 会查不到数据
        List<Long> memberProjectIds = List.of();
        Long effectiveId = req.ownerId() != null ? req.ownerId() : userId;
        if (effectiveId != null) {
            List<Long> asMember = projectMemberRepo.findProjectIdsByUserId(effectiveId);
            List<Long> asManager = projectRepo.findProjectIdsByManagerId(effectiveId);
            List<Long> merged = new ArrayList<>(asMember);
            for (Long pid : asManager) {
                if (!merged.contains(pid)) merged.add(pid);
            }
            memberProjectIds = merged;
            log.info(String.format("知识库列表: effectiveId=%s, kbType=%s, memberProjectCount=%d",
                    effectiveId, req.kbType(), memberProjectIds.size()));
        }
        if (DataPermissionUtil.isAdmin() && req.ownerId() == null) {
            userId = null;
            log.info("管理员查询所有知识库（未指定 ownerId）");
        }

        // 3. 创建分页对象
        Page<SysKnowledgeBase> pageParam = new Page<>(req.pageNo(), req.pageSize());

        // 4. 判断排序方式
        boolean asc = "ASC".equalsIgnoreCase(req.sortOrder());

        // 5. 执行分页查询（支持类型筛选、关键字、项目成员可见性）
        //    前端在「个人 / 项目」Tab 下会显式传入 ownerId（当前登录用户ID），用于后端据此筛选创建人；
        //    如果未传 ownerId，则退回到原有逻辑，仅按 userId 和 kbType / 项目成员进行可见性控制。
        var pageResult = knowledgeBaseRepo.pageByCondition(
                pageParam,
                userId,
                memberProjectIds,
                req.kbType(),
                req.keyword(),
                req.sortBy(),
                asc,
                req.ownerId());

        // 6. 转换为响应DTO列表
        var respList = knowledgeConverter.toRespList(pageResult.getRecords());

        // 7. 循环外一次查询当前用户作为负责人的项目 ID，避免 N+1
        Set<Long> managerProjectIds = new HashSet<>();
        if (userId != null) {
            managerProjectIds.addAll(projectRepo.findProjectIdsByManagerId(userId));
        }

        // 8. 为每个知识库的封面URL生成预签名URL，并设置 canEdit（供前端显示删除按钮等）
        var records = pageResult.getRecords();
        for (int i = 0; i < respList.size(); i++) {
            var resp = respList.get(i);
            if (resp.getCoverUrl() != null && !resp.getCoverUrl().trim().isEmpty()) {
                var presignedUrl = fileService.generatePresignedUrlFromFileUrl(resp.getCoverUrl(), null);
                if (presignedUrl != null) resp.setCoverUrl(presignedUrl);
            }
            if (i < records.size()) {
                var entity = records.get(i);
                // 可编辑：管理员 / 创建人；项目知识库仅创建人/项目负责人可编辑，项目成员仅查看
                boolean canEdit = DataPermissionUtil.isAdmin()
                        || (userId != null && entity.getOwnerId() != null && entity.getOwnerId().equals(userId))
                        || (entity.getProjectId() != null && managerProjectIds.contains(entity.getProjectId()));
                resp.setCanEdit(canEdit);
            }
        }

        // 9. 构建分页结果
        var result = new PageResult<KnowledgeResp>(
                respList,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize());

        log.info(String.format("分页查询知识库成功（支持关键字）: total=%s, current=%s, size=%s, keyword=%s",
                result.getTotal(), result.getCurrent(), result.getSize(), req.keyword()));
        return result;
    }

    /**
     * 上传文件到知识库
     *
     * @param knowledgeId 知识库ID（Dify知识库ID，String类型）
     * @param file        上传的文件
     * @param folderId    文件夹ID（可选，0为根目录）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(int knowledgeId, MultipartFile file, Long folderId) {
        // 单个文件上传，转换为多文件上传处理
        uploadFiles(knowledgeId, Arrays.asList(file), folderId);
    }

    /**
     * 上传多个文件到知识库（异步上传，支持部分成功）
     *
     * @param knowledgeId 知识库ID（Dify知识库ID，String类型）
     * @param files       上传的文件列表
     * @param folderId    文件夹ID（可选，0为根目录）
     * @return 每个文件的上传结果列表（包含成功和失败的详细信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<KnowledgeFileUploadResp> uploadFiles(int knowledgeId, List<MultipartFile> files, Long folderId) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件列表不能为空");
        }
        
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("上传多个文件到知识库: userId=%s, knowledgeId=%s, fileCount=%d, folderId=%s",
                userId, knowledgeId, files.size(), folderId));
        
        // 2. 根据系统知识库ID查询知识库信息（路径变量 id 为系统主键）
        SysKnowledgeBase knowledgeBase = knowledgeBaseRepo.findById((long) knowledgeId);
        if (knowledgeBase == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库不存在");
        }
        // 2.1 权限：管理员可上传；普通用户 = 创建人 或 项目知识库的项目成员 或 项目负责人（负责人视为拥有全部权限的成员）
        if (!DataPermissionUtil.isAdmin()) {
            boolean isOwner = knowledgeBase.getOwnerId().equals(userId);
            boolean isProjectMember = knowledgeBase.getProjectId() != null
                    && projectMemberRepo.findByProjectIdAndUserId(knowledgeBase.getProjectId(), userId) != null;
            boolean isProjectManager = knowledgeBase.getProjectId() != null
                    && projectRepo.findProjectIdsByManagerId(userId).contains(knowledgeBase.getProjectId());
            if (!isOwner && !isProjectMember && !isProjectManager) {
                throw BusinessException.of(ResultCode.FORBIDDEN, "无权向该知识库上传文件");
            }
        }

        // 3. 构建文件批量上传请求
        FileBatchUploadReq batchUploadReq = new FileBatchUploadReq(
                files.toArray(new MultipartFile[0]),
                AttachmentRelationStatus.KNOWLEDGE.getCode(),
                knowledgeBase.getId(),
                knowledgeBase.getName(),
                AttachmentCategoryStatus.DOCUMENT.getCode(),
                0
        );
        
        // 4. 🔥 使用通用的文件服务方法（支持进度返回，支持部分成功）
        List<FileUploadResp> fileUploadResults = fileService.uploadBatchWithProgress(batchUploadReq);
        
        // 5. 将 FileUploadResp 转换为 KnowledgeFileUploadResp，并处理 Dify 上传
        List<KnowledgeFileUploadResp> results = new ArrayList<>();
        List<FileUploadResp> minioSuccessResults = new ArrayList<>(); // 成功上传到 MinIO 的文件
        List<Integer> minioSuccessIndices = new ArrayList<>(); // 成功上传到 MinIO 的文件在 results 中的索引
        
        for (int i = 0; i < fileUploadResults.size(); i++) {
            FileUploadResp fileResult = fileUploadResults.get(i);
            
            // 转换为 KnowledgeFileUploadResp
            KnowledgeFileUploadResp knowledgeResult = KnowledgeFileUploadResp.builder()
                    .fileName(fileResult.getFileName())
                    .success(fileResult.getSuccess())
                    .errorMessage(fileResult.getErrorMessage())
                    .attachmentId(fileResult.getAttachmentId())
                    .fileSize(fileResult.getFileSize())
                    .stage(fileResult.getStage())
                    .stageDescription(fileResult.getStageDescription())
                    .build();
            results.add(knowledgeResult);
            
            // 记录成功上传到 MinIO 的文件（stage >= 2，即使最终可能失败）
            if (fileResult.getStage() != null && fileResult.getStage() >= 2 
                    && fileResult.getAttachmentId() != null) {
                minioSuccessResults.add(fileResult);
                minioSuccessIndices.add(i);
            }
        }
        
        if (minioSuccessResults.isEmpty()) {
            log.warn(String.format("所有文件MinIO上传失败: knowledgeId=%s, total=%d",
                    knowledgeId, files.size()));
            return results; // 返回所有结果（包含失败信息）
        }
        
        // 6. 获取 Dify 相关配置
        String datasetId = knowledgeBase.getDifyKbId();
        SysUser user = userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        DifyApiKey difyApiKey = null;
        QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("key_type", "dataset")
                .eq("is_active", true)
                .last("LIMIT 1");
        difyApiKey = difyApiKeyService.getOne(queryWrapper);
        String resourceId = Optional.ofNullable(difyApiKey)
                .map(DifyApiKey::getResourceId)
                .orElse(defaultResourceId);
        
        // 7. 从成功上传的文件中获取 MultipartFile（用于 Dify 上传）；.doc 转为 .docx 再上传以兼容 Dify
        List<MultipartFile> difyUploadFiles = new ArrayList<>();
        for (FileUploadResp fileResult : minioSuccessResults) {
            MultipartFile file = null;
            for (MultipartFile f : files) {
                if (f.getOriginalFilename().equals(fileResult.getFileName())) {
                    file = f;
                    break;
                }
            }
            if (file == null) {
                continue;
            }
            String ext = FileUtil.getFileExtension(file.getOriginalFilename());
            if ("doc".equalsIgnoreCase(ext)) {
                try {
                    var result = fileConvertService.convertDocToDocx(file.getInputStream(), file.getOriginalFilename());
                    byte[] docxBytes = result.inputStream().readAllBytes();
                    result.close();
                    String docxFileName = result.fileName();
                    MultipartFile docxFile = new MultipartFile() {
                        @Override
                        public String getName() { return "file"; }
                        @Override
                        public String getOriginalFilename() { return docxFileName; }
                        @Override
                        public String getContentType() { return result.mimeType(); }
                        @Override
                        public boolean isEmpty() { return docxBytes.length == 0; }
                        @Override
                        public long getSize() { return docxBytes.length; }
                        @Override
                        public byte[] getBytes() { return docxBytes; }
                        @Override
                        public InputStream getInputStream() { return new ByteArrayInputStream(docxBytes); }
                        @Override
                        public void transferTo(File dest) throws IOException { Files.write(dest.toPath(), docxBytes); }
                    };
                    difyUploadFiles.add(docxFile);
                } catch (Exception e) {
                    log.warn(String.format("doc 转 docx 失败，将按原文件上传 Dify: fileName=%s, err=%s", file.getOriginalFilename(), e.getMessage()));
                    difyUploadFiles.add(file);
                }
            } else {
                difyUploadFiles.add(file);
            }
        }
        
        // 8. 异步上传文件到 Dify API（只上传成功上传到 MinIO 的文件）
        log.info(String.format("开始异步上传 %d 个文件到 Dify API", difyUploadFiles.size()));
        CompletableFuture<List<ResponseEntity<String>>> difyUploadFuture = difyApiService.uploadDocumentsAsync(
                datasetId, difyUploadFiles, userId, resourceId, "dataset");

        // 9. 当前设计为同步等待 Dify 结果后再落库关联，避免“先返回再轮询”的复杂度；若 Dify 延迟可接受可保持现状
        List<ResponseEntity<String>> difyResponses;
        try {
            difyResponses = difyUploadFuture.get();
        } catch (Exception e) {
            log.error(String.format("异步上传文件到Dify失败: knowledgeId=%s, error=%s", knowledgeId, e.getMessage()), e);
            // 🔥 优化方案2：Dify 上传失败时，更新所有相关文件的结果
            for (int i = 0; i < minioSuccessResults.size(); i++) {
                FileUploadResp fileResult = minioSuccessResults.get(i);
                int fileResultIndex = minioSuccessIndices.get(i);
                results.set(fileResultIndex, KnowledgeFileUploadResp.builder()
                        .fileName(fileResult.getFileName())
                        .success(false)
                        .errorMessage("Dify上传失败: " + e.getMessage())
                        .attachmentId(fileResult.getAttachmentId())
                        .fileSize(fileResult.getFileSize())
                        .stage(3)
                        .stageDescription("Dify上传失败")
                        .build());
            }
            return results;
        }

        if (difyResponses == null || difyResponses.size() != difyUploadFiles.size()) {
            log.error(String.format("Dify上传结果数量不匹配: expected=%d, actual=%d",
                    difyUploadFiles.size(), difyResponses != null ? difyResponses.size() : 0));
            // 🔥 优化方案2：结果数量不匹配时，更新所有相关文件的结果
            for (int i = 0; i < minioSuccessResults.size(); i++) {
                FileUploadResp fileResult = minioSuccessResults.get(i);
                int fileResultIndex = minioSuccessIndices.get(i);
                results.set(fileResultIndex, KnowledgeFileUploadResp.builder()
                        .fileName(fileResult.getFileName())
                        .success(false)
                        .errorMessage("Dify上传失败: 结果数量不匹配")
                        .attachmentId(fileResult.getAttachmentId())
                        .fileSize(fileResult.getFileSize())
                        .stage(3)
                        .stageDescription("Dify上传失败")
                        .build());
            }
            return results;
        }

        // 10. 批量查询附件信息，避免循环内 N 次 findById
        List<Long> attachmentIds = minioSuccessResults.stream()
                .map(FileUploadResp::getAttachmentId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, SysAttachment> attachmentMap = attachmentRepo.findByIds(attachmentIds).stream()
                .filter(a -> a != null && a.getId() != null)
                .collect(Collectors.toMap(SysAttachment::getId, a -> a, (a, b) -> a));
        
        // 11. 处理每个文件的上传结果，更新详细状态
        for (int i = 0; i < difyUploadFiles.size(); i++) {
            MultipartFile file = difyUploadFiles.get(i);
            FileUploadResp fileResult = minioSuccessResults.get(i);
            ResponseEntity<String> difyResponse = difyResponses.get(i);
            int fileResultIndex = minioSuccessIndices.get(i);
            
            try {
                results.set(fileResultIndex, KnowledgeFileUploadResp.builder()
                        .fileName(fileResult.getFileName())
                        .success(false)
                        .attachmentId(fileResult.getAttachmentId())
                        .fileSize(fileResult.getFileSize())
                        .stage(3)
                        .stageDescription("Dify上传中")
                        .build());

                SysAttachment attachment = attachmentMap.get(fileResult.getAttachmentId());
                if (attachment == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_FOUND, "附件不存在");
                }
                
                // 构建 FileInfoResp
                FileInfoResp minioResult = new FileInfoResp(
                        attachment.getId(),
                        attachment.getFileName(),
                        attachment.getOriginalName(),
                        attachment.getFileType(),
                        attachment.getFileExtension(),
                        attachment.getFileSize(),
                        attachment.getMimeType(),
                        attachment.getFileUrl(),
                        attachment.getFilePath(),
                        attachment.getMd5Hash(),
                        attachment.getIsPublic(),
                        attachment.getDownloadCount(),
                        attachment.getUploaderId(),
                        attachment.getUploaderName(),
                        attachment.getUploadTime(),
                        null,
                        null
                );
                
                processSingleFileUpload(knowledgeId, file, folderId, difyResponse, userId, knowledgeBase, minioResult);
                
                // 更新结果：全部完成（展示用原始文件名与大小，含 .doc 转 .docx 场景）
                results.set(fileResultIndex, KnowledgeFileUploadResp.builder()
                        .fileName(fileResult.getFileName())
                        .success(true)
                        .attachmentId(fileResult.getAttachmentId())
                        .fileSize(fileResult.getFileSize())
                        .stage(5)
                        .stageDescription("上传完成")
                        .build());
                log.info(String.format("文件上传成功: fileName=%s, attachmentId=%s", 
                        fileResult.getFileName(), fileResult.getAttachmentId()));
            } catch (Exception e) {
                // 更新结果：处理失败，仅向前端返回用户友好文案，技术细节仅写日志
                String userMessage = toUserFriendlyKnowledgeUploadError(e);
                results.set(fileResultIndex, KnowledgeFileUploadResp.builder()
                        .fileName(fileResult.getFileName())
                        .success(false)
                        .errorMessage(userMessage)
                        .attachmentId(fileResult.getAttachmentId())
                        .fileSize(fileResult.getFileSize())
                        .stage(4)
                        .stageDescription("处理失败")
                        .build());
                log.error(String.format("处理文件上传结果失败: fileName=%s, error=%s",
                        fileResult.getFileName(), e.getMessage()), e);
            }
        }
        
        // 12. 统计并返回结果
        long successCount = results.stream().filter(r -> r.getSuccess() != null && r.getSuccess()).count();
        long failedCount = results.stream().filter(r -> r.getSuccess() == null || !r.getSuccess()).count();
        log.info(String.format("批量上传完成: knowledgeId=%s, total=%d, success=%d, failed=%d",
                knowledgeId, files.size(), successCount, failedCount));
        
        return results;
    }

    /**
     * 处理单个文件的上传结果
     */
    private void processSingleFileUpload(int knowledgeId, MultipartFile file, Long folderId,
            ResponseEntity<String> response, Long userId, SysKnowledgeBase knowledgeBase, FileInfoResp minioResult) {
        // 1. 检查 Dify 响应状态（技术细节仅写日志，向前端返回友好文案）
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            HttpStatusCode status = response.getStatusCode();
            log.error(String.format("Dify API上传文档失败: fileName=%s, status=%s, body=%s",
                    file.getOriginalFilename(), status, response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR,
                    toUserFriendlyMessageByHttpStatus(status));
        }

        // 2. 使用 MinIO 上传结果（已异步上传完成）
        Long attachmentId = minioResult.id();
        if (attachmentId == null) {
            log.error("知识库上传: MinIO 上传结果中 attachmentId 为空");
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "文件存储异常，请重试");
        }
        log.info(String.format("使用MinIO上传结果: attachmentId=%s", attachmentId));

        // 3. 解析返回的JSON
        JsonNode responseJson;
        try {
            responseJson = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error(String.format("解析Dify API响应失败: body=%s, error=%s", response.getBody(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "处理上传结果时出错，请重试");
        }

        // 4. 提取文档信息
        JsonNode documentNode = responseJson.get("document");
        if (documentNode == null) {
            log.error(String.format("Dify API返回数据缺少document字段: body=%s", response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, "处理上传结果时出错，请重试");
        }

        String difyDocId = documentNode.has("id") ? documentNode.get("id").asText() : null;
        String documentName = documentNode.has("name") ? documentNode.get("name").asText() : file.getOriginalFilename();
        String batch = responseJson.has("batch") ? responseJson.get("batch").asText() : null;

        if (difyDocId == null) {
            log.error(String.format("Dify API返回数据缺少document.id字段: body=%s", response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, "处理上传结果时出错，请重试");
        }

        // 5. 更新附件记录，保存 Dify 文档ID
        SysAttachment attachment = attachmentRepo.findById(attachmentId);
        if (attachment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "附件记录不存在");
        }
        attachment.setDifyDocId(difyDocId);
        attachment.setUpdatedBy(userId);
        boolean updateSuccess = attachmentRepo.update(attachment);
        if (!updateSuccess) {
            log.warn(String.format("更新附件记录失败: attachmentId=%s, difyDocId=%s", attachmentId, difyDocId));
        } else {
            log.info(String.format("更新附件记录成功: attachmentId=%s, difyDocId=%s", attachmentId, difyDocId));
        }

        // 6. 创建知识库文件关联记录
        SysKnowledgeFileRelation fileRelation = new SysKnowledgeFileRelation();
        fileRelation.setKnowledgeId(knowledgeBase.getId()); // 使用数据库主键ID
        fileRelation.setFolderId(folderId != null ? folderId : 0L);
        fileRelation.setAttachmentId(attachmentId);
        fileRelation.setFileName(attachment.getOriginalName() != null ? attachment.getOriginalName() : documentName);
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
     * 将知识库上传过程中的异常转为对用户友好的提示，避免将 Dify API、status 等技术信息暴露给前端。
     *
     * @param e 原始异常
     * @return 用户可见的简短提示
     */
    private String toUserFriendlyKnowledgeUploadError(Throwable e) {
        if (e == null) {
            return "上传失败，请稍后重试";
        }
        String msg = e.getMessage();
        if (msg != null) {
            if (msg.contains("413") || msg.contains("Payload Too Large")) {
                return "文件过大，请压缩或分批上传";
            }
            if (msg.contains("400") || msg.contains("BAD_REQUEST")) {
                return "文件可能过大、格式不支持或不符合要求，请检查后重试";
            }
            if (msg.contains("500") || msg.contains("502") || msg.contains("503") || msg.contains("SERVER_ERROR") || msg.contains("SERVICE_UNAVAILABLE")) {
                return "知识库服务暂时不可用，请稍后重试";
            }
            if (msg.contains("Dify") || msg.contains("status=") || msg.contains("API")) {
                return "上传失败，请稍后重试";
            }
        }
        return "上传失败，请稍后重试";
    }

    /**
     * 根据 HTTP 状态码返回对用户友好的上传失败提示。
     *
     * @param status 响应状态码
     * @return 用户可见的简短提示
     */
    private String toUserFriendlyMessageByHttpStatus(HttpStatusCode status) {
        if (status == null) {
            return "上传失败，请稍后重试";
        }
        if (status.value() == 413) {
            return "文件过大，请压缩或分批上传";
        }
        if (status.is4xxClientError()) {
            return "文件可能过大、格式不支持或不符合要求，请检查后重试";
        }
        if (status.is5xxServerError()) {
            return "知识库服务暂时不可用，请稍后重试";
        }
        return "上传失败，请稍后重试";
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
        // 构建用户上下文，用于异步线程
        LoginUserContext userContext = LoginUserContext.of(
                userId,
                String.valueOf(userId), // 使用 userId 作为 username
                String.valueOf(userId), // 使用 userId 作为 realName
                null, null, null, null, null);
        new Thread(() -> {
            try {
                // 设置异步用户上下文，使 LoginUserUtil 和 DataPermissionUtil 在异步线程中也能正常工作
                AsyncUserContext.set(userContext);

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

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info(String.format("删除知识库: id=%s", id));

        // 1. 查询知识库实体
        SysKnowledgeBase entity = knowledgeBaseRepo.findById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库不存在");
        }

        // 2. 关联项目的知识库不允许删除
        if (entity.getProjectId() != null) {
            String projectName = (entity.getProjectName() != null && !entity.getProjectName().trim().isEmpty())
                    ? entity.getProjectName() : "未知项目";
            throw BusinessException.of(ResultCode.FORBIDDEN, "当前知识库关联项目名称%s,不允许删除！", projectName);
        }

        // 3. 检查权限：个人知识库仅创建人可删除；项目知识库已在步骤2禁止删除
        if (!DataPermissionUtil.isAdmin() && !entity.getOwnerId().equals(StpUtil.getLoginIdAsLong())) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "无权删除该知识库");
        }

        // 4. 如果存在 Dify 数据集ID，调用 Dify API 删除数据集
        if (entity.getDifyKnowdataId() != null && !entity.getDifyKnowdataId().trim().isEmpty()) {
            try {
                // 获取用户的 Dify API Key
                QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("key_type", "dataset")
                        .eq("is_active", true)
                        .last("LIMIT 1");
                DifyApiKey difyApiKey = difyApiKeyService.getOne(queryWrapper);

                String resourceId = (difyApiKey != null && difyApiKey.getResourceId() != null)
                        ? difyApiKey.getResourceId()
                        : defaultResourceId;

                // 调用 Dify API 删除数据集
                ResponseEntity<String> response = difyApiService.deleteDataset(
                        entity.getDifyKnowdataId(),  resourceId, "dataset");

                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.warn(String.format("Dify API删除数据集失败: datasetId=%s, status=%s, body=%s",
                            entity.getDifyKnowdataId(), response.getStatusCode(), response.getBody()));
                    // 即使 Dify API 删除失败，也继续删除本地数据（避免数据不一致）
                } else {
                    log.info(String.format("Dify API删除数据集成功: datasetId=%s", entity.getDifyKnowdataId()));
                }
            } catch (Exception e) {
                log.error(String.format("调用 Dify API 删除数据集异常: datasetId=%s, err=%s",
                        entity.getDifyKnowdataId(), e.getMessage()), e);
                // 即使 Dify API 调用异常，也继续删除本地数据（避免数据不一致）
            }
        }
        // 5. 软删除本地知识库
        boolean success = knowledgeBaseRepo.deleteById(id);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }
        log.info(String.format("删除知识库成功: id=%s", id));
    }

    /**
     * 更新知识库
     *
     * @param id  知识库ID
     * @param req 更新请求
     * @return 知识库响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeResp update(Long id, KnowledgeUpdateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("更新知识库: userId=%s, id=%s", userId, id));

        // 1. 查询知识库实体
        SysKnowledgeBase entity = knowledgeBaseRepo.findById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库不存在");
        }

        // 2. 检查权限：管理员可更新全部；普通用户 = 创建人 或 项目知识库的项目成员 或 项目负责人
        if (!DataPermissionUtil.isAdmin()) {
            boolean isOwner = entity.getOwnerId().equals(userId);
            boolean isProjectMember = entity.getProjectId() != null
                    && projectMemberRepo.findByProjectIdAndUserId(entity.getProjectId(), userId) != null;
            boolean isProjectManager = entity.getProjectId() != null
                    && projectRepo.findProjectIdsByManagerId(userId).contains(entity.getProjectId());
            if (!isOwner && !isProjectMember && !isProjectManager) {
                throw BusinessException.of(ResultCode.FORBIDDEN, "无权更新该知识库");
            }
        }

        // 3. 更新字段（只更新非空字段）
        boolean hasUpdate = false;
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            // 检查名称是否重复（排除自己）
            var existingKnowledge = knowledgeBaseRepo.findByName(req.getName().trim());
            if (existingKnowledge != null && !existingKnowledge.getId().equals(id)) {
                throw BusinessException.of(ResultCode.KNOWLEDGE_NAME_DUPLICATE, "知识库名称已存在: %s", req.getName());
            }
            entity.setName(req.getName().trim());
            hasUpdate = true;
        }
        if (req.getDescription() != null) {
            entity.setDescription(req.getDescription());
            hasUpdate = true;
        }
        if (req.getProjectId() != null) {
            entity.setProjectId(req.getProjectId());
            hasUpdate = true;
        }
        // 仅个人知识库允许修改 is_shared；项目知识库忽略共享字段
        if (req.getIsShared() != null && entity.getProjectId() == null) {
            entity.setIsShared(req.getIsShared());
            hasUpdate = true;
        }

        // 4. 如果有更新，保存到数据库
        boolean nameOrDescUpdated = (req.getName() != null && !req.getName().trim().isEmpty()) || req.getDescription() != null;
        if (hasUpdate) {
            entity.setUpdatedBy(userId);
            boolean success = knowledgeBaseRepo.updateById(entity);
            if (!success) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
            }
            log.info(String.format("更新知识库成功: id=%s", id));

            // 4.1 若名称或描述有变更，同步到 Dify（PATCH /datasets/{dataset_id}），实现系统与 Dify 联动
            String difyDatasetId = entity.getDifyKbId() != null ? entity.getDifyKbId() : entity.getDifyKnowdataId();
            Boolean difySyncSuccess = null;
            String difySyncMessage = null;
            if (nameOrDescUpdated && difyDatasetId != null && !difyDatasetId.isEmpty()) {
                try {
                    QueryWrapper<DifyApiKey> keyQuery = new QueryWrapper<>();
                    keyQuery.eq("key_type", "dataset").eq("is_active", true).last("LIMIT 1");
                    DifyApiKey apiKey = difyApiKeyService.getOne(keyQuery);
                    String resourceId = (apiKey != null && apiKey.getResourceId() != null) ? apiKey.getResourceId() : defaultResourceId;
                    DifyDatasetPatchRequest patchReq = DifyDatasetPatchRequest.builder()
                            .name(entity.getName())
                            .description(entity.getDescription())
                            .build();
                    ResponseEntity<String> patchResponse = difyApiService.patchDataset(difyDatasetId, patchReq, userId, resourceId, "dataset");
                    if (patchResponse.getStatusCode().is2xxSuccessful()) {
                        log.info("知识库名称/描述已同步至 Dify: id={}, difyDatasetId={}", id, difyDatasetId);
                        difySyncSuccess = true;
                    } else {
                        log.warn("同步知识库至 Dify 未成功: id={}, difyDatasetId={}, status={}", id, difyDatasetId, patchResponse.getStatusCode());
                        difySyncSuccess = false;
                        difySyncMessage = "Dify 同步失败: " + patchResponse.getStatusCode();
                    }
                } catch (Exception e) {
                    log.warn("同步知识库至 Dify 异常（本地已更新）: id={}, difyDatasetId={}, err={}", id, difyDatasetId, e.getMessage());
                    difySyncSuccess = false;
                    difySyncMessage = e.getMessage() != null ? e.getMessage() : "Dify 同步异常";
                }
            }

            // 5. 构建响应（含 Dify 同步结果，便于前端展示）
            KnowledgeResp resp = knowledgeConverter.toResp(entity);
            resp.setDifySyncSuccess(difySyncSuccess);
            resp.setDifySyncMessage(difySyncMessage);
            return resp;
        } else {
            log.info(String.format("知识库无需更新: id=%s", id));
        }

        return knowledgeConverter.toResp(entity);
    }

    @Override
    public KnowledgeResp getById(Long id) {
        SysKnowledgeBase entity = knowledgeBaseRepo.findById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "知识库不存在");
        }
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
        }
        // 可见性：管理员可见全部；否则 = 本人创建的 OR 项目成员/负责人可见的项目知识库 OR 他人公开的个人知识库
        if (!DataPermissionUtil.isAdmin() && userId != null) {
            boolean isOwner = entity.getOwnerId().equals(userId);
            boolean isProjectMember = entity.getProjectId() != null
                    && projectMemberRepo.findByProjectIdAndUserId(entity.getProjectId(), userId) != null;
            boolean isProjectManager = entity.getProjectId() != null
                    && projectRepo.findProjectIdsByManagerId(userId).contains(entity.getProjectId());
            boolean isSharedPersonal = entity.getProjectId() == null
                    && Integer.valueOf(1).equals(entity.getIsShared());
            if (!isOwner && !isProjectMember && !isProjectManager && !isSharedPersonal) {
                throw BusinessException.of(ResultCode.FORBIDDEN, "无权查看该知识库");
            }
        } else if (!DataPermissionUtil.isAdmin() && userId == null) {
            // 未登录仅允许查看公开个人知识库
            boolean isSharedPersonal = entity.getProjectId() == null
                    && Integer.valueOf(1).equals(entity.getIsShared());
            if (!isSharedPersonal) {
                throw BusinessException.of(ResultCode.FORBIDDEN, "无权查看该知识库");
            }
        }
        KnowledgeResp resp = knowledgeConverter.toResp(entity);
        // 可编辑：管理员 / 创建人；个人知识库共享后他人仅查看；项目知识库仅创建人/项目负责人可编辑，项目成员仅查看
        boolean canEdit = DataPermissionUtil.isAdmin()
                || (userId != null && entity.getOwnerId() != null && entity.getOwnerId().equals(userId))
                || (entity.getProjectId() != null && userId != null && projectRepo.findProjectIdsByManagerId(userId).contains(entity.getProjectId()));
        resp.setCanEdit(canEdit);
        if (resp.getCoverUrl() != null && !resp.getCoverUrl().trim().isEmpty()) {
            var presignedUrl = fileService.generatePresignedUrlFromFileUrl(resp.getCoverUrl(), null);
            if (presignedUrl != null) {
                resp.setCoverUrl(presignedUrl);
            }
        }
        return resp;
    }

    /**
     * 上传知识库封面
     *
     * @param knowledgeId 知识库ID
     * @param file        封面图片文件
     * @return 知识库响应（包含更新后的封面信息）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeResp uploadCover(Long knowledgeId, MultipartFile file) {
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("上传知识库封面开始: userId=%s, knowledgeId=%s, fileName=%s",
                userId, knowledgeId, file.getOriginalFilename()));

        // 2. 查询知识库实体
        SysKnowledgeBase entity = knowledgeBaseRepo.findById(knowledgeId);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "知识库不存在");
        }

        // 3. 检查权限：管理员可更新全部；普通用户 = 创建人 或 项目知识库的项目成员 或 项目负责人
        if (!DataPermissionUtil.isAdmin()) {
            boolean isOwner = entity.getOwnerId().equals(userId);
            boolean isProjectMember = entity.getProjectId() != null
                    && projectMemberRepo.findByProjectIdAndUserId(entity.getProjectId(), userId) != null;
            boolean isProjectManager = entity.getProjectId() != null
                    && projectRepo.findProjectIdsByManagerId(userId).contains(entity.getProjectId());
            if (!isOwner && !isProjectMember && !isProjectManager) {
                throw BusinessException.of(ResultCode.FORBIDDEN, "无权更新该知识库");
            }
        }

        // 4. 查询用户信息
        SysUser user = userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 5. 调用文件服务上传封面
        FileUploadReq fileUploadReq = new FileUploadReq();
        fileUploadReq.setFile(file);
        fileUploadReq.setRelationType(AttachmentRelationStatus.KNOWLEDGE.getCode());
        fileUploadReq.setRelationId(knowledgeId);
        fileUploadReq.setRelationName(entity.getName());
        fileUploadReq.setAttachmentType(AttachmentCategoryStatus.IMAGE.getCode());
        fileUploadReq.setIsPublic(0);

        FileInfoResp fileInfo = fileService.upload(fileUploadReq, userId, user.getRealName());

        // 6. 更新知识库封面URL和附件ID（使用fileUrl，存储格式：bucketName/filePath）
        entity.setCoverUrl(fileInfo.fileUrl());
        entity.setCoverFileId(fileInfo.id());
        entity.setUpdatedBy(userId);

        boolean success = knowledgeBaseRepo.updateById(entity);
        if (!success) {
            log.error(String.format("更新知识库封面失败: knowledgeId=%s", knowledgeId));
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("上传知识库封面成功: knowledgeId=%s, coverUrl=%s, coverFileId=%s",
                knowledgeId, fileInfo.fileUrl(), fileInfo.id()));

        // 7. 转换为响应DTO
        var resp = knowledgeConverter.toResp(entity);

        // 8. 为封面URL生成预签名URL（参考pageKnowledgeBases方法的处理方式）
        if (resp.getCoverUrl() != null && !resp.getCoverUrl().trim().isEmpty()) {
            var presignedUrl = fileService.generatePresignedUrlFromFileUrl(resp.getCoverUrl(), null);
            if (presignedUrl != null) {
                resp.setCoverUrl(presignedUrl);
            }
        }

        return resp;
    }

    /**
     * 从 DifyDocumentConfig 构建创建知识库时的 retrieval_model，与图2 配置一致（权重融合、top_k 等）
     */
    private DifyDatasetRetrievalModelDto buildDatasetRetrievalModelFromConfig() {
        DifyDocumentConfig.RetrievalModel rm = difyDocumentConfig.getRetrievalModel();
        String rerankingMode = rm.getRerankingMode() != null ? rm.getRerankingMode() : "weight";

        DifyDatasetRetrievalModelDto.WeightsDto weights = null;
        if ("weight".equals(rerankingMode)) {
            weights = DifyDatasetRetrievalModelDto.WeightsDto.builder()
                    .weightType("customized")
                    .keywordSetting(DifyDatasetRetrievalModelDto.KeywordSettingDto.builder()
                            .keywordWeight(rm.getKeywordWeight() != null ? rm.getKeywordWeight() : 0.3)
                            .build())
                    .vectorSetting(DifyDatasetRetrievalModelDto.VectorSettingDto.builder()
                            .vectorWeight(rm.getVectorWeight() != null ? rm.getVectorWeight() : 0.7)
                            .embeddingModelName(difyDocumentConfig.getEmbeddingModel())
                            .embeddingProviderName(difyDocumentConfig.getEmbeddingModelProvider())
                            .build())
                    .build();
        }

        return DifyDatasetRetrievalModelDto.builder()
                .searchMethod(rm.getSearchMethod() != null ? rm.getSearchMethod() : "hybrid_search")
                .rerankingEnable(rm.getRerankingEnable() != null ? rm.getRerankingEnable() : true)
                .rerankingMode(rerankingMode)
                .topK(rm.getTopK() != null ? rm.getTopK() : 6)
                .scoreThresholdEnabled(rm.getScoreThresholdEnabled() != null ? rm.getScoreThresholdEnabled() : false)
                .scoreThreshold(rm.getScoreThreshold())
                .weights(weights)
                .build();
    }
}