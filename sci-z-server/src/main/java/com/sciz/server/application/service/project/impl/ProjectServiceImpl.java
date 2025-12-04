package com.sciz.server.application.service.project.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.project.MilestoneDocumentDeleteReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectListQueryReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.dto.response.project.MilestoneDocumentUploadResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectStatisticsResp;
import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.domain.pojo.dto.response.project.MilestoneAttachmentResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectMemberResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectMilestoneResp;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.entity.project.ProjectProgress;
import com.sciz.server.domain.pojo.dto.request.project.MilestoneDocumentUpdateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectMemberUpdateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectMilestoneUpdateReq;
import com.sciz.server.domain.pojo.entity.project.ProjectMember;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectMemberRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectProgressRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.infrastructure.external.dify.dto.response.DifyDocumentBatchUploadResp;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.OperationLogRecorderStatus;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.infrastructure.shared.utils.OperationLogRecorderUtil;
import com.sciz.server.interfaces.converter.ProjectConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目应用服务实现类
 *
 * @author JiaWen.Wu
 * @className ProjectServiceImpl
 * @date 2025-01-24 16:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepo projectRepo;
    private final ProjectProgressRepo projectProgressRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final DeclarationRepo declarationRepo;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final SysAttachmentRepo sysAttachmentRepo;
    private final SysUserRepo sysUserRepo;
    private final FileService fileService;
    private final OperationLogRecorderUtil operationLogRecorderUtil;
    private final ProjectConverter projectConverter;
    private final DifyWorkflowService difyWorkflowService;
    private final SysKnowledgeBaseRepo knowledgeBaseRepo;

    /**
     * 项目编号前缀
     */
    private static final String PROJECT_NUMBER_PREFIX = "PRJ";

    /**
     * 默认里程碑总数量（用于计算项目进度百分比）
     * TODO： 后续支持从里程碑模板配置表中动态读取
     */
    private static final int DEFAULT_MILESTONE_TOTAL_COUNT = 5;

    /**
     * 时间戳格式化器（年月日时分秒）
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern(SystemConstant.TIMESTAMP_FORMATTER);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProjectCreateReq req) {
        log.info(String.format("开始创建项目: name=%s", req.name()));

        try {
            // 1. 获取当前登录用户
            var currentUser = LoginUserUtil.requireCurrentUser();
            var userId = currentUser.userId();

            // 2. 调用内部方法创建项目
            return createWithUserId(req, userId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("项目创建失败: err=%s", e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "项目创建失败: " + e.getMessage());
        }
    }

    /**
     * 使用指定的用户ID创建项目（用于异步事件处理，避免Web上下文问题）
     *
     * @param req    创建请求
     * @param userId 用户ID（用于设置 createdBy/updatedBy）
     * @return 项目ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createWithUserId(ProjectCreateReq req, Long userId) {
        log.info(String.format("开始创建项目（指定用户ID）: name=%s, userId=%s", req.name(), userId));

        try {
            // 1. 转换为实体
            var entity = projectConverter.toEntity(req);

            // 2. 设置项目基本信息
            initializeProjectEntity(entity, userId);

            // 3. 保存项目
            var projectId = projectRepo.save(entity);
            if (projectId == null) {
                throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "项目保存失败");
            }

            log.info(String.format("项目保存成功: projectId=%s, number=%s", projectId, entity.getNumber()));
            return projectId;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("项目创建失败: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "项目创建失败: %s", e.getMessage());
        }
    }

    @Override
    public PageResult<ProjectListResp> page(ProjectListQueryReq req) {
        log.info(String.format("分页查询项目列表: pageNo=%s, pageSize=%s, keyword=%s",
                req.pageNo(), req.pageSize(), req.keyword()));

        var baseQuery = req.toBaseQuery();
        var page = new Page<Project>(baseQuery.pageNo(), baseQuery.pageSize());
        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("createdTime");

        // 1. 查询项目列表
        IPage<Project> projectPage = projectRepo.page(
                page, req.keyword(), req.status(), sortBy, asc);

        // 2. 批量查询项目最新进度
        var projectIds = projectPage.getRecords().stream()
                .map(Project::getId)
                .toList();
        var progressMap = projectProgressRepo.findLatestProgressByProjectIds(projectIds);

        // 3. 批量查询申报信息（获取开始时间、预计完成时间和项目负责人）
        var declarationIds = projectPage.getRecords().stream()
                .map(Project::getDeclarationId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        var declarationMap = declarationRepo.findByIds(declarationIds);

        // 4. 转换为响应对象，使用 project_progress 表的进度值，并设置状态描述和时间信息
        var records = projectPage.getRecords().stream()
                .map(project -> {
                    var baseResp = projectConverter.toListResp(project);
                    // 从 project_progress 表获取最新进度，如果没有则使用项目主表的进度（可能为0或null）
                    var progress = progressMap.getOrDefault(project.getId(), project.getProgress());
                    // 获取项目状态描述
                    var statusDescription = getProjectStatusDescription(project.getStatus());
                    // 从申报表获取开始时间、预计完成时间和项目负责人
                    var declaration = declarationMap.get(project.getDeclarationId());
                    var startTime = declaration != null ? declaration.getProjectStartTime() : null;
                    var estimatedCompletionTime = declaration != null ? declaration.getProjectEndTime() : null;
                    var projectLeader = declaration != null ? declaration.getProjectLeader() : null;
                    // 重新创建 Record，更新 progress、statusDescription、时间字段和项目负责人
                    return new ProjectListResp(
                            baseResp.id(),
                            baseResp.number(),
                            baseResp.name(),
                            baseResp.description(),
                            baseResp.declarationId(),
                            baseResp.budget(),
                            progress,
                            baseResp.status(),
                            statusDescription,
                            startTime,
                            estimatedCompletionTime,
                            projectLeader,
                            baseResp.difyKnowledgeId(),
                            baseResp.createdTime(),
                            baseResp.updatedTime());
                })
                .toList();

        Page<ProjectListResp> resultPage = new Page<>(projectPage.getCurrent(), projectPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(projectPage.getTotal());
        return PageResult.of(resultPage);
    }

    @Override
    public ProjectDetailResp findDetail(Long id) {
        log.info(String.format("查询项目详情: projectId=%s", id));

        // 1. 查询项目实体
        var project = Optional.ofNullable(projectRepo.findById(id))
                .orElseThrow(() -> BusinessException.of(ResultCode.PROJECT_NOT_FOUND));

        // 2. 查询申报信息（获取项目负责人、部门、时间、研究方向）
        Declaration declaration = Optional.ofNullable(project.getDeclarationId())
                .map(declarationRepo::findById)
                .orElse(null);

        // 3. 批量查询项目成员
        var memberRespList = buildMemberRespList(id);

        // 4. 批量查询里程碑及附件
        var milestoneRespList = buildMilestoneRespList(id);

        // 5. 获取项目状态描述
        var statusDescription = getProjectStatusDescription(project.getStatus());

        // 6. 构建响应对象
        var resp = new ProjectDetailResp(
                project.getId(),
                project.getNumber(),
                project.getName(),
                project.getDescription(),
                project.getBudget(),
                project.getProgress(),
                project.getStatus(),
                statusDescription,
                declaration != null ? declaration.getProjectLeader() : null,
                declaration != null ? declaration.getDepartment() : null,
                declaration != null ? declaration.getProjectStartTime() : null,
                declaration != null ? declaration.getProjectEndTime() : null,
                declaration != null ? declaration.getResearchDirection() : null,
                memberRespList,
                milestoneRespList);

        log.info(String.format("查询项目详情成功: projectId=%s", id));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProjectUpdateReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_UPDATE;
        var operation = operationType.getCode();

        log.info(String.format("开始更新项目: projectId=%s", req.id()));

        try {
            var currentUser = LoginUserUtil.requireCurrentUser();
            Long userId = currentUser.userId();
            String realName = currentUser.realName();

            // 1. 查询项目实体
            var project = Optional.ofNullable(projectRepo.findById(req.id()))
                    .orElseThrow(() -> BusinessException.of(ResultCode.PROJECT_NOT_FOUND));

            // 2. 更新项目基本信息
            updateProjectBasicInfo(project, req, userId);

            // 3. 更新申报表信息（项目负责人、开始时间、结束时间）
            updateDeclarationInfo(project, req);

            // 4. 处理项目成员（新增、更新、删除）
            updateProjectMembers(req.id(), req.members(), userId, realName);

            // 5. 处理里程碑（新增、更新、删除）
            updateProjectMilestones(req.id(), req.milestones(), userId, realName);

            // 6. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), project.getName(), req.id());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("项目更新成功: projectId=%s", req.id()));

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getMessage();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：项目ID %s", operation, req.id()),
                    errorMessage, executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：项目ID %s", operation, req.id()),
                    errorMessage, executionTime);
            log.error(String.format("项目更新失败: projectId=%s, err=%s", req.id(), e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "项目更新失败: %s", e.getMessage());
        }
    }

    /**
     * 更新项目基本信息
     */
    private void updateProjectBasicInfo(Project project, ProjectUpdateReq req, Long userId) {
        if (req.budget() != null) {
            project.setBudget(req.budget());
        }
        if (StringUtils.hasText(req.description())) {
            project.setDescription(req.description());
        }
        if (StringUtils.hasText(req.status())) {
            project.setStatus(req.status());
        }
        if (StringUtils.hasText(req.difyKnowledgeId())) {
            project.setDifyKnowledgeId(req.difyKnowledgeId());
        }
        project.setUpdatedBy(userId);
        project.setUpdatedTime(LocalDateTime.now());

        var success = projectRepo.updateById(project);
        if (!success) {
            throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "项目基本信息更新失败");
        }
    }

    /**
     * 更新申报表信息
     */
    private void updateDeclarationInfo(Project project, ProjectUpdateReq req) {
        if (project.getDeclarationId() == null) {
            return;
        }

        var declaration = declarationRepo.findById(project.getDeclarationId());
        if (declaration == null) {
            log.warn(String.format("申报不存在: declarationId=%s", project.getDeclarationId()));
            return;
        }

        boolean needUpdate = false;

        // 更新项目负责人
        if (StringUtils.hasText(req.manager())) {
            // manager 可能是用户名或ID，需要解析
            Long managerId = parseManagerId(req.manager());
            if (managerId != null) {
                var manager = sysUserRepo.findById(managerId);
                if (manager != null) {
                    declaration.setProjectLeader(manager.getRealName());
                    needUpdate = true;
                }
            } else {
                // 尝试按用户名查找
                var manager = sysUserRepo.findByUsername(req.manager());
                if (manager != null) {
                    declaration.setProjectLeader(manager.getRealName());
                    needUpdate = true;
                } else {
                    // 如果找不到，直接使用传入的值
                    declaration.setProjectLeader(req.manager());
                    needUpdate = true;
                }
            }
        }

        // 更新项目开始时间
        if (req.startTime() != null) {
            declaration.setProjectStartTime(req.startTime());
            needUpdate = true;
        }

        // 更新项目结束时间
        if (req.endTime() != null) {
            declaration.setProjectEndTime(req.endTime());
            needUpdate = true;
        }

        if (needUpdate) {
            var success = declarationRepo.updateById(declaration);
            if (!success) {
                throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "申报信息更新失败");
            }
        }
    }

    /**
     * 解析项目负责人ID（可能是数字字符串或用户名）
     */
    private Long parseManagerId(String manager) {
        try {
            return Long.parseLong(manager);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 更新项目成员
     */
    private void updateProjectMembers(Long projectId, List<ProjectMemberUpdateReq> memberReqs, Long userId,
            String realName) {
        if (CollectionUtils.isEmpty(memberReqs)) {
            return;
        }

        // 1. 查询现有成员
        var existingMembers = projectMemberRepo.findByProjectId(projectId);
        var existingMemberMap = existingMembers.stream()
                .collect(Collectors.toMap(ProjectMember::getUserId, member -> member));

        // 2. 处理成员更新
        var memberUserIds = memberReqs.stream()
                .map(ProjectMemberUpdateReq::userId)
                .toList();

        // 批量查询用户信息
        var users = sysUserRepo.findByIds(memberUserIds);
        var userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        var now = LocalDateTime.now();

        // 处理成员更新和新增
        memberReqs.stream()
                .filter(memberReq -> userMap.containsKey(memberReq.userId()))
                .forEach(memberReq -> {
                    var existingMember = existingMemberMap.get(memberReq.userId());
                    var user = userMap.get(memberReq.userId());

                    if (existingMember != null) {
                        // 更新现有成员
                        existingMember.setRole(memberReq.role());
                        existingMember.setUpdatedBy(userId);
                        existingMember.setUpdatedTime(now);
                        projectMemberRepo.updateById(existingMember);
                    } else {
                        // 新增成员
                        var newMember = new ProjectMember();
                        newMember.setProjectId(projectId);
                        newMember.setUserId(memberReq.userId());
                        newMember.setUserName(user.getRealName());
                        newMember.setRole(memberReq.role());
                        newMember.setJoinTime(now);
                        newMember.setCreatedBy(userId);
                        newMember.setUpdatedBy(userId);
                        newMember.setCreatedTime(now);
                        newMember.setUpdatedTime(now);
                        newMember.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
                        projectMemberRepo.save(newMember);
                    }
                });

        // 记录不存在的用户
        memberReqs.stream()
                .filter(memberReq -> !userMap.containsKey(memberReq.userId()))
                .forEach(memberReq -> log.warn(String.format("用户不存在: userId=%s", memberReq.userId())));

        // 3. 删除不在请求中的成员
        var requestUserIds = memberReqs.stream()
                .map(ProjectMemberUpdateReq::userId)
                .collect(Collectors.toSet());
        var toDeleteIds = existingMembers.stream()
                .filter(member -> !requestUserIds.contains(member.getUserId()))
                .map(ProjectMember::getId)
                .toList();
        if (!toDeleteIds.isEmpty()) {
            projectMemberRepo.deleteBatchByIds(toDeleteIds);
        }
    }

    /**
     * 更新项目里程碑
     */
    private void updateProjectMilestones(Long projectId, List<ProjectMilestoneUpdateReq> milestoneReqs, Long userId,
            String realName) {
        if (CollectionUtils.isEmpty(milestoneReqs)) {
            return;
        }

        // 1. 查询现有里程碑
        var existingMilestones = projectProgressRepo.findMilestonesByProjectId(projectId);
        var existingMilestoneMap = existingMilestones.stream()
                .collect(Collectors.toMap(ProjectProgress::getId, milestone -> milestone));

        var now = LocalDateTime.now();

        // 处理里程碑更新和新增
        milestoneReqs.forEach(milestoneReq -> {
            if (milestoneReq.id() != null && existingMilestoneMap.containsKey(milestoneReq.id())) {
                // 更新现有里程碑
                var existingMilestone = existingMilestoneMap.get(milestoneReq.id());
                existingMilestone.setTitle(milestoneReq.name());
                existingMilestone.setContent(milestoneReq.description());
                existingMilestone.setMilestoneStartTime(milestoneReq.startTime());
                existingMilestone.setMilestoneEndTime(milestoneReq.endTime());
                existingMilestone.setUpdatedBy(userId);
                existingMilestone.setUpdatedTime(now);
                projectProgressRepo.updateById(existingMilestone);

                // 更新里程碑文档关联
                updateMilestoneDocuments(existingMilestone.getId(), milestoneReq.documents(), projectId);
            } else {
                // 新增里程碑
                var newMilestone = new ProjectProgress();
                newMilestone.setProjectId(projectId);
                newMilestone.setTitle(milestoneReq.name());
                newMilestone.setContent(milestoneReq.description());
                newMilestone.setProgress(0);
                newMilestone.setIsMilestone(1);
                newMilestone.setMilestoneStartTime(milestoneReq.startTime());
                newMilestone.setMilestoneEndTime(milestoneReq.endTime());
                newMilestone.setRecorderId(userId);
                newMilestone.setRecorderName(realName);
                newMilestone.setRecordTime(now);
                newMilestone.setCreatedBy(userId);
                newMilestone.setUpdatedBy(userId);
                newMilestone.setCreatedTime(now);
                newMilestone.setUpdatedTime(now);
                newMilestone.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
                projectProgressRepo.save(newMilestone);
            }
        });

        // 3. 删除不在请求中的里程碑
        var requestMilestoneIds = milestoneReqs.stream()
                .map(ProjectMilestoneUpdateReq::id)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        var toDeleteIds = existingMilestones.stream()
                .filter(milestone -> !requestMilestoneIds.contains(milestone.getId()))
                .map(ProjectProgress::getId)
                .toList();
        if (!toDeleteIds.isEmpty()) {
            projectProgressRepo.deleteBatchByIds(toDeleteIds);
        }

        // 4. 根据最新里程碑数量重新计算项目进度
        recalculateProjectProgress(projectId);
    }

    /**
     * 更新里程碑文档关联
     */
    private void updateMilestoneDocuments(Long milestoneId, List<MilestoneDocumentUpdateReq> documentReqs,
            Long projectId) {
        if (CollectionUtils.isEmpty(documentReqs)) {
            return;
        }

        // 1. 查询现有文档关联（使用项目ID查询）
        var existingAttachmentIds = sysAttachmentRelationRepo.findAttachmentIds(
                AttachmentRelationStatus.PROJECT.getCode(), projectId);

        // 2. 获取请求中的文档ID
        var requestAttachmentIds = documentReqs.stream()
                .map(MilestoneDocumentUpdateReq::id)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 3. 删除不在请求中的关联
        existingAttachmentIds.stream()
                .filter(id -> !requestAttachmentIds.contains(id))
                .forEach(sysAttachmentRelationRepo::deleteByAttachmentId);
    }

    /**
     * 根据里程碑数量重新计算项目进度
     *
     * @param projectId Long 项目ID
     */
    private void recalculateProjectProgress(Long projectId) {
        var milestones = projectProgressRepo.findMilestonesByProjectId(projectId);
        if (milestones.isEmpty()) {
            return;
        }

        // 1. 确定总里程碑数量（后续可改为从配置读取）
        var totalCount = DEFAULT_MILESTONE_TOTAL_COUNT;

        // 2. 按开始时间排序，依次分配进度百分比
        var sortedMilestones = milestones.stream()
                .sorted(Comparator.comparing(ProjectProgress::getMilestoneStartTime)
                        .thenComparing(ProjectProgress::getId))
                .toList();

        int finalProgress = 0;
        for (int index = 0; index < sortedMilestones.size(); index++) {
            var milestone = sortedMilestones.get(index);
            int progress = Math.min(100, (index + 1) * 100 / totalCount);
            milestone.setProgress(progress);
            projectProgressRepo.updateById(milestone);
            finalProgress = progress;
        }

        // 3. 同步更新项目主表的进度百分比
        var project = projectRepo.findById(projectId);
        if (project != null) {
            project.setProgress(finalProgress);
            projectRepo.updateById(project);
        }
    }

    /**
     * 格式化文件大小（字节 → KB/MB）
     *
     * @param fileSize Long 文件大小（字节）
     * @return String 格式化后的文件大小（如 27.71 KB / 3.20 MB）
     */
    private String formatFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "0 KB";
        }
        double size = fileSize.doubleValue();
        double kb = size / 1024.0;
        if (kb < 1024.0) {
            return String.format("%.2f KB", kb);
        }
        double mb = size / (1024.0 * 1024.0);
        return String.format("%.2f MB", mb);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info(String.format("开始删除项目: projectId=%s", id));

        try {
            // 1. 验证项目是否存在
            if (projectRepo.findById(id) == null) {
                throw BusinessException.of(ResultCode.PROJECT_NOT_FOUND);
            }

            // 2. 软删除
            var success = projectRepo.deleteById(id);
            if (!success) {
                throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "项目删除失败");
            }

            log.info(String.format("项目删除成功: projectId=%s", id));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("项目删除失败: projectId=%s, err=%s", id, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "项目删除失败: %s", e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建项目成员响应列表
     *
     * @param projectId Long 项目ID
     * @return List<ProjectMemberResp> 成员响应列表
     */
    private List<ProjectMemberResp> buildMemberRespList(Long projectId) {
        var members = projectMemberRepo.findByProjectId(projectId);
        return members.stream()
                .map(member -> new ProjectMemberResp(
                        member.getId(),
                        member.getUserId(),
                        member.getUserName(),
                        member.getRole(),
                        member.getJoinTime()))
                .toList();
    }

    /**
     * 构建项目里程碑响应列表（包含附件）
     *
     * @param projectId Long 项目ID
     * @return List<ProjectMilestoneResp> 里程碑响应列表
     */
    private List<ProjectMilestoneResp> buildMilestoneRespList(Long projectId) {
        // 1. 批量查询里程碑
        var milestones = projectProgressRepo.findMilestonesByProjectId(projectId);
        if (milestones.isEmpty()) {
            return List.of();
        }

        // 2. 批量查询项目附件关联（性能优化：使用项目ID查询所有附件）
        final Map<Long, List<Long>> projectAttachmentMap = sysAttachmentRelationRepo
                .findAttachmentIdsByRelationIds(AttachmentRelationStatus.PROJECT.getCode(), List.of(projectId));
        final List<Long> allProjectAttachmentIds = projectAttachmentMap.getOrDefault(projectId, List.of());

        // 3. 批量查询附件详情（性能优化：一次性查询所有附件）
        final Map<Long, SysAttachment> attachmentMap = allProjectAttachmentIds.isEmpty()
                ? Map.<Long, SysAttachment>of()
                : sysAttachmentRepo.findByIds(allProjectAttachmentIds).stream()
                        .collect(Collectors.toMap(SysAttachment::getId, attachment -> attachment));

        // 4. 构建里程碑响应列表（所有里程碑共享项目的所有附件）
        return milestones.stream()
                .map(milestone -> {
                    // 所有里程碑显示项目的所有附件（因为附件关联的是项目ID）
                    var attachmentRespList = allProjectAttachmentIds.stream()
                            .map(attachmentMap::get)
                            .filter(java.util.Objects::nonNull)
                            .map(attachment -> new MilestoneAttachmentResp(
                                    attachment.getId(),
                                    attachment.getFileName(),
                                    attachment.getOriginalName(),
                                    attachment.getFileType(),
                                    attachment.getFileExtension(),
                                    attachment.getFileSize(),
                                    formatFileSize(attachment.getFileSize()),
                                    attachment.getUploaderName(),
                                    attachment.getUploadTime(),
                                    attachment.getFileUrl(),
                                    attachment.getDifyDocId()))
                            .toList();
                    return new ProjectMilestoneResp(
                            milestone.getId(),
                            milestone.getTitle(),
                            milestone.getContent(),
                            milestone.getMilestoneStartTime(),
                            milestone.getMilestoneEndTime(),
                            attachmentRespList);
                })
                .toList();
    }

    /**
     * 初始化项目实体基本信息
     *
     * @param entity 项目实体
     * @param userId 用户ID
     */
    private void initializeProjectEntity(Project entity, Long userId) {
        var now = LocalDateTime.now();
        entity.setNumber(generateProjectNumber());
        entity.setStatus(Optional.ofNullable(entity.getStatus())
                .orElse(String.valueOf(ProjectStatus.IN_PROGRESS.getCode())));
        entity.setProgress(Optional.ofNullable(entity.getProgress()).orElse(0));
        entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
    }

    /**
     * 生成项目编号
     * 格式：PRJ + 年月日时分秒（时间戳）
     * 示例：PRJ20250124143025
     *
     * @return 项目编号
     */
    private String generateProjectNumber() {
        var timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return PROJECT_NUMBER_PREFIX + timestamp;
    }

    /**
     * 获取项目状态描述
     *
     * @param status String 项目状态代码（字符串格式）
     * @return String 项目状态描述
     */
    private String getProjectStatusDescription(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            var statusCode = Integer.parseInt(status);
            var projectStatus = ProjectStatus.fromCode(statusCode);
            return projectStatus.getDescription();
        } catch (Exception e) {
            log.warn(String.format("解析项目状态失败: status=%s, err=%s", status, e.getMessage()));
            return null;
        }
    }

    @Override
    public ProjectStatisticsResp getStatistics() {
        log.info("获取项目统计信息");

        // 1. 统计总项目数（所有未删除的项目）
        var totalProjects = projectRepo.countByStatus(null);

        // 2. 统计进行中的项目（status = "1"）
        var inProgressCount = projectRepo.countByStatus(String.valueOf(ProjectStatus.IN_PROGRESS.getCode()));

        // 3. 统计已延期项目（status = "3"）
        var delayedCount = projectRepo.countByStatus(String.valueOf(ProjectStatus.DELAYED.getCode()));

        // 4. 统计已完成项目（status = "2"）
        var completedCount = projectRepo.countByStatus(String.valueOf(ProjectStatus.COMPLETED.getCode()));

        log.info(String.format("项目统计: 总数=%s, 进行中=%s, 已延期=%s, 已完成=%s",
                totalProjects, inProgressCount, delayedCount, completedCount));

        return new ProjectStatisticsResp(totalProjects, inProgressCount, delayedCount, completedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MilestoneDocumentUploadResp> uploadMilestoneDocument(FileBatchUploadReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_MILESTONE_DOCUMENT_UPLOAD;
        var operation = operationType.getCode();

        var fileCount = req.files() != null ? req.files().length : 0;
        log.info(String.format("开始批量上传里程碑文档: projectId=%s, fileCount=%s", req.relationId(), fileCount));

        try {
            // 1. 验证项目ID是否存在（前端传递的 relationId 就是项目ID）
            if (req.relationId() == null) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "项目ID不能为空");
            }
            var project = projectRepo.findById(req.relationId());
            if (project == null) {
                throw BusinessException.of(ResultCode.PROJECT_NOT_FOUND);
            }

            // 2. 调用文件服务批量上传文件到 MinIO（前端已传递所有关联信息：relationType、relationId、relationName等）
            List<FileInfoResp> fileInfoRespList = fileService.uploadBatch(req);

            // 3. 构建响应列表（只返回文件信息，前端根据附件ID调用文件服务的预览、下载、删除接口）
            var respList = fileInfoRespList.stream()
                    .map(fileInfoResp -> new MilestoneDocumentUploadResp(
                            fileInfoResp.id(),
                            fileInfoResp.fileName(),
                            fileInfoResp.originalName(),
                            fileInfoResp.fileType(),
                            fileInfoResp.fileExtension(),
                            fileInfoResp.fileSize(),
                            formatFileSize(fileInfoResp.fileSize()),
                            fileInfoResp.uploaderName(),
                            fileInfoResp.uploadTime(),
                            fileInfoResp.fileUrl(),
                            fileInfoResp.previewUrl()))
                    .toList();

            // 4. 异步上传到 Dify 知识库
            var currentUser = LoginUserUtil.requireCurrentUser();
            uploadToDifyKnowledgeBaseAsync(project, req.files(), fileInfoRespList, currentUser.userId());

            // 5. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：共 %s 个文件（项目：%s）",
                    operationType.getDescription(), fileCount, project.getName());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("批量上传里程碑文档成功: projectId=%s, fileCount=%s, successCount=%s",
                    req.relationId(), fileCount, respList.size()));
            return respList;

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getMessage();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：文件数量 %s，项目ID %s", operation, fileCount, req.relationId()),
                    errorMessage, executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：文件数量 %s，项目ID %s", operation, fileCount, req.relationId()),
                    errorMessage, executionTime);
            log.error(String.format("批量上传里程碑文档失败: projectId=%s, fileCount=%s, err=%s",
                    req.relationId(), fileCount, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "批量上传里程碑文档失败: %s", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMilestoneDocument(MilestoneDocumentDeleteReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_MILESTONE_DOCUMENT_DELETE;
        var operation = operationType.getCode();

        var attachmentId = req.attachmentId();
        var projectId = req.projectId();
        var difyDocId = req.difyDocId();

        log.info(String.format("开始删除里程碑文档: attachmentId=%s, projectId=%s, difyDocId=%s", attachmentId, projectId,
                difyDocId));

        try {
            var currentUser = LoginUserUtil.requireCurrentUser();
            Long userId = currentUser.userId();

            // 1. 查询附件信息（用于日志记录）
            var attachment = Optional.ofNullable(sysAttachmentRepo.findById(attachmentId))
                    .orElseThrow(() -> BusinessException.of(ResultCode.DATA_NOT_FOUND, "附件不存在"));

            // 2. 异步并行删除 MinIO 文件（包含附件表、附件关联表删除）和 Dify 知识库文档
            // 使用虚拟线程执行器并行执行删除操作
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                // 并行执行文件服务删除（会删除 MinIO 文件、附件表、附件关联表）和 Dify 删除
                var fileServiceFuture = CompletableFuture.runAsync(() -> {
                    try {
                        log.info(String.format("开始删除文件（MinIO、附件表、附件关联表）: attachmentId=%s", attachmentId));
                        fileService.delete(attachmentId);
                        log.info(String.format("文件删除成功（MinIO、附件表、附件关联表）: attachmentId=%s", attachmentId));
                    } catch (Exception e) {
                        log.error(String.format("文件删除失败: attachmentId=%s, err=%s", attachmentId,
                                e.getMessage()), e);
                    }
                }, executor);

                var difyFuture = CompletableFuture.runAsync(() -> {
                    try {
                        if (difyDocId != null && !difyDocId.trim().isEmpty() && projectId != null) {
                            log.info(String.format("开始删除 Dify 知识库文档: attachmentId=%s, difyDocId=%s", attachmentId,
                                    difyDocId));
                            deleteDifyDocument(projectId, difyDocId, userId);
                            log.info(String.format("Dify 知识库文档删除成功: attachmentId=%s, difyDocId=%s", attachmentId,
                                    difyDocId));
                        } else {
                            log.info(String.format("跳过 Dify 文档删除: attachmentId=%s, difyDocId=%s, projectId=%s",
                                    attachmentId, difyDocId, projectId));
                        }
                    } catch (Exception e) {
                        log.error(String.format("Dify 知识库文档删除失败: attachmentId=%s, difyDocId=%s, err=%s", attachmentId,
                                difyDocId, e.getMessage()), e);
                    }
                }, executor);

                // 等待所有异步操作完成
                CompletableFuture.allOf(fileServiceFuture, difyFuture)
                        .exceptionally(throwable -> {
                            log.error(String.format("异步删除操作异常: attachmentId=%s, err=%s", attachmentId,
                                    throwable.getMessage()), throwable);
                            return null;
                        });
            }

            // 3. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), attachment.getOriginalName(),
                    attachmentId);
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("删除里程碑文档成功: attachmentId=%s", attachmentId));

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getMessage();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：附件ID %s", operation, attachmentId),
                    errorMessage, executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：附件ID %s", operation, attachmentId),
                    errorMessage, executionTime);
            log.error(String.format("删除里程碑文档失败: attachmentId=%s, err=%s", attachmentId, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "删除里程碑文档失败: %s", e.getMessage());
        }
    }

    /**
     * 删除 Dify 知识库文档
     */
    private void deleteDifyDocument(Long projectId, String difyDocId, Long userId) {
        try {
            // 1. 查询项目信息
            var project = projectRepo.findById(projectId);
            if (project == null || project.getDifyKnowledgeId() == null) {
                log.warn(String.format("项目不存在或未关联知识库: projectId=%s", projectId));
                return;
            }

            // 2. 查询知识库信息，获取 Dify 数据集ID
            var knowledgeId = Long.parseLong(project.getDifyKnowledgeId());
            var knowledgeBase = knowledgeBaseRepo.findById(knowledgeId);
            if (knowledgeBase == null || knowledgeBase.getDifyKnowdataId() == null) {
                log.warn(String.format("知识库不存在或未关联 Dify 数据集: knowledgeId=%s", knowledgeId));
                return;
            }

            // 3. 固定 resourceId 为 knowledge_base_001，keyType 为 dataset
            var resourceId = "knowledge_base_001";
            var keyType = "dataset";

            // 4. 调用 Dify API 删除文档
            difyWorkflowService.deleteDocument(
                    knowledgeBase.getDifyKnowdataId(),
                    difyDocId,
                    userId,
                    resourceId,
                    keyType);

            log.info(String.format("Dify 知识库文档删除成功: datasetId=%s, documentId=%s", knowledgeBase.getDifyKnowdataId(),
                    difyDocId));
        } catch (Exception e) {
            log.error(String.format("删除 Dify 知识库文档失败: projectId=%s, difyDocId=%s, err=%s", projectId, difyDocId,
                    e.getMessage()), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 异步上传文件到 Dify 知识库
     *
     * @param project          项目实体
     * @param files            文件列表
     * @param fileInfoRespList 文件上传响应列表
     * @param userId           用户ID
     */
    private void uploadToDifyKnowledgeBaseAsync(Project project, MultipartFile[] files,
            List<FileInfoResp> fileInfoRespList, Long userId) {
        // 如果项目未关联知识库，跳过上传
        if (project.getDifyKnowledgeId() == null || project.getDifyKnowledgeId().trim().isEmpty()) {
            log.info(String.format("项目未关联知识库，跳过 Dify 上传: projectId=%s", project.getId()));
            return;
        }

        // 如果文件列表为空，跳过上传
        if (files == null || files.length == 0 || fileInfoRespList.isEmpty()) {
            log.info(String.format("文件列表为空，跳过 Dify 上传: projectId=%s", project.getId()));
            return;
        }

        // 使用虚拟线程执行器异步执行上传
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture.runAsync(() -> {
                try {
                    log.info(String.format("开始异步上传文件到 Dify 知识库: projectId=%s, fileCount=%s", project.getId(),
                            files.length));

                    // 1. 查询知识库信息，获取 Dify 数据集ID
                    var knowledgeId = Long.parseLong(project.getDifyKnowledgeId());
                    var knowledgeBase = knowledgeBaseRepo.findById(knowledgeId);
                    if (knowledgeBase == null || knowledgeBase.getDifyKnowdataId() == null) {
                        log.warn(String.format("知识库不存在或未关联 Dify 数据集: knowledgeId=%s", knowledgeId));
                        return;
                    }

                    // 2. 固定 resourceId 为 knowledge_base_001，keyType 为 dataset
                    var resourceId = "knowledge_base_001";
                    var keyType = "dataset";

                    // 3. 调用 Dify API 批量上传文档（返回类型安全的响应对象）
                    var batchUploadResp = difyWorkflowService.createDocumentsBatch(
                            knowledgeBase.getDifyKnowdataId(),
                            files,
                            userId,
                            resourceId,
                            keyType);

                    // 4. 更新附件表的 dify_doc_id 字段（使用类型安全的响应对象）
                    updateAttachmentDifyDocIds(batchUploadResp, fileInfoRespList, userId);

                    log.info(String.format("异步上传文件到 Dify 知识库成功: projectId=%s, fileCount=%s", project.getId(),
                            files.length));
                } catch (Exception e) {
                    log.error(
                            String.format("异步上传文件到 Dify 知识库失败: projectId=%s, err=%s", project.getId(), e.getMessage()),
                            e);
                    // 不抛出异常，避免影响主流程
                }
            }, executor).exceptionally(throwable -> {
                log.error(String.format("异步上传文件到 Dify 知识库异常: projectId=%s, err=%s", project.getId(),
                        throwable.getMessage()), throwable);
                return null;
            });
        }
    }

    /**
     * 更新附件表的 dify_doc_id 字段（使用类型安全的响应对象）
     *
     * @param batchUploadResp  DifyDocumentBatchUploadResp 批量文档上传响应（类型安全）
     * @param fileInfoRespList 文件上传响应列表
     * @param userId           用户ID
     */
    private void updateAttachmentDifyDocIds(DifyDocumentBatchUploadResp batchUploadResp,
            List<FileInfoResp> fileInfoRespList, Long userId) {
        if (batchUploadResp == null) {
            log.warn("Dify 批量文档上传响应为空，无法更新附件表的 dify_doc_id 字段");
            return;
        }

        if (CollectionUtils.isEmpty(fileInfoRespList)) {
            log.warn("文件上传响应列表为空，无法更新附件表的 dify_doc_id 字段");
            return;
        }

        try {
            // 1. 优先使用 documents 数组（如果存在，包含所有文件的详细信息）
            if (batchUploadResp.documents() != null && !batchUploadResp.documents().isEmpty()) {
                int documentCount = Math.min(batchUploadResp.documents().size(), fileInfoRespList.size());
                for (int i = 0; i < documentCount; i++) {
                    var docInfo = batchUploadResp.documents().get(i);
                    if (docInfo != null && docInfo.id() != null && !docInfo.id().trim().isEmpty()) {
                        var fileInfo = fileInfoRespList.get(i);
                        updateAttachmentDifyDocId(fileInfo.id(), docInfo.id(), userId);
                    }
                }
                log.info(String.format("批量更新附件 dify_doc_id 成功: fileCount=%s, documentCount=%s",
                        fileInfoRespList.size(), documentCount));
                return;
            }

            // 2. 如果 documents 数组不存在，使用单个 document 对象
            // 注意：批量上传时，单个 document 可能只包含第一个文件的信息
            if (batchUploadResp.document() != null && batchUploadResp.document().id() != null
                    && !batchUploadResp.document().id().trim().isEmpty()) {
                String difyDocId = batchUploadResp.document().id();
                // 如果存在 batch ID，说明是批量上传，但响应只返回了第一个文档
                // 此时只能更新第一个附件的 dify_doc_id
                if (batchUploadResp.isBatch()) {
                    log.warn(String.format(
                            "批量上传响应只包含第一个文档信息（batch=%s），其他文档的 dify_doc_id 需要通过批次状态查询获得",
                            batchUploadResp.batch()));
                    if (!fileInfoRespList.isEmpty()) {
                        var firstFileInfo = fileInfoRespList.get(0);
                        updateAttachmentDifyDocId(firstFileInfo.id(), difyDocId, userId);
                        log.info(String.format("更新第一个附件 dify_doc_id 成功: attachmentId=%s, difyDocId=%s",
                                firstFileInfo.id(), difyDocId));
                    }
                } else {
                    // 单文件上传，更新最后一个附件（兼容旧逻辑）
                    var lastFileInfo = fileInfoRespList.get(fileInfoRespList.size() - 1);
                    updateAttachmentDifyDocId(lastFileInfo.id(), difyDocId, userId);
                    log.info(String.format("更新附件 dify_doc_id 成功: attachmentId=%s, difyDocId=%s",
                            lastFileInfo.id(), difyDocId));
                }
                return;
            }

            log.warn(String.format("Dify 批量文档上传响应中缺少文档信息: batch=%s", batchUploadResp.batch()));

        } catch (Exception e) {
            log.error(String.format("更新附件 dify_doc_id 失败: err=%s", e.getMessage()), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 更新单个附件的 dify_doc_id 字段
     *
     * @param attachmentId 附件ID
     * @param difyDocId    Dify 文档ID
     * @param userId       用户ID
     */
    private void updateAttachmentDifyDocId(Long attachmentId, String difyDocId, Long userId) {
        try {
            var attachment = sysAttachmentRepo.findById(attachmentId);
            if (attachment != null) {
                attachment.setDifyDocId(difyDocId);
                attachment.setUpdatedBy(userId);
                sysAttachmentRepo.update(attachment);
                log.debug(String.format("更新附件 dify_doc_id 成功: attachmentId=%s, difyDocId=%s", attachmentId, difyDocId));
            } else {
                log.warn(String.format("附件不存在，无法更新 dify_doc_id: attachmentId=%s", attachmentId));
            }
        } catch (Exception e) {
            log.error(String.format("更新附件 dify_doc_id 失败: attachmentId=%s, difyDocId=%s, err=%s",
                    attachmentId, difyDocId, e.getMessage()), e);
            // 不抛出异常，避免影响主流程
        }
    }
}
