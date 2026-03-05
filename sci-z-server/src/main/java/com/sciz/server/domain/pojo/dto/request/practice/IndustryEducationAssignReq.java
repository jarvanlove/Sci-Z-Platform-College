package com.sciz.server.domain.pojo.dto.request.practice;

import jakarta.validation.constraints.NotNull;

/**
 * 产教研智能体 - 分发请求
 *
 * @param declarationId 申报ID
 * @param targetTeamId  目标团队ID（即 projectId，将使用该项目的负责人与成员创建新项目）
 */
public record IndustryEducationAssignReq(
        @NotNull(message = "申报ID不能为空") Long declarationId,
        @NotNull(message = "目标团队ID不能为空") Long targetTeamId) {
}
