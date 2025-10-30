package com.sciz.server.domain.pojo.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目成员实体
 *
 * - 对应表：project_member
 *
 * @author JiaWen.Wu
 * @className ProjectMember
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("project_member")
public class ProjectMember extends BaseEntity {

    /**
     * 成员ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 角色
     */
    @TableField("role")
    private String role;

    /**
     * 加入时间
     */
    @TableField("join_time")
    private LocalDateTime joinTime;
}
