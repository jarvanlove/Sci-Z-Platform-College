package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.ai.AiMessageService;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageQueryReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiMessageResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 消息控制器
 *
 * @author shihangshang
 * @className AiMessageController
 * @date 2025-11-14 10:00
 */
@Tag(name = "AI消息管理", description = "AI消息相关接口")
@RestController
@RequestMapping("/api/ai/message")
@RequiredArgsConstructor
public class AiMessageController {

    private final AiMessageService messageService;

    /**
     * 创建消息
     *
     * @param req 创建请求
     * @return 消息响应
     */
    @Operation(summary = "创建消息", description = "创建新的AI消息")
    @PostMapping
    public Result<AiMessageResp> createMessage(@Valid @RequestBody AiMessageCreateReq req) {
        AiMessageResp resp = messageService.create(req);
        return Result.success(resp);
    }

    /**
     * 更新消息
     *
     * @param req 更新请求
     * @return 消息响应
     */
    @Operation(summary = "更新消息", description = "更新AI消息信息")
    @PutMapping
    public Result<AiMessageResp> updateMessage(@Valid @RequestBody AiMessageUpdateReq req) {
        AiMessageResp resp = messageService.update(req);
        return Result.success(resp);
    }

    /**
     * 查询消息详情
     *
     * @param id 消息ID
     * @return 消息响应
     */
    @Operation(summary = "查询消息详情", description = "根据ID查询AI消息详情")
    @GetMapping("/{id}")
    public Result<AiMessageResp> getMessageDetail(@PathVariable String id) {
        AiMessageResp resp = messageService.findDetail(id);
        return Result.success(resp);
    }

    /**
     * 分页查询消息列表
     *
     * @param req 查询请求
     * @return 消息分页结果
     */
    @Operation(summary = "分页查询消息列表", description = "分页查询指定会话的消息列表")
    @GetMapping("/page")
    public Result<PageResult<AiMessageResp>> pageMessages(@Valid AiMessageQueryReq req) {
        PageResult<AiMessageResp> result = messageService.page(req);
        return Result.success(result);
    }

    /**
     * 查询消息列表
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    @Operation(summary = "查询消息列表", description = "查询指定会话的所有消息列表")
    @GetMapping("/list/{conversationId}")
    public Result<List<AiMessageResp>> listMessages(@PathVariable String conversationId) {
        List<AiMessageResp> list = messageService.listByConversationId(conversationId);
        return Result.success(list);
    }

    /**
     * 删除消息
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @Operation(summary = "删除消息", description = "根据ID删除AI消息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable String id) {
        messageService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除消息
     *
     * @param ids ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除消息", description = "批量删除AI消息")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatchMessages(@RequestBody List<String> ids) {
        messageService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 删除会话所有消息
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "删除会话所有消息", description = "删除指定会话的所有消息")
    @DeleteMapping("/conversation/{conversationId}")
    public Result<Void> deleteMessagesByConversationId(@PathVariable String conversationId) {
        messageService.deleteByConversationId(conversationId);
        return Result.success();
    }
}

