package com.sciz.server.infrastructure.common.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 增强注解使用示例
 * 
 * 展示如何使用增强后的 @Log、@Retry、@Idempotent 注解
 *
 * @author JiaWen.Wu
 * @className AnnotationExamples
 * @date 2025-10-29 14:00
 */
@Slf4j
@Service
public class AnnotationExamples {

    // ==================== @Log 注解示例 ====================

    /**
     * 基础日志记录
     */
    @Log("用户登录")
    public void login(String username, String password) {
        // 业务逻辑
    }

    /**
     * 详细日志记录（记录参数、返回值、执行时间、用户信息）
     */
    @Log(value = "创建项目", level = Log.LogLevel.INFO, logArgs = true, logResult = true, logExecutionTime = true, logUser = true, logIp = true, sensitiveParams = {
            "password", "token" })
    public String createProject(String projectName, String description, String password) {
        // 业务逻辑
        return "project-123";
    }

    /**
     * 错误日志记录
     */
    @Log(value = "删除项目", level = Log.LogLevel.ERROR, logException = true, logUser = true)
    public void deleteProject(Long projectId) {
        // 业务逻辑
    }

    // ==================== @Retry 注解示例 ====================

    /**
     * 基础重试（固定间隔）
     */
    @Retry(maxAttempts = 3, delay = 1000)
    public void callExternalApi() {
        // 调用外部API
    }

    /**
     * 指数退避重试
     */
    @Retry(maxAttempts = 5, delay = 1000, strategy = Retry.RetryStrategy.EXPONENTIAL, multiplier = 2.0, maxDelay = 10000, retryFor = {
            RuntimeException.class }, noRetryFor = { IllegalArgumentException.class }, logRetry = true)
    public void callUnreliableService() {
        // 调用不可靠的服务
    }

    /**
     * 条件重试
     */
    @Retry(maxAttempts = 3, delay = 2000, strategy = Retry.RetryStrategy.LINEAR, condition = "T(java.util.Optional).ofNullable(#result).map(T(java.lang.String)::isEmpty).orElse(true)", logRetry = true)
    public String fetchData(String key) {
        // 获取数据，如果为空则重试
        return null;
    }

    // ==================== @Idempotent 注解示例 ====================

    /**
     * 基础幂等性（拒绝重复请求）
     */
    @Idempotent(key = "#userId + '_' + #orderId", expire = 300)
    public void createOrder(Long userId, String orderId) {
        // 创建订单
    }

    /**
     * 返回缓存结果的幂等性
     */
    @Idempotent(key = "#projectId + '_get_project'", expire = 600, strategy = Idempotent.IdempotentStrategy.RETURN_CACHED, message = "项目信息正在处理中，请稍后查看", logIdempotent = true)
    public String getProject(Long projectId) {
        // 获取项目信息
        return "project-data";
    }

    /**
     * 等待执行的幂等性
     */
    @Idempotent(key = "#userId + '_' + #reportType", expire = 1800, strategy = Idempotent.IdempotentStrategy.WAIT_AND_RETURN, deleteAfterExecution = true, deleteOnException = true)
    public String generateReport(Long userId, String reportType) {
        // 生成报告
        return "report-data";
    }

    /**
     * 使用SpEL表达式的复杂键
     */
    @Idempotent(key = "T(java.util.UUID).randomUUID().toString() + '_' + #request.getUserId()", expire = 300, prefix = "api_call")
    public void processRequest(Object request) {
        // 处理请求
    }

    // ==================== 组合使用示例 ====================

    /**
     * 组合使用：日志 + 重试 + 幂等性
     */
    @Log(value = "支付处理", level = Log.LogLevel.INFO, logArgs = true, logResult = true, logExecutionTime = true, logUser = true, sensitiveParams = {
            "cardNumber", "cvv" })
    @Retry(maxAttempts = 3, delay = 2000, strategy = Retry.RetryStrategy.EXPONENTIAL, retryFor = {
            RuntimeException.class }, logRetry = true)
    @Idempotent(key = "#paymentId", expire = 600, strategy = Idempotent.IdempotentStrategy.REJECT, message = "支付请求正在处理中，请勿重复提交")
    public String processPayment(String paymentId, String cardNumber, String cvv, Long amount) {
        // 支付处理逻辑
        return "payment-success";
    }

    /**
     * 异步方法的重试和幂等性
     */
    @Log(value = "异步任务处理", level = Log.LogLevel.INFO)
    @Retry(maxAttempts = 5, delay = 5000, strategy = Retry.RetryStrategy.EXPONENTIAL, logRetry = true)
    @Idempotent(key = "#taskId", expire = 3600, strategy = Idempotent.IdempotentStrategy.WAIT_AND_RETURN, deleteAfterExecution = true)
    public void processAsyncTask(String taskId, String data) {
        // 异步任务处理
    }
}
