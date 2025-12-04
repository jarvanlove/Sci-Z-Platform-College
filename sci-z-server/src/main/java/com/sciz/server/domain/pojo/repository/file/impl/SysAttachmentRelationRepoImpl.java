package com.sciz.server.domain.pojo.repository.file.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import com.sciz.server.domain.pojo.mapper.file.SysAttachmentRelationMapper;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 附件关联仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysAttachmentRelationRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysAttachmentRelationRepoImpl implements SysAttachmentRelationRepo {

    private final SysAttachmentRelationMapper mapper;

    public SysAttachmentRelationRepoImpl(SysAttachmentRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysAttachmentRelation entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public List<Long> findAttachmentIds(String relationType, Long relationId) {
        if (!StringUtils.hasText(relationType) || relationId == null) {
            return Collections.emptyList();
        }
        List<SysAttachmentRelation> records = mapper.selectList(new LambdaQueryWrapper<SysAttachmentRelation>()
                .eq(SysAttachmentRelation::getRelationType, relationType)
                .eq(SysAttachmentRelation::getRelationId, relationId)
                .eq(SysAttachmentRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
        return records.stream().map(SysAttachmentRelation::getAttachmentId).toList();
    }

    @Override
    public boolean deleteByAttachmentId(Long attachmentId) {
        if (attachmentId == null) {
            return false;
        }
        return mapper.update(null, new LambdaUpdateWrapper<SysAttachmentRelation>()
                .eq(SysAttachmentRelation::getAttachmentId, attachmentId)
                .set(SysAttachmentRelation::getIsDeleted, DeleteStatus.DELETED.getCode())
                .set(SysAttachmentRelation::getUpdatedTime, LocalDateTime.now())
                .last("LIMIT 1")) > 0;
    }

    @Override
    public List<SysAttachmentRelation> findPendingRelations(String relationType, String relationName, Long userId) {
        if (!StringUtils.hasText(relationType) || !StringUtils.hasText(relationName) || userId == null) {
            return Collections.emptyList();
        }

        // 查询待关联的记录（relationId = 0 表示待关联）
        List<SysAttachmentRelation> relations = mapper.selectList(new LambdaQueryWrapper<SysAttachmentRelation>()
                .eq(SysAttachmentRelation::getRelationType, relationType)
                .eq(SysAttachmentRelation::getRelationName, relationName)
                .eq(SysAttachmentRelation::getRelationId, 0L) // 0 表示待关联
                .eq(SysAttachmentRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByDesc(SysAttachmentRelation::getCreatedTime)
                .last("LIMIT 10")); // 最多查询最近10条

        // 需要通过附件表验证 uploaderId，这里先返回，由调用方通过附件表验证
        return relations;
    }

    @Override
    public boolean updateRelationIds(List<Long> relationIds, Long relationId, Long userId) {
        if (relationIds == null || relationIds.isEmpty() || relationId == null) {
            return false;
        }

        var now = LocalDateTime.now();
        return mapper.update(null, new LambdaUpdateWrapper<SysAttachmentRelation>()
                .in(SysAttachmentRelation::getId, relationIds)
                .set(SysAttachmentRelation::getRelationId, relationId)
                .set(SysAttachmentRelation::getUpdatedBy, userId)
                .set(SysAttachmentRelation::getUpdatedTime, now)) > 0;
    }

    @Override
    public Map<Long, List<Long>> findAttachmentIdsByRelationIds(String relationType, List<Long> relationIds) {
        if (!StringUtils.hasText(relationType) || CollectionUtils.isEmpty(relationIds)) {
            return Map.of();
        }
        var records = mapper.selectList(new LambdaQueryWrapper<SysAttachmentRelation>()
                .eq(SysAttachmentRelation::getRelationType, relationType)
                .in(SysAttachmentRelation::getRelationId, relationIds)
                .eq(SysAttachmentRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
        return records.stream()
                .collect(Collectors.groupingBy(
                        SysAttachmentRelation::getRelationId,
                        Collectors.mapping(SysAttachmentRelation::getAttachmentId, Collectors.toList())));
    }

    @Override
    public SysAttachmentRelation findByAttachmentId(Long attachmentId, String relationType) {
        if (attachmentId == null) {
            return null;
        }
        var queryWrapper = new LambdaQueryWrapper<SysAttachmentRelation>()
                .eq(SysAttachmentRelation::getAttachmentId, attachmentId)
                .eq(StringUtils.hasText(relationType), SysAttachmentRelation::getRelationType, relationType)
                .eq(SysAttachmentRelation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .last("LIMIT 1");
        return mapper.selectOne(queryWrapper);
    }
}
