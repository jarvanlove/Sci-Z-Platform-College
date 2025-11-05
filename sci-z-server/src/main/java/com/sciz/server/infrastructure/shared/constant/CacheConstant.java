package com.sciz.server.infrastructure.shared.constant;

/**
 * 缓存常量
 *
 * @author JiaWen.Wu
 * @className CacheConstant
 * @date 2025-10-29 10:30
 */
public class CacheConstant {

    /**
     * 用户缓存前缀
     */
    public static final String USER_CACHE_PREFIX = "user:";

    /**
     * 用户会话缓存前缀
     */
    public static final String USER_SESSION_PREFIX = "session:";

    /**
     * 项目缓存前缀
     */
    public static final String PROJECT_CACHE_PREFIX = "project:";

    /**
     * 申报缓存前缀
     */
    public static final String DECLARATION_CACHE_PREFIX = "declaration:";

    /**
     * 报告缓存前缀
     */
    public static final String REPORT_CACHE_PREFIX = "report:";

    /**
     * 知识库缓存前缀
     */
    public static final String KNOWLEDGE_CACHE_PREFIX = "knowledge:";

    /**
     * 对话缓存前缀
     */
    public static final String CONVERSATION_CACHE_PREFIX = "conversation:";

    /**
     * 权限缓存前缀
     */
    public static final String PERMISSION_CACHE_PREFIX = "permission:";

    /**
     * 角色缓存前缀
     */
    public static final String ROLE_CACHE_PREFIX = "role:";

    /**
     * 系统配置缓存前缀
     */
    public static final String CONFIG_CACHE_PREFIX = "config:";

    /**
     * 验证码缓存前缀
     */
    public static final String CAPTCHA_CACHE_PREFIX = "captcha:";

    /**
     * 限流缓存前缀
     */
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * 分布式锁前缀
     */
    public static final String LOCK_PREFIX = "lock:";

    /**
     * 默认缓存过期时间（秒）
     */
    public static final Long DEFAULT_EXPIRE_TIME = 3600L;

    /**
     * 用户缓存过期时间（秒）
     */
    public static final Long USER_CACHE_EXPIRE = 7200L;

    /**
     * 会话缓存过期时间（秒）
     */
    public static final Long SESSION_CACHE_EXPIRE = 1800L;

    /**
     * 验证码缓存过期时间（秒）
     */
    public static final Long CAPTCHA_CACHE_EXPIRE = 300L;

    /**
     * 限流缓存过期时间（秒）
     */
    public static final Long RATE_LIMIT_CACHE_EXPIRE = 60L;
}
