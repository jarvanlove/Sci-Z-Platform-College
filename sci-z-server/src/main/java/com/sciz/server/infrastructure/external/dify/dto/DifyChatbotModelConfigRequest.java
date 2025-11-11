package com.sciz.server.infrastructure.external.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.*;

/**
 * Dify Chatbot 模型配置请求
 *
 * <p>默认按照控制台生成的配置初始化各项字段，便于直接提交。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifyChatbotModelConfigRequest {

    @JsonProperty("pre_prompt")
    private String prePrompt = "";

    @JsonProperty("prompt_type")
    private String promptType = "simple";

    @JsonProperty("chat_prompt_config")
    private Map<String, Object> chatPromptConfig = new HashMap<>();

    @JsonProperty("completion_prompt_config")
    private Map<String, Object> completionPromptConfig = new HashMap<>();

    @JsonProperty("user_input_form")
    private List<Object> userInputForm = new ArrayList<>();

    @JsonProperty("dataset_query_variable")
    private String datasetQueryVariable = "";

    @JsonProperty("more_like_this")
    private ToggleFlag moreLikeThis = ToggleFlag.disabled();

    @JsonProperty("opening_statement")
    private String openingStatement = "";

    @JsonProperty("suggested_questions")
    private List<String> suggestedQuestions = new ArrayList<>();

    @JsonProperty("sensitive_word_avoidance")
    private SensitiveWordAvoidance sensitiveWordAvoidance = SensitiveWordAvoidance.defaultConfig();

    @JsonProperty("speech_to_text")
    private ToggleFlag speechToText = ToggleFlag.disabled();

    @JsonProperty("text_to_speech")
    private ToggleFlag textToSpeech = ToggleFlag.disabled();

    @JsonProperty("file_upload")
    private FileUpload fileUpload = FileUpload.defaultConfig();

    @JsonProperty("suggested_questions_after_answer")
    private ToggleFlag suggestedQuestionsAfterAnswer = ToggleFlag.disabled();

    @JsonProperty("retriever_resource")
    private ToggleFlag retrieverResource = ToggleFlag.enabled();

    @JsonProperty("agent_mode")
    private AgentMode agentMode = AgentMode.defaultConfig();

    @JsonProperty("model")
    private Model model = Model.defaultConfig();

    @JsonProperty("dataset_configs")
    private DatasetConfigs datasetConfigs = DatasetConfigs.defaultConfig();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToggleFlag {
        private Boolean enabled;

        public ToggleFlag() {
        }

        private ToggleFlag(boolean enabled) {
            this.enabled = enabled;
        }

        public static ToggleFlag enabled() {
            return new ToggleFlag(true);
        }

        public static ToggleFlag disabled() {
            return new ToggleFlag(false);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SensitiveWordAvoidance {
        private Boolean enabled;
        private String type;
        private List<Object> configs;

        public SensitiveWordAvoidance() {
        }

        private SensitiveWordAvoidance(Boolean enabled, String type, List<Object> configs) {
            this.enabled = enabled;
            this.type = type;
            this.configs = configs;
        }

        public static SensitiveWordAvoidance defaultConfig() {
            return new SensitiveWordAvoidance(false, "", new ArrayList<>());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileUpload {
        private Boolean enabled;

        @JsonProperty("allowed_file_types")
        private List<String> allowedFileTypes;

        @JsonProperty("allowed_file_extensions")
        private List<String> allowedFileExtensions;

        @JsonProperty("allowed_file_upload_methods")
        private List<String> allowedFileUploadMethods;

        @JsonProperty("number_limits")
        private Integer numberLimits;

        private Image image;

        public FileUpload() {
        }

        private FileUpload(Boolean enabled,
                           List<String> allowedFileTypes,
                           List<String> allowedFileExtensions,
                           List<String> allowedFileUploadMethods,
                           Integer numberLimits,
                           Image image) {
            this.enabled = enabled;
            this.allowedFileTypes = allowedFileTypes;
            this.allowedFileExtensions = allowedFileExtensions;
            this.allowedFileUploadMethods = allowedFileUploadMethods;
            this.numberLimits = numberLimits;
            this.image = image;
        }

        public static FileUpload defaultConfig() {
            return new FileUpload(
                    false,
                    new ArrayList<>(),
                    new ArrayList<>(Arrays.asList(
                            ".JPG", ".JPEG", ".PNG", ".GIF", ".WEBP", ".SVG",
                            ".MP4", ".MOV", ".MPEG", ".WEBM")),
                    new ArrayList<>(Arrays.asList("remote_url", "local_file")),
                    3,
                    Image.defaultConfig()
            );
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Image {
            private Boolean enabled;

            @JsonProperty("detail")
            private String detail;

            @JsonProperty("number_limits")
            private Integer numberLimits;

            @JsonProperty("transfer_methods")
            private List<String> transferMethods;

            public Image() {
            }

            private Image(Boolean enabled, String detail, Integer numberLimits, List<String> transferMethods) {
                this.enabled = enabled;
                this.detail = detail;
                this.numberLimits = numberLimits;
                this.transferMethods = transferMethods;
            }

            public static Image defaultConfig() {
                return new Image(
                        false,
                        "high",
                        3,
                        new ArrayList<>(Arrays.asList("remote_url", "local_file"))
                );
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentMode {
        private Boolean enabled;

        @JsonProperty("max_iteration")
        private Integer maxIteration;

        private String strategy;
        private List<Object> tools;

        public AgentMode() {
        }

        private AgentMode(Boolean enabled, Integer maxIteration, String strategy, List<Object> tools) {
            this.enabled = enabled;
            this.maxIteration = maxIteration;
            this.strategy = strategy;
            this.tools = tools;
        }

        public static AgentMode defaultConfig() {
            return new AgentMode(false, 10, "function_call", new ArrayList<>());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Model {
        private String provider;
        private String name;
        private String mode;

        @JsonProperty("completion_params")
        private Map<String, Object> completionParams;

        public Model() {
        }

        private Model(String provider, String name, String mode, Map<String, Object> completionParams) {
            this.provider = provider;
            this.name = name;
            this.mode = mode;
            this.completionParams = completionParams;
        }

        public static Model defaultConfig() {
            return new Model(
                    "langgenius/tongyi/tongyi",
                    "qwen3-next-80b-a3b-instruct",
                    "chat",
                    new HashMap<>()
            );
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DatasetConfigs {
        @JsonProperty("retrieval_model")
        private String retrievalModel;

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("reranking_mode")
        private String rerankingMode;

        @JsonProperty("reranking_model")
        private RerankingModel rerankingModel;

        @JsonProperty("reranking_enable")
        private Boolean rerankingEnable;

        @JsonProperty("datasets")
        private DatasetCollection datasets;

        public DatasetConfigs() {
        }

        private DatasetConfigs(String retrievalModel,
                               Integer topK,
                               String rerankingMode,
                               RerankingModel rerankingModel,
                               Boolean rerankingEnable,
                               DatasetCollection datasets) {
            this.retrievalModel = retrievalModel;
            this.topK = topK;
            this.rerankingMode = rerankingMode;
            this.rerankingModel = rerankingModel;
            this.rerankingEnable = rerankingEnable;
            this.datasets = datasets;
        }

        public static DatasetConfigs defaultConfig() {
            return new DatasetConfigs(
                    "multiple",
                    4,
                    "reranking_model",
                    RerankingModel.defaultConfig(),
                    false,
                    DatasetCollection.defaultConfig()
            );
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RerankingModel {
            @JsonProperty("reranking_provider_name")
            private String rerankingProviderName;

            @JsonProperty("reranking_model_name")
            private String rerankingModelName;

            public RerankingModel() {
            }

            private RerankingModel(String rerankingProviderName, String rerankingModelName) {
                this.rerankingProviderName = rerankingProviderName;
                this.rerankingModelName = rerankingModelName;
            }

            public static RerankingModel defaultConfig() {
                return new RerankingModel("langgenius/tongyi/tongyi", "gte-rerank");
            }
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DatasetCollection {
            @JsonProperty("datasets")
            private List<DatasetWrapper> datasets;

            public DatasetCollection() {
            }

            private DatasetCollection(List<DatasetWrapper> datasets) {
                this.datasets = datasets;
            }

            public static DatasetCollection defaultConfig() {
                List<DatasetWrapper> list = new ArrayList<>();
                list.add(DatasetWrapper.defaultConfig());
                return new DatasetCollection(list);
            }

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class DatasetWrapper {
                private Dataset dataset;

                public DatasetWrapper() {
                }

                private DatasetWrapper(Dataset dataset) {
                    this.dataset = dataset;
                }

                public static DatasetWrapper defaultConfig() {
                    return new DatasetWrapper(Dataset.defaultConfig());
                }

                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Dataset {
                    private Boolean enabled;
                    private String id;

                    public Dataset() {
                    }

                    private Dataset(Boolean enabled, String id) {
                        this.enabled = enabled;
                        this.id = id;
                    }

                    public static Dataset defaultConfig() {
                        return new Dataset(true, "f3ddc6e8-7688-4fb3-b1e0-02ac38e3b651");
                    }
                }
            }
        }
    }
}

