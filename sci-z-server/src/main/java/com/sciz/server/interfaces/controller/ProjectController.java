package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author JiaWen.Wu
 * @className ProjectController
 * @date 2025-10-29 10:00
 */
@Tag(name = "项目控制器", description = "项目相关接口")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "创建项目", description = "创建新的科研项目")
    @PostMapping
    public Result<Void> createProject(@RequestBody Object request) {
        // TODO: 实现项目创建逻辑
        return Result.success();
    }

    @Operation(summary = "获取项目列表", description = "分页获取项目列表")
    @GetMapping
    public Result<Object> getProjects(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 实现项目列表查询逻辑
        return Result.success();
    }

    @Operation(summary = "获取项目详情", description = "根据ID获取项目详细信息")
    @GetMapping("/{id}")
    public Result<Object> getProject(@PathVariable Long id) {
        // TODO: 实现项目详情查询逻辑
        return Result.success();
    }

    @Operation(summary = "更新项目", description = "更新项目信息")
    @PutMapping("/{id}")
    public Result<Void> updateProject(@PathVariable Long id, @RequestBody Object request) {
        // TODO: 实现项目更新逻辑
        return Result.success();
    }

    @Operation(summary = "删除项目", description = "删除指定项目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        // TODO: 实现项目删除逻辑
        return Result.success();
    }
}
