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
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.awt.*;
import java.io.*;
import java.io.ByteArrayInputStream;
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
     * 增强容错性：处理各种格式的 Word 文档，包括大模型生成的不规范文档
     */
    private ConvertResult convertDocxToPdf(Path docxFile, String originalFileName) {
        XWPFDocument document = null;
        try (var docxStream = Files.newInputStream(docxFile)) {
            // 1. 验证文档是否可读
            try {
                document = new XWPFDocument(docxStream);
                validateDocument(document);
            } catch (Exception e) {
                log.warn(String.format("文档验证失败，尝试修复: err=%s", e.getMessage()));
                // 如果第一次读取失败，尝试重新读取（可能是流位置问题）
                try {
                    docxStream.close();
                    document = new XWPFDocument(Files.newInputStream(docxFile));
                    validateDocument(document);
                } catch (Exception e2) {
                    log.error(String.format("文档验证和修复均失败: err=%s", e2.getMessage()), e2);
                    throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED,
                            "Word 文档格式异常，无法读取。请检查文档是否损坏: %s", e2.getMessage());
                }
            }

            var pdfFile = Files.createTempFile("pdf_", ".pdf");
            var pdfDoc = new PDDocument();

            try {
                // 2. 加载字体（如果失败，使用降级方案）
                PDFont font;
                try {
                    font = loadSystemFont(pdfDoc);
                } catch (Exception e) {
                    log.warn(String.format("字体加载失败，使用默认字体: err=%s", e.getMessage()));
                    // 使用 PDFBox 内置字体作为降级方案（不支持中文，但至少能显示英文）
                    font = PDType1Font.HELVETICA;
                }

                // 3. 提取段落（增强容错性）
                var paragraphs = document.getParagraphs();
                log.info(String.format("文档包含 %d 个段落", paragraphs.size()));

                // 4. 创建 PDF
                if (paragraphs.isEmpty()) {
                    log.warn("文档没有段落，创建空 PDF");
                    createEmptyPdf(pdfDoc, pdfFile);
                } else {
                    createPdfFromParagraphs(pdfDoc, pdfFile, font, paragraphs, document);
                }

                // 5. 验证生成的 PDF
                var pdfSize = Files.size(pdfFile);
                if (pdfSize == 0) {
                    throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "生成的 PDF 文件为空");
                }

                // 6. 读取 PDF 文件内容到字节数组（避免流关闭问题）
                byte[] pdfBytes;
                try (var pdfInputStream = Files.newInputStream(pdfFile)) {
                    pdfBytes = pdfInputStream.readAllBytes();
                }

                log.info(String.format("DOCX → PDF 转换成功: size=%d", pdfSize));

                // 清理临时文件
                cleanupTempFile(pdfFile);
                cleanupTempFile(docxFile);

                // 返回字节数组输入流（不会有关闭问题）
                return new ConvertResult(
                        new ByteArrayInputStream(pdfBytes),
                        changeFileExtension(originalFileName, "pdf"),
                        "application/pdf",
                        pdfSize);

            } catch (BusinessException e) {
                pdfDoc.close();
                cleanupTempFile(pdfFile);
                cleanupTempFile(docxFile);
                throw e;
            } catch (Exception e) {
                pdfDoc.close();
                cleanupTempFile(pdfFile);
                cleanupTempFile(docxFile);
                log.error(String.format("DOCX → PDF 转换过程失败: err=%s", e.getMessage()), e);
                throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED,
                        "DOCX 转 PDF 失败: %s。文档可能包含不支持的格式或内容。", e.getMessage());
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException e) {
                        log.warn(String.format("关闭 Word 文档失败: err=%s", e.getMessage()));
                    }
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("DOCX → PDF 转换失败（IO异常）", e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED,
                    "DOCX 转 PDF 失败: %s。请检查文件是否可读。", e.getMessage());
        } catch (Exception e) {
            log.error("DOCX → PDF 转换失败（未知异常）", e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED,
                    "DOCX 转 PDF 失败: %s", e.getMessage());
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
            if (docxSize == 0) {
                cleanupTempFile(docxFile);
                cleanupTempFile(pdfFile);
                throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "生成的 DOCX 文件为空");
            }

            // 读取 DOCX 文件内容到字节数组（避免流关闭问题）
            byte[] docxBytes;
            try (var docxInputStream = Files.newInputStream(docxFile)) {
                docxBytes = docxInputStream.readAllBytes();
            }

            log.info(String.format("PDF → DOCX 转换成功: size=%d", docxSize));

            // 清理临时文件
            cleanupTempFile(docxFile);
            cleanupTempFile(pdfFile);

            // 返回字节数组输入流（不会有关闭问题）
            return new ConvertResult(
                    new ByteArrayInputStream(docxBytes),
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
     * 增强容错性：即使部分段落处理失败，也继续处理其他段落
     */
    private void createPdfFromParagraphs(PDDocument pdfDoc, Path pdfFile, PDFont font,
            List<XWPFParagraph> paragraphs, XWPFDocument document) throws IOException {
        var page = new PDPage(PDRectangle.A4);
        pdfDoc.addPage(page);
        var pageHeight = page.getMediaBox().getHeight();
        var maxWidth = page.getMediaBox().getWidth() - 2 * PDF_MARGIN;

        var context = new PdfContext(pdfDoc, page, font, pageHeight, maxWidth);
        var processedCount = new int[] { 0 };
        var failedCount = new int[] { 0 };

        try {
            context.startPage();

            // 处理段落：增强容错性，单个段落失败不影响整体转换
            for (var paragraph : paragraphs) {
                try {
                    var paragraphText = extractParagraphText(paragraph);
                    if (paragraphText == null || paragraphText.isBlank()) {
                        continue;
                    }

                    // 文本换行处理
                    List<String> lines;
                    try {
                        lines = wrapText(paragraphText, font, PDF_FONT_SIZE, maxWidth);
                    } catch (Exception e) {
                        log.warn(String.format("段落文本换行失败，使用原始文本: err=%s", e.getMessage()));
                        // 降级方案：如果换行失败，直接使用原始文本（可能会超出边界，但至少能显示）
                        lines = List.of(paragraphText);
                    }

                    // 写入每一行
                    for (var line : lines) {
                        try {
                            context.writeLine(line);
                        } catch (Exception e) {
                            log.warn(String.format("写入 PDF 行失败，跳过该行: err=%s", e.getMessage()));
                            failedCount[0]++;
                        }
                    }

                    processedCount[0]++;

                } catch (Exception e) {
                    failedCount[0]++;
                    log.warn(String.format("处理段落失败，跳过该段落（第 %d 个段落）: err=%s",
                            paragraphs.indexOf(paragraph) + 1, e.getMessage()));
                    // 继续处理下一个段落，不中断整个转换过程
                }
            }

            // 如果所有段落都处理失败，至少创建一个空页面
            if (processedCount[0] == 0 && failedCount[0] > 0) {
                log.warn(String.format("所有段落处理失败（共 %d 个段落），创建空 PDF", paragraphs.size()));
                context.writeLine("（文档内容无法正确解析）");
            }

            context.endPage();
        } catch (Exception e) {
            log.error(String.format("创建 PDF 过程失败: err=%s", e.getMessage()), e);
            throw new IOException("创建 PDF 失败: " + e.getMessage(), e);
        } finally {
            context.close();
        }

        // 保存 PDF
        try {
            pdfDoc.save(pdfFile.toFile());
            log.info(String.format("PDF 创建完成: 成功处理 %d 个段落，失败 %d 个段落",
                    processedCount[0], failedCount[0]));
        } catch (Exception e) {
            log.error(String.format("保存 PDF 文件失败: err=%s", e.getMessage()), e);
            throw new IOException("保存 PDF 文件失败: " + e.getMessage(), e);
        }
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
     * 增强容错性：处理各种边界情况，包括空 run、异常 run、特殊字符等
     */
    private String extractParagraphText(XWPFParagraph paragraph) {
        if (paragraph == null) {
            return "";
        }

        try {
            var runs = paragraph.getRuns();
            if (runs == null || runs.isEmpty()) {
                return "";
            }

            return runs.stream()
                    .filter(run -> run != null)
                    .<String>map(run -> {
                        try {
                            // 尝试获取文本（可能抛出异常）
                            var text = run.getText(0);
                            return text != null ? text : "";
                        } catch (Exception e) {
                            // 如果获取文本失败，记录日志并返回空字符串
                            log.debug(String.format("提取 run 文本失败: err=%s", e.getMessage()));
                            return "";
                        }
                    })
                    .filter(text -> !text.isEmpty())
                    .collect(java.util.stream.Collectors.joining());
        } catch (Exception e) {
            log.warn(String.format("提取段落文本失败: err=%s", e.getMessage()));
            return "";
        }
    }

    /**
     * 验证文档是否可读
     * 检查文档的基本结构是否完整
     */
    private void validateDocument(XWPFDocument document) {
        if (document == null) {
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED, "文档对象为空");
        }

        try {
            // 尝试获取段落列表（如果文档损坏，这里可能会抛出异常）
            var paragraphs = document.getParagraphs();
            if (paragraphs == null) {
                log.warn("文档段落列表为 null，但继续处理");
            }

            // 尝试获取文档属性（验证文档结构）
            var properties = document.getProperties();
            if (properties == null) {
                log.warn("文档属性为 null，但继续处理");
            }

            log.debug("文档验证通过");
        } catch (Exception e) {
            log.error(String.format("文档验证失败: err=%s", e.getMessage()), e);
            throw BusinessException.of(ResultCode.FILE_CONVERT_FAILED,
                    "文档格式异常，无法读取: %s", e.getMessage());
        }
    }

    /**
     * 文本换行处理（支持中英文混合）
     * 确保文本不会超出右边距
     * 当字体不支持某个字符时，使用替换字符（?）确保转换继续进行
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
                            } catch (IllegalArgumentException e) {
                                // 字体不支持该字符（如：U+2021 †），使用替换字符
                                log.warn(String.format("字体不支持字符 U+%04X (%c)，使用替换字符 '?'", (int) ch, ch));
                                var newCurrentLine = new StringBuilder(state.currentLine());
                                newCurrentLine.append('?'); // 使用 '?' 替换不支持的字符
                                return new WrapState(state.lines(), newCurrentLine);
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

    @Override
    public ConvertResult convertDocToDocx(InputStream docInputStream, String originalFileName) {
        if (docInputStream == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "doc 文件流不能为空");
        }
        try {
            byte[] docBytes = docInputStream.readAllBytes();
            String text;
            try (var docStream = new ByteArrayInputStream(docBytes);
                 var doc = new HWPFDocument(docStream);
                 var extractor = new WordExtractor(doc)) {
                text = extractor.getText();
            }
            if (text == null) {
                text = "";
            }
            try (var docxDoc = new XWPFDocument()) {
                createDocxFromText(docxDoc, text);
                var out = new ByteArrayOutputStream();
                docxDoc.write(out);
                byte[] docxBytes = out.toByteArray();
                String docxFileName = changeFileExtension(originalFileName, "docx");
                log.info(String.format("doc 转 docx 成功: original=%s, docxSize=%d", originalFileName, docxBytes.length));
                return new ConvertResult(
                        new ByteArrayInputStream(docxBytes),
                        docxFileName,
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        (long) docxBytes.length);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn(String.format("doc 转 docx 失败: fileName=%s, err=%s", originalFileName, e.getMessage()), e);
            throw BusinessException.of(ResultCode.SERVER_ERROR, "doc 文件转换失败，请检查文件是否损坏或使用 docx 格式上传: %s", e.getMessage());
        }
    }
}
