package com.server.agentbackendservices.modules.dify.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.agentbackendservices.modules.dify.config.DifyConfig;
import com.server.agentbackendservices.modules.dify.service.DifyApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
     * 流式请求方法（用于 SSE 流式响应）
     * 使用 Java 11+ 的 HttpClient 实现流式请求
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
        String url = buildUrl(path, null, 0);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body, userId, resourceId, keyType, 0);
        
        log.debug("Dify {} 流式请求: {}, userId={}, resourceId={}, keyType={}", 
                method, url, userId, resourceId, keyType);
        
        try {
            // 构建请求体 JSON 字符串
            String jsonBody;
            if (entity.getBody() instanceof String) {
                jsonBody = (String) entity.getBody();
            } else if (entity.getBody() != null) {
                jsonBody = objectMapper.writeValueAsString(entity.getBody());
            } else {
                jsonBody = "{}";
            }
            
            // 创建 HttpClient
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            
            // 构建 HttpRequest
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(5))
                    .header("Content-Type", "application/json");
            
            // 添加请求头
            HttpHeaders headers = entity.getHeaders();
            if (headers != null) {
                headers.forEach((name, values) -> {
                    if (values != null && !values.isEmpty()) {
                        requestBuilder.header(name, values.get(0));
                    }
                });
            }
            
            // 设置请求方法和请求体
            HttpRequest httpRequest = requestBuilder
                    .method(method.toUpperCase(), HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            
            // 发送请求并处理流式响应
            HttpResponse<InputStream> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofInputStream());
            
            // 检查响应状态码
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                // 读取错误响应
                try (InputStream errorStream = response.body();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                    StringBuilder errorBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBody.append(line).append("\n");
                    }
                    throw new RuntimeException("Dify 流式请求失败，url: " + url
                            + ", 状态码: " + response.statusCode()
                            + ", 响应体: " + errorBody.toString());
                }
            }
            
            // 流式读取响应
            try (InputStream inputStream = response.body();
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
            
        } catch (java.net.http.HttpTimeoutException e) {
            log.error("Dify 流式请求超时: url={}, err={}", url, e.getMessage());
            throw new RuntimeException("Dify 流式请求超时: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            log.error("Dify 流式请求IO异常: url={}, err={}", url, e.getMessage(), e);
            throw new RuntimeException("Dify 流式请求IO异常: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Dify 流式请求被中断: url={}, err={}", url, e.getMessage());
            throw new RuntimeException("Dify 流式请求被中断: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Dify 流式请求异常: url={}, err={}", url, e.getMessage(), e);
            throw new RuntimeException("Dify 流式请求异常: " + e.getMessage(), e);
        }
    }

    private void validateResponse(String url, ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Dify 请求失败，url: " + url
                    + ", 状态码: " + response.getStatusCode()
                    + ", 响应体: " + response.getBody());
        }
    }


}
