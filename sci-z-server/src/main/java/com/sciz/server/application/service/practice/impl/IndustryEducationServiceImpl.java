package com.sciz.server.application.service.practice.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.declaration.DeclarationService;
import com.sciz.server.application.service.message.MessageService;
import com.sciz.server.application.service.practice.IndustryEducationService;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.request.practice.DistributeReq;
import com.sciz.server.domain.pojo.dto.request.practice.DistributeRejectReq;
import com.sciz.server.domain.pojo.dto.request.practice.IndustryEducationAssignReq;
import com.sciz.server.domain.pojo.dto.request.practice.TeamMatchReq;
import com.sciz.server.domain.pojo.dto.response.practice.EfficiencyChartData;
import com.sciz.server.domain.pojo.dto.response.practice.ProjectLeaderResp;
import com.sciz.server.domain.pojo.dto.response.practice.TeamDetailResp;
import com.sciz.server.domain.pojo.dto.response.practice.TeamMemberResp;
import com.sciz.server.domain.pojo.dto.response.practice.TeamMatchResp;
import com.sciz.server.domain.pojo.dto.response.practice.TeamProjectResp;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.entity.message.SysMessage;
import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.domain.pojo.entity.project.ProjectMember;
import com.sciz.server.domain.pojo.entity.project.ProjectProgress;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.mapper.message.SysMessageMapper;
import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import com.sciz.server.domain.pojo.entity.user.SysProfileField;
import com.sciz.server.domain.pojo.entity.user.SysProfileFieldOption;
import com.sciz.server.domain.pojo.entity.user.SysUserProfile;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectMemberRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectProgressRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.domain.pojo.repository.user.SysProfileFieldRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserProfileRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 产教研智能体应用服务实现
 *
 * @author Sci-Z
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryEducationServiceImpl implements IndustryEducationService {

    private static final String PROJECT_NUMBER_PREFIX = "PRJ";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern(SystemConstant.TIMESTAMP_FORMATTER);
    private static final String EFFICIENCY_PLACEHOLDER = "—";
    private static final String HONORS_PLACEHOLDER = "—";
    private static final String MESSAGE_TYPE_DISTRIBUTE = "industry_education_distribute";
    private static final String MESSAGE_STATUS_PENDING = "pending";
    private static final String MESSAGE_STATUS_ACCEPTED = "accepted";
    private static final String MESSAGE_STATUS_REJECTED = "rejected";
    /** 用户档案中职称/职务属性编码，与 Auth 模块一致 */
    private static final String PROFILE_TITLE_CODE = "title";

    private final ProjectRepo projectRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectProgressRepo projectProgressRepo;
    private final DeclarationRepo declarationRepo;
    private final SysUserRepo sysUserRepo;
    private final SysDepartmentRepo sysDepartmentRepo;
    private final SysUserProfileRepo sysUserProfileRepo;
    private final SysProfileFieldRepo sysProfileFieldRepo;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final MessageService messageService;
    private final SysMessageMapper messageMapper;
    private final DeclarationService declarationService;
    private final ObjectMapper objectMapper;

    @Override
    public List<TeamMatchResp> matchTeams(TeamMatchReq req) {
        int limit = req.effectiveLimit();
        String keyword = StringUtils.hasText(req.keyword()) ? req.keyword().trim() : null;

        List<Long> declarationIds = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            declarationIds.addAll(declarationRepo.findIdsByKeywordForMatch(keyword, 200));
        }

        List<Project> projects = projectRepo.findForIndustryEducationMatch(declarationIds, keyword, limit);
        List<TeamMatchResp> result = new ArrayList<>(projects.size());

        for (Project p : projects) {
            String leaderName = resolveLeaderName(p.getManagerId(), p.getDeclarationId());
            int memberCount = projectMemberRepo.findByProjectId(p.getId()).size();
            int participantProjectCount = countParticipantProjects(p.getManagerId());
            result.add(new TeamMatchResp(
                    p.getId(),
                    p.getName(),
                    leaderName != null ? leaderName : "",
                    memberCount,
                    participantProjectCount,
                    EFFICIENCY_PLACEHOLDER
            ));
        }
        return result;
    }

    /**
     * 统计用户参与的项目总数（作为负责人或成员）
     *
     * @param userId 用户ID（通常为项目负责人）
     * @return 参与项目数量，0 表示无或未传用户
     */
    private int countParticipantProjects(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<Long> asManager = projectRepo.findProjectIdsByManagerId(userId);
        List<Long> asMember = projectMemberRepo.findProjectIdsByUserId(userId);
        return (int) java.util.stream.Stream.concat(asManager.stream(), asMember.stream())
                .distinct()
                .count();
    }

    @Override
    public TeamDetailResp getTeamDetail(Long teamId) {
        Project project = projectRepo.findById(teamId);
        if (project == null) {
            return null;
        }
        String leaderName = resolveLeaderName(project.getManagerId(), project.getDeclarationId());
        List<ProjectMember> members = projectMemberRepo.findByProjectId(teamId);
        ProjectLeaderResp leaderResp = resolveProjectLeader(project.getManagerId(), project.getDeclarationId());
        List<TeamMemberResp> memberResps = members.stream()
                .map(m -> resolveMemberResp(m.getUserId(), m.getUserName(), m.getRole()))
                .toList();
        String statusDesc = getProjectStatusDescription(project.getStatus());
        EfficiencyChartData chartData = buildEfficiencyChartData(teamId, project);
        List<TeamProjectResp> projectResps = List.of(
                new TeamProjectResp(
                        project.getId(),
                        project.getName(),
                        statusDesc,
                        leaderResp,
                        memberResps,
                        chartData,
                        List.of()
                ));
        EfficiencyChartData nonLeadChart = buildNonLeadParticipantChartData(project.getManagerId(), teamId);
        return new TeamDetailResp(
                project.getId(),
                project.getName(),
                leaderName != null ? leaderName : "",
                memberResps,
                projectResps,
                EFFICIENCY_PLACEHOLDER,
                HONORS_PLACEHOLDER,
                chartData,
                nonLeadChart
        );
    }

    private ProjectLeaderResp resolveProjectLeader(Long managerId, Long declarationId) {
        String realName = null;
        String college = null;
        if (managerId != null) {
            SysUser user = sysUserRepo.findById(managerId);
            if (user != null) {
                realName = StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
                if (user.getDepartmentId() != null) {
                    SysDepartment dept = sysDepartmentRepo.findById(user.getDepartmentId());
                    college = dept != null ? dept.getDepartmentName() : null;
                }
            }
        }
        if (realName == null && declarationId != null) {
            Declaration decl = declarationRepo.findById(declarationId);
            if (decl != null && StringUtils.hasText(decl.getProjectLeader())) {
                realName = decl.getProjectLeader();
            }
        }
        String position = resolveTitleLabel(managerId);
        return new ProjectLeaderResp(
                realName != null ? realName : "—",
                college != null ? college : "—",
                position != null ? position : "—"
        );
    }

    private TeamMemberResp resolveMemberResp(Long userId, String userName, String role) {
        String realName = userName;
        String college = "—";
        String position = resolveTitleLabel(userId);
        if (position == null) {
            position = role != null ? role : "—";
        }
        if (userId != null) {
            SysUser user = sysUserRepo.findById(userId);
            if (user != null) {
                realName = StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
                if (user.getDepartmentId() != null) {
                    SysDepartment dept = sysDepartmentRepo.findById(user.getDepartmentId());
                    college = dept != null ? dept.getDepartmentName() : "—";
                }
            }
        }
        return new TeamMemberResp(userId, userName != null ? userName : "—", realName != null ? realName : "—", college, position, role != null ? role : "—");
    }

    /**
     * 根据用户档案中的职称编码解析为展示用职务标签（查 sys_user_profile + sys_profile_field_option）
     *
     * @param userId 用户ID
     * @return 职务展示名，未配置或查不到时返回 null
     */
    private String resolveTitleLabel(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserRepo.findById(userId);
        if (user == null || !StringUtils.hasText(user.getIndustryType())) {
            return null;
        }
        SysUserProfile profile = sysUserProfileRepo.findByUserIdAndAttribute(userId, PROFILE_TITLE_CODE);
        if (profile == null || !StringUtils.hasText(profile.getAttributeValue())) {
            return null;
        }
        String titleCode = profile.getAttributeValue();
        Optional<SysProfileField> titleField = sysProfileFieldRepo.listEnabledByIndustry(user.getIndustryType()).stream()
                .filter(f -> PROFILE_TITLE_CODE.equals(f.getFieldCode()))
                .findFirst();
        if (titleField.isEmpty()) {
            return null;
        }
        List<SysProfileFieldOption> options = sysProfileFieldRepo.listOptionsByFieldIds(List.of(titleField.get().getId()));
        return options.stream()
                .filter(opt -> titleCode.equals(opt.getOptionValue()))
                .map(SysProfileFieldOption::getOptionLabel)
                .findFirst()
                .orElse(null);
    }

    /** 根据项目进度与里程碑构建效率图表数据：完成率、里程碑数、周期(天)、产出（里程碑文件数） */
    private EfficiencyChartData buildEfficiencyChartData(Long projectId, Project project) {
        List<String> x = List.of("完成率", "里程碑", "周期(天)", "产出");
        int progress = project.getProgress() != null ? project.getProgress() : 0;
        List<ProjectProgress> milestones = projectProgressRepo.findMilestonesByProjectId(projectId);
        long completedMilestones = milestones.stream().filter(m -> m.getProgress() != null && m.getProgress() >= 100).count();
        long cycleDays = resolveProjectCycleDays(project, milestones);
        long outputCount = countMilestoneAttachmentOutput(projectId, milestones);
        List<Number> y = List.of(progress, completedMilestones, cycleDays, outputCount);
        return new EfficiencyChartData(x, y);
    }

    /** 项目周期(天)：优先申报起止日，否则取里程碑最早开始至最晚结束 */
    private long resolveProjectCycleDays(Project project, List<ProjectProgress> milestones) {
        if (project.getDeclarationId() != null) {
            Declaration decl = declarationRepo.findById(project.getDeclarationId());
            if (decl != null && decl.getProjectStartTime() != null && decl.getProjectEndTime() != null) {
                return ChronoUnit.DAYS.between(decl.getProjectStartTime(), decl.getProjectEndTime());
            }
        }
        if (milestones.isEmpty()) {
            return 0;
        }
        LocalDate minStart = milestones.stream()
                .map(ProjectProgress::getMilestoneStartTime)
                .filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
        LocalDate maxEnd = milestones.stream()
                .map(ProjectProgress::getMilestoneEndTime)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
        if (minStart == null || maxEnd == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(minStart, maxEnd);
    }

    /** 产出：整个项目里程碑关联的附件数量（查 sys_attachment_relation） */
    private long countMilestoneAttachmentOutput(Long projectId, List<ProjectProgress> milestones) {
        if (milestones.isEmpty()) {
            return 0;
        }
        List<Long> milestoneIds = milestones.stream().map(ProjectProgress::getId).toList();
        Map<Long, List<Long>> relationMap = sysAttachmentRelationRepo.findAttachmentIdsByRelationIds(
                AttachmentRelationStatus.PROJECT.getCode(), milestoneIds);
        return relationMap.values().stream().mapToLong(List::size).sum();
    }

    /**
     * 非责任参与项目汇总图表：当前团队负责人作为「成员」参与（非负责人）的项目维度聚合
     *
     * @param managerId      当前团队负责人用户ID
     * @param currentTeamId  当前团队项目ID（排除，避免与当前项目重复）
     * @return 聚合后的效率图表数据，无此类项目时返回 null
     */
    private EfficiencyChartData buildNonLeadParticipantChartData(Long managerId, Long currentTeamId) {
        if (managerId == null) {
            return null;
        }
        List<Long> asMember = projectMemberRepo.findProjectIdsByUserId(managerId);
        List<Long> asManager = projectRepo.findProjectIdsByManagerId(managerId);
        List<Long> nonLeadIds = asMember.stream()
                .filter(id -> !asManager.contains(id) && !java.util.Objects.equals(id, currentTeamId))
                .distinct()
                .toList();
        if (nonLeadIds.isEmpty()) {
            return null;
        }
        List<String> x = List.of("完成率", "里程碑", "周期(天)", "产出");
        int count = nonLeadIds.size();
        int progressSum = 0;
        long completedMilestonesSum = 0;
        long cycleDaysSum = 0;
        long outputSum = 0;
        for (Long pid : nonLeadIds) {
            Project proj = projectRepo.findById(pid);
            if (proj == null) continue;
            progressSum += proj.getProgress() != null ? proj.getProgress() : 0;
            List<ProjectProgress> milestones = projectProgressRepo.findMilestonesByProjectId(pid);
            completedMilestonesSum += milestones.stream().filter(m -> m.getProgress() != null && m.getProgress() >= 100).count();
            cycleDaysSum += resolveProjectCycleDays(proj, milestones);
            outputSum += countMilestoneAttachmentOutput(pid, milestones);
        }
        List<Number> y = List.of(
                count > 0 ? progressSum / count : 0,
                completedMilestonesSum,
                count > 0 ? cycleDaysSum / count : 0,
                outputSum
        );
        return new EfficiencyChartData(x, y);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assign(IndustryEducationAssignReq req) {
        Declaration declaration = declarationRepo.findById(req.declarationId());
        if (declaration == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "申报不存在");
        }
        Project targetProject = projectRepo.findById(req.targetTeamId());
        if (targetProject == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "目标团队不存在");
        }
        Project existing = projectRepo.findByDeclarationId(req.declarationId());
        if (existing != null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "该申报已关联项目，请勿重复分发");
        }

        Long userId = LoginUserUtil.requireCurrentUser().userId();
        var now = LocalDateTime.now();

        Project entity = new Project();
        entity.setName(Optional.ofNullable(declaration.getResearchTopic()).filter(StringUtils::hasText)
                .orElse(declaration.getNumber() + " 配套项目"));
        entity.setDescription(declaration.getContentSummary());
        entity.setDeclarationId(declaration.getId());
        entity.setManagerId(targetProject.getManagerId());
        entity.setNumber(generateProjectNumber());
        entity.setStatus(String.valueOf(ProjectStatus.IN_PROGRESS.getCode()));
        entity.setProgress(0);
        entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);

        Long projectId = projectRepo.save(entity);
        if (projectId == null) {
            throw BusinessException.of(ResultCode.DATABASE_OPERATION_FAILED, "项目创建失败");
        }

        List<ProjectMember> sourceMembers = projectMemberRepo.findByProjectId(req.targetTeamId());
        if (!sourceMembers.isEmpty()) {
            List<ProjectMember> newMembers = sourceMembers.stream()
                    .map(m -> {
                        ProjectMember nm = new ProjectMember();
                        nm.setProjectId(projectId);
                        nm.setUserId(m.getUserId());
                        nm.setUserName(m.getUserName());
                        nm.setRole(m.getRole());
                        nm.setJoinTime(now);
                        nm.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
                        nm.setCreatedBy(userId);
                        nm.setUpdatedBy(userId);
                        nm.setCreatedTime(now);
                        nm.setUpdatedTime(now);
                        return nm;
                    })
                    .toList();
            projectMemberRepo.saveBatch(newMembers);
        }

        log.info("产教研分发成功: declarationId={}, targetTeamId={}, newProjectId={}", req.declarationId(), req.targetTeamId(), projectId);
        return projectId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long distribute(DistributeReq req) {
        Project targetProject = projectRepo.findById(req.targetTeamId());
        if (targetProject == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "目标团队不存在");
        }
        Long receiverId = targetProject.getManagerId();
        if (receiverId == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "目标团队无负责人，无法分发");
        }
        var currentUser = LoginUserUtil.requireCurrentUser();
        String senderName = currentUser.realName() != null ? currentUser.realName() : currentUser.username();
        String title = "科研项目分发";
        String content = String.format("%s 将以下科研项目分发给您：%s", senderName, req.topicLabel());
        Map<String, Object> extra = new HashMap<>();
        extra.put("topic_label", req.topicLabel());
        extra.put("target_team_id", req.targetTeamId());
        if (req.department() != null) extra.put("department", req.department());
        if (req.documentPublishTime() != null) extra.put("document_publish_time", req.documentPublishTime().toString());
        if (req.projectStartTime() != null) extra.put("project_start_time", req.projectStartTime().toString());
        if (req.projectEndTime() != null) extra.put("project_end_time", req.projectEndTime().toString());
        if (req.researchTopic() != null) extra.put("research_topic", req.researchTopic());
        if (req.researchDirection() != null) extra.put("research_direction", req.researchDirection());
        if (req.researchFields() != null && !req.researchFields().isEmpty()) extra.put("research_fields", req.researchFields());
        String extraJson;
        try {
            extraJson = objectMapper.writeValueAsString(extra);
        } catch (Exception e) {
            extraJson = "{}";
        }
        SysMessage message = new SysMessage();
        message.setType(MESSAGE_TYPE_DISTRIBUTE);
        message.setSenderId(currentUser.userId());
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setExtraJson(extraJson);
        message.setStatus(MESSAGE_STATUS_PENDING);
        Long messageId = messageService.createAndPush(message);
        log.info("产教研消息分发成功: messageId={}, topicLabel={}, targetTeamId={}, receiverId={}", messageId, req.topicLabel(), req.targetTeamId(), receiverId);
        return messageId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long acceptDistribute(Long messageId) {
        SysMessage message = messageMapper.selectById(messageId);
        if (message == null || message.getIsDeleted() != null && message.getIsDeleted().equals(DeleteStatus.DELETED.getCode())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "消息不存在");
        }
        Long currentUserId = LoginUserUtil.requireCurrentUserId();
        if (!currentUserId.equals(message.getReceiverId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "无权限操作该消息");
        }
        if (!MESSAGE_STATUS_PENDING.equals(message.getStatus())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "该消息已处理");
        }
        Map<String, Object> extra = parseExtraJson(message.getExtraJson());
        var currentUser = LoginUserUtil.requireCurrentUser();
        LocalDate today = LocalDate.now();

        DeclarationCreateReq createReq = buildDeclarationCreateReqFromExtra(extra, currentUser, today);
        Long declarationId = declarationService.create(createReq);

        extra.put("declaration_id", declarationId);
        String newExtraJson;
        try {
            newExtraJson = objectMapper.writeValueAsString(extra);
        } catch (Exception e) {
            newExtraJson = message.getExtraJson();
        }
        var now = LocalDateTime.now();
        message.setStatus(MESSAGE_STATUS_ACCEPTED);
        message.setExtraJson(newExtraJson);
        message.setUpdatedTime(now);
        message.setUpdatedBy(currentUser.userId());
        messageMapper.updateById(message);

        String topicLabel = createReq.researchTopic();
        String receiverName = currentUser.realName() != null ? currentUser.realName() : currentUser.username();
        String notifyTitle = "科研项目已接受";
        String notifyContent = String.format("%s 已接受科研项目：%s", receiverName, topicLabel);
        SysMessage notify = new SysMessage();
        notify.setType("industry_education_distribute_result");
        notify.setSenderId(currentUser.userId());
        notify.setReceiverId(message.getSenderId());
        notify.setTitle(notifyTitle);
        notify.setContent(notifyContent);
        notify.setStatus(MESSAGE_STATUS_PENDING);
        messageService.createAndPush(notify);

        log.info("产教研接受成功: messageId={}, declarationId={}", messageId, declarationId);
        return declarationId;
    }

    /**
     * 根据消息 extra 与当前用户构建申报创建请求（供接受分发时调用申报创建接口）
     */
    private DeclarationCreateReq buildDeclarationCreateReqFromExtra(Map<String, Object> extra, com.sciz.server.domain.pojo.dto.response.user.LoginUserContext currentUser, LocalDate today) {
        String topicLabel = parseTopicLabelFromExtra(extra);
        if (topicLabel == null || topicLabel.isBlank()) {
            topicLabel = "产教研分配项目";
        }
        String department = extra.containsKey("department") && extra.get("department") != null
                ? extra.get("department").toString() : "产教研分配";
        LocalDate documentPublishTime = parseLocalDateFromExtra(extra, "document_publish_time", today);
        LocalDate projectStartTime = parseLocalDateFromExtra(extra, "project_start_time", today);
        LocalDate projectEndTime = parseLocalDateFromExtra(extra, "project_end_time", today.plusYears(1));
        String researchTopic = extra.containsKey("research_topic") && extra.get("research_topic") != null
                ? extra.get("research_topic").toString() : topicLabel;
        String researchDirection = extra.containsKey("research_direction") && extra.get("research_direction") != null
                ? extra.get("research_direction").toString() : topicLabel;
        List<String> researchFields = parseResearchFieldsFromExtra(extra);
        String projectLeader = currentUser.realName() != null ? currentUser.realName() : currentUser.username();
        return new DeclarationCreateReq(
                department,
                projectLeader,
                currentUser.userId(),
                documentPublishTime,
                projectStartTime,
                projectEndTime,
                researchFields,
                researchDirection,
                researchTopic,
                "workflow_003"
        );
    }

    private String parseTopicLabelFromExtra(Map<String, Object> extra) {
        if (extra == null) return null;
        Object v = extra.get("topic_label");
        return v != null ? v.toString() : null;
    }

    private List<String> parseResearchFieldsFromExtra(Map<String, Object> extra) {
        if (extra == null || !extra.containsKey("research_fields")) {
            return Collections.emptyList();
        }
        Object raw = extra.get("research_fields");
        if (raw instanceof List) {
            return ((List<?>) raw).stream()
                    .map(o -> o != null ? o.toString() : "")
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectDistribute(Long messageId, DistributeRejectReq req) {
        SysMessage message = messageMapper.selectById(messageId);
        if (message == null || message.getIsDeleted() != null && message.getIsDeleted().equals(DeleteStatus.DELETED.getCode())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "消息不存在");
        }
        Long currentUserId = LoginUserUtil.requireCurrentUserId();
        if (!currentUserId.equals(message.getReceiverId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "无权限操作该消息");
        }
        if (!MESSAGE_STATUS_PENDING.equals(message.getStatus())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "该消息已处理");
        }
        String topicLabel = parseTopicLabelFromExtra(message.getExtraJson());
        if (topicLabel == null || topicLabel.isBlank()) {
            topicLabel = "科研项目";
        }
        var currentUser = LoginUserUtil.requireCurrentUser();

        Map<String, Object> extra = parseExtraJson(message.getExtraJson());
        extra.put("reject_reason", req.reason());
        String newExtraJson;
        try {
            newExtraJson = objectMapper.writeValueAsString(extra);
        } catch (Exception e) {
            newExtraJson = message.getExtraJson();
        }
        message.setStatus(MESSAGE_STATUS_REJECTED);
        message.setExtraJson(newExtraJson);
        message.setUpdatedTime(LocalDateTime.now());
        message.setUpdatedBy(currentUser.userId());
        messageMapper.updateById(message);

        String receiverName = currentUser.realName() != null ? currentUser.realName() : currentUser.username();
        String notifyTitle = "科研项目已拒绝";
        String notifyContent = String.format("%s 拒绝了科研项目「%s」，原因：%s", receiverName, topicLabel, req.reason());
        SysMessage notify = new SysMessage();
        notify.setType("industry_education_distribute_result");
        notify.setSenderId(currentUser.userId());
        notify.setReceiverId(message.getSenderId());
        notify.setTitle(notifyTitle);
        notify.setContent(notifyContent);
        notify.setStatus(MESSAGE_STATUS_PENDING);
        messageService.createAndPush(notify);

        log.info("产教研拒绝成功: messageId={}", messageId);
    }

    private String parseTopicLabelFromExtra(String extraJson) {
        if (extraJson == null || extraJson.isBlank()) return null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(extraJson, Map.class);
            Object v = map.get("topic_label");
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> parseExtraJson(String extraJson) {
        if (extraJson == null || extraJson.isBlank()) return new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(extraJson, Map.class);
            return map != null ? map : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private LocalDate parseLocalDateFromExtra(Map<String, Object> extra, String key, LocalDate defaultValue) {
        Object v = extra != null ? extra.get(key) : null;
        if (v == null || v.toString().isBlank()) return defaultValue;
        try {
            return LocalDate.parse(v.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String resolveLeaderName(Long managerId, Long declarationId) {
        if (managerId != null) {
            SysUser user = sysUserRepo.findById(managerId);
            if (user != null && user.getRealName() != null) {
                return user.getRealName();
            }
        }
        if (declarationId != null) {
            Declaration decl = declarationRepo.findById(declarationId);
            if (decl != null && StringUtils.hasText(decl.getProjectLeader())) {
                return decl.getProjectLeader();
            }
        }
        return null;
    }

    private String getProjectStatusDescription(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return ProjectStatus.fromCode(Integer.parseInt(status)).getDescription();
        } catch (Exception e) {
            return null;
        }
    }

    private static String generateProjectNumber() {
        return PROJECT_NUMBER_PREFIX + LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}
