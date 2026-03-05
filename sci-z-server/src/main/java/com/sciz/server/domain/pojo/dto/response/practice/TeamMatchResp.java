package com.sciz.server.domain.pojo.dto.response.practice;

/**
 * 产教研智能体 - 匹配团队项
 *
 * @param teamId                   团队ID（即 projectId）
 * @param teamName                 团队名称（项目名称）
 * @param leaderName               负责人姓名
 * @param memberCount              成员数
 * @param participantProjectCount  参与项目数量：当前项目负责人参与过的所有项目总数（作为负责人或成员）
 * @param efficiencyPlaceholder    效率指标占位，如 "—" 或 "平均周期：—"
 */
public record TeamMatchResp(
        Long teamId,
        String teamName,
        String leaderName,
        int memberCount,
        int participantProjectCount,
        String efficiencyPlaceholder) {
}
