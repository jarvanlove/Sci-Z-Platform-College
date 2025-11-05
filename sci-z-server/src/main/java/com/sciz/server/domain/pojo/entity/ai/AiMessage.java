package com.sciz.server.domain.pojo.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 消息实体
 *
 * - 对应表：ai_message
 *
 * @author JiaWen.Wu
 * @className AiMessage
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("ai_message")
public class AiMessage extends BaseEntity {

    /**
     * 消息ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 角色(user/assistant)
     */
    @TableField("role")
    private String role;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * Dify消息ID
     */
    @TableField("dify_message_id")
    private String difyMessageId;

    /**
     * 知识来源(JSON)
     */
    @TableField("sources")
    private String sources;

    /**
     * 置信度
     */
    @TableField("confidence")
    private BigDecimal confidence;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private LocalDateTime sendTime;
}
