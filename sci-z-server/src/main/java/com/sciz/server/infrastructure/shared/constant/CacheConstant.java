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

    // ==================== 文件转换相关缓存常量 ====================
    /**
     * 文件转换缓存前缀
     * 格式：file:convert:{md5}:{sourceFormat}:{targetFormat}:{fileName}
     */
    public static final String FILE_CONVERT_CACHE_PREFIX = "file:convert:";

    /**
     * 文件转换缓存过期时间（24小时）
     * 单位：秒
     */
    public static final Long FILE_CONVERT_CACHE_EXPIRE = 24 * 3600L;

    /**
     * 文件转换缓存目录（MinIO）
     * 用于存储转换后的文件
     */
    public static final String FILE_CONVERT_CACHE_DIR = "convert-cache/";

    /**
     * 最大转换文件大小（300MB）
     * 单位：字节
     */
    public static final Long FILE_CONVERT_MAX_SIZE = 300L * 1024 * 1024;

    /**
     * 文件转换超时时间（秒）
     * 默认：5分钟
     * 注意：当前为同步转换，超时控制需要在调用方实现（使用 CompletableFuture 或 ExecutorService 的 Future）
     * 实现示例：
     * 
     * <pre>
     * CompletableFuture&lt;ConvertResult&gt; future = CompletableFuture.supplyAsync(() -&gt; {
     *     return fileConvertService.convert(inputStream, sourceFormat, targetFormat, fileName);
     * });
     * try {
     *     ConvertResult result = future.get(CacheConstant.FILE_CONVERT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
     *     return result;
     * } catch (TimeoutException e) {
     *     future.cancel(true);
     *     throw new BusinessException(ResultCode.SERVER_ERROR, "文件转换超时");
     * }
     * </pre>
     */
    public static final int FILE_CONVERT_TIMEOUT_SECONDS = 300;

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

    // ==================== 邮箱验证码缓存常量 ====================
    /**
     * 邮箱验证码缓存键
     * 格式：auth:verification:email:{email}
     */
    public static final String AUTH_EMAIL_VERIFICATION_CODE_KEY = "auth:verification:email:%s";

    /**
     * 邮箱验证码发送频率限制键
     * 格式：auth:verification:limit:{email}
     */
    public static final String AUTH_EMAIL_VERIFICATION_RATE_LIMIT_KEY = "auth:verification:limit:%s";

    /**
     * 邮箱验证码过期时间（秒）
     * 默认：10分钟
     */
    public static final Long AUTH_EMAIL_VERIFICATION_CODE_EXPIRE = 10L * 60L;

    /**
     * 邮箱验证码发送间隔（秒）
     * 默认：60秒
     */
    public static final Long AUTH_EMAIL_VERIFICATION_CODE_INTERVAL = 60L;

    // ==================== 短信验证码缓存常量 ====================
    /**
     * 短信验证码缓存键
     * 格式：auth:verification:sms:{phone}
     */
    public static final String AUTH_SMS_VERIFICATION_CODE_KEY = "auth:verification:sms:%s";

    /**
     * 短信验证码发送频率限制键
     * 格式：auth:verification:sms:limit:{phone}
     */
    public static final String AUTH_SMS_VERIFICATION_RATE_LIMIT_KEY = "auth:verification:sms:limit:%s";

    /**
     * 短信验证码过期时间（秒）
     * 默认：10分钟
     */
    public static final Long AUTH_SMS_VERIFICATION_CODE_EXPIRE = 10L * 60L;

    /**
     * 短信验证码发送间隔（秒）
     * 默认：60秒
     */
    public static final Long AUTH_SMS_VERIFICATION_CODE_INTERVAL = 60L;

    /**
     * 短信验证码10分钟内发送次数限制键
     * 格式：auth:verification:sms:count:10min:{phone}
     */
    public static final String AUTH_SMS_VERIFICATION_COUNT_10MIN_KEY = "auth:verification:sms:count:10min:%s";

    /**
     * 短信验证码一天内发送次数限制键
     * 格式：auth:verification:sms:count:day:{phone}:{date}
     */
    public static final String AUTH_SMS_VERIFICATION_COUNT_DAY_KEY = "auth:verification:sms:count:day:%s:%s";

    /**
     * 短信验证码10分钟内最大发送次数
     * 默认：5次
     */
    public static final int AUTH_SMS_VERIFICATION_MAX_COUNT_10MIN = 5;

    /**
     * 短信验证码一天内最大发送次数
     * 默认：20次
     */
    public static final int AUTH_SMS_VERIFICATION_MAX_COUNT_DAY = 20;

    /**
     * 10分钟时间窗口（秒）
     * 与验证码有效期保持一致，用于限制10分钟内的发送次数
     */
    public static final Long AUTH_SMS_VERIFICATION_10MIN_WINDOW = 10L * 60L;

    /**
     * 一天时间窗口（秒）
     */
    public static final Long AUTH_SMS_VERIFICATION_DAY_WINDOW = 24L * 60L * 60L;

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

    // ==================== PDF缓存相关常量 ====================
    /**
     * PDF缓存前缀
     * 格式：pdf:cache:{id}
     */
    public static final String PDF_CACHE_PREFIX = "pdf:cache:";

    /**
     * PDF缓存过期时间（秒）
     * 默认：600秒（10分钟）
     */
    public static final Long PDF_CACHE_EXPIRE = 600L;

    /**
     * PDF预览URL过期时间（秒）
     * 默认：86400秒（24小时），比文件清理时间稍长
     */
    public static final Integer PDF_PREVIEW_EXPIRE_SECONDS = 86400;

    /**
     * PDF缓存文件存储目录（MinIO）
     */
    public static final String PDF_CACHE_DIR = "pdf-cache/";
}

