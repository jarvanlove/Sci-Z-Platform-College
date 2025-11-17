package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFolder;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeFolderMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFolderRepo;
import org.springframework.stereotype.Repository;

/**
 * 知识库文件夹仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysKnowledgeFolderRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysKnowledgeFolderRepoImpl implements SysKnowledgeFolderRepo {
    private final SysKnowledgeFolderMapper mapper;
    public SysKnowledgeFolderRepoImpl(SysKnowledgeFolderMapper mapper) {
        this.mapper = mapper;
    }
    @Override
    public Long save(SysKnowledgeFolder entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}