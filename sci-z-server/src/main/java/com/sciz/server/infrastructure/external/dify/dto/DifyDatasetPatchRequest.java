package com.sciz.server.infrastructure.external.dify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dify 修改知识库详情 PATCH 请求体
 * 对应官方文档：PATCH /datasets/{dataset_id}，仅包含需同步的字段（名称、描述）
 *
 * @author platform
 * @see <a href="https://docs.dify.ai/api-reference/datasets/update-dataset">修改知识库详情</a>
 */
/**
 * Dify 修改知识库详情 PATCH 请求体
 *
 * @author Jiawen.Wu
 * @className DifyDatasetPatchRequest
 * @date 2025-02-05 15:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifyDatasetPatchRequest {

    /**
     * 知识库名称（选填）
     */
    private String name;

    /**
     * 知识库描述（选填）
     */
    private String description;
}
