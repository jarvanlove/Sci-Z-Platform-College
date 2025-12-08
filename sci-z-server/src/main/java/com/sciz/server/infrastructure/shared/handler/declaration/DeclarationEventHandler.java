package com.sciz.server.infrastructure.shared.handler.declaration;

import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationCreatedEvent;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationSuccessEvent;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationUpdatedEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.context.AsyncUserContext;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import com.sciz.server.infrastructure.shared.utils.OperationLogRecorderUtil;
import com.sciz.server.infrastructure.shared.enums.OperationLogRecorderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 申报事件处理器
 * 处理申报相关的领域事件
 *
 * @author JiaWen.Wu
 * @className DeclarationEventHandler
 * @date 2025-10-29 11:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeclarationEventHandler {

    private final ProjectService projectService;
    private final KnowledgeService knowledgeService;
    private final OperationLogRecorderUtil operationLogRecorderUtil;

    /**
     * 处理申报创建事件
     *
     * @param event 申报创建事件
     */
    @EventListener
    @Async
    public void handleDeclarationCreated(DeclarationCreatedEvent event) {
        try {
            log.info("处理申报创建事件: declarationId={}, declarationName={}, applicantId={}",
                    event.getDeclarationId(), event.getDeclarationName(), event.getApplicantId());

            // 1. 发送申报创建通知
            sendDeclarationCreatedNotification(event);

            // 2. 初始化申报流程
            initializeDeclarationProcess(event);

            // 3. 记录申报创建审计日志
            logDeclarationCreation(event);

            // 4. 更新申报统计信息
            updateDeclarationStatistics(event);

            log.info("申报创建事件处理完成: declarationId={}", event.getDeclarationId());

        } catch (Exception e) {
            log.error("处理申报创建事件失败: declarationId={}", event.getDeclarationId(), e);
        }
    }

    /**
     * 处理申报更新事件
     *
     * @param event 申报更新事件
     */
    @EventListener
    @Async
    public void handleDeclarationUpdated(DeclarationUpdatedEvent event) {
        try {
            log.info("处理申报更新事件: declarationId={}, oldStatus={}, newStatus={}",
                    event.getDeclarationId(), event.getOldStatus(), event.getNewStatus());

            // 1. 发送申报状态变更通知
            sendDeclarationStatusChangeNotification(event);

            // 2. 更新申报流程状态
            updateDeclarationProcessStatus(event);

            // 3. 记录申报更新审计日志
            logDeclarationUpdate(event);

            // 4. 触发后续流程
            triggerFollowUpProcess(event);

            log.info("申报更新事件处理完成: declarationId={}", event.getDeclarationId());

        } catch (Exception e) {
            log.error("处理申报更新事件失败: declarationId={}", event.getDeclarationId(), e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 发送申报创建通知
     * <p>
     * TODO: 根据实际需求实现通知逻辑
     * 可能的实现方式：
     * 1. 发送邮件通知给申报人
     * 2. 发送站内消息通知
     * 3. 发送短信通知（可选）
     */
    private void sendDeclarationCreatedNotification(DeclarationCreatedEvent event) {
        log.info("发送申报创建通知: declarationId={}, applicantId={}, applicantName={}",
                event.getDeclarationId(), event.getApplicantId(), event.getApplicantName());
        // TODO: 实现发送通知的逻辑
        // 示例：邮件服务、站内信服务等
    }

    /**
     * 初始化申报流程
     * <p>
     * 注意：申报流程初始化已在 Service 层完成（保存申报、设置状态等）
     * 工作流处理由 DeclarationWorkflowTask 异步执行
     * 此方法主要用于未来可能的扩展，如：初始化关联数据、预加载资源等
     */
    private void initializeDeclarationProcess(DeclarationCreatedEvent event) {
        log.info("初始化申报流程: declarationId={}", event.getDeclarationId());
        // 申报流程初始化已在 Service 层完成
        // 工作流处理由 DeclarationWorkflowTask 异步执行
        // TODO: 如需扩展，可在此处添加额外的初始化逻辑
    }

    /**
     * 记录申报创建审计日志
     * <p>
     * TODO: 根据实际需求实现审计日志记录逻辑
     * 可能的实现方式：
     * 1. 写入专门的审计日志表
     * 2. 发送到日志系统（如 ELK、Splunk 等）
     * 3. 记录到操作日志表（sys_operation_log）
     * <p>
     * 注意：操作日志已由 OperationLogRecorderUtil 记录，此方法用于额外的审计需求
     */
    private void logDeclarationCreation(DeclarationCreatedEvent event) {
        log.info("记录申报创建审计日志: declarationId={}, declarationName={}, applicantId={}, applicantName={}",
                event.getDeclarationId(), event.getDeclarationName(), event.getApplicantId(), event.getApplicantName());
        // TODO: 实现记录审计日志的逻辑
        // 注意：操作日志已由 OperationLogRecorderUtil 记录，此方法用于额外的审计需求
    }

    /**
     * 更新申报统计信息
     * <p>
     * TODO: 根据实际需求实现统计信息更新逻辑
     * 可能的实现方式：
     * 1. 更新申报总数统计
     * 2. 更新按申报类型、状态、部门的统计
     * 3. 更新用户申报数量统计
     * 4. 更新缓存中的统计信息
     */
    private void updateDeclarationStatistics(DeclarationCreatedEvent event) {
        log.info("更新申报统计信息: declarationId={}, applicantId={}",
                event.getDeclarationId(), event.getApplicantId());
        // TODO: 实现更新统计信息的逻辑
        // 示例：更新统计表、更新缓存等
    }

    /**
     * 发送申报状态变更通知
     * <p>
     * 使用事件中的 description 和 updateReason 字段构建通知内容
     * 可能的实现方式：
     * 1. 当申报状态变为"申报成功"时，发送成功通知给申报人
     * 2. 当申报状态变为"申报失败"时，发送失败通知并说明原因
     * 3. 发送邮件、站内消息或短信通知
     */
    private void sendDeclarationStatusChangeNotification(DeclarationUpdatedEvent event) {
        log.info(
                "发送申报状态变更通知: declarationId={}, declarationName={}, oldStatus={}, newStatus={}, applicantId={}, applicantName={}, description={}, updateReason={}",
                event.getDeclarationId(), event.getDeclarationName(), event.getOldStatus(), event.getNewStatus(),
                event.getApplicantId(), event.getApplicantName(), event.getDescription(), event.getUpdateReason());
        // TODO: 实现发送通知的逻辑
        // 可以使用 event.getDescription() 和 event.getUpdateReason() 构建通知内容
        // 示例：根据状态变更类型发送不同的通知
    }

    /**
     * 更新申报流程状态
     * <p>
     * 注意：申报状态更新已在 Service 层完成
     * 此方法主要用于未来可能的扩展，如：同步更新关联数据、触发其他流程等
     */
    private void updateDeclarationProcessStatus(DeclarationUpdatedEvent event) {
        log.info("更新申报流程状态: declarationId={}, oldStatus={}, newStatus={}",
                event.getDeclarationId(), event.getOldStatus(), event.getNewStatus());
        // 申报状态更新已在 Service 层完成
        // TODO: 如需扩展，可在此处添加额外的流程状态更新逻辑
    }

    /**
     * 记录申报更新审计日志
     * <p>
     * 使用事件中的 description 和 updateReason 字段记录详细的更新信息
     * 可能的实现方式：
     * 1. 写入专门的审计日志表
     * 2. 发送到日志系统（如 ELK、Splunk 等）
     * 3. 记录状态变更历史
     * <p>
     * 注意：操作日志已由 OperationLogRecorderUtil 记录，此方法用于额外的审计需求
     */
    private void logDeclarationUpdate(DeclarationUpdatedEvent event) {
        log.info(
                "记录申报更新审计日志: declarationId={}, declarationName={}, oldStatus={}, newStatus={}, description={}, updateReason={}",
                event.getDeclarationId(), event.getDeclarationName(), event.getOldStatus(), event.getNewStatus(),
                event.getDescription(), event.getUpdateReason());
        // TODO: 实现记录审计日志的逻辑
        // 注意：操作日志已由 OperationLogRecorderUtil 记录，此方法用于额外的审计需求
        // 可以使用 event.getDescription() 和 event.getUpdateReason() 记录更详细的更新信息
    }

    /**
     * 触发后续流程
     * <p>
     * 使用事件中的 description 和 updateReason 字段判断是否需要触发后续流程
     * 可能的实现方式：
     * 1. 当申报状态变为"申报成功"时，自动创建项目记录
     * 2. 触发审批流程（如果需要）
     * 3. 生成相关文档或报告
     * 4. 同步到其他系统
     */
    private void triggerFollowUpProcess(DeclarationUpdatedEvent event) {
        log.info(
                "触发后续流程: declarationId={}, declarationName={}, oldStatus={}, newStatus={}, description={}, updateReason={}",
                event.getDeclarationId(), event.getDeclarationName(), event.getOldStatus(), event.getNewStatus(),
                event.getDescription(), event.getUpdateReason());
        // TODO: 实现触发后续流程的逻辑
        // 可以使用 event.getDescription() 和 event.getUpdateReason() 判断是否需要触发后续流程
        // 示例：当状态为"申报成功"时，创建项目记录
        // if (DeclarationStatus.SUCCESS.getCode().equals(event.getNewStatus())) {
        // // 创建项目记录
        // }
    }

    /**
     * 处理申报成功事件
     * <p>
     * 当申报状态更新为"申报成功"时，异步创建项目和知识库
     * <p>
     * 注意：使用事务确保原子性，如果任何步骤失败，整个流程回滚
     *
     * @param event 申报成功事件
     */
    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleDeclarationSuccess(DeclarationSuccessEvent event) {
        // 设置异步用户上下文，使 LoginUserUtil 在异步线程中也能正常工作
        var operatorId = event.getOperatorId() != null ? event.getOperatorId() : event.getApplicantId();
        try {
            AsyncUserContext.set(operatorId, event.getApplicantName(), event.getApplicantName());

            var startTime = DateUtil.now();
            var operationType = OperationLogRecorderStatus.DECLARATION_UPDATE;
            var operation = operationType.getCode();

            log.info(String.format("处理申报成功事件: declarationId=%s, researchTopic=%s, applicantId=%s",
                    event.getDeclarationId(), event.getResearchTopic(), event.getApplicantId()));

            // 1. 验证研究课题是否存在
            var researchTopic = event.getResearchTopic();
            if (researchTopic == null || researchTopic.trim().isEmpty()) {
                log.error(String.format("研究课题为空，无法创建项目和知识库: declarationId=%s", event.getDeclarationId()));
                recordFailureLog(operation, event.getDeclarationId(), "研究课题不能为空", startTime, event);
                throw BusinessException.of(ResultCode.BAD_REQUEST, "研究课题不能为空");
            }

            // 2. 创建项目（内部会记录操作日志，现在可以正常使用 LoginUserUtil）
            var projectId = createProject(event, researchTopic);

            // 3. 创建知识库（内部会记录操作日志，现在可以正常使用 LoginUserUtil）
            createKnowledgeBase(event, researchTopic, projectId);

            // 4. 记录整体流程操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：申报编号 %s（ID: %s），已创建项目和知识库",
                    operationType.getDescription(), event.getDeclarationNumber(), event.getDeclarationId());
            // 现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("申报成功事件处理完成: declarationId=%s, projectId=%s",
                    event.getDeclarationId(), projectId));

        } catch (Exception e) {
            log.error(String.format("处理申报成功事件失败: declarationId=%s, err=%s",
                    event.getDeclarationId(), e.getMessage()), e);
            var startTime = DateUtil.now();
            var operationType = OperationLogRecorderStatus.DECLARATION_UPDATE;
            var operation = operationType.getCode();
            recordFailureLog(operation, event.getDeclarationId(), e.getMessage(), startTime, event);
            // 抛出异常，触发事务回滚
            throw e;
        } finally {
            // 清理异步用户上下文（防止内存泄漏）
            AsyncUserContext.clear();
        }
    }

    /**
     * 创建项目
     *
     * @param event         申报成功事件
     * @param researchTopic 研究课题
     * @return 项目ID
     */
    private Long createProject(DeclarationSuccessEvent event, String researchTopic) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_CREATE;
        var operation = operationType.getCode();

        try {
            log.info(String.format("开始创建项目: researchTopic=%s, declarationId=%s",
                    researchTopic, event.getDeclarationId()));

            var projectCreateReq = new ProjectCreateReq(
                    researchTopic, // 项目名称 = 研究课题
                    researchTopic, // 项目描述 = 研究课题
                    event.getDeclarationId(), // 关联申报ID
                    null, // 预算（可选）
                    null, // 进度（可选）
                    String.valueOf(ProjectStatus.IN_PROGRESS.getCode()), // 状态 = 进行中
                    null // Dify知识库ID（创建知识库后更新）
            );

            // 注意：项目编号会在 ProjectServiceImpl.initializeProjectEntity 中自动生成（PRJ+时间戳）
            // 使用事件中的操作人ID创建项目，AsyncUserContext 已设置，可以直接使用 create 方法
            var operatorId = event.getOperatorId();
            if (operatorId == null) {
                // 如果没有操作人ID，使用申报人ID作为后备方案
                operatorId = event.getApplicantId();
                log.warn(String.format("事件中缺少操作人ID，使用申报人ID作为后备: applicantId=%s", operatorId));
            }
            // 现在可以正常使用 create 方法，LoginUserUtil 会从 AsyncUserContext 获取用户信息
            var projectId = projectService.create(projectCreateReq);
            log.info(String.format("项目创建成功: projectId=%s, name=%s, operatorId=%s", projectId, researchTopic,
                    operatorId));

            // 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), researchTopic, projectId);
            // 现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            return projectId;

        } catch (Exception e) {
            log.error(String.format("创建项目失败: declarationId=%s, err=%s",
                    event.getDeclarationId(), e.getMessage()), e);
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e instanceof BusinessException ? e.getMessage() : e.getClass().getSimpleName();
            // 现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：研究课题 %s", operation, researchTopic),
                    errorMessage, executionTime);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "创建项目失败: %s", e.getMessage());
        }
    }

    /**
     * 创建知识库
     *
     * @param event         申报成功事件
     * @param researchTopic 研究课题
     * @param projectId     项目ID
     */
    private void createKnowledgeBase(DeclarationSuccessEvent event, String researchTopic, Long projectId) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.KNOWLEDGE_CREATE;
        var operation = operationType.getCode();

        try {
            log.info(String.format("开始创建知识库: name=%s, description=%s, projectId=%s",
                    researchTopic, researchTopic, projectId));

            var knowledgeCreateReq = new KnowledgeCreateReq();
            knowledgeCreateReq.setUserId(event.getOperatorId()); // 用户ID
            knowledgeCreateReq.setName(researchTopic); // 知识库名称 = 研究课题
            knowledgeCreateReq.setDescription(researchTopic); // 知识库描述 = 研究课题
            knowledgeCreateReq.setProjectId(projectId); // 关联项目ID
            // AsyncUserContext 已设置，可以直接使用 create 方法，无需设置 userId

            var knowledgeResp = knowledgeService.create(knowledgeCreateReq);
            log.info(String.format("知识库创建成功: knowledgeId=%s, name=%s, difyKnowdataId=%s",
                    knowledgeResp.getId(), knowledgeResp.getName(), knowledgeResp.getDifyKnowdataId()));

            // 更新项目的 Dify 知识库ID（使用 knowledgeResp.getId()，即本地知识库ID）
            updateProjectDifyKnowledgeId(event, projectId, String.valueOf(knowledgeResp.getId()));

            // 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), researchTopic,
                    knowledgeResp.getId());
            // 现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

        } catch (Exception e) {
            log.error(String.format("创建知识库失败: declarationId=%s, projectId=%s, err=%s",
                    event.getDeclarationId(), projectId, e.getMessage()), e);
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e instanceof BusinessException ? e.getMessage() : e.getClass().getSimpleName();
            // 现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：研究课题 %s，项目ID %s", operation, researchTopic, projectId),
                    errorMessage, executionTime);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "创建知识库失败: %s", e.getMessage());
        }
    }

    /**
     * 更新项目的 Dify 知识库ID
     *
     * @param event           申报成功事件（用于获取操作人ID）
     * @param projectId       项目ID
     * @param difyKnowledgeId Dify知识库ID（本地知识库ID）
     */
    private void updateProjectDifyKnowledgeId(DeclarationSuccessEvent event, Long projectId, String difyKnowledgeId) {
        // 获取操作人ID（用于更新项目，避免在异步线程中获取Web上下文）
        var operatorId = event.getOperatorId();
        if (operatorId == null) {
            // 如果没有操作人ID，使用申报人ID作为后备方案
            operatorId = event.getApplicantId();
            log.warn(String.format("事件中缺少操作人ID，使用申报人ID作为后备: applicantId=%s", operatorId));
        }

        // 设置异步用户上下文，使 LoginUserUtil 在异步线程中也能正常工作
        try {
            AsyncUserContext.set(operatorId, event.getApplicantName(), event.getApplicantName());

            log.info(String.format("开始更新项目的Dify知识库ID: projectId=%s, difyKnowledgeId=%s",
                    projectId, difyKnowledgeId));

            // 查询项目实体
            var project = projectService.findDetail(projectId);
            if (project == null) {
                log.error(String.format("项目不存在，无法更新Dify知识库ID: projectId=%s", projectId));
                return;
            }

            // 构建更新请求（只更新 difyKnowledgeId）
            var updateReq = new ProjectUpdateReq(
                    projectId,
                    null, // manager 不更新
                    null, // startTime 不更新
                    null, // endTime 不更新
                    null, // budget 不更新
                    null, // description 不更新
                    null, // status 不更新
                    difyKnowledgeId, // 只更新 difyKnowledgeId
                    null, // members 不更新
                    null // milestones 不更新
            );

            // 更新项目（现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息）
            projectService.update(updateReq);

            log.info(String.format("项目Dify知识库ID更新成功: projectId=%s, difyKnowledgeId=%s",
                    projectId, difyKnowledgeId));

        } catch (Exception e) {
            log.error(String.format("更新项目Dify知识库ID失败: projectId=%s, difyKnowledgeId=%s, err=%s",
                    projectId, difyKnowledgeId, e.getMessage()), e);
            // 注意：不抛出异常，避免影响主流程
        } finally {
            // 清理异步用户上下文（防止内存泄漏）
            AsyncUserContext.clear();
        }
    }

    /**
     * 记录失败日志
     *
     * @param operation     操作名称
     * @param declarationId 申报ID
     * @param errorMessage  错误信息
     * @param startTime     开始时间
     * @param event         申报成功事件（用于获取用户信息）
     */
    private void recordFailureLog(String operation, Long declarationId, String errorMessage,
            java.time.LocalDateTime startTime, DeclarationSuccessEvent event) {
        try {
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            // 现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：申报ID %s（创建项目和知识库）", operation, declarationId),
                    errorMessage, executionTime);
        } catch (Exception e) {
            log.error(String.format("记录失败日志异常: declarationId=%s, err=%s", declarationId, e.getMessage()), e);
        }
    }
}
