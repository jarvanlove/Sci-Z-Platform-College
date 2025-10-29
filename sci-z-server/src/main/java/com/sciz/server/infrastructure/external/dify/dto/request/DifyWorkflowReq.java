package com.sciz.server.infrastructure.external.dify.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Dify工作流请求DTO
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowReq
 * @date 2025-10-29 10:30
 */
@Data
public class DifyWorkflowReq {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 工作流ID
     */
    @NotBlank(message = "工作流ID不能为空")
    private String workflowId;

    /**
     * 输入参数
     */
    private Map<String, Object> inputs;

    /**
     * 响应模式
     */
    private String responseMode = "blocking";

    /**
     * 流式响应
     */
    private Boolean stream = false;

    /**
     * 文件列表
     */
    private String[] files;
}
