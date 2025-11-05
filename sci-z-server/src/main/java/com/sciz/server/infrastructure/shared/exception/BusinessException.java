package com.sciz.server.infrastructure.shared.exception;

import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.Getter;

/**
 * 统一业务异常类
 * 
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

    // ==================== 工具方法 ====================

    /**
     * 是否为客户端错误（4xx）
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * 是否为服务器错误（5xx）
     */
    public boolean isServerError() {
        return code >= 500;
    }

    /**
     * 是否为业务错误（1000-9999）
     */
    public boolean isBusinessError() {
        return code >= 1000 && code < 10000;
    }

    @Override
    public String toString() {
        return String.format("BusinessException{code=%d, message='%s'}", code, message);
    }
}
