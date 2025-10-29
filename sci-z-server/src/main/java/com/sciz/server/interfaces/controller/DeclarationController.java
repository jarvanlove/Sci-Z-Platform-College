package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.declaration.DeclarationService;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author JiaWen.Wu
 * @className DeclarationController
 * @date 2025-01-27 10:00
 */
@Tag(name = "申报控制器", description = "申报相关接口")
@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
public class DeclarationController {

    private final DeclarationService declarationService;

    @Operation(summary = "提交申报", description = "提交新的申报申请")
    @PostMapping
    public Result<Void> submitDeclaration(@RequestBody Object request) {
        // TODO: 实现申报提交逻辑
        return Result.success();
    }

    @Operation(summary = "获取申报列表", description = "分页获取申报列表")
    @GetMapping
    public Result<Object> getDeclarations(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 实现申报列表查询逻辑
        return Result.success();
    }

    @Operation(summary = "获取申报详情", description = "根据ID获取申报详细信息")
    @GetMapping("/{id}")
    public Result<Object> getDeclaration(@PathVariable Long id) {
        // TODO: 实现申报详情查询逻辑
        return Result.success();
    }

    @Operation(summary = "更新申报", description = "更新申报信息")
    @PutMapping("/{id}")
    public Result<Void> updateDeclaration(@PathVariable Long id, @RequestBody Object request) {
        // TODO: 实现申报更新逻辑
        return Result.success();
    }

    @Operation(summary = "审核申报", description = "审核申报申请")
    @PutMapping("/{id}/review")
    public Result<Void> reviewDeclaration(@PathVariable Long id, @RequestBody Object request) {
        // TODO: 实现申报审核逻辑
        return Result.success();
    }
}
