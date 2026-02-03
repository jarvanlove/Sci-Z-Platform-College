package com.sciz.server.domain.pojo.dto.response.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 文件上传结果 DTO（通用）
 * 用于返回每个文件的上传状态和详细信息，支持进度显示
 *
 * @author JiaWen.Wu
 * @className FileUploadResp
 * @date 2026-01-28 14:30
 */
@Data
@Builder
@Schema(description = "文件上传结果")
public class FileUploadResp {

    /**
     * 文件名
     */
    @Schema(description = "文件名", example = "example.pdf")
    private String fileName;

    /**
     * 是否上传成功
     */
    @Schema(description = "是否上传成功", example = "true")
    private Boolean success;

    /**
     * 错误信息（失败时）
     */
    @Schema(description = "错误信息（失败时）", example = "文件类型不支持")
    private String errorMessage;

    /**
     * 附件ID（成功时）
     */
    @Schema(description = "附件ID（成功时）", example = "123")
    private Long attachmentId;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）", example = "1024")
    private Long fileSize;

    /**
     * 上传阶段
     * 0: 文件校验失败
     * 1: MinIO上传中
     * 2: MinIO上传完成
     * 3: 处理中（业务逻辑处理）
     * 4: 全部完成
     */
    @Schema(description = "上传阶段（0:校验失败, 1:MinIO上传中, 2:MinIO完成, 3:处理中, 4:全部完成）", example = "4")
    private Integer stage;

    /**
     * 阶段描述
     */
    @Schema(description = "阶段描述", example = "全部完成")
    private String stageDescription;
}
