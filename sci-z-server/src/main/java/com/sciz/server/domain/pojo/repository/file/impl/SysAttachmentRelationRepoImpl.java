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
import org.springframework.stereotype.Repository;
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
}
