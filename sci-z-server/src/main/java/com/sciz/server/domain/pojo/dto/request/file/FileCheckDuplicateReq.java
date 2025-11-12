package com.sciz.server.domain.pojo.dto.request.file;

import jakarta.validation.constraints.NotBlank;
import org.springframework.util.StringUtils;

/**
 * 文件去重检查请求
 *
 * @param md5          String 文件MD5哈希
 * @param fileSize     Long 文件大小（字节，可选）
 * @param originalName String 原始文件名（可选）
 *
 * @author JiaWen.Wu
 * @className FileCheckDuplicateReq
 * @date 2025-11-11 17:20
 */
public record FileCheckDuplicateReq(
        @NotBlank(message = "文件MD5不能为空") String md5,
        Long fileSize,
        String originalName) {

    public FileCheckDuplicateReq {
        if (!StringUtils.hasText(md5)) {
            throw new IllegalArgumentException("文件MD5不能为空");
        }
        originalName = StringUtils.hasText(originalName) ? originalName : null;
    }
}
