package com.sciz.server.domain.pojo.dto.response.project;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目里程碑响应
 *
 * @param id            Long 里程碑ID
 * @param milestoneName String 里程碑名称（title）
 * @param description   String 描述（content）
 * @param startTime     LocalDate 开始时间
 * @param endTime       LocalDate 结束时间
 * @param attachments   List<MilestoneAttachmentResp> 附件列表
 * @author JiaWen.Wu
 * @className ProjectMilestoneResp
 * @date 2025-12-01 09:18
 */
public record ProjectMilestoneResp(
        Long id,
        String milestoneName,
        String description,
        LocalDate startTime,
        LocalDate endTime,
        List<MilestoneAttachmentResp> attachments) {
}
