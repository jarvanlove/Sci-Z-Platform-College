package com.sciz.server.domain.pojo.entity.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 报告内容实体
 *
 * - 对应表：report_content
 *
 * @author JiaWen.Wu
 * @className ReportContent
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("report_content")
public class ReportContent extends BaseEntity {

    /**
     * 内容ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报告ID
     */
    @TableField("report_id")
    private Long reportId;

    /**
     * 报告标题
     */
    @TableField("title")
    private String title;

    /**
     * 报告HTML内容
     */
    @TableField("content")
    private String content;

    /**
     * 字数统计
     */
    @TableField("word_count")
    private Integer wordCount;

    /**
     * 引用文档数量
     */
    @TableField("ref_docs_count")
    private Integer refDocsCount;
}
