package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.message.MessageService;
import com.sciz.server.domain.pojo.dto.request.message.MessageListQueryReq;
import com.sciz.server.domain.pojo.dto.response.message.MessageDetailResp;
import com.sciz.server.domain.pojo.dto.response.message.MessageListResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 站内消息控制器
 *
 * @author Sci-Z
 */
@Tag(name = "站内消息", description = "消息列表、未读数、已读、全部已读")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "消息列表", description = "分页查询当前用户的消息，支持仅未读")
    @GetMapping
    public Result<PageResult<MessageListResp>> list(@Valid MessageListQueryReq req) {
        PageResult<MessageListResp> page = messageService.list(req);
        return Result.success(page);
    }

    @Operation(summary = "未读数量", description = "当前用户未读消息数")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        long count = messageService.unreadCount();
        return Result.success(count);
    }

    @Operation(summary = "消息详情", description = "获取单条消息详情（仅接收人可查看）")
    @GetMapping("/{id}")
    public Result<MessageDetailResp> getById(@PathVariable Long id) {
        MessageDetailResp detail = messageService.getById(id);
        return Result.success(detail);
    }

    @Operation(summary = "标为已读", description = "将指定消息标为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    @Operation(summary = "全部已读", description = "将当前用户全部消息标为已读")
    @PutMapping("/read-all")
    public Result<Void> markReadAll() {
        messageService.markReadAll();
        return Result.success();
    }
}
