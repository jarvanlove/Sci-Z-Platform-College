package com.sciz.server.domain.pojo.dto.request.file;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 单文件上传请求
 *
 * @author JiaWen.Wu
 * @className FileUploadReq
 * @date 2025-11-12 17:20
 */
@Getter
@Setter
public class FileUploadReq {

    /**
     * 上传文件
     */
    @NotNull(message = "上传文件不能为空")
    private MultipartFile file;

    /**
     * 关联类型（可选）
     */
    private String relationType;

    /**
     * 关联对象ID（可选）
     */
    private Long relationId;

    /**
     * 关联对象名称（可选）
     */
    private String relationName;

    /**
     * 附件业务类型（可选）
     */
    private String attachmentType;

    /**
     * 是否公开（0:私有,1:公开）
     */
    @PositiveOrZero(message = "是否公开标识不能为负数")
    private Integer isPublic = 0;
}
