package com.sciz.server.infrastructure.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 站内消息 WebSocket 处理器
 * <p>
 * 连接时从 query 取 token，校验后以 userId 绑定会话；关闭时移除。
 * </p>
 *
 * @author Sci-Z
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    public static final String ATTR_USER_ID = "userId";

    private final MessageWebSocketSessionHolder sessionHolder;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // HandshakeInterceptor 已将 userId 放入 session.getAttributes()
        Long userId = resolveUserId(session);
        if (userId == null) {
            log.warn("消息 WebSocket 连接未携带有效 token，关闭会话");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessionHolder.addSession(userId, session);
        log.debug("消息 WebSocket 连接建立: userId={}", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionHolder.removeSession(session);
        log.debug("消息 WebSocket 连接关闭: status={}", status);
    }

    private Long resolveUserId(WebSocketSession session) {
        try {
            Map<String, Object> attrs = session.getAttributes();
            // 握手时 HandshakeInterceptor 已把 userId 放入 attributes
            Object uid = attrs.get(ATTR_USER_ID);
            if (uid instanceof Long) return (Long) uid;
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
