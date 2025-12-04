package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.project.MilestoneDocumentDeleteReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectListQueryReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.project.MilestoneDocumentUploadResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;

import java.util.List;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
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
     * 删除项目
     *
     * @param id 项目ID
     * @return 成功标识
     */
    @Operation(summary = "删除项目", description = "根据ID删除项目（软删除）")
    @DeleteMapping("/{id}")
    // @SaCheckPermission("api:project:delete")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteById(id);
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
    @Operation(summary = "批量上传里程碑文档", description = "批量上传里程碑文档，返回文件信息列表。relationId为项目ID，relationName由前端传递")
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
    @DeleteMapping("/milestone/document")
    // @SaCheckPermission("api:project:milestone:document:delete")
    public Result<Void> deleteMilestoneDocument(@Valid @RequestBody MilestoneDocumentDeleteReq req) {
        projectService.deleteMilestoneDocument(req);
        return Result.success();
    }

}
