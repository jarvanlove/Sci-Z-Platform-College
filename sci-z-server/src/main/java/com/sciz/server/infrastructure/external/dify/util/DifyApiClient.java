package com.sciz.server.infrastructure.external.dify.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.infrastructure.external.dify.config.DifyConfig;
import com.sciz.server.infrastructure.external.dify.service.DifyApiKeyService;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.time.Duration;
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
    private final WebClient webClient;

    /**
     * 统一请求方法（使用动态密钥）
     * 
     * @param method     请求类型 (GET, POST, PUT, DELETE)
     * @param path       请求路径
     * @param body       请求体 (POST/PUT 时使用，会被序列化为 JSON)
     * @param params     查询参数 (GET 时使用，会拼接到 URL)
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @param key        URL 类型（0=baseUrl, 1=privateUrl）
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, Map<String, Object> params,
             String resourceId, String keyType, int key) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = buildUrl(path, params, key);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body,  resourceId, keyType, key);
        log.debug("Dify {} 请求: {}, userId={}, resourceId={}, keyType={}, hasBody={}",
                method, url,  resourceId, keyType, body != null);

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
     * @param method     请求类型（通常是 GET）
     * @param path       请求路径

     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path,  String resourceId, String keyType) {
        return request(method, path, null, null,  resourceId, keyType, 0);
    }

    /**
     * POST 请求方法（带请求体，body 会被序列化为 JSON）
     * 
     * @param method     请求类型（通常是 POST）
     * @param path       请求路径
     * @param body       请求体（会被序列化为 JSON 字符串）

     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body,  String resourceId,
            String keyType) {
        return request(method, path, body, null,  resourceId, keyType, 0);
    }

    /**
     * POST 请求方法（带请求体，body 会被序列化为 JSON，支持指定 URL 类型）
     * 
     * @param method     请求类型（通常是 POST）
     * @param path       请求路径
     * @param body       请求体（会被序列化为 JSON 字符串）

     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @param key        URL 类型（0=baseUrl, 1=privateUrl）
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Object body, String resourceId,
            String keyType, int key) {
        return request(method, path, body, null,  resourceId, keyType, key);
    }

    /**
     * GET 请求方法（带查询参数，params 会拼接到 URL）
     * 
     * @param method     请求类型（通常是 GET）
     * @param path       请求路径
     * @param params     查询参数（会拼接到 URL 的查询字符串）

     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> request(String method, String path, Map<String, Object> params,
            String resourceId, String keyType) {
        return request(method, path, null, params,  resourceId, keyType, 0);
    }

    /**
     * 文件上传请求方法（使用动态密钥）
     * 
     * @param method     请求类型 (POST)
     * @param path       请求路径
     * @param file       上传的文件
     * @param data       其他表单数据
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> uploadFile(String method, String path, MultipartFile file, Map<String, Object> data,
            Long userId, String resourceId, String keyType) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = difyConfig.getBaseUrl() + path;
        HttpEntity<?> entity = createFileUploadEntityWithDynamicKey(file, data,  resourceId, keyType);

        log.debug("Dify {} 文件上传请求: {}, userId={}, resourceId={}, keyType={}",
                method, url, userId, resourceId, keyType);
        ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
        validateResponse(url, response);
        return response;
    }

    /**
     * 批量文件上传请求方法（使用动态密钥）
     * 支持一次上传多个文件（最多20个，系统限制为10个）
     * 
     * @param method     请求类型 (POST)
     * @param path       请求路径
     * @param files      上传的文件列表
     * @param data       其他表单数据
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> uploadFiles(String method, String path, MultipartFile[] files,
            Map<String, Object> data,
            Long userId, String resourceId, String keyType) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        String url = difyConfig.getBaseUrl() + path;
        HttpEntity<?> entity = createBatchFileUploadEntityWithDynamicKey(files, data, userId, resourceId, keyType);

        log.debug("Dify {} 批量文件上传请求: {}, fileCount={}, userId={}, resourceId={}, keyType={}",
                method, url, files != null ? files.length : 0, userId, resourceId, keyType);
        ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
        validateResponse(url, response);
        return response;
    }

    /**
     * 构建完整URL
     */
    private String buildUrl(String path, Map<String, Object> params, int Urlkey) {
        String url = "";
        if (Urlkey == 0)
            url = difyConfig.getBaseUrl() + path;
        if (Urlkey == 1)
            url = difyConfig.getPrivateUrl() + path;

        if (params != null && !params.isEmpty()) {
            StringBuilder queryString = new StringBuilder("?");
            params.forEach((key, value) -> queryString.append(key).append("=").append(value).append("&"));
            url += queryString.toString().replaceAll("&$", "");
        }

        return url;
    }

    /**
     * 创建带动态密钥的 HTTP 实体
     * 
     * @param body       请求体（如果不为 null，会被序列化为 JSON 字符串）

     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @param IsKey      URL 类型（0=baseUrl, 1=privateUrl）
     * @return HTTP 实体
     */
    private HttpEntity<?> createHttpEntityWithDynamicKey(Object body,  String resourceId, String keyType,
            int IsKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 如果提供了 userId、resourceId、keyType，则使用动态密钥（无论是 baseUrl 还是 privateUrl）
        if ( resourceId != null && keyType != null) {
            // 动态获取API密钥
            String apiKey = difyApiKeyService.getApiKey( resourceId, keyType);
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
          String resourceId, String keyType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 动态获取API密钥
        String apiKey = difyApiKeyService.getApiKey( resourceId, keyType);
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
     * 创建带动态密钥的批量文件上传 HTTP 实体
     * 支持多个文件上传（每个文件使用相同的 field name "file"）
     * 根据 Dify 官方文档，批量上传时每个文件都使用 "file" 作为字段名
     */
    private HttpEntity<?> createBatchFileUploadEntityWithDynamicKey(MultipartFile[] files, Map<String, Object> data,
            long userId, String resourceId, String keyType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 动态获取API密钥
        String apiKey = difyApiKeyService.getApiKey( resourceId, keyType);
        headers.set("Authorization", "Bearer " + apiKey);

        // 构建multipart数据
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 添加多个文件（每个文件都使用 "file" 作为 field name，符合 Dify API 规范）
        // 这是批量上传的关键：多个文件使用相同的字段名 "file"
        if (files != null && files.length > 0) {
            int fileCount = 0;
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    body.add("file", file.getResource());
                    fileCount++;
                    log.debug("添加文件到批量上传请求: fileName={}, fileCount={}/{}",
                            file.getOriginalFilename(), fileCount, files.length);
                }
            }
            log.info("批量上传请求构建完成: totalFileCount={}, actualFileCount={}", files.length, fileCount);
        }

        // 添加其他数据（所有文件共享同一个 data 配置）
        if (data != null) {
            data.forEach((key, value) -> {
                if (value != null) {
                    body.add(key, value.toString());
                }
            });
            log.debug("添加表单数据到批量上传请求: dataKeys={}", data.keySet());
        }

        return new HttpEntity<>(body, headers);
    }

    /**
     * 真正的流式请求方法（使用 WebClient 处理 text/event-stream 响应，实时回调）
     * 在接收到每一行数据时立即调用回调函数，不等待完整响应
     * 
     * @param method     请求类型 (POST)
     * @param path       请求路径
     * @param body       请求体

     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @param onData     数据回调函数，每收到一行数据时立即调用
     */
    public void requestStreamWithCallback(String method, String path, Object body,
           String resourceId, String keyType,
            java.util.function.Consumer<String> onData) {
        String url = buildUrl(path, null, 0);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body,  resourceId, keyType, 0);

        // 从 SaToken 获取 userId（优先从 AsyncUserContext 获取，如果没有则从 Sa-Token Session 获取）
        Long userId = LoginUserUtil.getCurrentUserId().orElse(null);

        log.debug(String.format("Dify %s 流式请求（实时回调）: %s, userId=%s, resourceId=%s, keyType=%s, hasBody=%s",
                method, url, userId != null ? userId : "N/A", resourceId, keyType, body != null));

        try {
            // 构建请求体
            String jsonBody;
            if (entity.getBody() instanceof String) {
                jsonBody = (String) entity.getBody();
            } else if (entity.getBody() != null) {
                jsonBody = objectMapper.writeValueAsString(entity.getBody());
            } else {
                jsonBody = "{}";
            }
            
            // 构建 WebClient 请求
            WebClient.RequestBodySpec requestSpec = webClient.method(HttpMethod.valueOf(method.toUpperCase()))
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM);
            
            // 添加请求头（从 entity 中获取）
            HttpHeaders headers = entity.getHeaders();
            if (headers != null) {
                headers.forEach((name, values) -> {
                    if (values != null && !values.isEmpty() && !name.equalsIgnoreCase("Content-Type")) {
                        requestSpec.header(name, values.get(0));
                    }
                });
            }
            
            // 使用 bodyToFlux 接收流式响应，实时处理每一行数据
            Flux<String> eventStream = requestSpec
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToFlux(String.class);
            
            // 订阅流式数据，每收到一行立即调用回调
            eventStream
                    .doOnNext(event -> {
                        // 立即调用回调，不等待完整响应
                        if (onData != null && event != null) {
                            String trimmedEvent = event.trim();
                            if (!trimmedEvent.isEmpty()) {
                                onData.accept(trimmedEvent);
                            }
                        }
                    })
                    .doOnError(error -> {
                        log.error(String.format("流式请求处理错误: url=%s, err=%s", url, error.getMessage()), error);
                    })
                    .doOnComplete(() -> {
                        log.debug(String.format("流式响应读取完成: url=%s", url));
                    })
                    .blockLast(Duration.ofMinutes(10)); // 阻塞等待流结束，最多等待10分钟
            
        } catch (Exception e) {
            log.error(String.format("Dify 流式请求异常: url=%s, err=%s", url, e.getMessage()), e);
            throw new RuntimeException("Dify 流式请求异常: " + url, e);
        }
    }

    /**
     * 流式请求方法（使用 WebClient 处理 text/event-stream 响应）
     * 注意：此方法会等待完整响应后才返回，不是真正的实时流式
     * 
     * @param method     请求类型 (POST)
     * @param path       请求路径
     * @param body       请求体
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果
     */
    public ResponseEntity<String> requestStream(String method, String path, Object body,
            Long userId, String resourceId, String keyType) {
        String url = buildUrl(path, null, 0);
        HttpEntity<?> entity = createHttpEntityWithDynamicKey(body,  resourceId, keyType, 0);

        log.debug(String.format("Dify %s 流式请求: %s, userId=%s, resourceId=%s, keyType=%s, hasBody=%s",
                method, url, userId, resourceId, keyType, body != null));

        try {
            // 构建请求体
            String jsonBody;
            if (entity.getBody() instanceof String) {
                jsonBody = (String) entity.getBody();
            } else if (entity.getBody() != null) {
                jsonBody = objectMapper.writeValueAsString(entity.getBody());
            } else {
                jsonBody = "{}";
            }
            // 构建 WebClient 请求
            WebClient.RequestBodySpec requestSpec = webClient.method(HttpMethod.valueOf(method.toUpperCase()))
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM);
            // 添加请求头（从 entity 中获取）
            HttpHeaders headers = entity.getHeaders();
            if (headers != null) {
                headers.forEach((name, values) -> {
                    if (values != null && !values.isEmpty() && !name.equalsIgnoreCase("Content-Type")) {
                        requestSpec.header(name, values.get(0));
                    }
                });
            }
            // 使用 bodyToFlux 接收流式响应，然后收集所有数据
            Flux<String> eventStream = requestSpec
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToFlux(String.class);
            // 收集所有流式数据为字符串
            StringBuilder responseBodyBuilder = new StringBuilder();
            eventStream
                    .doOnNext(event -> {
                        responseBodyBuilder.append(event).append("\n");
                    })
                    .blockLast(Duration.ofMinutes(10)); // 阻塞等待流结束，最多等待10分钟
            String responseBody = responseBodyBuilder.toString();
            log.debug(String.format("流式响应读取成功: url=%s, bodyLength=%d", url, responseBody.length()));
            // 构建 ResponseEntity<String>
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.TEXT_EVENT_STREAM);
            ResponseEntity<String> response = new ResponseEntity<>(
                    responseBody,
                    responseHeaders,
                    HttpStatus.OK);

            validateResponse(url, response);
            return response;

        } catch (Exception e) {
            log.error(String.format("Dify 流式请求异常: url=%s, err=%s", url, e.getMessage()), e);
            throw new RuntimeException("Dify 流式请求异常: " + url, e);
        }
    }

    /**
     * 查询批次索引状态（用于批量上传后获取所有文件的详细信息）
     * 根据 Dify 官方文档：GET /datasets/{dataset_id}/documents/{batch}/indexing-status
     * 
     * @param datasetId  数据集ID
     * @param batch      批次ID
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return 响应结果（包含所有文件的详细信息）
     */
    public ResponseEntity<String> getBatchIndexingStatus(String datasetId, String batch, Long userId,
            String resourceId, String keyType) {
        try {
            // 修正路径：应该是 /documents/{batch}/indexing-status 而不是
            // /batch/{batch}/indexing-status
            String path = "/datasets/" + datasetId + "/documents/" + batch + "/indexing-status";
            log.debug("查询批次索引状态: datasetId={}, batch={}, userId={}, resourceId={}, keyType={}",
                    datasetId, batch, userId, resourceId, keyType);
            return request("GET", path, userId, resourceId, keyType);
        } catch (Exception e) {
            log.error("查询批次索引状态失败: datasetId={}, batch={}, error={}", datasetId, batch, e.getMessage(), e);
            throw new RuntimeException("查询批次索引状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证响应状态码
     *
     * @param url      请求URL
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
