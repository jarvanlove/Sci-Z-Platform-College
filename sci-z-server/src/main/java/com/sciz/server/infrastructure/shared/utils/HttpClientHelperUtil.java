package com.sciz.server.infrastructure.shared.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Java 21 HttpClient 工具类
 *
 * <p>
 * 提供常用 HTTP 请求封装，支持自定义请求头、超时时间、简单重试策略等能力。
 * 设计用于脱离 Spring Bean 的基础设施场景，业务代码可直接静态调用。
 * </p>
 *
 * @author JiaWen.Wu
 * @className HttpClientHelperUtil
 * @date 2025-11-12 14:00
 */
public final class HttpClientHelperUtil {

        private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
        private static final int DEFAULT_RETRY = 0;
        private static final HttpClient CLIENT = HttpClient.newBuilder()
                        .connectTimeout(DEFAULT_TIMEOUT)
                        .version(HttpClient.Version.HTTP_2)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build();

        private HttpClientHelperUtil() {
        }

        /**
         * HTTP 方法枚举
         *
         * <p>
         * 仅保留当前工具类支持的同步请求类型；若后续扩展其他方法，可在此枚举中追加。
         * </p>
         */
        public enum HttpMethod {
                GET, POST, PUT, DELETE
        }

        /**
         * 请求选项
         *
         * <p>
         * 使用不可变 Record 封装一次 HTTP 调用的全部参数，便于在辅助方法之间安全传递。
         * </p>
         *
         * @param url         String 请求地址
         * @param method      HttpMethod HTTP 方法
         * @param headers     Map<String, String> 自定义请求头
         * @param body        String 请求体内容
         * @param timeout     Duration 单次请求超时时间
         * @param retryTimes  int 重试次数
         * @param contentType String Content-Type 请求头
         */
        public record RequestOptions(
                        String url,
                        HttpMethod method,
                        Map<String, String> headers,
                        String body,
                        Duration timeout,
                        int retryTimes,
                        String contentType) {

                public RequestOptions {
                        Objects.requireNonNull(url, "url 不能为空");
                        Objects.requireNonNull(method, "method 不能为空");
                        headers = headers == null ? Map.of() : Map.copyOf(headers);
                        timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
                        retryTimes = Math.max(retryTimes, 0);
                }

                /**
                 * 构建器入口
                 *
                 * @param url    String 请求地址
                 * @param method HttpMethod HTTP 方法
                 * @return Builder 构建器
                 */
                public static Builder builder(String url, HttpMethod method) {
                        return new Builder(url, method);
                }

                /**
                 * RequestOptions 构建器
                 */
                public static final class Builder {
                        private final String url;
                        private final HttpMethod method;
                        private Map<String, String> headers = new LinkedHashMap<>();
                        private String body;
                        private Duration timeout = DEFAULT_TIMEOUT;
                        private int retryTimes = DEFAULT_RETRY;
                        private String contentType;

                        private Builder(String url, HttpMethod method) {
                                this.url = url;
                                this.method = method;
                        }

                        /**
                         * 设置请求头
                         *
                         * @param headers Map<String, String> 请求头
                         * @return Builder 构建器本身
                         */
                        public Builder headers(Map<String, String> headers) {
                                if (headers != null) {
                                        this.headers.putAll(headers);
                                }
                                return this;
                        }

                        /**
                         * 设置请求体
                         *
                         * @param body        String 请求体内容
                         * @param contentType String Content-Type 请求头
                         * @return Builder 构建器本身
                         */
                        public Builder body(String body, String contentType) {
                                this.body = body;
                                this.contentType = contentType;
                                return this;
                        }

                        /**
                         * 设置超时时间
                         *
                         * @param timeout Duration 超时时间
                         * @return Builder 构建器本身
                         */
                        public Builder timeout(Duration timeout) {
                                if (timeout != null) {
                                        this.timeout = timeout;
                                }
                                return this;
                        }

                        /**
                         * 设置重试次数
                         *
                         * @param retryTimes int 重试次数
                         * @return Builder 构建器本身
                         */
                        public Builder retryTimes(int retryTimes) {
                                this.retryTimes = Math.max(retryTimes, 0);
                                return this;
                        }

                        /**
                         * 构建请求选项
                         *
                         * @return RequestOptions 请求参数对象
                         */
                        public RequestOptions build() {
                                return new RequestOptions(url, method, headers, body, timeout, retryTimes, contentType);
                        }
                }
        }

