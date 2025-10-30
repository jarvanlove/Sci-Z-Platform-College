package com.server.agentbackendservices.modules.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Dify 文件上传响应 DTO
 *
 * @author shihang.shang
 * @className DifyFileUploadResponse
 * @date 2025-01-28 12:00
 */
@Data
@Schema(description = "Dify 文件上传响应结果")
public class DifyFileUploadResponse {

    @Schema(description = "文件ID")
    private String id;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "文件大小（字节）")
    private Long size;

    @Schema(description = "文件扩展名")
    private String extension;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "创建者ID")
    private String createdBy;

    @Schema(description = "创建时间")
    private Long createdAt;
}
