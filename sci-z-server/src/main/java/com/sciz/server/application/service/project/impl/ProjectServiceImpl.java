package com.sciz.server.application.service.project.impl;

import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.response.project.ProjectInfoResp;
import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.project.ProjectCreatedEvent;
import com.sciz.server.infrastructure.shared.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 项目服务实现类
 * 集成领域事件发布
 *
 * @author JiaWen.Wu
 * @className ProjectServiceImpl
 * @date 2025-10-29 10:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final EventPublisher eventPublisher;
    // 注入其他依赖：ProjectRepository等

    /**
     * 创建项目
     *
     * @param projectCreateReq 项目创建请求
     * @return 创建结果
     */
    @Transactional
    public Result<ProjectInfoResp> createProject(ProjectCreateReq projectCreateReq) {
        try {
            // 1. 执行业务逻辑
            Project project = createProjectEntity(projectCreateReq);
            // projectRepository.save(project); // 保存项目

            // 2. 发布项目创建事件
            ProjectCreatedEvent event = new ProjectCreatedEvent(
                    String.valueOf(project.getId()),
                    project.getProjectName(),
                    project.getDescription(),
                    "",
                    "",
                    "",
                    // String.valueOf(project.getCreatorId()),
                    // project.getCreatorName(),
                    // project.getProjectType(),
                    project.getStatus().toString());
            eventPublisher.publish(event);

            // 3. 构建响应
            ProjectInfoResp projectInfoResp = buildProjectInfoResp(project);

            log.info(String.format("项目创建成功: projectId=%s, projectName=%s",
                    String.valueOf(project.getId()), project.getProjectName()));
            return Result.success(projectInfoResp, "项目创建成功");

        } catch (Exception e) {
            log.error(String.format("项目创建失败 err=%s", e.getMessage()), e);
            return Result.fail("项目创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新项目
     *
     * @param projectId        项目ID
     * @param projectCreateReq 项目更新请求
     * @return 更新结果
     */
    @Transactional
    public Result<ProjectInfoResp> updateProject(Long projectId, ProjectCreateReq projectCreateReq) {
        try {
            // 1. 执行业务逻辑
            Project project = updateProjectEntity(projectId, projectCreateReq);
            // projectRepository.save(project); // 保存项目

            // 2. 发布项目更新事件（需要创建ProjectUpdatedEvent）
            // ProjectUpdatedEvent event = new ProjectUpdatedEvent(...);
            // eventPublisher.publish(event);

            // 3. 构建响应
            ProjectInfoResp projectInfoResp = buildProjectInfoResp(project);

            log.info(String.format("项目更新成功: projectId=%s", String.valueOf(projectId)));
            return Result.success(projectInfoResp, "项目更新成功");

        } catch (Exception e) {
            log.error(String.format("项目更新失败: projectId=%s err=%s", String.valueOf(projectId), e.getMessage()), e);
            return Result.fail("项目更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @return 删除结果
     */
    @Transactional
    public Result<Void> deleteProject(Long projectId) {
        try {
            // 1. 执行业务逻辑
            // projectRepository.deleteById(projectId); // 删除项目

            // 2. 发布项目删除事件（需要创建ProjectDeletedEvent）
            // ProjectDeletedEvent event = new ProjectDeletedEvent(...);
            // eventPublisher.publish(event);

            log.info(String.format("项目删除成功: projectId=%s", String.valueOf(projectId)));
            return Result.success("项目删除成功");

        } catch (Exception e) {
            log.error(String.format("项目删除失败: projectId=%s err=%s", String.valueOf(projectId), e.getMessage()), e);
            return Result.fail("项目删除失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建项目实体
     */
    private Project createProjectEntity(ProjectCreateReq projectCreateReq) {
        Project project = new Project();
        project.setId(System.currentTimeMillis()); // 模拟ID生成
        project.setProjectName(projectCreateReq.getProjectName());
        project.setDescription(projectCreateReq.getDescription());
        // project.setProjectType(projectCreateReq.getProjectType());
        // project.setCreatorId(1L); // 模拟创建者ID
        // project.setCreatorName("系统管理员"); // 模拟创建者名称
        project.setStatus(ProjectStatus.IN_PROGRESS.getCode());
        return project;
    }

    /**
     * 更新项目实体
     */
    private Project updateProjectEntity(Long projectId, ProjectCreateReq projectCreateReq) {
        Project project = new Project();
        project.setId(projectId);
        project.setProjectName(projectCreateReq.getProjectName());
        project.setDescription(projectCreateReq.getDescription());
        // project.setProjectType(projectCreateReq.getProjectType());
        return project;
    }

    /**
     * 构建项目信息响应
     */
    private ProjectInfoResp buildProjectInfoResp(Project project) {
        ProjectInfoResp projectInfoResp = new ProjectInfoResp();
        // projectInfoResp.setProjectId(project.getId());
        projectInfoResp.setProjectName(project.getProjectName());
        projectInfoResp.setDescription(project.getDescription());
        // projectInfoResp.setProjectType(project.getProjectType());
        // projectInfoResp.setCreatorId(project.getCreatorId());
        // projectInfoResp.setCreatorName(project.getCreatorName());
        // projectInfoResp.setStatus(project.getStatus().getDescription());
        return projectInfoResp;
    }
}
