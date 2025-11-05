package com.sciz.server.domain.pojo.dto.response.declaration;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className DeclarationInfoResp
 * @date 2025-01-27 10:00
 */
@Data
public class DeclarationInfoResp {
    private Long id;
    private String declarationName;
    private String declarationType;
    private Long projectId;
    private String projectName;
    private Long applicantId;
    private String applicantName;
    private String content;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}
