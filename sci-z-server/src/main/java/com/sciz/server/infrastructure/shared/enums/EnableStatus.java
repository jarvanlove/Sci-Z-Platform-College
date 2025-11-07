package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 启用状态枚举（0和1）
 * 适用于：启用/禁用、激活/未激活等场景
 * 对应数据库表中的 status 字段
 *
 * @author JiaWen.Wu
 * @className EnableStatus
 * @date 2025-11-07 17:45
 */
@Getter
public enum EnableStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    private final Integer code;
    private final String description;

    EnableStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 启用状态枚举
     */
    public static EnableStatus fromCode(Integer code) {
        for (EnableStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的启用状态代码: " + code);
    }

    /**
     * 判断是否为启用状态
     *
     * @return 是否为启用状态
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断是否为禁用状态
     *
     * @return 是否为禁用状态
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

    /**
     * 获取相反的状态
     *
     * @return 相反的状态
     */
    public EnableStatus reverse() {
        return this == ENABLED ? DISABLED : ENABLED;
    }
}
