package com.sciz.server.domain.pojo.dto.request.declaration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 申报创建请求
 *
 * @param department          String 课题发布部门
 * @param projectLeader       String 项目负责人
 * @param documentPublishTime LocalDate 红头文件发布时间
 * @param projectStartTime    LocalDate 项目开始时间
 * @param projectEndTime      LocalDate 项目结束时间
 * @param researchFields      List<String> 研究领域（最多10个）
 * @param researchDirection   String 研究方向（富文本）
 * @param researchTopic       String 研究课题
 * @param workflowId          String 工作流ID（工作流模板ID）
 * @author JiaWen.Wu
 * @className DeclarationCreateReq
 * @date 2025-01-20 15:00
 */
public record DeclarationCreateReq(
        @NotBlank(message = "课题发布部门不能为空") String department,

        @NotBlank(message = "项目负责人不能为空") String projectLeader,

        @NotNull(message = "红头文件发布时间不能为空") LocalDate documentPublishTime,

        @NotNull(message = "项目开始时间不能为空") LocalDate projectStartTime,

        @NotNull(message = "项目结束时间不能为空") LocalDate projectEndTime,

        List<String> researchFields,

        @NotBlank(message = "研究方向不能为空") String researchDirection,

        @NotBlank(message = "研究课题不能为空") String researchTopic,

        @NotBlank(message = "工作流ID不能为空") String workflowId) {
}
