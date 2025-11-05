package com.sciz.server.domain.pojo.repository.file.impl;

import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import com.sciz.server.domain.pojo.mapper.file.SysAttachmentRelationMapper;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import org.springframework.stereotype.Repository;

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
}
