package com.sciz.server.infrastructure.shared.handler.declaration;

import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import com.sciz.server.infrastructure.shared.enums.WorkflowStatus;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final SysKnowledgeBaseRepo knowledgeBaseRepo;
    private final DeclarationRepo declarationRepo;
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
     * 旧行为：当申报状态更新为「申报成功」时，自动创建项目和知识库，并在工作流中记录「项目创建」步骤。
     * 新需求：项目创建改为用户自主选择，不再在申报成功事件中自动创建项目/知识库，也不再写入「项目创建」工作流步骤。
     * <p>
     * 现行为：仅记录申报成功的操作日志，保留研究课题的校验逻辑。
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

            // 1. 验证研究课题是否存在（即便不自动创建项目，研究课题仍为必填业务字段）
            var researchTopic = event.getResearchTopic();
            if (researchTopic == null || researchTopic.trim().isEmpty()) {
                log.error(String.format("研究课题为空，无法完成申报成功流程: declarationId=%s", event.getDeclarationId()));
                recordFailureLog(operation, event.getDeclarationId(), "研究课题不能为空", startTime, event);
                throw BusinessException.of(ResultCode.BAD_REQUEST, "研究课题不能为空");
            }

            // 2. 记录整体流程操作日志（成功）——不再自动创建项目和知识库
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：申报编号 %s（ID: %s），状态已更新为申报成功（未自动创建项目和知识库）",
                    operationType.getDescription(), event.getDeclarationNumber(), event.getDeclarationId());
            // 现在可以正常使用 LoginUserUtil，它会从 AsyncUserContext 获取用户信息
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("申报成功事件处理完成（未自动创建项目/知识库）: declarationId=%s, researchTopic=%s",
                    event.getDeclarationId(), researchTopic));

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

            // 更新工作流步骤状态为"成功"
            updateWorkflowStepStatus(event.getDeclarationId(), "项目创建", "success");

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
            
            // 更新工作流步骤状态为"失败"
            updateWorkflowStepStatus(event.getDeclarationId(), "项目创建", "failed");
            
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
            // 1. 生成唯一的知识库名称（避免与 Dify 知识库名称重复）
            var uniqueKnowledgeName = generateUniqueKnowledgeName(researchTopic);
            
            log.info(String.format("开始创建知识库: originalName=%s, uniqueName=%s, description=%s, projectId=%s",
                    researchTopic, uniqueKnowledgeName, researchTopic, projectId));

            var knowledgeCreateReq = new KnowledgeCreateReq();
            knowledgeCreateReq.setUserId(event.getOperatorId()); // 用户ID
            knowledgeCreateReq.setName(uniqueKnowledgeName); // 知识库名称（已确保唯一）
            knowledgeCreateReq.setDescription(researchTopic); // 知识库描述 = 研究课题
            knowledgeCreateReq.setProjectId(projectId); // 关联项目ID
            knowledgeCreateReq.setProjectName(researchTopic); // 关联项目名称（研究课题）
            // AsyncUserContext 已设置，可以直接使用 create 方法，无需设置 userId

            var knowledgeResp = knowledgeService.create(knowledgeCreateReq);
            log.info(String.format("知识库创建成功: knowledgeId=%s, name=%s, difyKnowdataId=%s",
                    knowledgeResp.getId(), knowledgeResp.getName(), knowledgeResp.getDifyKnowdataId()));

            // 更新项目的 Dify 知识库ID（使用 knowledgeResp.getId()，即本地知识库ID）
            updateProjectDifyKnowledgeId(event, projectId, String.valueOf(knowledgeResp.getId()));

            // 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), uniqueKnowledgeName,
                    knowledgeResp.getId());
            // 如果名称被修改，在日志中记录原始名称
            if (!uniqueKnowledgeName.equals(researchTopic)) {
                detail = String.format("%s：%s（原始名称：%s，ID: %s）", operationType.getDescription(),
                        uniqueKnowledgeName, researchTopic, knowledgeResp.getId());
                log.info(String.format("知识库名称已自动重命名: originalName=%s, newName=%s", researchTopic, uniqueKnowledgeName));
            }
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
     * 生成唯一的知识库名称
     * <p>
     * 如果原始名称已存在，则自动添加后缀 _1, _2, _3... 直到找到一个不重复的名称
     * 确保与 Dify 知识库名称不重复（Dify 知识库名称不允许重复）
     *
     * @param originalName 原始知识库名称
     * @return 唯一的知识库名称
     */
    private String generateUniqueKnowledgeName(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "知识库名称不能为空");
        }

        var trimmedName = originalName.trim();
        
        // 1. 检查原始名称是否已存在
        var existingKnowledge = knowledgeBaseRepo.findByName(trimmedName);
        if (existingKnowledge == null) {
            // 原始名称可用，直接返回
            log.debug(String.format("知识库名称可用: name=%s", trimmedName));
            return trimmedName;
        }

        // 2. 原始名称已存在，生成新名称（添加后缀 _1, _2, _3...）
        log.info(String.format("知识库名称已存在，开始生成唯一名称: originalName=%s, existingId=%s",
                trimmedName, existingKnowledge.getId()));

        var maxAttempts = 1000; // 最大尝试次数，防止无限循环
        for (var suffix = 1; suffix <= maxAttempts; suffix++) {
            var candidateName = String.format("%s_%d", trimmedName, suffix);
            var existing = knowledgeBaseRepo.findByName(candidateName);
            
            if (existing == null) {
                // 找到可用的名称
                log.info(String.format("生成唯一知识库名称成功: originalName=%s, uniqueName=%s, attempts=%d",
                        trimmedName, candidateName, suffix));
                return candidateName;
            }
            
            // 继续尝试下一个后缀
            log.debug(String.format("知识库名称仍重复，继续尝试: candidateName=%s, existingId=%s",
                    candidateName, existing.getId()));
        }

        // 理论上不应该到达这里（除非有大量重复的名称）
        throw BusinessException.of(ResultCode.SERVER_ERROR,
                "无法生成唯一的知识库名称，已尝试 %d 次。请检查数据库中的知识库名称。", maxAttempts);
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
                    null, // managerId 不更新
                    null, // startTime 不更新
                    null, // endTime 不更新
                    null, // budget 不更新
                    null, // description 不更新
                    null, // status 不更新
                    difyKnowledgeId, // 只更新 difyKnowledgeId
                    null, // members 不更新
                    null  // milestones 不更新
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

    /**
     * 更新工作流步骤状态
     * 用于在事件处理器中更新工作流步骤的状态（如项目创建成功/失败）
     *
     * @param declarationId 申报ID
     * @param stepName     步骤名称
     * @param stepStatus   步骤状态（success/failed/running）
     */
    private void updateWorkflowStepStatus(Long declarationId, String stepName, String stepStatus) {
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
                            log.error(String.format("等待重试被中断: declarationId=%s, stepName=%s", declarationId, stepName), e);
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

                // 查找并更新步骤状态
                boolean stepFound = false;
                for (var step : steps) {
                    if (stepName.equals(step.get("name"))) {
                        step.put("status", stepStatus);
                        step.put("timestamp", DateUtil.formatDateTime(java.time.LocalDateTime.now()));
                        stepFound = true;
                        break;
                    }
                }

                // 如果步骤不存在，添加新步骤
                if (!stepFound) {
                    var step = new HashMap<String, Object>();
                    step.put("name", stepName);
                    step.put("status", stepStatus);
                    step.put("timestamp", DateUtil.formatDateTime(java.time.LocalDateTime.now()));
                    steps.add(step);
                }

                // 更新工作流结果
                workflowResult.put("steps", steps);
                
                // 检查所有步骤是否都完成，决定工作流状态
                WorkflowStatus newWorkflowStatus = checkAndUpdateWorkflowStatus(steps, declaration.getWorkflowStatus());
                
                declarationRepo.updateWorkflowStatus(declarationId,
                        newWorkflowStatus.getCode(), JsonUtil.toJson(workflowResult));

                log.info(String.format("更新工作流步骤状态成功: declarationId=%s, stepName=%s, stepStatus=%s, workflowStatus=%s",
                        declarationId, stepName, stepStatus, newWorkflowStatus.getCode()));
                break; // 成功执行后退出循环
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn(String.format("更新工作流步骤状态失败，等待后重试: declarationId=%s, stepName=%s, attempt=%d/%d, err=%s",
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
                    log.error(String.format("更新工作流步骤状态失败（重试%d次后仍失败）: declarationId=%s, stepName=%s, err=%s",
                            maxRetries, declarationId, stepName, e.getMessage()), e);
                    return;
                }
            }
        }
    }

    /**
     * 获取工作流结果
     *
     * @param declaration 申报实体
     * @return 工作流结果Map
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
     * 检查所有步骤状态，决定工作流状态
     * <p>
     * 规则：
     * 1. 如果所有步骤都是 "success"，返回 COMPLETED
     * 2. 如果有任何步骤是 "failed"，返回 FAILED
     * 3. 否则（有步骤是 "running" 或没有步骤），返回 RUNNING
     *
     * @param steps           步骤列表
     * @param currentStatus   当前工作流状态
     * @return 新的工作流状态
     */
    private WorkflowStatus checkAndUpdateWorkflowStatus(List<Map<String, Object>> steps, String currentStatus) {
        if (steps == null || steps.isEmpty()) {
            // 没有步骤，保持当前状态或返回 RUNNING
            return WorkflowStatus.RUNNING;
        }

        // 检查是否有失败的步骤
        boolean hasFailed = steps.stream()
                .anyMatch(step -> "failed".equals(step.get("status")));
        if (hasFailed) {
            log.info("检测到失败的步骤，工作流状态更新为 FAILED");
            return WorkflowStatus.FAILED;
        }

        // 检查是否所有步骤都成功
        boolean allSuccess = steps.stream()
                .allMatch(step -> "success".equals(step.get("status")));
        if (allSuccess) {
            log.info("所有步骤都已完成，工作流状态更新为 COMPLETED");
            return WorkflowStatus.COMPLETED;
        }

        // 有步骤还在进行中，保持 RUNNING
        log.info("仍有步骤在进行中，工作流状态保持为 RUNNING");
        return WorkflowStatus.RUNNING;
    }
}
