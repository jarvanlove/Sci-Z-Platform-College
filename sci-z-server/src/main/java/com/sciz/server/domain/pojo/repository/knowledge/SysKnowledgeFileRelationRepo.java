package com.sciz.server.domain.pojo.repository.knowledge;

import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;

/**
 * 知识库文件关系仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysKnowledgeFileRelationRepo
 * @date 2025-10-30 11:00
 */
public interface SysKnowledgeFileRelationRepo {

    /**
     * 保存知识库文件关系
     *
     * @param entity SysKnowledgeFileRelation 实体
     * @return 生成的主键ID
     */
    Long save(SysKnowledgeFileRelation entity);
}
