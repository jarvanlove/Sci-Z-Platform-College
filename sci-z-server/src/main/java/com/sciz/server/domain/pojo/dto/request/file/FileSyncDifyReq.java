package com.sciz.server.domain.pojo.dto.request.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件同步到 Dify 请求
 *
 * @param file       MultipartFile 上传文件
 * @param resourceId String 资源ID（工作流ID或数据集ID）
 * @param keyType    String 密钥类型（workflow/dataset/file）
 * @author JiaWen.Wu
 * @className FileSyncDifyReq
 * @date 2025-01-26 10:00
 */
public record FileSyncDifyReq(
        @NotNull(message = "上传文件不能为空") MultipartFile file,
        @NotBlank(message = "资源ID不能为空") String resourceId,
        @NotBlank(message = "密钥类型不能为空") String keyType) {
}
