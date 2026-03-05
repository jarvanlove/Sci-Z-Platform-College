package com.sciz.server.infrastructure.websocket;

import org.springframework.web.socket.WebSocketSession;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按用户维度的 WebSocket 会话持有者，用于消息推送
 *
 * @author Sci-Z
 */
@Component
public class MessageWebSocketSessionHolder {

    /** userId -> 该用户当前所有会话 */
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        userSessions.compute(userId, (k, set) -> {
            if (set == null) set = ConcurrentHashMap.newKeySet();
            set.add(session);
            return set;
        });
    }

    public void removeSession(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get(MessageWebSocketHandler.ATTR_USER_ID);
        if (userId == null) return;
        userSessions.computeIfPresent(userId, (k, set) -> {
            set.remove(session);
            return set.isEmpty() ? null : set;
        });
    }

    public Set<WebSocketSession> getSessions(Long userId) {
        Set<WebSocketSession> set = userSessions.get(userId);
        return set == null ? Set.of() : Set.copyOf(set);
    }

    /**
     * 向指定用户的所有在线会话发送文本消息
     *
     * @param userId  用户ID
     * @param payload 文本内容（通常为 JSON）
     * @return 是否至少发送成功一个会话
     */
    public boolean sendToUser(Long userId, String payload) {
        Set<WebSocketSession> sessions = getSessions(userId);
        boolean anySent = false;
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) continue;
            try {
                session.sendMessage(new org.springframework.web.socket.TextMessage(payload));
                anySent = true;
            } catch (IOException e) {
                // 单路发送失败不中断，继续其他会话
            }
        }
        return anySent;
    }
}
