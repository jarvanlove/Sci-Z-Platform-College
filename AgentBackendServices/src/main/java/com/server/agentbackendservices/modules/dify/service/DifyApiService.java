package com.server.agentbackendservices.modules.dify.service;

import com.server.agentbackendservices.modules.dify.config.DifyConfig;
import com.server.agentbackendservices.modules.dify.config.DifyDocumentConfig;
import com.server.agentbackendservices.modules.dify.dto.DifyDatasetRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyRetrieveRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyWorkflowRequest;
import com.server.agentbackendservices.modules.dify.entity.DifyApiKey;
import com.server.agentbackendservices.modules.dify.util.DifyApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    /**
     * 获取所有数据集
     */
    public ResponseEntity<String> getDatasets(int page, int limit, String userId, String resourceId, String keyType) {
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
    public ResponseEntity<String> createDataset(DifyDatasetRequest request, String userId, String resourceId, String keyType) {
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
    public ResponseEntity<String> getDataset(String datasetId, String userId, String resourceId, String keyType) {
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
    public ResponseEntity<String> updateDataset(String datasetId, DifyDatasetRequest request, String userId, String resourceId, String keyType) {
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
    public ResponseEntity<String> deleteDataset(String datasetId, String userId, String resourceId, String keyType) {
        try {
            return difyApiClient.request("DELETE", "/datasets/" + datasetId, userId, resourceId, keyType);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
    
    

    
    /**
     * 检索知识库
     */
    public ResponseEntity<String> retrieveDataset(String datasetId, DifyRetrieveRequest request, String userId, String resourceId, String keyType) {
        try {
            return difyApiClient.request("POST", "/datasets/" + datasetId + "/retrieve", request, userId, resourceId, keyType);
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
            case ".txt": return "text/plain";
            case ".pdf": return "application/pdf";
            case ".doc": return "application/msword";
            case ".docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".md": return "text/markdown";
            case ".csv": return "text/csv";
            case ".json": return "application/json";
            default: return "application/octet-stream";
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
        retrievalModel.put("score_threshold_enabled", difyDocumentConfig.getRetrievalModel().getScoreThresholdEnabled());
        retrievalModel.put("score_threshold", difyDocumentConfig.getRetrievalModel().getScoreThreshold());
        config.put("retrieval_model", retrievalModel);
        
        // 处理规则配置
        Map<String, Object> processRule = new HashMap<>();
        processRule.put("mode", difyDocumentConfig.getProcessRule().getMode());
        config.put("process_rule", processRule);
        
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException("构建默认配置JSON失败", e);
        }
    }
    /**
     * 上传文档到数据集（先存储文件，再调用Dify API）
     */
    public ResponseEntity<String> uploadDocumentWithFileStorage(String datasetId, MultipartFile file, String userId, String resourceId, String keyType) {
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
        // 从配置文件获取上传目录
        String uploadDir = difyConfig.getUpload().getDir();
        
        // 确保目录以/结尾
        if (!uploadDir.endsWith("/") && !uploadDir.endsWith("\\")) {
            uploadDir += "/";
        }
        
        // 创建上传目录
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("📁 创建上传目录: {}", uploadDir);
            } else {
                log.warn("⚠️ 无法创建上传目录: {}", uploadDir);
            }
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
                throw new RuntimeException("不支持的文件类型: " + extension + "，支持的类型: " + difyConfig.getUpload().getAllowedExtensions());
            }
        }
        // 生成唯一文件名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename : "file") + extension;
        
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
            public String getName() { return "file"; }
            
            @Override
            public String getOriginalFilename() { return filename; }
            
            @Override
            public String getContentType() { return mimeType; }
            
            @Override
            public boolean isEmpty() { return content.length == 0; }
            
            @Override
            public long getSize() { return content.length; }
            
            @Override
            public byte[] getBytes() { return content; }
            
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
     * @param request 工作流请求
     * @param userId 用户ID
     * @param workflowId 工作流ID
     * @return 工作流执行结果
     */
    public ResponseEntity<String> runWorkflowWithDynamicKey(DifyWorkflowRequest request, String userId, String workflowId) {
        try {
            log.info("开始执行 Dify 工作流（动态密钥），用户: {}, 工作流ID: {}", userId, workflowId);
            Map<String, Object> params = new HashMap<>();
            params.put("inputs", request.getInputs());
            params.put("response_mode", request.getResponseMode());
            params.put("user", request.getUser());
            if (workflowId != null && !workflowId.trim().isEmpty()) {
                params.put("workflow_id", workflowId);
            }
            String endpoint = "/workflows/run";
            ResponseEntity<String> response = difyApiClient.request("POST", endpoint, params, 
                    userId, workflowId, DifyApiKey.KeyType.WORKFLOW.getCode());
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
     * @param userId 用户ID
     * @param workflowId 工作流ID
     * @return 工作流运行状态
     */
    public ResponseEntity<String> getWorkflowRunStatusWithDynamicKey(String workflowRunId, String userId, String workflowId) {
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
     * @param page 页码
     * @param limit 每页数量
     * @param userId 用户ID
     * @param workflowId 工作流ID
     * @return 工作流日志
     */
    public ResponseEntity<String> getWorkflowLogsWithDynamicKey(Integer page, Integer limit, String userId, String workflowId) {
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
     * @param user 用户标识
     * @param file 上传的文件
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 文件上传结果
     */
    public ResponseEntity<String> uploadFileWithDynamicKey(String user, MultipartFile file, String userId, String resourceId) {
        try {
            log.info("开始上传文件到 Dify（动态密钥），用户: {}, 文件名: {}, 大小: {} bytes, 资源ID: {}", 
                    user, file.getOriginalFilename(), file.getSize(), resourceId);
            // 构建表单数据
            Map<String, Object> formData = new HashMap<>();
            formData.put("user", user);
            // 调用 Dify 文件上传 API
            String endpoint = "/files/upload";
            ResponseEntity<String> response = difyApiClient.uploadFile("POST", endpoint, file, formData, 
                    userId, resourceId, DifyApiKey.KeyType.DATASET.getCode());
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