package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.ai.ChatService;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author JiaWen.Wu
 * @className ChatController
 * @date 2025-01-27 10:00
 */
@Tag(name = "AI对话控制器", description = "AI对话相关接口")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送消息", description = "向AI发送消息")
    @PostMapping("/message")
    public Result<Object> sendMessage(@RequestBody Object request) {
        // TODO: 实现消息发送逻辑
        return Result.success(null);
    }

    @Operation(summary = "获取对话历史", description = "获取对话历史记录")
    @GetMapping("/conversations/{conversationId}")
    public Result<Object> getConversationHistory(@PathVariable String conversationId) {
        // TODO: 实现对话历史查询逻辑
        return Result.success(null);
    }

    @Operation(summary = "创建对话", description = "创建新的对话会话")
    @PostMapping("/conversations")
    public Result<Object> createConversation(@RequestBody Object request) {
        // TODO: 实现对话创建逻辑
        return Result.success(null);
    }

    @Operation(summary = "删除对话", description = "删除指定对话")
    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> deleteConversation(@PathVariable String conversationId) {
        // TODO: 实现对话删除逻辑
        return Result.success(null);
    }

    @Operation(summary = "获取对话列表", description = "获取用户的对话列表")
    @GetMapping("/conversations")
    public Result<Object> getConversations(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 实现对话列表查询逻辑
        return Result.success(null);
    }
}
