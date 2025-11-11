package com.server.agentbackendservices.modules.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Dify Chatbot 应用 API Key 响应
 *
 * @author
 * @since 2025-11-10
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyChatbotAppApiKeyResponse {

    private String id;
    private String type;
    private String token;

    @JsonProperty("last_used_at")
    private Long lastUsedAt;

    @JsonProperty("created_at")
    private Long createdAt;
}

