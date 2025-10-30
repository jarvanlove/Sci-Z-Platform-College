package com.sciz.server.domain.pojo.repository.knowledge;

import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFolder;

/**
 * 知识库文件夹仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysKnowledgeFolderRepo
 * @date 2025-10-30 11:00
 */
public interface SysKnowledgeFolderRepo {

    /**
     * 保存知识库文件夹
     *
     * @param entity SysKnowledgeFolder 实体
     * @return 生成的主键ID
     */
    Long save(SysKnowledgeFolder entity);
}
