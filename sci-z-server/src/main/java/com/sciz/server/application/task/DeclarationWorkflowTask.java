package com.sciz.server.application.task;

import com.sciz.server.application.service.declaration.DeclarationService;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationUpdateStatusReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.dto.response.DeclarationWorkflowResp;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import com.sciz.server.infrastructure.shared.enums.*;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationUpdatedEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;
import com.sciz.server.infrastructure.shared.context.AsyncUserContext;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申报工作流异步任务处理类
 *
 * @author JiaWen.Wu
 * @className DeclarationWorkflowTask
 * @date 2025-01-15 15:00
 */
@Slf4j
@Component
public class DeclarationWorkflowTask {

    private final DifyWorkflowService difyWorkflowService;
    private final DeclarationRepo declarationRepo;
    private final DeclarationService declarationService;
    private final FileService fileService;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final SysUserRepo sysUserRepo;
    private final EventPublisher eventPublisher;

    /**
     * 构造函数
     * 使用 @Lazy 延迟注入 DeclarationService，避免与 DeclarationServiceImpl 的循环依赖
     *
     * @param difyWorkflowService          Dify工作流服务
     * @param declarationRepo               申报仓储
     * @param declarationService            申报服务（延迟注入，避免循环依赖）
     * @param fileService                   文件服务
     * @param sysAttachmentRelationRepo     附件关联仓储
     * @param sysUserRepo                   用户仓储
     * @param eventPublisher                事件发布器
     */
    public DeclarationWorkflowTask(
            DifyWorkflowService difyWorkflowService,
            DeclarationRepo declarationRepo,
            @Lazy DeclarationService declarationService,
            FileService fileService,
            SysAttachmentRelationRepo sysAttachmentRelationRepo,
            SysUserRepo sysUserRepo,
            EventPublisher eventPublisher) {
        this.difyWorkflowService = difyWorkflowService;
        this.declarationRepo = declarationRepo;
        this.declarationService = declarationService;
        this.fileService = fileService;
        this.sysAttachmentRelationRepo = sysAttachmentRelationRepo;
        this.sysUserRepo = sysUserRepo;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 处理申报工作流（同步调试版本）
     * 用于本地debug，不包含 @Async 注解，可以同步执行和调试
     * 
     * ⚠️ 注意：此方法仅用于本地调试，生产环境请使用 processDeclarationWorkflow 异步方法
     *
     * @param declarationId 申报ID
     * @param resourceId    资源ID（工作流ID，用于查找 API Key）
     * @param inputs        工作流输入参数（类型安全）
     * @param userId        用户ID
     * @param keyType       密钥类型（workflow/file/chatbot）
     */
    public void processDeclarationWorkflowSync(Long declarationId, String resourceId,
            DeclarationWorkflowReq inputs, Long userId, String keyType) {
        log.info(String.format("开始同步处理申报工作流（调试模式）: declarationId=%s, resourceId=%s, keyType=%s",
                declarationId, resourceId, keyType));

        try {
            // 1. 更新工作流状态为"处理中"
            updateWorkflowStatus(declarationId, WorkflowStatus.RUNNING, null);

            // 2. 记录工作流启动步骤
            addWorkflowStep(declarationId, "工作流启动", "success");

            // 3. 调用申报工作流
            log.info(String.format("调用 Dify 工作流 API: declarationId=%s, resourceId=%s", declarationId, resourceId));
            DeclarationWorkflowResp workflowOutputs = difyWorkflowService.executeDeclarationWorkflow(
                    inputs, userId, resourceId, keyType);
            String fileUrl = workflowOutputs.fileUrl();

            // 去除 URL 中的所有空格（前后和中间）
            if (fileUrl != null) {
                fileUrl = fileUrl.trim().replaceAll("\\s+", "");
            }

            // 4. 记录 AI 内容分析步骤
            addWorkflowStep(declarationId, "AI 内容分析", "success");

            // 5. 记录项目信息生成步骤
            addWorkflowStep(declarationId, "申报信息生成", "success");

            // 6. 记录数据库存储步骤
            addWorkflowStep(declarationId, "数据库存储", "success");

            if (fileUrl == null || fileUrl.isEmpty()) {
                throw BusinessException.of(ResultCode.SERVER_ERROR, "工作流未返回文件下载URL");
            }

            log.info(String.format("工作流执行完成，获取文件URL: declarationId=%s, fileUrl=%s",
                    declarationId, fileUrl));

            // 7. 从 URL 下载文件
            var fileData = downloadFileFromUrl(fileUrl);

            // 8. 上传文件到 MinIO
            var attachmentId = uploadFileToMinio(declarationId, fileData, fileUrl, userId);

            // 9. 创建附件关联
            createAttachmentRelation(declarationId, attachmentId, userId);

            // 10. 记录申报书生成步骤
            addWorkflowStep(declarationId, "申报书生成", "success");

            // 11. 记录项目创建步骤（标记为"进行中"，实际创建由事件处理器完成）
            addWorkflowStep(declarationId, "项目创建", "running");
            log.info(String.format("项目创建步骤已记录，等待事件处理器创建项目: declarationId=%s", declarationId));

            // 12. 保持工作流状态为"处理中"，等待所有步骤（包括项目创建）完成后，由事件处理器更新为"已完成"
            // 注意：不要在这里更新为 COMPLETED，因为项目创建步骤还是 "running" 状态
            // 当项目创建步骤完成后，DeclarationEventHandler 会检查所有步骤状态并更新工作流状态
            updateWorkflowStatus(declarationId, WorkflowStatus.RUNNING, fileUrl);

            // 13. 工作流执行成功，自动更新申报状态为"申报已提交"（状态2）
            // 注意：updateStatus 会发布 DeclarationSuccessEvent 事件，事件处理器会创建项目和知识库
            updateDeclarationStatus(declarationId, DeclarationStatus.SUCCESS);
            log.info(String.format("工作流执行成功，自动更新申报状态为申报已提交: declarationId=%s", declarationId));

            // 14. 发布申报更新事件
            var declaration = declarationRepo.findById(declarationId);
            if (declaration != null) {
                var event = new DeclarationUpdatedEvent(
                        String.valueOf(declarationId),
                        declaration.getResearchTopic(), // 申报名称（研究课题）
                        String.valueOf(userId),
                        declaration.getApplicantName(), // 申报人姓名
                        String.valueOf(DeclarationStatus.IN_PROGRESS.getCode()),
                        String.valueOf(DeclarationStatus.SUCCESS.getCode()),
                        "申报书生成完成",
                        "工作流执行成功");
                eventPublisher.publish(event);
            } else {
                log.warn(String.format("发布申报更新事件失败：申报不存在: declarationId=%s", declarationId));
            }

            log.info(String.format("申报工作流处理完成: declarationId=%s, attachmentId=%s",
                    declarationId, attachmentId));

        } catch (Exception e) {
            log.error(String.format("申报工作流处理失败: declarationId=%s, err=%s",
                    declarationId, e.getMessage()), e);

            // 更新工作流状态为"失败"
            updateWorkflowStatus(declarationId, WorkflowStatus.FAILED, null);

            // 记录失败步骤
            addWorkflowStep(declarationId, "申报书生成", "failed");

            // 工作流执行失败，自动更新申报状态为"申报未通过"（状态3）
            updateDeclarationStatus(declarationId, DeclarationStatus.FAILED);
            log.info(String.format("工作流执行失败，自动更新申报状态为申报未通过: declarationId=%s", declarationId));
        }
    }

    /**
     * 处理申报工作流（异步）
     * 使用类型安全的工作流输入参数构建
     *
     * @param declarationId 申报ID
     * @param resourceId    资源ID（工作流ID，用于查找 API Key）
     * @param inputs        工作流输入参数（类型安全）
     * @param userId        用户ID
     * @param keyType       密钥类型（workflow/file/chatbot）
     */
    @Async("globalTaskExecutor")
    public void processDeclarationWorkflow(Long declarationId, String resourceId,
            DeclarationWorkflowReq inputs, Long userId, String keyType) {
        log.info(String.format("开始异步处理申报工作流: declarationId=%s, resourceId=%s, keyType=%s",
                declarationId, resourceId, keyType));

        // 设置异步用户上下文，使 LoginUserUtil 和 DataPermissionUtil 在异步线程中也能正常工作
        LoginUserContext userContext = null;
        try {
            // 从 userId 获取用户信息并构建 LoginUserContext
            var user = sysUserRepo.findById(userId);
            if (user != null) {
                userContext = LoginUserContext.of(
                        user.getId(),
                        user.getUsername(),
                        user.getRealName() != null ? user.getRealName() : user.getUsername(),
                        null, null, null, null, null);
                AsyncUserContext.set(userContext);
            } else {
                log.warn(String.format("用户不存在，无法设置异步用户上下文: userId=%s", userId));
            }
        } catch (Exception e) {
            log.warn(String.format("设置异步用户上下文失败: userId=%s, err=%s", userId, e.getMessage()), e);
        }

        try {
            // 1. 更新工作流状态为"处理中"
            updateWorkflowStatus(declarationId, WorkflowStatus.RUNNING, null);

            // 2. 记录工作流启动步骤
            addWorkflowStep(declarationId, "工作流启动", "success");

            // 3. 调用申报工作流
            log.info(String.format("调用 Dify 工作流 API: declarationId=%s, resourceId=%s", declarationId, resourceId));
            DeclarationWorkflowResp workflowOutputs = difyWorkflowService.executeDeclarationWorkflow(
                    inputs, userId, resourceId, keyType);
            String fileUrl = workflowOutputs.fileUrl();

            // 去除 URL 中的所有空格（前后和中间）
            if (fileUrl != null) {
                fileUrl = fileUrl.trim().replaceAll("\\s+", "");
            }

            // 4. 记录 AI 内容分析步骤
            addWorkflowStep(declarationId, "AI 内容分析", "success");

            // 5. 记录项目信息生成步骤
            addWorkflowStep(declarationId, "申报信息生成", "success");

            // 6. 记录数据库存储步骤
            addWorkflowStep(declarationId, "数据库存储", "success");

            if (fileUrl == null || fileUrl.isEmpty()) {
                throw BusinessException.of(ResultCode.SERVER_ERROR, "工作流未返回文件下载URL");
            }

            log.info(String.format("工作流执行完成，获取文件URL: declarationId=%s, fileUrl=%s",
                    declarationId, fileUrl));

            // 7. 从 URL 下载文件
            var fileData = downloadFileFromUrl(fileUrl);

            // 8. 上传文件到 MinIO
            var attachmentId = uploadFileToMinio(declarationId, fileData, fileUrl, userId);

            // 9. 创建附件关联
            createAttachmentRelation(declarationId, attachmentId, userId);

            // 10. 记录申报书生成步骤
            addWorkflowStep(declarationId, "申报书生成", "success");

            // 11. 记录项目创建步骤（标记为"进行中"，实际创建由事件处理器完成）
            addWorkflowStep(declarationId, "项目创建", "running");
            log.info(String.format("项目创建步骤已记录，等待事件处理器创建项目: declarationId=%s", declarationId));

            // 12. 保持工作流状态为"处理中"，等待所有步骤（包括项目创建）完成后，由事件处理器更新为"已完成"
            // 注意：不要在这里更新为 COMPLETED，因为项目创建步骤还是 "running" 状态
            // 当项目创建步骤完成后，DeclarationEventHandler 会检查所有步骤状态并更新工作流状态
            updateWorkflowStatus(declarationId, WorkflowStatus.RUNNING, fileUrl);

            // 13. 工作流执行成功，自动更新申报状态为"申报已提交"（状态2）
            // 注意：updateStatus 会发布 DeclarationSuccessEvent 事件，事件处理器会创建项目和知识库
            updateDeclarationStatus(declarationId, DeclarationStatus.SUCCESS);
            log.info(String.format("工作流执行成功，自动更新申报状态为申报已提交: declarationId=%s", declarationId));

            // 14. 发布申报更新事件
            var declaration = declarationRepo.findById(declarationId);
            if (declaration != null) {
                var event = new DeclarationUpdatedEvent(
                        String.valueOf(declarationId),
                        declaration.getResearchTopic(), // 申报名称（研究课题）
                        String.valueOf(userId),
                        declaration.getApplicantName(), // 申报人姓名
                        String.valueOf(DeclarationStatus.IN_PROGRESS.getCode()),
                        String.valueOf(DeclarationStatus.SUCCESS.getCode()),
                        "申报书生成完成",
                        "工作流执行成功");
                eventPublisher.publish(event);
            } else {
                log.warn(String.format("发布申报更新事件失败：申报不存在: declarationId=%s", declarationId));
            }

            log.info(String.format("申报工作流处理完成: declarationId=%s, attachmentId=%s",
                    declarationId, attachmentId));

        } catch (Exception e) {
            log.error(String.format("申报工作流处理失败: declarationId=%s, err=%s",
                    declarationId, e.getMessage()), e);

            // 更新工作流状态为"失败"
            updateWorkflowStatus(declarationId, WorkflowStatus.FAILED, null);

            // 记录失败步骤
            addWorkflowStep(declarationId, "申报书生成", "failed");

            // 工作流执行失败，自动更新申报状态为"申报未通过"（状态3）
            updateDeclarationStatus(declarationId, DeclarationStatus.FAILED);
            log.info(String.format("工作流执行失败，自动更新申报状态为申报未通过: declarationId=%s", declarationId));
        } finally {
            // 清理异步用户上下文（防止内存泄漏）
            AsyncUserContext.clear();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 更新工作流状态
     */
    private void updateWorkflowStatus(Long declarationId, WorkflowStatus status, String fileUrl) {
        // 重试机制：如果查询不到申报，可能是事务还没提交，等待后重试
        int maxRetries = 5;
        int retryDelayMs = 200; // 每次重试等待 200ms

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                var declaration = declarationRepo.findById(declarationId);
                if (declaration == null) {
                    if (attempt < maxRetries) {
                        log.warn(String.format("申报不存在，等待事务提交后重试: declarationId=%s, attempt=%d/%d",
                                declarationId, attempt, maxRetries));
                        try {
                            Thread.sleep(retryDelayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error(String.format("等待重试被中断: declarationId=%s", declarationId), e);
                            return;
                        }
                        continue; // 重试
                    } else {
                        log.error(String.format("申报不存在（重试%d次后仍失败）: declarationId=%s", maxRetries, declarationId));
                        return;
                    }
                }

                // 获取当前工作流结果
                var workflowResult = getWorkflowResult(declaration);

                // 如果有文件URL，更新到工作流结果中
                if (fileUrl != null && !fileUrl.isEmpty()) {
                    workflowResult.put("fileUrl", fileUrl);
                    // 从文件URL中提取文件格式
                    var fileFormat = extractFileFormat(fileUrl);
                    workflowResult.put("fileFormat", fileFormat);
                }

                // 更新工作流状态和工作流结果
                declarationRepo.updateWorkflowStatus(declarationId, status.getCode(), JsonUtil.toJson(workflowResult));

                log.info(String.format("更新工作流状态成功: declarationId=%s, status=%s", declarationId, status.getCode()));
                break; // 成功执行后退出循环
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn(String.format("更新工作流状态失败，等待后重试: declarationId=%s, attempt=%d/%d, err=%s",
                            declarationId, attempt, maxRetries, e.getMessage()));
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error(String.format("等待重试被中断: declarationId=%s", declarationId), ie);
                        return;
                    }
                    continue; // 重试
                } else {
                    log.error(String.format("更新工作流状态失败（重试%d次后仍失败）: declarationId=%s, err=%s",
                            maxRetries, declarationId, e.getMessage()), e);
                    return;
                }
            }
        }
    }

