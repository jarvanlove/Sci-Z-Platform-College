package com.sciz.server.infrastructure.shared.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具
 * 
 * @author JiaWen.Wu
 * @className RedisUtil
 * @date 2025-10-29 17:00
 */
public final class RedisUtil {

    private RedisUtil() {
    }

    /**
     * 设置字符串 KV
     *
     * @param template StringRedisTemplate Redis模板
     * @param key      String 键
     * @param value    String 值
     * @param ttl      Duration 过期时间（可为 null）
     * @return void
     */
    public static void set(StringRedisTemplate template, String key, String value, Duration ttl) {
        if (ttl == null) {
            template.opsForValue().set(key, value);
        } else {
            template.opsForValue().set(key, value, ttl);
        }
    }

    /**
     * 获取字符串值
     *
     * @param template StringRedisTemplate Redis模板
     * @param key      String 键
     * @return String 值
     */
    public static String get(StringRedisTemplate template, String key) {
        return template.opsForValue().get(key);
    }

    /**
     * 设置字符串 KV（若不存在）
     *
     * @param template   StringRedisTemplate Redis模板
     * @param key        String 键
     * @param value      String 值
     * @param ttlSeconds long 过期秒
     * @return Boolean 是否成功
     */
    public static Boolean setIfAbsent(StringRedisTemplate template, String key, String value, long ttlSeconds) {
        return template.opsForValue().setIfAbsent(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * 删除键
     *
     * @param template StringRedisTemplate Redis模板
     * @param key      String 键
     * @return Boolean 删除结果
     */
    public static Boolean delete(StringRedisTemplate template, String key) {
        return template.delete(key);
    }

    /**
     * 自增计数
     *
     * @param template StringRedisTemplate Redis模板
     * @param key      String 键
     * @param delta    long 增量
     * @return Long 结果
     */
    public static Long incrBy(StringRedisTemplate template, String key, long delta) {
        return template.opsForValue().increment(key, delta);
    }

    /**
     * Hash 设置
     *
     * @param template  StringRedisTemplate Redis模板
     * @param key       String 键
     * @param hashKey   String 字段
     * @param hashValue String 值
     * @return void
     */
    public static void hset(StringRedisTemplate template, String key, String hashKey, String hashValue) {
        template.opsForHash().put(key, hashKey, hashValue);
    }

    /**
     * Hash 获取
     *
     * @param template StringRedisTemplate Redis模板
     * @param key      String 键
     * @param hashKey  String 字段
     * @return Object 值
     */
    public static Object hget(StringRedisTemplate template, String key, String hashKey) {
        return template.opsForHash().get(key, hashKey);
    }
}
