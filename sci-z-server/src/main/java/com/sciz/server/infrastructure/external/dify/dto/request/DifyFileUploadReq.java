package com.sciz.server.infrastructure.external.dify.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dify文件上传请求DTO
 *
 * @author JiaWen.Wu
 * @className DifyFileUploadReq
 * @date 2025-10-29 10:30
 */
@Data
public class DifyFileUploadReq {

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;

    /**
     * 上传文件
     */
    private MultipartFile file;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 是否自动解析
     */
    private Boolean autoParse = true;

    /**
     * 处理模式
     */
    private String processMode = "automatic";
}
