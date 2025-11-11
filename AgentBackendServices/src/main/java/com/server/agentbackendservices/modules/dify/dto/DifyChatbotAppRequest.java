package com.server.agentbackendservices.modules.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建 Dify Chatbot 应用请求
 *
 * @author
 * @since 2025-11-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Dify Chatbot 应用创建请求")
public class DifyChatbotAppRequest extends BaseDifyRequest {

    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Schema(description = "应用名称", example = "test")
    private String name;

    /**
     * 应用模式
     */
    @NotBlank(message = "应用模式不能为空")
    @Schema(description = "应用模式", example = "chat")
    private String mode;

    /**
     * 应用描述
     */
    @Schema(description = "应用描述", example = "testte")
    private String description;

    /**
     * 图标内容
     */
    @Schema(description = "应用图标", example = "🤖")
    private String icon;

    /**
     * 图标背景色
     */
    @Schema(description = "图标背景颜色", example = "#FFEAD5")
    private String icon_background;

    /**
     * 图标类型
     */
    @Schema(description = "图标类型", example = "emoji")
    private String icon_type;
}

