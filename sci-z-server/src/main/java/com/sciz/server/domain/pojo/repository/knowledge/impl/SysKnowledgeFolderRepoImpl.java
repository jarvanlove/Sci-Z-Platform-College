package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFolder;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeFolderMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFolderRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public SysKnowledgeFolder findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeFolder::getId, id)
                .eq(SysKnowledgeFolder::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public List<SysKnowledgeFolder> findByKnowledgeIdAndParentId(Long knowledgeId, Long parentId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeFolder::getKnowledgeId, knowledgeId)
                .eq(SysKnowledgeFolder::getParentId, parentId)
                .eq(SysKnowledgeFolder::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(SysKnowledgeFolder::getSortOrder)
                .orderByAsc(SysKnowledgeFolder::getId)
                .list();
    }

    @Override
    public List<SysKnowledgeFolder> findByKnowledgeId(Long knowledgeId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeFolder::getKnowledgeId, knowledgeId)
                .eq(SysKnowledgeFolder::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(SysKnowledgeFolder::getSortOrder)
                .orderByAsc(SysKnowledgeFolder::getId)
                .list();
    }

    @Override
    public SysKnowledgeFolder findByKnowledgeIdAndParentIdAndFolderName(Long knowledgeId, Long parentId, String folderName) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeFolder::getKnowledgeId, knowledgeId)
                .eq(SysKnowledgeFolder::getParentId, parentId)
                .eq(SysKnowledgeFolder::getFolderName, folderName)
                .eq(SysKnowledgeFolder::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public boolean updateById(SysKnowledgeFolder entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(SysKnowledgeFolder::getId, id)
                .set(SysKnowledgeFolder::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    @Override
    public long countByParentId(Long parentId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeFolder::getParentId, parentId)
                .eq(SysKnowledgeFolder::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .count();
    }

}