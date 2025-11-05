package com.sciz.server.domain.pojo.entity.file;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用附件实体
 *
 * - 对应表：sys_attachment
 *
 * @author JiaWen.Wu
 * @className SysAttachment
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_attachment")
public class SysAttachment extends BaseEntity {

    /**
     * 附件ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 原始文件名
     */
    @TableField("original_name")
    private String originalName;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件扩展名
     */
    @TableField("file_extension")
    private String fileExtension;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 存储路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * MIME类型
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 文件MD5哈希值
     */
    @TableField("md5_hash")
    private String md5Hash;

    /**
     * 上传人ID
     */
    @TableField("uploader_id")
    private Long uploaderId;

    /**
     * 上传人姓名
     */
    @TableField("uploader_name")
    private String uploaderName;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 下载次数
     */
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 是否公开（0:私有,1:公开）
     */
    @TableField("is_public")
    private Integer isPublic;

    /**
     * Dify文档ID
     */
    @TableField("dify_doc_id")
    private String difyDocId;
}
