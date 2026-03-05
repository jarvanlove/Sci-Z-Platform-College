package com.sciz.server.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 站内消息 WebSocket 配置
 * <p>
 * 端点：/ws/message，连接时需带 query token=xxx（Sa-Token 的 token）
 * </p>
 *
 * @author Sci-Z
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class MessageWebSocketConfig implements WebSocketConfigurer {

    private final MessageWebSocketHandler messageWebSocketHandler;
    private final MessageWebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws/message")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
