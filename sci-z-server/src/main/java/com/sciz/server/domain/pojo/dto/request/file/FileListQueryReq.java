package com.sciz.server.domain.pojo.dto.request.file;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.util.StringUtils;

/**
 * 文件列表查询请求
 *
 * @param pageNo         Integer 页码
 * @param pageSize       Integer 每页数量
 * @param sortBy         String 排序字段
 * @param sortOrder      String 排序方式（ASC/DESC）
 * @param relationType   String 关联类型
 * @param relationId     Long 关联对象ID
 * @param attachmentType String 附件类型
 * @param isPublic       Integer 是否公开
 * @param uploaderId     Long 上传人ID
 * @param keyword        String 模糊搜索关键字
 *
 * @author JiaWen.Wu
 * @className FileListQueryReq
 * @date 2025-11-11 17:20
 */
public record FileListQueryReq(
        Integer pageNo,
        Integer pageSize,
        String sortBy,
        String sortOrder,
        String relationType,
        Long relationId,
        String attachmentType,
        @PositiveOrZero(message = "是否公开标识不能为负数") Integer isPublic,
        Long uploaderId,
        String keyword) {

    public FileListQueryReq {
        pageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        sortOrder = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder).sortOrder();
        sortBy = StringUtils.hasText(sortBy) ? sortBy : null;
        relationType = StringUtils.hasText(relationType) ? relationType : null;
        attachmentType = StringUtils.hasText(attachmentType) ? attachmentType : null;
        keyword = StringUtils.hasText(keyword) ? keyword : null;
        isPublic = isPublic == null ? 0 : isPublic;
    }

    /**
     * 转换为基础分页请求
     */
    public BaseQueryReq toBaseQuery() {
        return BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
    }

    /**
     * 是否包含关键字
     */
    public boolean hasKeyword() {
        return StringUtils.hasText(keyword);
    }
}
