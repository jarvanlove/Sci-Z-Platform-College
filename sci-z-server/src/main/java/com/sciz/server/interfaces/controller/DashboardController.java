package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.dashboard.DashboardService;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardByDepartmentItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardByTypeItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardDelayWarningResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardStatusItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardTrendResp;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表板统计接口
 */
@Tag(name = "仪表板统计", description = "仪表板图表相关统计接口")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取申报 / 项目趋势统计", description = "按最近 6 个月统计申报与项目数量趋势")
    @GetMapping("/trend")
    public Result<DashboardTrendResp> getTrendStats() {
        var resp = dashboardService.getTrendStats();
        return Result.success(resp);
    }

    @Operation(summary = "获取申报状态分布", description = "按申报状态统计数量，用于仪表板申报状态分布图")
    @GetMapping("/declaration-status")
    public Result<List<DashboardStatusItemResp>> getDeclarationStatusStats() {
        var resp = dashboardService.getDeclarationStatusStats();
        return Result.success(resp);
    }

    @Operation(summary = "获取项目状态分布", description = "按项目状态统计数量，用于仪表板项目状态分布图")
    @GetMapping("/project-status")
    public Result<List<DashboardStatusItemResp>> getProjectStatusStats() {
        var resp = dashboardService.getProjectStatusStats();
        return Result.success(resp);
    }

    @Operation(summary = "按学院 / 团队统计项目数量", description = "基于项目关联申报的 department 字段统计学院 / 团队数量")
    @GetMapping("/by-department")
    public Result<List<DashboardByDepartmentItemResp>> getByDepartmentStats() {
        var resp = dashboardService.getByDepartmentStats();
        return Result.success(resp);
    }

    @Operation(summary = "按项目类型统计项目数量", description = "基于申报表 department 字段统计项目类型分布")
    @GetMapping("/by-type")
    public Result<List<DashboardByTypeItemResp>> getByTypeStats() {
        var resp = dashboardService.getByTypeStats();
        return Result.success(resp);
    }

    @Operation(summary = "获取项目延期预警统计", description = "统计已延期、即将到期项目数量，按风险等级分组")
    @GetMapping("/delay-warning")
    public Result<DashboardDelayWarningResp> getDelayWarningStats() {
        var resp = dashboardService.getDelayWarningStats();
        return Result.success(resp);
    }
}

