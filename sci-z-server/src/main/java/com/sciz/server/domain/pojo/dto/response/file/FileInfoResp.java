package com.sciz.server.domain.pojo.dto.response.file;

import java.time.LocalDateTime;

/**
 * 文件信息响应
 *
 * @param id            Long 附件ID
 * @param fileName      String 存储文件名
 * @param originalName  String 原始文件名
 * @param fileType      String 文件类型
 * @param fileExtension String 扩展名
 * @param fileSize      Long 文件大小（字节）
 * @param mimeType      String MIME 类型
 * @param fileUrl       String 文件访问路径
 * @param filePath      String 存储路径
 * @param md5Hash       String 文件 MD5
 * @param isPublic      Integer 是否公开
 * @param downloadCount Integer 下载次数
 * @param uploaderId    Long 上传人 ID
 * @param uploaderName  String 上传人姓名
 * @param uploadTime    LocalDateTime 上传时间
 * @param bucketName    String 存储桶名称
 * @param previewUrl    String 预览链接
 *
 * @author JiaWen.Wu
 * @className FileInfoResp
 * @date 2025-11-11 17:25
 */
public record FileInfoResp(
        Long id,
        String fileName,
        String originalName,
        String fileType,
        String fileExtension,
        Long fileSize,
        String mimeType,
        String fileUrl,
        String filePath,
        String md5Hash,
        Integer isPublic,
        Integer downloadCount,
        Long uploaderId,
        String uploaderName,
        LocalDateTime uploadTime,
        String bucketName,
        String previewUrl) {

    /**
     * 设置存储桶名称
     */
    public FileInfoResp withBucketName(String newBucketName) {
        return new FileInfoResp(id, fileName, originalName, fileType, fileExtension, fileSize, mimeType, fileUrl,
                filePath, md5Hash, isPublic, downloadCount, uploaderId, uploaderName, uploadTime, newBucketName,
                previewUrl);
    }

    /**
     * 设置预览链接
     */
    public FileInfoResp withPreviewUrl(String newPreviewUrl) {
        return new FileInfoResp(id, fileName, originalName, fileType, fileExtension, fileSize, mimeType, fileUrl,
                filePath, md5Hash, isPublic, downloadCount, uploaderId, uploaderName, uploadTime, bucketName,
                newPreviewUrl);
    }
}
