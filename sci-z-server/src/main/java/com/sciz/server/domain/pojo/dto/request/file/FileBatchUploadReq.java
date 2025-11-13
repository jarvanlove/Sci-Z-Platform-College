package com.sciz.server.domain.pojo.dto.request.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

/**
 * 批量文件上传请求
 *
 * @param files          MultipartFile[] 上传文件集合
 * @param relationType   String 关联类型（可选）
 * @param relationId     Long 关联对象ID（可选）
 * @param relationName   String 关联对象名称（可选）
 * @param attachmentType String 附件业务类型（可选）
 * @param isPublic       Integer 是否公开（0:私有,1:公开）
 *
 * @author JiaWen.Wu
 * @className FileBatchUploadReq
 * @date 2025-11-11 17:20
 */
public record FileBatchUploadReq(
        @NotEmpty(message = "上传文件列表不能为空") MultipartFile[] files,
        String relationType,
        Long relationId,
        String relationName,
        String attachmentType,
        @PositiveOrZero(message = "是否公开标识不能为负数") Integer isPublic) {

    public FileBatchUploadReq {
        files = files == null ? new MultipartFile[0] : files;
        relationType = StringUtils.hasText(relationType) ? relationType : null;
        relationName = StringUtils.hasText(relationName) ? relationName : null;
        attachmentType = StringUtils.hasText(attachmentType) ? attachmentType : null;
        isPublic = isPublic == null ? 0 : isPublic;
    }
}
