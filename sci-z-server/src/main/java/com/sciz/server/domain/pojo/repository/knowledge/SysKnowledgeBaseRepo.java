package com.sciz.server.domain.pojo.repository.knowledge;

import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;

/**
 * 知识库仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysKnowledgeBaseRepo
 * @date 2025-10-30 11:00
 */
public interface SysKnowledgeBaseRepo {

    /**
     * 保存知识库
     *
     * @param entity SysKnowledgeBase 实体
     * @return 生成的主键ID
     */
    Long save(SysKnowledgeBase entity);
}
