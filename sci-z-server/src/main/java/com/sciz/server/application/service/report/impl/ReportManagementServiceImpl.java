package com.sciz.server.application.service.report.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.application.service.report.ReportManagementService;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementCreateReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementListQueryReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementUpdateReq;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementDetailResp;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementListResp;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.entity.report.ReportManagement;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.domain.pojo.repository.report.ReportManagementRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowRequest;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiKeyService;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import com.sciz.server.infrastructure.external.dify.util.DifyApiClient;
import com.sciz.server.infrastructure.shared.enums.AttachmentCategoryStatus;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.infrastructure.shared.utils.FileUtil;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.infrastructure.shared.utils.MinioUtil;
import com.sciz.server.interfaces.converter.ReportManagementConverter;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 报告管理应用服务实现类
 *
 * @author JiaWen.Wu
 * @className ReportManagementServiceImpl
 * @date 2025-01-24 14:30
 */
@Slf4j
@Service
public class ReportManagementServiceImpl implements ReportManagementService {

    private final ReportManagementRepo reportManagementRepo;
    private final ReportManagementConverter reportManagementConverter;
    private final DifyApiKeyService difyApiKeyService;
    private final DifyWorkflowService difyWorkflowService;
    private final DifyApiClient difyApiClient;
    private final ObjectMapper objectMapper;
    private final DifyConfig difyConfig;
    private final FileService fileService;
    private final Executor globalTaskExecutor;
    private final MinioClient minioClient;
    private final SysAttachmentRepo sysAttachmentRepo;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final SysKnowledgeBaseRepo knowledgeBaseRepo;
    
    private static final DateTimeFormatter DATE_FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    
    @Value("${minio.bucket:sciz-files}")
    private String bucketName;

