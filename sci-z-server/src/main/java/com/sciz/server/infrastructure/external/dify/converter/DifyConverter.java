package com.sciz.server.infrastructure.external.dify.converter;

import com.sciz.server.domain.pojo.entity.ai.Conversation;
import com.sciz.server.domain.pojo.entity.ai.Message;
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
    public DifyChatReq toDifyChatReq(Conversation conversation, Message message) {
        DifyChatReq req = new DifyChatReq();
        req.setUserId(String.valueOf(conversation.getUserId()));
        req.setConversationId(conversation.getConversationId());
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
    public Message toMessage(DifyChatResp resp, String conversationId) {
        Message message = new Message();
        message.setMessageId(resp.getMessageId());
        message.setConversationId(conversationId);
        message.setContent(resp.getAnswer());
        message.setRole("assistant");
        message.setCreateTime(resp.getCreatedAt());
        return message;
    }
}
