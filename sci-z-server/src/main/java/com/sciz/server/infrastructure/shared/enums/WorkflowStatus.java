package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 工作流状态枚举
 *
 * @author JiaWen.Wu
 * @className WorkflowStatus
 * @date 2025-01-20 15:00
 */
@Getter
public enum WorkflowStatus {

    /**
     * 待处理
     */
    PENDING("pending", "待处理"),

    /**
     * 处理中
     */
    RUNNING("running", "处理中"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 失败
     */
    FAILED("failed", "失败");

    private final String code;
    private final String description;

    WorkflowStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 工作流状态枚举
     */
    public static WorkflowStatus fromCode(String code) {
        for (WorkflowStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的工作流状态代码: " + code);
    }

    /**
     * 判断是否为待处理状态
     *
     * @return 是否为待处理状态
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 判断是否为处理中状态
     *
     * @return 是否为处理中状态
     */
    public boolean isRunning() {
        return this == RUNNING;
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
     * 判断是否为失败状态
     *
     * @return 是否为失败状态
     */
    public boolean isFailed() {
        return this == FAILED;
    }
}
