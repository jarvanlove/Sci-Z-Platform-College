package com.sciz.server.infrastructure.external.dify.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 流式处理工具类
 * 用于处理 Dify API 返回的 SSE 流式数据
 *
 * @author JiaWen.Wu
 * @className SseStreamHandler
 * @date 2025-12-22 14:30
 */
@Slf4j
@Component
public class SseStreamHandler {

    /**
     * 处理 SSE 流式数据行
     *
     * @param emitter SSE 发射器
     * @param line    数据行
     */
    public void handleStreamLine(SseEmitter emitter, String line) {
        try {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                return;
            }

            // 处理 SSE 格式的数据行
            if (trimmedLine.startsWith("data:")) {
                String data = trimmedLine.substring(5).trim();
                if (!data.isEmpty() && !data.equals("[DONE]")) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(data));
                }
            } else if (trimmedLine.startsWith("event:")) {
                // 处理事件类型
                String eventType = trimmedLine.substring(6).trim();
                log.debug(String.format("收到SSE事件: %s", eventType));
            } else {
                // 如果不是标准 SSE 格式，直接发送原始数据
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(trimmedLine));
            }
        } catch (Exception e) {
            log.warn(String.format("处理流式数据行失败: line=%s, err=%s", line, e.getMessage()));
        }
    }

    /**
     * 完成 SSE 流
     *
     * @param emitter SSE 发射器
     */
    public void completeStream(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("message_end")
                    .data("{}"));
            emitter.complete();
        } catch (Exception e) {
            log.error("发送完成事件失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 发送错误消息并完成流
     *
     * @param emitter SSE 发射器
     * @param error   错误信息
     */
    public void sendErrorAndComplete(SseEmitter emitter, String error) {
        try {
            String errorMessage = String.format("{\"error\": true, \"message\": \"%s\"}", error);
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorMessage));
            emitter.complete();
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
            emitter.completeWithError(e);
        }
    }
}
