package com.sciz.server.domain.pojo.repository.file.impl;

import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.mapper.file.SysAttachmentMapper;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import org.springframework.stereotype.Repository;

/**
 * 附件仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysAttachmentRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysAttachmentRepoImpl implements SysAttachmentRepo {

    private final SysAttachmentMapper mapper;

    public SysAttachmentRepoImpl(SysAttachmentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysAttachment entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
