package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 登录类型枚举
 *
 * @author JiaWen.Wu
 * @className LoginType
 * @date 2025-01-15 17:30
 */
@Getter
public enum LoginTypeStatus {

    /**
     * 账号密码登录
     */
    PASSWORD("password", "账号密码登录"),

    /**
     * 短信验证码登录
     */
    SMS("sms", "短信验证码登录");

    private final String code;
    private final String description;

    LoginTypeStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 登录类型枚举
     */
    public static LoginTypeStatus fromCode(String code) {
        if (code == null) {
            return SMS; // 默认返回短信登录
        }
        for (LoginTypeStatus type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的登录类型代码: " + code);
    }

    /**
     * 判断是否为密码登录
     *
     * @return 是否为密码登录
     */
    public boolean isPassword() {
        return this == PASSWORD;
    }

    /**
     * 判断是否为短信登录
     *
     * @return 是否为短信登录
     */
    public boolean isSms() {
        return this == SMS;
    }
}

