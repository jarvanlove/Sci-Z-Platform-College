package com.sciz.server.infrastructure.external.dify.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Dify聊天请求DTO
 *
 * @author JiaWen.Wu
 * @className DifyChatReq
 * @date 2025-10-29 10:30
 */
@Data
public class DifyChatReq {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;

    /**
     * 流式响应
     */
    private Boolean stream = false;

    /**
     * 查询参数
     */
    private String query;

    /**
     * 响应模式
     */
    private String responseMode = "blocking";
}
