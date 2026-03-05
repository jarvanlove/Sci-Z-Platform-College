package com.sciz.server.domain.pojo.dto.response.practice;

import java.util.List;

/**
 * 产教研智能体 - 团队明细（团队项目介绍）
 *
 * @param teamId                      团队ID（即 projectId）
 * @param teamName                    团队名称
 * @param leaderName                  负责人姓名（兼容）
 * @param members                     成员列表（兼容）
 * @param projects                    关联项目列表，每项含负责人、成员、效率图表、荣誉
 * @param efficiencyPlaceholder       效率指标占位（兼容）
 * @param honorsPlaceholder           荣誉占位（兼容）
 * @param efficiencyChartData         团队级效率指标图表数据（底部「效率指标」用）
 * @param nonLeadParticipantChartData 非责任参与项目汇总图表（负责人作为成员参与的项目维度聚合，可为 null）
 */
public record TeamDetailResp(
        Long teamId,
        String teamName,
        String leaderName,
        List<TeamMemberResp> members,
        List<TeamProjectResp> projects,
        String efficiencyPlaceholder,
        String honorsPlaceholder,
        EfficiencyChartData efficiencyChartData,
        EfficiencyChartData nonLeadParticipantChartData) {
}
