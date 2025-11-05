package com.sciz.server.infrastructure.external.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Dify Workflow 响应 DTO
 *
 * @author shihang.shang
 * @className DifyWorkflowResponse
 * @date 2025-01-28 10:30
 */
@Data
@Schema(description = "Dify Workflow 响应结果")
public class DifyWorkflowResponse {

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "工作流运行ID")
    private String workflowRunId;

    @Schema(description = "工作流数据")
    private WorkflowData data;

    @Data
    @Schema(description = "工作流数据详情")
    public static class WorkflowData {

        @Schema(description = "工作流运行ID")
        private String id;

        @Schema(description = "工作流ID")
        private String workflowId;

        @Schema(description = "执行状态")
        private String status;

        @Schema(description = "输出结果")
        private WorkflowOutputs outputs;

        @Schema(description = "错误信息")
        private String error;

        @Schema(description = "执行时间（秒）")
        private Double elapsedTime;

        @Schema(description = "总Token数")
        private Integer totalTokens;

        @Schema(description = "总步骤数")
        private Integer totalSteps;

        @Schema(description = "创建时间")
        private Long createdAt;

        @Schema(description = "完成时间")
        private Long finishedAt;
    }

    @Data
    @Schema(description = "工作流输出结果")
    public static class WorkflowOutputs {

        @Schema(description = "生成的文件列表")
        private List<WorkflowFile> files;

        @Schema(description = "文本输出")
        private String text;

        @Schema(description = "JSON输出")
        private List<Map<String, Object>> json;
    }

    @Data
    @Schema(description = "工作流生成的文件")
    public static class WorkflowFile {

        @Schema(description = "文件ID")
        private String id;

        @Schema(description = "文件名")
        private String filename;

        @Schema(description = "文件扩展名")
        private String extension;

        @Schema(description = "MIME类型")
        private String mimeType;

        @Schema(description = "文件大小（字节）")
        private Long size;

        @Schema(description = "文件URL")
        private String url;
    }
}
