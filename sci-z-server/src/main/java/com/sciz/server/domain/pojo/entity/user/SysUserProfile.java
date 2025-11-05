package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户扩展属性实体
 *
 * - 对应表：sys_user_profile
 * - 存储行业或系统自定义的用户扩展属性
 *
 * @author JiaWen.Wu
 * @className SysUserProfile
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_user_profile")
public class SysUserProfile extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 属性名称
     */
    @TableField("attribute_name")
    private String attributeName;

    /**
     * 属性值
     */
    @TableField("attribute_value")
    private String attributeValue;

    /**
     * 属性类型(text/select/number/date)
     */
    @TableField("attribute_type")
    private String attributeType;

    /**
     * 是否必填(0:否,1:是)
     */
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
