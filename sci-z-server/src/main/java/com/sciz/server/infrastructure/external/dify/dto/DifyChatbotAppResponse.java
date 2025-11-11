package com.sciz.server.infrastructure.external.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Dify Chatbot 应用创建响应
 *
 * @author
 * @since 2025-11-10
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyChatbotAppResponse {

    private String id;
    private String name;
    private String description;
    private String mode;
    private String icon;

    @JsonProperty("icon_background")
    private String iconBackground;

    @JsonProperty("use_icon_as_answer_icon")
    private Boolean useIconAsAnswerIcon;

    @JsonProperty("enable_site")
    private Boolean enableSite;

    @JsonProperty("enable_api")
    private Boolean enableApi;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("updated_at")
    private Long updatedAt;

    @JsonProperty("access_mode")
    private String accessMode;

    private List<String> tags;

    @JsonProperty("model_config")
    private ModelConfig modelConfig;

    private Object workflow;
    private Object tracing;

    @JsonProperty("api_token")
    private String apiToken;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelConfig {

        @JsonProperty("opening_statement")
        private String openingStatement;

        @JsonProperty("suggested_questions")
        private List<String> suggestedQuestions;

        @JsonProperty("suggested_questions_after_answer")
        private ToggleFlag suggestedQuestionsAfterAnswer;

        @JsonProperty("speech_to_text")
        private ToggleFlag speechToText;

        @JsonProperty("text_to_speech")
        private ToggleFlag textToSpeech;

        @JsonProperty("retriever_resource")
        private ToggleFlag retrieverResource;

        @JsonProperty("annotation_reply")
        private ToggleFlag annotationReply;

        @JsonProperty("more_like_this")
        private ToggleFlag moreLikeThis;

        @JsonProperty("sensitive_word_avoidance")
        private SensitiveWordAvoidance sensitiveWordAvoidance;

        @JsonProperty("external_data_tools")
        private List<Object> externalDataTools;

        private Model model;

        @JsonProperty("user_input_form")
        private List<Object> userInputForm;

        @JsonProperty("dataset_query_variable")
        private String datasetQueryVariable;

        @JsonProperty("pre_prompt")
        private String prePrompt;

        @JsonProperty("agent_mode")
        private AgentMode agentMode;

        @JsonProperty("prompt_type")
        private String promptType;

        @JsonProperty("chat_prompt_config")
        private Map<String, Object> chatPromptConfig;

        @JsonProperty("completion_prompt_config")
        private Map<String, Object> completionPromptConfig;

        @JsonProperty("dataset_configs")
        private DatasetConfigs datasetConfigs;

        @JsonProperty("file_upload")
        private FileUpload fileUpload;

        @JsonProperty("created_by")
        private String createdBy;

        @JsonProperty("created_at")
        private Long createdAt;

        @JsonProperty("updated_by")
        private String updatedBy;

        @JsonProperty("updated_at")
        private Long updatedAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToggleFlag {
        private Boolean enabled;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SensitiveWordAvoidance {
        private Boolean enabled;
        private String type;
        private List<Object> configs;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Model {
        private String provider;
        private String name;
        private String mode;

        @JsonProperty("completion_params")
        private Map<String, Object> completionParams;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentMode {
        private Boolean enabled;
        private String strategy;
        private List<Object> tools;
        private String prompt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DatasetConfigs {
        @JsonProperty("retrieval_model")
        private String retrievalModel;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileUpload {
        private Image image;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private Boolean enabled;

        @JsonProperty("number_limits")
        private Integer numberLimits;

        private String detail;

        @JsonProperty("transfer_methods")
        private List<String> transferMethods;
    }
}

