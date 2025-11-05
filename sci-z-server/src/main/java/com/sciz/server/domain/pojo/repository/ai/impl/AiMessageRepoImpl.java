package com.sciz.server.domain.pojo.repository.ai.impl;

import com.sciz.server.domain.pojo.entity.ai.AiMessage;
import com.sciz.server.domain.pojo.mapper.ai.AiMessageMapper;
import com.sciz.server.domain.pojo.repository.ai.AiMessageRepo;
import org.springframework.stereotype.Repository;

/**
 * AI 消息仓储实现
 * 
 * @author JiaWen.Wu
 * @className AiMessageRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class AiMessageRepoImpl implements AiMessageRepo {

    private final AiMessageMapper mapper;

    public AiMessageRepoImpl(AiMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(AiMessage entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
