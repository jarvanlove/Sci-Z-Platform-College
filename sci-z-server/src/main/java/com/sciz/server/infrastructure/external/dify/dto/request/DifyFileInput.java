package com.sciz.server.infrastructure.external.dify.dto.request;

/**
 * 文件输入值（单个文件）
 *
 * @param transferMethod String 传输方式（固定为 "local_file"）
 * @param uploadFileId   String Dify 文件ID
 * @param type           String 文件类型（document/image/audio/video）
 * @author JiaWen.Wu
 * @className DifyFileInput
 * @date 2025-01-26 15:00
 */
public record DifyFileInput(
        String transferMethod,
        String uploadFileId,
        String type) implements DifyInputValue {

    /**
     * 使用默认传输方式创建文件输入
     *
     * @param uploadFileId String Dify 文件ID
     * @param type         String 文件类型
     * @return DifyFileInput 文件输入
     */
    public static DifyFileInput of(String uploadFileId, String type) {
        return new DifyFileInput("local_file", uploadFileId, type);
    }
}
