package com.sciz.server.application.service.pdf;

import com.sciz.server.infrastructure.shared.exception.BusinessException;

import java.io.InputStream;

/**
 * PDF缓存服务接口
 * 用于下载、缓存和管理PDF文件
 * 使用 presignedGetUrl 作为预览 key，返回唯一 id 用于查询、预览、删除
 *
 * @author System
 * @className PdfCacheService
 * @date 2025-01-XX
 */
public interface PdfCacheService {

    /**
     * 按业务文件id获取或创建PDF流
     * 先查 Redis(fileId→MinIO路径)：有则从 MinIO 取文件返回；无则用 url 下载并上传 MinIO，存 Redis 后取文件返回。
     *
     * @param fileId 业务文件id（前端传入），Redis key
     * @param url PDF 下载地址，无缓存时必填
     * @return PdfStreamResult 含输入流与文件名
     * @throws BusinessException 无缓存且未提供 url、下载失败或需授权等
     */
    PdfStreamResult getOrCreatePdfByFileId(String fileId, String url) throws BusinessException;

    /**
     * 下载并缓存PDF文件到MinIO
     * 先查询Redis是否有存储文件key，如果有则直接返回
     * 如果没有则下载并缓存，返回唯一id和presignedUrl
     *
     * @param pdfUrl PDF文件的URL
     * @param id 文献ID（paperInfo.id）
     * @return PdfCacheResult 缓存结果，包含id、presignedUrl和文件名
     * @throws BusinessException 如果下载失败或需要授权
     */
    PdfCacheResult downloadAndCache(String pdfUrl, String id) throws BusinessException;

    /**
     * 根据id获取PDF预览URL（重新生成presignedUrl）
     *
     * @param id PDF缓存id
     * @return String presignedUrl预览链接
     * @throws BusinessException 如果缓存不存在或文件已过期
     */
    String getPreviewUrl(String id) throws BusinessException;

    /**
     * 根据id删除PDF缓存
     * 删除Redis中的缓存记录和MinIO中的文件
     *
     * @param id PDF缓存id
     * @throws BusinessException 如果缓存不存在
     */
    void deleteCache(String id) throws BusinessException;

    /**
     * PDF缓存结果
     *
     * @param id 唯一id（用于查询、预览、删除）
     * @param presignedUrl presignedGetUrl预览链接
     * @param fileName 文件名
     */
    record PdfCacheResult(String id, String presignedUrl, String fileName) {
    }

    /**
     * PDF 流结果（按业务文件 id 获取）
     *
     * @param stream PDF 输入流，调用方负责关闭
     * @param fileName 文件名
     */
    record PdfStreamResult(InputStream stream, String fileName) {
    }
}
