package com.sciz.server.domain.pojo.repository.file;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import java.util.List;

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

    /**
     * 根据ID查询附件
     *
     * @param id Long 附件ID
     * @return SysAttachment 附件实体
     */
    SysAttachment findById(Long id);

    /**
     * 根据MD5查询附件
     *
     * @param md5 String 文件MD5
     * @return SysAttachment 附件实体
     */
    SysAttachment findByMd5(String md5);

    /**
     * 更新附件信息
     *
     * @param entity SysAttachment 实体
     * @return boolean 是否更新成功
     */
    boolean update(SysAttachment entity);

    /**
     * 标记附件为已删除
     *
     * @param id     Long 附件ID
     * @param userId Long 操作人ID
     * @return boolean 是否成功
     */
    boolean markDeleted(Long id, Long userId);

    /**
     * 下载次数+1
     *
     * @param id     Long 附件ID
     * @param userId Long 操作人ID
     * @return boolean 是否成功
     */
    boolean increaseDownloadCount(Long id, Long userId);

    /**
     * 分页查询附件
     *
     * @param page           Page<SysAttachment> 分页参数
     * @param relationType   String 关联类型
     * @param relationIds    List<Long> 关联附件ID集合
     * @param attachmentType String 附件类型
     * @param isPublic       Integer 是否公开
     * @param uploaderId     Long 上传人ID
     * @param keyword        String 模糊关键词
     * @param sortBy         String 排序字段
     * @param asc            boolean 是否升序
     * @return IPage<SysAttachment> 分页结果
     */
    IPage<SysAttachment> page(Page<SysAttachment> page,
            String relationType,
            List<Long> relationIds,
            String attachmentType,
            Integer isPublic,
            Long uploaderId,
            String keyword,
            String sortBy,
            boolean asc);
}
