package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeBaseMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;

/**
 * 知识库仓储实现
 * 
 * @author ShiHang.Shang
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

    @Override
    public SysKnowledgeBase findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeBase::getId, id)
                .eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public SysKnowledgeBase findByDifyKnowdataId(String difyKnowdataId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeBase::getDifyKnowdataId, difyKnowdataId)
                .eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 分页查询知识库列表
     *
     * @param page 分页对象
     * @param userId 用户ID（可选，如果为null则查询所有）
     * @return 分页结果
     */
    @Override
    public IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId) {
        LambdaQueryWrapper<SysKnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());
        
        // 如果指定了用户ID，则只查询该用户的知识库
        if (userId != null) {
            queryWrapper.eq(SysKnowledgeBase::getOwnerId, userId);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysKnowledgeBase::getCreatedTime);
        
        return mapper.selectPage(page, queryWrapper);
    }
}
