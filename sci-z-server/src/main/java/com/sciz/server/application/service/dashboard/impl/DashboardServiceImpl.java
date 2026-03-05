package com.sciz.server.application.service.dashboard.impl;

import com.sciz.server.application.service.dashboard.DashboardService;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardByDepartmentItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardByTypeItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardDelayWarningResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardDelayWarningResp.RiskLevelItem;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardDelayWarningResp.UpcomingDeadlineItem;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardStatusItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardTrendResp;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 仪表板统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DeclarationRepo declarationRepo;
    private final ProjectRepo projectRepo;

    @Override
    public DashboardTrendResp getTrendStats() {
        // 最近 6 个月（含本月），按时间顺序排列
        var months = new ArrayList<YearMonth>();
        var current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }

        List<String> x = months.stream()
                .map(m -> String.format(Locale.ROOT, "%02d", m.getMonthValue()))
                .toList();

        List<Long> declarationSeries = new ArrayList<>();
        List<Long> projectSeries = new ArrayList<>();

        for (YearMonth month : months) {
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

            Long declarationCount = declarationRepo.countBySubmitTimeBetween(start, end);
            Long projectCount = projectRepo.countByCreatedTimeBetween(start, end);

            declarationSeries.add(declarationCount != null ? declarationCount : 0L);
            projectSeries.add(projectCount != null ? projectCount : 0L);
        }

        return new DashboardTrendResp(x, declarationSeries, projectSeries);
    }

    @Override
    public List<DashboardStatusItemResp> getDeclarationStatusStats() {
        Map<String, Long> statusMap = declarationRepo.countByStatus();
        return statusMap.entrySet().stream()
                .map(entry -> new DashboardStatusItemResp(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardStatusItemResp::status))
                .toList();
    }

    @Override
    public List<DashboardStatusItemResp> getProjectStatusStats() {
        List<DashboardStatusItemResp> result = new ArrayList<>();

        // 使用 ProjectStatus 枚举中的所有状态（1=进行中, 2=已完成, 3=已延期, 4=已取消）
        // 注意：0=未开始 的项目通常不显示在仪表板
        String[] statuses = {
                ProjectStatus.IN_PROGRESS.getCode().toString(),      // 1 - 进行中
                ProjectStatus.COMPLETED.getCode().toString(),        // 2 - 已完成
                ProjectStatus.DELAYED.getCode().toString(),          // 3 - 已延期
                ProjectStatus.CANCELLED.getCode().toString()         // 4 - 已取消
        };

        for (String status : statuses) {
            Long count = projectRepo.countByStatus(status);
            result.add(new DashboardStatusItemResp(status, count != null ? count : 0L));
        }

        return result;
    }

    @Override
    public List<DashboardByDepartmentItemResp> getByDepartmentStats() {
        // 基于当前用户可见的项目列表进行聚合
        List<Project> projects = projectRepo.findAll();
        if (projects.isEmpty()) {
            return List.of();
        }

        List<Long> declarationIds = projects.stream()
                .map(Project::getDeclarationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Declaration> declarationMap = declarationRepo.findByIds(declarationIds);

        Map<String, Long> deptCountMap = projects.stream()
                .map(project -> declarationMap.get(project.getDeclarationId()))
                .filter(Objects::nonNull)
                .map(Declaration::getDepartment)
                .filter(dept -> dept != null && !dept.isBlank())
                .collect(Collectors.groupingBy(dept -> dept, Collectors.counting()));

        return deptCountMap.entrySet().stream()
                .map(entry -> new DashboardByDepartmentItemResp(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardByDepartmentItemResp::departmentName))
                .toList();
    }

    @Override
    public List<DashboardByTypeItemResp> getByTypeStats() {
        // 基于当前用户可见项目关联的申报表 department 字段统计项目类型
        List<Project> projects = projectRepo.findAll();
        if (projects.isEmpty()) {
            return List.of();
        }

        List<Long> declarationIds = projects.stream()
                .map(Project::getDeclarationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Declaration> declarationMap = declarationRepo.findByIds(declarationIds);

        Map<String, Long> typeCountMap = projects.stream()
                .map(project -> declarationMap.get(project.getDeclarationId()))
                .filter(Objects::nonNull)
                .map(Declaration::getDepartment)
                .filter(type -> type != null && !type.isBlank())
                .collect(Collectors.groupingBy(type -> type, Collectors.counting()));

        return typeCountMap.entrySet().stream()
                .map(entry -> new DashboardByTypeItemResp(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardByTypeItemResp::type))
                .toList();
    }

    @Override
    public DashboardDelayWarningResp getDelayWarningStats() {
        log.info("开始获取项目延期预警统计...");
        
        // 获取所有项目
        List<Project> projects = projectRepo.findAll();
        log.info("查询到项目数量: {}", projects.size());
        
        if (projects.isEmpty()) {
            return new DashboardDelayWarningResp(
                    List.of(
                            new RiskLevelItem(1, 0L, "已延期"),
                            new RiskLevelItem(2, 0L, "7天内到期"),
                            new RiskLevelItem(3, 0L, "30天内到期"),
                            new RiskLevelItem(4, 0L, "正常")
                    ),
                    0L,
                    0L,
                    List.of(
                            new UpcomingDeadlineItem("1-7天", 0L),
                            new UpcomingDeadlineItem("8-14天", 0L),
                            new UpcomingDeadlineItem("15-30天", 0L),
                            new UpcomingDeadlineItem("30天以上", 0L)
                    )
            );
        }

        // 获取项目关联的申报信息（包含项目结束时间）
        List<Long> declarationIds = projects.stream()
                .map(Project::getDeclarationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Declaration> declarationMap = declarationRepo.findByIds(declarationIds);

        LocalDate today = LocalDate.now();

        // 风险等级统计
        long highRiskCount = 0;    // 已延期
        long mediumRiskCount = 0;  // 7天内到期
        long lowRiskCount = 0;     // 30天内到期
        long normalCount = 0;      // 正常

        // 即将到期分布统计
        long oneToSevenDays = 0;      // 1-7天
        long eightToFourteenDays = 0; // 8-14天
        long fifteenToThirtyDays = 0; // 15-30天
        long overThirtyDays = 0;       // 30天以上

        String completedCode = ProjectStatus.COMPLETED.getCode().toString();
        String delayedCode = ProjectStatus.DELAYED.getCode().toString();
        log.info("项目状态编码 - 已完成: {}, 已延期: {}", completedCode, delayedCode);

        for (Project project : projects) {
            Declaration declaration = declarationMap.get(project.getDeclarationId());
            if (declaration == null || declaration.getProjectEndTime() == null) {
                continue;
            }

            LocalDate endTime = declaration.getProjectEndTime();
            long daysUntilDeadline = ChronoUnit.DAYS.between(today, endTime);
            String projectStatus = project.getStatus();
            
            log.debug("项目分析 - ID: {}, 状态: {}, 结束时间: {}, 距离截止天数: {}", 
                    project.getId(), projectStatus, endTime, daysUntilDeadline);

            // 情况1：项目已明确标记为延期状态
            if (delayedCode.equals(projectStatus)) {
                log.info("项目标记为延期状态 - ID: {}", project.getId());
                highRiskCount++;
            }
            // 情况2：结束时间已过（且不是已完成状态）
            else if (daysUntilDeadline < 0 && !completedCode.equals(projectStatus)) {
                log.info("项目已过期 - ID: {}, 过期天数: {}", project.getId(), Math.abs(daysUntilDeadline));
                highRiskCount++;
            }
            // 7天内到期（高风险）
            else if (daysUntilDeadline >= 0 && daysUntilDeadline <= 7) {
                mediumRiskCount++;
                oneToSevenDays++;
            }
            // 8-14天到期
            else if (daysUntilDeadline >= 8 && daysUntilDeadline <= 14) {
                lowRiskCount++;
                eightToFourteenDays++;
            }
            // 15-30天到期
            else if (daysUntilDeadline >= 15 && daysUntilDeadline <= 30) {
                lowRiskCount++;
                fifteenToThirtyDays++;
            }
            // 30天以上
            else if (daysUntilDeadline > 30) {
                normalCount++;
                overThirtyDays++;
            }
        }

        List<RiskLevelItem> riskLevels = List.of(
                new RiskLevelItem(1, highRiskCount, "已延期"),
                new RiskLevelItem(2, mediumRiskCount, "7天内到期"),
                new RiskLevelItem(3, lowRiskCount, "30天内到期"),
                new RiskLevelItem(4, normalCount, "正常")
        );

        List<UpcomingDeadlineItem> upcomingDeadlines = List.of(
                new UpcomingDeadlineItem("1-7天", oneToSevenDays),
                new UpcomingDeadlineItem("8-14天", eightToFourteenDays),
                new UpcomingDeadlineItem("15-30天", fifteenToThirtyDays),
                new UpcomingDeadlineItem("30天以上", overThirtyDays)
        );

        log.info("延期预警统计结果 - 已延期: {}, 7天内到期: {}, 30天内到期: {}, 正常: {}",
                highRiskCount, mediumRiskCount, lowRiskCount, normalCount);

        return new DashboardDelayWarningResp(
                riskLevels,
                highRiskCount,
                mediumRiskCount + highRiskCount,
                upcomingDeadlines
        );
    }
}

