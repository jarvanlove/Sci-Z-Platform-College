package com.sciz.server.application.service.declaration.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.declaration.DeclarationService;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.application.task.DeclarationWorkflowTask;
import com.sciz.server.domain.pojo.dto.request.file.FileSyncDifyReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationListQueryReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationUpdateStatusReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationDetailResp;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationListResp;
import com.sciz.server.domain.pojo.dto.response.declaration.RedHeaderFileParseResp;
import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectMemberRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.enums.DeclarationStatus;
import com.sciz.server.infrastructure.shared.enums.WorkflowStatus;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationCreatedEvent;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationSuccessEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.DataPermissionUtil;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import com.sciz.server.infrastructure.shared.utils.DeclarationUtil;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.infrastructure.shared.utils.OperationLogRecorderUtil;
import com.sciz.server.infrastructure.shared.enums.OperationLogRecorderStatus;
import com.sciz.server.interfaces.converter.DeclarationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 申报应用服务实现类
 * 
 * @author JiaWen.Wu
 * @className DeclarationServiceImpl
 * @date 2025-01-20 15:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeclarationServiceImpl implements DeclarationService {

    private final DeclarationRepo declarationRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectRepo projectRepo;
    private final SysUserRepo sysUserRepo;
    private final DeclarationConverter declarationConverter;
    private final EventPublisher eventPublisher;
    private final OperationLogRecorderUtil operationLogRecorderUtil;
    private final DeclarationWorkflowTask declarationWorkflowTask;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final FileService fileService;
    private final DifyWorkflowService difyWorkflowService;

    /**
     * 创建申报
     *
     * @param req 创建请求
     * @return 申报ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeclarationCreateReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.DECLARATION_CREATE;
        var operation = operationType.getCode();

        try {
            log.info(String.format("开始创建申报: researchTopic=%s, workflowId=%s",
                    req.researchTopic(), req.workflowId()));

            // 1. 获取当前登录用户
            var currentUser = LoginUserUtil.requireCurrentUser();
            var userId = currentUser.userId();
            var realName = currentUser.realName();

            // 2. 转换为实体
            var entity = declarationConverter.toEntity(req);

            // 2.1 项目负责人：若传 projectLeaderId 则据此解析姓名；否则用 projectLeader 姓名；至少其一非空
            if (req.projectLeaderId() != null) {
                entity.setProjectLeaderId(req.projectLeaderId());
                var leader = sysUserRepo.findById(req.projectLeaderId());
                if (leader != null) {
                    entity.setProjectLeader(leader.getRealName());
                }
            }
            if (entity.getProjectLeader() == null || entity.getProjectLeader().isBlank()) {
                if (req.projectLeader() != null && !req.projectLeader().isBlank()) {
                    entity.setProjectLeader(req.projectLeader());
                } else {
                    throw BusinessException.of(ResultCode.BAD_REQUEST, "项目负责人不能为空");
                }
            }

            // 3. 设置申报基本信息
            initializeDeclarationEntity(entity, userId, realName);

            // 4. 初始化工作流结果（记录申报提交步骤）
            var workflowResult = initializeWorkflowResult();
            entity.setWorkflowResult(JsonUtil.toJson(workflowResult));

            // 5. 保存申报
            var declarationId = declarationRepo.save(entity);
            if (declarationId == null) {
                throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "申报保存失败");
            }

            log.info(String.format("申报保存成功: declarationId=%s, number=%s", declarationId, entity.getNumber()));

            // 6. 发布申报创建事件
            var event = new DeclarationCreatedEvent(
                    String.valueOf(declarationId),
                    req.researchTopic(),
                    String.valueOf(userId),
                    realName,
                    String.valueOf(DeclarationStatus.IN_PROGRESS.getCode()),
                    String.format("申报编号: %s", entity.getNumber()));
            eventPublisher.publish(event);

            // 7. 异步触发工作流处理
            triggerWorkflowAsync(declarationId, req);

            // 8. 记录操作日志
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（申报编号: %s）",
                    operationType.getDescription(), req.researchTopic(), entity.getNumber());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("申报创建成功: declarationId=%s", declarationId));
            return declarationId;

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：研究课题 %s", operation, req.researchTopic()),
                    e.getMessage(), executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：研究课题 %s", operation, req.researchTopic()),
                    e.getClass().getSimpleName(), executionTime);
            log.error(String.format("申报创建失败: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "申报创建失败: %s", e.getMessage());
        }
    }

    /**
     * 分页查询申报列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<DeclarationListResp> page(DeclarationListQueryReq req) {
        var baseQuery = req.toBaseQuery();
        var page = new Page<Declaration>(baseQuery.pageNo(), baseQuery.pageSize());
        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("submitTime");

        // 项目成员/负责人可见：普通用户可见自己创建的、所属项目关联的、或作为项目负责人关联的申报
        List<Long> includeDeclarationIdsForMember = null;
        Long userId = DataPermissionUtil.getDataPermissionFilter();
        if (userId != null) {
            var memberProjectIds = projectMemberRepo.findProjectIdsByUserId(userId);
            var managerProjectIds = projectRepo.findProjectIdsByManagerId(userId);
            var mergedProjectIds = new ArrayList<Long>(memberProjectIds);
            for (Long pid : managerProjectIds) {
                if (!mergedProjectIds.contains(pid)) mergedProjectIds.add(pid);
            }
            includeDeclarationIdsForMember = projectRepo.findDeclarationIdsByProjectIds(mergedProjectIds);
        }

        IPage<Declaration> declarationPage = declarationRepo.page(page, req.keyword(), req.status(), sortBy, asc,
                includeDeclarationIdsForMember);

        // 批量查询附件信息（用于判断是否有附件和获取附件ID）
        var declarationIds = declarationPage.getRecords().stream()
                .map(Declaration::getId)
                .toList();

        var attachmentMap = new HashMap<Long, Boolean>();
        var attachmentIdMap = new HashMap<Long, Long>();
        if (!declarationIds.isEmpty()) {
            declarationIds.forEach(declarationId -> {
                var attachmentIds = sysAttachmentRelationRepo.findAttachmentIds(
                        AttachmentRelationStatus.DECLARATION.getCode(), declarationId);
                attachmentMap.put(declarationId, !attachmentIds.isEmpty());
                // 如果有附件，取第一个附件ID
                if (!attachmentIds.isEmpty()) {
                    attachmentIdMap.put(declarationId, attachmentIds.get(0));
                }
            });
        }

        var records = declarationPage.getRecords().stream()
                .map(declaration -> {
                    var resp = declarationConverter.toListResp(declaration);

                    // 设置状态描述
                    if (declaration.getStatus() != null) {
                        try {
                            var statusCode = Integer.parseInt(declaration.getStatus());
                            var status = DeclarationStatus.fromCode(statusCode);
                            resp.setStatusDescription(status.getDescription());
                            resp.setStatus(statusCode);
                        } catch (Exception e) {
                            log.warn(String.format("解析申报状态失败: declarationId=%s, status=%s",
                                    declaration.getId(), declaration.getStatus()));
                        }
                    }

                    // 设置工作流状态描述
                    if (declaration.getWorkflowStatus() != null) {
                        try {
                            var workflowStatus = WorkflowStatus.fromCode(declaration.getWorkflowStatus());
                            resp.setWorkflowStatusDescription(workflowStatus.getDescription());
                            resp.setWorkflowStatus(workflowStatus.getCode());
                        } catch (Exception e) {
                            log.warn(String.format("解析工作流状态失败: declarationId=%s, workflowStatus=%s",
                                    declaration.getId(), declaration.getWorkflowStatus()));
                        }
                    }

                    // 设置是否有附件和附件ID
                    var hasAttachment = attachmentMap.getOrDefault(declaration.getId(), false);
                    resp.setHasAttachment(hasAttachment);
                    resp.setAttachmentId(attachmentIdMap.get(declaration.getId()));

                    // 设置研究领域
                    if (declaration.getResearchFields() != null && !declaration.getResearchFields().isEmpty()) {
                        try {
                            var researchFields = JsonUtil.fromJsonList(declaration.getResearchFields(), String.class);
                            resp.setResearchFields(researchFields);
                        } catch (Exception e) {
                            log.warn(String.format("解析研究领域失败: declarationId=%s, err=%s",
                                    declaration.getId(), e.getMessage()));
                        }
                    }

                    return resp;
                })
                .toList();

        Page<DeclarationListResp> resultPage = new Page<>(declarationPage.getCurrent(), declarationPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(declarationPage.getTotal());
        return PageResult.of(resultPage);
    }

    /**
     * 获取申报详情
     *
     * @param id 申报ID
     * @return 申报详情
     */
    @Override
    public DeclarationDetailResp findDetail(Long id) {
        log.info(String.format("查询申报详情: declarationId=%s", id));

        // 1. 查询申报实体
        var declaration = declarationRepo.findById(id);
        if (declaration == null) {
            throw BusinessException.of(ResultCode.DECLARATION_NOT_FOUND);
        }

        // 2. 转换为响应对象
        var resp = declarationConverter.toDetailResp(declaration);

        // 3. 设置状态描述
        var statusCode = Integer.parseInt(declaration.getStatus());
        var status = DeclarationStatus.fromCode(statusCode);
        resp.setStatusDescription(status.getDescription());

        // 4. 查询附件信息
        var attachmentIds = sysAttachmentRelationRepo.findAttachmentIds(
                AttachmentRelationStatus.DECLARATION.getCode(), id);
        if (!attachmentIds.isEmpty()) {
            resp.setHasAttachment(true);
            resp.setAttachmentId(attachmentIds.get(0));
            // 生成预览URL
            try {
                var previewUrl = fileService.preview(attachmentIds.get(0), null);
                resp.setAttachmentUrl(previewUrl);
            } catch (Exception e) {
                log.warn(String.format("生成附件预览URL失败: attachmentId=%s, err=%s",
                        attachmentIds.get(0), e.getMessage()));
            }
        } else {
            resp.setHasAttachment(false);
        }

        // 5. 解析工作流结果
        if (declaration.getWorkflowResult() != null && !declaration.getWorkflowResult().isEmpty()) {
            var workflowResultMap = JsonUtil.fromJsonToMap(declaration.getWorkflowResult());
            if (workflowResultMap != null) {
                var workflowResult = new DeclarationDetailResp.WorkflowResult();

                // 解析步骤列表
                @SuppressWarnings("unchecked")
                var stepsList = (List<Map<String, Object>>) workflowResultMap.get("steps");
                if (stepsList != null) {
                    var steps = stepsList.stream()
                            .map(stepMap -> {
                                var step = new DeclarationDetailResp.WorkflowStep();
                                step.setName((String) stepMap.get("name"));
                                step.setStatus((String) stepMap.get("status"));
                                step.setTimestamp((String) stepMap.get("timestamp"));
                                return step;
                            })
                            .toList();
                    workflowResult.setSteps(steps);
                }

                // 解析文件URL和格式
                workflowResult.setFileUrl((String) workflowResultMap.get("fileUrl"));
                workflowResult.setFileFormat((String) workflowResultMap.get("fileFormat"));

                resp.setWorkflowResult(workflowResult);
            }
        }

        log.info(String.format("查询申报详情成功: declarationId=%s", id));
        return resp;
    }

    /**
     * 获取工作流状态
     *
     * @param id 申报ID
     * @return 工作流状态信息
     */
    @Override
    public DeclarationDetailResp.WorkflowResult getWorkflowStatus(Long id) {
        log.info(String.format("查询工作流状态: declarationId=%s", id));

        // 1. 查询申报实体
        var declaration = declarationRepo.findById(id);
        if (declaration == null) {
            throw BusinessException.of(ResultCode.DECLARATION_NOT_FOUND);
        }

        // 2. 解析工作流结果
        var workflowResult = new DeclarationDetailResp.WorkflowResult();

        // 设置工作流状态
        workflowResult.setWorkflowStatus(declaration.getWorkflowStatus());

        if (declaration.getWorkflowResult() != null && !declaration.getWorkflowResult().isEmpty()) {
            var workflowResultMap = JsonUtil.fromJsonToMap(declaration.getWorkflowResult());
            if (workflowResultMap != null) {
                // 解析步骤列表
                @SuppressWarnings("unchecked")
                var stepsList = (List<Map<String, Object>>) workflowResultMap.get("steps");
                if (stepsList != null) {
                    var steps = stepsList.stream()
                            .map(stepMap -> {
                                var step = new DeclarationDetailResp.WorkflowStep();
                                step.setName((String) stepMap.get("name"));
                                step.setStatus((String) stepMap.get("status"));
                                step.setTimestamp((String) stepMap.get("timestamp"));
                                return step;
                            })
                            .toList();
                    workflowResult.setSteps(steps);
                }

                // 解析文件URL和格式
                workflowResult.setFileUrl((String) workflowResultMap.get("fileUrl"));
                workflowResult.setFileFormat((String) workflowResultMap.get("fileFormat"));
            }
        }

        log.info(String.format("查询工作流状态成功: declarationId=%s, workflowStatus=%s",
                id, declaration.getWorkflowStatus()));
        return workflowResult;
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化工作流结果
     *
     * @return 工作流结果
     */
    private Map<String, Object> initializeWorkflowResult() {
        var result = new HashMap<String, Object>();
        var steps = List.of(
                Map.of("name", "申报提交", "status", "success",
                        "timestamp", DateUtil.formatDateTime(LocalDateTime.now())));
        result.put("steps", steps);
        return result;
    }

    /**
     * 异步触发工作流处理
     *
     * @param declarationId 申报ID
     * @param req           创建请求
     */
    public void triggerWorkflowAsync(Long declarationId, DeclarationCreateReq req) {
        var currentUser = LoginUserUtil.requireCurrentUser();
        var userId = currentUser.userId();

        // 构建类型安全的工作流输入参数
        var inputs = DeclarationWorkflowReq.of(
                req.researchFields(), req.researchDirection(), req.researchTopic());

        // 使用 workflowId 作为 resourceId 调用工作流
        String resourceId = req.workflowId();
        String keyType = DifyApiKey.KeyType.WORKFLOW.getCode();

        // ==================== 本地调试模式 ====================
        // 方案一：临时改为同步执行（推荐用于本地debug）
        // 取消下面的注释，注释掉异步调用，即可同步调试
        // declarationWorkflowTask.processDeclarationWorkflowSync(declarationId,
        // resourceId, inputs, userId, keyType);
        // log.info(String.format("同步触发工作流处理（调试模式）: declarationId=%s, workflowId=%s,
        // resourceId=%s",
        // declarationId, req.workflowId(), resourceId));
        // return; // 调试模式下直接返回，避免继续执行后续代码

        // ==================== 生产模式 ====================
        // 调用异步任务处理类（传递类型安全的 inputs 对象）
        declarationWorkflowTask.processDeclarationWorkflow(declarationId, resourceId,
                inputs, userId, keyType);
        log.info(String.format("异步触发工作流处理: declarationId=%s, workflowId=%s,resourceId=%s",
                declarationId, req.workflowId(), resourceId));
    }

    /**
     * 初始化申报实体基本信息
     *
     * @param entity   申报实体
     * @param userId   用户ID
     * @param realName 用户真实姓名
     */
    private void initializeDeclarationEntity(Declaration entity, Long userId, String realName) {
        var now = LocalDateTime.now();
        entity.setNumber(DeclarationUtil.generateDeclarationNumber());
        entity.setApplicantId(userId);
        entity.setApplicantName(realName);
        entity.setStatus(String.valueOf(DeclarationStatus.IN_PROGRESS.getCode()));
        entity.setWorkflowStatus(WorkflowStatus.RUNNING.getCode());
        entity.setSubmitTime(now);
        // 研究内容摘要（初始为空，后续由工作流生成）
        if (entity.getContentSummary() == null) {
            entity.setContentSummary("");
        }
        entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
    }

    /**
     * 上传红头文件
     *
     * @param req 文件上传请求
     * @return 红头文件解析响应（包含研究领域、研究方向、研究课题）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RedHeaderFileParseResp uploadRedHeaderFile(FileUploadReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.DECLARATION_UPLOAD_RED_HEADER_FILE;
        var operation = operationType.getCode();

        try {
            var fileName = Optional.ofNullable(req.getFile())
                    .map(file -> Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"))
                    .orElse("unknown");
            log.info(String.format("开始上传红头文件: fileName=%s", fileName));

            // 1. 获取当前登录用户
            var currentUser = LoginUserUtil.requireCurrentUser();
            Long userId = currentUser.userId();

            // 2. 准备 Dify 同步参数
            String resourceId = "workflow_002";
            String keyType = DifyApiKey.KeyType.WORKFLOW.getCode();

            // 3. 构建同步请求
            var syncReq = new FileSyncDifyReq(req.getFile(), resourceId, keyType);

            // 4. 同步上传文件到 Dify
            log.info(String.format("开始同步文件到 Dify: fileName=%s, resourceId=%s, keyType=%s",
                    fileName, resourceId, keyType));
            var syncResp = difyWorkflowService.syncFileToDify(syncReq);
            String difyFileId = syncResp.difyFileId();

            log.info(String.format("文件已上传到 Dify: difyFileId=%s", difyFileId));

            // 5. 调用红头文件解析工作流
            log.info(String.format("开始调用红头文件解析工作流: difyFileId=%s, workflowId=%s", difyFileId, resourceId));
            RedHeaderFileParseResp resp = difyWorkflowService.executeRedHeaderFileWorkflow(
                    difyFileId, userId, resourceId, keyType);

            // 6. 记录操作日志
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（Dify文件ID: %s）",
                    operationType.getDescription(), fileName, difyFileId);
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("红头文件上传完成: fileName=%s, difyFileId=%s", fileName, difyFileId));
            return resp;

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var fileName = Optional.ofNullable(req.getFile())
                    .map(file -> Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"))
                    .orElse("unknown");
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：文件名 %s", operation, fileName),
                    e.getMessage(), executionTime);
            log.error(String.format("红头文件上传失败: err=%s", e.getMessage()), e);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var fileName = Optional.ofNullable(req.getFile())
                    .map(file -> Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"))
                    .orElse("unknown");
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：文件名 %s", operation, fileName),
                    e.getClass().getSimpleName(), executionTime);
            log.error(String.format("红头文件上传异常: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "红头文件上传失败: %s", e.getMessage());
        }
    }

    /**
     * 更新申报状态
     *
     * @param req 更新状态请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(DeclarationUpdateStatusReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.DECLARATION_UPDATE;
        var operation = operationType.getCode();

        try {
            log.info(String.format("开始更新申报状态: declarationId=%s, status=%s", req.id(), req.status()));

            // 1. 查询申报实体
            var declaration = declarationRepo.findById(req.id());
            if (declaration == null) {
                throw BusinessException.of(ResultCode.DECLARATION_NOT_FOUND);
            }

            // 2. 解析申报状态
            var newStatus = DeclarationStatus.fromCode(req.status());
            var oldStatus = DeclarationStatus.fromCode(Integer.parseInt(declaration.getStatus()));

            // 3. 更新申报状态
            var success = declarationRepo.updateStatus(req.id(), String.valueOf(newStatus.getCode()));
            if (!success) {
                throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "申报状态更新失败");
            }

            // 4. 如果更新为"申报成功"，发布异步事件创建项目和知识库
            if (newStatus.isSuccess()) {
                log.info(String.format("申报状态更新为成功，发布异步事件创建项目和知识库: declarationId=%s", req.id()));
                // 获取当前操作人ID（在Web上下文中获取，传递给异步事件）
                var operatorId = LoginUserUtil.requireCurrentUserId();
                var successEvent = new DeclarationSuccessEvent(
                        declaration.getId(),
                        declaration.getNumber(),
                        declaration.getResearchTopic(),
                        declaration.getApplicantId(),
                        declaration.getApplicantName(),
                        operatorId);
                eventPublisher.publish(successEvent);
            }

            // 5. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：申报编号 %s（ID: %s），状态从 %s 更新为 %s",
                    operationType.getDescription(), declaration.getNumber(), req.id(),
                    oldStatus.getDescription(), newStatus.getDescription());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("更新申报状态成功: declarationId=%s, oldStatus=%s, newStatus=%s",
                    req.id(), oldStatus.getDescription(), newStatus.getDescription()));

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：申报ID %s", operation, req.id()),
                    e.getMessage(), executionTime);
            log.error(String.format("更新申报状态失败: declarationId=%s, err=%s", req.id(), e.getMessage()), e);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：申报ID %s", operation, req.id()),
                    e.getClass().getSimpleName(), executionTime);
            log.error(String.format("更新申报状态异常: declarationId=%s, err=%s", req.id(), e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "更新申报状态失败: %s", e.getMessage());
        }
    }

}
