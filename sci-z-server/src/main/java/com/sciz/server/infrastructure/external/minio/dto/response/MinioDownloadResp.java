package com.sciz.server.infrastructure.external.minio.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * MinIO下载响应DTO
 *
 * @author JiaWen.Wu
 * @className MinioDownloadResp
 * @date 2025-10-29 10:30
 */
@Data
public class MinioDownloadResp {

    /**
     * 预签名URL
     */
    private String presignedUrl;

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
     * 最后修改时间
     */
    private LocalDateTime lastModified;

    /**
     * 过期时间
     */
    private LocalDateTime expires;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;
}
