package com.sciz.server.infrastructure.external.dify.util;

import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.service.DifyApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Dify API 客户端工具类
 * 封装 HTTP 请求和 Authorization 认证
 * 
 * @author shihang.shang
 * @since 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DifyApiClient {
    
    private final DifyConfig difyConfig;
    private final RestTemplate restTemplate;
    private final DifyApiKeyService difyApiKeyService;
    
    /**
     * 统一请求方法（使用动态密钥）
     * 
     * @param method 请求类型 (GET, POST, PUT, DELETE)
     * @param path 请求路径
     * @param body 请求体 (POST/PUT 时使用)
     * @param params 查询参数 (GET 时使用)
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, Map<String, Object> params,
                                         String userId, String resourceId, String keyType) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = buildUrl(path, params);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body, userId, resourceId, keyType);
        
        log.debug("Dify {} 请求: {}, userId={}, resourceId={}, keyType={}", 
                method, url, userId, resourceId, keyType);
        return restTemplate.exchange(url, httpMethod, entity, String.class);
    }

    /**
     * 简化请求方法 - 无请求体
     */
    public ResponseEntity<String> request(String method, String path, String userId, String resourceId, String keyType) {
        return request(method, path, null, null, userId, resourceId, keyType);
    }
    
    /**
     * 简化请求方法 - 带请求体
     */
    public ResponseEntity<String> request(String method, String path, Object body, String userId, String resourceId, String keyType) {
        return request(method, path, body, null, userId, resourceId, keyType);
    }
    
    /**
     * 简化请求方法 - 带查询参数
     */
    public ResponseEntity<String> request(String method, String path, Map<String, Object> params, String userId, String resourceId, String keyType) {
        return request(method, path, null, params, userId, resourceId, keyType);
    }
    
    /**
     * 文件上传请求方法（使用动态密钥）
     * 
     * @param method 请求类型 (POST)
     * @param path 请求路径
     * @param file 上传的文件
     * @param data 其他表单数据
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> uploadFile(String method, String path, MultipartFile file, Map<String, Object> data,
                                           String userId, String resourceId, String keyType) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = difyConfig.getBaseUrl() + path;
        HttpEntity<?> entity = createFileUploadEntityWithDynamicKey(file, data, userId, resourceId, keyType);
        
        log.debug("Dify {} 文件上传请求: {}, userId={}, resourceId={}, keyType={}", 
                method, url, userId, resourceId, keyType);
        return restTemplate.exchange(url, httpMethod, entity, String.class);
    }
    
    
    /**
     * 构建完整URL
     */
    private String buildUrl(String path, Map<String, Object> params) {
        String url = difyConfig.getBaseUrl() + path;
        
        if (params != null && !params.isEmpty()) {
            StringBuilder queryString = new StringBuilder("?");
            params.forEach((key, value) -> 
                queryString.append(key).append("=").append(value).append("&"));
            url += queryString.toString().replaceAll("&$", "");
        }
        
        return url;
    }
    
    
    

    /**
     * 创建带动态密钥的 HTTP 实体
     */
    private HttpEntity<?> createHttpEntityWithDynamicKey(Object body, String userId, String resourceId, String keyType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 动态获取API密钥
        String apiKey = difyApiKeyService.getApiKey(userId, resourceId, keyType);
        headers.set("Authorization", "Bearer " + apiKey);
        
        return new HttpEntity<>(body, headers);
    }

    /**
     * 创建带动态密钥的文件上传 HTTP 实体
     */
    private HttpEntity<?> createFileUploadEntityWithDynamicKey(MultipartFile file, Map<String, Object> data, 
                                                              String userId, String resourceId, String keyType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        // 动态获取API密钥
        String apiKey = difyApiKeyService.getApiKey(userId, resourceId, keyType);
        headers.set("Authorization", "Bearer " + apiKey);
        
        // 构建multipart数据
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // 添加文件
        if (file != null && !file.isEmpty()) {
            body.add("file", file.getResource());
        }
        
        // 添加其他数据
        if (data != null) {
            data.forEach((key, value) -> {
                if (value != null) {
                    body.add(key, value.toString());
                }
            });
        }
        
        return new HttpEntity<>(body, headers);
    }

}