    public ReportManagementServiceImpl(
            ReportManagementRepo reportManagementRepo,
            ReportManagementConverter reportManagementConverter,
            DifyApiKeyService difyApiKeyService,
            DifyWorkflowService difyWorkflowService,
            DifyApiClient difyApiClient,
            ObjectMapper objectMapper,
            DifyConfig difyConfig,
            FileService fileService,
            @Qualifier("globalTaskExecutor") Executor globalTaskExecutor,
            MinioClient minioClient,
            SysAttachmentRepo sysAttachmentRepo,
            SysAttachmentRelationRepo sysAttachmentRelationRepo, KnowledgeService knowledgeService, SysKnowledgeBaseRepo knowledgeBaseRepo) {
        this.reportManagementRepo = reportManagementRepo;
        this.reportManagementConverter = reportManagementConverter;
        this.difyApiKeyService = difyApiKeyService;
        this.difyWorkflowService = difyWorkflowService;
        this.difyApiClient = difyApiClient;
        this.objectMapper = objectMapper;
        this.difyConfig = difyConfig;
        this.fileService = fileService;
        this.globalTaskExecutor = globalTaskExecutor;
        this.minioClient = minioClient;
        this.sysAttachmentRepo = sysAttachmentRepo;
        this.sysAttachmentRelationRepo = sysAttachmentRelationRepo;
        this.knowledgeBaseRepo = knowledgeBaseRepo;
    }
    /**
     * 报告编号前缀
     */
    private static final String REPORT_NUMBER_PREFIX = "RPT";
    /**
     * 时间戳格式化器（年月日时分秒）
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ReportManagementCreateReq req) {
        log.info(String.format("开始创建报告管理: Id=%s, reportType=%s",
                req.projectId(), req.reportType()));
        try {
            // 1. 获取当前登录用户
            var currentUser = LoginUserUtil.requireCurrentUser();
            var userId = currentUser.userId();
            var realName = currentUser.realName();

            String s = req.projectKnowledgeId();
            // 2. 转换为实体
            var entity = reportManagementConverter.toEntity(req);


            log.info(String.format("设置 Dify API Keys ID 为固定值: difyApiKeysId=9"));

            // 4. 设置报告基本信息
            initializeReportEntity(entity, userId, realName);

            // 5. 保存报告
            var reportId = reportManagementRepo.save(entity);
            if (reportId == null) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "报告保存失败");
            }
            log.info(String.format("报告保存成功: reportId=%s, number=%s", reportId, entity.getNumber()));

            // 6. 异步调用 Dify 工作流生成报告（使用 CompletableFuture 实现真正的异步）
            // 注意：在异步线程中无法获取 Web 上下文，所以需要在调用前获取用户信息
//            CompletableFuture.runAsync(() -> {
                try {
                    triggerDifyWorkflowAsync(reportId, userId, realName);
                } catch (Exception e) {
                    log.error(String.format("异步执行 Dify 工作流失败: reportId=%s, err=%s", 
                            entity.getId(), e.getMessage()), e);
                }
//            }, globalTaskExecutor);
//
            log.info(String.format("已提交异步任务: reportId=%s", reportId));
            return reportId;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("报告创建失败: err=%s", e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "报告创建失败: " + e.getMessage());
        }
    }
    @Override
    public PageResult<ReportManagementListResp> page(ReportManagementListQueryReq req) {
        log.info(String.format("分页查询报告列表: pageNo=%s, pageSize=%s, keyword=%s", 
                req.pageNo(), req.pageSize(), req.keyword()));

        var baseQuery = req.toBaseQuery();
        var page = new Page<ReportManagement>(baseQuery.pageNo(), baseQuery.pageSize());
        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("generateTime");

        IPage<ReportManagement> reportPage = reportManagementRepo.page(
                page, req.keyword(), req.status(), req.reportType(), sortBy, asc);

        var records = reportPage.getRecords().stream()
                .map(reportManagementConverter::toListResp)
                .toList();

        Page<ReportManagementListResp> resultPage = new Page<>(reportPage.getCurrent(), reportPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(reportPage.getTotal());
        return PageResult.of(resultPage);
    }

    @Override
    public ReportManagementDetailResp findDetail(Long id) {
        log.info(String.format("查询报告详情: reportId=%s", id));

        // 1. 查询报告实体
        var report = reportManagementRepo.findById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "报告不存在");
        }

        // 2. 转换为响应对象
        var resp = reportManagementConverter.toDetailResp(report);

        log.info(String.format("查询报告详情成功: reportId=%s", id));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ReportManagementUpdateReq req) {
        log.info(String.format("开始更新报告管理: reportId=%s", req.id()));

        try {
            // 1. 查询报告实体
            var report = reportManagementRepo.findById(req.id());
            if (report == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "报告不存在");
            }

            // 2. 更新实体
            reportManagementConverter.updateEntity(report, req);

            // 3. 设置更新信息
            var currentUser = LoginUserUtil.requireCurrentUser();
            report.setUpdatedBy(currentUser.userId());
            report.setUpdatedTime(LocalDateTime.now());

            // 4. 保存更新
            var success = reportManagementRepo.updateById(report);
            if (!success) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "报告更新失败");
            }

            log.info(String.format("报告更新成功: reportId=%s", req.id()));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("报告更新失败: reportId=%s, err=%s", req.id(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "报告更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info(String.format("开始删除报告管理: reportId=%s", id));

        try {
            // 1. 查询报告实体
            var report = reportManagementRepo.findById(id);
            if (report == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "报告不存在");
            }

            // 2. 软删除
            var success = reportManagementRepo.deleteById(id);
            if (!success) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "报告删除失败");
            }

            log.info(String.format("报告删除成功: reportId=%s", id));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("报告删除失败: reportId=%s, err=%s", id, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "报告删除失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化报告实体基本信息
     *
     * @param entity   报告实体
     * @param userId   用户ID
     * @param realName 用户真实姓名
     */
    private void initializeReportEntity(ReportManagement entity, Long userId, String realName) {
        var now = LocalDateTime.now();
        entity.setNumber(generateReportNumber());
        entity.setCreatorId(userId);
        entity.setCreatorName(realName);
        entity.setStatus("pending"); // 默认状态：待生成
        entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
    }

    /**
     * 生成报告编号
     * 格式：RPT + 年月日时分秒（时间戳）
     * 示例：RPT20250124143025
     *
     * @return 报告编号
     */
    private String generateReportNumber() {
        var timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return REPORT_NUMBER_PREFIX + timestamp;
    }

