package com.sciz.server.domain.pojo.dto.response.dashboard;

import java.util.List;

/**
 * 仪表板趋势统计响应
 *
 * @param x                 横轴刻度（例如月份字符串）
 * @param declarationSeries 申报数量序列
 * @param projectSeries     项目数量序列
 */
public record DashboardTrendResp(
        List<String> x,
        List<Long> declarationSeries,
        List<Long> projectSeries) {
}

