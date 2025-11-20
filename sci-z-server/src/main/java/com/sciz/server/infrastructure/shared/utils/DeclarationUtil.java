package com.sciz.server.infrastructure.shared.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.sciz.server.infrastructure.shared.constant.SystemConstant;

/**
 * 申报工具类
 *
 * @author JiaWen.Wu
 * @className DeclarationUtil
 * @date 2025-01-20 15:00
 */
public final class DeclarationUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private DeclarationUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 申报编号前缀
     */
    private static final String DECLARATION_NUMBER_PREFIX = "NSFC";

    /**
     * 时间戳格式化器（年月日时分秒）
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern(SystemConstant.TIMESTAMP_FORMATTER);

    /**
     * 生成申报编号
     * 格式：NSFC + 年月日时分秒（时间戳）
     * 示例：NSFC20250115143025
     *
     * @return 申报编号
     */
    public static String generateDeclarationNumber() {
        var timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return DECLARATION_NUMBER_PREFIX + timestamp;
    }

    /**
     * 生成申报编号（指定时间）
     * 格式：NSFC + 年月日时分秒（时间戳）
     *
     * @param dateTime 指定时间
     * @return 申报编号
     */
    public static String generateDeclarationNumber(LocalDateTime dateTime) {
        var timestamp = dateTime.format(TIMESTAMP_FORMATTER);
        return DECLARATION_NUMBER_PREFIX + timestamp;
    }
}