        /**
         * 执行 HTTP 请求
         *
         * <p>
         * 内部统一入口，负责根据配置构造 HttpRequest 并执行，同步处理简单的指数退避重试策略。
         * 当前重试条件：响应状态码为 5xx 或 429，且剩余重试次数大于 0。
         * </p>
         *
         * @param options RequestOptions 请求配置
         * @return HttpResponse<String> 响应结果
         * @throws Exception 网络异常或构造请求异常
         */
        public static HttpResponse<String> execute(RequestOptions options) throws Exception {
                HttpRequest request = buildHttpRequest(options);
                int attempts = 0;
                Exception lastException = null;
                while (attempts <= options.retryTimes()) {
                        try {
                                HttpResponse<String> response = CLIENT.send(request,
                                                HttpResponse.BodyHandlers.ofString());
                                if (shouldRetry(response, attempts, options.retryTimes())) {
                                        attempts++;
                                        Thread.sleep(calculateBackoff(attempts));
                                        continue;
                                }
                                return response;
                        } catch (Exception exception) {
                                lastException = exception;
                                attempts++;
                                if (attempts > options.retryTimes()) {
                                        throw exception;
                                }
                                Thread.sleep(calculateBackoff(attempts));
                        }
                }
                throw lastException != null ? lastException : new IllegalStateException("未知错误");
        }

        /**
         * 异步执行 HTTP 请求
         *
         * <p>
         * 使用 Java 21 原生 HttpClient 的 {@code sendAsync} 方法，配合与同步版本一致的重试策略。
         * </p>
         *
         * @param options RequestOptions 请求配置
         * @return CompletableFuture<HttpResponse<String>> 异步响应结果
         */
        public static CompletableFuture<HttpResponse<String>> executeAsync(RequestOptions options) {
                HttpRequest request = buildHttpRequest(options);
                return executeAsyncWithRetry(request, options, 0);
        }

        /**
         * 异步执行 HTTP 请求并重试
         *
         * @param request  HttpRequest 请求对象
         * @param options  RequestOptions 请求配置
         * @param attempts int 已尝试次数
         * @return CompletableFuture<HttpResponse<String>> 异步响应结果
         */
        private static CompletableFuture<HttpResponse<String>> executeAsyncWithRetry(HttpRequest request,
                        RequestOptions options,
                        int attempts) {
                return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                                .handle((response, throwable) -> new AsyncSendResult(response, throwable))
                                .thenCompose(result -> {
                                        if (result.throwable() != null) {
                                                if (attempts >= options.retryTimes()) {
                                                        return CompletableFuture.failedFuture(result.throwable());
                                                }
                                                int nextAttempt = attempts + 1;
                                                return delay(calculateBackoff(nextAttempt))
                                                                .thenCompose(ignored -> executeAsyncWithRetry(request,
                                                                                options,
                                                                                nextAttempt));
                                        }

                                        if (shouldRetry(result.response(), attempts, options.retryTimes())) {
                                                int nextAttempt = attempts + 1;
                                                return delay(calculateBackoff(nextAttempt))
                                                                .thenCompose(ignored -> executeAsyncWithRetry(request,
                                                                                options,
                                                                                nextAttempt));
                                        }
                                        return CompletableFuture.completedFuture(result.response());
                                });
        }

        /**
         * 判断是否需要重试
         *
         * @param response HttpResponse<String> 响应对象
         * @param attempts int 已尝试次数
         * @param maxRetry int 最大重试次数
         * @return boolean 是否继续重试
         */
        private static boolean shouldRetry(HttpResponse<String> response, int attempts, int maxRetry) {
                if (attempts >= maxRetry) {
                        return false;
                }
                int status = response.statusCode();
                return status >= 500 || status == 429;
        }

        /**
         * 计算退避时间
         *
         * @param attempts int 已尝试次数
         * @return long 休眠毫秒数
         */
        private static long calculateBackoff(int attempts) {
                return Math.min(1000L * attempts, 5000L);
        }

