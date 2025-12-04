package com.sciz.server.domain.pojo.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目里程碑更新请求
 *
 * @param id          Long 里程碑ID（更新时必填，新增时为null）
 * @param name        String 里程碑名称
 * @param description String 描述
 * @param startTime   LocalDate 开始时间
 * @param endTime     LocalDate 结束时间
 * @param documents   List<MilestoneDocumentUpdateReq> 文档列表（附件ID列表）
 * @author JiaWen.Wu
 * @className ProjectMilestoneUpdateReq
 * @date 2025-12-01 10:00
 */
public record ProjectMilestoneUpdateReq(
        Long id,
        @NotBlank(message = "里程碑名称不能为空") String name,
        String description,
        LocalDate startTime,
        LocalDate endTime,
        List<MilestoneDocumentUpdateReq> documents) {
}
