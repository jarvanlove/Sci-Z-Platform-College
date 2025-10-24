package com.server.agentbackendservices.modules.dify.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Dify 数据集请求DTO
 * 根据 Dify API 文档设计
 * 
 * @author jarvanlove
 * @since 2024-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyDatasetRequest {
    
    /**
     * 数据集名称
     */
    @NotBlank(message = "数据集名称不能为空")
    private String name;
    
    /**
     * 数据集描述
     */
    private String description;
    
    /**
     * 权限设置
     */
    @Builder.Default
    private String permission = "only_me";
    
    /**
     * 索引技术
     */
    @Builder.Default
    private String indexing_technique = "high_quality";
}
