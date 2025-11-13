package com.sciz.server.domain.pojo.dto.response.file;

import io.minio.GetObjectResponse;

/**
 * 文件下载上下文
 *
 * <p>
 * 封装下载所需的元信息与数据流，调用方在处理完成后应显式关闭 {@link #inputStream()}。
 * </p>
 *
 * @param fileName      String 存储文件名
 * @param originalName  String 原始文件名
 * @param contentType   String 内容类型
 * @param contentLength Long 内容长度
 * @param inputStream   GetObjectResponse 文件数据流
 *
 * @author JiaWen.Wu
 * @className FileDownloadContext
 * @date 2025-11-11 17:25
 */
public record FileDownloadContext(
        String fileName,
        String originalName,
        String contentType,
        Long contentLength,
        GetObjectResponse inputStream) implements AutoCloseable {

    @Override
    public void close() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
