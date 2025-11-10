package com.sciz.server.infrastructure.shared.utils;

import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * 文件工具类
 *
 * @author JiaWen.Wu
 * @className FileUtils
 * @date 2025-10-29 10:30
 */
@Slf4j
public class FileUtil {

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getFileExtension(String fileName) {
        return Optional.ofNullable(fileName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> {
                    int lastDotIndex = name.lastIndexOf('.');
                    if (lastDotIndex == -1 || lastDotIndex == name.length() - 1) {
                        return "";
                    }
                    return name.substring(lastDotIndex + 1).toLowerCase();
                })
                .orElse("");
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param fileName 文件名
     * @return 不带扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        return Optional.ofNullable(fileName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> {
                    int lastDotIndex = name.lastIndexOf('.');
                    return lastDotIndex == -1 ? name : name.substring(0, lastDotIndex);
                })
                .orElse("");
    }

    /**
     * 检查文件类型是否支持
     *
     * @param fileName 文件名
     * @return 是否支持
     */
    public static boolean isSupportedFileType(String fileName) {
        String extension = getFileExtension(fileName);
        return Arrays.asList(SystemConstant.SUPPORTED_FILE_TYPES).contains(extension);
    }

    /**
     * 检查文件大小是否超出限制
     *
     * @param fileSize 文件大小
     * @return 是否超出限制
     */
    public static boolean isFileSizeExceeded(Long fileSize) {
        return Optional.ofNullable(fileSize)
                .map(size -> size > SystemConstant.MAX_FILE_SIZE)
                .orElse(false);
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (extension.isEmpty()) {
            return uuid;
        }
        return uuid + "." + extension;
    }

    /**
     * 创建目录
     *
     * @param dirPath 目录路径
     * @return 是否创建成功
     */
    public static boolean createDirectory(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("目录创建成功: {}", dirPath);
                return true;
            }
            return true;
        } catch (IOException e) {
            log.error("目录创建失败: {}", dirPath, e);
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: {}", filePath);
                return true;
            }
            return true;
        } catch (IOException e) {
            log.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 获取文件大小
     *
     * @param filePath 文件路径
     * @return 文件大小（字节）
     */
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("获取文件大小失败: {}", filePath, e);
            return 0;
        }
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取文件MIME类型
     *
     * @param fileName 文件名
     * @return MIME类型
     */
    public static String getMimeType(String fileName) {
        try {
            Path path = Paths.get(fileName);
            return Files.probeContentType(path);
        } catch (IOException e) {
            log.error("获取MIME类型失败: {}", fileName, e);
            return "application/octet-stream";
        }
    }

    /**
     * 验证文件路径
     *
     * @param filePath 文件路径
     * @return 是否有效
     */
    public static boolean isValidFilePath(String filePath) {
        return Optional.ofNullable(filePath)
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .map(value -> {
                    try {
                        Path path = Paths.get(value);
                        return path.isAbsolute() || path.normalize().equals(path);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    /**
     * 获取文件父目录
     *
     * @param filePath 文件路径
     * @return 父目录路径
     */
    public static String getParentDirectory(String filePath) {
        return Optional.ofNullable(filePath)
                .map(Paths::get)
                .map(Path::getParent)
                .map(Path::toString)
                .orElse(null);
    }

    /**
     * 合并路径
     *
     * @param basePath     基础路径
     * @param relativePath 相对路径
     * @return 合并后的路径
     */
    public static String combinePath(String basePath, String relativePath) {
        return Paths.get(basePath, relativePath).toString();
    }

    /**
     * 获取临时文件路径
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 临时文件路径
     */
    public static String getTempFilePath(String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            log.error("创建临时文件失败", e);
            return null;
        }
    }
}
