package com.sciz.server.infrastructure.shared.constant;

/**
 * 系统常量
 *
 * @author JiaWen.Wu
 * @className SystemConstant
 * @date 2025-10-29 10:30
 */
public class SystemConstant {

    /**
     * 系统名称
     */
    public static final String SYSTEM_NAME = "Sci-Z-Platform";

    /**
     * 系统版本
     */
    public static final String SYSTEM_VERSION = "1.0.0";

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认超时时间（毫秒）
     */
    public static final Long DEFAULT_TIMEOUT = 30000L;

    /**
     * 默认重试次数
     */
    public static final Integer DEFAULT_RETRY_COUNT = 3;

    /**
     * 默认重试间隔（毫秒）
     */
    public static final Long DEFAULT_RETRY_INTERVAL = 1000L;

    /**
     * 默认缓存过期时间（秒）
     */
    public static final Long DEFAULT_CACHE_EXPIRE = 3600L;

    /**
     * 用户会话过期时间（秒）
     */
    public static final Long USER_SESSION_EXPIRE = 7200L;

    /**
     * JWT令牌过期时间（秒）
     */
    public static final Long JWT_TOKEN_EXPIRE = 86400L;

    /**
     * 刷新令牌过期时间（秒）
     */
    public static final Long REFRESH_TOKEN_EXPIRE = 604800L;

    /**
     * 文件上传最大大小（字节）
     */
    public static final Long MAX_FILE_SIZE = 10485760L; // 10MB

    /**
     * 支持的文件类型
     */
    public static final String[] SUPPORTED_FILE_TYPES = {
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md"
    };

    /**
     * 默认用户角色编码
     */
    public static final String DEFAULT_USER_ROLE_CODE = "user";

    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 重置密码邮件主题
     */
    public static final String EMAIL_SUBJECT_RESET_PASSWORD = "Sci-Z Platform 账号重置验证码";

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";
}
