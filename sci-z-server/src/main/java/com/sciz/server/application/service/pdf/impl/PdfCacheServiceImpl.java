package com.sciz.server.application.service.pdf.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.application.service.pdf.PdfCacheService;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.MinioUtil;
import io.minio.MinioClient;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.io.*;
import java.net.URI;
import java.time.Duration;

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
    private final FileService fileService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * MinIO存储桶名称
     */
    @Value("${minio.bucket:sciz-files}")
    private String bucketName;

    /**
     * WebClient 实例（用于异步非阻塞HTTP请求）
     * 超时配置：
     * - 连接超时：20秒（如果20秒内无法建立连接，则超时）
     * - 下载超时：60秒（连接成功后，下载数据的时间限制为60秒）
     */
    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000) // 连接超时20秒（无法连接时）
                            .doOnConnected(conn -> 
                                conn.addHandlerLast(new ReadTimeoutHandler(60)) // 读取超时60秒（连接成功后下载时间）
                                    .addHandlerLast(new WriteTimeoutHandler(20)) // 写入超时20秒
                            )
            ))
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
     * @param fileUrl 文件URL（格式：bucketName/filePath）
     * @param fileName 原始文件名
     * @param createTime 创建时间戳
     */
    private record CacheInfo(String filePath, String fileUrl, String fileName, long createTime) {
    }

    @Override
    public PdfStreamResult getOrCreatePdfByFileId(String fileId, String url) throws BusinessException {
        if (fileId == null || fileId.isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "文件id不能为空");
        }
        
        // 统一使用业务文件id作为缓存key
        String cacheKey = CacheConstant.PDF_CACHE_PREFIX + fileId;
        String cachedInfoJson = stringRedisTemplate.opsForValue().get(cacheKey);

        // 如果缓存存在，直接返回文件流
        if (cachedInfoJson != null && !cachedInfoJson.isEmpty()) {
            try {
                CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);
                String filePath = cacheInfo.filePath();
                
                // 检查MinIO中的文件是否存在
                MinioUtil.statObject(minioClient, bucketName, filePath);
                
                String fileName = cacheInfo.fileName();
                InputStream stream = MinioUtil.download(minioClient, bucketName, filePath);
                log.info("从缓存获取PDF: fileId={}, filePath={}", fileId, filePath);
                return new PdfStreamResult(stream, fileName);
            } catch (Exception e) {
                log.warn("缓存文件不存在或读取失败，删除缓存: fileId={}, err={}", fileId, e.getMessage());
                stringRedisTemplate.delete(cacheKey);
            }
        }

        // 缓存不存在，需要下载
        if (url == null || url.isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "无缓存且未提供url，无法获取PDF");
        }
        
        // 下载并缓存（使用业务文件id）
        PdfCacheResult result = downloadAndCache(url, fileId);
        
        // 从缓存获取文件路径
        String resultCachedInfoJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (resultCachedInfoJson == null || resultCachedInfoJson.isEmpty()) {
            throw BusinessException.of(ResultCode.SERVER_ERROR, "下载成功但缓存信息缺失");
        }
        
        try {
            CacheInfo cacheInfo = objectMapper.readValue(resultCachedInfoJson, CacheInfo.class);
            String filePath = cacheInfo.filePath();
            InputStream stream = MinioUtil.download(minioClient, bucketName, filePath);
            return new PdfStreamResult(stream, result.fileName());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析缓存信息失败: fileId={}, err={}", fileId, e.getMessage(), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "解析缓存信息失败: " + e.getMessage());
        }
    }


    @Override
    public PdfCacheResult downloadAndCache(String pdfUrl, String id) throws BusinessException {
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "PDF URL不能为空");
        }
        if (id == null || id.isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "文献ID不能为空");
        }
        
        // 统一使用业务文件id作为缓存key（不使用MD5）
        String redisKey = CacheConstant.PDF_CACHE_PREFIX + id;
        
        // 先查询Redis是否有缓存
        String cachedInfoJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (cachedInfoJson != null && !cachedInfoJson.isEmpty()) {
            try {
                CacheInfo cacheInfo = objectMapper.readValue(cachedInfoJson, CacheInfo.class);
                // 检查MinIO中的文件是否存在
                try {
                    MinioUtil.statObject(minioClient, bucketName, cacheInfo.filePath());
                    
                    // 重新生成presignedUrl（使用FileService的方法）
                    // 如果fileUrl存在则使用，否则构建fileUrl
                    String fileUrl = cacheInfo.fileUrl() != null && !cacheInfo.fileUrl().isEmpty() 
                            ? cacheInfo.fileUrl() 
                            : bucketName + "/" + cacheInfo.filePath();
                    String presignedUrl = fileService.generatePresignedUrlFromFileUrl(
                            fileUrl, 
                            CacheConstant.PDF_PREVIEW_EXPIRE_SECONDS);
                    if (presignedUrl == null) {
                        throw BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "生成预览URL失败");
                    }
                    
                    log.info("从缓存获取PDF: id={}, filePath={}", id, cacheInfo.filePath());
                    
                    // 刷新过期时间
                    stringRedisTemplate.opsForValue().set(
                            redisKey,
                            cachedInfoJson,
                            Duration.ofSeconds(CacheConstant.PDF_CACHE_EXPIRE));
                    
                    // 返回的id就是传入的业务文件id
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

        // 下载PDF文件并缓存（使用业务文件id）
        PdfCacheResult result = downloadAndSave(pdfUrl, id);
        
        // 返回的id就是传入的业务文件id
        return result;
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
            
            // 重新生成presignedUrl（使用FileService的方法）
            try {
                // 如果fileUrl存在则使用，否则构建fileUrl
                String fileUrl = cacheInfo.fileUrl() != null && !cacheInfo.fileUrl().isEmpty() 
                        ? cacheInfo.fileUrl() 
                        : bucketName + "/" + cacheInfo.filePath();
                String presignedUrl = fileService.generatePresignedUrlFromFileUrl(
                        fileUrl, 
                        CacheConstant.PDF_PREVIEW_EXPIRE_SECONDS);
                if (presignedUrl == null) {
                    throw BusinessException.of(ResultCode.FILE_DOWNLOAD_FAILED, "生成预览URL失败");
                }
                return presignedUrl;
            } catch (BusinessException e) {
                throw e;
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
                // 超时已通过 HttpClient 配置：连接超时20秒，读取超时60秒
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

                        // 使用FileService.upload方法上传文件
                        return Mono.fromCallable(() -> {
                            // 创建MultipartFile对象
                            MultipartFile multipartFile = new ByteArrayMultipartFile(safeFileName, fileBytes, "application/pdf");
                            
                            // 创建上传请求
                            FileUploadReq uploadReq = new FileUploadReq();
                            uploadReq.setFile(multipartFile);
                            uploadReq.setIsPublic(0); // 私有文件
                            
                            // 使用FileService.upload方法上传（传入系统用户信息）
                            FileInfoResp fileInfo = fileService.upload(uploadReq, 1L, "系统");
                            
                            // 从返回结果获取文件路径（MinIO objectName）
                            // fileInfo.filePath() 直接返回文件路径
                            String filePath = fileInfo.filePath();
                            
                            return new FileUploadResult(filePath, fileInfo.fileUrl());
                        })
                        .flatMap(uploadResult -> {
                            // 保存缓存信息到Redis（包含filePath和fileUrl）
                            CacheInfo cacheInfo = new CacheInfo(
                                    uploadResult.filePath,
                                    uploadResult.fileUrl,
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

                                // 生成presignedUrl（使用FileService的方法）
                                String presignedUrl = fileService.generatePresignedUrlFromFileUrl(
                                        uploadResult.fileUrl, 
                                        CacheConstant.PDF_PREVIEW_EXPIRE_SECONDS);

                                log.info("PDF下载并缓存成功: id={}, filePath={}, fileName={}, size={} bytes", 
                                        id, uploadResult.filePath, fileName, fileBytes.length);
                                
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

    /**
     * 文件上传结果（用于在Mono链中传递）
     */
    private record FileUploadResult(String filePath, String fileUrl) {
    }
    /**
     * 字节数组 MultipartFile 实现（用于从HTTP下载的文件）
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String fileName;
        private final byte[] content;
        private final String contentType;

        public ByteArrayMultipartFile(String fileName, byte[] content, String contentType) {
            this.fileName = fileName;
            this.content = content;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content != null ? content.length : 0;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public org.springframework.core.io.Resource getResource() {
            return new org.springframework.core.io.ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
        }

        @Override
        public void transferTo(java.io.File dest) {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }
}