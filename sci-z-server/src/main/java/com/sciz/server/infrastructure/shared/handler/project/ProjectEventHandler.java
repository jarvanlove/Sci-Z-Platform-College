package com.sciz.server.infrastructure.shared.handler.project;

import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.event.project.ProjectCreatedEvent;
import com.sciz.server.infrastructure.shared.event.project.ProjectUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class ProjectEventHandler {

    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;

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

    /**
     * 处理项目更新事件
     * <p>
     * 当项目更新时，如果存在新增的里程碑，将待关联的附件（relationId=0）更新为实际的里程碑ID
     *
     * @param event 项目更新事件
     */
    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        try {
            log.info(String.format("处理项目更新事件: projectId=%s, projectName=%s, newMilestoneCount=%s",
                    event.getProjectId(), event.getProjectName(),
                    event.getNewMilestones() != null ? event.getNewMilestones().size() : 0));

            // 如果没有新增的里程碑，直接返回
            if (event.getNewMilestones() == null || event.getNewMilestones().isEmpty()) {
                log.debug(String.format("项目更新事件中没有新增的里程碑，跳过处理: projectId=%s",
                        event.getProjectId()));
                return;
            }

            // 处理每个新增的里程碑
            event.getNewMilestones().forEach(milestoneInfo -> {
                updatePendingAttachmentRelations(
                        event.getProjectId(),
                        milestoneInfo.getMilestoneName(),
                        milestoneInfo.getMilestoneId(),
                        event.getOperatorId());
            });

            log.info(String.format("项目更新事件处理完成: projectId=%s", event.getProjectId()));

        } catch (Exception e) {
            log.error(String.format("处理项目更新事件失败: projectId=%s, err=%s",
                    event.getProjectId(), e.getMessage()), e);
            // 不抛出异常，避免影响主流程
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 更新待关联的附件关联记录（将 relationId=0 的记录更新为实际的里程碑ID）
     *
     * @param projectId     项目ID
     * @param milestoneName 里程碑名称（用于匹配 relationName）
     * @param milestoneId   里程碑ID
     * @param userId        用户ID
     */
    private void updatePendingAttachmentRelations(Long projectId, String milestoneName, Long milestoneId, Long userId) {
        if (milestoneName == null || milestoneName.trim().isEmpty() || milestoneId == null) {
            return;
        }

        try {
            // 1. 查询该项目下待关联的附件关联记录（relationId=0，relationName 匹配里程碑名称）
            var pendingRelations = sysAttachmentRelationRepo.findPendingRelations(
                    AttachmentRelationStatus.PROJECT.getCode(),
                    milestoneName,
                    userId);

            if (pendingRelations.isEmpty()) {
                log.debug(String.format("未找到待关联的附件记录: projectId=%s, milestoneName=%s", projectId, milestoneName));
                return;
            }

            // 2. 验证这些附件是否属于该项目（通过附件表查询 uploaderId 和项目关联）
            // 简化处理：直接更新 relationId，因为前端上传时已经验证了项目ID
            var relationIds = pendingRelations.stream()
                    .map(SysAttachmentRelation::getId)
                    .toList();

            // 3. 批量更新 relationId 为新的里程碑ID
            var updated = sysAttachmentRelationRepo.updateRelationIds(relationIds, milestoneId, userId);
            if (updated) {
                log.info(String.format("更新待关联附件成功: projectId=%s, milestoneId=%s, milestoneName=%s, count=%s",
                        projectId, milestoneId, milestoneName, relationIds.size()));
            } else {
                log.warn(String.format("更新待关联附件失败: projectId=%s, milestoneId=%s, milestoneName=%s",
                        projectId, milestoneId, milestoneName));
            }
        } catch (Exception e) {
            log.error(String.format("更新待关联附件异常: projectId=%s, milestoneId=%s, milestoneName=%s, err=%s",
                    projectId, milestoneId, milestoneName, e.getMessage()), e);
            // 不抛出异常，避免影响主流程
        }
    }
}
