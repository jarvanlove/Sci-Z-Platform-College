package com.sciz.server.infrastructure.external.minio.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * MinIO下载请求DTO
 *
 * @author JiaWen.Wu
 * @className MinioDownloadReq
 * @date 2025-10-29 10:30
 */
@Data
public class MinioDownloadReq {

    /**
     * 存储桶名称
     */
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    /**
     * 文件对象名称
     */
    @NotBlank(message = "文件对象名称不能为空")
    private String objectName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 过期时间（秒）
     */
    private Integer expires = 3600;

    /**
     * 响应内容类型
     */
    private String responseContentType;

    /**
     * 响应内容处置
     */
    private String responseContentDisposition;
}
