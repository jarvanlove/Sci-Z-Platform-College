package com.server.agentbackendservices.modules.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Dify 工作流日志响应 DTO
 *
 * @author shihang.shang
 * @className DifyWorkflowLogsResponse
 * @date 2025-01-28 11:30
 */
@Data
@Schema(description = "Dify 工作流日志响应")
public class DifyWorkflowLogsResponse {

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer limit;

    @Schema(description = "总数量")
    private Integer total;

    @Schema(description = "是否有更多数据")
    private Boolean hasMore;

    @Schema(description = "日志数据列表")
    private List<WorkflowLogItem> data;

    @Data
    @Schema(description = "工作流日志项")
    public static class WorkflowLogItem {

        @Schema(description = "日志ID")
        private String id;

        @Schema(description = "工作流运行信息")
        private WorkflowRunInfo workflowRun;

        @Schema(description = "创建来源")
        private String createdFrom;

        @Schema(description = "创建者角色")
        private String createdByRole;

        @Schema(description = "创建者账户ID")
        private String createdByAccount;

        @Schema(description = "创建者终端用户信息")
        private CreatedByEndUser createdByEndUser;

        @Schema(description = "创建时间")
        private Long createdAt;
    }

    @Data
    @Schema(description = "工作流运行信息")
    public static class WorkflowRunInfo {

        @Schema(description = "运行ID")
        private String id;

        @Schema(description = "版本")
        private String version;

        @Schema(description = "状态")
        private String status;

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
    @Schema(description = "创建者终端用户信息")
    public static class CreatedByEndUser {

        @Schema(description = "用户ID")
        private String id;

        @Schema(description = "用户类型")
        private String type;

        @Schema(description = "是否匿名")
        private Boolean isAnonymous;

        @Schema(description = "会话ID")
        private String sessionId;
    }
}
