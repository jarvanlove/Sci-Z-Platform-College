package com.sciz.server.domain.pojo.dto.response.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目详情响应
 *
 * @param id                Long 项目ID
 * @param number            String 项目编号
 * @param name              String 项目名称
 * @param description       String 项目描述
 * @param budget            BigDecimal 项目预算
 * @param progress          Integer 进度百分比
 * @param status            String 项目状态
 * @param statusDescription String 项目状态描述
 * @param projectLeader     String 项目负责人姓名（来自申报表）
 * @param projectLeaderId   Long 项目负责人用户ID（来自申报表/项目表，用于前端权限与下拉回显）
 * @param department        String 课题发布部门（来自申报表）
 * @param projectStartTime  LocalDate 项目开始时间（来自申报表）
 * @param projectEndTime    LocalDate 项目结束时间（来自申报表）
 * @param researchDirection String 研究方向（来自申报表）
 * @param createdBy         Long 项目创建人用户ID（用于前端仅对创建人显示添加/移除成员、里程碑完成/删除等按钮）
 * @param members           List<ProjectMemberResp> 项目成员列表
 * @param milestones        List<ProjectMilestoneResp> 项目里程碑列表
 * @author JiaWen.Wu
 * @className ProjectDetailResp
 * @date 2025-12-01 09:18
 */
public record ProjectDetailResp(
        Long id,
        String number,
        String name,
        String description,
        BigDecimal budget,
        Integer progress,
        String status,
        String statusDescription,
        String projectLeader,
        Long projectLeaderId,
        String department,
        LocalDate projectStartTime,
        LocalDate projectEndTime,
        String researchDirection,
        Long createdBy,
        List<ProjectMemberResp> members,
        List<ProjectMilestoneResp> milestones) {
}
