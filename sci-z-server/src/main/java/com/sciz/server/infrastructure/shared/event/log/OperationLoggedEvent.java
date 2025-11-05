package com.sciz.server.infrastructure.shared.event.log;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志事件（用于异步落库 sys_operation_log）
 * 
 * @author JiaWen.Wu
 * @className OperationLoggedEvent
 * @date 2025-10-29 14:00
 */
@Getter
@Setter
public class OperationLoggedEvent extends DomainEvent {

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 业务操作名称/描述
     */
    private String operation;
    /**
     * 方法标识（类名#方法名 或 HTTP 方法）
     */
    private String method;
    /**
     * 请求URL
     */
    private String requestUrl;
    /**
     * 请求参数（已脱敏）
     */
    private String requestParams;
    /**
     * 响应结果（已脱敏/截断）
     */
    private String responseResult;
    /**
     * IP 地址
     */
    private String ipAddress;
    /**
     * 地理位置
     */
    private String location;
    /**
     * 浏览器
     */
    private String browser;
    /**
     * 操作系统
     */
    private String os;
    /**
     * 操作状态（1成功 0失败）
     */
    private Integer status;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 执行耗时（毫秒）
     */
    private Integer executionTime;
    /**
     * TraceId（链路追踪）
     */
    private String traceId;

    public OperationLoggedEvent() {
        super();
    }

    public OperationLoggedEvent(Long userId, String username, String operation, String method,
            String requestUrl, String requestParams, String responseResult,
            String ipAddress, String location, String browser, String os,
            Integer status, String errorMessage, Integer executionTime,
            String traceId) {
        super();
        this.userId = userId;
        this.username = username;
        this.operation = operation;
        this.method = method;
        this.requestUrl = requestUrl;
        this.requestParams = requestParams;
        this.responseResult = responseResult;
        this.ipAddress = ipAddress;
        this.location = location;
        this.browser = browser;
        this.os = os;
        this.status = status;
        this.errorMessage = errorMessage;
        this.executionTime = executionTime;
        this.traceId = traceId;
    }

    @Override
    public String getAggregateId() {
        return userId == null ? "0" : String.valueOf(userId);
    }

    @Override
    public String getAggregateType() {
        return "OperationLog";
    }
}
