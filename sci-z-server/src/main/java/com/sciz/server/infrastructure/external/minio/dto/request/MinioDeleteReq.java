package com.sciz.server.infrastructure.external.minio.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * MinIO删除请求DTO
 *
 * @author JiaWen.Wu
 * @className MinioDeleteReq
 * @date 2025-10-29 10:30
 */
@Data
public class MinioDeleteReq {

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
     * 是否强制删除
     */
    private Boolean force = false;
}
