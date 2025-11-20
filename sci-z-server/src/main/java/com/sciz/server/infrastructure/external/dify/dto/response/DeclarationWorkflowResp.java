package com.sciz.server.infrastructure.external.dify.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 申报工作流响应 DTO
 *
 * @param taskId        String 任务ID
 * @param workflowRunId String 工作流运行ID
 * @param data          WorkflowData 工作流数据
 * @author JiaWen.Wu
 * @className DeclarationWorkflowResp
 * @date 2025-01-20 15:00
 */
@Schema(description = "申报工作流响应结果")
public record DeclarationWorkflowResp(
        @Schema(description = "任务ID") String taskId,

        @Schema(description = "工作流运行ID") @JsonProperty("workflow_run_id") String workflowRunId,

        @Schema(description = "工作流数据") WorkflowData data) {

    /**
     * 工作流数据详情
     *
     * @param id          String 工作流运行ID
     * @param workflowId  String 工作流ID
     * @param status      String 执行状态
     * @param outputs     WorkflowOutputs 输出结果
     * @param error       String 错误信息
     * @param elapsedTime Double 执行时间（秒）
     * @param totalTokens Integer 总Token数
     * @param totalSteps  Integer 总步骤数
     * @param createdAt   Long 创建时间
     * @param finishedAt  Long 完成时间
     */
    @Schema(description = "工作流数据详情")
    public record WorkflowData(
            @Schema(description = "工作流运行ID") String id,

            @Schema(description = "工作流ID") @JsonProperty("workflow_id") String workflowId,

            @Schema(description = "执行状态") String status,

            @Schema(description = "输出结果") WorkflowOutputs outputs,

            @Schema(description = "错误信息") String error,

            @Schema(description = "执行时间（秒）") @JsonProperty("elapsed_time") Double elapsedTime,

            @Schema(description = "总Token数") @JsonProperty("total_tokens") Integer totalTokens,

            @Schema(description = "总步骤数") @JsonProperty("total_steps") Integer totalSteps,

            @Schema(description = "创建时间") @JsonProperty("created_at") Long createdAt,

            @Schema(description = "完成时间") @JsonProperty("finished_at") Long finishedAt) {
    }

    /**
     * 工作流输出结果
     *
     * @param files List<WorkflowFile> 生成的文件列表
     * @param text  String 文本输出
     * @param json  List<Map<String, Object>> JSON输出
     */
    @Schema(description = "工作流输出结果")
    public record WorkflowOutputs(
            @Schema(description = "生成的文件列表") List<WorkflowFile> files,

            @Schema(description = "文本输出") String text,

            @Schema(description = "JSON输出") List<Map<String, Object>> json) {
    }

    /**
     * 工作流生成的文件
     *
     * @param id        String 文件ID
     * @param filename  String 文件名
     * @param extension String 文件扩展名
     * @param mimeType  String MIME类型
     * @param size      Long 文件大小（字节）
     * @param url       String 文件URL（用于下载）
     */
    @Schema(description = "工作流生成的文件")
    public record WorkflowFile(
            @Schema(description = "文件ID") String id,

            @Schema(description = "文件名") String filename,

            @Schema(description = "文件扩展名") String extension,

            @Schema(description = "MIME类型") @JsonProperty("mime_type") String mimeType,

            @Schema(description = "文件大小（字节）") Long size,

            @Schema(description = "文件URL（用于下载）") String url) {
    }
}
