package com.sciz.server.domain.pojo.repository.file;

import com.sciz.server.domain.pojo.entity.file.SysAttachment;

/**
 * 附件仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysAttachmentRepo
 * @date 2025-10-30 11:00
 */
public interface SysAttachmentRepo {

    /**
     * 保存附件
     *
     * @param entity SysAttachment 实体
     * @return 生成的主键ID
     */
    Long save(SysAttachment entity);
}
