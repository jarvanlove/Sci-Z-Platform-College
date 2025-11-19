package com.sciz.server.interfaces.controller;
import com.sciz.server.application.service.knowledge.KnowledgeService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeChatbotStreamReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeCreateReq;
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
            @RequestParam(defaultValue = "10") int size) {
        PageResult<KnowledgeResp> result = knowledgeService.page(page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取知识库详情", description = "根据ID获取知识库详细信息")
    @GetMapping("/{id}")
    public Result<Object> getKnowledgeBase(@PathVariable Long id) {
        // TODO: 实现知识库详情查询逻辑
        return Result.success();
    }

    /**
     * 上传文件到知识库
     *
     * @param id 知识库ID
     * @param file 上传的文件
     * @param folderId 文件夹ID（可选，0为根目录）
     * @return 操作结果
     */
    @Operation(summary = "上传文件", description = "向知识库上传文件，调用Dify API上传文档并保存关联关系")
    @PostMapping("/{id}/upload")
    public Result<Void> uploadFile(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false, defaultValue = "0") Long folderId) {
        knowledgeService.uploadFile(id, file, folderId);
        return Result.success();
    }

    /**
     * 基于知识库的 Chatbot 流式对话
     *
     * @param req 流式对话请求
     * @return 流式响应（SSE格式）
     */
    @Operation(summary = "知识库Chatbot流式对话", description = "基于知识库的Chatbot流式问答，如果用户未创建Chatbot则返回提示")
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

    @Operation(summary = "获取文件夹树", description = "获取知识库文件夹树")
    @GetMapping("/{id}/folders")
    public Result<Object> getFolderTree(@PathVariable Long id) {
        return Result.success();
    }

    @Operation(summary = "创建文件夹", description = "在知识库创建文件夹")
    @PostMapping("/{id}/folders")
    public Result<Object> createFolder(@PathVariable Long id, @RequestBody Object request) {
        return Result.success();
    }

    @Operation(summary = "更新文件夹", description = "更新文件夹信息")
    @PutMapping("/folders/{folderId}")
    public Result<Object> updateFolder(@PathVariable Long folderId, @RequestBody Object request) {
        return Result.success();
    }

    @Operation(summary = "删除文件夹", description = "删除指定文件夹")
    @DeleteMapping("/folders/{folderId}")
    public Result<Void> deleteFolder(@PathVariable Long folderId) {
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
}
