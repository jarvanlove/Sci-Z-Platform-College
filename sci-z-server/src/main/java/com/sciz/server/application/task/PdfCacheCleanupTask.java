package com.sciz.server.application.task;

import com.sciz.server.application.service.pdf.PdfCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * PDF缓存清理任务
 * 定期清理过期的PDF缓存文件
 *
 * @author System
 * @className PdfCacheCleanupTask
 * @date 2025-01-XX
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfCacheCleanupTask {

    private final PdfCacheService pdfCacheService;
//
//    /**
//     * 每5分钟执行一次清理任务
//     */
//    @Scheduled(cron = "0 0/5 * * * ?")
//    public void cleanupExpiredCache() {
//        try {
//            log.info("开始执行PDF缓存清理任务");
//            pdfCacheService.cleanupExpiredCache();
//            log.info("PDF缓存清理任务执行完成");
//        } catch (Exception e) {
//            log.error("PDF缓存清理任务执行失败", e);
//        }
//    }
}