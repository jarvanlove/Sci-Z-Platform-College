package com.sciz.server.infrastructure.external.dify.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dify 工作流 Draft 配置响应
 * 用于获取和更新工作流的 draft 版本配置
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowDraftResp
 * @date 2025-12-08 15:00
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyWorkflowDraftResp {

    /**
     * 工作流ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 工作流图配置（包含 nodes 和 edges）
     */
    @JsonProperty("graph")
    private Map<String, Object> graph;

    /**
     * 工作流特性配置
     */
    @JsonProperty("features")
    private Map<String, Object> features;

    /**
     * 工作流配置的 hash 值（用于版本控制，GET 和 POST 必须一致）
     */
    @JsonProperty("hash")
    private String hash;

    /**
     * 版本（draft/published）
     */
    @JsonProperty("version")
    private String version;

    /**
     * 标记名称
     */
    @JsonProperty("marked_name")
    private String markedName;

    /**
     * 标记注释
     */
    @JsonProperty("marked_comment")
    private String markedComment;

    /**
     * 创建人信息
     */
    @JsonProperty("created_by")
    private Map<String, Object> createdBy;

    /**
     * 创建时间（时间戳）
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 更新人信息
     */
    @JsonProperty("updated_by")
    private Map<String, Object> updatedBy;

    /**
     * 更新时间（时间戳）
     */
    @JsonProperty("updated_at")
    private Long updatedAt;

    /**
     * 工具是否已发布
     */
    @JsonProperty("tool_published")
    private Boolean toolPublished;

    /**
     * 环境变量
     */
    @JsonProperty("environment_variables")
    private List<Map<String, Object>> environmentVariables;

    /**
     * 对话变量
     */
    @JsonProperty("conversation_variables")
    private List<Map<String, Object>> conversationVariables;

    /**
     * 存储其他未知字段
     */
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * 获取所有属性（包括未知字段）
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * 设置未知字段
     */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    /**
     * 更新 knowledge-retrieval 类型节点的 dataset_ids
     * 更新所有 type = "knowledge-retrieval" 的节点的 dataset_ids
     *
     * @param datasetIds 新的知识库ID列表
     * @return 更新的节点数量
     */
    @SuppressWarnings("unchecked")
    public int updateKnowledgeRetrievalDatasetIds(List<String> datasetIds) {
        if (graph == null) {
            return 0;
        }

        Object nodesObj = graph.get("nodes");
        if (!(nodesObj instanceof List)) {
            return 0;
        }

        List<Map<String, Object>> nodes = (List<Map<String, Object>>) nodesObj;
        int updatedCount = 0;
        for (Map<String, Object> node : nodes) {
            Object dataObj = node.get("data");
            if (dataObj instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) dataObj;
                Object typeObj = data.get("type");
                if ("knowledge-retrieval".equals(typeObj)) {
                    // 更新 dataset_ids（使用传入的知识库ID列表）
                    data.put("dataset_ids", datasetIds != null ? datasetIds : List.of());
                    updatedCount++;
                }
            }
        }
        return updatedCount;
    }
}

