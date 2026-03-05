package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.declaration.DeclarationService;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationListQueryReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationUpdateStatusReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationDetailResp;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationListResp;
import com.sciz.server.domain.pojo.dto.response.declaration.RedHeaderFileParseResp;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 申报控制器
 *
 * @author JiaWen.Wu
 * @className DeclarationController
 * @date 2025-01-20 15:00
 */
@Tag(name = "申报管理", description = "申报相关接口")
@RestController
@RequestMapping("/api/declaration")
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
    @PostMapping("/create")
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
    @PostMapping("/list")
    public Result<PageResult<DeclarationListResp>> pageDeclarations(@Valid @RequestBody DeclarationListQueryReq req) {
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
    @GetMapping("/detail/{id}")
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
    @Operation(summary = "获取申报书工作流状态", description = "查询申报工作流执行状态和进度")
    @GetMapping("/{id}/workflow/status")
    public Result<DeclarationDetailResp.WorkflowResult> getWorkflowStatus(@PathVariable Long id) {
        DeclarationDetailResp.WorkflowResult workflowResult = declarationService.getWorkflowStatus(id);
        return Result.success(workflowResult);
    }

    /**
     * 红头文件上传
     *
     * @param req 文件上传请求
     * @return 红头文件解析响应（包含研究领域、研究方向、研究课题）
     */
    @Operation(summary = "红头文件上传", description = "上传红头文件到 MinIO 和 Dify，执行工作流解析，返回解析结果（研究领域、研究方向、研究课题）")
    @PostMapping("/red-header-file/upload")
    public Result<RedHeaderFileParseResp> uploadRedHeaderFile(@Valid @ModelAttribute FileUploadReq req) {
        RedHeaderFileParseResp resp = declarationService.uploadRedHeaderFile(req);
        return Result.success(resp);
    }

    /**
     * 更新申报状态
     *
     * @param id  申报ID
     * @param req 更新状态请求
     * @return 操作结果
     */
    @Operation(summary = "更新申报状态", description = "更新申报状态，当状态更新为申报成功时，会自动创建项目和该项目的知识库")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody DeclarationUpdateStatusReq req) {
        if (!id.equals(req.id())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "路径参数中的申报ID与请求体中的ID不一致");
        }
        declarationService.updateStatus(req);
        return Result.success();
    }

    /**
     * 上传/覆盖申报书文档
     * 每次上传覆盖该申报当前关联的申报书附件，下载时获取最新文件。
     */
    @Operation(summary = "申报书编辑上传", description = "上传申报书文件，覆盖原申报书附件；下载时获取当前上传的文件")
    @PostMapping("/{id}/document/upload")
    public Result<FileInfoResp> uploadDeclarationDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        FileInfoResp resp = declarationService.uploadDeclarationDocument(id, file);
        return Result.success(resp);
    }
}
