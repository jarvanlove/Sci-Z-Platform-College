package com.server.agentbackendservices.modules.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Dify 知识库检索请求 DTO
 * 用于封装检索知识库的请求参数
 *
 * @author jarvanlove
 * @since 2024-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyRetrieveRequest {
    
    /**
     * 检索关键词
     */
    @NotBlank(message = "检索关键词不能为空")
    private String query;
}