    /**
     * 异步触发 Dify 工作流生成报告（内部方法，由 CompletableFuture 调用）
     *
     * @param reportId 报告ID
     * @param userId 用户ID
     * @param realName 用户姓名
     */
    private void triggerDifyWorkflowAsync(Long reportId, Long userId, String realName) {
        try {
            // 1. 查询报告实体
            var entity = reportManagementRepo.findById(reportId);

            if (entity == null) {
                log.error(String.format("报告不存在，无法触发工作流: reportId=%s", reportId));
                return;
            }
            
            // 2. 更新报告状态为"生成中"
            entity.setStatus("generating");
            entity.setUpdatedBy(userId);
            entity.setUpdatedTime(LocalDateTime.now());
            reportManagementRepo.updateById(entity);
            log.info(String.format("报告状态已更新为生成中: reportId=%s", reportId));
            // 3. 执行工作流
            triggerDifyWorkflow(entity, userId, realName);
            // 4. 工作流执行成功，状态已在 downloadAndSaveFile 中更新为 "generated"
            log.info(String.format("Dify 工作流异步执行完成: reportId=%s", reportId));
            
        } catch (Exception e) {
            log.error(String.format("Dify 工作流异步执行失败: reportId=%s, err=%s", reportId, e.getMessage()), e);
            // 5. 更新报告状态为"失败"
            try {
                var report = reportManagementRepo.findById(reportId);
                if (report != null) {
                    report.setStatus("failed");
                    report.setUpdatedBy(userId);
                    report.setUpdatedTime(LocalDateTime.now());
                    reportManagementRepo.updateById(report);
                    log.info(String.format("报告状态已更新为失败: reportId=%s", reportId));
                }
            } catch (Exception updateException) {
                log.error(String.format("更新报告状态为失败时出错: reportId=%s, err=%s", 
                        reportId, updateException.getMessage()), updateException);
            }
        }
    }

