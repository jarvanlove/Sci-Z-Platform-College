package com.sciz.server.application.service.file.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.file.FileConvertService;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.FileUtil;
import com.sciz.server.infrastructure.shared.utils.MinioUtil;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * 文件格式转换服务实现类
 *
 * @author JiaWen.Wu
 * @className FileConvertServiceImpl
 * @date 2025-01-28 16:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileConvertServiceImpl implements FileConvertService {

    /**
     * 并发控制：最多同时转换 3 个文件
     */
    private static final Semaphore CONVERT_SEMAPHORE = new Semaphore(3);

    /**
     * Redis 模板（用于缓存转换结果）
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * MinIO 客户端
     */
    private final MinioClient minioClient;

    /**
     * MinIO 存储桶名称
     */
    @Value("${minio.bucket:sciz-files}")
    private String bucketName;

    /**
     * 存储桶初始化标记
     */
    private final AtomicBoolean bucketInitialized = new AtomicBoolean(false);

    /**
     * JSON 序列化器
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 缓存文件信息（用于序列化到 Redis）
     */
    private record CachedFileInfo(
            String objectName, // MinIO 对象路径
            String fileName, // 文件名
            String mimeType, // MIME 类型
            Long contentLength) { // 文件大小
    }

    /**
     * 转换文件格式
     *
     * @param sourceInputStream InputStream 源文件输入流
     * @param sourceFormat      String 源文件格式（docx/pdf）
     * @param targetFormat      String 目标文件格式（docx/pdf）
     * @param originalFileName  String 原始文件名（用于生成转换后的文件名）
     * @return ConvertResult 转换结果
     */
    @Override
    public ConvertResult convert(InputStream sourceInputStream, String sourceFormat, String targetFormat,
            String originalFileName) {
        log.info(String.format("开始文件格式转换: sourceFormat=%s, targetFormat=%s, originalFileName=%s",
                sourceFormat, targetFormat, originalFileName));

        // 1. 校验转换支持
        if (!isSupported(sourceFormat, targetFormat)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST,
                    "不支持的文件格式转换: %s → %s", sourceFormat, targetFormat);
        }

        // 2. 如果源格式和目标格式相同，直接返回原文件
        if (sourceFormat.equalsIgnoreCase(targetFormat)) {
            log.info("源格式和目标格式相同，无需转换");
            return convertSameFormat(sourceInputStream, originalFileName, sourceFormat);
        }

        // 3. 生成缓存 key（基于文件内容和转换参数）
        var cacheKey = generateCacheKey(sourceInputStream, sourceFormat, targetFormat, originalFileName);

        // 4. 检查缓存
        var cachedResult = getCachedResult(cacheKey);
        if (cachedResult != null) {
            log.info(String.format("从缓存获取转换结果: cacheKey=%s", cacheKey));
            return cachedResult;
        }

        // 5. 获取转换许可（并发控制）
        try {
            CONVERT_SEMAPHORE.acquire();
            log.info(String.format("获取转换许可，开始转换: sourceFormat=%s, targetFormat=%s", sourceFormat, targetFormat));

            // 6. 执行格式转换
            var result = switch (sourceFormat.toLowerCase()) {
                case "docx" -> convertDocxToPdf(sourceInputStream, originalFileName);
                case "pdf" -> convertPdfToDocx(sourceInputStream, originalFileName);
                default -> throw BusinessException.of(ResultCode.BAD_REQUEST,
                        "不支持的源文件格式: %s", sourceFormat);
            };

            // 7. 缓存转换结果
            cacheResult(cacheKey, result);

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.of(ResultCode.SERVER_ERROR, "转换被中断: %s", e.getMessage());
        } finally {
            CONVERT_SEMAPHORE.release();
            log.info("释放转换许可");
        }
    }

    /**
     * 检查是否支持转换
     *
     * @param sourceFormat String 源文件格式
     * @param targetFormat String 目标文件格式
     * @return boolean 是否支持
     */
    @Override
    public boolean isSupported(String sourceFormat, String targetFormat) {
        if (sourceFormat == null || targetFormat == null) {
            return false;
        }

        var source = sourceFormat.toLowerCase();
        var target = targetFormat.toLowerCase();

        // 支持 docx ↔ pdf 转换
        return (source.equals("docx") && target.equals("pdf")) ||
                (source.equals("pdf") && target.equals("docx")) ||
                source.equals(target); // 相同格式也支持（直接返回）
    }

    /**
     * 相同格式转换（直接返回原文件）
     * 使用临时文件避免大文件内存占用
     */
    private ConvertResult convertSameFormat(InputStream sourceInputStream, String originalFileName, String format) {
        final Path[] tempFileRef = new Path[1];
        try {
            // 创建临时文件
            tempFileRef[0] = Files.createTempFile("convert_", "." + format);
            final Path tempFile = tempFileRef[0];

            // 流式复制到临时文件
            Files.copy(sourceInputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            var fileSize = Files.size(tempFile);
            var mimeType = getMimeType(format);
            var fileName = changeFileExtension(originalFileName, format);

            // 检查文件大小
            if (fileSize > CacheConstant.FILE_CONVERT_MAX_SIZE) {
                Files.deleteIfExists(tempFile);
                throw BusinessException.of(ResultCode.BAD_REQUEST,
                        "文件过大，无法转换（最大支持 %dMB，当前文件约 %dMB）",
                        CacheConstant.FILE_CONVERT_MAX_SIZE / 1024 / 1024, fileSize / 1024 / 1024);
            }

            // 返回文件输入流（使用临时文件）
            var fileInputStream = new FileInputStream(tempFile.toFile()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    // 关闭时删除临时文件
                    Files.deleteIfExists(tempFile);
                }
            };

            return new ConvertResult(
                    fileInputStream,
                    fileName,
                    mimeType,
                    fileSize);

        } catch (IOException e) {
            if (tempFileRef[0] != null) {
                try {
                    Files.deleteIfExists(tempFileRef[0]);
                } catch (IOException ignored) {
                }
            }
            log.error(String.format("读取文件流失败: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "文件读取失败: %s", e.getMessage());
        }
    }

    /**
     * DOCX 转 PDF
     * 使用临时文件优化大文件处理
     */
    private ConvertResult convertDocxToPdf(InputStream docxInputStream, String originalFileName) {
        final Path[] tempDocxFileRef = new Path[1];
        final Path[] tempPdfFileRef = new Path[1];
        XWPFDocument document = null;
        final PDDocument[] pdfDocumentRef = new PDDocument[1];

        try {
            log.info("开始 DOCX → PDF 转换");

            // 1. 将输入流保存到临时文件（避免内存占用）
            tempDocxFileRef[0] = Files.createTempFile("docx_", ".docx");
            final Path tempDocxFile = tempDocxFileRef[0];
            Files.copy(docxInputStream, tempDocxFile, StandardCopyOption.REPLACE_EXISTING);

            var fileSize = Files.size(tempDocxFile);
            if (fileSize > CacheConstant.FILE_CONVERT_MAX_SIZE) {
                throw BusinessException.of(ResultCode.BAD_REQUEST,
                        "文件过大，无法转换（最大支持 %dMB，当前文件约 %dMB）",
                        CacheConstant.FILE_CONVERT_MAX_SIZE / 1024 / 1024, fileSize / 1024 / 1024);
            }

            // 2. 从临时文件读取 DOCX 文档
            try (var fileInputStream = Files.newInputStream(tempDocxFile)) {
                document = new XWPFDocument(fileInputStream);
            }

            // 3. 提取文本内容（流式处理，避免一次性加载所有内容）
            var textContent = extractTextFromDocx(document);
            document.close();
            document = null;

            // 4. 创建 PDF 文档（使用临时文件）
            tempPdfFileRef[0] = Files.createTempFile("pdf_", ".pdf");
            final Path tempPdfFile = tempPdfFileRef[0];
            pdfDocumentRef[0] = new PDDocument();
            var pageRef = new PDPage[] { new PDPage() };
            pdfDocumentRef[0].addPage(pageRef[0]);
            var contentStreamRef = new PDPageContentStream[] { new PDPageContentStream(pdfDocumentRef[0], pageRef[0]) };

            // 5. 设置字体和写入文本
            contentStreamRef[0].setFont(PDType1Font.HELVETICA, 12f);
            contentStreamRef[0].beginText();
            contentStreamRef[0].newLineAtOffset(50, 750);

            // 6. 写入文本（优化：使用 Stream API 处理，避免传统循环）
            var lines = Arrays.stream(textContent.split("\n"));
            var yPositionRef = new float[] { 750f };
            var lineHeight = 15f;
            var maxLineWidth = 100; // 每行最大字符数

            // 使用 Stream API 处理每一行
            lines.forEach(line -> {
                // 检查是否需要创建新页面
                if (yPositionRef[0] < 50) {
                    try {
                        contentStreamRef[0].endText();
                        contentStreamRef[0].close();
                        pageRef[0] = new PDPage();
                        pdfDocumentRef[0].addPage(pageRef[0]);
                        contentStreamRef[0] = new PDPageContentStream(pdfDocumentRef[0], pageRef[0]);
                        contentStreamRef[0].setFont(PDType1Font.HELVETICA, 12f);
                        contentStreamRef[0].beginText();
                        yPositionRef[0] = 750f;
                    } catch (IOException e) {
                        log.error("创建新页面失败", e);
                        throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "创建新页面失败: %s", e.getMessage());
                    }
                }

                // 处理长行（自动换行）
                if (line.length() > maxLineWidth) {
                    var chunks = splitLongLine(line, maxLineWidth);
                    // 使用 Stream API 处理每个 chunk
                    Arrays.stream(chunks).forEach(chunk -> {
                        try {
                            contentStreamRef[0].newLineAtOffset(0, -lineHeight);
                            contentStreamRef[0].showText(chunk);
                            yPositionRef[0] -= lineHeight;

                            // 检查是否需要创建新页面
                            if (yPositionRef[0] < 50) {
                                try {
                                    contentStreamRef[0].endText();
                                    contentStreamRef[0].close();
                                    pageRef[0] = new PDPage();
                                    pdfDocumentRef[0].addPage(pageRef[0]);
                                    contentStreamRef[0] = new PDPageContentStream(pdfDocumentRef[0], pageRef[0]);
                                    contentStreamRef[0].setFont(PDType1Font.HELVETICA, 12f);
                                    contentStreamRef[0].beginText();
                                    yPositionRef[0] = 750f;
                                } catch (IOException e) {
                                    log.error("创建新页面失败", e);
                                    throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "创建新页面失败: %s",
                                            e.getMessage());
                                }
                            }
                        } catch (IOException e) {
                            log.error("写入文本失败", e);
                            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "写入文本失败: %s", e.getMessage());
                        }
                    });
                } else {
                    try {
                        contentStreamRef[0].newLineAtOffset(0, -lineHeight);
                        contentStreamRef[0].showText(line);
                        yPositionRef[0] -= lineHeight;
                    } catch (IOException e) {
                        log.error("写入文本失败", e);
                        throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "写入文本失败: %s", e.getMessage());
                    }
                }
            });

            contentStreamRef[0].endText();
            contentStreamRef[0].close();

            // 7. 保存 PDF 到临时文件
            pdfDocumentRef[0].save(tempPdfFile.toFile());
            pdfDocumentRef[0].close();
            pdfDocumentRef[0] = null;

            var pdfFileSize = Files.size(tempPdfFile);
            var fileName = changeFileExtension(originalFileName, "pdf");
            var mimeType = "application/pdf";

            log.info(String.format("DOCX → PDF 转换成功: fileName=%s, size=%d", fileName, pdfFileSize));

            // 8. 返回文件输入流（使用临时文件，关闭时自动删除）
            var fileInputStream = new FileInputStream(tempPdfFile.toFile()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    // 清理临时文件
                    Files.deleteIfExists(tempPdfFile);
                    Files.deleteIfExists(tempDocxFile);
                }
            };

            return new ConvertResult(
                    fileInputStream,
                    fileName,
                    mimeType,
                    pdfFileSize);

        } catch (Exception e) {
            // 清理资源
            if (document != null) {
                try {
                    document.close();
                } catch (IOException ignored) {
                }
            }
            if (pdfDocumentRef[0] != null) {
                try {
                    pdfDocumentRef[0].close();
                } catch (IOException ignored) {
                }
            }
            try {
                if (tempPdfFileRef[0] != null)
                    Files.deleteIfExists(tempPdfFileRef[0]);
                if (tempDocxFileRef[0] != null)
                    Files.deleteIfExists(tempDocxFileRef[0]);
            } catch (IOException ignored) {
            }

            log.error(String.format("DOCX → PDF 转换失败: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "文件格式转换失败: %s", e.getMessage());
        }
    }

    /**
     * PDF 转 DOCX
     * 使用临时文件优化大文件处理
     */
    private ConvertResult convertPdfToDocx(InputStream pdfInputStream, String originalFileName) {
        final Path[] tempPdfFileRef = new Path[1];
        final Path[] tempDocxFileRef = new Path[1];
        final PDDocument[] pdfDocumentRef = new PDDocument[1];
        final XWPFDocument[] docxDocumentRef = new XWPFDocument[1];

        try {
            log.info("开始 PDF → DOCX 转换");

            // 1. 将输入流保存到临时文件（避免内存占用）
            tempPdfFileRef[0] = Files.createTempFile("pdf_", ".pdf");
            final Path tempPdfFile = tempPdfFileRef[0];
            Files.copy(pdfInputStream, tempPdfFile, StandardCopyOption.REPLACE_EXISTING);

            var fileSize = Files.size(tempPdfFile);
            if (fileSize > CacheConstant.FILE_CONVERT_MAX_SIZE) {
                throw BusinessException.of(ResultCode.BAD_REQUEST,
                        "文件过大，无法转换（最大支持 %dMB，当前文件约 %dMB）",
                        CacheConstant.FILE_CONVERT_MAX_SIZE / 1024 / 1024, fileSize / 1024 / 1024);
            }

            // 2. 从临时文件加载 PDF 文档
            try (var fileInputStream = Files.newInputStream(tempPdfFile)) {
                pdfDocumentRef[0] = PDDocument.load(fileInputStream);
            }

            // 3. 提取文本内容（优化：分批处理大文件）
            var textStripper = new PDFTextStripper();
            // 设置提取策略：保留段落结构
            textStripper.setParagraphStart("\n");
            textStripper.setParagraphEnd("\n");
            textStripper.setPageStart("\n");
            textStripper.setPageEnd("\n");

            var textContent = textStripper.getText(pdfDocumentRef[0]);
            pdfDocumentRef[0].close();
            pdfDocumentRef[0] = null;

            // 4. 创建 DOCX 文档（使用临时文件）
            tempDocxFileRef[0] = Files.createTempFile("docx_", ".docx");
            final Path tempDocxFile = tempDocxFileRef[0];
            docxDocumentRef[0] = new XWPFDocument();

            // 5. 将文本内容写入 DOCX（优化：使用 Stream API 处理，保留段落结构）
            var maxParagraphs = 10000; // 限制段落数量，避免内存溢出
            var paragraphCountRef = new int[] { 0 };

            // 使用 Stream API 处理每一行
            Arrays.stream(textContent.split("\n"))
                    .takeWhile(line -> paragraphCountRef[0] < maxParagraphs) // Java 21: takeWhile 限制处理数量
                    .forEach(line -> {
                        if (paragraphCountRef[0] >= maxParagraphs) {
                            log.warn(String.format("段落数量超过限制（%d），截断处理", maxParagraphs));
                            return;
                        }

                        var trimmedLine = line.trim();
                        if (trimmedLine.isEmpty()) {
                            // 空行：创建空段落
                            docxDocumentRef[0].createParagraph();
                            paragraphCountRef[0]++;
                        } else {
                            // 非空行：创建段落并写入文本
                            var paragraph = docxDocumentRef[0].createParagraph();
                            var run = paragraph.createRun();
                            run.setText(trimmedLine);
                            paragraphCountRef[0]++;
                        }
                    });

            // 6. 保存 DOCX 到临时文件
            try (var fileOutputStream = Files.newOutputStream(tempDocxFile)) {
                docxDocumentRef[0].write(fileOutputStream);
            }
            docxDocumentRef[0].close();
            docxDocumentRef[0] = null;

            var docxFileSize = Files.size(tempDocxFile);
            var fileName = changeFileExtension(originalFileName, "docx");
            var mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            log.info(String.format("PDF → DOCX 转换成功: fileName=%s, size=%d, paragraphs=%d",
                    fileName, docxFileSize, paragraphCountRef[0]));

            // 7. 返回文件输入流（使用临时文件，关闭时自动删除）
            var fileInputStream = new FileInputStream(tempDocxFile.toFile()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    // 清理临时文件
                    Files.deleteIfExists(tempDocxFile);
                    Files.deleteIfExists(tempPdfFile);
                }
            };

            return new ConvertResult(
                    fileInputStream,
                    fileName,
                    mimeType,
                    docxFileSize);

        } catch (Exception e) {
            // 清理资源
            if (pdfDocumentRef[0] != null) {
                try {
                    pdfDocumentRef[0].close();
                } catch (IOException ignored) {
                }
            }
            if (docxDocumentRef[0] != null) {
                try {
                    docxDocumentRef[0].close();
                } catch (IOException ignored) {
                }
            }
            try {
                if (tempDocxFileRef[0] != null)
                    Files.deleteIfExists(tempDocxFileRef[0]);
                if (tempPdfFileRef[0] != null)
                    Files.deleteIfExists(tempPdfFileRef[0]);
            } catch (IOException ignored) {
            }

            log.error(String.format("PDF → DOCX 转换失败: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "文件格式转换失败: %s", e.getMessage());
        }
    }

    /**
     * 从 DOCX 文档中提取文本
     * 优化：使用 Stream API 流式处理，避免一次性加载所有内容
     */
    private String extractTextFromDocx(XWPFDocument document) {
        return document.getParagraphs().stream()
                .map(paragraph -> paragraph.getRuns().stream()
                        .map(run -> run.getText(0))
                        .filter(text -> text != null && !text.isEmpty())
                        .collect(java.util.stream.Collectors.joining()))
                .filter(text -> !text.isEmpty())
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    /**
     * 分割长行（自动换行）
     * 使用 Stream API 优化
     */
    private String[] splitLongLine(String line, int maxWidth) {
        if (line.length() <= maxWidth) {
            return new String[] { line };
        }

        // 使用 Stream API 生成 chunks
        return Stream.iterate(0, start -> start < line.length(), start -> start + maxWidth)
                .map(start -> {
                    var end = Math.min(start + maxWidth, line.length());
                    return line.substring(start, end);
                })
                .toArray(String[]::new);
    }

    /**
     * 生成缓存 key
     * 基于文件内容 MD5 + 转换参数
     * 
     * 注意：此方法会读取输入流，如果流不支持 mark/reset，需要在调用前处理
     */
    private String generateCacheKey(InputStream sourceInputStream, String sourceFormat,
            String targetFormat, String originalFileName) {
        try {
            // 如果流支持 mark，先标记位置
            if (sourceInputStream.markSupported()) {
                sourceInputStream.mark(Integer.MAX_VALUE);
            }

            // 计算文件内容的 MD5（用于缓存 key）
            var md5 = DigestUtils.md5DigestAsHex(sourceInputStream);

            // 重置流（如果支持）
            if (sourceInputStream.markSupported()) {
                sourceInputStream.reset();
            } else {
                // 如果流不支持 reset，记录警告
                log.warn("输入流不支持 reset，缓存 key 生成后流可能无法重用");
            }

            return String.format("%s%s:%s:%s:%s",
                    CacheConstant.FILE_CONVERT_CACHE_PREFIX, md5, sourceFormat, targetFormat, originalFileName);
        } catch (IOException e) {
            log.warn("无法生成缓存 key，使用文件名: {}", e.getMessage());
            // 降级方案：使用文件名和格式
            return String.format("%s%s:%s:%s",
                    CacheConstant.FILE_CONVERT_CACHE_PREFIX, originalFileName, sourceFormat, targetFormat);
        }
    }

    /**
     * 从缓存获取转换结果
     * 从 Redis 读取文件信息，然后从 MinIO 下载文件
     */
    private ConvertResult getCachedResult(String cacheKey) {
        try {
            // 1. 从 Redis 获取缓存的文件信息
            var cachedInfoJson = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedInfoJson == null || cachedInfoJson.isEmpty()) {
                return null;
            }

            // 2. 反序列化文件信息
            var cachedInfo = objectMapper.readValue(cachedInfoJson, CachedFileInfo.class);

            // 3. 确保存储桶存在
            ensureBucket();

            // 4. 从 MinIO 下载文件
            var getObjectResponse = MinioUtil.download(minioClient, bucketName, cachedInfo.objectName());

            // 5. 创建 ConvertResult（使用 MinIO 响应流）
            // 注意：GetObjectResponse 实现了 InputStream，可以直接使用
            return new ConvertResult(
                    getObjectResponse,
                    cachedInfo.fileName(),
                    cachedInfo.mimeType(),
                    cachedInfo.contentLength());

        } catch (Exception e) {
            log.warn(String.format("从缓存获取转换结果失败: cacheKey=%s, err=%s", cacheKey, e.getMessage()));
            return null;
        }
    }

    /**
     * 缓存转换结果
     * 将转换结果保存到 MinIO，然后在 Redis 中缓存文件信息
     */
    private void cacheResult(String cacheKey, ConvertResult result) {
        Path tempFile = null;
        try {
            // 1. 确保存储桶存在
            ensureBucket();

            // 2. 生成 MinIO 对象路径（使用缓存 key 的哈希值，避免路径过长）
            var objectNameHash = DigestUtils.md5DigestAsHex(cacheKey.getBytes());
            var fileExtension = FileUtil.getFileExtension(result.fileName());
            var objectName = String.format("%s%s/%s.%s",
                    CacheConstant.FILE_CONVERT_CACHE_DIR,
                    objectNameHash.substring(0, 2), // 使用前2位作为子目录
                    objectNameHash,
                    fileExtension);

            // 3. 将 InputStream 保存到临时文件（因为 MinIO 上传需要知道文件大小）
            tempFile = Files.createTempFile("convert_cache_", "." + fileExtension);
            try (var inputStream = result.inputStream();
                    var outputStream = Files.newOutputStream(tempFile)) {
                inputStream.transferTo(outputStream);
            }

            var fileSize = Files.size(tempFile);

            // 4. 上传到 MinIO
            try (var fileInputStream = Files.newInputStream(tempFile)) {
                MinioUtil.upload(minioClient, bucketName, objectName,
                        fileInputStream, fileSize, result.mimeType());
            }

            // 5. 构建缓存信息
            var cachedInfo = new CachedFileInfo(
                    objectName,
                    result.fileName(),
                    result.mimeType(),
                    result.contentLength());

            // 6. 序列化并保存到 Redis
            var cachedInfoJson = objectMapper.writeValueAsString(cachedInfo);
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    cachedInfoJson,
                    Duration.ofSeconds(CacheConstant.FILE_CONVERT_CACHE_EXPIRE));

            log.info(String.format("转换结果已缓存: cacheKey=%s, objectName=%s, size=%d",
                    cacheKey, objectName, fileSize));

        } catch (Exception e) {
            log.warn(String.format("缓存转换结果失败: cacheKey=%s, err=%s", cacheKey, e.getMessage()));
            // 缓存失败不影响主流程，只记录警告
        } finally {
            // 清理临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucket() {
        if (!bucketInitialized.compareAndSet(false, true)) {
            return;
        }
        try {
            MinioUtil.makeBucketIfAbsent(minioClient, bucketName);
        } catch (Exception e) {
            bucketInitialized.set(false);
            log.error(String.format("初始化 MinIO 桶失败: bucket=%s", bucketName), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR,
                    "初始化存储桶失败: %s", e.getMessage());
        }
    }

    /**
     * 修改文件扩展名
     */
    private String changeFileExtension(String fileName, String newExtension) {
        var baseName = FileUtil.getFileNameWithoutExtension(fileName);
        return baseName + "." + newExtension;
    }

    /**
     * 根据格式获取 MIME 类型
     */
    private String getMimeType(String format) {
        return switch (format.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }
}
