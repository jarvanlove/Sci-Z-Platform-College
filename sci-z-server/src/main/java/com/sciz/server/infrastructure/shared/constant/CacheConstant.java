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

    // ==================== 认证相关缓存常量 ====================
    /**
     * 认证失败记录缓存前缀（用于记录登录失败次数）
     * 格式：auth:fail:{username}
     */
    public static final String AUTH_FAIL_KEY = "auth:fail:%s";

    /**
     * 账号锁定缓存前缀（用于锁定登录失败超过阈值的账号）
     * 格式：auth:lock:{username}
     */
    public static final String AUTH_LOCK_KEY = "auth:lock:%s";

    /**
     * 认证令牌缓存前缀（用于存储用户登录token）
     * 格式：auth:token:{userId}
     */
    public static final String AUTH_TOKEN_KEY = "auth:token:%s";

    /**
     * 最大登录失败次数（超过此次数将锁定账号）
     */
    public static final int MAX_LOGIN_FAIL_COUNT = 6;

    /**
     * 账号锁定时长（秒）
     * 默认：30分钟
     */
    public static final Long AUTH_LOCK_DURATION = 30L * 60L;

    // ==================== 权限相关缓存常量 ====================
    /**
     * 权限缓存命名空间前缀
     * 格式：sciz:auth:perm:{userId}:{industryType}:{type}
     * type 可以是：roles、permissions、menus
     */
    public static final String PERMISSION_CACHE_NAMESPACE = "sciz:auth:perm";

    /**
     * 权限缓存过期时间（秒）
     * 默认：30分钟
     */
    public static final Long PERMISSION_CACHE_EXPIRE = 30L * 60L;

    // ==================== 验证码相关缓存常量 ====================
    /**
     * 验证码缓存前缀
     * 格式：captcha:{captchaKey}
     */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:%s";

    /**
     * 验证码过期时间（秒）
     * 默认：5分钟
     */
    public static final Long CAPTCHA_EXPIRE = 5L * 60L;

    /**
     * 需要验证码的登录失败次数阈值
     */
    public static final int CAPTCHA_REQUIRED_FAIL_COUNT = 5;

    // ==================== 行业配置相关缓存常量 ====================
    /**
     * 行业配置缓存命名空间
     * 格式：sciz:cfg:industry
     */
    public static final String INDUSTRY_CONFIG_NAMESPACE = "sciz:cfg:industry";

    /**
     * 当前行业整体配置缓存键
     * 格式：sciz:cfg:industry:current
     */
    public static final String INDUSTRY_CONFIG_CURRENT_KEY = INDUSTRY_CONFIG_NAMESPACE + ":current";

    /**
     * 行业配置缓存过期时间（秒）
     * 默认：1天
     */
    public static final Long INDUSTRY_CONFIG_CACHE_EXPIRE = 24L * 60L * 60L;

    // ==================== 系统配置键常量 ====================
    /**
     * 当前行业类型配置键
     */
    public static final String CONFIG_KEY_INDUSTRY_TYPE = "current_industry_type";

    /**
     * 当前行业名称配置键
     */
    public static final String CONFIG_KEY_INDUSTRY_NAME = "current_industry_name";

    /**
     * 部门标签配置键
     */
    public static final String CONFIG_KEY_LABEL_DEPT = "label.department";

    /**
     * 角色标签配置键
     */
    public static final String CONFIG_KEY_LABEL_ROLE = "label.role";

    /**
     * 员工ID标签配置键
     */
    public static final String CONFIG_KEY_LABEL_EMP = "label.employee_id";
}
