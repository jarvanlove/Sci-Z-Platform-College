package com.sciz.server.infrastructure.external.dify.converter;

import com.sciz.server.domain.pojo.entity.ai.AiConversation;
import com.sciz.server.domain.pojo.entity.ai.AiMessage;
import com.sciz.server.infrastructure.external.dify.dto.request.DifyChatReq;
import com.sciz.server.infrastructure.external.dify.dto.response.DifyChatResp;
import org.springframework.stereotype.Component;

/**
 * Dify模型转换器
 *
 * @author JiaWen.Wu
 * @className DifyConverter
 * @date 2025-10-29 10:30
 */
@Component
public class DifyConverter {

    /**
     * 转换对话实体为Dify聊天请求
     *
     * @param conversation 对话实体
     * @param message      消息实体
     * @return Dify聊天请求
     */
    public DifyChatReq toDifyChatReq(AiConversation conversation, AiMessage message) {
        DifyChatReq req = new DifyChatReq();
        req.setUserId(String.valueOf(conversation.getUserId()));
        req.setConversationId(String.valueOf(conversation.getId()));
        req.setMessage(message.getContent());
        req.setAppId("default"); // 默认应用ID
        req.setStream(false);
        req.setResponseMode("blocking");
        return req;
    }

    /**
     * 转换Dify聊天响应为消息实体
     *
     * @param resp           Dify聊天响应
     * @param conversationId 对话ID
     * @return 消息实体
     */
    public AiMessage toMessage(DifyChatResp resp, String conversationId) {
        AiMessage message = new AiMessage();
        // message.setMessageId(resp.getMessageId());
        message.setConversationId(Long.parseLong(conversationId));
        message.setContent(resp.getAnswer());
        message.setRole("assistant");
        // message.setCreateTime(resp.getCreatedAt());
        return message;
    }
}
