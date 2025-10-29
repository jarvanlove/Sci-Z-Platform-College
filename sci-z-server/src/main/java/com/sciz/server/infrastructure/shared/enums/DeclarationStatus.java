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
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 待提交
     */
    PENDING_SUBMIT(1, "待提交"),

    /**
     * 已提交
     */
    SUBMITTED(2, "已提交"),

    /**
     * 待审批
     */
    PENDING_APPROVAL(3, "待审批"),

    /**
     * 审批中
     */
    APPROVING(4, "审批中"),

    /**
     * 已通过
     */
    APPROVED(5, "已通过"),

    /**
     * 已驳回
     */
    REJECTED(6, "已驳回"),

    /**
     * 已撤回
     */
    WITHDRAWN(7, "已撤回");

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
     * 判断是否为草稿状态
     *
     * @return 是否为草稿状态
     */
    public boolean isDraft() {
        return this == DRAFT;
    }

    /**
     * 判断是否为待提交状态
     *
     * @return 是否为待提交状态
     */
    public boolean isPendingSubmit() {
        return this == PENDING_SUBMIT;
    }

    /**
     * 判断是否为已提交状态
     *
     * @return 是否为已提交状态
     */
    public boolean isSubmitted() {
        return this == SUBMITTED;
    }

    /**
     * 判断是否为待审批状态
     *
     * @return 是否为待审批状态
     */
    public boolean isPendingApproval() {
        return this == PENDING_APPROVAL;
    }

    /**
     * 判断是否为审批中状态
     *
     * @return 是否为审批中状态
     */
    public boolean isApproving() {
        return this == APPROVING;
    }

    /**
     * 判断是否为已通过状态
     *
     * @return 是否为已通过状态
     */
    public boolean isApproved() {
        return this == APPROVED;
    }

    /**
     * 判断是否为已驳回状态
     *
     * @return 是否为已驳回状态
     */
    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * 判断是否为已撤回状态
     *
     * @return 是否为已撤回状态
     */
    public boolean isWithdrawn() {
        return this == WITHDRAWN;
    }
}
