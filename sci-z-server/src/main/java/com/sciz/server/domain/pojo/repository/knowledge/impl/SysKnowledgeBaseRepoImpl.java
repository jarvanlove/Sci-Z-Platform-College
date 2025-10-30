package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeBaseMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import org.springframework.stereotype.Repository;

/**
 * 知识库仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysKnowledgeBaseRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysKnowledgeBaseRepoImpl implements SysKnowledgeBaseRepo {

    private final SysKnowledgeBaseMapper mapper;

    public SysKnowledgeBaseRepoImpl(SysKnowledgeBaseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysKnowledgeBase entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
