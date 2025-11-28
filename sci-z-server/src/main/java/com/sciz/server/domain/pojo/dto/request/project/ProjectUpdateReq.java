package com.sciz.server.domain.pojo.dto.request.project;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

/**
 * 项目更新请求
 *
 * @param id              Long 项目ID（必填）
 * @param name            String 项目名称
 * @param description     String 项目描述
 * @param declarationId   Long 关联申报ID
 * @param budget          BigDecimal 项目预算
 * @param progress        Integer 进度百分比（0-100）
 * @param status          String 项目状态
 * @param difyKnowledgeId String Dify知识库ID
 * @param userId          Long 用户ID（可选，用于异步场景，如果不提供则从上下文获取）
 * @author JiaWen.Wu
 * @className ProjectUpdateReq
 * @date 2025-01-24 16:00
 */
public record ProjectUpdateReq(
                @NotNull(message = "项目ID不能为空") Long id,
                String name,
                String description,
                Long declarationId,
                @DecimalMin(value = "0.0", message = "项目预算不能为负数") BigDecimal budget,
                @Min(value = 0, message = "进度百分比最小为0") @Max(value = 100, message = "进度百分比最大为100") Integer progress,
                String status,
                String difyKnowledgeId,
                Long userId) {
}
