package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectListQueryReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
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
    @GetMapping("/{id}")
    public Result<ProjectDetailResp> getProjectDetail(@PathVariable Long id) {
        ProjectDetailResp resp = projectService.findDetail(id);
        return Result.success(resp);
    }

    /**
     * 更新项目
     *
     * @param req 更新请求
     * @return 成功标识
     */
    @Operation(summary = "更新项目", description = "更新项目信息")
    @PutMapping
    // @SaCheckPermission("api:project:update")
    public Result<Void> updateProject(@Valid @RequestBody ProjectUpdateReq req) {
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

}
