package com.sciz.server.domain.pojo.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 站内消息实体
 * <p>
 * 对应表：sys_message。用于产教研分发通知、接受/拒绝结果及系统通知。
 * </p>
 *
 * @author Sci-Z
 */
@Getter
@Setter
@TableName("sys_message")
public class SysMessage extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 消息类型：industry_education_distribute、system_notice 等 */
    @TableField("type")
    private String type;

    @TableField("sender_id")
    private Long senderId;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    /** 扩展 JSON：topic_label、target_team_id、reject_reason 等 */
    @TableField("extra_json")
    private String extraJson;

    /** 状态：pending、read、accepted、rejected */
    @TableField("status")
    private String status;

    @TableField("read_time")
    private LocalDateTime readTime;
}
