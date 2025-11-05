package com.server.agentbackendservices.modules.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dify 文件上传请求 DTO
 *
 * @author shihang.shang
 * @className DifyFileUploadRequest
 * @date 2025-01-28 12:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Dify 文件上传请求参数")
public class DifyFileUploadRequest extends BaseDifyRequest {

    /**
     * 用户标识
     */
    @Schema(description = "用户标识", required = true, example = "workflow_user_001")
    private String user;

    /**
     * 上传的文件
     */
    @Schema(description = "上传的文件", required = true)
    private MultipartFile file;
}