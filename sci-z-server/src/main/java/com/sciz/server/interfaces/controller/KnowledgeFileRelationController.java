package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.knowledge.KnowledgeFileRelationService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileRelationResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库文件关联控制器
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationController
 * @date 2025-01-28 16:00
 */
@Tag(name = "知识库文件关联管理", description = "知识库文件关联相关接口")
@RestController
@RequestMapping("/api/knowledge/file-relation")
@RequiredArgsConstructor
public class KnowledgeFileRelationController {

    private final KnowledgeFileRelationService fileRelationService;

    /**
     * 创建知识库文件关联
     *
     * @param req 创建请求
     * @return 响应
     */
    @Operation(summary = "创建知识库文件关联", description = "创建新的知识库文件关联记录")
    @PostMapping
    public Result<KnowledgeFileRelationResp> createFileRelation(@Valid @RequestBody KnowledgeFileRelationCreateReq req) {
        KnowledgeFileRelationResp resp = fileRelationService.create(req);
        return Result.success(resp);
    }

    /**
     * 更新知识库文件关联
     *
     * @param id 关联ID
     * @param req 更新请求
     * @return 响应
     */
    @Operation(summary = "更新知识库文件关联", description = "更新知识库文件关联信息")
    @PutMapping("/{id}")
    public Result<KnowledgeFileRelationResp> updateFileRelation(
            @PathVariable String id,
            @Valid @RequestBody KnowledgeFileRelationUpdateReq req) {
        KnowledgeFileRelationResp resp = fileRelationService.update(id, req);
        return Result.success(resp);
    }

    /**
     * 删除知识库文件关联
     *
     * @param id 关联ID
     * @return 操作结果
     */
    @Operation(summary = "删除知识库文件关联", description = "删除知识库文件关联记录（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFileRelation(@PathVariable String id) {
        fileRelationService.delete(id);
        return Result.success();
    }

    /**
     * 查询知识库文件关联详情
     *
     * @param id 关联ID
     * @return 响应
     */
    @Operation(summary = "查询知识库文件关联详情", description = "根据ID查询知识库文件关联详细信息")
    @GetMapping("/{id}")
    public Result<KnowledgeFileRelationResp> getFileRelationDetail(@PathVariable String id) {
        KnowledgeFileRelationResp resp = fileRelationService.findDetail(id);
        return Result.success(resp);
    }

    /**
     * 分页查询知识库文件关联列表
     *
     * @param req 查询请求（支持单个knowledgeId或批量knowledgeIds查询）
     * @return 分页结果
     */
    @Operation(summary = "分页查询知识库文件关联列表", description = "根据知识库ID或知识库ID列表分页查询文件关联列表，支持单个或批量查询")
    @GetMapping
    public Result<PageResult<KnowledgeFileRelationResp>> pageFileRelations(KnowledgeFileRelationQueryReq req) {
        PageResult<KnowledgeFileRelationResp> result = fileRelationService.page(req);
        return Result.success(result);
    }

    
}