    /**
     * 添加工作流步骤
     */
    private void addWorkflowStep(Long declarationId, String stepName, String stepStatus) {
        // 重试机制：如果查询不到申报，可能是事务还没提交，等待后重试
        int maxRetries = 5;
        int retryDelayMs = 200; // 每次重试等待 200ms

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                var declaration = declarationRepo.findById(declarationId);
                if (declaration == null) {
                    if (attempt < maxRetries) {
                        log.warn(String.format("申报不存在，等待事务提交后重试: declarationId=%s, stepName=%s, attempt=%d/%d",
                                declarationId, stepName, attempt, maxRetries));
                        try {
                            Thread.sleep(retryDelayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error(String.format("等待重试被中断: declarationId=%s, stepName=%s", declarationId, stepName),
                                    e);
                            return;
                        }
                        continue; // 重试
                    } else {
                        log.error(String.format("申报不存在（重试%d次后仍失败）: declarationId=%s, stepName=%s",
                                maxRetries, declarationId, stepName));
                        return;
                    }
                }

                // 获取当前工作流结果
                var workflowResult = getWorkflowResult(declaration);

                // 获取步骤列表
                @SuppressWarnings("unchecked")
                var steps = (List<Map<String, Object>>) workflowResult.getOrDefault("steps", new ArrayList<>());

                // 添加新步骤
                var step = new HashMap<String, Object>();
                step.put("name", stepName);
                step.put("status", stepStatus);
                step.put("timestamp", DateUtil.formatDateTime(LocalDateTime.now()));
                steps.add(step);

                // 更新工作流结果
                workflowResult.put("steps", steps);
                declarationRepo.updateWorkflowStatus(declarationId,
                        declaration.getWorkflowStatus(), JsonUtil.toJson(workflowResult));

                log.info(String.format("添加工作流步骤成功: declarationId=%s, stepName=%s, stepStatus=%s",
                        declarationId, stepName, stepStatus));
                break; // 成功执行后退出循环
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn(String.format("添加工作流步骤失败，等待后重试: declarationId=%s, stepName=%s, attempt=%d/%d, err=%s",
                            declarationId, stepName, attempt, maxRetries, e.getMessage()));
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error(String.format("等待重试被中断: declarationId=%s, stepName=%s", declarationId, stepName), ie);
                        return;
                    }
                    continue; // 重试
                } else {
                    log.error(String.format("添加工作流步骤失败（重试%d次后仍失败）: declarationId=%s, stepName=%s, err=%s",
                            maxRetries, declarationId, stepName, e.getMessage()), e);
                    return;
                }
            }
        }
    }

    /**
     * 获取工作流结果
     */
    private Map<String, Object> getWorkflowResult(Declaration declaration) {
        var workflowResultJson = declaration.getWorkflowResult();
        if (workflowResultJson == null || workflowResultJson.isEmpty()) {
            return new HashMap<>();
        }
        var result = JsonUtil.fromJsonToMap(workflowResultJson);
        return result != null ? result : new HashMap<>();
    }

    /**
     * 从文件URL中提取文件格式
     */
    private String extractFileFormat(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "unknown";
        }
        var lowerUrl = fileUrl.toLowerCase();
        if (lowerUrl.endsWith(".pdf")) {
            return "pdf";
        } else if (lowerUrl.endsWith(".docx") || lowerUrl.endsWith(".doc")) {
            return "docx";
        }
        return "unknown";
    }

    /**
     * 从 URL 下载文件
     */
    private FileData downloadFileFromUrl(String fileUrl) {
        try {
            // 去除 URL 中的所有空格（前后和中间）
            if (fileUrl != null) {
                fileUrl = fileUrl.trim().replaceAll("\\s+", "");
            }

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
                throw BusinessException.of(ResultCode.SERVER_ERROR,
                        "文件下载失败: HTTP %d", response.statusCode());
            }

            var fileName = extractFileNameFromUrl(fileUrl);
            var contentType = response.headers().firstValue("Content-Type")
                    .orElse("application/octet-stream");

            var fileContent = response.body();

            // 检查文件是否为空
            if (fileContent == null || fileContent.length == 0) {
                throw BusinessException.of(ResultCode.SERVER_ERROR,
                        "文件下载失败: 文件为空（0字节）");
            }

            log.info(String.format("文件下载成功: fileName=%s, size=%d, contentType=%s",
                    fileName, fileContent.length, contentType));

            return new FileData(fileName, fileContent, contentType);

        } catch (Exception e) {
            log.error(String.format("文件下载失败: fileUrl=%s, err=%s", fileUrl, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "文件下载失败: %s", e.getMessage());
        }
    }

    /**
     * 从 URL 中提取文件名
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
            // 清理文件名：去除 Content-Disposition 格式的内容
            return cleanFileName(fileName);
        } catch (Exception e) {
            return "declaration_file_" + System.currentTimeMillis();
        }
    }

    /**
     * 清理文件名，去除 Content-Disposition 格式的内容
     * <p>
     * 处理以下情况：
     * 1. 去除 `; filename=...` 之后的所有内容
     * 2. 去除 `filename*=UTF-8''...` 格式的内容
     * 3. 去除引号
     *
     * @param fileName 原始文件名
     * @return 清理后的文件名
     */
    private String cleanFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "file";
        }

        // 去除分号之后的所有内容（Content-Disposition 格式）
        if (fileName.contains(";")) {
            fileName = fileName.substring(0, fileName.indexOf(';'));
        }

        // 去除引号
        fileName = fileName.replace("\"", "").replace("'", "");

        // 去除前后空格
        fileName = fileName.trim();

        // 如果清理后为空，返回默认文件名
        if (fileName.isEmpty()) {
            return "file";
        }

        return fileName;
    }

    /**
     * 上传文件到 MinIO
     */
    private Long uploadFileToMinio(Long declarationId, FileData fileData,
            String originalUrl, Long userId) {
        try {
            // 1. 验证文件内容不为空
            if (fileData.content == null || fileData.content.length == 0) {
                log.error(String.format("文件内容为空，无法上传: declarationId=%s, fileName=%s",
                        declarationId, fileData.fileName));
                throw BusinessException.of(ResultCode.FILE_UPLOAD_FAILED,
                        "文件内容为空（0字节），无法上传到 MinIO");
            }

            log.info(String.format("开始上传文件到 MinIO: declarationId=%s, fileName=%s, fileSize=%d",
                    declarationId, fileData.fileName, fileData.content.length));

            // 2. 查询申报信息，用于设置 relation_name
            var declaration = declarationRepo.findById(declarationId);
            if (declaration == null) {
                throw BusinessException.of(ResultCode.DECLARATION_NOT_FOUND,
                        "申报不存在: %s", declarationId);
            }

            // 3. 查询用户信息，获取 realName（用于异步上下文）
            var user = sysUserRepo.findById(userId);
            if (user == null) {
                throw BusinessException.of(ResultCode.USER_NOT_FOUND,
                        "用户不存在: %s", userId);
            }
            var realName = user.getRealName() != null ? user.getRealName() : user.getUsername();

            // 4. 生成有意义的文件名：申报编号_申报书.docx（如果研究课题不为空，则使用研究课题）
            var meaningfulFileName = buildMeaningfulFileName(declaration, fileData.fileName);

            // 5. 构建上传请求
            var uploadReq = new FileUploadReq();
            uploadReq.setFile(new ByteArrayMultipartFile(meaningfulFileName, fileData.content, fileData.contentType));
            uploadReq.setRelationType(AttachmentRelationStatus.DECLARATION.getCode());
            uploadReq.setRelationId(declarationId);
            uploadReq.setRelationName(buildDeclarationRelationName(declaration));
            uploadReq.setAttachmentType(AttachmentCategoryStatus.DOCUMENT.getCode());
            uploadReq.setIsPublic(0);

            // 6. 再次验证 MultipartFile 不为空
            if (uploadReq.getFile().isEmpty()) {
                log.error(String.format("MultipartFile 为空，无法上传: declarationId=%s, fileName=%s",
                        declarationId, meaningfulFileName));
                throw BusinessException.of(ResultCode.FILE_UPLOAD_FAILED,
                        "文件内容为空，无法上传到 MinIO");
            }

            // 7. 上传文件（使用支持异步上下文的重载方法，传入 userId 和 realName）
            var fileInfo = fileService.upload(uploadReq, userId, realName);

            log.info(String.format("文件上传成功: declarationId=%s, attachmentId=%s, fileSize=%d",
                    declarationId, fileInfo.id(), fileData.content.length));

            return fileInfo.id();

        } catch (Exception e) {
            log.error(String.format("文件上传失败: declarationId=%s, err=%s",
                    declarationId, e.getMessage()), e);
            throw BusinessException.of(ResultCode.FILE_UPLOAD_FAILED, "文件上传失败: %s", e.getMessage());
        }
    }

    /**
     * 创建附件关联
     */
    private void createAttachmentRelation(Long declarationId, Long attachmentId, Long userId) {
        // 查询申报信息，用于设置 relation_name
        var declaration = declarationRepo.findById(declarationId);
        if (declaration == null) {
            throw BusinessException.of(ResultCode.DECLARATION_NOT_FOUND,
                    "申报不存在: %s", declarationId);
        }

        // 构建 relation_name：申报编号/研究课题（类似 user 的 "admin/系统管理员" 格式）
        var relationName = buildDeclarationRelationName(declaration);

        var relation = new SysAttachmentRelation();
        relation.setAttachmentId(attachmentId);
        relation.setRelationType(AttachmentRelationStatus.DECLARATION.getCode());
        relation.setRelationId(declarationId);
        relation.setRelationName(relationName);
        relation.setAttachmentType(AttachmentCategoryStatus.DOCUMENT.getCode());
        relation.setSortOrder(0);
        relation.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        relation.setCreatedBy(userId);
        relation.setUpdatedBy(userId);
        relation.setCreatedTime(LocalDateTime.now());
        relation.setUpdatedTime(LocalDateTime.now());
        sysAttachmentRelationRepo.save(relation);
    }

    /**
     * 构建申报关联名称
     * <p>
     * 格式：申报编号/研究课题（类似 user 的 "admin/系统管理员" 格式）
     * 如果研究课题过长（超过60字符），则只存申报编号
     *
     * @param declaration 申报实体
     * @return 关联名称
     */
    private String buildDeclarationRelationName(
            com.sciz.server.domain.pojo.entity.declaration.Declaration declaration) {
        var number = declaration.getNumber();
        var researchTopic = declaration.getResearchTopic();

        // 如果研究课题为空或过长，只存申报编号
        if (researchTopic == null || researchTopic.isEmpty() || researchTopic.length() > 60) {
            return number != null ? number : "申报-" + declaration.getId();
        }

        // 格式：申报编号/研究课题
        return String.format("%s/%s", number, researchTopic);
    }

    /**
     * 构建有意义的文件名
     * <p>
     * 格式：申报编号_申报书.扩展名 或 研究课题_申报书.扩展名
     * 如果从URL提取的文件名是UUID格式，则使用申报信息生成有意义的文件名
     *
     * @param declaration 申报实体
     * @param urlFileName 从URL提取的文件名
     * @return 有意义的文件名
     */
    private String buildMeaningfulFileName(Declaration declaration, String urlFileName) {
        // 从URL文件名中提取扩展名
        var extension = "";
        if (urlFileName != null && urlFileName.contains(".")) {
            extension = urlFileName.substring(urlFileName.lastIndexOf('.'));
        } else {
            extension = ".docx"; // 默认扩展名
        }

        // 判断是否为UUID格式的文件名（UUID格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.扩展名）
        var isUuidFormat = urlFileName != null
                && urlFileName.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[^.]+$");

        if (isUuidFormat) {
            // 如果是UUID格式，使用申报信息生成有意义的文件名
            var number = declaration.getNumber();
            var researchTopic = declaration.getResearchTopic();

            // 优先使用研究课题，如果为空或过长则使用申报编号
            if (researchTopic != null && !researchTopic.isEmpty() && researchTopic.length() <= 50) {
                // 清理研究课题中的特殊字符，避免文件名问题
                var cleanTopic = researchTopic.replaceAll("[\\\\/:*?\"<>|]", "_");
                return String.format("%s_申报书%s", cleanTopic, extension);
            } else if (number != null && !number.isEmpty()) {
                return String.format("%s_申报书%s", number, extension);
            } else {
                return String.format("申报_%s_申报书%s", declaration.getId(), extension);
            }
        } else {
            // 如果不是UUID格式，直接使用原文件名
            return urlFileName != null ? urlFileName : "申报书" + extension;
        }
    }

    /**
     * 更新申报状态
     * 调用 DeclarationService.updateStatus 执行完整的业务逻辑（包括事件发布等）
     *
     * @param declarationId 申报ID
     * @param status        申报状态
     */
    private void updateDeclarationStatus(Long declarationId, DeclarationStatus status) {
        try {
            var req = new DeclarationUpdateStatusReq(declarationId, status.getCode());
            declarationService.updateStatus(req);
            log.info(String.format("更新申报状态成功: declarationId=%s, status=%s", declarationId, status.getCode()));
        } catch (Exception e) {
            log.error(String.format("更新申报状态异常: declarationId=%s, err=%s", declarationId, e.getMessage()), e);
        }
    }

    /**
     * 文件数据
     */
    private record FileData(String fileName, byte[] content, String contentType) {
    }

    /**
     * 字节数组 MultipartFile 实现
     */
    private static class ByteArrayMultipartFile implements org.springframework.web.multipart.MultipartFile {
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
