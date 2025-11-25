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
     * 分页查询项目列表（兼容旧接口）
     *
     * @param page    Integer 页码（旧参数，兼容）
     * @param size    Integer 每页数量（旧参数，兼容）
     * @param keyword String 搜索关键字
     * @param status  String 项目状态（支持 active 字符串，兼容旧接口）
     * @return 分页结果
     */
    @Operation(summary = "分页查询项目列表（兼容接口）", description = "根据关键字、状态分页查询项目列表，兼容旧的参数格式")
    @GetMapping("/list")
    public Result<PageResult<ProjectListResp>> pageProjectList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        // 兼容旧参数格式：page/size -> pageNo/pageSize
        Integer pageNo = page != null ? page : 1;
        Integer pageSize = size != null ? size : 10;
        
        // 处理 status=active 的情况（转换为进行中状态）
        String normalizedStatus = normalizeStatus(status);
        
        // 构建查询请求
        var req = new ProjectListQueryReq(pageNo, pageSize, null, "DESC", keyword, normalizedStatus);
        PageResult<ProjectListResp> pageResult = projectService.page(req);
        return Result.success(pageResult);
    }

    /**
     * 分页查询项目列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Operation(summary = "分页查询项目列表", description = "根据关键字、状态分页查询项目列表")
    @GetMapping
    public Result<PageResult<ProjectListResp>> pageProject(@Valid ProjectListQueryReq req) {
        // 处理 status=active 的情况
        if (req.status() != null && "active".equalsIgnoreCase(req.status())) {
            var normalizedReq = new ProjectListQueryReq(
                    req.pageNo(), req.pageSize(), req.sortBy(), req.sortOrder(),
                    req.keyword(), "3"); // 3 = 进行中
            PageResult<ProjectListResp> pageResult = projectService.page(normalizedReq);
            return Result.success(pageResult);
        }
        PageResult<ProjectListResp> pageResult = projectService.page(req);
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

    // ==================== 私有方法 ====================

    /**
     * 规范化项目状态
     * 将前端传入的状态字符串转换为后端识别的状态值
     *
     * @param status String 原始状态值
     * @return String 规范化后的状态值（null 或状态码字符串）
     */
    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String trimmed = status.trim();
        // 兼容旧接口：active -> 3（进行中）
        if ("active".equalsIgnoreCase(trimmed)) {
            return "3";
        }
        // 其他状态值直接返回（0-7）
        return trimmed;
    }
}
