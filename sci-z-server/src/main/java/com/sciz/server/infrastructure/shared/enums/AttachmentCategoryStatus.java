package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 附件分类枚举
 * <p>
 * 用于区分附件类别（文档、图片、导出文件等），便于业务逻辑按类别处理。
 *
 * @author JiaWen.Wu
 * @className AttachmentCategoryStatus
 * @date 2025-11-12 20:30
 */
@Getter
public enum AttachmentCategoryStatus {

    /**
     * 文档类附件
     */
    DOCUMENT("document"),

    /**
     * 图片类附件
     */
    IMAGE("image"),

    /**
     * 导出文件
     */
    EXPORT("export"),

    /**
     * 其他类型
     */
    OTHER("other");

    private final String code;

    AttachmentCategoryStatus(String code) {
        this.code = code;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code String 编码
     * @return AttachmentCategoryType 枚举
     */
    public static AttachmentCategoryStatus fromCode(String code) {
        for (AttachmentCategoryStatus type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported attachment category type: " + code);
    }

    /**
     * 默认附件类型（文档）
     *
     * @return AttachmentCategoryType 默认类型
     */
    public static AttachmentCategoryStatus defaultType() {
        return DOCUMENT;
    }
}
