package com.sciz.server.infrastructure.external.minio.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * MinIO删除响应DTO
 *
 * @author JiaWen.Wu
 * @className MinioDeleteResp
 * @date 2025-10-29 10:30
 */
@Data
public class MinioDeleteResp {

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 文件对象名称
     */
    private String objectName;

    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 删除的文件数量
     */
    private Integer deletedCount = 1;
}
