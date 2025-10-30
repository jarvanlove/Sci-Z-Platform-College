package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeFileRelationMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import org.springframework.stereotype.Repository;

/**
 * 知识库文件关系仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysKnowledgeFileRelationRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysKnowledgeFileRelationRepoImpl implements SysKnowledgeFileRelationRepo {

    private final SysKnowledgeFileRelationMapper mapper;

    public SysKnowledgeFileRelationRepoImpl(SysKnowledgeFileRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysKnowledgeFileRelation entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
