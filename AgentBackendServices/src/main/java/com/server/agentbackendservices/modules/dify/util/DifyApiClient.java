package com.server.agentbackendservices.modules.dify.util;

import com.server.agentbackendservices.modules.dify.config.DifyConfig;
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
 * @author jarvanlove
 * @since 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DifyApiClient {
    
    private final DifyConfig difyConfig;
    private final RestTemplate restTemplate;
    
    /**
     * 统一请求方法
     * 
     * @param method 请求类型 (GET, POST, PUT, DELETE)
     * @param path 请求路径
     * @param body 请求体 (POST/PUT 时使用)
     * @param params 查询参数 (GET 时使用)
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, Map<String, Object> params) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = buildUrl(path, params);
        HttpEntity<?> entity = createHttpEntity(body);
        
        log.debug("Dify {} 请求: {}", method, url);
        return restTemplate.exchange(url, httpMethod, entity, String.class);
    }
    
    /**
     * 文件上传请求方法
     * 
     * @param method 请求类型 (POST)
     * @param path 请求路径
     * @param file 上传的文件
     * @param data 其他表单数据
     * @return 响应结果
     */
    public ResponseEntity<String> uploadFile(String method, String path, MultipartFile file, Map<String, Object> data) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = difyConfig.getBaseUrl() + path;
        HttpEntity<?> entity = createFileUploadEntity(file, data);
        
        log.debug("Dify {} 文件上传请求: {}", method, url);
        return restTemplate.exchange(url, httpMethod, entity, String.class);
    }
    
    /**
     * 简化请求方法 - 无请求体
     */
    public ResponseEntity<String> request(String method, String path) {
        return request(method, path, null, null);
    }
    
    /**
     * 简化请求方法 - 带请求体
     */
    public ResponseEntity<String> request(String method, String path, Object body) {
        return request(method, path, body, null);
    }
    
    /**
     * 简化请求方法 - 带查询参数
     */
    public ResponseEntity<String> request(String method, String path, Map<String, Object> params) {
        return request(method, path, null, params);
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
     * 创建 HTTP 实体（带认证头）
     */
    private HttpEntity<?> createHttpEntity() {
        return createHttpEntity(null);
    }
    
    /**
     * 创建 HTTP 实体（带认证头和请求体）
     */
    private HttpEntity<?> createHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + difyConfig.getApiKey());
        
        return new HttpEntity<>(body, headers);
    }
    
    /**
     * 创建文件上传 HTTP 实体
     */
    private HttpEntity<?> createFileUploadEntity(MultipartFile file, Map<String, Object> data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + difyConfig.getApiKey());
        
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
