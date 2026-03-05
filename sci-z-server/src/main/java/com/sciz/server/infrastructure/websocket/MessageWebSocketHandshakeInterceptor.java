package com.sciz.server.infrastructure.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 消息 WebSocket 握手拦截器：从 query 取 token 并校验，将 userId 写入 attributes 供 Handler 使用
 *
 * @author Sci-Z
 */
@Slf4j
@Component
public class MessageWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final String TOKEN_PARAM = "token";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        String token = servletRequest.getServletRequest().getParameter(TOKEN_PARAM);
        if (token == null || token.isBlank()) {
            log.warn("消息 WebSocket 握手缺少 token 参数");
            return false;
        }
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) return false;
            long userId = loginId instanceof Number n ? n.longValue() : Long.parseLong(loginId.toString());
            attributes.put(MessageWebSocketHandler.ATTR_USER_ID, userId);
            return true;
        } catch (Exception e) {
            log.warn("消息 WebSocket 握手 token 无效: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
