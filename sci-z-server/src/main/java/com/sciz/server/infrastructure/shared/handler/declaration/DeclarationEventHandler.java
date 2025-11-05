package com.sciz.server.infrastructure.shared.handler.declaration;

import com.sciz.server.infrastructure.shared.event.declaration.DeclarationCreatedEvent;
import com.sciz.server.infrastructure.shared.event.declaration.DeclarationUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
public class DeclarationEventHandler {

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
     */
    private void sendDeclarationCreatedNotification(DeclarationCreatedEvent event) {
        log.info("发送申报创建通知: declarationId={}, applicantId={}",
                event.getDeclarationId(), event.getApplicantId());
        // 实现发送通知的逻辑
    }

    /**
     * 初始化申报流程
     */
    private void initializeDeclarationProcess(DeclarationCreatedEvent event) {
        log.info("初始化申报流程: declarationId={}", event.getDeclarationId());
        // 实现初始化申报流程的逻辑
    }

    /**
     * 记录申报创建审计日志
     */
    private void logDeclarationCreation(DeclarationCreatedEvent event) {
        log.info("记录申报创建审计日志: declarationId={}, declarationName={}, applicantId={}",
                event.getDeclarationId(), event.getDeclarationName(), event.getApplicantId());
        // 实现记录审计日志的逻辑
    }

    /**
     * 更新申报统计信息
     */
    private void updateDeclarationStatistics(DeclarationCreatedEvent event) {
        log.info("更新申报统计信息: declarationId={}, declarationType={}",
                event.getDeclarationId(), event.getDeclarationType());
        // 实现更新统计信息的逻辑
    }

    /**
     * 发送申报状态变更通知
     */
    private void sendDeclarationStatusChangeNotification(DeclarationUpdatedEvent event) {
        log.info("发送申报状态变更通知: declarationId={}, oldStatus={}, newStatus={}",
                event.getDeclarationId(), event.getOldStatus(), event.getNewStatus());
        // 实现发送通知的逻辑
    }

    /**
     * 更新申报流程状态
     */
    private void updateDeclarationProcessStatus(DeclarationUpdatedEvent event) {
        log.info("更新申报流程状态: declarationId={}, newStatus={}",
                event.getDeclarationId(), event.getNewStatus());
        // 实现更新流程状态的逻辑
    }

    /**
     * 记录申报更新审计日志
     */
    private void logDeclarationUpdate(DeclarationUpdatedEvent event) {
        log.info("记录申报更新审计日志: declarationId={}, oldStatus={}, newStatus={}",
                event.getDeclarationId(), event.getOldStatus(), event.getNewStatus());
        // 实现记录审计日志的逻辑
    }

    /**
     * 触发后续流程
     */
    private void triggerFollowUpProcess(DeclarationUpdatedEvent event) {
        log.info("触发后续流程: declarationId={}, newStatus={}",
                event.getDeclarationId(), event.getNewStatus());
        // 实现触发后续流程的逻辑
    }
}
