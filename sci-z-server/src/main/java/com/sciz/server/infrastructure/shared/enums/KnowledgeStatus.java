package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 知识库类型枚举
 * <p>
 * 对应 sys_knowledge_base.kb_type 字段：personal=个人知识库，project=项目知识库
 *
 * @author Jiawen.Wu
 * @className KnowledgeStatus
 * @date 2026-01-29 11:00
 */
@Getter
public enum KnowledgeStatus {

    /**
     * 个人知识库（用户页面创建）
     */
    PERSONAL("personal", "个人知识库"),

    /**
     * 项目知识库（申报提交后后端自动创建）
     */
    PROJECT("project", "项目知识库");

    private final String code;
    private final String description;

    KnowledgeStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码（personal / project）
     * @return 知识库类型枚举，未匹配返回 null
     */
    public static KnowledgeStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String lower = code.trim().toLowerCase();
        for (KnowledgeStatus type : values()) {
            if (type.getCode().equals(lower)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否为有效类型编码（用于入参校验）
     *
     * @param code 编码
     * @return 是否为 personal 或 project
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
