package com.sciz.server.application.service.file;

import java.io.InputStream;

/**
 * 文件格式转换服务接口
 *
 * @author JiaWen.Wu
 * @className FileConvertService
 * @date 2025-01-28 16:00
 */
public interface FileConvertService {

    /**
     * 转换文件格式
     *
     * @param sourceInputStream InputStream 源文件输入流
     * @param sourceFormat      String 源文件格式（docx/pdf）
     * @param targetFormat      String 目标文件格式（docx/pdf）
     * @param originalFileName  String 原始文件名（用于生成转换后的文件名）
     * @return ConvertResult 转换结果（包含转换后的文件流、文件名、MIME类型、文件大小）
     */
    ConvertResult convert(InputStream sourceInputStream, String sourceFormat, String targetFormat,
            String originalFileName);

    /**
     * 将 .doc（Word 97-2003）转为 .docx，用于 Dify 等仅支持 docx 的场景。
     *
     * @param docInputStream   .doc 文件输入流
     * @param originalFileName 原始文件名（含 .doc 扩展名）
     * @return ConvertResult 转换后的 docx 流与元数据
     */
    ConvertResult convertDocToDocx(InputStream docInputStream, String originalFileName);

    /**
     * 检查是否支持转换
     *
     * @param sourceFormat String 源文件格式
     * @param targetFormat String 目标文件格式
     * @return boolean 是否支持
     */
    boolean isSupported(String sourceFormat, String targetFormat);

    /**
     * 文件转换结果
     *
     * @param inputStream   InputStream 转换后的文件流
     * @param fileName      String 转换后的文件名
     * @param mimeType      String MIME类型
     * @param contentLength Long 文件大小（字节）
     */
    record ConvertResult(
            InputStream inputStream,
            String fileName,
            String mimeType,
            Long contentLength) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
