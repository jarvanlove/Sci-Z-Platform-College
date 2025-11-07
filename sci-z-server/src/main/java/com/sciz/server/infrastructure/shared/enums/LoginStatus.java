package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 登录状态枚举
 * 用于登录日志记录
 *
 * @author JiaWen.Wu
 * @className LoginStatus
 * @date 2025-11-07 17:45
 */
@Getter
public enum LoginStatus {

    /**
     * 登录失败
     */
    FAILURE(0, "登录失败"),

    /**
     * 登录成功
     */
    SUCCESS(1, "登录成功");

    private final Integer code;
    private final String description;

    LoginStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 登录状态枚举
     */
    public static LoginStatus fromCode(Integer code) {
        for (LoginStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的登录状态代码: " + code);
    }

    /**
     * 判断是否为成功状态
     *
     * @return 是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为失败状态
     *
     * @return 是否为失败状态
     */
    public boolean isFailure() {
        return this == FAILURE;
    }
}
