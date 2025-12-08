package com.sciz.server.domain.pojo.repository.file;

import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import java.util.List;
import java.util.Map;

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

    /**
     * 查找待关联的附件关联记录（relationId 为临时值 0）
     *
     * @param relationType String 关联类型
     * @param relationName String 关联名称
     * @return List<SysAttachmentRelation> 待关联的记录列表
     */
    List<SysAttachmentRelation> findPendingRelations(String relationType, String relationName);

    /**
     * 批量更新关联记录的 relationId
     *
     * @param relationIds List<Long> 关联记录ID列表
     * @param relationId  Long 新的关联对象ID
     * @param userId      Long 更新人ID
     * @return boolean 是否更新成功
     */
    boolean updateRelationIds(List<Long> relationIds, Long relationId, Long userId);

    /**
     * 根据关联类型和关联ID列表批量查询附件ID集合
     *
     * @param relationType String 关联类型
     * @param relationIds  List<Long> 关联对象ID列表
     * @return Map<Long, List<Long>> 关联对象ID -> 附件ID列表
     */
    Map<Long, List<Long>> findAttachmentIdsByRelationIds(String relationType, List<Long> relationIds);

    /**
     * 根据附件ID查询关联信息
     *
     * @param attachmentId Long 附件ID
     * @param relationType String 关联类型（可选，如果为null则查询所有类型）
     * @return SysAttachmentRelation 关联信息（如果不存在则返回null）
     */
    SysAttachmentRelation findByAttachmentId(Long attachmentId, String relationType);
}
