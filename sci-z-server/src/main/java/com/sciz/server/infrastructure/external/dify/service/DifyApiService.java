package com.sciz.server.infrastructure.external.dify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.config.DifyDocumentConfig;
import com.sciz.server.infrastructure.external.dify.dto.*;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
import com.sciz.server.infrastructure.external.dify.util.DifyApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dify API 服务类
 * 使用 DifyApiClient 工具类进行 API 调用
 * 
 * @author shihang.shang
 * @since 2024-10-22
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DifyApiService {

    private final DifyApiClient difyApiClient;
    private final DifyConfig difyConfig;
    private final DifyDocumentConfig difyDocumentConfig;
    private final ObjectMapper objectMapper;
    private final DifyApiKeyServiceImpl difyApiKeyService;

    /**
     * 创建 Chatbot 应用
     */
    public ResponseEntity<DifyChatbotAppResponse> createChatbotApp(DifyChatbotAppRequest request) {
        try {
            ResponseEntity<String> response = difyApiClient.request("POST", "/console/api/apps", request,
                    request.getUserId(), request.getResourceId(), request.getKeyType(), 1);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ResponseEntity.status(response.getStatusCode()).body(null);
            }
            DifyChatbotAppResponse body = objectMapper.readValue(response.getBody(), DifyChatbotAppResponse.class);
            DifyChatbotAppApiKeyResponse apiKeyResponse = createChatbotAppApiKey(request, body);
            body.setApiToken(apiKeyResponse.getToken());
            persistChatbotMetadata(request, body, apiKeyResponse);
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (HttpClientErrorException e) {
            log.error("Dify Chatbot 创建失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (JsonProcessingException e) {
            log.error("Dify Chatbot 响应解析失败", e);
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            log.error("Dify Chatbot API Key 创建失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新 Chatbot 应用模型配置
     */
    public ResponseEntity<String> updateChatbotModelConfig(String appId, DifyChatbotModelConfigRequest config) {
        try {
            return difyApiClient.request("POST", "/console/api/apps/" + appId + "/model-config", config,
                    null, null, null, 1);
        } catch (HttpClientErrorException e) {
            log.error("更新 Chatbot 模型配置失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 发送 Chatbot 对话（支持阻塞与流式）
     */
    public ResponseEntity<String> sendChatbotMessage(DifyChatbotMessageRequest request) {
        try {
            return difyApiClient.request("POST", "/chat-messages", request,
                    request.getUserId(), request.getResourceId(), request.getKeyType());
        } catch (HttpClientErrorException e) {
            log.error("发送 Chatbot 对话失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 发送 Chatbot 流式对话（SSE）
     * 
     * @param request 对话请求
     * @param onData  数据回调函数，每收到一行数据时调用
     * @return 响应结果
     */
    public ResponseEntity<String> sendChatbotMessageStream(DifyChatbotMessageRequest request,
            java.util.function.Consumer<String> onData) {
        try {
            log.info(String.format("发送 Chatbot 流式对话请求: userId=%s, resourceId=%s, query=%s, responseMode=%s",
                    request.getUserId(), request.getResourceId(), request.getQuery(), request.getResponseMode()));

            // 调用普通 HTTP 请求获取完整响应体
            ResponseEntity<String> response = difyApiClient.requestStream("POST", "/chat-messages", request,
                    request.getUserId(), request.getResourceId(), request.getKeyType());

            log.info(String.format("Chatbot 流式对话响应: statusCode=%s, hasBody=%s, contentType=%s",
                    response.getStatusCode(), response.getBody() != null,
                    response.getHeaders().getContentType()));

            // 如果响应体不为空，解析 SSE 格式并逐行调用回调
            if (response.getBody() != null && onData != null) {
                String responseBody = response.getBody();
                log.debug(String.format("响应体长度: %d 字符", responseBody.length()));

                // 按行分割响应体（SSE 格式通常是按行分隔的）
                String[] lines = responseBody.split("\n");
                log.debug(String.format("响应体行数: %d", lines.length));

                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        onData.accept(trimmedLine);
                    }
                }
            } else {
                log.warn(String.format("响应体为空或回调函数为空: hasBody=%s, hasCallback=%s",
                        response.getBody() != null, onData != null));
            }

            return response;
        } catch (Exception e) {
            log.error(String.format("发送 Chatbot 流式对话失败: err=%s", e.getMessage()), e);
            throw e;
        }
    }

    private DifyChatbotAppApiKeyResponse createChatbotAppApiKey(DifyChatbotAppRequest request,
            DifyChatbotAppResponse appResponse) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", UUID.randomUUID().toString());
        payload.put("type", "app");
        payload.put("token", "app-" + UUID.randomUUID().toString().replace("-", ""));
        payload.put("created_at", Instant.now().getEpochSecond());
        payload.put("last_used_at", null);
        ResponseEntity<String> response = difyApiClient.request(
                "POST",
                "/console/api/apps/" + appResponse.getId() + "/api-keys",
                payload,
                request.getUserId(),
                request.getResourceId(),
                request.getKeyType(),
                1);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("创建 Chatbot API Key 失败，状态码: " + response.getStatusCode());
        }
        return objectMapper.readValue(response.getBody(), DifyChatbotAppApiKeyResponse.class);
    }

    private void persistChatbotMetadata(DifyChatbotAppRequest request, DifyChatbotAppResponse response,
            DifyChatbotAppApiKeyResponse apiKeyResponse) {
        DifyApiKey difyApiKey = new DifyApiKey();
        difyApiKey.setUserId(1L);
        difyApiKey.setKeyType("chatbot");
        difyApiKey.setResourceId(response.getId());
        difyApiKey.setApiKey(apiKeyResponse.getToken());
        difyApiKey.setKeyName("chatbot_" + UUID.randomUUID());
        difyApiKey.setDescription(response.getDescription());
        difyApiKey.setIsActive(Boolean.TRUE);
        difyApiKey.setCreatedBy("admin");
        difyApiKey.setUpdatedBy("admin");
        difyApiKeyService.save(difyApiKey);
    }

    /**
     * 获取所有数据集
     */
    public ResponseEntity<String> getDatasets(int page, int limit, Long userId, String resourceId, String keyType) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("limit", limit);
            return difyApiClient.request("GET", "/datasets", params, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            // 直接返回Dify的错误响应给前端
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 创建数据集
     */
    public ResponseEntity<String> createDataset(DifyDatasetRequest request, Long userId, String resourceId,
            String keyType) {
        try {
            return difyApiClient.request("POST", "/datasets", request, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            // 直接返回Dify的错误响应给前端
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 获取数据集详情
     */
    public ResponseEntity<String> getDataset(String datasetId, Long userId, String resourceId, String keyType) {
        try {
            return difyApiClient.request("GET", "/datasets/" + datasetId, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 更新数据集
     */
    public ResponseEntity<String> updateDataset(String datasetId, DifyDatasetRequest request, Long userId,
            String resourceId, String keyType) {
        try {
            return difyApiClient.request("PUT", "/datasets/" + datasetId, request, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 删除数据集
     */
    public ResponseEntity<String> deleteDataset(String datasetId, Long userId, String resourceId, String keyType) {
        try {
            return difyApiClient.request("DELETE", "/datasets/" + datasetId, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 删除文档
     *
     * @param datasetId  数据集ID
     * @param documentId 文档ID
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> deleteDocument(String datasetId, String documentId, Long userId, String resourceId,
            String keyType) {
        try {
            log.info(
                    String.format("删除 Dify 文档: datasetId=%s, documentId=%s, userId=%s", datasetId, documentId, userId));
            return difyApiClient.request("DELETE", "/datasets/" + datasetId + "/documents/" + documentId, userId,
                    resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error(String.format("Dify API调用失败: datasetId=%s, documentId=%s, err=%s", datasetId, documentId,
                    e.getMessage()));
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 检索知识库
     */
    public ResponseEntity<String> retrieveDataset(String datasetId, DifyRetrieveRequest request, Long userId,
            String resourceId, String keyType) {
        try {
            return difyApiClient.request("POST", "/datasets/" + datasetId + "/retrieve", request, userId, resourceId,
                    keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * 根据文件扩展名获取MIME类型
     */
    private String getMimeType(String ext) {
        switch (ext) {
            case ".txt":
                return "text/plain";
            case ".pdf":
                return "application/pdf";
            case ".doc":
                return "application/msword";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".md":
                return "text/markdown";
            case ".csv":
                return "text/csv";
            case ".json":
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 构建默认配置JSON
     * 使用配置文件中的文档处理配置
     */
    private String buildDefaultConfigJson() {
        Map<String, Object> config = new HashMap<>();

        // 基础配置
        config.put("indexing_technique", difyDocumentConfig.getIndexingTechnique());
        config.put("doc_form", difyDocumentConfig.getDocForm());
        config.put("doc_language", difyDocumentConfig.getDocLanguage());
        config.put("embedding_model", difyDocumentConfig.getEmbeddingModel());
        config.put("embedding_model_provider", difyDocumentConfig.getEmbeddingModelProvider());

        // 检索模型配置
        Map<String, Object> retrievalModel = new HashMap<>();
        retrievalModel.put("search_method", difyDocumentConfig.getRetrievalModel().getSearchMethod());
        retrievalModel.put("reranking_enable", difyDocumentConfig.getRetrievalModel().getRerankingEnable());
        retrievalModel.put("top_k", difyDocumentConfig.getRetrievalModel().getTopK());
        retrievalModel.put("score_threshold_enabled",
                difyDocumentConfig.getRetrievalModel().getScoreThresholdEnabled());
        retrievalModel.put("score_threshold", difyDocumentConfig.getRetrievalModel().getScoreThreshold());
        config.put("retrieval_model", retrievalModel);

        // 处理规则配置
        Map<String, Object> processRule = new HashMap<>();
        processRule.put("mode", difyDocumentConfig.getProcessRule().getMode());
        config.put("process_rule", processRule);

        try {
            return new ObjectMapper().writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException("构建默认配置JSON失败", e);
        }
    }

    /**
     * 上传文档到数据集（先存储文件，再调用Dify API）
     */
    public ResponseEntity<String> uploadDocumentWithFileStorage(String datasetId, MultipartFile file, Long userId,
            String resourceId, String keyType) {
        try {
            // 1. 先验证文件
            validateFile(file);

            // 2. 存储文件到本地
            String storedFilePath = storeFile(file);
            log.info("📁 文件已存储到: {}", storedFilePath);

            // 3. 从存储的文件创建新的MultipartFile并调用Dify API
            File storedFile = new File(storedFilePath);
            MultipartFile newMultipartFile = createMultipartFileFromFile(storedFile, file.getOriginalFilename());
            Map<String, Object> data = new HashMap<>();
            data.put("data", buildDefaultConfigJson());
            return difyApiClient.uploadFile("POST", "/datasets/" + datasetId + "/document/create-by-file",
                    newMultipartFile, data, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            // 直接返回Dify的错误响应给前端
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("文件存储并上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件存储并上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 存储文件到本地
     */
    private String storeFile(MultipartFile file) throws IOException {
        // 检查配置是否存在
        if (difyConfig.getUpload() == null) {
            throw new RuntimeException("Dify上传配置未设置，请在配置文件中添加 dify.upload 配置");
        }

        // 从配置文件获取上传目录，如果为空则使用默认值
        String uploadDir = difyConfig.getUpload().getDir();
        if (uploadDir == null || uploadDir.trim().isEmpty()) {
            uploadDir = "upload"; // 默认上传目录
            log.warn("⚠️ 未配置上传目录，使用默认目录: {}", uploadDir);
        }

        // 如果是相对路径，转换为绝对路径（相对于项目根目录）
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.isAbsolute()) {
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            uploadDirFile = new File(projectRoot, uploadDir);
            uploadDir = uploadDirFile.getAbsolutePath();
        }

        // 确保目录以/结尾（Windows使用\）
        String separator = File.separator;
        if (!uploadDir.endsWith("/") && !uploadDir.endsWith("\\")) {
            uploadDir += separator;
        }

        // 创建上传目录（如果不存在）
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("📁 创建上传目录: {}", uploadDir);
            } else {
                throw new IOException("无法创建上传目录: " + uploadDir);
            }
        } else if (!directory.isDirectory()) {
            throw new IOException("上传路径已存在但不是目录: " + uploadDir);
        }
        // 验证文件大小
        if (difyConfig.getUpload().getMaxFileSize() != null &&
                file.getSize() > difyConfig.getUpload().getMaxFileSize()) {
            throw new RuntimeException("文件大小超过限制: " + (difyConfig.getUpload().getMaxFileSize() / 1024 / 1024) + "MB");
        }
        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && difyConfig.getUpload().getAllowedExtensions() != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String[] allowedExts = difyConfig.getUpload().getAllowedExtensions().split(",");
            boolean isAllowed = false;
            for (String ext : allowedExts) {
                if (extension.equalsIgnoreCase(ext.trim())) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new RuntimeException(
                        "不支持的文件类型: " + extension + "，支持的类型: " + difyConfig.getUpload().getAllowedExtensions());
            }
        }
        // 生成唯一文件名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename : "file")
                + extension;

        // 存储文件
        String filePath = uploadDir + filename;
        File destFile = new File(filePath);
        file.transferTo(destFile);

        log.info("📁 文件已存储: {} (大小: {} KB)", filePath, file.getSize() / 1024);

        // 返回绝对路径
        return destFile.getAbsolutePath();
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
    }

    /**
     * 从文件创建MultipartFile
     */
    private MultipartFile createMultipartFileFromFile(File file, String originalFilename) throws IOException {
        byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
        String filename = originalFilename != null ? originalFilename : file.getName();

        // 获取文件扩展名和MIME类型
        String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        String mimeType = getMimeType(ext);

        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return mimeType;
            }

            @Override
            public boolean isEmpty() {
                return content.length == 0;
            }

            @Override
            public long getSize() {
                return content.length;
            }

            @Override
            public byte[] getBytes() {
                return content;
            }

            @Override
            public java.io.InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(content);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                java.nio.file.Files.write(dest.toPath(), content);
            }
        };
    }

    /**
     * 执行 Dify 工作流 - 阻塞模式
     *
     * @param request 工作流请求参数
     * @return 工作流执行结果
     */

    // ==================== 动态密钥相关方法 ====================

    /**
     * 使用动态密钥运行工作流
     *
     * @param request    工作流请求
     * @param userId     用户ID
     * @param workflowId 工作流ID
     * @return 工作流执行结果
     */
    public ResponseEntity<String> runWorkflowWithDynamicKey(DifyWorkflowRequest request, Long userId,
            String workflowId) {
        return runWorkflowWithDynamicKey(request, userId, workflowId, DifyApiKey.KeyType.WORKFLOW.getCode());
    }

    /**
     * 使用动态密钥执行工作流（支持指定 keyType）
     *
     * @param request    工作流请求
     * @param userId     用户ID
     * @param workflowId 工作流ID（用于查找 API Key，不放入请求体）
     * @param keyType    密钥类型（workflow/file）
     * @return 工作流执行结果
     */
    public ResponseEntity<String> runWorkflowWithDynamicKey(DifyWorkflowRequest request, Long userId, String workflowId,
            String keyType) {
        try {
            log.info("开始执行 Dify 工作流（动态密钥），用户: {}, 工作流ID: {}, keyType: {}", userId, workflowId, keyType);

            // 记录 inputs Map 的内容，便于排查参数名问题
            if (request.getInputs() != null) {
                log.info("工作流 inputs 参数（序列化前）: {}", request.getInputs());
                log.info("工作流 inputs 参数的所有 key: {}", request.getInputs().keySet());
            }

            Map<String, Object> bodymap = new HashMap<>();
            // 构建请求体：inputs, response_mode, user
            bodymap.put("inputs", request.getInputs());
            // 使用 responseMode 字段，如果为空则使用默认值
            String responseMode = request.getResponseMode() != null ? request.getResponseMode() : "blocking";
            bodymap.put("response_mode", responseMode);
            if (request.getUser() != null && !request.getUser().trim().isEmpty()) {
                bodymap.put("user", request.getUser());
            }
            // 注意：workflow_id 不放入请求体，仅用于查找 API Key
            String endpoint = "/workflows/run";

            Object body = bodymap;
            // 将 body 作为请求体传递，而不是查询参数
            ResponseEntity<String> response = difyApiClient.request("POST", endpoint, body,
                    userId, workflowId, keyType);
            log.info("Dify 工作流执行完成（动态密钥），状态码: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Dify 工作流执行失败（动态密钥）: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Dify 工作流执行异常（动态密钥）: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"error\": \"工作流执行异常: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 使用动态密钥获取工作流运行状态
     *
     * @param workflowRunId 工作流运行ID
     * @param userId        用户ID
     * @param workflowId    工作流ID
     * @return 工作流运行状态
     */
    public ResponseEntity<String> getWorkflowRunStatusWithDynamicKey(String workflowRunId, Long userId,
            String workflowId) {
        try {
            log.info("获取工作流运行状态（动态密钥），运行ID: {}, 用户: {}, 工作流ID: {}", workflowRunId, userId, workflowId);
            String endpoint = "/workflows/run/" + workflowRunId;
            ResponseEntity<String> response = difyApiClient.request("GET", endpoint,
                    userId, workflowId, DifyApiKey.KeyType.WORKFLOW.getCode());
            log.info("获取工作流运行状态完成（动态密钥），状态码: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("获取工作流运行状态失败（动态密钥）: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("获取工作流运行状态异常（动态密钥）: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"error\": \"获取工作流运行状态异常: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 使用动态密钥获取工作流日志
     *
     * @param page       页码
     * @param limit      每页数量
     * @param userId     用户ID
     * @param workflowId 工作流ID
     * @return 工作流日志
     */
    public ResponseEntity<String> getWorkflowLogsWithDynamicKey(Integer page, Integer limit, Long userId,
            String workflowId) {
        try {
            log.info("获取工作流日志（动态密钥），页码: {}, 每页数量: {}, 用户: {}, 工作流ID: {}", page, limit, userId, workflowId);
            Map<String, Object> params = new HashMap<>();
            if (page != null) {
                params.put("page", page);
            }
            if (limit != null) {
                params.put("limit", limit);
            }
            String endpoint = "/workflows/logs";
            ResponseEntity<String> response = difyApiClient.request("GET", endpoint, params,
                    userId, workflowId, DifyApiKey.KeyType.WORKFLOW.getCode());
            log.info("获取工作流日志完成（动态密钥），状态码: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("获取工作流日志失败（动态密钥）: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("获取工作流日志异常（动态密钥）: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"error\": \"获取工作流日志异常: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 使用动态密钥上传文件
     *
     * @param user       用户标识
     * @param file       上传的文件
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @return 文件上传结果
     */
    public ResponseEntity<String> uploadFileWithDynamicKey(Long user, MultipartFile file, Long userId,
            String resourceId) {
        return uploadFileWithDynamicKey(user, file, userId, resourceId, DifyApiKey.KeyType.DATASET.getCode());
    }

    /**
     * 使用动态密钥上传文件（支持指定 keyType）
     *
     * @param user       用户标识
     * @param file       上传的文件
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型（dataset/file）
     * @return 文件上传结果
     */
    public ResponseEntity<String> uploadFileWithDynamicKey(Long user, MultipartFile file, Long userId,
            String resourceId, String keyType) {
        try {
            log.info("开始上传文件到 Dify（动态密钥），用户: {}, 文件名: {}, 大小: {} bytes, 资源ID: {}, keyType: {}",
                    user, file.getOriginalFilename(), file.getSize(), resourceId, keyType);
            // 构建表单数据
            Map<String, Object> formData = new HashMap<>();
            formData.put("user", user);
            // 调用 Dify 文件上传 API
            String endpoint = "/files/upload";
            ResponseEntity<String> response = difyApiClient.uploadFile("POST", endpoint, file, formData,
                    userId, resourceId, keyType);
            log.info("文件上传完成（动态密钥），状态码: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("文件上传失败（动态密钥）: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("文件上传异常（动态密钥）: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"error\": \"文件上传异常: " + e.getMessage() + "\"}");
        }

    }
}