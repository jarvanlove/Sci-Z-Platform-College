package com.sciz.server.infrastructure.external.minio.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * MinIO上传响应DTO
 *
 * @author JiaWen.Wu
 * @className MinioUploadResp
 * @date 2025-10-29 10:30
 */
@Data
public class MinioUploadResp {

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 文件对象名称
     */
    private String objectName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * ETag
     */
    private String etag;

    /**
     * 版本ID
     */
    private String versionId;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;
}
