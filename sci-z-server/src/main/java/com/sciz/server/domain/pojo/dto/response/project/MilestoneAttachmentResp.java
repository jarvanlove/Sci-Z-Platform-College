package com.sciz.server.domain.pojo.dto.response.project;

import java.time.LocalDateTime;

/**
 * 里程碑附件响应
 *
 * @param id            Long 附件ID
 * @param fileName      String 文件名称（存储名）
 * @param originalName  String 原始文件名（全名称，含扩展名）
 * @param fileType      String 文件类型（PDF、DOCX等）
 * @param fileExtension String 文件扩展名（如 pdf、docx）
 * @param fileSize      Long 文件大小（字节）
 * @param fileSizeLabel String 文件大小展示文本（如 27.71 KB / 3.20 MB）
 * @param uploaderId    Long 上传人用户ID（用于项目成员仅可删除自己上传的附件）
 * @param uploaderName  String 上传人姓名
 * @param uploadTime    LocalDateTime 上传时间
 * @param fileUrl       String 文件URL
 * @param difyDocId     String Dify文档ID（可选，如果附件未上传到Dify则为null）
 * @author JiaWen.Wu
 * @className MilestoneAttachmentResp
 * @date 2025-12-01 09:18
 */
public record MilestoneAttachmentResp(
        Long id,
        String fileName,
        String originalName,
        String fileType,
        String fileExtension,
        Long fileSize,
        String fileSizeLabel,
        Long uploaderId,
        String uploaderName,
        LocalDateTime uploadTime,
        String fileUrl,
        String difyDocId) {
}
