package com.sciz.server.domain.pojo.dto.response.dashboard;

import java.util.List;

/**
 * 项目延期预警响应
 *
 * @param riskLevels 风险等级分布（高风险、中风险、低风险、正常）
 * @param totalDelayed 已延期项目总数
 * @param totalAtRisk 有风险项目总数
 * @param upcomingDeadlines 即将到期项目数量（按天数分组）
 */
public record DashboardDelayWarningResp(
        List<RiskLevelItem> riskLevels,
        Long totalDelayed,
        Long totalAtRisk,
        List<UpcomingDeadlineItem> upcomingDeadlines) {

    /**
     * 风险等级分布项
     *
     * @param level 风险等级（1=高风险/已延期, 2=中风险/7天内到期, 3=低风险/30天内到期, 4=正常）
     * @param count 项目数量
     * @param name 等级名称（用于前端展示）
     */
    public record RiskLevelItem(Integer level, Long count, String name) {
    }

    /**
     * 即将到期项目分布
     *
     * @param dayRange 天数范围（如"1-7天", "8-14天", "15-30天", "30天以上"）
     * @param count 项目数量
     */
    public record UpcomingDeadlineItem(String dayRange, Long count) {
    }
}
