package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author JiaWen.Wu
 * @className UserStatus
 * @date 2025-10-29 10:30
 */
@Getter
public enum UserStatus {

    /**
     * 正常
     */
    NORMAL(1, "正常"),

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 锁定
     */
    LOCKED(-1, "锁定"),

    /**
     * 待激活
     */
    PENDING(2, "待激活");

    private final Integer code;
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 用户状态枚举
     */
    public static UserStatus fromCode(Integer code) {
        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的用户状态代码: " + code);
    }

    /**
     * 判断是否为正常状态
     *
     * @return 是否为正常状态
     */
    public boolean isNormal() {
        return this == NORMAL;
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
     * 判断是否为锁定状态
     *
     * @return 是否为锁定状态
     */
    public boolean isLocked() {
        return this == LOCKED;
    }
}
