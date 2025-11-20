package com.sciz.server.infrastructure.external.dify.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * 申报工作流请求 DTO
 *
 * @param userId       Long 用户ID
 * @param resourceId   String 资源ID（工作流ID）
 * @param keyType      String 密钥类型
 * @param inputs       Map<String, Object> 工作流输入参数
 * @param responseMode String 响应模式（blocking：阻塞式，等待工作流完成）
 * @param user         String 用户标识
 * @author JiaWen.Wu
 * @className DeclarationWorkflowReq
 * @date 2025-01-20 15:00
 */
@Schema(description = "申报工作流请求参数")
public record DeclarationWorkflowReq(
        @Schema(description = "用户ID", required = true, example = "admin") @NotNull(message = "用户ID不能为空") Long userId,

        @Schema(description = "资源ID（工作流ID）", required = true, example = "workflow_001") @NotNull(message = "资源ID不能为空") @NotEmpty(message = "资源ID不能为空") String resourceId,

        @Schema(description = "密钥类型", required = true, example = "workflow", allowableValues = {
                "dataset", "workflow" }) @NotNull(message = "密钥类型不能为空") String keyType,

        @Schema(description = "工作流输入参数", required = true) @NotNull(message = "输入参数不能为null") @NotEmpty(message = "输入参数不能为空") Map<String, Object> inputs,

        @Schema(description = "响应模式", example = "blocking", allowableValues = { "blocking",
                "streaming" }) @JsonProperty("response_mode") String responseMode,

        @Schema(description = "用户标识", example = "admin") String user){

    /**
     * 使用默认值创建
     */
    public DeclarationWorkflowReq {
        if (responseMode == null || responseMode.isEmpty()) {
            responseMode = "blocking";
        }
        if (user == null || user.isEmpty()) {
            user = "admin";
        }
    }
}
