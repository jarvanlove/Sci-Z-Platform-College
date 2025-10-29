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
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 待审批
     */
    PENDING(1, "待审批"),

    /**
     * 已审批
     */
    APPROVED(2, "已审批"),

    /**
     * 进行中
     */
    IN_PROGRESS(3, "进行中"),

    /**
     * 已完成
     */
    COMPLETED(4, "已完成"),

    /**
     * 已暂停
     */
    SUSPENDED(5, "已暂停"),

    /**
     * 已取消
     */
    CANCELLED(6, "已取消"),

    /**
     * 已驳回
     */
    REJECTED(7, "已驳回");

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
     * 判断是否为草稿状态
     *
     * @return 是否为草稿状态
     */
    public boolean isDraft() {
        return this == DRAFT;
    }

    /**
     * 判断是否为待审批状态
     *
     * @return 是否为待审批状态
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 判断是否为已审批状态
     *
     * @return 是否为已审批状态
     */
    public boolean isApproved() {
        return this == APPROVED;
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
     * 判断是否为已暂停状态
     *
     * @return 是否为已暂停状态
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
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
     * 判断是否为已驳回状态
     *
     * @return 是否为已驳回状态
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
}
