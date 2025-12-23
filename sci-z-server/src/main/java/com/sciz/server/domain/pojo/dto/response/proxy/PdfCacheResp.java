package com.sciz.server.domain.pojo.dto.response.proxy;

import lombok.Builder;
import lombok.Data;

/**
 * PDF缓存响应
 *
 * @author System
 * @className PdfCacheResp
 * @date 2025-01-XX
 */
@Data
@Builder
public class PdfCacheResp {
    /**
     * 唯一id（用于查询、预览、删除）
     */
    private String cacheKey;

    /**
     * presignedUrl预览链接（使用presignedGetUrl生成）
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;
}
