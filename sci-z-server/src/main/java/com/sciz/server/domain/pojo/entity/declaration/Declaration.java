package com.sciz.server.domain.pojo.entity.declaration;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className Declaration
 * @date 2025-10-29 10:00
 */
@Data
public class Declaration {
    private Long id;
    private String declarationName;
    private String declarationType;
    private Long projectId;
    private Long applicantId;
    private String content;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
