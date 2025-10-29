package com.sciz.server.domain.pojo.dto.request.declaration;

import lombok.Data;

/**
 * @author JiaWen.Wu
 * @className DeclarationSubmitReq
 * @date 2025-10-29 10:00
 */
@Data
public class DeclarationSubmitReq {
    private String declarationName;
    private String declarationType;
    private Long projectId;
    private String content;
}
