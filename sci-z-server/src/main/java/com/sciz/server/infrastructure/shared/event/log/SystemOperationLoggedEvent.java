package com.sciz.server.infrastructure.shared.event.log;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统操作日志事件
 *
 * @author JiaWen.Wu
 * @className SystemOperationLoggedEvent
 * @date 2025-11-14 21:00
 */
@Getter
@Setter
public class SystemOperationLoggedEvent extends DomainEvent {

    /**
     * 日志级别（INFO/WARN/ERROR）
     */
    private String level;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 操作名称
     */
    private String operation;
    /**
     * 详细描述
     */
    private String detail;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求URL
     */
    private String requestUrl;
    /**
     * 请求参数
     */
    private String requestParams;
    /**
     * 响应结果
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
     * 执行耗时（毫秒）
     */
    private Integer executionTime;

    public SystemOperationLoggedEvent() {
        super();
    }

    @Override
    public String getAggregateId() {
        return Optional.ofNullable(userId)
                .map(String::valueOf)
                .orElse("0");
    }

    @Override
    public String getAggregateType() {
        return "SystemOperationLog";
    }
}
