package com.sciz.server.interfaces.controller;
import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeChatbotStreamReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeListQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileUploadResp;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 知识库控制器
 *
 * @author ShiHang.Shang
 * @className KnowledgeController
 * @date 2025-01-28 14:30
 */
@Tag(name = "知识库管理", description = "知识库相关接口")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    /**
     * 创建知识库
     *
     * @param req 创建请求
     * @return 知识库响应
     */
    @Operation(summary = "创建知识库", description = "创建新的知识库，调用Dify API创建数据集并保存到数据库")
    @PostMapping
    public Result<KnowledgeResp> createKnowledgeBase(@Valid @RequestBody KnowledgeCreateReq req) {
        KnowledgeResp resp = knowledgeService.create(req);
        return Result.success(resp);
    }
    /**
     * 分页获取知识库列表
     *
     * @param page 页码
     * @param size 页大小
     * @return 知识库分页结果
     */
    @Operation(summary = "获取知识库列表", description = "分页获取知识库列表，如果用户已登录则只返回该用户的知识库")
    @GetMapping
    public Result<PageResult<KnowledgeResp>> pageKnowledgeBases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {
        PageResult<KnowledgeResp> result = knowledgeService.page(page, size);
        return Result.success(result);
    }

    /**
     * 分页查询知识库列表（支持关键字搜索）
     *
     * @param req 查询请求（包含分页参数和关键字）
     * @return 知识库分页结果
     */
    @Operation(summary = "分页查询知识库列表", description = "分页查询知识库列表，支持关键字模糊搜索（知识库名称/描述）")
    @PostMapping("/list")
    public Result<PageResult<KnowledgeResp>> pageKnowledgeBasesWithKeyword(@Valid @RequestBody KnowledgeListQueryReq req) {
        PageResult<KnowledgeResp> result = knowledgeService.pageKnowledgeBases(req);
        return Result.success(result);
    }

    @Operation(summary = "获取知识库详情", description = "根据ID获取知识库详细信息，含可见性校验（本人/项目成员/他人公开个人知识库）")
    @GetMapping("/{id}")
    public Result<KnowledgeResp> getKnowledgeBase(@PathVariable Long id) {
        KnowledgeResp resp = knowledgeService.getById(id);
        return Result.success(resp);
    }
    /**
     * 上传文件到知识库（支持单个文件）
     *
     * @param id 知识库ID
     * @param file 上传的文件
     * @param folderId 文件夹ID（可选，0为根目录）
     * @return 操作结果
     */
    @Operation(summary = "上传文件", description = "向知识库上传单个文件，调用Dify API上传文档并保存关联关系")
    @PostMapping("/{id}/upload")
    public Result<Void> uploadFile(
            @PathVariable int id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false, defaultValue = "0") Long folderId) {
        knowledgeService.uploadFile(id, file, folderId);
        return Result.success();
    }

    /**
     * 上传多个文件到知识库（支持多文件上传，支持部分成功）
     *
     * @param id 知识库ID
     * @param files 上传的文件列表
     * @param folderId 文件夹ID（可选，0为根目录）
     * @return 每个文件的上传结果列表（包含成功和失败的详细信息）
     */
    @Operation(summary = "批量上传文件", description = "向知识库批量上传文件，支持异步分批上传，调用Dify API上传文档并保存关联关系。支持部分成功，返回每个文件的上传结果")
    @PostMapping("/{id}/upload/batch")
    public Result<List<KnowledgeFileUploadResp>> uploadFiles(
            @PathVariable int id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folderId", required = false, defaultValue = "0") Long folderId) {
        List<KnowledgeFileUploadResp> results = knowledgeService.uploadFiles(id, files, folderId);
        return Result.success(results);
    }
    /**
     * 基于知识库的 Chatbot 流式对话
     *
     * @param req 流式对话请求
     * @return 流式响应（SSE格式）
     */
    @Operation(summary = "知识库Chatbot流式对话", description = "基于知识库的Chatbot流式问答，如果用户未则返回提示")
    @PostMapping(value = "/chatbot/stream", produces = "text/event-stream")
    public SseEmitter chatbotStream(@Valid @RequestBody KnowledgeChatbotStreamReq req) {
        return knowledgeService.chatbotStream(req);
    }

    @Operation(summary = "搜索知识库", description = "在知识库中搜索内容")
    @GetMapping("/{id}/search")
    public Result<Object> searchKnowledge(@PathVariable Long id, @RequestParam String query) {
        // TODO: 实现知识库搜索逻辑
        return Result.success();
    }

    @Operation(summary = "获取文件列表", description = "获取知识库文件列表")
    @GetMapping("/{id}/files")
    public Result<Object> listFiles(@PathVariable Long id) {
        return Result.success();
    }

    @Operation(summary = "删除文件", description = "删除知识库文件")
    @DeleteMapping("/files/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        return Result.success();
    }

    @Operation(summary = "重命名文件", description = "重命名知识库文件")
    @PutMapping("/files/rename/{fileId}")
    public Result<Void> renameFile(@PathVariable Long fileId, @RequestBody Object request) {
        return Result.success();
    }

    @Operation(summary = "移动文件", description = "移动知识库文件")
    @PutMapping("/files/move/{fileId}")
    public Result<Void> moveFile(@PathVariable Long fileId, @RequestBody Object request) {
        return Result.success();
    }

    @Operation(summary = "删除知识库", description = "删除指定知识库，同时调用 Dify API 删除数据集")
    @DeleteMapping("/{id}")
    public Result<Void> deleteKnowledgeBase(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }

    /**
     * 更新知识库
     *
     * @param id  知识库ID
     * @param req 更新请求
     * @return 知识库响应
     */
    @Operation(summary = "更新知识库", description = "更新知识库基本信息（名称、描述、项目关联、共享状态）")
    @PutMapping("/{id}")
    public Result<KnowledgeResp> updateKnowledgeBase(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeUpdateReq req) {
        KnowledgeResp resp = knowledgeService.update(id, req);
        return Result.success(resp);
    }

    /**
     * 上传知识库封面
     *
     * @param id   知识库ID
     * @param file 封面图片文件
     * @return 知识库响应（包含更新后的封面信息）
     */
    @Operation(summary = "上传知识库封面", description = "上传知识库封面图片，建议尺寸 200x200px")
    @PostMapping("/{id}/cover")
    public Result<KnowledgeResp> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        KnowledgeResp resp = knowledgeService.uploadCover(id, file);
        return Result.success(resp);
    }
}
