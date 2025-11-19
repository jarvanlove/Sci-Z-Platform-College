package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.ai.AiConversationService;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationQueryReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiConversationResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 会话控制器
 *
 * @author shihangshang
 * @className AiConversationController
 * @date 2025-11-14 10:00
 */
@Tag(name = "AI会话管理", description = "AI会话相关接口")
@RestController
@RequestMapping("/api/ai/conversation")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService conversationService;

    /**
     * 创建会话
     *
     * @param req 创建请求
     * @return 会话响应
     */
    @Operation(summary = "创建会话", description = "创建新的AI会话")
    @PostMapping
    public Result<AiConversationResp> createConversation(@Valid @RequestBody AiConversationCreateReq req) {
        AiConversationResp resp = conversationService.create(req);
        return Result.success(resp);
    }

    /**
     * 更新会话
     *
     * @param req 更新请求
     * @return 会话响应
     */
    @Operation(summary = "更新会话", description = "更新AI会话信息")
    @PutMapping
    public Result<AiConversationResp> updateConversation(@Valid @RequestBody AiConversationUpdateReq req) {
        AiConversationResp resp = conversationService.update(req);
        return Result.success(resp);
    }

    /**
     * 查询会话详情
     *
     * @param id 会话ID
     * @return 会话响应
     */
    @Operation(summary = "查询会话详情", description = "根据ID查询AI会话详情")
    @GetMapping("/{id}")
    public Result<AiConversationResp> getConversationDetail(@PathVariable String id) {
        AiConversationResp resp = conversationService.findDetail(id);
        return Result.success(resp);
    }

    /**
     * 分页查询会话列表
     *
     * @param req 查询请求
     * @return 会话分页结果
     */
    @Operation(summary = "分页查询会话列表", description = "分页查询当前用户的AI会话列表")
    @GetMapping("/page")
    public Result<PageResult<AiConversationResp>> pageConversations(@Valid AiConversationQueryReq req) {
        PageResult<AiConversationResp> result = conversationService.page(req);
        return Result.success(result);
    }

    /**
     * 查询会话列表
     *
     * @return 会话列表
     */
    @Operation(summary = "查询会话列表", description = "查询当前用户的所有AI会话列表")
    @GetMapping("/list")
    public Result<List<AiConversationResp>> listConversations() {
        List<AiConversationResp> list = conversationService.list();
        return Result.success(list);
    }

    /**
     * 删除会话
     *
     * @param id 会话ID
     * @return 操作结果
     */
    @Operation(summary = "删除会话", description = "根据ID删除AI会话")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConversation(@PathVariable String id) {
        conversationService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除会话
     *
     * @param ids ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除会话", description = "批量删除AI会话")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatchConversations(@RequestBody List<String> ids) {
        conversationService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 更新置顶状态
     *
     * @param id 会话ID
     * @param isPinned 是否置顶(0:否,1:是)
     * @return 会话响应
     */
    @Operation(summary = "更新置顶状态", description = "更新AI会话的置顶状态")
    @PutMapping("/{id}/pinned")
    public Result<AiConversationResp> updatePinnedStatus(
            @PathVariable String id,
            @RequestParam Integer isPinned) {
        AiConversationResp resp = conversationService.updatePinnedStatus(id, isPinned);
        return Result.success(resp);
    }
}

