package com.sciz.server.domain.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 通用基础实体
 * 
 * - 提供全局公共字段：is_deleted、created_by、updated_by、created_time、updated_time
 * - 建议所有实体继承，保持与数据库字段一一对应
 *
 * @author JiaWen.Wu
 * @className BaseEntity
 * @date 2025-10-29 16:00
 */
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * 逻辑删除标识：0=未删除，1=已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
