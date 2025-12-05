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
import com.sciz.server.domain.pojo.dto.response.project.ProjectProgressResp;
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
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.infrastructure.external.dify.dto.response.DifyDocumentBatchUploadResp;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.OperationLogRecorderStatus;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.project.ProjectUpdatedEvent;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final SysKnowledgeFileRelationRepo knowledgeFileRelationRepo;
    private final EventPublisher eventPublisher;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.beans.factory.annotation.Qualifier("globalTaskExecutor")
    private Executor globalTaskExecutor;

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

        // 2. 批量查询申报信息（获取开始时间、预计完成时间和项目负责人）
        var declarationIds = projectPage.getRecords().stream()
                .map(Project::getDeclarationId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        var declarationMap = declarationRepo.findByIds(declarationIds);

        // 3. 转换为响应对象，直接使用项目主表的进度值，并设置状态描述和时间信息
        var records = projectPage.getRecords().stream()
                .map(project -> {
                    var baseResp = projectConverter.toListResp(project);
                    // 直接使用项目主表的进度值（根据里程碑完成情况计算得出）
                    var progress = project.getProgress() != null ? project.getProgress() : 0;
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

            // 5. 处理里程碑（新增、更新、删除），返回新增的里程碑列表
            var newMilestones = updateProjectMilestones(req.id(), req.milestones(), userId, realName);

            // 6. 发布项目更新事件（如果存在新增的里程碑，用于异步更新待关联附件）
            if (!newMilestones.isEmpty()) {
                var event = new ProjectUpdatedEvent(
                        req.id(),
                        project.getName(),
                        userId,
                        realName,
                        newMilestones);
                eventPublisher.publishAsync(event);
                log.info(String.format("发布项目更新事件: projectId=%s, newMilestoneCount=%s",
                        req.id(), newMilestones.size()));
            }

            // 7. 记录操作日志（成功）
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
     *
     * @param projectId     项目ID
     * @param milestoneReqs 里程碑更新请求列表
     * @param userId        用户ID
     * @param realName      用户姓名
     * @return List<ProjectUpdatedEvent.MilestoneInfo> 新增的里程碑列表（用于事件发布）
     */
    private List<ProjectUpdatedEvent.MilestoneInfo> updateProjectMilestones(Long projectId,
            List<ProjectMilestoneUpdateReq> milestoneReqs, Long userId, String realName) {
        if (CollectionUtils.isEmpty(milestoneReqs)) {
            return List.of();
        }

        // 1. 查询现有里程碑
        var existingMilestones = projectProgressRepo.findMilestonesByProjectId(projectId);
        var existingMilestoneMap = existingMilestones.stream()
                .collect(Collectors.toMap(ProjectProgress::getId, milestone -> milestone));

        var now = LocalDateTime.now();
        var newMilestones = new ArrayList<ProjectUpdatedEvent.MilestoneInfo>();

        // 处理里程碑更新和新增
        milestoneReqs.forEach(milestoneReq -> {
            if (milestoneReq.id() != null && existingMilestoneMap.containsKey(milestoneReq.id())) {
                // 更新现有里程碑
                var existingMilestone = existingMilestoneMap.get(milestoneReq.id());
                var oldStartTime = existingMilestone.getMilestoneStartTime();
                var oldEndTime = existingMilestone.getMilestoneEndTime();

                existingMilestone.setTitle(milestoneReq.name());
                existingMilestone.setContent(milestoneReq.description());
                existingMilestone.setMilestoneStartTime(milestoneReq.startTime());
                existingMilestone.setMilestoneEndTime(milestoneReq.endTime());
                existingMilestone.setUpdatedBy(userId);
                existingMilestone.setUpdatedTime(now);

                // 如果开始时间或结束时间发生变化，需要重新计算里程碑进度
                boolean timeChanged = !java.util.Objects.equals(oldStartTime, milestoneReq.startTime())
                        || !java.util.Objects.equals(oldEndTime, milestoneReq.endTime());

                if (timeChanged) {
                    // 重新计算里程碑进度（基于新的时间）
                    var today = LocalDate.now();
                    int newProgress = calculateMilestoneProgressByTime(existingMilestone, today);

                    // 如果里程碑不是手动完成的，更新进度
                    // 如果用户已经手动完成了（progress = 100% 且当前时间 < 结束时间），不更新
                    boolean isManuallyCompleted = existingMilestone.getProgress() != null
                            && existingMilestone.getProgress() == 100
                            && milestoneReq.endTime() != null
                            && today.isBefore(milestoneReq.endTime());

                    if (!isManuallyCompleted) {
                        existingMilestone.setProgress(newProgress);
                    }
                }

                projectProgressRepo.updateById(existingMilestone);

                // 更新里程碑文档关联
                updateMilestoneDocuments(existingMilestone.getId(), milestoneReq.documents(), projectId);
            } else {
                // 新增里程碑
                var newMilestone = new ProjectProgress();
                newMilestone.setProjectId(projectId);
                newMilestone.setTitle(milestoneReq.name());
                newMilestone.setContent(milestoneReq.description());
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

                // 根据开始时间和结束时间计算初始进度（与创建时保持一致）
                var today = LocalDate.now();
                int initialProgress = calculateMilestoneProgressByTime(newMilestone, today);
                newMilestone.setProgress(initialProgress);

                var savedMilestoneId = projectProgressRepo.save(newMilestone);

                // 收集新增的里程碑信息（用于事件发布）
                newMilestones.add(new ProjectUpdatedEvent.MilestoneInfo(savedMilestoneId, milestoneReq.name()));
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

        return newMilestones;
    }

    /**
     * 更新里程碑文档关联
     *
     * @param milestoneId  里程碑ID
     * @param documentReqs 文档更新请求列表
     * @param projectId    项目ID（已废弃，保留用于兼容）
     */
    private void updateMilestoneDocuments(Long milestoneId, List<MilestoneDocumentUpdateReq> documentReqs,
            Long projectId) {
        if (CollectionUtils.isEmpty(documentReqs)) {
            return;
        }

        // 1. 查询现有文档关联（使用里程碑ID查询，因为里程碑文件上传时关联ID用的是里程碑ID）
        var existingAttachmentIds = sysAttachmentRelationRepo.findAttachmentIds(
                AttachmentRelationStatus.PROJECT.getCode(), milestoneId);

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
     * 根据里程碑完成情况重新计算项目进度
     *
     * @param projectId Long 项目ID
     */
    private void recalculateProjectProgress(Long projectId) {
        // 1. 查询所有里程碑
        var milestones = projectProgressRepo.findMilestonesByProjectId(projectId);

        // 2. 如果里程碑数量为0，项目进度为0%
        if (milestones.isEmpty()) {
            var project = projectRepo.findById(projectId);
            if (project != null) {
                project.setProgress(0);
                projectRepo.updateById(project);
            }
            return;
        }

        // 3. 确定总里程碑数量（固定为5）
        int totalCount = DEFAULT_MILESTONE_TOTAL_COUNT; // 5

        // 4. 按开始时间排序，只取前5个里程碑
        var sortedMilestones = milestones.stream()
                .sorted(Comparator.comparing(ProjectProgress::getMilestoneStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ProjectProgress::getId))
                .limit(totalCount)
                .toList();

        // 5. 计算每个里程碑的当前进度，并统计完成数量
        var today = LocalDate.now();

        // 使用 Stream API 处理每个里程碑，并统计完成数量
        int completedCount = (int) sortedMilestones.stream()
                .map(milestone -> {
                    // 计算里程碑的当前进度（自动计算或手动完成）
                    int milestoneProgress = calculateMilestoneProgressByTime(milestone, today);

                    // 更新里程碑的进度值（如果时间进度变化了，且不是手动完成的）
                    // 如果用户已经手动完成了（progress = 100% 且当前时间 < 结束时间），不更新
                    boolean isManuallyCompleted = milestone.getProgress() != null
                            && milestone.getProgress() == 100
                            && milestone.getMilestoneEndTime() != null
                            && today.isBefore(milestone.getMilestoneEndTime());

                    // 如果里程碑不是手动完成的，且进度需要更新（进度为null或计算出的进度与当前进度不同），则更新
                    if (!isManuallyCompleted
                            && (milestone.getProgress() == null
                                    || !milestone.getProgress().equals(milestoneProgress))) {
                        milestone.setProgress(milestoneProgress);
                        projectProgressRepo.updateById(milestone);
                    }

                    return milestoneProgress;
                })
                .filter(progress -> progress >= 100)
                .count();

        // 6. 计算项目进度：已完成里程碑数 / 总里程碑数 * 100%
        int projectProgress = (completedCount * 100) / totalCount;

        // 7. 更新项目主表的进度百分比
        var project = projectRepo.findById(projectId);
        if (project != null) {
            project.setProgress(projectProgress);
            projectRepo.updateById(project);
        }

        log.info(String.format("项目进度重新计算: projectId=%s, completedCount=%s/%s, progress=%s%%",
                projectId, completedCount, totalCount, projectProgress));
    }

    /**
     * 计算里程碑进度（自动计算，基于时间）
     *
     * @param milestone   里程碑实体
     * @param currentDate 当前日期
     * @return 进度百分比 (0-100)
     */
    private int calculateMilestoneProgressByTime(ProjectProgress milestone, LocalDate currentDate) {
        LocalDate startTime = milestone.getMilestoneStartTime();
        LocalDate endTime = milestone.getMilestoneEndTime();
        Integer currentProgress = milestone.getProgress();

        // 如果开始时间或结束时间为空，返回当前进度（或0）
        if (startTime == null || endTime == null) {
            return currentProgress != null ? currentProgress : 0;
        }

        // 如果用户已经手动完成了（progress = 100% 且当前时间 < 结束时间），保持100%
        if (currentProgress != null && currentProgress == 100 && currentDate.isBefore(endTime)) {
            return 100; // 手动完成，不再自动更新
        }

        // 当前时间 < 开始时间：0%
        if (currentDate.isBefore(startTime)) {
            return 0;
        }

        // 当前时间 >= 结束时间：100%（自动完成）
        if (currentDate.isAfter(endTime) || currentDate.isEqual(endTime)) {
            return 100;
        }

        // 开始时间 <= 当前时间 < 结束时间：按时间比例计算
        long totalDays = ChronoUnit.DAYS.between(startTime, endTime) + 1; // +1 包含结束当天
        long passedDays = ChronoUnit.DAYS.between(startTime, currentDate) + 1; // +1 包含当天

        // 按时间比例计算进度（最小0%，最大100%）
        int timeBasedProgress = (int) Math.min(100, Math.max(0, (passedDays * 100 / totalDays)));

        return timeBasedProgress;
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
    public void cancelById(Long id) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_CANCEL;
        var operation = operationType.getCode();

        log.info(String.format("开始取消项目: projectId=%s", id));

        try {
            // 1. 验证项目是否存在
            var project = projectRepo.findById(id);
            if (project == null) {
                throw BusinessException.of(ResultCode.PROJECT_NOT_FOUND);
            }

            // 2. 检查项目是否已经是已取消状态
            if (ProjectStatus.CANCELLED.getCode().toString().equals(project.getStatus())) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "项目已经是已取消状态");
            }

            // 3. 更新项目状态为已取消
            project.setStatus(ProjectStatus.CANCELLED.getCode().toString());
            var success = projectRepo.updateById(project);
            if (!success) {
                throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "项目取消失败");
            }

            // 4. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s）", operationType.getDescription(), project.getName(), id);
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("项目取消成功: projectId=%s, projectName=%s", id, project.getName()));

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getMessage();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：项目ID %s", operation, id),
                    errorMessage, executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：项目ID %s", operation, id),
                    e.getClass().getSimpleName(), executionTime);
            log.error(String.format("项目取消失败: projectId=%s, err=%s", id, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "项目取消失败: %s", e.getMessage());
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

        // 2. 获取所有里程碑ID
        var milestoneIds = milestones.stream()
                .map(ProjectProgress::getId)
                .toList();

        // 3. 批量查询里程碑附件关联（性能优化：使用里程碑ID查询所有附件）
        final Map<Long, List<Long>> milestoneAttachmentMap = sysAttachmentRelationRepo
                .findAttachmentIdsByRelationIds(AttachmentRelationStatus.PROJECT.getCode(), milestoneIds);

        // 4. 收集所有附件ID
        final List<Long> allAttachmentIds = milestoneAttachmentMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        // 5. 批量查询附件详情（性能优化：一次性查询所有附件）
        final Map<Long, SysAttachment> attachmentMap = allAttachmentIds.isEmpty()
                ? Map.<Long, SysAttachment>of()
                : sysAttachmentRepo.findByIds(allAttachmentIds).stream()
                        .collect(Collectors.toMap(SysAttachment::getId, attachment -> attachment));

        // 6. 构建里程碑响应列表（每个里程碑只显示关联到该里程碑的附件）
        return milestones.stream()
                .map(milestone -> {
                    // 获取该里程碑关联的附件ID列表
                    var milestoneAttachmentIds = milestoneAttachmentMap.getOrDefault(milestone.getId(), List.of());
                    // 构建该里程碑的附件响应列表
                    var attachmentRespList = milestoneAttachmentIds.stream()
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
    public ProjectProgressResp findProgress(Long id) {
        log.info(String.format("查询项目进度: projectId=%s", id));

        // 1. 查询项目实体
        var project = Optional.ofNullable(projectRepo.findById(id))
                .orElseThrow(() -> BusinessException.of(ResultCode.PROJECT_NOT_FOUND));

        // 2. 查询申报信息（获取项目负责人、开始时间、结束时间）
        var declaration = Optional.ofNullable(project.getDeclarationId())
                .map(declarationRepo::findById)
                .orElse(null);

        // 3. 查询所有里程碑
        var milestones = projectProgressRepo.findMilestonesByProjectId(id);

        // 4. 构建整体进度
        var overallProgress = new ProjectProgressResp.OverallProgress(
                project.getProgress(),
                declaration != null ? declaration.getProjectStartTime() : null,
                declaration != null ? declaration.getProjectEndTime() : null);

        // 5. 构建项目基本信息
        var projectInfo = new ProjectProgressResp.ProjectBasicInfo(
                project.getName(),
                project.getNumber(),
                declaration != null ? declaration.getProjectLeader() : null,
                getProjectStatusDescription(project.getStatus()));

        // 6. 构建里程碑响应列表（按开始时间排序）
        var milestoneRespList = buildMilestoneProgressRespList(milestones);

        // 7. 构建响应对象（移除 progressStats 字段）
        var resp = new ProjectProgressResp(
                projectInfo,
                overallProgress,
                milestoneRespList);

        log.info(String.format("查询项目进度成功: projectId=%s", id));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MilestoneDocumentUploadResp> uploadMilestoneDocument(FileBatchUploadReq req) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_MILESTONE_DOCUMENT_UPLOAD;
        var operation = operationType.getCode();

        var fileCount = req.files() != null ? req.files().length : 0;
        log.info(String.format("开始批量上传里程碑文档: relationId=%s, fileCount=%s", req.relationId(), fileCount));

        try {
            // 1. 前端传的 relationId 实际上是 projectId（此时还没有进度数据，也就没有进度ID）
            Long projectId = req.relationId();
            if (projectId == null) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "项目ID不能为空");
            }

            // 2. 验证项目是否存在
            var project = projectRepo.findById(projectId);
            if (project == null) {
                throw BusinessException.of(ResultCode.PROJECT_NOT_FOUND);
            }

            // 3. 构建新的上传请求，附件关联表的 relationId 设置为 null（文件服务会使用默认值 0，待关联）
            // 后续通过事件处理更新为实际的里程碑ID
            var uploadReq = new FileBatchUploadReq(
                    req.files(),
                    AttachmentRelationStatus.PROJECT.getCode(), // 关联类型仍然是 project
                    null, // relationId 设置为 null，文件服务会使用默认值 0（待关联）
                    req.relationName(),
                    req.attachmentType(),
                    req.isPublic());

            // 4. 调用文件服务批量上传文件到 MinIO
            List<FileInfoResp> fileInfoRespList = fileService.uploadBatch(uploadReq);

            // 6. 构建响应列表（只返回文件信息，前端根据附件ID调用文件服务的预览、下载、删除接口）
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

            // 7. 异步上传到 Dify 知识库（不阻塞接口响应）
            var currentUser = LoginUserUtil.requireCurrentUser();
            var userId = currentUser.userId();
            var username = currentUser.realName();

            // 在异步执行前获取必要的上下文信息（避免在异步线程中访问 Web 上下文）
            var asyncProjectId = project.getId();
            var asyncProjectName = project.getName();
            var asyncDifyKnowledgeId = project.getDifyKnowledgeId();
            var asyncFiles = req.files();

            // 异步执行 Dify 上传（使用全局任务执行器）
            CompletableFuture.runAsync(() -> {
                try {
                    log.info(String.format("开始异步上传文件到 Dify 知识库: projectId=%s, fileCount=%s", asyncProjectId,
                            asyncFiles.length));
                    uploadToDifyKnowledgeBaseAsync(asyncProjectId, asyncProjectName, asyncDifyKnowledgeId, asyncFiles,
                            fileInfoRespList, userId, username);
                } catch (Exception e) {
                    log.error(String.format("异步上传文件到 Dify 知识库失败: projectId=%s, err=%s", asyncProjectId, e.getMessage()),
                            e);
                    // 不抛出异常，避免影响主流程
                }
            }, globalTaskExecutor);

            log.info(String.format("已提交异步 Dify 上传任务: projectId=%s, fileCount=%s", asyncProjectId, asyncFiles.length));

            // 8. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：共 %s 个文件（项目：%s）", operationType.getDescription(), fileCount,
                    project.getName());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("批量上传里程碑文档成功: projectId=%s, fileCount=%s, successCount=%s",
                    projectId, fileCount, respList.size()));
            return respList;

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getMessage();
            var projectId = req.relationId(); // 前端传的 relationId 实际上是 projectId
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：文件数量 %s，项目ID %s", operation, fileCount, projectId),
                    errorMessage, executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getClass().getSimpleName();
            var projectId = req.relationId(); // 前端传的 relationId 实际上是 projectId
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：文件数量 %s，项目ID %s", operation, fileCount, projectId),
                    errorMessage, executionTime);
            log.error(String.format("批量上传里程碑文档失败: projectId=%s, fileCount=%s, err=%s",
                    projectId, fileCount, e.getMessage()), e);
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

            // 2. 删除知识库文件关联表（同步删除，避免数据不一致）
            try {
                log.info(String.format("开始删除知识库文件关联: attachmentId=%s", attachmentId));
                var deleted = knowledgeFileRelationRepo.deleteByAttachmentId(attachmentId);
                if (deleted) {
                    log.info(String.format("知识库文件关联删除成功: attachmentId=%s", attachmentId));
                } else {
                    log.info(String.format("知识库文件关联不存在或已删除: attachmentId=%s", attachmentId));
                }
            } catch (Exception e) {
                log.error(String.format("删除知识库文件关联失败: attachmentId=%s, err=%s", attachmentId,
                        e.getMessage()), e);
                // 不抛出异常，避免影响主流程
            }

            // 3. 同步顺序删除 MinIO 文件和 Dify 知识库文档（改为同步执行，便于排查问题）
            // 3.1. 先删除 MinIO 文件（包含附件表、附件关联表删除）
            try {
                log.info(String.format("开始删除文件（MinIO、附件表、附件关联表）: attachmentId=%s", attachmentId));
                fileService.delete(attachmentId);
                log.info(String.format("文件删除成功（MinIO、附件表、附件关联表）: attachmentId=%s", attachmentId));
            } catch (Exception e) {
                log.error(String.format("文件删除失败: attachmentId=%s, err=%s", attachmentId, e.getMessage()), e);
                // 不抛出异常，继续执行 Dify 删除
            }

            // 3.2. 再删除 Dify 知识库文档
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
                // 不抛出异常，避免影响主流程
            }

            // 4. 记录操作日志（成功）
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
     * @param projectId        项目ID
     * @param projectName      项目名称
     * @param difyKnowledgeId  知识库ID（字符串格式）
     * @param files            文件列表
     * @param fileInfoRespList 文件上传响应列表
     * @param userId           用户ID
     * @param username         用户名（用于操作日志）
     */
    private void uploadToDifyKnowledgeBaseAsync(Long projectId, String projectName, String difyKnowledgeId,
            MultipartFile[] files, List<FileInfoResp> fileInfoRespList, Long userId, String username) {
        // 如果项目未关联知识库，跳过上传
        if (difyKnowledgeId == null || difyKnowledgeId.trim().isEmpty()) {
            log.info(String.format("项目未关联知识库，跳过 Dify 上传: projectId=%s", projectId));
            return;
        }

        // 如果文件列表为空，跳过上传
        if (files == null || files.length == 0 || fileInfoRespList.isEmpty()) {
            log.info(String.format("文件列表为空，跳过 Dify 上传: projectId=%s", projectId));
            return;
        }

        var fileCount = files.length;
        var operationType = OperationLogRecorderStatus.KNOWLEDGE_UPLOAD_DIFY;
        var operation = operationType.getCode();
        var startTime = DateUtil.now();

        try {
            log.info(String.format("开始异步上传文件到 Dify 知识库: projectId=%s, fileCount=%s", projectId, fileCount));

            // 1. 查询知识库信息，获取 Dify 数据集ID
            var knowledgeId = Long.parseLong(difyKnowledgeId);
            var knowledgeBase = knowledgeBaseRepo.findById(knowledgeId);
            if (knowledgeBase == null || knowledgeBase.getDifyKnowdataId() == null) {
                log.warn(String.format("知识库不存在或未关联 Dify 数据集: knowledgeId=%s", knowledgeId));
                // 记录操作日志（失败）
                var endTime = DateUtil.now();
                var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
                operationLogRecorderUtil.recordFailure(operation,
                        String.format("%s失败：知识库不存在或未关联 Dify 数据集（项目：%s，知识库ID：%s）",
                                operation, projectName, knowledgeId),
                        "知识库不存在或未关联 Dify 数据集", executionTime, userId, username);
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

            // 5. 创建知识库文件关联记录
            createKnowledgeFileRelations(knowledgeBase.getId(), batchUploadResp, fileInfoRespList, userId);

            // 6. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：共 %s 个文件（项目：%s，知识库：%s）",
                    operationType.getDescription(), fileCount, projectName, knowledgeBase.getName());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime, userId, username);

            log.info(String.format("异步上传文件到 Dify 知识库成功: projectId=%s, fileCount=%s", projectId, fileCount));
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e instanceof BusinessException ? e.getMessage() : e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：共 %s 个文件（项目：%s）", operation, fileCount, projectName),
                    errorMessage, executionTime, userId, username);
            log.error(
                    String.format("异步上传文件到 Dify 知识库失败: projectId=%s, err=%s", projectId, e.getMessage()),
                    e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 构建里程碑进度响应列表（按开始时间排序）
     *
     * @param milestones List<ProjectProgress> 里程碑列表
     * @return List<MilestoneProgressResp> 里程碑进度响应列表
     */
    private List<ProjectProgressResp.MilestoneProgressResp> buildMilestoneProgressRespList(
            List<ProjectProgress> milestones) {
        if (milestones.isEmpty()) {
            return List.of();
        }

        var today = java.time.LocalDate.now();

        return milestones.stream()
                .sorted(Comparator.comparing(ProjectProgress::getMilestoneStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ProjectProgress::getId))
                .map(milestone -> {
                    var progress = milestone.getProgress() != null ? milestone.getProgress() : 0;
                    var endTime = milestone.getMilestoneEndTime();
                    var status = determineMilestoneStatus(progress, endTime, today);

                    return new ProjectProgressResp.MilestoneProgressResp(
                            milestone.getId(),
                            milestone.getTitle(),
                            milestone.getContent(),
                            milestone.getMilestoneStartTime(),
                            milestone.getMilestoneEndTime(),
                            progress,
                            status);
                })
                .toList();
    }

    /**
     * 判断里程碑状态
     *
     * @param progress Integer 进度百分比
     * @param endTime  LocalDate 结束时间
     * @param today    LocalDate 今天日期
     * @return String 状态描述（使用 ProjectStatus 枚举）
     */
    private String determineMilestoneStatus(Integer progress, LocalDate endTime, LocalDate today) {
        var actualProgress = progress != null ? progress : 0;

        if (actualProgress == 100) {
            return ProjectStatus.COMPLETED.getDescription();
        } else if (actualProgress > 0 && actualProgress < 100) {
            // 进行中，但如果已过结束时间，视为已延期
            if (endTime != null && endTime.isBefore(today)) {
                return ProjectStatus.DELAYED.getDescription();
            }
            return ProjectStatus.IN_PROGRESS.getDescription();
        } else {
            // 未开始，但如果已过结束时间，视为已延期
            if (endTime != null && endTime.isBefore(today)) {
                return ProjectStatus.DELAYED.getDescription();
            }
            return ProjectStatus.NOT_STARTED.getDescription();
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
                var documentCount = Math.min(batchUploadResp.documents().size(), fileInfoRespList.size());
                var updateCount = IntStream.range(0, documentCount)
                        .filter(i -> {
                            var docInfo = batchUploadResp.documents().get(i);
                            return docInfo != null && docInfo.id() != null && !docInfo.id().trim().isEmpty();
                        })
                        .map(i -> {
                            var docInfo = batchUploadResp.documents().get(i);
                            var fileInfo = fileInfoRespList.get(i);
                            updateAttachmentDifyDocId(fileInfo.id(), docInfo.id(), userId);
                            return 1;
                        })
                        .sum();
                log.info(String.format("批量更新附件 dify_doc_id 成功: fileCount=%s, documentCount=%s, updateCount=%s",
                        fileInfoRespList.size(), documentCount, updateCount));
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
                    // 单文件上传，fileInfoRespList 中只有一个文件，更新该附件
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

    /**
     * 创建知识库文件关联记录
     *
     * @param knowledgeId      知识库ID
     * @param batchUploadResp  Dify 批量上传响应
     * @param fileInfoRespList 文件信息响应列表
     * @param userId           用户ID
     */
    private void createKnowledgeFileRelations(Long knowledgeId, DifyDocumentBatchUploadResp batchUploadResp,
            List<FileInfoResp> fileInfoRespList, Long userId) {
        if (batchUploadResp == null || CollectionUtils.isEmpty(fileInfoRespList)) {
            log.warn("Dify 批量上传响应或文件列表为空，无法创建知识库文件关联");
            return;
        }

        try {
            var now = LocalDateTime.now();

            // 1. 优先使用 documents 数组（如果存在，包含所有文件的详细信息）
            if (batchUploadResp.documents() != null && !batchUploadResp.documents().isEmpty()) {
                var documentCount = Math.min(batchUploadResp.documents().size(), fileInfoRespList.size());
                IntStream.range(0, documentCount)
                        .filter(i -> {
                            var docInfo = batchUploadResp.documents().get(i);
                            return docInfo != null && docInfo.id() != null && !docInfo.id().trim().isEmpty();
                        })
                        .forEach(i -> {
                            var docInfo = batchUploadResp.documents().get(i);
                            var fileInfo = fileInfoRespList.get(i);
                            createKnowledgeFileRelation(knowledgeId, fileInfo, docInfo, userId, now,
                                    batchUploadResp);
                        });
                log.info(String.format("批量创建知识库文件关联成功: knowledgeId=%s, fileCount=%s, documentCount=%s",
                        knowledgeId, fileInfoRespList.size(), documentCount));
                return;
            }

            // 2. 如果 documents 数组不存在，使用单个 document 对象
            if (batchUploadResp.document() != null && batchUploadResp.document().id() != null
                    && !batchUploadResp.document().id().trim().isEmpty()) {
                String difyDocId = batchUploadResp.document().id();
                // 如果存在 batch ID，说明是批量上传，但响应只返回了第一个文档
                // 此时只能创建第一个文件的知识库关联
                if (batchUploadResp.isBatch()) {
                    log.warn(String.format(
                            "批量上传响应只包含第一个文档信息（batch=%s），其他文档的知识库关联需要通过批次状态查询获得",
                            batchUploadResp.batch()));
                    if (!fileInfoRespList.isEmpty()) {
                        var firstFileInfo = fileInfoRespList.get(0);
                        createKnowledgeFileRelation(knowledgeId, firstFileInfo, batchUploadResp.document(), userId,
                                now, batchUploadResp);
                        log.info(String.format("创建第一个文件的知识库关联成功: knowledgeId=%s, attachmentId=%s, difyDocId=%s",
                                knowledgeId, firstFileInfo.id(), difyDocId));
                    }
                } else {
                    // 单文件上传，fileInfoRespList 中只有一个文件，创建该文件的知识库关联
                    var lastFileInfo = fileInfoRespList.get(fileInfoRespList.size() - 1);
                    createKnowledgeFileRelation(knowledgeId, lastFileInfo, batchUploadResp.document(), userId, now,
                            batchUploadResp);
                    log.info(String.format("创建知识库文件关联成功: knowledgeId=%s, attachmentId=%s, difyDocId=%s",
                            knowledgeId, lastFileInfo.id(), difyDocId));
                }
                return;
            }

            log.warn(String.format("Dify 批量文档上传响应中缺少文档信息: batch=%s", batchUploadResp.batch()));

        } catch (Exception e) {
            log.error(String.format("创建知识库文件关联失败: knowledgeId=%s, err=%s", knowledgeId, e.getMessage()), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 创建单个知识库文件关联记录
     *
     * @param knowledgeId     知识库ID
     * @param fileInfo        文件信息
     * @param docInfo         Dify 文档信息
     * @param userId          用户ID
     * @param now             当前时间
     * @param batchUploadResp Dify 批量上传响应（用于获取回调数据）
     */
    private void createKnowledgeFileRelation(Long knowledgeId, FileInfoResp fileInfo,
            DifyDocumentBatchUploadResp.DifyDocumentInfo docInfo, Long userId, LocalDateTime now,
            DifyDocumentBatchUploadResp batchUploadResp) {
        try {
            var relation = new SysKnowledgeFileRelation();
            relation.setKnowledgeId(knowledgeId);
            relation.setFolderId(0L); // 根目录
            relation.setAttachmentId(fileInfo.id());
            relation.setFileName(fileInfo.originalName());
            relation.setSortOrder(0);
            relation.setCallback(null);
            relation.setCreatedBy(userId);
            relation.setUpdatedBy(userId);
            relation.setCreatedTime(now);
            relation.setUpdatedTime(now);
            relation.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());

            var relationId = knowledgeFileRelationRepo.save(relation);
            if (relationId != null) {
                log.debug(String.format("创建知识库文件关联成功: knowledgeId=%s, attachmentId=%s, difyDocId=%s",
                        knowledgeId, fileInfo.id(), docInfo.id()));
            } else {
                log.warn(String.format("创建知识库文件关联失败: knowledgeId=%s, attachmentId=%s",
                        knowledgeId, fileInfo.id()));
            }
        } catch (Exception e) {
            log.error(String.format("创建知识库文件关联异常: knowledgeId=%s, attachmentId=%s, err=%s",
                    knowledgeId, fileInfo.id(), e.getMessage()), e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeMilestone(Long milestoneId) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_MILESTONE_COMPLETE;
        var operation = operationType.getCode();

        log.info(String.format("开始完成里程碑: milestoneId=%s", milestoneId));

        try {
            // 1. 查询里程碑
            var milestone = projectProgressRepo.findById(milestoneId);
            if (milestone == null) {
                throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "里程碑不存在");
            }

            // 2. 验证是否是里程碑
            if (milestone.getIsMilestone() == null || milestone.getIsMilestone() != 1) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "该记录不是里程碑");
            }

            // 3. 设置进度为100%（手动完成）
            var currentUser = LoginUserUtil.requireCurrentUser();
            var now = LocalDateTime.now();
            milestone.setProgress(100);
            milestone.setUpdatedBy(currentUser.userId());
            milestone.setUpdatedTime(now);
            projectProgressRepo.updateById(milestone);

            // 4. 重新计算项目主表进度
            recalculateProjectProgress(milestone.getProjectId());

            // 5. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s，项目ID: %s）",
                    operationType.getDescription(), milestone.getTitle(), milestoneId, milestone.getProjectId());
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("里程碑完成成功: milestoneId=%s, projectId=%s", milestoneId, milestone.getProjectId()));

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getMessage();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：里程碑ID %s", operation, milestoneId),
                    errorMessage, executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：里程碑ID %s", operation, milestoneId),
                    errorMessage, executionTime);
            log.error(String.format("里程碑完成失败: milestoneId=%s, err=%s", milestoneId, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "里程碑完成失败: %s", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCompleteMilestone(Long milestoneId) {
        var startTime = DateUtil.now();
        var operationType = OperationLogRecorderStatus.PROJECT_MILESTONE_CANCEL_COMPLETE;
        var operation = operationType.getCode();

        log.info(String.format("开始取消完成里程碑: milestoneId=%s", milestoneId));

        try {
            // 1. 查询里程碑
            var milestone = projectProgressRepo.findById(milestoneId);
            if (milestone == null) {
                throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "里程碑不存在");
            }

            // 2. 验证是否是里程碑
            if (milestone.getIsMilestone() == null || milestone.getIsMilestone() != 1) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "该记录不是里程碑");
            }

            // 3. 验证是否已完成
            if (milestone.getProgress() == null || milestone.getProgress() < 100) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "该里程碑未完成，无需取消");
            }

            // 4. 临时将进度设置为 null，以便 calculateMilestoneProgressByTime 能正确计算时间进度
            var originalProgress = milestone.getProgress();
            milestone.setProgress(null);

            // 5. 重新计算里程碑进度（基于时间自动计算）
            var today = LocalDate.now();
            int newProgress = calculateMilestoneProgressByTime(milestone, today);

            // 6. 更新里程碑进度
            var currentUser = LoginUserUtil.requireCurrentUser();
            var now = LocalDateTime.now();
            milestone.setProgress(newProgress);
            milestone.setUpdatedBy(currentUser.userId());
            milestone.setUpdatedTime(now);
            projectProgressRepo.updateById(milestone);

            // 7. 重新计算项目主表进度
            recalculateProjectProgress(milestone.getProjectId());

            // 8. 记录操作日志（成功）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var detail = String.format("%s：%s（ID: %s，项目ID: %s，进度: %s%% → %s%%）",
                    operationType.getDescription(), milestone.getTitle(), milestoneId, milestone.getProjectId(),
                    originalProgress, newProgress);
            operationLogRecorderUtil.recordSuccess(operation, detail, executionTime);

            log.info(String.format("取消完成里程碑成功: milestoneId=%s, projectId=%s, originalProgress=%s%%, newProgress=%s%%",
                    milestoneId, milestone.getProjectId(), originalProgress, newProgress));

        } catch (BusinessException e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getMessage();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：里程碑ID %s", operation, milestoneId),
                    errorMessage, executionTime);
            throw e;
        } catch (Exception e) {
            // 记录操作日志（失败）
            var endTime = DateUtil.now();
            var executionTime = (int) DateUtil.millisBetween(startTime, endTime);
            var errorMessage = e.getClass().getSimpleName();
            operationLogRecorderUtil.recordFailure(operation,
                    String.format("%s失败：里程碑ID %s", operation, milestoneId),
                    errorMessage, executionTime);
            log.error(String.format("取消完成里程碑失败: milestoneId=%s, err=%s", milestoneId, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "取消完成里程碑失败: %s", e.getMessage());
        }
    }
}
