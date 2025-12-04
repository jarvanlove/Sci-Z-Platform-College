package com.sciz.server.infrastructure.external.dify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.infrastructure.external.dify.config.DifyDocumentConfig;
import com.sciz.server.infrastructure.external.dify.dto.*;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.service.impl.DifyApiKeyServiceImpl;
import com.sciz.server.infrastructure.external.dify.util.DifyApiClient;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
    private final DifyDocumentConfig difyDocumentConfig;
    private final ObjectMapper objectMapper;
    private final DifyApiKeyServiceImpl difyApiKeyService;

    @Autowired
    @Qualifier("globalTaskExecutor")
    private Executor globalTaskExecutor;

    /**
     * 每批上传的文件数量
     */
    private static final int BATCH_SIZE = 5;

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
     * 异步上传文档到数据集（支持单个或多个文件，分批上传）
     * 
     * @param datasetId  数据集ID
     * @param files      文件列表（支持单个或多个文件，可以传入单个文件或文件数组）
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return CompletableFuture，包含所有上传结果的列表
     */
    public CompletableFuture<List<ResponseEntity<String>>> uploadDocumentsAsync(String datasetId,
            List<MultipartFile> files, Long userId, String resourceId, String keyType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 验证并过滤文件
                List<MultipartFile> validFiles = validateAndFilterFiles(files);

                if (validFiles.isEmpty()) {
                    log.warn("没有有效的文件需要上传");
                    return new ArrayList<>();
                }

                int totalFiles = validFiles.size();
                log.info("开始异步上传文档到 Dify API: datasetId={}, totalFiles={}, userId={}",
                        datasetId, totalFiles, userId);
                // 2. 分批上传文件
                List<ResponseEntity<String>> results = new ArrayList<>();
                List<List<MultipartFile>> batches = partitionFiles(validFiles, BATCH_SIZE);

                log.info("文件将分为 {} 批上传，每批最多 {} 个文件", batches.size(), BATCH_SIZE);

                for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
                    List<MultipartFile> batch = batches.get(batchIndex);
                    log.info("开始上传第 {}/{} 批文件，本批文件数: {}",
                            batchIndex + 1, batches.size(), batch.size());

                    // 3. 批量上传当前批次
                    List<CompletableFuture<ResponseEntity<String>>> batchFutures = new ArrayList<>();
                    for (MultipartFile file : batch) {
                        CompletableFuture<ResponseEntity<String>> future = CompletableFuture.supplyAsync(() -> {
                            try {
                                return uploadSingleDocument(datasetId, file, userId, resourceId, keyType);
                            } catch (Exception e) {
                                log.error("上传文件失败: fileName={}, error={}",
                                        file.getOriginalFilename(), e.getMessage(), e);
                                return ResponseEntity.status(500)
                                        .body("{\"error\": \"上传文件失败: " + e.getMessage() + "\"}");
                            }
                        }, globalTaskExecutor);
                        batchFutures.add(future);
                    }
                    // 4. 等待当前批次所有文件上传完成
                    CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();

                    // 5. 收集当前批次的结果
                    for (CompletableFuture<ResponseEntity<String>> future : batchFutures) {
                        try {
                            results.add(future.get());
                        } catch (Exception e) {
                            log.error("获取上传结果失败: error={}", e.getMessage(), e);
                            results.add(ResponseEntity.status(500)
                                    .body("{\"error\": \"获取上传结果失败: " + e.getMessage() + "\"}"));
                        }
                    }

                    log.info("第 {}/{} 批文件上传完成", batchIndex + 1, batches.size());
                }

                log.info("所有文件上传完成: datasetId={}, totalFiles={}, successCount={}",
                        datasetId, totalFiles, results.stream()
                                .filter(r -> r.getStatusCode().is2xxSuccessful())
                                .count());

                return results;
            } catch (Exception e) {
                log.error("异步上传文档失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
                throw new RuntimeException("异步上传文档失败: " + e.getMessage(), e);
            }
        }, globalTaskExecutor);
    }

    /**
     * 异步上传文档到数据集（支持单个文件，便捷方法）
     * 
     * @param datasetId  数据集ID
     * @param file       单个文件
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return CompletableFuture，包含上传结果
     */
    public CompletableFuture<ResponseEntity<String>> uploadDocumentAsync(String datasetId,
            MultipartFile file, Long userId, String resourceId, String keyType) {
        List<MultipartFile> files = Arrays.asList(file);
        return uploadDocumentsAsync(datasetId, files, userId, resourceId, keyType)
                .thenApply(results -> results.isEmpty() ? ResponseEntity.status(400).body("{\"error\": \"上传失败\"}")
                        : results.get(0));
    }

    /**
     * 上传单个文档（内部方法）
     */
    private ResponseEntity<String> uploadSingleDocument(String datasetId, MultipartFile file,
            Long userId, String resourceId, String keyType) {
        try {
            // 验证文件
            validateFile(file);

            // 构建表单数据
            Map<String, Object> data = new HashMap<>();
            data.put("data", buildDefaultConfigJson());

            log.info("上传单个文件到 Dify API: fileName={}, size={}",
                    file.getOriginalFilename(), file.getSize());

            return difyApiClient.uploadFile("POST", "/datasets/" + datasetId + "/document/create-by-file",
                    file, data, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: fileName={}, error={}",
                    file.getOriginalFilename(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("上传文件到 Dify API 失败: fileName={}, error={}",
                    file.getOriginalFilename(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("{\"error\": \"上传文件失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 批量上传文档到数据集（直接使用 MultipartFile）
     * 支持一次上传多个文件（最多20个，系统限制为10个）
     * <p>
     * 性能优化说明：
     * 1. 提前验证所有文件，避免无效文件进入上传流程
     * 2. 一次性 HTTP 请求上传所有文件，网络开销最小
     * 3. 使用 Stream API 进行高效的文件验证和统计
     * 4. 详细的日志记录，便于性能监控和问题排查
     *
     * @param datasetId  数据集ID
     * @param files      文件列表
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> uploadDocumentsBatchWithFileStorage(String datasetId, MultipartFile[] files,
            Long userId,
            String resourceId, String keyType) {
        try {
            // 1. 验证文件数量（系统限制最多10个，Dify API限制最多20个）
            if (files == null || files.length == 0) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "文件列表不能为空");
            }
            if (files.length > 10) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "文件数量超过限制，最多支持10个文件");
            }

            // 2. 验证所有文件并统计信息（使用 Stream API，提前失败）
            var fileStats = Arrays.stream(files)
                    .peek(file -> {
                        if (file == null || file.isEmpty()) {
                            throw BusinessException.of(ResultCode.BAD_REQUEST, "文件不能为空");
                        }
                        validateFile(file);
                    })
                    .map(file -> new FileStat(file.getOriginalFilename(), file.getSize()))
                    .toList();

            // 3. 计算总文件大小（用于日志和监控）
            long totalSize = fileStats.stream()
                    .mapToLong(FileStat::size)
                    .sum();
            var fileNames = fileStats.stream()
                    .map(FileStat::fileName)
                    .toList();

            // 4. 构建表单数据（所有文件共享同一个 data 配置）
            Map<String, Object> data = new HashMap<>();
            data.put("data", buildDefaultConfigJson());

            // 5. 直接调用 Dify API 批量上传
            log.info("📤 开始批量上传文件到 Dify: datasetId={}, fileCount={}, totalSize={} bytes ({}), userId={}",
                    datasetId, files.length, totalSize, formatFileSize(totalSize), userId);
            log.debug("📤 上传文件列表: {}", fileNames);

            var startTime = System.currentTimeMillis();
            var response = difyApiClient.uploadFiles("POST", "/datasets/" + datasetId + "/document/create-by-file",
                    files, data, userId, resourceId, keyType);
            var duration = System.currentTimeMillis() - startTime;

            log.info("✅ 批量上传完成: datasetId={}, fileCount={}, totalSize={} bytes ({}), duration={}ms, status={}",
                    datasetId, files.length, totalSize, formatFileSize(totalSize), duration, response.getStatusCode());

            return response;
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: datasetId={}, fileCount={}, status={}, error={}",
                    datasetId, files != null ? files.length : 0, e.getStatusCode(), e.getMessage());
            // 直接返回Dify的错误响应给前端
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("批量文件上传失败: datasetId={}, fileCount={}, error={}",
                    datasetId, files != null ? files.length : 0, e.getMessage(), e);
            throw BusinessException.of(ResultCode.FILE_UPLOAD_FAILED, "批量文件上传失败: %s", e.getMessage());
        }
    }

    /**
     * 文件统计信息（内部类）
     */
    private record FileStat(String fileName, long size) {
    }

    /**
     * 格式化文件大小（用于日志）
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }

    /**
     * 验证并过滤文件列表
     */
    private List<MultipartFile> validateAndFilterFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<MultipartFile> validFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                validateFile(file);
                validFiles.add(file);
            } catch (Exception e) {
                log.warn("文件验证失败，跳过: fileName={}, error={}",
                        file != null ? file.getOriginalFilename() : "null", e.getMessage());
            }
        }

        return validFiles;
    }

    /**
     * 将文件列表分批
     */
    private List<List<MultipartFile>> partitionFiles(List<MultipartFile> files, int batchSize) {
        List<List<MultipartFile>> batches = new ArrayList<>();
        for (int i = 0; i < files.size(); i += batchSize) {
            int end = Math.min(i + batchSize, files.size());
            batches.add(new ArrayList<>(files.subList(i, end)));
        }
        return batches;
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
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

    /**
     * 使用动态密钥上传多个文件（支持指定 keyType）
     * 注意：由于 Dify API 只支持单个文件上传，此方法会循环调用单个文件上传接口
     *
     * @param user       用户标识
     * @param files      上传的文件列表（支持单个或多个文件）
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型（dataset/file）
     * @return 最后一个文件的上传结果（建议使用异步方法批量上传）
     */
    public ResponseEntity<String> uploadFilesWithDynamicKey(Long user, List<MultipartFile> files, Long userId,
            String resourceId, String keyType) {
        try {
            int fileCount = (files != null) ? files.size() : 0;
            log.info("开始上传多个文件到 Dify（动态密钥），用户: {}, 文件数量: {}, 资源ID: {}, keyType: {}",
                    user, fileCount, resourceId, keyType);

            if (files == null || files.isEmpty()) {
                log.warn("文件列表为空，无法上传");
                return ResponseEntity.status(400).body("{\"error\": \"文件列表不能为空\"}");
            }

            // 循环上传每个文件（Dify API 只支持单个文件上传）
            ResponseEntity<String> lastResponse = null;
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file == null || file.isEmpty()) {
                    log.warn("跳过空文件: index={}", i);
                    continue;
                }

                log.info("上传第 {}/{} 个文件: fileName={}", i + 1, fileCount, file.getOriginalFilename());
                lastResponse = uploadFileWithDynamicKey(user, file, userId, resourceId, keyType);

                if (!lastResponse.getStatusCode().is2xxSuccessful()) {
                    log.error("文件上传失败: fileName={}, status={}",
                            file.getOriginalFilename(), lastResponse.getStatusCode());
                }
            }

            if (lastResponse == null) {
                return ResponseEntity.status(400).body("{\"error\": \"没有成功上传的文件\"}");
            }

            log.info("多个文件上传完成（动态密钥），文件数量: {}", fileCount);
            return lastResponse;
        } catch (Exception e) {
            log.error("多个文件上传异常（动态密钥）: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"error\": \"多个文件上传异常: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 使用动态密钥上传多个文件（默认使用 dataset keyType）
     *
     * @param user       用户标识
     * @param files      上传的文件列表（支持单个或多个文件）
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @return 文件上传结果
     */
    public ResponseEntity<String> uploadFilesWithDynamicKey(Long user, List<MultipartFile> files, Long userId,
            String resourceId) {
        return uploadFilesWithDynamicKey(user, files, userId, resourceId, DifyApiKey.KeyType.DATASET.getCode());
    }
}