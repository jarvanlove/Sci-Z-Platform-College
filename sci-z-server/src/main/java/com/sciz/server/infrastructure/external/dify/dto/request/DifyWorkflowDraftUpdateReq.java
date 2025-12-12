package com.sciz.server.infrastructure.external.dify.dto.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dify 工作流 Draft 配置更新请求
 * 用于更新工作流的 draft 版本配置，主要修改 knowledge-retrieval 节点的 dataset_ids
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowDraftUpdateReq
 * @date 2025-12-08 15:00
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyWorkflowDraftUpdateReq {

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
     * 工作流配置的 hash 值（必须与 GET 返回的 hash 值一致）
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
}