    /**
     * 触发 Dify 工作流生成报告（内部方法，由异步方法调用）
     *
     * @param entity 报告实体
     * @param userId 用户ID
     * @param realName 用户姓名
     */
    private void triggerDifyWorkflow(ReportManagement entity, Long userId, String realName) {
        log.info(String.format("开始触发 Dify 工作流: reportId=%s, difyApiKeysId=%s", 
                entity.getId(), entity.getDifyApiKeysId()));

        // 1. 根据 difyApiKeysId 获取 DifyApiKey
        entity.setDifyApiKeysId("7");
        if (entity.getDifyApiKeysId() == null || entity.getDifyApiKeysId().trim().isEmpty()) {
            log.warn(String.format("Dify API Keys ID 为空，跳过工作流调用: reportId=%s", entity.getId()));
            return;
        }


        Long difyApiKeysId;
        try {
            difyApiKeysId = Long.parseLong(entity.getDifyApiKeysId());
        } catch (NumberFormatException e) {
            log.error(String.format("Dify API Keys ID 格式错误: difyApiKeysId=%s, err=%s", 
                    entity.getDifyApiKeysId(), e.getMessage()));
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "Dify API Keys ID 格式错误");
        }
        var difyApiKey = difyApiKeyService.getById(difyApiKeysId);
        if (difyApiKey == null) {
            log.error(String.format("Dify API Key 不存在: difyApiKeysId=%s", difyApiKeysId));
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Dify API Key 不存在");
        }
        var workflowId = difyApiKey.getResourceId();
        if (workflowId == null || workflowId.trim().isEmpty()) {
            log.error(String.format("Dify API Key 的 resourceId 为空: difyApiKeysId=%s", difyApiKeysId));
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "工作流ID不存在");
        }

        log.info(String.format("获取到工作流ID: workflowId=%s, keyName=%s, apiKey=%s", 
                workflowId, difyApiKey.getKeyName(), 
                difyApiKey.getApiKey() != null ? "***" + difyApiKey.getApiKey().substring(Math.max(0, difyApiKey.getApiKey().length() - 4)) : "null"));

        // 2. 如果存在项目知识库ID，先更新工作流配置（更新 knowledge-retrieval 节点的 dataset_ids）
        if (StringUtils.hasText(entity.getProjectKnowledgeId())) {
            try {
                log.info(String.format("开始更新工作流配置: workflowId=%s, projectKnowledgeId=%s", 
                        workflowId, entity.getProjectKnowledgeId()));
                // 将 projectKnowledgeId 转换为 List
                Long id = Long.valueOf(entity.getProjectKnowledgeId());
                SysKnowledgeBase byId = knowledgeBaseRepo.findById(id);
                List<String> projectKnowledgeIds = List.of(byId.getDifyKbId());
                
                // 调用综合接口更新并发布工作流
                difyWorkflowService.updateAndPublishWorkflow(
                        workflowId,  // appId 就是 workflowId
                        projectKnowledgeIds,  // 知识库ID列表
                        "",  // markedName 为空
                        ""   // markedComment 为空
                );
                
                log.info(String.format("工作流配置更新并发布成功: workflowId=%s, projectKnowledgeId=%s", 
                        workflowId, entity.getProjectKnowledgeId()));
            } catch (Exception e) {
                log.error(String.format("更新工作流配置失败: workflowId=%s, projectKnowledgeId=%s, err=%s", 
                        workflowId, entity.getProjectKnowledgeId(), e.getMessage()), e);
                // 更新失败不影响后续工作流执行，只记录日志
            }
        } else {
            log.warn(String.format("项目知识库ID为空，跳过工作流配置更新: reportId=%s", entity.getId()));
        }

        // 3. 构建工作流请求参数
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("technology_report", entity.getProjectName());
        inputs.put("SetCongfig", entity.getProjectName());

        // 4. 构建工作流请求（使用流式模式）
        var workflowRequest = new DifyWorkflowRequest();
        workflowRequest.setInputs(inputs);
        workflowRequest.setResponseMode("streaming"); // 使用流式模式
        workflowRequest.setUser(String.valueOf(userId));

        // 5. 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", inputs);
        requestBody.put("response_mode", "streaming");
        requestBody.put("user", String.valueOf(userId));

        // 6. 使用流式方式调用工作流
        log.info(String.format("调用 Dify 工作流（流式）: workflowId=%s, userId=%s", workflowId, userId));
        ResponseEntity<String> response = difyApiClient.requestStream(
                "POST", "/workflows/run", requestBody, userId, workflowId, DifyApiKey.KeyType.WORKFLOW.getCode());

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error(String.format("Dify 工作流执行失败: statusCode=%s, body=%s", 
                    response.getStatusCode(), response.getBody()));
            throw new BusinessException(ResultCode.SERVER_ERROR, 
                    "工作流执行失败: " + (response.getBody() != null ? response.getBody() : response.getStatusCode()));
        }

        // 6. 解析流式响应，提取 body 中的参数
        String responseBody = response.getBody();
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            parseStreamingResponse(responseBody, entity.getId(), userId, realName);
        }

        log.info(String.format("Dify 工作流执行成功: reportId=%s, workflowId=%s", entity.getId(), workflowId));
    }

    /**
     * 解析流式响应，提取 body 中的参数
     *
     * @param responseBody 流式响应体（每行一个 JSON 对象）
     * @param reportId 报告ID（用于日志）
     * @param userId 用户ID（用于更新状态）
     * @param realName 用户姓名（用于文件上传）
     */
    private void parseStreamingResponse(String responseBody, Long reportId, Long userId, String realName) {
        try {
            log.info(String.format("开始解析流式响应: reportId=%s, bodyLength=%d", reportId, responseBody.length()));

            // 按行分割响应体（每行是一个 JSON 对象）
            String[] lines = responseBody.split("\n");
            String workflowFinishedData = null;

            // 遍历所有行，找到 workflow_finished 事件
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }
                try {
                    // 解析 JSON，检查是否是 workflow_finished 事件
                    JsonNode rootNode = objectMapper.readTree(trimmedLine);
                    if (rootNode.has("event") && "workflow_finished".equals(rootNode.get("event").asText())) {
                        workflowFinishedData = trimmedLine;
                        log.info(String.format("找到 workflow_finished 事件: reportId=%s", reportId));
                        break; // 找到后退出循环
                    }
                } catch (Exception e) {
                    // 忽略解析失败的行，继续处理下一行
                    log.debug(String.format("解析行失败（跳过）: line=%s, err=%s", trimmedLine.substring(0, Math.min(50, trimmedLine.length())), e.getMessage()));
                }
            }
            // 如果找到 workflow_finished 事件，提取文件 URL 并下载
            if (workflowFinishedData != null) {
                log.info(String.format("流式响应解析完成: reportId=%s, workflowFinishedDataLength=%d", reportId, workflowFinishedData.length()));
                
                // 提取文件 URL 并下载保存
                try {
                    String fileUrl = extractFileUrlFromLastData(workflowFinishedData);
                    if (StringUtils.hasText(fileUrl)) {
                        downloadAndSaveFile(fileUrl, reportId, userId, realName);
                        // downloadAndSaveFile 中已更新状态为 "generated"
                    } else {
                        log.warn(String.format("未找到文件URL: reportId=%s", reportId));
                        // 未找到文件URL，更新状态为失败
                        updateReportStatusToFailed(reportId, userId);
                    }
                } catch (Exception e) {
                    log.error(String.format("处理文件下载失败: reportId=%s, err=%s", reportId, e.getMessage()), e);
                    // 文件下载失败，更新状态为失败
                    updateReportStatusToFailed(reportId, userId);
                    throw e; // 重新抛出异常，让上层捕获
                }
            } else {
                log.warn(String.format("流式响应中未找到 workflow_finished 事件: reportId=%s", reportId));
                // 未找到 workflow_finished 事件，更新状态为失败
                updateReportStatusToFailed(reportId, userId);
            }

        } catch (Exception e) {
            log.error(String.format("解析流式响应失败: reportId=%s, err=%s", reportId, e.getMessage()), e);
        }
    }

    /**
     * 从响应数据中提取文件 URL
     *
     * @param jsonData JSON 数据字符串
     * @return 文件 URL，如果不存在则返回 null
     */
    private String extractFileUrlFromLastData(String jsonData) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonData);
            // 检查是否是 workflow_finished 事件
            if (rootNode.has("event") && "workflow_finished".equals(rootNode.get("event").asText())) {
                JsonNode dataNode = rootNode.get("data");
                if (dataNode != null && dataNode.has("outputs")) {
                    JsonNode outputsNode = dataNode.get("outputs");
                    
                    // 从 outputs.files 中提取 URL
                    if (outputsNode.has("files") && outputsNode.get("files").isArray()) {
                        JsonNode filesNode = outputsNode.get("files");
                        if (filesNode.size() > 0) {
                            JsonNode firstFile = filesNode.get(0);
                            if (firstFile.has("url")) {
                                String url = firstFile.get("url").asText();
                                log.info(String.format("提取到文件URL: url=%s", url));
                                
                                // 拼接 private-url + url + true
                                String privateUrl = difyConfig.getPrivateUrl();
                                if (!StringUtils.hasText(privateUrl)) {
                                    log.error("Dify private-url 配置为空");
                                    throw new BusinessException(ResultCode.SERVER_ERROR, "Dify private-url 配置为空");
                                }
                                
                                // 确保 privateUrl 不以 / 结尾，url 不以 / 开头
                                if (privateUrl.endsWith("/")) {
                                    privateUrl = privateUrl.substring(0, privateUrl.length() - 1);
                                }
                                if (url.startsWith("/")) {
                                    url = url.substring(1);
                                }
                                
                                // 拼接完整 URL
                                String fullUrl = privateUrl + "/" + url + "&as_attachment=true";
                                
                                log.info(String.format("拼接后的完整URL: fullUrl=%s", fullUrl));
                                return fullUrl;
                            }
                        }
                    }
                }
            } else {
                log.debug(String.format("当前事件不是 workflow_finished: event=%s", 
                        rootNode.has("event") ? rootNode.get("event").asText() : "null"));
            }
            
            return null;
        } catch (Exception e) {
            log.error(String.format("提取文件URL失败: err=%s", e.getMessage()), e);
            return null;
        }
    }

    /**
     * 下载文件并保存到 MinIO（直接上传，不通过 FileService）
     *
     * @param fileUrl 文件下载 URL
     * @param reportId 报告ID
     * @param userId 用户ID
     * @param realName 用户姓名
     */
    private void downloadAndSaveFile(String fileUrl, Long reportId, Long userId, String realName) {
        try {
            log.info(String.format("开始下载文件: fileUrl=%s, reportId=%s", fileUrl, reportId));
            
            // 1. 下载文件
            byte[] fileData = downloadFileFromUrl(fileUrl);
            if (fileData == null || fileData.length == 0) {
                log.warn(String.format("文件数据为空: fileUrl=%s", fileUrl));
                return;
            }
            
            // 2. 提取文件名和 MIME 类型
            String fileName = extractFileNameFromUrl(fileUrl);
            String contentType = "application/octet-stream"; // 默认类型，实际应从响应头获取
            
            // 3. 创建 MultipartFile
            MultipartFile multipartFile = new ByteArrayMultipartFile(fileName, fileData, contentType);
            
            // 4. 查询报告信息，用于设置 relation_name
            var report = reportManagementRepo.findById(reportId);
            if (report == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "报告不存在: " + reportId);
            }
            
            // 5. 直接上传文件到 MinIO（不通过 FileService，避免在异步线程中获取用户信息）
            Long attachmentId = uploadFileToMinioDirectly(multipartFile, reportId, userId, realName, report);
            log.info(String.format("文件上传成功: reportId=%s, attachmentId=%s", reportId, attachmentId));
            
            // 6. 更新报告状态为已生成，并保存附件 ID
            report.setStatus("generated");
            report.setGenerateTime(LocalDateTime.now());
            report.setAttachmentId(attachmentId); // 保存 MinIO 返回的附件 ID
            report.setUpdatedBy(userId);
            report.setUpdatedTime(LocalDateTime.now());
            reportManagementRepo.updateById(report);
            
            log.info(String.format("文件下载并保存完成: reportId=%s, attachmentId=%s", reportId, attachmentId));
            
        } catch (Exception e) {
            log.error(String.format("下载并保存文件失败: fileUrl=%s, reportId=%s, err=%s", 
                    fileUrl, reportId, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "文件下载并保存失败: " + e.getMessage());
        }
    }

    /**
     * 从 URL 下载文件
     *
     * @param fileUrl 文件 URL
     * @return 文件字节数组
     */
    private byte[] downloadFileFromUrl(String fileUrl) {
        try {
            log.info(String.format("开始下载文件: fileUrl=%s", fileUrl));
            
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .timeout(Duration.ofMinutes(10))
                    .GET()
                    .build();
            
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            
            if (response.statusCode() != 200) {
                log.error(String.format("文件下载失败: fileUrl=%s, statusCode=%s", fileUrl, response.statusCode()));
                throw new BusinessException(ResultCode.SERVER_ERROR, 
                        "文件下载失败: HTTP " + response.statusCode());
            }
            
            byte[] fileData = response.body();
            log.info(String.format("文件下载成功: fileUrl=%s, size=%d", fileUrl, fileData.length));
            return fileData;
            
        } catch (Exception e) {
            log.error(String.format("文件下载失败: fileUrl=%s, err=%s", fileUrl, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 更新报告状态为失败
     *
     * @param reportId 报告ID
     * @param userId 用户ID
     */
    private void updateReportStatusToFailed(Long reportId, Long userId) {
        try {
            var report = reportManagementRepo.findById(reportId);
            if (report != null) {
                report.setStatus("failed");
                report.setUpdatedBy(userId);
                report.setUpdatedTime(LocalDateTime.now());
                reportManagementRepo.updateById(report);
                log.info(String.format("报告状态已更新为失败: reportId=%s", reportId));
            }
        } catch (Exception e) {
            log.error(String.format("更新报告状态为失败时出错: reportId=%s, err=%s", 
                    reportId, e.getMessage()), e);
        }
    }

    /**
     * 从 URL 中提取文件名
     *
     * @param url 文件 URL
     * @return 文件名
     */
    private String extractFileNameFromUrl(String url) {
        try {
            var uri = new URI(url);
            var path = uri.getPath();
            var fileName = path.substring(path.lastIndexOf('/') + 1);
            // 如果文件名包含查询参数，需要处理
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf('?'));
            }
            return fileName;
        } catch (Exception e) {
            log.warn(String.format("从URL提取文件名失败: url=%s, 使用默认文件名", url), e);
            return "report_file_" + System.currentTimeMillis() + ".docx";
        }
    }

    /**
     * 构建报告关联名称
     * 格式：报告编号/项目名称
     *
     * @param report 报告实体
     * @return 关联名称
     */
    private String buildReportRelationName(ReportManagement report) {
        var relationName = report.getNumber();
        if (StringUtils.hasText(report.getProjectName())) {
            relationName = relationName + "/" + report.getProjectName();
        }
        return relationName;
    }

    /**
     * 直接上传文件到 MinIO（不通过 FileService，避免在异步线程中获取用户信息）
     *
     * @param multipartFile 文件
     * @param reportId 报告ID
     * @param userId 用户ID
     * @param realName 用户姓名
     * @param report 报告实体
     * @return 附件ID
     */
    private Long uploadFileToMinioDirectly(MultipartFile multipartFile, Long reportId, Long userId, 
            String realName, ReportManagement report) {
        try {
            // 1. 解析文件名和扩展名
            String originalName = StringUtils.hasText(multipartFile.getOriginalFilename())
                    ? multipartFile.getOriginalFilename()
                    : multipartFile.getName();
            String extension = FileUtil.getFileExtension(originalName);
            
            // 2. 构建对象名称（存储路径）
            String folder = LocalDate.now().format(DATE_FOLDER_FORMAT);
            String uniqueName = FileUtil.generateUniqueFileName(originalName);
            String objectName = String.format("%s/%s", folder, uniqueName);
            
            // 3. 解析 MIME 类型
            String mimeType = multipartFile.getContentType();
            if (!StringUtils.hasText(mimeType)) {
                mimeType = FileUtil.getMimeType(originalName);
            }
            if (!StringUtils.hasText(mimeType)) {
                mimeType = "application/octet-stream";
            }
            
            // 4. 计算 MD5
            String md5;
            try (InputStream inputStream = multipartFile.getInputStream()) {
                md5 = DigestUtils.md5DigestAsHex(inputStream);
            }
            
            // 5. 确保存储桶存在
            try {
                MinioUtil.makeBucketIfAbsent(minioClient, bucketName);
            } catch (Exception e) {
                log.warn(String.format("存储桶检查失败（可能已存在）: bucket=%s", bucketName), e);
            }
            
            // 6. 上传到 MinIO
            try (InputStream inputStream = multipartFile.getInputStream()) {
                MinioUtil.upload(minioClient, bucketName, objectName, inputStream, 
                        multipartFile.getSize(), mimeType);
            }
            
            // 7. 构建附件实体
            var now = LocalDateTime.now();
            var attachment = new SysAttachment();
            attachment.setFileName(objectName);
            attachment.setOriginalName(originalName);
            attachment.setFileType(AttachmentCategoryStatus.EXPORT.getCode());
            attachment.setFileExtension(extension);
            attachment.setFileSize(multipartFile.getSize());
            attachment.setFileUrl(String.format("%s/%s", bucketName, objectName));
            attachment.setFilePath(objectName);
            attachment.setMimeType(mimeType);
            attachment.setMd5Hash(md5);
            attachment.setUploaderId(userId);
            attachment.setUploaderName(realName);
            attachment.setUploadTime(now);
            attachment.setDownloadCount(0);
            attachment.setIsPublic(0);
            attachment.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
            attachment.setCreatedBy(userId);
            attachment.setUpdatedBy(userId);
            attachment.setCreatedTime(now);
            attachment.setUpdatedTime(now);
            
            // 8. 保存附件记录
            Long attachmentId = sysAttachmentRepo.save(attachment);
            if (attachmentId == null) {
                throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "附件保存失败");
            }
            attachment.setId(attachmentId);
            // 9. 创建附件关联
            var relation = new SysAttachmentRelation();
            relation.setAttachmentId(attachmentId);
            relation.setRelationType(AttachmentRelationStatus.REPORT.getCode());
            relation.setRelationId(reportId);
            relation.setRelationName(buildReportRelationName(report));
            relation.setAttachmentType(AttachmentCategoryStatus.EXPORT.getCode());
            relation.setSortOrder(0);
            relation.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
            relation.setCreatedBy(userId);
            relation.setUpdatedBy(userId);
            relation.setCreatedTime(now);
            relation.setUpdatedTime(now);
            sysAttachmentRelationRepo.save(relation);
            
            log.info(String.format("文件直接上传到 MinIO 成功: reportId=%s, attachmentId=%s, objectName=%s", 
                    reportId, attachmentId, objectName));
            
            return attachmentId;
            
        } catch (IOException e) {
            log.error(String.format("文件上传到 MinIO 失败（IO异常）: reportId=%s, err=%s", reportId, e.getMessage()), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error(String.format("文件上传到 MinIO 失败: reportId=%s, err=%s", reportId, e.getMessage()), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        }
    }
    /**
     * 字节数组 MultipartFile 实现
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
        public void transferTo(java.io.File dest) {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }
}