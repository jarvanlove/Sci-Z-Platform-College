package com.server.agentbackendservices.modules.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.server.agentbackendservices.modules.common.enums.StatusCodeEnum;
import com.server.agentbackendservices.modules.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResultVO<Void> handleNotLoginException(NotLoginException e) {
        log.warn("Sa-Token未登录异常: {}", e.getMessage());
        return ResultVO.fail(StatusCodeEnum.NO_LOGIN.getCode(), "请先登录");
    }

    /**
     * 处理 Sa-Token 权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResultVO<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ResultVO.fail(StatusCodeEnum.AUTHORIZED.getCode(), "权限不足，无法访问");
    }

    /**
     * 处理 Sa-Token 角色不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResultVO<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足: {}", e.getMessage());
        return ResultVO.fail(StatusCodeEnum.AUTHORIZED.getCode(), "角色权限不足，无法访问");
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ResultVO.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<Void> handleValidationException(MethodArgumentNotValidException e) {
        log.error("参数校验异常: {}", e.getMessage(), e);
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            errorMsg.append(fieldError.getDefaultMessage()).append("; ");
        }
        return ResultVO.fail(StatusCodeEnum.VALID_ERROR.getCode(), errorMsg.toString());
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<Void> handleBindException(BindException e) {
        log.error("参数绑定异常: {}", e.getMessage(), e);
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            errorMsg.append(fieldError.getDefaultMessage()).append("; ");
        }
        return ResultVO.fail(StatusCodeEnum.VALID_ERROR.getCode(), errorMsg.toString());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResultVO.fail(StatusCodeEnum.SYSTEM_ERROR.getCode(), "系统异常，请联系管理员");
    }
}
