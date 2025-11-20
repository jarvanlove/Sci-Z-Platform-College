package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.declaration.DeclarationService;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationListQueryReq;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationDetailResp;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationListResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 申报控制器
 *
 * @author JiaWen.Wu
 * @className DeclarationController
 * @date 2025-01-20 15:00
 */
@Tag(name = "申报管理", description = "申报相关接口")
@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
public class DeclarationController {

    private final DeclarationService declarationService;

    /**
     * 创建申报
     *
     * @param req 创建请求
     * @return 申报ID
     */
    @Operation(summary = "创建申报", description = "创建新的申报申请，异步触发工作流生成申报书")
    @PostMapping
    public Result<Long> createDeclaration(@Valid @RequestBody DeclarationCreateReq req) {
        Long declarationId = declarationService.create(req);
        return Result.success(declarationId);
    }

    /**
     * 分页查询申报列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Operation(summary = "分页查询申报列表", description = "根据关键字和状态分页查询申报列表")
    @GetMapping
    public Result<PageResult<DeclarationListResp>> pageDeclarations(@Valid DeclarationListQueryReq req) {
        PageResult<DeclarationListResp> pageResult = declarationService.page(req);
        return Result.success(pageResult);
    }

    /**
     * 获取申报详情
     *
     * @param id 申报ID
     * @return 申报详情
     */
    @Operation(summary = "获取申报详情", description = "根据ID获取申报详细信息，包含工作流进度")
    @GetMapping("/{id}")
    public Result<DeclarationDetailResp> getDeclarationDetail(@PathVariable Long id) {
        DeclarationDetailResp resp = declarationService.findDetail(id);
        return Result.success(resp);
    }

    /**
     * 获取工作流状态
     *
     * @param id 申报ID
     * @return 工作流状态信息
     */
    @Operation(summary = "获取工作流状态", description = "查询申报工作流执行状态和进度")
    @GetMapping("/{id}/workflow/status")
    public Result<DeclarationDetailResp.WorkflowResult> getWorkflowStatus(@PathVariable Long id) {
        DeclarationDetailResp.WorkflowResult workflowResult = declarationService.getWorkflowStatus(id);
        return Result.success(workflowResult);
    }
}
