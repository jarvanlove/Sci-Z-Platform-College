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
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

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
     * @param body 请求体 (POST/PUT 时使用)
     * @param params 查询参数 (GET 时使用)
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, Map<String, Object> params,
                                         Long userId, String resourceId, String keyType, int key) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = buildUrl(path, params,key);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body, userId, resourceId, keyType,key);
        log.debug("Dify {} 请求: {}, userId={}, resourceId={}, keyType={}", 
                method, url, userId, resourceId, keyType);
        ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
        validateResponse(url, response);
        return response;
    }
    /**
     * 简化请求方法 - 无请求体
     */
    public ResponseEntity<String> request(String method, String path, Long userId, String resourceId, String keyType) {
        return request(method, path, null, null, userId, resourceId, keyType,0);
    }
    
    /**
     * 简化请求方法 - 带请求体
     */
    public ResponseEntity<String> request(String method, String path, Object body, Long userId, String resourceId, String keyType) {
        return request(method, path, body, null, userId, resourceId, keyType,0);
    }

    public ResponseEntity<String> request(String method, String path, Object body, Long userId, String resourceId, String keyType,int  key) {
        return request(method, path, body, null, userId, resourceId, keyType,key);
    }

    /**
     * 简化请求方法 - 带查询参数
     */
    public ResponseEntity<String> request(String method, String path, Map<String, Object> params, Long userId, String resourceId, String keyType) {
        return request(method, path, null, params, userId, resourceId, keyType,0);
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
     */
    private HttpEntity<?> createHttpEntityWithDynamicKey(Object body, Long userId, String resourceId, String keyType,int IsKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (IsKey==0){
            // 动态获取API密钥
            String apiKey = difyApiKeyService.getApiKey(userId, resourceId, keyType);
            headers.set("Authorization", "Bearer " + apiKey);
        }

        return new HttpEntity<>(body, headers);
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
     * 流式请求方法（用于 SSE 流式响应）
     * 
     * @param method 请求类型 (POST)
     * @param path 请求路径
     * @param body 请求体
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @param onData 数据回调函数，每收到一行数据时调用
     */
    public void requestStream(String method, String path, Object body,
                             Long userId, String resourceId, String keyType,
                             Consumer<String> onData) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = buildUrl(path, null, 0);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body, userId, resourceId, keyType, 0);
        
        log.debug("Dify {} 流式请求: {}, userId={}, resourceId={}, keyType={}", 
                method, url, userId, resourceId, keyType);
        
        ResponseExtractor<Void> responseExtractor = response -> {
            HttpStatusCode statusCode = response.getStatusCode();
            
            if (!statusCode.is2xxSuccessful()) {
                // 读取错误响应
                try (InputStream inputStream = response.getBody();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder errorBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBody.append(line).append("\n");
                    }
                    throw new RuntimeException("Dify 流式请求失败，url: " + url
                            + ", 状态码: " + statusCode
                            + ", 响应体: " + errorBody.toString());
                }
            }
            
            // 流式读取响应
            try (InputStream inputStream = response.getBody();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (onData != null) {
                        onData.accept(line);
                    }
                }
            } catch (Exception e) {
                log.error("流式读取响应失败: {}", e.getMessage(), e);
                throw new RuntimeException("流式读取响应失败: " + e.getMessage(), e);
            }
            
            return null;
        };
        restTemplate.execute(url, httpMethod, 
                request -> {
                    // 设置请求头
                    HttpHeaders headers = entity.getHeaders();
                    if (headers != null) {
                        headers.forEach((name, values) -> {
                            request.getHeaders().put(name, values);
                        });
                    }
                    // 设置请求体
                    if (entity.getBody() != null) {
                        try {
                            String jsonBody = objectMapper.writeValueAsString(entity.getBody());
                            request.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            log.error("序列化请求体失败: {}", e.getMessage(), e);
                            throw new RuntimeException("序列化请求体失败: " + e.getMessage(), e);
                        }
                    }
                }, 
                responseExtractor);
    }

    private void validateResponse(String url, ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Dify 请求失败，url: " + url
                    + ", 状态码: " + response.getStatusCode()
                    + ", 响应体: " + response.getBody());
        }
    }


}
