package com.sciz.server.infrastructure.external.minio.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO上传请求DTO
 *
 * @author JiaWen.Wu
 * @className MinioUploadReq
 * @date 2025-10-29 10:30
 */
@Data
public class MinioUploadReq {

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
     * 上传文件
     */
    private MultipartFile file;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 是否公开访问
     */
    private Boolean isPublic = false;

    /**
     * 过期时间（秒）
     */
    private Integer expires = 3600;
}
