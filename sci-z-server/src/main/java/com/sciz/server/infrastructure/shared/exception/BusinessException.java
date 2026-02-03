package com.sciz.server.infrastructure.shared.exception;

import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.Getter;

/**
 * 统一业务异常类
 * 
 * <p>
 * 用于统一处理业务异常，提供错误码和错误消息，便于前端统一处理和展示。
 * </p>
 *
 * @author JiaWen.Wu
 * @className BusinessException
 * @date 2025-10-29 10:00
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    // ==================== 构造函数 ====================

    /**
     * 使用 ResultCode 创建异常
     *
     * @param resultCode 结果码
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 使用 ResultCode 和自定义消息创建异常
     *
     * @param resultCode 结果码
     * @param message    自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    /**
     * 使用自定义错误码和消息创建异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 使用 ResultCode 创建异常（静态工厂方法）
     *
     * @param resultCode 结果码
     * @return BusinessException 业务异常
     */
    public static BusinessException of(ResultCode resultCode) {
        return new BusinessException(resultCode);
    }

    /**
     * 使用 ResultCode 和自定义消息创建异常（静态工厂方法）
     *
     * @param resultCode 结果码
     * @param message    自定义错误消息
     * @return BusinessException 业务异常
     */
    public static BusinessException of(ResultCode resultCode, String message) {
        return new BusinessException(resultCode, message);
    }

    /**
     * 使用 ResultCode 和格式化消息创建异常（静态工厂方法）
     *
     * @param resultCode 结果码
     * @param template   消息模板（支持 %s, %d 等占位符）
     * @param args       参数
     * @return BusinessException 业务异常
     */
    public static BusinessException of(ResultCode resultCode, String template, Object... args) {
        var message = String.format(template, args);
        return new BusinessException(resultCode, message);
    }

    // ==================== 工具方法 ====================

    /**
     * 是否为客户端错误（4xx 或业务错误码 1000-9999）
     * 
     * <p>
     * 业务错误码（1000-9999）通常表示用户输入或操作导致的错误，
     * 应该返回 400 状态码而不是 500，以便前端正确显示错误信息。
     * </p>
     *
     * @return boolean 是否为客户端错误
     */
    public boolean isClientError() {
        // HTTP 4xx 错误码或业务错误码（1000-9999）都视为客户端错误
        return (code >= 400 && code < 500) || (code >= 1000 && code < 10000);
    }

    /**
     * 是否为服务器错误（5xx）
     *
     * @return boolean 是否为服务器错误
     */
    public boolean isServerError() {
        return code >= 500;
    }

    /**
     * 是否为业务错误（1000-9999）
     *
     * @return boolean 是否为业务错误
     */
    public boolean isBusinessError() {
        return code >= 1000 && code < 10000;
    }

    /**
     * 获取错误类型描述
     *
     * @return String 错误类型描述
     */
    public String getErrorType() {
        if (isClientError()) {
            return "客户端错误";
        } else if (isServerError()) {
            return "服务器错误";
        } else if (isBusinessError()) {
            return "业务错误";
        } else {
            return "未知错误";
        }
    }

    @Override
    public String toString() {
        return """
                BusinessException{
                    code=%d,
                    message='%s',
                    errorType='%s'
                }
                """.formatted(code, message, getErrorType());
    }
}
