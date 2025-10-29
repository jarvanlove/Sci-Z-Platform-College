package com.sciz.server.domain.pojo.entity.declaration;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className DeclarationTemplate
 * @date 2025-10-29 10:00
 */
@Data
public class DeclarationTemplate {
    private Long id;
    private String templateName;
    private String templateType;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
