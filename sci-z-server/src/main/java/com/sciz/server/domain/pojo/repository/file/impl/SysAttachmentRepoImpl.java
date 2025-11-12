package com.sciz.server.domain.pojo.repository.file.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.mapper.file.SysAttachmentMapper;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    @Override
    public SysAttachment findById(Long id) {
        if (id == null) {
            return null;
        }
        return mapper.selectById(id);
    }

    @Override
    public SysAttachment findByMd5(String md5) {
        if (!StringUtils.hasText(md5)) {
            return null;
        }
        return mapper.selectOne(new LambdaQueryWrapper<SysAttachment>()
                .eq(SysAttachment::getMd5Hash, md5)
                .eq(SysAttachment::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .last("LIMIT 1"));
    }

    @Override
    public boolean update(SysAttachment entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        return mapper.updateById(entity) > 0;
    }

    @Override
    public boolean markDeleted(Long id, Long userId) {
        if (id == null) {
            return false;
        }
        return mapper.update(null, new LambdaUpdateWrapper<SysAttachment>()
                .eq(SysAttachment::getId, id)
                .set(SysAttachment::getIsDeleted, DeleteStatus.DELETED.getCode())
                .set(SysAttachment::getUpdatedBy, userId)
                .set(SysAttachment::getUpdatedTime, LocalDateTime.now())
                .last("LIMIT 1")) > 0;
    }

    @Override
    public boolean increaseDownloadCount(Long id, Long userId) {
        if (id == null) {
            return false;
        }
        return mapper.update(null, new LambdaUpdateWrapper<SysAttachment>()
                .eq(SysAttachment::getId, id)
                .set(SysAttachment::getUpdatedBy, userId)
                .set(SysAttachment::getUpdatedTime, LocalDateTime.now())
                .setSql("download_count = COALESCE(download_count,0) + 1")
                .last("LIMIT 1")) > 0;
    }

    @Override
    public IPage<SysAttachment> page(Page<SysAttachment> page,
            String relationType,
            List<Long> relationIds,
            String attachmentType,
            Integer isPublic,
            Long uploaderId,
            String keyword,
            String sortBy,
            boolean asc) {
        LambdaQueryWrapper<SysAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAttachment::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());
        Optional.ofNullable(attachmentType)
                .filter(StringUtils::hasText)
                .ifPresent(value -> wrapper.eq(SysAttachment::getFileType, value));
        Optional.ofNullable(isPublic)
                .ifPresent(value -> wrapper.eq(SysAttachment::getIsPublic, value));
        Optional.ofNullable(uploaderId)
                .ifPresent(value -> wrapper.eq(SysAttachment::getUploaderId, value));
        Optional.ofNullable(keyword)
                .filter(StringUtils::hasText)
                .ifPresent(value -> wrapper.like(SysAttachment::getOriginalName, value));

        if (StringUtils.hasText(relationType) && relationIds != null) {
            if (relationIds.isEmpty()) {
                page.setRecords(List.of());
                page.setTotal(0);
                return page;
            }
            wrapper.in(SysAttachment::getId, relationIds);
        }

        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "uploadTime" -> wrapper.orderBy(true, asc, SysAttachment::getUploadTime);
                case "fileSize" -> wrapper.orderBy(true, asc, SysAttachment::getFileSize);
                case "downloadCount" -> wrapper.orderBy(true, asc, SysAttachment::getDownloadCount);
                default -> wrapper.orderBy(true, false, SysAttachment::getUploadTime);
            }
        } else {
            wrapper.orderBy(true, false, SysAttachment::getUploadTime);
        }

        return mapper.selectPage(page, wrapper);
    }
}
