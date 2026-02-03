package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.knowledge.KnowledgeFileRelationService;
import com.sciz.server.application.service.knowledge.KnowledgeFolderService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationUpdateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileRelationResp;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFolderResp;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFolderWithFilesResp;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final KnowledgeFolderService knowledgeFolderService;

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
     * @param req 查询请求
     * @return 分页结果
     */
    @Operation(summary = "分页查询知识库文件关联列表", description = "根据知识库ID分页查询文件关联列表")
    @GetMapping
    public Result<PageResult<KnowledgeFileRelationResp>> pageFileRelations(KnowledgeFileRelationQueryReq req) {
        PageResult<KnowledgeFileRelationResp> result = fileRelationService.page(req);
        return Result.success(result);
    }

    /**
     * 分页查询知识库文件夹及文件列表（树形结构）
     *
     * @param knowledgeId 知识库ID
     * @param page 页码（从1开始，默认1）
     * @param size 每页大小（默认10）
     * @return 文件夹及文件列表（分页，只返回有文件的文件夹，没有绑定文件夹的文档显示在"未分类"文件夹中）
     */
    @Operation(summary = "分页查询知识库文件夹及文件列表", description = "根据知识库ID和文件夹ID分页查询文件夹及文件列表（混合分页：文件夹在前，文件在后）。根目录时返回按文件夹分组的结构，文件夹内时返回混合列表")
    @GetMapping("/knowledge/{knowledgeId}/folders-files")
    public Result<PageResult<KnowledgeFolderWithFilesResp>> listFoldersWithFiles(
            @PathVariable Long knowledgeId,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        PageResult<KnowledgeFolderWithFilesResp> result = fileRelationService.listFoldersWithFiles(knowledgeId, folderId, page, size);
        return Result.success(result);
    }

    // ==================== 文件夹管理接口 ====================

    @Operation(summary = "获取文件夹树", description = "获取知识库文件夹树")
    @GetMapping("/knowledge/{id}/folders/tree")
    public Result<List<KnowledgeFolderResp>> getFolderTree(@PathVariable Long id) {
        List<KnowledgeFolderResp> tree = knowledgeFolderService.getFolderTree(id);
        return Result.success(tree);
    }

    @Operation(summary = "获取文件夹列表", description = "根据知识库ID和父文件夹ID获取文件夹列表")
    @GetMapping("/knowledge/{id}/folders")
    public Result<List<KnowledgeFolderResp>> listFolders(
            @PathVariable Long id,
            @RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId) {
        List<KnowledgeFolderResp> folders = knowledgeFolderService.listByParentId(id, parentId);
        return Result.success(folders);
    }

    @Operation(summary = "创建文件夹", description = "在知识库创建文件夹，parentId为0表示根目录（知识库本身）。请求体中的knowledgeId必须与路径参数中的id一致")
    @PostMapping("/knowledge/{id}/folders")
    public Result<KnowledgeFolderResp> createFolder(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeFolderCreateReq req) {
        // 校验路径参数中的知识库ID与请求体中的知识库ID必须一致
        if (req.getKnowledgeId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "知识库ID不能为空");
        }
        if (!req.getKnowledgeId().equals(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "路径参数中的知识库ID与请求体中的知识库ID不一致");
        }
        KnowledgeFolderResp resp = knowledgeFolderService.create(req);
        return Result.success(resp);
    }

    @Operation(summary = "获取文件夹详情", description = "根据文件夹ID获取文件夹详细信息")
    @GetMapping("/folders/{folderId}")
    public Result<KnowledgeFolderResp> getFolderDetail(@PathVariable Long folderId) {
        KnowledgeFolderResp resp = knowledgeFolderService.findDetail(folderId);
        return Result.success(resp);
    }

    @Operation(summary = "更新文件夹", description = "更新文件夹信息")
    @PutMapping("/folders/{folderId}")
    public Result<KnowledgeFolderResp> updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody KnowledgeFolderUpdateReq req) {
        KnowledgeFolderResp resp = knowledgeFolderService.update(folderId, req);
        return Result.success(resp);
    }    @Operation(summary = "删除文件夹", description = "删除指定文件夹（如果文件夹下有子文件夹则无法删除）")
    @DeleteMapping("/folders/{folderId}")
    public Result<Void> deleteFolder(@PathVariable Long folderId) {
        knowledgeFolderService.delete(folderId);
        return Result.success();
    }
}
