package com.sciz.server.infrastructure.shared.utils;

import com.sciz.server.infrastructure.shared.constant.SystemConstant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.Date;

/**
 * 日期时间工具类
 * 基于 Java 21 的 java.time API，提供常用的日期时间操作方法
 *
 * @author JiaWen.Wu
 * @className DateUtil
 * @date 2025-10-29 10:30
 */
public final class DateUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private DateUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 默认日期时间格式化器
     */
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter
            .ofPattern(SystemConstant.DATE_TIME_FORMAT);

    /**
     * 默认日期格式化器
     */
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter
            .ofPattern(SystemConstant.DATE_FORMAT);

    /**
     * 默认时间格式化器
     */
    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter
            .ofPattern(SystemConstant.TIME_FORMAT);

    /**
     * 获取当前日期时间
     *
     * @return 当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public static LocalTime currentTime() {
        return LocalTime.now();
    }

    /**
     * 格式化日期时间为字符串
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 格式化日期为字符串
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate date) {
        return date.format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * 格式化时间为字符串
     *
     * @param time 时间
     * @return 格式化后的字符串
     */
    public static String formatTime(LocalTime time) {
        return time.format(DEFAULT_TIME_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @return 日期时间
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return 日期
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DEFAULT_DATE_FORMATTER);
    }

    /**
     * 解析时间字符串
     *
     * @param timeStr 时间字符串
     * @return 时间
     */
    public static LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr, DEFAULT_TIME_FORMATTER);
    }

    /**
     * 计算两个日期之间的天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的小时数
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime   结束日期时间
     * @return 小时数
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个日期时间之间的分钟数
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime   结束日期时间
     * @return 分钟数
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个日期时间之间的秒数
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime   结束日期时间
     * @return 秒数
     */
    public static long secondsBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.SECONDS.between(startDateTime, endDateTime);
    }

    /**
     * 添加天数
     *
     * @param date 日期
     * @param days 天数
     * @return 新日期
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /**
     * 添加小时
     *
     * @param dateTime 日期时间
     * @param hours    小时数
     * @return 新日期时间
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    /**
     * 添加分钟
     *
     * @param dateTime 日期时间
     * @param minutes  分钟数
     * @return 新日期时间
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    /**
     * 添加秒数
     *
     * @param dateTime 日期时间
     * @param seconds  秒数
     * @return 新日期时间
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.plusSeconds(seconds);
    }

    /**
     * 减去天数
     *
     * @param date 日期
     * @param days 天数
     * @return 新日期
     */
    public static LocalDate minusDays(LocalDate date, long days) {
        return date.minusDays(days);
    }

    /**
     * 减去小时
     *
     * @param dateTime 日期时间
     * @param hours    小时数
     * @return 新日期时间
     */
    public static LocalDateTime minusHours(LocalDateTime dateTime, long hours) {
        return dateTime.minusHours(hours);
    }

    /**
     * 减去分钟
     *
     * @param dateTime 日期时间
     * @param minutes  分钟数
     * @return 新日期时间
     */
    public static LocalDateTime minusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.minusMinutes(minutes);
    }

    /**
     * 减去秒数
     *
     * @param dateTime 日期时间
     * @param seconds  秒数
     * @return 新日期时间
     */
    public static LocalDateTime minusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.minusSeconds(seconds);
    }

    /**
     * 判断是否为今天
     *
     * @param date 日期
     * @return 是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(today());
    }

    /**
     * 判断是否为昨天
     *
     * @param date 日期
     * @return 是否为昨天
     */
    public static boolean isYesterday(LocalDate date) {
        return date.equals(today().minusDays(1));
    }

    /**
     * 判断是否为明天
     *
     * @param date 日期
     * @return 是否为明天
     */
    public static boolean isTomorrow(LocalDate date) {
        return date.equals(today().plusDays(1));
    }

    /**
     * 获取一天的开始时间
     *
     * @param date 日期
     * @return 一天的开始时间
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 获取一天的结束时间
     *
     * @param date 日期
     * @return 一天的结束时间
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 转换为Date对象
     *
     * @param dateTime 日期时间
     * @return Date对象
     */
    public static Date toDate(LocalDateTime dateTime) {
        return java.sql.Timestamp.valueOf(dateTime);
    }

    /**
     * 从Date对象转换
     *
     * @param date Date对象
     * @return 日期时间
     */
    public static LocalDateTime fromDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
