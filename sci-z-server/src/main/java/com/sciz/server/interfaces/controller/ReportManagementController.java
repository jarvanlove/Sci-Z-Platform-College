package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.report.ReportManagementService;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementCreateReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementListQueryReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementUpdateReq;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementDetailResp;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementListResp;
import com.sciz.server.domain.pojo.dto.response.report.ReportTypeResp;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.DifyApiKeyService;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报告管理控制器
 *
 * @author JiaWen.Wu
 * @className ReportManagementController
 * @date 2025-01-24 14:30
 */
@Tag(name = "报告管理", description = "报告管理相关接口")
@RestController
@RequestMapping("/api/report-management")
@RequiredArgsConstructor
public class ReportManagementController {

    private final ReportManagementService reportManagementService;
    private final DifyApiKeyService difyApiKeyService;

    /**
     * 创建报告管理
     *
     * @param req 创建请求
     * @return 报告ID
     */
    @Operation(summary = "创建报告管理", description = "创建新的报告管理记录")
    @PostMapping
    public Result<Long> createReportManagement(@Valid @RequestBody ReportManagementCreateReq req) {
        Long reportId = reportManagementService.create(req);
        return Result.success(reportId);
    }
    /**
     * 分页查询报告列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Operation(summary = "分页查询报告列表", description = "根据关键字、状态、类型分页查询报告列表")
    @GetMapping
    public Result<PageResult<ReportManagementListResp>> pageReportManagement(@Valid ReportManagementListQueryReq req) {
        PageResult<ReportManagementListResp> pageResult = reportManagementService.page(req);
        return Result.success(pageResult);
    }

    /**
     * 获取报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    @Operation(summary = "获取报告详情", description = "根据ID获取报告详细信息")
    @GetMapping("/{id}")
    public Result<ReportManagementDetailResp> getReportManagementDetail(@PathVariable Long id) {
        ReportManagementDetailResp resp = reportManagementService.findDetail(id);
        return Result.success(resp);
    }

    /**
     * 更新报告管理
     *
     * @param req 更新请求
     * @return 成功标识
     */
    @Operation(summary = "更新报告管理", description = "更新报告管理信息")
    @PutMapping
    public Result<Void> updateReportManagement(@Valid @RequestBody ReportManagementUpdateReq req) {
        reportManagementService.update(req);
        return Result.success();
    }

    /**
     * 删除报告管理
     *
     * @param id 报告ID
     * @return 成功标识
     */
    @Operation(summary = "删除报告管理", description = "根据ID删除报告管理（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteReportManagement(@PathVariable Long id) {
        reportManagementService.deleteById(id);
        return Result.success();
    }

    /**
     * 获取报告类型列表
     * 查询 key_type = 'workflow' 且 key_name 包含"报告"的密钥列表
     *
     * @return 报告类型列表
     */
    @Operation(summary = "查询报告工作流", description = "查询工作流类型且名称包含'报告'的密钥列表，用于动态获取报告类型选项")
    @GetMapping("/workflow")
    public Result<List<ReportTypeResp>> getReportTypes() {
        List<DifyApiKey> apiKeys = difyApiKeyService.getReportTypes();
        List<ReportTypeResp> reportTypes = apiKeys.stream()
                .map(apiKey -> {
                    ReportTypeResp resp = new ReportTypeResp();
                    resp.setId(apiKey.getId());
                    resp.setResourceId(apiKey.getResourceId());
                    resp.setKeyName(apiKey.getKeyName());
                    resp.setDescription(apiKey.getDescription());

                    return resp;
                })
                .toList();
        return Result.success(reportTypes);
    }
}

