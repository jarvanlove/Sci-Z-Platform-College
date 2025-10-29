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

    private Long userId;
    private String username;
    private String operation; // 业务操作名称/描述
    private String method; // 类名#方法名 或 HTTP 方法
    private String requestUrl;
    private String requestParams; // 已脱敏
    private String responseResult; // 已脱敏/截断
    private String ipAddress;
    private String location;
    private String browser;
    private String os;
    private Integer status; // 1 成功 0 失败
    private String errorMessage;
    private Integer executionTime; // 毫秒
    private String traceId;

    @Override
    public String getAggregateId() {
        return userId == null ? "0" : String.valueOf(userId);
    }

    @Override
    public String getAggregateType() {
        return "OperationLog";
    }
}
