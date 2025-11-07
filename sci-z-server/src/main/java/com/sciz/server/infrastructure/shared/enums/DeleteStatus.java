package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 删除标记枚举
 * 适用于软删除场景
 * 对应数据库表中的 is_deleted 字段
 *
 * @author JiaWen.Wu
 * @className DeleteStatus
 * @date 2025-11-07 17:45
 */
@Getter
public enum DeleteStatus {

    /**
     * 未删除
     */
    NOT_DELETED(0, "未删除"),

    /**
     * 已删除
     */
    DELETED(1, "已删除");

    private final Integer code;
    private final String description;

    DeleteStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 删除标记枚举
     */
    public static DeleteStatus fromCode(Integer code) {
        for (DeleteStatus flag : values()) {
            if (flag.getCode().equals(code)) {
                return flag;
            }
        }
        throw new IllegalArgumentException("无效的删除标记代码: " + code);
    }

    /**
     * 判断是否为已删除状态
     *
     * @return 是否为已删除状态
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * 判断是否为未删除状态
     *
     * @return 是否为未删除状态
     */
    public boolean isNotDeleted() {
        return this == NOT_DELETED;
    }
}
