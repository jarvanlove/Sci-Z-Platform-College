package com.sciz.server.domain.pojo.dto.request.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目更新请求
 *
 * @param id              Long 项目ID（必填）
 * @param manager         String 项目负责人姓名（展示用；若传 managerId 则后端会据此更新姓名）
 * @param managerId       Long 项目负责人用户ID（可选，与下拉框选中用户一致时传此值）
 * @param startTime       LocalDate 项目开始时间
 * @param endTime         LocalDate 项目结束时间
 * @param budget          BigDecimal 项目预算
 * @param description     String 项目描述
 * @param status          String 项目状态
 * @param difyKnowledgeId String Dify知识库ID
 * @param members         List<ProjectMemberUpdateReq> 项目成员列表
 * @param milestones      List<ProjectMilestoneUpdateReq> 项目里程碑列表
 * @author JiaWen.Wu
 * @className ProjectUpdateReq
 * @date 2025-12-01 10:00
 */
public record ProjectUpdateReq(
        @NotNull(message = "项目ID不能为空") Long id,
        String manager,
        Long managerId,
        LocalDate startTime,
        LocalDate endTime,
        @DecimalMin(value = "0.0", message = "项目预算不能为负数") BigDecimal budget,
        String description,
        String status,
        String difyKnowledgeId,
        @Valid List<ProjectMemberUpdateReq> members,
        @Valid List<ProjectMilestoneUpdateReq> milestones) {
}
