package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.project.MilestoneDocumentDeleteReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectListQueryReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.project.MilestoneDocumentUploadResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectProgressResp;

import java.util.List;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectSelectResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectStatisticsResp;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 项目控制器
 *
 * @author JiaWen.Wu
 * @className ProjectController
 * @date 2025-01-24 16:00
 */
@Tag(name = "项目管理", description = "项目管理相关接口")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     *
     * @param req 创建请求
     * @return 项目ID
     */
    @Operation(summary = "创建项目", description = "创建新的科研项目")
    @PostMapping
    // @SaCheckPermission("api:project:create")
    public Result<Long> createProject(@Valid @RequestBody ProjectCreateReq req) {
        Long projectId = projectService.create(req);
        return Result.success(projectId);
    }

    /**
     * 分页查询项目列表
     *
     * @param req ProjectListQueryReq 查询请求
     * @return Result<PageResult<ProjectListResp>> 分页结果
     */
    @Operation(summary = "分页查询项目列表", description = "根据关键字、状态分页查询项目列表，支持按项目编号、项目名称搜索")
    @PostMapping("/list")
    public Result<PageResult<ProjectListResp>> pageProject(@Valid @RequestBody ProjectListQueryReq req) {
        var pageResult = projectService.page(req);
        return Result.success(pageResult);
    }

    /**
     * 获取项目详情
     *
     * @param id 项目ID
     * @return 项目详情
     */
    @Operation(summary = "获取项目详情", description = "根据ID获取项目详细信息")
    @GetMapping("/detail/{id}")
    public Result<ProjectDetailResp> getProjectDetail(@PathVariable Long id) {
        ProjectDetailResp resp = projectService.findDetail(id);
        return Result.success(resp);
    }

    /**
     * 更新项目
     *
     * @param id  项目ID
     * @param req 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新项目", description = "更新项目信息（包括基本信息、成员、里程碑）")
    @PutMapping("/update/{id}")
    // @SaCheckPermission("api:project:update")
    public Result<Void> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectUpdateReq req) {
        if (!id.equals(req.id())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "路径参数中的项目ID与请求体中的ID不一致");
        }
        projectService.update(req);
        return Result.success();
    }

    /**
     * 取消项目
     *
     * @param id 项目ID
     * @return 成功标识
     */
    @Operation(summary = "取消项目", description = "根据ID取消项目，将项目状态更新为已取消")
    @PutMapping("/{id}/cancel")
    // @SaCheckPermission("api:project:cancel")
    public Result<Void> cancelProject(@PathVariable Long id) {
        projectService.cancelById(id);
        return Result.success();
    }

    /**
     * 获取项目统计信息
     *
     * @return 项目统计信息（总项目数、进行中、已延期、已完成）
     */
    @Operation(summary = "获取项目统计信息", description = "获取项目统计数据：总项目数、进行中的项目数、已延期项目数、已完成项目数")
    @GetMapping("/statistics")
    public Result<ProjectStatisticsResp> getProjectStatistics() {
        var statistics = projectService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 批量上传里程碑文档
     *
     * @param req 批量文件上传请求
     * @return 文档上传响应列表
     */
    @Operation(summary = "批量上传里程碑文档", description = "批量上传里程碑文档，返回文件信息列表。前端传递的relationId为项目ID（此时还没有进度数据）")
    @PostMapping("/milestone/document")
    // @SaCheckPermission("api:project:milestone:document:upload")
    public Result<List<MilestoneDocumentUploadResp>> uploadMilestoneDocument(
            @Valid @ModelAttribute FileBatchUploadReq req) {
        var respList = projectService.uploadMilestoneDocument(req);
        return Result.success(respList);
    }

    /**
     * 删除里程碑文档
     *
     * @param req 删除请求（包含附件ID、项目ID、Dify文档ID）
     * @return 操作结果
     */
    @Operation(summary = "删除里程碑文档", description = "删除里程碑文档，同步删除关联表，异步并行删除 MinIO 文件和 Dify 知识库文档")
    @DeleteMapping("/milestone/document/delete")
    // @SaCheckPermission("api:project:milestone:document:delete")
    public Result<Void> deleteMilestoneDocument(@Valid @RequestBody MilestoneDocumentDeleteReq req) {
        projectService.deleteMilestoneDocument(req);
        return Result.success();
    }

    /**
     * 获取项目进度
     *
     * @param id 项目ID
     * @return 项目进度响应（包含项目基本信息、进度统计、整体进度、里程碑列表）
     */
    @Operation(summary = "获取项目进度", description = "获取项目进度信息，包括项目基本信息、进度统计（已完成、进行中、未开始、已延期）、整体进度（百分比、开始时间、预计完成时间）和里程碑列表（时间轴视图）")
    @GetMapping("/progress/{id}")
    public Result<ProjectProgressResp> getProjectProgress(@PathVariable Long id) {
        var resp = projectService.findProgress(id);
        return Result.success(resp);
    }

    /**
     * 完成里程碑
     *
     * @param milestoneId 里程碑ID
     * @return 操作结果
     */
    @Operation(summary = "完成里程碑", description = "手动完成里程碑，将里程碑进度设置为100%，支持提前完成")
    @PutMapping("/milestone/{milestoneId}/complete")
    // @SaCheckPermission("api:project:milestone:complete")
    public Result<Void> completeMilestone(@PathVariable Long milestoneId) {
        projectService.completeMilestone(milestoneId);
        return Result.success();
    }

    /**
     * 取消完成里程碑
     *
     * @param milestoneId 里程碑ID
     * @return 操作结果
     */
    @Operation(summary = "取消完成里程碑", description = "取消里程碑的完成状态，重新按时间自动计算进度")
    @PutMapping("/milestone/{milestoneId}/cancel-complete")
    // @SaCheckPermission("api:project:milestone:cancel-complete")
    public Result<Void> cancelCompleteMilestone(@PathVariable Long milestoneId) {
        projectService.cancelCompleteMilestone(milestoneId);
        return Result.success();
    }

    /**
     * 查询报告项目下拉框列表
     *
     * @return 项目下拉框列表（包含项目ID、编号、名称、状态描述、文档数量、文档总字数、项目进度）
     */
    @Operation(summary = "查询报告项目下拉框列表", description = "查询所有项目的下拉框数据，用于报告生成等场景的项目选择")
    @GetMapping("report/select")
    public Result<List<ProjectSelectResp>> getProjectSelectList() {
        var respList = projectService.findSelectList();
        return Result.success(respList);
    }
}
