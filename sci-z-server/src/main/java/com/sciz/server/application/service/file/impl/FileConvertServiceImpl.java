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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
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
     * PDF 页面边距（单位：点）
     */
    private static final float PDF_MARGIN = 50f;

    /**
     * PDF 行高（单位：点）
     */
    private static final float PDF_LINE_HEIGHT = 16f;

    /**
     * PDF 字体大小
     */
    private static final float PDF_FONT_SIZE = 12f;

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
            String objectName,
            String fileName,
            String mimeType,
            Long contentLength) {
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

        if (!isSupported(sourceFormat, targetFormat)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST,
                    "不支持的文件格式转换: %s → %s", sourceFormat, targetFormat);
        }

        if (sourceFormat.equalsIgnoreCase(targetFormat)) {
            log.info("源格式和目标格式相同，无需转换");
            return convertSameFormat(sourceInputStream, originalFileName, sourceFormat);
        }

        // 保存源文件到临时文件
        var tempSourceFile = saveToTempFile(sourceInputStream, sourceFormat);

        try {
            // 生成缓存 key
            var cacheKey = generateCacheKey(tempSourceFile, sourceFormat, targetFormat, originalFileName);

            // 检查缓存
            var cachedResult = getCachedResult(cacheKey);
            if (cachedResult != null) {
                cleanupTempFile(tempSourceFile);
                log.info(String.format("从缓存获取转换结果: cacheKey=%s", cacheKey));
                return cachedResult;
            }

            // 执行转换
            return executeConversion(tempSourceFile, sourceFormat, targetFormat, originalFileName, cacheKey);

        } catch (Exception e) {
            cleanupTempFile(tempSourceFile);
            log.error(String.format("文件格式转换失败: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "文件格式转换失败: %s", e.getMessage());
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
        return (source.equals("docx") && target.equals("pdf")) ||
                (source.equals("pdf") && target.equals("docx")) ||
                source.equals(target);
    }

    /**
     * 相同格式转换（直接返回原文件）
     */
    private ConvertResult convertSameFormat(InputStream sourceInputStream, String originalFileName, String format) {
        var tempFile = saveToTempFile(sourceInputStream, format);
        var fileSize = getFileSize(tempFile);
        validateFileSize(fileSize);

        return new ConvertResult(
                createAutoCloseInputStream(tempFile),
                changeFileExtension(originalFileName, format),
                getMimeType(format),
                fileSize);
    }

    /**
     * DOCX 转 PDF
     */
    private ConvertResult convertDocxToPdf(Path docxFile, String originalFileName) {
        try (var docxStream = Files.newInputStream(docxFile);
                var document = new XWPFDocument(docxStream)) {

            var pdfFile = Files.createTempFile("pdf_", ".pdf");
            var pdfDoc = new PDDocument();

            try {
                var font = loadSystemFont(pdfDoc);
                var paragraphs = document.getParagraphs();

                if (paragraphs.isEmpty()) {
                    createEmptyPdf(pdfDoc, pdfFile);
                } else {
                    createPdfFromParagraphs(pdfDoc, pdfFile, font, paragraphs);
                }

                var pdfSize = Files.size(pdfFile);
                log.info(String.format("DOCX → PDF 转换成功: size=%d", pdfSize));

                return new ConvertResult(
                        createAutoCloseInputStream(pdfFile, docxFile),
                        changeFileExtension(originalFileName, "pdf"),
                        "application/pdf",
                        pdfSize);

            } catch (Exception e) {
                pdfDoc.close();
                cleanupTempFile(pdfFile);
                throw e;
            }
        } catch (IOException e) {
            log.error("DOCX → PDF 转换失败", e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "DOCX 转 PDF 失败: %s", e.getMessage());
        }
    }

    /**
     * PDF 转 DOCX
     */
    private ConvertResult convertPdfToDocx(Path pdfFile, String originalFileName) {
        try (var pdfStream = Files.newInputStream(pdfFile);
                var pdfDoc = PDDocument.load(pdfStream)) {

            var textStripper = new PDFTextStripper();
            textStripper.setParagraphStart("\n");
            textStripper.setParagraphEnd("\n");
            var textContent = textStripper.getText(pdfDoc);

            var docxFile = Files.createTempFile("docx_", ".docx");
            try (var docxDoc = new XWPFDocument()) {
                createDocxFromText(docxDoc, textContent);
                try (var outputStream = Files.newOutputStream(docxFile)) {
                    docxDoc.write(outputStream);
                }
            }

            var docxSize = Files.size(docxFile);
            log.info(String.format("PDF → DOCX 转换成功: size=%d", docxSize));

            return new ConvertResult(
                    createAutoCloseInputStream(docxFile, pdfFile),
                    changeFileExtension(originalFileName, "docx"),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    docxSize);

        } catch (IOException e) {
            log.error("PDF → DOCX 转换失败", e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "PDF 转 DOCX 失败: %s", e.getMessage());
        }
    }

    /**
     * 从段落创建 PDF
     */
    private void createPdfFromParagraphs(PDDocument pdfDoc, Path pdfFile, PDFont font, List<XWPFParagraph> paragraphs)
            throws IOException {
        var page = new PDPage(PDRectangle.A4);
        pdfDoc.addPage(page);
        var pageHeight = page.getMediaBox().getHeight();
        var maxWidth = page.getMediaBox().getWidth() - 2 * PDF_MARGIN;

        var context = new PdfContext(pdfDoc, page, font, pageHeight, maxWidth);

        try {
            context.startPage();

            paragraphs.stream()
                    .map(this::extractParagraphText)
                    .filter(text -> text != null && !text.isBlank())
                    .flatMap(text -> {
                        try {
                            return wrapText(text, font, PDF_FONT_SIZE, maxWidth).stream();
                        } catch (IOException e) {
                            log.error("文本换行失败", e);
                            return Stream.empty();
                        }
                    })
                    .forEach(context::writeLine);

            context.endPage();
        } finally {
            context.close();
        }

        pdfDoc.save(pdfFile.toFile());
    }

    /**
     * PDF 写入上下文（管理页面和内容流）
     */
    private static class PdfContext {
        private final PDDocument pdfDoc;
        private final PDFont font;
        private final float pageHeight;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float yPosition;

        PdfContext(PDDocument pdfDoc, PDPage page, PDFont font, float pageHeight, float maxWidth) {
            this.pdfDoc = pdfDoc;
            this.page = page;
            this.font = font;
            this.pageHeight = pageHeight;
            this.yPosition = pageHeight - PDF_MARGIN;
        }

        void startPage() throws IOException {
            this.contentStream = new PDPageContentStream(pdfDoc, page);
            contentStream.setFont(font, PDF_FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(PDF_MARGIN, yPosition);
        }

        void writeLine(String line) {
            try {
                if (yPosition < PDF_MARGIN) {
                    newPage();
                }
                contentStream.newLineAtOffset(0, -PDF_LINE_HEIGHT);
                contentStream.showText(line);
                yPosition -= PDF_LINE_HEIGHT;
            } catch (IOException e) {
                log.error("写入PDF行失败", e);
                throw new RuntimeException(e);
            }
        }

        void newPage() throws IOException {
            contentStream.endText();
            contentStream.close();
            page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);
            yPosition = pageHeight - PDF_MARGIN;
            startPage();
        }

        void endPage() throws IOException {
            if (contentStream != null) {
                contentStream.endText();
            }
        }

        void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }

    /**
     * 创建空 PDF
     */
    private void createEmptyPdf(PDDocument pdfDoc, Path pdfFile) throws IOException {
        var page = new PDPage(PDRectangle.A4);
        pdfDoc.addPage(page);
        pdfDoc.save(pdfFile.toFile());
    }

    /**
     * 从文本创建 DOCX
     */
    private void createDocxFromText(XWPFDocument docxDoc, String textContent) {
        Arrays.stream(textContent.split("\n"))
                .map(String::trim)
                .forEach(line -> {
                    var paragraph = docxDoc.createParagraph();
                    if (!line.isEmpty()) {
                        paragraph.createRun().setText(line);
                    }
                });
    }

    /**
     * 提取段落文本
     */
    private String extractParagraphText(XWPFParagraph paragraph) {
        return paragraph.getRuns().stream()
                .map(run -> run.getText(0))
                .filter(text -> text != null && !text.isEmpty())
                .collect(java.util.stream.Collectors.joining());
    }

    /**
     * 文本换行处理（支持中英文混合）
     * 确保文本不会超出右边距
     */
    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        // 文本换行状态
        record WrapState(List<String> lines, StringBuilder currentLine) {
        }

        // 使用 Stream API 处理每个字符
        var result = IntStream.range(0, text.length())
                .mapToObj(text::charAt)
                .reduce(
                        new WrapState(new java.util.ArrayList<>(), new StringBuilder()),
                        (state, ch) -> {
                            try {
                                var testLine = state.currentLine().toString() + ch;
                                var width = font.getStringWidth(testLine) / 1000 * fontSize;

                                // 如果添加当前字符会超出边界，且当前行不为空，则换行
                                if (width > maxWidth && !state.currentLine().isEmpty()) {
                                    var newLines = new java.util.ArrayList<>(state.lines());
                                    newLines.add(state.currentLine().toString());
                                    return new WrapState(newLines, new StringBuilder(String.valueOf(ch)));
                                } else {
                                    var newCurrentLine = new StringBuilder(state.currentLine());
                                    newCurrentLine.append(ch);
                                    return new WrapState(state.lines(), newCurrentLine);
                                }
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        },
                        (state1, state2) -> {
                            // 合并器（并行流时使用，但这里不需要并行）
                            throw new UnsupportedOperationException("不支持并行流");
                        });

        // 添加最后一行
        var finalLines = new ArrayList<>(result.lines());
        if (!result.currentLine().isEmpty()) {
            finalLines.add(result.currentLine().toString());
        }

        return finalLines.isEmpty() ? List.of("") : finalLines;
    }

    /**
     * 加载系统字体（支持中文）
     * 使用 Java GraphicsEnvironment 动态获取系统可用字体
     * 遍历所有可用的中文字体，逐个尝试加载，直到成功或全部失败
     */
    private PDFont loadSystemFont(PDDocument document) {
        var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        var availableFonts = ge.getAvailableFontFamilyNames();

        // 优先尝试的中文字体名称（按优先级排序）
        var preferredFontNames = new String[] {
                "SimSun", "宋体", // Windows 宋体
                "SimHei", "黑体", // Windows 黑体
                "Microsoft YaHei", "微软雅黑", // Windows 微软雅黑
                "WenQuanYi Micro Hei", // Linux 文泉驿微米黑
                "WenQuanYi Zen Hei", // Linux 文泉驿正黑
                "Noto Sans CJK SC", // Linux Noto Sans
                "STHeiti", "华文黑体", // macOS 黑体
                "PingFang SC", "苹方" // macOS 苹方
        };

        // 查找所有可用的中文字体（去重）
        var availableChineseFonts = Arrays.stream(preferredFontNames)
                .filter(name -> Arrays.asList(availableFonts).contains(name))
                .distinct()
                .toList();

        if (availableChineseFonts.isEmpty()) {
            log.error("系统中未找到任何可用的中文字体");
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED,
                    "系统缺少中文字体支持，无法转换包含中文的文档。请安装中文字体（如：SimSun、SimHei、Microsoft YaHei 等）");
        }

        // 遍历所有可用的中文字体，逐个尝试加载
        var lastException = new Exception[1];
        for (var fontName : availableChineseFonts) {
            var fontFile = getFontFile(fontName);
            if (fontFile == null) {
                log.debug(String.format("字体文件不存在: %s", fontName));
                continue;
            }

            try (var fontStream = Files.newInputStream(fontFile)) {
                var font = PDType0Font.load(document, fontStream);
                log.info(String.format("成功加载字体: %s (%s)", fontName, fontFile));
                return font;
            } catch (Exception e) {
                lastException[0] = e;
                log.warn(String.format("加载字体文件失败: %s (%s), err=%s", fontName, fontFile, e.getMessage()));
                // 继续尝试下一个字体
            }
        }

        // 所有字体都加载失败，抛出明确的业务异常
        var errorMsg = lastException[0] != null
                ? String.format("所有中文字体加载失败，最后一个错误: %s", lastException[0].getMessage())
                : "所有中文字体加载失败";
        log.error(errorMsg);
        throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED,
                "无法加载中文字体，可能是字体文件损坏或格式不正确。请检查系统字体文件（如：C:/Windows/Fonts/simsun.ttc）");
    }

    /**
     * 获取字体文件路径
     * 根据字体名称查找对应的字体文件
     */
    private Path getFontFile(String fontName) {
        return findFontFileInSystemDirectory(fontName);
    }

    /**
     * 从系统字体目录查找字体文件
     */
    private Path findFontFileInSystemDirectory(String fontFamily) {
        var osName = System.getProperty("os.name").toLowerCase();
        var fontDir = getSystemFontDirectory(osName);

        if (fontDir == null) {
            return null;
        }

        // 字体名称到文件名的映射（支持多个可能的文件名）
        var fontMappings = new java.util.HashMap<String, String[]>();
        fontMappings.put("SimSun", new String[] { "simsun.ttc", "simsun.ttf" });
        fontMappings.put("宋体", new String[] { "simsun.ttc", "simsun.ttf" });
        fontMappings.put("SimHei", new String[] { "simhei.ttf" });
        fontMappings.put("黑体", new String[] { "simhei.ttf" });
        fontMappings.put("Microsoft YaHei", new String[] { "msyh.ttc", "msyh.ttf" });
        fontMappings.put("微软雅黑", new String[] { "msyh.ttc", "msyh.ttf" });
        fontMappings.put("WenQuanYi Micro Hei", new String[] { "wqy-microhei.ttc", "wqy-microhei.ttf" });
        fontMappings.put("WenQuanYi Zen Hei", new String[] { "wqy-zenhei.ttc", "wqy-zenhei.ttf" });
        fontMappings.put("Noto Sans CJK SC", new String[] { "NotoSansCJK-Regular.ttc", "NotoSansCJK-Regular.otf" });
        fontMappings.put("STHeiti", new String[] { "STHeiti Light.ttc", "STHeiti.ttc" });
        fontMappings.put("华文黑体", new String[] { "STHeiti Light.ttc", "STHeiti.ttc" });
        fontMappings.put("PingFang SC", new String[] { "PingFang.ttc" });
        fontMappings.put("苹方", new String[] { "PingFang.ttc" });

        var fileNames = fontMappings.get(fontFamily);
        if (fileNames != null) {
            for (var fileName : fileNames) {
                var fontPath = Path.of(fontDir, fileName);
                if (Files.exists(fontPath) && Files.isReadable(fontPath)) {
                    return fontPath;
                }
            }
        }

        return null;
    }

    /**
     * 获取系统字体目录
     */
    private String getSystemFontDirectory(String osName) {
        if (osName.contains("win")) {
            return "C:/Windows/Fonts";
        } else if (osName.contains("linux")) {
            return "/usr/share/fonts";
        } else if (osName.contains("mac")) {
            return "/System/Library/Fonts";
        }
        return null;
    }

    /**
     * 保存输入流到临时文件
     */
    private Path saveToTempFile(InputStream inputStream, String format) {
        try {
            var tempFile = Files.createTempFile("convert_", "." + format);
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        } catch (IOException e) {
            log.error("保存临时文件失败", e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "保存临时文件失败: %s", e.getMessage());
        }
    }

    /**
     * 获取文件大小
     */
    private long getFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            throw BusinessException.of(ResultCode.SERVER_ERROR, "获取文件大小失败: %s", e.getMessage());
        }
    }

    /**
     * 验证文件大小
     */
    private void validateFileSize(long fileSize) {
        if (fileSize == 0) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "文件为空（0字节），无法转换");
        }
        if (fileSize > CacheConstant.FILE_CONVERT_MAX_SIZE) {
            throw BusinessException.of(ResultCode.BAD_REQUEST,
                    "文件过大，无法转换（最大支持 %dMB，当前文件约 %dMB）",
                    CacheConstant.FILE_CONVERT_MAX_SIZE / 1024 / 1024, fileSize / 1024 / 1024);
        }
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFile(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 创建自动关闭的输入流（关闭时删除临时文件）
     */
    private InputStream createAutoCloseInputStream(Path file) {
        return createAutoCloseInputStream(file, null);
    }

    /**
     * 创建自动关闭的输入流（关闭时删除多个临时文件）
     */
    private InputStream createAutoCloseInputStream(Path file, Path additionalFile) {
        try {
            return new FileInputStream(file.toFile()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    cleanupTempFile(file);
                    cleanupTempFile(additionalFile);
                }
            };
        } catch (FileNotFoundException e) {
            throw BusinessException.of(ResultCode.SERVER_ERROR, "文件不存在: %s", file);
        }
    }

    /**
     * 执行转换
     */
    private ConvertResult executeConversion(Path tempSourceFile, String sourceFormat, String targetFormat,
            String originalFileName, String cacheKey) throws Exception {
        try {
            CONVERT_SEMAPHORE.acquire();
            log.info(String.format("获取转换许可，开始转换: sourceFormat=%s, targetFormat=%s", sourceFormat, targetFormat));

            ConvertResult result;
            try (var fileInputStream = Files.newInputStream(tempSourceFile)) {
                result = switch (sourceFormat.toLowerCase()) {
                    case "docx" -> convertDocxToPdf(tempSourceFile, originalFileName);
                    case "pdf" -> convertPdfToDocx(tempSourceFile, originalFileName);
                    default -> throw BusinessException.of(ResultCode.BAD_REQUEST,
                            "不支持的源文件格式: %s", sourceFormat);
                };
            }

            cacheResult(cacheKey, result);
            cleanupTempFile(tempSourceFile);
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
     * 生成缓存 key
     */
    private String generateCacheKey(Path tempFile, String sourceFormat, String targetFormat, String originalFileName) {
        try (var fileInputStream = Files.newInputStream(tempFile)) {
            var md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            return String.format("%s%s:%s:%s:%s",
                    CacheConstant.FILE_CONVERT_CACHE_PREFIX, md5, sourceFormat, targetFormat, originalFileName);
        } catch (IOException e) {
            log.warn("无法生成缓存 key，使用文件名: {}", e.getMessage());
            return String.format("%s%s:%s:%s",
                    CacheConstant.FILE_CONVERT_CACHE_PREFIX, originalFileName, sourceFormat, targetFormat);
        }
    }

    /**
     * 从缓存获取转换结果
     */
    private ConvertResult getCachedResult(String cacheKey) {
        try {
            var cachedInfoJson = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedInfoJson == null || cachedInfoJson.isEmpty()) {
                return null;
            }

            var cachedInfo = objectMapper.readValue(cachedInfoJson, CachedFileInfo.class);
            ensureBucket();

            var getObjectResponse = MinioUtil.download(minioClient, bucketName, cachedInfo.objectName());
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
     */
    private void cacheResult(String cacheKey, ConvertResult result) {
        Path tempFile = null;
        try {
            ensureBucket();

            var objectNameHash = DigestUtils.md5DigestAsHex(cacheKey.getBytes());
            var fileExtension = FileUtil.getFileExtension(result.fileName());
            var objectName = String.format("%s%s/%s.%s",
                    CacheConstant.FILE_CONVERT_CACHE_DIR,
                    objectNameHash.substring(0, 2),
                    objectNameHash,
                    fileExtension);

            tempFile = Files.createTempFile("convert_cache_", "." + fileExtension);
            try (var inputStream = result.inputStream();
                    var outputStream = Files.newOutputStream(tempFile)) {
                inputStream.transferTo(outputStream);
            }

            var fileSize = Files.size(tempFile);
            try (var fileInputStream = Files.newInputStream(tempFile)) {
                MinioUtil.upload(minioClient, bucketName, objectName,
                        fileInputStream, fileSize, result.mimeType());
            }

            var cachedInfo = new CachedFileInfo(
                    objectName,
                    result.fileName(),
                    result.mimeType(),
                    result.contentLength());

            var cachedInfoJson = objectMapper.writeValueAsString(cachedInfo);
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    cachedInfoJson,
                    Duration.ofSeconds(CacheConstant.FILE_CONVERT_CACHE_EXPIRE));

            log.info(String.format("转换结果已缓存: cacheKey=%s, objectName=%s, size=%d",
                    cacheKey, objectName, fileSize));

        } catch (Exception e) {
            log.warn(String.format("缓存转换结果失败: cacheKey=%s, err=%s", cacheKey, e.getMessage()));
        } finally {
            cleanupTempFile(tempFile);
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
     * 如果原始文件名已经包含目标扩展名，则直接返回，避免重复拼接
     */
    private String changeFileExtension(String fileName, String newExtension) {
        if (fileName == null || fileName.isEmpty()) {
            return "file." + newExtension;
        }

        // 获取原始文件的扩展名
        var originalExtension = FileUtil.getFileExtension(fileName);

        // 如果扩展名已经匹配，直接返回原文件名（避免重复拼接）
        if (newExtension.equalsIgnoreCase(originalExtension)) {
            return fileName;
        }

        // 提取基础名称并拼接新扩展名
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
