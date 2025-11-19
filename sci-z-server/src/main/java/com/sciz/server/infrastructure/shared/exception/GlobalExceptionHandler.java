package com.sciz.server.infrastructure.shared.exception;

import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局异常处理器
 * 
 * 统一处理系统中的各种异常，返回标准的错误响应
 *
 * @author JiaWen.Wu
 * @className GlobalExceptionHandler
 * @date 2025-10-29 12:00
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.error(String.format("业务异常: code=%s, message=%s", String.valueOf(e.getCode()), e.getMessage()), e);

        Result<Void> result = Result.fail(e.getCode(), e.getMessage());
        result.setData(null);

        HttpStatus status = e.isClientError() ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 处理参数验证异常（@Valid 注解）
     *
     * @param e 参数验证异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn(String.format("参数验证异常: %s", e.getMessage()));

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "参数验证失败: " + errors.toString();
        Result<Void> result = Result.fail(ResultCode.VALIDATION_ERROR.getCode(), message);

        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理绑定异常
     *
     * @param e 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        log.warn(String.format("绑定异常: %s", e.getMessage()));

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "参数绑定失败: " + errors.toString();
        Result<Void> result = Result.fail(ResultCode.VALIDATION_ERROR.getCode(), message);

        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理约束违反异常
     *
     * @param e 约束违反异常
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn(String.format("约束违反异常: %s", e.getMessage()));

        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        String message = "约束验证失败: " + errors.toString();
        Result<Void> result = Result.fail(ResultCode.VALIDATION_ERROR.getCode(), message);

        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn(String.format("非法参数异常: %s", e.getMessage()));

        Result<Void> result = Result.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理空指针异常
     *
     * @param e 空指针异常
     * @return 错误响应
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNullPointerException(NullPointerException e) {
        log.error(String.format("空指针异常 err=%s", e.getMessage()), e);
        Result<Void> result = Result.fail(ResultCode.SERVER_ERROR.getCode(), "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理运行时异常
     *
     * @param e 运行时异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        log.error(String.format("运行时异常 err=%s", e.getMessage()), e);

        Result<Void> result = Result.fail(ResultCode.SERVER_ERROR.getCode(), "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e 请求方法不支持异常
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn(String.format("请求方法不支持: %s", e.getMessage()));

        String message = "请求方法 '" + e.getMethod() + "' 不支持，支持的方法: " + String.join(", ", e.getSupportedMethods());
        Result<Void> result = Result.fail(ResultCode.METHOD_NOT_ALLOWED.getCode(), message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(result);
    }

    /**
     * 处理请求参数缺失异常
     *
     * @param e 请求参数缺失异常
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn(String.format("请求参数缺失: %s", e.getMessage()));

        String message = "缺少必需的请求参数: " + e.getParameterName();
        Result<Void> result = Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e 参数类型不匹配异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn(String.format("参数类型不匹配: %s", e.getMessage()));

        String message = "参数 '" + e.getName() + "' 的值 '" + e.getValue() + "' 类型不匹配，期望类型: "
                + e.getRequiredType().getSimpleName();
        Result<Void> result = Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
        return ResponseEntity.badRequest().body(result);
    }
    /**
     * 处理请求体不可读异常
     *
     * @param e 请求体不可读异常
     * @return 错误响应
     */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn(String.format("请求体不可读: %s", e.getMessage()));

        Result<Void> result = Result.fail(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误或不可读");
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理404异常（路径不存在）
     *
     * @param e 404异常
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn(String.format("请求路径不存在: %s", e.getMessage()));

        String message = "请求路径 '" + e.getRequestURL() + "' 不存在";
        Result<Void> result = Result.fail(ResultCode.NOT_FOUND.getCode(), message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 处理其他异常
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error(String.format("未知异常 err=%s", e.getMessage()), e);

        Result<Void> result = Result.fail(ResultCode.SERVER_ERROR.getCode(), "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
