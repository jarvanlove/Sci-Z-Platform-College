package com.sciz.server.domain.pojo.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

/**
 * 项目创建请求
 *
 * @param name            String 项目名称
 * @param description     String 项目描述
 * @param declarationId   Long 关联申报ID（可选）
 * @param budget          BigDecimal 项目预算（可选）
 * @param progress        Integer 进度百分比（0-100，可选）
 * @param status          String 项目状态（可选，默认进行中）
 * @param difyKnowledgeId String Dify知识库ID（可选）
 * @author JiaWen.Wu
 * @className ProjectCreateReq
 * @date 2025-11-24 16:00
 */
public record ProjectCreateReq(
                @NotBlank(message = "项目名称不能为空") String name,
                String description,
                Long declarationId,
                @DecimalMin(value = "0.0", message = "项目预算不能为负数") BigDecimal budget,
                @Min(value = 0, message = "进度百分比最小为0") @Max(value = 100, message = "进度百分比最大为100") Integer progress,
                String status,
                String difyKnowledgeId) {
}
