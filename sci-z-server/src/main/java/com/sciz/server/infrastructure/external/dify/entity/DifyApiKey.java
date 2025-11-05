package com.sciz.server.infrastructure.external.dify.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.infrastructure.external.dify.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Dify API 密钥实体类
 *
 * @author shihang.shang
 * @className DifyApiKey
 * @date 2025-01-28 12:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dify_api_keys")
public class DifyApiKey extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 密钥类型
     */
    @TableField("key_type")
    private String keyType;

    /**
     * 资源ID（知识库ID或工作流ID）
     */
    @TableField("resource_id")
    private String resourceId;

    /**
     * API密钥
     */
    @TableField("api_key")
    private String apiKey;

    /**
     * 密钥名称
     */
    @TableField("key_name")
    private String keyName;

    /**
     * 密钥描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 密钥类型枚举
     */
    public enum KeyType {
        DATASET("dataset", "知识库密钥"),
        WORKFLOW("workflow", "工作流密钥");

        private final String code;
        private final String description;

        KeyType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
