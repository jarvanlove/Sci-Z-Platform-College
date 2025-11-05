package com.sciz.server.domain.pojo.repository.ai.impl;

import com.sciz.server.domain.pojo.entity.ai.AiConversation;
import com.sciz.server.domain.pojo.mapper.ai.AiConversationMapper;
import com.sciz.server.domain.pojo.repository.ai.AiConversationRepo;
import org.springframework.stereotype.Repository;

/**
 * AI 会话仓储实现
 * 
 * @author JiaWen.Wu
 * @className AiConversationRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class AiConversationRepoImpl implements AiConversationRepo {

    private final AiConversationMapper mapper;

    public AiConversationRepoImpl(AiConversationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(AiConversation entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
