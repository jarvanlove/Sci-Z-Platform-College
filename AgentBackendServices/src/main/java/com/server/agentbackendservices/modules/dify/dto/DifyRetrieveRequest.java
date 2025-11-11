package com.server.agentbackendservices.modules.dify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dify 知识库检索请求 DTO
 * 用于封装检索知识库的请求参数
 *
 * @author shihang.shang
 * @since 2024-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyRetrieveRequest extends BaseDifyRequest {
    /**
     * 检索关键词
     */
    @NotBlank(message = "检索关键词不能为空")
    private String query;
}