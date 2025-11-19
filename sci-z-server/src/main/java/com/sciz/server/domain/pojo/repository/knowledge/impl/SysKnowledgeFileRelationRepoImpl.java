package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeFileRelationMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;

/**
 * 知识库文件关系仓储实现
 * 
 * @author ShiHang.Shang
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

    @Override
    public SysKnowledgeFileRelation findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeFileRelation::getId, id)
                .eq(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public boolean updateById(SysKnowledgeFileRelation entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(SysKnowledgeFileRelation::getId, id)
                .set(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    @Override
    public IPage<SysKnowledgeFileRelation> pageByKnowledgeId(Page<SysKnowledgeFileRelation> page, Long knowledgeId, Long folderId) {
        LambdaQueryWrapper<SysKnowledgeFileRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysKnowledgeFileRelation::getKnowledgeId, knowledgeId)
                .eq(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());
        
        // 如果指定了文件夹ID，则只查询该文件夹下的文件
        if (folderId != null) {
            queryWrapper.eq(SysKnowledgeFileRelation::getFolderId, folderId);
        }
        
        // 按排序号升序，创建时间倒序排列
        queryWrapper.orderByAsc(SysKnowledgeFileRelation::getSortOrder)
                .orderByDesc(SysKnowledgeFileRelation::getCreatedTime);
        
        return mapper.selectPage(page, queryWrapper);
    }
}
