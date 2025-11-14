package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 系统日志级别
 *
 * @author JiaWen.Wu
 * @className LogLevelStatus
 * @date 2025-11-15 09:00
 */
@Getter
public enum LogLevelStatus {

    /**
     * 信息
     */
    INFO("INFO", "信息"),

    /**
     * 警告
     */
    WARN("WARN", "警告"),

    /**
     * 错误
     */
    ERROR("ERROR", "错误");

    private final String code;
    private final String description;

    LogLevelStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码解析日志级别
     *
     * @param code String 代码
     * @return LogLevel
     */
    public static LogLevelStatus fromCode(String code) {
        if (code == null) {
            return INFO;
        }
        var normalized = code.trim().toUpperCase();
        for (LogLevelStatus level : values()) {
            if (level.code.equals(normalized)) {
                return level;
            }
        }
        return INFO;
    }
}
