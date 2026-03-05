package com.sciz.server.application.task;

import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 项目自动更新定时任务
 * <p>
 * 功能：
 * 1. 每天凌晨 1:00 自动更新所有活跃项目的进度和状态
 * 2. 根据里程碑完成情况重新计算项目进度
 * 3. 根据进度和时间自动判断项目状态（已完成/已延期/进行中/未开始）
 *
 * @author JiaWen.Wu
 * @className ProjectAutoUpdateTask
 * @date 2025-12-11 16:00
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectAutoUpdateTask {

    private final ProjectRepo projectRepo;
    private final DeclarationRepo declarationRepo;
    private final ProjectService projectService;

    /**
     * 定时任务：自动更新所有项目的进度和状态
     * <p>
     * 执行时间：每天凌晨 1:00
     * Cron 表达式：0 0 1 * * ? （秒 分 时 日 月 周）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoUpdateAllProjects() {
        log.info("开始定时任务：自动更新项目进度和状态");

        try {
            // 1. 查询所有需要更新的项目（排除已取消和已删除的项目，定时任务用专用方法不走数据权限）
            var projects = projectRepo.findAllActiveProjectsForTask();

            if (projects.isEmpty()) {
                log.info("没有需要更新的项目");
                return;
            }

            log.info(String.format("找到 %d 个需要更新的项目", projects.size()));

            // 2. 批量查询申报信息（避免 N+1 查询，提升性能）
            var declarationIds = projects.stream()
                    .map(Project::getDeclarationId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Declaration> declarationMap = Map.of();
            if (!declarationIds.isEmpty()) {
                declarationMap = declarationRepo.findByIds(declarationIds);
                log.debug(String.format("批量查询申报信息: declarationCount=%d", declarationMap.size()));
            }

            // 3. 使用 Stream API 批量更新项目进度和状态
            var updateResults = projects.stream()
                    .map(project -> {
                        try {
                            // 3.1 调用 Service 层的自动更新方法
                            boolean success = projectService.autoUpdateProjectProgressAndStatus(project.getId());

                            if (success) {
                                // 重新查询项目，检查状态是否发生变化
                                var updatedProject = projectRepo.findById(project.getId());
                                boolean statusChanged = updatedProject != null
                                        && !updatedProject.getStatus().equals(project.getStatus());

                                return new UpdateResult(true, statusChanged, null);
                            } else {
                                return new UpdateResult(false, false, null);
                            }
                        } catch (Exception e) {
                            log.error(String.format("自动更新项目失败: projectId=%s, projectName=%s, err=%s",
                                    project.getId(), project.getName(), e.getMessage()), e);
                            return new UpdateResult(false, false, e);
                        }
                    })
                    .toList();

            // 4. 统计更新结果
            long successCount = updateResults.stream()
                    .filter(UpdateResult::success)
                    .count();
            long failCount = updateResults.stream()
                    .filter(result -> !result.success())
                    .count();
            long statusUpdatedCount = updateResults.stream()
                    .filter(UpdateResult::statusChanged)
                    .count();

            // 5. 记录任务执行结果（合并为一个日志）
            log.info(String.format("定时任务完成: 项目总数=%d, 更新成功=%d, 更新失败=%d, 状态变更=%d",
                    projects.size(), successCount, failCount, statusUpdatedCount));

        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }
    }

    /**
     * 更新结果记录类
     *
     * @param success       是否更新成功
     * @param statusChanged 状态是否发生变化
     * @param exception     异常信息（如果更新失败）
     */
    private record UpdateResult(boolean success, boolean statusChanged, Exception exception) {
    }
}
