package com.server.agentbackendservices.modules.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Dify API 基础请求类
 * 包含所有 Dify API 请求的通用参数
 *
 * @author shihang.shang
 * @className BaseDifyRequest
 * @date 2025-01-28 15:00
 */
@Data
@Schema(description = "Dify API 基础请求参数")
public class BaseDifyRequest {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", required = true, example = "admin")
    private String userId;

    /**
     * 资源ID（知识库ID或工作流ID）
     */
    @Schema(description = "资源ID", required = true, example = "knowledge_base_001")
    private String resourceId;

    /**
     * 密钥类型
     */
    @Schema(description = "密钥类型", required = true, example = "dataset", allowableValues = {"dataset", "workflow"})
    private String keyType;
}
