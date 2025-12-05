package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 项目状态枚举
 *
 * @author JiaWen.Wu
 * @className ProjectStatus
 * @date 2025-10-29 10:30
 */
@Getter
public enum ProjectStatus {

    /**
     * 未开始
     */
    NOT_STARTED(0, "未开始"),

    /**
     * 进行中
     */
    IN_PROGRESS(1, "进行中"),

    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),

    /**
     * 已延期
     */
    DELAYED(3, "已延期"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String description;

    ProjectStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 项目状态枚举
     */
    public static ProjectStatus fromCode(Integer code) {
        for (ProjectStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的项目状态代码: " + code);
    }

    /**
     * 判断是否为进行中状态
     *
     * @return 是否为进行中状态
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * 判断是否为已完成状态
     *
     * @return 是否为已完成状态
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * 判断是否为已延期状态
     *
     * @return 是否为已延期状态
     */
    public boolean isDelayed() {
        return this == DELAYED;
    }

    /**
     * 判断是否为已取消状态
     *
     * @return 是否为已取消状态
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * 判断是否为未开始状态
     *
     * @return 是否为未开始状态
     */
    public boolean isNotStarted() {
        return this == NOT_STARTED;
    }
}
