package com.sciz.server.domain.pojo.dto.response.project;

/**
 * 项目统计响应
 *
 * @param totalProjects   Long 总项目数
 * @param inProgressCount Long 进行中的项目数
 * @param delayedCount    Long 已延期项目数
 * @param completedCount  Long 已完成项目数
 * @author JiaWen.Wu
 * @className ProjectStatisticsResp
 * @date 2025-12-01 09:18
 */
public record ProjectStatisticsResp(
                Long totalProjects,
                Long inProgressCount,
                Long delayedCount,
                Long completedCount) {
}
