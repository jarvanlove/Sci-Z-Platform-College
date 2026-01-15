package com.sciz.server.application.service.pdf.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.pdf.PdfCacheService;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.MinioUtil;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.util.Set;

/**
 * PDF缓存服务实现类
 * 所有PDF文件都存储在MinIO中，使用 presignedGetUrl 作为预览 key
 * 返回唯一 id 用于查询、预览、删除
 *
 * @author System
 * @className PdfCacheServiceImpl
 * @date 2025-01-XX
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfCacheServiceImpl implements PdfCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final MinioClient minioClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * MinIO存储桶名称
     */
    @Value("${minio.bucket:sciz-files}")
    private String bucketName;

    /**
     * WebClient 实例（用于异步非阻塞HTTP请求）
     */
    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(80 * 1024 * 1024)) // 80MB缓冲区
                    .build())
            .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
            .defaultHeader("Referer", "https://ieeexplore.ieee.org/")
            .build();

    /**
     * 缓存信息记录
     * 
     * @param filePath MinIO对象路径（objectName）
     * @param fileName 原始文件名
     * @param createTime 创建时间戳
     */
    private record CacheInfo(String filePath, String fileName, long createTime) {
    }

    @Override
    public PdfCacheResult downloadAndCache(String pdfUrl) throws BusinessException {
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "PDF URL不能为空");
        }

        // 生成唯一id（基于URL的MD5）
        String id = generateId(pdfUrl);
        String redisKey = CacheConstant.PDF_CACHE_PREFIX + id;

        // 先查询Redis是否有存储文件key
        String cachedInfoJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (cachedInfoJson != null && !cachedInfoJson.isEmpty()) {
            try {
                CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);
                // 检查MinIO中的文件是否存在
                try {
                    MinioUtil.statObject(minioClient, bucketName, cacheInfo.filePath());
                    
                    // 重新生成presignedUrl（使用presignedGetUrl）
                    String presignedUrl = MinioUtil.presignedGetUrl(
                            minioClient, 
                            bucketName, 
                            cacheInfo.filePath(), 
                            CacheConstant.PDF_PREVIEW_EXPIRE_SECONDS);
                    
                    log.info("从缓存获取PDF: id={}, filePath={}", id, cacheInfo.filePath());
                    
                    // 刷新过期时间
                    stringRedisTemplate.opsForValue().set(
                            redisKey,
                            cachedInfoJson,
                            Duration.ofSeconds(CacheConstant.PDF_CACHE_EXPIRE));
                    
                    return new PdfCacheResult(id, presignedUrl, cacheInfo.fileName());
                } catch (Exception e) {
                    // 文件不存在，删除Redis记录
                    log.warn("MinIO文件不存在，删除Redis记录: id={}, filePath={}", id, cacheInfo.filePath());
                    stringRedisTemplate.delete(redisKey);
                }
            } catch (Exception e) {
                log.warn("解析缓存信息失败，重新下载: id={}, err={}", id, e.getMessage());
                stringRedisTemplate.delete(redisKey);
            }
        }

        // 下载PDF文件并缓存
        return downloadAndSave(pdfUrl, id);
    }

    @Override
    public String getPreviewUrl(String id) throws BusinessException {
        String redisKey = CacheConstant.PDF_CACHE_PREFIX + id;
        String cachedInfoJson = stringRedisTemplate.opsForValue().get(redisKey);
        
        if (cachedInfoJson == null || cachedInfoJson.isEmpty()) {
            throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "PDF缓存不存在或已过期");
        }

        try {
            CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);
            
            // 检查MinIO中的文件是否存在
            try {
                MinioUtil.statObject(minioClient, bucketName, cacheInfo.filePath());
            } catch (Exception e) {
                // 文件不存在，删除Redis记录
                stringRedisTemplate.delete(redisKey);
                throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "PDF缓存文件不存在");
            }
            
            // 重新生成presignedUrl（使用presignedGetUrl）
            try {
                return MinioUtil.presignedGetUrl(
                        minioClient, 
                        bucketName, 
                        cacheInfo.filePath(), 
                        CacheConstant.PDF_PREVIEW_EXPIRE_SECONDS);
            } catch (Exception e) {
                log.error("生成预览URL失败: id={}, filePath={}", id, cacheInfo.filePath(), e);
                throw BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "生成预览URL失败: " + e.getMessage());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析缓存信息失败: id={}", id, e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "解析缓存信息失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream getCachedPdf(String id) throws BusinessException {
        String filePath = getCachedPdfPathInternal(id);
        try {
            return MinioUtil.download(minioClient, bucketName, filePath);
        } catch (Exception e) {
            log.error("从MinIO读取缓存PDF文件失败: id={}, filePath={}", id, filePath, e);
            throw BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "读取PDF文件失败: " + e.getMessage());
        }
    }

    @Override
    public String getCachedPdfFileName(String id) throws BusinessException {
        String redisKey = CacheConstant.PDF_CACHE_PREFIX + id;
        String cachedInfoJson = stringRedisTemplate.opsForValue().get(redisKey);
        
        if (cachedInfoJson == null || cachedInfoJson.isEmpty()) {
            throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "PDF缓存不存在或已过期");
        }

        try {
            CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);
            return cacheInfo.fileName();
        } catch (Exception e) {
            log.error("解析缓存信息失败: id={}", id, e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "解析缓存信息失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteCache(String id) throws BusinessException {
        String redisKey = CacheConstant.PDF_CACHE_PREFIX + id;
        String cachedInfoJson = stringRedisTemplate.opsForValue().get(redisKey);
        
        if (cachedInfoJson == null || cachedInfoJson.isEmpty()) {
            throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "PDF缓存不存在");
        }

        try {
            CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);
            
            // 删除MinIO中的文件
            try {
                MinioUtil.deleteObject(minioClient, bucketName, cacheInfo.filePath());
                log.info("删除MinIO文件成功: id={}, filePath={}", id, cacheInfo.filePath());
            } catch (Exception e) {
                log.warn("删除MinIO文件失败: id={}, filePath={}", id, cacheInfo.filePath(), e);
                // 继续删除Redis记录
            }
            
            // 删除Redis记录
            stringRedisTemplate.delete(redisKey);
            log.info("删除PDF缓存成功: id={}", id);
        } catch (Exception e) {
            log.error("删除PDF缓存失败: id={}", id, e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "删除PDF缓存失败: " + e.getMessage());
        }
    }

    @Override
    public void cleanupExpiredCache() {
        try {
            // 获取所有PDF缓存key
            Set<String> keys = stringRedisTemplate.keys(CacheConstant.PDF_CACHE_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            int deletedCount = 0;
            int fileDeletedCount = 0;

            for (String key : keys) {
                try {
                    String cachedInfoJson = stringRedisTemplate.opsForValue().get(key);
                    if (cachedInfoJson == null || cachedInfoJson.isEmpty()) {
                        // Redis中已过期，直接删除key
                        stringRedisTemplate.delete(key);
                        deletedCount++;
                        continue;
                    }

                    CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);

                    // 检查MinIO中的文件是否存在
                    try {
                        MinioUtil.statObject(minioClient, bucketName, cacheInfo.filePath());
                    } catch (Exception e) {
                        // 文件不存在，删除Redis记录
                        log.warn("MinIO文件不存在，删除Redis记录: filePath={}", cacheInfo.filePath());
                        stringRedisTemplate.delete(key);
                        deletedCount++;
                        continue;
                    }

                    // 检查是否过期（创建时间 + 过期时间 < 当前时间）
                    long expireTime = cacheInfo.createTime() + CacheConstant.PDF_CACHE_EXPIRE * 1000;
                    if (System.currentTimeMillis() > expireTime) {
                        // 已过期，删除MinIO中的文件和Redis记录
                        try {
                            MinioUtil.deleteObject(minioClient, bucketName, cacheInfo.filePath());
                            fileDeletedCount++;
                            log.info("删除过期PDF缓存文件: filePath={}", cacheInfo.filePath());
                        } catch (Exception e) {
                            log.warn("删除MinIO过期文件失败: filePath={}, err={}", cacheInfo.filePath(), e.getMessage());
                        }
                        stringRedisTemplate.delete(key);
                        deletedCount++;
                    }
                } catch (Exception e) {
                    log.warn("清理缓存时出错: key={}, err={}", key, e.getMessage());
                    // 出错时也删除Redis记录，避免重复处理
                    stringRedisTemplate.delete(key);
                    deletedCount++;
                }
            }

            if (deletedCount > 0 || fileDeletedCount > 0) {
                log.info("PDF缓存清理完成: 删除Redis记录={}, 删除文件={}", deletedCount, fileDeletedCount);
            }
        } catch (Exception e) {
            log.error("清理PDF缓存失败", e);
        }
    }

    /**
     * 异步下载并保存PDF文件
     *
     * @param pdfUrl PDF文件URL
     * @param id 唯一id
     * @return Mono<PdfCacheResult> 异步结果
     */
    private Mono<PdfCacheResult> downloadAndSaveAsync(String pdfUrl, String id) {
        // 使用 WebClient 异步下载文件
        return webClient.get()
                .uri(pdfUrl)
                .header("Accept", "*/*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 401 || 
                        response.statusCode().value() == 403) {
                        return Mono.error(BusinessException.of(
                                ResultCode.FORBIDDEN, 
                                "该PDF需要授权访问，请前往官网下载后手动导入知识库"));
                    }   
                    return Mono.error(BusinessException.of(
                            ResultCode.FILE_DOWNLOAD_FAILED, 
                            "PDF下载失败: HTTP " + response.statusCode().value()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return Mono.error(BusinessException.of(
                            ResultCode.FILE_DOWNLOAD_FAILED, 
                            "PDF下载失败: HTTP " + response.statusCode().value()));
                })
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(60))
                .flatMap(fileBytes -> {
                    // 验证文件内容
                    if (fileBytes == null || fileBytes.length == 0) {
                        return Mono.error(BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "下载的文件为空"));
                    }

                    // 检查文件大小，小于500KB则认为是需要授权访问的PDF
                    long fileSizeInBytes = fileBytes.length;
                    long minSizeInBytes = 500 * 1024; // 500KB
                    if (fileSizeInBytes < minSizeInBytes) {
                        log.warn("下载的PDF文件过小，可能需授权访问: url={}, size={} bytes", pdfUrl, fileSizeInBytes);
                        return Mono.error(BusinessException.of(
                                ResultCode.FORBIDDEN,
                                "该PDF需要授权访问，请手动下载后上传"));
                    }

                    try {
                        // 从URL提取文件名
                        String fileName = extractFileNameFromUrl(pdfUrl);
                        
                        // 清理文件名，移除非法字符
                        String safeFileName = sanitizeFileName(fileName);

                        // 构建MinIO对象名称（存储路径）
                        String objectName = CacheConstant.PDF_CACHE_DIR + id + "/" + safeFileName;

                        // 异步上传文件到MinIO
                        return Mono.fromCallable(() -> {
                            // 确保存储桶存在
                            MinioUtil.makeBucketIfAbsent(minioClient, bucketName);
                            
                            // 上传文件到MinIO
                            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                                MinioUtil.upload(minioClient, bucketName, objectName, inputStream, 
                                        fileBytes.length, "application/pdf");
                            }
                            return objectName;
                        })
                        .flatMap(filePath -> {
                            // 保存缓存信息到Redis
                            CacheInfo cacheInfo = new CacheInfo(
                                    filePath,
                                    fileName,
                                    System.currentTimeMillis()
                            );
                            try {
                                String cacheInfoJson = objectMapper.writeValueAsString(cacheInfo);
                                String redisKey = CacheConstant.PDF_CACHE_PREFIX + id;
                                stringRedisTemplate.opsForValue().set(
                                        redisKey,
                                        cacheInfoJson,
                                        Duration.ofSeconds(CacheConstant.PDF_CACHE_EXPIRE));

                                // 生成presignedUrl（使用presignedGetUrl）
                                String presignedUrl = MinioUtil.presignedGetUrl(
                                        minioClient, 
                                        bucketName, 
                                        filePath, 
                                        CacheConstant.PDF_PREVIEW_EXPIRE_SECONDS);

                                log.info("PDF下载并缓存成功: id={}, filePath={}, fileName={}, size={} bytes", 
                                        id, filePath, fileName, fileBytes.length);
                                
                                return Mono.just(new PdfCacheResult(id, presignedUrl, fileName));
                            } catch (Exception e) {
                                log.error("保存缓存信息到Redis失败: id={}, err={}", id, e.getMessage(), e);
                                return Mono.error(BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "保存缓存信息失败: " + e.getMessage()));
                            }
                        });
                    } catch (Exception e) {
                        log.error("处理下载文件失败: url={}, err={}", pdfUrl, e.getMessage(), e);
                        return Mono.error(BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "处理下载文件失败: " + e.getMessage()));
                    }
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof BusinessException) {
                        return throwable;
                    }
                    log.error("下载PDF文件失败: url={}, err={}", pdfUrl, throwable.getMessage(), throwable);
                    return BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "PDF下载失败: " + throwable.getMessage());
                });
    }

    /**
     * 下载并保存PDF文件（同步包装，内部使用异步）
     */
    private PdfCacheResult downloadAndSave(String pdfUrl, String id) throws BusinessException {
        return downloadAndSaveAsync(pdfUrl, id)
                .block(Duration.ofSeconds(90)); // 阻塞等待异步结果，超时90秒
    }

    /**
     * 内部方法：根据id获取PDF文件在MinIO中的对象路径
     */
    private String getCachedPdfPathInternal(String id) throws BusinessException {
        String redisKey = CacheConstant.PDF_CACHE_PREFIX + id;
        String cachedInfoJson = stringRedisTemplate.opsForValue().get(redisKey);
        
        if (cachedInfoJson == null || cachedInfoJson.isEmpty()) {
            throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "PDF缓存不存在或已过期");
        }

        try {
            CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);
            // 检查MinIO中的文件是否存在
            try {
                MinioUtil.statObject(minioClient, bucketName, cacheInfo.filePath());
                return cacheInfo.filePath();
            } catch (Exception e) {
                // 文件不存在，删除Redis记录
                stringRedisTemplate.delete(redisKey);
                throw BusinessException.of(ResultCode.DATA_NOT_FOUND, "PDF缓存文件不存在");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析缓存信息失败: id={}", id, e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "解析缓存信息失败: " + e.getMessage());
        }
    }

    /**
     * 生成唯一id（基于URL的MD5）
     */
    private String generateId(String pdfUrl) {
        String md5 = DigestUtils.md5DigestAsHex(pdfUrl.getBytes());
        return md5;
    }

    /**
     * 清理文件名，移除文件系统非法字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "document.pdf";
        }
        // 移除Windows文件系统非法字符: < > : " / \ | ? *
        String sanitized = fileName
            .replaceAll("[<>:\"/\\\\|?*]", "_")
            .replaceAll("\\s+", "_")
            .trim();
        
        // 确保有.pdf扩展名
        if (!sanitized.toLowerCase().endsWith(".pdf")) {
            sanitized = sanitized + ".pdf";
        }
        
        // 限制文件名长度
        if (sanitized.length() > 200) {
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf(".pdf"));
            String ext = sanitized.substring(sanitized.lastIndexOf(".pdf"));
            sanitized = nameWithoutExt.substring(0, 200 - ext.length()) + ext;
        }
        
        return sanitized;
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String pdfUrl) {
        try {
            URI uri = new URI(pdfUrl);
            String path = uri.getPath();
            if (path != null && path.contains("/")) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                // 移除查询参数
                if (fileName.contains("?")) {
                    fileName = fileName.substring(0, fileName.indexOf("?"));
                }
                if (fileName.contains("#")) {
                    fileName = fileName.substring(0, fileName.indexOf("#"));
                }
                if (!fileName.isEmpty()) {
                    // 如果没有扩展名，添加.pdf
                    if (!fileName.contains(".")) {
                        fileName = fileName + ".pdf";
                    }
                    return fileName;
                }
            }
        } catch (Exception e) {
            log.warn("从URL提取文件名失败: url={}", pdfUrl, e);
        }

        // 默认文件名
        return "document.pdf";
    }
}
