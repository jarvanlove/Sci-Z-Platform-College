package com.sciz.server.infrastructure.external.dify.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dify文件上传响应DTO
 *
 * @author JiaWen.Wu
 * @className DifyFileUploadResp
 * @date 2025-10-29 10:30
 */
@Data
public class DifyFileUploadResp {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 文档名称
     */
    private String documentName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 处理进度
     */
    private Integer progress;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 解析结果
     */
    private ParseResult parseResult;

    @Data
    public static class ParseResult {
        /**
         * 解析状态
         */
        private String status;

        /**
         * 解析错误
         */
        private String error;

        /**
         * 解析结果
         */
        private String result;

        /**
         * 分块数量
         */
        private Integer chunkCount;

        /**
         * 分块列表
         */
        private List<Chunk> chunks;
    }

    @Data
    public static class Chunk {
        /**
         * 分块ID
         */
        private String chunkId;

        /**
         * 内容
         */
        private String content;

        /**
         * 位置信息
         */
        private Position position;
    }

    @Data
    public static class Position {
        /**
         * 开始位置
         */
        private Integer start;

        /**
         * 结束位置
         */
        private Integer end;

        /**
         * 页码
         */
        private Integer pageNumber;
    }
}
