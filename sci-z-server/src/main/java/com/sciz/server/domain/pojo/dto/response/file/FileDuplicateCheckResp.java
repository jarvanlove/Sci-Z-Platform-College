package com.sciz.server.domain.pojo.dto.response.file;

/**
 * 文件去重检查响应
 *
 * @param exists   boolean 是否存在重复文件
 * @param fileInfo FileInfoResp 重复文件信息
 *
 * @author JiaWen.Wu
 * @className FileDuplicateCheckResp
 * @date 2025-11-11 17:25
 */
public record FileDuplicateCheckResp(
        boolean exists,
        FileInfoResp fileInfo) {
}
