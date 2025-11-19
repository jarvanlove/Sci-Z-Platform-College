package com.sciz.server.infrastructure.external.dify.util;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
    
    /**
     * 统一请求方法（使用动态密钥）
     * 
     * @param method 请求类型 (GET, POST, PUT, DELETE)
     * @param path 请求路径
     * @param body 请求体 (POST/PUT 时使用，会被序列化为 JSON)
     * @param params 查询参数 (GET 时使用，会拼接到 URL)
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @param key URL 类型（0=baseUrl, 1=privateUrl）
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, Map<String, Object> params,
                                         Long userId, String resourceId, String keyType, int key) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = buildUrl(path, params, key);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body, userId, resourceId, keyType, key);
        log.debug("Dify {} 请求: {}, userId={}, resourceId={}, keyType={}, hasBody={}", 
                method, url, userId, resourceId, keyType, body != null);
        
        // 使用 URI.create() 避免 RestTemplate 将 URL 中的 {} 当作 URI 模板变量处理
        try {
            java.net.URI uri = java.net.URI.create(url);
            ResponseEntity<String> response = restTemplate.exchange(uri, httpMethod, entity, String.class);
            validateResponse(url, response);
            return response;
        } catch (IllegalArgumentException e) {
            log.error("URL 格式错误: {}, 错误: {}", url, e.getMessage());
            throw new RuntimeException("URL 格式错误: " + url, e);
        }
    }
    
    /**
     * GET 请求方法（无请求体，无查询参数）
     * 
     * @param method 请求类型（通常是 GET）
     * @param path 请求路径
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Long userId, String resourceId, String keyType) {
        return request(method, path, null, null, userId, resourceId, keyType, 0);
    }
    
    /**
     * POST 请求方法（带请求体，body 会被序列化为 JSON）
     * 
     * @param method 请求类型（通常是 POST）
     * @param path 请求路径
     * @param body 请求体（会被序列化为 JSON 字符串）
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, Long userId, String resourceId, String keyType) {
        return request(method, path, body, null, userId, resourceId, keyType, 0);
    }

    /**
     * POST 请求方法（带请求体，body 会被序列化为 JSON，支持指定 URL 类型）
     * 
     * @param method 请求类型（通常是 POST）
     * @param path 请求路径
     * @param body 请求体（会被序列化为 JSON 字符串）
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @param key URL 类型（0=baseUrl, 1=privateUrl）
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, Long userId, String resourceId, String keyType, int key) {
        return request(method, path, body, null, userId, resourceId, keyType, key);
    }

    /**
     * GET 请求方法（带查询参数，params 会拼接到 URL）
     * 
     * @param method 请求类型（通常是 GET）
     * @param path 请求路径
     * @param params 查询参数（会拼接到 URL 的查询字符串）
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Map<String, Object> params, Long userId, String resourceId, String keyType) {
        return request(method, path, null, params, userId, resourceId, keyType, 0);
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
                                           Long userId, String resourceId, String keyType) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = difyConfig.getBaseUrl() + path;
        HttpEntity<?> entity = createFileUploadEntityWithDynamicKey(file, data, userId, resourceId, keyType);
        
        log.debug("Dify {} 文件上传请求: {}, userId={}, resourceId={}, keyType={}", 
                method, url, userId, resourceId, keyType);
        ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
        validateResponse(url, response);
        return response;
    }
    
    
    /**
     * 构建完整URL
     */
    private String buildUrl(String path, Map<String, Object> params,int Urlkey) {
        String url = "";
        if (Urlkey==0) url = difyConfig.getBaseUrl() + path;
        if (Urlkey==1) url = difyConfig.getPrivateUrl() + path;
        
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
     * 
     * @param body 请求体（如果不为 null，会被序列化为 JSON 字符串）
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @param IsKey URL 类型（0=baseUrl, 1=privateUrl）
     * @return HTTP 实体
     */
    private HttpEntity<?> createHttpEntityWithDynamicKey(Object body, Long userId, String resourceId, String keyType, int IsKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (IsKey == 0) {
            // 动态获取API密钥
            String apiKey = difyApiKeyService.getApiKey(userId, resourceId, keyType);
            headers.set("Authorization", "Bearer " + apiKey);
        }

        // 如果 body 不为 null，将其序列化为 JSON 字符串，确保作为请求体发送（而不是查询参数）
        if (body != null) {
            try {
                String jsonBody = objectMapper.writeValueAsString(body);
                return new HttpEntity<>(jsonBody, headers);
            } catch (Exception e) {
                log.error("序列化请求体失败: {}", e.getMessage(), e);
                throw new RuntimeException("序列化请求体失败: " + e.getMessage(), e);
            }
        }

        return new HttpEntity<>(headers);
    }

    /**
     * 创建带动态密钥的文件上传 HTTP 实体
     */
    private HttpEntity<?> createFileUploadEntityWithDynamicKey(MultipartFile file, Map<String, Object> data, 
                                                              long userId, String resourceId, String keyType) {
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


    /**
     * 流式请求方法（改为普通 HTTP 请求）
     * 
     * @param method 请求类型 (POST)
     * @param path 请求路径
     * @param body 请求体
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> requestStream(String method, String path, Object body,
                                                Long userId, String resourceId, String keyType) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = buildUrl(path, null, 0);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body, userId, resourceId, keyType, 0);
        
        // 为流式请求添加 Accept 头，接收 text/event-stream 格式
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(entity.getHeaders());
        headers.setAccept(java.util.Collections.singletonList(MediaType.TEXT_EVENT_STREAM));
        HttpEntity<?> streamEntity = new HttpEntity<>(entity.getBody(), headers);
        
        log.debug(String.format("Dify %s 流式请求: %s, userId=%s, resourceId=%s, keyType=%s, hasBody=%s", 
                method, url, userId, resourceId, keyType, body != null));
        
        // 使用 URI.create() 避免 RestTemplate 将 URL 中的 {} 当作 URI 模板变量处理
        try {
            java.net.URI uri = java.net.URI.create(url);
            ResponseEntity<String> response = restTemplate.exchange(uri, httpMethod, streamEntity, String.class);
            validateResponse(url, response);
            return response;
        } catch (IllegalArgumentException e) {
            log.error(String.format("URL 格式错误: url=%s, err=%s", url, e.getMessage()));
            throw new RuntimeException("URL 格式错误: " + url, e);
        }
    }

    /**
     * 验证响应状态码
     *
     * @param url 请求URL
     * @param response HTTP响应
     */
    private void validateResponse(String url, ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Dify 请求失败，url: " + url
                    + ", 状态码: " + response.getStatusCode()
                    + ", 响应体: " + response.getBody());
        }
    }


}
