package com.sciz.server.domain.pojo.dto.request.project;

import jakarta.validation.constraints.NotNull;

/**
 * 删除里程碑文档请求
 *
 * @param attachmentId Long 附件ID（必填）
 * @param projectId    Long 项目ID（必填，避免查询）
 * @param difyDocId    String Dify文档ID（可选，如果为空则跳过Dify删除）
 * @author JiaWen.Wu
 * @className MilestoneDocumentDeleteReq
 * @date 2025-12-01 14:00
 */
public record MilestoneDocumentDeleteReq(
        @NotNull(message = "附件ID不能为空") Long attachmentId,
        @NotNull(message = "项目ID不能为空") Long projectId,
        String difyDocId) {
}
