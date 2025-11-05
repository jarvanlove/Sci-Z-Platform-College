package com.sciz.server.infrastructure.shared.result;

import com.sciz.server.infrastructure.shared.utils.TraceIdUtil;

/**
 * @author JiaWen.Wu
 * @className Result
 * @date 2025-10-28 00:00
 */
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    private String traceId;

    /**
     * 构建成功结果（无数据）
     *
     * @return Result
     */
    public static <T> Result<T> success() {
        return success(null, ResultCode.SUCCESS.getMessage());
    }

    /**
     * 构建成功结果（带消息）
     *
     * @param message 成功消息
     * @return Result
     */
    public static <T> Result<T> success(String message) {
        return success(null, message);
    }

    /**
     * 构建成功结果（带数据）
     *
     * @param data 响应数据
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return success(data, ResultCode.SUCCESS.getMessage());
    }

    /**
     * 构建成功结果（带数据和消息）
     *
     * @param data    响应数据
     * @param message 成功消息
     * @return Result
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = message;
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        r.traceId = TraceIdUtil.getTraceId();
        return r;
    }

    /**
     * 构建失败结果（使用错误码枚举）
     *
     * @param resultCode 错误码枚举
     * @return Result
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 构建失败结果（使用错误码枚举和自定义消息）
     *
     * @param resultCode 错误码枚举
     * @param message    自定义错误消息
     * @return Result
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return fail(resultCode.getCode(), message);
    }

    /**
     * 构建失败结果（使用默认错误码）
     *
     * @param message 错误消息
     * @return Result
     */
    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.SERVER_ERROR.getCode(), message);
    }

    /**
     * 构建失败结果（使用自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return Result
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.timestamp = System.currentTimeMillis();
        r.traceId = TraceIdUtil.getTraceId();
        return r;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
