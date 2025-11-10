package com.sciz.server.infrastructure.shared.result;

/**
 * 统一结果码枚举
 * 包含系统级错误码和业务级错误码
 *
 * @author JiaWen.Wu
 * @className ResultCode
 * @date 2025-10-29 10:30
 */
public enum ResultCode {

    // ==================== 系统级错误码 (HTTP状态码) ====================
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权访问"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求方法不允许
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 服务器内部错误
     */
    SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // ==================== 业务级错误码 (1000-9999) ====================
    /**
     * 业务处理失败
     */
    BUSINESS_ERROR(1000, "业务处理失败"),

    /**
     * 数据验证失败
     */
    VALIDATION_ERROR(1001, "数据验证失败"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(1002, "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(1003, "数据已存在"),

    /**
     * 操作失败
     */
    OPERATION_FAILED(1004, "操作失败"),

    // ==================== 用户相关错误码 (2000-2999) ====================
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(2001, "用户不存在"),

    /**
     * 用户已存在
     */
    USER_ALREADY_EXISTS(2002, "用户已存在"),

    /**
     * 用户名或密码错误
     */
    USER_LOGIN_FAILED(2003, "用户名或密码错误"),

    /**
     * 用户账户已锁定
     */
    USER_ACCOUNT_LOCKED(2004, "用户账户已锁定"),

    /**
     * 用户账户已禁用
     */
    USER_ACCOUNT_DISABLED(2005, "用户账户已禁用"),

    /**
     * 用户权限不足
     */
    USER_PERMISSION_DENIED(2006, "用户权限不足"),

    /**
     * 登录成功
     */
    USER_LOGIN_SUCCESS(2100, "登录成功"),

    /**
     * 注册成功
     */
    USER_REGISTER_SUCCESS(2101, "注册成功"),

    /**
     * 退出成功
     */
    USER_LOGOUT_SUCCESS(2102, "退出成功"),

    // ==================== 项目相关错误码 (3000-3999) ====================
    /**
     * 项目不存在
     */
    PROJECT_NOT_FOUND(3001, "项目不存在"),

    /**
     * 项目已存在
     */
    PROJECT_ALREADY_EXISTS(3002, "项目已存在"),

    /**
     * 项目状态错误
     */
    PROJECT_STATUS_ERROR(3003, "项目状态错误"),

    /**
     * 项目权限不足
     */
    PROJECT_PERMISSION_DENIED(3004, "项目权限不足"),

    /**
     * 项目创建成功
     */
    PROJECT_CREATE_SUCCESS(3100, "项目创建成功"),

    /**
     * 项目更新成功
     */
    PROJECT_UPDATE_SUCCESS(3101, "项目更新成功"),

    /**
     * 项目删除成功
     */
    PROJECT_DELETE_SUCCESS(3102, "项目删除成功"),

    /**
     * 项目状态更新成功
     */
    PROJECT_STATUS_UPDATE_SUCCESS(3103, "项目状态更新成功"),

    // ==================== 申报相关错误码 (4000-4999) ====================
    /**
     * 申报不存在
     */
    DECLARATION_NOT_FOUND(4001, "申报不存在"),

    /**
     * 申报状态错误
     */
    DECLARATION_STATUS_ERROR(4002, "申报状态错误"),

    /**
     * 申报权限不足
     */
    DECLARATION_PERMISSION_DENIED(4003, "申报权限不足"),

    /**
     * 申报提交成功
     */
    DECLARATION_SUBMIT_SUCCESS(4100, "申报提交成功"),

    /**
     * 申报更新成功
     */
    DECLARATION_UPDATE_SUCCESS(4101, "申报更新成功"),

    /**
     * 申报删除成功
     */
    DECLARATION_DELETE_SUCCESS(4102, "申报删除成功"),

    /**
     * 申报审批成功
     */
    DECLARATION_APPROVE_SUCCESS(4103, "申报审批成功"),

    /**
     * 申报驳回成功
     */
    DECLARATION_REJECT_SUCCESS(4104, "申报驳回成功"),

    // ==================== 报告相关错误码 (5000-5999) ====================
    /**
     * 报告不存在
     */
    REPORT_NOT_FOUND(5001, "报告不存在"),

    /**
     * 报告生成成功
     */
    REPORT_GENERATE_SUCCESS(5100, "报告生成成功"),

    /**
     * 报告更新成功
     */
    REPORT_UPDATE_SUCCESS(5101, "报告更新成功"),

    /**
     * 报告删除成功
     */
    REPORT_DELETE_SUCCESS(5102, "报告删除成功"),

    // ==================== 知识库相关错误码 (6000-6999) ====================
    /**
     * 知识库不存在
     */
    KNOWLEDGE_NOT_FOUND(6001, "知识库不存在"),

    /**
     * 知识库创建成功
     */
    KNOWLEDGE_CREATE_SUCCESS(6100, "知识库创建成功"),

    /**
     * 知识库更新成功
     */
    KNOWLEDGE_UPDATE_SUCCESS(6101, "知识库更新成功"),

    /**
     * 知识库删除成功
     */
    KNOWLEDGE_DELETE_SUCCESS(6102, "知识库删除成功"),

    /**
     * 知识库文件上传成功
     */
    KNOWLEDGE_FILE_UPLOAD_SUCCESS(6103, "文件上传成功"),

    /**
     * 知识库文件删除成功
     */
    KNOWLEDGE_FILE_DELETE_SUCCESS(6104, "文件删除成功"),

    // ==================== 对话相关错误码 (7000-7999) ====================
    /**
     * 对话不存在
     */
    CONVERSATION_NOT_FOUND(7001, "对话不存在"),

    /**
     * 对话创建成功
     */
    CONVERSATION_CREATE_SUCCESS(7100, "对话创建成功"),

    /**
     * 对话更新成功
     */
    CONVERSATION_UPDATE_SUCCESS(7101, "对话更新成功"),

    /**
     * 对话删除成功
     */
    CONVERSATION_DELETE_SUCCESS(7102, "对话删除成功"),

    /**
     * 消息发送成功
     */
    MESSAGE_SEND_SUCCESS(7103, "消息发送成功"),

    // ==================== 文件相关错误码 (8000-8999) ====================
    /**
     * 文件不存在
     */
    FILE_NOT_FOUND(8001, "文件不存在"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(8002, "文件上传失败"),

    /**
     * 文件下载失败
     */
    FILE_DOWNLOAD_FAILED(8003, "文件下载失败"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORTED(8004, "文件类型不支持"),

    /**
     * 文件大小超出限制
     */
    FILE_SIZE_EXCEEDED(8005, "文件大小超出限制"),

    /**
     * 文件上传成功
     */
    FILE_UPLOAD_SUCCESS(8100, "文件上传成功"),

    /**
     * 文件下载成功
     */
    FILE_DOWNLOAD_SUCCESS(8101, "文件下载成功"),

    /**
     * 文件删除成功
     */
    FILE_DELETE_SUCCESS(8102, "文件删除成功"),

    // ==================== 验证相关错误码 (9000-9999) ====================
    /**
     * 需要验证码
     */
    CAPTCHA_REQUIRED(9000, "需要验证码"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(9001, "验证码错误"),

    /**
     * 验证码无效
     */
    CAPTCHA_INVALID(9001, "验证码无效"),

    /**
     * 验证码已过期
     */
    CAPTCHA_EXPIRED(9002, "验证码已过期"),

    /**
     * 邮箱未注册
     */
    EMAIL_NOT_FOUND(9005, "邮箱未注册"),

    /**
     * 邮箱验证码发送频繁
     */
    EMAIL_CODE_RATE_LIMITED(9006, "验证码发送频繁，请稍后再试"),

    /**
     * 邮箱验证码错误
     */
    EMAIL_CODE_INVALID(9007, "邮箱验证码错误"),

    /**
     * 邮箱验证码已过期
     */
    EMAIL_CODE_EXPIRED(9008, "邮箱验证码已过期"),

    /**
     * 邮箱验证码发送成功
     */
    EMAIL_CODE_SEND_SUCCESS(9100, "验证码发送成功"),

    /**
     * 重置密码成功
     */
    RESET_PASSWORD_SUCCESS(9101, "重置密码成功"),

    /**
     * 令牌已过期
     */
    TOKEN_EXPIRED(9003, "令牌已过期"),

    /**
     * 令牌无效
     */
    TOKEN_INVALID(9004, "令牌无效"),

    // ==================== 外部服务相关错误码 (10000-10999) ====================
    /**
     * 外部服务调用失败
     */
    EXTERNAL_SERVICE_ERROR(10001, "外部服务调用失败"),

    /**
     * 外部服务超时
     */
    EXTERNAL_SERVICE_TIMEOUT(10002, "外部服务超时"),

    /**
     * 外部服务不可用
     */
    EXTERNAL_SERVICE_UNAVAILABLE(10003, "外部服务不可用"),

    // ==================== 缓存相关错误码 (11000-11999) ====================
    /**
     * 缓存操作失败
     */
    CACHE_OPERATION_FAILED(11001, "缓存操作失败"),

    /**
     * 缓存连接失败
     */
    CACHE_CONNECTION_FAILED(11002, "缓存连接失败"),

    // ==================== 数据库相关错误码 (12000-12999) ====================
    /**
     * 数据库操作失败
     */
    DATABASE_OPERATION_FAILED(12001, "数据库操作失败"),

    /**
     * 数据库连接失败
     */
    DATABASE_CONNECTION_FAILED(12002, "数据库连接失败"),

    /**
     * 数据重复
     */
    DATABASE_DUPLICATE_KEY(12003, "数据重复"),

    /**
     * 数据约束违反
     */
    DATABASE_CONSTRAINT_VIOLATION(12004, "数据约束违反");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return ResultCode
     */
    public static ResultCode fromCode(int code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode() == code) {
                return resultCode;
            }
        }
        return SERVER_ERROR;
    }

    /**
     * 判断是否为成功状态
     *
     * @return 是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为客户端错误
     *
     * @return 是否为客户端错误
     */
    public boolean isClientError() {
        return this.code >= 400 && this.code < 500;
    }

    /**
     * 判断是否为服务器错误
     *
     * @return 是否为服务器错误
     */
    public boolean isServerError() {
        return this.code >= 500;
    }

    /**
     * 判断是否为业务错误
     *
     * @return 是否为业务错误
     */
    public boolean isBusinessError() {
        return this.code >= 1000 && this.code < 10000;
    }
}