        /**
         * GET 请求
         *
         * @param url        String 请求地址
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return HttpResponse<String> 响应结果
         * @throws Exception 网络异常或执行异常
         */
        public static HttpResponse<String> get(String url, Map<String, String> headers, Duration timeout,
                        int retryTimes)
                        throws Exception {
                return execute(RequestOptions.builder(url, HttpMethod.GET)
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * GET 请求（无额外配置）
         *
         * @param url String 请求地址
         * @return HttpResponse<String> 响应结果
         * @throws Exception 网络异常或执行异常
         */
        public static HttpResponse<String> get(String url) throws Exception {
                return get(url, Map.of(), DEFAULT_TIMEOUT, DEFAULT_RETRY);
        }

        /**
         * POST JSON 请求
         *
         * @param url        String 请求地址
         * @param jsonBody   String JSON 请求体
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return HttpResponse<String> 响应结果
         * @throws Exception 网络异常或执行异常
         */
        public static HttpResponse<String> postJson(String url, String jsonBody, Map<String, String> headers,
                        Duration timeout, int retryTimes) throws Exception {
                return execute(RequestOptions.builder(url, HttpMethod.POST)
                                .body(jsonBody, "application/json")
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * POST 表单请求（application/x-www-form-urlencoded）
         *
         * @param url        String 请求地址
         * @param form       Map<String, String> 表单参数
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return HttpResponse<String> 响应结果
         * @throws Exception 网络异常或执行异常
         */
        public static HttpResponse<String> postForm(String url, Map<String, String> form, Map<String, String> headers,
                        Duration timeout, int retryTimes) throws Exception {
                String body = buildFormBody(form);
                return execute(RequestOptions.builder(url, HttpMethod.POST)
                                .body(body, "application/x-www-form-urlencoded")
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * PUT JSON 请求
         *
         * @param url        String 请求地址
         * @param jsonBody   String JSON 请求体
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return HttpResponse<String> 响应结果
         * @throws Exception 网络异常或执行异常
         */
        public static HttpResponse<String> putJson(String url, String jsonBody, Map<String, String> headers,
                        Duration timeout, int retryTimes) throws Exception {
                return execute(RequestOptions.builder(url, HttpMethod.PUT)
                                .body(jsonBody, "application/json")
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * DELETE 请求
         *
         * @param url        String 请求地址
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return HttpResponse<String> 响应结果
         * @throws Exception 网络异常或执行异常
         */
        public static HttpResponse<String> delete(String url, Map<String, String> headers, Duration timeout,
                        int retryTimes)
                        throws Exception {
                return execute(RequestOptions.builder(url, HttpMethod.DELETE)
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * 构建表单请求体
         *
         * @param form Map<String, String> 表单参数
         * @return String URL 编码后的正文
         */
        private static String buildFormBody(Map<String, String> form) {
                return form.entrySet().stream()
                                .map(entry -> String.format("%s=%s",
                                                encode(entry.getKey()),
                                                encode(entry.getValue())))
                                .reduce((left, right) -> left + "&" + right)
                                .orElse("");
        }

        /**
         * URL 编码
         *
         * @param value String 待编码值
         * @return String 编码结果
         */
        private static String encode(String value) {
                return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
        }

        /**
         * 构建 HttpRequest
         *
         * @param options RequestOptions 请求选项
         * @return HttpRequest 构建后的请求
         */
        private static HttpRequest buildHttpRequest(RequestOptions options) {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                                .uri(URI.create(options.url()))
                                .timeout(options.timeout());

                options.headers().forEach(builder::header);
                if (options.contentType() != null && !options.contentType().isBlank()) {
                        builder.header("Content-Type", options.contentType());
                }

                switch (options.method()) {
                        case GET -> builder.GET();
                        case DELETE -> builder.DELETE();
                        case POST -> builder.POST(HttpRequest.BodyPublishers
                                        .ofString(options.body() == null ? "" : options.body()));
                        case PUT -> builder.PUT(HttpRequest.BodyPublishers
                                        .ofString(options.body() == null ? "" : options.body()));
                        default -> throw new IllegalArgumentException("不支持的 HTTP 方法: " + options.method());
                }
                return builder.build();
        }

        /**
         * 延迟执行
         *
         * @param millis long 延迟毫秒数
         * @return CompletableFuture<Void> 完成信号
         */
        private static CompletableFuture<Void> delay(long millis) {
                long delayMillis = Math.max(millis, 0L);
                if (delayMillis == 0L) {
                        return CompletableFuture.completedFuture(null);
                }
                return CompletableFuture.runAsync(() -> {
                }, CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS));
        }

        /**
         * 异步发送结果容器
         *
         * @param response  HttpResponse<String> 响应
         * @param throwable Throwable 异常
         */
        private record AsyncSendResult(HttpResponse<String> response, Throwable throwable) {
        }

        /**
         * 异步 GET 请求
         *
         * @param url        String 请求地址
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return CompletableFuture<HttpResponse<String>> 异步响应
         */
        public static CompletableFuture<HttpResponse<String>> getAsync(String url, Map<String, String> headers,
                        Duration timeout,
                        int retryTimes) {
                return executeAsync(RequestOptions.builder(url, HttpMethod.GET)
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * 异步 GET 请求（无额外配置）
         *
         * @param url String 请求地址
         * @return CompletableFuture<HttpResponse<String>> 异步响应
         */
        public static CompletableFuture<HttpResponse<String>> getAsync(String url) {
                return getAsync(url, Map.of(), DEFAULT_TIMEOUT, DEFAULT_RETRY);
        }

        /**
         * 异步 POST JSON 请求
         *
         * @param url        String 请求地址
         * @param jsonBody   String JSON 请求体
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return CompletableFuture<HttpResponse<String>> 异步响应
         */
        public static CompletableFuture<HttpResponse<String>> postJsonAsync(String url, String jsonBody,
                        Map<String, String> headers,
                        Duration timeout, int retryTimes) {
                return executeAsync(RequestOptions.builder(url, HttpMethod.POST)
                                .body(jsonBody, "application/json")
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * 异步 POST 表单请求
         *
         * @param url        String 请求地址
         * @param form       Map<String, String> 表单参数
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return CompletableFuture<HttpResponse<String>> 异步响应
         */
        public static CompletableFuture<HttpResponse<String>> postFormAsync(String url, Map<String, String> form,
                        Map<String, String> headers,
                        Duration timeout, int retryTimes) {
                String body = buildFormBody(form);
                return executeAsync(RequestOptions.builder(url, HttpMethod.POST)
                                .body(body, "application/x-www-form-urlencoded")
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * 异步 PUT JSON 请求
         *
         * @param url        String 请求地址
         * @param jsonBody   String JSON 请求体
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return CompletableFuture<HttpResponse<String>> 异步响应
         */
        public static CompletableFuture<HttpResponse<String>> putJsonAsync(String url, String jsonBody,
                        Map<String, String> headers,
                        Duration timeout, int retryTimes) {
                return executeAsync(RequestOptions.builder(url, HttpMethod.PUT)
                                .body(jsonBody, "application/json")
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * 异步 DELETE 请求
         *
         * @param url        String 请求地址
         * @param headers    Map<String, String> 自定义请求头
         * @param timeout    Duration 超时时间
         * @param retryTimes int 重试次数
         * @return CompletableFuture<HttpResponse<String>> 异步响应
         */
        public static CompletableFuture<HttpResponse<String>> deleteAsync(String url, Map<String, String> headers,
                        Duration timeout,
                        int retryTimes) {
                return executeAsync(RequestOptions.builder(url, HttpMethod.DELETE)
                                .headers(headers)
                                .timeout(timeout)
                                .retryTimes(retryTimes)
                                .build());
        }

        /**
         * 异步 DELETE 请求（无额外配置）
         *
         * @param url String 请求地址
         * @return CompletableFuture<HttpResponse<String>> 异步响应
         */
        public static CompletableFuture<HttpResponse<String>> deleteAsync(String url) {
                return deleteAsync(url, Map.of(), DEFAULT_TIMEOUT, DEFAULT_RETRY);
        }
}
