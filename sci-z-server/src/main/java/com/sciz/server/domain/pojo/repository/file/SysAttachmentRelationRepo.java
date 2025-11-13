package com.sciz.server.domain.pojo.repository.file;

import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import java.util.List;

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

    /**
     * 根据关联信息查询附件ID集合
     *
     * @param relationType String 关联类型
     * @param relationId   Long 关联对象ID
     * @return List<Long> 附件ID集合
     */
    List<Long> findAttachmentIds(String relationType, Long relationId);

    /**
     * 根据附件ID删除关联
     *
     * @param attachmentId Long 附件ID
     * @return boolean 是否成功
     */
    boolean deleteByAttachmentId(Long attachmentId);
}
