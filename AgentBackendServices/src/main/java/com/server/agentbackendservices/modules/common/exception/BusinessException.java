package com.server.agentbackendservices.modules.common.exception;

import com.server.agentbackendservices.modules.common.enums.StatusCodeEnum;
import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = StatusCodeEnum.FAIL.getCode();
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(StatusCodeEnum statusCodeEnum) {
        super(statusCodeEnum.getDesc());
        this.code = statusCodeEnum.getCode();
    }
    
    public BusinessException(StatusCodeEnum statusCodeEnum, String message) {
        super(message);
        this.code = statusCodeEnum.getCode();
    }
}
