package com.sciz.server.domain.pojo.dto.response.practice;

import java.util.List;

/**
 * 产教研智能体 - 团队关联项目项（用于团队项目介绍：项目名、状态、负责人、成员、效率图表、荣誉）
 *
 * @param projectId         项目ID
 * @param projectName       项目名称
 * @param statusDesc        状态描述，如 "进行中"、"已完成"
 * @param leader            项目负责人（姓名、学院、职务）
 * @param members           项目成员列表（姓名、学院、职务）
 * @param efficiencyChartData 效率图表数据（完成率/里程碑等）
 * @param honors            荣誉列表（占位，可后续接业务）
 */
public record TeamProjectResp(
        Long projectId,
        String projectName,
        String statusDesc,
        ProjectLeaderResp leader,
        List<TeamMemberResp> members,
        EfficiencyChartData efficiencyChartData,
        List<HonorItem> honors) {

    /** 兼容旧版：仅基础三字段 */
    public static TeamProjectResp ofBasic(Long projectId, String projectName, String statusDesc) {
        return new TeamProjectResp(projectId, projectName, statusDesc, null, null, null, null);
    }
}
