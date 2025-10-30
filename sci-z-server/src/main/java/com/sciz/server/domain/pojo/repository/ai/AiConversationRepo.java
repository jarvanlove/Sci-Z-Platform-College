package com.sciz.server.domain.pojo.repository.ai;

import com.sciz.server.domain.pojo.entity.ai.AiConversation;

/**
 * AI 会话仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className AiConversationRepo
 * @date 2025-10-30 11:00
 */
public interface AiConversationRepo {

    /**
     * 保存 AI 会话
     *
     * @param entity AiConversation 实体
     * @return 生成的主键ID
     */
    Long save(AiConversation entity);
}
