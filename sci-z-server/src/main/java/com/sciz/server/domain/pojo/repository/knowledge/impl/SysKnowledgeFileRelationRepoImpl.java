package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeFileRelationMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public IPage<SysKnowledgeFileRelation> pageByKnowledgeId(Page<SysKnowledgeFileRelation> page, Long knowledgeId,
            Long folderId) {
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

    @Override
    public boolean deleteByAttachmentId(Long attachmentId) {
        if (attachmentId == null) {
            return false;
        }
        // PostgreSQL 不支持在 UPDATE 语句中使用 LIMIT，移除 LIMIT
        // 一个附件ID可能有多条知识库文件关联记录，应该全部删除
        return mapper.update(null, new LambdaUpdateWrapper<SysKnowledgeFileRelation>()
                .eq(SysKnowledgeFileRelation::getAttachmentId, attachmentId)
                .eq(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .set(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.DELETED.getCode())
                .set(SysKnowledgeFileRelation::getUpdatedTime, LocalDateTime.now())) > 0;
    }

    @Override
    public Long countByKnowledgeId(Long knowledgeId) {
        if (knowledgeId == null) {
            return 0L;
        }
        return mapper.selectCount(new LambdaQueryWrapper<SysKnowledgeFileRelation>()
                .eq(SysKnowledgeFileRelation::getKnowledgeId, knowledgeId)
                .eq(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
    }

    @Override
    public Map<Long, List<Long>> findAttachmentIdsByKnowledgeIds(List<Long> knowledgeIds) {
        if (CollectionUtils.isEmpty(knowledgeIds)) {
            return Map.of();
        }
        var records = mapper.selectList(new LambdaQueryWrapper<SysKnowledgeFileRelation>()
                .in(SysKnowledgeFileRelation::getKnowledgeId, knowledgeIds)
                .eq(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
        return records.stream()
                .collect(Collectors.groupingBy(
                        SysKnowledgeFileRelation::getKnowledgeId,
                        Collectors.mapping(SysKnowledgeFileRelation::getAttachmentId, Collectors.toList())));
    }

    @Override
    public List<SysKnowledgeFileRelation> findByKnowledgeId(Long knowledgeId) {
        if (knowledgeId == null) {
            return List.of();
        }
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeFileRelation::getKnowledgeId, knowledgeId)
                .eq(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(SysKnowledgeFileRelation::getSortOrder)
                .orderByDesc(SysKnowledgeFileRelation::getCreatedTime)
                .list();
    }

    @Override
    public Long countByFolderId(Long folderId) {
        if (folderId == null) {
            return 0L;
        }
        return mapper.selectCount(new LambdaQueryWrapper<SysKnowledgeFileRelation>()
                .eq(SysKnowledgeFileRelation::getFolderId, folderId)
                .eq(SysKnowledgeFileRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
    }
}
