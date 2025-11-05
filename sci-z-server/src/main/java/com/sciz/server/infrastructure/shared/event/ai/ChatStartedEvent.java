package com.sciz.server.infrastructure.shared.event.ai;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 聊天开始事件
 *
 * @author JiaWen.Wu
 * @className ChatStartedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class ChatStartedEvent extends DomainEvent {

    private String chatId;
    private String userId;
    private String userName;
    private String projectId;
    private String projectName;
    private String chatType;
    private String startTime;
    private String sessionId;

    public ChatStartedEvent(String chatId, String userId, String userName,
            String projectId, String projectName, String chatType,
            String startTime, String sessionId) {
        super();
        this.chatId = chatId;
        this.userId = userId;
        this.userName = userName;
        this.projectId = projectId;
        this.projectName = projectName;
        this.chatType = chatType;
        this.startTime = startTime;
        this.sessionId = sessionId;
    }

    @Override
    public String getAggregateId() {
        return chatId;
    }

    @Override
    public String getAggregateType() {
        return "Chat";
    }
}
