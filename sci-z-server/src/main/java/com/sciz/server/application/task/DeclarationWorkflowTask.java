package com.sciz.server.application.task;

import com.sciz.server.application.service.file.FileService;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.dto.response.DeclarationWorkflowResp;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.enums.DeclarationStatus;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.WorkflowStatus;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationUpdatedEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class DeclarationWorkflowTask {

    private final DifyWorkflowService difyWorkflowService;
    private final DeclarationRepo declarationRepo;
    private final FileService fileService;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final EventPublisher eventPublisher;

    /**
     * 处理申报工作流（异步）
     *
     * @param declarationId 申报ID
     * @param workflowId    工作流ID
     * @param inputs        工作流输入参数
     * @param userId        用户ID
     */
    @Async("globalTaskExecutor")
    public void processDeclarationWorkflow(Long declarationId, String workflowId,
            Map<String, Object> inputs, Long userId) {
        log.info(String.format("开始异步处理申报工作流: declarationId=%s, workflowId=%s",
                declarationId, workflowId));

        try {
            // 1. 更新工作流状态为"处理中"
            updateWorkflowStatus(declarationId, WorkflowStatus.RUNNING, null);

            // 2. 记录工作流启动步骤
            addWorkflowStep(declarationId, "工作流启动", "success");

            // 3. 构建工作流请求
            var workflowRequest = new DeclarationWorkflowReq(
                    userId,
                    workflowId,
                    "workflow",
                    inputs,
                    "blocking",
                    String.valueOf(userId));

            // 4. 调用 Dify 工作流 API（阻塞等待完成，3-5分钟）
            log.info(String.format("调用 Dify 工作流 API: declarationId=%s", declarationId));
            var workflowResponse = difyWorkflowService.executeDeclarationWorkflow(workflowRequest);

            // 5. 记录 AI 内容分析步骤
            addWorkflowStep(declarationId, "AI 内容分析", "success");

            // 6. 记录项目信息生成步骤
            addWorkflowStep(declarationId, "项目信息生成", "success");

            // 7. 记录数据库存储步骤
            addWorkflowStep(declarationId, "数据库存储", "success");

            // 8. 解析工作流响应，获取文件下载 URL
            var fileUrl = extractFileUrl(workflowResponse);
            if (fileUrl == null || fileUrl.isEmpty()) {
                throw new BusinessException(ResultCode.SERVER_ERROR, "工作流未返回文件下载URL");
            }

            log.info(String.format("工作流执行完成，获取文件URL: declarationId=%s, fileUrl=%s",
                    declarationId, fileUrl));

            // 9. 从 URL 下载文件
            var fileData = downloadFileFromUrl(fileUrl);

            // 10. 上传文件到 MinIO
            var attachmentId = uploadFileToMinio(declarationId, fileData, fileUrl, userId);

            // 11. 创建附件关联
            createAttachmentRelation(declarationId, attachmentId, userId);

            // 12. 记录申报书生成步骤
            addWorkflowStep(declarationId, "申报书生成", "success");

            // 13. 更新工作流状态为"已完成"，申报状态为"申报成功"
            updateWorkflowStatus(declarationId, WorkflowStatus.COMPLETED, fileUrl);
            updateDeclarationStatus(declarationId, DeclarationStatus.SUCCESS);

            // 14. 发布申报更新事件
            var event = new DeclarationUpdatedEvent(
                    String.valueOf(declarationId),
                    null,
                    String.valueOf(userId),
                    null,
                    String.valueOf(DeclarationStatus.IN_PROGRESS.getCode()),
                    String.valueOf(DeclarationStatus.SUCCESS.getCode()),
                    "申报书生成完成",
                    "工作流执行成功");
            eventPublisher.publish(event);

            log.info(String.format("申报工作流处理完成: declarationId=%s, attachmentId=%s",
                    declarationId, attachmentId));

        } catch (Exception e) {
            log.error(String.format("申报工作流处理失败: declarationId=%s, err=%s",
                    declarationId, e.getMessage()), e);

            // 更新工作流状态为"失败"，申报状态为"申报失败"
            updateWorkflowStatus(declarationId, WorkflowStatus.FAILED, null);
            updateDeclarationStatus(declarationId, DeclarationStatus.FAILED);

            // 记录失败步骤
            addWorkflowStep(declarationId, "申报书生成", "failed");
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 更新工作流状态
     */
    private void updateWorkflowStatus(Long declarationId, WorkflowStatus status, String fileUrl) {
        try {
            var declaration = declarationRepo.findById(declarationId);
            if (declaration == null) {
                log.error(String.format("申报不存在: declarationId=%s", declarationId));
                return;
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
        } catch (Exception e) {
            log.error(String.format("更新工作流状态失败: declarationId=%s, err=%s", declarationId, e.getMessage()), e);
        }
    }

    /**
     * 添加工作流步骤
     */
    private void addWorkflowStep(Long declarationId, String stepName, String stepStatus) {
        try {
            var declaration = declarationRepo.findById(declarationId);
            if (declaration == null) {
                log.error(String.format("申报不存在: declarationId=%s", declarationId));
                return;
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
        } catch (Exception e) {
            log.error(String.format("添加工作流步骤失败: declarationId=%s, err=%s", declarationId, e.getMessage()), e);
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
     * 从工作流响应中提取文件下载 URL
     */
    private String extractFileUrl(DeclarationWorkflowResp response) {
        if (response == null || response.data() == null ||
                response.data().outputs() == null) {
            return null;
        }

        var outputs = response.data().outputs();
        if (outputs.files() != null && !outputs.files().isEmpty()) {
            return outputs.files().get(0).url();
        }

        return null;
    }

    /**
     * 从 URL 下载文件
     */
    private FileData downloadFileFromUrl(String fileUrl) {
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
                throw new BusinessException(ResultCode.SERVER_ERROR,
                        "文件下载失败: HTTP " + response.statusCode());
            }

            var fileName = extractFileNameFromUrl(fileUrl);
            var contentType = response.headers().firstValue("Content-Type")
                    .orElse("application/octet-stream");

            log.info(String.format("文件下载成功: fileName=%s, size=%d, contentType=%s",
                    fileName, response.body().length, contentType));

            return new FileData(fileName, response.body(), contentType);

        } catch (Exception e) {
            log.error(String.format("文件下载失败: fileUrl=%s, err=%s", fileUrl, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "文件下载失败: " + e.getMessage());
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
            return fileName;
        } catch (Exception e) {
            return "declaration_file_" + System.currentTimeMillis();
        }
    }

    /**
     * 上传文件到 MinIO
     */
    private Long uploadFileToMinio(Long declarationId, FileData fileData,
            String originalUrl, Long userId) {
        try {
            log.info(String.format("开始上传文件到 MinIO: declarationId=%s, fileName=%s",
                    declarationId, fileData.fileName));

            // 查询申报信息，用于设置 relation_name
            var declaration = declarationRepo.findById(declarationId);
            if (declaration == null) {
                throw new BusinessException(ResultCode.DECLARATION_NOT_FOUND,
                        "申报不存在: " + declarationId);
            }

            // 构建上传请求
            var uploadReq = new FileUploadReq();
            uploadReq.setFile(new ByteArrayMultipartFile(fileData.fileName, fileData.content, fileData.contentType));
            uploadReq.setRelationType(AttachmentRelationStatus.DECLARATION.getCode());
            uploadReq.setRelationId(declarationId);
            uploadReq.setRelationName(buildDeclarationRelationName(declaration));
            uploadReq.setAttachmentType("document");
            uploadReq.setIsPublic(0);

            // 上传文件
            var fileInfo = fileService.upload(uploadReq);

            log.info(String.format("文件上传成功: declarationId=%s, attachmentId=%s",
                    declarationId, fileInfo.id()));

            return fileInfo.id();

        } catch (Exception e) {
            log.error(String.format("文件上传失败: declarationId=%s, err=%s",
                    declarationId, e.getMessage()), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 创建附件关联
     */
    private void createAttachmentRelation(Long declarationId, Long attachmentId, Long userId) {
        // 查询申报信息，用于设置 relation_name
        var declaration = declarationRepo.findById(declarationId);
        if (declaration == null) {
            throw new BusinessException(ResultCode.DECLARATION_NOT_FOUND,
                    "申报不存在: " + declarationId);
        }

        // 构建 relation_name：申报编号/研究课题（类似 user 的 "admin/系统管理员" 格式）
        var relationName = buildDeclarationRelationName(declaration);

        var relation = new SysAttachmentRelation();
        relation.setAttachmentId(attachmentId);
        relation.setRelationType(AttachmentRelationStatus.DECLARATION.getCode());
        relation.setRelationId(declarationId);
        relation.setRelationName(relationName);
        relation.setAttachmentType("document");
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
     * 更新申报状态
     */
    private void updateDeclarationStatus(Long declarationId, DeclarationStatus status) {
        try {
            var success = declarationRepo.updateStatus(declarationId, String.valueOf(status.getCode()));
            if (success) {
                log.info(String.format("更新申报状态成功: declarationId=%s, status=%s", declarationId, status.getCode()));
            } else {
                log.warn(String.format("更新申报状态失败: declarationId=%s, status=%s", declarationId, status.getCode()));
            }
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
