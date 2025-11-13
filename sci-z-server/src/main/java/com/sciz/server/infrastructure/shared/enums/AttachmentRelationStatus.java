package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 附件关联类型枚举
 * <p>
 * 用于约束附件与业务对象之间的关联，确保前后端使用统一的编码。
 *
 * @author JiaWen.Wu
 * @className AttachmentRelationStatus
 * @date 2025-11-12 20:30
 */
@Getter
public enum AttachmentRelationStatus {

    /**
     * 项目
     */
    PROJECT("project"),

    /**
     * 申报
     */
    DECLARATION("declaration"),

    /**
     * 报告
     */
    REPORT("report"),

    /**
     * 用户
     */
    USER("user"),

    /**
     * 知识库
     */
    KNOWLEDGE("knowledge");

    private final String code;

    AttachmentRelationStatus(String code) {
        this.code = code;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code String 编码
     * @return AttachmentRelationType 枚举
     */
    public static AttachmentRelationStatus fromCode(String code) {
        for (AttachmentRelationStatus type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported attachment relation type: " + code);
    }
}
