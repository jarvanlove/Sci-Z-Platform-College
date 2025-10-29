package com.sciz.server.infrastructure.external.dify.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Dify工作流响应DTO
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowResp
 * @date 2025-10-29 10:30
 */
@Data
public class DifyWorkflowResp {

    /**
     * 工作流运行ID
     */
    private String workflowRunId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 数据
     */
    private Map<String, Object> data;

    /**
     * 输出
     */
    private Map<String, Object> outputs;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime finishedAt;

    /**
     * 执行时间（毫秒）
     */
    private Long elapsedTime;
}
