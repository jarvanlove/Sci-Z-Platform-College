package com.sciz.server.application.service.declaration.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.declaration.DeclarationService;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.application.task.DeclarationWorkflowTask;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationListQueryReq;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationDetailResp;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationListResp;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.enums.DeclarationStatus;
import com.sciz.server.infrastructure.shared.enums.WorkflowStatus;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationCreatedEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
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
    private final DeclarationConverter declarationConverter;
    private final EventPublisher eventPublisher;
    private final OperationLogRecorderUtil operationLogRecorderUtil;
    private final DeclarationWorkflowTask declarationWorkflowTask;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final FileService fileService;

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

            // 3. 设置申报基本信息
            initializeDeclarationEntity(entity, userId, realName);

            // 4. 初始化工作流结果（记录申报提交步骤）
            var workflowResult = initializeWorkflowResult();
            entity.setWorkflowResult(JsonUtil.toJson(workflowResult));

            // 5. 保存申报
            var declarationId = declarationRepo.save(entity);
            if (declarationId == null) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "申报保存失败");
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
            throw new BusinessException(ResultCode.SERVER_ERROR, "申报创建失败: " + e.getMessage());
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

        IPage<Declaration> declarationPage = declarationRepo.page(page, req.keyword(), req.status(), sortBy, asc);

        // 批量查询附件信息（用于判断是否有附件）
        var declarationIds = declarationPage.getRecords().stream()
                .map(Declaration::getId)
                .toList();

        var attachmentMap = new HashMap<Long, Boolean>();
        if (!declarationIds.isEmpty()) {
            declarationIds.forEach(declarationId -> {
                var attachmentIds = sysAttachmentRelationRepo.findAttachmentIds(
                        AttachmentRelationStatus.DECLARATION.getCode(), declarationId);
                attachmentMap.put(declarationId, !attachmentIds.isEmpty());
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

                    // 设置是否有附件
                    resp.setHasAttachment(attachmentMap.getOrDefault(declaration.getId(), false));

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
            throw new BusinessException(ResultCode.DECLARATION_NOT_FOUND);
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
            throw new BusinessException(ResultCode.DECLARATION_NOT_FOUND);
        }

        // 2. 解析工作流结果
        var workflowResult = new DeclarationDetailResp.WorkflowResult();

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

        // 构建工作流输入参数
        var inputs = buildWorkflowInputs(req);

        // 调用异步任务处理类
        declarationWorkflowTask.processDeclarationWorkflow(declarationId, req.workflowId(), inputs, userId);

        log.info(String.format("异步触发工作流处理: declarationId=%s, workflowId=%s",
                declarationId, req.workflowId()));
    }

    /**
     * 初始化申报实体基本信息
     *
     * @param entity   申报实体
     * @param userId   用户ID
     * @param realName 用户真实姓名
     */
    private void initializeDeclarationEntity(com.sciz.server.domain.pojo.entity.declaration.Declaration entity,
            Long userId, String realName) {
        var now = LocalDateTime.now();
        entity.setNumber(DeclarationUtil.generateDeclarationNumber());
        entity.setApplicantId(userId);
        entity.setApplicantName(realName);
        entity.setStatus(String.valueOf(DeclarationStatus.IN_PROGRESS.getCode()));
        entity.setWorkflowStatus(WorkflowStatus.RUNNING.getCode());
        entity.setSubmitTime(now);
        entity.setIsDeleted(0);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
    }

    /**
     * 构建工作流输入参数
     *
     * @param req 创建请求
     * @return 工作流输入参数
     */
    private Map<String, Object> buildWorkflowInputs(DeclarationCreateReq req) {
        var inputs = new HashMap<String, Object>();
        inputs.put("department", req.department());
        inputs.put("projectLeader", req.projectLeader());
        inputs.put("documentPublishTime", req.documentPublishTime().toString());
        inputs.put("projectStartTime", req.projectStartTime().toString());
        inputs.put("projectEndTime", req.projectEndTime().toString());
        inputs.put("researchFields", req.researchFields());
        inputs.put("researchDirection", req.researchDirection());
        inputs.put("researchTopic", req.researchTopic());
        return inputs;
    }
}
