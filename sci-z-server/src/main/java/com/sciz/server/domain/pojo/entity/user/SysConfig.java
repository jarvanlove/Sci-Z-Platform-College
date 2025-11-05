package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统配置实体
 *
 * - 对应表：sys_config
 *
 * @author JiaWen.Wu
 * @className SysConfig
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    /**
     * 配置ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键，唯一标识
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置类型：string/number/json/boolean
     */
    @TableField("config_type")
    private String configType;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否加密：0=否，1=是
     */
    @TableField("is_encrypted")
    private Integer isEncrypted;
}
