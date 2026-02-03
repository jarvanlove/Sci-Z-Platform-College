package com.sciz.server.domain.pojo.repository.knowledge;

import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFolder;

import java.util.List;

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

    /**
     * 根据ID查询文件夹
     *
     * @param id 文件夹ID
     * @return 文件夹实体
     */
    SysKnowledgeFolder findById(Long id);

    /**
     * 根据知识库ID和父文件夹ID查询文件夹列表
     *
     * @param knowledgeId 知识库ID
     * @param parentId 父文件夹ID（0为根目录）
     * @return 文件夹列表
     */
    List<SysKnowledgeFolder> findByKnowledgeIdAndParentId(Long knowledgeId, Long parentId);

    /**
     * 根据知识库ID查询所有文件夹
     *
     * @param knowledgeId 知识库ID
     * @return 文件夹列表
     */
    List<SysKnowledgeFolder> findByKnowledgeId(Long knowledgeId);

    /**
     * 根据知识库ID和文件夹名称查询文件夹（同级目录下不能重名）
     *
     * @param knowledgeId 知识库ID
     * @param parentId 父文件夹ID
     * @param folderName 文件夹名称
     * @return 文件夹实体
     */
    SysKnowledgeFolder findByKnowledgeIdAndParentIdAndFolderName(Long knowledgeId, Long parentId, String folderName);

    /**
     * 更新文件夹
     *
     * @param entity 文件夹实体
     * @return 是否更新成功
     */
    boolean updateById(SysKnowledgeFolder entity);

    /**
     * 根据ID删除文件夹（软删除）
     *
     * @param id 文件夹ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据父文件夹ID查询子文件夹数量
     *
     * @param parentId 父文件夹ID
     * @return 子文件夹数量
     */
    long countByParentId(Long parentId);

}
