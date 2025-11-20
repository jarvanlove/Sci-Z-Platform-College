package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 申报状态枚举
 *
 * @author JiaWen.Wu
 * @className DeclarationStatus
 * @date 2025-10-29 10:30
 */
@Getter
public enum DeclarationStatus {

    /**
     * 申报中
     */
    IN_PROGRESS(1, "申报中"),

    /**
     * 申报成功
     */
    SUCCESS(2, "申报成功"),

    /**
     * 申报失败
     */
    FAILED(3, "申报失败");

    private final Integer code;
    private final String description;

    DeclarationStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 申报状态枚举
     */
    public static DeclarationStatus fromCode(Integer code) {
        for (DeclarationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的申报状态代码: " + code);
    }

    /**
     * 判断是否为申报中状态
     *
     * @return 是否为申报中状态
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * 判断是否为申报成功状态
     *
     * @return 是否为申报成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为申报失败状态
     *
     * @return 是否为申报失败状态
     */
    public boolean isFailed() {
        return this == FAILED;
    }
}
