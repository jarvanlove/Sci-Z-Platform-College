package com.sciz.server.domain.pojo.repository.knowledge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;

import java.util.List;

/**
 * 知识库文件关系仓储（领域层抽象）
 * 
 * @author ShiHang.Shang
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

    /**
     * 根据ID查询知识库文件关系
     *
     * @param id 关联ID
     * @return 知识库文件关系实体
     */
    SysKnowledgeFileRelation findById(Long id);

    /**
     * 更新知识库文件关系
     *
     * @param entity 实体
     * @return 是否更新成功
     */
    boolean updateById(SysKnowledgeFileRelation entity);

    /**
     * 根据ID删除知识库文件关系（软删除）
     *
     * @param id 关联ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据知识库ID分页查询文件关联列表
     *
     * @param page 分页对象
     * @param knowledgeId 知识库ID
     * @param folderId 文件夹ID（可选，如果为null则查询所有文件夹）
     * @return 分页结果
     */
    IPage<SysKnowledgeFileRelation> pageByKnowledgeId(Page<SysKnowledgeFileRelation> page, Long knowledgeId, Long folderId);

    /**
     * 根据多个知识库ID分页查询文件关联列表
     *
     * @param page 分页对象
     * @param knowledgeIds 知识库ID列表
     * @param folderId 文件夹ID（可选，如果为null则查询所有文件夹）
     * @return 分页结果
     */
    IPage<SysKnowledgeFileRelation> pageByKnowledgeIds(Page<SysKnowledgeFileRelation> page, List<Long> knowledgeIds, Long folderId);
}
