package com.sciz.server.domain.pojo.repository.ai;

import com.sciz.server.domain.pojo.entity.ai.AiMessage;

/**
 * AI 消息仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className AiMessageRepo
 * @date 2025-10-30 11:00
 */
public interface AiMessageRepo {

    /**
     * 保存 AI 消息
     *
     * @param entity AiMessage 实体
     * @return 生成的主键ID
     */
    Long save(AiMessage entity);
}
