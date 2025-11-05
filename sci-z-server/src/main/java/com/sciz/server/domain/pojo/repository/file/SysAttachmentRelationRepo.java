package com.sciz.server.domain.pojo.repository.file;

import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;

/**
 * 附件关联仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysAttachmentRelationRepo
 * @date 2025-10-30 11:00
 */
public interface SysAttachmentRelationRepo {

    /**
     * 保存附件关联
     *
     * @param entity SysAttachmentRelation 实体
     * @return 生成的主键ID
     */
    Long save(SysAttachmentRelation entity);
}
