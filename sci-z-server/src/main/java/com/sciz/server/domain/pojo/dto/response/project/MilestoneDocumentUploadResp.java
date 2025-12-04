package com.sciz.server.domain.pojo.dto.response.project;

import java.time.LocalDateTime;

/**
 * 里程碑文档上传响应
 * <p>
 * 注意：前端可根据附件ID调用文件服务的预览、下载、删除接口
 *
 * @param id            Long 附件ID（前端用于调用文件服务接口）
 * @param fileName      String 文件名称（存储名）
 * @param originalName  String 原始文件名（全名称，含扩展名）
 * @param fileType      String 文件类型（PDF、DOCX等）
 * @param fileExtension String 文件扩展名（如 pdf、docx）
 * @param fileSize      Long 文件大小（字节）
 * @param fileSizeLabel String 文件大小展示文本（如 27.71 KB / 3.20 MB）
 * @param uploaderName  String 上传人姓名
 * @param uploadTime    LocalDateTime 上传时间
 * @param fileUrl       String 文件URL
 * @param previewUrl    String 预览URL（文件服务返回的预览链接）
 * @author JiaWen.Wu
 * @className MilestoneDocumentUploadResp
 * @date 2025-12-01 10:00
 */
public record MilestoneDocumentUploadResp(
                Long id,
                String fileName,
                String originalName,
                String fileType,
                String fileExtension,
                Long fileSize,
                String fileSizeLabel,
                String uploaderName,
                LocalDateTime uploadTime,
                String fileUrl,
                String previewUrl) {
}
