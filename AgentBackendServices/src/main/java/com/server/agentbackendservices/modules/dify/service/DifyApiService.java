package com.server.agentbackendservices.modules.dify.service;

import com.server.agentbackendservices.modules.dify.config.DifyConfig;
import com.server.agentbackendservices.modules.dify.config.DifyDocumentConfig;
import com.server.agentbackendservices.modules.dify.dto.DifyDatasetRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyRetrieveRequest;
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
 * @author jarvanlove
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
    public ResponseEntity<String> getDatasets(int page, int limit) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("limit", limit);
            
            return difyApiClient.request("GET", "/datasets", params);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            // 直接返回Dify的错误响应给前端
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
    
    /**
     * 创建数据集
     */
    public ResponseEntity<String> createDataset(DifyDatasetRequest request) {
        try {
            return difyApiClient.request("POST", "/datasets", request);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            // 直接返回Dify的错误响应给前端
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
    
    /**
     * 获取数据集详情
     */
    public ResponseEntity<String> getDataset(String datasetId) {
        try {
            return difyApiClient.request("GET", "/datasets/" + datasetId);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
    
    /**
     * 更新数据集
     */
    public ResponseEntity<String> updateDataset(String datasetId, DifyDatasetRequest request) {
        try {
            return difyApiClient.request("PUT", "/datasets/" + datasetId, request);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
    
    /**
     * 删除数据集
     */
    public ResponseEntity<String> deleteDataset(String datasetId) {
        try {
            return difyApiClient.request("DELETE", "/datasets/" + datasetId);
        } catch (HttpClientErrorException e) {
            log.error("Dify API调用失败: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
    
    

    
    /**
     * 检索知识库
     */
    public ResponseEntity<String> retrieveDataset(String datasetId, DifyRetrieveRequest request) {
        try {
            return difyApiClient.request("POST", "/datasets/" + datasetId + "/retrieve", request);
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
    public ResponseEntity<String> uploadDocumentWithFileStorage(String datasetId, MultipartFile file) {
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
                    newMultipartFile, data);
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
}