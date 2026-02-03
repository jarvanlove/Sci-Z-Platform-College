package com.sciz.server.infrastructure.external.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import lombok.Data;

import java.util.*;

/**
 * Dify Chatbot 模型配置请求
 *
 * <p>配置项已从硬编码改为从配置文件读取，使用 fromConfig 方法从配置构建实例。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifyChatbotModelConfigRequest {

    @JsonProperty("pre_prompt")
    private String prePrompt;

    @JsonProperty("prompt_type")
    private String promptType;

    @JsonProperty("chat_prompt_config")
    private Map<String, Object> chatPromptConfig = new HashMap<>();

    @JsonProperty("completion_prompt_config")
    private Map<String, Object> completionPromptConfig = new HashMap<>();

    @JsonProperty("user_input_form")
    private List<Object> userInputForm = new ArrayList<>();

    @JsonProperty("dataset_query_variable")
    private String datasetQueryVariable;

    @JsonProperty("more_like_this")
    private ToggleFlag moreLikeThis;

    @JsonProperty("opening_statement")
    private String openingStatement;

    @JsonProperty("suggested_questions")
    private List<String> suggestedQuestions = new ArrayList<>();

    @JsonProperty("sensitive_word_avoidance")
    private SensitiveWordAvoidance sensitiveWordAvoidance;

    @JsonProperty("speech_to_text")
    private ToggleFlag speechToText;

    @JsonProperty("text_to_speech")
    private ToggleFlag textToSpeech;

    @JsonProperty("file_upload")
    private FileUpload fileUpload;

    @JsonProperty("suggested_questions_after_answer")
    private ToggleFlag suggestedQuestionsAfterAnswer;

    @JsonProperty("retriever_resource")
    private ToggleFlag retrieverResource;

    @JsonProperty("agent_mode")
    private AgentMode agentMode;

    @JsonProperty("model")
    private Model model;

    @JsonProperty("dataset_configs")
    private DatasetConfigs datasetConfigs;

    /**
     * 从配置构建 DifyChatbotModelConfigRequest 实例
     *
     * @param config Dify Chatbot 配置
     * @return DifyChatbotModelConfigRequest 实例
     */
    public static DifyChatbotModelConfigRequest fromConfig(DifyConfig.Chatbot config) {
        if (config == null) {
            return defaultConfig();
        }

        DifyChatbotModelConfigRequest request = new DifyChatbotModelConfigRequest();

        // 基础配置
        request.setPrePrompt(config.getPrePrompt());
        request.setPromptType(config.getPromptType() != null ? config.getPromptType() : "simple");
        request.setOpeningStatement(config.getOpeningStatement() != null ? config.getOpeningStatement() : "");
        request.setSuggestedQuestions(config.getSuggestedQuestions() != null ? config.getSuggestedQuestions() : new ArrayList<>());
        request.setDatasetQueryVariable(config.getDatasetQueryVariable() != null ? config.getDatasetQueryVariable() : "");

        // ToggleFlag 配置
        request.setMoreLikeThis(config.getMoreLikeThis() != null ? ToggleFlag.of(config.getMoreLikeThis()) : ToggleFlag.disabled());
        request.setSpeechToText(config.getSpeechToText() != null ? ToggleFlag.of(config.getSpeechToText()) : ToggleFlag.disabled());
        request.setTextToSpeech(config.getTextToSpeech() != null ? ToggleFlag.of(config.getTextToSpeech()) : ToggleFlag.disabled());
        request.setSuggestedQuestionsAfterAnswer(config.getSuggestedQuestionsAfterAnswer() != null ? ToggleFlag.of(config.getSuggestedQuestionsAfterAnswer()) : ToggleFlag.disabled());
        request.setRetrieverResource(config.getRetrieverResource() != null ? ToggleFlag.of(config.getRetrieverResource()) : ToggleFlag.enabled());

        // 敏感词规避配置
        if (config.getSensitiveWordAvoidance() != null) {
            DifyConfig.Chatbot.SensitiveWordAvoidance swa = config.getSensitiveWordAvoidance();
            request.setSensitiveWordAvoidance(new SensitiveWordAvoidance(
                    swa.getEnabled() != null ? swa.getEnabled() : false,
                    swa.getType() != null ? swa.getType() : "",
                    swa.getConfigs() != null ? swa.getConfigs() : new ArrayList<>()
            ));
        } else {
            request.setSensitiveWordAvoidance(SensitiveWordAvoidance.defaultConfig());
        }

        // 文件上传配置
        if (config.getFileUpload() != null) {
            DifyConfig.Chatbot.FileUpload fu = config.getFileUpload();
            FileUpload fileUpload = new FileUpload();
            fileUpload.setEnabled(fu.getEnabled() != null ? fu.getEnabled() : false);
            fileUpload.setAllowedFileTypes(fu.getAllowedFileTypes() != null ? fu.getAllowedFileTypes() : new ArrayList<>());
            fileUpload.setAllowedFileExtensions(fu.getAllowedFileExtensions() != null ? fu.getAllowedFileExtensions() : new ArrayList<>());
            fileUpload.setAllowedFileUploadMethods(fu.getAllowedFileUploadMethods() != null ? fu.getAllowedFileUploadMethods() : new ArrayList<>());
            fileUpload.setNumberLimits(fu.getNumberLimits() != null ? fu.getNumberLimits() : 3);

            if (fu.getImage() != null) {
                DifyConfig.Chatbot.FileUpload.Image img = fu.getImage();
                FileUpload.Image image = new FileUpload.Image();
                image.setEnabled(img.getEnabled() != null ? img.getEnabled() : false);
                image.setDetail(img.getDetail() != null ? img.getDetail() : "high");
                image.setNumberLimits(img.getNumberLimits() != null ? img.getNumberLimits() : 3);
                image.setTransferMethods(img.getTransferMethods() != null ? img.getTransferMethods() : new ArrayList<>());
                fileUpload.setImage(image);
            } else {
                fileUpload.setImage(FileUpload.Image.defaultConfig());
            }
            request.setFileUpload(fileUpload);
        } else {
            request.setFileUpload(FileUpload.defaultConfig());
        }

        // Agent模式配置
        if (config.getAgentMode() != null) {
            DifyConfig.Chatbot.AgentMode am = config.getAgentMode();
            request.setAgentMode(new AgentMode(
                    am.getEnabled() != null ? am.getEnabled() : false,
                    am.getMaxIteration() != null ? am.getMaxIteration() : 10,
                    am.getStrategy() != null ? am.getStrategy() : "function_call",
                    am.getTools() != null ? am.getTools() : new ArrayList<>()
            ));
        } else {
            request.setAgentMode(AgentMode.defaultConfig());
        }

        // 模型配置
        if (config.getModel() != null) {
            DifyConfig.Chatbot.Model modelConfig = config.getModel();
            Map<String, Object> completionParams = new HashMap<>();
            // 设置 enable_search，默认值为 false
            if (modelConfig.getCompletionParams() != null && modelConfig.getCompletionParams().getEnableSearch() != null) {
                completionParams.put("enable_search", modelConfig.getCompletionParams().getEnableSearch());
            } else {
                completionParams.put("enable_search", false);
            }
            request.setModel(new Model(
                    modelConfig.getProvider() != null ? modelConfig.getProvider() : "langgenius/tongyi/tongyi",
                    modelConfig.getName() != null ? modelConfig.getName() : "qwen3-next-80b-a3b-instruct",
                    modelConfig.getMode() != null ? modelConfig.getMode() : "chat",
                    completionParams
            ));
        } else {
            // 使用默认配置，包含 enable_search: false
            Map<String, Object> defaultCompletionParams = new HashMap<>();
            defaultCompletionParams.put("enable_search", false);
            Model defaultModel = Model.defaultConfig();
            defaultModel.setCompletionParams(defaultCompletionParams);
            request.setModel(defaultModel);
        }

        // 数据集配置
        if (config.getDatasetConfigs() != null) {
            DifyConfig.Chatbot.DatasetConfigs dc = config.getDatasetConfigs();
            DatasetConfigs datasetConfigs = new DatasetConfigs();
            datasetConfigs.setRetrievalModel(dc.getRetrievalModel() != null ? dc.getRetrievalModel() : "multiple");
            datasetConfigs.setTopK(dc.getTopK() != null ? dc.getTopK() : 4);
            datasetConfigs.setRerankingMode(dc.getRerankingMode() != null ? dc.getRerankingMode() : "reranking_model");
            datasetConfigs.setRerankingEnable(dc.getRerankingEnable() != null ? dc.getRerankingEnable() : false);

            // 重排序模型配置
            if (config.getRerankingModel() != null) {
                datasetConfigs.setRerankingModel(new DatasetConfigs.RerankingModel(
                        config.getRerankingModel().getRerankingProviderName() != null ? config.getRerankingModel().getRerankingProviderName() : "langgenius/tongyi/tongyi",
                        config.getRerankingModel().getRerankingModelName() != null ? config.getRerankingModel().getRerankingModelName() : "gte-rerank"
                ));
            } else {
                datasetConfigs.setRerankingModel(DatasetConfigs.RerankingModel.defaultConfig());
            }

            // 数据集集合配置
            if (dc.getDatasets() != null && dc.getDatasets().getDatasets() != null && !dc.getDatasets().getDatasets().isEmpty()) {
                List<DatasetConfigs.DatasetCollection.DatasetWrapper> datasetList = new ArrayList<>();
                for (DifyConfig.Chatbot.DatasetConfigs.DatasetCollection.DatasetWrapper wrapper : dc.getDatasets().getDatasets()) {
                    if (wrapper.getDataset() != null) {
                        DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset dataset = new DatasetConfigs.DatasetCollection.DatasetWrapper.Dataset(
                                wrapper.getDataset().getEnabled() != null ? wrapper.getDataset().getEnabled() : true,
                                wrapper.getDataset().getId() != null ? wrapper.getDataset().getId() : ""
                        );
                        DatasetConfigs.DatasetCollection.DatasetWrapper datasetWrapper = new DatasetConfigs.DatasetCollection.DatasetWrapper(dataset);
                        datasetList.add(datasetWrapper);
                    }
                }
                DatasetConfigs.DatasetCollection datasetCollection = new DatasetConfigs.DatasetCollection(datasetList);
                datasetConfigs.setDatasets(datasetCollection);
            } else {
                datasetConfigs.setDatasets(DatasetConfigs.DatasetCollection.defaultConfig());
            }

            request.setDatasetConfigs(datasetConfigs);
        } else {
            request.setDatasetConfigs(DatasetConfigs.defaultConfig());
        }

        return request;
    }

    /**
     * 创建默认配置（向后兼容，使用硬编码默认值）
     *
     * @return DifyChatbotModelConfigRequest 实例
     */
    public static DifyChatbotModelConfigRequest defaultConfig() {
        DifyChatbotModelConfigRequest request = new DifyChatbotModelConfigRequest();
        request.setPrePrompt("你叫小域，是一位专业、严谨的学术助手，专注于科研与教学支持。请以清晰、准确、逻辑严密的方式回答问题，语言应符合学术规范，避免口语化、夸张或情绪化表达。若涉及数据、方法或结论，请注明假设条件与适用范围；若问题存在歧义或信息不足，请明确指出并请求澄清；优先使用正式术语，必要时可提供定义或简要解释；不使用表情符号、颜文字或非必要的修饰性语言；回答应结构清晰，可适当使用标题、编号或表格以提升可读性，但需保持内容精炼。请根据上述原则，就用户提出的问题提供专业、可靠、有依据的回应。");
        request.setPromptType("simple");
        request.setOpeningStatement("");
        request.setSuggestedQuestions(new ArrayList<>());
        request.setDatasetQueryVariable("");
        request.setMoreLikeThis(ToggleFlag.disabled());
        request.setSensitiveWordAvoidance(SensitiveWordAvoidance.defaultConfig());
        request.setSpeechToText(ToggleFlag.disabled());
        request.setTextToSpeech(ToggleFlag.disabled());
        request.setFileUpload(FileUpload.defaultConfig());
        request.setSuggestedQuestionsAfterAnswer(ToggleFlag.disabled());
        request.setRetrieverResource(ToggleFlag.enabled());
        request.setAgentMode(AgentMode.defaultConfig());
        request.setModel(Model.defaultConfig());
        request.setDatasetConfigs(DatasetConfigs.defaultConfig());
        return request;
    }

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

        /**
         * 根据布尔值创建 ToggleFlag
         *
         * @param enabled 是否启用
         * @return ToggleFlag 实例
         */
        public static ToggleFlag of(boolean enabled) {
            return new ToggleFlag(enabled);
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
            Map<String, Object> completionParams = new HashMap<>();
            completionParams.put("enable_search", false);
            return new Model(
                    "langgenius/tongyi/tongyi",
                    "qwen3-next-80b-a3b-instruct",
                    "chat",
                    completionParams
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
                        return new Dataset(true, "");
                    }
                }
            }
        }
    }
}

