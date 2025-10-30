package com.sciz.server.domain.pojo.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 对话实体
 *
 * - 对应表：ai_conversation
 *
 * @author JiaWen.Wu
 * @className AiConversation
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("ai_conversation")
public class AiConversation extends BaseEntity {

    /**
     * 会话ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话标题
     */
    @TableField("title")
    private String title;

    /**
     * Dify会话ID
     */
    @TableField("dify_conversation_id")
    private String difyConversationId;

    /**
     * 是否置顶(0:否,1:是)
     */
    @TableField("is_pinned")
    private Integer isPinned;
}
