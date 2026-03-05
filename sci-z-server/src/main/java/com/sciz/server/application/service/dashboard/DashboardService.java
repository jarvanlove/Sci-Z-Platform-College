package com.sciz.server.application.service.dashboard;

import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardByDepartmentItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardByTypeItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardDelayWarningResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardStatusItemResp;
import com.sciz.server.domain.pojo.dto.response.dashboard.DashboardTrendResp;
import java.util.List;

/**
 * 仪表板统计服务
 */
public interface DashboardService {

    /**
     * 获取申报 / 项目趋势统计（按最近 6 个月）
     */
    DashboardTrendResp getTrendStats();

    /**
     * 获取申报状态分布
     */
    List<DashboardStatusItemResp> getDeclarationStatusStats();

    /**
     * 获取项目状态分布
     */
    List<DashboardStatusItemResp> getProjectStatusStats();

    /**
     * 按学院 / 团队统计项目数量
     */
    List<DashboardByDepartmentItemResp> getByDepartmentStats();

    /**
     * 按项目类型统计项目数量（基于申报表 department 字段）
     */
    List<DashboardByTypeItemResp> getByTypeStats();

    /**
     * 获取项目延期预警统计
     */
    DashboardDelayWarningResp getDelayWarningStats();
}

