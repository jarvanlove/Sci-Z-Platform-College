package com.sciz.server.infrastructure.shared.handler.project;

import com.sciz.server.infrastructure.shared.event.project.ProjectCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 项目事件处理器
 * 处理项目相关的领域事件
 *
 * @author JiaWen.Wu
 * @className ProjectEventHandler
 * @date 2025-10-29 10:30
 */
@Slf4j
@Component
public class ProjectEventHandler {

    /**
     * 处理项目创建事件
     *
     * @param event 项目创建事件
     */
    @EventListener
    @Async
    public void handleProjectCreated(ProjectCreatedEvent event) {
        try {
            log.info("处理项目创建事件: projectId={}, projectName={}, creatorId={}",
                    event.getProjectId(), event.getProjectName(), event.getCreatorId());

            // 1. 发送项目创建通知
            sendProjectCreatedNotification(event);

            // 2. 初始化项目设置
            initializeProjectSettings(event);

            // 3. 记录项目创建审计日志
            logProjectCreation(event);

            // 4. 更新项目统计信息
            updateProjectStatistics(event);

            // 5. 创建项目工作空间
            createProjectWorkspace(event);

            log.info("项目创建事件处理完成: projectId={}", event.getProjectId());

        } catch (Exception e) {
            log.error("处理项目创建事件失败: projectId={}", event.getProjectId(), e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 发送项目创建通知
     */
    private void sendProjectCreatedNotification(ProjectCreatedEvent event) {
        log.info("发送项目创建通知: projectId={}, creatorId={}",
                event.getProjectId(), event.getCreatorId());
        // 实现发送通知的逻辑
        // 1. 通知项目创建者
        // 2. 通知相关管理员
        // 3. 发送邮件通知
    }

    /**
     * 初始化项目设置
     */
    private void initializeProjectSettings(ProjectCreatedEvent event) {
        log.info("初始化项目设置: projectId={}", event.getProjectId());
        // 实现初始化项目设置的逻辑
        // 1. 设置默认权限
        // 2. 创建项目文件夹结构
        // 3. 初始化项目配置
    }

    /**
     * 记录项目创建审计日志
     */
    private void logProjectCreation(ProjectCreatedEvent event) {
        log.info("记录项目创建审计日志: projectId={}, projectName={}, creatorId={}",
                event.getProjectId(), event.getProjectName(), event.getCreatorId());
        // 实现记录审计日志的逻辑
    }

    /**
     * 更新项目统计信息
     */
    private void updateProjectStatistics(ProjectCreatedEvent event) {
        log.info("更新项目统计信息: projectId={}, creatorId={}",
                event.getProjectId(), event.getCreatorId());
        // 实现更新统计信息的逻辑
        // 1. 更新用户项目数量
        // 2. 更新系统项目总数
        // 3. 更新项目类型统计
    }

    /**
     * 创建项目工作空间
     */
    private void createProjectWorkspace(ProjectCreatedEvent event) {
        log.info("创建项目工作空间: projectId={}", event.getProjectId());
        // 实现创建项目工作空间的逻辑
        // 1. 创建项目文件夹
        // 2. 初始化项目文档
        // 3. 设置项目权限
    }
}
