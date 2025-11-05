package com.server.agentbackendservices.modules.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含通用字段：主键、创建时间、更新时间、逻辑删除等
 * 
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Data
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 主键ID
     */

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField( fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 逻辑删除标识
     * 0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 版本号（乐观锁）
     */
//    @Version
//    private Integer version;

    // 查询条件字段（不映射到数据库）
    @TableField(exist = false)
    private int pageNum = 1;
    
    @TableField(exist = false)
    private int pageSize = 10;
    
    @TableField(exist = false)
    private String where;
    
    @TableField(exist = false)
    private String order;
    
    @TableField(exist = false)
    private String key;
}
